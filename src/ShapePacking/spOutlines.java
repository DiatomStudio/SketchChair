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

import nu.xom.Attribute;
import nu.xom.Element;

import cc.sketchchair.core.LOGGER;

import ToolPathWriter.DXFWriter;
import ToolPathWriter.HPGLWriter;
import processing.core.PGraphics;

import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

/**
 * Container class for spOutlines.
 * @author gregsaul
 *
 */
public class spOutlines {
	List l = new ArrayList();

	public void add(spOutline spOutline) {
		this.l.add(spOutline);
	}

	
	
	public spOutlines clone() {
		
		spOutlines clone = new spOutlines();
	
		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);
			clone.add(outline.clone());

		}
		return clone;
		
	}
	public void build() {
		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);
			if (outline.l.size() < 2){
				this.l.remove(i);
			i--;
			}

		}

		//offset to 0
		float minX = getMinX();
		float minY = getMinY();		
		
		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);
			outline.translate(new Vec2D(-minX,-minY));

		}
	}
	


	public float getHeight() {

		float minY = 0;
		float maxY = 0;

		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);

			if (i == 0) {
				minY = outline.getMinY();
				maxY = outline.getMaxY();
			}

			if (outline.getMinY() < minY)
				minY = outline.getMinX();

			if (outline.getMaxY() > maxY)
				maxY = outline.getMaxY();

		}

		return maxY - minY;

	}

	public float getMinX() {

		float minX = -1;

		for (int i = 0; i < l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);


			if (i == 0 || outline.getMinX() < minX)
				minX = outline.getMinX();
			
			
		}

		return minX;

	}

	public float getMinY() {

		float minY = -1;

		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);

			if (i == 0 || outline.getMinY() < minY)
				minY = outline.getMinY();

		}

		return minY;

	}
	
	
	
	
	
	
	

	public float getMaxX() {

		float maxX = -1;

		for (int i = 0; i < l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);

			if (i == 0 || outline.getMaxX() > maxX)
				maxX = outline.getMaxX();
		}

		return maxX;

	}

	public float getMaxY() {

		float maxY = -1;

		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);

			if (i == 0 || outline.getMaxY() > maxY)
				maxY = outline.getMaxY();

		}

		return maxY;

	}

	
	
	
	
	

	public float getWidth() {

		float minX = 0;
		float maxX = 0;

		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);

			if (i == 0) {
				minX = outline.getMinX();
				maxX = outline.getMaxX();
			}

			if (outline.getMinX() < minX)
				minX = outline.getMinX();

			if (outline.getMaxX() > maxX)
				maxX = outline.getMaxX();

		}

		return maxX - minX;

	}

	public void render(PGraphics g) {
		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);
			outline.render(g);

		}
	}
	
	
	public void renderDebug(PGraphics g) {
		g.noFill();
		g.stroke(255,0,0);
		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);
			outline.renderDebug(g);

		}		
	}
	
	

	public void renderDXF(DXFWriter dxf, float offsetX, float offsetY) {
		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);
			outline.renderDXF(dxf, offsetX, offsetY);

		}
	}

	/*
		public void addBeziers(Hashtable beziers) {
			spOutline outline = (spOutline) this.l.get(this.l.size()-1);
			outline.addBeziers(beziers);
		}

	*/

	public void renderToPlotter(HPGLWriter hpglWriter) {
		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);
			outline.renderToPlotter(hpglWriter);

		}
	}

	public void scale(float scale) {
		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);

			outline.scale(scale);
		}
	}

	public void translate(float xPos, float yPos) {
		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);

			outline.translate(new Vec2D(xPos,yPos));
		}		
	}

	public void rotate(float r, Vec2D centre) {
		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);
			outline.rotate(r,centre);
		}				
	}

	public boolean collides(spShape shapeOther) {
		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);
			
			for (int i2 = 0; i2 < shapeOther.collisionOutlines.l.size(); i2++) {
				spOutline outlineOther = (spOutline) shapeOther.collisionOutlines.l.get(i2);
				if((!outline.equals(outlineOther)) && (outline.intersects(outlineOther) || outline.inside(outlineOther)))
				return true;
			}	
			
			
		}	
		return false;
	}



	public void simplifyDouglasPeucker(float simplifyAmount) {
		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);
			outline.simplifyDouglasPeucker(simplifyAmount);
		}
	}



	public Vec2D getCentre() {
		return new Vec2D(this.getMinX()+(this.getWidth()/2),this.getMinY()+(this.getHeight()/2));
	}



		public Element toXML() {
		Element element = new Element("g","http://www.w3.org/2000/svg");
		element.addAttribute(new Attribute("id","outlines"));				
		
		for (int i = 0; i < this.l.size(); i++) {
			spOutline outline = (spOutline) l.get(i);
			element.appendChild(outline.toXML_SVG());
		}
		
		
		return element;

		
	}



		


}
