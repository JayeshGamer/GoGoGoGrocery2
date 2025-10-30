@echo off
echo ========================================
echo Quick Security Error Fix
echo ========================================
echo.

echo Getting SHA-1 fingerprint...
cd %USERPROFILE%\.android
for /f "tokens=2" %%i in ('keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android ^| findstr "SHA1:"') do set SHA1=%%i

echo.
echo YOUR SHA-1 FINGERPRINT: %SHA1%
echo.
echo COPY THIS AND ADD IT TO FIREBASE!
echo.
echo Opening Firebase Console...
start https://console.firebase.google.com/project/grocerygo123456/settings/general
echo.
echo Instructions:
echo 1. In the opened browser, scroll to "Your apps"
echo 2. Find com.grocerygo.app
echo 3. Click "Add fingerprint"
echo 4. Paste: %SHA1%
echo 5. Download new google-services.json and replace it in app folder
echo.
pause

echo.
echo Cleaning and rebuilding...
cd C:\Users\vmand\AndroidStudioProjects\GoGoGoGrocery
call gradlew.bat clean
call gradlew.bat --stop

echo.
echo Resetting ADB and Google Play Services...
adb kill-server
timeout /t 2 /nobreak >nul
adb start-server
adb shell pm clear com.google.android.gms
adb uninstall com.grocerygo.app

echo.
echo Building and installing fresh...
call gradlew.bat installDebug

echo.
echo Launching app...
adb shell am start -n com.grocerygo.app/com.grocerygo.SplashActivity

echo.
echo ========================================
echo Done! Check if the error is fixed.
echo ========================================
pause

