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
import javax.vecmath.Vector3f;

import cc.sketchchair.core.LOGGER;

import com.bulletphysics.linearmath.Transform;
import ShapePacking.spShape;
import processing.core.PApplet;
import processing.core.PGraphics;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

/**
 * Represents the generated outline of SketchShapes, this is used as the cutting outline and is passed to the shape packing package.
 * @author gregsaul
 *
 */
//#ENDIF JAVA

public class SketchOutline {
	//List<SketchPoint> l = new ArrayList<SketchPoint>();

	private SketchPath path = new SketchPath(getParentSketch());
	public Sketch parentSketch = null;

	public SketchOutline(Sketch sketch) {
		setParentSketch(sketch);
		this.getPath().parentSketch = this.parentSketch;
		getPath().editable = false;
		getPath().setClosed(true);
	}

	public void add(SketchPoint point) {
		getPath().add(point);
		// this.l.add(vec2d);
	}

	public ArrayList cloneArray() {
		return getPath().cloneArray();
	}

	public void flipHorizontal(toxi.geom.Vec3D centre) {
		this.getPath().flipHorizontal(centre);

	}

	public float getArea() {

		return this.getWidth() * this.getHeight();

	}

	public Vec2D getCentreOfMass() {

		long x = 0;
		long y = 0;
		for (int i = 0; i < this.getPath().size(); i++) {
			SketchPoint v = (SketchPoint) this.getPath().get(i);

			x += v.x;
			y += v.y;
		}
		if (this.getPath().size() > 0)
			return new Vec2D(x / this.getPath().size(), y
					/ this.getPath().size());
		else
			return null;
	}

	float getHeight() {
		float minY = -1;
		float maxY = -1;

		for (int i = 0; i < this.getPath().size(); i++) {
			SketchPoint v = (SketchPoint) this.getPath().get(i);
			if (minY == -1 || v.y < minY)
				minY = v.y;

			if (maxY == -1 || v.y > maxY)
				maxY = v.y;
		}

		return maxY - minY;
	}

	float getMaxX() {
		float maxX = -1;

		for (int i = 0; i < this.getPath().size(); i++) {
			SketchPoint v = (SketchPoint) this.getPath().get(i);

			if (maxX == -1 || v.x > maxX)
				maxX = v.x;
		}

		return maxX;
	}

	public float getMaxXWorldSpace(Transform currentWorldTransform) {
		float maxX = -1;

		for (int i = 0; i < this.getPath().size(); i++) {
			SketchPoint v = (SketchPoint) this.getPath().get(i);

			Vector3f vecWorld = new Vector3f(
					v.x
							/ getParentSketch().getSketchGlobals().physicsEngineScale,
					v.y
							/ getParentSketch().getSketchGlobals().physicsEngineScale,
					0);
			currentWorldTransform.transform(vecWorld);
			vecWorld.x -= currentWorldTransform.origin.x;
			vecWorld.scale(getParentSketch().getSketchGlobals().physicsEngineScale);

			if (i == 0 || vecWorld.x > maxX)
				maxX = vecWorld.x;
		}

		return maxX;
	}

	float getMaxY() {
		float maxY = -1;

		for (int i = 0; i < this.getPath().size(); i++) {
			SketchPoint v = (SketchPoint) this.getPath().get(i);

			if (maxY == -1 || v.y > maxY)
				maxY = v.y;
		}

		return maxY;
	}

	public float getMaxYWorldSpace(Transform currentWorldTransform) {
		float maxY = -1;

		for (int i = 0; i < this.getPath().size(); i++) {
			SketchPoint v = (SketchPoint) this.getPath().get(i);

			Vector3f vecWorld = new Vector3f(
					v.x
							/ getParentSketch().getSketchGlobals().physicsEngineScale,
					v.y
							/ getParentSketch().getSketchGlobals().physicsEngineScale,
					0);
			//Vector3f vecWorld = new Vector3f(1,1,1);
			//System.out.print(currentWorldTransform.origin);
			//System.out.print("before Y:"+vecWorld);
			currentWorldTransform.transform(vecWorld);
			//System.out.print(" after1  Y:"+vecWorld);

			vecWorld.y -= currentWorldTransform.origin.y;
			//System.out.print(" after2  Y:"+vecWorld);
			vecWorld.scale(getParentSketch().getSketchGlobals().physicsEngineScale);
			//System.out.println(" after3  Y:"+vecWorld);

			if (i == 0 || -vecWorld.y > maxY)
				maxY = -vecWorld.y;
		}

		return maxY;
	}

