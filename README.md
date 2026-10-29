# TV Launcher for Yoton Y4 Pro

A simple Apple TV-style home screen. Tiles: HDMI, Sources, Original launcher, Settings, Screensaver (from Google Photos), then all installed apps.

## Build
1. Push this folder to a new GitHub repo.
2. Open the **Actions** tab, run **Build APK** (it also runs on every push).
3. Download the `tv-launcher-apk` artifact, unzip it to get `app-debug.apk`.

## Install (from platform-tools)
    ./adb install app-debug.apk
    ./adb shell cmd package set-home-activity com.mrtechead.launcher/.MainActivity

## Go back to the original launcher at any time
    ./adb shell cmd package set-home-activity com.htc.luminaos/.activity.MainActivity

If the Home button doesn't open the new launcher, press it and choose "Home" > Always.
