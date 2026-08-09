@echo off
setlocal

echo ============================================
echo  Building Fake Money Scoreboard
echo ============================================
echo.

if exist "gradlew.bat" (
    call gradlew.bat build
) else (
    echo No Gradle wrapper found in this folder.
    echo Falling back to a system-installed "gradle" command.
    echo ^(If this fails, run "gradle wrapper" once first - see README.md^)
    call gradle build
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ============================================
    echo  Build FAILED. Scroll up for the error.
    echo ============================================
    exit /b %ERRORLEVEL%
)

echo.
echo ============================================
echo  Build succeeded!
echo  JAR location: build\libs\fake-money-scoreboard-1.0.0.jar
echo ============================================
pause
