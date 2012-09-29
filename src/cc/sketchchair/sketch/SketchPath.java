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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.PickBuffer;
import cc.sketchchair.core.SETTINGS;
import cc.sketchchair.functions.functions;
import cc.sketchchair.geometry.SlicePlane;
import cc.sketchchair.triangulate.Vector2D;

import nu.xom.Attribute;
import nu.xom.Element;
import ShapePacking.BezierControlNode;
import ShapePacking.spPoint;
import ShapePacking.spShape;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

/**
 * SketchPath is the base class for most Sketch objects it contains low level functions for manipulating and calculating paths in SketchChair. 
 * @author gregsaul
 *
 */
//#ENDIF JAVA

public class SketchPath extends SketchShape {
	
	  // constants for using the bounding_box
	  int MIN_X  = 0;
	  int MIN_Y  = 1;
	  int MAX_X = 2;
	  int MAX_Y = 3;
	  
	  
	  
	public List<SketchPoint> l = new ArrayList<SketchPoint>();
	boolean isOptimized;
	float pointDist = 5;//SETTINGS_SKETCH.dist_between_points; // distance between points
	// on the optimized line
	float offsetSize = 20;//SETTINGS_SKETCH.offset_size;
	SliceSlots slots = new SliceSlots();
	boolean slots_on_inside = true;

	int offsetType = 2;

	//private boolean open = false;

	boolean isOutline;
	private boolean cacheLength = false;
	private float cachedLength = -1;
	private float lastStepPercent;
	private int lastStepStartPos;
	private float lastStepLengthAlong;
	private float lastStepAlongBezier;
	private Vec2D lastStepMeasuredPoint;
	private float lastStepLenghtSegment;
	private boolean woundClockwise;
	private boolean woundClockwiseReset;
	private float cachedGetMaxX;
	private float cachedGetMaxY;
	private float cachedGetMinX;
	private float cachedGetMinY;

	static int OFFSET_LEFT = 0;
	static int OFFSET_RIGHT = 1;
	static int OFFSET_BOTH = 2;

	SketchPath(int offsetType, Sketch parentSketch) {
		super(parentSketch);
		this.offsetType = offsetType;

	}

	public SketchPath(Sketch parentSketch) {
		super(parentSketch);

	}

	//#IF JAVA
	public SketchPath(Sketch parentSketch, Element element) {
		super(parentSketch);
		//wrong type
		if (!element.getLocalName().equals("SketchPath"))
			return;

		if (element.getAttributeValue("id") != null) {
			this.setId(Integer.valueOf(element.getAttributeValue("id")));
		}

		if (element.getAttributeValue("closed") != null) {
			if (element.getAttributeValue("closed").equals("true")) {
				this.setClosed(true);
			} else {
				this.setClosed(false);
			}
		}

		//legacy fallback
		if (element.getAttributeValue("closed") == null)
			this.setClosed(true);

		if (element.getAttributeValue("isConstructionLine") != null) {
			//	this.setIsContructionLine(true);
		}

		if (element.getAttributeValue("union") != null) {
			this.union = Integer.valueOf(element.getAttributeValue("union"));
		}

		//for(int i = element.getChildCount()-1 ; i >= 0 ; i--){
		for (int i = 0; i < element.getChildCount(); i++) {
			Element child = (Element) element.getChild(i);
			if (child != null && child.getLocalName().equals("SketchPoint"))
				this.add(new SketchPoint(child));

		}

	}

	//#ENDIF JAVA

	public SketchPath(Sketch parentSketch, ArrayList<SketchPoint> outline) {
		super(parentSketch);
		for (int i = 0; i < outline.size(); i++) {
			SketchPoint p = outline.get(i);
			this.add(p);
		}
	}

	public void add(int i, SketchPoint point) {
		this.l.add(i, point);
		this.resetCachedVariables();
	}

	public void add(SketchPoint point) {
		this.l.add(point);
		this.resetCachedVariables();
	}

	public void addBezier(SketchPoint point, Vec2D controlP1, Vec2D controlP2) {
		//legacy code
		point.controlPoint1 = controlP1.copy();
		point.controlPoint2 = controlP2.copy();
	}

	public void build() {
		woudClockwiseReset();
	}

	//#IF JAVA
	/**
	 * In special cases cache the length of our line to optimize calculations
	 * @param cache
	 */
	//#ENDIF JAVA

	public void cacheLength(boolean cache) {
		this.cacheLength = cache;
		this.cachedLength = -1;
	}

	public SketchPath clone() {
		SketchPath newSpline = new SketchPath(getParentSketch());

		newSpline.setClosed(this.getClosed());
		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint curPoint = this.l.get(i);
			SketchPoint point = curPoint.clone();//new SketchPoint(curPoint.x, curPoint.y);
			newSpline.l.add(point);
		}

		newSpline.slots = this.slots.clone();

