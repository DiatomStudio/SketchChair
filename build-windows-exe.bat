@echo off
REM Build Windows .exe installer with embedded JRE using jpackage
echo Building SketchChair Windows Installer...

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot
set ANT_HOME=C:\ant\apache-ant-1.10.15
set WIX=C:\Program Files (x86)\WiX Toolset v3.14
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%WIX%\bin;%PATH%

cd /d C:\Git\Diatom\SketchChair

echo Step 1: Building standard JAR...
call ant clean build.standard
if errorlevel 1 (
    echo ERROR: Failed to build JAR
    exit /b 1
)

echo Step 2: Creating Windows .exe installer with jpackage...
call ant jpackage.win
if errorlevel 1 (
    echo ERROR: Failed to create .exe installer
    exit /b 1
)

echo.
echo SUCCESS! Windows installer created at: dist\SketchChair-1.0.exe
echo.
dir dist\*.exe
