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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;

import com.wsi.all_audiodemo.MainActivity;
import com.wsi.all_audiodemo.R;

import java.io.File;

import static com.wsi.all_audiodemo.notify.NotifyChannels.INVALID_RES_ID;

public class NotifyUtil {
    public static final int REQUEST_CODE = 1001;
    public static final String EXTRA_AUDIO = "audio";

    public static PendingIntent getPendingAction(Context context, String audio, String action) {
        // Prepare intent which is triggered if the
        // notification is selected

        try {
            // Create PendingIntent to take us to DetailsActivity
            // as a result of notification action
            PendingIntent pIntent;
            if (true) {
                // Setup main activity is notify pending intent.

                Intent notifyIntent = new Intent(context, MainActivity.class);
                notifyIntent.setAction(action);
                notifyIntent.putExtra(EXTRA_AUDIO, audio);
                pIntent = PendingIntent.getActivity(
                        context,
                        REQUEST_CODE,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }  else {
                // Setup alternate activity as notify intent.
                Intent notifyIntent = new Intent(context, NotifyActivity.class);
                notifyIntent.setAction(action);
                notifyIntent.putExtra(EXTRA_AUDIO, audio);
                notifyIntent.putExtra("EXTRA_DETAILS_ID", 42);
                pIntent = PendingIntent.getActivity(
                        context,
                        REQUEST_CODE,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
            }
            return pIntent;
        } catch (Exception ne) {
            Log.w("Notify", "Failed to make notify intent, " + ne.getMessage());
            return null;
        }
    }


    public static boolean getIsForeground(@NonNull View view) {
        Rect rect = new Rect();
        boolean isForeground = view.isShown() && view.getGlobalVisibleRect(rect) && !rect.isEmpty();
        isForeground |=  view.getWindowSystemUiVisibility() == View.VISIBLE
                && view.getWindowVisibility() == View.VISIBLE;
        return isForeground;
    }



    public static  void notifySound(@NonNull Context context, @NonNull String assetName, boolean isForeground) {
        // Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String RESOURCE_PATH = ContentResolver.SCHEME_ANDROID_RESOURCE + "://";

        // Two ways to provide resource, either using its name or resource id.
        //
        // Name
        //    Syntax  :  android.resource://[package]/[res type]/[res name]
        //    Example : Uri.parse("android.resource://com.my.package/raw/sound1");
        //
        // Resource id
        //    Syntax  : android.resource://[package]/[resource_id]
        //    Example : Uri.parse("android.resource://com.my.package/" + R.raw.sound1);

        String path;
        @RawRes int soundRes = INVALID_RES_ID;
        if (false) {
            path = RESOURCE_PATH + context.getPackageName() + "/raw/" + assetName;
        } else {
            soundRes = context.getResources().getIdentifier(assetName, "raw", context.getPackageName());
            path = RESOURCE_PATH + context.getPackageName() + File.separator + soundRes;
        }
        Uri soundUri = Uri.parse(path);

        //  Build notification - use OS specific API.

        Bitmap iconBM  = null;
        iconBM = ((BitmapDrawable) AppCompatResources.getDrawable(context, R.mipmap.ic_launcher)).getBitmap();

        PendingIntent pendingIntent = NotifyUtil.getPendingAction(context, assetName, "replay");
        NotificationCompat.Builder builder;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    NotifyChannels.setSound(context, NotifyChannels.Channel.ALERTS, soundRes);
            builder = new NotificationCompat.Builder(context,
                    notificationChannel.getId());
        } else {
            //noinspection deprecation
            builder = new NotificationCompat.Builder(context);
        }

        builder
                .setContentTitle("Played sound")
                .setContentText((isForeground ? "Foreground":"Background") + " sound " + assetName)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setSound(soundUri)                 // This sets the sound to play
                .setLargeIcon(iconBM);

        builder.addAction(R.drawable.replay_button, "Replay", pendingIntent);
        builder.addAction(R.drawable.replay_button, "Next", pendingIntent);

        Notification notification = builder.build();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotifyChannels.notify(context, NotifyActivity.NOTIFICATION_ID, notification);
        } else {
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NotifyActivity.NOTIFICATION_ID, notification);
        }
    }


}
