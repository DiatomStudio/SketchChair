# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**SketchChair** is an open-source furniture design and fabrication tool built with Processing. Users sketch 2D designs, test them with an ergonomic simulation, and generate cutting patterns for CNC fabrication.

- **Language**: Java (Processing 4.3 framework)
- **Java Version**: Java 17+
- **Build System**: Apache Ant
- **Current Branch**: `modernization-2025` (Processing 4 upgrade - 95% complete)
- **Main Branch**: `master`

## Essential Build Commands

```bash
# Clean build directories
ant clean

# Compile only (check for errors)
ant compile

# Build standard JAR with external dependencies
ant build.standard

# Run the application (macOS recommended)
./launch-mac.command
# Or manually:
cd build
java -Djogamp.gluegen.UseTempJarCache=true -jar SketchChair-standard.jar

# Create native macOS .app bundle (requires JDK 25 for best results)
ant jpackage.mac

# Create Windows portable distribution (ZIP)
ant dist.windows.portable

# Create Windows installer (.exe) - must run on Windows
ant jpackage.win

# Create Linux package (.deb) - must run on Linux
ant jpackage.linux
```

**Important**: Always use `-Djogamp.gluegen.UseTempJarCache=true` when running the JAR directly. This is required for JOGL native library loading.

## Code Architecture

### Core Packages

#### `cc.sketchchair.core`
Main application code that ties together all subsystems. Key files:
- `main.java` - Application entry point (extends PApplet)
- `SETTINGS.java` - Configuration constants
- `Legacy.java` - Compatibility layer for Processing API differences

#### `cc.sketchchair.sketch`
Vector drawing system - can function independently as a standalone vector editor.
Manages user drawings on SketchPlanes.

#### `cc.sketchchair.geometry`
Geometry calculations for chair structure:
- Calculates cross-slice forms from SketchPlane intersections
- Generates slots for finger joints
- Creates 3D representations from 2D sketches

#### `cc.sketchchair.ragdoll`
Ergonomic figure simulation for testing designs.
Uses physics engine to simulate sitting posture.

### Custom Libraries (in `src/`)

#### `ModalGUI`
Custom GUI framework with standard widgets built specifically for SketchChair.
Handles windows, buttons, panels, and other interface components.

#### `ShapePacking`
2D packing algorithm for arranging cutting outlines on material sheets.
Optimizes material usage for fabrication.

#### `ToolPathWriter`
Export system supporting multiple formats:
- DXF (AutoCAD)
- G-code (CNC machines)
- PDF (documentation)
- HPGL, CraftRobo formats

#### `CloudHook`
Server communication for design sharing and user authentication.
Backend implemented in PHP (separate repository).

### Third-Party Libraries

