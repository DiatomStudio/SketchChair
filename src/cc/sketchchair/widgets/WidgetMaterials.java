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

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.SETTINGS;
import cc.sketchchair.core.UITools;

import ModalGUI.GUIButton;
import ModalGUI.GUILabel;
import ModalGUI.GUINumberfield;
import ModalGUI.GUIPanel;
import ModalGUI.GUISlider;
import ModalGUI.GUITextfield;
import ModalGUI.GUIToggle;
import ModalGUI.ModalGUI;
import processing.core.PApplet;

/**
 * GUI widget used for selecting material properties. 
 * @author gregsaul
 *
 */
public class WidgetMaterials extends GUIPanel{

	public GUIPanel panel;

	public GUINumberfield slotSizeTextfield = null;

	private GUISlider scaleSlider;

	private GUINumberfield cuttingBitSize;

	private GUIToggle toggleDogbones;

	public WidgetMaterials(float x, float y, float w, float h, ModalGUI gui) {
		super(x, y, w, h, gui);
		


	float toggleSize = 25;

		

		//GUILabel label = new GUILabel(15, 15, "cutting sheet size", gui);
		//add(label);


		
		
		
		
		
		
		
		
		
		
		
		label = new GUILabel(20, 30, "scale", gui);
		add(label);
		
		
		
		scaleSlider = new GUISlider(70, 30, 85, 1, 25, this.controller);
		scaleSlider.setParentPanel(this);
		scaleSlider.setVal(10);
		scaleSlider.setShowValLabel(true);
		scaleSlider.setFormatValLabel("1:%1$.0f");
		scaleSlider.setLabelValMultiplier(1.0f);
		scaleSlider.setEndImgs(GLOBAL.applet.loadImage("gui/SLIDER_DESIGN_SCALE_MIN.png"),GLOBAL.applet.loadImage("gui/SLIDER_DESIGN_SCALE_MAX.png"));
		add(scaleSlider);
		
		
		slotSizeTextfield = new GUINumberfield(70, 70,
				60, 15, gui);
		


		slotSizeTextfield.setText(
				Float.toString(SETTINGS.materialThickness));
		slotSizeTextfield.setLabel("mm (d)");

		add(slotSizeTextfield);
		slotSizeTextfield.setParentPanel(
				this);
		
		
		
		
		
		label = new GUILabel(250, 30, "width", gui);
		add(label);
		GUINumberfield numberField = new GUINumberfield(300, 30, 60, 15, gui);
		numberField.setLabel("mm (w)");
		numberField.setText(String.valueOf(GLOBAL.shapePack.materialWidth));
		numberField.addActionListener(GLOBAL.shapePack, "materialWidth");
		add(numberField);

		label = new GUILabel(250, 50, "height", gui);
		add(label);
		numberField = new GUINumberfield(300, 50, 60, 15, gui);
		numberField.setLabel("mm (h)");
		numberField.setText(String.valueOf(GLOBAL.shapePack.materialHeight));
		numberField.addActionListener(GLOBAL.shapePack, "materialHeight");
		add(numberField);
		
		
		
		
		/*
		scaleTextfield = new GUITextfield(160, 30, 30,
				15, gui);
		scaleTextfield
				.setText((int) (1 / SETTINGS.scale) + "");
		
		scaleTextfield.addActionListener(null,
				"mirrorPlanesToggle", UITools.LEG_TOOL);
		add(scaleTextfield);
		scaleTextfield.setParentPanel(this);
		
		scaleTextfield.setLabel("chair scale");
		GUILabel scalelabel = new GUILabel(145, 30, "1:", gui);
		add(scalelabel);
*/
		/*
		numberField = new GUINumberfield(150, 15, 60, 15, gui);
		numberField.setLabel("extendSlots");
		numberField.setText("0");
		numberField.addActionListener(GLOBAL.SketchGlobals, "extendSlots");
		materialsPanel.add(numberField);
		*/
		
		//label = new GUILabel(300, 15, "pattern features", gui);
		//add(label);

		GUIToggle toggle;
	
		toggleDogbones = new GUIToggle(500, 15, toggleSize, toggleSize, "gui/GUI_MATERIAL_DOGBONE_SLOT.png",gui);
		toggleDogbones.addActionListener(GLOBAL.shapePack, "addDogbones");
		toggleDogbones.setLabel("add dogbones");
		//toggle.label.align = GUILabel.CENTRE; toggle.label.layout = GUILabel;
		add(toggleDogbones);

		toggle = new GUIToggle(500, 40, toggleSize, toggleSize, "gui/GUI_MATERIAL_SEPERATE_SLOT.png",gui);
		toggle.addActionListener(GLOBAL.SketchGlobals, "seperate_slots");
		toggle.setLabel("seperate slots");
		//toggle.label.align = GUILabel.CENTRE; toggle.label.layout = GUILabel.UNDER_COMPONENT;
		add(toggle);

		toggle = new GUIToggle(500, 65, toggleSize, toggleSize, "gui/GUI_MATERIAL_BEVEL_SLOT.png",gui);
		toggle.addActionListener(GLOBAL.shapePack, "add_guide_divets");
		toggle.setLabel("is paper cut");
		//toggle.label.align = GUILabel.CENTRE; toggle.label.layout = GUILabel;
		add(toggle);

		cuttingBitSize = new GUINumberfield(650, 15, 30, 15, gui);
		cuttingBitSize.setLabel("bit size(mm)");
		cuttingBitSize.setText("5");
		cuttingBitSize.addActionListener(GLOBAL.shapePack, "inner_corner_radius");
		
		add(cuttingBitSize);
		
		//label = new GUILabel(450, 15, "packing options", gui);
		//add(label);
		
		toggle = new GUIToggle(650, 35, toggleSize, toggleSize, "gui/GUI_PACK_AUTO.png",gui);
		toggle.addActionListener(GLOBAL.shapePack, "autoPackPieces");
		toggle.setLabel("auto pack");
		//toggle.label.align = GUILabel.CENTRE; toggle.label.layout = GUILabel;
		add(toggle);
		
		 toggle = new GUIToggle(650, 65, toggleSize, toggleSize, "gui/GUI_PACK_LABELS.png",gui);
		 toggle.addActionListener(GLOBAL.shapePack, "addLabels");
	     toggle.toggleDown();
		 toggle.setLabel("add labels");
			//toggle.label.align = GUILabel.CENTRE; toggle.label.layout = GUILabel;
			add(toggle);
			
		GUIButton button = new GUIButton(800, 30, 60, 15,"Apply", gui);
		button.addActionListener(GLOBAL.uiTools, "applyMaterialSettings");
		add(button);
	}



