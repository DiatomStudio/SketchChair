/**
 *                 SlicePlanes
 *                 
 *                 ___________________
 *                /                  /_
 *               /                  / /_
 *              /                  / / /_
 *             /                  / / / /
 *            /                  / / / /
 *           /__________________/ / / /
 *            /__________________/ / /
 *             /__________________/ /
 *              /__________________/
 */

package cc.sketchchair.geometry;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.pdf.PGraphicsPDF;
import toxi.geom.Plane;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.PickBuffer;
import cc.sketchchair.sketch.SETTINGS_SKETCH;
import cc.sketchchair.sketch.Sketch;
import cc.sketchchair.sketch.SketchPath;
import cc.sketchchair.sketch.SketchPoint;
import cc.sketchchair.sketch.SketchShape;
import cc.sketchchair.sketch.SketchShapes;
import cc.sketchchair.sketch.SketchSpline;

import com.bulletphysics.linearmath.Transform;

/**
 * A container class for Sliceplanes.
 * @author gregsaul
 *
 */
public class SlicePlanes {

	SlicePlane selectedSlicePlane;
	int selectedSlicePlaneNum = 0;

	private List<SlicePlane> l = new ArrayList<SlicePlane>();
	public float depthZ;

	public SlicePlanes() {
		// TODO Auto-generated constructor stub
	}

	public SlicePlanes(Element element) {
		//wrong type
		if (!element.getLocalName().equals("SlicePlanes"))
			return;

		for (int i = 0; i < element.getChildCount(); i++) {
			Element child = (Element) element.getChild(i);

			if (child != null && child.getLocalName().equals("SlicePlane")) {
				SlicePlane plane = new SlicePlane(child);
				if (plane != null)
					this.add(plane);
			}

		}

	}

	public void add(int index, SlicePlane copyPlane) {
		getList().add(index, copyPlane);
	}

	public void add(SlicePlane plane) {
		this.getList().add(plane);

	}

	public void add(SlicePlanes slicePlanesIn) {
		for (int i = 0; i < slicePlanesIn.getList().size(); i++) {
			SlicePlane curSlice = slicePlanesIn.getList().get(i);
			this.getList().add(curSlice);
		}
	}
	
	/*
	void addFelt(FeltCover felt) {
		for (int i = 0; i < this.getList().size() - 1; i++) {
			SlicePlane curSlice = this.getList().get(i);
			SlicePlane nextSlice = this.getList().get(i + 1);
			curSlice.loftFelt(nextSlice, felt);
		}
	}
	*/

