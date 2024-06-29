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

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.RequiresApi;

import com.wsi.all_audiodemo.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Manage notification channels.
 * Used to shows message in notification that audio was played.
 */
@SuppressWarnings({"ConstantConditions", "ConstantIfStatement", "unused", "SameParameterValue"})
public class NotifyChannels {

    private static final ChannelSpec[] CHANNEL_SPECS = new ChannelSpec[]{
            new ChannelSpec(Channel.ALERTS, "Alerts", Priority.LOW, true, R.raw.alert_alarm_clock, true),
            new ChannelSpec(Channel.LIGHTNING, "Lightning",  Priority.HIGH, true),
            new ChannelSpec(Channel.PRECIPITATION, "Precipitation", Priority.HIGH),
            new ChannelSpec(Channel.STATION, "Station", Priority.LOW),
            new ChannelSpec(Channel.BANNER, "Banner", Priority.LOW),
    };
    private static final Map<Channel, ChannelSpec> CHANNEL_SPEC_MAP = new HashMap<>();

    public static final @RawRes int INVALID_RES_ID = -1;

    public static boolean haveChannels() {
        return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O);
    }

    // @RequiresApi(24)
    public static void initChannels(Context context) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    getServiceSafe(context, Context.NOTIFICATION_SERVICE);

            for (ChannelSpec channelSpec : CHANNEL_SPECS) {
                NotificationChannel notificationChannel = createChannel(context, channelSpec, INVALID_RES_ID);
                notificationManager.createNotificationChannel(notificationChannel);
                CHANNEL_SPEC_MAP.put(channelSpec.id, channelSpec);
            }
        }
    }

    @RequiresApi(26)
    private static void deleteChannel(Context context, ChannelSpec channelSpec) {
        NotificationManager notificationManager =
                getServiceSafe(context, Context.NOTIFICATION_SERVICE);
        notificationManager.deleteNotificationChannel(channelSpec.getId());
    }

    @RequiresApi(26)
    private static NotificationChannel createChannel(
            Context context, ChannelSpec channelSpec, @RawRes int soundOverrideRes) {

        if (soundOverrideRes != INVALID_RES_ID) {
            channelSpec.soundRes = soundOverrideRes;
        }

        NotificationChannel notificationChannel =
                new NotificationChannel(channelSpec.getId(), channelSpec.name,
                        channelSpec.importance);

        notificationChannel.enableLights(channelSpec.color != 0);
        notificationChannel.setLightColor(channelSpec.color);
        notificationChannel.enableVibration(channelSpec.vibrate);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        if (channelSpec.soundRes != INVALID_RES_ID) {
            Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/"
                    + channelSpec.soundRes);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .build();
            notificationChannel.setSound(soundUri, audioAttributes);
        }
        return notificationChannel;
    }


    @RequiresApi(26)
    synchronized
    public static NotificationChannel setSound(Context context, Channel channel, @RawRes int soundRes) {
        NotificationManager notificationManager =
                getServiceSafe(context, Context.NOTIFICATION_SERVICE);
        ChannelSpec channelSpec = CHANNEL_SPEC_MAP.get(channel);
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelSpec.getId());

        if (channelSpec.appSound) {
            channelSpec.soundRes = soundRes;
        } else {
            // Recreate the channel to get new sound set (new channel has new unique id per sound).
            // WARNING - Google does not like apps which delete and re-create channels.
            deleteChannel(context, channelSpec);
            // TODO - copy some values from original channel into new channel.
            notificationChannel = createChannel(context, channelSpec, soundRes);

            if (false) {
                List<NotificationChannel> channels = notificationManager.getNotificationChannels();
                Log.d("channel", "Channels=" + channels.size());
                StatusBarNotification[] activeNotifications =
                        notificationManager.getActiveNotifications();
                if (activeNotifications != null) {
                    Log.d("channel", "Active notifications=" + activeNotifications.length);
                }
            }

            notificationManager.createNotificationChannel(notificationChannel);
        }

        return notificationChannel;
    }

    @RequiresApi(26)
    public static void notify(Context context, int notificationId, Notification notification) {
        NotificationManager notificationManager =
                getServiceSafe(context, Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
        try {
            Channel channel = Channel.valueOf(notification.getChannelId());
            ChannelSpec channelSpec = CHANNEL_SPEC_MAP.get(channel);
            if (channelSpec.appSound) {
                playAppSound(context, channelSpec.soundRes);
            }
        } catch (Exception ignore) {
        }
    }

    private static void playAppSound(Context context, @RawRes int soundRes) {
        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + soundRes);
        // mSoundName.setText(path);

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(context, soundUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception ignore) {
        }
    }


    @NonNull
    private static <T> T getServiceSafe(@Nullable Context context, String service) {
        Objects.requireNonNull(context);
        //noinspection unchecked
        return (T)Objects.requireNonNull(context.getSystemService(service));
    }

    public static boolean isDeviceLocked(Context context) {

        /*
         *  If the screen is OFF then the device has been locked,
         *  but when screen is ON it doesn't mean that screen is not locked.
         */
        PowerManager powerManager = getServiceSafe(context, Context.POWER_SERVICE);
        boolean isLocked = false;
        isLocked = !powerManager.isInteractive();

        KeyguardManager keyguardManager = getServiceSafe(context, Context.KEYGUARD_SERVICE);
        isLocked = Objects.requireNonNull(keyguardManager).inKeyguardRestrictedInputMode() || isLocked;
        return isLocked;
    }

    /**
     * see      developer.android.com/design/patterns/notifications.html
     * <p>
     * MAX  Use  for  critical  and  urgent  notifications  that  alert the user to a
     * condition  that  is  time-critical  or  needs  to  be resolved before they can
     * continue   with   a   particular   task.
     * <p>
     * HIGH Use  primarily  for  important communication,   such   as  message  or
     * chat  events  with  content  that  is  particularly  interesting for the user.
     * High-priority notifications trigger the heads-up notification display.
     * <p>
     * DEFAULT Use for all notifications that don't fall   into   any  of  the  other
     * priorities described here.
     * <p>
     * LOW  Use  for notifications  that  you want the user to be informed about, but
     * that are less urgent. Low-priority notifications tend to show up at the bottom
     * of  the  list,  which  makes  them  a  good  choice  for things like public or
     * undirected  social  updates: The user has asked to be notified about them, but
     * these  notifications  should  never  take  precedence  over  urgent  or direct
     * communication.
     * <p>
     * MIN  Use  for contextual or background information such as weather information
     * or  contextual  location  information.  Minimum-priority  notifications do not
     * appear   in  the  status  bar.  The  user  discovers  them  on  expanding  the
     * notification shade.
     */

    // =============================================================================================
    public enum Priority {
        MIN, LOW, DEFAULT, HIGH
    }


    // =============================================================================================
    public enum Channel {
        ALERTS, LIGHTNING, PRECIPITATION, STATION, BANNER
    }
    // =============================================================================================
    public static class ChannelSpec {
        Channel id;
        String name;
        Priority priority;
        int importance;
        boolean vibrate;
        @RawRes int soundRes;
        @ColorInt int color;
        boolean appSound = false;

        ChannelSpec(Channel id, String name, Priority priority) {
            common(id, name, priority);
            this.vibrate = false;
            this.soundRes = INVALID_RES_ID;
        }
        ChannelSpec(Channel id, String name, Priority priority, boolean vibrate) {
            common(id, name, priority);
            this.vibrate = vibrate;
            this.soundRes = INVALID_RES_ID;
        }
        ChannelSpec(Channel id, String name, Priority priority, boolean vibrate, @RawRes int soundRes) {
            common(id, name, priority);
            this.vibrate = vibrate;
            this.soundRes = soundRes;
        }
        ChannelSpec(Channel id, String name, Priority priority, boolean vibrate, @RawRes int soundRes, boolean appSound) {
            common(id, name, priority);
            this.vibrate = vibrate;
            this.soundRes = soundRes;
            this.appSound = appSound;
        }

        String getId() {
            // If sound played by app, then channel id is enum name.
            // If notification playing sound, then channel id is unique for enum and sound.
            return appSound ? id.name() : (id.name() + this.soundRes);
        }

        private void common(Channel id, String name, Priority priority) {
            this.id = id;
            this.name = name;
            this.priority = priority;
            this.color = Color.RED;

            int tmpImportance = 0;
            if (Build.VERSION.SDK_INT >= 26) {
                switch (priority) {
                    case MIN:
                        tmpImportance =  NotificationManager.IMPORTANCE_MIN;
                        break;
                    case LOW:
                        tmpImportance =  NotificationManager.IMPORTANCE_LOW;
                        break;
                    case DEFAULT:
                        tmpImportance =  NotificationManager.IMPORTANCE_DEFAULT;
                        break;
                    case HIGH:
                        tmpImportance =  NotificationManager.IMPORTANCE_HIGH;
                        break;
                }
            }
            importance = tmpImportance;
        }
    }
}
