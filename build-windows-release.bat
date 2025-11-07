@echo off
REM Complete Windows Release Build Script
REM Creates both installer .exe and portable app-image with all files
echo ========================================
echo SketchChair Windows Release Builder
echo ========================================
echo.

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot
set ANT_HOME=C:\ant\apache-ant-1.10.15
set WIX=C:\Program Files (x86)\WiX Toolset v3.14
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%WIX%\bin;%PATH%

cd /d C:\Git\Diatom\SketchChair

REM Clean previous builds
echo Cleaning previous builds...
rmdir /s /q dist 2>nul
mkdir dist

REM Step 1: Build standard JAR
echo.
echo [1/4] Building standard JAR...
call ant clean build.standard
if errorlevel 1 (
    echo ERROR: Failed to build JAR
    exit /b 1
)

REM Step 2: Create installer .exe
echo.
echo [2/4] Creating Windows installer .exe...
call ant jpackage.win
if errorlevel 1 (
    echo ERROR: Failed to create .exe installer
    exit /b 1
)

REM Step 3: Create portable app-image
echo.
echo [3/4] Creating portable app-image...
jpackage ^
  --type app-image ^
  --input build ^
  --name SketchChair ^
  --main-jar SketchChair-standard.jar ^
  --dest dist ^
  --app-version 1.0 ^
  --vendor "Diatom Studio" ^
  --copyright "Copyright (C) 2012-2025 Diatom Studio" ^
  --icon data/icons/program_icon_02_b.ico ^
  --java-options "-Djogamp.gluegen.UseTempJarCache=true"

if errorlevel 1 (
    echo ERROR: Failed to create portable app
    exit /b 1
)

REM Step 4: Package distributions with documentation
echo.
echo [4/4] Packaging distributions...

REM Create installer package folder
echo   - Preparing installer package...
mkdir dist\SketchChair-Installer
copy dist\SketchChair-1.0.exe dist\SketchChair-Installer\ >nul
copy changelog.txt dist\SketchChair-Installer\ >nul
copy SketchChair.properties dist\SketchChair-Installer\ >nul
copy README.md dist\SketchChair-Installer\ 2>nul

REM Create README for installer
echo SketchChair Windows Installer > dist\SketchChair-Installer\README.txt
echo ================================ >> dist\SketchChair-Installer\README.txt
echo. >> dist\SketchChair-Installer\README.txt
echo To install: >> dist\SketchChair-Installer\README.txt
echo 1. Double-click SketchChair-1.0.exe >> dist\SketchChair-Installer\README.txt
echo 2. Follow the installation wizard >> dist\SketchChair-Installer\README.txt
echo 3. Launch from Start Menu or Desktop shortcut >> dist\SketchChair-Installer\README.txt
echo. >> dist\SketchChair-Installer\README.txt
echo No Java installation required - JRE is bundled! >> dist\SketchChair-Installer\README.txt
echo. >> dist\SketchChair-Installer\README.txt
echo See changelog.txt for version history. >> dist\SketchChair-Installer\README.txt

REM Add documentation to portable app
echo   - Adding documentation to portable app...
copy changelog.txt dist\SketchChair\ >nul
copy SketchChair.properties dist\SketchChair\ >nul
copy README.md dist\SketchChair\ 2>nul

REM Create README for portable app
echo SketchChair Portable Edition > dist\SketchChair\README.txt
echo =============================== >> dist\SketchChair\README.txt
echo. >> dist\SketchChair\README.txt
echo To run: >> dist\SketchChair\README.txt
echo 1. Extract this folder anywhere >> dist\SketchChair\README.txt
echo 2. Double-click SketchChair.exe >> dist\SketchChair\README.txt
echo. >> dist\SketchChair\README.txt
echo No installation or Java required - JRE is bundled! >> dist\SketchChair\README.txt
echo. >> dist\SketchChair\README.txt
echo Perfect for USB drives or running without admin rights. >> dist\SketchChair\README.txt
echo. >> dist\SketchChair\README.txt
echo See changelog.txt for version history. >> dist\SketchChair\README.txt

REM Create ZIP packages
echo   - Creating ZIP archives...
powershell -Command "Compress-Archive -Path 'dist\SketchChair-Installer' -DestinationPath 'dist\SketchChair-Installer.zip' -Force"
powershell -Command "Compress-Archive -Path 'dist\SketchChair' -DestinationPath 'dist\SketchChair-Portable.zip' -Force"

REM Summary
echo.
echo ========================================
echo BUILD COMPLETE!
echo ========================================
echo.
echo Created in dist/:
echo   1. SketchChair-Installer/
echo      - SketchChair-1.0.exe (installer)
echo      - Documentation files
echo      - SketchChair-Installer.zip
echo.
echo   2. SketchChair/ (portable)
echo      - SketchChair.exe (run directly)
echo      - Bundled JRE
echo      - Documentation files
echo      - SketchChair-Portable.zip
echo.
echo Distribution packages:
dir dist\*.zip
echo.
echo Ready for distribution!
echo ========================================
