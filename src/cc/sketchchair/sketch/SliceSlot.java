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

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.SETTINGS;
import cc.sketchchair.functions.functions;
import cc.sketchchair.geometry.SlicePlane;

import processing.core.PGraphics;
import toxi.geom.Plane;
import toxi.geom.Vec2D;

/**
 * SketchSlot represents the slot created between two overlapping SketchPlanes in order to join them. 
 * Parameters can be used to change the tolerance of the joint. 
 * @author gregsaul
 *
 */
//#ENDIF JAVA

public class SliceSlot {
	public static final int SLOT = 1;
	public static final int FINGER = 2;
	public static final int SLOTS_AND_FINGER = 3;

	private Vec2D pos;
	Vec2D dir;
	float slotLen;
	float width_mm;
	float width_screen;

	private Vec2D linkedVec1;
	private Vec2D linkedVec2;
	private float t;
	private SketchSpline onSpline;
	public Plane constrainPlane = null;
	public boolean destroy = false;
	public SlicePlane slice = null;
	private float fingerLen;
	private int numSlots = 1;
	private boolean startOnSlot = true;
	public int type = SLOT;
	private boolean onEdge;
	private Vec2D dirToEdge;
	public float fingerTollerance = 0.0f;
	public boolean makesEdge = false;

	public SliceSlot(SlicePlane slice, Vec2D pos, Vec2D dir, float len,
			float width) {
		this.setPos(pos);
		this.slice = slice;
		this.dir = dir;
		this.slotLen = len;
		this.width_mm = width;
	}

	public SliceSlot(SlicePlane slice, Vec2D pos, Vec2D dir, float slotLen,
		float fingerLen, float width, int numSlots, boolean startOnSlot,int type) {
		
		this.setPos(pos);
		this.slice = slice;
		this.dir = dir;
		this.slotLen = slotLen;
		this.fingerLen = fingerLen;
		this.numSlots = numSlots;
		this.width_mm = width;
		this.startOnSlot = startOnSlot;
		this.type = type;
		
	}

	public SliceSlot(SlicePlane slice, Vec2D pos, Vec2D dir, float t,
			SketchSpline onSpline, float slotLen, float fingerLen, float width,
			int numSlots, boolean startOnSlot, int type) {
		
		this.setPos(pos);
		this.slice = slice;
		this.dir = dir;
		this.slotLen = slotLen;
		this.fingerLen = fingerLen;

		this.width_mm = width;
		// this.linkedVec1 = linkedVec1;
		// this.linkedVec2 = linkedVec2;
		this.t = t;
		this.onSpline = onSpline;
		this.startOnSlot = startOnSlot;
		this.numSlots = numSlots;
		this.type = type;
		
	}

	public void buildSketchOutlines(SketchOutlines sketchOutlines,
			Sketch parentSketch) {

		float slotOffset = 0;
		for (int i = 1; i < numSlots + 1; i++) {
			if (i % 2 == 0) {
				if (!this.startOnSlot) {
					SketchOutline sltOutline = new SketchOutline(parentSketch);
					sltOutline.getPath().setPath(
							getOutline(slotOffset, this.slotLen));
					sketchOutlines.add(sltOutline);
					slotOffset += this.slotLen;
				} else {
					slotOffset += this.fingerLen;
				}
			} else {
				if (!this.startOnSlot) {
					slotOffset += this.fingerLen;
				} else {
					SketchOutline sltOutline = new SketchOutline(parentSketch);
					sltOutline.getPath().setPath(
							getOutline(slotOffset, this.slotLen));
					sketchOutlines.add(sltOutline);
					slotOffset += this.slotLen;
				}
			}

		}

	}

