@echo off
echo Building Templater for IntelliJ IDEA...
call gradlew.bat clean buildPlugin -Pvariant=intellij
echo.
echo Build complete: build\distributions\intellij-templater-*.zip

