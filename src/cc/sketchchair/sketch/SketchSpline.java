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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.UndoAction;
import cc.sketchchair.functions.functions;
import cc.sketchchair.geometry.SlicePlane;

import nu.xom.Attribute;
import nu.xom.Element;

import processing.core.PGraphics;

import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

/**
 * A SketchSpline is a SketchPath with a offset.
 * @author gregsaul
 *
 */
//#ENDIF JAVA

public class SketchSpline extends SketchShape {

	SketchPath path;
	private SketchPath centrePath;

	public static final int TYPE_LEG = 43;
	public static final int CAP_ROUND = 1;
	public static final int CAP_BUTT = 2;
	public static final int CAP_SQUARE = 3;
	public static final int CAP_PARRALEL = 4;
	public static final int CAP_LEG = 5;

	public static final int JOIN_BEVEL = 1;
	public static final int JOIN_MITER = 2;
	public static final int JOIN_ROUND = 3;
	
	public static int OFFSET_LEFT = 0;
	public static int OFFSET_RIGHT = 1;
	public static int OFFSET_BOTH = 2;
	

	int capType = CAP_ROUND;
	int joinType = JOIN_BEVEL;

	ArrayList<SketchPoint> outineLeft = new ArrayList<SketchPoint>();
	ArrayList<SketchPoint> outineRight = new ArrayList<SketchPoint>();

	private Map<Integer, SketchPoint> outlineOffset = new Hashtable<Integer, SketchPoint>();
	private Map<Integer, Float> centreOffset = new Hashtable<Integer, Float>();

	int id = 0;

	boolean selected = true;
	boolean autoSmooth = true;

	boolean isOptimized;
	float pointDist = 5;// SETTINGS_SKETCH.dist_between_points; // distance
						// between
						// points
	// on the optimized line
	private float offsetSize = 20;// SETTINGS_SKETCH.offset_size;
	//public float offsetSizeEnd = 1;

	SliceSlots slots = new SliceSlots();
	boolean slots_on_inside = true;

	int offsetType = 2;
	public boolean isBuilt = false;
	private Object getSelectedNodes;
	private boolean legClickedOn;
	private boolean cacheLength;
	private int cachedLength;



	public SketchSpline(Sketch parentSketch) {
		super(parentSketch);

		path = new SketchPath(getParentSketch());
		centrePath = new SketchPath(getParentSketch());
		this.id = getParentSketch().sketch_id++;
		this.setType(SketchShape.TYPE_SPLINE);
		path.editable = false;
		path.setClosed(true);
		this.path.setParentSketch(this.getParentSketch());

	}

	public SketchSpline(Sketch parentSketch, Element element) {
		super(parentSketch);
		setParentSketch(parentSketch);
		path = new SketchPath(getParentSketch());
		centrePath = new SketchPath(getParentSketch());

		// wrong type
		if (!element.getLocalName().equals("SketchSpline"))
			return;

		if (element.getAttributeValue("id") != null) {
			this.setId(Integer.valueOf(element.getAttributeValue("id")));
		}

		if (element.getAttributeValue("outlineId") != null) {
			this.path.setId(Integer.valueOf(element
					.getAttributeValue("outlineId")));
		}

		if (element.getAttributeValue("centreId") != null) {
			this.getCentrePath().setId(
					Integer.valueOf(element.getAttributeValue("centreId")));
		}

		if (element.getAttributeValue("offsetSize") != null) {
			this.setOffsetSize(Float.valueOf(element
					.getAttributeValue("offsetSize")));
		}

		if (element.getAttributeValue("splineType") != null) {
			this.setType(Integer.valueOf(element
					.getAttributeValue("splineType")));
		}

		if (element.getAttributeValue("endCap") != null) {
			this.setCap(Integer.valueOf(element.getAttributeValue("endCap")));
		}

		if (element.getAttributeValue("joinType") != null) {
			this.setJoinType(Integer.valueOf(element
					.getAttributeValue("joinType")));
		}

		//this is from a legacy version also add a cap style to say it's a leg
		if (this.getType() == 0) {
			if (element.getAttributeValue("offsetSizeEnd") != null) {
			this.getCentreOffset().put(1,
					Float.valueOf(element.getAttributeValue("offsetSizeEnd")));
			}
			
			this.capType = SketchSpline.CAP_LEG;
			this.setType(TYPE_LEG);
		}

		if (element.getAttributeValue("isConstructionLine") != null) {
			this.setIsContructionLine(true);
		}

		if (element.getAttributeValue("union") != null) {
			this.union = Integer.valueOf(element.getAttributeValue("union"));
		}

		// for(int i = element.getChildCount()-1 ; i >= 0 ; i--){
		for (int i = 0; i < element.getChildCount(); i++) {

			Element child = (Element) element.getChild(i);

			if (child != null && child.getLocalName().equals("SketchPoint"))
				getCentrePath().add(new SketchPoint(child));

			if (child != null
					&& child.getLocalName().equals("SketchSplineCentrePath")) {
				// for(int j = child.getChildCount()-1 ; j >= 0 ; j--){
				for (int j = 0; j < child.getChildCount(); j++) {

					Element child2 = (Element) child.getChild(j);

					if (child2 != null
							&& child2.getLocalName().equals("SketchPoint"))
						getCentrePath().add(new SketchPoint(child2));

				}

			}

			if (child != null
					&& child.getLocalName().equals("SketchSplineOffsets")) {
				// for(int j = child.getChildCount()-1 ; j >= 0 ; j--){
				for (int j = 0; j < child.getChildCount(); j++) {

					Element child2 = (Element) child.getChild(j);
					if (child2 != null
							&& child2.getLocalName().equals(
									"SketchSplineOffset")) {

						if (child2.getAttributeValue("linked_id") != null
								&& child2.getAttributeValue("x_offset") != null
								&& child2.getAttributeValue("y_offset") != null) {
							int index = Integer.valueOf(child2
									.getAttributeValue("linked_id"));
							float x_offset = Float.valueOf(child2
									.getAttributeValue("x_offset"));
							float y_offset = Float.valueOf(child2
									.getAttributeValue("y_offset"));

							outlineOffset.put(index, new SketchPoint(x_offset,
									y_offset));
							// outineOffset.put(1, new Vec2D(1,1));

						}

					}
				}
			}

			if (child != null
					&& child.getLocalName().equals("SketchSplinePathOffsets")) {

				// for(int j = child.getChildCount()-1 ; j >= 0 ; j--){
				for (int j = 0; j < child.getChildCount(); j++) {

					Element child2 = (Element) child.getChild(j);
					if (child2 != null
							&& child2.getLocalName().equals(
									"SketchSplinePathOffset")) {

						if (child2.getAttributeValue("linked_index") != null
								&& child2.getAttributeValue("offset") != null) {
							int index = Integer.valueOf(child2
									.getAttributeValue("linked_index"));
							float offset = Float.valueOf(child2
									.getAttributeValue("offset"));

							this.getCentreOffset().put(index, offset);
							// outineOffset.put(1, new Vec2D(1,1));

						}

					}
				}
			}

		}
		this.setClosed(true);

		path.editable = false;
		path.setClosed(true);
	}

	public SketchSpline(Sketch parentSketch, int offsetType) {
		super(parentSketch);
		setParentSketch(parentSketch);

		path = new SketchPath(getParentSketch());
		centrePath = new SketchPath(getParentSketch());
		this.offsetType = offsetType;
		this.id = getParentSketch().sketch_id++;
		path.editable = false;
		path.setClosed(true);
		this.path.setParentSketch(this.getParentSketch());

	}

	public void add(SketchPoint newVec) {

		if (getCentrePath().size() < 2
				|| this.getLast().distanceTo(newVec) > 10) {
			getCentrePath().add(newVec);

			if (SETTINGS_SKETCH.dynamic_offset)
				this.offset();

		}

	}

	void addDynamicOffset(Vec2D newVec) {

		if (getCentrePath().size() == 3)
			this.offset();

		if (getCentrePath().size() > 3) {

			float tempOffset = this.getOffsetSize();
			float lenCurVec = this.getLenTo(newVec);
			if (lenCurVec <= this.getOffsetSize()) {
				tempOffset = (getOffsetSize() * ((float) Math
						.sqrt(1 - ((1 - (lenCurVec / getOffsetSize())) * (1 - (lenCurVec / getOffsetSize()))))));
			} else {
				tempOffset = this.getOffsetSize();

			}

			if (this.offsetType == SketchSpline.OFFSET_LEFT) {
				this.outineLeft
						.add((SketchPoint) getPerp(
								(float) (Math.PI / 2),
								tempOffset,
								(Vec2D) getCentrePath().get(
										getCentrePath().size() - 1),
								(Vec2D) getCentrePath().get(
										getCentrePath().size() - 3)));
				this.outineRight.add((SketchPoint) newVec);
			}

			if (this.offsetType == SketchSpline.OFFSET_RIGHT) {
				this.outineLeft.add((SketchPoint) newVec);
				this.outineRight
						.add((SketchPoint) getPerp(
								(float) (-Math.PI / 2),
								tempOffset,
								(Vec2D) getCentrePath().get(
										getCentrePath().size() - 1),
								(Vec2D) getCentrePath().get(
										getCentrePath().size() - 3)));
			}

			if (this.offsetType == SketchSpline.OFFSET_BOTH) {
				this.outineLeft
						.add((SketchPoint) getPerp(
								(float) (Math.PI / 2),
								tempOffset,
								(Vec2D) getCentrePath().get(
										getCentrePath().size() - 1),
								(Vec2D) getCentrePath().get(
										getCentrePath().size() - 3)));
				this.outineRight
						.add((SketchPoint) getPerp(
								(float) (-Math.PI / 2),
								tempOffset,
								(Vec2D) getCentrePath().get(
										getCentrePath().size() - 1),
								(Vec2D) getCentrePath().get(
										getCentrePath().size() - 3)));
			}

		}

	}

