/**
 *                 SlicePlane
 *                 ___________________
 *                /    / /           /
 *               /    / /_____      /
 *              /    / ___   /     /
 *             /    / /   / /     /
 *            /    /_/   /_/     /
 *           /__________________/
 */

package cc.sketchchair.geometry;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Element;

import ShapePacking.BezierControlNode;
import ShapePacking.spShape;
import cc.sketchchair.core.CrossSliceSelection;
import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.PickBuffer;
import cc.sketchchair.core.SETTINGS;
import cc.sketchchair.core.UITools;
import cc.sketchchair.functions.functions;
import cc.sketchchair.sketch.SETTINGS_SKETCH;
import cc.sketchchair.sketch.Sketch;
import cc.sketchchair.sketch.SketchOutline;
import cc.sketchchair.sketch.SketchPath;
import cc.sketchchair.sketch.SketchPoint;
import cc.sketchchair.sketch.SketchShape;
import cc.sketchchair.sketch.SketchSpline;
import cc.sketchchair.sketch.SliceSlot;
import cc.sketchchair.triangulate.Delaunay;

import com.bulletphysics.collision.shapes.IndexedMesh;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;

import processing.core.PApplet;
import processing.core.PGraphics;
import toxi.geom.Plane;
import toxi.geom.Ray3D;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

/**
 * SlicePlanes are the planes that contain a Sketch. A Sliceplane is made for each part of the design. 
 * @author gregsaul
 *
 */
public class SlicePlane {

	SketchSpline sketchSpline = null;

	private Sketch sketch;

	Vec3D offset = null;
	Vec3D planeNormal = null;
	private Plane plane = null;
	SliceSlot constraintSlot = null;

	transient IndexedMesh indexedMesh;
	transient TriangleIndexVertexArray indexVertexArray;

	private boolean selected = false;

	public Vec2D debugPoint = new Vec2D();

	public boolean destroy = false;

	public float profileHeight;

	public float profileMinY;
	public float profileMaxY;

	public SketchPath coverPath;

	public boolean tiedToLeg = false;

	public float thickness = SETTINGS_SKETCH.plane_thickness;
	//public float thickness_pixels = SETTINGS_SKETCH.plane_thickness_mm*SETTINGS_SKETCH.pixels_per_mm;

	public boolean guide = false;

	private Vec2D debugMousePoint = null;

	public List<Vec2D> debugIntersectionPoints = new ArrayList();
	public List<Vec2D> debugIntersectionPointsTop = new ArrayList();
	public List<Vec2D> debugIntersectionPointsBottom = new ArrayList();

	public Vec2D debugIntersetStart = null;
	
	private CrossSliceSelection crossSliceSelection = null;

	
int id; 

public int getId() {
	return this.id;
}
public void setId(int id) {
	this.id = id;
}
	public SlicePlane(Element element) {

		//wrong type
		if (!element.getLocalName().equals("SlicePlane"))
			return;

		
		
		if (element.getAttributeValue("id") != null)
			this.setId(Integer.parseInt(element.getAttributeValue("id")));
			
			
		if (element.getAttributeValue("guide") != null)
			this.guide = true;

		this.setSelected(true);

		for (int i = 0; i < element.getChildCount(); i++) {
			Element child = (Element) element.getChild(i);

			if (child != null && child.getLocalName().equals("Sketch")) {
				this.setSketch(new Sketch(GLOBAL.uiTools.SketchTools,
						GLOBAL.SketchGlobals, child));
				this.getSketch().setOnSlicePlane(this);
			}

			if (child != null && child.getLocalName().equals("SketchShapes")) {
				this.setSketch(new Sketch(GLOBAL.uiTools.SketchTools,
						GLOBAL.SketchGlobals, child));
				this.getSketch().setOnSlicePlane(this);
			}

			if (child != null && child.getLocalName().equals("Plane")) {

				if (child.getAttributeValue("x") != null
						&& child.getAttributeValue("y") != null
						&& child.getAttributeValue("z") != null
						&& child.getAttributeValue("nx") != null
						&& child.getAttributeValue("ny") != null
						&& child.getAttributeValue("nz") != null) {
					float xmlX = Float.valueOf(child.getAttributeValue("x"));
					float xmlY = Float.valueOf(child.getAttributeValue("y"));
					float xmlZ = Float.valueOf(child.getAttributeValue("z"));

					float xmlNX = Float.valueOf(child.getAttributeValue("nx"));
					float xmlNY = Float.valueOf(child.getAttributeValue("ny"));
					float xmlNZ = Float.valueOf(child.getAttributeValue("nz"));

					Plane pl = new Plane(new Vec3D(xmlX, xmlY, xmlZ),
							new Vec3D(xmlNX, xmlNY, xmlNZ));
					this.setPlane(pl);
				} else {
					LOGGER.warning("requred data not found in XML file");
					return;
				}
			}

		}

		if (this.getPlane() == null) {
			this.setPlane(new Plane());
			LOGGER.warning("no Plane def found in XML making a defaul plane");

		}

	}

	public SlicePlane(Plane plane) {
		this.setId(GLOBAL.planeID++);

		setSketch(new Sketch(GLOBAL.uiTools.SketchTools, GLOBAL.SketchGlobals,
				this));
		this.setPlane(plane);
		this.getSketch().setOnSlicePlane(this);
		this.select();
	}

	public SlicePlane(Sketch parentSketch, Plane plane) {
		this.setId(GLOBAL.planeID++);

		
		this.setSketch(parentSketch);
		this.getSketch().setOnSlicePlane(this);
		this.setPlane(plane);
		coverPath = new SketchPath(getSketch());
		//this.build();
		this.select();

		//	GLOBAL.undo.addOperation(new UndoAction(this, UndoAction.ADD_PLANE));
	}

