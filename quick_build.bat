@echo off
echo Building GroceryGo App...
cd /d D:\GoGoGoGrocery2
call gradlew.bat assembleDebug --stacktrace
if errorlevel 1 (
    echo.
    echo BUILD FAILED! Check errors above.
    pause
    exit /b 1
)
echo.
echo BUILD SUCCESSFUL! APK location:
echo app\build\outputs\apk\debug\app-debug.apk
echo.
pause

