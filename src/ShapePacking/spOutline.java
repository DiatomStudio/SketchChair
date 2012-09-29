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
package ShapePacking;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import cc.sketchchair.functions.functions;
import cc.sketchchair.sketch.Sketch;
import cc.sketchchair.sketch.SketchPath;
import cc.sketchchair.sketch.SketchPoint;
import cc.sketchchair.sketch.SketchSpline;

import ToolPathWriter.DXFWriter;
import ToolPathWriter.HPGLWriter;

import processing.core.PConstants;
import processing.core.PGraphics;

import toxi.geom.Vec2D;

/**
 * Cutting paths for pieces. 
 * @author gregsaul
 *
 */
public class spOutline extends SketchPath {
//	List<spPoint> l = new ArrayList<spPoint>(); // list of Vec2D points (toxi)
//
//	//Map bezierPoints = new Hashtable();
//
//	spOutline(ArrayList l2) {
//		this.l = l2;
//
//	}
//
//	//public spOutline(ArrayList l2) {
//	//	this.l = l2;
//	//this.bezierPoints = beziers;
//
//	//System.out.println(this.bezierPoints);
//	//}
//
//	public float getMaxX() {
//		float maxX = 0;
//		for (int i = 0; i < l.size(); i++) {
//			spPoint vec = (spPoint) l.get(i);
//			if (i == 0)
//				maxX = vec.x;
//
//			if (vec.x > maxX)
//				maxX = vec.x;
//		}
//		return maxX;
//	}
//
//	public float getMaxY() {
//		float maxY = 0;
//		for (int i = 0; i < l.size(); i++) {
//			spPoint vec = (spPoint) l.get(i);
//			if (i == 0)
//				maxY = vec.y;
//
//			if (vec.y > maxY)
//				maxY = vec.y;
//		}
//		return maxY;
//	}
//
//	public float getMinX() {
//		float minX = 0;
//
//		for (int i = 0; i < l.size(); i++) {
//			spPoint vec = (spPoint) l.get(i);
//
//			if (i == 0)
//				minX = vec.x;
//
//			if (vec.x < minX)
//				minX = vec.x;
//
//		}
//		return minX;
//
//	}
//
//	public float getMinY() {
//		float minY = 0;
//		for (int i = 0; i < l.size(); i++) {
//			spPoint vec = (spPoint) l.get(i);
//			if (i == 0)
//				minY = vec.y;
//
//			if (vec.y < minY)
//				minY = vec.y;
//		}
//		return minY;
//	}
//
//	public void offsetPath(Vec2D offset) {
//		for (int i = 0; i < l.size(); i++) {
//			spPoint vec = (spPoint) l.get(i);
//			spPoint newVec = new spPoint(vec.x + offset.x, vec.y + offset.y);
//
//			//	if(vec.containsBezier())
//			//		System.out.println("OUTLINE " + vec + " : " + vec.controlPoint1 + " : " + vec.controlPoint2);
//
//			if (vec.containsBezier()) {
//
//				if (vec.controlPoint1 != null) {
//					newVec.controlPoint1 = new Vec2D(vec.controlPoint1.x
//							+ offset.x, vec.controlPoint1.y + offset.y);
//				}
//
//				if (vec.controlPoint2 != null) {
//					newVec.controlPoint2 = new Vec2D(vec.controlPoint2.x
//							+ offset.x, vec.controlPoint2.y + offset.y);
//				}
//
//			}
//			/*
//			if(this.bezierPoints.containsKey(vec)){
//				BezierControlNode bc = (BezierControlNode) this.bezierPoints.get(vec);
//				bc.c1.x += offset.x;
//				bc.c1.y += offset.y;
//				bc.c2.x += offset.x;
//				bc.c2.y += offset.y;
//				this.bezierPoints.remove(vec);
//				this.bezierPoints.put(newVec, bc);
//			}
//			*/
//
//			this.l.set(i, newVec);
//
//		}
//	}
//
	public void render(PGraphics g) {
		this.setClosed(true);
		this.renderFace(g);
		//this.renderNodes(g);
	}

	public spOutline(Sketch parentSketch, ArrayList<SketchPoint> outline) {
		super(parentSketch, outline);
		this.setClosed(true);
		// TODO Auto-generated constructor stub
	}