	void addOffset(SketchPoint newVec) {
		toxi.geom.Vec3D v = new toxi.geom.Vec3D(newVec.x, newVec.y, 0);
		this.outineLeft.add((SketchPoint) newVec);
	}

	private void applyOutlineOffset() {
		// this.optimize();
		if (getCentrePath().size() == 2)
			this.path.editable = true;
		else
			this.path.editable = false;

		if (getCentrePath().size() > 2)
			return;

		for (int i = 0; i < getCentrePath().size(); i++) {
			SketchPoint curVec = (SketchPoint) getCentrePath().get(i);

			if (this.outlineOffset.containsKey(i) && this.path.size() >= i) {
				if (this.path.size() > i) {
					Vec2D outline = (Vec2D) this.path.get(i);
					Vec2D offset = this.outlineOffset.get(i);
					outline.x = curVec.sub(offset).x;
					outline.y = curVec.sub(offset).y;
				}
				// this.outineLeft.set(i, outline);
			}

		}

		for (int i = 0; i < getCentrePath().size(); i++) {
			Vec2D curVec = (Vec2D) getCentrePath().get(i);
			int i2 = ((getCentrePath().size() * 2) - i - 1);
			if (this.outlineOffset.containsKey(i2) && this.path.size() >= i2) {
				if (this.path.size() >= i2) {
					Vec2D outline = (Vec2D) this.path.get(i2);
					Vec2D offset = this.outlineOffset.get(i2);
					outline.x = curVec.sub(offset).x;
					outline.y = curVec.sub(offset).y;
				}
				// this.outineLeft.set(i, outline);
			}

		}

	}

	public void build() {
		this.offset();
	}

	private void buildPath() {

		this.path.reset();

		for (int i = 0; i < this.getCombinedSize(); i++) {
			SketchPoint curVec = (SketchPoint) this.getCombined(i);
			this.path.add(curVec);
		}
	}

	public SketchSpline clone() {
		SketchSpline newSpline = new SketchSpline(getParentSketch());
		newSpline.setType(this.getType());
		newSpline.setCap(this.getCap());
		newSpline.setJoinType(this.getJoinType());

		if (this.getCentrePath() != null)
			newSpline.setCentrePath(this.getCentrePath().clone());

		for (int i = 0; i < this.outineLeft.size(); i++) {
			Vec2D curVec = this.outineLeft.get(i);
			newSpline.outineLeft.add(new SketchPoint(curVec.x, curVec.y));
		}

		for (int i = 0; i < this.outineRight.size(); i++) {
			Vec2D curVec = this.outineRight.get(i);
			newSpline.outineRight.add(new SketchPoint(curVec.x, curVec.y));
		}

		for (int i = 0; i < this.getCentrePath().size(); i++) {
			if (this.getCentreOffset().get(i) != null)
				newSpline.getCentreOffset().put(i,
						this.getCentreOffset().get(i));
		}

		newSpline.slots = this.slots.clone();

		newSpline.setOffsetSize(this.getOffsetSize());
		newSpline.path = this.path.clone();
		newSpline.isBuilt = this.isBuilt;
		newSpline.offsetType = this.offsetType;
		newSpline.path.editable = this.path.editable;
		newSpline.setClosed(this.getClosed());
		//newSpline.offset();
		return newSpline;

	}

	public SketchSpline copy(Sketch parentSketch) {
		SketchSpline newSketchSpline = new SketchSpline(parentSketch);

		newSketchSpline.setId(this.getId());
		newSketchSpline.path.setId(this.path.getId());
		newSketchSpline.getCentrePath().setId(this.getCentrePath().getId());

		newSketchSpline.offsetSize = this.offsetSize;
		newSketchSpline.setType(this.getType());
		newSketchSpline.setCap(this.getCap());
		newSketchSpline.setJoinType(this.getJoinType());

		for (int i = 0; i < getCentrePath().size(); i++) {
			SketchPoint skPoint = (SketchPoint) getCentrePath().get(i);
			SketchPoint skPointCopy = skPoint.clone();
			newSketchSpline.getCentrePath().add(skPointCopy);
			if (this.getCentreOffset().containsKey(i)) {
				newSketchSpline.getCentreOffset().put(i,
						getCentreOffset().get(i));
			}

		}

		newSketchSpline.setClosed(this.getClosed());

		return newSketchSpline;
	}

	public int countSelectedNodes() {
		return getSelectedNodes().size() + this.path.countSelectedNodes();
	}

	public void destroy() {
		this.setDestroy(true);

		if (this.getCentrePath() != null)
			this.getCentrePath().setDestroy(true);
		//this.setCentrePath(null);
	}

	public void flipHorizontal(toxi.geom.Vec3D centre) {
		this.getCentrePath().flipHorizontal(centre);
		this.path.flipHorizontal(centre);
	}

	public int getCap() {
		return capType;
	}


	public Map<Integer, Float> getCentreOffset() {
		return centreOffset;
	}

	public Vec2D getCentreOfMass() {
		long x = 0;
		long y = 0;

		for (int i = 0; i < this.getCombinedSize(); i++) {
			Vec2D v = (Vec2D) this.getCombined(i);
			x += v.x;
			y += v.y;
		}
		return new Vec2D(x / this.getCombinedSize(), y / this.getCombinedSize());
	}

	public SketchPath getCentrePath() {
		return this.centrePath;
	}

	public float getClosestPercent(float mouseX, float mouseY) {
		return this.getCentrePath().getClosestPercent(mouseX, mouseY);
		//return this.path.getClosestPercent(mouseX, mouseY);
	}

	public SketchPoint getClosestPoint(SketchPoint pointOnPlan) {
		return this.getCentrePath().getClosestPoint(pointOnPlan);
	}

	public SketchPoint getClosestPoint(Vec2D pointOnPlan) {
		return this.getCentrePath().getClosestPoint(pointOnPlan);
	}

	public Vec2D getClosestPointAlongPath(float x, float y) {
		Vec2D mPos = new Vec2D(x, y);
		Vec2D centreP = this.getCentrePath().getClosestPointAlongPath(x, y);
		Vec2D outlineP = this.getPath().getClosestPointAlongPath(x, y);

		if (mPos == null || centreP == null || outlineP == null)
			return null;

		if (centreP.distanceTo(mPos) < outlineP.distanceTo(mPos))
			return centreP;
		else
			return outlineP;

	}

	int getColor(int id) {
		return -(id + 2);
	}

	public SketchPoint getCombined(int i) {
		if (i < this.outineRight.size())
			return this.outineRight.get(i);
		else
			return this.outineLeft.get(this.outineLeft.size()
					- (i - this.outineRight.size()) - 1);
	}

	public int getCombinedSize() {
		return this.outineRight.size() + this.outineLeft.size();
	}

	private float getDistBetween(Vec2D first, Vec2D second) {
		float length = 0;

		if (first == second)
			return length;

		Vec2D startVec = null;

		for (int i = 0; i < getCentrePath().size() - 1; i++) {
			Vec2D curVec = (Vec2D) getCentrePath().get(i);
			Vec2D nextVec = (Vec2D) getCentrePath().get(i + 1);

			if (startVec == null && (curVec == first || curVec == second))
				startVec = curVec;

			if (startVec != null)
				length += curVec.distanceTo(nextVec);

			if (startVec != null && (nextVec == first || nextVec == second))
				return length;

		}
		return length;
	}

	public Vec2D getFirst() {

		if (getCentrePath().size() > 0)
			return (Vec2D) getCentrePath().get(0);
		else
			return null;
	}

	private int getIndex(Vec2D startVec) {
		for (int i = 0; i < getCentrePath().size(); i++) {
			Vec2D vec = (Vec2D) getCentrePath().get(i);
			if (startVec == vec)
				return i;
		}

		return -1;
	}

	private int getJoinType() {
		return this.joinType;
	}

