package cc.sketchchair.sketch;

import toxi.geom.Vec2D.Axis;

//#IF JAVA


//#ENDIF JAVA
//#NOPROCESS

public class Vec2D {


	 public float x;

	 public float y;

	Vec2D X_AXIS = new Vec2D(1, 0);

	Vec2D Y_AXIS = new Vec2D(0, 1);

	
	public static final Vec2D fromTheta(float theta) {
		return new Vec2D((float) Math.cos(theta), (float) Math.sin(theta));
	}



	public Vec2D() {
		x = 0;
		y = 0;
	}

	/**
	 * Creates a new vector with the given coordinates
	 * 
	 * @param x
	 * @param y
	 */
	public Vec2D(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Creates a new vector with the coordinates of the given vector
	 * 
	 * @param v
	 *            vector to be copied
	 */
	public Vec2D(Vec2D v) {
		set(v);
	}

	/**
	 * Adds vector {a,b,c} and returns result as new vector.
	 * 
	 * @param a
	 *            X coordinate
	 * @param b
	 *            Y coordinate
	 * @return result as new vector
	 */
	public final Vec2D add(float a, float b) {
		return new Vec2D(x + a, y + b);
	}

	/**
	 * Add vector v and returns result as new vector.
	 * 
	 * @param v
	 *            vector to add
	 * @return result as new vector
	 */
	public final Vec2D add(Vec2D v) {
		return new Vec2D(x + v.x, y + v.y);
	}

	/**
	 * Adds vector {a,b,c} and overrides coordinates with result.
	 * 
	 * @param a
	 *            X coordinate
	 * @param b
	 *            Y coordinate
	 * @return itself
	 */
	public final Vec2D addSelf(float a, float b) {
		x += a;
		y += b;
		return this;
	}

	/**
	 * Adds vector v and overrides coordinates with result.
	 * 
	 * @param v
	 *            vector to add
	 * @return itself
	 */
	public final Vec2D addSelf(Vec2D v) {
		x += v.x;
		y += v.y;
		return this;
	}

	/**
	 * Computes the angle between this vector and vector V. This function
	 * assumes both vectors are normalized, if this can't be guaranteed, use the
	 * alternative implementation {@link #angleBetween(Vec2D, boolean)}
	 * 
	 * @param v
	 *            vector
	 * @return angle in radians, or NaN if vectors are parallel
	 */
	public final float angleBetween(Vec2D v) {
		return (float) Math.acos(dot(v));
	}

	/**
	 * Computes the angle between this vector and vector V
	 * 
	 * @param v
	 *            vector
	 * @param forceNormalize
	 *            true, if normalized versions of the vectors are to be used
	 *            (Note: only copies will be used, original vectors will not be
	 *            altered by this method)
	 * @return angle in radians, or NaN if vectors are parallel
	 */
	public final float angleBetween(Vec2D v, boolean forceNormalize) {
		float theta;
		if (forceNormalize) {
			theta = getNormalized().dot(v.getNormalized());
		} else {
			theta = dot(v);
		}
		return (float) Math.acos(theta);
	}

	/**
	 * Sets all vector components to 0.
	 * 
	 * @return itself
	 */
	public final Vec2D clear() {
		x = y = 0;
		return this;
	}

	/**
	 * Computes the closest point on the given line segment.
	 * 
	 * @param a
	 *            start point of line segment
	 * @param b
	 *            end point of line segment
	 * @return closest point on the line segment a -> b
	 */

	public Vec2D closestPointOnLine(Vec2D a, Vec2D b) {
		final Vec2D v = b.sub(a);
		final float t = sub(a).dot(v) / v.magSquared();
		// Check to see if t is beyond the extents of the line segment
		if (t < 0.0f) {
			return a;
		}
		if (t > 1.0f) {
			return b;
		}
		// Return the point between 'a' and 'b'
		return a.add(v.scaleSelf(t));
	}

	/**
	 * Finds and returns the closest point on any of the edges of the given
	 * triangle.
	 * 
	 * @param a
	 *            triangle vertex
	 * @param b
	 *            triangle vertex
	 * @param c
	 *            triangle vertex
	 * @return closest point
	 */

	public Vec2D closestPointOnTriangle(Vec2D a, Vec2D b, Vec2D c) {
		Vec2D Rab = closestPointOnLine(a, b);
		Vec2D Rbc = closestPointOnLine(b, c);
		Vec2D Rca = closestPointOnLine(c, a);

		float dAB = sub(Rab).magnitude();
		float dBC = sub(Rbc).magnitude();
		float dCA = sub(Rca).magnitude();

		float min = dAB;
		Vec2D result = Rab;

		if (dBC < min) {
			min = dBC;
			result = Rbc;
		}
		if (dCA < min) {
			result = Rca;
		}

		return result;
	}

	/**
	 * Compares the length of the vector with another one.
	 * 
	 * @param v
	 *            vector to compare with
	 * @return -1 if other vector is longer, 0 if both are equal or else +1
	 */
	public int compareTo(Vec2D v) {
		if (x == v.x && y == v.y) {
			return 0;
		}
		return (int) (magSquared() - v.magSquared());
	}




	/**
	 * @return a new independent instance/copy of a given vector
	 */
	public final Vec2D copy() {
		return new Vec2D(this);
	}

	/**
	 * Calculates the cross-product with the given vector.
	 * 
	 * @param v
	 *            vector
	 * @return the magnitude of the vector that would result from a regular 3D
	 *         cross product of the input vectors, taking their Z values
	 *         implicitly as 0 (i.e. treating the 2D space as a plane in the 3D
	 *         space). The 3D cross product will be perpendicular to that plane,
	 *         and thus have 0 X & Y components (thus the scalar returned is the
	 *         Z value of the 3D cross product vector).
	 * @see <a href="http://stackoverflow.com/questions/243945/">Stackoverflow
	 *      entry</a>
	 */
	public float cross(Vec2D v) {
		return (x * v.y) - (y * v.x);
	}

	/**
	 * Calculates distance to another vector
	 * 
	 * @param v
	 *            non-null vector
	 * @return distance or Float.NaN if v=null
	 */
	public final float distanceTo(Vec2D v) {
		if (v != null) {
			float dx = x - v.x;
			float dy = y - v.y;
			return (float) Math.sqrt(dx * dx + dy * dy);
		} else {
			return 0;
		}
	}

	/**
	 * Calculates the squared distance to another vector
	 * 
	 * @see #magSquared()
	 * @param v
	 *            non-null vector
	 * @return distance or NaN if v=null
	 */
	public final float distanceToSquared(Vec2D v) {
		if (v != null) {
			float dx = x - v.x;
			float dy = y - v.y;
			return dx * dx + dy * dy;
		} else {
			return 0;
		}
	}

	/**
	 * Computes the scalar product (dot product) with the given vector.
	 * 
	 * @see <a href="http://en.wikipedia.org/wiki/Dot_product">Wikipedia entry<
	 *      /a>
	 * 
	 * @param v
	 * @return dot product
	 */
	public final float dot(Vec2D v) {
		return x * v.x + y * v.y;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Vec2D) {
			final Vec2D v = (Vec2D) obj;
			return x == v.x && y == v.y;
		}

		return false;
	}






