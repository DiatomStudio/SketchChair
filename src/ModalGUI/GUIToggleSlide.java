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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class GUIToggleSlide extends GUIToggle {

	GUILabel labelLeft;
	GUILabel labelRight;

	public GUIToggleSlide(float x, float y, String leftLabel,
			String rightLabel, ModalGUI c) {
		super(x, y, "toggleSlideRight.png", "toggleSlideLeft.png", c);
		this.setController(c);

		labelLeft = new GUILabel(this, leftLabel, this.controller);
		labelLeft.align = GUILabel.RIGHT;
		labelLeft.layout = GUILabel.LEFT_OF_COMPONENT;

		labelRight = new GUILabel(this, rightLabel, this.controller);
		labelRight.align = GUILabel.RIGHT;
		labelRight.layout = GUILabel.RIGHT_OF_COMPONENT;

		//this.setSize(labelRight.getWidth() + labelLeft.getWidth()+ 100 , 20);
	}

	@Override
	public void render(PGraphics g) {

		if (!this.visible)
			return;

		if (this.destroy)
			return;

		checkLinkedVal();

		if (this.isDown) {
			if (this.img_down != null)
				g.image(this.img_down, (int)this.getX(), (int)this.getY());
		} else {
			if (this.img_up != null)
				g.image(this.img_up, (int)this.getX(), (int)this.getY());

		}

		this.labelLeft.render(g);
		this.labelRight.render(g);

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
		//renderLabel(g);
	}

	public void setState(boolean newState) {
		this.isDown = newState;
	}

	@Override
	public void setup() {
		// TODO Auto-generated method stub
		if (this.labelLeft != null)
			this.labelLeft.controller = this.controller;

		if (this.labelRight != null)
			this.labelRight.controller = this.controller;

	}

}
