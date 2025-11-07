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
package ModalGUI;
import cc.sketchchair.core.KeyEventSK;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.MouseEventSK;
import processing.core.PGraphics;
import processing.core.PImage;

public class GUIScrollbar extends GUIComponent {
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	float width;
	float maxVal;
	float minVal;
	public float curVal = 0f;
	boolean clickedOn = false;

	float selectArea = 15f;
	float selectAreaLen = selectArea;

	float trigSize = 14f;
	public int orientation = 0;
	private float clickOffsetX;
	private float mouseClickDeltaX;
	private float mouseClickDeltaY;
	private float scrollX;
	private float scrollY;

	PImage handle = null;

	public GUIScrollbar(float x, float y, float width, float minVal,
			float maxVal, int orientation, ModalGUI c) {
		this.setController(c);

		this.setPos(x, y);
		this.width = width;
		this.minVal = minVal;
		this.maxVal = maxVal;
		this.orientation = orientation;
		//this.handle = controller.applet.loadImage("handlescrollhoriz.png");

	}

	public GUIScrollbar(float x, float y, float width, float minVal,
			float maxVal, ModalGUI c) {
		this.setController(c);

		this.setPos(x, y);
		this.width = width;
		this.minVal = minVal;
		this.maxVal = maxVal;
		//this.handle = controller.applet.loadImage("handlescrollhoriz.png");
	}

	private void changeval() {
	
		if (this.orientation == HORIZONTAL) {

			float mouseX = controller.applet.mouseX - mouseClickDeltaX;
			float mouseDelta = ((mouseX - this.getX()) / (this.width - selectArea));

			if (mouseDelta > 1)
				mouseDelta = 1;

			if (mouseDelta < 0)
				mouseDelta = 0;

			this.curVal = (((maxVal - minVal) * mouseDelta));

			//if(this.curVal < minVal)
			//	this.curVal = minVal;
		} else {
			float mouseY = controller.applet.mouseY - mouseClickDeltaY;
			float mouseDelta = ((mouseY - this.getY()) / (this.width - (selectArea / 2)));

			if (mouseDelta > 1)
				mouseDelta = 1;

			if (mouseDelta < 0)
				mouseDelta = 0;

			this.curVal = (((maxVal - minVal) * mouseDelta));

			if (this.curVal < minVal)
				this.curVal = minVal;

			this.fireEventNotification(this.curVal);

		}
		//	listener.val = this.curVal;
		//	this.fireEventNotification(null, "");
		this.fireEventNotification(this.curVal);
		reRender();
		
	}
	
	@Override
	public 
	void reRender(){
		super.reRender();
		
	}

	private void clicked() {

		this.mouseClickDeltaX = controller.applet.mouseX
				- (this.getX() + this.scrollX);
		this.mouseClickDeltaY = controller.applet.mouseY
				- (this.getY() + this.scrollY);

	}

	public float getVal() {
		return curVal;
	}

	public boolean isMouseOver() {

		return isMouseOverDragPoint();
	}

	boolean isMouseOverDragPoint() {

		float mouseX = controller.applet.mouseX;
		float mouseY = controller.applet.mouseY;
		

		if (this.orientation == HORIZONTAL) {

			float scrollX = (this.width - selectArea)
					* (curVal / (maxVal - minVal));
			//scrollX += this.getX();

			
			return mouseX >= scrollX + this.getX() && mouseY >= this.getY()- (selectArea*2)
					&& mouseX <= scrollX + this.getX() + (selectArea)
					&& mouseY <= this.getY() + (selectArea);
		} else {

			float scrollY = (this.width - selectArea)
					* (curVal / (maxVal - minVal));

			//	scrollY += this.getY();

			return mouseY >= scrollY + this.getY()
					&& mouseY <= scrollY + this.getY() + (selectArea)
					&& mouseX >= getX() - (selectArea / 2)
					&& mouseX <= getX() + (selectArea / 2);

		}
	}

	@Override
	public void keyEvent(KeyEventSK theKeyEvent) {
	}

