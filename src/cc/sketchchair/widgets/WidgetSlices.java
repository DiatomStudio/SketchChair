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

import java.util.ArrayList;

import cc.sketchchair.core.CrossSliceSelection;
import cc.sketchchair.core.CrossSliceSelections;
import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.Localization;
import cc.sketchchair.core.SETTINGS;
import cc.sketchchair.core.UITools;
import cc.sketchchair.geometry.SlicePlane;

import ModalGUI.GUIButton;
import ModalGUI.GUIComponent;
import ModalGUI.GUIComponentSet;
import ModalGUI.GUIComponents;
import ModalGUI.GUIEvent;
import ModalGUI.GUILabel;
import ModalGUI.GUINumberfield;
import ModalGUI.GUIPanel;
import ModalGUI.GUIRadioBox;
import ModalGUI.GUISlider;
import ModalGUI.GUITextfield;
import ModalGUI.GUIToggle;
import ModalGUI.ModalGUI;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * GUI widget used for changing properties of slices. 
 * @author gregsaul
 *
 */
public class WidgetSlices extends GUIPanel {
	ModalGUI gui;
	public GUIPanel panel;
	GUIPanel scrollPanel;
	GUIPanel propertiesPanel;
	float XPos = 5;

	private String LANGUAGE;
	private GUIButton buttonAddPlane;
	GUIComponentSet radioSet = new GUIComponentSet();
	CrossSliceSelections sliceSelections = new CrossSliceSelections();
	CrossSliceSelection selectedSlice = null;
	private GUISlider sliderStart;
	private GUISlider sliderEnd;
	private GUISlider sliderSpacing;
	private int SliceCount = 0;
	GUIComponentSet toggleSetSlices = new GUIComponentSet();
	private int numberOfSlicesInPanel = -1;

	public WidgetSlices(float x, float y, float w, float h, ModalGUI gui) {
		super(x, y, w, h, gui);
		this.setController(gui);
		// TODO Auto-generated constructor stub
		this.gui = gui;
	}

	public void addNewSlicePlane(GUIEvent e) {

		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().addNewSlicePlane();

	}

