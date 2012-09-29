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
//#IF JAVA
package cc.sketchchair.sketch;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.Undo;
import cc.sketchchair.functions.functions;

/**
 * A  object to store global variables for the Sketch object.
 * This object is stored in the SketchChair Globals object and passed to each new Sketch object so that it is shared.
 * @author gregsaul
 *
 */
//#ENDIF JAVA

public class SketchGlobals {

	public boolean renderVolume = false;
	public boolean renderScreenshot = false;
	public float zoom = 1;
	public float physicsEngineScale = 1;

	public boolean seperate_slots = false;
	public int chairColour = 200;//functions.color(255, 255, 255);
	public Undo undo;
	public int sketch_id = 0;

	public float BEZIER_DETAIL_OFFSET = 0.1f;
	public float BEZIER_DETAIL_CALCULATIONS = 0.001f; // smaller numbers more accurate but very very slow.
	public boolean mousePressed;
	public boolean TOUCH_SCREEN_MODE = false;
	public float extendSlots = 0;
	public float slotPierceLen = 10;

}