	public SlicePlane(Sketch Sketch, SliceSlot slot) {
		this.setId(GLOBAL.planeID++);

		//setSketch( new Sketch(GLOBAL.uiTools.SketchTools, GLOBAL.SketchGlobals,this));
		this.constraintSlot = slot;
		this.setSketch(Sketch);
		this.setPlane(this.constraintSlot.constrainPlane);
		this.getSketch().setOnSlicePlane(this);
		this.build();
		this.select();
		
		// this.Sketch.add(linkedSpline);
	}

	public boolean addPointAlongPath(float x, float y) {
		return this.getSketch().addPointAlongPath(x, y);
	}

	public void applyRotationMatrix(PGraphics g) {

		g.translate(this.getPlane().x, this.getPlane().y, this.getPlane().z);

		// g.rotateX(this.plane.normal.headingXY());
		g.rotateY((float) (functions.angleOf(new Vec2D(
				this.getPlane().normal.x, this.getPlane().normal.z)) - (Math.PI / 2)));
		// g.rotateY((float) Math.PI/2);

		float rotateAn = (functions.angleOf(new Vec2D(this.getPlane().normal.x,
				this.getPlane().normal.y)));

		// rotateAn = this.testAn;

		if (rotateAn < Math.PI / 2 || rotateAn > (Math.PI / 2) * 3)
			rotateAn = (float) ((Math.PI * 2) - Math.abs(rotateAn));

		g.rotateX(rotateAn);

	}

	void build() {
		this.getSketch().build();
	}

	public void buildCoverPath() {

		if (this.getSketch().getSketchShapes().sketchOutlines
				.getOutterOutline() != null)
			this.coverPath = this.getSketch().getSketchShapes().sketchOutlines
					.getOutterOutline().getPath();
	}

	public void checkForCollisions() {
		
		//remove slots that don't pierce the edge at all. 
		this.getSketch().getSlots().removeNonPiercing(this.getSketch());

		//remove slots that collide with others
		this.getSketch().getSlots().removeIntersecting();

		//remove slots that are impossible to put in
		this.getSketch().getSlots().removeTrappedSlots(this.getSketch());
		
		//First check if slots are colliding if they are remove one plane
	//	this.getSketch().getSlots().checkForSlotCollisions();
		
		
	}

	@Override
	public SlicePlane clone() {
		SlicePlane copiedPlane = new SlicePlane(
				this.getSketch().clone(),
				new Plane(this.getPlane().copy(), this.getPlane().normal.copy()));
		return copiedPlane;
	}

	public SlicePlane copy() {
		SlicePlane newSlicePlane = new SlicePlane(new Plane(this.plane.copy(),
				this.plane.normal.copy()));
		newSlicePlane.guide = this.guide;
		newSlicePlane.setSketch(this.getSketch().copy());
		newSlicePlane.selected = this.selected;
		newSlicePlane.getSketch().setOnSketchPlane(newSlicePlane);
		newSlicePlane.setId(this.getId());
		return newSlicePlane;

	}

	public void destroy() {
		this.destroy = true;
	}

	public void flipHorizontal(Vec3D centre) {
		this.getSketch().flipHorizontal(centre);
	}

	public void generate() {
		this.profileHeight = this.getSketch().getHeight();
		this.profileMinY = this.getSketch().getMinY();
		this.profileMaxY = this.getSketch().getMaxY();
	}

	public float getArea() {
		return this.getSketch().getArea();
	}

	public Vec3D getCentreOfMass() {
		Vec2D centre = this.getSketch().getCentreOfMass();

		if (centre != null)
			return new Vec3D(GLOBAL.jBullet.scaleVal(centre.x),
					GLOBAL.jBullet.scaleVal(centre.y),
					GLOBAL.jBullet.scaleVal(this.getPlane().z));
		else
			return null;
	}

	public SketchPoint getClosestPathVertex(Vec2D pointOnPlan) {
		return this.getSketch().getClosestPathVertex(pointOnPlan);
	}

	public IndexedMesh getIndexedMesh(float offsetX, float offsetY,
			float offsetZ) {

		IndexedMesh indexedMesh = new IndexedMesh();
		// Vec2D centre = this.sketchSpline.getCentreOfMass();

		this.getSketch().buildOutline();

		if (this.getSketch().getSketchShapes().sketchOutlines
				.getOutterOutline() != null) {
			this.getSketch().getSketchShapes().sketchOutlines
					.getOutterOutline().optimizeForCollision();

			ArrayList loop = this.getSketch().getSketchShapes().sketchOutlines
					.getOutterOutline().getVector2DLoop();
			Delaunay triangulate = new Delaunay();

			if (loop == null || loop.size() < 3)
				return null;

			triangulate.triangulate_main(loop);

			int vertNum = triangulate.vertexs.size() * 3;

			float[] gVertices = new float[vertNum];
			for (int i = 0; i < triangulate.vertexs.size(); i++) {
				Delaunay.Vertex v = (Delaunay.Vertex) triangulate.vertexs
						.get(i);

				Vec3D pointOnePlane = new Vec3D(new Float(v.x), new Float(v.y),
						0);

				if (this.getPlane().normal.z == -1) {
					pointOnePlane.z = this.getPlane().z;
				} else {
					pointOnePlane = pointOnePlane.rotateY(PApplet.PI / 2);

					float planeAngle = functions.angleOf(new Vec2D(this
							.getPlane().normal.x, this.getPlane().normal.y));
					pointOnePlane = pointOnePlane.rotateZ(-planeAngle);
					pointOnePlane.x += this.getPlane().x;
					pointOnePlane.y += this.getPlane().y;
					pointOnePlane.z += this.getPlane().z;
				}

				gVertices[(3 * v.getIndex()) + 0] = GLOBAL.jBullet
						.scaleVal((float) pointOnePlane.x) - offsetX;
				gVertices[(3 * v.getIndex()) + 1] = GLOBAL.jBullet
						.scaleVal((float) -pointOnePlane.y) + offsetY;
				gVertices[(3 * v.getIndex()) + 2] = GLOBAL.jBullet
						.scaleVal(pointOnePlane.z) - offsetZ;

			}

			int indNum = (triangulate.triangles.size() * 3);
			int triNum = triangulate.triangles.size();
			int indDiffNum = (triangulate.triangles.size());
			int[] gIndices = new int[indNum];

			for (int i = 0; i < triangulate.triangles.size(); i++) {
				Delaunay.Triangle t = (Delaunay.Triangle) triangulate.triangles
						.get(i);
				gIndices[(i * 3) + 0] = (t.get_vertex(0).getIndex());
				gIndices[(i * 3) + 1] = (t.get_vertex(1).getIndex());
				gIndices[(i * 3) + 2] = (t.get_vertex(2).getIndex());

			}

			indexVertexArray = new TriangleIndexVertexArray(triNum,
					functions.getIndexBuffer(gIndices), 4 * 3, vertNum / 3,
					functions.getVertexBuffer(gVertices), 4 * 3);
			// System.out.println("size" +
			// indexVertexArray.getIndexedMeshArray().size());
			indexedMesh = indexVertexArray.getIndexedMeshArray().get(0);

		} else {
			return null;
		}

		return indexedMesh;
	}

