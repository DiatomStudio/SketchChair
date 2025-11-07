@echo off
REM SketchChair Windows Launcher
REM This script properly launches SketchChair with required JVM options for Windows
REM Requires JDK 17+ for Processing 4 compatibility with JOGL 2.x

REM Get the directory where this script is located
set SCRIPT_DIR=%~dp0

REM Change to the build directory
cd "%SCRIPT_DIR%build"

REM Launch SketchChair with required JVM options
java ^
     -Djogamp.gluegen.UseTempJarCache=true ^
     -jar SketchChair-standard.jar

pause
