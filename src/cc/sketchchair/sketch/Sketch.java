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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;
import ShapePacking.spShape;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.UITools;
import cc.sketchchair.core.Undo;
import cc.sketchchair.core.UndoAction;
import cc.sketchchair.geometry.SlicePlane;

import com.bulletphysics.linearmath.Transform;
import com.kitfox.svg.Group;
import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;

/**
 * Main Sketch class used to interact with SketchShapes. 
 * Currently Sketch and SketchShapes perform many of the same functions and may be rewritten to a little more clean. 
 * There is one Sketch object per SlicePlane. 
 * @author gregsaul
 *
 */
//#ENDIF JAVA

public class Sketch {
	public static final int RENDER_3D_EDITING_PLANES = 1; // a plane is selected and being edited
	public static final int RENDER_3D_PREVIW = 2; // top right hand preview and layer selector
	public static final int RENDER_3D_DIAGRAM = 3; // exporting a picture as a diagram // add extra edges
	public static final int RENDER_EDIT_SELECT = 4; //
	public static final int RENDER_3D_NORMAL = 5; // Nothing is selected or being edited
	
	SketchShapes sketchShapes;
	boolean selected = true;
	public SketchTools sketchTools;

	public SketchGlobals sketchGlobals;
	private SlicePlane onSlicePlane;
	public boolean screenshot = false;

	public int sketch_id;
	private ArrayList<Object> selectedNodes = new ArrayList();
	private boolean layerSelected = true;

	private int renderMode;
	private boolean render3D;

	public Sketch(PApplet app) {
		sketchShapes = new SketchShapes(this);
		setSketchTools(new SketchTools(app));
		setSketchGlobals(new SketchGlobals());
	}

	public Sketch(SketchTools sTools, SketchGlobals sGlobals) {
		sketchShapes = new SketchShapes(this);
		setSketchTools(sTools);
		setSketchGlobals(sGlobals);
	}

	public Sketch(SketchTools sTools, SketchGlobals sGlobals, Element element) {
		setSketchTools(sTools);
		setSketchGlobals(sGlobals);
		sketchShapes = new SketchShapes(this, element);
	}

	public Sketch(SketchTools sTools, SketchGlobals sGlobals, SlicePlane slice) {
		sketchShapes = new SketchShapes(this);
		sketchShapes.onSlicePlane = slice;
		setSketchTools(sTools);
		setSketchGlobals(sGlobals);
	}

	public void add(SketchShape sketchShape) {
		sketchShapes.add(sketchShape);
	}

	public boolean addPointAlongPath(float x, float y) {
		return sketchShapes.addPointAlongPath(x, y);

	}

	public void build() {
		for (int i = 0; i < this.getSketchShapes().l.size(); i++) {
			SketchShape s = this.getSketchShapes().l.get(i);
			s.build();
		}
		this.buildOutline();
	}

	public void buildOutline() {
		sketchShapes.buildOutline(false, false);
	}

	public void buildOutline(boolean addSlots, boolean booleanSlots) {
		sketchShapes.buildOutline(addSlots, booleanSlots);

	}
	

	public Sketch clone() {
		Sketch s = new Sketch(getSketchTools(), getSketchGlobals());
		s.sketchShapes = this.sketchShapes.clone();
		s.sketchShapes.setParentSketch(s);

		return s;
		//return clone();
	}

	public boolean contains(SketchShape path) {
		return sketchShapes.contains(path);
	}

	public Sketch copy() {
		Sketch newSketch = new Sketch(this.getSketchTools(),
				this.getSketchGlobals());
		newSketch.sketchShapes = this.sketchShapes.copy(newSketch);
		return newSketch;
	}

	public int countSelectedNodes() {
		return sketchShapes.countSelectedNodes();

	}

	public void deleteAll() {
		this.getSketchShapes().deleteAll();
	}

	public void deleteSelectedNodes() {
		for (int i = 0; i < this.getSelectedNodes().size(); i++) {
			SketchPoint sketchP = (SketchPoint) this.getSelectedNodes().get(i);
			this.removeVertex(sketchP);
		}
		this.buildOutline();
	}

	public void deleteSelectedShapes() {
		sketchShapes.deleteSelectedShapes();
		this.buildOutline();
		this.deleteSelectedNodes();
	}

