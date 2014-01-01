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

import processing.core.PGraphics;
import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.Localization;
import cc.sketchchair.core.UITools;
import cc.sketchchair.sketch.SketchShape;
import cc.sketchchair.sketch.SketchSpline;
import cc.sketchchair.sketch.SketchTools;
import ModalGUI.GUIButton;
import ModalGUI.GUIColourPicker;
import ModalGUI.GUIComponentSet;
import ModalGUI.GUILabel;
import ModalGUI.GUIPanel;
import ModalGUI.GUISlider;
import ModalGUI.GUIToggle;
import ModalGUI.ModalGUI;

/**
 * GUI tool panel
 * @author gregsaul
 *
 */
public class WidgetToolSettings extends GUIPanel {
	public GUIPanel panel;
	private ModalGUI gui;

	public WidgetToolSettings(float x, float y, float w, float h, ModalGUI gui) {
		super(x, y, w, h, gui);
		this.setController(gui);
		this.gui = gui;
		renderBorder = false;
		hideSelectBar = true;
		setupPanel();
		hide();
	}

	@Override
	public void update() {
		boolean show = false;

		
		//See what tool we have selected
		switch (GLOBAL.uiTools.SketchTools.getCurrentTool()) {
		case SketchTools.DRAW_TOOL:
			show = true;
			break;
		case SketchTools.DRAW_OFFSETPATH_TOOL:
			show = true;
			break;

		}
		
		//Go through all selected shapes and see if we have settings for them
		if(GLOBAL.sketchChairs.getCurChair() != null){
			for(int i = 0 ; i < GLOBAL.sketchChairs.getCurChair().selectedPlanes.count(); i++){
				SketchShape selectedShape = GLOBAL.sketchChairs.getCurChair().selectedPlanes.getSelectedShape();
				
				if(selectedShape != null && selectedShape instanceof SketchSpline)
					show = true;
			}
		}
		

		if (show)
			show();
		else
			hide();

		super.update();
	}

	void toolSelected(int tool) {

	}

	void shapeSelected(SketchShape shape) {

	}

	//at the moment all settings are the same for tools and paths this might change in the near future 
	public void setupPanel() {
		int yPos = 5;
		
		

		
		
		yPos = 32;
		GUISlider brushSize = new GUISlider(20, yPos, 150, 1, 100, gui);
		brushSize.setLabel(Localization.getString("brush_size"));
		brushSize.addActionListener(GLOBAL.uiTools, "setBrushDia",
				UITools.SCALE_TOOL);
		brushSize.setVal(GLOBAL.uiTools.SketchTools.brush_dia);
		brushSize.label.align = GUILabel.LEFT;
		brushSize.label.layout = GUILabel.UNDER_COMPONENT;
		brushSize.setEndImgs(GLOBAL.applet.loadImage("gui/SLIDER_BRUSH_SIZE_SMALL.png"),GLOBAL.applet.loadImage("gui/SLIDER_BRUSH_SIZE_BIG.png"));
		brushSize.setShowValLabel(false);
		brushSize.setLabelValMultiplier(2.0f);
		this.add(brushSize);
		brushSize.addNumberField();

		/*
		GUIColourPicker colourPicker = new GUIColourPicker(20, 20, gui);
		colourPicker.setLabel("chair colour");
		colourPicker.addActionListener(GLOBAL.uiTools, "setChairColour");
		colourPicker.label.align = GUILabel.LEFT;
		colourPicker.label.layout = GUILabel.UNDER_COMPONENT;
		add(colourPicker);
		*/

		//end caps

		 yPos = 65;
		 float xPos = 20;
		GUIComponentSet toggleSetCaps = new GUIComponentSet();

		
		GUILabel label = new GUILabel(xPos-2, yPos-16, "cap\nstyle",gui);
		this.add(label);
		
		xPos+=30;
		GUIToggle toggle = new GUIToggle(xPos, yPos, "gui/GUI_SHAPE_CAP_ROUND.png",
				gui);
		toggle.setComponentSet(toggleSetCaps);
		toggle.addActionListener(GLOBAL.uiTools.SketchTools, "selectCap",
				SketchSpline.CAP_ROUND);
		/*toggle.setLabel("round");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;*/
		if(GLOBAL.uiTools.SketchTools.getCap() == SketchSpline.CAP_ROUND)
			toggle.toggleDown();
		
		add(toggle);

		xPos+=30;
		toggle = new GUIToggle(xPos, yPos, "gui/GUI_SHAPE_CAP_BUT.png", gui);
		toggle.setComponentSet(toggleSetCaps);
		toggle.addActionListener(GLOBAL.uiTools.SketchTools, "selectCap",
				SketchSpline.CAP_BUTT);
		/*toggle.setLabel("butt");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;*/
		if(GLOBAL.uiTools.SketchTools.getCap() == SketchSpline.CAP_BUTT)
			toggle.toggleDown();
		add(toggle);
		
		xPos+=30;
		toggle = new GUIToggle(xPos, yPos, "gui/GUI_SHAPE_CAP_PARALLEL.png", gui);
		toggle.setComponentSet(toggleSetCaps);
		toggle.addActionListener(GLOBAL.uiTools.SketchTools, "selectCap",
				SketchSpline.CAP_PARRALEL);
		/*toggle.setLabel("parallel");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;*/
		if(GLOBAL.uiTools.SketchTools.getCap() == SketchSpline.CAP_PARRALEL)
			toggle.toggleDown();
		add(toggle);

		
		xPos+=85;

		
		 label = new GUILabel(xPos-35, yPos-14, "cut\nshape",gui);
		this.add(label);
		
		
		toggle = new GUIToggle(xPos, yPos,
				"gui/GUI_SHAPE_CUT.png", gui);
		toggle.addActionListener(GLOBAL.uiTools.SketchTools, "toggleUnion",
				SketchSpline.CAP_PARRALEL);
	
		add(toggle);
		
		
		
		
	}

	@Override
	public void render(PGraphics g) {

		if (!this.visible)
			return;

		g.stroke(0);
		g.strokeWeight(1);
		g.line(getX(), getY() + 10, getX(), getY() + getHeight() - 10);

		super.render(g);
	}

}
