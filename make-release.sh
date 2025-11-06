#!/bin/bash
# SketchChair Release Builder
# Creates versioned, multi-platform distribution packages with bundled Java
# Usage: ./make-release.sh <version> [changelog-entry]
# Example: ./make-release.sh 1.0.0.2 "Fixed PDF export scaling issue"

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if version is provided
if [ -z "$1" ]; then
    echo -e "${RED}Error: Version number required${NC}"
    echo "Usage: ./make-release.sh <version> [changelog-entry]"
    echo "Example: ./make-release.sh 1.0.0.2 \"Fixed PDF export scaling issue\""
    exit 1
fi

VERSION="$1"
CHANGELOG_ENTRY="${2:-Release $VERSION}"
DATE=$(date +"%Y-%m-%d")
PLATFORM=$(uname)

echo -e "${BLUE}=================================${NC}"
echo -e "${BLUE}SketchChair Release Builder${NC}"
echo -e "${BLUE}=================================${NC}"
echo -e "Version: ${GREEN}$VERSION${NC}"
echo -e "Date: $DATE"
echo -e "Platform: $PLATFORM"
echo ""

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Clean and build
echo -e "${YELLOW}[1/5] Cleaning previous builds...${NC}"
ant distclean
rm -rf dist/releases

echo -e "${YELLOW}[2/5] Building application...${NC}"
ant clean compile build.standard

# Create dist/releases directory
mkdir -p dist/releases

# Update changelog
echo -e "${YELLOW}[3/5] Updating changelog...${NC}"
TEMP_CHANGELOG=$(mktemp)
echo "[$VERSION] - $DATE" > "$TEMP_CHANGELOG"
echo "+ $CHANGELOG_ENTRY" >> "$TEMP_CHANGELOG"
echo "" >> "$TEMP_CHANGELOG"
cat changelog.txt >> "$TEMP_CHANGELOG"
mv "$TEMP_CHANGELOG" changelog.txt

echo -e "${YELLOW}[4/5] Creating native package for current platform...${NC}"

# Build platform-specific native package
case "$PLATFORM" in
    Darwin)
        echo -e "${GREEN}Building macOS .app bundle with bundled JDK 25...${NC}"

        # Build the .app using jpackage
        ant jpackage.mac

        if [ -d "dist/SketchChair.app" ]; then
            # Copy changelog and properties into the app bundle
            cp changelog.txt "dist/SketchChair.app/Contents/"
            cp SketchChair.properties "dist/SketchChair.app/Contents/"

            # Copy changelog and properties to dist/ for including in ZIP at root level
            cp changelog.txt "dist/"
            cp SketchChair.properties "dist/"

            # Create ZIP of the .app bundle with changelog and properties at root
            cd dist
            ZIP_NAME="SketchChair_v${VERSION}_macOS.zip"
            echo -e "  Creating $ZIP_NAME..."
            # Include changelog and properties at the root of the ZIP alongside the .app
            zip -r "releases/$ZIP_NAME" SketchChair.app changelog.txt SketchChair.properties >/dev/null

            # Clean up the temporary copies
            rm changelog.txt SketchChair.properties
            cd ..

            echo -e "${GREEN}✓ Created: dist/releases/$ZIP_NAME${NC}"
            echo -e "  ${BLUE}Contents: Native .app bundle with JDK 25 runtime${NC}"
        else
            echo -e "${RED}✗ Failed to create .app bundle${NC}"
            echo -e "  Make sure you're running on macOS with JDK 25 installed"
            exit 1
        fi
        ;;

    MINGW*|MSYS*|CYGWIN*)
        echo -e "${GREEN}Building Windows .exe installer with bundled JRE...${NC}"

        # Build the .exe installer using jpackage
        ant jpackage.win

        # Find the generated installer
        EXE_FILE=$(find dist -name "*.exe" -type f | head -1)

        if [ -n "$EXE_FILE" ]; then
            # Rename to versioned name
            NEW_NAME="SketchChair_v${VERSION}_Windows.exe"
            mv "$EXE_FILE" "dist/releases/$NEW_NAME"

            echo -e "${GREEN}✓ Created: dist/releases/$NEW_NAME${NC}"
            echo -e "  ${BLUE}Contents: Native .exe installer with bundled JRE${NC}"
        else
            echo -e "${RED}✗ Failed to create .exe installer${NC}"
            echo -e "  Make sure you're running on Windows with JDK 17+ installed"
            exit 1
        fi
        ;;

    Linux)
        echo -e "${GREEN}Building Linux .deb package with bundled JRE...${NC}"

        # Build the .deb package using jpackage
        ant jpackage.linux

        # Find the generated package
        DEB_FILE=$(find dist -name "*.deb" -type f | head -1)

        if [ -n "$DEB_FILE" ]; then
            # Rename to versioned name
            NEW_NAME="SketchChair_v${VERSION}_Linux.deb"
            mv "$DEB_FILE" "dist/releases/$NEW_NAME"

            echo -e "${GREEN}✓ Created: dist/releases/$NEW_NAME${NC}"
            echo -e "  ${BLUE}Contents: Native .deb package with bundled JRE${NC}"
        else
            echo -e "${RED}✗ Failed to create .deb package${NC}"
            echo -e "  Make sure you're running on Linux with JDK 17+ installed"
            exit 1
        fi
        ;;

    *)
        echo -e "${RED}✗ Unsupported platform: $PLATFORM${NC}"
        echo -e "  Supported platforms: macOS (Darwin), Windows (MINGW/MSYS), Linux"
        exit 1
        ;;