	public void checkForCollision(SliceSlot otherSlot) {

		float tempWidthThis = slice.thickness / SETTINGS.scale;

		Vec2D corner1This = this.getPos().add(this.dir.getRotated(0).scale(
				tempWidthThis / 2));
		Vec2D corner2This = this.getPos().add(this.dir.getRotated(0).scale(
				-tempWidthThis / 2));
		Vec2D corner3This = this.getPos().add(
				this.dir.getRotated((float) Math.PI / 2).scale(this.slotLen))
				.add(dir.getRotated(0).scale(-tempWidthThis / 2));
		Vec2D corner4This = this.getPos().add(
				this.dir.getRotated((float) Math.PI / 2).scale(this.slotLen))
				.add(dir.getRotated(0).scale(tempWidthThis / 2));

		float tempWidthThat = otherSlot.slice.thickness / SETTINGS.scale;

		Vec2D corner1That = otherSlot.getPos().add(otherSlot.dir.getRotated(0)
				.scale(tempWidthThis / 2));
		Vec2D corner2That = otherSlot.getPos().add(otherSlot.dir.getRotated(0)
				.scale(-tempWidthThis / 2));

		//not very scientific add 10 units to the end of each path to make sure it breaks though the edge
		Vec2D corner3That = otherSlot.getPos().add(
				otherSlot.dir.getRotated((float) Math.PI / 2).scale(
						otherSlot.slotLen + 10)).add(
				dir.getRotated(0).scale(-tempWidthThat / 2));
		Vec2D corner4That = otherSlot.getPos().add(
				otherSlot.dir.getRotated((float) Math.PI / 2).scale(
						otherSlot.slotLen + 10)).add(
				dir.getRotated(0).scale(tempWidthThat / 2));

		if (functions
				.intersect(corner1This, corner2This, corner3This, corner4This,
						corner1That, corner2That, corner3That, corner4That) == functions.DO_INTERSECT) {

			if (this.slice.tiedToLeg) {
				if (otherSlot.slice != null) {
					otherSlot.destroy();
					otherSlot.slice.destroy();
				}

			} else if (otherSlot.slice.tiedToLeg) {
				if (this.slice != null) {
					this.slice.destroy();
					this.destroy();
				}
			} else {
				if (this.slice != null) {
					this.slice.destroy();
					this.destroy();
				}
			}

		}

	}

	public SliceSlot clone() {
		SliceSlot newSlot = new SliceSlot(this.slice, this.getPos(), this.dir,
				this.t, this.onSpline, this.slotLen, this.fingerLen,
				this.width_mm, this.numSlots, this.startOnSlot, this.type);
		return newSlot;

	}

	void destroy() {
		this.destroy = true;
	}
	public ArrayList<SketchPoint> getOutline(float offset, float sLen) {
		return this.getOutline(offset, sLen,0); 
	}
	public ArrayList<SketchPoint> getOutline(float offset, float sLen,float extendLen) {
		ArrayList<SketchPoint> vec2Ds = new ArrayList<SketchPoint>();

		if(sLen > 0)
			extendLen = -extendLen;
		
		float tempWidth = slice.thickness / SETTINGS.scale;
		float tempLength = sLen + (fingerTollerance * 2)-(extendLen*2);

		Vec2D corner1 = getPos().add(
				dir.getRotated((float) Math.PI / 2).scale(offset+extendLen)).add(
				dir.getRotated(0).normalize().scale(tempWidth / 2));
		Vec2D corner2 = getPos().add(
				dir.getRotated((float) Math.PI / 2).scale(offset+extendLen)).add(
				dir.getRotated(0).normalize().scale(-tempWidth / 2));
		Vec2D corner3 = getPos().add(
				dir.getRotated((float) Math.PI / 2).scale(offset + tempLength+extendLen))
				.add(dir.getRotated(0).scale(-tempWidth / 2));
		Vec2D corner4 = getPos().add(
				dir.getRotated((float) Math.PI / 2).scale(offset + tempLength+extendLen))
				.add(dir.getRotated(0).scale(tempWidth / 2));

		vec2Ds.add(new SketchPoint(corner1));
		vec2Ds.add(new SketchPoint(corner2));
		vec2Ds.add(new SketchPoint(corner3));
		vec2Ds.add(new SketchPoint(corner4));

		return vec2Ds;

	}

