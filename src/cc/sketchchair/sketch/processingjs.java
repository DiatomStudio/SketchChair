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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.UITools;

import ModalGUI.GUIComponentSet;
import ModalGUI.GUILabel;
import ModalGUI.GUIPanel;
import ModalGUI.GUIPanelTabbed;
import ModalGUI.GUIToggle;
import ModalGUI.ModalGUI;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * main program file for starting Sketch in processing js, the javascript version of processing. 
 * @author gregsaul
 *
 */
public class processingjs extends PApplet {
//#ENDIF JAVA

	//SETTINGS_SKETCH SETTINGS_SKETCH;
	Sketch sketch;
	PGraphics g = createGraphics(600, 600,P2D);
	List<Sketch> l = new ArrayList<Sketch>();

	boolean mouseDown = false;
	Test test = new Test();
	public void draw() {

		//MOUSE STUFF
		sketch.getSketchTools().updateMouse(mouseX,mouseY,pmouseX,pmouseY,mouseDown,mouseButton);
		
		//#IF JAVA
		///GLOBAL.uiTools.updateMouseWorld();
		//#ENDIF JAVA
		
		background(255,0,0);
		

		sketch.update();
		//sketch.render(g);
		
		//sketch.getSketchTools().render(this.g);

		if (mousePressed && (mouseX != pmouseX || mouseY != pmouseY))
			sketch.mouseDragged(mouseX, mouseY);

		if (!mousePressed && mouseDown)
			sketch.mouseReleased(mouseX, mouseY);

		if (mousePressed && !mouseDown)
			sketch.mousePressed(mouseX, mouseY);

		if (mousePressed)
			mouseDown = true;
		else
			mouseDown = false;
		/*
*/
		//test.hello(g,mouseX,mouseY);
		image(g,0,0);
	}

	public void setup() {
		size(600, 600, OPENGL);
		sketch = new Sketch(this);
		sketch.sketchTools = new SketchTools(this);
		// SETTINGS_SKETCH = new SETTINGS_SKETCH();
	}

//#IF JAVA	
}
//#ENDIF JAVA
