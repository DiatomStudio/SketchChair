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
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Element;

import cc.sketchchair.core.LOGGER;
import ToolPathWriter.DXFWriter;
import ToolPathWriter.HPGLWriter;

import processing.core.PGraphics;

/**
 * Container class for spShapes.
 * @author gregsaul
 *
 */
public class spShapes {
	float plotterWidth = 180;

	List l = new ArrayList();

	float targetWidth = 180;
	float targetHeight = 250;
	float pageBoarder = 20;

	private float yGap = 5;
	private float xGap = 5;

	public void add(spShape shape) {
		this.l.add(shape);
	}

	public spShapes copy() {
		spShapes shapesCopy = new spShapes();

		for (int i = 0; i < l.size(); i++) {
			spShape shape = (spShape) l.get(i);
			shapesCopy.add(shape);
		}
		return shapesCopy;
	}

	public void empty() {
		this.l = new ArrayList();
	}

	float getWidth() {
		float width = 0;
		for (int i = 0; i < l.size(); i++) {
			spShape shape = (spShape) l.get(i);
			width += shape.getWidth();
		}
		return width;
	}

	
	public float getHeight() {
		float height = 0;
		for (int i = 0; i < l.size(); i++) {
			spShape shape = (spShape) l.get(i);
			height += shape.getHeight();
		}
		return height;		
	}
	
	
	public void renderList(PGraphics g) {
		renderList(g, true);
	}

	public void renderList(PGraphics g, boolean scale) {
		g.pushMatrix();
		if (scale) {
			g.translate(200, 30);
			g.rect(0, 0, targetWidth, targetHeight);
			g.translate(pageBoarder, pageBoarder);

		} else
			g.translate(30, 30);

		float lineWidth = getWidth();
		float currentLineWidth = 0;
		float tallestFound = 0;
		g.textSize(11);
		g.noFill();

		//float pageScale = targetWidth / lineWidth; 

		if (scale) {

			//g.scale((targetWidth / lineWidth));

		}
		float currentY = 0;
		for (int i = 0; i < l.size(); i++) {
			spShape shape = (spShape) l.get(i);
			//System.out.println(shape.getWidth());
			g.pushMatrix();

			if ((currentLineWidth + shape.getWidth()) > targetWidth
					- (pageBoarder * 2)) {
				currentY += tallestFound + yGap;
				currentLineWidth = 0;
				tallestFound = 0;
			}

			g.translate(currentLineWidth, currentY);
			shape.render(g);
			//g.ellipse(0,0, 4, 4);
			currentLineWidth += shape.getWidth() + xGap;

			if (shape.getHeight() > tallestFound)
				tallestFound = shape.getHeight();

			//System.out.println(currentLineWidth + " : " + lineWidth);

			g.popMatrix();
		}
		g.popMatrix();

	}

	public void renderPage(PGraphics g) {
		LOGGER.info("spShapes.renderPage: Rendering " + l.size() + " shapes");
		for (int i = 0; i < l.size(); i++) {
			spShape shape = (spShape) l.get(i);
			g.pushMatrix();
			//g.translate(shape.offsetX, shape.offsetY);
			LOGGER.debug("spShapes.renderPage: Rendering shape " + i);
			shape.render(g);
			g.popMatrix();
		}
		LOGGER.info("spShapes.renderPage: Finished rendering all shapes");
	}

	
	public void renderPickBufferPage(PGraphics pickBuffer) {
		for (int i = 0; i < l.size(); i++) {
			spShape shape = (spShape) l.get(i);
			pickBuffer.pushMatrix();
			//g.translate(shape.offsetX, shape.offsetY);
			shape.renderPickBuffer(pickBuffer);
			pickBuffer.popMatrix();
		}
	}
	
	
	
	public void renderPageDXF(DXFWriter dxf, float offsetX, float offsetY) {
		for (int i = 0; i < l.size(); i++) {
			spShape shape = (spShape) l.get(i);
			//shape.renderDXF(dxf, offsetX + shape.offsetX, offsetY
			//		+ shape.offsetY);
			shape.renderDXF(dxf,0,0);
		}

	}

	public void renderToPlotter(HPGLWriter hpglWriter) {
		// TODO Auto-generated method stub

		for (int i = 0; i < l.size(); i++) {
			spShape shape = (spShape) l.get(i);
			hpglWriter.pushMatrix();
			hpglWriter.translate(shape.offsetX, shape.offsetY);
			shape.renderToPlotter(hpglWriter);
			hpglWriter.popMatrix();
		}

		/*
		float lineWidth = plotterWidth;
		float currentLineWidth = 0;
		float tallestFound = 0;
		float currentY = 0;
		
		for(int i = 0; i < l.size(); i++){
			spShape shape = (spShape) l.get(i);
			hpglWriter.resetMatrix();
			hpglWriter.translate(pageBoarder,pageBoarder);
			
		
			if((currentLineWidth+shape.getWidth()) > targetWidth-(pageBoarder*2)){
				currentY += tallestFound + yGap;
				currentLineWidth = 0;
				tallestFound = 0;
			}
			
			hpglWriter.translate(currentLineWidth, currentY);
			shape.renderToPlotter(hpglWriter);
			//g.ellipse(0,0, 4, 4);
			currentLineWidth += shape.getWidth()+xGap;

			if(shape.getHeight() > tallestFound)
				tallestFound = shape.getHeight();
			
			

		
			}
			*/

	}

	public void scale(float scale) {
		for (int i = 0; i < l.size(); i++) {
			spShape shape = (spShape) l.get(i);
			shape.scale(scale);
			shape.build();
		}

	}

	public boolean hasCollisions() {
		for (int i = 0; i < l.size(); i++) {
			spShape shape = (spShape) l.get(i);
			
			for (int i2 = 0; i2 < l.size(); i2++) {
				spShape shapeOther = (spShape) l.get(i2);
			
				
				if((!shape.equals(shapeOther)) && shape.collides(shapeOther))
					return true;
				
			}
		}		
		
		return false;
	}

	public boolean hasCollisions(spShape shape) {
		for (int i = 0; i < l.size(); i++) {
			spShape shapeOther = (spShape) l.get(i);
			if((!shape.equals(shapeOther)) && shape.collides(shapeOther))
				return true;
		}		
		
		return false;	
	}

	
	public boolean hasCollisionsBounds(spShape shape, float border) {
		for (int i = 0; i < l.size(); i++) {
			spShape shapeOther = (spShape) l.get(i);
			if((!shape.equals(shapeOther)) && shape.collidesBounds(shapeOther,border))
				return true;
		}		
		
		return false;	
	}
	
	public Element toXML() {
		Element element = new Element("g","http://www.w3.org/2000/svg");
		element.addAttribute(new Attribute("id","shapes"));		
		
		for (int i = 0; i < l.size(); i++) {
			spShape shape = (spShape) l.get(i);
			element.appendChild(shape.toXML());
		}		
		
		return element;

		
	}

	


}
