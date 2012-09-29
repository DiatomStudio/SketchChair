package cc.sketchchair.triangulate;

import java.util.*;

/**
 * class to perform Delaunay triangulation.
 * @author gregsaul
 *
 */
public class Delaunay {

	class Edge {
		Triangle left;
		Triangle right;
		Vertex start;
		Vertex end;
		boolean constrained;
		boolean flipped;
		int index;

		Edge(Vertex v0, Vertex v1, boolean c) {
			start = v0;
			end = v1;
			constrained = c;

			start.edges.add(this);
			end.edges.add(this);

		}

		public boolean contains(Vertex v) {
			return (v == start || v == end);
		}

		public void delete() {
			start.remove(this);
			end.remove(this);
		}

		public Vertex get_common_vertex(Edge edge) {
			if (edge.contains(start))
				return start;
			if (edge.contains(end))
				return end;
			return null;
		}

		public void remove(Triangle triangle) {
			if (left == triangle)
				left = null;
			if (right == triangle)
				right = null;
		}

		public void set_left(Triangle triangle) {
			//	if (left != null)	LOGGE("Error in Edge.set_left");
			left = triangle;
		}

		public void set_right(Triangle triangle) {
			//	if (right != null)	System.out.println("Error in Edge.set_right");
			right = triangle;
		}
	}

	public class Triangle {
		Edge[] edges = new Edge[3];

		Triangle(Edge e0, Edge e1, Edge e2) {
			edges[0] = e0;
			edges[1] = e1;
			edges[2] = e2;

			if (e1.contains(e0.end))
				e0.set_left(this);
			else
				e0.set_right(this);
			if (e2.contains(e1.end))
				e1.set_left(this);
			else
				e1.set_right(this);
			if (e0.contains(e2.end))
				e2.set_left(this);
			else
				e2.set_right(this);
		}

		public void delete() {
			edges[0].remove(this);
			edges[1].remove(this);
			edges[2].remove(this);
		}

		public Edge get_the_other_edge(Vertex v) {
			if (!edges[0].contains(v))
				return edges[0];
			if (!edges[1].contains(v))
				return edges[1];
			if (!edges[2].contains(v))
				return edges[2];
			return null;
		}

		public Vertex get_the_other_vertex(Edge edge) {
			if (edge == edges[0])
				return edges[1].get_common_vertex(edges[2]);
			if (edge == edges[1])
				return edges[2].get_common_vertex(edges[0]);
			if (edge == edges[2])
				return edges[0].get_common_vertex(edges[1]);
			return null;
		}

		public Vertex get_vertex(int i) {
			if (i == 0)
				return edges[1].get_common_vertex(edges[2]);
			if (i == 1)
				return edges[2].get_common_vertex(edges[0]);
			if (i == 2)
				return edges[0].get_common_vertex(edges[1]);
			return null;
		}
	}

	public class Vertex extends Vector2D {
		ArrayList edges = new ArrayList();
		int index;

		Vertex(Vector2D v, int index) {
			x = v.x;
			y = v.y;
			this.index = index;

		}

		public int getIndex() {
			return this.index;
		}

		public void remove(Edge edge) {
			edges.remove(edge);
		}

		public Vector2D Vector2D() {
			return new Vector2D(x, y);
		}
	}

	// return {delaunay_edges, delaunay_triangles}
	public static ArrayList triangulate(ArrayList loop) {
		return (new Delaunay()).triangulate_main(loop);
	}

	public ArrayList triangles;

	public ArrayList vertexs;
	public ArrayList edges;
	public Vertex baseVertex;
	public Vertex prevVertex;
	public Edge prevEdge;
	Vertex latest_vertex;

	public Edge add(Edge edge) {
		edges.add(edge);
		return edge;
	}

	public Triangle add(Triangle triangle) {
		triangles.add(triangle);
		return triangle;
	}

	public void add_vertex(Vertex v) {
		Edge edge0 = add(new Edge(v, baseVertex, false));
		Edge edge1 = add(new Edge(prevVertex, v, true));
		Triangle triangle = add(new Triangle(prevEdge, edge1, edge0));

		latest_vertex = v;
		clearFlipped();
		recursiveFlip(prevEdge, null);

		clearFlipped();
		for (int i = 0; i < edges.size(); i++) {
			Edge edge = (Edge) edges.get(i);
			//if (double_flipped(edge)){
			recursiveFlip(edge, null);
			clearFlipped();
			//}
		}

		prevVertex = v;
		prevEdge = edge0;
	}

	public void clearFlipped() {
		for (int i = 0; i < edges.size(); i++)
			((Edge) edges.get(i)).flipped = false;
	}

	public void delete(Edge edge) {
		edges.remove(edge);
		edge.delete();
	}

	public void delete(Triangle triangle) {
		triangles.remove(triangle);
		triangle.delete();
	}

	public boolean double_flipped(Edge edge) {
		if (edge.flipped)
			return false;
		if (edge.constrained)
			return false;
		Triangle triangle0 = edge.left;
		Triangle triangle1 = edge.right;
		if (triangle0 == null || triangle1 == null)
			return false;
		Vertex v0 = triangle0.get_the_other_vertex(edge);
		Vertex v1 = triangle1.get_the_other_vertex(edge);
		return (same_side(edge.start, edge.end, v0, v1));// &&same_side(v0, v1, edge.start, edge.end));
	}

