#!/bin/bash
# SketchChair macOS Launcher
# This script properly launches SketchChair with required JVM options for macOS
# Requires JDK 25 for macOS 15.3+ compatibility with JOGL 2.6.0

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Change to the build directory
cd "$SCRIPT_DIR/build"

# Launch SketchChair with JDK 25 and required JVM options
/usr/libexec/java_home -v 25 --exec java \
     --enable-native-access=ALL-UNNAMED \
     --add-exports=java.desktop/com.apple.eawt=ALL-UNNAMED \
     -Djogamp.gluegen.UseTempJarCache=true \
     -Dsun.java2d.metal=true \
     -Dapple.awt.application.appearance=system \
     -Dapple.laf.useScreenMenuBar=true \
     -jar SketchChair-standard.jar