	void getOutlineGeneralpath(float offset, Area outlineArea, float sLen) {
		//#IF JAVA
		GeneralPath gPath = new GeneralPath();
		float tempWidth = slice.thickness / SETTINGS.scale;
		float tempLength = sLen + (fingerTollerance * 2) + GLOBAL.SketchGlobals.extendSlots;
		/*
		 
		   s1
		 _______
		|		|
	s4	|		| s2
		|_______|
		start point
			s3
			
				start point
	_________|______________
			| |
			| | --> slot
			| |
			|_|
					
					
		
		*/

		Vec2D corner1, corner2, corner3, corner4;
		
		float s1 = 0, s2 = 0, s3 = 0, s4 = 0;
		if (this.onEdge) {
			float extendLen = GLOBAL.SketchGlobals.slotPierceLen;
			if (dirToEdge.equalsWithTolerance(new Vec2D(0, 1), 1f))
				s3 = +extendLen;

			if (dirToEdge.equalsWithTolerance(new Vec2D(1, 0), 1f))
				s2 = extendLen;

			if (dirToEdge.equalsWithTolerance(new Vec2D(0, -1), 1f))
				s3 = -extendLen;

			if (dirToEdge.equalsWithTolerance(new Vec2D(-1, 0), 1f))
				s4 = extendLen;
		}
		//tempWidth = tempWidth * (.1f / SETTINGS.scale  );
		corner1 = getPos().add(
				dir.getRotated((float) Math.PI / 2).scale(offset + s3)).add(
				dir.getRotated(0).normalize().scale((tempWidth / 2) + s4));
		corner2 = getPos().add(
				dir.getRotated((float) Math.PI / 2).scale(offset + s3)).add(
				dir.getRotated(0).normalize().scale((-tempWidth / 2) - s2));
		corner3 = getPos().add(
				dir.getRotated((float) Math.PI / 2).scale(
						(offset + tempLength) - s1)).add(
				dir.getRotated(0).normalize().scale((-tempWidth / 2) - s2));
		corner4 = getPos().add(
				dir.getRotated((float) Math.PI / 2).scale(
						(offset + tempLength) - s1)).add(
				dir.getRotated(0).normalize().scale((tempWidth / 2) + s4));

		gPath.moveTo(corner1.x, corner1.y);
		gPath.moveTo(corner2.x, corner2.y);
		gPath.lineTo(corner3.x, corner3.y);
		gPath.lineTo(corner4.x, corner4.y);
		gPath.lineTo(corner1.x, corner1.y);
		outlineArea.add(new Area(gPath));

		if (GLOBAL.shapePack.add_guide_divets) {
			float r = GLOBAL.shapePack.inner_corner_radius;

			Vec2D radCentre1 = getPos().add(
					dir.getRotated((float) Math.PI / 2).scale(-(r / 2))).add(
					dir.getRotated(0).normalize().scale(-tempWidth / 2));
			Vec2D radCentre2 = getPos().add(
					dir.getRotated((float) Math.PI / 2).scale(-(r / 2))).add(
					dir.getRotated(0).normalize().scale(tempWidth / 2));

			Ellipse2D.Float ellipse = new Ellipse2D.Float(radCentre1.x
					- (r / 2), radCentre1.y - (r / 2), r, r);
			outlineArea.add(new Area(ellipse));

			ellipse = new Ellipse2D.Float(radCentre2.x - (r / 2), radCentre2.y
					- (r / 2), r, r);
			outlineArea.add(new Area(ellipse));

		}
		
		
		if (GLOBAL.shapePack.addDogbones) {
			float r = GLOBAL.shapePack.inner_corner_radius;
			
//LOGGER.info(dirToEdge.toString());
			

			int sign = -1;
			
				
			Vec2D radCentre1 = getPos().add(
					dir.getRotated((float) Math.PI / 2).scale(
							(offset + tempLength+(r / 2)) - s1)).add(
									dir.getRotated(0).normalize().scale((-(tempWidth+(r / 2)) / 2) - s2));

			Vec2D radCentre2 = getPos().add(
					dir.getRotated((float) Math.PI / 2).scale(
							(offset + tempLength+(r / 2)) - s1)).add(
									dir.getRotated(0).normalize().scale((+(tempWidth+(r / 2)) / 2) - s2));

			Ellipse2D.Float ellipse = new Ellipse2D.Float(radCentre1.x
					- (r / 2), radCentre1.y + ((r / 2)*sign), r, r);
			outlineArea.add(new Area(ellipse));

			ellipse = new Ellipse2D.Float(radCentre2.x - (r / 2), radCentre2.y
					+ ((r / 2)*sign), r, r);
			outlineArea.add(new Area(ellipse));

		}
		
		//#ENDIF JAVA
	}

