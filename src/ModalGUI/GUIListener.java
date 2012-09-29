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

public class GUIListener {
	Object object;
	String methodName = null;
	float val = -1;
	String stringVal;
	boolean booleanVal;

	public GUIListener(Object newListener) {
		object = newListener;
	}

	public GUIListener(Object newListener, String methodName) {
		object = newListener;
		this.methodName = methodName;
	}

	public GUIListener(Object newListener, String methodName, boolean v) {
		object = newListener;
		this.booleanVal = v;
		this.methodName = methodName;
	}

	public GUIListener(Object newListener, String methodName, int v) {
		object = newListener;
		this.val = v;
		this.methodName = methodName;
	}

	public GUIListener(Object newListener, String methodName, String v) {
		object = newListener;
		this.stringVal = v;
		this.methodName = methodName;
	}

	public String getMethodToCall() {
		if (this.methodName == null)
			return "GUIEvent";
		else
			return this.methodName;
	}

}
