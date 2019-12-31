/*
 * Copyright (c) 2019 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 *
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
 * @author Dennis Lang  (30-Dec-2019)
 * @see http://LanDenLabs.com/
 *
 */

package com.wsi.all_audiodemo;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.wsi.all_audiodemo.notify.ManageService;
import com.wsi.all_audiodemo.notify.NotifyChannels;
import com.wsi.all_audiodemo.notify.NotifyUtil;

import java.io.File;
import java.util.Locale;

import static com.wsi.all_audiodemo.notify.NotifyUtil.notifySound;

/**
 * Demonstrate how to play a sound mp2 file by accessing  asset mp2 file two different ways and
 * playing  either in foreground or background.
 * <p>
 * Also include some code to show information in notification bar using channels.
 */
@SuppressWarnings({"ConstantConditions", "ConstantIfStatement"})
public class MainActivity extends AppCompatActivity {

    String mSound = "alert_air_horn";
    ListView mListView;
    TextView mSoundName;
    View mAboutView;
    ManageService mManageService;

    // https://github.com/codepath/android_guides/wiki/Video-and-Audio-Playback-and-Recording
    MediaPlayer mMediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 21) {
            Toolbar toolbar = findViewById(R.id.app_bar);
            setSupportActionBar(toolbar);
        }

        setupSoundSelectionView();
        mSoundName = findViewById(R.id.soundName);
        mAboutView = findViewById(R.id.about_text);

        // 1. Play and notify in foreground
        findViewById(R.id.notifyFg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifySoundFg(mSound);
            }
        });
        // 2. Play and notify in Background
        findViewById(R.id.notifyBg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifySoundBg(mSound);
            }
        });
        // 3. Access mp2 via Raw asset path, play in foreground
        findViewById(R.id.playRawBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRawSound(mSound);
            }
        });
        // 4. Access mp2 via Asset path, play in foreground
        findViewById(R.id.playAssetBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAssetSound(mSound);
            }
        });

        // Setup notification - used for option #1 and #2 above.
        NotifyChannels.initChannels(this);
        mManageService = new ManageService(getApplication());
        mManageService.install();

        // Handle Notification button press.
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            String intentSound = intent.getStringExtra(NotifyUtil.EXTRA_AUDIO);
            if (!TextUtils.isEmpty(intentSound)) {
                for (int idx = 0; idx < mListView.getAdapter().getCount(); idx++) {
                    if (mListView.getAdapter().getItem(idx).equals(intentSound)) {
                        mSound = intentSound;
                        mListView.setSelection(idx);
                        playRawSound(mSound);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_about) {
            mAboutView.setVisibility(View.VISIBLE);
            mAboutView.findViewById(R.id.about_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAboutView.setVisibility(View.GONE);
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ---------------------------------------------------------------------------------------------
    // Private class logic

    /**
     * Play sound in background:
     *   1. Move activity to back of stack
     *   2. Bring home page of device into view.
     *   3. Sleep for 2 seconds to make sure we are in background.
     *   4. Play sound using notification, update notification msg.
     */
    private void notifySoundBg(String assetName) {
        //  JobScheduler
        boolean sentAppToBackground =  moveTaskToBack(true);
        if (!sentAppToBackground){
            Intent i = new Intent();
            i.setAction(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            this.startActivity(i);
        }

        new Handler().postDelayed (() -> {
            notifySound(getApplicationContext(), assetName, NotifyUtil.getIsForeground(mSoundName));
        }, 2000);
    }

    /**
     * Play sound in foreground using a notification.
     */
    private void notifySoundFg(String assetName) {
        notifySound(getApplicationContext(), assetName, NotifyUtil.getIsForeground(mSoundName));
    }


    /**
     * Example playing sound with in-package resource.
     */
    @SuppressWarnings("unused")
    private void playSound() {
        // Play sound using  resource reference.
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = MediaPlayer.create(this, R.raw.alert_air_horn);
        mMediaPlayer.start();
    }

    /**
     * Play sound file stored in res/raw/ directory
     */
    private void playRawSound(String rawName) {
        try {
            // Two ways to provide resource, either using its name or resource id.
            //
            // Name
            //    Syntax  :  android.resource://[package]/[res type]/[res name]
            //    Example : Uri.parse("android.resource://com.my.package/raw/sound1");
            //
            // Resource id
            //    Syntax  : android.resource://[package]/[resource_id]
            //    Example : Uri.parse("android.resource://com.my.package/" + R.raw.sound1);

            String RESOURCE_PATH = ContentResolver.SCHEME_ANDROID_RESOURCE + "://";

            String path;
            if (false) {
                // Build path using resource name
                path = RESOURCE_PATH + getPackageName() + "/raw/" + rawName;
            } else {
                // Build path using resource number
                int resID = getResources().getIdentifier(rawName, "raw", getPackageName());
                path = RESOURCE_PATH + getPackageName() + File.separator + resID;
            }
            Uri soundUri = Uri.parse(path);
            mSoundName.setText(path);

            mMediaPlayer = new MediaPlayer();
            // mMediaPlayer.setVolume(1.0f, 1.0f);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Toast.makeText(getApplicationContext(),
                            "start playing sound", Toast.LENGTH_SHORT).show();
                    mMediaPlayer.start();
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Toast.makeText(getApplicationContext(), String.format(Locale.US,
                            "Media error what=%d extra=%d", what, extra), Toast.LENGTH_LONG).show();
                    return false;
                }
            });

            //
            //  Three different ways to load audio into player:
            //   1. Using path to resource by name or by id
            //   2. Accessing external data not found inside package. SD card or network.
            //      May require settings in AndroidManifest to enable ClearText and internet permission.
            //   3. Using content provider to load audio and passing a file descriptor.
            if (true) {
                // 1. open audio using path to data inside package
                mMediaPlayer.setDataSource(getApplicationContext(), soundUri);
                mMediaPlayer.prepare();
            } else if (false) {
                // 2.  How to load external audio files from SD card or network.
                //   "/sdcard/sample.mp3";
                //   "http://www.bogotobogo.com/Audio/sample.mp3";
                // see supported file formats
                //   https://developer.android.com/guide/topics/media/media-formats
                // see enable cleartext
                //   https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted
                // Add network permission to AndroidManifest.xml
                //   <uses-permission android:name="android.permission.INTERNET" />
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource("http://landenlabs.com/android/audiodemo/alert_air_horn.mp3");
                // mMediaPlayer = MediaPlayer.create(this, Uri.parse("http://landenlabs.com/android/audiodemo/alert_air_horn.mp3"));
                mMediaPlayer.prepareAsync();
            } else {
                // 3. Load using content provider, passing file descriptor.
                ContentResolver resolver = getContentResolver();
                AssetFileDescriptor afd = resolver.openAssetFileDescriptor(soundUri, "r");
                mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                mMediaPlayer.prepareAsync();
            }

            // See setOnPreparedListener above
            //  mMediaPlayer.start();

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Play sound file stored in assets/sounds/ directory.
     */
    private void playAssetSound(String assetName) {
        try {
            AssetFileDescriptor afd = getAssets().openFd("sounds/" + assetName + ".mp3");
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * setup sound selection list view.
     */
    private void setupSoundSelectionView() {
        // Get ListView object from xml
        mListView = findViewById(R.id.listview);

        // Defined Array values to show in ListView
        String[] values = new String[]{
                "alert_air_horn",
                "alert_alarm_clock",
                "alert_blop",
                "alert_censored_beep",
                "alert_electrical_sweep",
                "alert_fire_pager",
                "alert_fog_horn",
                "alert_metal_gong",
                "alert_pling",
                "alert_power_up",
                "alert_railroad_crossing",
                "alert_sad_trombone",
                "alert_school_fire_alarm",
                "alert_ship_bell",
                "alert_siren_noise",
                "alert_store_door_chime",
                "alert_temple_bell",
                "alert_tornado_siren",
                "alert_train_whistle",
                "alert_ufo_takeoff",

                "animal_bluejay",
                "animal_cow",
                "animal_horned_owl",
                "animal_pterodactyl_screech",
                "animal_rooster",
                "animal_turkey",

                "long_news_intro",

                "weather_hailstorm",
                "weather_rain",
                "weather_rainstorm",
                "weather_thunder1",
                "weather_thunder2",
                "weather_thunder3",
                "weather_wind",

                "test_hurwrn"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_activated_1,
                // android.R.layout.simple_selectable_list_item,
                values);

        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSound = (String) mListView.getItemAtPosition(position);
                mSoundName.setText(mSound);
            }
        });
    }
}
