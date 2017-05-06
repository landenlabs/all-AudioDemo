# a LanDen Labs - AudioDemo
Android - Audio Demo

Demonstrate playing audio wav files from res/raw and assets directories

Apk v1.0 available in app directory 

![assets directory](https://raw.github.com/landenlabs2/all_AudioDemo/master/screenshots/audiodemo.png)


Audio demo website
[http://landenlabs.com/android/audiodemo/audiodemo.html](http://landenlabs.com/android/audiodemo/audiodemo.html)

***
mp3 sound files stored in assets directory in sub folder sound:
***
![assets directory](http://landenlabs.com/android/audiodemo/dir-assets.png)

***
mp3 sound files stored in res/raw directory
***
![res/raw directory](http://landenlabs.com/android/audiodemo/dir-res-raw.png)

***
Simple audio demnonstration program screen:
***
![screen](http://landenlabs.com/android/audiodemo/audiodemo.jpg)

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
            // Internal asset
            mMediaPlayer.setDataSource(getApplicationContext(), soundUri);
            mMediaPlayer.prepare();
        } else if (false) {
            // Load external audio files url:
            //  "http://www.bogotobogo.com/Audio/sample.mp3";
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
        } else {
            // Internal resource asset
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

