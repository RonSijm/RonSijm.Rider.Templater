@echo off
echo Building Templater Plugin...
call gradlew.bat :plugin:buildPlugin
if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful!
    echo Output: build\plugin\templater-1.1.0.zip
    echo.
    echo Install in IDE: Settings ^> Plugins ^> Gear icon ^> Install Plugin from Disk
) else (
    echo.
    echo Build failed!
    exit /b 1
)

