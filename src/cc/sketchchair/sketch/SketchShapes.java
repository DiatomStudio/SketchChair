/*******************************************************************************
 * This is part of SketchChair, an open-source tool for designing your own furniture.
 *     www.sketchchair.cc
 *     
 *     Copyright (C) 2012, Diatom Studio ltd.  Contact: hello@diatom.cc
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
//#IF JAVA
package cc.sketchchair.sketch;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;
import processing.core.PApplet;
import processing.core.PGraphics;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;
import ShapePacking.spShape;

import cc.sketchchair.core.LOGGER;
import cc.sketchchair.geometry.SlicePlane;

import com.bulletphysics.linearmath.Transform;


/**
 * Container class for SketchShapes. 
 * @author gregsaul
 *
 */
//#ENDIF JAVA

public class SketchShapes {
	List<SketchShape> l = new ArrayList<SketchShape>();

	public SketchShape currentShape = null;
	public SketchShape selectedShape = null;
	public SketchOutlines sketchOutlines = new SketchOutlines(this);
	public SlicePlane onSlicePlane = null;

	private SliceSlots slots = new SliceSlots();;

	public boolean renderShapes = false;

	public boolean selected = true;
	boolean editing = true;

	private boolean preview;

	private Sketch parentSketch;

	public SketchShapes(Sketch parentS) {
		setParentSketch(parentS);
		// TODO Auto-generated constructor stub

		slots = new SliceSlots();
		slots.setParentSketch(parentS);

	}

	public SketchShapes(Sketch sketch, Element element) {
		setParentSketch(sketch);
		slots.setParentSketch(sketch);
		//wrong type
		if (!element.getLocalName().equals("SketchShapes"))
			return;

		//select plane
		selected = true;

		for (int i = 0; i < element.getChildCount(); i++) {
			Element child = (Element) element.getChild(i);

			if (child != null && child.getLocalName().equals("SketchPath"))
				this.add(new SketchPath(getParentSketch(), child));

			if (child != null && child.getLocalName().equals("SketchSpline"))
				this.add(new SketchSpline(getParentSketch(), child));

		}
	}

	SketchShapes(Sketch parentS, SlicePlane slice) {
		setParentSketch(parentS);
		slots.setParentSketch(parentS);

		onSlicePlane = slice;
	}

	public void add(SketchShape sketchShape) {
		this.currentShape = sketchShape;
		this.l.add(sketchShape);

		//sketchShape.setParentSketch(this.getParentSketch());

		if (sketchShape instanceof SketchSpline
				&& ((SketchSpline) sketchShape).path != null)
			((SketchSpline) sketchShape).path.parentSketch = this.parentSketch;

	}

	public void addAll(List<SketchShape> shapes) {
		this.l.addAll(shapes);
	}

	public boolean addPointAlongPath(float x, float y) {
		SketchPoint closestPoint = null;
		SketchShape closestShape = null;
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			if (curSketch instanceof SketchPath
					|| curSketch instanceof SketchSpline) {
				Vec2D pointOnPath = curSketch.getClosestPointAlongPath(x, y);

				if ((pointOnPath != null && closestPoint == null)
						|| (pointOnPath != null && pointOnPath
								.distanceTo(new Vec2D(x, y)) < closestPoint
								.distanceTo(new Vec2D(x, y)))) {
					closestPoint = new SketchPoint(pointOnPath.copy());
					closestShape = curSketch;

				}

			}
		}

