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

import nu.xom.Attribute;
import nu.xom.Element;
import cc.sketchchair.core.LOGGER;
import ToolPathWriter.CraftRoboWriter;
import ToolPathWriter.DXFWriter;
import ToolPathWriter.HPGLWriter;
import processing.core.PGraphics;

/**
 * Represents a single page in a multipage cutting file. Holds spOutlines.
 * @author gregsaul
 *
 */
public class spPage {
	spShapePack shapePack;
	spShapes shapes = new spShapes();
	float yPos  = 0;
	float xPos  = 0;
	float lastHeight  = 0;
	int tilePackCount = 0;
	
	public void packSmart(spShapes packingShapes) {
		LOGGER.info("packSmart");

		float lineWidth = 0;
		float lineHeight = 0;
		float tallestOnLine = 0;
		float gap = shapePack.shapeGap;
		float xPos = gap;
		float yPos = gap;
		float pageBorder = 10;
		float step = 5*shapePack.scale;
		boolean	ignorePageBounds = false;
		
		//Lets smart pack everything
		//we could start by ordering parts from large to small but it shouldn't be necessary
		
		for (int i = 0; i < packingShapes.l.size(); i++) {
			spShape shape = (spShape) packingShapes.l.get(i);
			spShape shapeCopy = shape.clone();
			shape.translate(-shape.outlines.getMinX(), -shape.outlines.getMinY());
		
			ignorePageBounds = false;
			if(shapeCopy.width >= (this.shapePack.materialWidth-(pageBorder*2)) && shapeCopy.height >= this.shapePack.materialHeight-(pageBorder*2)){
				ignorePageBounds = true;
			LOGGER.info("IGNORE BOUNDS");
			}
			
			boolean placeFound = false;
			//try each position and rotation on the material
			placingLoop :	for(float yOffsetTry = 0 ; yOffsetTry < this.shapePack.materialWidth ; yOffsetTry+=step){
				for(float xOffsetTry = 0 ; xOffsetTry < this.shapePack.materialHeight ; xOffsetTry+=step){
					for(float rotateTry = 0 ; rotateTry < (Math.PI*2) ; rotateTry+= (Math.PI/2)){
						
						shapeCopy = shape.clone();
						shapeCopy.translate(xOffsetTry, yOffsetTry);

						
						shapeCopy.rotate(rotateTry);
						//LOGGER.info("looking");
						boolean inBounds = shapeCopy.inBoundss(pageBorder,pageBorder,this.shapePack.materialWidth-(pageBorder*2),this.shapePack.materialHeight-(pageBorder*2));
					//	float shapesArea = (this.shapes.getWidth()*this.shapes.getHeight());
						
						
						if( !placeFound && (inBounds || ignorePageBounds)){
							boolean hasCollision =	this.shapes.hasCollisions(shapeCopy);
							if(!hasCollision ){
						this.shapes.add(shapeCopy);
						placeFound = true;
						
						packingShapes.l.remove(i);
						i--;
						break placingLoop;
						}
					}

				
				
				
				
					}
				}
			}
			
			
			
			
			
			

		
			
		}

	}
	
	public void packTile(spShapes packingShapes) {
		LOGGER.info("packTile Page");

		float lineWidth = 0;
		float lineHeight = 0;
		float tallestOnLine = 0;
		float gap = shapePack.shapeGap;
		 xPos = gap;
		 yPos = gap;

		for (int i = 0; i < packingShapes.l.size(); i++) {
			spShape shape = (spShape) packingShapes.l.get(i);

			if (shape.getWidth() + xPos + gap > this.shapePack.materialWidth) {
				xPos = gap;
				yPos += tallestOnLine + gap;
				lastHeight = tallestOnLine+ gap;
				tallestOnLine = 0;
			}

			if (yPos + shape.getHeight() + gap > this.shapePack.materialHeight) {
				i = packingShapes.l.size() + 1;

				return;
			}

			if (shape.getHeight() > tallestOnLine){
				tallestOnLine = shape.getHeight();
				lastHeight = shape.getHeight()+gap;
			}
			//shape.offsetX = xPos;
			//shape.offsetY = yPos;

			
			shape.translate(xPos,yPos);
			xPos += shape.getWidth() + gap;

			shape.packed = true;

			this.shapes.add(shape);
			packingShapes.l.remove(i);
			i--;
		}

	}
	
	
	