	@Override
	public void mouseEvent(MouseEventSK e) {

		//

		if (e.getAction() == MouseEventSK.PRESS) {

			if (isMouseOverDragPoint()) {			
				if (wasClicked == false)
					this.clicked();

				wasClicked = true;

			}

			//        if(isMouseOverDragPoint() && wasClicked){
			//    		//System.out.println("Moue over click");
			//
			//        	float scrollX  = width * (curVal/(maxVal-minVal));
			//        	
			//        	
			//        	scrollX += this.getX();
			//        	
			//            this.clickOffsetX = controller.parent.mouseX - scrollX;
			//           // System.out.println("clicked");
			//
			//        }

		} else if (e.getAction() == MouseEventSK.RELEASE && wasClicked) {
			fireEventNotification(this, "Clicked");
			wasClicked = false;
		}

	}

	@Override
	public void render(PGraphics g) {

		if (!this.visible)
			return;

		if (this.orientation == HORIZONTAL) {

			//fill colours
			if (this.getFillColour() != -2)
				g.fill(this.getFillColour());
			if (this.getStrokeColour() != -2)
				g.stroke(this.getStrokeColour());
			g.fill(235, 235, 235);
			g.noStroke();
			g.rect(this.getX()+(selectArea/2), this.getY(), this.width-(selectArea), selectArea);
			g.ellipse(this.getX()+(selectArea/2), this.getY()+(selectArea/2), this.selectArea, this.selectArea);
			g.ellipse(this.getX()+this.width-(selectArea/2), this.getY()+(selectArea/2), this.selectArea, this.selectArea);

			

			this.scrollX = (this.width - selectArea)
					* (curVal / (maxVal - minVal));

			//scrollX += this.getX();
			//g.ellipseMode(0);

			if (this.handle != null)
				g.image(this.handle, (int)(scrollX + this.getX()
						- (this.handle.width / 8)), (int)this.getY());
			else{
				//g.rect(scrollX + this.getX(), this.getY(), selectArea,
				//		selectArea);
				
				g.fill(60);
				g.rect(scrollX + this.getX()+(this.selectArea/2)+2, this.getY()+2,
						selectAreaLen-(this.selectArea/2)-4, selectArea-4);
				g.ellipse(scrollX + this.getX()+(this.selectArea/2)+2, this.getY()+(this.selectArea/2), this.selectArea-4, this.selectArea-4);
				g.ellipse(scrollX + this.getX()+selectAreaLen-4, this.getY()+(this.selectArea/2), this.selectArea-4, this.selectArea-4);

			}

		} else {

			g.stroke(200, 200, 200);
			g.noFill();
			g.strokeWeight(1);
			//fill colours
			if (this.getFillColour() != -2)
				g.fill(this.getFillColour());
			if (this.getStrokeColour() != -2)
				g.stroke(this.getStrokeColour());
			g.fill(235, 235, 235);
			g.noStroke();
			g.rect(this.getX() - (selectArea / 2), this.getY(), selectArea,
					this.width);
			g.ellipse(this.getX(), this.getY(), this.selectArea, this.selectArea);
			g.ellipse(this.getX(), this.getY()+this.width, this.selectArea, this.selectArea);

			this.scrollY = width * (curVal / (maxVal - minVal));
			//scrollY += this.getY();

			if (this.handle != null)
				g.image(this.handle, (int)(this.getX() - (selectArea / 2)), (int)(scrollY
						+ this.getY()));
			else{
				g.fill(60);
				g.rect((this.getX() - (selectArea / 2))+2, scrollY + this.getY()-(selectAreaLen /2.0f)+2,
						selectArea-4, selectAreaLen /2.0f-4);
				g.ellipse(this.getX(), scrollY + this.getY()-(selectAreaLen /2.0f)+2, this.selectArea-4, this.selectArea-4);
				g.ellipse(this.getX(), scrollY + this.getY()-4, this.selectArea-4, this.selectArea-4);

				
			}

		}
		renderLabel(g);
	}

	@Override
	public void renderLabel(PGraphics g) {

		if (this.orientation == HORIZONTAL) {

			if (this.label != null) {
				this.label.align = GUILabel.LEFT;
				this.label.render(g, this.getX(), this.getY() + selectArea);
			}
		} else {

			if (this.label != null) {
				this.label.align = GUILabel.LEFT;
				this.label.render(g, this.getX(), this.getY() + 22);
			}
		}

	}

	public void setMaxVal(float val) {
		this.maxVal = val;
	}

	public void setMinVal(float val) {
		this.minVal = val;
	}

	@Override
	public void setup() {
		// TODO Auto-generated method stub

	}

	public void setVal(float val) {
		this.curVal = val;
	}

	@Override
	public void update() {


		if (this.wasClicked)
			this.changeval();
	}

}