	void addRow(CrossSliceSelection sliceSelection) {

		sliceSelections.add(sliceSelection);
		SliceCount++;

		String iconFile = "";

		if (sliceSelection.type == CrossSliceSelection.SINGLE_SLICE)
			iconFile = "gui/GUI_SLICE_SLICE_ICON.png";

		if (sliceSelection.type == CrossSliceSelection.PLANE_ON_EDGE
				|| sliceSelection.type == CrossSliceSelection.PLANE)
			iconFile = "gui/GUI_SLICE_SLAT_ICON.png";

		if (sliceSelection.type == CrossSliceSelection.SLICES)
			iconFile = "gui/GUI_SLICE_MULTISLICE_ICON.png";

		if (sliceSelection.type == CrossSliceSelection.LEG)
			iconFile = "gui/GUI_SLICE_LEG_ICON.png";

		//default
		if (iconFile == "")
			iconFile = "gui/GUI_SLICE_MULTISLICE_ICON.png";

		GUIToggle toggle = new GUIToggle(XPos, 30, 35, 35, iconFile, gui);
		toggle.setComponentSet(toggleSetSlices);
		toggle.addActionListener(this, "selectSlice", sliceSelections.size());

		toggle.setLabel(Integer.toString(SliceCount));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;

		this.scrollPanel.add(toggle);

		XPos += 35;

		//GUILabel labelToggle = new GUILabel((int) 40f, 0,"flip"); 
		//this.scrollPanel.add(labelToggle);

		// labelToggle = new GUILabel((int) 70f, 0,"constrain"); 
		// this.scrollPanel.add(labelToggle);

		/*
		GUINumberfield numFieldStart = new GUINumberfield(50, YPos, 15,15);
		this.scrollPanel.addComponent(numFieldStart);
		
		
		GUINumberfield numFieldEnd = new GUINumberfield(70, YPos, 15,15);
		this.scrollPanel.addComponent(numFieldEnd);
		
		GUINumberfield numFieldspace = new GUINumberfield(90, YPos, 15,15);
		this.scrollPanel.addComponent(numFieldspace);
		*/
		/*
		
		GUIToggle flipSide = new GUIToggle(40f, YPos, 15f,15f);
		flipSide.addActionListener(sliceSelection, "flipSide",
				UITools.LEG_TOOL);
		
		flipSide.setState(sliceSelection.flipSide);
		this.scrollPanel.add(flipSide);
		
		
		
		GUIToggle constrainToshape = new GUIToggle(70f, YPos, 15f,15f);
		constrainToshape.addActionListener(sliceSelection, "toggleConstrainToshape",
				UITools.LEG_TOOL);
		this.scrollPanel.add(constrainToshape);
		constrainToshape.setState(sliceSelection.cropToCurrentShape);
		//this.scrollPanel.add(labelButton);
		

		GUIToggle chnageMode = new GUIToggle(90f, YPos, 15f,15f);
		chnageMode.addActionListener(sliceSelection, "toggleSliceMode",
				UITools.LEG_TOOL);
		this.scrollPanel.add(chnageMode);
		//this.scrollPanel.add(labelButton);
		
		
			
		GUIButton editButton = new GUIButton(130f, YPos, 40f,15f, "edit");
		editButton.addActionListener(sliceSelection, "edit",UITools.SELECT_TOOL);
		scrollPanel.add(editButton);

		
		GUIButton buttonDeletePlane = new GUIButton(200f, YPos-3, "gui/GUI_PLANE_REMOVE_UP.png",
				"gui/GUI_PLANE_REMOVE_DOWN.png");
		buttonDeletePlane.addActionListener(sliceSelection, "destroy",UITools.LEG_TOOL);
		
		//buttonDeletePlane.addToolTip(GLOBAL.applet,"gui/GUI_PLANE_REMOVE_TOOLTIP", LANGUAGE);

		scrollPanel.add(buttonDeletePlane);
		*/

	}

	public void rebuild(CrossSliceSelections sliceSelections) {

		//check to see if we really need to rebuild?
		if (sliceSelections.size() != this.numberOfSlicesInPanel)
			this.numberOfSlicesInPanel = sliceSelections.size();
		else
			return;

		if (SETTINGS.EXPERT_MODE)
			this.scrollPanel.clear();

		this.sliceSelections.clear();

		XPos = 5;
		SliceCount = 0;

		toggleSetSlices = new GUIComponentSet();
		if (SETTINGS.EXPERT_MODE) {
			for (int i = 0; i < sliceSelections.l.size(); i++) {
				CrossSliceSelection creossSelection = (CrossSliceSelection) sliceSelections.l
						.get(i);
				if (!creossSelection.destroy) {
					this.addRow(creossSelection);
				}
			}
		}

		GLOBAL.gui.update();

	}

	public void rebuildChair(GUIEvent e) {
		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().build();
	}

	public void removeRow(CrossSliceSelection crossSliceSelection) {
		if (GLOBAL.sketchChairs.getCurChair() != null)
			this.rebuild(GLOBAL.sketchChairs.getCurChair().crossSliceSelections);

	}

	public void removeSliceCleanup(GUIEvent e) {
		this.scrollPanel.clear();

		numberOfSlicesInPanel = numberOfSlicesInPanel + 1 * 2;//force rebuild
		this.propertiesPanel.clear();

		if (GLOBAL.sketchChairs.getCurChair() != null)
			this.rebuild(GLOBAL.sketchChairs.getCurChair().crossSliceSelections);

	}

	
	public void editSlice(GUIEvent e) {
		editSlice(sliceSelections.get((int) (e.val )));
	}
	