	public void packTileSmart(spShapes packingShapes) {
		
LOGGER.debug("packTileSmart Page");

		float lineWidth = 0;
		float lineHeight = 0;
		
		float tallestOnLine = 0;
		float shortestOnLine = -1;

		float gap = 2.0f*shapePack.scale;//shapePack.shapeGap;
		float xPos = gap;
		float yPos = gap;
		float pageBorder = 10;
		float startX =0;
		float startY = 0;
		
		float step = 5*shapePack.scale;
		
		//Lets smart pack everything
		//we could start by ordering parts from large to small but it shouldn't be necessary
		
		boolean firstPlace = true;
		boolean ignorePageBounds = false;
		
		for (int i = 0; i < packingShapes.l.size(); i++) {
			spShape shape = (spShape) packingShapes.l.get(i);
			spShape shapeCopy = shape.clone();
			shape.translate(-shape.outlines.getMinX(), -shape.outlines.getMinY());
			
			float shapeWidth = shapeCopy.width;
			
			if(shapeCopy.width == 0)
				break;
			
			boolean placeFound = false;
			firstPlace = true;

			ignorePageBounds = false;
			
			
			if(shapeCopy.width >= (this.shapePack.materialWidth-(pageBorder*2)) || shapeCopy.height >= this.shapePack.materialHeight-(pageBorder*2)){
				ignorePageBounds = true;
				LOGGER.debug("ignorePageBounds");
			}else{
				LOGGER.debug(" dont ignorePageBounds");

			}
			//try each position and rotation on the material
			placingLoop :	
				for(float yOffsetTry = pageBorder ; yOffsetTry < this.shapePack.materialHeight ; yOffsetTry+=step){	
				boolean collisionFoundOnRow = false;
				
					for(float xOffsetTry = pageBorder ; xOffsetTry < this.shapePack.materialWidth ; xOffsetTry+=step){
						
					
					if(yOffsetTry > this.shapePack.materialHeight)
						break placingLoop;

					//LOGGER.info("checking  x" + xOffsetTry + " y" + yOffsetTry + " of " +  this.shapePack.materialHeight);

					
					if(firstPlace){
						yOffsetTry = startY;
						xOffsetTry = startX;
					}
					
					
					shapeCopy = shape.clone();
						
					
					
						// if we can't fit on the page at least alight to the left
						if(ignorePageBounds){
							shapeCopy.translate(pageBorder, yOffsetTry);
							xOffsetTry = this.shapePack.materialWidth;

						}else{
						shapeCopy.translate(xOffsetTry, yOffsetTry);
						}
						
		
						
						//LOGGER.info("looking at " + xOffsetTry + " " + yOffsetTry);
						boolean inBounds = shapeCopy.inBounds(pageBorder,pageBorder,this.shapePack.materialWidth-(pageBorder*2),this.shapePack.materialHeight-(pageBorder*2));
					//	float shapesArea = (this.shapes.getWidth()*this.shapes.getHeight());
						
						

						
						if(!firstPlace && !collisionFoundOnRow && !ignorePageBounds && xOffsetTry + shapeCopy.width + gap > this.shapePack.materialWidth-pageBorder){
							xOffsetTry = pageBorder;
							yOffsetTry += shortestOnLine+(gap*2);
							shortestOnLine = -1;
						}
						firstPlace = false;

							
						if( !placeFound && (inBounds || ignorePageBounds)){
							boolean hasCollision = this.shapes.hasCollisionsBounds(shapeCopy, gap);
						
						if(!hasCollision ){
						this.shapes.add(shapeCopy);
						placeFound = true;
						
						startX = xOffsetTry + shapeCopy.width + (gap);
						startY = yOffsetTry;
						
						if(ignorePageBounds){
							startY += shapeCopy.height + (gap*2);
						}

						
						if(shortestOnLine == -1 || shapeCopy.height < shortestOnLine)
							shortestOnLine = shapeCopy.height;
						
						
						packingShapes.l.remove(i);
						i--;
						break placingLoop;
						}else{
							collisionFoundOnRow = true;
						}

					}
				}
			}
			
			
			
			
			
			

		
			
		}

	}
	
	

	public void render(PGraphics g) {
		g.stroke(0);
		g.fill(250);
		g.rect(0, 0, this.shapePack.materialWidth, this.shapePack.materialHeight);
		shapes.renderPage(g);
	}
	
	public void renderPickBuffer(PGraphics pickBuffer) {
		pickBuffer.stroke(0);
		pickBuffer.fill(250);
		pickBuffer.rect(0, 0, this.shapePack.materialWidth, this.shapePack.materialHeight);
		shapes.renderPickBufferPage(pickBuffer);		
	}
	

	public void renderList(PGraphics g) {
		shapes.renderPage(g);		
	}
	
	
	public void renderPickBufferList(PGraphics pickBuffer) {
		shapes.renderPickBufferPage(pickBuffer);		

	}
	
	
	
	public void renderDXF(DXFWriter dxf, float offsetX, float offsetY) {
		//g.stroke(0);
		//g.fill(255);
		//g.rect(0,0,this.shapePack.pageWidth,this.shapePack.pageHeight);
		shapes.renderPageDXF(dxf, offsetX, offsetY);
	}

	public void renderToPlotter(HPGLWriter craftRoboWriter) {
		this.shapes.renderToPlotter(craftRoboWriter);

	}

	public float getHeight() {
		return yPos+lastHeight;
	}

	public Element toXML() {

			Element element = new Element("g","http://www.w3.org/2000/svg");
			element.addAttribute(new Attribute("id","page"));
			element.appendChild(this.shapes.toXML());
			
			return element;

	}



	



}
