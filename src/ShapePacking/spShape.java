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

import nu.xom.Attribute;
import nu.xom.Element;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.PickBuffer;
import cc.sketchchair.core.SETTINGS;
import cc.sketchchair.sketch.Sketch;
import cc.sketchchair.sketch.SketchSpline;

import ToolPathWriter.DXFWriter;
import ToolPathWriter.HPGLWriter;
import processing.core.PGraphics;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

/**
 * A low level base class for any SketchShapes in the package.
 * @author gregsaul
 *
 */
public class spShape {
	spOutlines outlines = new spOutlines();
	spOutlines collisionOutlines = new spOutlines();

	Sketch sketch;
	boolean built = false; 
	
	float width = 0;
	float height = 0;
	float offsetX = 0;
	float offsetY = 0;
	float scale = 1;
	float rotate = 0;
	
	
	public Object linkedObject;

	private String label;

	public boolean packed = false;
	public spShapePack shapePack;


	public void addOutline(ArrayList l) {
		outlines.add(new spOutline(new Sketch(GLOBAL.uiTools.SketchTools, GLOBAL.SketchGlobals),l));
	}

	public void addCollisionOutline(ArrayList l) {
		collisionOutlines.add(new spOutline(new Sketch(GLOBAL.uiTools.SketchTools, GLOBAL.SketchGlobals),l));
	}
	
	
	
	
	public void build() {

		
		this.outlines.build();
		this.collisionOutlines.build();
		width = this.outlines.getWidth();
		height = this.outlines.getHeight();
		
		
		for(int i =0; i < this.collisionOutlines.l.size();i++){
			spOutline outline = (spOutline) this.collisionOutlines.l.get(i);
			outline.offsetPath(1.5f*shapePack.scale);
			//float scaleCol = 0.1f;
			//outlineClone.scale(scaleCol,new Vec3D(0,0,0));		
			//outlineClone.offsetPath(new Vec2D(-(width-(width/scaleCol))/2,-(height-(height/scaleCol))/2));	
		}
		
		//collisionOutlines.simplifyDouglasPeucker(1f);


	}

	public float getHeight() {
		return height;
	}

	public float getWidth() {
		return width;
	}

	public void render(PGraphics g) {

		g.fill(0);
		if(GLOBAL.shapePack.addLabels && this.label != null){
		g.text(this.label,outlines.getMinX()-(g.textWidth(this.label)+1),outlines.getMinY()- (1));
		}

		g.fill(0);
		g.noStroke();
		//g.stroke(0,0,0);
		this.outlines.render(g);
		
		if(SETTINGS.DEBUG){
			g.stroke(255,0,0);
			this.collisionOutlines.render(g);
			this.collisionOutlines.renderDebug(g);
		}


	}
	
	
	public void renderPickBuffer(PGraphics pickBuffer) {
				
		pickBuffer.fill(PickBuffer.getInstance().getPickColour(this.linkedObject));
		pickBuffer.noStroke();
		this.outlines.render(pickBuffer);
		
	}
	
	

	public void renderDXF(DXFWriter dxf, float offsetX, float offsetY) {

		//if(this.label != null)
		//g.text(this.label,outlines.getMinX(),outlines.getMinY());

		this.outlines.renderDXF(dxf, offsetX, offsetY);

	}

	public void renderToPlotter(HPGLWriter hpglWriter) {
		this.outlines.renderToPlotter(hpglWriter);

	}

	public void scale(float scale) {
		this.outlines.scale(scale);
		this.collisionOutlines.scale(scale);

	}

	public void setLabel(String string) {
		this.label = string;
	}

	public void translate(float xPos, float yPos) {
		this.outlines.translate(xPos,yPos);
		this.collisionOutlines.translate(xPos,yPos);

		
	}
	
	protected spShape clone(){
		spShape clone = new spShape();
		clone.outlines = this.outlines.clone();
		clone.collisionOutlines = this.collisionOutlines.clone();
		clone.height =  this.height;
		clone.width = this.width;
		clone.offsetX = this.offsetX;
		clone.offsetY = this.offsetY;
		clone.label = this.label;
		clone.linkedObject = this.linkedObject;
		return clone;
	}

	public void rotate(float r) {
		Vec2D centre = this.outlines.getCentre();
		this.outlines.rotate(r,centre);
		this.collisionOutlines.rotate(r,centre);


	}

	public boolean collides(spShape shapeOther) {
		return this.collisionOutlines.collides(shapeOther);
	}

	public boolean inBoundss(float pageX, float pageY, float pageW,float pageH) {
		if(this.collisionOutlines.getMaxX() > pageX+pageW || this.collisionOutlines.getMinX() < pageX ||
			this.collisionOutlines.getMaxY() > pageY+pageH || this.collisionOutlines.getMinY() < pageY	
				)
			return false;
		
		return true;
		
	}
	public boolean inBounds(float pageX, float pageY, float pageW,float pageH) {
		float maxX = this.outlines.getMaxX() ; 
		float minX = this.outlines.getMinX();
		float maxY = this.outlines.getMaxY() ;
		float minY = this.outlines.getMinY() ;
		
		
		if(maxX > pageX+pageW ||  minX < pageX ||
				maxY > pageY+pageH || minY< pageY	
				)
			return false;
		
		return true;
		
	}
	

	public Element toXML() {
		Element element = new Element("g","http://www.w3.org/2000/svg");
		element.addAttribute(new Attribute("id","shape"));		
		
		element.appendChild(outlines.toXML());
		
		
		
		
		
		return element;

		
	}

	public boolean collidesBounds(spShape shapeOther, float border) {
		float x1max = this.outlines.getMaxX() + border;
		float x1min = this.outlines.getMinX()- border;
		float y1max = this.outlines.getMaxY()+ border;
		float y1min = this.outlines.getMinY()- border;

		float x2max = shapeOther.outlines.getMaxX()+ border;
		float x2min = shapeOther.outlines.getMinX()- border;
		float y2max = shapeOther.outlines.getMaxY()+ border;
		float y2min = shapeOther.outlines.getMinY()- border;

		if ((x1min < x2max) && (x1max > x2min) && (y1min < y2max)
				&& (y1max > y2min)

		) {
			return true;
		} else {
			return false;
		}
	}





}
