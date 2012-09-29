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

public class function {

	public final static int color(int x, int y, int z) { // ignore
		return color(x, y, z, 255);
	}

	public final static int color(int x, int y, int z, int a) { // ignore

		if (a > 255)
			a = 255;
		else if (a < 0)
			a = 0;
		if (x > 255)
			x = 255;
		else if (x < 0)
			x = 0;
		if (y > 255)
			y = 255;
		else if (y < 0)
			y = 0;
		if (z > 255)
			z = 255;
		else if (z < 0)
			z = 0;

		return (a << 24) | (x << 16) | (y << 8) | z;

	}

}