	/**
	 * 
	 * @param slicePlane (Plane to intersect)
	 * @param constrain (Should we crop to the current shape?)
	 * @param cropToSketch (What is the current shape)
	 * @return
	 * 
	 * 
	 */

	public List<List<Vec2D>> getIntersection(SlicePlane slicePlane,
			boolean constrain, SketchShape cropToSketch) {

		List<List<Vec2D>> returnList = new ArrayList<List<Vec2D>>();

		List<SketchShape> ShetchShapesIntersect = new ArrayList<SketchShape>(); //shapes to check for intersections		

		if (constrain) {
			Object object = cropToSketch;

			if (object instanceof SketchSpline) {
				SketchSpline spline = (SketchSpline) object;
				ShetchShapesIntersect.add(spline.getPath());
			}

			if (object instanceof SketchPath) {
				
				//TODO: we need to make the crop to shop the correct one on each plane. 
				SketchPath spline = (SketchPath) object;
				ShetchShapesIntersect.add(spline);
			}

		} else {
			SketchOutline outline = this.getSketch().getSketchShapes().sketchOutlines
					.getOutterOutline();

			for (int o = 0; o < this.getSketch().getSketchShapes().sketchOutlines
					.getList().size(); o++) {
				SketchPath outline2 = this.getSketch().getSketchShapes().sketchOutlines
						.getList().get(o).getPath();
				ShetchShapesIntersect.add(outline2);

			}

			//ShetchShapesIntersect.add(this.getSketch().getSketchShapes().sketchOutlines.getOutterOutline().getPath());
			//path = outline.getPath();
		}

		if (ShetchShapesIntersect.size() == 0)
			return null;

		for (int sh = 0; sh < ShetchShapesIntersect.size(); sh++) {
			SketchPath path = (SketchPath) ShetchShapesIntersect.get(sh);

			for (int i = 1; i <= path.size(); i++) {
				Vec3D p1 = null;
				Vec3D p2 = null;

				if (i != 0) {
					p1 = this.getWorldPos((Vec2D) path.get(i - 1));
				} else {
					p1 = this.getWorldPos((Vec2D) path.get(path.size() - 1));
				}

				if (i != path.size()) {
					p2 = this.getWorldPos((Vec2D) path.get(i));
				} else {
					p2 = this.getWorldPos((Vec2D) path.get(0));
				}
				// 

				// if one point is on one side of the plane and the other point is
				// on the other side !

				if (slicePlane.getPlane().classifyPoint(p1) != slicePlane
						.getPlane().classifyPoint(p2)) {
					//we found a intersect
					SketchPoint vec2D1 = null;

					if (i != 0) {
						vec2D1 = (SketchPoint) path.get(i - 1);
					} else {
						vec2D1 = (SketchPoint) path.get(path.size() - 1);
					}

					SketchPoint vec2D2 = null;
					if (i < path.size()) {
						vec2D2 = (SketchPoint) path.get(i);
					} else {
						vec2D2 = (SketchPoint) path.get(0);
					}

					if (vec2D1 == null || vec2D2 == null)
						break;

					if (vec2D1.containsBezier() || vec2D2.containsBezier()) {
						for (float t = 0; t < 1 - GLOBAL.SketchGlobals.BEZIER_DETAIL_CALCULATIONS; t += GLOBAL.SketchGlobals.BEZIER_DETAIL_CALCULATIONS) {
							Vec2D bez1 = vec2D1;
							Vec2D bez2 = vec2D2;

							if (vec2D1.controlPoint2 != null) {
								bez1 = vec2D1.controlPoint2;

							}

							if (vec2D2.controlPoint1 != null) {
								bez2 = vec2D2.controlPoint1;

							}

							float x = GLOBAL.g.bezierPoint(vec2D1.x, bez1.x,
									bez2.x, vec2D2.x, t);
							float y = GLOBAL.g.bezierPoint(vec2D1.y, bez1.y,
									bez2.y, vec2D2.y, t);

							float x2 = GLOBAL.g
									.bezierPoint(
											vec2D1.x,
											bez1.x,
											bez2.x,
											vec2D2.x,
											t
													+ (GLOBAL.SketchGlobals.BEZIER_DETAIL_CALCULATIONS));
							float y2 = GLOBAL.g
									.bezierPoint(
											vec2D1.y,
											bez1.y,
											bez2.y,
											vec2D2.y,
											t
													+ (GLOBAL.SketchGlobals.BEZIER_DETAIL_CALCULATIONS));

							Vec2D curveP1 = new Vec2D(x, y);
							Vec2D curveP2 = new Vec2D(x2, y2);

							Vec3D p1b = this.getWorldPos(curveP1);
							Vec3D p2b = this.getWorldPos(curveP2);

							if (slicePlane.getPlane().classifyPoint(p1b) != slicePlane
									.getPlane().classifyPoint(p2b)) {

								Vec3D intersect = null;
								intersect = (Vec3D) slicePlane.getPlane()
										.getIntersectionWithRay(
												new Ray3D(p2b, p1b.sub(p2b)));
								if (intersect == null)
									intersect = (Vec3D) slicePlane
											.getPlane()
											.getIntersectionWithRay(
													new Ray3D(p1b, p2b.sub(p1b)));

								Vec2D intexsecX = new Vec2D(intersect.x,
										intersect.y);

								if (intersect != null) {

									intersect.subSelf(slicePlane.getPlane());

									intersect
											.rotateY((float) (functions.angleOf(new Vec2D(
													slicePlane.getPlane().normal.x,
													slicePlane.getPlane().normal.z)) - (Math.PI / 2)));

									float rotateAn = (functions
											.angleOf(new Vec2D(
													slicePlane.getPlane().normal.x,
													slicePlane.getPlane().normal.y)));

									if (rotateAn < Math.PI / 2
											|| rotateAn > (Math.PI / 2) * 3)
										rotateAn = (float) ((Math.PI * 2) - Math
												.abs(rotateAn));

									intersect.rotateX(rotateAn);

									List<Vec2D> listItem = new ArrayList<Vec2D>();
									listItem.add(new Vec2D(intersect.x,
											intersect.y));
									listItem.add(intexsecX);
									returnList.add(listItem);
									//t = 2;
								}
							}

						}

					} else {

						Vec3D intersect = null;
						intersect = (Vec3D) slicePlane.getPlane()
								.getIntersectionWithRay(
										new Ray3D(p2, p1.sub(p2)));
						if (intersect == null)
							intersect = (Vec3D) slicePlane.getPlane()
									.getIntersectionWithRay(
											new Ray3D(p1, p2.sub(p1)));

						if (intersect != null) {
							Vec2D intexsecX = new Vec2D(intersect.x,
									intersect.y);

							intersect.subSelf(slicePlane.getPlane());

							intersect
									.rotateY((float) (functions.angleOf(new Vec2D(
											slicePlane.getPlane().normal.x,
											slicePlane.getPlane().normal.z)) - (Math.PI / 2)));

							float rotateAn = (functions.angleOf(new Vec2D(
									slicePlane.getPlane().normal.x, slicePlane
											.getPlane().normal.y)));

							if (rotateAn < Math.PI / 2
									|| rotateAn > (Math.PI / 2) * 3)
								rotateAn = (float) ((Math.PI * 2) - Math
										.abs(rotateAn));

							intersect.rotateX(rotateAn);

							List<Vec2D> listItem = new ArrayList<Vec2D>();
							listItem.add(new Vec2D(intersect.x, intersect.y));
							listItem.add(intexsecX);

							returnList.add(listItem);
						}
					}
				}

			}
		}
		return returnList;

	}

