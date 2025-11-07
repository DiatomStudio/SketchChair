#!/bin/bash
# SketchChair Linux Launcher
# This script properly launches SketchChair with required JVM options for Linux
# Requires JDK 17+ for Processing 4 compatibility with JOGL 2.x

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Launch SketchChair with required JVM options
java \
    -Djogamp.gluegen.UseTempJarCache=true \
    -jar "$SCRIPT_DIR/SketchChair-standard.jar"