		return newSpline;

	}

	public ArrayList<SketchPoint> cloneArray() {
		ArrayList<SketchPoint> loop = new ArrayList<SketchPoint>();
		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint v = this.l.get(i).clone();
			loop.add(v);
		} // TODO Auto-generated method stub
		return loop;
	}

	public boolean contains(SketchPoint vec) {
		return l.contains(vec);
	}

	public SketchShape copy(Sketch parentSketch) {
		SketchPath newSpline = new SketchPath(parentSketch);

		newSpline.setClosed(this.getClosed());
		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint curPoint = this.l.get(i);
			SketchPoint point = curPoint.clone();//new SketchPoint(curPoint.x, curPoint.y);
			newSpline.l.add(point);
		}

		newSpline.slots = this.slots.clone();
		newSpline.setId(this.getId());
		return newSpline;
	}

	public void destroy() {
		this.setDestroy(true);
	}

	public void flipHorizontal(toxi.geom.Vec3D centre) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint curVec = this.l.get(i);
			curVec.x -= (curVec.x - centre.x) * 2;
			//curVec.y += (curVec.y - centre.y) * scale;

			if (curVec.containsBezier()) {

				if (curVec.controlPoint1 != null) {
					curVec.controlPoint1.x -= (curVec.controlPoint1.x - centre.x) * 2;
					//curVec.controlPoint1.y += (curVec.controlPoint1.y - centre.y) * scale;
				}

				if (curVec.controlPoint2 != null) {
					curVec.controlPoint2.x -= (curVec.controlPoint2.x - centre.x) * 2;
					//curVec.controlPoint2.y += (curVec.controlPoint2.y - centre.y) * scale;
				}
			}

		}

	}

	public SketchPoint get(int i) {

		return (SketchPoint)this.l.get(i);
	}

	public SketchPoint getCentreOfMass() {
		long x = 0;
		long y = 0;

		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint v = this.l.get(i);

			x += v.x;
			y += v.y;
		}
		return new SketchPoint(x / this.l.size(), y / this.l.size());

	}

	public SketchPoint getCentre() {

		return new SketchPoint(this.getMinX() + (this.getWidth() / 2),
				this.getMinY() + (this.getHeight() / 2));

	}

	private float getWidth() {
		return this.getMaxX() - this.getMinX();
	}

	private float getHeight() {
		return this.getMaxY() - this.getMinY();
	}

	public float getClosestPercent(float mouseX, float mouseY) {

		float closestDist = -1;
		float val = -1;
		this.cacheLength(true);
		SketchPoint mousePos = new SketchPoint(mouseX, mouseY);
		float step = SETTINGS_SKETCH.select_on_path_step / this.getlength();
		for (float i = 0; i <= 1; i += step) {
			Vec2D pos = this.getPos(i);

			if (i == 0 || pos.distanceTo(mousePos) < closestDist && pos != null) {
				val = i;
				closestDist = pos.distanceTo(mousePos);
			}

		}
		this.cacheLength(false);

		return val;
	}

	public SketchPoint getClosestPoint(SketchPoint pointOnPlan) {
		// TODO Auto-generated method stub
		return null;
	}

	public SketchPoint getClosestPoint(Vec2D pointOnPlan) {
		SketchPoint p = null;
		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint curVec = this.l.get(i);

			if (p == null
					|| curVec.distanceTo(pointOnPlan) < p
							.distanceTo(pointOnPlan))
				p = curVec;
		}

		return p;

	}

	public Vec2D getClosestPointAlongPath(float x, float y) {

		float closestDist = -1;
		Vec2D closestPoint = null;
		this.cacheLength(true);
		this.resetPosStep();
		Vec2D mousePos = new SketchPoint(x, y);
		float step = SETTINGS_SKETCH.select_on_path_step / this.getlength();
		step = .01f;
		for (float i = 0; i < 1; i += step) {
			Vec2D pos = this.getPosStep(step);
			if ((closestDist == -1 && pos != null)
					|| (pos != null && pos.distanceTo(mousePos) < closestDist)) {
				closestPoint = pos;
				closestDist = pos.distanceTo(mousePos);
			}

		}
		this.cacheLength(false);

		return closestPoint;
	}

	public Vec2D getClosestPointOnLine(float mouseX, float mouseY) {

		float closestDist = -1;
		float val = -1;
		Vec2D v = null;

		Vec2D mousePos = new SketchPoint(mouseX, mouseY);
		float step = SETTINGS_SKETCH.select_on_path_step / this.getlength();
		for (float i = 0; i < 1; i += step) {
			Vec2D pos = this.getPos(i);

			if (closestDist == -1 || pos.distanceTo(mousePos) < closestDist) {
				val = i;
				closestDist = pos.distanceTo(mousePos);
				v = pos;
			}

		}

		return v;
	}

	// id 0 gives color -2, etc.
	int getColor(int id) {
		return -(id + 2);
	}

	private int getIndex(SketchPoint p) {
		return this.l.indexOf(p);
	}

	public SketchPoint getLast() {

		if (this.l.size() > 0)
			return this.l.get(this.l.size() - 1);
		else
			return null;
	}
	public SketchPoint getFirst() {

		if (this.l.size() > 0)
			return this.l.get(0);
		else
			return null;
	}

	public float getlength() {

		if (this.cachedLength != -1 && this.cacheLength)
			return this.cachedLength;

		float length = 0;

		int offset = 0;

		if (!this.getClosed())
			offset = 1;

		this.cachedLength = this.getLengthTo(this.l.size() - offset);
		return this.cachedLength;

	}

	public int getLength() {
		return this.l.size();
	}

	public float getLengthBetween(int indexStart, int indexEnd) {
		float length = 0;

		Vec2D lastMeasured = null;
		for (int i = indexStart; i < indexEnd; i++) {
			SketchPoint curVec = this.l.get(i);
			SketchPoint nextVec = null;
			if (i < this.l.size() - 1)
				nextVec = this.l.get(i + 1);
			else
				nextVec = this.l.get(0);

			if (curVec.containsBezier() || nextVec.containsBezier()) {
				Vec2D bez1 = curVec;
				Vec2D bez2 = nextVec;

				if (curVec.containsBezier()) {
					bez1 = curVec.getControlPoint2();
				}

				if (nextVec.containsBezier()) {
					bez2 = nextVec.getControlPoint1();
				}

				lastMeasured = curVec.copy();
				float step = getParentSketch().getSketchGlobals().BEZIER_DETAIL_CALCULATIONS;
				;
				for (float t = 0; t <= 1; t += step) {
					float x = functions.bezierPoint(curVec.x, bez1.x, bez2.x,
							nextVec.x, t);
					float y = functions.bezierPoint(curVec.y, bez1.y, bez2.y,
							nextVec.y, t);
					length += lastMeasured.distanceTo(new Vec2D(x, y));
					lastMeasured = new Vec2D(x, y);
				}
			} else {
				length += curVec.distanceTo(nextVec);
				lastMeasured = nextVec.copy();
			}

		}

		return length;

	}

	public float getlengthPerPercent() {
		return this.getlength() / 100;
	}

	public float getLengthTo(int index) {
		return getLengthBetween(0, index);
	}

	public ArrayList<SketchPoint> getList() {
		return (ArrayList<SketchPoint>) l;
	}

	public float getMaxX() {

		if (cachedGetMaxX != -1)
			return cachedGetMaxX;

		float maxX = 0;
		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint v = this.l.get(i);
			if (i == 0 || v.x > maxX)
				maxX = v.x;
		}
		
		for (int i = 1; i < this.l.size()-1; i++) {
			SketchPoint v = this.l.get(i-1);
			SketchPoint vNext = this.l.get(i);

			if(v.containsBezier() || vNext.containsBezier()){
				float[] bounds = calculate_standard_bbox(v.x, v.y, v.getControlPoint2().x,v.getControlPoint2().y,vNext.getControlPoint1().x,vNext.getControlPoint1().y,  vNext.x, vNext.y);	
			if(bounds[MAX_X] > maxX)
				maxX = bounds[MAX_X];
			}
			
		}

		
		cachedGetMaxX = maxX;
		return maxX;
	}

	public float getMaxY() {
		if (cachedGetMaxY != -1)
			return cachedGetMaxY;

		float maxY = 0;
		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint v = this.l.get(i);
			if (i == 0 || v.y > maxY)
				maxY = v.y;

		}
		
		
		for (int i = 1; i < this.l.size()-1; i++) {
			SketchPoint v = this.l.get(i-1);
			SketchPoint vNext = this.l.get(i);

			if(v.containsBezier() || vNext.containsBezier()){
				float[] bounds = calculate_standard_bbox(v.x, v.y, v.getControlPoint2().x,v.getControlPoint2().y,vNext.getControlPoint1().x,vNext.getControlPoint1().y,  vNext.x, vNext.y);	
			if(bounds[MAX_Y] > maxY)
				maxY = bounds[MAX_Y];
			}
			
		}
		
		
		
		cachedGetMaxY = maxY;
		return maxY;
	}

	public float getMinX() {

		if (cachedGetMinX != -1)
			return cachedGetMinX;

		float minX = 0;
		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint v = this.l.get(i);
			if (i == 0 || v.x < minX)
				minX = v.x;
		}
		
		
		for (int i = 1; i < this.l.size()-1; i++) {
			SketchPoint v = this.l.get(i-1);
			SketchPoint vNext = this.l.get(i);

			if(v.containsBezier() || vNext.containsBezier()){
				float[] bounds = calculate_standard_bbox(v.x, v.y, v.getControlPoint2().x,v.getControlPoint2().y,vNext.getControlPoint1().x,vNext.getControlPoint1().y,  vNext.x, vNext.y);	
			if(bounds[MIN_X] < minX)
				minX = bounds[MIN_X];
			}
			
		}
		
		
		
		cachedGetMinX = minX;
		return minX;
	}

	public float getMinY() {

		if (cachedGetMinY != -1)
			return cachedGetMinY;

		float minY = 0;
		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint v = this.l.get(i);
			if (i == 0 || v.y < minY)
				minY = v.y;
		}
		
		
		for (int i = 1; i < this.l.size()-1; i++) {
			SketchPoint v = this.l.get(i-1);
			SketchPoint vNext = this.l.get(i);

			if(v.containsBezier() || vNext.containsBezier()){
				float[] bounds = calculate_standard_bbox(v.x, v.y, v.getControlPoint2().x,v.getControlPoint2().y,vNext.getControlPoint1().x,vNext.getControlPoint1().y,  vNext.x, vNext.y);	
			if(bounds[MIN_Y] < minY)
				minY = bounds[MIN_Y];
			}
			
		}
		
		
		
		cachedGetMinY = minY;
		return minY;
	}
	
	
	  /**
	   * Calculate the bezier value for one dimension at distance 't'
	   */
	  float calculate_bezier(float t, float p0, float p1, float p2, float p3) {
	    float mt = (1-t);
	    return (mt*mt*mt*p0) + (3*mt*mt*t*p1) + (3*mt*t*t*p2) + (t*t*t*p3); }
	  
	  /**
	   * expand the x-bounds, if the value lies outside the bounding box
	   */
	  void exandXBounds(float[] bounds, float value) {
	    if(bounds[MIN_X] > value) { bounds[MIN_X] = value; }
	    else if(bounds[MAX_X] < value) { bounds[MAX_X] = value; }}

	  /**
	   * expand the x-bounds, if the value lies outside the bounding box
	   */
	  void exandYBounds(float[] bounds, float value) {
	    if(bounds[MIN_Y] > value) { bounds[MIN_Y] = value; }
	    else if(bounds[MAX_Y] < value) { bounds[MAX_X] = value; }}

	  
	  /**
	   * Calculate the bounding box for this bezier curve. The next bit is technical. See the comment on this topic on
	   * http://newsgroups.derkeiler.com/Archive/Comp/comp.graphics.algorithms/2005-07/msg00334.html
	   * and the worked out mathematics at http://pomax.nihongoresources.com/downloads/bezierbounds.html
	   * for an explanation of why the following code is being used, and why it works.
	   */
	
	float[] calculate_standard_bbox(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2)
	  {
	    // compute linear bounds first
	    float[] bounds = {Math.min(x1,x2), Math.min(y1,y2), Math.max(x1,x2), Math.max(y1,y2)};
	  
	    float dcx0 = cx1 - x1;
	    float dcy0 = cy1  -y1;
	    float dcx1 = cx2 - cx1;
	    float dcy1 = cy2 - cy1;
	    float dcx2 = x2 - cx2;
	    float dcy2 = y2 - cy2;
	  
	    // Recompute bounds projected on the x-axis, if the control points lie outside the bounding box x-bounds
	    if(cx1<bounds[MIN_X] || cx1>bounds[MAX_X] || cx2<bounds[MIN_X] || cx2>bounds[MAX_X]) {
	      // we don't need to do this, but 'a', 'b' and 'c' are easier to read.
	      float a = dcx0; float b = dcx1; float c = dcx2;
	  
	      // Do we have a problematic discriminator if we use these values?
	      // If we do, because we're computing at sub-pixel level anyway, simply salt 'b' a tiny bit.
	      if(a+c != 2*b) { b+=0.01; }
	  
	      float numerator = 2*(a - b);
	      float denominator = 2*(a - 2*b + c);
	      float quadroot = (2*b-2*a)*(2*b-2*a) - 2*a*denominator;
	      float root = (float) Math.sqrt(quadroot);
	  
	      // there are two possible values for 't' that yield inflection points,
	      // and each of these inflection points might be outside the linear bounds
	      float t1 =  (numerator + root) / denominator;
	      float t2 =  (numerator - root) / denominator;
	  
	      // so, which of these is the useful point? (remember that t must lie
	      // in [0,1], and that t=0 and t=1 are already the linear bounds)
	      if(0<t1 && t1<1) { exandXBounds(bounds, calculate_bezier(t1, x1, cx1, cx2, x2)); }
	      if(0<t2 && t2<1) { exandXBounds(bounds, calculate_bezier(t2, x1, cx1, cx2, x2)); }
	    }

	    // Recompute bounds projected on the y-axis, if the control points lie outside the bounding box
	    // y-bounds. No comments this time, because the code is the same, just with 'y' instead of 'x'
	    if(cy1<bounds[MIN_Y] || cy1>bounds[MAX_Y] || cy2<bounds[MIN_Y] || cy2>bounds[MAX_Y]) {
	      float a = dcy0; float b = dcy1; float c = dcy2;
	      if(a+c != 2*b) { b+=0.01; }
	      float numerator = 2*(a - b);
	      float denominator = 2*(a - 2*b + c);
	      float quadroot = (2*b-2*a)*(2*b-2*a) - 2*a*denominator;
	      float root = (float) Math.sqrt(quadroot);
	      float t1 =  (numerator + root) / denominator;
	      float t2 =  (numerator - root) / denominator;
	      if(0<t1 && t1<1) { exandYBounds(bounds, calculate_bezier(t1, y1, cy1, cy2, y2)); }
	      if(0<t2 && t2<1) { exandYBounds(bounds, calculate_bezier(t2, y1, cy1, cy2, y2)); }
	    }

	    // and we're done, form this box's rectangle and return
	    float[] bbox = {bounds[0],bounds[1], bounds[2],bounds[3] };
	    return bbox;
	  }

	public SketchPoint getNodeBetween(float t1, float t2) {

		if (t1 > 1 || t1 < 0 || t2 > 1 || t2 < 0)
			return null;

		float totalLen = this.getlength();
		float destLen1 = totalLen * t1;
		float destLen2 = totalLen * t2;
		float length = 0;

		// /-------------------------------------------------------------------------------------------------------
		// SLOTS ON INSIDE
		// --------------------------------------------------------------------------------------------------------

		for (int i = 0; i < this.l.size() - 1; i++) {
			SketchPoint curVec = this.l.get(i);
			SketchPoint nextVec = this.l.get(i + 1);
			length += curVec.distanceTo(nextVec);

			if (length > destLen1) {
				if (length < destLen2)
					return nextVec;
				else
					return null;

			}

		}

		return null;
	}

	public GeneralPath getOutlineGeneralPath() {
		GeneralPath gPath = new GeneralPath();

		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint curVec = null;
			SketchPoint nextVec = null;

			if (i >= this.l.size() - 1) {
				nextVec = this.l.get(0);
			} else {
				nextVec = this.l.get(i + 1);
			}

			curVec = this.l.get(i);

			if (i == 0) {
				gPath.moveTo(curVec.x, curVec.y);
				// gPath.lineTo(curVec.x, curVec.y);
			}
			if (i == this.l.size() - 1) {
				gPath.moveTo(curVec.x, curVec.y);
				// gPath.lineTo(curVec.x, curVec.y);
			}

			if (curVec.containsBezier() || nextVec.containsBezier()) {

				// gPath.lineTo(vec.x, vec.y);
				// System.out.println(i + " curveTo " + vec + " " + bc1.c1 + " "
				// + bc1.c2+ " " + bc2.c1 + " " + bc2.c2);
				//gPath.lineTo(nextVec.x, nextVec.y);

				gPath.curveTo(curVec.getControlPoint2().x,
						curVec.getControlPoint2().y,
						nextVec.getControlPoint1().x,
						nextVec.getControlPoint1().y, nextVec.x, nextVec.y);
			} else {
				// System.out.println(i + "lineTo " + vec );
				gPath.lineTo(nextVec.x, nextVec.y);

			}
		}

		return gPath;

	}

	public SketchShape getOverShape(float x, float y) {
		Vec2D closePoint = getClosestPointAlongPath(x, y);
		if (closePoint != null
				&& closePoint.distanceTo(new Vec2D(x, y)) < SETTINGS_SKETCH.SELECT_EDGE_DIST) {
			this.lastMouseOverVec = closePoint;
			this.lastMouseOverPercent = getClosestPercent(x, y);
			return this;
		}
		return null;
	}

	public SketchPath getPath() {
		return this;

	}

	public float getPercent(SketchPoint p) {
		int index = getIndex(p);
		return getLengthTo(index) / this.getlength();
	}

	public Vec2D getPerpendicular(float percent) {

		Vec2D vecBefore = null;
		Vec2D vecAfter = null;

		float offsetSearch = .01f;

		if (percent - offsetSearch < 0)
			vecBefore = this.getPos((percent));
		else
			vecBefore = this.getPos((percent - offsetSearch));

		if (percent + offsetSearch > 1)
			vecAfter = this.getPos((percent));
		else
			vecAfter = this.getPos((percent + offsetSearch));

		if (vecAfter == null)
			return null;

		vecAfter = (Vec2D) vecAfter.sub(vecBefore);
		vecAfter.normalize();
		// SketchPoint newAn = vecAfter.getRotated((float)(Math.PI/2));

		if (WoundClockwise())
			vecAfter = vecAfter.rotate((float) Math.PI);

		return vecAfter;
	}

	// /-------------------------------------------------------------------------------------------------------
	// GET POS
	// --------------------------------------------------------------------------------------------------------
	public Vec2D getPos(float percent) {

		if (percent > 1 || percent < 0 || this.l.size() < 1)
			return null;

		float totalLen = this.getlength();
		float destLen = totalLen * percent;

		float length = 0;
		int offset = 0;

		if (!this.getClosed())
			offset = 1;

		Vec2D lastMeasuredPoint = (Vec2D) this.l.get(0);
		for (int i = 0; i < this.l.size() - offset; i++) {
			SketchPoint curP = this.l.get(i);
			SketchPoint nextP = null;
			if (i < this.l.size() - 1)
				nextP = this.l.get(i + 1);
			else
				nextP = this.l.get(0);

			if (curP.containsBezier() || nextP.containsBezier()) {

				float bezStep = getParentSketch().getSketchGlobals().BEZIER_DETAIL_CALCULATIONS;
				for (float t = bezStep; t < 1; t += bezStep) {
					float x = functions.bezierPoint(curP.x,
							curP.getControlPoint2().x,
							nextP.getControlPoint1().x, nextP.x, t);
					float y = functions.bezierPoint(curP.y,
							curP.getControlPoint2().y,
							nextP.getControlPoint1().y, nextP.y, t);

					length += lastMeasuredPoint.distanceTo(new Vec2D(x, y));
					lastMeasuredPoint = new Vec2D(x, y);

					if (length >= destLen)
						return new SketchPoint(x, y);

				}

			} else {
				length += curP.distanceTo(nextP);
				lastMeasuredPoint = nextP.copy();

				//LOGGER.info("percent "+percent+ " totalLen " + totalLen + " destLen "+ destLen + " length " + length);

				if (length > destLen) {

					float segLen = curP.distanceTo(nextP);
					float lastLen = length - segLen;
					float curPos = destLen - lastLen;
					float t = curPos / segLen;

					float x = curP.x + ((nextP.x - curP.x) * t);
					float y = curP.y + ((nextP.y - curP.y) * t);

					return new SketchPoint(x, y);

				}

			}

		}

		return null;
	}

	public Vec2D getPos(int index, float percent) {

		if (percent > 1 || percent < 0)
			return null;

		SketchPoint curP = this.l.get(index);
		SketchPoint nextP = this.l.get(index + 1);

		if (curP.containsBezier() || nextP.containsBezier()) {
			Vec2D bez1 = curP;
			Vec2D bez2 = nextP;

			if (curP.containsBezier()) {
				bez1 = curP.getControlPoint2();
			}

			if (nextP.containsBezier()) {
				bez2 = nextP.getControlPoint1();
			}

			float x = functions.bezierPoint(curP.x, bez1.x, bez2.x, nextP.x,
					percent);
			float y = functions.bezierPoint(curP.y, bez1.y, bez2.y, nextP.y,
					percent);

			/*
			 * To find the real point along a spline we will need to set though each percentage (t) until we find the correct len then filter back
			 */

			return new Vec2D(x, y);
		} else {
			float x = curP.x + ((nextP.x - curP.x) * percent);
			float y = curP.y + ((nextP.y - curP.y) * percent);

			return new Vec2D(x, y);
		}
	}

	public Vec2D getPosApprox(SketchPoint curP, SketchPoint nextP, float percent) {

		if (percent > 1 || percent < 0)
			return null;
		if (curP.containsBezier() || nextP.containsBezier()) {

			float x = functions.bezierPoint(curP.x, curP.getControlPoint2().x,
					nextP.getControlPoint1().x, nextP.x, percent);
			float y = functions.bezierPoint(curP.y, curP.getControlPoint2().y,
					nextP.getControlPoint1().y, nextP.y, percent);
			return new SketchPoint(x, y);

		} else {
			float x = curP.x + ((nextP.x - curP.x) * percent);
			float y = curP.y + ((nextP.y - curP.y) * percent);
			return new SketchPoint(x, y);

		}

	}

	public int getPosIndex(float percent) {

		if (percent > 1 || percent < 0)
			return 0;

		float totalLen = this.getlength();
		float destLen = totalLen * percent;

		int offset = 0;

		if (this.getClosed())
			offset = 1;

		for (int i = 0; i < this.l.size() + offset; i++) {
			if (getLengthBetween(0, i) > destLen)
				return i - 1;

		}

		return 0;
	}

	// /-------------------------------------------------------------------------------------------------------
	// GET THE POSITION A CENTAIN PERCENT ALONG THE PATH! 
	// --------------------------------------------------------------------------------------------------------
	public Vec2D getPosStep(float step) {

		lastStepPercent += step;

		float percent = lastStepPercent;

		if (percent > 1 || percent < 0)
			return null;

		float totalLen = this.getlength();

		float destLen = totalLen * percent; //|----------/----------| = .5f = total len * .5f

		float lengthToCurrentPos = lastStepLengthAlong; // what length was the previous part found at?

		int offset = 0;

		if (!this.getClosed())
			offset = 1;

		if (lastStepMeasuredPoint == null && this.l.size() > 0)
			lastStepMeasuredPoint = (Vec2D) this.l.get(0);

		for (int i = lastStepStartPos; i < this.l.size() - offset; i++) {
			SketchPoint curP = this.l.get(i);
			SketchPoint nextP = null;

			//loop around
			if (i < this.l.size() - 1)
				nextP = this.l.get(i + 1);
			else
				nextP = this.l.get(0);

			if (curP.containsBezier() || nextP.containsBezier()) {

				//LOGGER.info("percent "+percent+ " totalLen " + totalLen + " destLen "+ destLen + " lengthToCurrentPos " + lengthToCurrentPos);

				float bezStep = getParentSketch().getSketchGlobals().BEZIER_DETAIL_CALCULATIONS;
				for (float t = bezStep + lastStepAlongBezier; t < 1; t += bezStep) {

					float x = functions.bezierPoint(curP.x,
							curP.getControlPoint2().x,
							nextP.getControlPoint1().x, nextP.x, t);
					float y = functions.bezierPoint(curP.y,
							curP.getControlPoint2().y,
							nextP.getControlPoint1().y, nextP.y, t);

					//if(lastStepMeasuredPoint.distanceTo(new Vec2D(x,y)) < 100)
					//LOGGER.info("add this" + lastStepMeasuredPoint.distanceTo(new Vec2D(x,y)) + " percent " + percent );

					lengthToCurrentPos += lastStepMeasuredPoint
							.distanceTo(new Vec2D(x, y));
					lastStepMeasuredPoint = new Vec2D(x, y);

					if (lengthToCurrentPos > destLen) {
						//	lastStepStartPos = i; //remember
						lastStepAlongBezier = t;
						lastStepLengthAlong = lengthToCurrentPos;
						lastStepStartPos = i;

						//we need to find the actual intersection point here!
						return new SketchPoint(x, y);
					}

				}
				lastStepAlongBezier = 0; //gone though one segment and did not fidn a intersection so reset

			} else {

				//LOGGER.info("HERE AT ALL");

				lengthToCurrentPos += curP.distanceTo(nextP);
				//	LOGGER.info("percent "+percent+ " totalLen " + totalLen + " destLen "+ destLen + " lengthToCurrentPos " + lengthToCurrentPos);

				if (lengthToCurrentPos > destLen) {
					//		LOGGER.info("percent "+percent+ " totalLen " + totalLen + " destLen "+ destLen + " length " + length);

					float segLen = curP.distanceTo(nextP);
					float lastLen = lengthToCurrentPos - segLen;
					float curPos = destLen - lastLen;
					float t = curPos / segLen;
					//	LOGGER.info("t"+t);

					float x = curP.x + ((nextP.x - curP.x) * t);
					float y = curP.y + ((nextP.y - curP.y) * t);
					lengthToCurrentPos -= (segLen);
					lastStepLengthAlong = lengthToCurrentPos;
					lastStepStartPos = i;
					lastStepMeasuredPoint = nextP.copy();

					return new SketchPoint(x, y);

				} else {
					//	lastStepStartPos = ; //remember
				}

			}

			lastStepAlongBezier = 0; //reset to start of bezier

		}

		return null;

	}

	public Vec2D getPosStep(SketchPoint curP, SketchPoint nextP, float step) {

		lastStepPercent += step;
		float percent = lastStepPercent;

		if (percent > 1 || percent < 0)
			return null;

		if (lastStepLenghtSegment == 0)
			lastStepLenghtSegment = this.getLengthBetween(this.getIndex(curP),
					this.getIndex(nextP));

		float totalLen = lastStepLenghtSegment;

		float destLen = totalLen * percent;

		float length = lastStepLengthAlong;

		if (lastStepMeasuredPoint == null)
			lastStepMeasuredPoint = (Vec2D) curP.copy();

		if (curP.containsBezier() || nextP.containsBezier()) {

			float bezStep = getParentSketch().getSketchGlobals().BEZIER_DETAIL_CALCULATIONS;
			for (float t = lastStepAlongBezier; t <= 1; t += bezStep) {
				float x = functions.bezierPoint(curP.x,
						curP.getControlPoint2().x, nextP.getControlPoint1().x,
						nextP.x, t);
				float y = functions.bezierPoint(curP.y,
						curP.getControlPoint2().y, nextP.getControlPoint1().y,
						nextP.y, t);

				length += lastStepMeasuredPoint.distanceTo(new Vec2D(x, y));
				lastStepMeasuredPoint = new Vec2D(x, y);

				if (length > destLen) {
					/*
					LOGGER.info(" t " + t +
							" percent " + percent +
							" totalLen " + totalLen +
							" lastStepAlongBezier " + lastStepAlongBezier  +
							" lastStepLengthAlong " + lastStepLengthAlong);
					*/
					lastStepAlongBezier = t;
					lastStepLengthAlong = length;
					return new SketchPoint(x, y);
				}

			}

		} else {

			if (length > destLen) {

				float segLen = curP.distanceTo(nextP);
				float lastLen = length - segLen;
				float curPos = destLen - lastLen;
				float t = curPos / segLen;

				float x = curP.x + ((nextP.x - curP.x) * t);
				float y = curP.y + ((nextP.y - curP.y) * t);

				lastStepLengthAlong = length;
				return new SketchPoint(x, y);

			} else {
				length += curP.distanceTo(nextP);
				lastStepMeasuredPoint = nextP.copy();

			}

		}
		lastStepAlongBezier = 0; //reset to start of bezier
		return null;

	}

	public SketchPoint getSketchPointpickBuffer(int col) {

		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint curVec = this.l.get(i);
			if (col == getColor(i + (this.id * 100)))
				return curVec;
		}
		return null;
	}

	public SketchPoint getVec2DpickBuffer(int col) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Vector2D> getVectorLoop() {
		if (this.l.size() < 2)
			return null;

		ArrayList<Vector2D> loop = new ArrayList<Vector2D>();
		Vec2D prevVec = null;
		if (SETTINGS_SKETCH.build_collision_mesh_detailed) {

			float step = SETTINGS_SKETCH.build_collision_mesh_res
					/ this.getlength();

			for (float t = 0; t < 1; t += step) {
				Vec2D v = this.getPos(t);

				if (t != 0) {
					SketchPoint nodeBetween = this.getNodeBetween(t - step, t);
					if (nodeBetween != null) {
						loop.add(new Vector2D(nodeBetween.x, nodeBetween.y));
					}

				}

				if (prevVec == null || v.x != prevVec.x && v.y != prevVec.y
						&& v != null) {
					prevVec = v;
					loop.add(new Vector2D(v.x, v.y));
				}

			}

		} else {
			for (int i = 1; i < this.l.size(); i++) {
				SketchPoint v = this.l.get(i);
				if (prevVec == null || v.x != prevVec.x && v.y != prevVec.y) {
					prevVec = v;
					loop.add(new Vector2D(v.x, v.y));
				}

			}
		}

		return loop;
	}

	public int indexOf(SketchPoint vec) {
		return l.indexOf(vec);
	}

	public void insertPoint(SketchPoint closestPoint) {
		int index = this.getPosIndex(this.getClosestPercent(closestPoint.x,
				closestPoint.y));

		//System.out.println(this.getClosestPercent(closestPoint.x, closestPoint.y) +"DANGER1");
		this.l.add(index + 1, closestPoint);

	}

	public boolean intersects(SketchPath path) {
		if (intersectsCount(path) > 0)
			return true;
		else
			return false;

	}
	
	
	
	public boolean inside(SketchPath path) {
		
		SketchPoint firstPoint = this.get(0);
		if(path.inside(firstPoint))
			return true;
		else
			return false;

	}
	
	
	public boolean inside(SketchPoint point) {
		
		float x1max = this.getMaxX();
		float x1min = this.getMinX();
		float y1max = this.getMaxY();
		float y1min = this.getMinY();

		float x2max = point.x;
		float x2min = point.x;
		float y2max = point.y;
		float y2min = point.y;

		if ((x1min < x2max) && (x1max > x2min) && (y1min < y2max)
				&& (y1max > y2min)){
				return this.pointInPolygon(point);

		}else{
			return false;
		}

	}

	public int intersectsCount(SketchPath path2) {
		float x1max = this.getMaxX();
		float x1min = this.getMinX();
		float y1max = this.getMaxY();
		float y1min = this.getMinY();

		float x2max = path2.getMaxX();
		float x2min = path2.getMinX();
		float y2max = path2.getMaxY();
		float y2min = path2.getMinY();

		if ((x1min < x2max) && (x1max > x2min) && (y1min < y2max)
				&& (y1max > y2min)

		) {

			return intersectsPath(path2);

			//return true;
		} else {
			return 0;
		}
		/*
		 * } for (int p1 = 0; p1 < this.l.size()-1; p1++) { SketchPoint v = (SketchPoint)
		 * this.l.get(p1); SketchPoint newVec = new SketchPoint(v.x, v.y);
		 */

	}

	int intersectsPath(SketchPath otherPath) {
		int intersectCount = 0;

		int loop = 0;
		if (this.getClosed())
			loop = 1;

		int loop2 = 0;

		if (otherPath.getClosed())
			loop2 = 1;

		for (int i = 1; i < this.l.size() + loop; i++) {
			SketchPoint curVec = null;
			SketchPoint preVec = null;

			// --- last or first point ---
			if (i == 1) {
				curVec = this.l.get(0);
			}

			if (i >= 1)
				preVec = this.l.get(i - 1);

			if (i == this.l.size()) {
				curVec = this.l.get(0);
				preVec = this.l.get(this.l.size() - 1);
			} else {
				curVec = this.l.get(i);
			}

			for (int i2 = 1; i2 < otherPath.l.size() + loop2; i2++) {
				SketchPoint curVec2 = null;
				SketchPoint preVec2 = null;

				// --- last or first point ---
				if (i2 == 1) {
					curVec2 = otherPath.l.get(0);
				}

				if (i2 >= 1)
					preVec2 = otherPath.l.get(i2 - 1);

				if (i2 == otherPath.l.size()) {
					curVec2 = otherPath.l.get(0);
					preVec2 = otherPath.l.get(otherPath.l.size() - 1);
				} else {
					curVec2 = otherPath.l.get(i2);
				}

				if (functions.intersect(curVec.x, curVec.y, preVec.x, preVec.y,
						curVec2.x, curVec2.y, preVec2.x, preVec2.y) == functions.DO_INTERSECT)
					intersectCount++;
			}

		}
		return intersectCount;

	}

	public boolean isPointInside(Vec2D p) {
		return this.pointInPolygon(p);
		// return this.path.isPointInside(p);
	}

	public void mouseDragged(float mouseX, float mouseY) {

		Vec2D pointOnPlane = new Vec2D(mouseX, mouseY);
		if ((getParentSketch().getSketchTools().getCurrentTool() == SketchTools.SELECT_TOOL || getParentSketch()
				.getSketchTools().getCurrentTool() == SketchTools.SELECT_BEZIER_TOOL)
				&& getParentSketch().getSketchTools().getMouseButton() == SketchTools.MOUSE_LEFT) {

			for (int i = 0; i < this.getSelectedNodes().size(); i++) {
				Object o = this.getSelectedNodes().get(i);

				if (o instanceof SketchPoint) {
					SketchPoint v = (SketchPoint) o;
					if (v.containsBezier()) {

						Vec2D delta = v.sub(pointOnPlane);
						//System.out.println(pointOnPlane);

						v.controlPoint1.subSelf(delta);
						v.controlPoint2.subSelf(delta);

					}
					v.set(pointOnPlane.x, pointOnPlane.y);

				}

				//is a bezier point your dragging
				if (o instanceof Vec2D) {
					Vec2D v = (Vec2D) o;
					v.x = pointOnPlane.x;
					v.y = pointOnPlane.y;

					//if dragging with the select bezier tool also mirror the opposite bezzier
					if (getParentSketch().getSketchTools().getCurrentTool() == SketchTools.SELECT_BEZIER_TOOL) {
						//Vec2D otherBez = (Vec2D)o;

					}

				}

				this.resetCachedVariables(); //forget cached length

			}
		}

	}

	public void mouseReleased(float mouseX, float mouseY) {
		//if(getParentSketch().getSketchTools().getMouseButton() == PConstants.RIGHT)
		//	this.closed = true;
	}

	public void movePoint(SketchPoint selectedVec, Vec2D planePoint) {
		// TODO Auto-generated method stub

	}

	public void offset() {

	}

	public void optimize() {
		/*
		 * if(this.l.size() < 3) return;
		 * 
		 * SketchPoint lastStoredPoint = (SketchPoint) this.l.get(0); List optimizedArray =
		 * new ArrayList(); optimizedArray.add(lastStoredPoint);
		 * 
		 * float step = SETTINGS_SKETCH.spline_point_every / this.getlength(); for
		 * (float i = 0; i < 1; i += step) {
		 * 
		 * optimizedArray.add(this.getPos(i)); }
		 * 
		 * this.l = optimizedArray;
		 */
		// this.offset();
	}

	// does this Polygon contain the point p?
	// if p is on boundary then 0 or 1 is returned, and p is in exactly one
	// point of every partition of plane
	// Reference: http://exaflop.org/docs/cgafaq/cga2.html
	public boolean pointInPolygon(Vec2D p) {
		int crossings = 0;
		for (int i = 0; i < this.l.size() - 1; i++) {
			SketchPoint curVec = this.l.get(i);
			SketchPoint nextVec = this.l.get(i + 1);
			double slope = (nextVec.x - curVec.x) / (nextVec.y - curVec.y);
			boolean cond1 = (curVec.y <= p.y) && (p.y < nextVec.y);
			boolean cond2 = (nextVec.y <= p.y) && (p.y < curVec.y);
			boolean cond3 = p.x < slope * (p.y - curVec.y) + curVec.x;
			if ((cond1 || cond2) && cond3)
				crossings++;
		}
		return (crossings % 2 != 0);
	}

	public void remove(int i) {
		this.l.remove(i);
		this.resetCachedVariables();
	}

	public void remove(SketchPoint point) {
		l.remove(point);
	}

	public void removeVertex(SketchPoint v) {
		if (this.getClosed() && this.l.contains(v))
			this.l.remove(v);

		if (this.l.size() == 0)
			this.destroy();

	}

	public void render(PGraphics g) {
		
		if (this.isOutline) {
			//return;
			
		}
		
		
		//TODO:SET according to render mode 

		//if(isConstructionLine())
		//	return;

		//if (!this.getClosed() && !isConstructionLine())
		//	this.select();

		//if we are on a selected layer
		if (!getParentSketch().screenshot
				&& this.getParentSketch().getLayerSelected()) {
			g.fill(SETTINGS_SKETCH.SKETCHSHAPE_FILL_SELECTED_COLOUR);
			g.strokeWeight(SETTINGS_SKETCH.SKETCHSHAPE_FILL_SELECTED_WEIGHT);
			g.stroke(SETTINGS_SKETCH.SKETCHSHAPE_PATH_COLOUR_SELECTED);
			//g.stroke(SETTINGS_SKETCH.SKETCHSHAPE_PATH_COLOUR_UNSELECTED);	
		} else {

			//don't render details on unselected layers! 
			g.noStroke();
			//g.noFill();
			g.fill(SETTINGS_SKETCH.SKETCHSHAPE_FILL_UNSELECTED_COLOUR);
			//g.strokeWeight(SETTINGS_SKETCH.SKETCHOUTLINE_UNSELECTED_WEIGHT);
			//g.stroke(SETTINGS_SKETCH.SKETCHSHAPE_PATH_COLOUR_UNSELECTED);

		}

		// RENDER MODES
		switch (getParentSketch().getRenderMode()) {

		//#IF JAVA
		/*	3D preview
		 *  Used to render the 3D preview in the top right corner of the screen
		 */
		//#ENDIF JAVA

		case Sketch.RENDER_3D_PREVIW:
			
			//don't review guides
			//if(getParentSketch().getOnSketchPlane().guide)
			//return;


			if (this.getParentSketch().getLayerSelected())
				g.fill(SETTINGS_SKETCH.SKETCHSHAPE_FILL_SELECTED_DIAGRAM_COLOUR);
			else
				g.fill(SETTINGS_SKETCH.SKETCHSHAPE_FILL_DIAGRAM_COLOUR);

			g.noStroke();

			if (this.isOutline) {
				g.stroke(SETTINGS_SKETCH.SKETCHOUTLINE_PATH_COLOUR_DIAGRAM);
				g.strokeWeight(SETTINGS_SKETCH.SKETCHOUTLINE_PATH_WEIGHT_DIAGRAM
						* SETTINGS_SKETCH.PATH_WIDTH_ZOOM);
				g.noFill();
			}

			float outlineOffset2 = 0;
			if (this.isOutline)
				outlineOffset2 = 0.1f;

			this.renderFace(g);

			break;
			
			//#IF JAVA
		/*	3D Normal
		 *  Nothing is being selected or edited 
		 */
			//#ENDIF JAVA

		case Sketch.RENDER_3D_NORMAL: {

			
			//don't review guides
			if(getParentSketch().getOnSketchPlane().guide)
			return;
			
			
			float extrudeDepth;
					if(this.getParentSketch().getOnSketchPlane() != null)
						extrudeDepth = 	(this.getParentSketch().getOnSketchPlane().thickness / 2)
					/ SETTINGS_SKETCH.scale;
					else
						extrudeDepth =0;

			//We might want to use a user selected chair colour in here again
			if (this.isOutline ) {

				g.fill(SETTINGS_SKETCH.RENDER_3D_NORMAL_SKETCHSHAPE_SIDE_FILL_COLOUR);
				renderSide(g, extrudeDepth * 2);

				g.noFill();
				g.stroke(SETTINGS_SKETCH.RENDER_3D_NORMAL_SKETCHOUTLINE_STROKE_COLOUR);
				g.strokeWeight(SETTINGS_SKETCH.RENDER_3D_NORMAL_SKETCHOUTLINE_STROKE_WEIGHT);

				g.pushMatrix();
				g.translate(0, 0, extrudeDepth
						+ SETTINGS_SKETCH.OUTLINE_RENDER_OFFSET);
				this.renderFace(g);
				g.popMatrix();

				g.pushMatrix();
				g.translate(0, 0,
						-(extrudeDepth + SETTINGS_SKETCH.OUTLINE_RENDER_OFFSET));
				this.renderFace(g);
				g.popMatrix();
			} else {

	
				g.fill(SETTINGS_SKETCH.RENDER_3D_NORMAL_SKETCHSHAPE_FILL_COLOUR);

				g.pushMatrix();
				g.translate(0, 0, extrudeDepth);
				this.renderFace(g);
				g.popMatrix();

				g.pushMatrix();
				g.translate(0, 0, -(extrudeDepth));
				this.renderFace(g);
				g.popMatrix();
			}
		}

			break;

		case Sketch.RENDER_3D_EDITING_PLANES: {

			float extrudeDepth = 0;
			
			if(this.getParentSketch().getOnSketchPlane() != null)
				extrudeDepth = (this.getParentSketch().getOnSketchPlane().thickness / 2)
					/ SETTINGS_SKETCH.scale;
			float offsetSize = 0;

			//if (!getParentSketch().getLayerSelected())
			//	g.stroke(SETTINGS_SKETCH.SKETCHSHAPE_PATH_COLOUR_UNSELECTED_LAYER);

			/*
			if ((getParentSketch().getSketchTools().getCurrentTool() == SketchTools.SELECT_TOOL || getParentSketch()
					.getSketchTools().getCurrentTool() == SketchTools.SELECT_BEZIER_TOOL)
					&& !getParentSketch().screenshot)
					g.noFill();
			*/

			if (this.isOutline) {

				if (this.getParentSketch().getLayerSelected()  ) {

					g.fill(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHSHAPE_SIDE_FILL_COLOUR_SELECTED);

					//only render if we are displaying in 3D
					if (getParentSketch().getRender3D())
						renderSide(g, extrudeDepth * 2);

					g.noFill();
					g.stroke(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHOUTLINE_STROKE_COLOUR_SELECTED);
					g.strokeWeight(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHOUTLINE_STROKE_WEIGHT_SELECTED);
					offsetSize = SETTINGS_SKETCH.OUTLINE_RENDER_OFFSET;

				} else {

					if (getParentSketch().getRender3D()) {
						
						g.stroke(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHOUTLINE_STROKE_COLOUR_UNSELECTED);

						//g.fill(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHSHAPE_SIDE_FILL_COLOUR_UNSELECTED);
						//renderSide(g, extrudeDepth * 2);
						//g.noStroke();
						g.noFill();	
					} else {

						//g.fill(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHSHAPE_SIDE_FILL_COLOUR_UNSELECTED);
						//renderSide(g, extrudeDepth * 2);

						g.noFill();
						g.stroke(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHOUTLINE_STROKE_COLOUR_UNSELECTED);
						g.strokeWeight(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHOUTLINE_STROKE_WEIGHT_UNSELECTED);
						offsetSize = SETTINGS_SKETCH.OUTLINE_RENDER_OFFSET;
					}
				}

				//only render if we are displaying in 3D
				if (getParentSketch().getRender3D()) {

					//3D
					g.pushMatrix();
					g.translate(0, 0, extrudeDepth + offsetSize);
					this.renderFace(g);
					g.popMatrix();

					g.pushMatrix();
					g.translate(0, 0, -(extrudeDepth + offsetSize));
					this.renderFace(g);
					g.popMatrix();
				} else {
					//2D
					g.pushMatrix();
					g.translate(0, 0, offsetSize);
					this.renderFace(g);
					g.popMatrix();
					
					
				}

				//NOT OUTLINE
			} else {

				//we don't draw internal shapes on unselected layers at in 2D.
				if (!this.getParentSketch().getLayerSelected()
						&& getParentSketch().getRender3D())
					return;

				if (this.getParentSketch().getLayerSelected()) {
					if (this.selected || this.highlighted) {
						g.noFill();
						g.stroke(SETTINGS_SKETCH.SKETCHSHAPE_PATH_COLOUR_SELECTED);
						g.strokeWeight(SETTINGS_SKETCH.SKETCHSHAPE_PATH_WEIGHT_SELECTED);
						offsetSize = SETTINGS_SKETCH.OUTLINE_RENDER_OFFSET * 2;
					} else {
						g.fill(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHSHAPE_FILL_COLOUR_SELECTED);
						g.stroke(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHSHAPE_STROKE_COLOUR_SELECTED);
						g.strokeWeight(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHSHAPE_STROKE_WEIGHT_SELECTED);
						offsetSize = 0;
					}
					
					if(this.highlighted){
						//g.stroke(SETTINGS_SKETCH.SKETCHSHAPE_PATH_COLOUR_SELECTED);
						//offsetSize = SETTINGS_SKETCH.OUTLINE_RENDER_OFFSET * 3;
					}
					
				} else {

					if (getParentSketch().getRender3D()) {
						g.fill(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHSHAPE_FILL_COLOUR_UNSELECTED);
						g.noStroke();
						offsetSize = 0;
					} else {
						g.fill(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHSHAPE_FILL_COLOUR_UNSELECTED);
						g.stroke(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHSHAPE_STROKE_COLOUR_UNSELECTED);
						g.strokeWeight(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHSHAPE_STROKE_WEIGHT_UNSELECTED);
						offsetSize = 0;
					}
				}

				//g.noStroke();
				//				g.stroke(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHOUTLINE_STROKE_COLOUR_UNSELECTED);
				//				g.strokeWeight(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_SKETCHOUTLINE_STROKE_WEIGHT_UNSELECTED);

				if ((SETTINGS_SKETCH.SLICEPLACE_RENDER_VOLUME)
						&& this.getParentSketch() != null
						&& this.getParentSketch().getOnSketchPlane().thickness > SETTINGS.MIN_RENDER_WIDTH) {

					g.noFill();

					//only render if we are displaying in 3D
					if (getParentSketch().getRender3D()) {
						//render the face of the chairs
						g.pushMatrix();
						g.translate(0, 0, extrudeDepth + offsetSize);
						this.renderFace(g);
						g.popMatrix();

						g.pushMatrix();
						g.translate(0, 0, -(extrudeDepth + offsetSize));
						this.renderFace(g);
						g.popMatrix();
					} else {
						//2D
						g.pushMatrix();
						g.translate(0, 0, offsetSize);
						this.renderFace(g);
						g.popMatrix();
					}

				} else {
					//to thin so we only need to rend
					g.noFill();
					g.pushMatrix();
					g.translate(0, 0, offsetSize);
					this.renderFace(g);
					g.popMatrix();
				}
			}

			if (this.getParentSketch().getLayerSelected()) {

				g.pushMatrix();
				//g.translate(0, 0, extrudeDepth
				//		+ (SETTINGS_SKETCH.OUTLINE_RENDER_OFFSET * 2));
				float d = extrudeDepth
						+ (SETTINGS_SKETCH.OUTLINE_RENDER_OFFSET * 2);
				g.translate(0, 0,d);
				
				
				if (this.editable
						&& !getParentSketch().getSketchGlobals().renderScreenshot
						&& getParentSketch().getSketchTools().getCurrentTool() == SketchTools.SELECT_BEZIER_TOOL) {
					if (this.getType() == SketchShape.TYPE_SPLINE)
						renderNodesSmoothedSpline(g);
					else
						renderNodes(g);

				}

				if ((this.editable && (getParentSketch().getSketchTools()
						.getCurrentTool() == SketchTools.SELECT_TOOL || getParentSketch()
						.getSketchTools().getCurrentTool() == SketchTools.SELECT_BEZIER_TOOL ||
						getParentSketch()
						.getSketchTools().getCurrentTool() == SketchTools.DRAW_PATH_TOOL))
						&& !getParentSketch().getSketchGlobals().renderScreenshot) {
					if (this.getType() == SketchShape.TYPE_SPLINE)
						renderNodesSmoothedSpline(g);
					else
						renderNodesOutline(g);

				}

				g.popMatrix();
			}

		}
			break;

		case Sketch.RENDER_3D_DIAGRAM:
			float outlineOffset = 0;
			float extrudeDepth = this.getParentSketch().getOnSketchPlane().thickness / 2;
			extrudeDepth /= SETTINGS_SKETCH.scale;

			g.fill(SETTINGS_SKETCH.SKETCHSHAPE_FILL_DIAGRAM_COLOUR);
			g.noStroke();

			
			if (this.isOutline) {
				g.noStroke();
				g.fill(SETTINGS_SKETCH.SKETCHSHAPE_FILL_DIAGRAM_COLOUR);
				renderSide(g, extrudeDepth * 2);
				g.noFill();
			}
			
			
			if (this.isOutline) {
				g.stroke(SETTINGS_SKETCH.SKETCHOUTLINE_PATH_COLOUR_DIAGRAM);
				g.strokeWeight(SETTINGS_SKETCH.SKETCHOUTLINE_PATH_WEIGHT_DIAGRAM
						* SETTINGS_SKETCH.PATH_WIDTH_ZOOM);
				g.noFill();
			}

			if (this.isOutline)
				outlineOffset = SETTINGS_SKETCH.OUTLINE_RENDER_OFFSET ;



			g.pushMatrix();
			g.translate(0, 0, extrudeDepth + outlineOffset);
			this.renderFace(g);
			g.popMatrix();

			g.pushMatrix();
			g.translate(0, 0, -(extrudeDepth + outlineOffset));
			this.renderFace(g);
			g.popMatrix();

			break;

		case Sketch.RENDER_EDIT_SELECT:
			LOGGER.info("RENDER_EDIT_SELECT"+Math.random());

			break;

		}
/*
		if (SETTINGS.DEBUG) {
			Vec2D p = this.getPos(this.debugPercent);
			if (p != null)
				g.ellipse(p.x, p.y, 25, 25);

		}
*/
	}

	private void removeBeziers() {
		for (int i = 0; i < this.l.size() - 1; i++) {
			SketchPoint curVec = this.l.get(i);
			curVec.controlPoint1 = null;
			curVec.controlPoint2 = null;

		}
	}

	protected void renderFace(PGraphics g) {
		g.beginShape();


		int loop = 0;
		if (this.getClosed() &&(this.l.size() > 0 && (this.l.get(0).containsBezier() || this.l.get(this.l.size()-1).containsBezier())))
			loop = 1;

		if (!this.getClosed())
			g.noFill();

		for (int i = 1; i < this.l.size() + loop; i++) {
			SketchPoint curVec = null;
			SketchPoint preVec = null;

			// --- last or first point ---
			if (i == 1) {
				curVec = this.l.get(0);
				g.vertex(curVec.x, curVec.y);
			}

			if (i >= 1)
				preVec = this.l.get(i - 1);

			if (i == this.l.size()) {
				curVec = this.l.get(0);
				preVec = this.l.get(this.l.size() - 1);
			} else {
				curVec = this.l.get(i);
			}

			if (curVec.containsBezier() || preVec != null
					&& preVec.containsBezier()) {

				Vec2D c1 = (SketchPoint) preVec;
				Vec2D c2 = (SketchPoint) curVec;

				if (c1 == null)
					c1 = new SketchPoint(0, 0);

				if (preVec != null && preVec.containsBezier()) 
				{
					c1 = preVec.getControlPoint2();
				}

				if (curVec.containsBezier()) {
					c2 = curVec.getControlPoint1();
				}

				if (c1 != null && c2 != null && curVec != null)
					g.bezierVertex(c1.x, c1.y, c2.x, c2.y, curVec.x, curVec.y);

			} else {
				if (SETTINGS_SKETCH.Draw_Curves) {
					g.curveVertex(curVec.x, curVec.y);
				} else {
					g.vertex(curVec.x, curVec.y);
				}
			}

		}
		if (this.getClosed())
			g.endShape(PConstants.CLOSE);
		else
			g.endShape(PConstants.OPEN);

		if (getParentSketch().getSketchTools().getCurrentTool() == SketchTools.DRAW_PATH_TOOL
				&& !this.getClosed() && this.selected && this.getLength() > 0) {

			Vec2D firstPoint = this.get(0);
			Vec2D mousePos = new Vec2D(
					getParentSketch().getSketchTools().mouseX,
					getParentSketch().getSketchTools().mouseY);

			//#IF JAVA
			if (getParentSketch().getOnSketchPlane() != null)
				mousePos = GLOBAL.uiTools.getPointOnPlane(mousePos,
						getParentSketch().getOnSketchPlane().getPlane());
			//#ENDIF JAVA
			
			if (firstPoint.distanceTo(mousePos) < SETTINGS_SKETCH.MIN_CLOSE_SHAPE_DIST)
				g.ellipse(firstPoint.x, firstPoint.y, 20, 20);

		}

	}

	private void renderNodesSmoothedSpline(PGraphics g) {
		float selectDia = (float) (SETTINGS_SKETCH.select_dia
				* (1 / getParentSketch().getSketchTools().zoom));

		if (selectDia > SETTINGS_SKETCH.select_dia * 1.5f)
			selectDia = SETTINGS_SKETCH.select_dia;

		selectDia = selectDia / 2;

		// g.fill(SETTINGS_SKETCH.sChair_controle_points_fill);
		g.noFill();
		g.stroke(SETTINGS_SKETCH.CONTROL_POINT_STROKE_COLOUR);



		g.noFill();
		g.beginShape();
		g.strokeWeight(SETTINGS_SKETCH.CONTROL_SPLINE_WEIGHT);

		//find if we are over a select point on the editble curve
		for (int i = 1; i < size() - 1; i++) {
			SketchPoint curVec = (SketchPoint) get(i);
			if (curVec.isOver) {
				g.strokeWeight(SETTINGS_SKETCH.CONTROL_SPLINE_WEIGHT * 1.5f);
				curVec.isOver = false;
			}
		}

		for (int i = 0; i < size(); i++) {
			Vec2D curVec = (Vec2D) get(i);

			g.vertex(curVec.x, curVec.y);

			// 
			// selectDia);
		}
		g.endShape();
		
		g.fill(SETTINGS_SKETCH.CONTROL_POINT_FILL_COLOUR);

		
		SketchPoint startVect = (SketchPoint) get(0);
		if (startVect != null) {
			if (startVect.isOver) {
				g.rect(startVect.x - ((selectDia * 2.5f)/2), startVect.y- ((selectDia * 2.5f)/2), (selectDia * 2.5f),
						selectDia * 2.5f);
				startVect.isOver = false;
			} else
				g.rect(startVect.x - ((selectDia * 1.5f)/2), startVect.y - ((selectDia * 1.5f)/2), (selectDia * 1.5f),
						(selectDia * 1.5f));

		}

		SketchPoint endVect = (SketchPoint) get(size() - 1);
		if (endVect != null) {
			if (endVect.isOver) {
				g.rect(endVect.x- ((selectDia * 2.5f)/2), endVect.y- ((selectDia * 2.5f)/2), selectDia * 2.5f,
						selectDia * 2.5f);
				endVect.isOver = false;
			} else
				g.rect(endVect.x- ((selectDia * 1.5f)/2), endVect.y- ((selectDia * 1.5f)/2), selectDia * 1.5f,
						selectDia * 1.5f);

		}
		
		

		g.noFill();

	}

	public void renderNodes(PGraphics g) {
		if (getParentSketch().getSketchTools().renderNodesFlag) {
			g.strokeWeight(1);
			// draw selection points
			for (int i = 0; i < this.l.size(); i++) {
				SketchPoint curVec = this.l.get(i);
				//g.noFill();

				g.strokeWeight(1);
				g.stroke(SETTINGS_SKETCH.CONTROL_POINT_STROKE_COLOUR);
				// g.ellipse(curVec.x, curVec.y, 5, 5);
				g.fill(SETTINGS_SKETCH.CONTROL_POINT_STROKE_COLOUR);

				if (curVec.containsBezier()) {

					if (curVec.controlPoint1 != null) {
						g.line(curVec.x, curVec.y, curVec.controlPoint1.x,
								curVec.controlPoint1.y);
						g.ellipse(curVec.controlPoint1.x,
								curVec.controlPoint1.y, 3, 3);
					}
					if (curVec.controlPoint2 != null) {
						g.line(curVec.x, curVec.y, curVec.controlPoint2.x,
								curVec.controlPoint2.y);
						g.ellipse(curVec.controlPoint2.x,
								curVec.controlPoint2.y, 3, 3);
					}
				}

				if (curVec.isOver) {
					g.ellipse(curVec.x, curVec.y, 10, 10);
					curVec.isOver = false;
				}

			}
		}
		/*
		 * //draw selection points for (int i = 0; i < this.bezierPoints.size();
		 * i++) { BezierControlNode node = (BezierControlNode)
		 * this.bezierPoints.get(i); BezierControlNode node =
		 * (BezierControlNode) this.bezierPoints.(i);
		 * g.ellipse(curVec.x,curVec.y,5,5); }
		 */

	}

	
	public void renderDebug(PGraphics g) {
			g.strokeWeight(1);
			// draw selection points
			for (int i = 0; i < this.l.size(); i++) {
				SketchPoint curVec = this.l.get(i);
				//g.noFill();

				g.strokeWeight(1);
				g.stroke(SETTINGS_SKETCH.CONTROL_POINT_STROKE_COLOUR);
				// g.ellipse(curVec.x, curVec.y, 5, 5);
				g.fill(SETTINGS_SKETCH.CONTROL_POINT_STROKE_COLOUR);

				if (curVec.containsBezier()) {

					if (curVec.controlPoint1 != null) {
						g.line(curVec.x, curVec.y, curVec.controlPoint1.x,
								curVec.controlPoint1.y);
						g.ellipse(curVec.controlPoint1.x,
								curVec.controlPoint1.y, 0.1f, 0.1f);
					}
					if (curVec.controlPoint2 != null) {
						g.line(curVec.x, curVec.y, curVec.controlPoint2.x,
								curVec.controlPoint2.y);
						g.ellipse(curVec.controlPoint2.x,
								curVec.controlPoint2.y, 0.1f, 0.1f);
					}
				}

				

g.rect(curVec.x,curVec.y,0.5f,0.5f);
		}
	
	}

	
	
	private void renderNodesOutline(PGraphics g) {
		

		
		
		g.noStroke();
		float selectDia = (float) (SETTINGS_SKETCH.select_dia
				* (1 / getParentSketch().getSketchTools().zoom));

		if (selectDia > SETTINGS_SKETCH.select_dia * 1.5f)
			selectDia = SETTINGS_SKETCH.select_dia;

		selectDia = selectDia / 2;

		if (!getParentSketch().getSketchTools().mouseDown) {
			g.fill(SETTINGS_SKETCH.CONTROL_POINT_FILL_COLOUR);
			//g.noFill();
			

			//g.strokeWeight(1);
			//g.stroke(SETTINGS_SKETCH.sChair_controle_points_fill);
			for (int i = 0; i < this.l.size(); i++) {
				SketchPoint curVec = this.l.get(i);

				g.fill(SETTINGS_SKETCH.CONTROL_POINT_FILL_COLOUR);

				if (this.getSelectedNodes().contains(curVec))
					g.fill(SETTINGS_SKETCH.CONTROL_POINT_FILL_SELECTED_COLOUR);

				
				g.strokeWeight(1);
				g.stroke(SETTINGS_SKETCH.CONTROL_POINT_STROKE_COLOUR);
				//if(i == 0)
				//		g.ellipse(curVec.x, curVec.y, 100, 100);

				//g.ellipse(curVec.x, curVec.y, selectDia, selectDia);
				// g.ellipse(curVec.x, curVec.y, selectDia,
				// selectDia);
				

				if (curVec.isOver) {
				float overDia = selectDia*1.5f;
				g.rect(curVec.x-(overDia/2), curVec.y-(overDia/2), overDia, overDia);
				curVec.isOver = false;
				}else{
					g.rect(curVec.x-(selectDia/2), curVec.y-(selectDia/2), selectDia, selectDia);
				}
				
				

			}

			//g.noFill();
		}
	}

	public void renderPickBuffer(PGraphics g) {
		renderSilhouette(g);
		int fill = g.fillColor;
		float extrudeDepth = this.getParentSketch().getOnSketchPlane().thickness / 2;
		extrudeDepth /= SETTINGS_SKETCH.scale;
		renderSide(g, extrudeDepth * 2);

		/*
		 * g.noStroke(); for (int i = 0; i < this.l.size(); i++) { SketchPoint curVec
		 * = (SketchPoint) this.l.get(i); g.fill(getColor(i + (this.id * 100)));
		 * g.pushMatrix(); g.translate(curVec.x, curVec.y); g.sphereDetail(4);
		 * g.sphere(4); g.popMatrix(); }
		 */

	}

	void renderSide(PGraphics g, float width) {

		float halfWidth = width / 2;
		g.noStroke();
		g.beginShape(PConstants.TRIANGLES);
		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint curPoint = null;
			SketchPoint prevPoint = null;

			curPoint = this.l.get(i);

			if (i == 0)
				prevPoint = l.get(l.size() - 1);
			else
				prevPoint = l.get(i - 1);

			//If we are drawing a bezier edge we now need to step along the bezier

			if (prevPoint.controlPoint2 != null
					|| curPoint.controlPoint1 != null) {

				float dist = prevPoint.distanceTo(curPoint);
				float step = SETTINGS_SKETCH.RENDER_PIXELS_PER_TRIANGLE_BEZIER
						/ dist;

				float prevX = prevPoint.x;
				float prevY = prevPoint.y;
				for (float t = 0; t <= 1; t += step) {

					float bx = functions.bezierPoint(prevPoint.x,
							prevPoint.getControlPoint2().x,
							curPoint.getControlPoint1().x, curPoint.x, t);

					float by = functions.bezierPoint(prevPoint.y,
							prevPoint.getControlPoint2().y,
							curPoint.getControlPoint1().y, curPoint.y, t);

					g.vertex(prevX, prevY, halfWidth);
					g.vertex(prevX, prevY, -halfWidth);
					g.vertex(bx, by, -halfWidth);

					g.vertex(bx, by, -halfWidth);
					g.vertex(bx, by, halfWidth);
					g.vertex(prevX, prevY, halfWidth);

					prevX = bx;
					prevY = by;
				}

			} else {

				g.vertex(prevPoint.x, prevPoint.y, halfWidth);
				g.vertex(prevPoint.x, prevPoint.y, -halfWidth);
				g.vertex(curPoint.x, curPoint.y, -halfWidth);

				g.vertex(curPoint.x, curPoint.y, -halfWidth);
				g.vertex(curPoint.x, curPoint.y, halfWidth);
				g.vertex(prevPoint.x, prevPoint.y, halfWidth);
			}

		}
		g.endShape(PConstants.CLOSE);
	}

	public void renderSilhouette(PGraphics g) {

		g.beginShape();

		for (int i = 1; i < this.l.size() + 1; i++) {
			SketchPoint curVec = null;
			SketchPoint preVec = null;

			// --- last or first point ---
			if (i == 1) {
				curVec = this.l.get(0);
				g.vertex(curVec.x, curVec.y);
			}

			if (i >= 1)
				preVec = this.l.get(i - 1);

			if (i == this.l.size()) {
				curVec = this.l.get(0);
				preVec = this.l.get(this.l.size() - 1);

			} else {
				curVec = this.l.get(i);
			}

			if (curVec.containsBezier() || preVec != null
					&& preVec.containsBezier()) {

				Vec2D c1 = preVec;
				Vec2D c2 = curVec;

				if (c1 == null)
					c1 = new SketchPoint(0, 0);

				if (preVec != null && preVec.containsBezier()) {

					c1 = preVec.getControlPoint2();
				}

				if (curVec.containsBezier()) {
					c2 = curVec.getControlPoint1();
				}

				if (c1 != null && c2 != null && curVec != null)
					g.bezierVertex(c1.x, c1.y, c2.x, c2.y, curVec.x, curVec.y);

				/*
				 * // --- last or first point --- if (i == this.l.size()-1 ) {
				 * curVec = (SketchPoint) this.l.get(0); g.vertex(curVec.x, curVec.y);
				 * }
				 */

			} else {
				if (SETTINGS_SKETCH.Draw_Curves) {
					g.curveVertex(curVec.x, curVec.y);
				} else {
					g.vertex(curVec.x, curVec.y);
				}
			}

		}

		g.endShape();
	}

	public void replace(SketchShape objClone) {
		// TODO Auto-generated method stub

	}

	public void reset() {
		this.l = new ArrayList<SketchPoint>();
	}

	public void resetCachedVariables() {
		cachedLength = -1;
		cachedGetMaxX = -1;
		cachedGetMaxY = -1;
		cachedGetMinX = -1;
		cachedGetMinY = -1;
	}

	public void resetPosStep() {

		lastStepStartPos = 0;
		lastStepPercent = 0;
		lastStepLengthAlong = 0;
		lastStepAlongBezier = 0;
		lastStepMeasuredPoint = null;
		lastStepLenghtSegment = 0;
	}

	public void scale(float scale, toxi.geom.Vec3D centre) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint curVec = this.l.get(i);

			LOGGER.info("-> s " + scale);

			LOGGER.info("-> 1 " + curVec.x);

			curVec.x += (curVec.x - centre.x) * scale;
			LOGGER.info("-> 2 " + curVec.x);

			LOGGER.info("");

			curVec.y += (curVec.y - centre.y) * scale;

			if (curVec.containsBezier()) {

				if (curVec.controlPoint1 != null) {
					curVec.controlPoint1.x += (curVec.controlPoint1.x - centre.x)
							* scale;
					curVec.controlPoint1.y += (curVec.controlPoint1.y - centre.y)
							* scale;
				}

				if (curVec.controlPoint2 != null) {
					curVec.controlPoint2.x += (curVec.controlPoint2.x - centre.x)
							* scale;
					curVec.controlPoint2.y += (curVec.controlPoint2.y - centre.y)
							* scale;
				}
			}

		}
	}

	public void scale(float scale) {
		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint curVec = this.l.get(i);

			curVec.scaleSelf(scale);

			if (curVec.containsBezier()) {

				if (curVec.controlPoint1 != null) {
					curVec.controlPoint1.scaleSelf(scale);
				}

				if (curVec.controlPoint2 != null) {
					curVec.controlPoint2.scaleSelf(scale);

				}
			}

		}
		resetCachedVariables();
	}

	public void rotate(float r) {
		this.rotate(r, this.getCentre());
	}

	public void rotate(float r, Vec2D centre) {

		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint curVec = this.l.get(i);

			Vec2D v = functions.rotate(curVec, centre, r);
			curVec.x = v.x;
			curVec.y = v.y;

			if (curVec.containsBezier()) {

				if (curVec.controlPoint1 != null) {
					curVec.controlPoint1 = (Vec2D) functions.rotate(
							curVec.controlPoint1, centre, r);
				}

				if (curVec.controlPoint2 != null) {
					curVec.controlPoint2 = (Vec2D) functions.rotate(
							curVec.controlPoint2, centre, r);

				}
			}

		}
		resetCachedVariables();

	}

	public void translate(Vec2D delta) {

		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint curVec = this.l.get(i);
			curVec.x += delta.x;
			curVec.y += delta.y;

			if (curVec.containsBezier()) {

				if (curVec.controlPoint1 != null) {
					curVec.controlPoint1.x += delta.x;
					curVec.controlPoint1.y += delta.y;
				}

				if (curVec.controlPoint2 != null) {
					curVec.controlPoint2.x += delta.x;
					curVec.controlPoint2.y += delta.y;
				}
			}

		}
		resetCachedVariables();
	}

	public void select() {
		this.selected = true;
	}

	public void selectNodes(float mouseX, float mouseY) {

		if (getParentSketch().getSketchTools().getCurrentTool() == SketchTools.SELECT_TOOL) {
			this.unselectNodes();
			for (int i = 0; i < this.l.size(); i++) {
				SketchPoint v = this.l.get(i);

				float selectDia = SETTINGS_SKETCH.select_dia
						* (1 / getParentSketch().getSketchGlobals().zoom);

				if (selectDia > SETTINGS_SKETCH.select_dia * 1.5f)
					selectDia = SETTINGS_SKETCH.select_dia;

				if (v.distanceTo(new SketchPoint(mouseX, mouseY)) < selectDia)
					this.getSelectedNodes().add(v);
				// System.out.println( this.selectedNodes.size() + "got one");
			} // TODO Auto-generated method stub

		}

		if (getParentSketch().getSketchTools().getCurrentTool() == SketchTools.SELECT_BEZIER_TOOL) {
			this.unselectNodes();
			for (int i = 0; i < this.l.size(); i++) {
				SketchPoint v = this.l.get(i);

				if (v.containsBezier()) {
					if (v.controlPoint1.distanceTo(new SketchPoint(mouseX,
							mouseY)) < SETTINGS_SKETCH.select_dia)
						this.getSelectedNodes().add(v.controlPoint1);

					if (v.controlPoint2.distanceTo(new SketchPoint(mouseX,
							mouseY)) < SETTINGS_SKETCH.select_dia)
						this.getSelectedNodes().add(v.controlPoint2);

				}

				if (v.distanceTo(new SketchPoint(mouseX, mouseY)) < SETTINGS_SKETCH.select_dia) {
					//System.out.println("HERE");

					if (!v.containsBezier()) {
						
						this.addBezier(v, v.copy(),v.copy());

						//choose left or right
						this.getSelectedNodes().add(v.controlPoint1);

					}

				}
				// System.out.println( this.selectedNodes.size() + "got one");
			} // TODO Auto-generated method stub

		}

	}

	public void set(int index, SketchPoint point) {
		l.set(index, point);
	}

	public void setPath(ArrayList<SketchPoint> outline) {
		this.l = outline;

	}

	public SketchPoint setSketchPointpickBuffer(int col,
			SketchPoint selectedVec, SketchShape selectedShape,
			SlicePlane selectedVecPlane, boolean isSelectedVecOnOutline) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setupSpShape(spShape shape) {

		ArrayList<SketchPoint> loop = new ArrayList<SketchPoint>();
		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint v = this.l.get(i);

			SketchPoint point = new SketchPoint(v.x, v.y);

			if (v.controlPoint1 != null) {
				point.controlPoint1 = new Vec2D(v.controlPoint1.x,
						v.controlPoint1.y);
			}

			if (v.controlPoint2 != null) {
				point.controlPoint2 = new Vec2D(v.controlPoint2.x,
						v.controlPoint2.y);
			}

			loop.add(point);

		} // TODO Auto-generated method stub

		shape.addOutline(loop);
		// shape.addBeziers(bezierReturn);
		//shape.scale(SETTINGS_SKETCH.pixels_per_mm);
	}

	public void addCollisionToSpShape(spShape shape) {

		ArrayList<SketchPoint> loop = new ArrayList<SketchPoint>();
		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint v = this.l.get(i);
			SketchPoint point = new SketchPoint(v.x, v.y);

			if (v.controlPoint1 != null) {
				point.controlPoint1 = new Vec2D(v.controlPoint1.x,
						v.controlPoint1.y);
			}

			if (v.controlPoint2 != null) {
				point.controlPoint2 = new Vec2D(v.controlPoint2.x,
						v.controlPoint2.y);
			}

			loop.add(point);

		} // TODO Auto-generated method stub

		shape.addCollisionOutline(loop);

	}

	public SketchPoint setVec2DpickBuffer(int col, SketchPoint selectedVec,
			SketchShape selectedShape, SlicePlane selectedVecPlane,
			boolean isSelectedVecOnOutline) {
		// TODO Auto-generated method stub
		return null;
	}

	public int size() {
		return l.size();
	}

	public void smooth(float scale) {

		if (!WoundClockwise())
			smoothLeft(scale);
		else
			smoothRight(scale);

	}

	public void smoothLeft(float scale) {

		if (size() < 2) {
			return;
		}

		if (!getClosed()) {
			for (int i = 0; i < size(); i++) {
				if (i == 0) // is first
				{
					SketchPoint p1 = get(i);
					SketchPoint p2 = get(i + 1);
					Vec2D tangent = p2.sub(p1);
					Vec2D q1 = p1.add(tangent.scale(scale));
					p1.controlPoint1 = p1;
					p1.controlPoint2 = q1;
				} else if (i == size() - 1) //last
				{
					SketchPoint p0 = get(i - 1);
					SketchPoint p1 = get(i);
					Vec2D tangent = p1.sub(p0);
					Vec2D q0 = p1.sub(tangent.scale(scale));

					p1.controlPoint1 = q0;
					p1.controlPoint2 = p1.copy();
				} else {

					SketchPoint p0, p1, p2;
					p0 = get(i - 1);
					p1 = get(i);
					p2 = get(i + 1);

					Vec2D tangent = (p2.sub(p0)).normalize();
					Vec2D q0 = p1.sub(tangent.scale(scale).scale(
							p1.sub(p0).magnitude()));
					Vec2D q1 = p1.add(tangent.scale(scale).scale(
							p2.sub(p1).magnitude()));
					p1.controlPoint2 = q0;
					p1.controlPoint1 = q1;

				}
			}
		} else {
			for (int i = 0; i < size(); i++) {

				SketchPoint p0, p1, p2;
				if (i > 0)
					p0 = get(i - 1);
				else
					p0 = get(size() - 1);

				p1 = get(i);

				if (i < size() - 1)
					p2 = get(i + 1);
				else
					p2 = get(0);

				Vec2D tangent = (p2.sub(p0)).normalize();
				Vec2D q0 = p1.sub(tangent.scale(scale).scale(
						p1.sub(p0).magnitude()));
				Vec2D q1 = p1.add(tangent.scale(scale).scale(
						p2.sub(p1).magnitude()));
				p1.controlPoint1 = q0;
				p1.controlPoint2 = q1;

			}
		}
	}

	public void smoothRight(float scale) {

		if (size() < 2) {
			return;
		}

		if (!getClosed()) {
			for (int i = size() - 1; i >= 0; i--) {
				if (i == 0) // is first
				{
					SketchPoint p1 = get(i);
					SketchPoint p2 = get(i + 1);
					Vec2D tangent = p2.sub(p1);
					Vec2D q1 = p1.add(tangent.scale(scale));
					p1.controlPoint1 = p1;
					p1.controlPoint2 = q1;
				} else if (i == size() - 1) //last
				{
					SketchPoint p0 = get(i - 1);
					SketchPoint p1 = get(i);
					Vec2D tangent = p1.sub(p0);
					Vec2D q0 = p1.sub(tangent.scale(scale));

					p1.controlPoint1 = q0;
					p1.controlPoint2 = p1.copy();
				} else {

					SketchPoint p0, p1, p2;
					p0 = get(i + 1);
					p1 = get(i);
					p2 = get(i - 1);

					Vec2D tangent = (p2.sub(p0)).normalize();
					Vec2D q0 = p1.sub(tangent.scale(scale).scale(
							p1.sub(p0).magnitude()));
					Vec2D q1 = p1.add(tangent.scale(scale).scale(
							p2.sub(p1).magnitude()));
					p1.controlPoint2 = q0;
					p1.controlPoint1 = q1;

				}
			}
		} else {
			for (int i = size() - 1; i >= 0; i--) {

				SketchPoint p0, p1, p2;
				if (i > 0)
					p0 = get(i - 1);
				else
					p0 = get(size() - 1);

				p1 = get(i);

				if (i < size() - 1)
					p2 = get(i + 1);
				else
					p2 = get(0);

				Vec2D tangent = (p2.sub(p0)).normalize();
				Vec2D q0 = p1.sub(tangent.scale(scale).scale(
						p1.sub(p0).magnitude()));
				Vec2D q1 = p1.add(tangent.scale(scale).scale(
						p2.sub(p1).magnitude()));
				p1.controlPoint1 = q0;
				p1.controlPoint2 = q1;

			}

		}
	}

	public Element toXML() {
		Element element = new Element("SketchPath");

		element.addAttribute(new Attribute("id", String.valueOf(this.getId())));

		if (isConstructionLine())
			element.addAttribute(new Attribute("isConstructionLine", "true"));

		if (this.getClosed())
			element.addAttribute(new Attribute("closed", "true"));
		else
			element.addAttribute(new Attribute("closed", "false"));

		element.addAttribute(new Attribute("isConstructionLine", "true"));

		element.addAttribute(new Attribute("union", String.valueOf(this.union)));

		for (int i = 0; i < this.l.size(); i++) {
			SketchPoint point = this.l.get(i);
			element.appendChild(point.toXML());
		}
		return element;
	}
	
	
	public Element toXML_SVG() {
		
		Element element = new Element("path","http://www.w3.org/2000/svg");
		String svgString = "";
		
		SketchPoint firstPoint = this.l.get(0);
		svgString += "M " + firstPoint.x+ "," + firstPoint.y + " ";
		
		for (int i = 1; i < this.l.size(); i++) {
			SketchPoint point = this.l.get(i);
			SketchPoint prevPoint = this.l.get(i-1);

			if(point.containsBezier() || prevPoint.containsBezier()){
				svgString += "C " + prevPoint.getControlPoint2().x + ","+ prevPoint.getControlPoint2().y +" "+  point.getControlPoint1().x+","+point.getControlPoint1().y+ " " + point.x+ "," + point.y + " ";
			}else{
				svgString += "L " + point.x+ "," + point.y + " ";
			}
			
		}
		
		svgString += " Z";
		
		element.addAttribute(new Attribute("d", svgString));


		return element;
	}
	
	

	public void unselect() {
		this.selected = false;
	}

	public void update() {

	}

	public void woudClockwiseReset() {
		woundClockwiseReset = true;
	}

	public boolean WoundClockwise() {
		if (!woundClockwiseReset)
			return woundClockwise;

		float area = 0;
		int offset = 1;

		if (this.getClosed())
			offset = 0;

		for (int i = 0; i < this.size() - offset; i++) {
			SketchPoint p1 = this.get(i);
			SketchPoint p2;

			if (i == this.size() - 1)
				p2 = this.get(0);
			else
				p2 = this.get(i + 1);

			area += (p1.x * p2.y) - (p2.x * p1.y);
		}

		if (area < 0)
			woundClockwise = true;
		else
			woundClockwise = false;

		woundClockwiseReset = false;

		return woundClockwise;

	}

	public void reverseWinding() {
		Collections.reverse(this.l);

	}

	public void renderFlat(PGraphics g) {

		g.beginShape();

		int loop = 0;
		if (this.getClosed())
			loop = 1;

		for (int i = 1; i < this.l.size() + loop; i++) {
			SketchPoint curVec = null;
			SketchPoint preVec = null;

			// --- last or first point ---
			if (i == 1) {
				curVec = this.l.get(0);
				g.vertex(curVec.x, curVec.y);
			}

			if (i >= 1)
				preVec = this.l.get(i - 1);

			if (i == this.l.size()) {
				curVec = this.l.get(0);
				preVec = this.l.get(this.l.size() - 1);
			} else {
				curVec = this.l.get(i);
			}

			if (curVec.containsBezier() || preVec != null
					&& preVec.containsBezier()) {

				Vec2D c1 = (SketchPoint) preVec;
				Vec2D c2 = (SketchPoint) curVec;

				if (c1 == null)
					c1 = new SketchPoint(0, 0);

				if (preVec != null && preVec.containsBezier()) {

					c1 = preVec.getControlPoint2();
				}

				if (curVec.containsBezier()) {
					c2 = curVec.getControlPoint1();
				}

				if (c1 != null && c2 != null && curVec != null) {
					//g.vertex(curVec.x, curVec.y);
					g.bezierVertex(c1.x, c1.y, c2.x, c2.y, curVec.x, curVec.y);

				}

			} else {

				g.vertex(curVec.x, curVec.y);

			}

		}
		if (this.getClosed())
			g.endShape(PConstants.CLOSE);
		else
			g.endShape(PConstants.OPEN);

	}

	void removeOverlapping(float tollerance) {


		List<SketchPoint> newPoints = new ArrayList();
		SketchPoint lastAddedPoint = null;

		
		
		//go though all point and add or adjust for beziers apart from last 
		for (int i = 0; i < this.l.size()-1 ; i++) {
			SketchPoint curVec = this.l.get(i);

			if (lastAddedPoint == null
					|| lastAddedPoint.distanceTo(curVec) > tollerance
					 ) {
				newPoints.add(curVec);
				lastAddedPoint = curVec;
			}else{
				
				if(lastAddedPoint.distanceTo(curVec) < tollerance && curVec.containsBezier()){
					if(lastAddedPoint.containsBezier())
						lastAddedPoint.controlPoint2 = curVec.controlPoint2;
					else{
						lastAddedPoint.controlPoint1 = curVec.controlPoint1;
						lastAddedPoint.controlPoint2 = curVec.controlPoint2;
					}

				}
				
			}

		}
		
		if(this.l.size() > 2){
			SketchPoint firstVec = this.l.get(0);
			SketchPoint lastVec = this.l.get(this.l.size()-1);
			
			if(firstVec.distanceTo(lastVec) < tollerance  && firstVec.containsBezier() && lastVec.containsBezier())
				firstVec.controlPoint1 = lastVec.controlPoint1;
			
if(firstVec.distanceTo(lastVec) > tollerance)
	newPoints.add(lastVec);


			
		}
		
		this.l = newPoints;
	

	/*
		
		if(this.l.size() > 1){
		for (int i = 1; i < this.l.size() ; i++) {
			SketchPoint prevVec = this.l.get(i-1);

			SketchPoint curVec = this.l.get(i);

			String bez = "";
			
			if(curVec.containsBezier())
				bez = " BEZIER";
			
			LOGGER.info("DIST " + prevVec.distanceTo(curVec) + bez);

		}
		
		
		
		String bez = "";
		
		if(this.l.get(0).containsBezier())
			bez = " BEZIER";
		
		LOGGER.info("DIST " + this.l.get(0).distanceTo(this.l.get(this.l.size()-1)) + bez);
		LOGGER.info("");
		}
		
		
		*/
	}

	void flattenCurves(float flatness) {

		List<SketchPoint> newPoints = new ArrayList();

		int loop = 1;
		if (this.getClosed())
			loop = 0;

		for (int i = 0; i < this.l.size() - loop; i++) {
			SketchPoint curVec = this.l.get(i);
			SketchPoint nextVec = null;
			if (i < this.l.size() - 1)
				nextVec = this.l.get(i + 1);
			else
				nextVec = this.l.get(0);

			newPoints.add(curVec);

			if (curVec.containsBezier() || nextVec.containsBezier()) {
				Vec2D bez1 = curVec;
				Vec2D bez2 = nextVec;

				if (curVec.containsBezier()) {
					bez1 = curVec.getControlPoint2();
				}

				if (nextVec.containsBezier()) {
					bez2 = nextVec.getControlPoint1();
				}

				for (float t = 0; t <= 1; t += flatness) {
					float x = functions.bezierPoint(curVec.x, bez1.x, bez2.x,
							nextVec.x, t);
					float y = functions.bezierPoint(curVec.y, bez1.y, bez2.y,
							nextVec.y, t);

					newPoints.add(new SketchPoint(x, y));
				}
			}

		}

		this.removeBeziers();
		this.l = newPoints;

	}

	public void offsetPath(float offset) {
		this.setClosed(true);
		this.flattenCurves(0.2f);
		this.removeOverlapping(0.1f);
		
		//this.add((SketchPoint)this.get(0).clone());
		SketchSpline spline = new SketchSpline(this.getParentSketch());
		spline.setCentrePath(this);
		spline.getCentrePath().setClosed(true);
		spline.joinType = SketchSpline.JOIN_BEVEL;
		spline.capType = SketchSpline.CAP_SQUARE;
		spline.setOffsetSize(offset);
		//spline.getParentSketch().getSketchGlobals().BEZIER_DETAIL_OFFSET = 0.01f;
		spline.offset();
		SketchPath sLeft = new SketchPath(this.getParentSketch(),
				spline.outineLeft);
		SketchPath sRight = new SketchPath(this.getParentSketch(),spline.outineRight);
		this.l = sLeft.l;
		
		this.removeBeziers();
		this.removeOverlapping(0.2f);

	}

	public void simplifyDouglasPeucker(float epsilon) {
		this.l = this.simplifyDouglasPeucker(
				functions.getRange(this.l, 0, this.l.size() - 1), epsilon);
	}

	public List<SketchPoint> simplifyDouglasPeucker(List<SketchPoint> points,
			float epsilon) {
		if (points.size() <= 1)
			return new ArrayList();

		//function DouglasPeucker(PointList[], epsilon)
		//Find the point with the maximum distance
		double dmax = 0;
		int index = 0;

		for (int i = 1; i < points.size() - 2; i++) {

			double d = PerpendicularDistance(points.get(0).copy(),
					points.get(points.size() - 1).copy(), points.get(i).copy());

			if (d > dmax) {
				index = i;
				dmax = d;
			}
		}
		ArrayList<SketchPoint> resultList = new ArrayList();
		//If max distance is greater than epsilon, recursively simplify
		if (dmax >= epsilon) {
			//Recursive call

			List<SketchPoint> recResults1 = simplifyDouglasPeucker(
					functions.getRange(points, 0, index), epsilon);
			List<SketchPoint> recResults2 = simplifyDouglasPeucker(
					functions.getRange(points, index, points.size() - 1),
					epsilon);

			resultList.addAll(functions.getRange(recResults1, 0,
					recResults1.size() - 2));
			resultList.addAll(recResults2);

			// Build the result list
			//  ResultList[] = {recResults1[1...end-1] recResults2[1...end]}
		} else {
			resultList.add((SketchPoint) points.get(0).clone());
			resultList.add((SketchPoint) points.get(points.size() - 1).clone());
		}

		//Return the result
		return resultList;

	}

	public static double PerpendicularDistance(Vec2D Point1, Vec2D Point2,
			Vec2D Point) {

		//LOGGER.info("PerpendicularDistance");
		//Area = |(1/2)(x1y2 + x2y3 + x3y1 - x2y1 - x3y2 - x1y3)|   *Area of triangle
		//Base = v((x1-x2)²+(x1-x2)²)                               *Base of Triangle*
		//Area = .5*Base*H                                          *Solve for height
		//Height = Area/.5/Base

		double area = Math
				.abs(.5 * (Point1.x * Point2.y + Point2.x * Point.y + Point.x
						* Point1.y - Point2.x * Point1.y - Point.x * Point2.y - Point1.x
						* Point.y));
		double bottom = Math.sqrt(Math.pow(Point1.x - Point2.x, 2)
				+ Math.pow(Point1.y - Point2.y, 2));
		double height = area / bottom * 2;

		return height;

		//Another option
		//Double A = Point.X - Point1.X;
		//Double B = Point.Y - Point1.Y;
		//Double C = Point2.X - Point1.X;
		//Double D = Point2.Y - Point1.Y;

		//Double dot = A * C + B * D;
		//Double len_sq = C * C + D * D;
		//Double param = dot / len_sq;

		//Double xx, yy;

		//if (param < 0)
		//{
		//    xx = Point1.X;
		//    yy = Point1.Y;
		//}
		//else if (param > 1)
		//{
		//    xx = Point2.X;
		//    yy = Point2.Y;
		//}
		//else
		//{
		//    xx = Point1.X + param * C;
		//    yy = Point1.Y + param * D;
		//}

		//Double d = DistanceBetweenOn2DPlane(Point, new Point(xx, yy));
	}
@Override
public 
void setClosed(boolean _closed){
	super.setClosed(_closed);
	
	
	//if closed always make sure we don't start and end on the same point
	if(_closed){
		
		if(this.l.size() > 2){
			SketchPoint firstVec = this.l.get(0);
			SketchPoint lastVec = this.l.get(this.l.size()-1);
			if(firstVec.distanceTo(lastVec) < PApplet.EPSILON){
			if(lastVec.containsBezier())
				firstVec.controlPoint1 = lastVec.controlPoint1;
			
			//remove the last element
			this.l.remove(this.l.size()-1);
			}
		}
	}
}
}
