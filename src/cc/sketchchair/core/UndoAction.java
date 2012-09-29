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
package cc.sketchchair.core;

/**
 * Actions to undo, No longer used. 
 * @author gregsaul
 *
 */
public class UndoAction {
	public static final int ADD_PLANE = 2;
	public static final int ADD_CHAIR = 3;
	public static final int EDIT_SHAPE = 4;
	Object obj = null;
	Object objClone = null;
	int action = -1;

	public static int ADD_SHAPE = 1;

	public UndoAction(Object obj, int action) {
		this.obj = obj;
		this.action = action;

	}

	public UndoAction(Object obj, Object clone, int action) {
		this.obj = obj;
		this.action = action;
		this.objClone = clone;

	}

}