	public SketchPoint getLast() {

		if (getCentrePath().size() > 0)
			return (SketchPoint) getCentrePath()
					.get(getCentrePath().size() - 1);
		else
			return null;
	}

	public float getlength() {
		float length = 0;

		for (int i = 0; i < getCentrePath().size() - 1; i++) {
			Vec2D curVec = (Vec2D) getCentrePath().get(i);
			Vec2D nextVec = (Vec2D) getCentrePath().get(i + 1);

			if (curVec != null && nextVec != null)
				length += curVec.distanceTo(nextVec);
		}

		return length;
	}

	public int getLength() {
		return getCentrePath().size();
	}

	public float getLenTo(Vec2D curVecC) {
		float length = 0;

		if (curVecC == null)
			return -1;

		for (int i = 0; i < getCentrePath().size() - 1; i++) {
			Vec2D curVec = (Vec2D) getCentrePath().get(i);
			Vec2D nextVec = (Vec2D) getCentrePath().get(i + 1);

			if (curVecC != nextVec && curVecC != curVec && nextVec != null)
				length += curVec.distanceTo(nextVec);
			else
				return length;

		}
		return -1;

	}

	private Vec2D getNearestVec(Vec2D selectedNode) {
		Vec2D foundVec = null;
		float dist = 0;

		for (int i = 0; i < getCentrePath().size(); i++) {
			Vec2D vec = (Vec2D) getCentrePath().get(i);
			if (vec.distanceTo(selectedNode) < dist || foundVec == null) {
				dist = vec.distanceTo(selectedNode);
				foundVec = vec;
			}
		}

		return foundVec;
	}

	private int getNearestVecIndex(Vec2D selectedNode) {
		int index = -1;
		float dist = 0;

		for (int i = 0; i < getCentrePath().size(); i++) {
			Vec2D vec = (Vec2D) getCentrePath().get(i);
			if (vec.distanceTo(selectedNode) < dist || index == -1) {
				dist = vec.distanceTo(selectedNode);
				index = i;
			}
		}

		return index;
	}

	/**
	 * @return the offsetSize
	 */
	public float getOffsetSize() {
		return offsetSize;
	}

	public float getOffsetSize(float percent) {

		int startNodeIndex = this.getCentrePath().getPosIndex(percent);
		float startNodePercent = this.getCentrePath().getPercent(
				this.getCentrePath().get(startNodeIndex));
		float endNodePercent = this.getCentrePath().getPercent(
				this.getCentrePath().get(startNodeIndex + 1));
		float currentSegPercent = (endNodePercent - startNodePercent);
		float betweenPercent = (percent - startNodePercent) / currentSegPercent;

		float thisOffset = this.getOffsetSize();
		float nextOffset = this.getOffsetSize();

		if (this.getCentreOffset().containsKey(startNodeIndex))
			thisOffset = getCentreOffset().get(startNodeIndex);

		if (this.getCentreOffset().containsKey(startNodeIndex + 1))
			nextOffset = getCentreOffset().get(startNodeIndex + 1);

		return (thisOffset * (1 - betweenPercent))
				+ (nextOffset * (betweenPercent));

	}

	public GeneralPath getOutlineGeneralPath() {

		return this.path.getOutlineGeneralPath();

	}

	public SketchShape getOverShape(float x, float y) {
		Vec2D closePoint = this.path.getClosestPointAlongPath(x, y);
		Vec2D closePointCentre = this.getCentrePath().getClosestPointAlongPath(
				x, y);

		float distToClosePoint = 0;
		float distToClosePointCentre = 0;

		if (closePoint != null)
			distToClosePoint = closePoint.distanceTo(new Vec2D(x, y));

		if (closePointCentre != null)
			distToClosePointCentre = closePointCentre
					.distanceTo(new Vec2D(x, y));

		if (closePoint != null && (distToClosePoint <= distToClosePointCentre)
				&& distToClosePoint < SETTINGS_SKETCH.SELECT_EDGE_DIST) {
			this.path.lastMouseOverVec = closePoint;
			this.path.lastMouseOverPercent = this.path.getClosestPercent(x, y);
			return this.path;
		}

		closePoint = this.getCentrePath().getClosestPointAlongPath(x, y);

		if (closePoint != null && (distToClosePointCentre < distToClosePoint)
				&& distToClosePointCentre < SETTINGS_SKETCH.SELECT_EDGE_DIST) {
			this.lastMouseOverVec = closePoint;
			this.lastMouseOverPercent = this.getCentrePath().getClosestPercent(
					x, y);
			return this;
		}

		/*
		 * Vec2D closePointC = this.centrePath.getClosestPointAlongPath(x,y);
		 * if(closePointC != null && closePointC.distanceTo(new Vec2D(x,y)) <
		 * SETTINGS_SKETCH.SELECT_EDGE_DIST){ this.centrePath.lastMouseOverVec =
		 * closePointC; this.centrePath.lastMouseOverPercent =
		 * this.centrePath.getClosestPercent(x,y); return this.centrePath; }
		 */

		return null;
	}

	public SketchPath getPath() {
		return this.path;

	}

	SketchPoint getPerp(float angle, float offsetDelta, Vec2D vec1, Vec2D vec2) {

		Vec2D curVec2 = vec1;// (Vec2D) centrePath.get(centrePath.size() - 1);
		Vec2D prevVec = vec2;// (Vec2D) centrePath.get(centrePath.size() - 3);
		Vec2D curAnNext = curVec2.sub(prevVec);
		curAnNext.normalize();
		Vec2D newAn = curAnNext.getRotated(angle);
		newAn.normalize();
		newAn.scaleSelf(offsetDelta);
		newAn.addSelf(curVec2);
		return new SketchPoint(newAn.x, newAn.y);

	}

	public Vec2D getPerpendicular(float percent) {
		return this.getCentrePath().getPerpendicular(percent);


	}

	public Vec2D getPos(float percent) {
		return getCentrePath().getPos(percent);
		
	}

	protected ArrayList<Object> getSelectedNodes() {
		return this.getCentrePath().getSelectedNodes();
	}

	public SketchPoint getSketchPointpickBuffer(int col) {
		// TODO Auto-generated method stub
		return null;
	}

	public SketchPoint getVec2DpickBuffer(int col) {
		for (int i = 0; i < getCentrePath().size(); i++) {
			SketchPoint curVec = (SketchPoint) getCentrePath().get(i);
			if (col == getColor(i + (this.id * 100)))
				return curVec;
		}

		for (int i = 0; i < this.path.size(); i++) {
			SketchPoint curVec = (SketchPoint) this.path.get(i);
		
			if (col == getColor(getCentrePath().size() + i + (this.id * 100))) {
				return curVec;
			}
		}

		return null;
	}

	public void insertPoint(SketchPoint closestPoint) {
		this.getCentrePath().insertPoint(closestPoint);
	}

	public boolean isPointInside(Vec2D p) {
		return this.path.isPointInside(p);
	}

	public void mouseDragged(float mouseX, float mouseY) {

		Vec2D pointOnPlane = new Vec2D(mouseX, mouseY);

		if (this.path.editable)
			this.path.mouseDragged(mouseX, mouseY);

		if (getParentSketch().getSketchTools().getCurrentTool() == SketchTools.SELECT_TOOL) {
			for (int i = 0; i < this.getSelectedNodes().size(); i++) {
				Vec2D v = (Vec2D) this.getSelectedNodes().get(i);

				if (this.getType() == SketchShape.TYPE_SPLINE)
					this.movePointFalloff(v, pointOnPlane);

				if (this.getType() == SketchShape.OFFSET_SPLINE)
					this.movePoint(v, pointOnPlane);

				if (getCentrePath().size() == 2)
					this.legClickedOn = true;

			}

			for (int i = 0; i < this.getSelectedNodes().size(); i++) {
				Object v = (Object) this.getSelectedNodes().get(i);

				if (v instanceof SketchPoint) {
					this.movePoint((SketchPoint) v, pointOnPlane);
				} else if (v instanceof Vec2D)
					this.movePoint((Vec2D) v, pointOnPlane);

				//TODO: this could be a smooth tool!
				//if (getCentrePath().size() != 2)
				//	this.optimize();

			}

		}

		this.getCentrePath().mouseDragged(mouseX, mouseY);

		if (this.getSelectedNodes().size() > 0
				|| this.getCentrePath().getSelectedNodes().size() > 0)
			this.offset();

	}

	public void mouseReleased(float mouseX, float mouseY) {
		this.optimize();
		this.legClickedOn = false;

		//remove stray points
		if(this.centrePath.size() <= 1){
			this.destroy();
		LOGGER.debug("destroy shape not enough points");	
		}
	}

	public void movePoint(SketchPoint selectedVec, Vec2D planePoint) {
		// TODO Auto-generated method stub
		if (selectedVec.containsBezier()) {

			//Vec2D delta = selectedVec.sub(planePoint);
			//System.out.println(pointOnPlane);
			//LOGGER.info("here 1 "+ planePoint);
			//LOGGER.info("here 2 "+ selectedVec);

			//selectedVec.controlPoint1.subSelf(delta);
			//selectedVec.controlPoint2.subSelf(delta);

		}

		//selectedVec.x = planePoint.x;
		//selectedVec.y = planePoint.y;

	}

