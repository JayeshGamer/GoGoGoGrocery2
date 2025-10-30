@echo off
echo ========================================
echo Gradle Build and Debug Script
echo ========================================
echo.
echo This script will:
echo 1. Clean the project
echo 2. Build the debug APK
echo 3. Install it on your device/emulator
echo 4. Launch the Database Populator
echo.
echo ========================================
echo Step 1: Cleaning project...
echo ========================================
call gradlew.bat clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Clean failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Step 2: Building debug APK...
echo ========================================
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    echo.
    echo Common fixes:
    echo - Check that Java/JDK is installed
    echo - Verify Android SDK path in local.properties
    echo - Run: gradlew.bat --stacktrace for details
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo SUCCESS! APK Built Successfully
echo ========================================
echo.
echo APK location: app\build\outputs\apk\debug\app-debug.apk
echo.
echo ========================================
echo Step 3: Installing APK on device...
echo ========================================
call gradlew.bat installDebug
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Installation failed!
    echo Make sure device/emulator is connected.
    echo You can manually install from: app\build\outputs\apk\debug\app-debug.apk
    echo.
    choice /C YN /M "Continue to launch anyway"
    if errorlevel 2 exit /b 1
)

echo.
echo ========================================
echo Step 4: Launching Database Populator...
echo ========================================
adb shell am start -n com.grocerygo/.DatabasePopulatorActivity
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! Build and Install Complete
    echo ========================================
    echo.
    echo The Database Populator screen is now open.
    echo.
    echo NEXT STEPS:
    echo 1. Click "Populate Database" button in the app
    echo 2. Wait for completion (shows success message)
    echo 3. Browse to Categories to verify products load
    echo 4. No more "no products available" errors!
    echo.
) else (
    echo.
    echo App installed successfully!
    echo You can manually open the app on your device.
    echo.
)

echo Build completed at: %DATE% %TIME%
echo.
pause
