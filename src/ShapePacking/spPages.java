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

import cc.sketchchair.core.LOGGER;
import cc.sketchchair.sketch.SketchShape;

import nu.xom.Attribute;
import nu.xom.Element;

import ToolPathWriter.CraftRoboWriter;
import ToolPathWriter.DXFWriter;
import ToolPathWriter.HPGLWriter;

import processing.core.PGraphics;
import processing.pdf.PGraphicsPDF;

/**
 * Container class for spPages.
 * @author gregsaul
 *
 */
public class spPages {
	public List l = new ArrayList();
	spShapePack shapePack;

	public void addShapes(spShapes shapes) {
		spPage page = new spPage();

		//if(scale){
		//g.translate(200,30);
		//g.rect(0,0,targetWidth,targetHeight);
		//g.translate(pageBoarder, pageBoarder);

		//}else
		//g.translate(30,30);

		float lineWidth = 200;//getWidth();
		float currentLineWidth = 0;
		float tallestFound = 0;
		//g.textSize(11);
		//g.noFill();

		//float pageScale = targetWidth / lineWidth; 

		//if(scale){

		//g.scale((targetWidth / lineWidth));

		//}
		float currentY = 0;
		for (int i = 0; i < l.size(); i++) {
			spShape shape = (spShape) l.get(i);
			//System.out.println(shape.getWidth());
			//g.pushMatrix();

			/*
			if((currentLineWidth+shape.getWidth()) > targetWidth-(pageBoarder*2)){
				currentY += tallestFound + yGap ;
				currentLineWidth = 0;
				tallestFound = 0;
			}
			*/

			//g.translate(currentLineWidth, currentY);
			//shape.render(g);
			//g.ellipse(0,0, 4, 4);
			//currentLineWidth += shape.getWidth()+xGap;

			if (shape.getHeight() > tallestFound)
				tallestFound = shape.getHeight();

			//System.out.println(currentLineWidth + " : " + lineWidth);

			//g.popMatrix();
		}
		//g.popMatrix();		
	}

	public void empty() {
		this.l.clear();
	}
	
	public void packShapesTile02(spShapes shapes) {

		spShapes packingShapes = shapes.copy();
		int i = 0;

		while (packingShapes.l.size() > 0 && i < 10000) {
			spPage packPage = new spPage();
			packPage.shapePack = this.shapePack;

			packPage.packTile(packingShapes);
			this.l.add(packPage);
			i++;
		}
	}
	
	
	

	public void packShapesTile(spShapes shapes) {

		LOGGER.debug("packTileSmart");

		spShapes packingShapes = shapes.copy();
		int i = 0;

		while (packingShapes.l.size() > 0 && i < 12) {
			spPage packPage = new spPage();
			packPage.shapePack = this.shapePack;

			packPage.packTileSmart(packingShapes);
			this.l.add(packPage);
			i++;
		}
	}
	
	
	public void packShapesSmart(spShapes shapes) {

		spShapes packingShapes = shapes.copy();
		int i = 0;

		while (packingShapes.l.size() > 0 && i < 10) {
			spPage packPage = new spPage();
			packPage.shapePack = this.shapePack;

			packPage.packSmart(packingShapes);
			this.l.add(packPage);
			i++;
		}
	}

	void render(PGraphics g) {

		for (int i = 0; i < l.size(); i++) {
			spPage page = (spPage) this.l.get(i);
			g.pushMatrix();
			g.translate(this.shapePack.materialWidth * i, 0);
			page.render(g);
			g.popMatrix();

		}
	}
	
	public void renderPickBuffer(PGraphics pickBuffer) {
		for (int i = 0; i < l.size(); i++) {
			spPage page = (spPage) this.l.get(i);
			pickBuffer.pushMatrix();
			pickBuffer.translate(this.shapePack.materialWidth * i, 0);
			page.renderPickBuffer(pickBuffer);
			pickBuffer.popMatrix();

		}		
	}
	
	
	
	public void renderList(PGraphics g) {
		for (int i = 0; i < l.size(); i++) {
			spPage page = (spPage) this.l.get(i);
			g.pushMatrix();
			g.translate(this.shapePack.materialWidth * i, 0);
			page.renderList(g);
			g.popMatrix();

		}		
	}
	
	
	public void renderPickBufferList(PGraphics pickBuffer) {
		for (int i = 0; i < l.size(); i++) {
			spPage page = (spPage) this.l.get(i);
			pickBuffer.pushMatrix();
			pickBuffer.translate(this.shapePack.materialWidth * i, 0);
			page.renderPickBufferList(pickBuffer);
			pickBuffer.popMatrix();

		}			
	}
	
	

	public void renderDXF(DXFWriter dxf) {
		for (int i = 0; i < l.size(); i++) {
			spPage page = (spPage) this.l.get(i);
			page.renderDXF(dxf, page.shapes.getWidth(), 0);
			//dxf.translate(page.shapes.getWidth(), 0);
		}
	}

	public void renderPDF(PGraphicsPDF pdf, float pageScale) {
		for (int i = 0; i < l.size(); i++) {
			spPage page = (spPage) this.l.get(i);

			pdf.scale(pageScale);
			page.render(pdf);

			if (i != l.size() - 1)
				pdf.nextPage();

		}
	}

	public void renderToPlotter(HPGLWriter craftRoboWriter) {
		for (int i = 0; i < l.size(); i++) {
			spPage page = (spPage) this.l.get(i);

			page.renderToPlotter(craftRoboWriter);
			//craftRoboWriter.nextPage();
			if (craftRoboWriter instanceof DXFWriter)
				craftRoboWriter.translate(this.shapePack.materialWidth, 0);

		}
	}

	public float getHeight() {
float returnHeight = 0;
for (int i = 0; i < l.size(); i++) {
	spPage page = (spPage) this.l.get(i);
	returnHeight += page.getHeight();
}
return returnHeight;
	}

	public Element toXML() {
		Element element = new Element("g","http://www.w3.org/2000/svg");
		element.addAttribute(new Attribute("id","pages"));

		
		for (int i = 0; i < l.size(); i++) {
			spPage page = (spPage) this.l.get(i);
			element.appendChild(page.toXML());	
		}
		
		return element;

		
	}







}