	float getMinX() {

		float minX = -1;

		for (int i = 0; i < this.getPath().size(); i++) {
			SketchPoint v = (SketchPoint) this.getPath().get(i);
			if (minX == -1 || v.x < minX)
				minX = v.x;

		}

		return minX;
	}

	public float getMinXWorldSpace(Transform currentWorldTransform) {
		float MinX = -1;

		for (int i = 0; i < this.getPath().size(); i++) {
			SketchPoint v = (SketchPoint) this.getPath().get(i);

			Vector3f vecWorld = new Vector3f(
					v.x
							/ getParentSketch().getSketchGlobals().physicsEngineScale,
					v.y
							/ getParentSketch().getSketchGlobals().physicsEngineScale,
					0);

			currentWorldTransform.transform(vecWorld);
			vecWorld.x -= currentWorldTransform.origin.x;
			vecWorld.scale(getParentSketch().getSketchGlobals().physicsEngineScale);

			if (i == 0 || vecWorld.x < MinX)
				MinX = vecWorld.x;
		}

		return MinX;
	}

	float getMinY() {

		float minY = -1;

		for (int i = 0; i < this.getPath().size(); i++) {
			SketchPoint v = (SketchPoint) this.getPath().get(i);
			if (minY == -1 || v.y < minY)
				minY = v.y;

		}

		return minY;
	}

	public float getMinYWorldSpace(Transform currentWorldTransform) {
		float MinY = -1;

		for (int i = 0; i < this.getPath().size(); i++) {
			SketchPoint v = (SketchPoint) this.getPath().get(i);

			Vector3f vecWorld = new Vector3f(
					v.x
							/ getParentSketch().getSketchGlobals().physicsEngineScale,
					v.y
							/ getParentSketch().getSketchGlobals().physicsEngineScale,
					0);
			currentWorldTransform.transform(vecWorld);
			vecWorld.y -= currentWorldTransform.origin.y;
			vecWorld.scale(getParentSketch().getSketchGlobals().physicsEngineScale);

			if (i == 0 || -vecWorld.y < MinY)
				MinY = -vecWorld.y;
		}

		return MinY;
	}

	public Sketch getParentSketch() {
		return parentSketch;
	}

	/**
	 * @return the path
	 */
	public SketchPath getPath() {
		return this.path;
	}

	public ArrayList getVector2DLoop() {
		return getPath().getVectorLoop();
	}

	float getWidth() {
		float minX = -1;
		float maxX = -1;

		for (int i = 0; i < this.getPath().size(); i++) {
			SketchPoint v = (SketchPoint) this.getPath().get(i);
			if (minX == -1 || v.x < minX)
				minX = v.x;

			if (maxX == -1 || v.x > maxX)
				maxX = v.x;
		}

		return maxX - minX;
	}

	public void optimize() {
		this.path.removeOverlapping(PApplet.EPSILON);
	}

	public void optimizeForCollision() {

		/*
		 * Vec2D lastStoredPoint = (Vec2D) this.l.get(0); float minDist = 5;
		 * float lastAngle = 0; for (int i = 2; i < this.l.size()-1; i++) {
		 * Vec2D curVec = (Vec2D) this.l.get(i);
		 * 
		 * if(curVec.distanceTo(lastStoredPoint) > minDist){ lastStoredPoint =
		 * curVec; }else{
		 * 
		 * this.l.remove(i-1); i--; }
		 * 
		 * }
		 */

	}

	public void render(PGraphics g) {
		getPath().isOutline = true;
		getPath().setClosed(true);
		getPath().render(g);
	}
	
	public void renderFlat(PGraphics g){
		getPath().renderFlat(g);
	}

	public void scale(float scale, toxi.geom.Vec3D centre) {
		this.getPath().scale(scale, centre);
	}

	public void setParentSketch(Sketch sketch) {
		parentSketch = sketch;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(SketchPath path) {
		this.path = path;
	}

	public void setupSpShape(spShape shape) {
		getPath().setupSpShape(shape);
	}
	public void addCollisionToSpShape(spShape shape) {
		getPath().addCollisionToSpShape(shape);
	}
	

}
