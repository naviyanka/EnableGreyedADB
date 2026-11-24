#!/bin/sh
# Restore script for Airtel Xstream Smart Box
# Re-enables all packages disabled during debloat
# Run: adb -s <TV_IP>:5555 shell < restore.sh

echo "=== Restoring disabled packages ==="

# Airtel bloatware
pm enable --user 0 com.airtel.tv
pm enable --user 0 tv.airtel.xstream.tvapp
pm enable --user 0 com.airtel.fti
pm enable --user 0 com.uei.uas.airtel

# Telemetry / analytics
pm enable --user 0 com.tataelxsi.icx
pm enable --user 0 com.technicolor.android.BootupTraceQR

# Updaters
pm enable --user 0 com.apps.updater
pm enable --user 0 com.firmware.update
pm enable --user 0 com.technicolor.checkota

# Downloaders / bloat
pm enable --user 0 com.esaba.downloader

# Google bloat
pm enable --user 0 com.google.android.tv.bugreportsender
pm enable --user 0 com.google.android.feedback
pm enable --user 0 com.google.android.onetimeinitializer
pm enable --user 0 com.google.android.partnersetup
pm enable --user 0 com.google.android.syncadapters.calendar
pm enable --user 0 com.google.android.tv.frameworkpackagestubs
pm enable --user 0 com.google.android.sss.authbridge
pm enable --user 0 com.google.android.tungsten.setupwraith

# System bloat
pm enable --user 0 com.android.printspooler
pm enable --user 0 com.android.wallpaperbackup
pm enable --user 0 com.android.dreams.basic
pm enable --user 0 com.android.htmlviewer

# Broadcom helpers
pm enable --user 0 com.broadcom.nrdphelper
pm enable --user 0 com.broadcom.dnyhelper
pm enable --user 0 com.broadcom.customizer

# OEM misc
pm enable --user 0 com.technicolor.avsignalstatus
pm enable --user 0 com.technicolor.onetouchplay
pm enable --user 0 com.technicolor.android.hddtemperaturemonitor
pm enable --user 0 android.autoinstalls.config.technicolor
pm enable --user 0 android.autoinstalls.config.bcm.device

# Provisioning / partner (disabled in final lockdown)
pm enable --user 0 com.technicolor.leanbacklauncher.partnercustomizer
pm enable --user 0 com.android.managedprovisioning

# Previously disabled (by user before this session)
pm enable --user 0 com.android.companiondevicemanager
pm enable --user 0 com.google.android.marvin.talkback
pm enable --user 0 com.amazon.amazonvideo.livingroom
pm enable --user 0 com.google.android.tvrecommendations
pm enable --user 0 com.google.android.youtube.tvmusic
pm enable --user 0 com.google.android.videos
pm enable --user 0 com.google.android.play.games
pm enable --user 0 com.netflix.ninja

# Restore update/OTA settings
settings put global ota_disable_automatic_update 0
settings put global package_verifier_enable 1
settings put global verifier_verify_adb_installs 1
settings put global upload_apk_enable 1
settings put global background_process_limit -1

# Restore animation scales
settings put global window_animation_scale 1.0
settings put global transition_animation_scale 1.0
settings put global animator_duration_scale 1.0

# Remove custom launcher config and restore default home
rm -f /sdcard/Android/data/com.gathin.tvhome/files/config.json
settings put secure screensaver_components ""

echo "=== Restore complete. Reboot TV now. ==="
echo "To fully revert: adb -s <TV_IP>:5555 shell pm uninstall com.gathin.tvhome"
echo "Then: adb -s <TV_IP>:5555 reboot"
