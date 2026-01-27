@echo off
echo =====================================
echo Building VanillaRPC...
echo =====================================
echo.

call gradlew.bat clean build

echo.
echo =====================================
echo Build complete!
echo Your mod is in: build\libs\
echo =====================================
pause