	/**
	 * getIntersectionCentre
	 * @param slicePlane
	 * @param spline
	 * @param externalCurvePercent
	 * @param percentReturn
	 * @param intersectXReturn
	 * Return information about a intersection between a slicePlane and a spline
	 */
	public void getIntersectionCentre(SlicePlane slicePlane,
			SketchSpline spline, float externalCurvePercent,
			Vec2D percentReturn, Vec2D intersectXReturn) {

		//LOGGER.info("");
		//LOGGER.info("");

		float neartestDist = -1;
		Vec3D returnVec3D = null;
		int collisionsFound = 0;

		float lenToNext = 0;

		//return if spline is too short
		if (spline.getCentrePath().size() < 2)
			return;

		spline.getCentrePath().cacheLength(true);

		Vec3D p1 = null, p2 = null;
		Vec2D p12D = null, p22D = null;

		for (int i = 0; i < spline.getCentrePath().size() - 1; i++) {

			SketchPoint point1 = spline.getCentrePath().get(i);
			SketchPoint point2 = spline.getCentrePath().get(i + 1);

			float step = 1;

			if (point1.containsBezier() || point2.containsBezier())
				step = GLOBAL.SketchGlobals.BEZIER_DETAIL_CALCULATIONS;

			float offset = 0;

			if (step != 1)
				offset = step;

			p22D = (Vec2D) point1.copy();
			spline.getCentrePath().resetPosStep();

			for (float i2 = 0; i2 < 1; i2 += step) {

				//If we are on a curve step though the curve
				if (step == 1) {
					p12D = (Vec2D) spline.getCentrePath().get(i);
					p22D = (Vec2D) spline.getCentrePath().get(i + 1);
					p1 = this.getWorldPos(p12D);
					p2 = this.getWorldPos(p22D);

				} else {
					p12D = p22D.copy();//get the previous point// (Vec2D) spline.getCentrePath().getPosStep(step);
					Vec2D p22dTemp = (Vec2D) spline.getCentrePath().getPosStep(
							point1, point2, step);//step to the next point

					if (p22dTemp != null) {
						p22D = p22dTemp;
					}
					p1 = this.getWorldPos(p12D);
					p2 = this.getWorldPos(p22D);
					// LOGGER.info("p1" + p1.x + ":" + p1.y);
					// LOGGER.info("p2" + p2.x + ":" + p2.y);
				}

				if (p2 != null) {
					/*
									if(p12D.distanceTo(p22D) > 100)
										LOGGER.info("-------------------------------------------------------");
									
									LOGGER.info("p12D.distanceTo(p22D); " + p12D.distanceTo(p22D)  + " i2 " + i2);
									LOGGER.info(" p1 " + p1  + " p2 " + p2);
									LOGGER.info("");
									*/

					//if(p12D.distanceTo(p22D) < 100)
					//	

					lenToNext += p12D.distanceTo(p22D);

					// if one point is on one side of the plane and the other point is
					// on the other side !
					if ((slicePlane.getPlane().classifyPoint(p1) != slicePlane
							.getPlane().classifyPoint(p2))) {
						//LOGGER.info("i2 on p"+i2 );

						Vec3D intersect = null;

						//try intersecting  ray in one direction
						intersect = (Vec3D) slicePlane.getPlane()
								.getIntersectionWithRay(
										new Ray3D(p1, p2.sub(p1)));

						//if we do not find a point try the other direction
						if (intersect == null)
							intersect = (Vec3D) slicePlane.getPlane()
									.getIntersectionWithRay(
											new Ray3D(p2, p1.sub(p2)));

						//if we do not find a point try the other direction
						if (intersect == null)
							intersect = (Vec3D) slicePlane.getPlane()
									.getIntersectionWithRay(
											new Ray3D(p2.sub(p1.sub(p2)), p1
													.sub(p2)));

						//if we do not find a point try the other direction
						if (intersect == null)
							intersect = (Vec3D) slicePlane.getPlane()
									.getIntersectionWithRay(
											new Ray3D(p2.add(p1.sub(p2)), p1
													.sub(p2)));

						collisionsFound++;
						//we found a intersection with the plane
						if (intersect != null) {

							float distToP1 = intersect.distanceTo(p1); //dist to intersection
							float distToP2 = intersect.distanceTo(p2); //dist to intersection
							float distBetweenP1P2 = p1.distanceTo(p2);
							float percentBetween = distToP1 / distBetweenP1P2; //distBetweenP1P2;

							float percent = 0;

							//special case for 2 point lines
							float splneLen = (spline.getCentrePath()
									.getlength());
							percent = ((lenToNext - distToP2) / splneLen);

							// System.out.println(percentBetween + "in");

							// for (int k = 0; k < points.size(); k++) {
							// float percent = (Float) points.get(k);
							float deltaPercent = Math.abs(percent
									- externalCurvePercent);
							/*
							LOGGER.info("distToP1 " + distToP1 +
									" distToP2 "+distToP2 +
									" lenToNext " + lenToNext + 
									" percentBetween " + percentBetween + 
									" percent" + percent + 
									" splneLen " + splneLen + 
									" i " + i + " i2 " + i2 + 
									" externalCurvePercent "+externalCurvePercent + 
									" deltaPercent "+ deltaPercent +
									" neartestDist " + neartestDist);

							*/
							if (deltaPercent < neartestDist
									|| neartestDist == -1) {
								//	if (	collisionsFound == 3){

								neartestDist = deltaPercent;

								percentReturn.x = (p12D.x * (1 - percentBetween))
										+ (p22D.x * (percentBetween));
								percentReturn.y = (p12D.y * (1 - percentBetween))
										+ (p22D.y * (percentBetween));
								returnVec3D = intersect.copy();

								//if(neartestDist > .2f)
								//	returnVec3D = null;
								// System.out.println(percent +" P");
								//LOGGER.info("percent "+percentBetween);
							}

							// }

							// returnList.add(new Float(percent));

						}
						// System.out.println(p1);
						// System.out.println(p2);
					}

				}
			}
			//.info("returnVec3D " + returnVec3D);
		}
		if (returnVec3D != null) {

	
			returnVec3D.subSelf(slicePlane.getPlane());
			returnVec3D
					.rotateY((float) (functions.angleOf(new Vec2D(slicePlane
							.getPlane().normal.x,
							slicePlane.getPlane().normal.z)) - (Math.PI / 2)));

			float rotateAn = (functions.angleOf(new Vec2D(
					slicePlane.getPlane().normal.x,
					slicePlane.getPlane().normal.y)));

			if (rotateAn < Math.PI / 2 || rotateAn > (Math.PI / 2) * 3){
				rotateAn = (float) ((Math.PI * 2) - Math.abs(rotateAn));
			}
			//LOGGER.info("GRAVITY");	

			returnVec3D.rotateX(rotateAn);
			//LOGGER.info("slicePlane.getPlane().normal.x " +slicePlane.getPlane().normal.x + "slicePlane.getPlane().normal.y " +slicePlane.getPlane().normal.y);


			// intersectXReturn = new Vec2D(returnVec3D.x,returnVec3D.y);
			intersectXReturn.x = returnVec3D.x;
			intersectXReturn.y = returnVec3D.y;
		}else{
			
		}

		spline.getCentrePath().cacheLength(false);

	}

