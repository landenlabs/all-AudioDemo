/*
 *  Copyright (c) 2015 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 *   associated documentation files (the "Software"), to deal in the Software without restriction, including
 *   without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 *    following conditions:
 *
 *    The above copyright notice and this permission notice shall be included in all copies or substantial
 *    portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 *   LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 *   NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *   WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *   SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *    @author Dennis Lang  (12/13/2015)
 *    @see http://landenlabs.com/
 */

package com.wsi.all_audiodemo;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ScrollingActivity extends AppCompatActivity {

    String mSound = "blzwrn";
    ListView mListView;
    TextView mSoundName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        setupListView();

        mSoundName = (TextView) findViewById(R.id.soundName);

        findViewById(R.id.notify1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifySound(mSound);
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound2(mSound);
            }
        });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound3(mSound);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void notifySound(String assetName) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        // Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String RESOURCE_PATH = ContentResolver.SCHEME_ANDROID_RESOURCE + "://";

        String path;
        if (false) {
            path = RESOURCE_PATH + getPackageName() + "/raw/" + assetName;
        } else {
            int resID = getResources().getIdentifier(assetName, "raw", getPackageName());
            path = RESOURCE_PATH + getPackageName() + File.separator + resID;
        }
        Uri soundUri = Uri.parse(path);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("Title")
                .setContentText("Message")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(soundUri); //This sets the sound to play

        notificationManager.notify(10, mBuilder.build());
    }

    // https://github.com/codepath/android_guides/wiki/Video-and-Audio-Playback-and-Recording
    MediaPlayer mMediaPlayer = null;
    private void playSound1(String assetName) {
        // mMediaPlayer = MediaPlayer.create(this, R.raw.sound1);
        mMediaPlayer.start();
    }

    private void playSound2(String assetName) {
        try {
            // Syntax  :  android.resource://[package]/[res type]/[res name]
            // Example : Uri.parse("android.resource://com.my.package/raw/sound1");
            //
            // Syntax  : android.resource://[package]/[resource_id]
            // Example : Uri.parse("android.resource://com.my.package/" + R.raw.sound1);

            String RESOURCE_PATH = ContentResolver.SCHEME_ANDROID_RESOURCE + "://";

            String path;
            if (false) {
                 path = RESOURCE_PATH + getPackageName() + "/raw/" + assetName;
            } else {
                int resID = getResources().getIdentifier(assetName, "raw", getPackageName());
                path = RESOURCE_PATH + getPackageName() + File.separator + resID;
            }
            Uri soundUri = Uri.parse(path);
            mSoundName.setText(path);

            mMediaPlayer = new MediaPlayer();
            if (true) {
                mMediaPlayer.setDataSource(getApplicationContext(), soundUri);
                mMediaPlayer.prepare();
            } else if (false) {
                // How to load external audio files.
                // "/sdcard/sample.mp3";
                //  "http://www.bogotobogo.com/Audio/sample.mp3";
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepare();
            } else {
                ContentResolver resolver = getContentResolver();
                AssetFileDescriptor afd = resolver.openAssetFileDescriptor(soundUri, "r");
                mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
            }

            mMediaPlayer.start();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void playSound3(String assetName) {
        try {
            AssetFileDescriptor afd =  getAssets().openFd("sounds/" + assetName + ".mp3");
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupListView() {
        // Get ListView object from xml
        mListView = (ListView) findViewById(R.id.listview);

        // Defined Array values to show in ListView
        String[] values = new String[] {
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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);

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
