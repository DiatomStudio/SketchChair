package processing.core;

/**
 * Compatibility shim for Processing 4.
 * In Processing 4, PGraphicsJava2D was moved to processing.awt package.
 * This class provides backward compatibility for libraries that expect it in processing.core.
 */
public class PGraphicsJava2D extends processing.awt.PGraphicsJava2D {
  // Inherit all functionality from the new location
}
