@echo off
echo Building Templater for Rider...
call gradlew.bat clean buildPlugin -Pvariant=rider
echo.
echo Build complete: build\distributions\rider-templater-*.zip