	public void flipHorizontal(toxi.geom.Vec3D centre) {
		sketchShapes.flipHorizontal(centre);
	}

	public PApplet getApplet() {
		// TODO Auto-generated method stub
		return null;
	}

	public float getArea() {
		return sketchShapes.getArea();

	}

	public Vec2D getCentreOfMass() {
		return sketchShapes.getCentreOfMass();
	}

	public SketchPoint getClosestPathVertex(Vec2D pointOnPlan) {
		return sketchShapes.getClosestPathVertex(pointOnPlan);
	}

	public SketchShape getCurrentShape() {
		return getSketchShapes().currentShape;
	}

	public SketchShape getFirst() {
		return sketchShapes.getFirst();
	}

	public float getHeight() {
		return sketchShapes.getHeight();
	}

	public SketchShape getLast() {
		return sketchShapes.getLast();
	}

	public Vec2D getLastVec() {
		return sketchShapes.getLastVec();
	}

	public boolean getLayerSelected() {
		return this.layerSelected;
	}

	public float getMaxX() {
		return sketchShapes.getMaxX();
	}

	public float getMaxXWorldSpace(Transform currentWorldTransform) {
		return sketchShapes.sketchOutlines
				.getMaxXWorldSpace(currentWorldTransform);
	}

	public float getMaxY() {
		return sketchShapes.getMaxY();
	}

	public float getMaxYWorldSpace(Transform currentWorldTransform) {
		return sketchShapes.sketchOutlines
				.getMaxYWorldSpace(currentWorldTransform);
	}

	public float getMinX() {
		return sketchShapes.sketchOutlines.getMinX();
	}

	public float getMinXWorldSpace(Transform currentWorldTransform) {
		return sketchShapes.sketchOutlines
				.getMinXWorldSpace(currentWorldTransform);
	}

	public float getMinY() {
		return sketchShapes.sketchOutlines.getMinY();
	}

	public float getMinYWorldSpace(Transform currentWorldTransform) {
		return sketchShapes.sketchOutlines
				.getMinYWorldSpace(currentWorldTransform);
	}

	public SlicePlane getOnSketchPlane() {
		return getOnSlicePlane();
	}

	/**
	 * @return the onSlicePlane
	 */
	public SlicePlane getOnSlicePlane() {
		return onSlicePlane;
	}

	public SketchPoint getOverSelectPoint(float x, float y) {
		return sketchShapes.getOverSelectPoint(x, y);
	}

	public List<SketchShape> getOverShape(float x, float y) {
		return sketchShapes.getOverShape(x,y);
	}

	public int getRenderMode() {
		return this.renderMode;
	}

	protected ArrayList<Object> getSelectedNodes() {
		return this.selectedNodes;
	}

	public SketchShape getSelectedShape() {
		// TODO Auto-generated method stub
		return getSketchShapes().selectedShape;
	}

	public SketchShape getShapePickBuffer(int col) {
		return sketchShapes.getShapePickBuffer(col);
	}

	public SketchGlobals getSketchGlobals() {
		return sketchGlobals;
	}

	public SketchShape getSketchShapeById(int linkedSketchId) {
		return sketchShapes.getSketchShapeById(linkedSketchId);
	}

	public SketchShapes getSketchShapes() {
		// TODO Auto-generated method stub
		return sketchShapes;
	}

	public SketchTools getSketchTools() {
		return sketchTools;
	}

	/**
	 * @return the slots
	 */
	public SliceSlots getSlots() {
		return sketchShapes.getSlots();
	}

	public spShape getspShape() {
		return sketchShapes.getspShape();
	}

	public Vec2D getVec2DpickBuffer(int col) {
		return sketchShapes.getVec2DpickBuffer(col);

	}

	public int getZOOM() {
		// TODO Auto-generated method stub
		return 1;
	}

	private boolean isEditing() {
		return sketchShapes.isEditing();
	}

	boolean isSelected() {
		//return false;
		return sketchShapes.selected;
	}

	public boolean lastSketchOverlaps() {
		return sketchShapes.lastSketchOverlaps();
	}

