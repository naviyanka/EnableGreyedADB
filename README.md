# EnableGreyedADB

**Enable USB Debugging on Android TV set-top boxes where the ADB toggle is greyed out.**

Tested on the **Airtel Xstream Smart Box** (Technicolor HSW4026ATL, Android 11, SDK 30), where USB Debugging is locked by the operator. This exploit uses the Technicolor content provider to flip `adb_enabled` via an Accessibility Service — no root, no unlocked bootloader required.

This same approach may work on other Technicolor-based Android TV devices where the manufacturer exposes a writable content provider.

---

## Table of Contents

- [Background](#background)
- [What You Need](#what-you-need)
- [Step-by-Step Guide](#step-by-step-guide)
  - [Step 1: Factory Reset](#step-1-factory-reset)
  - [Step 2: Access Settings](#step-2-access-settings)
  - [Step 3: Install Play Store Apps](#step-3-install-play-store-apps)
  - [Step 4: Take a Screenshot](#step-4-take-a-screenshot)
  - [Step 5: Transfer the APK](#step-5-transfer-the-apk)
  - [Step 6: Install the APK on STB](#step-6-install-the-apk-on-stb)
  - [Step 7: Enable the Accessibility Service](#step-7-enable-the-accessibility-service)
  - [Step 8: Connect via ADB](#step-8-connect-via-adb)
- [How It Works](#how-it-works)
- [Building from Source](#building-from-source)
- [Troubleshooting](#troubleshooting)
- [Disclaimer](#disclaimer)

---

## Background

On certain operator-locked Android TV set-top boxes (like the Airtel Xstream Smart Box), the **USB Debugging** toggle in Developer Options is greyed out and cannot be enabled through the UI. The operator disables it via device policy to prevent sideloading and debugging.

However, the Technicolor firmware exposes a content provider (`content://com.technicolor.contentprovider`) that stores device configuration including the ADB flag. By writing to this content provider from an Accessibility Service (which has elevated privileges), we can flip `adb_enabled` to `1` and get a working ADB connection over the network.

Once ADB is enabled, you can connect from your computer and have full shell access to the device.

---

## What You Need

| Item | Purpose |
|------|---------|
| Airtel Xstream Smart Box (or similar Technicolor STB) | The target device |
| USB keyboard | Navigate the STB UI and take screenshots |
| USB mouse (optional) | Easier navigation |
| Android phone | Transfer files to/from STB |
| Computer with ADB installed | Connect to the STB after enabling ADB |
| Wi-Fi network | Both STB and computer on same network |

---

## Step-by-Step Guide

### Step 1: Factory Reset

If you haven't already, factory reset your STB to start clean:

**Settings > Device Preferences > Reset**

After reset, go through the initial setup. Connect to your Wi-Fi network and sign in to your Google account when prompted.

### Step 2: Access Settings

After setup, you'll be on the Airtel launcher which has limited options. To access full Android TV Settings:

1. Grab your **Airtel remote**
2. **Press and hold** the button at the **bottom-right** (next to the number `0`) — this is the **Options/Menu** button
3. While holding that, press the **Settings gear icon** button
4. This opens the full Android TV Settings menu

### Step 3: Install Play Store Apps

From the Settings menu:

1. Navigate to **Apps > See all apps > Google Play Store > Open**
2. Sign in if prompted
3. Search for and install **"Send Files to TV" (SFTTV)** by Yablio

> This app lets you transfer files between your phone and the STB over Wi-Fi.

Also install **SFTTV** on your Android phone from the Play Store.

### Step 4: Take a Screenshot

You need to get the `EnableADB.apk` file onto the STB. Since there's no file manager or browser access yet, we use a creative workaround:

1. **Attach a USB keyboard** to the STB
2. Press **`Ctrl + Windows + S`** on the keyboard — this captures a screenshot on the STB
3. A small preview appears in the bottom-left corner of the screen
4. **Hover your mouse** over it (or right-click on it)
5. You'll see a **Share** option — click it
6. Choose **"Send Files to TV"** from the share menu

This sends the screenshot from the STB to your phone via SFTTV. The point isn't the screenshot itself — it's to establish a connection between the STB and phone via SFTTV.

### Step 5: Transfer the APK

Now that SFTTV is working between your phone and STB:

**Option A: Use the pre-built APK**

1. Download [`EnableADB.apk`](app/EnableADB.apk) from this repository to your phone
2. Open SFTTV on your phone
3. Send the APK to the STB

**Option B: Build the APK yourself**

See [Building from Source](#building-from-source) below, then transfer the built APK to your phone and send it to the STB via SFTTV.

### Step 6: Install the APK on STB

On the STB:

1. Open SFTTV (or your file manager)
2. Find the received `EnableADB.apk` file
3. Tap on it > **Open**
4. Choose **"Package Installer"** when prompted
5. Allow installation from unknown sources if asked
6. Install and **Open** the app

The app shows a status screen with several buttons. You don't need to press anything yet — the magic happens via the Accessibility Service.

### Step 7: Enable the Accessibility Service

This is the key step:

1. Using your remote, navigate to **Settings > Device Preferences > Accessibility**
2. Find **"ADB Enable v11"** in the list
3. **Enable it**

Once enabled, the Accessibility Service runs automatically and:
- Writes to the Technicolor content provider to set `adb=1`
- Triggers system services that read the ADB flag
- Attempts shell commands to start `adbd`
- Opens Developer Options and tries to click the USB Debugging toggle via UI automation
- Confirms ADB is enabled by checking port 5555

**A green overlay will appear on screen showing the progress.** Look for the line `PORT 5555=OPEN!!!` — that means ADB is running.

### Step 8: Connect via ADB

On your computer (same Wi-Fi network as the STB):

```bash
# Find STB IP in Settings > Network & Internet > [Your WiFi] > IP Address
adb connect <STB_IP>:5555

# Verify
adb devices

# Get a shell
adb shell
```

You now have full ADB access.

---

## How It Works

The app uses a multi-phase approach since different devices may respond to different methods:

### Phase 1: Content Provider Exploit

The Technicolor firmware includes an unprotected content provider at `content://com.technicolor.contentprovider`. It stores key-value pairs in a SQLite table called `tch_info`, including the `adb` flag.

The app writes `adb=1` to this provider using multiple methods:
- `INSERT` on the base URI
- `INSERT` on the `/info` URI
- `UPDATE` with different WHERE clauses
- `DELETE` + re-`INSERT`

When the system reads this value, it enables ADB.

### Phase 2: Service Triggers

The app starts Technicolor system services that read the content provider and apply the ADB setting:
- `EnableService` — the primary service that reads and applies device config
- `AtlSystemService` — Airtel's system service
- `TchVendorIntfService` — Technicolor vendor interface
- Sends `BOOT_COMPLETED` broadcast to trigger config reload

### Phase 3: Shell Commands

Direct shell attempts (may work if the content provider alone doesn't):
```
settings put global adb_enabled 1
setprop persist.sys.usb.config adb
start adbd
```

### Phase 4: UI Automation

As a last resort, the Accessibility Service:
1. Opens Developer Options
2. Finds the "USB Debugging" toggle in the accessibility tree
3. Clicks it programmatically (bypassing the greyed-out UI state)
4. Automatically confirms any "Allow USB debugging?" dialog

---

## Building from Source

### Prerequisites

- **Java JDK 21** (or 17+)
- **Android SDK** with:
  - Build Tools 36.0.0 (or similar)
  - Platform `android-34` or higher

### Build

**Windows (PowerShell):**

```powershell
# Set paths (adjust for your system)
$SDK = "$env:LOCALAPPDATA\Android\Sdk"
$BT  = "$SDK\build-tools\36.0.0"
$AJ  = "$SDK\platforms\android-34\android.jar"  # or android-37.0
$JDK = "C:\Program Files\Java\jdk-21"
$P   = "path\to\EnableGreyedADB\app"

# Compile Java
& "$JDK\bin\javac.exe" -source 8 -target 8 -bootclasspath $AJ `
    -d "$P\classes" `
    "$P\src\com\debloat\adbaccess\AdbService.java" `
    "$P\src\com\debloat\adbaccess\MainActivity.java"

# Convert to DEX
& "$BT\d8.bat" --min-api 21 --output "$P\out" (Get-ChildItem "$P\classes" -Recurse -Filter "*.class" | % { $_.FullName })

# Compile resources
& "$BT\aapt2.exe" compile --dir "$P\res" -o "$P\compiled_res.zip"

# Link APK
& "$BT\aapt2.exe" link -I $AJ --manifest "$P\AndroidManifest.xml" `
    --min-sdk-version 21 --target-sdk-version 29 -o "$P\app-unaligned.apk" "$P\compiled_res.zip"

# Add DEX
& "$JDK\bin\jar.exe" uf "$P\app-unaligned.apk" -C "$P\out" classes.dex

# Align
& "$BT\zipalign.exe" -f 4 "$P\app-unaligned.apk" "$P\app-aligned.apk"

# Generate keystore (first time only)
& "$JDK\bin\keytool.exe" -genkeypair -keystore "$P\ks.jks" -alias a `
    -keyalg RSA -keysize 2048 -validity 10000 `
    -storepass android -keypass android -dname "CN=debloat"

# Sign
& "$BT\apksigner.bat" sign --ks "$P\ks.jks" --ks-pass pass:android `
    --key-pass pass:android --out "$P\EnableADB.apk" "$P\app-aligned.apk"
```

**Linux/macOS:**

```bash
SDK="$HOME/Android/Sdk"
BT="$SDK/build-tools/36.0.0"
AJ="$SDK/platforms/android-34/android.jar"
P="path/to/EnableGreyedADB/app"

javac -source 8 -target 8 -bootclasspath "$AJ" \
    -d "$P/classes" \
    "$P/src/com/debloat/adbaccess/AdbService.java" \
    "$P/src/com/debloat/adbaccess/MainActivity.java"

"$BT/d8" --min-api 21 --output "$P/out" $(find "$P/classes" -name "*.class")

"$BT/aapt2" compile --dir "$P/res" -o "$P/compiled_res.zip"

"$BT/aapt2" link -I "$AJ" --manifest "$P/AndroidManifest.xml" \
    --min-sdk-version 21 --target-sdk-version 29 -o "$P/app-unaligned.apk" "$P/compiled_res.zip"

jar uf "$P/app-unaligned.apk" -C "$P/out" classes.dex

"$BT/zipalign" -f 4 "$P/app-unaligned.apk" "$P/app-aligned.apk"

keytool -genkeypair -keystore "$P/ks.jks" -alias a \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass android -keypass android -dname "CN=debloat"

"$BT/apksigner" sign --ks "$P/ks.jks" --ks-pass pass:android \
    --key-pass pass:android --out "$P/EnableADB.apk" "$P/app-aligned.apk"
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Screenshot shortcut doesn't work | Try `Ctrl + Win + S` or `Ctrl + Shift + S`. Some STBs use different combos. |
| SFTTV can't find the STB | Ensure both devices are on the same Wi-Fi network. |
| APK won't install | Go to Settings > Security > Unknown sources > Enable for SFTTV or the file manager. |
| Accessibility Service not listed | Reboot the STB after installing the APK. |
| ADB doesn't enable (port 5555 closed) | Try rebooting the STB. The content provider write persists and may take effect on next boot. |
| `adb connect` refused | Check the STB's IP address. Ensure your computer is on the same network. Try `adb connect <IP>:5555`. |
| ADB works but disconnects after reboot | The content provider write should persist across reboots. If not, re-enable the Accessibility Service. |

---

## Tested Devices

| Device | Model | Android | Status |
|--------|-------|---------|--------|
| Airtel Xstream Smart Box | Technicolor HSW4026ATL | Android 11 (SDK 30) | Working |

If you test on another device, please open an issue or PR with results.

---

## Disclaimer

This tool is provided for **educational and personal use** on devices you own. Enabling ADB bypasses operator restrictions. Use responsibly and at your own risk.

---

## License

MIT License. See [LICENSE](LICENSE) for details.
