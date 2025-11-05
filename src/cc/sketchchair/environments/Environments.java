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
package cc.sketchchair.environments;

import java.awt.FileDialog;
import java.io.File;
import java.util.ArrayList;

import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;
import javax.jnlp.ServiceManager;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.Localization;
import cc.sketchchair.core.SETTINGS;
import cc.sketchchair.core.UITools;
import cc.sketchchair.sketch.SketchTools;

import ModalGUI.GUIButton;
import ModalGUI.GUIEvent;
import ModalGUI.GUIPanel;
import ModalGUI.ModalGUI;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import toxi.geom.Vec2D;

/**
 * Container class for Environments. 
 * @author gregsaul
 *
 */
public class Environments {
	public ArrayList<Environment> l = new ArrayList<Environment>();

	public boolean render = true;

	public GUIPanel panel;

	public Environments() {

	}

	public boolean containsEnvironment(String fileName) {
		for (Environment evenvironment : l) {
			if (evenvironment.fileName == fileName)
				return true;
		}
		return false;

	}

	public Environment get(String fileName) {
		for (Environment evenvironment : l) {
			if (evenvironment.fileName == fileName)
				return evenvironment;
		}
		return null;

	}

	public void loadEnvironment(GUIEvent e) {

		if (!containsEnvironment(e.stringVal)) {
			Environment environment = new Environment(e.stringVal,
					GLOBAL.applet);
			this.l.add(environment);
		} else {
			Environment environment = get(e.stringVal);
			environment.destroy();
		}

		//	System.out.println(e.stringVal);
	}

	void loadFolder(String folder, Object loadObj, ModalGUI gui,
			GUIPanel panel, PApplet applet) {
		File fp = new File(folder);

		if (!fp.isDirectory())
			return;
		GUIButton button = null;

		String dirContence[] = fp.list();

		for (int i = 0; i < dirContence.length; i++) {

			String path = folder + "\\" + dirContence[i];

			int dot = path.lastIndexOf(".");
			String ext = path.substring(dot + 1);

			if (ext.equals("jpg") || ext.equals("png")) {

				PImage image = applet.loadImage(path);
				PImage imageCpy = applet.createImage(40, 40, 2);

				if (image.width > image.height)
					imageCpy.copy(image, 0, 0, image.height, image.height, 0,
							0, 40, 40);
				else
					imageCpy.copy(image, 0, 0, image.width, image.width, 0, 0,
							40, 40);

				//image.resize(40, 40);

				button = new GUIButton(0, 0, imageCpy, null, GLOBAL.applet, gui);
				button.addActionListener(GLOBAL.environments,
						"loadEnvironment", path);

				//gui.add(button);
				panel.placeComponent(button);
			}
		}

	}

	public void mouseDragged(float mouseX, float mouseY) {

	}

	public void mousePressed(float mouseX, float mouseY) {
		if (render) {

			if ((GLOBAL.uiTools.getCurrentTool() == UITools.MOVE_OBJECT || GLOBAL.uiTools.SketchTools
					.getCurrentTool() == SketchTools.SELECT_TOOL
					&& !GLOBAL.uiTools.keyPressed)) {
				for (Environment evenvironment : l) {
					if (evenvironment.visible
							&& evenvironment.isOver(new Vec2D(mouseX, mouseY)))
						evenvironment.beingDragged = true;
				}

			}

			if ((GLOBAL.uiTools.getCurrentTool() == UITools.SCALE_TOOL)) {
				for (Environment evenvironment : l) {
					if (evenvironment.visible
							&& evenvironment.isOver(new Vec2D(mouseX, mouseY)))
						evenvironment.beingScaled = true;
				}

			}

			if ((GLOBAL.uiTools.SketchTools.getCurrentTool() == SketchTools.SELECT_TOOL && GLOBAL.uiTools.keyPressed)) {
				for (Environment evenvironment : l) {
					if (evenvironment.visible
							&& evenvironment.isOver(new Vec2D(mouseX, mouseY)))
						evenvironment.beingPushed = true;
				}

			}

			if (((GLOBAL.uiTools.getCurrentTool() == UITools.MOVE_OBJECT || GLOBAL.uiTools.SketchTools
					.getCurrentTool() == SketchTools.SELECT_TOOL) && GLOBAL.uiTools.mouseButton == UITools.MOUSE_RIGHT)) {

				for (Environment evenvironment : l) {
					if (evenvironment.visible
							&& evenvironment.isOver(new Vec2D(mouseX, mouseY))) {
						evenvironment.wakeUp();
						break;
					}
				}

			}

		}
	}

