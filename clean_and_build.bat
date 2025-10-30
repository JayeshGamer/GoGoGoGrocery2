@echo off
echo Cleaning build cache...
cd /d D:\GoGoGoGrocery2

REM Clean the project
gradlew clean

echo Build cleaned successfully!
echo.
echo Now building the project...

REM Build the debug APK
gradlew assembleDebug

echo.
echo Build complete!
pause

