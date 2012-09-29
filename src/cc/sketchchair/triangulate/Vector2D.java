package cc.sketchchair.triangulate;

import java.awt.Point;

public class Vector2D {
	/*
	public static double distance(Vertex2D start, Vertex2D end) {
	    return distance(start.x, start.y, end.x, end.y);
	}

	public Vertex2D vertex2D() {
	    return new Vertex2D(x, y);
	}
	*/
	static public Vector2D add(Vector2D u, Vector2D v) {
		return new Vector2D(u.x + v.x, u.y + v.y);
	}

	public static double cos(Vector2D u, Vector2D v) {
		double length = Math.sqrt((u.x * u.x + u.y * u.y)
				* (v.x * v.x + v.y * v.y));
		if (length > 0) {
			return dot_product(u, v) / length;
		} else {
			return 0;
		}
	}

	public static double cross_product(Vector2D u, Vector2D v) {
		return u.x * v.y - u.y * v.x;
	}

	public static double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	public static double distance(int x1, int y1, int x2, int y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	public static double distance(Point start, Point end) {
		return distance(start.x, start.y, end.x, end.y);
	}

	public static double distance(Vector2D start, Vector2D end) {
		return distance(start.x, start.y, end.x, end.y);
	}

	public static double dot_product(Vector2D u, Vector2D v) {
		return u.x * v.x + u.y * v.y;
	}

	public static double get_angle_360(Vector2D u, Vector2D v) {
		double cos = cos(u, v);
		double sin = sin(u, v);
		if (cos == 0) {
			if (sin > 0) {
				return 90;
			} else {
				return 270;
			}
		}
		if (sin == 0) {
			if (cos > 0) {
				return 0;
			} else {
				return 180; // -PI/2   ...   +PI/2
			}
		}
		double angle = 180 * Math.atan(sin / cos) / Math.PI;

		if (cos < 0) {
			angle = angle + 180;
		}
		if (angle < 0) {
			angle = angle + 360;
		}
		return angle;
	}

	public static Vector2D interpolate(Point start, Point end, double t) {
		return new Vector2D(start.x * (1 - t) + end.x * t, start.y * (1 - t)
				+ end.y * t);
	}

	public static Vector2D interpolate(Vector2D start, Vector2D end, double t) {
		return new Vector2D(start.x * (1 - t) + end.x * t, start.y * (1 - t)
				+ end.y * t);
	}

	public static Vector2D multiply(Vector2D v, double b) {
		return new Vector2D(v.x * b, v.y * b);
	}

	public static double sin(Vector2D u, Vector2D v) {
		double length = Math.sqrt((u.x * u.x + u.y * u.y)
				* (v.x * v.x + v.y * v.y));
		if (length > 0) {
			return cross_product(u, v) / length;
		} else {
			return 0;
		}
	}

	static public Vector2D subtract(Vector2D u, Vector2D v) {
		return new Vector2D(u.x - v.x, u.y - v.y);
	}

	// 2D vecoter
	// double x,y
	// vector2(Point, Point)
	// double inner_product(Vector2)
	// double get_cos(Vector2)
	// double distance(x1,y1,x2,y2)
	public double x;

	public double y;

	public Vector2D() {
	}

	public Vector2D(double _x, double _y) {
		x = _x;
		y = _y;
	}

	public Vector2D(Point p) {
		x = p.x;
		y = p.y;
	}

	//Vector2(Node start, Node end){
	//	x = end.x -  start.x;
	//	y = end.y -  start.y;
	//}
	public Vector2D(Point start, Point end) {
		x = end.x - start.x;
		y = end.y - start.y;
	}

	public Vector2D(Vector2D start, Vector2D end) {
		x = end.x - start.x;
		y = end.y - start.y;
	}

	protected Vector2D add(Vector2D v) {
		return new Vector2D(x + v.x, y + v.y);
	}

	public void add_self(Vector2D v) {
		x += v.x;
		y += v.y;
	}

	public double cos(Vector2D v) {
		return get_cos(v);
	}

	public double cross_product(Vector2D v) {
		return x * v.y - y * v.x;
	}

	public double dot_product(Vector2D v) {
		return x * v.x + y * v.y;
	}

	public Vector2D flip_y_axis() {
		return new Vector2D(-x, y);
	}

	public double get_angle(Vector2D v) {
		double cos = get_cos(v);
		double sin = get_sin(v);
		if (cos == 0) {
			if (sin > 0) {
				return 90;
			} else {
				return 270;
			}
		}
		if (sin == 0) {
			if (cos > 0) {
				return 0;
			} else {
				return 180; // -PI/2   ...   +PI/2
			}
		}
		double angle = 180 * Math.atan(sin / cos) / Math.PI;

		if (cos < 0) {
			angle = angle + 180;
		}
		if (angle < 0) {
			angle = angle + 360;
		}
		return angle;
	}

	public double get_cos(Vector2D v) {
		double length = Math.sqrt((x * x + y * y) * (v.x * v.x + v.y * v.y));
		if (length > 0) {
			return inner_product(v) / length;
		} else {
			return 0;
		}
	}

	public Vector2D get_normalized() {
		double length = length();
		if (length == 0) {
			length = 1;
		}
		return new Vector2D(x / length, y / length);
	}

	public double get_relative_angle(Vector2D v) {
		double cosine = cos(v);

		if (cosine <= -1) {
			return Math.PI;
		} else if (cosine >= 1) {
			return 0;
		} else {
			return Math.acos(cos(v));
		}
	}

	public double get_sin(Vector2D v) {
		double length = Math.sqrt((x * x + y * y) * (v.x * v.x + v.y * v.y));
		if (length > 0) {
			return cross_product(v) / length;
		} else {
			return 0;
		}
	}

	public double inner_product(Vector2D v) {
		return x * v.x + y * v.y;
	}

	public double length() {
		return Math.sqrt(x * x + y * y);
	}

	public Vector2D multiple(double m) {
		return new Vector2D(x * m, y * m);
	}

	public void multiple_self(double m) {
		x *= m;
		y *= m;
	}

	public Vector2D normalize() {
		double length = length();
		if (length == 0) {
			length = 1;
		}
		return new Vector2D(x / length, y / length);
	}

	public void normalize_self() {
		double length = length();
		if (length == 0) {
			length = 1;
		}
		x = x / length;
		y = y / length;
	}

	public double outer_product(Vector2D v) {
		return x * v.y - y * v.x;
	}

	public Point point() {
		return new Point((int) x, (int) y);
	}

	public Vector2D rotate(double degree) {
		if (degree == 90) {
			return new Vector2D(-y, x);
		} else if (degree == 180) {
			return new Vector2D(-x, -y);
		} else if (degree == 270) {
			return new Vector2D(y, -x);
		}
		double radian = degree * Math.PI / 180.0;
		double cos = Math.cos(radian);
		double sin = Math.sin(radian);

		return new Vector2D(x * cos - y * sin, x * sin + y * cos);
	}

	public Vector2D rotate90() {
		return new Vector2D(-y, x);
	}

	public void rotate90_self() {
		double xx = x;
		double yy = y;
		x = -y;
		y = x;
	}

	public Vector2D scale(double scale) {
		return new Vector2D(x * scale, y * scale);
	}

	public double sin(Vector2D v) {
		return get_sin(v);
	}

	public void subtract(Vector2D v) {
		x -= v.x;
		y -= v.y;
	}
}