	public void mouseDragged(float mouseX, float mouseY) {

		//#IF JAVA
		if(GLOBAL.gui != null && GLOBAL.gui.overComponent())
			return;
		//#ENDIF JAVA
		
		sketchShapes.mouseDragged(mouseX, mouseY);

		if (getSketchTools().getCurrentTool() == SketchTools.DRAW_TOOL) {

			if (getCurrentShape() != null) {
				getCurrentShape().add(new SketchPoint(mouseX, mouseY));
			}

		}

		if (getSketchTools().getCurrentTool() == SketchTools.LEG_TOOL) {
			Vec2D pointOnPlan = new Vec2D(mouseX, mouseY);
			if (getLastVec() != null) {
				getLastVec().set(pointOnPlan.x, pointOnPlan.y);
				getCurrentShape().offset();
			}
		}

	}

	public void mousePressed(float mouseX, float mouseY) {
		
		LOGGER.debug("mousePressed" +getSketchTools().getMouseButton());

		
		//#IF JAVA
		if(GLOBAL.gui != null && GLOBAL.gui.overComponent())
			return;
		//#ENDIF JAVA
		

		//DRAW TOOL OPERATIONS
		if (getSketchTools().getCurrentTool() == SketchTools.DRAW_TOOL
				&& getSketchTools().getMouseButton() == SketchTools.MOUSE_LEFT) {
			Vec2D pointOnPlan = new Vec2D(mouseX, mouseY);
			//alert(pointOnPlan.distanceTo(pointOnPlan.add(new Vec2D(10,10))));
			SketchPoint sp = new SketchPoint(mouseX, mouseY);
			//alert(sp.distanceTo(pointOnPlan.add(new Vec2D(10,10))));

			SketchSpline newSketch = new SketchSpline(this,
					SketchSpline.OFFSET_BOTH);
			newSketch.setOffsetSize(getSketchTools().brush_dia);
			newSketch.setCap(getSketchTools().getCurrentCapType());
			newSketch.add(new SketchPoint(mouseX, mouseY));
			newSketch.setType(SketchShape.TYPE_SPLINE);
			add(newSketch);
			setCurrentShape(newSketch);

			//if(getSketchGlobals().undo != null)
			//getSketchGlobals().undo.addOperation(new UndoAction(newSketch,UndoAction.ADD_SHAPE));	
		}

		//LEG TOOL OPERATIONS
		if (getSketchTools().getCurrentTool() == SketchTools.LEG_TOOL
				&& getSketchTools().getMouseButton() == SketchTools.MOUSE_LEFT) {

			Vec2D pointOnPlan = new Vec2D(mouseX, mouseY);

			SketchSpline newSketch = new SketchSpline(this,
					SketchSpline.OFFSET_BOTH);
			newSketch.setType(SketchSpline.TYPE_LEG);
			newSketch.setOffsetSize(getSketchTools().brush_dia
					* SETTINGS_SKETCH.LEG_BRUSH_RATIO_TOP);
			//newSketch.offsetSizeEnd = getSketchTools().brush_dia*SETTINGS_SKETCH.LEG_BRUSH_RATIO_BOTTOM;
			newSketch.getCentreOffset().put(
					1,
					getSketchTools().brush_dia
							* SETTINGS_SKETCH.LEG_BRUSH_RATIO_BOTTOM);
			newSketch.capType = SketchSpline.CAP_LEG;

			newSketch.add(new SketchPoint(pointOnPlan.x, pointOnPlan.y));
			newSketch.add(new SketchPoint(pointOnPlan.x, pointOnPlan.y));
			newSketch.path.editable = true;
			add(newSketch);
			setCurrentShape(newSketch);
			//getSketchGlobals().undo.addOperation(new UndoAction(newSketch,UndoAction.ADD_SHAPE));
		}

		if ((getSketchTools().getCurrentTool() == SketchTools.SELECT_TOOL || getSketchTools()
				.getCurrentTool() == SketchTools.SELECT_BEZIER_TOOL)
				&& getSketchTools().getMouseButton() == SketchTools.MOUSE_LEFT) {
			selectNodes(mouseX, mouseY);
		}

		if (getSketchTools().getCurrentTool() == SketchTools.SELECT_BEZIER_TOOL
				&& getSelectedShape() != null && getSelectedNodes().size() > 0
				&& getSelectedNodes().get(0) != null) {

			if (getSelectedNodes().size() > 0) {
				Object obj = (Object) getSelectedNodes().get(0);
				if (obj instanceof SketchPoint) {
					SketchPoint selectedVec = (SketchPoint) obj;
					if (!selectedVec.containsBezier()) {
					
//						getSelectedShape().getPath().addBezier(
//								(SketchPoint) selectedVec, new Vec2D(selectedVec.x - 10,
//										selectedVec.y + 10), new Vec2D(
//										selectedVec.x + 10, selectedVec.y + 10));

					}
				}
			}
		}

		
		this.unSelectAll();

	}