	public float getComponent(Axis id) {
		switch (id) {
		case X:
			return x;
		case Y:
			return y;
		}
		return 0;
	}


	/**
	 * Scales vector uniformly by factor -1 ( v = -v )
	 * 
	 * @return result as new vector
	 */
	public final Vec2D getInverted() {
		return new Vec2D(-x, -y);
	}

	/**
	 * Creates a copy of the vector with its magnitude limited to the length
	 * given
	 * 
	 * @param lim
	 *            new maximum magnitude
	 * @return result as new vector
	 */
	public final Vec2D getLimited(float lim) {
		if (magSquared() > lim * lim) {
			return getNormalized().scaleSelf(lim);
		}
		return new Vec2D(this);
	}

	/**
	 * Produces the normalized version as a new vector
	 * 
	 * @return new vector
	 */
	public final Vec2D getNormalized() {
		return new Vec2D(this).normalize();
	}

	/**
	 * Produces a new vector normalized to the given length.
	 * 
	 * @param len
	 *            new desired length
	 * 
	 * @return new vector
	 */
	public Vec2D getNormalizedTo(float len) {
		return getNormalized().scaleSelf(len);
	}

	public final Vec2D getPerpendicular() {
		return new Vec2D(this).perpendicular();
	}

	public final Vec2D getReciprocal() {
		return copy().reciprocal();
	}

	/**
	 * Creates a new vector rotated by the given angle around the Z axis.
	 * 
	 * @param theta
	 * @return rotated vector
	 */
	public final Vec2D getRotated(float theta) {
		return new Vec2D(this).rotate(theta);
	}

	/**
	 * Creates a new vector in which all components are replaced with the signum
	 * of their original values. In other words if a components value was
	 * negative its new value will be -1, if zero => 0, if positive => +1
	 * 
	 * @return result vector
	 */
	public Vec2D getSignum() {
		return new Vec2D(this).signum();
	}



