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
package cc.sketchchair.widgets;

import java.io.File;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.SETTINGS;
import cc.sketchchair.core.UITools;

import ModalGUI.GUIButton;
import ModalGUI.GUIEvent;
import ModalGUI.GUIImage;
import ModalGUI.GUIPanel;
import ModalGUI.GUIWindow;
import ModalGUI.ModalGUI;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * GUI widget used to display a window for loading designs from a folder. 
 * @author gregsaul
 *
 */
public class WidgetLoad {
	public GUIPanel panel;
	GUIPanel scrollPanel;
	float YPos = 0;
	private String LANGUAGE;
	GUIWindow window = null;
	
	public WidgetLoad() {

	}

	void hide() {
		this.panel.hide();
		this.scrollPanel.hide();
	}

	public void loadChair(GUIEvent e) {
		GLOBAL.sketchChairs.killAll();
		String chairLocation = e.stringVal.substring(0,
				e.stringVal.length() - 4);
		UITools.load(chairLocation);

		this.hide();

	}

	void loadFolder(String folder, Object loadObj, ModalGUI gui,
			GUIPanel panel, PApplet applet) {
		
		int thumbSize = 150;

		//System.out.println(folder);
		File fp = new File(folder);

		if (!fp.isDirectory())
			return;
		GUIButton button = null;

		String dirContence[] = fp.list();

		for (int i = 0; i < dirContence.length; i++) {

			String path = folder + "/" + dirContence[i];

			int dot = path.lastIndexOf(".");
			String ext = path.substring(dot + 1);

			if (ext.equals("jpg") || ext.equals("png")) {

				PImage image = applet.loadImage(path);
				PImage imageCpy = applet.createImage(thumbSize, thumbSize, 2);

				if (image.width > image.height)
					imageCpy.copy(image, 0, 0, image.height, image.height, 0,
							0, thumbSize, thumbSize);
				else
					imageCpy.copy(image, 0, 0, image.width, image.width, 0, 0,
							thumbSize, thumbSize);

				//image.resize(40, 40);

				button = new GUIButton(0, 0, imageCpy, null, GLOBAL.applet, gui);
				button.addActionListener(window, "close", path);
				button.addActionListener(this, "loadChair", path);

				//gui.add(button);
				this.scrollPanel.placeComponent(button);
			}
		}

	}

	public void setupGUI(PApplet applet, ModalGUI gui) {

		LANGUAGE = "ENG";

		float panelX = 200;
		//this.panel = new GUIPanel(10, 10, GLOBAL.windowWidth - 20,
		//		GLOBAL.windowHeight - 20, gui);
		//this.panel.setLabel("load");
		//gui.add(this.panel);
		
		
		float windowWidth = SETTINGS.GUIDE_WINDOW_WIDTH;
		float windowHeight = SETTINGS.GUIDE_WINDOW_HEIGHT;
		float borderTop = 100;
		
		window = new GUIWindow(0f,0f,windowWidth,windowHeight,gui);
		window.centre();
		window.setLightboxed(true);
		gui.add(window);


		GUIImage guiImg = new GUIImage(0,0,"gui/GUI_WINDOW_LIBRARY_TITLE.png",gui);
		window.add(guiImg);

		this.scrollPanel = new GUIPanel(0, borderTop, window.getWidth(),
				window.getHeight() - (borderTop), true, applet, gui);
		this.scrollPanel.setParentPanel(window);
		this.scrollPanel.isDraggable = false;
		this.scrollPanel.useScroolBarY = true;
		this.scrollPanel.hideSelectBar = true;
		window.add(this.scrollPanel);
		this.scrollPanel.spacingX = 20;
		this.scrollPanel.spacingY = 20;


		if (SETTINGS.WEB_MODE)
			return;

		String currentDir = new File(".").getAbsolutePath();

		currentDir = currentDir.substring(0, currentDir.length() - 1);
		// currentDir = System.getProperty("java.io.tmpdir");
		//currentDir =  currentDir+  "\\savedChairs\\";
		currentDir = currentDir + "templateChairs";

		this.loadFolder(currentDir, this, gui, this.panel, applet);

	}

	public void show() {
		this.panel.show();
		this.scrollPanel.show();
	}

}