	public void mouseReleased(float mouseX, float mouseY) {
		
		
		LOGGER.debug("mouseReleased" +getSketchTools().getMouseButton());
		
		//#IF JAVA
		if(GLOBAL.gui != null && GLOBAL.gui.overComponent())
			return;
		//#ENDIF JAVA
		
		
		//LOGGER.info(" mouseReeased" + this.onSlicePlane.getId());
		sketchShapes.mouseReleased(mouseX, mouseY);

		//OFFSETPATH TOOL
		//___________________________________________________________________________________________________

		if (getSketchTools().getCurrentTool() == SketchTools.DRAW_OFFSETPATH_TOOL) {
			boolean skip = false;
			Vec2D pointOnPlan = new Vec2D(mouseX, mouseY);

			if (getCurrentShape() != null
					&& getCurrentShape().getType() == SketchShape.OFFSET_SPLINE
					&& getSketchTools().getMouseButton() == PConstants.RIGHT
					&& !getCurrentShape().getClosed() && !skip) {
				SketchSpline spline = (SketchSpline) getCurrentShape();
				spline.getCentrePath().remove(spline.getCentrePath().getLast());
				getCurrentShape().setClosed(true);
				spline.offset();
				skip = true;
			}

			if (getCurrentShape() != null
					&& (getCurrentShape().getType() != SketchShape.OFFSET_SPLINE || getCurrentShape()
							.getClosed())
					&& getSketchTools().getMouseButton() == PConstants.LEFT
					&& !skip) {

				SketchSpline sketch = new SketchSpline(this);
				sketch.setType(SketchShape.OFFSET_SPLINE);
				sketch.autoSmooth = false;
				sketch.setOffsetSize(this.getSketchTools().brush_dia);
				sketch.setJoinType(SketchSpline.JOIN_ROUND);
				sketch.setCap(getSketchTools().getCurrentCapType());
				add(sketch);

			}

			if (getCurrentShape() == null
					&& getSketchTools().getMouseButton() == PConstants.LEFT
					&& !skip) {

				SketchSpline sketch = new SketchSpline(this);
				sketch.setType(SketchShape.OFFSET_SPLINE);
				sketch.autoSmooth = false;
				sketch.setJoinType(SketchSpline.JOIN_ROUND);
				sketch.setOffsetSize(this.getSketchTools().brush_dia);
				sketch.setCap(getSketchTools().getCurrentCapType());
				add(sketch);
			}

			//add a point
			if (getCurrentShape() != null
					&& getCurrentShape().getType() == SketchShape.OFFSET_SPLINE
					&& getSketchTools().getMouseButton() == PConstants.LEFT
					&& !skip) {

				getCurrentShape().add(
						new SketchPoint(pointOnPlan.x, pointOnPlan.y));
				getCurrentShape().add(
						new SketchPoint(pointOnPlan.x + 10, pointOnPlan.y + 1));
			}

		}

		

		
		
		//PATH TOOL
		//___________________________________________________________________________________________________
		if (getSketchTools().getCurrentTool() == SketchTools.DRAW_PATH_TOOL) {

			boolean skip = false;

			
			
			
			
			//Remove vertex!
			if (!skip && (getSketchTools().getMouseButton() == PConstants.LEFT|| getSketchTools().getMouseButton() == PConstants.RIGHT)
					&& getSketchTools().keyPressed
					&& getSketchTools().keyCode == PConstants.CONTROL) {

				Vec2D pointOnPlane = new Vec2D(mouseX, mouseY);

				SketchPoint pathVert = getClosestPathVertex(pointOnPlane);
		
				if (pathVert != null && pointOnPlane.distanceTo(pathVert) < SETTINGS_SKETCH.select_dia) {
					removeVertex(pathVert);
					skip = true;
				}

			}

			//check to see if we are adding a new point to an existing path
			if (!skip && (getSketchTools().getMouseButton() == PConstants.LEFT || getSketchTools().getMouseButton() == PConstants.RIGHT)
					&& getSketchTools().keyPressed
					&& getSketchTools().keyCode == PConstants.CONTROL) {

				Vec2D pointOnPlane = new Vec2D(mouseX, mouseY);

				if (addPointAlongPath(pointOnPlane.x, pointOnPlane.y))
					skip = true;

			}


				Vec2D pointOnPlan = new Vec2D(mouseX, mouseY);
				
				
				/*
				if (getSketchTools().getMouseButton() == PConstants.RIGHT && !skip) {

					SketchPoint pathVert = getClosestPathVertex(pointOnPlan);

					
					if (pathVert != null
							&& pointOnPlan.distanceTo(pathVert) < SETTINGS_SKETCH.select_dia) {
						removeVertex(pathVert);
						skip = true;
					}
					
				}
				*/

				if (getCurrentShape() != null
						&& getCurrentShape().getType() == SketchShape.TYPE_PATH
						&& getSketchTools().getMouseButton() == PConstants.LEFT
						&& !skip) {

					SketchPath sketchP = (SketchPath) getCurrentShape();
					if (sketchP.getClosed()) {

						SketchPath sketch = new SketchPath(this);
						sketch.setType(SketchShape.TYPE_PATH);
						add(sketch);
						getCurrentShape().add(
								new SketchPoint(pointOnPlan.x, pointOnPlan.y));

					}
				}

				if (getCurrentShape() == null
						|| getCurrentShape().getType() != SketchShape.TYPE_PATH
						&& getSketchTools().getMouseButton() == PConstants.LEFT
						&& !skip) {

					SketchPath sketch = new SketchPath(this);
					sketch.setType(SketchShape.TYPE_PATH);
					sketch.setClosed(false);
					add(sketch);
					getCurrentShape().add(
							new SketchPoint(pointOnPlan.x, pointOnPlan.y));

				}

				if (getCurrentShape().getType() == SketchShape.TYPE_PATH
						&& getSketchTools().getMouseButton() == PConstants.LEFT
						&& !skip) {

					if (getCurrentShape().getLength() > 2) {

						Vec2D firstPoint = (Vec2D) ((SketchPath) getCurrentShape())
								.get(0);
						Vec2D mousePos = new Vec2D(mouseX, mouseY);
						
						//mousePos = GLOBAL.uiTools.getPointOnPlane(mousePos, getCurrentShape().getParentSketch().getOnSketchPlane().getPlane());
						if (firstPoint.distanceTo(mousePos) < SETTINGS_SKETCH.MIN_CLOSE_SHAPE_DIST) {
							SketchPath path = (SketchPath) getCurrentShape();
							path.remove(path.getLast());
							((SketchPath) getCurrentShape()).setClosed(true);

							skip = true;
						}
					}
				}

				//add a point
				if ((getCurrentShape().getType() == SketchShape.TYPE_PATH || getCurrentShape()
						.getType() == SketchShape.TYPE_SPLINE)
						&& getSketchTools().getMouseButton() == PConstants.LEFT
						&& !skip) {

					getCurrentShape().add(
							new SketchPoint(pointOnPlan.x, pointOnPlan.y));
					//getCurrentShape().add(new SketchPoint(0, 0));

				}

				if (getCurrentShape().getType() == SketchShape.TYPE_PATH
						&& getSketchTools().getMouseButton() == PConstants.RIGHT
						&& !getCurrentShape().getClosed() && !skip) {

					SketchPath path = (SketchPath) getCurrentShape();
					path.remove(path.getLast());
					path.setClosed(true);
				}

			
		}

		//#IF JAVA
		buildOutline();
		//#ENDIF JAVA

	}
	
	
	public void mouseDoubleClick(float x, float y) {
		
		LOGGER.debug("double Click");
		
		
		if(this.sketchTools.getCurrentTool() == SketchTools.SELECT_TOOL)
		this.selectShape(x, y)	;
		
		
		
		//OFFSETPATH TOOL
		//___________________________________________________________________________________________________

		if (getSketchTools().getCurrentTool() == SketchTools.DRAW_OFFSETPATH_TOOL) {
			boolean skip = false;

			
			if (getCurrentShape() != null
					&& getCurrentShape().getType() == SketchShape.OFFSET_SPLINE
					&& !getCurrentShape().getClosed() && !skip) {
				SketchSpline spline = (SketchSpline) getCurrentShape();
				spline.getCentrePath().remove(spline.getCentrePath().getLast());
				getCurrentShape().setClosed(true);
				spline.offset();
				skip = true;
			}
			
		}
		
		
		if (getSketchTools().getCurrentTool() == SketchTools.DRAW_PATH_TOOL) {
		if (getCurrentShape().getType() == SketchShape.TYPE_PATH
				&& getSketchTools().getMouseButton() == PConstants.LEFT
				&& !getCurrentShape().getClosed()) {
			SketchPath path = (SketchPath) getCurrentShape();
			path.remove(path.getLast());
			path.remove(path.getLast());
			path.setClosed(true);
		}}}

