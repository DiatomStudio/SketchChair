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

public class GUIEvent {

	private GUIComponent source;
	private String message;
	public float val;
	public String stringVal = null;

	public GUIEvent(float curVal) {
		val = val;
	}

	public GUIEvent(GUIComponent argSource, String argMessage) {
		source = argSource;
		message = argMessage;
	}

	public GUIEvent(GUIComponent argSource, String argMessage, int val) {
		source = argSource;
		message = argMessage;
		val = val;
	}

	public String getMessage() {
		return message;
	}

	public GUIComponent getSource() {
		return source;
	}

	public String getString() {
		return stringVal;
	}

	public float getVal() {
		return val;
	}
}