	public Area getOutlineGeneralPath() {
		Area outlineArea = new Area();

		float slotOffset = 0;
		for (int i = 1; i < numSlots + 1; i++) {
			
			if (i % 2 == 0) {
				if (!this.startOnSlot) {
					getOutlineGeneralpath(slotOffset, outlineArea, slotLen);
					slotOffset += this.slotLen;
				} else {
					slotOffset += this.fingerLen;
				}
			} else {
				if (!this.startOnSlot) {
					slotOffset += this.fingerLen;
				} else {
					getOutlineGeneralpath(slotOffset, outlineArea, slotLen);
					slotOffset += this.slotLen;
				}
			}

		}

		return outlineArea;

	}

	public Area getOutlineGeneralPathFingers() {
		Area outlineArea = new Area();

		float slotOffset = 0;
		for (int i = 1; i < numSlots + 1; i++) {
			if (i % 2 == 0) {
				if (this.startOnSlot) {
					getOutlineGeneralpath(slotOffset, outlineArea, slotLen);
					slotOffset += this.slotLen;
				} else {
					slotOffset += this.fingerLen;
				}
			} else {
				if (this.startOnSlot) {
					slotOffset += this.fingerLen;
				} else {
					getOutlineGeneralpath(slotOffset, outlineArea, slotLen);
					slotOffset += this.slotLen;
				}
			}

		}

		return outlineArea;
	}
	
	
	public void renderEdge(PGraphics g) {
		
		//if we're not making a edge
		//if(!makesEdge)
		//	return;
		g.pushMatrix();
		g.translate(this.getPos().x, this.getPos().y, 0.0f);
		g.rotate(-(functions.angleOf(this.dir)));
		float tempThickness = slice.thickness / SETTINGS.scale;
		g.line(-(tempThickness / 2), 0,-(tempThickness / 2),this.slotLen*2);
		g.line((tempThickness / 2), 0,(tempThickness / 2),this.slotLen*2);
		g.line(-(tempThickness / 2), 0,-(tempThickness / 2),0);
		g.line(-(tempThickness / 2), this.slotLen*2,-(tempThickness / 2),this.slotLen*2);
		g.popMatrix();

	}
	
	