	public int numerOfShapes() {
		// TODO Auto-generated method stub
		return getSketchShapes().l.size();
	}

	public void optimize() {
		sketchShapes.optimize();
	}

	public boolean overSelectPoint(float mouseX, float mouseY) {
		return sketchShapes.overSelectPoint(mouseX, mouseY);
	}

	public void removeLast() {
		sketchShapes.removeLast();
	}

	public void removeVertex(SketchPoint v) {
		sketchShapes.removeVertex(v);
	}

	public void render(PGraphics g) {
		
	
		
		switch(getRenderMode()){
		case Sketch.RENDER_3D_PREVIW:
			sketchShapes.sketchOutlines.render(g);
			sketchShapes.render(g);

		break;
			
		case Sketch.RENDER_3D_EDITING_PLANES:
			if (getLayerSelected() == true){
				if(!sketchGlobals.mousePressed)
				sketchShapes.sketchOutlines.render(g);
				
				getSlots().render(g);
				sketchShapes.render(g);

			}else{
				sketchShapes.sketchOutlines.render(g);
				sketchShapes.render(g);

			}
		break;
			
		case Sketch.RENDER_3D_DIAGRAM:
			sketchShapes.sketchOutlines.render(g);
			sketchShapes.render(g);
			
			
			float extrudeDepth = getOnSketchPlane().thickness / 2;
			extrudeDepth /= SETTINGS_SKETCH.scale;
			
			g.stroke(SETTINGS_SKETCH.SKETCHSHAPE_PATH_COLOUR_UNSELECTED);
			g.strokeWeight(SETTINGS_SKETCH.SKETCHSLOTEDGE_PATH_WEIGHT_DIAGRAM);
			
			g.pushMatrix();
			g.translate(0, 0, extrudeDepth+SETTINGS_SKETCH.OUTLINE_RENDER_OFFSET );
			getSlots().renderEdge(g);
			g.popMatrix();

			g.pushMatrix();
			g.translate(0, 0, -(extrudeDepth+SETTINGS_SKETCH.OUTLINE_RENDER_OFFSET ));
			getSlots().renderEdge(g);
			g.popMatrix();
			
			getSlots().renderEdge(g);

		break;
		
		
		case Sketch.RENDER_3D_NORMAL:
			sketchShapes.sketchOutlines.render(g);
			sketchShapes.render(g);

		break;
		
		
		}
		

	}

