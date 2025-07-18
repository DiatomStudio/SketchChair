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


import cc.sketchchair.core.MouseEventSK;
import processing.core.PGraphics;
import processing.core.PImage;

public class GUIColourPicker extends GUIComponent {
	PImage img;
	int selectedColour = -1;
	int overColour = -1;

	public GUIColourPicker(float x, float y, ModalGUI c) {
		this.setController(c);
		this.setPos(x, y);
		this.img = controller.applet.loadImage("colorpallet.png");
		this.setSize(img.width, img.height);

	}

	public int getColourOver() {
		int mouseX = 0;
		if (controller != null)
			mouseX = controller.applet.mouseX;

		int mouseY = 0;
		if (controller != null)
			mouseY = controller.applet.mouseY;

		int c = img.get(mouseX - (int) this.getX(), mouseY - (int) this.getY());

		return c;

	}

	@Override
	public void mouseEvent(MouseEventSK e) {

		if (this.destroy)
			return;

		if (e.getAction() == MouseEventSK.PRESS) {
			if (isMouseOver()) {
				int c = this.getColourOver();

				if (c != 0) {
					for (int i = 0; i < listeners.size(); i++) {
						GUIListener listener = listeners.get(i);
						selectedColour = c;
						listener.val = this.selectedColour;
						fireEventNotification(this, "");
					}
				}
			}
		} else if (e.getAction() == MouseEventSK.RELEASE && wasClicked
				&& isMouseOver()) {
			fireEventNotification(this, "Clicked");
			wasClicked = false;
		}

	}

	@Override
	public void render(PGraphics g) {

		if (!this.visible)
			return;

		int c = this.getColourOver();

		if (c != 0)
			overColour = c;
		else
			overColour = -1;

		this.update();

		g.image(this.img, (int)this.getX(), (int)this.getY());
		g.strokeWeight(1);
		g.stroke(0);

		if (overColour != -1)
			g.fill(overColour);
		else
			g.fill(selectedColour);

		controller.roundrect(g, this.getX() - 20, this.getY() + 2, 18, 12, 4);
		renderLabel(g);

	}

	@Override
	public void setup() {
		// TODO Auto-generated method stub

	}

}