esac

# Create portable ZIP as alternative (optional)
echo -e "${YELLOW}[5/5] Creating portable ZIP package (requires Java)...${NC}"

create_portable_zip() {
    local PLATFORM_NAME=$1
    local ZIP_NAME="SketchChair_v${VERSION}_${PLATFORM_NAME}_portable.zip"
    local TEMP_DIR="dist/releases/temp_portable"
    local PACKAGE_DIR="SketchChair"

    mkdir -p "$TEMP_DIR/$PACKAGE_DIR"

    # Copy application files
    cp build/SketchChair-standard.jar "$TEMP_DIR/$PACKAGE_DIR/"
    cp -r build/lib "$TEMP_DIR/$PACKAGE_DIR/"
    cp -r build/data "$TEMP_DIR/$PACKAGE_DIR/"
    cp -r build/languages "$TEMP_DIR/$PACKAGE_DIR/"
    cp -r build/binlib "$TEMP_DIR/$PACKAGE_DIR/"
    cp changelog.txt "$TEMP_DIR/$PACKAGE_DIR/"
    cp SketchChair.properties "$TEMP_DIR/$PACKAGE_DIR/"

    # Copy platform-specific launcher (modified for portable package)
    case "$PLATFORM_NAME" in
        macOS)
            # Create a modified launcher that doesn't cd into build/
            cat > "$TEMP_DIR/$PACKAGE_DIR/launch-mac.command" <<'LAUNCHER_EOF'
#!/bin/bash
# SketchChair macOS Launcher (Portable)
# This script properly launches SketchChair with required JVM options for macOS
# Requires JDK 25 for macOS 15.3+ compatibility with JOGL 2.6.0

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Launch SketchChair with JDK 25 and required JVM options
cd "$SCRIPT_DIR"
/usr/libexec/java_home -v 25 --exec java \
     --enable-native-access=ALL-UNNAMED \
     --add-exports=java.desktop/com.apple.eawt=ALL-UNNAMED \
     -Djogamp.gluegen.UseTempJarCache=true \
     -Dsun.java2d.metal=true \
     -Dapple.awt.application.appearance=system \
     -Dapple.laf.useScreenMenuBar=true \
     -jar SketchChair-standard.jar
LAUNCHER_EOF
            chmod +x "$TEMP_DIR/$PACKAGE_DIR/launch-mac.command"
            cat > "$TEMP_DIR/$PACKAGE_DIR/README.txt" <<EOF
SketchChair $VERSION for macOS (Portable)
==========================================

This is a portable version that requires Java to be installed.
For easier installation, use SketchChair_v${VERSION}_macOS.zip instead.

Requirements:
- macOS 10.14 or later
- JDK 17+ (JDK 25 recommended for macOS 15.3+)
  Download from: https://adoptium.net/

To Run:
- Double-click launch-mac.command

