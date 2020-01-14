#!/bin/tcsh -f

./gradlew assemble
cp app/build/outputs/apk/debug/*.apk ~/private/websites/landenlabs-ipage/android/audiodemo/audiodemo.apk

./gradlew clean

source make-src-zip.csh
cp audio*.zip ~/private/websites/landenlabs-ipage/android/audiodemo/
 
pushd ~/private/websites/landenlabs-ipage/android/audiodemo/
 rm audiodemo-apk.zip
 zip audiodemo-apk.zip  *.apk
popd