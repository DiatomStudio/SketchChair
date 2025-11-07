# Build Script Standardization Plan

## Current Issues

### 1. Version Management
- **Problem**: Version hardcoded as "1.0" in `build.xml` (lines 240, 267, 294, 325)
- **Solution**: Create `version.properties` file and load it in all build scripts

### 2. Inconsistent Naming Conventions

#### Current State:
| Target | Current Output | Issues |
|--------|---------------|---------|
| `jpackage.mac` | `SketchChair.app` | No version in filename |
| `jpackage.mac.dmg` | `SketchChair-1.0.dmg` | Hardcoded version, no platform |
| `jpackage.win` | `SketchChair-1.0.exe` | Hardcoded version, no platform clarity |
| `jpackage.linux` | `sketchchair_1.0-1_amd64.deb` | Inconsistent casing, generated name |
| `dist.windows.portable` | `SketchChair-Windows-Portable.zip` | No version, mixed case platform |
| `make-release.sh` | `SketchChair_v1.0.0.3_macOS.zip` | Underscores, "v" prefix, mixed case |

#### Proposed Standard:
**Format**: `SketchChair-{version}-{platform}-{type}.{ext}`

**Examples**:
- `SketchChair-1.0.3-macos.zip` (macOS app bundle in ZIP)
- `SketchChair-1.0.3-macos.dmg` (macOS DMG installer)
- `SketchChair-1.0.3-windows-installer.exe` (Windows installer)
- `SketchChair-1.0.3-windows-portable.zip` (Windows portable)
- `SketchChair-1.0.3-linux-amd64.deb` (Linux package)

**Rationale**:
- Kebab-case (dashes) - industry standard for filenames
- Lowercase platform names - consistent with package managers
- No "v" prefix - version speaks for itself
- Clear type suffix when needed (installer/portable)

### 3. Changelog Integration
- **Problem**: Only `make-release.sh` includes changelog in packages
- **Solution**: All jpackage targets should copy changelog to dist

### 4. Multiple Build Systems
- **build.xml**: Ant targets for jpackage (direct builds)
- **make-release.sh**: Bash script wrapper (release automation)
- **Result**: Duplication and inconsistency

## Proposed Changes

### Phase 1: Version Properties File ✓
Create `version.properties`:
```properties
version=1.0.3
```

### Phase 2: Update build.xml

1. **Load version.properties**:
```xml
<property file="version.properties"/>
```

2. **Update all `--app-version` args** to use `${version}`

3. **Add changelog copying** to all jpackage targets

4. **Standardize output naming**:
   - macOS: Create `SketchChair-${version}-macos.zip`
   - Windows: Rename to `SketchChair-${version}-windows-installer.exe`
   - Linux: Rename to `SketchChair-${version}-linux-amd64.deb`
   - Portable: `SketchChair-${version}-windows-portable.zip`

### Phase 3: Update make-release.sh

1. **Read version from version.properties** instead of command-line arg (optional)
2. **Use standard naming**: `SketchChair-${VERSION}-{platform}.zip`
3. **Remove "v" prefix** and underscores

### Phase 4: Deprecate Redundancy

**Option A** (Recommended): Keep both systems but clarify:
- **build.xml jpackage targets**: Quick local builds for testing
- **make-release.sh**: Official releases with full automation

**Option B**: Consolidate everything into make-release.sh

## Implementation Order

1. ✓ Create version.properties
2. Update build.xml jpackage targets
3. Update make-release.sh naming
4. Update dist.windows.portable naming
5. Test all targets
6. Update documentation

## Testing Checklist

- [ ] `ant jpackage.mac` produces `dist/SketchChair-1.0.3-macos.zip`
- [ ] `ant jpackage.win` produces `dist/SketchChair-1.0.3-windows-installer.exe`
- [ ] `ant jpackage.linux` produces `dist/SketchChair-1.0.3-linux-amd64.deb`
- [ ] `ant dist.windows.portable` produces `dist/SketchChair-1.0.3-windows-portable.zip`
- [ ] `./make-release.sh 1.0.4` uses version.properties if no arg provided
- [ ] All packages include changelog.txt
- [ ] Version numbers match across all outputs

## Files to Modify

- [x] `version.properties` (create)
- [ ] `build.xml` (update jpackage targets, add version loading)
- [ ] `make-release.sh` (standardize naming)
- [ ] `BUILD_STANDARDIZATION.md` (this file - for reference)
- [ ] `release-instructions.md` (update with new naming)

---

**Created**: 2025-01-06
**Status**: Planning Complete, Implementation Pending
