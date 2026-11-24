# CleanYourTV: Complete Debloat Guide for Airtel Xstream Smart Box

A tested, step-by-step guide to fully debloat and customize your Airtel Xstream Smart Box (Technicolor HSW4026ATL, Android 11) using [CleanYourTV](https://github.com/Gathin23/cleanyourtv) launcher and ADB.

**Credits:** This guide is built on top of [Gathin23/cleanyourtv](https://github.com/Gathin23/cleanyourtv) — the launcher and the idea. The ADB enable exploit and debloat process documented here were reverse-engineered using Claude (AI) as a partner.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Phase 1: Enable ADB](#phase-1-enable-adb)
- [Phase 2: Connect via ADB](#phase-2-connect-via-adb)
- [Phase 3: Install CleanYourTV Launcher](#phase-3-install-cleanyourtv-launcher)
- [Phase 4: Debloat — Disable Bloatware](#phase-4-debloat--disable-bloatware)
- [Phase 5: Performance Optimization](#phase-5-performance-optimization)
- [Phase 6: Lock Down Updates](#phase-6-lock-down-updates)
- [Phase 7: Chromecast Fix](#phase-7-chromecast-fix)
- [Phase 8: Launcher Configuration](#phase-8-launcher-configuration)
- [Known Issues and Solutions](#known-issues-and-solutions)
- [Restore / Undo Everything](#restore--undo-everything)
- [Claude Prompt for Automated Setup](#claude-prompt-for-automated-setup)
- [Files Reference](#files-reference)

---

## Prerequisites

Before starting, you need:

1. **ADB enabled** on the STB — follow the [EnableGreyedADB guide](README.md) if USB Debugging is greyed out
2. **ADB installed** on your computer — [Android SDK Platform Tools](https://developer.android.com/tools/releases/platform-tools)
3. **STB IP address** — find it at: `Settings > Device Preferences > About > Status > IP Address`
4. **Computer on the same Wi-Fi network** as the STB
5. **CleanYourTV APK** — download from [CleanYourTV releases](https://github.com/Gathin23/cleanyourtv/releases)

### Verify ADB Connection

```bash
adb connect <STB_IP>:5555
adb devices
# Should show: <STB_IP>:5555    device
```

If it shows `unauthorized`, check the TV screen for an authorization dialog and approve it.

---

## Phase 1: Enable ADB

If USB Debugging is greyed out on your Airtel Xstream Smart Box, follow the [EnableGreyedADB guide](README.md) first.

Once ADB is enabled, continue to Phase 2.

---

## Phase 2: Connect via ADB

```bash
adb connect <STB_IP>:5555
```

Verify with:
```bash
adb shell getprop ro.product.model
# Expected: XStream_Smart_Box_001

adb shell getprop ro.build.version.sdk
# Expected: 30
```

---

## Phase 3: Install CleanYourTV Launcher

### 3.1 Install the Launcher APK

```bash
adb install -r cleanyourtv.apk
```

### 3.2 Grant Required Permissions

The launcher needs `QUERY_ALL_PACKAGES` to see all installed apps. On Android 11 (SDK 30), this requires explicit granting:

```bash
adb shell pm grant com.gathin.tvhome android.permission.WRITE_SECURE_SETTINGS
```

### 3.3 Set as Default Home

```bash
# Press Home on remote — a chooser dialog appears
# Select "Clean Your TV" and choose "Always"
```

If the chooser doesn't appear:
```bash
adb shell cmd package set-home-activity com.gathin.tvhome/.MainActivity
```

---

## Phase 4: Debloat — Disable Bloatware

**CRITICAL SAFETY RULES:**
- **Use `pm disable-user --user 0`, never `pm uninstall` on system packages** — uninstall can brick
- **Run commands one per shell call** — chained commands may silently stop after the first
- **Never disable:** TV input services, remote control services, keyboard, Play Store, Play Services, the ADB enabler app (`com.technicolor.enableUpdates`)
- **Never launch** any package whose name contains `factory`, `production`, `AT`, or `fac menu`

### 4.1 Airtel Bloatware

These are Airtel's own apps — safe to disable:

```bash
adb shell pm disable-user --user 0 tv.airtel.xstream.tvapp
adb shell pm disable-user --user 0 com.airtel.tv
adb shell pm disable-user --user 0 com.airtel.fti
adb shell pm disable-user --user 0 com.uei.uas.airtel
```

### 4.2 Telemetry and Tracking

```bash
adb shell pm disable-user --user 0 com.tataelxsi.icx
adb shell pm disable-user --user 0 com.technicolor.android.BootupTraceQR
```

### 4.3 Update Services

Disable these to prevent the operator from pushing updates that revert your changes:

```bash
adb shell pm disable-user --user 0 com.apps.updater
adb shell pm disable-user --user 0 com.firmware.update
adb shell pm disable-user --user 0 com.technicolor.checkota
```

### 4.4 Google Bloat

```bash
adb shell pm disable-user --user 0 com.google.android.tv.bugreportsender
adb shell pm disable-user --user 0 com.google.android.feedback
adb shell pm disable-user --user 0 com.google.android.onetimeinitializer
adb shell pm disable-user --user 0 com.google.android.partnersetup
adb shell pm disable-user --user 0 com.google.android.syncadapters.calendar
adb shell pm disable-user --user 0 com.google.android.tv.frameworkpackagestubs
adb shell pm disable-user --user 0 com.google.android.sss.authbridge
adb shell pm disable-user --user 0 com.google.android.tungsten.setupwraith
```

### 4.5 Unused Streaming Apps

```bash
adb shell pm disable-user --user 0 com.amazon.amazonvideo.livingroom
adb shell pm disable-user --user 0 com.netflix.ninja
adb shell pm disable-user --user 0 com.google.android.youtube.tvmusic
adb shell pm disable-user --user 0 com.google.android.videos
adb shell pm disable-user --user 0 com.google.android.play.games
```

### 4.6 System Bloat

```bash
adb shell pm disable-user --user 0 com.android.printspooler
adb shell pm disable-user --user 0 com.android.wallpaperbackup
adb shell pm disable-user --user 0 com.android.dreams.basic
adb shell pm disable-user --user 0 com.android.htmlviewer
adb shell pm disable-user --user 0 com.android.companiondevicemanager
adb shell pm disable-user --user 0 com.google.android.marvin.talkback
adb shell pm disable-user --user 0 com.google.android.tvrecommendations
```

### 4.7 Broadcom/Technicolor OEM Bloat

```bash
adb shell pm disable-user --user 0 com.broadcom.nrdphelper
adb shell pm disable-user --user 0 com.broadcom.dnyhelper
adb shell pm disable-user --user 0 com.broadcom.customizer
adb shell pm disable-user --user 0 com.technicolor.avsignalstatus
adb shell pm disable-user --user 0 com.technicolor.onetouchplay
adb shell pm disable-user --user 0 com.technicolor.android.hddtemperaturemonitor
adb shell pm disable-user --user 0 android.autoinstalls.config.technicolor
adb shell pm disable-user --user 0 android.autoinstalls.config.bcm.device
adb shell pm disable-user --user 0 com.technicolor.leanbacklauncher.partnercustomizer
adb shell pm disable-user --user 0 com.android.managedprovisioning
```

### 4.8 Verify

Check all disabled packages:

```bash
adb shell pm list packages -d
```

You should see ~40 disabled packages.

---

## Phase 5: Performance Optimization

### 5.1 Reduce Animation Scales

```bash
adb shell settings put global window_animation_scale 0.5
adb shell settings put global transition_animation_scale 0.5
adb shell settings put global animator_duration_scale 0.5
```

### 5.2 Limit Background Processes

The STB has only 1.5GB RAM. Limiting background processes frees memory:

```bash
adb shell settings put global background_process_limit 4
```

### 5.3 Run Background DEX Optimization

This pre-compiles app bytecode for faster startup:

```bash
adb shell cmd package bg-dexopt-job
```

This takes a few minutes. Let it finish.

---

## Phase 6: Lock Down Updates

Prevent OTA updates from reverting your debloat:

```bash
adb shell settings put global ota_disable_automatic_update 1
adb shell settings put global package_verifier_enable 0
adb shell settings put global verifier_verify_adb_installs 0
adb shell settings put global upload_apk_enable 0
```

Also manually disable auto-updates in Play Store:
1. Open Play Store on the STB
2. Go to Settings > Auto-update apps > Don't auto-update apps

---

## Phase 7: Chromecast Fix

The built-in Chromecast (`com.google.android.apps.mediashell`) may not initialize after debloat. The Cast receiver service needs to be manually started.

### 7.1 Restart Cast Service

```bash
adb shell am force-stop com.google.android.apps.mediashell
adb shell am startservice -n com.google.android.apps.mediashell/.MediaShellCastReceiverService
```

### 7.2 Verify Cast is Running

```bash
adb shell netstat -tlnp | grep -E "8008|8009"
# Should show LISTEN on ports 8008 and 8009
```

### 7.3 Install CastLauncher (Optional)

The Chromecast app (`mediashell`) has no launcher activity — it's a background service and won't appear as a tile on the home screen. We built a small helper app that:
- Shows a "Chromecast" tile on the home dock
- Opens Cast settings when tapped
- Auto-starts the Cast receiver service after every reboot

Source code and APK: [`castlauncher/`](castlauncher/)

```bash
adb install -r CastLauncher.apk
```

### 7.4 Casting from Phone

This STB does **not** support Miracast/WiFi Direct (no `wifip2p` service). Phone's built-in "Screen Cast" or "Wireless Display" won't find it.

**Use Chromecast protocol instead:**
1. Install **Google Home** app on your phone
2. Ensure phone is on the **same Wi-Fi network** as STB
3. Open Google Home > find **"Navi Xstream"** (or your STB's name) > **Cast my screen**

---

## Phase 8: Launcher Configuration

### 8.1 Push Config File

Create a `config.json` and push it to the STB:

```json
{
    "dock_apps": [
        "com.google.android.youtube.tv",
        "com.debloat.castlauncher",
        "zank.remote",
        "com.android.vending",
        "com.android.tv.settings"
    ],
    "columns": 5,
    "app_library": true,
    "app_library_button": true,
    "library_ok_opens": true,
    "tile_menu": true,
    "launcher_settings_button": true,
    "show_network_pill": true,
    "wifi_status_color": true,
    "wifi_status_warnings": true,
    "show_greeting": true,
    "dock_title": true,
    "ambient_glow": true,
    "reduced_motion": true,
    "advanced_effects": false,
    "animations": true,
    "clock_format": "12",
    "hidden_apps": [
        "com.gathin.tvhome",
        "com.debloat.adbaccess",
        "com.debloat.adbenable",
        "moe.shizuku.privileged.api"
    ]
}
```

```bash
adb push config.json /sdcard/Android/data/com.gathin.tvhome/files/config.json
adb shell am force-stop com.gathin.tvhome
adb shell input keyevent KEYCODE_HOME
```

### 8.2 Customize Dock Apps

Edit the `dock_apps` array in config.json. Use package names of installed apps. Some useful ones:

| App | Package Name |
|-----|-------------|
| YouTube | `com.google.android.youtube.tv` |
| Play Store | `com.android.vending` |
| Settings | `com.android.tv.settings` |
| CastLauncher | `com.debloat.castlauncher` |
| LocalSend | `org.localsend.localsend_app` |
| Zank Remote | `zank.remote` |

### 8.3 Hide Utility Apps

Add package names to the `hidden_apps` array to hide them from the app grid while keeping them functional.

---

## Known Issues and Solutions

### Apps disappear after uninstall/reinstall

**Never use `pm uninstall -k --user 0` on user-installed (non-system) apps.** The `-k` flag only preserves data for system apps. For user-installed apps, it deletes the APK entirely, leaving a ghost entry.

If this happens:
```bash
# Fully remove the ghost entry
adb shell pm uninstall --user 0 <package>
# Then reinstall from Play Store
```

### Play Store shows "Open" button missing for sideloaded apps

Apps installed via `adb install` are user-scoped by default. This is normal. The Play Store shows "Open" for apps it manages, and "Uninstall" for sideloaded ones.

### Launcher crashes with NullPointerException

After installing CleanYourTV, the first HOME press may throw an NPE. Wait 2 seconds and press HOME again — the launcher initializes on second launch.

### Cast receiver not initialized after reboot

The Cast receiver (`mediashell`) sometimes doesn't auto-start. Install the CastLauncher helper app which starts it on boot, or run manually:

```bash
adb shell am startservice -n com.google.android.apps.mediashell/.MediaShellCastReceiverService
```

### Phone can't find STB for screen mirroring

The STB has no Miracast/WiFi Direct support. Use **Google Home app > Cast my screen** instead of the phone's built-in Cast/Screen Mirror feature.

### Settings layout too large

The STB's display density is 320 (effective resolution 960x540dp). UI elements designed for 160dpi TVs render 2x too large. If building CleanYourTV from source, adjust layout XML sizes accordingly.

---

## Restore / Undo Everything

If you need to restore the STB to its pre-debloat state, run the restore script:

```bash
adb shell << 'EOF'
# Re-enable all disabled packages
pm enable --user 0 com.airtel.tv
pm enable --user 0 tv.airtel.xstream.tvapp
pm enable --user 0 com.airtel.fti
pm enable --user 0 com.uei.uas.airtel
pm enable --user 0 com.tataelxsi.icx
pm enable --user 0 com.technicolor.android.BootupTraceQR
pm enable --user 0 com.apps.updater
pm enable --user 0 com.firmware.update
pm enable --user 0 com.technicolor.checkota
pm enable --user 0 com.google.android.tv.bugreportsender
pm enable --user 0 com.google.android.feedback
pm enable --user 0 com.google.android.onetimeinitializer
pm enable --user 0 com.google.android.partnersetup
pm enable --user 0 com.google.android.syncadapters.calendar
pm enable --user 0 com.google.android.tv.frameworkpackagestubs
pm enable --user 0 com.google.android.sss.authbridge
pm enable --user 0 com.google.android.tungsten.setupwraith
pm enable --user 0 com.android.printspooler
pm enable --user 0 com.android.wallpaperbackup
pm enable --user 0 com.android.dreams.basic
pm enable --user 0 com.android.htmlviewer
pm enable --user 0 com.broadcom.nrdphelper
pm enable --user 0 com.broadcom.dnyhelper
pm enable --user 0 com.broadcom.customizer
pm enable --user 0 com.technicolor.avsignalstatus
pm enable --user 0 com.technicolor.onetouchplay
pm enable --user 0 com.technicolor.android.hddtemperaturemonitor
pm enable --user 0 android.autoinstalls.config.technicolor
pm enable --user 0 android.autoinstalls.config.bcm.device
pm enable --user 0 com.technicolor.leanbacklauncher.partnercustomizer
pm enable --user 0 com.android.managedprovisioning
pm enable --user 0 com.android.companiondevicemanager
pm enable --user 0 com.google.android.marvin.talkback
pm enable --user 0 com.amazon.amazonvideo.livingroom
pm enable --user 0 com.google.android.tvrecommendations
pm enable --user 0 com.google.android.youtube.tvmusic
pm enable --user 0 com.google.android.videos
pm enable --user 0 com.google.android.play.games
pm enable --user 0 com.netflix.ninja

# Restore settings
settings put global ota_disable_automatic_update 0
settings put global package_verifier_enable 1
settings put global verifier_verify_adb_installs 1
settings put global upload_apk_enable 1
settings put global background_process_limit -1
settings put global window_animation_scale 1.0
settings put global transition_animation_scale 1.0
settings put global animator_duration_scale 1.0

# Remove launcher config
rm -f /sdcard/Android/data/com.gathin.tvhome/files/config.json
EOF
```

Then uninstall CleanYourTV and reboot:
```bash
adb shell pm uninstall com.gathin.tvhome
adb reboot
```

---

## Claude Prompt for Automated Setup

If you want to use Claude (AI) to automate the entire debloat process, copy the prompt below after completing the prerequisites. This prompt is specific to the Airtel Xstream Smart Box and assumes ADB is already enabled using our [EnableGreyedADB](README.md) method.

### Prerequisites Before Using the Prompt

1. ADB is enabled on the STB (using the EnableGreyedADB app)
2. You know the STB's IP address: `Settings > Device Preferences > About > Status > IP Address`
3. You've verified ADB works: `adb connect <STB_IP>:5555` shows `connected`
4. You've downloaded CleanYourTV APK from https://github.com/Gathin23/cleanyourtv/releases
5. (Optional) You have the CastLauncher APK from this repo's `castlauncher/` folder

### The Prompt

Copy everything between the triple backticks and paste into Claude Code (or any Claude session with shell/ADB access):

````
I have an Airtel Xstream Smart Box (Technicolor HSW4026ATL, Android 11, SDK 30, density 320, 1920x1080, 1.5GB RAM). ADB is already enabled and connected.

Device IP: <REPLACE_WITH_YOUR_STB_IP>
ADB target: <REPLACE_WITH_YOUR_STB_IP>:5555

I want to fully debloat it and set up CleanYourTV launcher. Here is the exact plan:

## Safety Rules (MUST follow):
- Use `pm disable-user --user 0` to disable packages. NEVER use `pm uninstall` on system packages.
- Run pm commands ONE PER SHELL CALL. Chained commands silently stop after the first.
- NEVER disable: com.technicolor.enableUpdates (this is what enabled ADB), Play Store (com.android.vending), Play Services (com.google.android.gms), TV input services, remote/bluetooth services, keyboard, or Android core packages.
- NEVER launch any package with factory/production/AT/fac in its name.
- Before disabling any streaming app, Chromecast, Miracast, voice assistant, or update service — ask me first.

## Step 1: Verify ADB connection
Run: adb connect <REPLACE_WITH_YOUR_STB_IP>:5555
Verify: adb devices shows "device" (not offline/unauthorized)
Get device info: model, SDK, density, installed packages count

## Step 2: Install CleanYourTV launcher
Install the APK I have at: <REPLACE_WITH_PATH_TO_CLEANYOURTV_APK>
Grant permissions: adb shell pm grant com.gathin.tvhome android.permission.WRITE_SECURE_SETTINGS
Set as default home launcher.

## Step 3: Disable Airtel bloatware (run each command separately)
adb shell pm disable-user --user 0 tv.airtel.xstream.tvapp
adb shell pm disable-user --user 0 com.airtel.tv
adb shell pm disable-user --user 0 com.airtel.fti
adb shell pm disable-user --user 0 com.uei.uas.airtel

## Step 4: Disable telemetry
adb shell pm disable-user --user 0 com.tataelxsi.icx
adb shell pm disable-user --user 0 com.technicolor.android.BootupTraceQR

## Step 5: Disable update services
adb shell pm disable-user --user 0 com.apps.updater
adb shell pm disable-user --user 0 com.firmware.update
adb shell pm disable-user --user 0 com.technicolor.checkota

## Step 6: Disable Google bloat
adb shell pm disable-user --user 0 com.google.android.tv.bugreportsender
adb shell pm disable-user --user 0 com.google.android.feedback
adb shell pm disable-user --user 0 com.google.android.onetimeinitializer
adb shell pm disable-user --user 0 com.google.android.partnersetup
adb shell pm disable-user --user 0 com.google.android.syncadapters.calendar
adb shell pm disable-user --user 0 com.google.android.tv.frameworkpackagestubs
adb shell pm disable-user --user 0 com.google.android.sss.authbridge
adb shell pm disable-user --user 0 com.google.android.tungsten.setupwraith

## Step 7: Disable unused streaming apps
adb shell pm disable-user --user 0 com.amazon.amazonvideo.livingroom
adb shell pm disable-user --user 0 com.netflix.ninja
adb shell pm disable-user --user 0 com.google.android.youtube.tvmusic
adb shell pm disable-user --user 0 com.google.android.videos
adb shell pm disable-user --user 0 com.google.android.play.games

## Step 8: Disable system bloat
adb shell pm disable-user --user 0 com.android.printspooler
adb shell pm disable-user --user 0 com.android.wallpaperbackup
adb shell pm disable-user --user 0 com.android.dreams.basic
adb shell pm disable-user --user 0 com.android.htmlviewer
adb shell pm disable-user --user 0 com.android.companiondevicemanager
adb shell pm disable-user --user 0 com.google.android.marvin.talkback
adb shell pm disable-user --user 0 com.google.android.tvrecommendations

## Step 9: Disable OEM bloat
adb shell pm disable-user --user 0 com.broadcom.nrdphelper
adb shell pm disable-user --user 0 com.broadcom.dnyhelper
adb shell pm disable-user --user 0 com.broadcom.customizer
adb shell pm disable-user --user 0 com.technicolor.avsignalstatus
adb shell pm disable-user --user 0 com.technicolor.onetouchplay
adb shell pm disable-user --user 0 com.technicolor.android.hddtemperaturemonitor
adb shell pm disable-user --user 0 android.autoinstalls.config.technicolor
adb shell pm disable-user --user 0 android.autoinstalls.config.bcm.device
adb shell pm disable-user --user 0 com.technicolor.leanbacklauncher.partnercustomizer
adb shell pm disable-user --user 0 com.android.managedprovisioning

## Step 10: Performance optimization
adb shell settings put global window_animation_scale 0.5
adb shell settings put global transition_animation_scale 0.5
adb shell settings put global animator_duration_scale 0.5
adb shell settings put global background_process_limit 4
adb shell cmd package bg-dexopt-job

## Step 11: Lock down updates
adb shell settings put global ota_disable_automatic_update 1
adb shell settings put global package_verifier_enable 0
adb shell settings put global verifier_verify_adb_installs 0
adb shell settings put global upload_apk_enable 0

## Step 12: Fix Chromecast
adb shell am force-stop com.google.android.apps.mediashell
adb shell am startservice -n com.google.android.apps.mediashell/.MediaShellCastReceiverService
Verify ports 8008/8009 are listening: adb shell netstat -tlnp | grep 8008

## Step 13: Configure launcher
Push this config.json to /sdcard/Android/data/com.gathin.tvhome/files/config.json:
{
    "dock_apps": [
        "com.google.android.youtube.tv",
        "com.android.vending",
        "com.android.tv.settings"
    ],
    "columns": 5,
    "app_library": true,
    "app_library_button": true,
    "library_ok_opens": true,
    "tile_menu": true,
    "launcher_settings_button": true,
    "show_network_pill": true,
    "wifi_status_color": true,
    "wifi_status_warnings": true,
    "show_greeting": true,
    "dock_title": true,
    "ambient_glow": true,
    "reduced_motion": true,
    "advanced_effects": false,
    "animations": true,
    "clock_format": "12",
    "hidden_apps": [
        "com.gathin.tvhome",
        "com.debloat.adbaccess",
        "com.debloat.adbenable",
        "moe.shizuku.privileged.api"
    ]
}

Then restart the launcher:
adb shell am force-stop com.gathin.tvhome
adb shell input keyevent KEYCODE_HOME

## Step 14: Verify everything
- List disabled packages: adb shell pm list packages -d (should be ~40)
- Check launcher is home: press HOME on remote
- Check Chromecast: adb shell netstat -tlnp | grep 8008
- Report summary of what was done

Execute each step in order. Run each pm command as a separate adb shell call. Report progress after each phase. If any command fails, report the error and continue to the next.
````

Replace `<REPLACE_WITH_YOUR_STB_IP>` with your STB's actual IP address (e.g., `192.168.1.36`) and `<REPLACE_WITH_PATH_TO_CLEANYOURTV_APK>` with the path to the APK on your computer.

---

## Files Reference

| File | Description |
|------|-------------|
| [`app/EnableADB.apk`](app/EnableADB.apk) | Pre-built APK to enable greyed-out ADB |
| [`app/src/`](app/src/) | Source code for the ADB enabler app |
| [`castlauncher/`](castlauncher/) | CastLauncher helper app (source + APK) |
| [`restore.sh`](restore.sh) | Script to undo all debloat changes |
| [`config.json`](config.json) | Sample CleanYourTV launcher configuration |

---

*This guide was tested on Airtel Xstream Smart Box (Technicolor HSW4026ATL, Android 11, firmware hsw4026atl-5.0.2803). Results may vary on different firmware versions. Always keep the restore script handy before making changes.*