	public void renderOutline(PGraphics g) {
		sketchShapes.renderOutline(g);

	}

	public void renderPickBuffer(PGraphics g) {
		sketchShapes.renderPickBuffer(g);
	}

	public void renderSide(PGraphics g) {
		sketchShapes.renderSide(g);

	}

	public void renderSilhouette(PGraphics g) {
		sketchShapes.renderSilhouette(g);
	}

	public void scale(float scale, toxi.geom.Vec3D centre) {
		sketchShapes.scale(scale, centre);
	}

	public void select() {
		this.selected = true;
	}

	public void selectNodes(float x, float y) {
		sketchShapes.selectNodes(x, y);
	}

	public void selectShape(float x, float y) {
		sketchShapes.selectShape(x, y);
	}

	public void setBrushCap(int cap) {
		SketchShape sketch = getSketchShapes().selectedShape;

		if (sketch instanceof SketchSpline) {
			SketchSpline spline = (SketchSpline) sketch;
			spline.setCap(cap);
			spline.offset();
		}

	}

	public void setBrushDia(float val) {
		SketchShape sketch = getSketchShapes().selectedShape;

		if (sketch instanceof SketchSpline) {
			SketchSpline spline = (SketchSpline) sketch;
			spline.setOffsetSize(val);
		}

		for (int i = 0; i < this.getSketchShapes().l.size(); i++) {
			SketchShape s = this.getSketchShapes().l.get(i);
			if (s instanceof SketchSpline) {
				SketchSpline spline = (SketchSpline) s;
				spline.setOffsetSizeCentre(val);
				spline.offset();

			}
		}

	}

