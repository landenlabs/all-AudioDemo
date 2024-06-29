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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.wsi.all_audiodemo.R;

/**
 * Background service.
 * @see ManageService
 */
public class NotifyService extends Service {

    private static final String TAG = "AAD AlertService";

    private static final String NOTIFICATION_CHANNEL_ID = "audiodemo-service";
    private static final int NOTIFICATION_ID = Integer.MAX_VALUE - 100;

    // private static final String ACTION_SHOW_SUFFIX = ".debugoverlay_ACTION_SHOW";
    private static final String ACTION_SOUND_SUFFIX = ".debugoverlay_ACTION_SOUND";

    static final String KEY_CONFIG = "com.wsi.all_audiodemo.extra.CONFIG";
    static final String ACTION_UNBIND = "com.wsi.all_audiodemo.ACTION_UNBIND";
    Config config;

    private final IBinder binder = new LocalBinder();

    private NotificationManager notificationManager;

    // private String actionShow = "";
    private String actionSound = "";


    public static Intent createIntent(Context context) {
        return new Intent(context, NotifyService.class);
    }

    class LocalBinder extends Binder {
        NotifyService getService() {
            // Return this instance of DebugOverlayService so clients can call public methods
            return NotifyService.this;
        }
    }


    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate() called");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        createNotificationChannel();

        String packageName = getPackageName();
        // actionShow = getActionShow(packageName);
        actionSound = getActionSound(packageName);

        IntentFilter intentFilter = new IntentFilter();
        // intentFilter.addAction(actionShow);
        intentFilter.addAction(actionSound);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(receiver, intentFilter);
        }
    }

    /*
    public static String getActionShow(String packageName) {
        return packageName + ACTION_SHOW_SUFFIX;
    }
    */
    public static String getActionSound(String packageName) {
        return packageName + ACTION_SOUND_SUFFIX;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand() called");
        config = intent.getParcelableExtra(KEY_CONFIG);
        // no need to restart this service
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy() called");
        unregisterReceiver(receiver);
        cancelNotification();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind() called");
        return binder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(TAG, "onTaskRemoved() called");
        stopSelf();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcastSync(new Intent(ACTION_UNBIND));
    }


    public void updateNotification() {
        Log.i(TAG, "updateNotification() called");
        showNotification();
    }

    public void startTimer() {
        Log.i(TAG, "startTimer() called");
    }
    public void cancelTimer() {
        Log.i(TAG, "cancelTimer() called");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        "Service", NotificationManager.IMPORTANCE_LOW);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification() {
        Log.i(TAG, "showNotification() called");
        try {
            PendingIntent notificationIntent = getNotificationIntent(this, config, actionSound);
            if (notificationIntent != null) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Control"))
                        //    .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(getAppIcon(this))
                        .setOngoing(true)
                        .setContentTitle("Audio player Tittle")
                        .setContentText("Service notification status");
                //        .setContentIntent(null);

                builder.addAction(R.drawable.play_button, "SoundOut",
                        getNotificationIntent(this, config, null));

                // show the notification
                startForeground(NOTIFICATION_ID, builder.build());
            }
        } catch (Exception ex) {
            Log.e("Audio", ex.getMessage());
        }
    }

    private void cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public static PendingIntent getNotificationIntent(Context context, Config config, String action) {
        if (action == null) {
            PendingIntent pendingIntent = null;
            if (config.getActivityName() != null) {
                try {
                    Intent intent = new Intent(context, Class.forName(config.getActivityName()));
                    pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                } catch (ClassNotFoundException ne) {
                    Log.w(TAG, config.getActivityName() + " was not found - " + ne.getMessage());
                }
            }
            return pendingIntent;
        } else {
            Intent intent = new Intent(action);
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
    }

    // ---------------------------------------------------------------------------------------------
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            /*
            if (actionShow.equals(action)) {
                // update notification
                showNotification();
            }
            */
            if (actionSound.equals(action)) {
                // update notification
                showNotification();
            }
        }
    };

    @Nullable
    private static Bitmap getAppIcon(@NonNull Context context) {
        Drawable drawable = null;
        try {
            drawable = context.getPackageManager().getApplicationIcon(context.getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Package Not found:" + context.getPackageName());
        }
        return (drawable instanceof BitmapDrawable) ? ((BitmapDrawable) drawable).getBitmap() : null;
    }

    // =============================================================================================
    static class Config implements Parcelable {

        @ColorInt
        private final int bgColor;
        @ColorInt
        private final int textColor;
        private final float textSize;
        private final float textAlpha;
        private final boolean allowSystemLayer;
        private final boolean showNotification;
        private final String activityName;

        Config(@ColorInt int bgColor, @ColorInt int textColor, float textSize,
               float textAlpha, boolean allowSystemLayer, boolean showNotification, String activityName) {
            this.bgColor = bgColor;
            this.textColor = textColor;
            this.textSize = textSize;
            this.textAlpha = textAlpha;
            this.allowSystemLayer = allowSystemLayer;
            this.showNotification = showNotification;
            this.activityName = activityName;
        }


        public int getBgColor() {
            return bgColor;
        }

        public int getTextColor() {
            return textColor;
        }

        public float getTextSize() {
            return textSize;
        }

        public float getTextAlpha() {
            return textAlpha;
        }

        public boolean isAllowSystemLayer() {
            return allowSystemLayer;
        }

        public boolean isShowNotification() {
            return showNotification;
        }

        String getActivityName() {
            return activityName;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.bgColor);
            dest.writeInt(this.textColor);
            dest.writeFloat(this.textSize);
            dest.writeFloat(this.textAlpha);
            dest.writeByte(this.allowSystemLayer ? (byte) 1 : (byte) 0);
            dest.writeByte(this.showNotification ? (byte) 1 : (byte) 0);
            dest.writeString(this.activityName);
        }

        Config(Parcel in) {
            this.bgColor = in.readInt();
            this.textColor = in.readInt();
            this.textSize = in.readFloat();
            this.textAlpha = in.readFloat();
            this.allowSystemLayer = in.readByte() != 0;
            this.showNotification = in.readByte() != 0;
            this.activityName = in.readString();
        }

        public static final Parcelable.Creator<Config> CREATOR = new Parcelable.Creator<Config>() {
            @Override
            public Config createFromParcel(Parcel source) {
                return new Config(source);
            }

            @Override
            public Config[] newArray(int size) {
                return new Config[size];
            }
        };
    }

}
