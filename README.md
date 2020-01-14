# a LanDen Labs - AudioDemo
Android - Audio Demo    ( Dec-2019  )

Demonstrate playing audio mp3 files from **res/raw** and **assets** directories.
Also includes sample code to play audio from external souerces such as **SD card** or **network**.

**Warning** the audio files are pulled from various sources with undefined copyright, so use at your own peril.


  [![Build status](https://travis-ci.org/landenlabs/all_AudioDemo.svg?branch=master)](https://travis-ci.org/landenlabs/all_AudioDemo)
  

Apk available in **app/build** directory 

![assets directory](https://raw.github.com/landenlabs2/all_AudioDemo/master/screenshots/audiodemo.png)


Audio demo website
[http://landenlabs.com/android/audiodemo/audiodemo.html](http://landenlabs.com/android/audiodemo/audiodemo.html)

***
Mp3 sound files stored in **assets/sounds/** directory
***
![assets directory](http://landenlabs.com/android/audiodemo/dir-assets.png)

***
Mp3 sound files stored in **res/raw/** directory
***
![res/raw directory](http://landenlabs.com/android/audiodemo/dir-res-raw.png)

***
Simple audio demnonstration program screen:
***
![screen](http://landenlabs.com/android/audiodemo/screen.png)

***
Sample code to play audio mp3 file using Status Notifcation service:
***
```javascript
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
```

***
Sample code to play mp3 audio file from res/raw directory as either resource ID or by name.
***
```javascript
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
        mMediaPlayer.setVolume(1.0f, 1.0f);
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
        //  Different ways to load audio into player:
        //   1. Using path to resource by name or by id
        //   2. Using content provider to load audio and passing a file descriptor.
        if (true) {
            // 1. open audio using path to data inside package
            mMediaPlayer.setDataSource(getApplicationContext(), soundUri);
            mMediaPlayer.prepare();
        }   else {
            // 2. Load using content provider, passing file descriptor.
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
```

***
Sample code to play mp3 audio file from assets directory. 
Notes all files in assets directory are merged together and you need to provide the file offset and length to the MediaPlayer to setup the data source. 
***
```javascript
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
```

***
Sample code to play mp3 audio file from remote source, such as network.
***
```javascript
/**
 * Play sound file from remote location.
 * Accessing external data not found inside package. SD card or network.
 *   "/sdcard/sample.mp3";
 *   "http://www.bogotobogo.com/Audio/sample.mp3";
 *
 * May require settings in AndroidManifest to enable ClearText and internet permission.
 *
 * See supported file formats
 *   https://developer.android.com/guide/topics/media/media-formats
 * See enable cleartext if network path is using http. Not required for https
 *   https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted
 * Add network permission to AndroidManifest.xml
 *   <uses-permission android:name="android.permission.INTERNET" />
 */
private void playNetworkSound(String netUrl) {
    try {
        mSoundName.setText(netUrl);

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

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setDataSource(netUrl);
        mMediaPlayer.prepareAsync();        // Use prepareAsync for external sources.

    } catch (Exception ex) {
        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
    }
}
```