		if (closestPoint != null
				&& closestPoint.distanceTo(new Vec2D(x, y)) < SETTINGS_SKETCH.select_dia) {
			closestShape.insertPoint(closestPoint);
			return true;
		} else {
			return false;
		}

	}

	public void buildOutline() {
		this.buildOutline(true, true);
	}

	//#IF JAVA

	/**
	 * Build Outline for cutting, add slots and finger joints.
	 * 
	 * @param includeSlots
	 */
	//#ENDIF JAVA

	public void buildOutline(boolean addSlots, boolean includeSlots) {

		
		
		
		
		this.sketchOutlines.clear();

		if (this.l.size() < 1)
			return;

		Area outlineArea = new Area();

		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			
			//if we're not a closed path skip it
			if(curSketch instanceof SketchPath  && !((SketchPath)curSketch).getClosed())
				continue;
			
			
			GeneralPath gPath = curSketch.getOutlineGeneralPath();

			if (curSketch.union == SketchShape.UNION_ADD)
				outlineArea.add(new Area(gPath));

			if (curSketch.union == SketchShape.UNION_SUBTRACT)
				outlineArea.subtract(new Area(gPath));

		}
		if (addSlots) {
			for (int k = 0; k < getSlots().size(); k++) {
				SliceSlot slot = getSlots().get(k);

				if (slot.type == SliceSlot.FINGER
						|| slot.type == SliceSlot.SLOTS_AND_FINGER) {
					Area area = slot.getOutlineGeneralPathFingers();
					outlineArea.add(area);
				}
			}

			if (includeSlots) {
				//this.buildOutline();
				for (int k = 0; k < getSlots().size(); k++) {
					SliceSlot slot = getSlots().get(k);

					if (slot.type == SliceSlot.SLOT
							|| slot.type == SliceSlot.SLOTS_AND_FINGER) {
						Area area = slot.getOutlineGeneralPath();
						outlineArea.subtract(area);
					}
				}
			} else {
				for (int k = 0; k < getSlots().size(); k++) {
					SliceSlot slot = getSlots().get(k);
					if (slot.type == SliceSlot.SLOT
							|| slot.type == SliceSlot.SLOTS_AND_FINGER) {
						slot.buildSketchOutlines(this.sketchOutlines,
								this.parentSketch);
					}
				}
				//outlineArea.subtract(new Area(gPath));

			}
		}
		
		buildOutlinesFromAWTShape(outlineArea);
		//#IF JAVA

		/*
				SketchShape curSketch = null;
				GeneralPath gPath = null;

				if(this.l.size() > 0){
				 curSketch = (SketchShape) this.l.get(0);
				 gPath = curSketch.getOutlineGeneralPath();
				}
				*/
		//#ENDIF JAVA
