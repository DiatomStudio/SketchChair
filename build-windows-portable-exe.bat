@echo off
REM Build Windows portable app (folder with .exe, no installer needed)
echo Building SketchChair Windows Portable App...

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot
set ANT_HOME=C:\ant\apache-ant-1.10.15
set WIX=C:\Program Files (x86)\WiX Toolset v3.14
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%WIX%\bin;%PATH%

cd /d C:\Git\Diatom\SketchChair

REM Read version from version.properties
for /f "tokens=2 delims==" %%a in ('findstr "^version=" version.properties') do set VERSION=%%a

echo Step 1: Building standard JAR...
call ant clean build.standard
if errorlevel 1 (
    echo ERROR: Failed to build JAR
    exit /b 1
)

echo Step 2: Creating portable app-image with jpackage...
rmdir /s /q dist\SketchChair-Portable 2>nul
mkdir dist

jpackage ^
  --type app-image ^
  --input build ^
  --name SketchChair ^
  --main-jar SketchChair-standard.jar ^
  --dest dist ^
  --app-version %VERSION% ^
  --vendor "Diatom Studio" ^
  --copyright "Copyright (C) 2012-2025 Diatom Studio" ^
  --icon data/icons/program_icon_02_b.ico ^
  --java-options "-Djogamp.gluegen.UseTempJarCache=true"

if errorlevel 1 (
    echo ERROR: Failed to create portable app
    exit /b 1
)

echo.
echo SUCCESS! Portable app created at: dist\SketchChair\
echo.
echo To run: dist\SketchChair\SketchChair.exe
echo To distribute: ZIP the entire dist\SketchChair\ folder
echo.
dir dist\SketchChair\*.exe