	public void renderDXF(DXFWriter dxf, float offsetX, float offsetY) {

		for (int i = 1; i < this.l.size() + 1; i++) {
			SketchPoint curVec = null;
			SketchPoint preVec = null;

			if (i >= 1)
				preVec = (SketchPoint) this.l.get(i - 1);

			// --- last or first point ---
			if (i == 1) {
				dxf.lineTo(preVec.x + offsetX, preVec.y + offsetY);
			}

			if (i == this.l.size()) {
				curVec = (SketchPoint) this.l.get(0);
			} else {
				curVec = (SketchPoint) this.l.get(i);
			}

			if (curVec.containsBezier() || preVec.containsBezier()) {

				for (float t = 0; t <= 1; t += 0.1f) {
					float x = functions.bezierPoint(preVec.x,
							preVec.getControlPoint2().x,
							curVec.getControlPoint1().x, curVec.x, t);
					float y = functions.bezierPoint(preVec.y,
							preVec.getControlPoint2().y,
							curVec.getControlPoint1().y, curVec.y, t);
					dxf.lineTo(x + offsetX, y + offsetY);
				}

			} else {
				dxf.lineTo(curVec.x + offsetX, curVec.y + offsetY);
			}

			if (i == this.l.size()) {
				dxf.lineTo(curVec.x + offsetX, curVec.y + offsetY);

			}

		}

	}

	/*
				public void addBeziers(Hashtable beziers) {
					this.bezierPoints = beziers;	
				}
				*/

	public void renderToPlotter(HPGLWriter hpglWriter) {
		for (int i = 1; i < this.l.size() + 1; i++) {
			SketchPoint curVec = null;
			SketchPoint preVec = null;

			// --- last or first point ---
			if (i == 1) {
				curVec = (SketchPoint) this.l.get(0);
				hpglWriter.move(curVec.x, curVec.y);
			}

			if (i >= 1)
				preVec = (SketchPoint) this.l.get(i - 1);

			if (i == this.l.size()) {
				curVec = (SketchPoint) this.l.get(0);
				preVec = (SketchPoint) this.l.get(this.l.size() - 1);

			} else {
				curVec = (SketchPoint) this.l.get(i);
			}

			/*
			if (this.bezierPoints.containsKey(curVec) || preVec != null
					&& this.bezierPoints.containsKey(preVec)) {

				spPoint c1 = preVec;
				spPoint c2 = curVec;

				if (c1 == null)
					c1 = new spPoint(0, 0);

				if (preVec != null && this.bezierPoints.containsKey(preVec)) {
					BezierControlNode bc = (BezierControlNode) this.bezierPoints
							.get(preVec);
					c1 = bc.c2;
				}

				if (this.bezierPoints.containsKey(curVec)) {
					BezierControlNode bc = (BezierControlNode) this.bezierPoints
							.get(curVec);
					c2 = bc.c1;
				}

				if (c1 != null && c2 != null && curVec != null)
				//	g.bezierVertex(c1.x, c1.y, c2.x, c2.y, curVec.x, curVec.y);
				hpglWriter.bezier(preVec.x,preVec.y,c1.x, c1.y, c2.x, c2.y, curVec.x, curVec.y);


			} 
			*/

			if (curVec.containsBezier() || preVec.containsBezier()) {
				hpglWriter.bezier(preVec.x, preVec.y,
						preVec.getControlPoint2().x,
						preVec.getControlPoint2().y,
						curVec.getControlPoint1().x,
						curVec.getControlPoint1().y, curVec.x, curVec.y);

			} else {
				if (false) {
					//g.curveVertex(curVec.x, curVec.y);
				} else {
					//g.vertex(curVec.x, curVec.y);
					hpglWriter.lineTo(curVec.x, curVec.y);
				}
			}

		}

	}
//
//	public void scale(float scale) {
//		for (int i = 0; i < l.size(); i++) {
//			spPoint vec = (spPoint) l.get(i);
//			vec.scaleSelff(scale);
//		}
//		/*
//		for (Object obj : this.bezierPoints.values()){
//
//			BezierControlNode bc = (BezierControlNode)obj;
//			
//			bc.c1.scaleSelf(scale);
//			bc.c2.scaleSelf(scale);
//			
//		}
//		*/
//
//	}
//
	
		
//		ArrayList points =  new ArrayList<spPoint>(); 
//		
//		for(int i =0 ; i < this.l.size() ; i++){
//			spPoint point = this.l.get(i);
//			points.add(point.clone()); 
//		}
//		spOutline clone = new spOutline(points);
//		return clone;
//	}

	public spOutline clone() {
		spOutline clone = new spOutline(this.parentSketch,this.cloneArray());
		return clone;
		
	}
	
	public void offsetPath(float o){
		super.offsetPath(o);
	}

	



}