this.sketchOutlines.optimize();
	}
	
	public void buildPathsFromAWTShape(Shape awtShape){

		PathIterator iter = awtShape.getPathIterator(null);
		SketchPath path = new SketchPath(getParentSketch());
		path.setType(SketchShape.TYPE_PATH);
		float coords[] = new float[6];
		SketchPoint prevVec = null;
		
		while (!iter.isDone()) {
			switch (iter.currentSegment(coords)) {

			case PathIterator.SEG_MOVETO: // 1 point (2 vars) in coords
				this.add(path);
				path = new SketchPath(this.getParentSketch());
				path.add(new SketchPoint(coords[0], coords[1]));

				break;

			case PathIterator.SEG_LINETO: // 1 point
				path.add(new SketchPoint(coords[0], coords[1]));
				break;

			case PathIterator.SEG_QUADTO: // 2 points
				break;

			case PathIterator.SEG_CUBICTO: // 3 points

				prevVec = path.getPath().getLast();
				SketchPoint newVec = new SketchPoint(coords[4], coords[5]);
				Vec2D controlNodeBack = new Vec2D(coords[0], coords[1]);
				Vec2D controlNodeFoward = new Vec2D(coords[2], coords[3]);

				newVec.controlPoint1 = controlNodeFoward;

				if (prevVec != null) {
					prevVec.controlPoint2 = controlNodeBack;
					path.set(path.size() - 1,
							prevVec);
				}
				path.add(newVec);
				break;

			case PathIterator.SEG_CLOSE:
				break;
			}
			iter.next();
		}
		path.setClosed(true);
		
		if(path.getList().size() > 1)
		this.add(path);

	}

	

	public void buildOutlinesFromAWTShape(Shape awtShape){

		PathIterator iter = awtShape.getPathIterator(null);
		//PathIterator iter = gPath.getPathIterator(null);
		SketchOutline sktOutline = new SketchOutline(this.getParentSketch());

		float coords[] = new float[6];
		//  System.out.println("before");

		SketchPoint prevVec = null;
		while (!iter.isDone()) {
			switch (iter.currentSegment(coords)) {

			case PathIterator.SEG_MOVETO: // 1 point (2 vars) in coords
				this.sketchOutlines.add(sktOutline);
				sktOutline = new SketchOutline(this.getParentSketch());
				sktOutline.getPath().add(new SketchPoint(coords[0], coords[1]));
				//System.out.println("newSketch");

				break;

			case PathIterator.SEG_LINETO: // 1 point
				//	System.out.println( "SEG_LINETO " + coords[0]+" "+coords[1]);
				sktOutline.getPath().add(new SketchPoint(coords[0], coords[1]));
				break;

			case PathIterator.SEG_QUADTO: // 2 points
				//System.out.println("QUAD");
				break;

			case PathIterator.SEG_CUBICTO: // 3 points

				prevVec = sktOutline.getPath().getLast();
				SketchPoint newVec = new SketchPoint(coords[4], coords[5]);

				//SketchPoint preVecBez1 =  (SketchPoint) sktOutline.path.get(sktOutline.path.size()-2);
				//SketchPoint preVecBez2 =  (SketchPoint) sktOutline.path.get(sktOutline.path.size()-1);

				Vec2D controlNodeBack = new Vec2D(coords[0], coords[1]);
				Vec2D controlNodeFoward = new Vec2D(coords[2], coords[3]);

				//System.out.println("new back:"+newVec+":" + controlNodeBack + " : foward" + prevVec+":" + controlNodeFoward);

				newVec.controlPoint1 = controlNodeFoward;

				if (prevVec != null) {
					prevVec.controlPoint2 = controlNodeBack;
					sktOutline.getPath().set(sktOutline.getPath().size() - 1,
							prevVec);
				}
				sktOutline.getPath().add(newVec);
				//#IF JAVA
				/*
				if(preVecBez1.containsBezier()){
					preVecBez1.controlPoint2 = new Vec2D(controlNodeBack.x,controlNodeBack.y);
				}else{
				 bezier = new  BezierControlNode(new Vec2D(preVecBez1.x,preVecBez1.y),controlNodeBack);
				   // sktOutline.path.addBezier(preVecBez1, bezier);
				}
				
				
				if(preVecBez2.containsBezier()){
					preVecBez2.controlPoint1 = new Vec2D(controlNodeFoward.x,controlNodeFoward.y);
				}else{
				 bezier = new  BezierControlNode(new Vec2D(controlNodeFoward.x,controlNodeFoward.y),controlNodeFoward);
				// sktOutline.path.addBezier(preVecBez2, bezier);
				}
				
				
				
				if(preVecBez2.containsBezier()){
				//  	BezierControlNode prevControleNode = (BezierControlNode) sktOutline.path.bezierPoints.get(preVecBez2);
				//  	prevControleNode.c2.x = controlNodeFoward.x;
				// 	prevControleNode.c2.x = controlNodeFoward.y;
				}else{
				//	BezierControlNode bezierPrev = new  BezierControlNode(new Vec2D(preVecBez2.x,preVecBez2.y),controlNodeFoward);
				//sktOutline.path.addBezier(preVecBez2, bezierPrev);
				
				}
				*/
				//#ENDIF JAVA

				break;

			case PathIterator.SEG_CLOSE:
				//   	System.out.println("CLOSE");

				break;
			}
			iter.next();
		}

		this.sketchOutlines.add(sktOutline);
		//this.sketchOutlines.optimize();

		//   if(sktOutline != null)
		//  this.onSlicePlane.coverPath = sktOutline.path ;

	}
	
	public SketchShapes clone() {
		SketchShapes returnsketchShapes = new SketchShapes(getParentSketch(),
				this.onSlicePlane);

		SketchShape curCloned = null;
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);

			SketchShape clonedShape = curSketch.clone();

			if (curSketch == this.currentShape)
				returnsketchShapes.currentShape = clonedShape;

			returnsketchShapes.add(clonedShape);
		}
		returnsketchShapes.buildOutline();
		return returnsketchShapes;
	}

	public boolean contains(SketchShape path) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);

			if (curSketch instanceof SketchSpline
					&& ((SketchSpline) curSketch).path == path)
				return true;

			if (curSketch instanceof SketchSpline
					&& ((SketchSpline) curSketch).getCentrePath() == path)
				return true;

			if (curSketch == path)
				return true;
		}

		return false;
	}

	public SketchShapes copy(Sketch parentSketch) {
		SketchShapes newSketchShapes = new SketchShapes(parentSketch);

		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			newSketchShapes.add(curSketch.copy(parentSketch));
		}

		return newSketchShapes;

	}

	public int countSelectedNodes() {
		int count = 0;

		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			count += curSketch.countSelectedNodes();
		}
		return count;
	}

	public void deleteAll() {
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			curSketch.destroy();
		}

		this.l.clear();
	}

	public void deleteSelectedShapes() {

		if (this.selectedShape != null)
			this.selectedShape.destroy();

		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			if (curSketch.selected) {
				curSketch.destroy();

			}
		}

	}

	public void flipHorizontal(toxi.geom.Vec3D centre) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			curSketch.flipHorizontal(centre);

		}
		this.sketchOutlines.flipHorizontal(centre);
	}

	public float getArea() {
		return this.sketchOutlines.getArea();

	}

	public Vec2D getCentreOfMass() {

		return this.sketchOutlines.getCentreOfMass();
	}

	public SketchShape getClosest(int mouseX, int mouseY) {
		float closestDist = -1;
		SketchShape returnShape = null;
		Vec2D mPos = new Vec2D(mouseX, mouseY);

		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);

			Vec2D closestPointTemp = curSketch.getClosestPointAlongPath(mouseX,
					mouseY);

			if (returnShape == null
					|| closestPointTemp.distanceTo(mPos) < closestDist) {
				closestDist = closestPointTemp.distanceTo(mPos);
				returnShape = curSketch;
			}

		}
		return returnShape;
	}

	public SketchPoint getClosestPathVertex(Vec2D pointOnPlan) {
		SketchPoint closestpoint = null;
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);

			if (curSketch instanceof SketchPath) {
				SketchPoint p = curSketch.getClosestPoint(pointOnPlan);

				if (closestpoint == null
						|| pointOnPlan.distanceTo(p) < pointOnPlan
								.distanceTo(closestpoint))
					closestpoint = p;
			}

			if (curSketch instanceof SketchSpline) {
				SketchPoint p = ((SketchSpline) curSketch).getCentrePath().getClosestPoint(pointOnPlan);
				if (closestpoint == null
						|| pointOnPlan.distanceTo(p) < pointOnPlan
								.distanceTo(closestpoint))
					closestpoint = p;
			}

		}
		return closestpoint;
	}

	public SketchShape getFirst() {
		if (this.l.size() > 0)
			return this.l.get(0);
		else
			return null;
	}

	public float getHeight() {
		return this.sketchOutlines.getHeight();
	}

	public SketchShape getLast() {

		if (this.l.size() > 0)
			return this.l.get(this.l.size() - 1);
		else
			return null;
	}

	Vec2D getLastVec() {
		return this.getLast().getLast();

	}

	public float getMaxX() {
		return this.sketchOutlines.getMaxX();
	}

	public float getMaxXWorldSpace(Transform currentWorldTransform) {
		return this.sketchOutlines.getMaxXWorldSpace(currentWorldTransform);
	}

	public float getMaxY() {
		return this.sketchOutlines.getMaxY();
	}

	public float getMaxYWorldSpace(Transform currentWorldTransform) {
		return this.sketchOutlines.getMaxYWorldSpace(currentWorldTransform);
	}

	public float getMinX() {
		return this.sketchOutlines.getMinX();
	}

	public float getMinXWorldSpace(Transform currentWorldTransform) {
		return this.sketchOutlines.getMinXWorldSpace(currentWorldTransform);
	}

	public float getMinY() {
		return this.sketchOutlines.getMinY();
	}

	public float getMinYWorldSpace(Transform currentWorldTransform) {
		return this.sketchOutlines.getMinYWorldSpace(currentWorldTransform);
	}

	public SketchPoint getOverSelectPoint(float x, float y) {
		Vec2D pointOnPlan = new Vec2D(x, y);
		SketchPoint p = this.getClosestPathVertex(pointOnPlan);

		if (p != null && pointOnPlan.distanceTo(p) < SETTINGS_SKETCH.select_dia)
			return p;
		else
			return null;
	}

	public List<SketchShape> getOverShape(float x, float y) {
		List<SketchShape> overShapes = new ArrayList<SketchShape>();

		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			SketchShape returnedSketch = curSketch.getOverShape(x, y);
			if (returnedSketch != null)
				overShapes.add(returnedSketch);

		}
		return overShapes;
	}

	private Sketch getParentSketch() {
		return this.parentSketch;
	}

	public SketchShape getShapePickBuffer(int col) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			Vec2D ret = curSketch.getVec2DpickBuffer(col);

			if (ret != null) {
				return curSketch;

			}
		}
		return null;
	}

	//#ENDIF JAVA
	public SketchShape getSketchShapeById(int linkedSketchId) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);

			if (curSketch instanceof SketchSpline
					&& ((SketchSpline) curSketch).path.getId() == linkedSketchId)
				return ((SketchSpline) curSketch).path;

			if (curSketch instanceof SketchSpline
					&& ((SketchSpline) curSketch).getCentrePath().getId() == linkedSketchId)
				return ((SketchSpline) curSketch).getCentrePath();

			if (curSketch.getId() == linkedSketchId)
				return curSketch;
		}

		return null;
	}


	public SliceSlots getSlots() {
		return slots;
	}

	public spShape getspShape() {

		if (getParentSketch().getSketchGlobals().seperate_slots) {
			this.buildOutline(true, false);
		} else {
			this.buildOutline(true, true);

		}

		spShape returnShape = this.sketchOutlines.getspShape();
		
		//add collisionOutlines
		 this.buildOutline(false, false);
		 this.sketchOutlines.addCollisionToSpShape(returnShape);
		
		
		return returnShape;

	}

	public Vec2D getVec2DpickBuffer(int col) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			Vec2D ret = curSketch.getVec2DpickBuffer(col);
			if (ret != null)
				return ret;
		}
		return null;
	}

	boolean isEditing() {
		return true;
	}

	boolean isSelected() {
		return this.selected;
	}

	public boolean lastSketchOverlaps() {
		boolean overlaps = false;
		if (this.l.size() > 1) {
			SketchShape lastSketch = this.getLast();
			for (int i = 0; i < this.l.size() - 1; i++) {
				SketchShape curSketch = this.l.get(i);
				if (lastSketch.overlaps(curSketch))
					overlaps = true;
			}

		}
		return overlaps;
	}

	public void mouseDragged(float mouseX, float mouseY) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			curSketch.mouseDragged(mouseX, mouseY);

		}
	}

	public void mouseReleased(float mouseX, float mouseY) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			curSketch.mouseReleased(mouseX, mouseY);
		}
	}

	public void optimize() {
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			curSketch.optimize();
		}

	}

	public boolean overSelectPoint(float mouseX, float mouseY) {
		Vec2D pointOnPlan = new Vec2D(mouseX, mouseY);
		SketchPoint p = this.getClosestPathVertex(pointOnPlan);

		if (p != null && pointOnPlan.distanceTo(p) < SETTINGS_SKETCH.select_dia) {

			return true;
		} else
			return false;
	}

	void removeLast() {
		if (this.l.size() > 0)
			this.l.remove(this.l.size() - 1);

		this.buildOutline();
	}

	public void removeVertex(SketchPoint v) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			curSketch.removeVertex(v);
		}
	}

	public void render(PGraphics g) {

		this.update();
	


		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			curSketch.render(g);

		}
	
	}

	public void renderOutline(PGraphics g) {

		this.sketchOutlines.render(g);

	}

	public void renderPickBuffer(PGraphics g) {

			for (int i = 0; i < this.l.size(); i++) {
				SketchShape curSketch = this.l.get(i);
				curSketch.renderPickBuffer(g);
			}
		

	}

	public void renderSide(PGraphics g) {
		
		// RENDER MODES
		switch(getParentSketch().getRenderMode()){
		
		//#IF JAVA
		/*	3D preview
		 *  Used to render the 3D preview in the top right corner of the screen
		 */
		//#ENDIF JAVA

		case Sketch.RENDER_3D_EDITING_PLANES:
		if(getParentSketch().getLayerSelected()){
			
			g.stroke(SETTINGS_SKETCH.RENDER_3D_NORMAL_SKETCHOUTLINE_STROKE_COLOUR);
			g.strokeWeight(SETTINGS_SKETCH.RENDER_3D_NORMAL_SKETCHOUTLINE_STROKE_WEIGHT);
			
			
			float maxY = this.getMaxY();
			float minY = this.getMinY();
			float maxX = this.getMaxX();

			float extrudeDepth = (this.getParentSketch().getOnSketchPlane().thickness)/SETTINGS_SKETCH.scale;
			float topLayerX = maxX;
			
			g.pushMatrix();
			g.beginShape();
			g.vertex(topLayerX,minY,-(extrudeDepth/2f));
			g.vertex(topLayerX,minY,(extrudeDepth/2f));
			g.vertex(topLayerX,maxY,(extrudeDepth/2f));
			g.vertex(topLayerX,maxY,-(extrudeDepth/2f));
			g.vertex(topLayerX,minY,-(extrudeDepth/2f));
			g.endShape();
			g.popMatrix();
			//g.rect(0, minY, 100, maxY - minY);
			
			
		}
		
			
		break;
		
		//#IF JAVA
			
		/*	3D preview
		 *  Used to render the 3D preview in the top right corner of the screen
		 */
		//#ENDIF JAVA

		case Sketch.RENDER_3D_NORMAL:
			
			g.stroke(SETTINGS_SKETCH.RENDER_3D_NORMAL_SKETCHOUTLINE_STROKE_COLOUR);
			g.strokeWeight(SETTINGS_SKETCH.RENDER_3D_NORMAL_SKETCHOUTLINE_STROKE_WEIGHT);
			
			
			float maxY = this.getMaxY();
			float minY = this.getMinY();
			float maxX = this.getMaxX();

			float extrudeDepth = (this.getParentSketch().getOnSketchPlane().thickness)/SETTINGS_SKETCH.scale;
			float topLayerX = maxX;
			
			g.pushMatrix();
			g.beginShape();
			g.vertex(topLayerX,minY,-(extrudeDepth/2f));
			g.vertex(topLayerX,minY,(extrudeDepth/2f));
			g.vertex(topLayerX,maxY,(extrudeDepth/2f));
			g.vertex(topLayerX,maxY,-(extrudeDepth/2f));
			g.vertex(topLayerX,minY,-(extrudeDepth/2f));
			g.endShape();
			g.popMatrix();
			//g.rect(0, minY, 100, maxY - minY);
			

			
		break;
			
			
		}
		
		



	}

	public void renderSilhouette(PGraphics g) {

		
		this.sketchOutlines.renderSilhouette(g);
	}

	public void scale(float scale, toxi.geom.Vec3D centre) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			curSketch.scale(scale, centre);

		}
		this.sketchOutlines.scale(scale, centre);
	}

	public void select() {
		this.selected = true;
	}

	public void selectAll() {
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			curSketch.select();
		}
	}

	public void selectNodes(float x, float y) {
		boolean nodeFound = false;
		Vec2D selectedNode = null;
		Object selectedNodeObj = null;
		SketchShape nodesFoundInSketch = null;

		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			curSketch.selectNodes(x, y);

			//only select one node at a time
			if (curSketch.getSelectedNodes().size() > 0) {

				for (int n = 0; n < curSketch.getSelectedNodes().size(); n++) {
					Object o = curSketch.getSelectedNodes().get(n);
					Vec2D p = (Vec2D) curSketch.getSelectedNodes().get(n);

					nodeFound = true;
					if (selectedNode == null
							|| p.distanceTo(new Vec2D(x, y)) < selectedNode
									.distanceTo(new Vec2D(x, y))) {
						selectedNode = p;
						selectedNodeObj = o;
					}
				}
			}

			if (nodeFound) {

				//was there a point found in a previous sketch?
				if (nodesFoundInSketch != null) {
					nodesFoundInSketch.getSelectedNodes().clear();
				}
				getParentSketch().getSelectedNodes().clear();
				curSketch.getSelectedNodes().clear();
				curSketch.getSelectedNodes().add(selectedNodeObj);
				nodesFoundInSketch = curSketch;
			}
			nodeFound = false;

		}

		if (selectedNode != null)
			getParentSketch().getSelectedNodes().add(selectedNodeObj);

		//	this.selectShape(x, y);
		//else

	}

	public void selectShape(float x, float y) {
		if (this.selectedShape != null) {
			this.selectedShape.selected = false;
			this.selectedShape = null;
		}

		unSelectAll();

		// TODO Auto-generated method stub
		SketchShape closestShape = null;
		Vec2D closestPoint = null;
		Vec2D mousePos = new Vec2D(x, y);
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			Vec2D point = curSketch.getClosestPointAlongPath(x, y);

			if (point == null)
				return;

			if (i == 0
					|| mousePos.distanceTo(point) < mousePos
							.distanceTo(closestPoint)) {
				closestPoint = point;
				closestShape = curSketch;
			}

		}
		if (mousePos.distanceTo(closestPoint) < SETTINGS_SKETCH.select_dia
				&& closestShape != null) {
			this.selectedShape = closestShape;
			this.selectedShape.selected = true;
			closestShape.select();
		}

	}

	public void setEditing(boolean e) {
		editing = e;
	}

	void setParentSketch(Sketch parentS) {
		parentSketch = parentS;

		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			curSketch.setParentSketch(parentS);
		}
	}

	public void setSlots(SliceSlots slots) {
		this.slots = slots;
	}

	public Vec2D setVec2DpickBuffer(int col, SketchPoint selectedVec,
			SketchShape selectedShape, SlicePlane selectedVecPlane,
			boolean isSelectedVecOnOutline) {

		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			Vec2D ret = curSketch.setVec2DpickBuffer(col, selectedVec,
					selectedShape, selectedVecPlane, isSelectedVecOnOutline);
			if (ret != null) {
				selectedShape = curSketch;
				return ret;

			}
		}
		return null;
	}

	public int size() {
		return this.l.size();
	}

	public Element toXML() {
		Element element = new Element("SketchShapes");
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			element.appendChild(curSketch.toXML());
		}
		return element;
	}

	public void unselect() {
		this.selected = false;
	}

	public void unSelectAll() {

		for (int i = 0; i < this.l.size(); i++) {
			SketchShape sketch = this.l.get(i);
			sketch.unselect();
		}
	}

	void update() {

		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);

			if (curSketch.isDestroying() == true) {
				this.l.remove(i);

				if (this.currentShape == curSketch) {
					if (this.l.size() > 0)
						this.currentShape = this.l.get(this.l.size() - 1);
					else
						this.currentShape = null;

				}

				this.buildOutline();
				//if(GLOBAL.sketchChairs.getCurChair() != null)
				//	GLOBAL.sketchChairs.getCurChair().setUpdateCollisionMesh(true);
			}

			curSketch.selected = false;

			if (i == this.l.size() - 1) {
				//	curSketch.selected = true;
			}

		}

	}

	public void removeLegs() {
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			if(curSketch instanceof SketchSpline && ((SketchSpline)curSketch).getType() == SketchSpline.TYPE_LEG ){
				curSketch.destroy();
				this.l.remove(i);
				i--;				
			}
		}
		this.buildOutline();
	}
	
	public void setEditable(boolean editable) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchShape curSketch = this.l.get(i);
			curSketch.setEditable(editable);
		}
		}

	

}