	public void movePoint(Vec2D v, Vec2D planePoint) {

		//if (getCentrePath().size() > 2)
		//return;

		//v.set(planePoint);

		/*
		// System.out.print("here");
		if (getCentrePath().contains(selectedVec)) {
			selectedVec.x = planePoint.x;
			selectedVec.y = planePoint.y;

			this.offset();
		}

		if (this.path.contains(selectedVec)) {
			int index = this.path.indexOf(selectedVec);
			Vec2D outlineP = (Vec2D) this.path.get(index);
			Vec2D centreP = null;

			if (index < getCentrePath().size())
				centreP = (Vec2D) getCentrePath().get(index);
			else
				centreP = (Vec2D) getCentrePath().get(
						(getCentrePath().size() * 2) - index - 1);

			if (!outineOffset.containsKey(index))
				outineOffset.put(index, new Vec2D());

			Vec2D offset = outineOffset.get(index);

			offset.x = centreP.x - planePoint.x;
			offset.y = centreP.y - planePoint.y;
			applyOutlineOffset();

		}
		*/

	}

	private void movePointFalloff(Vec2D selectVec, Vec2D pointOnPlane) {

		if (this.getCentrePath().size() <= 2)
			return;

		Vec2D diff = pointOnPlane.sub(selectVec);
		Vec2D startVec = null;
		Vec2D endVec = null;
		Vec2D lastVec = null;

		int pointIndex = this.getIndex(selectVec);

		if (pointIndex == 0) {
			// if we have one of the end points just drag them
			selectVec.set(pointOnPlane);

			if (startVec == null)
				startVec = selectVec;

			if (startVec != null && getCentrePath().size() > 3)
				endVec = (Vec2D) getCentrePath().get(3);
			else
				endVec = (Vec2D) getCentrePath()
						.get(getCentrePath().size() - 1);

		} else if (pointIndex == getCentrePath().size() - 1) {

			// if we have one of the end point just drag them
			selectVec.set(pointOnPlane);

			if (getCentrePath().size() > 1) {
				Vec2D v = (Vec2D) getCentrePath().get(
						getCentrePath().size() - 2);

				v.x += diff.scale(.2f).x;
				v.y += diff.scale(.2f).y;
			}

			if (startVec == null && getCentrePath().size() > 3)
				startVec = (Vec2D) getCentrePath().get(
						getCentrePath().size() - 3);
			else
				startVec = (Vec2D) getCentrePath().get(0);

			if (startVec != null)
				endVec = selectVec;

		} else {

			for (int i = 0; i < getCentrePath().size(); i++) {
				Vec2D v = (Vec2D) getCentrePath().get(i);

				// float dist = v.distanceTo(selectVec);

				float dist = this.getDistBetween(v, selectVec);
				float fieldDia = SETTINGS_SKETCH.splineMoveFalloff * (1 / getParentSketch().getZOOM());
				float delta = (fieldDia - dist) / fieldDia;
				
				// if (delta < 0)
				// delta = 0;
				// System.out.println(dist);
				
				if (dist < fieldDia) {

					float scale = (float) Math.sin(((Math.PI / 2) * delta));
					Vec2D shapedDiff = diff.scale(scale);

					/*
					if (i != 0 && i != getCentrePath().size() - 1
							|| delta > .99f) {
							*/
						// if(true){
						v.x += shapedDiff.x;
						v.y += shapedDiff.y;

						// System.out.println("d"+dist);

						if (startVec == null)
							startVec = v;

						if (startVec != null)
							endVec = v;

					//}

				}

			}
		}
	
		optimizeRange(startVec, endVec);
		// optimize();
	}

	/**
	 * Apply outline offset
	 * 
	 */

