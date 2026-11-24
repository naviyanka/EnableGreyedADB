$ErrorActionPreference = "Stop"
$P   = "C:\Users\nsaha\Downloads\castlauncher"
$SDK = "$env:LOCALAPPDATA\Android\Sdk"
$BT  = "$SDK\build-tools\36.0.0"
$AJ  = "$SDK\platforms\android-37.0\android.jar"
$JDK = "C:\Program Files\Java\jdk-21"
$KS  = "C:\Users\nsaha\Downloads\adbaccess\ks.jks"

Set-Location $P
Remove-Item "$P\classes","$P\out","$P\compiled_res.zip","$P\app-unaligned.apk","$P\app-aligned.apk","$P\CastLauncher.apk" -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force "$P\classes","$P\out" | Out-Null

Write-Output "== javac =="
& "$JDK\bin\javac.exe" -source 8 -target 8 -bootclasspath $AJ -d "$P\classes" "$P\src\com\debloat\castlauncher\CastActivity.java" "$P\src\com\debloat\castlauncher\BootReceiver.java"

Write-Output "== d8 =="
$allClasses = Get-ChildItem "$P\classes" -Recurse -Filter "*.class" | ForEach-Object { $_.FullName }
& "$BT\d8.bat" --min-api 21 --output "$P\out" @allClasses

Write-Output "== aapt2 compile res =="
& "$BT\aapt2.exe" compile --dir "$P\res" -o "$P\compiled_res.zip"

Write-Output "== aapt2 link =="
& "$BT\aapt2.exe" link -I $AJ --manifest "$P\AndroidManifest.xml" --min-sdk-version 21 --target-sdk-version 30 -o "$P\app-unaligned.apk" "$P\compiled_res.zip"

Write-Output "== add classes.dex =="
& "$JDK\bin\jar.exe" uf "$P\app-unaligned.apk" -C "$P\out" classes.dex

Write-Output "== zipalign =="
& "$BT\zipalign.exe" -f 4 "$P\app-unaligned.apk" "$P\app-aligned.apk"

Write-Output "== sign =="
& "$BT\apksigner.bat" sign --ks $KS --ks-pass pass:android --key-pass pass:android --out "$P\CastLauncher.apk" "$P\app-aligned.apk"

Write-Output "== verify =="
& "$BT\apksigner.bat" verify "$P\CastLauncher.apk"
Get-Item "$P\CastLauncher.apk" | Select-Object FullName,Length
