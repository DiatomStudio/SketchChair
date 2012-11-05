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
//#IF JAVA
package cc.sketchchair.sketch;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.UITools;

import ModalGUI.GUIComponentSet;
import ModalGUI.GUILabel;
import ModalGUI.GUIPanel;
import ModalGUI.GUIPanelTabbed;
import ModalGUI.GUIToggle;
import ModalGUI.ModalGUI;

import processing.core.PApplet;


/**
 * Main program file to start Sketch in standalone mode, this is good for debugging and can be used to test compatibility with javascript without relying on jBullet. 
 * @author gregsaul
 *
 */
public class main extends PApplet {
	public static void main(String args[]) {
		PApplet.main(new String[] { main.class.getName() });
	}

	/**
	 * 
	 */

	//#ENDIF JAVA

	public ModalGUI gui;
	Sketch sketch = new Sketch(this);

	boolean mouseDown = false;
	boolean mouseDoubleClick = false;
	//SETTINGS_SKETCH SETTINGS_SKETCH = new SETTINGS_SKETCH();

	public void draw() {

			sketch.getSketchTools().keyPressed = keyPressed;

		//MOUSE STUFF
		sketch.getSketchTools().updateMouse(mouseX, mouseY, pmouseX, pmouseY, mouseDown, mouseButton);
		//GLOBAL.uiTools.updateMouseWorld();

		background(200, 200, 200);
		sketch.getSketchTools().render(g);

		gui.update();
		gui.render(g);
		g.fill(0);
		g.text((int) frameRate, 0, 10);

		
		
		scale(sketch.getSketchGlobals().zoom);

		sketch.update();
		sketch.render(g);
		//sketch.renderOutline(g);

		sketch.getSketchTools().render(g);

		if (mousePressed && (mouseX != pmouseX || mouseY != pmouseY)
				&& !gui.overComponent() && !gui.components.isMouseOver())
			sketch.mouseDragged(mouseX, mouseY);

		if (!mousePressed && mouseDown && !gui.components.isMouseOver() && !mouseDoubleClick) {
			sketch.mouseReleased(mouseX, mouseY);
			sketch.buildOutline();	
		}

		if (mousePressed && !mouseDown && !gui.components.isMouseOver() )
			sketch.mousePressed(mouseX, mouseY);

		
		
		if (!mousePressed && mouseDown && !gui.components.isMouseOver() && mouseDoubleClick)
			mouseDoubleClick = false;
		
		
		if (mousePressed)
			mouseDown = true;
		else
			mouseDown = false;
		
	
		
	}

	public void setup() {
		size(600, 400, P3D);
		sketch.sketchTools = new SketchTools(this);
		sketch.sketchTools.build(this);
		
		sketch.setRenderMode(Sketch.RENDER_3D_EDITING_PLANES);
		sketch.select();
		SETTINGS_SKETCH.SLICEPLACE_RENDER_VOLUME = false;

		gui = new ModalGUI();
		gui.renderOnUpdate = false;
		gui.setup(this);
		gui.myFontMedium = loadFont("TrebuchetMS-12.vlw");
		setupGUI(gui);
	//	smooth(8);
		ortho();
	    textFont(gui.myFontMedium);
	    frameRate(200);

		hint(PApplet.DISABLE_STROKE_PERSPECTIVE);
		addMouseWheelListener(new MouseWheelListener() {
		
			public void mouseWheelMoved(MouseWheelEvent e) {
				int notches = e.getWheelRotation();
				if (notches < 0) {
					sketch.getSketchGlobals().zoom -= (notches / 10f);
				} else {
					sketch.getSketchGlobals().zoom -= (notches / 10f);
			}
			}
		});

	}

	public void setupGUI(ModalGUI gui) {

		float button_width = 45;
		float button_height = 45;

		float posY = 10;
		float posX = 10;
		float panelHeight = 120;

		GUIPanelTabbed tabbedPanel = new GUIPanelTabbed(0f, height
				- panelHeight, (int) width, (int) panelHeight, gui);
		gui.add(tabbedPanel);

		//tools
		GUIPanel toolPanel = new GUIPanel(0f, height - panelHeight,
				(int) width, (int) panelHeight, gui);
		tabbedPanel.addTabbedPanel(toolPanel, "tools", gui);

		GUIComponentSet toggleSet = new GUIComponentSet();

		GUIToggle toggle = new GUIToggle(posX, posY, button_width,
				button_height, "gui/draw.png", gui);
		toggle.setComponentSet(toggleSet);
		toggle.addActionListener(sketch.getSketchTools(), "selectTool",
				SketchTools.DRAW_TOOL);
		toggle.setLabel("brush");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/add_leg.png", gui);
		toggle.setComponentSet(toggleSet);
		toggle.addActionListener(sketch.getSketchTools(), "selectTool",
				SketchTools.LEG_TOOL);
		toggle.setLabel("leg");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/draw_path.png", gui);
		toggle.setComponentSet(toggleSet);
		toggle.addActionListener(sketch.getSketchTools(), "selectTool",
				SketchTools.DRAW_PATH_TOOL);
		toggle.setLabel("path");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/select.png", gui);
		toggle.setComponentSet(toggleSet);
		toggle.addActionListener(sketch.getSketchTools(), "selectTool",
				SketchTools.SELECT_TOOL);
		toggle.setLabel("select");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/bezier.png", gui);
		toggle.setComponentSet(toggleSet);
		toggle.addActionListener(sketch.getSketchTools(), "selectTool",
				SketchTools.SELECT_BEZIER_TOOL);
		toggle.setLabel("bezier");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/draw_path.png", gui);
		toggle.setComponentSet(toggleSet);
		toggle.addActionListener(sketch.getSketchTools(), "selectTool",
				SketchTools.DRAW_OFFSETPATH_TOOL);
		toggle.setLabel("offset path");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		toolPanel.placeComponent(toggle);

	}

	
	public void mousePressed(MouseEvent e) {
		 if (e.getClickCount()==1) {
		 }  
		 else if (e.getClickCount()==2) {
			 mouseDoubleClick = true;
			 sketch.mouseDoubleClick(mouseX, mouseY);
		 }
		 
			super.mousePressed(e);

		}
	
	/*
	public void keyPressed() {
		sketch.getSketchTools().keyCode = keyCode;
		sketch.getSketchTools().key = key;
if(key == 'x')
	sketch.deleteAll();
	}
	*/
	
	//#IF JAVA
}
//#ENDIF JAVA
