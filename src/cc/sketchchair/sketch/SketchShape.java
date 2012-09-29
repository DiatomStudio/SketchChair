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

import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import cc.sketchchair.core.LOGGER;
import cc.sketchchair.geometry.SlicePlane;

import nu.xom.Element;
import processing.core.PGraphics;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;
 
/**
 * A low level class that represents any 2d Shape in SketchChair. 
 * @author gregsaul
 *
 */
//#ENDIF JAVA

public abstract class SketchShape {

	public static final int TYPE_PATH = 2;
	public static final int TYPE_SPLINE = 3;
	public static final int OFFSET_SPLINE = 4;
	public static final int UNION_ADD = 0;
	public static final int UNION_SUBTRACT = 1;
	private ArrayList<Object> selectedNodes = new ArrayList<Object>();
	private int type = 0;
	private boolean destroy = false;
	boolean selected = false;
	boolean editable = true;
	public boolean isSelectedShape = false;
	int id = 0;
	public float lastMouseOverPercent = 0.0f;
	public Vec2D lastMouseOverVec;

	public Sketch parentSketch;
	private boolean closed = false;
	public int union = UNION_ADD;
	private boolean isConstructionLine = false;
	public float debugPercent = 0;
	protected boolean highlighted = false;

	SketchShape(Sketch parentSketch) {
		setParentSketch(parentSketch);

		if (getParentSketch() != null
				&& getParentSketch().getSketchGlobals() != null)
			this.id = getParentSketch().getSketchGlobals().sketch_id++;
	}

	public abstract void add(SketchPoint point);

	public abstract void build();

	public abstract SketchShape clone();

	public abstract SketchShape copy(Sketch parentSketch);

	public int countSelectedNodes() {
		return getSelectedNodes().size();
	}

	public abstract void destroy();

	public abstract void flipHorizontal(toxi.geom.Vec3D centre);

	public SketchPoint get(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getClosed() {
		return closed;
	}

	public abstract float getClosestPercent(float mouseX, float mouseY);

	public abstract SketchPoint getClosestPoint(SketchPoint pointOnPlan);

	public abstract SketchPoint getClosestPoint(Vec2D pointOnPlan);

	public abstract Vec2D getClosestPointAlongPath(float x, float y);

	public int getId() {
		return this.id;
	}

	public abstract SketchPoint getLast();

	public float getlength() {
		// TODO Auto-generated method stub
		return 0;
	}

	public abstract int getLength();

	public float getlengthPerPercent() {
		// TODO Auto-generated method stub
		return 0;
	}

	public abstract GeneralPath getOutlineGeneralPath();

	public abstract SketchShape getOverShape(float x, float y);

	public Sketch getParentSketch() {
		return parentSketch;
	}

	public abstract SketchPath getPath();

	public abstract Vec2D getPerpendicular(float i);

	public abstract Vec2D getPos(float i);

	protected ArrayList<Object> getSelectedNodes() {
		return this.selectedNodes;
	}

	public abstract SketchPoint getSketchPointpickBuffer(int col);

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	public abstract SketchPoint getVec2DpickBuffer(int col);

	public abstract void insertPoint(SketchPoint closestPoint);

	public boolean isConstructionLine() {
		return this.isConstructionLine;
	}

	/**
	 * @return the destroy
	 */
	public boolean isDestroying() {
		return destroy;
	}

	public abstract boolean isPointInside(Vec2D p);

	public abstract void mouseDragged(float mouseX, float mouseY);

	public abstract void mouseReleased(float mouseX, float mouseY);

	public abstract void movePoint(SketchPoint selectedVec, Vec2D planePoint);

	public abstract void offset();

	public abstract void optimize();

	public boolean overlaps(SketchShape curSketch) {
		SketchPath path1 = this.getPath();
		SketchPath path2 = curSketch.getPath();
		return path1.intersects(path2);
	}

	public abstract void removeVertex(SketchPoint v);

	public abstract void render(PGraphics g);

	public abstract void renderPickBuffer(PGraphics g);

	public abstract void renderSilhouette(PGraphics g);

	public abstract void replace(SketchShape objClone);

	public abstract void scale(float scale, toxi.geom.Vec3D centre);

	public abstract void select();

	public abstract void selectNodes(float x, float y);

	public void setClosed(boolean flag) {
		closed = flag;
	}

	/**
	 * @param destroy the destroy to set
	 */
	public void setDestroy(boolean destroy) {
		this.destroy = destroy;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setIsContructionLine(boolean construction) {
		this.isConstructionLine = construction;
	}

	public void setParentSketch(Sketch sketch) {
		parentSketch = sketch;
	}

	/**
	 * @param selectedNodes the selectedNodes to set
	 */
	public void setSelectedNodes(ArrayList<Object> selectedNodes) {
		this.selectedNodes = selectedNodes;
	}

	public SketchPoint setSketchPointpickBuffer(int col,
			SketchPoint selectedVec, SketchShape selectedShape,
			SlicePlane selectedVecPlane, boolean isSelectedVecOnOutline) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	public abstract SketchPoint setVec2DpickBuffer(int col,
			SketchPoint selectedVec, SketchShape selectedShape,
			SlicePlane selectedVecPlane, boolean isSelectedVecOnOutline);

	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public abstract Element toXML();

	public abstract void unselect();

	public final void unselectNodes() {
		this.getSelectedNodes().clear();
	}

	public void setEditable(boolean editable_in) {
		this.editable = editable_in;	
	}

	public void highlight() {
		this.highlighted = true;		
	}

}
