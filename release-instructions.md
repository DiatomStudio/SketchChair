# SketchChair Release Instructions

This document explains how to create versioned release packages for SketchChair.

## Overview

The `make-release.sh` script creates platform-specific native packages with bundled Java runtime, ensuring users don't need to install Java separately.

## Prerequisites

### For macOS builds:
- macOS with JDK 25 installed
- Ant build tool
- Access to this repository

### For Windows builds:
- Windows 10+ with JDK 17+ installed
- Ant build tool
- Access to this repository

### For Linux builds:
- Linux with JDK 17+ installed
- Ant build tool
- Access to this repository

## Quick Start

```bash
# Make the script executable (first time only)
chmod +x make-release.sh

# Create a release
./make-release.sh 1.0.0.2 "Fixed PDF export scaling issue"
```

## Usage

```bash
./make-release.sh <version> [changelog-entry]
```

### Parameters:
- `version` (required): Version number (e.g., `1.0.0.2`)
- `changelog-entry` (optional): Description of changes for this release

### Examples:

```bash
# Simple release
./make-release.sh 1.0.0.2

# Release with changelog entry
./make-release.sh 1.0.0.2 "Fixed PDF export scaling issue"

# Major release
./make-release.sh 2.0.0.0 "Upgraded to Processing 4 with Apple Silicon support"
```

## What It Does

1. **Cleans** previous builds
2. **Compiles** the application
3. **Updates** changelog.txt with the new version
4. **Builds** native package for current platform:
   - **macOS**: Creates `.app` bundle with JDK 25 runtime (~200MB)
   - **Windows**: Creates `.exe` installer with JRE (~150MB)
   - **Linux**: Creates `.deb` package with JRE (~150MB)
5. **Creates** portable ZIP (macOS only, as fallback)
6. **Generates** release summary with checksums

## Output

All packages are created in `dist/releases/`:

```
dist/releases/
├── SketchChair_v1.0.0.2_macOS.zip          # Native .app with JDK 25
├── SketchChair_v1.0.0.2_macOS_portable.zip # Portable (requires Java)
└── RELEASE-1.0.0.2.txt                      # Release summary
```

## Cross-Platform Releases

**Important**: Native packages can only be built on their target platform.

To create a complete multi-platform release:

### 1. Build on macOS:
```bash
./make-release.sh 1.0.0.2 "Release notes here"
# Creates: SketchChair_v1.0.0.2_macOS.zip
```

### 2. Build on Windows:
```bash
# Copy repository to Windows machine
./make-release.sh 1.0.0.2 "Release notes here"
# Creates: SketchChair_v1.0.0.2_Windows.exe
```

### 3. Build on Linux:
```bash
# Copy repository to Linux machine
./make-release.sh 1.0.0.2 "Release notes here"
# Creates: SketchChair_v1.0.0.2_Linux.deb
```

### 4. Collect all packages:
```
SketchChair_v1.0.0.2_macOS.zip    (~200MB, includes JDK 25)
SketchChair_v1.0.0.2_Windows.exe  (~150MB, includes JRE)
SketchChair_v1.0.0.2_Linux.deb    (~150MB, includes JRE)
```

## Version Numbering

Follow the format used in previous releases:

- **Format**: `MAJOR.MINOR.PATCH.BUILD`
- **Example**: `1.0.0.2`
- **Previous**: `0.9.0.1` (last released version)

### Recommended Versioning:
- `1.0.0.0` - First Processing 4 release
- `1.0.0.1` - Bug fix
- `1.0.1.0` - Minor feature addition
- `1.1.0.0` - Significant new features
- `2.0.0.0` - Major version with breaking changes

## After Building

### 1. Test the Package
- Install/run on the target platform
- Test key functionality (design, export, PDF generation)
- Verify changelog is included

### 2. Commit and Tag
```bash
# Commit the updated changelog
git add changelog.txt
git commit -m "Release 1.0.0.2"

# Create git tag
git tag -a v1.0.0.2 -m "Release 1.0.0.2: Fixed PDF export scaling"

# Push everything
git push origin modernization-2025
git push --tags
```

### 3. Upload Packages
Upload all platform packages to:
- GitHub Releases (recommended)
- SketchChair website
- Distribution hosting service

## Troubleshooting

### Error: "jpackage command not found"
- **Solution**: Install JDK 17+ (Linux/Windows) or JDK 25 (macOS)
- **macOS**: `brew install openjdk@25`
- **Check**: `/usr/libexec/java_home -V` (macOS) or `java --version`

### Error: ".app bundle creation failed"
- **macOS only**: Ensure JDK 25 is installed
- **Check path**: `/usr/libexec/java_home -v 25`

### Error: "ant command not found"
- **Solution**: Install Apache Ant
- **macOS**: `brew install ant`
- **Linux**: `sudo apt install ant` or `sudo dnf install ant`
- **Windows**: Download from https://ant.apache.org/

### Large Package Sizes
Native packages are large because they include the full Java runtime:
- **macOS .app**: ~200MB (includes JDK 25)
- **Windows .exe**: ~150MB (includes JRE)
- **Linux .deb**: ~150MB (includes JRE)

This is intentional - users don't need to install Java separately!

## Files and Directories

```
SketchChair/
├── make-release.sh           # Main release script
├── launch-mac.command        # macOS launcher (portable)
├── launch-windows.bat        # Windows launcher (portable)
├── launch-linux.sh           # Linux launcher (portable)
├── changelog.txt             # Version history (auto-updated)
├── SketchChair.properties    # Configuration file
├── build.xml                 # Ant build configuration
└── dist/releases/            # Output directory (created during build)
```

## Manual Build (Alternative)

If you prefer to build manually without the script:

```bash
# Build the application
ant clean compile build.standard

# Build native package
ant jpackage.mac    # macOS
ant jpackage.win    # Windows
ant jpackage.linux  # Linux

# Package is created in dist/
```

## CI/CD Integration

For automated builds, you can integrate with GitHub Actions or similar:

```yaml
# Example GitHub Actions workflow (not included)
- Run on macOS runner for .app
- Run on Windows runner for .exe
- Run on Linux runner for .deb
- Collect and upload all artifacts
```

## Support

For questions or issues:
- GitHub: https://github.com/diatom/SketchChair
- Website: www.sketchchair.cc
- Email: hello@diatom.cc

---

**Last Updated**: 2025-01-06
**SketchChair Version**: 1.0.0.x (Processing 4)