	public void offset() {
		if (getCentrePath().size() < 2)
			return;

		float offsetSize1 = 0;
		float offsetSize2 = 0;

		if (this.getCentreOffset().get(1) != null)
			offsetSize1 = this.getCentreOffset().get(1);

		//if (offsetSize1 == 0)
		//SketchSpline.	offsetSize1 = this.offsetSizeEnd;

		if (this.getCentreOffset().get(0) != null)
			offsetSize2 = this.getCentreOffset().get(0);

		if (offsetSize2 == 0)
			offsetSize2 = this.getOffsetSize();

		
		//if (getCentrePath().size() < 3 && this.getType() != SketchSpline.OFFSET_SPLINE)
		//	return;

		this.outineLeft = new ArrayList<SketchPoint>();
		this.outineRight = new ArrayList<SketchPoint>();

		float tempOffset = getOffsetSize();
		float tempOffsetNext = tempOffset;

		/**
		 * OFFSET the path
		 * 
		 * |:| :| |:
		 * |:| :| |: 
		 * |:| :| |:
		 * 
		 */
		//Go through each point on the path and work out the angle of the line to and from it.
		for (int i = 0; i < getCentrePath().size(); i++) {
			Vec2D curveNormal1 = null;
			Vec2D curveNormal2 = null;
			
			Vec2D curveNormalPrev1 = null;
			Vec2D curveNormalPrev2 = null;

			Vec2D prevVec = null;
			Vec2D nextVec = null;
			Vec2D curVec = null;
			SketchPoint curPoint = null;
			SketchPoint nextPoint = null;
			SketchPoint prevPoint = null;

			curVec = (Vec2D) getCentrePath().get(i);
			curPoint = getCentrePath().get(i);

			if (curVec == null)
				break;

			if (i == 0 && i < getCentrePath().size()) {
				nextVec = (Vec2D) getCentrePath().get(i + 1);
				nextPoint = getCentrePath().get(i + 1);
				curveNormal2 = (Vec2D) nextVec.sub(curVec).normalize();
			} else if (i > getCentrePath().size() - 2) {
				prevVec = (Vec2D) getCentrePath().get(i - 1);
				prevPoint = getCentrePath().get(i - 1);
				curveNormal1 = curVec.sub((Vec2D) prevVec).normalize();
			} else {

				prevVec = (Vec2D) getCentrePath().get(i - 1);
				nextVec = (Vec2D) getCentrePath().get(i + 1);
				nextPoint = getCentrePath().get(i + 1);
				prevPoint = getCentrePath().get(i - 1);

				if (nextVec == null)
					nextVec = curVec;

				Vec2D curAnPrev = curVec.sub(prevVec);
				Vec2D curAnNext = nextVec.sub(curVec);

				curAnPrev.normalize();
				curAnNext.normalize();

				// curAnPrev.addSelf(curAnNext);

				// curAnPrev.scaleSelf(0.5f);
				curveNormal1 = curAnPrev;
				curveNormal2 = curAnNext;

			}
			

			tempOffset = this.getOffsetSize();
			tempOffsetNext = this.getOffsetSize();

			if (getCentreOffset().containsKey(i))
				tempOffset = this.getCentreOffset().get(i);

			if (nextPoint != null && getCentreOffset().containsKey(i + 1)) {
				tempOffsetNext = this.getCentreOffset().get(i + 1);
			}

			if (prevPoint != null && prevPoint.controlPoint2 != null
					&& !prevPoint.controlPoint2.equals(prevPoint)) {
				prevVec = prevPoint.controlPoint2;
				curveNormal1 = curVec.sub((Vec2D) prevVec).normalize();
			}

			if (nextPoint != null && nextPoint.controlPoint1 != null
					&& !nextPoint.controlPoint1.equals(nextPoint)) {
				nextVec = nextPoint.controlPoint1;
				curveNormal2 = nextVec.sub((Vec2D) curVec).normalize();
			}

			//Are we a bezier 
			if (curPoint.controlPoint1 != null
					&& !curPoint.controlPoint1.equals(curPoint)
					&& prevVec != null) {
				prevVec = curPoint.controlPoint1;
				curveNormal1 = curVec.sub((Vec2D) prevVec).normalize();
			}

			if (curPoint.controlPoint2 != null
					&& !curPoint.controlPoint2.equals(curPoint)
					&& nextVec != null) {
				nextVec = curPoint.controlPoint2;
				curveNormal2 = (Vec2D) nextVec.sub(curVec).normalize();
			}

			/*
			 * Now we Have these angles go though and work out the offset points
			 */
			Vec2D newAnLeft1 = null, newAnRight1 = null, newAnLeft2 = null, newAnRight2 = null;

if((curveNormal1 != null && curveNormal1.x == 0 && curveNormal1.y ==0))
	curveNormal1 = curveNormalPrev1;

if((curveNormal2 != null && curveNormal2.x == 0 && curveNormal2.y ==0))
	curveNormal2 = curveNormalPrev2;
	

curveNormalPrev1 = curveNormal1;
curveNormalPrev2 = curveNormal2;
	
			if (curveNormal1 != null) {
				newAnLeft1 = (Vec2D) curveNormal1
						.getRotated((float) (Math.PI / 2));
				newAnLeft1.normalize();
				newAnLeft1.scaleSelf(tempOffset);
				newAnLeft1.addSelf(curVec);

				newAnRight1 = (Vec2D) curveNormal1
						.getRotated((float) (Math.PI / 2));
				newAnRight1.normalize();
				newAnRight1.scaleSelf(-tempOffset);
				newAnRight1.addSelf(curVec);
			}

			if (curveNormal2 != null ) {
				newAnLeft2 = (Vec2D) curveNormal2
						.getRotated((float) (Math.PI / 2));
				newAnLeft2.normalize();
				newAnLeft2.scaleSelf(tempOffset);
				newAnLeft2.addSelf(curVec);

				newAnRight2 = (Vec2D) curveNormal2
						.getRotated((float) (Math.PI / 2));
				newAnRight2.normalize();
				newAnRight2.scaleSelf(-tempOffset);
				newAnRight2.addSelf(curVec);
			}
			



			/*
			 * If join is set to MITER then move points to form a point
			 */

			if (newAnLeft1 != null && newAnLeft2 != null
					&& this.joinType == SketchSpline.JOIN_MITER) {
				float projectLen = 1000;
				Vec2D backLine = newAnLeft1.add(curveNormal1.scale(projectLen));
				Vec2D fowardLine = newAnLeft2.sub(curveNormal2
						.scale(projectLen));

				if (functions.intersect(newAnLeft1.x, newAnLeft1.y, backLine.x,
						backLine.y, newAnLeft2.x, newAnLeft2.y, fowardLine.x,
						fowardLine.y) == functions.DO_INTERSECT) {
					newAnLeft1.x = functions.x;
					newAnLeft1.y = functions.y;
					newAnLeft2.x = functions.x;
					newAnLeft2.y = functions.y;

				}

			}

			if (newAnRight1 != null && newAnRight2 != null
					&& this.joinType == SketchSpline.JOIN_MITER) {
				float projectLen = 1000;
				Vec2D backLine = newAnRight1
						.add(curveNormal1.scale(projectLen));
				Vec2D fowardLine = newAnRight2.sub(curveNormal2
						.scale(projectLen));

				if (functions.intersect(newAnRight1.x, newAnRight1.y,
						backLine.x, backLine.y, newAnRight2.x, newAnRight2.y,
						fowardLine.x, fowardLine.y) == functions.DO_INTERSECT) {
					newAnRight1.x = functions.x;
					newAnRight1.y = functions.y;
					newAnRight2.x = functions.x;
					newAnRight2.y = functions.y;

				}
			}

			

			/*
			 * Modify Offset points to account for offset dir.
			 */

			SketchPoint outlinePointLeft1 = null, outlinePointRight1 = null, outlinePointLeft2 = null, outlinePointRight2 = null;

			if (this.offsetType == SketchSpline.OFFSET_LEFT) {
				if (newAnLeft1 != null) {
					outlinePointLeft1 = new SketchPoint(newAnLeft1);
					outlinePointRight1 = new SketchPoint(curVec);
				}

				if (newAnLeft2 != null) {
					outlinePointLeft2 = new SketchPoint(newAnLeft2);
					outlinePointRight2 = new SketchPoint(curVec);
				}

			}

			if (this.offsetType == SketchSpline.OFFSET_RIGHT) {
				if (newAnRight1 != null) {
					outlinePointLeft1 = new SketchPoint(curVec);
					outlinePointRight1 = new SketchPoint(newAnRight1);
				}

				if (newAnRight2 != null) {
					outlinePointLeft2 = new SketchPoint(curVec);
					outlinePointRight2 = new SketchPoint(newAnRight2);
				}

			}

			if (this.offsetType == SketchSpline.OFFSET_BOTH) {
				if (newAnLeft1 != null && newAnRight1 != null) {
					outlinePointLeft1 = new SketchPoint(newAnLeft1);
					outlinePointRight1 = new SketchPoint(newAnRight1);
				}

				if (newAnLeft2 != null && newAnRight2 != null) {
					outlinePointLeft2 = new SketchPoint(newAnLeft2);
					outlinePointRight2 = new SketchPoint(newAnRight2);
				}
			}

			

			
			/*
			 * If Join is set to JOIN_ROUND then add beziers to joins and move acute angled points to allow interior curves to be added
			 * Should we more outside points to make a more constant curve?
			 */
			if (this.joinType == SketchSpline.JOIN_ROUND) {

				if (outlinePointLeft1 != null && outlinePointLeft2 != null
						&& outlinePointLeft1.distanceTo(outlinePointLeft2) == 0) {
					float pushDist = outlinePointRight1
							.distanceTo(outlinePointRight2) / 2;

					if (prevVec != null
							&& curVec.distanceTo(prevVec) < pushDist)
						pushDist = curVec.distanceTo(prevVec);

					if (nextVec != null
							&& curVec.distanceTo(nextVec) < pushDist)
						pushDist = curVec.distanceTo(nextVec);

					outlinePointLeft1.subSelf(curveNormal1.scale(pushDist));
					outlinePointLeft2.addSelf(curveNormal2.scale(pushDist));

				}

				if (outlinePointRight1 != null
						&& outlinePointRight2 != null
						&& outlinePointRight1.distanceTo(outlinePointRight2) == 0) {
					float pushDist = outlinePointLeft1
							.distanceTo(outlinePointLeft2) / 2;

					if (prevVec != null
							&& curVec.distanceTo(prevVec) < pushDist)
						pushDist = curVec.distanceTo(prevVec);

					if (nextVec != null
							&& curVec.distanceTo(nextVec) < pushDist)
						pushDist = curVec.distanceTo(nextVec);

					outlinePointRight1.subSelf(curveNormal1.scale(pushDist));
					outlinePointRight2.addSelf(curveNormal2.scale(pushDist));

				}

				if (outlinePointLeft1 != null && outlinePointLeft2 != null
						&& outlinePointLeft1.distanceTo(outlinePointLeft2) > 0) {
					outlinePointLeft1.controlPoint1 = outlinePointLeft1
							.add(curveNormal1.scale(outlinePointLeft1
									.distanceTo(outlinePointLeft2) / 2));
					outlinePointLeft2.controlPoint2 = outlinePointLeft2
							.sub(curveNormal2.scale(outlinePointLeft1
									.distanceTo(outlinePointLeft2) / 2));
				}

				if (outlinePointRight1 != null
						&& outlinePointRight2 != null
						&& outlinePointRight1.distanceTo(outlinePointRight2) > 0) {
					outlinePointRight1.controlPoint2 = outlinePointRight1
							.add(curveNormal1.scale(outlinePointRight1
									.distanceTo(outlinePointRight2) / 2));
					outlinePointRight2.controlPoint1 = outlinePointRight2
							.sub(curveNormal2.scale(outlinePointRight1
									.distanceTo(outlinePointRight2) / 2));
				}

			}

			//

			/*
			 * Finally add the offset points to the path
			 */

			if (outlinePointLeft1 != null)
				this.outineLeft.add(outlinePointLeft1);

			if (outlinePointRight1 != null)
				this.outineRight.add(outlinePointRight1);

			if (outlinePointLeft2 != null)
				this.outineLeft.add(outlinePointLeft2);

			if (outlinePointRight2 != null)
				this.outineRight.add(outlinePointRight2);

		}


		/*
		 * NOW approxomate Our bezier curve offset
		 */
		int leftOffset = 0;
		int rightOffset = 0;
		getCentrePath().cacheLength(true);
		
		int loop = 1;
		
		if(getCentrePath().getClosed())
			loop = 0;

		for (int i = 0; i < getCentrePath().size() - loop; i++) {
			SketchPoint curPoint = getCentrePath().get(i);
			
			int nextIndex = i+1; 
			
			if(i == getCentrePath().size()-1)
				nextIndex = 0;
			
			SketchPoint nextPoint = getCentrePath().get(nextIndex);
	

			tempOffset = this.getOffsetSize();
			tempOffsetNext = this.getOffsetSize();

			if (getCentreOffset().containsKey(i))
				tempOffset = this.getCentreOffset().get(i);

			if (nextPoint != null && getCentreOffset().containsKey(nextIndex)) {
				tempOffsetNext = this.getCentreOffset().get(nextIndex);
			}

			leftOffset++;
			rightOffset++;

			if ((curPoint.containsBezier() || nextPoint.containsBezier())) {
				

				float step = getParentSketch().getSketchGlobals().BEZIER_DETAIL_OFFSET;
				step /= getCentrePath().size();

				float pDelta = (getCentrePath().getPercent(nextPoint) - step)
						- (getCentrePath().getPercent(curPoint) + step);
				float p = 0;
				for (float s = getCentrePath().getPercent(curPoint) + step; s < getCentrePath()
						.getPercent(nextPoint) - step; s += step) {
					Vec2D curveVec = getCentrePath().getPos(s);
					Vec2D perp = getCentrePath().getPerpendicular(s);

					float offset = tempOffset;
					p += (step / pDelta);

					offset = (tempOffset * (1 - p)) + (tempOffsetNext * p);

					if(rightOffset < outineRight.size()){
					this.outineRight.add(
							rightOffset,
							new SketchPoint(curveVec.add(perp
									.getRotated((float) (Math.PI / 2))
									.normalize().scaleSelf(-offset))));
					rightOffset++;
					}
					if(leftOffset < outineLeft.size()){

					this.outineLeft.add(
							leftOffset,
							new SketchPoint(curveVec.add(perp
									.getRotated((float) (Math.PI / 2))
									.normalize().scaleSelf(offset))));
					leftOffset++;
					}

				}

			}

			leftOffset++;
			rightOffset++;

		}


		getCentrePath().cacheLength(false);

		

		/**
		 * BUILD end caps
		 * 
		 * ___ 
		 * |:| 
		 * |:|
		 * ---
		 */
		
		
		applyOutlineOffset();
		buildPath();


		if (this.getCap() == SketchSpline.CAP_LEG && this.outineLeft.size() >= 2) {
			
			// remember starts and ends
			SketchPoint start = this.getCentrePath().get(0);
			SketchPoint startNext = this.getCentrePath().get(1);

			SketchPoint startLeft = this.outineLeft.get(0);
			SketchPoint startRight = this.outineRight.get(0);
			SketchPoint endPrev = this.getCentrePath().get(
					this.getCentrePath().size() - 2);
			SketchPoint end = this.getCentrePath().get(
					this.getCentrePath().size() - 1);
			SketchPoint endLeft = this.outineLeft
					.get(this.outineLeft.size() - 1);
			SketchPoint endRight = this.outineRight
					.get(this.outineRight.size() - 1);

			float a = (float) functions.angleOf(endLeft.copy().sub(endRight)
					.normalize());
			a = (float) (a - Math.PI);
			float Adjacent = end.distanceTo(endLeft);
			float Opposite = (float) (Math.tan(a) * Adjacent);
			float Hypotenues = (float) Math.sqrt(Math.pow(Adjacent, 2)
					+ Math.pow(Opposite, 2));

			if (Hypotenues > end.distanceTo(endPrev))
				Hypotenues = end.distanceTo(endPrev);

			float flip = 1;

			//flip side
			if (a > (Math.PI / 2) || a < -(Math.PI / 2))
				flip = -flip;

			endLeft.y = end.y;
			endLeft.x = end.x + (Hypotenues * (-flip));
			endRight.y = end.y;
			endRight.x = end.x + (Hypotenues * flip);

			//endRight.y = end.y;

			float startLen = startLeft.distanceTo(startRight) / 2;
			float endLen = endLeft.distanceTo(endRight) / 2;
			// remove ends
			float totalLen = this.getlength();
			int arrayOffset = 1;

			Vec2D dir = startLeft.copy().sub(startRight).rotate((float) (Math.PI / 2));
			dir.normalize();

			Vec2D bezierLeftStart = this.outineLeft.get(0).copy();
			bezierLeftStart.addSelf(dir.scale(startLen * 1.5f));

			Vec2D bezierRightStart = this.outineRight.get(0).copy();
			bezierRightStart.addSelf(dir.scale(startLen * 1.5f));

			this.path.addBezier(
					startLeft,startLeft.copy(), bezierLeftStart
							.copy());
			this.path.addBezier(startRight, 
					bezierRightStart.copy(), startRight.copy());

		}

		if (this.getCap() == SketchSpline.CAP_PARRALEL && this.outineLeft.size() >= 2) {
			// remember starts and ends
			SketchPoint start = this.getCentrePath().get(0);
			SketchPoint startNext = this.getCentrePath().get(1);

			SketchPoint startLeft = this.outineLeft.get(0);
			SketchPoint startRight = this.outineRight.get(0);
			SketchPoint endPrev = this.getCentrePath().get(
					this.getCentrePath().size() - 2);
			SketchPoint end = this.getCentrePath().get(
					this.getCentrePath().size() - 1);
			SketchPoint endLeft = this.outineLeft
					.get(this.outineLeft.size() - 1);
			SketchPoint endRight = this.outineRight
					.get(this.outineRight.size() - 1);

			float a = (float) functions.angleOf(endLeft.sub(endRight)
					.normalize());
			a = (float) (a - Math.PI);
			float Adjacent = end.distanceTo(endLeft);
			float Opposite = (float) (Math.tan(a) * Adjacent);
			float Hypotenues = (float) Math.sqrt(Math.pow(Adjacent, 2)
					+ Math.pow(Opposite, 2));

			if (Hypotenues > end.distanceTo(endPrev))
				Hypotenues = end.distanceTo(endPrev);

			float flip = 1;

			//flip side
			if (a > (Math.PI / 2) || a < -(Math.PI / 2))
				flip = -flip;

			endLeft.y = end.y;
			endLeft.x = end.x + (Hypotenues * (-flip));
			endRight.y = end.y;
			endRight.x = end.x + (Hypotenues * flip);

			//endRight.y = end.y;

			//Other end

			a = (float) functions
					.angleOf(startLeft.sub(startRight).normalize());
			a = (float) (a - Math.PI);
			Adjacent = start.distanceTo(startLeft);
			Opposite = (float) (Math.tan(a) * Adjacent);
			Hypotenues = (float) Math.sqrt(Math.pow(Adjacent, 2)
					+ Math.pow(Opposite, 2));

			if (Hypotenues > start.distanceTo(startNext))
				Hypotenues = start.distanceTo(startNext);

			flip = 1;

			//flip side
			if (a > (Math.PI / 2) || a < -(Math.PI / 2))
				flip = -flip;

			startLeft.y = start.y;
			startLeft.x = start.x + (Hypotenues * (-flip));
			startRight.y = start.y;
			startRight.x = start.x + (Hypotenues * flip);
			//endRight.y = end.y;

		}

		
		
		
		if (this.getCap() == SketchSpline.CAP_ROUND && this.outineLeft.size() >= 2) {
			// remember starts and ends
			SketchPoint startLeft = this.outineLeft.get(0);
			SketchPoint startRight = this.outineRight.get(0);
			SketchPoint endLeft = this.outineLeft
					.get(this.outineLeft.size() - 1);
			SketchPoint endRight = this.outineRight
					.get(this.outineRight.size() - 1);

			float startLen = startLeft.distanceTo(startRight) / 2;
			float endLen = endLeft.distanceTo(endRight) / 2;
			// remove ends
			float totalLen = this.getlength();
			int arrayOffset = 1;

			Vec2D dir = startLeft.copy().sub(startRight).rotate((float) (Math.PI / 2));
			dir.normalize();

			Vec2D bezierLeftStart = this.outineLeft.get(0).copy();
			bezierLeftStart.addSelf(dir.scale(startLen * 1.5f));

			Vec2D bezierRightStart = this.outineRight.get(0).copy();
			bezierRightStart.addSelf(dir.scale(startLen * 1.5f));

			
			this.path.addBezier(
					startLeft,
			startLeft.copy(), bezierLeftStart
							.copy());
			this.path.addBezier(startRight, 
					bezierRightStart.copy(), startRight.copy());

			/*
			 * END Cap now
			 */

			Vec2D dirEnd = endLeft.copy().sub(endRight).rotate((float) (Math.PI / 2));
			dirEnd.normalize();

			Vec2D bezierLeftEnd = this.outineLeft.get(
					this.outineLeft.size() - 1).copy();
			bezierLeftEnd.subSelf(dirEnd.scale(endLen * 1.5f));

			Vec2D bezierRightEnd = this.outineRight.get(
					this.outineRight.size() - 1).copy();
			bezierRightEnd.subSelf(dirEnd.scale(endLen * 1.5f));

	
			// bezierLeftEnd.addSelf(new Vec2D(100,100));

			
			this.path
					.addBezier(
							endLeft,bezierLeftEnd.copy(), endLeft
									.copy());
			this.path.addBezier(endRight, 
					endRight.copy(), bezierRightEnd.copy());

			//}

			/*
			if (this.offsetType == SketchSpline.OFFSET_RIGHT
					|| this.offsetType == SketchSpline.OFFSET_BOTH
					&& this.outineRight.size() > 0) {

				int offset = 0;
				if (this.offsetType == SketchSpline.OFFSET_BOTH)
					offset++;

			}
			*/
		}

		/**
		 * CAP BUTT
		 */

		if (this.getCap() == SketchSpline.CAP_BUTT) {

			applyOutlineOffset();
			buildPath();
		}

		this.isBuilt = true;

	}

