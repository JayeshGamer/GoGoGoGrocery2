@echo off
echo ========================================
echo GroceryGo App - Quick Run Script
echo ========================================
echo.

cd /d D:\GoGoGoGrocery2

echo [1/4] Cleaning previous builds...
call gradlew.bat clean
if errorlevel 1 (
    echo ERROR: Clean failed!
    pause
    exit /b 1
)

echo.
echo [2/4] Building the project...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [3/4] Installing APK on connected device...
call gradlew.bat installDebug
if errorlevel 1 (
    echo ERROR: Installation failed! Make sure a device is connected.
    pause
    exit /b 1
)

echo.
echo [4/4] Launching the app...
adb shell am start -n com.grocerygo.app/com.grocerygo.SplashActivity
if errorlevel 1 (
    echo WARNING: Could not launch app automatically.
    echo Please launch it manually from your device.
)

echo.
echo ========================================
echo App installed successfully!
echo ========================================
pause

