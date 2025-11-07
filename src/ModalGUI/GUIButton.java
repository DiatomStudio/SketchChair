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
import cc.sketchchair.core.MouseEventSK;
import cc.sketchchair.sketch.LOGGER;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

public class GUIButton extends GUIComponent {

	PImage img_up = null;
	PImage img_down = null;
	PImage img_over = null;
	String img_up_txt = null;
	String img_down_txt = null;
	PImage img_clicked;

	//GUILabel label = null;

	public GUIButton(float x, float y, float w, float h) {
		// TODO Auto-generated constructor stub
	}

	public GUIButton(float x, float y, float w, float h, GUILabel labelButton,
			ModalGUI c) {
		this.setController(c);
		this.setPos(x, y);
		this.setSize(w, h);
		this.label = labelButton;
	}

	public GUIButton(float x, float y, float w, float h, String label,
			ModalGUI c) {
		this.setController(c);
		this.setPos(x, y);
		this.setSize(w, h);
		this.label = new GUILabel(this, label, this.controller);
		this.label.layout = GUILabel.CENTRE_OF_COMPONENT;

		//System.out.println("HERE"+this.label.labelStr);

	}

	public GUIButton(float x, float y, float w, float h, String img_up_path,
			String img_down_path, ModalGUI c) {
		this.setPos(x, y);
		this.setController(c);
		//this.setSize(w,h);
		//this.setLabel(label);

		if (img_up_path == null)
			return;

		PImage img_src = controller.applet.loadImage(img_up_path);

		if (w == 0 || h == 0) {
			w = img_src.width;
			h = img_src.height;
			img_up = img_src;

		} else {
			img_up = controller.makeImgUp((int) w, (int) h, img_src);

		}

		if (img_down_path != null && img_down_path != "")
			img_down = controller.applet.loadImage(img_down_path);
		else
			img_down = controller.makeImgDown(w, h, img_src);//ModalGUI.applet.loadImage(img_up_path);

		if (img_up != null)
			img_over = controller.makeImgOver((int) w, (int) h, img_src);//ModalGUI.applet.loadImage(img_up_path);

		this.setSize(this.img_up.width, this.img_up.height);
	}

	public GUIButton(float x, float y, PImage img_up_in, PImage img_down_in,
			PApplet applet, ModalGUI c) {
		this.setPos(x, y);
		this.setController(c);
		//this.setSize(w,h);
		//this.setLabel(label);
		this.img_up = img_up_in;
		
		if(img_down_in == null)
		this.img_down = controller.makeImgOver(img_up.width, img_up.height, img_up_in);
		else
		this.img_down = img_down_in;

		// img_up = applet.loadImage(img_up_path);

		//if(img_down != null)

		this.setSize(this.img_up.width, this.img_up.height);

	}

	public GUIButton(float x, float y, String img_up, ModalGUI c) {
		this(x, y, img_up, null, c);
	}

	public GUIButton(float x, float y, String img_up_path,
			String img_down_path, ModalGUI c) {
		this(x, y, 0, 0, img_up_path, img_down_path, c);
	}

	public boolean isMouseOver() {

		
		
		if (super.isMouseOver()) {

			if (!controller.useAlphaMouseOver)
				return true;

			if (this.img_down != null) {
				this.img_up.loadPixels();
				int index = (int) (((controller.applet.mouseY - this.getY()) * this.img_up.width) + (controller.applet.mouseX - this
						.getX()));
				int c = 0;

				if (index < this.img_up.width * this.img_up.height)
					c = this.img_up.pixels[index];

				if (controller.applet.alpha(c) == 255)
					return true;
				else
					return false;
			}
		} else {
			return false;
		}
		return false;
	}

	@Override
	public void keyEvent(KeyEventSK theKeyEvent) {
	}
	
	public void mouseEvent(MouseEventSK e) {

		if (!this.visible)
			return ;
		
		if (this.destroy)
			return;

		if (e.getAction() == MouseEventSK.PRESS) {
			if (isMouseOver())
				wasClicked = true;
		} else if (e.getAction() == MouseEventSK.RELEASE && wasClicked
				&& isMouseOver()) {
			fireEventNotification(this, "Clicked");
			wasClicked = false;
		}

	}

	@Override
	public void render(PGraphics g) {
		super.render(g);
		
		
		if(controller.renderOnUpdate && !reRender){
			return;
		}
		
		
		if(controller.renderOnUpdate)
		reRender = false; // only render once

		if (!this.visible)
			return;

		if (this.getFillColour() != -2)
			g.fill(this.getFillColour());
		if (this.getStrokeColour() != -2)
			g.stroke(this.getStrokeColour());

		if (this.isDown) {
			if (this.getFillColourDown() != -2)
				g.fill(this.getFillColourDown());
			if (this.getStrokeColourDown() != -2)
				g.stroke(this.getStrokeColourDown());
		}

		if (isMouseOver() && !this.isDown) {
			if (this.getFillColourOver() != -2)
				g.fill(this.getFillColourOver());
			if (this.getStrokeColourOver() != -2)
				g.stroke(this.getStrokeColourOver());
		}

		if (this.label != null && this.img_up == null) {
			g.noFill();
			if (wasClicked) {
			} else {
				if (isMouseOver()) {
					g.strokeWeight(2);
				} else {
					g.strokeWeight(1);
				}
			}

			g.rectMode(PConstants.CORNER);
			g.rect(this.getX(), this.getY(), this.getWidth(), this.getHeight());

			if (this.img_up == null) {
				//label.align = GUILabel.CENTRE ;
				//label.render(g,this.getX()+(this.getWidth()/2) , this.getY() + (this.getHeight()/2));
			}

		}

		if (wasClicked && this.img_clicked != null) {
			g.image(this.img_clicked, (int)this.getX(), (int)this.getY());

		} else {
			if (isMouseOver() && this.img_down != null) {
				g.image(this.img_down, (int)this.getX(), (int)this.getY());
				reRender();

			} else if (isMouseOver() && this.img_down == null
					&& this.img_up != null) {
				g.image(this.img_up, (int)this.getX(), (int)this.getY());

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
		
		renderLabel(g);

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

	@Override
	public void setup() {
		// TODO Auto-generated method stub

	}

}
