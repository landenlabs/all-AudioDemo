
rm audiodemo-src.zip
# zip audiodemo-src.zip *
# zip -r audiodemo-src.zip gradle app
zip -r audiodemo-src.zip app license.txt README.md gradl* settings.gradle -x \*.mp3 

rm audio-mp3.zip
zip -r audio-mp3.zip app/src/main/assets/sounds/
 
