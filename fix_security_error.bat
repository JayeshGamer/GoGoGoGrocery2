@echo off
echo ========================================
echo Google Play Services Security Error Fix
echo ========================================
echo.

echo Step 1: Getting SHA-1 certificate fingerprint...
echo.
cd %USERPROFILE%\.android
echo === DEBUG KEYSTORE SHA-1 ===
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android | findstr SHA1
echo.
echo === SHA-256 (also useful) ===
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android | findstr SHA256
echo.
echo IMPORTANT: Copy the SHA-1 and SHA-256 fingerprints above!
echo.

echo Step 2: Add SHA-1 to Firebase Console
echo 1. Go to: https://console.firebase.google.com/
echo 2. Select project: grocerygo123456
echo 3. Project Settings ^> General
echo 4. Scroll to "Your apps" section
echo 5. Click your Android app (com.grocerygo.app)
echo 6. Click "Add fingerprint"
echo 7. Paste the SHA-1 from above
echo 8. Click "Add fingerprint" again and add SHA-256 too
echo 9. Save
echo.
echo Press any key AFTER you have added the fingerprints to Firebase...
pause

echo.
echo Step 3: Downloading updated google-services.json...
echo IMPORTANT: After adding SHA-1, download the new google-services.json
echo 1. In Firebase Console, click the Settings icon next to your app
echo 2. Click "google-services.json" to download
echo 3. Replace the file at: C:\Users\vmand\AndroidStudioProjects\GoGoGoGrocery\app\google-services.json
echo.
echo Press any key AFTER you have downloaded and replaced google-services.json...
pause

echo.
echo Step 4: Cleaning project...
cd C:\Users\vmand\AndroidStudioProjects\GoGoGoGrocery
call gradlew.bat clean

echo.
echo Step 5: Building project with new dependencies...
call gradlew.bat assembleDebug

echo.
echo Step 6: Stopping ADB server and restarting...
adb kill-server
adb start-server

echo.
echo Step 7: Clearing Google Play Services cache on device...
adb shell pm clear com.google.android.gms

echo.
echo Step 8: Uninstalling old app...
adb uninstall com.grocerygo.app

echo.
echo Step 9: Installing new build...
call gradlew.bat installDebug

echo.
echo Step 10: Clearing app data and restarting...
adb shell pm clear com.grocerygo.app
adb shell am start -n com.grocerygo.app/com.grocerygo.SplashActivity

echo.
echo ========================================
echo Fix Complete!
echo ========================================
echo.
echo If the error persists:
echo 1. Make sure you're using a Google Play enabled emulator/device
echo 2. Update Google Play Services on your device/emulator:
echo    - Open Play Store on emulator
echo    - Search for "Google Play Services"
echo    - Update if available
echo 3. Verify SHA-1 was added to Firebase Console
echo 4. Make sure google-services.json was updated
echo 5. Try restarting the emulator/device completely
echo.
pause
