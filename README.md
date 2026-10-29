# TV Launcher for Yoton Y4 Pro

A simple Apple TV-style home screen. Tiles: HDMI, Sources, Original launcher, Settings, Screensaver (from Google Photos), then all installed apps.

---

Select one of two options for downloading the APK.

## 1. Build
This is not recommended as the source code is not regularly updated, and may result in an old version of the launcher.
1. Push this folder to a new GitHub repo.
2. Open the **Actions** tab, run **Build APK** (it also runs on every push).
3. Download the `tv-launcher-apk` artifact, unzip it to get `app-debug.apk`.

## 2. Releases
1. Go to the [Releases Page](https://github.com/MrTecHead/yoton-launcher/releases)
3. Download the latest release 

## Install 
## From platform-tools
    (move apk to platform tools folder)
    ./adb install app-debug.apk
    ./adb shell cmd package set-home-activity com.mrtechead.launcher/.MainActivity
## Using regular adb
    cd path/to/parent-folder/of/apk (eg. /home/user/Downloads or C:/Users/user/Downloads)
    adb install app-debug.apk
    adb shell cmd package set-home-activity com.mrtechead.launcher/.MainActivity
## Go back to the original launcher at any time
    For platform-tools
    ./adb shell cmd package set-home-activity com.htc.luminaos/.activity.MainActivity
    For regular adb
    adb shell cmd package set-home-activity com.htc.luminaos/.activity.MainActivity
If the Home button doesn't open the new launcher, press it and choose "Home" > Always.
