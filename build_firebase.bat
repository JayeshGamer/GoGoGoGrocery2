@echo off
echo ========================================
echo Building GoGoGoGrocery with Firebase
echo ========================================
echo.

echo Step 1: Cleaning project...
call gradlew clean

echo.
echo Step 2: Building project...
call gradlew assembleDebug

echo.
echo ========================================
echo Build complete!
echo ========================================
pause