	/**
	 * Computes the vector's direction in the XY plane (for example for 2D
	 * points). The positive X axis equals 0 degrees.
	 * 
	 * @return rotation angle
	 */
	public final float heading() {
		return (float) Math.atan2(y, x);
	}

	/**
	 * Interpolates the vector towards the given target vector, using linear
	 * interpolation
	 * 
	 * @param v
	 *            target vector
	 * @param f
	 *            interpolation factor (should be in the range 0..1)
	 * @return result as new vector
	 */
	public final Vec2D interpolateTo(Vec2D v, float f) {
		return new Vec2D(x + (v.x - x) * f, y + (v.y - y) * f);
	}


	/**
	 * Interpolates the vector towards the given target vector, using linear
	 * interpolation
	 * 
	 * @param v
	 *            target vector
	 * @param f
	 *            interpolation factor (should be in the range 0..1)
	 * @return itself, result overrides current vector
	 */
	public final Vec2D interpolateToSelf(Vec2D v, float f) {
		x += (v.x - x) * f;
		y += (v.y - y) * f;
		return this;
	}


	/**
	 * Calculates the distance of the vector to the given sphere in the
	 * specified direction. A sphere is defined by a 3D point and a radius.
	 * Normalized directional vectors expected.
	 * 
	 * @param rayDir
	 *            intersection direction
	 * @param circleOrigin
	 * @param circleRadius
	 * @return distance to sphere in world units, -1 if no intersection.
	 */

	public float intersectRayCircle(Vec2D rayDir, Vec2D circleOrigin,
			float circleRadius) {
		Vec2D q = circleOrigin.sub(this);
		float distSquared = q.magSquared();
		float v = q.dot(rayDir);
		float d = circleRadius * circleRadius - (distSquared - v * v);

		// If there was no intersection, return -1
		if (d < 0.0) {
			return -1;
		}

		// Return the distance to the [first] intersecting point
		return v - (float) Math.sqrt(d);
	}

	/**
	 * Scales vector uniformly by factor -1 ( v = -v ), overrides coordinates
	 * with result
	 * 
	 * @return itself
	 */
	public final Vec2D invert() {
		x *= -1;
		y *= -1;
		return this;
	}

	/**
	 * Checks if the point is inside the given sphere.
	 * 
	 * @param sO
	 *            circle origin/centre
	 * @param sR
	 *            circle radius
	 * @return true, if point is in sphere
	 */

	public boolean isInCircle(Vec2D sO, float sR) {
		float d = sub(sO).magSquared();
		return (d <= sR * sR);
	}



	/**
	 * Checks if vector has a magnitude of 0
	 * 
	 * @return true, if vector = {0,0,0}
	 */
	public final boolean isZeroVector() {
		return x == 0 && y == 0;
	}

	/**
	 * Limits the vector's magnitude to the length given
	 * 
	 * @param lim
	 *            new maximum magnitude
	 * @return itself
	 */
	public final Vec2D limit(float lim) {
		if (magSquared() > lim * lim) {
			return normalize().scaleSelf(lim);
		}
		return this;
	}