	public void optimize() {

		if (!autoSmooth || this.getType() == SketchSpline.OFFSET_SPLINE)
			return;

		if (getCentrePath().size() < 3)
			return;

		// System.out.println("optimize sketch");

		SketchPoint lastStoredPoint = (SketchPoint) getCentrePath().get(0);
		List<SketchPoint> optimizedArray = new ArrayList<SketchPoint>();
		optimizedArray.add(lastStoredPoint);

		float step = SETTINGS_SKETCH.spline_point_every / this.getlength();
		for (float i = step; i < 1 ; i += step) {
			optimizedArray.add(new SketchPoint(this.getPos(i)));
		}
		optimizedArray.add(getCentrePath().get(getCentrePath().size() - 1));
		// go through and add selected points replacing the closesed points to
		// these
		getCentrePath().setPath((ArrayList<SketchPoint>) optimizedArray);

		for (int i = 0; i < this.getSelectedNodes().size(); i++) {
			Object objNode =  this.getSelectedNodes()
					.get(i);
			
			if(objNode instanceof SketchPoint){
			SketchPoint selectedNode =  (SketchPoint)objNode;
			
			int index = this.getNearestVecIndex(selectedNode);
			getCentrePath().set(index, selectedNode);
			// centrePath.l.remove(index);
			// centrePath.l.add(index, selectedNode);
			}
		}
	}