	public double get_angle(Vertex v0, Vertex v1, Vertex v2) {
		Vector2D vec0 = new Vector2D(v1, v0);
		Vector2D vec1 = new Vector2D(v1, v2);
		return vec0.get_relative_angle(vec1);
	}

	public double min(double a, double b, double c, double d) {
		return Math.min(Math.min(a, b), Math.min(c, d));
	}

	public void recursiveFlip(Edge edge, Edge fixed) {
		if (edge.constrained)
			return;

		Triangle triangle0 = edge.left;
		Triangle triangle1 = edge.right;

		if (triangle0 == null || triangle1 == null)
			return;

		Vertex v0 = triangle0.get_the_other_vertex(edge);
		Vertex v1 = triangle1.get_the_other_vertex(edge);

		if (should_flip(edge, v0, v1)) {
			Edge edge0s = triangle0.get_the_other_edge(edge.end);
			Edge edge0e = triangle0.get_the_other_edge(edge.start);
			Edge edge1s = triangle1.get_the_other_edge(edge.end);
			Edge edge1e = triangle1.get_the_other_edge(edge.start);

			delete(triangle0);
			delete(triangle1);
			delete(edge);

			Edge flippeEdge = add(new Edge(v0, v1, false));
			flippeEdge.flipped = true;
			triangle0 = add(new Triangle(flippeEdge, edge1e, edge0e));
			triangle1 = add(new Triangle(flippeEdge, edge0s, edge1s));

			if (edge0s != fixed)
				recursiveFlip(edge0s, edge);
			if (edge0e != fixed)
				recursiveFlip(edge0e, edge);
			if (edge1s != fixed)
				recursiveFlip(edge1s, edge);
			if (edge1e != fixed)
				recursiveFlip(edge1e, edge);
		}
	}

	public boolean same_side(Vertex start, Vertex end, Vertex v0, Vertex v1) {
		Vector2D axis = new Vector2D(start, end);
		Vector2D vec0 = new Vector2D(start, v0);
		Vector2D vec1 = new Vector2D(start, v1);
		return (axis.cross_product(vec0) * axis.cross_product(vec1) > 0);
	}

	public boolean should_flip(Edge edge, Vertex v0, Vertex v1) {
		if (edge.flipped)
			return false;

		if (v0.x == v1.x && v0.y == v1.y)
			return false;

		if (same_side(edge.start, edge.end, v0, v1)) {
			return true;
		}

		if (same_side(v0, v1, edge.start, edge.end))
			return false;

		double originalMinimumAngle = min(get_angle(edge.start, edge.end, v0),
				get_angle(edge.end, edge.start, v0),
				get_angle(edge.start, edge.end, v1),
				get_angle(edge.end, edge.start, v1));
		double flippedMinimumAngle = min(get_angle(edge.start, v0, v1),
				get_angle(edge.start, v1, v0), get_angle(edge.end, v0, v1),
				get_angle(edge.end, v1, v0));

		return (flippedMinimumAngle > originalMinimumAngle);
	}

	public ArrayList triangulate_main(ArrayList loop) {
		triangles = new ArrayList();
		edges = new ArrayList();
		vertexs = new ArrayList();

		Vertex v0 = new Vertex((Vector2D) loop.get(0), 0);
		Vertex v1 = new Vertex((Vector2D) loop.get(1), 1);
		Vertex v2 = new Vertex((Vector2D) loop.get(2), 2);
		Edge e0 = add(new Edge(v0, v1, true));
		Edge e1 = add(new Edge(v1, v2, true));
		Edge e2 = add(new Edge(v2, v0, false));
		Triangle t0 = add(new Triangle(e0, e1, e2));
		vertexs.add(v0);
		vertexs.add(v1);
		vertexs.add(v2);

		baseVertex = v0;
		prevVertex = v2;
		prevEdge = e2;
		for (int i = 3; i < loop.size(); i++) {
			Vertex v = new Vertex((Vector2D) loop.get(i), i);
			add_vertex(v);
			vertexs.add(v);
		}

		//		ArrayList delaunay_edges = new ArrayList();
		//		for(int i=0; i<edges.size(); i++){
		//			Edge edge = (Edge) edges.get(i);
		//			edge.index = i;
		//			int[] delaunay_edge = {edge.start.index, edge.end.index};
		//			delaunay_edges.add(delaunay_edge);
		//		}
		//		
		//		ArrayList delaunay_triangles = new ArrayList();
		//		for(int i=0; i<triangles.size(); i++){
		//			Triangle triangle = (Triangle) triangles.get(i);
		//			int[] delaunay_triangle = {triangle.edges[0].index, triangle.edges[1].index, triangle.edges[2].index};
		//			delaunay_triangles.add(delaunay_triangle);
		//		}
		//
		//		ArrayList[] result = {delaunay_edges, delaunay_triangles};		
		//		return result;	

		ArrayList delaunay_triangles = new ArrayList();
		for (int i = 0; i < triangles.size(); i++) {
			Triangle triangle = (Triangle) triangles.get(i);
			int[] delaunay_triangle = {
					triangle.edges[0].get_common_vertex(triangle.edges[1]).index,
					triangle.edges[1].get_common_vertex(triangle.edges[2]).index,
					triangle.edges[2].get_common_vertex(triangle.edges[0]).index };
			//int[] delaunay_triangle = {triangle.edges[0].index, triangle.edges[1].index, triangle.edges[2].index};
			delaunay_triangles.add(delaunay_triangle);
		}
		return delaunay_triangles;

	}

}