	public SketchShape getLastSketch() {
		return this.getSketch().getLast();
	}

	public SketchPoint getOverSelectPoint(float x, float y) {
		return this.getSketch().getOverSelectPoint(x, y);
	}

	public List<SketchShape> getOverShape(float x, float y) {
		return this.getSketch().getOverShape(x, y);

	}

	/**
	 * @return the plane
	 */
	public Plane getPlane() {
		return plane;
	}

	/**
	 * @return the Sketch
	 */
	public Sketch getSketch() {
		return sketch;
	}

	public SketchShape getSketchShapeById(int linkedSketchId) {
		return this.getSketch().getSketchShapeById(linkedSketchId);
	}

	public spShape getspShape() {
		return this.getSketch().getspShape();
	}

	Vec3D getWorldPos(Vec2D vec) {

		if (vec == null)
			return null;

		Vec3D returnVec = new Vec3D(vec.x, vec.y, 0);

		// returnVec.rotateX(this.plane.x);
		// returnVec.rotateY(this.plane.y);
		// returnVec.rotateZ(this.plane.z);

		returnVec.addSelf(this.getPlane());
		return returnVec;

	}

	public Vec3D getWorldPosIntersect(Vec3D vec) {

		vec.subSelf(this.getPlane());

		vec.rotateY((float) (functions.angleOf(new Vec2D(getPlane().normal.x,
				this.getPlane().normal.z)) - (Math.PI / 2)));

		float rotateAn = (functions.angleOf(new Vec2D(this.getPlane().normal.x,
				this.getPlane().normal.y)));

		if (rotateAn < Math.PI / 2 || rotateAn > (Math.PI / 2) * 3)
			rotateAn = (float) ((Math.PI * 2) - Math.abs(rotateAn));

		vec.rotateX(rotateAn);

		return vec;

	}

