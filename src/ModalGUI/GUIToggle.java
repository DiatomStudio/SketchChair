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

import java.lang.reflect.Field;

import cc.sketchchair.core.LOGGER;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import cc.sketchchair.core.MouseEventSK;

public class GUIToggle extends GUIButton {

	PImage img_clicked;

	boolean linkedVal;
	public boolean justMade = true;
	private GUIComponentSet componentSet = null;

	public boolean isTab = false;

	public float cornerRad = 0;

	public GUIToggle(float x, float y, float w, float h) {
		super(x, y, w, h);
		this.setPos(x, y);
		this.setSize(w, h);
	}

	public GUIToggle(float x, float y, float w, float h,
			GUIComponentSet componentSet, ModalGUI c) {
		super(x, y, w, h, "", c);
		this.setComponentSet(componentSet);
		componentSet.add(this);
		this.setPos(x, y);
		this.setSize(w, h);
	}

	public GUIToggle(float x, float y, float w, float h, String label,
			GUIComponentSet componentSet, ModalGUI c) {
		super(x, y, w, h, "", c);
		this.componentSet = componentSet;
		componentSet.add(this);
		this.setPos(x, y);
		this.setSize(w, h);
		this.label = new GUILabel(this, label, c);

	}

	public GUIToggle(float x, float y, float w, float h, String img_up,
			ModalGUI c) {
		this(x, y, w, h, img_up, "", c);
	}

	public GUIToggle(float x, float y, float w, float h, String img_up_path,
			String img_down_path, ModalGUI c) {
		super(x, y, w, h, img_up_path, img_down_path, c);
		//this.setPos(x,y);
		//this.setSize(w,h);
		//this.setLabel(label);
		this.setController(c);

		PImage img_src = controller.applet.loadImage(img_up_path);

		img_up = controller.makeImgUp((int) w, (int) h, img_src);

		if (img_down_path != null && img_down_path != "")
			img_down = controller.makeImgDown((int) w, (int) h, controller.applet.loadImage(img_down_path));
		else
			img_down = controller.makeImgDown((int) w, (int) h, img_src);//ModalGUI.applet.loadImage(img_up_path);

		if (img_up != null)
			img_over = controller.makeImgOver((int) w, (int) h, img_src);//ModalGUI.applet.loadImage(img_up_path);

		this.setSize(this.img_up.width, this.img_up.height);

	}

	public GUIToggle(float x, float y, String img_up, ModalGUI c) {
		this(x, y, img_up, null, c);
	}

	public GUIToggle(float x, float y, String img_up_path,
			String img_down_path, ModalGUI c) {
		super(y, y, null, null, c);
		this.setController(c);
		this.setPos(x, y);
		//this.setSize(w,h);
		//this.setLabel(label);

		img_up = controller.applet.loadImage(img_up_path);

		if (img_down_path != null)
			img_down = controller.applet.loadImage(img_down_path);
		else
			img_down = controller.makeImgDown(img_up);//ModalGUI.applet.loadImage(img_up_path);

		if (img_up != null)
			img_over = controller.makeImgOver(img_up);//ModalGUI.applet.loadImage(img_up_path);

		this.setSize(this.img_up.width, this.img_up.height);

	}

	public void addLinkedVal(boolean link) {
		this.linkedVal = link;
		this.isDown = this.linkedVal;
	}