	/**
	 * Calculates the magnitude/eucledian length of the vector
	 * 
	 * @return vector length
	 */
	public final float magnitude() {
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * Calculates only the squared magnitude/length of the vector. Useful for
	 * inverse square law applications and/or for speed reasons or if the real
	 * eucledian distance is not required (e.g. sorting).
	 * 
	 * Please note the vector should contain cartesian (not polar) coordinates
	 * in order for this function to work. The magnitude of polar vectors is
	 * stored in the x component.
	 * 
	 * @return squared magnitude (x^2 + y^2)
	 */
	public final float magSquared() {
		return x * x + y * y;
	}




	/**
	 * Normalizes the vector so that its magnitude = 1
	 * 
	 * @return itself
	 */
	public final Vec2D normalize() {
		float mag = x * x + y * y;
		if (mag > 0) {
			mag = 1f / (float) Math.sqrt(mag);
			x *= mag;
			y *= mag;
		}
		return this;
	}

	/**
	 * Normalizes the vector to the given length.
	 * 
	 * @param len
	 *            desired length
	 * @return itself
	 */
	public Vec2D normalizeTo(float len) {
		return normalize().scaleSelf(len);
	}

	public final Vec2D perpendicular() {
		float t = x;
		x = -y;
		y = t;
		return this;
	}


	public final Vec2D reciprocal() {
		x = 1f / x;
		y = 1f / y;
		return this;
	}

	/**
	 * Rotates the vector by the given angle around the Z axis.
	 * 
	 * @param theta
	 * @return itself
	 */
	public final Vec2D rotate(float theta) {
		float co = (float) Math.cos(theta);
		float si = (float) Math.sin(theta);
		float xx = co * x - si * y;
		y = si * x + co * y;
		x = xx;
		return this;
	}

	/**
	 * Scales vector uniformly and returns result as new vector.
	 * 
	 * @param s
	 *            scale factor
	 * @return new vector
	 */
	public final Vec2D scale(float s) {
		return new Vec2D(x * s, y * s);
	}

	/**
	 * Scales vector non-uniformly and returns result as new vector.
	 * 
	 * @param a
	 *            scale factor for X coordinate
	 * @param b
	 *            scale factor for Y coordinate
	 * @return new vector
	 */
	public final Vec2D scale(float a, float b) {
		return new Vec2D(x * a, y * b);
	}

	/**
	 * Scales vector non-uniformly by vector v and returns result as new vector
	 * 
	 * @param s
	 *            scale vector
	 * @return new vector
	 */
	public final Vec2D scale(Vec2D s) {
		return new Vec2D(x * s.x, y * s.y);
	}

	/**
	 * Scales vector uniformly and overrides coordinates with result
	 * 
	 * @param s
	 *            scale factor
	 * @return itself
	 */
	public Vec2D scaleSelf(float s) {
		x *= s;
		y *= s;
		return this;
	}

	/**
	 * Scales vector non-uniformly by vector {a,b,c} and overrides coordinates
	 * with result
	 * 
	 * @param a
	 *            scale factor for X coordinate
	 * @param b
	 *            scale factor for Y coordinate
	 * @return itself
	 */
	public final Vec2D scaleSelf(float a, float b) {
		x *= a;
		y *= b;
		return this;
	}

	/**
	 * Scales vector non-uniformly by vector v and overrides coordinates with
	 * result
	 * 
	 * @param s
	 *            scale vector
	 * @return itself
	 */

	public final Vec2D scaleSelf(Vec2D s) {
		x *= s.x;
		y *= s.y;
		return this;
	}

	/**
	 * Overrides coordinates with the given values
	 * 
	 * @param x
	 * @param y
	 * @return itself
	 */
	public final Vec2D set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	/**
	 * Overrides coordinates with the ones of the given vector
	 * 
	 * @param v
	 *            vector to be copied
	 * @return itself
	 */
	public final Vec2D set(Vec2D v) {
		x = v.x;
		y = v.y;
		return this;
	}

	public Vec2D setComponent(Axis id, float val) {
		switch (id) {
		case X:
			x = val;
			break;
		case Y:
			y = val;
			break;
		}
		return this;
	}

	/**
	 * Replaces all vector components with the signum of their original values.
	 * In other words if a components value was negative its new value will be
	 * -1, if zero => 0, if positive => +1
	 * 
	 * @return itself
	 */
	public Vec2D signum() {
		x = (x < 0 ? -1 : x == 0 ? 0 : 1);
		y = (y < 0 ? -1 : y == 0 ? 0 : 1);
		return this;
	}

	/**
	 * Subtracts vector {a,b,c} and returns result as new vector.
	 * 
	 * @param a
	 *            X coordinate
	 * @param b
	 *            Y coordinate
	 * @return result as new vector
	 */
	public final Vec2D sub(float a, float b) {
		return new Vec2D(x - a, y - b);
	}


	public final Vec2D sub(Vec2D v) {
		return new Vec2D(x - v.x, y - v.y);
	}


	public final Vec2D subSelf(float a, float b) {
		x -= a;
		y -= b;
		return this;
	}


	public final Vec2D subSelf(Vec2D v) {
		x -= v.x;
		y -= v.y;
		return this;
	}


	public Vec2D tangentNormalOfEllipse(Vec2D eO, Vec2D eR) {
		Vec2D p = this.sub(eO);

		float xr2 = eR.x * eR.x;
		float yr2 = eR.y * eR.y;

		return new Vec2D(p.x / xr2, p.y / yr2).normalize();
	}



	public float[] toArray() {
		return new float[] { x, y };
	}

	public Vec2D toCartesian() {
		float xx = (float) (x * Math.cos(y));
		y = (float) (x * Math.sin(y));
		x = xx;
		return this;
	}


	public Vec2D toPolar() {
		float r = (float) Math.sqrt(x * x + y * y);
		y = (float) Math.atan2(y, x);
		x = r;
		return this;
	}


	public String toString() {
		StringBuffer sb = new StringBuffer(32);
		sb.append("{x:").append(x).append(", y:").append(y).append("}");
		return sb.toString();
	}
	
	
}


