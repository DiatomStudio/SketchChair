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

import java.util.ArrayList;
import java.util.List;

import processing.core.PGraphics;

public class GUIButtons {

	List l = new ArrayList();

	public void add(GUIButton button) {
		this.l.add(button);
	}

	void render(PGraphics g) {
		for (int i = 0; i < this.l.size(); i++) {
			GUIButton button = (GUIButton) this.l.get(i);
			button.render(g);
		}
	}

	void update() {

	}
}