	public void editSlice(CrossSliceSelection crossSliceSelection) {
		GLOBAL.sketchChairs.getCurChair().creossSelectionTempOver = crossSliceSelection;
		GLOBAL.sketchChairs.getCurChair().creossSelectionTempOver.edit(null);
		GLOBAL.sketchChairs.getCurChair().creossSelectionTempOver.tempSlice = true;
		GLOBAL.sketchChairs.getCurChair().crossSliceSelections.l.remove(crossSliceSelection);
	}
	
	
	public void selectSlice(GUIEvent e) {
		selectSlice(sliceSelections.get((int) (e.val - 1)));
	}


	public void selectSlice(CrossSliceSelection crossSliceSelection) {

		if (this.scrollPanel == null)
			return;

		for (int i = 0; i < this.scrollPanel.components.size(); i++) {
			GUIComponent component = (GUIComponent) this.scrollPanel.components
					.get(i);

			if (component.label.getLabelStr().startsWith(
					Integer.toString(((this.sliceSelections.l
							.indexOf(crossSliceSelection) + 1))))) {
				((GUIToggle) component).toggleDown();

			}
		}

		if (GLOBAL.sketchChairs.getCurChair() != null){
			GLOBAL.sketchChairs.getCurChair().crossSliceSelections.unselect();
			GLOBAL.sketchChairs.getCurChair().selectedPlanes.unselectAll();
			GLOBAL.sketchChairs.getCurChair().selectedPlanes.empty();
		}

		if (this.selectedSlice != null)
			this.selectedSlice.unselect();

		this.selectedSlice = crossSliceSelection;

		if (this.selectedSlice != null) {
			this.selectedSlice.select();

			this.setupPropertiesPanel(this.selectedSlice);
		}

		if (GLOBAL.sketchChairs.getCurChair() != null) {

			for (int i = 0; i < GLOBAL.sketchChairs.getCurChair().slicePlanesSlatSlices
					.size(); i++) {
				SlicePlane plane = GLOBAL.sketchChairs.getCurChair().slicePlanesSlatSlices
						.get(i);
				if (plane.getCrossSliceSelection() != null
						&& plane.getCrossSliceSelection().selected) {
					plane.select();
					GLOBAL.sketchChairs.getCurChair().selectedPlanes.add(plane);

				}
			}

		}

		
	}

	public void setStart(GUIEvent e) {

	}

	public void setupButtons() {

		LANGUAGE = "JP";

		float panelX = 200;
		this.panel = new GUIPanel(0, 0, 300f, 100f, this.controller);
		//this.panel.setLabel("slices");
		this.panel.hideSelectBar = true;
		this.hideSelectBar = true;
		this.panel.renderBorder = false;
		this.renderBorder = false;
		
		this.add(this.panel);

		this.scrollPanel = new GUIPanel(0, 0, 190f, 100f, this.gui);
		this.scrollPanel.setParentPanel(panel);
		this.scrollPanel.isDraggable = false;
		this.scrollPanel.useScroolBarX = true;
		this.scrollPanel.hideSelectBar = true;
		this.scrollPanel.useCanvas = true;
		this.scrollPanel.renderBorder = false;

		this.add(this.scrollPanel);

		this.propertiesPanel = new GUIPanel(190, 0, 900f, 100f, this.gui);
		this.propertiesPanel.setParentPanel(this);
		this.propertiesPanel.isDraggable = false;
		this.propertiesPanel.useScroolBarX = true;
		this.propertiesPanel.hideSelectBar = true;
		this.propertiesPanel.useCanvas = true;
		this.propertiesPanel.renderBorder = false;
		this.add(this.propertiesPanel);

		/*
		buttonAddPlane = new GUIButton(150, 0, "gui/GUI_PLANE_ADD_UP.png",
				"gui/GUI_PLANE_ADD_DOWN.png", this.controller);
		buttonAddPlane.addActionListener(this, "addNewSlicePlane",
				UITools.LEG_TOOL);

		panel.add(buttonAddPlane);
		
		
		buttonAddPlane.setParentPanel(this.panel);
*/
		if (GLOBAL.sketchChairs.getCurChair() != null)
			this.rebuild(GLOBAL.sketchChairs.getCurChair().crossSliceSelections);

	}

