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
package ToolPathWriter;

import java.io.IOException;

/**
 * Writes gcode for a CraftRobo. 
 * @author gregsaul
 *
 */
public class CraftRoboWriter extends HPGLWriter {

	public CraftRoboWriter(String location) {
		super(location);
	}

	@Override
	public void bezier(float x1, float y1, float cx1, float cy1, float cx2,
			float cy2, float x2, float y2) {

		x1 = getTranslatedX(x1);
		y1 = getTranslatedY(y1);

		cx1 = getTranslatedX(cx1);
		cy1 = getTranslatedY(cy1);

		cx2 = getTranslatedX(cx2);
		cy2 = getTranslatedY(cy2);

		x2 = getTranslatedX(x2);
		y2 = getTranslatedY(y2);

		try {
			out.write("BZ1," + Float.toString(y1) + "," + Float.toString(x1)
					+ "," + Float.toString(cy1) + "," + Float.toString(cx1)
					+ "," + Float.toString(cy2) + "," + ","
					+ Float.toString(cx2) + "," + Float.toString(y2) + ","
					+ "," + Float.toString(x2) + ",");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//BZ1,1577,522,1675,522,1755,442,1755,344,

	}

	@Override
	public void lineTo(float x, float y) {
		x = getTranslatedX(x);
		y = getTranslatedY(y);

		try {
			out.write("D" + Float.toString(y) + "," + Float.toString(x) + ",");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void move(float x, float y) {

		x = getTranslatedX(x);
		y = getTranslatedY(y);

		try {
			out.write("M" + Float.toString(y) + "," + Float.toString(x) + ",");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void nextPage() {
		// TODO Auto-generated method stub

	}

	//Set pen force from 0 - 30
	@Override
	public void setPenForce(int strength) {

		try {
			out.write("FX" + strength + ",");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setupDefault() {
		// setup default settings for the craft robo
		try {
			this.out.write("FN0,&100,100,100,^0,0,\\0,0,L0,!110,");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