	private void setPlaneWidth(float planeThickness) {
		// TODO Auto-generated method stub
	}

	

	public void setupGUI(PApplet applet, ModalGUI gui) {
		String LANGUAGE = "ENG";

		
		float panelX = 10;
		this.panel = new GUIPanel(GLOBAL.windowWidth - 240, 7, 220f, 70f, gui);
		gui.add(this.panel);

		this.panel.setLabel("materials");

		slotSizeTextfield = new GUINumberfield(panelX, 25, 60, 15, gui);
		

		slotSizeTextfield.setText(
				Float.toString(SETTINGS.DEFAULT_MATERIAL_WIDTH));
		slotSizeTextfield.setLabel("mm:   material width");
		
		panel.add(slotSizeTextfield);
		slotSizeTextfield.setParentPanel(this.panel);

		
		/*
		this.scaleTextfield = new GUITextfield(panelX, 45, 60, 15, gui);
		this.scaleTextfield.setText("10");
		this.scaleTextfield.setLabel("chair scale");
		this.scaleTextfield.addActionListener(this, "mirrorPlanesToggle",
				UITools.LEG_TOOL);
		//	this.scaleTextfield.addToolTip(GLOBAL.applet,
		//			"proButtons/GUI_SLICE_MIRROR_TOOLTIP", LANGUAGE);

		panel.add(this.scaleTextfield);
		
		this.scaleTextfield.setParentPanel(this.panel);
*/
	}

	public void update() {
		super.update();
		
		if (slotSizeTextfield== null)
			return;



				float planeThickness = slotSizeTextfield.getVal();
				SETTINGS.materialThickness = planeThickness;
				
				
				if (GLOBAL.sketchChairs.getCurChair() != null)
					GLOBAL.sketchChairs.getCurChair().setPlaneWidth(
							SETTINGS.materialThickness);

		
	
			float scale = 1;
			
				scale = (int)this.scaleSlider.getVal();
				SETTINGS.scale = 1 / scale;
				SETTINGS.pixels_per_mm = SETTINGS.pixels_per_mm_base
						/ SETTINGS.scale;


		GLOBAL.shapePack.scale = SETTINGS.scale / .1f;
		
		if(toggleDogbones.isDown)
			cuttingBitSize.show();
		else
			cuttingBitSize.hide();

	}

}