For more information: www.sketchchair.cc
EOF
            ;;
    esac

    # Create ZIP file
    cd "$TEMP_DIR"
    zip -r "../$ZIP_NAME" "$PACKAGE_DIR" >/dev/null
    cd "$SCRIPT_DIR"
    rm -rf "$TEMP_DIR"

    echo -e "${GREEN}✓ Created: dist/releases/$ZIP_NAME${NC}"
    echo -e "  ${BLUE}Contents: Portable JAR (requires Java installation)${NC}"
}

# Only create portable version for macOS (as fallback)
if [ "$PLATFORM" == "Darwin" ]; then
    create_portable_zip "macOS"
fi

# Create a release summary file
echo ""
echo -e "${YELLOW}Creating release summary...${NC}"
cat > "dist/releases/RELEASE-$VERSION.txt" <<EOF
SketchChair Release $VERSION
============================
Build Date: $DATE
Build Platform: $PLATFORM
Changelog: $CHANGELOG_ENTRY

Packages Created:
EOF

# List all created packages
for file in dist/releases/SketchChair_v${VERSION}_*; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        size=$(ls -lh "$file" | awk '{print $5}')
        echo "- $filename ($size)" >> "dist/releases/RELEASE-$VERSION.txt"
    fi
done

echo "" >> "dist/releases/RELEASE-$VERSION.txt"
echo "Platform-Specific Notes:" >> "dist/releases/RELEASE-$VERSION.txt"
echo "- macOS: .app bundle with JDK 25 runtime (recommended for macOS 15.3+)" >> "dist/releases/RELEASE-$VERSION.txt"
echo "- Windows: .exe installer with bundled JRE (double-click to install)" >> "dist/releases/RELEASE-$VERSION.txt"
echo "- Linux: .deb package with bundled JRE (sudo dpkg -i SketchChair_*.deb)" >> "dist/releases/RELEASE-$VERSION.txt"
echo "" >> "dist/releases/RELEASE-$VERSION.txt"

echo "File Checksums (SHA256):" >> "dist/releases/RELEASE-$VERSION.txt"
cd dist/releases
for file in SketchChair_v${VERSION}_*; do
    if [ -f "$file" ]; then
        sha256sum "$file" >> "RELEASE-$VERSION.txt" 2>/dev/null || shasum -a 256 "$file" >> "RELEASE-$VERSION.txt"
    fi
done
cd "$SCRIPT_DIR"

# Print summary
echo ""
echo -e "${GREEN}=================================${NC}"
echo -e "${GREEN}Release build complete!${NC}"
echo -e "${GREEN}=================================${NC}"
echo -e "Version: ${BLUE}$VERSION${NC}"
echo -e "Location: ${BLUE}dist/releases/${NC}"
echo ""
echo -e "${YELLOW}Created packages:${NC}"
ls -lh dist/releases/SketchChair_v${VERSION}_* 2>/dev/null | awk '{print "  " $9 " (" $5 ")"}'
echo ""
echo -e "${YELLOW}Release summary:${NC} dist/releases/RELEASE-$VERSION.txt"
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Important Notes:${NC}"
echo ""
echo -e "${RED}Cross-Platform Builds:${NC}"
echo "  Native packages can only be built on their target platform:"
echo "  • macOS .app  → Build on macOS"
echo "  • Windows .exe → Build on Windows"
echo "  • Linux .deb  → Build on Linux"
echo ""
echo -e "  ${GREEN}Current platform: $PLATFORM${NC}"
echo ""
echo -e "${YELLOW}To create packages for other platforms:${NC}"
echo "  1. Copy this repository to the target platform"
echo "  2. Run: ./make-release.sh $VERSION \"$CHANGELOG_ENTRY\""
echo "  3. Collect all platform-specific packages"
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${GREEN}Next steps:${NC}"
echo "1. Test the package on this platform"
echo "2. Build on other platforms (Windows, Linux) if needed"
echo "3. Commit changelog: git add changelog.txt && git commit -m \"Release $VERSION\""
echo "4. Create git tag: git tag -a v$VERSION -m \"Release $VERSION\""
echo "5. Push changes: git push && git push --tags"
echo "6. Upload packages to GitHub releases or website"
echo ""