	public void setCurrentShape(SketchShape newSketch) {
		getSketchShapes().currentShape = newSketch;

	}

	public void setEditing(boolean e) {
		sketchShapes.editing = e;
	}

	public void setLayerSelected(boolean selected) {
		this.layerSelected = selected;
	}

	public void setOnSketchPlane(SlicePlane slicePlane) {
		setOnSlicePlane(slicePlane);
	}

	/**
	 * @param onSlicePlane the onSlicePlane to set
	 */
	public void setOnSlicePlane(SlicePlane sp) {
		onSlicePlane = sp;
	}

	public void setRenderMode(int mode) {
		this.renderMode = mode;
	}

	public void setSketchGlobals(SketchGlobals sGlobals) {
		sketchGlobals = sGlobals;
	}

	public void setSketchTools(SketchTools sTools) {
		sketchTools = sTools;
	}


	public void setSlots(SliceSlots slots) {
		sketchShapes.setSlots(slots);
	}

	public Vec2D setVec2DpickBuffer(int col, SketchPoint selectedVec,
			SketchShape selectedShape, SlicePlane selectedVecPlane,
			boolean isSelectedVecOnOutline) {
		return sketchShapes.setVec2DpickBuffer(col, selectedVec, selectedShape,
				selectedVecPlane, isSelectedVecOnOutline);

	}

	public void toggleUnion() {
		SketchShape sketch = getSketchShapes().selectedShape;

		if (sketch.union == SketchShape.UNION_ADD)
			sketch.union = SketchShape.UNION_SUBTRACT;
		else
			sketch.union = SketchShape.UNION_ADD;

	}

	public Element toXML() {
		return sketchShapes.toXML();
	}

	public void unselect() {
		this.selected = false;
//		sketchShapes.unSelectAll();

	}

	public void unSelectAll() {
		sketchShapes.unSelectAll();
	}