**lib/** - SketchChair-specific dependencies:
- `AppleJavaExtensions.jar` - Mac-specific extensions
- `dxf.jar` - DXF export support
- `janino.jar` - Runtime Java compiler
- `jbullet.jar` - Physics simulation engine
- `stack-alloc.jar` - Memory optimization
- `svgSalamander-tiny.jar` - SVG import/export
- `toxilib_2.jar` - Geometry utilities
- `vecmath.jar` - Vector math for physics
- `xom-1.2.6_mod.jar` - XML processing

**libProcessing4/** - Processing 4 + JOGL dependencies:
- `core.jar` - Processing 4.3 (custom patched with macOS icon fixes)
- `jogl-all.jar`, `gluegen-rt.jar` - JOGL 2.6.0 OpenGL bindings
- 66+ platform-specific native JARs for JOGL
- `itext.jar`, `pdf.jar` - PDF export (Processing 4 versions)

## How the Application Works

1. **Sketch Phase**: User draws on SketchPlanes using vector tools
2. **Build Phase**: Geometry engine calculates 3D forms from 2D sketches:
   - Analyzes slice selections
   - Computes intersections between planes
   - Generates slots and joints
3. **Simulation Phase**: Physics engine tests design with ergonomic figure
4. **Export Phase**: ShapePacking optimizes layout, ToolPathWriter generates output
5. **Save/Share**: Custom XML format stores designs, CloudHook uploads to server

## Important Modernization Context

The codebase is currently undergoing a **Processing 1.x → Processing 4 upgrade** on the `modernization-2025` branch.

**Status**: Phase 5 Complete (95% done) - Native distribution ready! 🚀

**Key Changes Made**:
- Java 8 → Java 17
- JOGL 1.x → JOGL 2.x (Apple Silicon support)
- Frame API → Surface API (Processing 4 requirement)
- One-JAR packaging → Standard JAR with lib/ folder
- Added jpackage targets for native .app/.dmg/.exe distribution
- Fixed PDF export with Processing 4.4 compatible iText/pdf.jar

**Key Files to Check**:
- `processing4-upgrade.md` - Comprehensive upgrade progress document
- `build.xml` - All build targets and configurations
- `changelog.txt` - Version history

**Remaining Work**:
- Cross-platform testing (Windows, Linux, Intel Mac)
- Feature validation
- Performance benchmarking

## Processing 4 Specific Notes

### Critical Runtime Requirement
Always launch with: `-Djogamp.gluegen.UseTempJarCache=true`

This enables JOGL's temp JAR cache for native library extraction.

### API Patterns

**Surface API** (replaces Frame API):
```java
// Window management
surface.setSize(width, height);
surface.setLocation(x, y);
surface.setResizable(true);
```

**JOGL 2.x OpenGL**:
```java
PGraphicsOpenGL pg = (PGraphicsOpenGL)g;
PJOGL pgl = (PJOGL)pg.beginPGL();
GL2 gl = pgl.gl.getGL2();
// ... OpenGL calls ...
pg.endPGL();
```

**Event Registration**:
```java
registerMethod("mouseEvent", object);
registerMethod("keyEvent", object);
```

**settings() Method**:
```java
// Processing 4 requires size() in settings(), not setup()
public void settings() {
    size(width, height, P3D);
}
```

## File Locations

- **Source**: `src/` (all Java source files)
- **Libraries**: `lib/` (SketchChair-specific), `libProcessing4/` (Processing + JOGL)
- **Resources**: `data/` (images, fonts, GUI assets)
- **Languages**: `languages/` (localization .properties files)
- **Native Binaries**: `binlib/` (platform-specific tools)
- **Build Output**: `build/` (compiled classes, JARs)
- **Distribution**: `dist/` (native .app, .dmg, .exe packages)

## Development Workflow

### For Bug Fixes or Features

1. Check current branch: `git branch --show-current`
2. Compile to verify current state: `ant compile`
3. Make changes
4. Test frequently: `ant compile` or `ant run.standard`
5. Commit with descriptive messages
6. Update relevant documentation if needed

### For Continuing the Modernization

1. **Read** `processing4-upgrade.md` first for current status
2. Check remaining tasks in Phase 6 (testing) or Phase 7 (validation)
3. Follow the upgrade patterns documented in that file
4. **Always update** `processing4-upgrade.md` when completing tasks

## Common Issues

### "cannot find symbol" errors
Check import statements - Processing 4 moved some classes to different packages.

### JOGL native library errors
Ensure `-Djogamp.gluegen.UseTempJarCache=true` is set when running.

### dataPath() or loadImage() failures
Use `ant build.standard` (not old One-JAR build). Resources must be in external folders.

### Window/Frame API errors
Use `surface` instead of `frame` (Processing 4 API change).

### PDF export "Unbalanced save/restore state operators" error
Ensure using Processing 4.4 compatible `itext.jar` and `pdf.jar` from `libProcessing4/`.
These are the correct versions - do NOT use older versions from other sources.

## Testing

### Manual Testing Checklist
- [ ] Application launches
- [ ] 3D rendering works
- [ ] Mouse/keyboard input responsive
- [ ] GUI widgets functional
- [ ] Physics simulation runs
- [ ] PDF export works (no save/restore errors)
- [ ] DXF export works
- [ ] Load old .xml designs
- [ ] Save/load roundtrip

### Build Verification
```bash
ant clean compile  # Should complete with 0 errors
ant build.standard # Should create SketchChair-standard.jar
cd build && java -Djogamp.gluegen.UseTempJarCache=true -jar SketchChair-standard.jar
```

## Documentation

- **API Docs**: `/doc/index.html` (JavaDoc for custom classes)
- **README**: `README.md` (project overview, setup)
- **Build Windows**: `build-windows.md` (Windows-specific instructions)
- **Release Process**: `release-instructions.md` (creating releases)
- **Upgrade Progress**: `processing4-upgrade.md` (detailed migration notes)

## Contact

- **Studio**: Diatom Studio (diatom.cc)
- **Email**: hello@diatom.cc
- **Website**: sketchchair.cc