	public boolean intersects(SlicePlane otherSlice) {

		Vec2D pminYThis = new Vec2D(0, this.profileMinY);
		Vec2D pmaxYThis = new Vec2D(0, this.profileMaxY);

		float pangleThis = functions.angleOf(new Vec2D(
				this.getPlane().normal.x, this.getPlane().normal.y));
		Vec2D pposThis = new Vec2D(this.getPlane().x, this.getPlane().y);

		pminYThis.rotate(pangleThis);
		pmaxYThis.rotate(pangleThis);

		pminYThis.addSelf(pposThis);
		pmaxYThis.addSelf(pposThis);

		Vec2D pminYThat = new Vec2D(0, otherSlice.profileMinY);
		Vec2D pmaxYThat = new Vec2D(0, otherSlice.profileMaxY);

		float pangleThat = functions
				.angleOf(new Vec2D(otherSlice.getPlane().normal.x, otherSlice
						.getPlane().normal.y));
		Vec2D pposThat = new Vec2D(otherSlice.getPlane().x,
				otherSlice.getPlane().y);

		pminYThat.rotate(pangleThat);
		pmaxYThat.rotate(pangleThat);

		pminYThat.addSelf(pposThat);
		pmaxYThat.addSelf(pposThat);

		if (functions
				.intersect(pminYThis.x, pminYThis.y, pmaxYThis.x, pmaxYThis.y,
						pminYThat.x, pminYThat.y, pmaxYThat.x, pmaxYThat.y) == functions.DO_INTERSECT) {
			return true;
		}

		return false;
	}

	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}

	public boolean lastSketchOverlaps() {
		return this.getSketch().lastSketchOverlaps();
	}

	public IndexedMesh loftCollisonMeshBetween(SketchPath coverPath,
			SlicePlane planeOther, SketchPath coverPathOther, float stepRes,
			Vec3D offset) {
		IndexedMesh vertexArray = new IndexedMesh();

		if (coverPath == null || coverPath.size() < 2 || coverPathOther == null
				|| coverPathOther.size() < 2) {
			//System.out.println("cover wrong");
			return null;
		}

		Vec3D prevHere = null;
		Vec3D prevThere = null;

		int vertNum = ((int) (1 / stepRes) * 3 * 2);
		int indNum = (int) (1 / stepRes) * 6;

		float[] gVertices = new float[vertNum];
		int[] gIndices = new int[indNum];

		int SideLen = (int) (1 / stepRes) - 1;
		int trigCount = 0;
		int step = 0;
		for (float i = 0; i <= 1 - stepRes; i += stepRes) {

			Vec2D pHere2D = coverPath.getPos(i);

			Vec3D pHere = new Vec3D(pHere2D.x, pHere2D.y, this.getPlane().z);

			Vec2D pThere2D = coverPathOther.getPos(i);
			Vec3D pThere = new Vec3D(pHere2D.x, pHere2D.y,
					planeOther.getPlane().z);

			if (pHere != null && pThere != null) {
				gVertices[((3 * step) + 0)] = GLOBAL.jBullet.scaleVal(pHere.x)
						- offset.x;
				gVertices[((3 * step) + 1)] = GLOBAL.jBullet.scaleVal(-pHere.y)
						+ offset.y;
				gVertices[((3 * step) + 2)] = GLOBAL.jBullet.scaleVal(-pHere.z)
						- offset.z;

				gVertices[((3 * step) + 0) + (SideLen * 3)] = GLOBAL.jBullet
						.scaleVal(pThere.x) - offset.x;
				gVertices[((3 * step) + 1) + (SideLen * 3)] = GLOBAL.jBullet
						.scaleVal(-pThere.y) + offset.y;
				gVertices[((3 * step) + 2) + (SideLen * 3)] = GLOBAL.jBullet
						.scaleVal(-pThere.z) - offset.z;

				// System.out.println("here" + pHere);
				// System.out.println("there" + pThere);
				//
				//	

				if (prevHere != null && prevThere != null) {

					gIndices[((step * 6) + 0)] = (((step - 1)) + 0);
					gIndices[((step * 6) + 1)] = (((SideLen + step - 1)) + 0);
					gIndices[((step * 6) + 2)] = ((step) + 0);

					gIndices[((step * 6) + 3)] = ((step) + 0);
					gIndices[((step * 6) + 4)] = (((SideLen + step - 1)) + 0);
					gIndices[((step * 6) + 5)] = (((SideLen + step)) + 0);
					trigCount++;
					trigCount++;
				}

				prevHere = pHere;
				prevThere = pThere;
				step++;
			} else {
				// System.out.println("NULL");
			}
		}

		// System.out.println("start");
		// for(float i : gVertices){
		// System.out.println(i);
		// }
		// System.out.println("end");
		// System.out.println("steps" + step + " : " + vertNum/6);
		//	
		indexVertexArray = new TriangleIndexVertexArray(trigCount,
				functions.getIndexBuffer(gIndices), 4 * 3, step / 3,
				functions.getVertexBuffer(gVertices), 4 * 3);
		//System.out
		//		.println(indexVertexArray.getIndexedMeshArray().get(0).numVertices);
		indexedMesh = indexVertexArray.getIndexedMeshArray().get(0);

		//	System.out.println("made");

		return indexedMesh;

	}

	/*
	public void loftFelt(SlicePlane nextSlice, FeltCover felt) {

		SketchPath thisPath = this.getSketch().getFirst().getPath();
		SketchPath thatPath = this.getSketch().getFirst().getPath();

		for (int i = 0; i <= 1 - SETTINGS_SKETCH.feltResolution; i += SETTINGS_SKETCH.feltResolution) {
			// Vec3D thisPathp1 = thisPathp1.g
		}
	}
*/
	
	public void mouseDragged(float mouseX, float mouseY) {
		Vec2D v = GLOBAL.uiTools.getPointOnPlane(new Vec2D(mouseX, mouseY),
				this.getPlane());
		this.getSketch().mouseDragged(v.x, v.y);
		if (SETTINGS.DEBUG) {
			debugMousePoint = v.copy();
		}
	}

	public void mousePressed(float mouseX, float mouseY) {
		Vec2D v = GLOBAL.uiTools.getPointOnPlane(new Vec2D(mouseX, mouseY),
				this.getPlane());
		this.getSketch().mousePressed(v.x, v.y);
	}

	public void mouseReleased(float mouseX, float mouseY) {
		Vec2D v = GLOBAL.uiTools.getPointOnPlane(new Vec2D(mouseX, mouseY),
				this.getPlane());
		this.getSketch().mouseReleased(v.x, v.y);
	}
	
	
	public void mouseDoubleClick(int mouseX, int mouseY) {
		Vec2D v = GLOBAL.uiTools.getPointOnPlane(new Vec2D(mouseX, mouseY),
				this.getPlane());
		this.getSketch().mouseDoubleClick(v.x, v.y);		
	}

	public boolean overSelectPoint(float mouseX, float mouseY) {

		if (this.getSketch().overSelectPoint(mouseX, mouseY))
			return true;
		else
			return false;

	}

	void recordCollision() {

	}

	public void removeVertex(SketchPoint v) {
		this.getSketch().removeVertex(v);
	}

	public void render(PGraphics g) {



		g.pushMatrix();

		this.applyRotationMatrix(g);

		//render debug point
		if (SETTINGS.DEBUG && debugMousePoint != null) {
			g.noFill();
			g.stroke(255,0,0);
			g.ellipse(debugMousePoint.x, debugMousePoint.y, 30, 30);
		}


		
		//optimize so that we do not draw the side view unless we need to.
		//currently is only relative to camera rotation, if a model rotates side view will not be drawn
		g.bezierDetail(SETTINGS_SKETCH.BEZIER_DETAIL_EDIT);
		if (this.getPlane().normal.z == -1) {
			this.getSketch().render(g);
		} else {
		g.bezierDetail(SETTINGS_SKETCH.BEZIER_DETAIL_3D_PREVIEW);

		if(getSketch().getRenderMode() == Sketch.RENDER_3D_DIAGRAM)
			g.bezierDetail(SETTINGS_SKETCH.BEZIER_DETAIL_3D_DIAGRAM);
			
			if (GLOBAL.rotateModelsX == 0 && GLOBAL.rotateModelsY == 0
					&& !GLOBAL.screenshot && this.getSketch().getRenderMode() != Sketch.RENDER_3D_DIAGRAM
					&& this.getSketch().getRenderMode() != Sketch.RENDER_3D_PREVIW) {
				this.getSketch().renderSide(g);
			} else{
				this.getSketch().render(g);
			}
		}

		if (SETTINGS.DEBUG) {
			g.stroke(255, 0, 0);
			for (int i = 0; i < this.debugIntersectionPoints.size(); i++) {
				Vec2D p = this.debugIntersectionPoints.get(i);
				float crossWidth = 20;
				g.line(p.x - (crossWidth / 2), p.y + (crossWidth / 2), p.x
						+ (crossWidth / 2), p.y - (crossWidth / 2));
				g.line(p.x + (crossWidth / 2), p.y + (crossWidth / 2), p.x
						- (crossWidth / 2), p.y - (crossWidth / 2));
				//g.ellipse(p.x,p.y,0,20);
			}

			g.stroke(0, 0, 255);
			for (int i = 0; i < this.debugIntersectionPointsTop.size(); i++) {
				Vec2D p = this.debugIntersectionPointsTop.get(i);
				float tWidth = 20;
				float hover = 20;
				if (p != null) {
					g.line(p.x, p.y - hover, p.x, p.y - (tWidth + hover));
					g.line(p.x - (tWidth / 2), p.y - (tWidth + hover), p.x
							+ (tWidth / 2), p.y - (tWidth + hover));
				}
			}

			g.stroke(0, 0, 255);
			for (int i = 0; i < this.debugIntersectionPointsBottom.size(); i++) {
				Vec2D p = this.debugIntersectionPointsBottom.get(i);
				float bHeight = 20;
				float bWidth = 10;
				float hover = 20;
				if (p != null) {
					g.line(p.x - (bWidth / 2), p.y + hover, p.x - (bWidth / 2),
							p.y + (bHeight + hover));
					g.ellipse(p.x, p.y + (bHeight + hover - bWidth / 2),
							bWidth, bWidth);
				}
			}

			if (debugIntersetStart != null) {
				g.stroke(0, 255, 0);
				g.ellipse(debugIntersetStart.x, debugIntersetStart.y, 40, 40);
			}

		}

		g.popMatrix();
	}
	
	
	
	
	public void renderPickBuffer(PGraphics pickBuffer) {
		pickBuffer.fill(PickBuffer.getInstance().getPickColour(this));
		pickBuffer.noStroke();
		pickBuffer.pushMatrix();

		this.applyRotationMatrix(pickBuffer);

	
		pickBuffer.bezierDetail(SETTINGS_SKETCH.BEZIER_DETAIL_3D_PREVIEW);
		this.getSketch().renderPickBuffer(pickBuffer);
		pickBuffer.popMatrix();

	}
	
	
	

	public void renderSilhouette(PGraphics g) {
		
		g.pushMatrix();
		this.applyRotationMatrix(g);
		g.fill(39, 35, 36);
		g.noStroke();
		this.getSketch().renderSilhouette(g);
		g.popMatrix();
	}

	/*
	 * float[] gVertices = new float[vertNum]; for(int i = 0; i <
	 * triangulate.vertexs.size(); i++){ Delaunay.Vertex v =
	 * (Delaunay.Vertex)triangulate.vertexs.get(i);
	 * //System.out.println(this.offset.z); gVertices[(3*v.getIndex())+0] =
	 * GLOBAL.jBullet.scaleVal((float)v.x) - offsetX; gVertices[(3*v.getIndex())+1]
	 * = GLOBAL.jBullet.scaleVal((float)-v.y)+ offsetY;
	 * gVertices[(3*v.getIndex())+2] = GLOBAL.jBullet.scaleVal(this.plane.z) -
	 * offsetZ;
	 * 
	 * // System.out.println((int)(3*v.getIndex()) + "  x:" +
	 * gVertices[(3*v.getIndex())+0]+ "y:" + gVertices[(3*v.getIndex())+1]+ "z:" +
	 * gVertices[(3*v.getIndex())+2] );
	 * 
	 * }
	 * 
	 * 
	 * 
	 * int indNum = (triangulate.triangles.size()*3); int triNum =
	 * triangulate.triangles.size(); int indDiffNum =
	 * (triangulate.triangles.size()); int[] gIndices = new int[indNum];
	 * 
	 * for(int i = 0; i < triangulate.triangles.size(); i++){ Delaunay.Triangle t =
	 * (Delaunay.Triangle)triangulate.triangles.get(i); gIndices[(i*3)+0] =
	 * (t.get_vertex(0).getIndex()); gIndices[(i*3)+1] =
	 * (t.get_vertex(1).getIndex()); gIndices[(i*3)+2] =
	 * (t.get_vertex(2).getIndex());
	 * 
	 * }
	 * 
	 * 
	 * indexVertexArray = new TriangleIndexVertexArray(triNum, functions
	 * .getIndexBuffer(gIndices), 4 * 3, vertNum/3, functions
	 * .getVertexBuffer(gVertices), 4 * 3);
	 */
	void resetRecordedCollisions() {

	}

	public void scale(float scale, Vec3D centre) {
		// scale z dir
		this.getPlane().z += ((this.getPlane().z - centre.z) * scale);
		this.getSketch().scale(scale, centre);
	}

	public void select() {
		this.setSelected(true);
		this.getSketch().select();
		this.getSketch().setLayerSelected(true);
	}

	public void selectNodes(int mouseX, int mouseY) {
		if (this.getPlane() != null && this.getSketch() != null) {
			Vec2D transMouse = GLOBAL.uiTools.getPointOnPlane(new Vec2D(mouseX,
					mouseY), this.getPlane());

			if (transMouse != null)
				this.getSketch().selectNodes(transMouse.x, transMouse.y);
		}
	}

	public void setBrushCap(int cap) {
		this.getSketch().setBrushCap(cap);
	}

	public void setBrushDia(float val) {
		this.getSketch().setBrushDia(val);
	}

	/**
	 * @param plane the plane to set
	 */
	public void setPlane(Plane plane) {
		this.plane = plane;
	}

	public void setRenderMode(int mode) {
		this.getSketch().setRenderMode(mode);
	}

	/**
	 * @param selected the selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * @param Sketch2 the Sketch to set
	 */
	public void setSketch(Sketch s) {
		this.sketch = s;
	}

	public void toggleUnion() {
		this.getSketch().toggleUnion();
	}

	public nu.xom.Element toXML() {
		Element element = new Element("SlicePlane");

		element.addAttribute(new Attribute("id", String.valueOf(this.getId())));

		
		if (this.guide)
			element.addAttribute(new Attribute("guide", String
					.valueOf(this.guide)));

		Element plane = new Element("Plane");
		

		plane.addAttribute(new Attribute("x", String.valueOf(this.getPlane().x)));
		plane.addAttribute(new Attribute("y", String.valueOf(this.getPlane().y)));
		plane.addAttribute(new Attribute("z", String.valueOf(this.getPlane().z)));

		plane.addAttribute(new Attribute("nx",
				String.valueOf(this.getPlane().normal.x)));
		plane.addAttribute(new Attribute("ny",
				String.valueOf(this.getPlane().normal.y)));
		plane.addAttribute(new Attribute("nz",
				String.valueOf(this.getPlane().normal.z)));

		element.appendChild(plane);
		element.appendChild(getSketch().toXML());

		return element;
	}

	public void unselect() {
		this.setSelected(false);
		this.getSketch().unselect();
		this.getSketch().setLayerSelected(false);
		
		
		if(this.getCrossSliceSelection() != null)
			this.getCrossSliceSelection().selected = false;
		
	}

	public void update() {
		if(thickness <= 0)
			thickness = .1f;
		
		this.getSketch().update();
		this.generate();
		this.getSketch().getSlots().update();
		if (this.constraintSlot != null) {

		}
		//this.Sketch.buildOutline();
	}

	public void buildOutline(boolean includeSlot, boolean addToPath) {
		this.getSketch().buildOutline(includeSlot, addToPath);
	}

	public void importSVG(String path) {
		this.getSketch().importSVG(path);
	}

	public void setEditable(boolean editable) {
		this.getSketch().setEditable(editable);
		}

	/**
	 * @return the crossSliceSelection
	 */
	public CrossSliceSelection getCrossSliceSelection() {
		return crossSliceSelection;
	}

	/**
	 * @param crossSliceSelection the crossSliceSelection to set
	 */
	public void setCrossSliceSelection(CrossSliceSelection crossSliceSelection) {
		this.crossSliceSelection = crossSliceSelection;
		crossSliceSelection.planes.add(this);
	}

	public void setRender3D(boolean b) {
		this.getSketch().setRender3D(b);		
	}

	public void unselectShapes() {
		this.getSketch().unselectShapes();		
	}



	
}
