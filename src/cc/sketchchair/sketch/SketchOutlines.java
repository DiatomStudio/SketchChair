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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cc.sketchchair.core.LOGGER;

import com.bulletphysics.linearmath.Transform;
import ShapePacking.spShape;
import processing.core.PGraphics;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

/**
 * Container class for SketchOutlines.
 * @author Greg
 *
 */
//#ENDIF JAVA

public class SketchOutlines {
	List<SketchOutline> l = new ArrayList<SketchOutline>();
	public SketchShapes parentSketchShapes = null;

	public SketchOutlines(SketchShapes sketchShapes) {
		this.parentSketchShapes = sketchShapes;

	}

	public void add(SketchOutline sktOutline) {
		this.getList().add(sktOutline);
	}

	public void clear() {
		this.l = new ArrayList<SketchOutline>();
		this.getList().clear();
	}

	public void flipHorizontal(toxi.geom.Vec3D centre) {
		for (int i = 0; i < this.getList().size(); i++) {
			SketchOutline sktOutline = this.getList().get(i);
			sktOutline.flipHorizontal(centre);
		}
	}

	public float getArea() {
		SketchOutline outerOutline = this.getOutterOutline();

		if (outerOutline == null)
			return 0;

		float totalArea = outerOutline.getArea();

		for (int i = 0; i < this.getList().size(); i++) {
			SketchOutline sktOutline = this.getList().get(i);

			//	System.out.println(sktOutline.getArea());

			if (sktOutline != outerOutline)
				totalArea -= sktOutline.getArea();
		}

		return totalArea;
	}

	public Vec2D getCentreOfMass() {

		if (getOutterOutline() != null)
			return getOutterOutline().getCentreOfMass();
		else
			return null;
	}

	public float getHeight() {
		SketchOutline outline = getOutterOutline();
		if (outline != null)
			return outline.getHeight();
		else
			return 0;
	}

	/**
	* @return the l
	*/
	public List<SketchOutline> getList() {
		return l;
	}

	public float getMaxX() {
		if (getOutterOutline() != null)
			return getOutterOutline().getMaxX();
		else
			return -1;
	}

	public float getMaxXWorldSpace(Transform currentWorldTransform) {
		if (getOutterOutline() != null)
			return getOutterOutline().getMaxXWorldSpace(currentWorldTransform);
		else
			return -1;
	}

	public float getMaxY() {
		float maxY = -1;

		SketchOutline outline = getOutterOutline();
		if (outline != null)
			maxY = outline.getMaxY();

		return maxY;

	}

	public float getMaxYWorldSpace(Transform currentWorldTransform) {
		if (getOutterOutline() != null)
			return getOutterOutline().getMaxYWorldSpace(currentWorldTransform);
		else
			return -1;
	}

	public float getMinX() {
		if (getOutterOutline() != null)
			return getOutterOutline().getMinX();
		else
			return -1;
	}

	public float getMinXWorldSpace(Transform currentWorldTransform) {
		if (getOutterOutline() != null)
			return getOutterOutline().getMinXWorldSpace(currentWorldTransform);
		else
			return -1;
	}

	public float getMinY() {
		if (getOutterOutline() != null)
			return getOutterOutline().getMinY();
		else
			return -1;
	}

	public float getMinYWorldSpace(Transform currentWorldTransform) {
		if (getOutterOutline() != null)
			return getOutterOutline().getMinYWorldSpace(currentWorldTransform);
		else
			return -1;
	}

	public SketchOutline getOutterOutline() {

		if (this.getList().size() > 0) {
			SketchOutline foundOutline = this.getList().get(
					this.getList().size() - 1);
			for (int i = 0; i < this.getList().size(); i++) {
				SketchOutline tempOutline = this.getList().get(i);
				if (tempOutline != null && foundOutline != null
						&& tempOutline.getArea() > foundOutline.getArea())
					foundOutline = tempOutline;
			}

			return foundOutline;

		} else
			return null;
	}

	public spShape getspShape() {
		spShape shape = new spShape();
		for (int i = 0; i < this.getList().size(); i++) {
			SketchOutline sktOutline = this.getList().get(i);
			sktOutline.setupSpShape(shape);
		}

		return shape;

	}
	
	public spShape addCollisionToSpShape(spShape shape) {
		for (int i = 0; i < this.getList().size(); i++) {
			SketchOutline sktOutline = this.getList().get(i);
			sktOutline.addCollisionToSpShape(shape);
		}

		return shape;

	}

	public void optimize() {
		for (int i = 0; i < this.getList().size(); i++) {
			SketchOutline sktOutline = this.getList().get(i);
			sktOutline.optimize();
		}

	}

	public void render(PGraphics g) {

		g.pushMatrix();
		for (int i = 0; i < this.getList().size(); i++) {
			SketchOutline sktOutline = this.getList().get(i);
			sktOutline.render(g);
		}
		g.popMatrix();

	}

	public void renderSilhouette(PGraphics g) {
		if(this.getOutterOutline() == null)
			return;
		
		g.fill(0,0,0);
		
		this.getOutterOutline().getPath().setClosed(true);
		this.getOutterOutline().getPath().renderFlat(g);
		/*
		if(this.getOutterOutline() == null)
			return;
		
		g.fill(0,0,0);
		g.stroke(0,0,0);
		//g.noFill();
		if(!this.getOutterOutline().getPath().WoundClockwise())
			this.getOutterOutline().getPath().reverseWinding();
		
		this.getOutterOutline().renderFlat(g);
		
		
		//g.fill(255,0,0);
		g.breakShape();
		for (int i = 0; i < this.getList().size(); i++) {
			SketchOutline sktOutline = this.getList().get(i);
			
			sktOutline.getPath().setClosed(true);
			
			if(!sktOutline.equals(this.getOutterOutline())){
				
				if(sktOutline.getPath().WoundClockwise())
					sktOutline.getPath().reverseWinding();
				
			sktOutline.renderFlat(g);
			}
		}
		
		//LOGGER.info("hi");
		//this.render(g);
		 * */
		 
	}

	public void scale(float scale, toxi.geom.Vec3D centre) {
		for (int i = 0; i < this.getList().size(); i++) {
			SketchOutline sktOutline = this.getList().get(i);
			sktOutline.scale(scale, centre);
		}
	}


	public void setList(List<SketchOutline> l) {
		this.l = l;
	}

}