	void render(PGraphics g) {
		//#IF JAVA
		if(SETTINGS.DEBUG){
			g.stroke(255,255,0);
			SketchPath path = new SketchPath(this.slice.getSketch(),this.getOutline(0, this.slotLen,GLOBAL.SketchGlobals.slotPierceLen));
			path.build();
			//path.setClosed(true);
			path.render(g);

			return;
		}
		//#ENDIF JAVA

		g.pushMatrix();
		g.translate(this.getPos().x, this.getPos().y, 1f);
		g.rotate(-(functions.angleOf(this.dir)));
		// g.translate(this.width/2,0);
		float tempThickness = slice.thickness / SETTINGS.scale;
		// g.ellipse(0,0,tempThickness,tempThickness);
		for (int i = 1; i < numSlots + 1; i++) {
			if (i % 2 == 0) {
				if (!this.startOnSlot) {

					if (SETTINGS.DEBUG) {
						g.ellipse(0, 0, 10, 10);
						if (this.onEdge) {
							g.line(0, this.slotLen / 2,
									(this.dirToEdge.y * 10), (this.slotLen / 2)
											+ (this.dirToEdge.x * 10));
						}

					}

					
					g.pushMatrix();
					g.translate(0, 0,(tempThickness/2));
					g.rect(-(tempThickness / 2), 0, (tempThickness),
							this.slotLen);
					g.popMatrix();
					
					g.pushMatrix();
					g.translate(0, 0,-(tempThickness/2));
					g.rect(-(tempThickness / 2), 0, (tempThickness),
							this.slotLen);
					g.popMatrix();
					
					
					
					g.translate(0, this.slotLen);
				} else {
					g.translate(0, this.fingerLen);
				}
			} else {
				if (!this.startOnSlot) {
					g.translate(0, this.fingerLen);
				} else {

					if (SETTINGS.DEBUG) {
						g.ellipse(0, 0, 10, 10);
						if (this.onEdge) {
							g.line(0, this.slotLen / 2,
									(this.dirToEdge.y * 10), (this.slotLen / 2)
											+ (this.dirToEdge.x * 10));
						}
					}
					g.pushMatrix();
					g.translate(0, 0,(tempThickness/2));
					g.rect(-(tempThickness / 2), 0, (tempThickness),
							this.slotLen);
					g.popMatrix();
					
					g.pushMatrix();
					g.translate(0, 0,-(tempThickness/2));
					g.rect(-(tempThickness / 2), 0, (tempThickness),
							this.slotLen);
					g.popMatrix();
					g.translate(0, this.slotLen);
				}

			}
		}
		g.popMatrix();

	}

	public void setFingerTollerance(float t) {
		fingerTollerance = t;
	}

	public void setOnEdge(Vec2D dirToEdge) {
		this.onEdge = true;
		this.dirToEdge = dirToEdge;

	}

	public void swap() {
		getPos().addSelf(dir.getRotated((float) Math.PI / 2).scale((this.slotLen)));
		dir.rotate((float) Math.PI);
	}

	void update() {
		this.setPos(this.onSpline.getPos(this.t));
		this.dir = this.onSpline.getPerpendicular(this.t);

		if (this.constrainPlane != null) {
			this.constrainPlane.x = this.getPos().x;
			this.constrainPlane.y = this.getPos().y;

			this.constrainPlane.normal.x = this.dir.x;
			this.constrainPlane.normal.y = this.dir.y;
		}
	}

	/**
	 * @return the pos
	 */
	public Vec2D getPos() {
		return pos;
	}

	/**
	 * @param pos the pos to set
	 */
	public void setPos(Vec2D pos) {
		this.pos = pos;
	}

	public void removeNonPiercing(Sketch outlineSketch) {
		//#IF JAVA
		SketchPath path = new SketchPath(this.slice.getSketch(),this.getOutline(0, this.slotLen,GLOBAL.SketchGlobals.slotPierceLen));
		path.build();
		//#ENDIF JAVA
		
		boolean intersectionFound = false;
		for(int i = 0; i < outlineSketch.getSketchShapes().sketchOutlines.l.size();i++){
			SketchOutline outline = outlineSketch.getSketchShapes().sketchOutlines.l.get(i);
			if(outline.getPath().intersects(path))
				intersectionFound = true;
		}
		
		if(!intersectionFound && !this.slice.tiedToLeg){
			if (this.slice != null) {
				this.slice.destroy();
				this.destroy();
			}
		}
		
		
	}
	
	
	

	public void removeTrappedSlots(Sketch outlineSketch) {
		//#IF JAVA
		SketchPath path = new SketchPath(this.slice.getSketch(),this.getOutline(0, -this.slotLen,GLOBAL.SketchGlobals.slotPierceLen));
		path.setClosed(true);
		path.build();
		//#ENDIF JAVA
		
		boolean intersectionFound = false;
		for(int i = 0; i < outlineSketch.getSketchShapes().sketchOutlines.l.size();i++){
			SketchOutline outline = outlineSketch.getSketchShapes().sketchOutlines.l.get(i);
			if(outline.getPath().intersectsCount(path) > 2 && !this.slice.tiedToLeg){
				this.slice.destroy();
				this.destroy();	
				}
		
		
	}}



}
