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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Writes standard gcode.
 * @author gregsaul
 *
 */
public class HPGLWriter {

	BufferedWriter out;//= new BufferedWriter(fstream);

	float translateX = 0;
	float translateY = 0;
	float translateZ = 0;
	float scale = 1f;

	float ptranslateX = 0;
	float ptranslateY = 0;
	float ptranslateZ = 0;
	float pscale = 1f;

	HPGLWriter(String location) {
		try {
			this.out = new BufferedWriter(new FileWriter(location));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void bezier(float x1, float y1, float cx1, float cy1, float cx2,
			float cy2, float x2, float y2) {
		try {
			out.write("BZ1," + Float.toString(getTranslatedX(x1)) + ","
					+ Float.toString(getTranslatedY(y1)) + ","
					+ Float.toString(getTranslatedX(cx1)) + ","
					+ Float.toString(getTranslatedY(cy1)) + ","
					+ Float.toString(getTranslatedX(cx2)) + "," + ","
					+ Float.toString(getTranslatedY(cy2)) + ","
					+ Float.toString(getTranslatedX(x2)) + "," + ","
					+ Float.toString(getTranslatedY(y2)) + ",");
		} catch (IOException e) {
			e.printStackTrace();
		}

		//BZ1,1577,522,1675,522,1755,442,1755,344,

	}

	public void close() {
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	float getTranslatedX(float xIn) {
		return (xIn += this.translateX) * scale;
	}

	float getTranslatedY(float yIn) {
		return (yIn += this.translateY) * scale;
	}

	float getTranslatedZ(float zIn) {
		return (zIn += this.translateZ) * scale;
	}

	public void lineTo(float x, float y) {
		try {
			out.write("D" + Float.toString(getTranslatedX(x)) + ","
					+ Float.toString(getTranslatedY(y)) + ",");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void move(float x, float y) {
		try {
			out.write("M" + Float.toString(getTranslatedX(x)) + ","
					+ Float.toString(getTranslatedY(y)) + ",");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void nextPage() {
		// TODO Auto-generated method stub

	}

	public void popMatrix() {
		translateX = ptranslateX;
		translateY = ptranslateY;
		translateZ = ptranslateZ;
		scale = pscale;
	}

	public void pushMatrix() {
		ptranslateX = translateX;
		ptranslateY = translateY;
		ptranslateZ = translateZ;
		pscale = scale;
	}

	public void resetMatrix() {
		this.translateX = 0;
		this.translateY = 0;
		this.translateZ = 0;
	}

	public void scale(float s) {
		this.scale = s;
	}

	//Set pen force from 0 - 30
	void setPenForce(int strength) {
		try {
			out.write("FC0," + strength + ",");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void setupDefault() {
		// setup default settings for the craft robo
		try {
			this.out.write("FN0,&100,100,100,^0,0,\0,0,L0,!110,");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void translate(float deltaX, float deltaY) {
		this.translateX += deltaX;
		this.translateY += deltaY;
	}

	void translate(float deltaX, float deltaY, float deltaZ) {
		this.translateX += deltaX;
		this.translateY += deltaY;
		this.translateZ += deltaZ;
	}

}
