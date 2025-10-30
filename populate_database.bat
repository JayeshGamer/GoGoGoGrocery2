@echo off
echo ========================================
echo Database Populator Script
echo ========================================
echo.
echo This script will help you populate the Firebase database
echo with sample categories and products.
echo.
echo Instructions:
echo 1. Make sure your app is installed on your device/emulator
echo 2. The app will launch to the Database Populator screen
echo 3. Click the "Populate Database" button in the app
echo.
pause

echo.
echo Launching Database Populator Activity...
echo.

adb shell am start -n com.grocerygo/.DatabasePopulatorActivity

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Success! The Database Populator has been launched.
    echo ========================================
    echo.
    echo Next steps:
    echo - Click "Populate Database" button in the app
    echo - Wait for the process to complete
    echo - You will see 12 categories and 70+ products added
    echo.
) else (
    echo.
    echo ========================================
    echo Error: Failed to launch the activity
    echo ========================================
    echo.
    echo Possible reasons:
    echo - App is not installed
    echo - Device/Emulator is not connected
    echo - ADB is not in your PATH
    echo.
    echo Please make sure:
    echo 1. Your device/emulator is connected
    echo 2. The app is installed
    echo 3. Run "gradlew installDebug" to install the app
    echo.
)

pause