	public void optimizeRange(Vec2D startVec, Vec2D endVec) {

		if (!autoSmooth || this.getType() == SketchSpline.OFFSET_SPLINE)
			return;

		if (getCentrePath().size() < 3)
			return;

		// build arrays
		ArrayList<SketchPoint> startList = new ArrayList<SketchPoint>();
		ArrayList<SketchPoint> middleList = new ArrayList<SketchPoint>();
		ArrayList<SketchPoint> endList = new ArrayList<SketchPoint>();

		// get the index of the start and end point
		int startIndex = this.getIndex(startVec);
		int endIndex = this.getIndex(endVec);

		// flags
		Vec2D beforeStart = null;
		Vec2D afterEnd = null;

		if (startIndex > 0)
			beforeStart = (Vec2D) getCentrePath().get(startIndex - 1);

		if (endIndex < getCentrePath().size() - 1)
			afterEnd = (Vec2D) getCentrePath().get(endIndex + 1);

		// build the start
		for (int i = 0; i < startIndex - 2; i++) {
			SketchPoint vec = (SketchPoint) getCentrePath().get(i);
			startList.add(vec);

		}

		// System.out.println("startPos" + this.getLenTo(beforeStart));

		float step = SETTINGS_SKETCH.spline_point_every / this.getlength();
		// store the start percent of our offset
		float startPos = this.getLenTo(beforeStart) / this.getlength();

		if (beforeStart == null)
			startPos = 0;

		float endPos = this.getLenTo(afterEnd) / this.getlength();

		if (afterEnd == null)
			endPos = 1;

		// System.out.println("start percent:"+startPos + " index:" +
		// startIndex+ "    end percent:" + endPos + " index:" + endIndex +
		// "     total len:" + centrePath.size() + "  step:" + step);

		for (float i = startPos; i < endPos; i += step) {
			// System.out.println("adding to middle" + i);

			Vec2D addVec = this.getPos(i);
			SketchPoint addP = new SketchPoint(addVec);
			// if(i < 1 && addVec.distanceTo(startVec) >
			// SETTINGS_SKETCH.spline_point_every && addVec.distanceTo(endVec) >
			// SETTINGS_SKETCH.spline_point_every)

			if (i < 1 - step)
				;
			middleList.add(addP);

		}

		for (int i = endIndex; i < getCentrePath().size(); i++) {
			SketchPoint vec = (SketchPoint) getCentrePath().get(i);
			endList.add(vec);
		}

		for (int i = 0; i < middleList.size(); i++) {
			SketchPoint vec = middleList.get(i);
			startList.add(vec);
		}

		for (int i = 0; i < endList.size(); i++) {
			SketchPoint vec = endList.get(i);
			startList.add(vec);
		}

		getCentrePath().setPath(startList);

		for (int i = 0; i < this.getSelectedNodes().size(); i++) {
			SketchPoint selectedNode = (SketchPoint) this.getSelectedNodes()
					.get(i);
			int index = this.getNearestVecIndex(selectedNode);

			if (index != -1)
				getCentrePath().set(index, selectedNode);
			// centrePath.l.remove(index);
			// centrePath.l.add(index, selectedNode);

		}

	}

	public void removeVertex(SketchPoint v) {
		if (this.getCentrePath().contains(v)) {
			this.getCentrePath().remove(v);
			this.build();
		}

		if (this.getCentrePath().size() == 1)
			this.destroy();

	}

	public void render(PGraphics g) {

		
		
		Sketch s = getParentSketch();
		SketchTools st = s.getSketchTools();
		
		this.path.editable = false;
		this.path.setParentSketch(this.getParentSketch());
		this.path.render(g);
		this.centrePath.editable = true;
		this.centrePath.setType(this.getType());
		this.centrePath.setClosed(false);
		
		
		
		switch(getParentSketch().getRenderMode()){
		case Sketch.RENDER_3D_PREVIW:
		break;
			
		case Sketch.RENDER_3D_EDITING_PLANES:
			this.centrePath.render(g);
		break;
			
		case Sketch.RENDER_3D_DIAGRAM:
		break;
		
		case Sketch.RENDER_EDIT_SELECT:

		break;
		
		}
		
		/*
		

		if (getParentSketch().getSketchTools().getCurrentTool() != SketchTools.SELECT_TOOL) {
			if (this.getParentSketch().isSelected()) { // is parent layer
				g.fill(getParentSketch().getSketchGlobals().chairColour);
			} else {
				g.fill(SETTINGS_SKETCH.SKETCHSHAPE_FILL_UNSELECTED_LAYER_COLOUR);

			}
		} else {
		}



		if (this.isBuilt) {
			this.path.setParentSketch(this.getParentSketch());
			this.path.render(g);

		} else {
			// draw selection points
			if (getParentSketch().getSketchTools().getCurrentTool() == SketchTools.SELECT_TOOL
					&& !getParentSketch().getSketchTools().mouseDown) {

				float selectDia = SETTINGS_SKETCH.select_dia
						* (1 / getParentSketch().getSketchGlobals().zoom);

				if (selectDia > SETTINGS_SKETCH.select_dia * 1.5f)
					selectDia = SETTINGS_SKETCH.select_dia;

				for (int i = 0; i < getCentrePath().size(); i++) {
					Vec2D curVec = (Vec2D) getCentrePath().get(i);
					g.ellipse(curVec.x, curVec.y, selectDia, selectDia);
				}
			}

			if (SETTINGS_SKETCH.fill_sketch
					&& getParentSketch().getSketchTools().getCurrentTool() != SketchTools.SELECT_TOOL) {
				if (selected) {
					g.fill(SETTINGS_SKETCH.SKETCHSHAPE_FILL_SELECTED_COLOUR);
				} else {
					g.fill(getParentSketch().getSketchGlobals().chairColour);
				}
			} else {
				g.noFill();
			}

			// ------------------------------------------------
			if (this.getCombinedSize() > 0) {
				g.beginShape();

				for (int i = this.getCombinedSize() - 1; i > -1; i--) {
					Vec2D curVec = (Vec2D) this.getCombined(i);

					if (SETTINGS_SKETCH.Draw_Curves)
						g.curveVertex(curVec.x, curVec.y);
					else
						g.vertex(curVec.x, curVec.y);
				}

				Vec2D curVec = (Vec2D) this
						.getCombined(this.getCombinedSize() - 1);

				if (SETTINGS_SKETCH.Draw_Curves) {
					g.curveVertex(curVec.x, curVec.y);
					g.curveVertex(curVec.x, curVec.y);

				} else {
					g.vertex(curVec.x, curVec.y);
					g.vertex(curVec.x, curVec.y);
				}

			}

			g.endShape();
			g.noFill();

		}

		this.centrePath.editable = true;
		this.centrePath.setClosed(false);
		
		
		
		
			switch(getParentSketch().getRenderMode()){
			case Sketch.RENDER_3D_PREVIW:
			break;
				
			case Sketch.RENDER_3D_EDITING_PLANES:
				
				
				renderNodes(g);
				
				//this.centrePath.render(g);
			break;
				
			case Sketch.RENDER_3D_DIAGRAM:
			break;
			
			}
			*/
	}

