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

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cc.sketchchair.sketch.SETTINGS_SKETCH;
import cc.sketchchair.sketch.SketchSpline;
import cc.sketchchair.sketch.SketchTools;
import cc.sketchchair.widgets.WidgetMaterials;
import cc.sketchchair.widgets.WidgetPlanes;
import cc.sketchchair.widgets.WidgetPreviewPanel;
import cc.sketchchair.widgets.WidgetSlices;
import cc.sketchchair.widgets.WidgetToolSettings;

import ModalGUI.GUIButton;
import ModalGUI.GUIColourPicker;
import ModalGUI.GUIComponentSet;
import ModalGUI.GUIEvent;
import ModalGUI.GUIImage;
import ModalGUI.GUILabel;
import ModalGUI.GUINumberfield;
import ModalGUI.GUIPanel;
import ModalGUI.GUIPanelTabbed;
import ModalGUI.GUISlider;
import ModalGUI.GUITextfield;
import ModalGUI.GUIToggle;
import ModalGUI.GUIToggleSlide;
import ModalGUI.GUIWindow;
import ModalGUI.ModalGUI;

import processing.core.PApplet;
import processing.core.PConstants;

class myMenuListener implements ActionListener, ItemListener {
	public ActionEvent quedAction = null;

	myMenuListener() {

	}

	public void actionPerformed(ActionEvent e) {
		quedAction = e;
	}

	public void processAction() {

		MenuItem source = (MenuItem) (quedAction.getSource());

		if (source.getActionCommand().equals("selectTool")) {
			GLOBAL.uiTools.selectTool(Integer.parseInt(source.getName()));
		}

		try {

			GUIEvent e2 = new GUIEvent(null, null);

			if (!source.getName().startsWith("menuitem"))
				e2.val = Integer.parseInt(source.getName());

			Method m = GLOBAL.uiTools.getClass().getDeclaredMethod(
					source.getActionCommand(), new Class[] { e2.getClass() });
			try {
				m.invoke(GLOBAL.uiTools, new Object[] { e2 });
			} catch (InvocationTargetException ex) {
				LOGGER.error(ex.getCause().getMessage());
			} catch (IllegalAccessException illegalaccessexception) {
			}
		} catch (NoSuchMethodException ex) {
			LOGGER.error("NoSuchMethodException");
		}
		quedAction = null;
	}

	//gets the class name of an object
	protected String getClassName(Object o) {
		String classString = o.getClass().getName();
		int dotIndex = classString.lastIndexOf(".");
		return classString.substring(dotIndex + 1);
	}

	public void itemStateChanged(ItemEvent e) {
		MenuItem source = (MenuItem) (e.getSource());
		String s = "Item event detected."
				+ source.getName()
				+ " "
				+ "    Event source: "
				+ source.getLabel()
				+ " (an instance of "
				+ getClassName(source)
				+ ")"
				+ "    New state: "
				+ ((e.getStateChange() == ItemEvent.SELECTED) ? "selected"
						: "unselected");
		LOGGER.debug(s);
	}
}

/**
 * Sets up the GUI.
 * @author gregsaul
 *
 */

class UI {
	static String LANGUAGE = "ENG"; //"JP";

	static int mouseX;
	static int mouseY;
	public static boolean mouseDown;
	public static int pmouseX;
	public static int pmouseY;
	public static boolean mouseClicked;

	static MenuBar myMenu;
	static Menu topButton;
	static MenuItem item1;
	static MenuItem item2;
	static MenuItem item3;
	static MenuItem item4;
	static MenuItem item5;

	static myMenuListener menuListen;
	public static GUIToggle physicsToggle;
	private static int mouseButton;
	private static GUIToggle patternButton;
	private static GUIToggle savePdfButton;
	private static GUIToggle printPdfButton;
	private static GUIToggle printRoboButton;
	private static GUIToggle rotateCamera;
	private static GUIToggle saveChairToggle;
	private static GUIToggle saveChairFileButton;
	private static GUIToggle shareChairButton;

