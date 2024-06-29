/*
 * Copyright (c) 2020 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Dennis Lang
 * @see https://lanDenLabs.com/
 */
package com.wsi.all_audiodemo.notify;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


/**
 *
 */
public class ManageService {
    private static final String TAG = "ManageService";

    private final Application application;
    private final NotifyService.Config config = new NotifyService.Config(
            0, 0, 0, 0, false, false, "");

    private NotifyService overlayService;

    // ---------------------------------------------------------------------------------------------
    final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "AlertService is connected");
            // We've bound to AlertService, cast the IBinder and get AlertService instance
            NotifyService.LocalBinder binder = (NotifyService.LocalBinder) service;
            overlayService = binder.getService();
            overlayService.updateNotification();
            overlayService.startTimer();
        }

        // This is called when the connection with the service has been
        // unexpectedly disconnected -- that is, its process crashed.
        // So, this is not called when the client unbinds.
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private ActivityLifecycleHandler activityLifecycleHandler;
    private boolean installed;
    private boolean unBindRequestReceived;


    // ---------------------------------------------------------------------------------------------
    final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NotifyService.ACTION_UNBIND.equals(intent.getAction())) {
                Log.d(TAG, "Notify Service unbind request received");
                unBindRequestReceived = true;
                unbindFromDebugOverlayService();
            }
        }
    };

    public ManageService(Application application) {
        this.application = application;
    }

    public void install() {
        if (installed) {
            throw new IllegalStateException("install() can be called only once!");
        }

        startAndBindDebugOverlayService();

        activityLifecycleHandler = new ActivityLifecycleHandler();
        application.registerActivityLifecycleCallbacks(activityLifecycleHandler);

        installed = true;
    }

    // @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void uninstall() {
        unbindFromDebugOverlayService();
        application.stopService(NotifyService.createIntent(application));
        application.unregisterActivityLifecycleCallbacks(activityLifecycleHandler);
        installed = false;
    }

    private void startAndBindDebugOverlayService() {
        // start & bind AlertService
        Intent intent = new Intent(application, NotifyService.class);
        intent.putExtra(NotifyService.KEY_CONFIG, config);
        application.startService(intent);
        bindToDebugOverlayService();
    }

    private void bindToDebugOverlayService() {
        boolean bound = application.bindService(NotifyService.createIntent(application),
                serviceConnection, Context.BIND_AUTO_CREATE);
        if (!bound) {
            throw new RuntimeException("Could not bind the AlertService");
        }
        LocalBroadcastManager.getInstance(application)
                .registerReceiver(receiver, new IntentFilter(NotifyService.ACTION_UNBIND));
    }

    private void unbindFromDebugOverlayService() {
        if (overlayService != null) {
            application.unbindService(serviceConnection);
            overlayService = null;
        }
        LocalBroadcastManager.getInstance(application).unregisterReceiver(receiver);
    }

    // =============================================================================================
    class ActivityLifecycleHandler implements Application.ActivityLifecycleCallbacks {


        private int numRunningActivities;

        ActivityLifecycleHandler() {
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.i(TAG, "onCreate():" + activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityStarted(Activity activity) {
            Log.i(TAG, "onStart():" + activity.getClass().getSimpleName());
            incrementNumRunningActivities();
        }

        @Override
        public void onActivityResumed(Activity activity) {
            Log.i(TAG, "onResume():" + activity.getClass().getSimpleName());

            if (overlayService != null) {
                overlayService.updateNotification();
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Log.i(TAG, "onPause():" + activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityStopped(Activity activity) {
            Log.i(TAG, "onStop():" + activity.getClass().getSimpleName());
            decrementNumRunningActivities();
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, @NonNull Bundle outState) {
            Log.i(TAG, "onSaveInstanceState():" + activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.i(TAG, "onDestroy():" + activity.getClass().getSimpleName());
        }

        private void incrementNumRunningActivities() {
            if (numRunningActivities == 0) {
                // app is in foreground
                if (overlayService == null && unBindRequestReceived) {
                    // service already un-bound by a explicit request, but restart here since it is now in foreground
                    startAndBindDebugOverlayService();
                    unBindRequestReceived = false;
                }
            }

            numRunningActivities++;
        }

        private void decrementNumRunningActivities() {
            numRunningActivities--;
            if (numRunningActivities <= 0) {
                numRunningActivities = 0;
                // apps is in background
                if (overlayService != null) {
                    overlayService.cancelTimer();
                }
            }
        }
    }

}