	public void mouseReleased(float mouseX, float mouseY) {

		for (Environment evenvironment : l) {
			evenvironment.beingDragged = false;
			evenvironment.beingScaled = false;
			evenvironment.beingPushed = false;

		}

	}

	public void openEnvironmentFromFile(GUIEvent e) {
		LOGGER.info("Preparing to open SketchChair file.");

		// Processing 4: Get Frame from PSurface
		java.awt.Frame parentFrame = null;
		if (GLOBAL.surface != null) {
			Object nativeWindow = GLOBAL.surface.getNative();
			if (nativeWindow instanceof java.awt.Frame) {
				parentFrame = (java.awt.Frame) nativeWindow;
			}
		}
		FileDialog fd = new FileDialog(parentFrame, "open",
				FileDialog.LOAD);
		fd.setFile("chair" + SETTINGS.chairSaveNum + ".png");
		String currentDir = new File(".").getAbsolutePath();
		fd.setDirectory(currentDir + "\\savedChairs\\");
		/*
		fd.setFilenameFilter(new FilenameFilter(){
			public boolean accept(File directory, String filename)
			{
			return (filename.endsWith("*.cha"));
			}
			});
		*/
		fd.setLocation(50, 50);
		fd.pack();

		fd.show();

		//System.out.println(fd.getDirectory() +fd.getFile());
		if (fd.getName() != null) {
			String filename = fd.getFile();

			LOGGER.info("Loading: " + fd.getDirectory() + filename);

			Environment environment = new Environment(fd.getDirectory()
					+ filename, GLOBAL.applet);
			this.l.add(environment);

		} else {
			// println("not an stl file");
		}

	}

	public void render(PGraphics g) {

		update();

		//if(render){
		for (Environment evenvironment : l) {

			evenvironment.render(g);

		}
		//}
	}

	public void setupGUI(ModalGUI gui, PApplet applet) {

		this.panel = new GUIPanel(GLOBAL.windowWidth - 240, 220, 220f, 110f,
				true, applet, gui);
		gui.add(this.panel);
		this.panel.setLabel(Localization.getString("environments"));

		/*
		buttonAddPlane = new GUI.GUIButton(panelX, 15, "proButtons/GUI_PLANE_ADD_UP.png",
					"GUI_PLANE_ADD_DOWN.png", GLOBAL.applet);
		buttonAddPlane.addActionListener(GLOBAL.uiTools, "addPlane",
					UITools.LINE_TOOL);
		buttonAddPlane.addToolTip(GLOBAL.applet,"GUI_PLANE_ADD_TOOLTIP", LANGUAGE);

			gui.add(buttonAddPlane);
			
			if(SETTINGS.WEB_MODE)
			return;
		*/
		if (SETTINGS.WEB_MODE)
			return;
		//String currentDir = new File(".").getAbsolutePath();
		//currentDir =  currentDir+  "\\environments\\";

		//this.loadFolder(currentDir, this, gui, this.panel, applet);

	}

	void update() {

		for (int i = 0; i < this.l.size(); i++) {
			Environment evenvironment = this.l.get(i);
			if (evenvironment.destroy)
				this.l.remove(i);
			else
				evenvironment.update();
		}
	}
}
