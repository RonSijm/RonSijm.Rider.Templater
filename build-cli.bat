@echo off
echo Building Templater CLI (JAR and batch launcher)...
call gradlew.bat :cli:shadowJar :cli:createBatchFile
if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful!
    echo Outputs:
    echo   - build\cli\templater.jar
    echo   - build\cli\templater.bat ^(Windows launcher^)
    echo.
    echo Usage ^(JAR^):   java -jar build\cli\templater.jar ^<input.md^> [options]
    echo Usage ^(Batch^): build\cli\templater.bat ^<input.md^> [options]
    echo.
    echo Note: The batch file can be double-clicked or run from command line
) else (
    echo.
    echo Build failed!
    exit /b 1
)