	private void renderNodes(PGraphics g) {

		float selectDia = SETTINGS_SKETCH.select_dia
				* (1 / getParentSketch().getZOOM());

		if (selectDia > SETTINGS_SKETCH.select_dia * 1.5f)
			selectDia = SETTINGS_SKETCH.select_dia;

		selectDia = selectDia / 2;

		if (true) {
			// g.fill(SETTINGS_SKETCH.sChair_controle_points_fill);
			g.noFill();
			g.stroke(SETTINGS_SKETCH.CONTROL_POINT_STROKE_COLOUR);
			g.fill(SETTINGS_SKETCH.CONTROL_POINT_FILL_COLOUR);
			
			Vec2D startVect = (Vec2D) getCentrePath().get(0);
			if(startVect != null)
			g.ellipse(startVect.x, startVect.y, selectDia,selectDia);
			
			Vec2D endVect = (Vec2D) getCentrePath().get(getCentrePath().size()-1);
			if(startVect != null)
			g.ellipse(endVect.x, endVect.y, selectDia,selectDia);
			
			
			g.noFill();
			g.beginShape();	
			for (int i = 0; i < getCentrePath().size(); i++) {
				Vec2D curVec = (Vec2D) getCentrePath().get(i);
				
				g.vertex(curVec.x,curVec.y);
				
				// 
				// selectDia);
			}
			g.endShape();

			g.noFill();
		}
	}


	public void renderPickBuffer(PGraphics g) {
		//g.noStroke();
	
		this.path.renderPickBuffer(g);


	}

	public void renderSilhouette(PGraphics g) {
		this.path.renderSilhouette(g);
	}

	public void replace(SketchShape clone) {

		SketchSpline cloneLocal = (SketchSpline) clone;

		getCentrePath().setPath(cloneLocal.getCentrePath().getList());
		this.outineLeft = cloneLocal.outineLeft;
		this.outineRight = cloneLocal.outineRight;
		this.slots = cloneLocal.slots;

		this.setOffsetSize(cloneLocal.getOffsetSize());
		this.path = cloneLocal.path;
		this.path.editable = false;
		this.isBuilt = cloneLocal.isBuilt;
		this.offsetType = cloneLocal.offsetType;

	}

	public void scale(float scale, toxi.geom.Vec3D centre) {
		// this.scale += scale;
		this.setOffsetSize(this.getOffsetSize()
				+ (this.getOffsetSize() * scale));
		this.getCentrePath().scale(scale, centre);
		this.path.scale(scale, centre);
	}

	public void select() {
		this.selected = true;
		if (this.path != null)
			this.path.select();
		
		this.getCentrePath().select();
	}
	
	public void unselect() {
				
		
		this.selected = false;
		
		if (this.path != null)
			this.path.unselect();
		
		this.getCentrePath().unselect();


	}
	

	public void selectNodes(float mouseX, float mouseY) {
		// this.path.selectNodes(mouseX, mouseY);

		boolean shapeSelected = false;
		float selectDia = SETTINGS_SKETCH.select_dia
				* (1 / getParentSketch().getZOOM());

		if (selectDia > SETTINGS_SKETCH.select_dia * 1.5f)
			selectDia = SETTINGS_SKETCH.select_dia;

		this.unselectNodes();
		this.path.unselectNodes();
		for (int i = 0; i < getCentrePath().size(); i++) {
			SketchPoint v = (SketchPoint) getCentrePath().get(i);
			if (v.distanceTo(new Vec2D(mouseX, mouseY)) < selectDia) {
				this.getSelectedNodes().add(v);
				shapeSelected = true;
			}
		} // TODO Auto-generated method stub

		if (shapeSelected && getParentSketch().getSketchGlobals().undo != null)
			getParentSketch().getSketchGlobals().undo
					.addOperation(new UndoAction(this, this.clone(),
							UndoAction.EDIT_SHAPE));

		//if (!shapeSelected)
		this.getCentrePath().selectNodes(mouseX, mouseY);

		if (this.path.getSelectedNodes().size() > 0 && !shapeSelected)
			getParentSketch().getSketchGlobals().undo
					.addOperation(new UndoAction(this, this.clone(),
							UndoAction.EDIT_SHAPE));

	}

	public void setCap(int cap) {
		capType = cap;
	}


	public void setCentreOffset(Map<Integer, Float> centreOffset) {
		this.centreOffset = centreOffset;
	}


	public void setCentrePath(SketchPath centrePath) {
		this.centrePath = centrePath;
	}

	public void setJoinType(int j) {
		this.joinType = j;
	}


	public void setOffsetSize(float offsetSize) {
		this.offsetSize = offsetSize;
	}

	public void setOffsetSizeCentre(float val) {
		for (int i = 0; i < this.getCentrePath().getSelectedNodes().size(); i++) {
			SketchPoint p = (SketchPoint) this.getCentrePath()
					.getSelectedNodes().get(i);
			int index = this.getCentrePath().l.indexOf(p);
			this.getCentreOffset().put(index, val);
		}
	}
	

	public SketchPoint setVec2DpickBuffer(int col, SketchPoint selectedVec,
			SketchShape selectedShape, SlicePlane selectedVecPlane,
			boolean isSelectedVecOnOutline) {

		for (int i = 0; i < getCentrePath().size(); i++) {
			SketchPoint curVec = (SketchPoint) getCentrePath().get(i);

			if (col == getColor(i + (this.id * 100))) {
				isSelectedVecOnOutline = false;
				selectedShape = this;
				selectedVec = curVec;

				return selectedVec;
			}
		}

		for (int i = 0; i < this.getCombinedSize(); i++) {
			SketchPoint curVec = (SketchPoint) this.getCombined(i);
			if (col == getColor(getCentrePath().size() + i + (this.id * 100))) {
				isSelectedVecOnOutline = true;
				selectedShape = this;
				selectedVec = curVec;
				return selectedVec;
			}
		}
		selectedVec = null;
		return null;

	}

	public Element toXML() {
		Element element = new Element("SketchSpline");

		element.addAttribute(new Attribute("id", String.valueOf(this.getId())));
		element.addAttribute(new Attribute("outlineId", String
				.valueOf(this.path.getId())));
		element.addAttribute(new Attribute("centreId", String.valueOf(this
				.getCentrePath().getId())));

		element.addAttribute(new Attribute("offsetSize", String.valueOf(this
				.getOffsetSize())));

		element.addAttribute(new Attribute("splineType", String.valueOf(this
				.getType())));

		element.addAttribute(new Attribute("endCap", String.valueOf(this
				.getCap())));

		element.addAttribute(new Attribute("joinType", String.valueOf(this
				.getJoinType())));


		if (isConstructionLine())
			element.addAttribute(new Attribute("isConstructionLine", "true"));

		element.addAttribute(new Attribute("union", String.valueOf(this.union)));

		Element elementCentre = new Element("SketchSplineCentrePath");
		for (int i = 0; i < this.getCentrePath().size(); i++) {
			SketchPoint point = this.getCentrePath().get(i);
			elementCentre.appendChild(point.toXML());
		}

		element.appendChild(elementCentre);

		Element SketchSplineOffsets = new Element("SketchSplineOffsets");
		for (int i = 0; i < getCentrePath().size(); i++) {
			SketchPoint curVec = (SketchPoint) getCentrePath().get(i);

			if (this.outlineOffset.containsKey(i)) {
				Vec2D offset = this.outlineOffset.get(i);
				Element SketchSplineOffset = new Element("SketchSplineOffset");
				SketchSplineOffset.addAttribute(new Attribute("linked_id",
						String.valueOf(i)));
				SketchSplineOffset.addAttribute(new Attribute("x_offset",
						String.valueOf(offset.x)));
				SketchSplineOffset.addAttribute(new Attribute("y_offset",
						String.valueOf(offset.y)));

				SketchSplineOffsets.appendChild(SketchSplineOffset);
			}

		}

		Element SketchSplinePathOffsets = new Element("SketchSplinePathOffsets");
		for (int i = 0; i < getCentrePath().size(); i++) {
			SketchPoint curVec = (SketchPoint) getCentrePath().get(i);

			if (this.getCentreOffset().containsKey(i)) {
				float offset = this.getCentreOffset().get(i);
				Element SketchSplineOffset = new Element(
						"SketchSplinePathOffset");
				SketchSplineOffset.addAttribute(new Attribute("linked_index",
						String.valueOf(i)));
				SketchSplineOffset.addAttribute(new Attribute("offset", String
						.valueOf(offset)));

				SketchSplinePathOffsets.appendChild(SketchSplineOffset);
			}

		}

		element.appendChild(SketchSplinePathOffsets);

		for (int i = 0; i < getCentrePath().size(); i++) {
			Vec2D curVec = (Vec2D) getCentrePath().get(i);
			int i2 = ((getCentrePath().size() * 2) - i - 1);

			if (this.outlineOffset.containsKey(i2)) {
				Vec2D offset = this.outlineOffset.get(i2);

				Element SketchSplineOffset = new Element("SketchSplineOffset");

				SketchSplineOffset.addAttribute(new Attribute("linked_id",
						String.valueOf(i2)));
				SketchSplineOffset.addAttribute(new Attribute("x_offset",
						String.valueOf(offset.x)));
				SketchSplineOffset.addAttribute(new Attribute("y_offset",
						String.valueOf(offset.y)));

				SketchSplineOffsets.appendChild(SketchSplineOffset);

			}

		}

		element.appendChild(SketchSplineOffsets);

		return element;
	}



}