	public void addNewSketchShape() {

		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			SketchSpline newSketch = new SketchSpline(curSlice.getSketch());
			curSlice.getSketch().add(newSketch);
			curSlice.getSketch().setCurrentShape(newSketch);
		}
	}

	public void addSketchVec2D(float mouseX, float mouseY) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.getSketch().getCurrentShape()
					.add(new SketchPoint(mouseX, mouseY));
		}
	}

	public void build() {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curPlane = this.getList().get(i);
			curPlane.build();
		}
	}

	public void buildCurrentSketch() {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);

			if (curSlice.getSketch().getCurrentShape() != null) {
				// curSlice.sketchShapes.getCurrentShape().optimize();
				// curSlice.sketchShapes.getCurrentShape().offset();
				//

				if (curSlice.getSketch().getCurrentShape() != null)
					curSlice.getSketch()
							.getCurrentShape()
							.mouseReleased(GLOBAL.uiTools.mouseX,
									GLOBAL.uiTools.mouseY);
			}

			curSlice.getSketch().buildOutline();

		}

	}

	public boolean checkForCollisions() {

		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.checkForCollisions();

		}

		return false;
	}

	public void clear() {
		this.getList().clear();
	}

	public SlicePlanes copy() {
		SlicePlanes newSlicePlanes = new SlicePlanes();
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curPlane = this.getList().get(i);
			newSlicePlanes.add(curPlane.copy());
		}
		return newSlicePlanes;
	}

	public int count() {
		return this.getList().size();
	}

	public int countNumberOfShapes() {
		int val = 0;
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			val += curSlice.getSketch().numerOfShapes();

		}
		return val;
	}

	public int countSelectedNodes() {
		int count = 0;
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			count += curSlice.getSketch().countSelectedNodes();

		}
		return count;
	}

	public void deleteSelectedShapes() {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.getSketch().deleteSelectedShapes();

		}
	}

	public void destroyPlanes() {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.destroy = true;
		}
	}

	public void empty() {
		this.setList(new ArrayList<SlicePlane>());
	}

	public void flipHorizontal(Vec3D centre) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.flipHorizontal(centre);
		}
	}

	public SlicePlane get(int i) {
		return getList().get(i);
	}

	public float getArea() {
		float area = 0;
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			area += curSlice.getArea();
		}
		return area;
	}

	public Vec3D getCentre() {
		Vec3D centre = new Vec3D();

		for (SlicePlane plane : this.getList()) {
			centre.addSelf(plane.getPlane());
		}
		centre.scaleSelf(1 / this.getList().size());

		return centre;
	}

	public SlicePlane getFirst() {
		if (this.getList().size() > 0)
			return this.getList().get(0);
		else
			return null;
	}

	public SlicePlane getLast() {
		if (this.getList().size() > 0)
			return this.getList().get(this.getList().size() - 1);
		else
			return null;
	}

	/**
	 * @return the l
	 */
	public List<SlicePlane> getList() {
		return l;
	}

	public float getMaxX() {

		float maxX = -1;
		for (SlicePlane plane : this.getList()) {
			float returnVal = plane.getSketch().getMaxX();
			
			if (returnVal != -1 && (maxX == -1 || returnVal > maxX))
				maxX = returnVal;
		}
		return maxX;
	   }

	public float getMaxXWorldSpace(Transform currentWorldTransform) {
		float maxX = -1;
		for (SlicePlane plane : this.getList()) {
			float returnVal = plane.getSketch().getMaxXWorldSpace(
					currentWorldTransform);
			
			if (returnVal != -1 && (maxX == -1 ||returnVal > maxX ))
				maxX = returnVal;
		}
		return maxX;
	}

	public float getMaxY() {

		float maxY = -1;
		for (SlicePlane plane : this.getList()) {
			float returnVal = plane.getSketch().getMaxY() ;
			if (returnVal != -1 && (maxY == -1 || returnVal > maxY))
				maxY = returnVal;
		}
		return maxY;
	}

	public float getMaxYWorldSpace(Transform currentWorldTransform) {
		float maxY = -1;
		for (SlicePlane plane : this.getList()) {
			float returnVal = plane.getSketch().getMaxYWorldSpace(
					currentWorldTransform);
			if (returnVal != -1 && (maxY == -1 || returnVal > maxY))
				maxY = returnVal;
		}
		return maxY;
	}

	public float getMaxZ() {

		float maxZ = -1;
		for (SlicePlane plane : this.getList()) {
			float returnVal = plane.getPlane().z;
			if (returnVal != -1 && (maxZ == -1 || returnVal > maxZ))
				maxZ = plane.getPlane().z;
		}
		return maxZ;
	}

	public float getMinX() {

		float minX = -1;
		for (SlicePlane plane : this.getList()) {
			float returnVal = plane.getSketch().getMinX() ;
			if (returnVal != -1 &&(minX == -1 ||  returnVal< minX))
				minX = returnVal;
		}

		return minX;
	}
	
	public float getMaxProfileHeight() {
		float maxHeight = -1;
		for (SlicePlane plane : this.getList()) {
			float returnVal = plane.getSketch().getMinX();
			if (returnVal != -1 && (maxHeight == -1 || returnVal > maxHeight))
				maxHeight = plane.profileHeight;
		}

		return maxHeight;
	}

	public float getMinXWorldSpace(Transform currentWorldTransform) {
		float minX = -1;
		for (SlicePlane plane : this.getList()) {
			float returnVal = plane.getSketch().getMinXWorldSpace(
					currentWorldTransform);
			if (returnVal != -1 && (minX == -1
					|| returnVal < minX ))
				minX = returnVal;
		}
		return minX;
	}

	public float getMinY() {

		float minY = -1;
		for (SlicePlane plane : this.getList()) {
			float returnVal = plane.getSketch().getMinY();
			if (returnVal != -1 && (minY == -1 || returnVal< minY))
				minY = returnVal;
		}

		return minY;
	}

	public float getMinYWorldSpace(Transform currentWorldTransform) {
		float minY = -1;
		for (SlicePlane plane : this.getList()) {
			float returnVal = plane.getSketch().getMinYWorldSpace(
					currentWorldTransform) ;
			if (returnVal != -1 && (minY == -1
					|| returnVal< minY ))
				minY = returnVal;
		}
		return minY;
	}

	public float getMinZ() {

		float minZ = -1;
		for (SlicePlane plane : this.getList()) {
			float returnVal = plane.getPlane().z ;
			if (returnVal != -1 && (minZ == -1 || returnVal < minZ))
				minZ = returnVal;
		}

		return minZ;
	}

	public SketchPoint getOverSelectPoint(float x, float y) {

		SketchPoint p = null;
		SketchPoint closest = null;
		float dist = -1;

		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curPlane = this.getList().get(i);

			Vec2D pointOnPlan = GLOBAL.uiTools.getPointOnPlane(new Vec2D(x, y),
					curPlane.getPlane());

			p = curPlane.getOverSelectPoint(pointOnPlan.x, pointOnPlan.y);
			if (p != null
					&& (dist == -1 || p.distanceTo(new Vec2D(pointOnPlan.x,
							pointOnPlan.y)) < dist)) {
				closest = p;
				dist = p.distanceTo(new Vec2D(pointOnPlan.x, pointOnPlan.y));
			}

		}
		return closest;
	}

	public SketchShapes getOverShapes(float x, float y) {
		SketchShapes overShapes = new SketchShapes(null);

		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curPlane = this.getList().get(i);
			Vec2D pointOnPlan = GLOBAL.uiTools.getPointOnPlane(new Vec2D(x, y),
					curPlane.getPlane());
			overShapes.addAll(curPlane.getOverShape(pointOnPlan.x,
					pointOnPlan.y));
		}
		return overShapes;
	}

	public SlicePlane getPlaneContainingShape(SketchShape path) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			if (curSlice.getSketch().contains(path))
				return curSlice;
		}
		return null;
	}

	public SlicePlane getPlanePickBuffer(int col) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			Vec2D selectedVec = curSlice.sketchSpline.getVec2DpickBuffer(col);

			if (selectedVec != null)
				return curSlice;
		}
		return null;

	}

	public SketchShape getSelectedShape() {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			if (curSlice.getSketch().getSelectedShape() != null)
				return curSlice.getSketch().getSelectedShape();

		}
		return null;
	}

	public SlicePlane getSelectedShapePlane() {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			if (curSlice.getSketch().getSelectedShape() != null)
				return curSlice;

		}
		return null;
	}

	public SketchShape getShapePickBuffer(int col) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			SketchShape shape = curSlice.getSketch().getShapePickBuffer(col);
			if (shape != null)
				return shape;
		}
		return null;
	}

	public SketchShape getSketchShapeById(int linkedSketchId) {

		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);

			if (curSlice.getSketchShapeById(linkedSketchId) != null)
				return curSlice.getSketchShapeById(linkedSketchId);

		}
		return null;
	}

	public Vec2D getVec2DpickBuffer(int col) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);

			Vec2D selectedVec = curSlice.getSketch().getVec2DpickBuffer(col);
			if (selectedVec != null)
				return selectedVec;

		}
		return null;

	}

	public int indexOf(SlicePlane first) {
		return getList().indexOf(first);
	}

	public boolean lastSketchOverlaps() {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			if (!curSlice.lastSketchOverlaps())
				return false;

		}

		return true;

	}

	public void mouseDragged(float mouseX, float mouseY) {

		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.mouseDragged(mouseX, mouseY);

		}

		/*
		if ((GLOBAL.uiTools.getCurrentTool() == UITools.DRAW_TOOL || GLOBAL.uiTools.getCurrentTool() == UITools.LEG_TOOL)
				&& !GLOBAL.gui.overComponent()) {

			if (GLOBAL.uiTools.getCurrentTool() == UITools.LEG_TOOL) {

				for (int i = 0; i < this.getList().size(); i++) {
					SlicePlane curSlice = this.getList().get(i);
					Vec2D pointOnPlan = GLOBAL.uiTools.getPointOnPlane(
							new Vec2D(mouseX, mouseY), curSlice.getPlane());
					if (curSlice.getSketch().getLastVec() != null) {
						curSlice.getSketch().getLastVec().set(pointOnPlan.x,
								pointOnPlan.y);
						curSlice.getSketch().getCurrentShape().offset();
					}
				}

			}

			if (GLOBAL.uiTools.getCurrentTool() == UITools.DRAW_TOOL) {

				for (int i = 0; i < this.getList().size(); i++) {
					SlicePlane curSlice = this.getList().get(i);
					Vec2D pointOnPlan = GLOBAL.uiTools.getPointOnPlane(
							new Vec2D(mouseX, mouseY), curSlice.getPlane());
					this.addSketchVec2D(pointOnPlan.x, pointOnPlan.y);

				}

			}
		}

		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.mouseDragged();
		}
		*/

	}

	public void mousePressed(float mouseX, float mouseY) {
		Sketch tempSketch = new Sketch(GLOBAL.uiTools.SketchTools,
				GLOBAL.SketchGlobals);

		//THIS IS A HACK TO ADD A PLANE IF NONE EXISTS CHANGE IT!!!!!
		/*
		if (count() == 0) {
			Plane plane = new Plane(new Vec3D(0, 0, 0), new Vec3D(0, 0, -1));
			SlicePlane slicePlane = new SlicePlane(tempSketch, plane);
			tempSketch.setOnSlicePlane(slicePlane);
			// this.curSlicePlane = slicePlane;
			add(slicePlane);
		}
*/

		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.mousePressed(mouseX, mouseY);

		}
		/*
				if (GLOBAL.uiTools.getCurrentTool() == UITools.DRAW_TOOL) {

					for (int i = 0; i < this.getList().size(); i++) {
						SlicePlane curSlice = this.getList().get(i);
						SketchSpline newSketch = new SketchSpline(curSlice.getSketch(),SETTINGS_SKETCH.offsetSide);
						newSketch.setOffsetSize(GLOBAL.uiTools.brush_dia);
						curSlice.getSketch().add(newSketch);
						curSlice.getSketch().setCurrentShape(newSketch);

						GLOBAL.undo.addOperation(new UndoAction(newSketch,
								UndoAction.ADD_SHAPE));
					}

				}

				if (GLOBAL.uiTools.getCurrentTool() == UITools.LEG_TOOL) {

					for (int i = 0; i < this.getList().size(); i++) {

						SlicePlane curSlice = this.getList().get(i);

						Vec2D pointOnPlan = GLOBAL.uiTools.getPointOnPlane(new Vec2D(
								mouseX, mouseY), curSlice.getPlane());

						SketchSpline newSketch = new SketchSpline(curSlice.getSketch(),
								SketchSpline.OFFSET_BOTH);
						newSketch.setOffsetSize(GLOBAL.uiTools.brush_dia*SETTINGS_SKETCH.LEG_BRUSH_RATIO_TOP);
						newSketch.offsetSizeEnd = GLOBAL.uiTools.brush_dia*SETTINGS_SKETCH.LEG_BRUSH_RATIO_BOTTOM;
						newSketch.add(new SketchPoint(pointOnPlan.x, pointOnPlan.y));
						newSketch.add(new SketchPoint(pointOnPlan.x, pointOnPlan.y));
						//newSketch.offset();
						
						
						//SketchPoint p1 = (SketchPoint) newSketch.outineRight
						//	.get(1);
						//SketchPoint p2 = (SketchPoint) newSketch.outineLeft
						//	.get(0);
					 
						//newSketch.movePoint(p1,new Vec2D(p1.x,p1.y));
						//newSketch.movePoint(p2,new Vec2D(p2.x,p2.y));

						
						curSlice.getSketch().add(newSketch);
						curSlice.getSketch().setCurrentShape(newSketch) ;
						GLOBAL.undo.addOperation(new UndoAction(newSketch,
								UndoAction.ADD_SHAPE));
					}
				}
				*/
	}

	public void mouseReleased(float mouseX, float mouseY) {

		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.mouseReleased(mouseX, mouseY);
		}
		/*
				if ((GLOBAL.uiTools.getCurrentTool() == UITools.DRAW_TOOL
						&& !GLOBAL.gui.overComponent())) {

					for (int i = 0; i < this.getList().size(); i++) {
						SlicePlane curSlice = this.getList().get(i);

						if (curSlice.getSketch().getCurrentShape() == null
								&& curSlice.getSketch().getCurrentShape().getType() != SketchShape.TYPE_SPLINE) {
							curSlice.getSketch().getCurrentShape().optimize();
						}
					}

				}

				if ((GLOBAL.uiTools.getCurrentTool() == UITools.DRAW_PATH_TOOL)
						&& !GLOBAL.gui.overComponent()) {
					
					boolean skip = false;
					
					if(!skip && GLOBAL.uiTools.mouseButton == PConstants.LEFT && GLOBAL.uiTools.keyPressed && GLOBAL.uiTools.keyCode == PConstants.CONTROL){
						for (int i = 0; i < this.getList().size(); i++) {
							SlicePlane curSlice = this.getList().get(i);
							
							Vec2D pointOnPlane = GLOBAL.uiTools.getPointOnPlane(new Vec2D(
									mouseX, mouseY), curSlice.getPlane());
							
							SketchPoint pathVert =  curSlice.getSketch().getClosestPathVertex(pointOnPlane);

						if(pathVert != null && pointOnPlane.distanceTo(pathVert) < SETTINGS_SKETCH.select_dia){
							curSlice.removeVertex(pathVert);
							skip = true;
						}
						}
					}
					
					//check to see if we are adding a new point to an existing path
					if(!skip && GLOBAL.uiTools.mouseButton == PConstants.LEFT && GLOBAL.uiTools.keyPressed && GLOBAL.uiTools.keyCode == PConstants.CONTROL){
						for (int i = 0; i < this.getList().size(); i++) {
							SlicePlane curSlice = this.getList().get(i);

							Vec2D pointOnPlane = GLOBAL.uiTools.getPointOnPlane(new Vec2D(
									mouseX, mouseY), curSlice.getPlane());
							
							if(curSlice.addPointAlongPath(pointOnPlane.x,pointOnPlane.y))
								skip = true;
							
							
						}
						
					}
					

					if(!skip){
					for (int i = 0; i < this.getList().size(); i++) {
						SlicePlane curSlice = this.getList().get(i);

						Vec2D pointOnPlan = GLOBAL.uiTools.getPointOnPlane(new Vec2D(
								mouseX, mouseY), curSlice.getPlane());
						if(GLOBAL.uiTools.mouseButton == PConstants.RIGHT){
						
							SketchPoint pathVert =  curSlice.getClosestPathVertex(pointOnPlan);

						if(pathVert != null && pointOnPlan.distanceTo(pathVert) < SETTINGS_SKETCH.select_dia){
							curSlice.removeVertex(pathVert);
							skip = true;
						}
						}
						
						

						
						

						if (curSlice.getSketch().getCurrentShape() != null
								&& curSlice.getSketch().getCurrentShape().getType() == SketchShape.TYPE_PATH
								&& GLOBAL.uiTools.mouseButton == PConstants.LEFT
								&& !skip) {

							SketchPath sketchP = (SketchPath) curSlice.getSketch().getCurrentShape();
							if (sketchP.closed) {

								SketchPath sketch = new SketchPath(curSlice.getSketch());
								sketch.setType(SketchShape.TYPE_PATH);
								curSlice.getSketch().add(sketch);

								//sketch.add(new Vec2D(pointOnPlan.x, pointOnPlan.y));
								

							}
						}
						
						
						

						if (curSlice.getSketch().getCurrentShape() == null
								|| curSlice.getSketch().getCurrentShape().getType() != SketchShape.TYPE_PATH
								&& GLOBAL.uiTools.mouseButton == PConstants.LEFT
								&& !skip
						) {

							SketchPath sketch = new SketchPath(curSlice.getSketch());
							sketch.setType(SketchShape.TYPE_PATH);
							sketch.setOpen(true);
							curSlice.getSketch().add(sketch);

							//sketch.add(new Vec2D(pointOnPlan.x, pointOnPlan.y));

						}

						
						
						
						if (curSlice.getSketch().getCurrentShape().getType() == SketchShape.TYPE_PATH
								&& GLOBAL.uiTools.mouseButton == PConstants.LEFT
								&& !skip) {

							curSlice.getSketch().getCurrentShape().add(new SketchPoint(
									pointOnPlan.x, pointOnPlan.y));
						}
						
						
						
						

						if (curSlice.getSketch().getCurrentShape().getType() == SketchShape.TYPE_PATH
								&& GLOBAL.uiTools.mouseButton == PConstants.RIGHT
								&& !skip) {

							SketchPath sketch = (SketchPath)curSlice.getSketch().getCurrentShape();
							sketch.closed = true;
							sketch.setOpen(false);
						}
						
						

					}
					}
				}
				*/
	}
	
	
	public void mouseDoubleClick(int mouseX, int mouseY) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.mouseDoubleClick(mouseX, mouseY);
		}		
	}

	public boolean overSelectPoint(float mouseX, float mouseY) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curPlane = this.getList().get(i);

			Vec2D pointOnPlan = GLOBAL.uiTools.getPointOnPlane(new Vec2D(
					mouseX, mouseY), curPlane.getPlane());
			if (curPlane.overSelectPoint(pointOnPlan.x, pointOnPlan.y))
				return true;
		}
		return false;
	}

	public void remove(int i) {
		getList().remove(i);
	}

	public void remove(SlicePlane removePlane) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			if (curSlice == removePlane)
				this.getList().remove(i);
		}

	}

	public boolean removeCollisions() {

		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);

			for (int j = 0; j < this.getList().size(); j++) {
				SlicePlane otherSlice = this.getList().get(j);

				if (curSlice != otherSlice && !curSlice.destroy
						&& !otherSlice.destroy) {

					if (curSlice.intersects(otherSlice)) {
						if (curSlice.tiedToLeg)
							otherSlice.destroy();
						else if (otherSlice.tiedToLeg)
							curSlice.destroy();
						else
							curSlice.destroy();

					}

				}

			}

		}

		return false;
	}

	public void removeLastSketch() {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.getSketch().removeLast();
		}

	}

	public void render(PGraphics g) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.render(g);

		}

	}
	
	public void renderPickBuffer(PGraphics pickBuffer) {

		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.renderPickBuffer(pickBuffer);
		}		
	}

	
	

	public void renderSilhouette(PGraphics g) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.renderSilhouette(g);
		}
	}

	public void saveToPDF(PGraphics g) {
		PGraphicsPDF pdf = (PGraphicsPDF) g; // Get the renderer
		for (int i = 0; i < this.getList().size(); i++) {

			SlicePlane curSlice = this.getList().get(i);
			curSlice.render(g);
			pdf.nextPage();
		}

	}

	public void saveToPDF(PGraphics g, boolean translate) {

		PGraphicsPDF pdf = (PGraphicsPDF) g; // Get the renderer
		for (int i = 0; i < this.getList().size(); i++) {
			g.pushMatrix();
			g.translate(SETTINGS_SKETCH.chair_width, 20);
			SlicePlane curSlice = this.getList().get(i);
			curSlice.render(g);
			g.popMatrix();
			pdf.nextPage();
		}

	}

	public void scale(float scale, Vec3D centre) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.scale(scale, centre);
		}
	}

	void select(int i) {
		SlicePlane curSlice = this.getList().get(i);
		curSlice.select();
		// curSlice.sketchSpline.selected = true;
	}

	public void selectAll() {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.select();
		}
	}

	public SlicePlane selectNext() {

		unselectAll();
		selectedSlicePlaneNum++;

		if (selectedSlicePlaneNum > this.getList().size() - 1)
			selectedSlicePlaneNum = 0;

		SlicePlane curSlice = this.getList().get(selectedSlicePlaneNum);
		curSlice.select();
		return curSlice;
		// curSlice.sketchSpline.selected = true;
	}

	public void selectNodes(int mouseX, int mouseY) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.selectNodes(mouseX, mouseY);
		}

	}

	public SlicePlane selectPrev() {

		unselectAll();
		selectedSlicePlaneNum--;

		if (selectedSlicePlaneNum < 0)
			selectedSlicePlaneNum = this.getList().size() - 1;

		SlicePlane curSlice = this.getList().get(selectedSlicePlaneNum);
		curSlice.select();
		return curSlice;
		// curSlice.sketchSpline.selected = true;
	}

	public void setBrushCap(int cap) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curPlane = this.getList().get(i);
			curPlane.setBrushCap(cap);
		}
	}

	public void setBrushDia(float val) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curPlane = this.getList().get(i);
			curPlane.setBrushDia(val);
		}
	}

	/**
	 * @param l the l to set
	 */
	public void setList(List<SlicePlane> l) {
		this.l = l;
	}

	public void setPlaneWidth(float planeThickness) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curPlane = this.getList().get(i);
			curPlane.thickness = planeThickness;

		}
	}

	public void setRenderMode(int mode) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curPlane = this.getList().get(i);
			curPlane.setRenderMode(mode);
		}
	}

	public void setRender3D(boolean b) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curPlane = this.getList().get(i);
			curPlane.setRender3D(b);
		}		
	}

	public Vec2D setVec2DpickBuffer(int col, SketchPoint selectedVec,
			SketchShape selectedShape, SlicePlane selectedVecPlane,
			boolean isSelectedVecOnOutline) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);

			Vec2D selectedVec1 = curSlice.getSketch().setVec2DpickBuffer(col,
					selectedVec, selectedShape, selectedVecPlane,
					isSelectedVecOnOutline);
			if (selectedVec1 != null) {
				selectedVecPlane = curSlice;
				return selectedVec1;

			}

		}
		return null;
	}

	public int size() {
		return getList().size();
	}

	public void toggleGuide() {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curPlane = this.getList().get(i);
			curPlane.guide = !curPlane.guide;
		}
	}

	public void toggleUnion() {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curPlane = this.getList().get(i);
			curPlane.toggleUnion();
		}
	}

	public Element toXML() {

		Element element = new Element("SlicePlanes");
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);

			element.appendChild(curSlice.toXML());

		}

		return element;
	}

	public void unselectAll() {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.unselect();
			// curSlice.sketchSpline.selected = false;
		}
	}
	
	
	public void unselectShapesAll() {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.unselectShapes();
		}		
	}
	

	public void update() {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.update();

			if (curSlice.destroy) {
				this.remove(i);
			}
		}

	}

	public void buildOutline(boolean includeSlot, boolean addToPath) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.buildOutline(includeSlot,addToPath);
		}
	}

	public void importSVG(String path) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.importSVG(path);
		}		
	}
	
	
	public void setEditable(boolean editable) {
		
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			curSlice.setEditable(editable);

		}	
		
		}

	public Object getById(int _id) {
		for (int i = 0; i < this.getList().size(); i++) {
			SlicePlane curSlice = this.getList().get(i);
			if(curSlice.getId() == _id)
				return curSlice;

		}	
		return null;
	}



	

	



}