	void checkLinkedVal() {

		for (int i = 0; i < listeners.size(); i++) {
			GUIListener listener = listeners.get(i);

			if (listener == null || listener.methodName == "")
				return;
			Field field;
			try {

				field = listener.object.getClass()
						.getField(listener.methodName);
				Class classType = field.getType();

				if (classType.toString().equals("boolean"))
					this.isDown = field.getBoolean(listener.object);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}

	}

	@Override
	public void mouseEvent(MouseEventSK e) {
		//super.mouseEvent(e);

		if (!this.visible)
			return ;
		
		if(isMouseOver()){
			mouseOver = true;
			reRender();
		}else if(mouseOver){
			reRender();
			mouseOver = false;
		}
			
		
		
		if (this.destroy)
			return;

		if (justMade) {
			justMade = false;
			return;
		}
		//
		if (e.getAction() == MouseEventSK.PRESS) {

			if (isMouseOver() && !wasClicked) {
				wasClicked = true;

			}
		} else if (e.getAction() == MouseEventSK.RELEASE && wasClicked
				&& isMouseOver()) {

			fireEventNotification(this, "Clicked");
			wasClicked = false;
			this.toggle();

		}

	}

	@Override
	public void render(PGraphics g) {
		
		
		
		//Render on update optimization
		if(controller.renderOnUpdate && !reRender){return;}
		if(controller.renderOnUpdate)
		reRender = false; // only render once

		
		
		if (!this.visible)
			return;

		if (this.destroy)
			return;
		

		checkLinkedVal();

		g.strokeWeight(1);
		g.noFill();
		float sw = g.strokeWeight;

		//setup fill colours

		if (this.getFillColour() != -2)
			g.fill(this.getFillColour());
		if (this.getStrokeColour() != -2)
			g.stroke(this.getStrokeColour());

		if (this.isDown) {
			g.strokeWeight(2);

			if (this.getFillColourDown() != -2)
				g.fill(this.getFillColourDown());
			if (this.getStrokeColourDown() != -2)
				g.stroke(this.getStrokeColourDown());
		}

		if (isMouseOver() && !this.isDown) {
			g.strokeWeight(2);

			if (this.getFillColourOver() != -2)
				g.fill(this.getFillColourOver());
			if (this.getStrokeColourOver() != -2)
				g.stroke(this.getStrokeColourOver());
		}

		
		
		//no images set
		if (this.img_clicked == null && this.img_down == null || isTab) {
			
			if (isTab && this.isDown) {
				
				g.stroke(this.getStrokeColour());
				g.strokeWeight(1);
				
				g.rect(this.getX(), this.getY(), this.getWidth(),
						this.getHeight() + 3,cornerRad,cornerRad,0,0);
	
				g.noStroke();
				g.rect(this.getX(), this.getY()+this.getHeight()-2, this.getWidth(),
						8);
				
				if(this.img_down != null)
					g.image(this.img_down ,this.getX()+this.getWidth()-img_up.width-5,this.getY()+3);
						
			} else{
				g.rect(this.getX(), this.getY(), this.getWidth(),
						this.getHeight(),cornerRad,cornerRad,0,0);
				
				if(this.img_up != null)
					g.image(this.img_up ,this.getX()+this.getWidth()-img_up.width-5,this.getY()+3);
				
			}
			//	return ;
		}

		if(!isTab){
		//  boolean hasFocus = controller.getFocusStatusForComponent(this);
		if (wasClicked && this.img_clicked != null) {
			g.image(this.img_clicked, (int)this.getX(), (int)this.getY());
		} else {
			if (isMouseOver() && this.img_over != null && !this.isDown) {
					g.image(this.img_over, (int)this.getX(), (int)this.getY());

			} else {

				if (this.isDown) {
					if (this.img_down != null)
						g.image(this.img_down, (int)this.getX(), (int)this.getY());
					else
						g.rect(this.getX(), this.getY(), this.getWidth(),
								this.getHeight());
				} else {
					if (this.img_up != null)
						g.image(this.img_up, (int)this.getX(), (int)this.getY());
					else
						g.rect(this.getX(), this.getY(), this.getWidth(),
								this.getHeight());

				}

			}
		}
		}
		
		renderLabel(g);
		g.noStroke();
	}

	@Override
	public void renderTop(PGraphics g) {
		if (isMouseOver() && this.toolTip != null) {
			this.renderToolTip(g, controller.applet.mouseX,
					controller.applet.mouseY);
			if (toolTipAlpha < 255)
				this.toolTipAlpha += 20;
		} else {
			this.toolTipAlpha = -100;
		}
		
	
	}

	public void setComponentSet(GUIComponentSet componentSet) {
		this.componentSet = componentSet;
		componentSet.add(this);

	}

	public void setState(boolean newState) {
		this.isDown = newState;
	}

	void toggle() {
		if (this.componentSet != null)
			this.componentSet.setAllUp();

		this.isDown = !this.isDown;

		this.fireEventNotification(this.isDown);
		this.fireEventNotification("");

	}
	
	public void toggleDown() {
		if (this.componentSet != null)
			this.componentSet.setAllUp();

		this.isDown = true;

		this.fireEventNotification(this.isDown);
		this.fireEventNotification("");

	}

}
