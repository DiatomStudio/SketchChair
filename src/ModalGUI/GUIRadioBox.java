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

import java.awt.event.MouseEvent;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

public class GUIRadioBox extends GUIComponent {
	GUIComponentSet set = null;

	

	public GUIRadioBox(float x, float y, float w, GUIComponentSet set,
			ModalGUI c) {
		this.setController(c);
		this.setPos(x, y);
		this.setSize(w, w);
		this.set = set;
		this.set.add(this);
	}

	public GUIRadioBox(float x, float y, float w, ModalGUI c) {
		this.setController(c);
		this.setPos(x, y);
		this.setSize(w, w);
	}

	public void mouseEvent(MouseEvent e) {

		if (this.destroy)
			return;

		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			if (isMouseOver()) {
				if (set != null)
					set.setAllUp();

				isDown = true;
				wasClicked = true;

			}
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED && wasClicked
				&& isMouseOver()) {
			fireEventNotification(this, "Clicked");
			wasClicked = false;
		}

	}

	public void render(PGraphics g) {

		if (!this.visible)
			return;

		g.stroke(0);
		g.strokeWeight(1);
		g.noFill();
		g.ellipseMode(PConstants.CORNER);
		g.ellipse(this.getX(), this.getY(), this.getWidth(), this.getWidth());

		if (this.isDown) {
			g.fill(0);
			g.ellipse(this.getX() + 2, this.getY() + 2, this.getWidth() - 4,
					this.getWidth() - 4);
		}
		g.ellipseMode(PConstants.CENTER);

		renderLabel(g);

	}

	public void setComponentSet(GUIComponentSet toggleSet) {
		this.set = toggleSet;
		this.set.add(this);
	}

	public void setState(boolean newState) {
		this.isDown = newState;

	}

	public void setup() {
		// TODO Auto-generated method stub

	}

}
