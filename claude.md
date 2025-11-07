# Claude Code Instructions for SketchChair

## Project Context

This is **SketchChair** - a 12-year-old Processing-based application for designing furniture. We're currently upgrading it from Processing 1.x to Processing 4 to enable native Apple Silicon support and modern cross-platform compatibility.

**Branch**: `modernization-2025`
**Status**: Phase 1 Complete, Phase 2 In Progress

---

## Quick Reference

### Key Documents
- **📋 Progress Tracker**: `processing4-upgrade.md` - Comprehensive upgrade status (UPDATE THIS!)
- **🏗️ Build**: `build.xml` - Ant build configuration (Java 17)
- **📦 Libraries**: `libProcessing4/` - Processing 4.3 + JOGL 2.x libraries
- **📝 Original README**: `README.md` - Original project documentation

### Important Files to Track
- `src/cc/sketchchair/core/main.java` - Main application entry point
- `src/cc/sketchchair/core/Legacy.java` - Compatibility layer
- `src/cc/sketchchair/core/SETTINGS.java` - Configuration
- `build.xml` - Build configuration

---

## Current Status

**Overall Progress**: 20% (Phase 1 of 5 complete)

### ✅ What's Done (Phase 1)
1. ✅ Build system upgraded to Java 17
2. ✅ Processing 4.3 core library integrated
3. ✅ JOGL 2.x libraries added (all platforms)
4. ✅ Compilation works (37 expected API errors)
5. ✅ Progress document created

### 🔄 What's Next (Phase 2)
**37 compilation errors to fix** in these categories:

1. **Frame → Surface API** (15 instances)
   - Files: `main.java`, `simpleTest.java`, `Environments.java`, `SkchAutamata.java`
   - Change: `this.frame.setSize()` → `surface.setSize()`

2. **GL API Access** (6 instances)
   - Files: `main.java`, `simpleTest.java`, `ModalGUI.java`, `GUIPanel.java`
   - Change: `beginGL()/endGL()` → `beginPGL()/endPGL()` with JOGL 2.x syntax

3. **Event Registration** (3 instances)
   - Files: `Legacy.java`, `ModalGUI.java`
   - Change: `registerMouseEvent()` → `registerMethod("mouseEvent")`

4. **Other API incompatibilities** (13 instances)
   - MouseEvent methods, PGraphics casting, Component issues

---

## How to Continue This Work

### On Session Start

1. **Read the progress doc first**:
   ```bash
   cat processing4-upgrade.md
   ```

2. **Check current branch and status**:
   ```bash
   git branch
   git log --oneline -5
   git status
   ```

3. **Review compilation errors**:
   ```bash
   ant clean compile 2>&1 | grep "error:"
   ```

### Working on Fixes

1. **Pick a category from Phase 2** (see processing4-upgrade.md)

2. **Find affected files**:
   ```bash
   # Example for Frame API
   grep -rn "\.frame\." src/
   ```

3. **Make fixes** following the patterns in processing4-upgrade.md

4. **Test compilation frequently**:
   ```bash
   ant clean compile
   ```

5. **Commit after each logical group of fixes**:
   ```bash
   git add -A
   git commit -m "Phase 2: Fix Frame API in main.java (5 locations)"
   ```

6. **Update processing4-upgrade.md** with:
   - Change status from ⏳ to ✅
   - Mark files as complete
   - Note any issues discovered

### Important Patterns

#### Frame → Surface
```java
// OLD
this.frame.setSize(width, height);
GLOBAL.frame = this.frame;

// NEW
surface.setSize(width, height);
GLOBAL.surface = surface; // Update GLOBAL.java too
```

#### GL API
```java
// OLD (JOGL 1.x)
PGraphicsOpenGL pgl = (PGraphicsOpenGL)g;
GL gl = pgl.beginGL();
gl.glEnable(GL.GL_DEPTH_TEST);
pgl.endGL();

// NEW (JOGL 2.x)
PGraphicsOpenGL pg = (PGraphicsOpenGL)g;
PJOGL pgl = (PJOGL)pg.beginPGL();
GL2 gl = pgl.gl.getGL2();
gl.glEnable(GL.GL_DEPTH_TEST);
pg.endPGL();
```

#### Event Registration
```java
// OLD
_main.registerMouseEvent(_modalGUI);

// NEW
_main.registerMethod("mouseEvent", _modalGUI);
```

---

## Build Commands

```bash
# Clean build
ant clean

# Compile only
ant compile

# Full build (creates JAR)
ant build

# Run (after successful build)
ant run.SketchChair
# or
java -jar build/SketchChair.jar
```

---

## Testing Strategy

### After Each Fix Group
1. Compile to check error count decreases
2. Note any new errors introduced
3. Update progress document

### After Phase 2 Complete
1. Build should succeed with 0 errors
2. Test basic run: `java -jar build/SketchChair.jar`
3. Check for runtime errors
4. Test on target platform (Mac/Windows/Linux)

---

## Common Issues & Solutions

### Issue: "cannot find symbol" for Processing classes
**Solution**: Check import statements, Processing 4 may have moved classes

### Issue: GL/GL2 type mismatches
**Solution**: Use JOGL 2.x syntax with proper casting (see patterns above)

### Issue: PApplet no longer extends Component
**Solution**: Use PSurface API instead of direct Component access

### Issue: Import errors for JOGL classes
**Solution**: Add imports:
```java
import com.jogamp.opengl.*;
import processing.opengl.PJOGL;
```

---

## Git Workflow

### Commits
- Commit frequently (after each file or logical group)
- Use descriptive messages: "Phase 2: Fix [issue] in [file]"
- Always update PROCESSING4_UPGRADE.md in commits that complete tasks

### Branch
- Working branch: `modernization-2025`
- Parent branch: `develop` (most recent code)
- Don't merge to master until fully tested

---

## Resources

### Documentation
- **Processing 4 API**: https://processing.org/reference/
- **JOGL 2.x Migration**: https://jogamp.org/wiki/index.php/Migrating_From_JOGL_1_To_JOGL_2
- **PSurface Reference**: https://processing.org/reference/PSurface.html

### In This Repo
- **Detailed Plan**: `processing4-upgrade.md`
- **Original README**: `README.md`
- **Build Config**: `build.xml`

---

## Critical Reminders

1. **Always update PROCESSING4_UPGRADE.md** when completing tasks
2. **Test compilation after each change** (`ant compile`)
3. **Commit frequently** with clear messages
4. **Check for new errors** - fixes can sometimes introduce new issues
5. **Preserve backward compatibility** where possible (file formats!)
6. **Document any discoveries** that aren't in the plan

---

## Quick Status Check

Run this to see current state:
```bash
echo "=== Branch ==="
git branch --show-current
echo -e "\n=== Recent Commits ==="
git log --oneline -5
echo -e "\n=== Compilation Status ==="
ant clean compile 2>&1 | tail -20
echo -e "\n=== Files Changed ==="
git status --short
```

---

## Session End Checklist

Before ending a session:
- [ ] All changes committed
- [ ] PROCESSING4_UPGRADE.md updated with progress
- [ ] Compilation status documented (errors remaining)
- [ ] Any issues/discoveries noted in progress doc
- [ ] Git status is clean (except build artifacts)

---

**Last Updated**: November 5, 2025
**Next Action**: Start Phase 2 - Fix Frame API (main.java has most occurrences)
