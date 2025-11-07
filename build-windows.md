# Building SketchChair for Windows

This document explains how to build SketchChair for Windows, including creating a standalone Windows installer with bundled JRE.

## Prerequisites

### Required Software

1. **JDK 17 or newer** (JDK 21 or 25 recommended)
   - Download from: https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/
   - Set `JAVA_HOME` environment variable
   - Add `%JAVA_HOME%\bin` to system PATH

2. **Apache Ant** (for build system)
   - Download from: https://ant.apache.org/bindownload.cgi
   - Extract and add `bin` directory to system PATH

3. **Git** (for source control)
   - Download from: https://git-scm.com/download/win

## Quick Start - Running from JAR

If you just want to run SketchChair without building an installer:

1. Build the standard JAR:
   ```cmd
   ant clean build.standard
   ```

2. Run the launcher:
   ```cmd
   launch-windows.bat
   ```

   Or run directly:
   ```cmd
   cd build
   java -Djogamp.gluegen.UseTempJarCache=true -jar SketchChair-standard.jar
   ```

## Building Windows Installer with Bundled JRE

To create a Windows `.exe` installer with bundled JRE (so users don't need Java installed):

### Step 1: Build the Application

```cmd
ant clean build.standard
```

This creates:
- `build/SketchChair-standard.jar` - Main application
- `build/lib/*.jar` - All dependencies
- `build/data/` - Application resources
- `build/binlib/` - Native libraries (JOGL for Windows)

### Step 2: Create Windows Icon (Optional)

The build script expects `data/icon.ico`. If you don't have one:

1. You can convert the existing PNG icon using online tools or ImageMagick:
   ```cmd
   magick data/program_icon_02_b_128x128x32.png data/icon.ico
   ```

2. Or comment out the icon lines in `build.xml` (lines 278-279):
   ```xml
   <!-- <arg value="--icon"/>
   <arg value="data/icon.ico"/> -->
   ```

### Step 3: Build the Installer

Run the jpackage build target:

```cmd
ant jpackage.win
```

This will:
1. Bundle the JRE from your installed JDK
2. Create a Windows executable launcher
3. Package everything into `dist/SketchChair-1.0.exe`

The installer will be created in the `dist/` folder.

### What the Installer Includes

- SketchChair application
- Bundled Java Runtime (no need for users to install Java)
- All native libraries (JOGL, etc.)
- Windows Start Menu shortcut
- Desktop shortcut option
- Proper uninstaller

## Distribution Options

### Option 1: Portable ZIP Distribution

Create a portable distribution without installer:

```cmd
cd build
7z a ../SketchChair-Windows-Portable.zip SketchChair-standard.jar lib/ data/ languages/ binlib/
cd ..
copy launch-windows.bat SketchChair-Windows-Portable.zip
```

Users can:
1. Extract the ZIP
2. Run `launch-windows.bat`
3. Requires Java 17+ to be installed

### Option 2: JRE Bundle (Manual)

Bundle JRE manually without jpackage:

1. Download JRE 17+ from Adoptium or Oracle
2. Extract to `build/jre/`
3. Modify `launch-windows.bat` to use bundled JRE:

```batch
set SCRIPT_DIR=%~dp0
"%SCRIPT_DIR%jre\bin\java.exe" ^
     -Djogamp.gluegen.UseTempJarCache=true ^
     -jar "%SCRIPT_DIR%build\SketchChair-standard.jar"
```

## Troubleshooting

### jpackage not found

- Make sure you're using JDK (not JRE)
- JDK 14+ includes jpackage
- Verify: `jpackage --version`

### Missing WiX Toolset

For creating `.msi` installers, you need WiX Toolset:
- Download from: https://wixtoolset.org/
- Add to PATH
- Change build target type from `exe` to `msi` in `build.xml` line 273

### Icon errors

If you get icon-related errors:
- Comment out icon arguments in `build.xml` (lines 278-279)
- Or create `data/icon.ico` from the PNG file

### Build fails with "Main class not found"

Verify the manifest in JAR:
```cmd
jar tf build\SketchChair-standard.jar | findstr MANIFEST
jar xf build\SketchChair-standard.jar META-INF/MANIFEST.MF
type META-INF\MANIFEST.MF
```

Should show:
```
Main-Class: cc.sketchchair.core.main
```

## Testing the Build

After building:

1. **Test the JAR directly**:
   ```cmd
   cd build
   java -Djogamp.gluegen.UseTempJarCache=true -jar SketchChair-standard.jar
   ```

2. **Test the installer** (if created):
   - Run `dist/SketchChair-1.0.exe`
   - Install to a test location
   - Launch from Start Menu
   - Verify 3D rendering works
   - Test opening/saving files

## System Requirements

**Minimum**:
- Windows 10 or newer (64-bit)
- 2GB RAM
- OpenGL 2.0+ compatible graphics
- 100MB disk space

**Recommended**:
- Windows 11 (64-bit)
- 4GB RAM
- Dedicated graphics card
- 500MB disk space

## Notes

- Native libraries for Windows are in `binlib/windows64/`
- JOGL uses Windows-specific DLLs for OpenGL rendering
- The application uses Processing 4 with P3D renderer
- JVM options enable JOGL temp JAR caching for proper library loading

## Support

For build issues, check:
- Java version: `java -version` (should be 17+)
- Ant version: `ant -version`
- Build logs in console output
- GitHub Issues: https://github.com/diatom/SketchChair/issues