	public void update() {
		//buildOutline();
		//sketchShapes.update();

		Vec2D pointOnPlan = new Vec2D(getSketchTools().mouseX,
				getSketchTools().mouseY);

		//#IF JAVA
		if (getOnSketchPlane() != null)
			pointOnPlan = GLOBAL.uiTools.getPointOnPlane(pointOnPlan,
					getOnSketchPlane().getPlane());
		//#ENDIF JAVA
		
		//OFFSETPATH TOOL
		// Update paths position to show under mouse
		//___________________________________________________________________________________________________

		if (getSketchTools().getCurrentTool() == SketchTools.DRAW_OFFSETPATH_TOOL) {
			boolean skip = false;

			if (getCurrentShape() != null
					&& getCurrentShape().getType() == SketchShape.OFFSET_SPLINE
					&& !getCurrentShape().getClosed() && !skip) {
				SketchSpline spline = (SketchSpline) getCurrentShape();
				spline.getCentrePath().getLast().set(pointOnPlan);
				spline.offset();
			}
		}

		if (getSketchTools().getCurrentTool() == SketchTools.DRAW_PATH_TOOL) {

			
			this.getSketchTools().drawPathToolState = SketchTools.DRAW_PATH_TOOL_STATE_NORMAL;

			
			if(getSketchTools().keyPressed
			&& getSketchTools().keyCode == PConstants.CONTROL) {
				
				this.getSketchTools().drawPathToolState = SketchTools.DRAW_PATH_TOOL_STATE_ADD;
				 
				
				if(pointOnPlan.distanceTo(sketchShapes.getClosestPathVertex(pointOnPlan)) < SETTINGS_SKETCH.select_dia){
					this.getSketchTools().drawPathToolState = SketchTools.DRAW_PATH_TOOL_STATE_REMOVE;

				}
				

		//if (addPointAlongPath(pointOnPlane.x, pointOnPlane.y))
			
			}
			
			
			if (getCurrentShape() != null
					&& getCurrentShape().getType() == SketchShape.TYPE_PATH
					&& !getCurrentShape().getClosed()) {
				SketchPath path = (SketchPath) getCurrentShape();
				path.getLast().set(pointOnPlan);
				
				
		
				
				
				//SHOULD WE SHOW THE CONNECT CURSOR?
				
				if(path.size() > 2 && path.getLast().distanceTo(path.getFirst()) < 10)
					this.getSketchTools().drawPathToolState = SketchTools.DRAW_PATH_TOOL_STATE_CONNECT;
				

				
			}
		}

		//close paths if another tool is selected
		//close shape if tool is not selected! 
		if (getSketchTools().getCurrentTool() != SketchTools.DRAW_OFFSETPATH_TOOL
				&& getCurrentShape() != null
				&& getCurrentShape().getType() == SketchShape.OFFSET_SPLINE
				&& !getCurrentShape().getClosed()) {
			SketchSpline spline = (SketchSpline) getCurrentShape();
			spline.getCentrePath().remove(spline.getCentrePath().getLast());
			getCurrentShape().setClosed(true);
			spline.offset();
		}

		if (getSketchTools().getCurrentTool() != SketchTools.DRAW_PATH_TOOL
				&& getCurrentShape() != null
				&& getCurrentShape().getType() == SketchShape.TYPE_PATH
				&& !getCurrentShape().getClosed()) {
			SketchPath path = (SketchPath) getCurrentShape();
			path.remove(path.getLast());
			getCurrentShape().setClosed(true);
			//spline.offset();
		}
		
		//highlight over point
		if (getSketchTools().getCurrentTool() == SketchTools.SELECT_TOOL){
		SketchPoint p = this.getOverSelectPoint(pointOnPlan.x, pointOnPlan.y);
		if (p != null) {
			p.isOver = true;
		}
		}

	}

	public void removeLegs() {
		this.sketchShapes.removeLegs();
	}

	public void importSVG(String path) {
		URI fileUri = null;
		try {
			fileUri = new URI("file://"+path);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    SVGDiagram diagram = SVGCache.getSVGUniverse().getDiagram(fileUri);
	    
	    
	    
	    //diagram.getRoot().getChild(1);
	    for(int i = 0; i < diagram.getRoot().getNumChildren();i++){
	    	
	    	SVGElement element  = diagram.getRoot().getChild(i);
	    //SVGElement element = diagram.getElement("shape_01");
	    if(element != null){
	    List vector = element.getPath(null);
	    com.kitfox.svg.Path pathSVG = (com.kitfox.svg.Path) vector.get(1);  
	    Shape shape = pathSVG.getShape();
	    this.sketchShapes.buildPathsFromAWTShape(shape);
	    }
	    }
	   // element.
	    
	  //  }
	    //LOGGER.info(diagram.get)
	    /*
	    SVGElement element = diagram.getElement(pathName);  
	    List vector = element.getPath(null);  
	    // get the AWT Shape  
	    // iterate over the shape using a path iterator discretizing with distance 0.001 units  
	    PathIterator pathIterator = shape.getPathIterator(null, 0.001d);  
	    float[] coords = new float[2];  
	    while (!pathIterator.isDone()) {  
	    pathIterator.currentSegment(coords);  
	    points.add(new Vector2f(coords[0], coords[1]));  
	    pathIterator.next();  
	    
	    } 
	    */ 		
	}

	public void setEditable(boolean editable) {
		sketchShapes.setEditable(editable);	
	}

	public void setRender3D(boolean b) {
		this.render3D = b;		
	}
	public boolean getRender3D() {
		return this.render3D;		
	}

	public void unselectShapes() {
		sketchShapes.unSelectAll();	
	}

	

	

}