	static void setupGUI(PApplet applet, ModalGUI gui) {

		float posY = 10;
		float posX = 10;
		gui.reset();
		GUIButton resetButton;

		gui.useAlphaMouseOver = false;

		GUIButton button;
		GUIToggle toggle;
		String LANGUAGE = SETTINGS.LANGUAGE;
		String TOOL_NAME = null;

		//GUIPanel filePanel = new GUIPanel(5,3,37f,340f);
		//filePanel.setLabel("file");
		GUIPanel toolPanel = new GUIPanel(10, 15, 42f, 390f, gui);

		GUIPanel cameraPanel = new GUIPanel(65f, 15, 115, 87, gui);
		cameraPanel.setLabel(Localization.getString("camera"));

		GUIPanel physicsPanel = new GUIPanel(65f, 110f, 115, 95, gui);
		physicsPanel.setLabel(Localization.getString("physics"));

		GLOBAL.environments.setupGUI(GLOBAL.gui, GLOBAL.applet);
		GLOBAL.planesWidget.setupGUI(GLOBAL.applet, GLOBAL.gui);
		GLOBAL.loadWidget.setupGUI(GLOBAL.applet, GLOBAL.gui);
		GLOBAL.widgetMaterials.setupGUI(GLOBAL.applet, GLOBAL.gui);

		GLOBAL.environments.panel.docTo(GLOBAL.slicesWidget.panel);
		GLOBAL.loadWidget.panel.docTo(GLOBAL.environments.panel);
		GLOBAL.widgetMaterials.panel.docTo(GLOBAL.loadWidget.panel);

		//cameraPanel.autoHide = true;
		//physicsPanel.autoHide = true;
		//filePanel.autoHide = true;

		gui.add(toolPanel);

		gui.add(cameraPanel);

		gui.add(physicsPanel);

		button = new GUIButton(posX, posY, "proButtons/GUI_DRAW_TOOL_UP.png",
				"proButtons/GUI_DRAW_TOOL_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.DRAW_TOOL);

		button.addToolTip(GLOBAL.applet, "proButtons/GUI_DRAW_TOOLTIP",
				LANGUAGE);

		gui.add(button);
		posY += 30;
		toolPanel.placeComponent(button);

		button = new GUIButton(posX, posY, "proButtons/GUI_LEG_TOOL_UP.png",
				"proButtons/GUI_LEG_TOOL_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.LEG_TOOL);
		//button.addToolTip(GLOBAL.applet,"proButtons/GUI_LEG_TOOLTIP", LANGUAGE);
		gui.add(button);
		posY += 30;
		toolPanel.placeComponent(button);

		button = new GUIButton(posX, posY, "proButtons/GUI_SELECT_TOOL_UP.png",
				"proButtons/GUI_SELECT_TOOL_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.SELECT_TOOL);
		button.addToolTip(GLOBAL.applet, "proButtons/GUI_SELECT_TOOLTIP",
				LANGUAGE);
		gui.add(button);
		posY += 30;
		toolPanel.placeComponent(button);

		button = new GUIButton(posX, posY, "proButtons/GUI_DRAW_PATH_UP.png",
				"proButtons/GUI_DRAW_PATH_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.DRAW_PATH_TOOL);
		button.addToolTip(GLOBAL.applet, "proButtons/GUI_DRAW_PATH_TOOLTIP",
				LANGUAGE);
		gui.add(button);
		posY += 30;
		toolPanel.placeComponent(button);

		button = new GUIButton(posX, posY,
				"proButtons/GUI_SELECT_BEZIER_UP.png",
				"proButtons/GUI_SELECT_BEZIER_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.SELECT_BEZIER_TOOL);
		button.addToolTip(GLOBAL.applet,
				"proButtons/GUI_SELECT_BEZIER_TOOLTIP", LANGUAGE);
		gui.add(button);
		posY += 30;
		toolPanel.placeComponent(button);

		button = new GUIButton(posX, posY,
				"proButtons/GUI_MEASURE_TOOL_UP.png",
				"proButtons/GUI_MEASURE_TOOL_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.MEASURE_TOOL);
		button.addToolTip(GLOBAL.applet, "proButtons/GUI_MEASURE_TOOLTIP",
				LANGUAGE);
		gui.add(button);
		posY += 30;
		toolPanel.placeComponent(button);

		button = new GUIButton(posX, posY, "proButtons/GUI_ZOOM_TOOL_UP.png",
				"proButtons/GUI_ZOOM_TOOL_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.ZOOM_CAM_TOOL);
		button.addToolTip(GLOBAL.applet, "proButtons/GUI_ZOOM_TOOLTIP",
				LANGUAGE);
		gui.add(button);
		posY += 30;
		cameraPanel.placeComponent(button);

		button = new GUIButton(posX, posY, "proButtons/GUI_ROTATE_TOOL_UP.png",
				"proButtons/GUI_ROTATE_TOOL_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.ROTATE_CAM_TOOL);
		button.addToolTip(GLOBAL.applet, "proButtons/GUI_ZOOM_TOOLTIP",
				LANGUAGE);
		gui.add(button);
		posY += 30;
		cameraPanel.placeComponent(button);

		button = new GUIButton(posX, posY, "proButtons/GUI_CAMERA_MOVE_UP.png",
				"proButtons/GUI_CAMERA_MOVE_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.MOVE_CAM_TOOL);
		button.addToolTip(GLOBAL.applet, "proButtons/GUI_MOVE_CAM_TOOLTIP",
				LANGUAGE);

		gui.add(button);
		posY += 30;
		cameraPanel.placeComponent(button);

		button = new GUIButton(posX, posY,
				"proButtons/GUI_CAM_JUMP_FRONT_UP.png",
				"proButtons/GUI_CAM_JUMP_FRONT_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools, "camJumpFront",
				UITools.MOVE_CAM_TOOL);
		button.addToolTip(GLOBAL.applet, "proButtons/GUI_MOVE_OBJECT_TOOLTIP",
				LANGUAGE);

		gui.add(button);
		posY += 30;
		cameraPanel.placeComponent(button);

		button = new GUIButton(posX, posY,
				"proButtons/GUI_CAM_JUMP_SIDE_UP.png",
				"proButtons/GUI_CAM_JUMP_SIDE_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools, "camJumpSide",
				UITools.MOVE_CAM_TOOL);
		button.addToolTip(GLOBAL.applet,
				"proButtons/GUI_CAM_JUMP_SIDE_TOOLTIP", LANGUAGE);

		gui.add(button);
		posY += 30;
		cameraPanel.placeComponent(button);

		button = new GUIButton(posX, posY,
				"proButtons/GUI_CAM_JUMP_TOP_UP.png",
				"proButtons/GUI_CAM_JUMP_TOP_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools, "camJumpTop",
				UITools.MOVE_CAM_TOOL);
		button.addToolTip(GLOBAL.applet, "proButtons/GUI_CAM_JUMP_TOP_TOOLTIP",
				LANGUAGE);

		gui.add(button);
		posY += 30;
		cameraPanel.placeComponent(button);

		//PHYSICS controls
		physicsToggle = new GUIToggle(posX, posY,
				"proButtons/GUI_PHYSICS_START_UP.png",
				"proButtons/GUI_PHYSICS_START_DOWN.png", gui);
		physicsToggle.addActionListener(GLOBAL.uiTools, "physicsPlay",
				UITools.MOVE_CAM_TOOL);
		physicsToggle.addToolTip(GLOBAL.applet,
				"proButtons/GUI_PHYSICS_START_TOOLTIP", LANGUAGE);

		physicsToggle.addLinkedVal(gui.physics_on);

		gui.add(physicsToggle);
		posY += 30;
		physicsPanel.placeComponent(physicsToggle);

		//PHYSICS controls
		button = new GUIButton(posX, posY,
				"proButtons/GUI_PHYSICS_PAUSE_UP.png",
				"proButtons/GUI_PHYSICS_PAUSE_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools, "physicsPause",
				UITools.MOVE_CAM_TOOL);
		button.addToolTip(GLOBAL.applet,
				"proButtons/GUI_PHYSICS_PAUSE_TOOLTIP", LANGUAGE);

		gui.add(button);
		posY += 30;
		physicsPanel.placeComponent(button);

		//PHYSICS controls
		button = new GUIButton(posX, posY,
				"proButtons/GUI_PHYSICS_RESTART_UP.png",
				"proButtons/GUI_PHYSICS_RESTART_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools, "physicsRewind",
				UITools.MOVE_CAM_TOOL);
		button.addToolTip(GLOBAL.applet,
				"proButtons/GUI_PHYSICS_RESTART_TOOLTIP", LANGUAGE);

		gui.add(button);
		posY += 30;
		physicsPanel.placeComponent(button);

		button = new GUIButton(posX, posY,
				"proButtons/GUI_MOVE_CAM_TOOL_UP.png",
				"proButtons/GUI_MOVE_CAM_TOOL_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.MOVE_OBJECT);
		gui.add(button);
		posY += 30;
		physicsPanel.placeComponent(button);

		toggle = new GUIToggle(posX, posY, "proButtons/GUI_BUILD_CHAIR_UP.png",
				"proButtons/GUI_BUILD_CHAIR_DOWN.png", gui);
		toggle.addActionListener(GLOBAL.uiTools, "toggleCentreConstraintChair",
				UITools.MOVE_OBJECT);
		gui.add(toggle);
		posY += 30;
		physicsPanel.placeComponent(toggle);
		toggle.setState(true);

		button = new GUIButton(posX, posY, "proButtons/GUI_CAMERA_MOVE_UP.png",
				"proButtons/GUI_CAMERA_MOVE_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.MOVE_2D_TOOL);
		gui.add(button);
		posY += 30;
		physicsPanel.placeComponent(button);

		button = new GUIButton(posX, posY, "proButtons/GUI_SCALE_TOOL_UP.png",
				"proButtons/GUI_SCALE_TOOL_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.SCALE_TOOL);
		button.addToolTip(GLOBAL.applet, "proButtons/GUI_SCALE_TOOLTIP",
				LANGUAGE);

		gui.add(button);
		posY += 30;
		toolPanel.placeComponent(button);

		button = new GUIButton(posX, posY, "proButtons/GUI_UNDO_TOOL_UP.png",
				"proButtons/GUI_UNDO_TOOL_DOWN.png", gui);
		button.addActionListener(GLOBAL.uiTools, "undo", UITools.MOVE_CAM_TOOL);
		button.addToolTip(GLOBAL.applet, "proButtons/GUI_UNDO_TOOLTIP",
				LANGUAGE);

		gui.add(button);
		posY += 30;
		toolPanel.placeComponent(button);

		/*
			
			gui.add(filePanel);
			
			button = new GUI.GUIButton(posX, posY, "proButtons/GUI_SAVE_PATTERN_UP.png",
					"proButtons/GUI_SAVE_PATTERN_DOWN.png",GLOBAL.applet);
			button.addActionListener(GLOBAL.uiTools, "saveChairToFileAuto",
					UITools.MOVE_OBJECT);
			gui.add(button);
			posY += 30;		
			filePanel.placeComponent(button);
			
			
			
			button = new GUI.GUIButton(posX, posY, "proButtons/GUI_LOAD_UP.png",
					"proButtons/GUI_LOAD_DOWN.png",GLOBAL.applet);
			button.addActionListener(GLOBAL.uiTools, "openChairFromFileAuto",
					UITools.MOVE_OBJECT);
			gui.add(button);
			posY += 30;		
			filePanel.placeComponent(button);
			

			
			
			
			button = new GUI.GUIButton(posX, posY, "proButtons/GUI_EXPORT_UP.png",
					"proButtons/GUI_EXPORT_DOWN.png",GLOBAL.applet);
			button.addActionListener(GLOBAL.uiTools, "savePDFauto",
					UITools.MOVE_OBJECT);
			gui.add(button);
			posY += 30;		
			filePanel.placeComponent(button);

			
			
			

			
			
			

			
			toggle = new GUI.GUIToggle(posX, posY, "proButtons/GUI_REFERENCE_GEOM_UP.png",
					"proButtons/GUI_REFERENCE_GEOM_DOWN.png",GLOBAL.applet);
			toggle.addActionListener(GLOBAL.uiTools, "toggleReferenceGeom",
					UITools.MOVE_CAM_TOOL);
			toggle.addToolTip(GLOBAL.applet,"proButtons/GUI_REFERENCE_GEOM_TOOLTIP", LANGUAGE);

			//gui.add(toggle);
			//posY += 30;		
			//toolPanel.placeComponent(toggle);
			
			
			
			
			button = new GUI.GUIButton(posX, posY, "proButtons/GUI_REDO_TOOL_UP.png",
					"proButtons/GUI_REDO_TOOL_DOWN.png",GLOBAL.applet);
			button.addActionListener(GLOBAL.uiTools, "redo",
					UITools.MOVE_CAM_TOOL);
			button.addToolTip(GLOBAL.applet,"proButtons/GUI_REDO_TOOLTIP", LANGUAGE);

			gui.add(button);
			posY += 30;		
			filePanel.placeComponent(button);

			
			toggle = new GUI.GUIToggle(posX, posY, "proButtons/GUI_PRINT_UP.png",
					"proButtons/GUI_PRINT_DOWN.png");
			toggle.addActionListener(GLOBAL.uiTools, "changeView",
					UITools.VIEW_SHAPE_PACK);
			filePanel.addToolTip(GLOBAL.applet,"proButtons/GUI_PRINT_TOOLTIP", LANGUAGE);
			gui.add(toggle);
			posY += 30;
			filePanel.placeComponent(toggle);
			
			
			
			
			button = new GUI.GUIButton(posX, posY, "proButtons/GUI_DELETE_UP.png",
					"proButtons/GUI_DELETE_DOWN.png",GLOBAL.applet);
			button.addActionListener(GLOBAL.uiTools, "deleteAllChairs",
					UITools.MOVE_CAM_TOOL);
			button.addToolTip(GLOBAL.applet,"proButtons/GUI_DELETE_TOOLTIP", LANGUAGE);

			gui.add(button);
			posY += 30;		
			filePanel.placeComponent(button);
			
			
			toggle = new GUI.GUIToggle(posX, posY, "proButtons/GUI_PERFORMANCE_MODE_UP.png",
					"proButtons/GUI_PERFORMANCE_MODE_DOWN.png",GLOBAL.applet);
			toggle.addActionListener(GLOBAL.uiTools, "togglePerformance",
					UITools.MOVE_CAM_TOOL);
			toggle.addToolTip(GLOBAL.applet,"proButtons/GUI_PERFORMANCE_MODE_TOOLTIP", LANGUAGE);

			gui.add(toggle);
			posY += 30;		
			filePanel.placeComponent(toggle);
			
			
			button = new GUI.GUIButton(posX, posY, "proButtons/GUI_SCREEN_CAPTURE_UP.png",
					"proButtons/GUI_SCREEN_CAPTURE_DOWN.png",GLOBAL.applet);
			button.addActionListener(GLOBAL.uiTools, "captureScreen",
					UITools.MOVE_CAM_TOOL);
			button.addToolTip(GLOBAL.applet,"proButtons/GUI_SCREEN_CAPTURE_TOOLTIP", LANGUAGE);

			gui.add(button);
			posY += 30;		
			filePanel.placeComponent(button);
			
			
			toggle = new GUI.GUIToggle(posX, posY, "proButtons/GUI_PERFORMANCE_MODE_UP.png",
					"proButtons/GUI_PERFORMANCE_MODE_DOWN.png",GLOBAL.applet);
			toggle.addActionListener(GLOBAL.uiTools, "toggleExpert",
					UITools.MOVE_CAM_TOOL);
			toggle.addToolTip(GLOBAL.applet,"proButtons/GUI_PERFORMANCE_MODE_TOOLTIP", LANGUAGE);

			gui.add(toggle);
			posY += 30;		
			filePanel.placeComponent(toggle);
			
			
			

			resetButton = new GUI.GUIButton(posX, posY, "proButtons/UI_RESET_UP_JP.png",
					"proButtons/UI_RESET_DOWN_JP.png",GLOBAL.applet);
			resetButton.addActionListener(GLOBAL.uiTools, "reset",
					UITools.MOVE_CAM_TOOL);
			//button.addToolTip(GLOBAL.applet,"proButtons/GUI_SCREEN_CAPTURE_TOOLTIP", LANGUAGE);

			gui.add(resetButton);
			resetButton.setPos(GLOBAL.windowWidth-resetButton.getWidth(), GLOBAL.windowHeight-resetButton.getHeight());
			resetButton.setController(gui);
			posY += 30;		
			//filePanel.placeComponent(button);
			
			*/

		setupMenuBar(applet);
	}

	static void setupGUIIntroWindow(PApplet applet, ModalGUI gui) {

		float windowWidth = SETTINGS.GUIDE_WINDOW_WIDTH;
		float windowHeight = SETTINGS.GUIDE_WINDOW_HEIGHT;

		GUIWindow window = new GUIWindow(0f, 0f, windowWidth, windowHeight, gui);
		window.setLightboxed(true);
		window.centre();
		gui.add(window);

		float posX = 100;
		float posY = 100;
		float spacingY = 80;

		GUIImage guiImg;
		guiImg = new GUIImage(30, 50, "gui/WINDOW_INTRO_TITLE.png", gui);
		window.add(guiImg);

		GUIButton button;
		button = new GUIToggle(400, 50, "gui/WINDOW_INTRO_NEW_CHAIR-03.png",
				gui);
		button.addActionListener(GLOBAL.uiTools, "deleteAllChairs",
				UITools.LEG_TOOL);
		button.addActionListener(GLOBAL.uiTools, "setDisplaypanelAsShown",
				UITools.LEG_TOOL);
		button.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.DRAW_TOOL);

		button.addActionListener(window, "close", UITools.LEG_TOOL);
		window.add(button);
		posY += spacingY;

		button = new GUIToggle(400, 200, "gui/WINDOW_INTRO_LIBRARY.png", gui);
		button.addActionListener(window, "close", UITools.LEG_TOOL);
		button.addActionListener(GLOBAL.uiTools, "setDisplaypanelAsShown",
				UITools.LEG_TOOL);
		button.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.SELECT_TOOL);
		button.addActionListener(GLOBAL.uiTools, "loadTemplateChair",
				UITools.LEG_TOOL);
		window.add(button);

		posY += spacingY;

	}

	static void setupGUIExhibition(PApplet applet, ModalGUI gui) {

		if (true)
			return;

		float posY = 10;
		float posX = 10;

		gui.reset();
		GUIButton resetButton;

		gui.useAlphaMouseOver = true;

		GUIButton button;
		GUIToggle toggle;
		String TOOL_NAME = null;

		posY = 0;
		setupMenuBar(applet);

		/*
		button = new GUI.GUIButton(GLOBAL.windowWidth-100, posY, "touchButtons/GUI_NEWCHAIR_UP.png",
				"touchButtons/GUI_NEWCHAIR_DOWN.png", GLOBAL.applet);
		button.addActionListener(GLOBAL.uiTools, "newChair",
				UITools.DRAW_TOOL);
		
		button.addToolTip(GLOBAL.applet,"GUI_DRAW_TOOLTIP", LANGUAGE);
		
		gui.add(button);

		
		
		
		
		*/

		toggle = new GUIToggle(GLOBAL.windowWidth - 100, posY, "touchButtons"
				+ LANGUAGE + "/GUI_DRAW_UP.png", "touchButtons" + LANGUAGE
				+ "/GUI_DRAW_DOWN.png", gui);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.DRAW_TOOL);
		gui.add(toggle);

		posY += 85;

		toggle = new GUIToggle(GLOBAL.windowWidth - 100, posY, "touchButtons"
				+ LANGUAGE + "/GUI_DRAW_LEG_UP.png", "touchButtons" + LANGUAGE
				+ "/GUI_DRAW_LEG_DOWN.png", gui);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool", UITools.LEG_TOOL);

		gui.add(toggle);

		posY += 85;

		GUISlider slider = new GUISlider(GLOBAL.windowWidth - 60, posY, 60, 20,
				50, gui);
		slider.setVal(GLOBAL.uiTools.brush_dia);
		slider.addActionListener(GLOBAL.uiTools, "changeToolWidth",
				UITools.DRAW_TOOL);
		slider.setShowValLabel(true);
		slider.setLabelValMultiplier(2.0f);

		toggle.addToolTip(GLOBAL.applet, "proButtons/GUI_DRAW_TOOLTIP",
				LANGUAGE);
		//	gui.add(slider);

		posY += 5;
		toggle = new GUIToggle(GLOBAL.windowWidth - 100, posY, "touchButtons"
				+ LANGUAGE + "/GUI_EDIT_UP.png", "touchButtons" + LANGUAGE
				+ "/GUI_EDIT_DOWN.png", gui);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.SELECT_TOOL);

		gui.add(toggle);

		posY += 85;
		toggle = new GUIToggle(GLOBAL.windowWidth - 100, posY, "touchButtons"
				+ LANGUAGE + "/GUI_SIT_UP.png", "touchButtons" + LANGUAGE
				+ "/GUI_STAND_DOWN.png", gui);
		toggle.addActionListener(GLOBAL.uiTools, "sitStand", UITools.DRAW_TOOL);

		gui.add(toggle);

		/*	
			
			posY += 140;
			toggle = new GUI.GUIToggle(GLOBAL.windowWidth-100, posY, "touchButtons"+LANGUAGE+"/GUI_CAMERAZOOM_UP.png",
					"touchButtons"+LANGUAGE+"/GUI_CAMERAZOOM_DOWN.png", GLOBAL.applet);
			toggle.addActionListener(GLOBAL.uiTools, "selectTool",
					UITools.ZOOM_CAM_TOOL);
					
			gui.add(toggle);
			
			
			toggle = new GUI.GUIToggle(GLOBAL.windowWidth-66, posY, "touchButtons"+LANGUAGE+"/GUI_CAMERAMOVE_UP.png",
					"touchButtons"+LANGUAGE+"/GUI_CAMERAMOVE_DOWN.png", GLOBAL.applet);
			toggle.addActionListener(GLOBAL.uiTools, "selectTool",
					UITools.MOVE_CAM_TOOL);
					
			gui.add(toggle);
			posY -= 60;
			
			*/
		posY += 85;
		rotateCamera = new GUIToggle(GLOBAL.windowWidth - 100, posY,
				"touchButtons" + LANGUAGE + "/GUI_CAMERA_ROTATE_UP.png",
				"touchButtons" + LANGUAGE + "/GUI_CAMERA_ROTATE_DOWN.png", gui);
		rotateCamera.addActionListener(GLOBAL.uiTools, "autoRotate",
				UITools.ROTATE_CAM_TOOL);

		gui.add(rotateCamera);

		posY += 85;
		patternButton = new GUIToggle(GLOBAL.windowWidth - 100, posY,
				"touchButtons" + LANGUAGE + "/GUI_PATTERN_UP.png",
				"touchButtons" + LANGUAGE + "/GUI_PATTERN_DOWN.png", gui);
		patternButton.addActionListener(GLOBAL.uiTools, "toggleView",
				UITools.VIEW_SHAPE_PACK);
		gui.add(patternButton);

		posY += 85;
		savePdfButton = new GUIToggle(GLOBAL.windowWidth - 100, posY,
				"touchButtons" + LANGUAGE + "/GUI_SAVEPDF_UP.png",
				"touchButtons" + LANGUAGE + "/GUI_SAVEPDF_DOWN.png", gui);
		savePdfButton.addActionListener(GLOBAL.uiTools, "savePDF",
				UITools.VIEW_SHAPE_PACK);
		savePdfButton.hide();
		gui.add(savePdfButton);

		posY += 85;
		printPdfButton = new GUIToggle(GLOBAL.windowWidth - 100, posY,
				"touchButtons" + LANGUAGE + "/GUI_PRINT_UP.png", "touchButtons"
						+ LANGUAGE + "/GUI_PRINT_DOWN.png", gui);
		printPdfButton.addActionListener(GLOBAL.uiTools, "print",
				UITools.VIEW_SHAPE_PACK);
		printPdfButton.hide();
		gui.add(printPdfButton);

		posY += 85;
		printRoboButton = new GUIToggle(GLOBAL.windowWidth - 100, posY,
				"touchButtons" + LANGUAGE + "/GUI_CUTPLOT_UP.png",
				"touchButtons" + LANGUAGE + "/GUI_CUTPLOT_DOWN.png", gui);
		printRoboButton.addActionListener(GLOBAL.uiTools, "saveCraftRobo",
				UITools.VIEW_SHAPE_PACK);
		printRoboButton.hide();
		gui.add(printRoboButton);

		posY -= 85 * 2;

		saveChairToggle = new GUIToggle(GLOBAL.windowWidth - 100, posY,
				"touchButtons" + LANGUAGE + "/GUI_SAVECHAIR_UP.png",
				"touchButtons" + LANGUAGE + "/GUI_SAVECHAIR_DOWN.png", gui);
		saveChairToggle.addActionListener(GLOBAL.uiTools, "",
				UITools.VIEW_SAVE_CHAIR);

		gui.add(saveChairToggle);
		posY += 85;

		saveChairFileButton = new GUIToggle(GLOBAL.windowWidth - 100, posY,
				"touchButtons" + LANGUAGE + "/GUI_SAVECHAIRFILE_UP.png",
				"touchButtons" + LANGUAGE + "/GUI_SAVECHAIRFILE_DOWN.png", gui);
		saveChairFileButton.addActionListener(GLOBAL.uiTools,
				"saveChairToFile", UITools.DRAW_TOOL);
		saveChairFileButton.hide();
		gui.add(saveChairFileButton);
		posY += 85;

		shareChairButton = new GUIToggle(GLOBAL.windowWidth - 100, posY,
				"touchButtons" + LANGUAGE + "/GUI_SHARE_UP.png", "touchButtons"
						+ LANGUAGE + "/GUI_SHARE_DOWN.png", gui);
		shareChairButton.addActionListener(SketchChairCloudhook.getInstance(),
				"ShareChairOnline", UITools.DRAW_TOOL);
		shareChairButton.hide();
		gui.add(shareChairButton);
		posY -= 85;

		toggle = new GUIToggle(GLOBAL.windowWidth - 100, posY, "touchButtons"
				+ LANGUAGE + "/GUI_UNDO_UP.png", "touchButtons" + LANGUAGE
				+ "/GUI_UNDO_DOWN.png", gui);
		toggle.addActionListener(GLOBAL.uiTools, "undo", UITools.DRAW_TOOL);

		gui.add(toggle);

		posY += 85;
		toggle = new GUIToggle(GLOBAL.windowWidth - 100, posY,
				"/GUI_DELETE_UP.png", "/GUI_DELETE_DOWN.png", gui);
		toggle.addActionListener(GLOBAL.uiTools, "deleteLastChair",
				UITools.DRAW_TOOL);

		gui.add(toggle);

		
		/*
		posY += 85;
		toggle = new GUIToggle(GLOBAL.windowWidth - 100, posY, "touchButtons"
				+ LANGUAGE + "/GUI_RESTART_UP.png", "touchButtons" + LANGUAGE
				+ "/GUI_RESTART_DOWN.png", gui);
		toggle.addActionListener(GLOBAL.uiTools, "reset", UITools.DRAW_TOOL);

		gui.add(toggle);

		toggle = new GUIToggle(0, 0, "touchButtons" + LANGUAGE
				+ "/GUI_HELP_UP.png", "touchButtons" + LANGUAGE
				+ "/GUI_HELP_DOWN.png", gui);
		toggle.addActionListener(GLOBAL.uiTools, "showInstructions",
				UITools.DRAW_TOOL);

		gui.add(toggle);
*/
		//	toolPanel.placeComponent(button);

	}

	static void setupGUITabsAll(PApplet applet, ModalGUI gui) {
		
		
		
		GUIToggle toggle = null;
		float posX= 0; float posY = 0;
		float button_width = 45;
		float button_height = 45;
		
		GLOBAL.uiTools.toggleSet.reset();
		
		
		
		GLOBAL.previewWidget = new WidgetPreviewPanel(GLOBAL.windowWidth -260, 0,260,GLOBAL.windowHeight-250, gui);
		gui.add(GLOBAL.previewWidget);
		
		
		

		/*
		GUIButton imgB = new GUIButton(0, 0, "SketchCHAIRtittle.png",
				"SketchCHAIRtittleOVER.png", gui);
		imgB.addActionListener(GLOBAL.uiTools, "gotoWebsite", null);
		gui.add(imgB);
		*/
		/*

		GUIToggleSlide sliderToggle = new GUIToggleSlide(GLOBAL.windowWidth -110, 18, "basic",
				"expert", gui);
		sliderToggle.addActionListener(GLOBAL.uiTools, "toggleExpert", null);
		sliderToggle.isDown = !SETTINGS.EXPERT_MODE;
		gui.add(sliderToggle);
		*/
		
		
		
		
		/*
		
		//camera
		 * 
		 * 
		
		tabbedPanel.addTabbedPanel(cameraPanel, "camera", gui);
*/
		//toggleSet = new GUIComponentSet();

		
		 button_width = 25;

		GLOBAL.patternCameraPanel = new GUIPanel(0f, 10f, (int) 0,(int) 0, gui);
		gui.add(GLOBAL.patternCameraPanel);
		GLOBAL.patternCameraPanel.renderBorder =false;
		
		toggle = new GUIToggle(posX, posY, button_width, button_width,
				"gui/camera_zoom.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.ZOOM_CAM_TOOL);
		//toggle.setLabel("zoom");
		//toggle.label.align = GUILabel.CENTRE;
		//toggle.label.layout = GUILabel.UNDER_COMPONENT;

		GLOBAL.patternCameraPanel.add(toggle);

		posX += button_width+5;

	

		toggle = new GUIToggle(posX, posY, button_width, button_width,
				"gui/camera_move.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.MOVE_CAM_TOOL);
		//toggle.setLabel("move");
		//toggle.label.align = GUILabel.CENTRE;
		//toggle.label.layout = GUILabel.UNDER_COMPONENT;

		GLOBAL.patternCameraPanel.add(toggle);

		
		
		GLOBAL.patternCameraPanel.hide();
		
		
		
		
		
		
		GLOBAL.cameraPanel = new GUIPanel(0f, 10f, (int) 0,(int) 0, gui);
		gui.add(GLOBAL.cameraPanel);
		GLOBAL.cameraPanel.renderBorder =false;
		
		 posX = 0;
		 posY = 0;
		
		
		 
		 
		 GUIComponentSet physicsSet = new GUIComponentSet(); 
		 //Physics
		 
			toggle = new GUIToggle(posX, posY, button_width, button_width,
					"gui/GUI_PHYSICS_PLAY.png","gui/GUI_PHYSICS_PAUSE.png", gui);
			toggle.setComponentSet(physicsSet);
			toggle.addActionListener(GLOBAL.uiTools, "physicsPlayPause",
					UITools.ZOOM_CAM_TOOL);
			//toggle.setLabel("play");
			//toggle.label.align = GUILabel.CENTRE;
			//toggle.label.layout = GUILabel.UNDER_COMPONENT;

			GLOBAL.cameraPanel.add(toggle);

			
			posX += button_width+5;

			
			toggle = new GUIToggle(posX, posY, button_width, button_width,
					"gui/GUI_PHYSICS_STOP.png", gui);
			toggle.setComponentSet(physicsSet);
			toggle.addActionListener(GLOBAL.uiTools, "physicsRewind",
					UITools.ZOOM_CAM_TOOL);
			//toggle.setLabel("stop");
			//toggle.label.align = GUILabel.CENTRE;
			//toggle.label.layout = GUILabel.UNDER_COMPONENT;

			GLOBAL.cameraPanel.add(toggle);

			posX += button_width+25;
			
			//GUILabel label = new GUILabel(posX, posY,"view",gui);
			//gui.add(label);
			
		toggle = new GUIToggle(posX, posY, button_width, button_width,
				"gui/camera_zoom.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.ZOOM_CAM_TOOL);
		//toggle.setLabel("zoom");
		//toggle.label.align = GUILabel.CENTRE;
		//toggle.label.layout = GUILabel.UNDER_COMPONENT;

		GLOBAL.cameraPanel.add(toggle);

		posX += button_width+5;

	

		toggle = new GUIToggle(posX, posY, button_width, button_width,
				"gui/camera_move.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.MOVE_CAM_TOOL);
		//toggle.setLabel("move");
		//toggle.label.align = GUILabel.CENTRE;
		//toggle.label.layout = GUILabel.UNDER_COMPONENT;

		GLOBAL.cameraPanel.add(toggle);

		posX += button_width+5;

		
		toggle = new GUIToggle(posX, posY, button_width, button_width,
				"gui/camera_rotate.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.ROTATE_CAM_TOOL);
		//toggle.setLabel("rotate");
		//toggle.label.align = GUILabel.CENTRE;
		//toggle.label.layout = GUILabel.UNDER_COMPONENT;

		GLOBAL.cameraPanel.add(toggle);

		posX += button_width+5;
		
		/*
		new GUIPanel(GLOBAL.windowWidth - slicePanleWidth,
				0, slicePanleWidth, slicePanleHeight, gui);
		*/
		
		

		/*
		sliderToggle = new GUIToggleSlide(410, 18, "3D", "pattern", gui);
		sliderToggle.isDown = GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT;
		sliderToggle.addActionListener(GLOBAL.uiTools, "toggleView", null);
		gui.add(sliderToggle);

	*/
		
		
		
		

		
		
	}
	static void setupGUITabsBasic(PApplet applet, ModalGUI gui) {

		
		
		if(GLOBAL.designToolbarPattern != null)
		GLOBAL.designToolbarPattern.hide();
		
		if(GLOBAL.designToolbarBasic != null)
			GLOBAL.designToolbarBasic.show();
		
		if(GLOBAL.designToolbarAdvanced!= null)
			GLOBAL.designToolbarAdvanced.hide();
		
		

		//if we've already loaded the toolboar down't load it again
		if(GLOBAL.designToolbarAdvanced != null)
			GLOBAL.designToolbarAdvanced  = null;
		
		//if we're changing from basic to advanced view dump the basic gui.
		if(GLOBAL.designToolbarBasic != null)
			return;
		
		
		
		
		
		float button_width = 45;
		float button_height = 45;

		float posY = 10;
		float posX = 10;
		float panelHeight = 120;

		gui.reset();


		//panel for slice widget
		float slicePanleWidth = 100;
		float slicePanleHeight = 100;
		GLOBAL.slicePanel = new GUIPanel(GLOBAL.windowWidth - slicePanleWidth,
				0, slicePanleWidth, slicePanleHeight, gui);
		GLOBAL.slicePanel.invisible();
		gui.add(GLOBAL.slicePanel);
		
		GLOBAL.slicesWidget = new WidgetSlices(240, 0, 300, slicePanleHeight, gui);
		GLOBAL.planesWidget = new WidgetPlanes(0, 0, 900, slicePanleHeight, gui);

		
		

		setupGUITabsAll(applet,gui);

		GUIPanelTabbed tabbedPanel = new GUIPanelTabbed(0f, GLOBAL.windowHeight
				- panelHeight, (int) GLOBAL.windowWidth, (int) panelHeight, gui);
		gui.add(tabbedPanel);
		GLOBAL.designToolbarBasic = tabbedPanel;

		//tools
		GUIPanel toolPanel = new GUIPanel(0f,
				GLOBAL.windowHeight - panelHeight, (int) GLOBAL.windowWidth,
				(int) panelHeight, gui);
		tabbedPanel.addTabbedPanel(toolPanel, "tools", gui);


		GUIToggle toggle = new GUIToggle(posX, posY, button_width,
				button_height, "gui/draw.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.DRAW_TOOL);
		toggle.setLabel(Localization.getString("brush"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		if (GLOBAL.uiTools.SketchTools.getCurrentTool() == SketchTools.DRAW_TOOL)
			toggle.toggleDown();
		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/add_leg.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.LEG_TOOL);
		
		
		toggle.addActionListener(GLOBAL.uiTools, "selectLegPlanes",
				SketchTools.LEG_TOOL);
		
		
		toggle.setLabel(Localization.getString("leg"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		if (GLOBAL.uiTools.SketchTools.getCurrentTool() == SketchTools.LEG_TOOL)
			toggle.toggleDown();
		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/select.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.SELECT_TOOL);
		toggle.setLabel(Localization.getString("select"));
		
		if (GLOBAL.uiTools.SketchTools.getCurrentTool() == SketchTools.SELECT_TOOL)
			toggle.toggleDown();
		
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(120, posY, button_width, button_height,
				"gui/sit.png", "gui/stand.png", gui);
		toggle.addActionListener(GLOBAL.uiTools, "sitStand", UITools.DRAW_TOOL);
		toggle.setLabel(Localization.getString("sit_stand"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(120, posY, button_width, button_height,
				"gui/camera_rotate.png", gui);
		toggle.addActionListener(GLOBAL.uiTools, "autoRotate",
				UITools.DRAW_TOOL);
		toggle.setLabel(Localization.getString("rotate"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		toolPanel.placeComponent(toggle);

		GUIButton button = new GUIButton(posX, posY, button_width,
				button_height, "gui/undo.png", null, gui);
		button.addActionListener(GLOBAL.uiTools, "undo", UITools.SCALE_TOOL);
		button.setLabel(Localization.getString("undo"));
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;

		toolPanel.placeComponent(button);

		button = new GUIButton(posX, posY, button_width, button_height,
				"gui/GUI_SLICE_DELETE_BUTTON.png", null, gui);
		button.addActionListener(GLOBAL.uiTools, "deleteAllChairsFlag",
				UITools.SCALE_TOOL);
		button.setLabel(Localization.getString("delete"));
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;

		toolPanel.placeComponent(button);

		button = new GUIButton(posX, posY, button_width, button_height,
				"gui/GUI_MAKE_BUTTON.png", null, gui);
		button.addActionListener(GLOBAL.uiTools, "makeChairWindow",
				UITools.SCALE_TOOL);
		button.setLabel(Localization.getString("make"));
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;

		toolPanel.placeComponent(button);

		/*
		button = new GUIButton(GLOBAL.windowWidth - 80, posY + 10,
				button_width, button_height, "gui/GUI_SLICE_DELETE_BUTTON.png",
				null, gui);
		button.addActionListener(GLOBAL.uiTools, "reset", UITools.SCALE_TOOL);
		button.setLabel("reset");
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;

		toolPanel.add(button);
		*/

		setupMenuBar(applet);

		if (GLOBAL.displayIntroPanel && SETTINGS.displayIntroPanel) {
			setupGUIIntroWindow(applet, gui);
		}

	}

	static void setupGUITabsExpert(PApplet applet, ModalGUI gui) {

		
		
		if(GLOBAL.designToolbarPattern != null)
		GLOBAL.designToolbarPattern.hide();
		
		if(GLOBAL.designToolbarBasic != null)
			GLOBAL.designToolbarBasic.hide();
		
		if(GLOBAL.designToolbarAdvanced!= null)
			GLOBAL.designToolbarAdvanced.show();
		
		
		//if we're changing from basic to advanced view dump the basic gui.
		if(GLOBAL.designToolbarBasic != null)
			GLOBAL.designToolbarBasic = null;
		
		
		//if we've already loaded the toolboar down't load it again
		if(GLOBAL.designToolbarAdvanced != null)
			return;
		
		
		float button_width = 45;
		float button_height = 45;

		float posY = 10;
		float posX = 10;
		float panelHeight = SETTINGS.panelHeight;
		
		float panelWidth = SETTINGS.panelWidth;

		gui.reset();


		
		//panel for slice widget
		float slicePanleWidth = 100;
		float slicePanleHeight = 100;
		GLOBAL.slicePanel = new GUIPanel(GLOBAL.windowWidth - slicePanleWidth ,0 ,slicePanleWidth, slicePanleHeight ,gui);
		GLOBAL.slicePanel.invisible();
		gui.add(GLOBAL.slicePanel);
		setupGUITabsAll(applet,gui);

		/*
		sliderToggle = new GUIToggleSlide(GLOBAL.windowWidth - 100, 18,
				"3D Preview on", "off", gui);
		sliderToggle.isDown = true;
		sliderToggle.addActionListener(GLOBAL.uiTools, "render3dPreview", null);
		gui.add(sliderToggle);
		 */

		GUIPanelTabbed tabbedPanel = new GUIPanelTabbed((GLOBAL.windowWidth-panelWidth)/2.0f, GLOBAL.windowHeight
				- panelHeight, (int) panelWidth, (int) panelHeight, gui);
		gui.add(tabbedPanel);
		GLOBAL.designToolbarAdvanced = tabbedPanel;

		/*
				  ______
		_________/tools/____________________________________________________________________________
		
		Tools tab hold tools for manipulating design
		*/

		GUIPanel toolPanel = new GUIPanel(0f,0f, (int) tabbedPanel.getWidth(),
				(int) panelHeight, gui);
		tabbedPanel.addTabbedPanel(toolPanel, Localization.getString("tools") ,"gui/GUI_TAB_EDIT_UP.png","gui/GUI_TAB_EDIT_DOWN.png", gui);


		GUIToggle toggle = new GUIToggle(posX, posY, button_width,
				button_height, "gui/draw.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.DRAW_TOOL);
		toggle.setLabel(Localization.getString("brush"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		
		if (GLOBAL.uiTools.SketchTools.getCurrentTool() == SketchTools.DRAW_TOOL)
			toggle.toggleDown();
		
		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/add_leg.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.LEG_TOOL);
		
		if(SETTINGS.selectLegPlanesAuto)
		toggle.addActionListener(GLOBAL.uiTools, "selectLegPlanes",
				SketchTools.LEG_TOOL);
		
		toggle.setLabel(Localization.getString("leg"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		if (GLOBAL.uiTools.SketchTools.getCurrentTool() == SketchTools.LEG_TOOL)
			toggle.toggleDown();
		
		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/select.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.SELECT_TOOL);
		toggle.setLabel(Localization.getString("select"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;


		if (GLOBAL.uiTools.SketchTools.getCurrentTool() == SketchTools.SELECT_TOOL)
			toggle.toggleDown();
		
		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/draw_path.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.DRAW_PATH_TOOL);
		toggle.setLabel(Localization.getString("path"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		if (GLOBAL.uiTools.SketchTools.getCurrentTool() == SketchTools.DRAW_PATH_TOOL)
			toggle.toggleDown();
		
		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/GUI_OFFSET_PATH_ICON.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.DRAW_OFFSETPATH_TOOL);
		toggle.setLabel(Localization.getString("offset_path"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		if (GLOBAL.uiTools.SketchTools.getCurrentTool() == SketchTools.DRAW_OFFSETPATH_TOOL)
			toggle.toggleDown();
		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/bezier.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools.SketchTools, "selectTool",
				SketchTools.SELECT_BEZIER_TOOL);
		toggle.setLabel(Localization.getString("bezier"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		if (GLOBAL.uiTools.SketchTools.getCurrentTool() == SketchTools.SELECT_BEZIER_TOOL)
			toggle.toggleDown();
		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/measure.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.MEASURE_TOOL);
		toggle.setLabel(Localization.getString("measure"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		if (GLOBAL.uiTools.getCurrentTool() == UITools.MEASURE_TOOL)
			toggle.toggleDown();
		toolPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/scale.png", gui);
		toggle.setComponentSet(GLOBAL.uiTools.toggleSet);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.SCALE_TOOL);
		toggle.setLabel(Localization.getString("scale"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		toolPanel.placeComponent(toggle);

		GUIButton button = new GUIButton(posX, posY, button_width,
				button_height, "gui/undo.png", null, gui);
		button.addActionListener(GLOBAL.uiTools, "undo", UITools.SCALE_TOOL);
		button.setLabel(Localization.getString("undo"));
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;

		toolPanel.placeComponent(button);

		GLOBAL.widgetToolSettings = new WidgetToolSettings(600, 0, 250,
				toolPanel.getHeight(), gui);
		toolPanel.add(GLOBAL.widgetToolSettings);

		/*
		 * 
		 * 
		 * 
		 * 
				______
		_______/layers/____________________________________________________________________________

		Selecting and editing layers
		 
		 */

		GUIPanel LayerPanel = new GUIPanel(0f, 0f, (int) tabbedPanel.getWidth(), (int) panelHeight, gui);
		tabbedPanel.addTabbedPanel(LayerPanel, Localization.getString("layers"),"gui/GUI_TAB_LAYERS_UP.png","gui/GUI_TAB_LAYERS_DOWN.png", gui);

		GLOBAL.planesWidget = new WidgetPlanes(0, 0, 900, 100, gui);
		//GLOBAL.planesWidget.setup();

		LayerPanel.add(GLOBAL.planesWidget);

		/*
		 * 
		 * 
		 * 
		 * 
				______
		_______/slices/____________________________________________________________________________

		Selecting and editing slices
		 
		 */
		GUIPanel slicesPanel = new GUIPanel(0f, 0f, tabbedPanel.getWidth(), (int) panelHeight, gui);
		tabbedPanel.addTabbedPanel(slicesPanel, Localization.getString("slices"),"gui/GUI_TAB_SLICES_UP.png","gui/GUI_TAB_SLICES_DOWN.png", gui);

		GLOBAL.toggleSetSlices = new GUIComponentSet();

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/GUI_SLICE_SLICE_BUTTON.png", gui);
		toggle.setComponentSet(GLOBAL.toggleSetSlices);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.SLICES_SINGLE_SLICE);
		toggle.setLabel(Localization.getString("slice"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		slicesPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/GUI_SLICE_MULTISLICE_BUTTON.png", gui);
		toggle.setComponentSet(GLOBAL.toggleSetSlices);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.SLICES_GROUP_SLICES);
		toggle.setLabel(Localization.getString("slice_group"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		slicesPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/GUI_SLICE_SLAT_BUTTON.png", gui);
		toggle.setComponentSet(GLOBAL.toggleSetSlices);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.SLICES_SINGLE_SLAT);
		toggle.setLabel("slat");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		slicesPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/GUI_SLICE_SLAT_BUTTON.png", gui);
		toggle.setComponentSet(GLOBAL.toggleSetSlices);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.SLICES_SLATSLICE_GROUP);
		toggle.setLabel(Localization.getString("slatSlice_group"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		slicesPanel.placeComponent(toggle);

		GLOBAL.slicesWidget = new WidgetSlices(250, 0, 300, panelHeight, gui);
		GLOBAL.slicesWidget.setupButtons();
		slicesPanel.add(GLOBAL.slicesWidget);

		
		/*
		button = new GUIButton(GLOBAL.windowWidth - 80,
				GLOBAL.windowHeight - 100, button_width, button_height,
				"gui/GUI_SLICE_DELETE_BUTTON.png", null, gui);
		button.addActionListener(GLOBAL.uiTools, "reset", UITools.SCALE_TOOL);
		button.setLabel("reset");
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;

		gui.add(button);
*/
		//GLOBAL.widgetToolSettings.setupToolSettings(0);

		/*

		//physics
		GUIPanel physicsPanel = new GUIPanel(0f, GLOBAL.windowHeight
				- panelHeight, (int) GLOBAL.windowWidth, (int) panelHeight, gui);
		tabbedPanel.addTabbedPanel(physicsPanel, "physics", gui);

		physicsPanel.setContentPosition(30, 20);
		sliderToggle = new GUIToggleSlide(80, 50, "on", "off", gui);
		sliderToggle.addActionListener(GLOBAL.jBullet, "physics_on");
		physicsPanel.placeComponent(sliderToggle);

		posY = 20;
		posX = 0;
		posX += 185;
		toggle = new GUIToggle(120, posY, button_width, button_height,
				"gui/sit.png", "gui/stand.png", gui);
		toggle.addActionListener(GLOBAL.uiTools, "sitStand", UITools.DRAW_TOOL);
		toggle.setLabel("sit/stand");

		toggle.label.align = GUILabel.LEFT;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		physicsPanel.placeComponent(toggle);

		posX += 185;
		toggle = new GUIToggle(190, posY, button_width, button_height,
				"gui/pause.png", gui);
		toggle.addActionListener(GLOBAL.uiTools, "puase", UITools.DRAW_TOOL);
		toggle.setLabel("pause");

		toggle.label.align = GUILabel.LEFT;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		physicsPanel.placeComponent(toggle);

		//camera
		GUIPanel cameraPanel = new GUIPanel(0f, 0f, (int) GLOBAL.windowWidth,
				(int) panelHeight, gui);
		tabbedPanel.addTabbedPanel(cameraPanel, "camera", gui);

		//toggleSet = new GUIComponentSet();

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/camera_zoom.png", gui);
		toggle.setComponentSet(toggleSet);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.ZOOM_CAM_TOOL);
		toggle.setLabel("zoom");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		cameraPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/camera_rotate.png", gui);
		toggle.setComponentSet(toggleSet);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.ROTATE_CAM_TOOL);
		toggle.setLabel("rotate");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		cameraPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/camera_move.png", gui);
		toggle.setComponentSet(toggleSet);
		toggle.addActionListener(GLOBAL.uiTools, "selectTool",
				UITools.MOVE_CAM_TOOL);
		toggle.setLabel("move");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		cameraPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/GUI_CAM_JUMP_FRONT_UP.png", gui);
		toggle.setComponentSet(toggleSet);
		toggle.addActionListener(GLOBAL.uiTools, "camJumpFront",
				UITools.MOVE_CAM_TOOL);
		toggle.setLabel("front");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		cameraPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/GUI_CAM_JUMP_SIDE_UP.png", gui);
		toggle.setComponentSet(toggleSet);
		toggle.addActionListener(GLOBAL.uiTools, "camJumpSide",
				UITools.MOVE_CAM_TOOL);
		toggle.setLabel("side");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		cameraPanel.placeComponent(toggle);

		toggle = new GUIToggle(posX, posY, button_width, button_height,
				"gui/GUI_CAM_JUMP_TOP_UP.png", gui);
		toggle.setComponentSet(toggleSet);
		toggle.addActionListener(GLOBAL.uiTools, "camJumpTop",
				UITools.MOVE_CAM_TOOL);
		toggle.setLabel("top");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		cameraPanel.placeComponent(toggle);

		//save
		GUIPanel savePanel = new GUIPanel(0f, 0f, (int) GLOBAL.windowWidth,
				(int) panelHeight, gui);
		tabbedPanel.addTabbedPanel(savePanel, "save", gui);

		button = new GUIButton(0, 0, button_width, button_height,
				"gui/save_pdf.png", null, gui);
		button.addActionListener(GLOBAL.uiTools, "savePDF",
				UITools.VIEW_SHAPE_PACK);
		button.setLabel("save pdf");
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;
		savePanel.placeComponent(button);

		button = new GUIButton(0, 0, button_width, button_height,
				"gui/print.png", null, gui);
		button.addActionListener(GLOBAL.uiTools, "print",
				UITools.VIEW_SHAPE_PACK);
		button.setLabel("print");
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;
		savePanel.placeComponent(button);

		button = new GUIButton(0, 0, button_width, button_height,
				"gui/plot.png", null, gui);
		button.addActionListener(GLOBAL.uiTools, "saveCraftRobo",
				UITools.VIEW_SHAPE_PACK);
		button.setLabel("plot");
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;
		savePanel.placeComponent(button);

		button = new GUIButton(0, 0, button_width, button_height,
				"gui/save_cha.png", null, gui);
		button.addActionListener(GLOBAL.uiTools, "saveChairToFile",
				UITools.VIEW_SHAPE_PACK);
		button.setLabel("save");
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;
		savePanel.placeComponent(button);

		
		button = new GUIButton(0,0,button_width,button_height,  "gui/share.png",null,gui);
		button.addActionListener(GLOBAL.uiTools, "ShareChairOnline",
				UITools.VIEW_SHAPE_PACK);
		button.setLabel("upload");
		button.label.align = GUILabel.CENTRE; button.label.layout = GUILabel.UNDER_COMPONENT;
		
		savePanel.placeComponent(button);



		*/

		toolPanel.show();

		setupMenuBar(applet);

	}

	static void setupGUIPattern(PApplet applet, ModalGUI gui) {

		
		
		
		if(GLOBAL.designToolbarPattern != null)
		GLOBAL.designToolbarPattern.show();
		
		if(GLOBAL.designToolbarBasic != null)
			GLOBAL.designToolbarBasic.hide();
		
		if(GLOBAL.designToolbarAdvanced!= null)
			GLOBAL.designToolbarAdvanced.hide();
		
		if(GLOBAL.previewWidget != null){
		GLOBAL.previewWidget.minimizeTogglePattern(null);
		GLOBAL.previewWidget.designItBtn.show();
		GLOBAL.previewWidget.makeItBtn.hide();
		
		}
		
		
		if(GLOBAL.designToolbarPattern != null)
			return;
		
		
		float button_width = 45;
		float button_height = 45;

		float posY = 10;
		float posX = 10;
		
		float panelHeight = SETTINGS.panelHeight;
		
		float panelWidth = SETTINGS.panelWidth;
		
		

	//	gui.reset();
		
	//	setupGUITabsAll(applet,gui);
		

		//panel for slice widget
		float slicePanleWidth = 100;
		float slicePanleHeight = 100;
		GLOBAL.slicePanel = new GUIPanel(GLOBAL.windowWidth - slicePanleWidth,
				0, slicePanleWidth, slicePanleHeight, gui);
		GLOBAL.slicePanel.invisible();
		gui.add(GLOBAL.slicePanel);



		/*
		sliderToggle = new GUIToggleSlide(GLOBAL.windowWidth - 100, 18,
				"3D Preview on", "off", gui);
		sliderToggle.isDown = true;
		sliderToggle.addActionListener(GLOBAL.uiTools, "render3dPreview", null);
		gui.add(sliderToggle);
		 */

		GUIPanelTabbed tabbedPanel = new GUIPanelTabbed((GLOBAL.windowWidth-panelWidth)/2, GLOBAL.windowHeight
				- panelHeight, (int) panelWidth, (int) panelHeight, gui);
		gui.add(tabbedPanel);
		GLOBAL.designToolbarPattern = tabbedPanel;

		//tools
		GUIPanel toolPanel = new GUIPanel((GLOBAL.windowWidth-panelWidth)/2,
				GLOBAL.windowHeight - panelHeight, panelWidth,
				(int) panelHeight, gui);
		tabbedPanel.addTabbedPanel(toolPanel, Localization.getString("tools"), gui);

		
		//save

		GUIButton button = new GUIButton(0, 0, button_width, button_height,
				"gui/save_pdf.png", null, gui);
		button.addActionListener(GLOBAL.uiTools, "savePDF",
				UITools.VIEW_SHAPE_PACK);
		button.setLabel(Localization.getString("save_pdf"));
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;
		toolPanel.placeComponent(button);

		button = new GUIButton(0, 0, button_width, button_height,
				"gui/print.png", null, gui);
		button.addActionListener(GLOBAL.uiTools, "print",
				UITools.VIEW_SHAPE_PACK);
		button.setLabel(Localization.getString("print"));
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;
		toolPanel.placeComponent(button);

		button = new GUIButton(0, 0, button_width, button_height,
				"gui/plot.png", null, gui);
		button.addActionListener(GLOBAL.uiTools, "saveCraftRobo",
				UITools.VIEW_SHAPE_PACK);
		button.setLabel(Localization.getString("plot"));
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;
		toolPanel.placeComponent(button);
		
		// materials
		GLOBAL.widgetMaterials =  new WidgetMaterials(0f, GLOBAL.windowHeight
				- panelHeight, (int) panelWidth, (int) panelHeight, gui);
		
		tabbedPanel.addTabbedPanel(GLOBAL.widgetMaterials, Localization.getString("materials"), gui);
		

	}

	public static void setupMenuBar(PApplet applet) {
		//----------------------------------------------------------------------------------------
		//MENU BAR
		//-----------------------------------------------------------------------------------------
		//this doesn't demonstrate best coding practice, just a simple method
		//create the MenuBar Object
		
		
		
		menuListen = new myMenuListener();
		myMenu = new MenuBar();
		MenuItem item = null;

		//create the top level button

		Menu File = new Menu(Localization.getString("file"));

		item = new MenuItem(Localization.getString("new"),new MenuShortcut('N',false));
		item.setActionCommand("deleteAllChairs");
		item.addActionListener(menuListen);
		File.add(item);

		item = new MenuItem(Localization.getString("save_chair"),new MenuShortcut('S',false));
		item.setActionCommand("saveChairToFileAuto");
		item.addActionListener(menuListen);
		File.add(item);

		item = new MenuItem(Localization.getString("save_chair_as"),new MenuShortcut('S',true));
		item.setActionCommand("saveChairToFile");
		item.addActionListener(menuListen);
		File.add(item);

		item = new MenuItem(Localization.getString("share_chair_online"));
		item.setActionCommand("ShareChairOnline");
		item.addActionListener(menuListen);
		File.add(item);

		Menu Export = new Menu(Localization.getString("export_pattern"));

		item = new MenuItem(Localization.getString("to_PDF"));
		item.setActionCommand("savePDF");
		item.addActionListener(menuListen);
		Export.add(item);
		
		item = new MenuItem(Localization.getString("to_SVG"));
		item.setActionCommand("savePattern");
		item.addActionListener(menuListen);
		Export.add(item);

		item = new MenuItem(Localization.getString("to_DXF"));
		item.setActionCommand("savePatternDXF");
		item.addActionListener(menuListen);
		Export.add(item);

		item = new MenuItem(Localization.getString("model_to_DXF"));
		item.setActionCommand("saveModelDxf");
		item.addActionListener(menuListen);
		Export.add(item);

		item = new MenuItem(Localization.getString("to_craftRobo"));
		item.setActionCommand("saveCraftRobo");
		item.setName(Integer.toString(UITools.DRAW_TOOL));
		item.addActionListener(menuListen);
		Export.add(item);

		item = new MenuItem(Localization.getString("to_PNG_preview"));
		item.setActionCommand("exportPreview");
		item.addActionListener(menuListen);
		Export.add(item);

		File.add(Export);

		item = new MenuItem(Localization.getString("open"),new MenuShortcut('O',false));
		item.setActionCommand("openChairFromFile");
		item.addActionListener(menuListen);
		File.add(item);
		/*
		item = new MenuItem("open template chair");
		item.setActionCommand("loadTemplateChair");
		item.addActionListener(menuListen);
		File.add(item);
		*/
		item = new MenuItem(Localization.getString("import_image"));
		item.setActionCommand("openEnvironmentFromFile");
		item.addActionListener(menuListen);
		File.add(item);

		item = new MenuItem(Localization.getString("import_SVG"));
		item.setActionCommand("importSVG");
		item.addActionListener(menuListen);
		File.add(item);

		item = new MenuItem(Localization.getString("print_preview"));
		item.setActionCommand("toggleView");
		item.addActionListener(menuListen);
		File.add(item);

		if (SETTINGS.DEVELOPER_MODE) {
			item = new MenuItem(Localization.getString("run_automated_actions"));
			item.setActionCommand("runAutomatedActions");
			item.addActionListener(menuListen);
			File.add(item);

		}

		//add the button to the menu
		myMenu.add(File);

		Menu Edit = new Menu(Localization.getString("edit"));

		
		item = new MenuItem(Localization.getString("copy") ,new MenuShortcut('C',false));
		item.setActionCommand("copyChair");
		item.addActionListener(menuListen);
		Edit.add(item);
		
		item = new MenuItem(Localization.getString("paste") ,new MenuShortcut('V',false));
		item.setActionCommand("pasteChair");
		item.addActionListener(menuListen);
		Edit.add(item);
		
		
		//create all the Menu Items and add the menuListener to check their state.
		item = new MenuItem(Localization.getString("undo") ,new MenuShortcut('Z',false));
		item.setActionCommand("undo");
		item.addActionListener(menuListen);
		Edit.add(item);

		//create all the Menu Items and add the menuListener to check their state.
		item = new MenuItem(Localization.getString("redo"),new MenuShortcut('Z',true));
		item.setActionCommand("redo");
		item.addActionListener(menuListen);
		Edit.add(item);

		item = new MenuItem(Localization.getString("screen_capture"));
		item.setActionCommand("captureScreen");
		item.addActionListener(menuListen);
		Edit.add(item);

		item = new MenuItem(Localization.getString("flip_chair"));
		item.setActionCommand("flipChair");
		item.addActionListener(menuListen);
		Edit.add(item);

		myMenu.add(Edit);

		Menu Tools = new Menu(Localization.getString("tools"));

		//create all the Menu Items and add the menuListener to check their state.

		item = new MenuItem(Localization.getString("select"));
		item.setActionCommand("selectTool");
		item.setName(Integer.toString(UITools.SELECT_TOOL));
		item.addActionListener(menuListen);
		Tools.add(item);

		item = new MenuItem(Localization.getString("draw"));
		item.setActionCommand("selectTool");
		item.setName(Integer.toString(UITools.DRAW_TOOL));
		item.addActionListener(menuListen);
		Tools.add(item);

		item = new MenuItem(Localization.getString("leg"));
		item.setActionCommand("selectTool");
		item.setName(Integer.toString(UITools.LEG_TOOL));
		item.addActionListener(menuListen);
		Tools.add(item);

		item = new MenuItem(Localization.getString("path"));
		item.setActionCommand("selectTool");
		item.setName(Integer.toString(UITools.DRAW_PATH_TOOL));
		item.addActionListener(menuListen);
		Tools.add(item);

		item = new MenuItem(Localization.getString("bezier"));
		item.setActionCommand("selectTool");
		item.setName(Integer.toString(UITools.SELECT_BEZIER_TOOL));
		item.addActionListener(menuListen);
		Tools.add(item);

		item = new MenuItem(Localization.getString("scale"));
		item.setActionCommand("selectTool");
		item.setName(Integer.toString(UITools.SCALE_TOOL));
		item.addActionListener(menuListen);
		Tools.add(item);

		item = new MenuItem(Localization.getString("measure"));
		item.setActionCommand("selectTool");
		item.setName(Integer.toString(UITools.MEASURE_TOOL));
		item.addActionListener(menuListen);
		Tools.add(item);

		Menu camera = new Menu(Localization.getString("camera"));

		item = new MenuItem(Localization.getString("zoom"));
		item.setActionCommand("selectTool");
		item.setName(Integer.toString(UITools.ZOOM_CAM_TOOL));
		item.addActionListener(menuListen);
		camera.add(item);

		item = new MenuItem(Localization.getString("pan"));
		item.setActionCommand("selectTool");
		item.setName(Integer.toString(UITools.MOVE_CAM_TOOL));
		item.addActionListener(menuListen);
		camera.add(item);

		item = new MenuItem(Localization.getString("rotate"));
		item.setActionCommand("selectTool");
		item.setName(Integer.toString(UITools.ROTATE_CAM_TOOL));
		item.addActionListener(menuListen);
		camera.add(item);

		Menu View = new Menu(Localization.getString("view"));

		item = new MenuItem(Localization.getString("front"));
		item.setActionCommand("camJumpFront");
		item.addActionListener(menuListen);
		View.add(item);

		item = new MenuItem(Localization.getString("side"));
		item.setActionCommand("camJumpSide");
		item.addActionListener(menuListen);
		View.add(item);

		item = new MenuItem(Localization.getString("top"));
		item.setActionCommand("camJumpTop");
		item.addActionListener(menuListen);
		View.add(item);

		item = new MenuItem(Localization.getString("isometric"));
		item.setActionCommand("camJumpIso");
		item.addActionListener(menuListen);
		View.add(item);

		camera.add(View);

		Tools.add(camera);

		myMenu.add(Tools);

		
		Menu layers = new Menu(Localization.getString("layers"));

		item = new MenuItem(Localization.getString("select_next"),new MenuShortcut(KeyEvent.VK_LEFT,false));
		item.setActionCommand("layersSelectNext");
		item.addActionListener(menuListen);
		layers.add(item);

		item = new MenuItem(Localization.getString("select_prev"),new MenuShortcut(KeyEvent.VK_RIGHT,false));
		item.setActionCommand("layersSelectPrev");
		item.addActionListener(menuListen);
		layers.add(item);
		myMenu.add(layers);

		
		Menu physics = new Menu(Localization.getString("physics"));

		item = new MenuItem(Localization.getString("play_pause"),new MenuShortcut('G',false));
		item.setActionCommand("physicsPlayPause");
		item.addActionListener(menuListen);
		physics.add(item);



		item = new MenuItem(Localization.getString("rewind"),new MenuShortcut('G',true));
		item.setActionCommand("physicsRewind");
		item.addActionListener(menuListen);
		physics.add(item);

		myMenu.add(physics);

		Menu Mode = new Menu(Localization.getString("mode"));

		item = new MenuItem(Localization.getString("basic"));
		item.setActionCommand("changeModeBasic");
		item.addActionListener(menuListen);
		Mode.add(item);

		item = new MenuItem(Localization.getString("expert"));
		item.setActionCommand("changeModeExpert");
		item.addActionListener(menuListen);
		Mode.add(item);
		
		
		item = new MenuItem(Localization.getString("make_it"),new MenuShortcut('M',false));
		item.setActionCommand("viewPattern");
		item.addActionListener(menuListen);
		Mode.add(item);
		
		item = new MenuItem(Localization.getString("build_it"),new MenuShortcut('B',false));
		item.setActionCommand("viewModel");
		item.addActionListener(menuListen);
		Mode.add(item);
		
		myMenu.add(Mode);
		
		
		
		
		

		Menu view = new Menu(Localization.getString("view"));

		item = new MenuItem(Localization.getString("snap_to_grid"),new MenuShortcut('I',true));
		item.setActionCommand("toggleGrid");
		item.addActionListener(menuListen);
		view.add(item);

		
		item = new MenuItem(Localization.getString("ergonomic_figure"),new MenuShortcut('E',false));
		item.setActionCommand("togglePerson");
		item.addActionListener(menuListen);
		view.add(item);
		
		item = new MenuItem(Localization.getString("floor"),new MenuShortcut('F',false));
		item.setActionCommand("toggleFloor");
		item.addActionListener(menuListen);
		view.add(item);
		
		myMenu.add(view);
		
		/*
		
		Menu Language = new Menu("Language");

		item= new MenuItem("English");
		item.setActionCommand("selectLanguage");
		item.setName(Integer.toString(UITools.LANGUAGE_ENG));
		item.addActionListener(menuListen);
		Language.add(item);
		
		item= new MenuItem("Japanese");
		item.setActionCommand("selectLanguage");
		item.setName(Integer.toString(UITools.LANGUAGE_JP));
		item.addActionListener(menuListen);
		Language.add(item);
		
		myMenu.add(Language);
		*/

		// Processing 4: Get Frame from PSurface
		if (GLOBAL.surface == null)
			return;
		Object nativeWindow = GLOBAL.surface.getNative();
		if (!(nativeWindow instanceof java.awt.Frame))
			return;
		java.awt.Frame frame = (java.awt.Frame) nativeWindow;
		//add the menu to the frame!
		frame.setMenuBar(myMenu);

		Menu Help = new Menu(Localization.getString("help"));

		item = new MenuItem(Localization.getString("about"));
		item.setActionCommand("selectTool");
		item.setName(Integer.toString(UITools.ZOOM_CAM_TOOL));
		item.addActionListener(menuListen);
		Help.add(item);

		/*
		item= new MenuItem("Show Instructions");
		item.setActionCommand("showInstructions");
		item.addActionListener(menuListen);
		Help.add(item);
		
		
		item= new MenuItem("Show Instruction Movie");
		item.setActionCommand("showInstructionMovie");
		item.addActionListener(menuListen);
		Help.add(item);
		*/

		if (!System.getProperty("java.version").startsWith("1.5")) {
			item = new MenuItem(Localization.getString("reference"));
			item.setActionCommand("showReferenceWebpage");
			item.addActionListener(menuListen);
			Help.add(item);

			item = new MenuItem(Localization.getString("forum"));
			item.setActionCommand("showForumWebpage");
			item.addActionListener(menuListen);
			Help.add(item);

			item = new MenuItem(Localization.getString("report_a_bug"));
			item.setActionCommand("showBugsWebpage");
			item.addActionListener(menuListen);
			Help.add(item);
		}

		myMenu.add(Help);

		// Note: Menu bar already set at line 2112, no need to set again

		//applet.println(myMenu);
	}

	private String keyString = "";
	private int lastKey;

	/*Shortcuts
	Ctrl + Z (Undo)
	Ctrl + S (Save Chair)
	Ctrl + P (Print pdf)
	
	
⌘S Save
⇧⌘ Save As

⌘O Open

⌘Z Undo
⇧⌘Z Redo

⌘G Physics play/pause
⇧⌘G Physics reset

⇧⌘I  Show/Hide Grid
⌘E  Show/Hide Ergonomic Figure
⌘F  Show/Hide Floor


⌘M  make it
⌘B  build it



//Tools
		d Draw Tool
		a Select Tool
		b Bezier Tool
		o Offset Path Tool
		p Path tool
		l Leg tools
		
		
		Path Tool
		Ctrl add remove
		` DEBUG
		
	*/
	public void keyPressed(char key, int keyCode) {
			
		if (GLOBAL.gui.hasFocus()) {
			return;

		}

		
		//LOGGER.info("key pressed" + keyString);
		if (lastKey != keyCode) {
			//keyString +=  KeyEvent.getKeyText(keyCode);
			if (keyCode == 157 || keyCode == 17)
				keyString += "Ctrl";
			else
				keyString += key;

			lastKey = keyCode;
		}
		//LOGGER.debug(keyString);

		//shortcuts
	

		
		//reload defaults
		if (keyString.equals("CtrlR")) {
		GLOBAL.sketchProperties.loadDefaults();
		}

		// use a key press so that it doesn't make a million files

		if (GLOBAL.gui.textfieldHasFocus() || GLOBAL.gui.hasFocus())
			return;

		// if (key == PApplet.CODED) {

		if (keyCode == PConstants.BACKSPACE) {
			if (GLOBAL.sketchChairs.getCurChair() != null) {
				//GLOBAL.sketchChairs.curChair.selectedPlanes.removeLastSketch();
				//	GLOBAL.sketchChairs.curChair.updateCollisionShape();
			}
		}

		if (keyCode == PConstants.BACKSPACE || keyCode == PConstants.DELETE) {
			if (GLOBAL.sketchChairs.getCurChair() != null)
				GLOBAL.sketchChairs.getCurChair().deleteSelectedShapes();
		}

		//  }


		//Tools
		if (key == 'd') {
			GLOBAL.uiTools.SketchTools.selectTool(SketchTools.DRAW_TOOL);
		}
		if (key == 'a') {
			GLOBAL.uiTools.SketchTools.selectTool(SketchTools.SELECT_TOOL);
		}
		if (key == 'b') {
			GLOBAL.uiTools.SketchTools.selectTool(SketchTools.SELECT_BEZIER_TOOL);
		}	
		if (key == 'o') {
			GLOBAL.uiTools.SketchTools.selectTool(SketchTools.DRAW_OFFSETPATH_TOOL);
		}	
		if (key == 'p') {
			GLOBAL.uiTools.SketchTools.selectTool(SketchTools.DRAW_PATH_TOOL);
		}
		if (key == 'l') {
			GLOBAL.uiTools.SketchTools.selectTool(SketchTools.LEG_TOOL);
		}
		

		
	
		if (key == ' ') {
			GLOBAL.autoRotate = false;

			
			if(GLOBAL.uiTools.currentView == UITools.VIEW_SHAPE_PACK){
				
				GLOBAL.shapePack.ZOOM = ( (float)GLOBAL.applet.height/GLOBAL.shapePack.materialHeight);
				GLOBAL.shapePack.CAM_OFFSET_X = (int) -(GLOBAL.shapePack.materialWidth/2.0f);
				GLOBAL.shapePack.CAM_OFFSET_Y = (int) -(GLOBAL.shapePack.materialHeight/2.0f);
				
			}else{
			if (GLOBAL.rotateModelsX != 0 || GLOBAL.rotateModelsY != 0) {
				GLOBAL.prevRotateModelsX = GLOBAL.rotateModelsX;
				GLOBAL.prevRotateModelsY = GLOBAL.rotateModelsY;

				GLOBAL.rotateModelsX = 0;
				GLOBAL.rotateModelsY = 0;
			} else {
				GLOBAL.rotateModelsX = GLOBAL.prevRotateModelsX;
				GLOBAL.rotateModelsY = GLOBAL.prevRotateModelsY;
			}
			}
		}



	

		
		if (key == '`') {
			

			if(GLOBAL.debugPickBuffer){
				SETTINGS.show_framerate = false;
				SETTINGS.DEBUG = false;
				GLOBAL.debugPickBuffer = false;
				return;
			}
			
			
			
			if(SETTINGS.DEBUG ){
				SETTINGS.show_framerate = true;
				SETTINGS.DEBUG = false;
				GLOBAL.debugPickBuffer = false;
				return;
			}
			
			if(SETTINGS.show_framerate){
				SETTINGS.show_framerate = false;
				SETTINGS.DEBUG = false;
				GLOBAL.debugPickBuffer = true;
				return;
			}

			
			if(!SETTINGS.DEBUG){
			SETTINGS.DEBUG = true;
			return;
			}
			
			
		}

	
		
		if (key == 't') {
			SETTINGS.TOUCH_SCREEN_MODE = !SETTINGS.TOUCH_SCREEN_MODE;

			if (SETTINGS.TOUCH_SCREEN_MODE)
				SETTINGS_SKETCH.select_dia = SETTINGS_SKETCH.select_dia_touch;
			else
				SETTINGS_SKETCH.select_dia = SETTINGS_SKETCH.select_dia_default;

		}

		if (key == 'b') {
			if (SETTINGS.REC) {
				SETTINGS.REC = false;
				//GLOBAL.mm.finish();
			} else {
				String currentDir = new File(".").getAbsolutePath();
				String path = currentDir + "\\recordings\\drawing####.mov";

				int id = 0;
				boolean nameFound = false;
				String location = "";

				while (!nameFound && id < 10000) {
					location = currentDir + "\\recordings\\sketch-" + id
							+ ".mov";
					File f = new File(location);

					if (!f.exists())
						nameFound = true;

					id++;
				}

				/*
				System.out.println("REC to: " +location);
					GLOBAL.mm = new MovieMaker(GLOBAL.applet, GLOBAL.windowWidth, GLOBAL.windowHeight, location,
				              15, MovieMaker.ANIMATION, MovieMaker.HIGH);
					
					SETTINGS.REC = true;
					*/

			}
		}

	
		if (key == 'o') {

			GLOBAL.person.printOrigins();
		}

		
		// if(key == 'm')
	}

	public void keyReleased(char key, int keyCode) {
		// TODO Auto-generated method stub
		keyString = "";
		lastKey = -1;
	}

	public void toggleButtons() {

		if (SETTINGS.EXHIBITION_MODE && false) {
			GLOBAL.gui.components.showAll();
			UI.savePdfButton.hide();
			UI.printRoboButton.hide();
			UI.printPdfButton.hide();

			if (GLOBAL.uiTools.currentView == GLOBAL.uiTools.VIEW_SHAPE_PACK) {
				GLOBAL.gui.components.hideAll();
				UI.patternButton.show();
				UI.patternButton.justMade = true;

				UI.savePdfButton.show();
				UI.printRoboButton.show();
				UI.printPdfButton.show();

			} else {

				if (GLOBAL.autoRotate) {
					GLOBAL.gui.components.hideAll();
					UI.rotateCamera.show();

				}

				if (this.saveChairToggle.isDown) {
					GLOBAL.gui.components.hideAll();
					UI.saveChairToggle.show();
					//UI.saveChairToggle.justMade = true;

					UI.saveChairFileButton.show();
					UI.shareChairButton.show();

				} else {
					UI.saveChairFileButton.hide();
					UI.shareChairButton.hide();

				}
			}
		}
	}

	public void updateMouse(int mouseX, int mouseY, int pmouseX, int pmouseY,
			boolean mousePressed, int mouseButton) {
		UI.mouseX = mouseX;
		UI.mouseY = mouseY;
		UI.pmouseX = pmouseX;
		UI.pmouseY = pmouseY;
		UI.mouseDown = mousePressed;
		UI.mouseButton = mouseButton;

	}

}