	void setupPropertiesPanel(CrossSliceSelection sliceSelection) {

		this.propertiesPanel.clear();


		float yPos = 25;
		GUIButton button = new GUIButton(10f, yPos, 45, 45,
				"gui/GUI_SLICE_EDIT_BUTTOM.png", null, this.controller);
		button.addActionListener(this, "editSlice", this.sliceSelections.l.indexOf(sliceSelection));
		button.setLabel( Localization.getString("edit"));
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;
		this.propertiesPanel.add(button);
		//yp+= 20;

		button = new GUIButton(50f, yPos, 45, 45,
				"gui/GUI_SLICE_DELETE_BUTTON.png", null, this.controller);
		button.addActionListener(sliceSelection, "destroy", UITools.LEG_TOOL);
		button.addActionListener(this, "removeSliceCleanup", null);

		button.setLabel(Localization.getString("delete"));
		button.label.align = GUILabel.CENTRE;
		button.label.layout = GUILabel.UNDER_COMPONENT;
		this.propertiesPanel.add(button);

		GUIToggle toggle = null;

		float xPos = 95;
		GUIComponentSet toggleSet = new GUIComponentSet();

		 xPos += 20;
		 yPos = 10;

		
		GUILabel label = new GUILabel(xPos, yPos-10,"cap\nstyle",this.controller);
		this.propertiesPanel.add(label);
		xPos += 35;

		toggle = new GUIToggle(xPos, yPos, "gui/GUI_SLICE_CAP_WING.png",this.controller);
		toggle.setComponentSet(radioSet);
		toggle.addActionListener(sliceSelection, "setCapType",
				CrossSliceSelection.CAP_CURVE);
		toggle.addActionListener(this, "rebuildChair",
				CrossSliceSelection.CAP_CURVE);
		
		/*toggle.setLabel("cap\ncurve");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;*/
		this.propertiesPanel.add(toggle);
		toggle.setState((sliceSelection.getCapType() == CrossSliceSelection.CAP_CURVE));

		xPos += 30;
		toggle = new GUIToggle(xPos, yPos, "gui/GUI_SLICE_CAP_SQUARE.png", this.controller);
		toggle.setComponentSet(radioSet);
		toggle.addActionListener(sliceSelection, "setCapType",
				CrossSliceSelection.CAP_BUTT);
		toggle.addActionListener(this, "rebuildChair",
				CrossSliceSelection.CAP_CURVE);
		/*toggle.setLabel("cap\nsquare");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;*/
		this.propertiesPanel.add(toggle);
		toggle.setState((sliceSelection.getCapType() == CrossSliceSelection.CAP_BUTT));

		xPos += 30;
		toggle = new GUIToggle(xPos, yPos, "gui/GUI_SLICE_CUTOFF.png", this.controller);
		toggle.setComponentSet(radioSet);
		toggle.addActionListener(sliceSelection, "setCapType",
				CrossSliceSelection.CAP_INSIDE);
		toggle.addActionListener(this, "rebuildChair",
				CrossSliceSelection.CAP_CURVE);

		/*toggle.setLabel("cap\ninside");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;*/
		this.propertiesPanel.add(toggle);
		toggle.setState((sliceSelection.getCapType() == CrossSliceSelection.CAP_INSIDE));

		xPos += 30;
		toggle = new GUIToggle(xPos, yPos, "gui/GUI_SLICE_CAP_ROUNDRECT.png", this.controller);
		toggle.setComponentSet(radioSet);
		toggle.addActionListener(sliceSelection, "setCapType",
				CrossSliceSelection.CAP_ROUND_SQUARE);
		toggle.addActionListener(this, "rebuildChair",
				CrossSliceSelection.CAP_ROUND_SQUARE);
		/*toggle.setLabel("corner\nradius");
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;*/
		this.propertiesPanel.add(toggle);
		toggle.setState((sliceSelection.getCapType() == CrossSliceSelection.CAP_ROUND_SQUARE));

		
		
		
		xPos -= 90;
		yPos = 60;
		
		toggle = new GUIToggle(xPos, yPos,"gui/GUI_MATERIAL_THROUGHALL_SLOT.png", this.controller);
		toggle.addActionListener(sliceSelection, "toggleConstrainToshape",
				UITools.LEG_TOOL);
		toggle.setLabel(Localization.getString("through_all"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		this.propertiesPanel.add(toggle);
		toggle.setState(!sliceSelection.cropToCurrentShape);
		xPos += 45;

		
		toggle = new GUIToggle(xPos, yPos, "gui/GUI_SLOT_ROTATE_DOWN.png","gui/GUI_SLOT_ROTATE_UP.png", this.controller);
		toggle.setController(this.controller);
		toggle.addActionListener(sliceSelection, "flipSide",
				CrossSliceSelection.CAP_INSIDE);
		toggle.addActionListener(this, "rebuildChair",
				CrossSliceSelection.CAP_CURVE);
		toggle.setLabel(Localization.getString("flip_slots"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		toggle.setState(sliceSelection.flipSide);
		this.propertiesPanel.add(toggle);

		xPos += 45;
		toggle = new GUIToggle(xPos, yPos, "gui/GUI_SLICE_SMOOTHED.png", this.controller);
		toggle.setController(this.controller);
		toggle.addActionListener(sliceSelection, "smooth",
				CrossSliceSelection.CAP_INSIDE);
		toggle.addActionListener(this, "rebuildChair",
				CrossSliceSelection.CAP_CURVE);
		toggle.setLabel((Localization.getString("smooth")));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		toggle.setState(sliceSelection.smooth);
		this.propertiesPanel.add(toggle);

		xPos += 45;
		toggle = new GUIToggle(xPos, yPos, "gui/GUI_SLICE_FOLLOW_EDGE.png", this.controller);
		toggle.setController(this.controller);
		toggle.addActionListener(sliceSelection, "generateFlushTops",
				CrossSliceSelection.CAP_INSIDE);
		toggle.addActionListener(this, "rebuildChair",
				CrossSliceSelection.CAP_CURVE);
		toggle.setLabel(Localization.getString("flush_tops"));
		toggle.label.align = GUILabel.CENTRE;
		toggle.label.layout = GUILabel.UNDER_COMPONENT;
		toggle.setState(sliceSelection.smooth);
		this.propertiesPanel.add(toggle);

		xPos += 45;
		
		if(sliceSelection.tieToLeg){
			
			toggle = new GUIToggle(xPos, yPos, "gui/GUI_SLICE_FOLLOW_EDGE.png", this.controller);
			toggle.setController(this.controller);
			
	
			toggle.addActionListener(this, "rebuildChair",
					CrossSliceSelection.CAP_CURVE);
		
			
			toggle.addActionListener(sliceSelection, "extendLegSliceToTopOfLeg",
					CrossSliceSelection.CAP_INSIDE);
			
			
			
			
			toggle.setLabel(Localization.getString("extend_to_top"));
			toggle.label.align = GUILabel.CENTRE;
			toggle.label.layout = GUILabel.UNDER_COMPONENT;
			toggle.setState(sliceSelection.extendLegSliceToTopOfLeg);
			this.propertiesPanel.add(toggle);
		}

		if (sliceSelection.type == CrossSliceSelection.SLICES
				|| sliceSelection.type == CrossSliceSelection.SINGLE_SLICE
				|| sliceSelection.type == CrossSliceSelection.SLATSLICES) {
			GUINumberfield slatHeight = new GUINumberfield(xPos, yPos, 30, 15,
					this.controller);
			slatHeight.addActionListener(sliceSelection, "slatHeight");
			String sHeight = Float.toString(sliceSelection.getSlatHeight());
			if (sliceSelection.getSlatHeight() == 0)
				sHeight = "";

			slatHeight.setText(sHeight);
			slatHeight.setLabel(Localization.getString("slice_height"));
			slatHeight.label.align = GUILabel.CENTRE;
			slatHeight.label.layout = GUILabel.UNDER_COMPONENT;
			this.propertiesPanel.add(slatHeight);
		}

		if (sliceSelection.type == CrossSliceSelection.SLATSLICES
				|| sliceSelection.type == CrossSliceSelection.PLANE_ON_EDGE) {

			GUINumberfield slatHeight = new GUINumberfield(230, 45, 60, 15,
					this.controller);
			slatHeight.addActionListener(sliceSelection, "teethCount");
			String sHeight = Float.toString(sliceSelection.teethCount);
			slatHeight.setText(sHeight);
			slatHeight.setLabel(Localization.getString("teeth_count"));
			this.propertiesPanel.add(slatHeight);

			GUINumberfield fingerTollerance = new GUINumberfield(230, 65, 60,
					15, this.controller);
			fingerTollerance.addActionListener(sliceSelection,
					"fingerTollerance");
			String fingetTol = Float.toString(sliceSelection.fingerTollerance);
			fingerTollerance.setText(fingetTol);
			fingerTollerance.setLabel(Localization.getString("finger_tollerance"));
			this.propertiesPanel.add(fingerTollerance);
		}
		//this.scrollPanel.add(labelButton);

		/*
			
			GUISlider sliderStart = new GUISlider(10, yp, 60, 0, 1);
			  sliderStart.setVal(sliceSelection.start);
			  sliderStart.setLabel("start");
			  sliderStart.addActionListener(sliceSelection , "start");
			this.propertiesPanel.add(sliderStart);

			yp+= 20;
			
			GUISlider sliderEnd = new GUISlider(10, yp, 60, 0, 1);
			  sliderEnd.setVal(sliceSelection.end);
			  sliderEnd.setLabel("end");
			  sliderEnd.addActionListener(sliceSelection , "end");
			this.propertiesPanel.add(sliderEnd);

			yp+= 20;
			
			GUISlider sliderSpacing = new GUISlider(10, yp, 60, -100, 100);
			  sliderSpacing.setVal(sliceSelection.spacing);
			  sliderSpacing.setLabel("spacing");
			  sliderSpacing.addActionListener(sliceSelection , "spacing");
			this.propertiesPanel.add(sliderSpacing);

			yp+= 20;
			
			
			
			GUISlider sliderboarderX = new GUISlider(10, yp, 60, 0, 100);
			sliderboarderX.setVal(sliceSelection.boarderX);
			sliderboarderX.setLabel("boarderX");
			sliderboarderX.addActionListener(sliceSelection , "boarderX");
			this.propertiesPanel.add(sliderboarderX);

			yp+= 20;
			
			GUISlider sliderboarderY = new GUISlider(10, yp, 60, 0, 100);
			sliderboarderY.setVal(sliceSelection.boarderX);
			sliderboarderY.setLabel("boarderY");
			sliderboarderY.addActionListener(sliceSelection , "boarderY");
			this.propertiesPanel.add(sliderboarderY);

			yp+= 20;
			
			*/
		/*
		GUIToggle flipSide = new GUIToggle(10f, yp, 15f,15f);
		flipSide.addActionListener(sliceSelection, "flipSide",
				UITools.LEG_TOOL);
		flipSide.setLabel("flip side");
		
		flipSide.setState(sliceSelection.flipSide);
		this.propertiesPanel.add(flipSide);
		yp+= 20;
		
		
		GUIToggle chnageMode = new GUIToggle(10f, yp, 15f,15f);
		chnageMode.addActionListener(sliceSelection, "toggleSliceMode",
				UITools.LEG_TOOL);
		this.propertiesPanel.add(chnageMode);
		//this.scrollPanel.add(labelButton);
		yp+= 20;
		*/

	}

	public void unselectAll() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void render(PGraphics g) {

		if (!this.visible)
			return;

		g.stroke(0);
		g.strokeWeight(1);
		g.line(getX(), getY() + 10, getX(), getY() + getHeight() - 10);

		g.line(getX()+this.scrollPanel.getWidth(), getY() + 10, getX()+this.scrollPanel.getWidth(), getY() + getHeight() - 10);

		
		
		
		
		super.render(g);
	}

}
