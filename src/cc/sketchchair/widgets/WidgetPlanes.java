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
import cc.sketchchair.core.Localization;
import cc.sketchchair.core.MouseEventSK;
import cc.sketchchair.core.SETTINGS;
import cc.sketchchair.core.UITools;
import cc.sketchchair.geometry.SlicePlane;
import cc.sketchchair.geometry.SlicePlanes;
import ModalGUI.GUIButton;
import ModalGUI.GUIComponent;
import ModalGUI.GUIComponents;
import ModalGUI.GUIEvent;
import ModalGUI.GUILabel;
import ModalGUI.GUIPanel;
import ModalGUI.GUISlider;
import ModalGUI.GUITextfield;
import ModalGUI.GUIToggle;
import ModalGUI.ModalGUI;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * GUI widget used for changing layer properties.
 * @author gregsaul
 *
 */
public class WidgetPlanes extends GUIPanel {

	ModalGUI gui;
	public SlicePlanes planes = null;
	SlicePlane displayPlane = null;
	SlicePlane mouseOverPlane = null;
	GUIButton buttonAddPlane = null;

	GUIButton buttonDeletePlane = null;
	float offsetX = 650;
	float offsetY = 50;
	float minSelectHeight = 10;
	float scale = .2f;

	private float chairWidth;
	boolean resizing = false;
	private float chairDisplayWidth = 400;
	public GUISlider slider = null;
	private float lastWidth;
	private float chairScale = 1;
	private int selectWidth = 8;
	private GUIToggle mirrorPlaneToggle;
	private GUIButton selectAllPlanes;
	public boolean mirrorSelection = true;
	public GUISlider SlatSlider;
	GUISlider SlatSliderStart;
	public GUISlider SlatSliderEnd;
	GUISlider SlatSliderSpacing;
	private GUITextfield slotSizeTextfield;
	private GUITextfield scaleTextfield;
	private GUIButton toggleGuide;

	public WidgetPlanes(float x, float y, float w, float h, ModalGUI gui) {
		super(x, y, w, h, gui);
		this.gui = gui;
		this.renderBorder = false;

		// TODO Auto-generated constructor stub
	}

	private SlicePlane getMirrored(SlicePlane overPlane) {
		for (int i = 0; i < this.planes.getList().size(); i++) {
			SlicePlane curPlane = this.planes.getList().get(i);

			if (overPlane == curPlane) {
				int mirrorIndex = 0;

				mirrorIndex = this.planes.getList().size() - i;

				SlicePlane returnPlane = this.planes.getList().get(
						mirrorIndex - 1);

				if (returnPlane == overPlane)
					return null;
				else
					return returnPlane;

			}

		}

		return null;
	}

	SlicePlane getMouseOver(float mouseX, float mouseY) {

		if (this.planes == null)
			return null;

		float xOffset = 25;

		float plMaxY = planes.getMaxY();
		float plMinY = planes.getMinY();

		float plMaxX = planes.getMaxX();
		float plMinX = planes.getMinX();
		float plMinZ = planes.getMinZ();

		float chairW = (plMaxX - plMinX);
		float chairH = (plMaxY - plMinY);

		float largest = Math.max(chairW, chairH);
		float chairScale = this.chairDisplayWidth / largest;

		float centreX = ((plMaxX - plMinX) / 2) + plMinX;
		float centreY = ((plMaxY - plMinY) / 2) + plMinY;
		float maxProfileHeight = planes.getMaxProfileHeight();

		mouseOverPlane = null;
		float i = 0;
		for (SlicePlane plane : this.planes.getList()) {
			i++;
			float pX2 = 5;
			float selectBoxHeight = plane.profileHeight; 
			float planeMaxY = plane.getSketch().getMaxY();

			if(selectBoxHeight <= 5){
				selectBoxHeight = maxProfileHeight;
				planeMaxY = plMaxY;
			}
			
			

			float pX1 = (((i * pX2 * 2)))

			//float pX1 = ((plane.getPlane().z - plMinZ) * chairScale)
					+ this.chairDisplayWidth + this.getX() + xOffset;
			float pY1 = this.getY() + (this.getHeight() - 30)
					- ((plMaxY - planeMaxY) * chairScale);
			float pY2 = -(selectBoxHeight * chairScale);

			if (mouseX >= pX1 - this.selectWidth / 2
					&& mouseX <= pX1 + this.selectWidth / 2 && mouseY <= pY1
					&& mouseY >= pY1 + pY2) {
				//System.out.println("over");
				// plane.selected = true;
				mouseOverPlane = plane;
			}

		}
		return mouseOverPlane;

	}

	public void mirrorPlanesToggle(GUIEvent e) {
		this.mirrorSelection = !this.mirrorSelection;
	}

	public void mouseClicked(float mouseX, float mouseY) {

		if (mouseX >= offsetX && mouseX <= offsetX + 30 && mouseY >= offsetY
				&& mouseY <= offsetY + 130) {

			this.resizing = true;

		}

	}

	public void mouseDragged(int mouseX, int mouseY) {
		float deltaMouseY = (GLOBAL.uiTools.mouseX - GLOBAL.uiTools.pmouseX)
				* 1 / this.scale;

		// if()
		// if(this.resizing && GLOBAL.sketchChairs.curChair != null)
		// GLOBAL.sketchChairs.curChair.changeWidth(deltaMouseY);
	}

	@Override
	public void mouseEvent(MouseEventSK e) {

		if (!this.visible)
			return;

		super.mouseEvent(e);

		int mouseX = controller.applet.mouseX;
		int mouseY = controller.applet.mouseY;

		if (e.getAction() == MouseEventSK.PRESS) {
			this.mousePressed(mouseX, mouseY);

		} else if (e.getAction() == MouseEventSK.RELEASE && wasClicked
				&& isMouseOver()) {
			this.mouseClicked(mouseX, mouseY);
		}

		if (e.getAction() == MouseEventSK.RELEASE) {
			this.mouseReleased(mouseX, mouseY);
		}

	}

	public void mousePressed(float mouseX, float mouseY) {

	}

	public void mouseReleased(float mouseX, float mouseY) {

		SlicePlane overPlane = this.getMouseOver(mouseX, mouseY);
		planeClickedOn(overPlane);
	}
	
	
	
	public void planeClickedOn(SlicePlane plane) {
		
		
		SlicePlane overPlane = plane;
		SlicePlane mirroredPlane = null;

		if (overPlane != null) {
			
			if (this.mirrorSelection)
				mirroredPlane = this.getMirrored(overPlane);

			if (GLOBAL.uiTools.keyPressed
					&& (GLOBAL.uiTools.keyCode == PConstants.CONTROL || GLOBAL.uiTools.keyCode == 157)) {
				if (GLOBAL.sketchChairs.getCurChair().selectedPlanes.getList()
						.contains(overPlane)) {
					GLOBAL.sketchChairs.getCurChair().selectedPlanes.getList()
							.remove(overPlane);
				} else {
					GLOBAL.sketchChairs.getCurChair().selectedPlanes
							.add(overPlane);
					overPlane.setSelected(true);
				}

				if (mirroredPlane != null) {
					if (GLOBAL.sketchChairs.getCurChair().selectedPlanes
							.getList().contains(mirroredPlane)) {
						GLOBAL.sketchChairs.getCurChair().selectedPlanes
								.getList().remove(mirroredPlane);
					} else {
						GLOBAL.sketchChairs.getCurChair().selectedPlanes
								.add(mirroredPlane);
						mirroredPlane.setSelected(true);
					}
				}

			} else {
				GLOBAL.sketchChairs.getCurChair().slicePlanesSlatSlices.unselectAll();
				GLOBAL.sketchChairs.getCurChair().selectedPlanes.unselectAll();
				GLOBAL.sketchChairs.getCurChair().selectedPlanes.empty();
				GLOBAL.sketchChairs.getCurChair().selectedPlanes.add(overPlane);
				
				
				overPlane.setSelected(true);
				//GLOBAL.sketchChairs.getCurChair().selectedPlanes
				//.add(overPlane);
				if (mirroredPlane != null) {
					GLOBAL.sketchChairs.getCurChair().selectedPlanes
							.add(mirroredPlane);
					mirroredPlane.setSelected(true);

				}
			}

		}

		if (this.resizing && GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().updateCollisionShape();

		GLOBAL.sketchChairs.getCurChair().selectedPlanes.selectAll();
		
		GLOBAL.previewWidget.rebuildPatternPreview();
		this.resizing = false;

	}
	
	

	public void render(PGraphics g) {

		super.render(g);

		if (this.minimized || !this.visible)
			return;

		update();

		g.fill(0);

		if (this.planes == null)
			return;

		float plMaxY = planes.getMaxY();
		float plMinY = planes.getMinY();
		float plMaxX = planes.getMaxX();
		float plMinX = planes.getMinX();
		float plMinZ = planes.getMinZ();

		float chairW = (plMaxX - plMinX);
		float chairH = (plMaxY - plMinY);

		float largest = Math.max(chairW, chairH);
		this.chairDisplayWidth = 45;
		this.chairScale = this.chairDisplayWidth / largest;
		float centreX = ((plMaxX - plMinX) / 2) + plMinX;
		float centreY = ((plMaxY - plMinY) / 2) + plMinY;

		float maxProfileHeight = planes.getMaxProfileHeight();
		// System.out.println(centreX);
		float xOffset = 25;
		int i = 0;
		for (SlicePlane plane : this.planes.getList()) {
			i++;
			
			float selectBoxHeight = plane.profileHeight;
			float planeMaxY = plane.getSketch().getMaxY();
			
			g.strokeWeight(1);
			g.noFill();

			if (plane.guide) {
				if (plane.isSelected())
					g.stroke(10, 10, 20);
				else
					g.stroke(100, 100, 200);
			} else {
				if (plane.isSelected())
					g.stroke(20, 20, 20);
				else
					g.stroke(200, 200, 200);
			}
			
			if(selectBoxHeight <= 5){
				if (plane.isSelected())
					g.stroke(100, 100, 200);
				else
					g.stroke(200, 200, 255);
				
				selectBoxHeight = maxProfileHeight;
				planeMaxY = plMaxY;
			}
				
			g.strokeWeight(2);

			float pX2 = 5;

			float pX1 = (((i * pX2 * 2))) + this.chairDisplayWidth
					+ this.getX() + xOffset;
			float pY1 = this.getY() + (this.getHeight() - 30)
					- ((plMaxY - planeMaxY) * chairScale);
			float pY2 = -(selectBoxHeight * chairScale);

			g.rect(pX1, pY1, pX2, pY2);
		}

		if (displayPlane == null)
			return;

		g.pushMatrix();
		//
		// g.translate(((GLOBAL.windowWidth-plMaxX)*scale) +
		// (offsetX-chairWidth),offsetY);
		g.translate(this.getX() + 15, this.getY() + (this.getHeight() - 30));

		g.scale(chairScale);
		g.translate(-plMinX, -plMaxY);
		// g.scale(chairScale);
		// g.translate(100,100);
		if (mouseOverPlane == null)
			displayPlane.renderSilhouette(g);
		else
			mouseOverPlane.renderSilhouette(g);

		g.popMatrix();
	}

	public void selectAllPlanes(GUIEvent e) {
		if (GLOBAL.sketchChairs.getCurChair() != null) {
			GLOBAL.sketchChairs.getCurChair().selectedPlanes.empty();

			GLOBAL.sketchChairs.getCurChair().selectedPlanes
					.add(GLOBAL.sketchChairs.getCurChair().slicePlanesSlatSlices);
			GLOBAL.sketchChairs.getCurChair().selectedPlanes
					.add(GLOBAL.sketchChairs.getCurChair().getSlicePlanesY());

		}
	}

	@Override
	public void setup() {
		String LANGUAGE = "ENG";
		float panelX = 200;
		this.clear();
		this.setContentPosition(250, 25);// = 200;

		buttonAddPlane = new GUIButton(panelX, 15,
				"gui/GUI_LAYER_NEWLAYER_BUTTON.png", this.controller);
		buttonAddPlane.addActionListener(GLOBAL.uiTools, "addPlane",
				UITools.LEG_TOOL);
		buttonAddPlane.setLabel(Localization.getString("new"));
		buttonAddPlane.label.align = GUILabel.CENTRE;
		buttonAddPlane.label.layout = GUILabel.UNDER_COMPONENT;
		placeComponent(buttonAddPlane);
		buttonAddPlane.setParentPanel(this);

		buttonDeletePlane = new GUIButton(panelX, 15,
				"gui/GUI_LAYER_DELETE_BUTTON.png", this.controller);
		buttonDeletePlane.addActionListener(GLOBAL.uiTools,
				"removeSelectedPlanes", UITools.LEG_TOOL);
		buttonDeletePlane.setLabel(Localization.getString("delete"));
		buttonDeletePlane.label.align = GUILabel.CENTRE;
		buttonDeletePlane.label.layout = GUILabel.UNDER_COMPONENT;

		super.placeComponent(buttonDeletePlane);
		buttonDeletePlane.setParentPanel(this);

		this.mirrorPlaneToggle = new GUIToggle(200, 50,
				"gui/GUI_LAYER_MIRROR_BUTTON.png", this.controller);
		mirrorPlaneToggle.addActionListener(this, "mirrorPlanesToggle",
				UITools.LEG_TOOL);
		mirrorPlaneToggle.setLabel(Localization.getString("mirror"));
		mirrorPlaneToggle.label.align = GUILabel.CENTRE;
		mirrorPlaneToggle.label.layout = GUILabel.UNDER_COMPONENT;

		//this.mirrorPlaneToggle.addToolTip(GLOBAL.applet,
		//		"GUI_SLICE_MIRROR_TOOLTIP", LANGUAGE);

		super.placeComponent(this.mirrorPlaneToggle);
		this.mirrorPlaneToggle.setParentPanel(this);
		this.mirrorPlaneToggle.setState(true);

		this.selectAllPlanes = new GUIButton(200, 70,
				"gui/GUI_LAYER_SELECTALL_BUTTON.png", this.controller);
		this.selectAllPlanes.addActionListener(this, "selectAllPlanes",
				UITools.LEG_TOOL);
		this.selectAllPlanes.setLabel(Localization.getString("all"));
		this.selectAllPlanes.label.align = GUILabel.CENTRE;
		this.selectAllPlanes.label.layout = GUILabel.UNDER_COMPONENT;
		///this.selectAllPlanes.addToolTip(GLOBAL.applet,
		//		"proButtons/GUI_SLICE_SELECT_ALL_TOOLTIP", LANGUAGE);

		super.placeComponent(this.selectAllPlanes);
		this.selectAllPlanes.setParentPanel(this);

		this.toggleGuide = new GUIButton(230, 15,
				"gui/GUI_LAYER_GUIDELAYER_BUTTON.png", this.controller);
		this.toggleGuide.addActionListener(this, "toggleGuide",
				UITools.LEG_TOOL);
		toggleGuide.setLabel(Localization.getString("guide"));
		toggleGuide.label.align = GUILabel.CENTRE;
		toggleGuide.label.layout = GUILabel.UNDER_COMPONENT;

		///this.selectAllPlanes.addToolTip(GLOBAL.applet,
		//		"proButtons/GUI_SLICE_SELECT_ALL_TOOLTIP", LANGUAGE);

		super.placeComponent(this.toggleGuide);
		this.toggleGuide.setParentPanel(this);

		slider = new GUISlider(550, 40, 150, 0, 2000, this.controller);
		slider.setParentPanel(this);
		slider.setVal(SETTINGS.chair_width);
		slider.setLabel(Localization.getString("chair_width"));
		slider.label.align = GUILabel.CENTRE;
		slider.label.layout = GUILabel.UNDER_COMPONENT;
		slider.setShowValLabel(true);
		slider.setLabelValMultiplier(1.0f);
		
		
		
		
		//TODO removed this 
		/*
		GLOBAL.widgetMaterials.setSlotSizeTextfield(new GUITextfield(15, 70,
				60, 15, gui));
		GLOBAL.widgetMaterials.getSlotSizeTextfield().addActionListener(null,
				"mirrorPlanesToggle", UITools.LEG_TOOL);

		GLOBAL.widgetMaterials.getSlotSizeTextfield().setText(
				Float.toString(SETTINGS.materialThickness));
		GLOBAL.widgetMaterials.getSlotSizeTextfield().setLabel("mm (d)");

	
		materialsPanel.add(GLOBAL.widgetMaterials.getSlotSizeTextfield());
		GLOBAL.widgetMaterials.getSlotSizeTextfield().setParentPanel(
				materialsPanel);
		*/
		super.add(slider);
		slider.addNumberField();

		this.hideSelectBar = true;

	}

	public void setupGUI(PApplet applet, ModalGUI gui) {

	}

	public void toggleGuide(GUIEvent e) {
		if (GLOBAL.sketchChairs.getCurChair() != null) {
			GLOBAL.sketchChairs.getCurChair().selectedPlanes.toggleGuide();
		}
	}

	@Override
	public void update() {
		super.update();

		if (slider == null)
			return;

		if (GLOBAL.sketchChairs.getCurChair() != null) {
			// GLOBAL.sketchChairs.curChair.startCoverPercent =
			// this.SlatSliderStart.getVal();
			// GLOBAL.sketchChairs.curChair.endCoverPercent =
			// this.SlatSliderEnd.getVal();
			// GLOBAL.sketchChairs.curChair.slatSpacingX =
			// this.SlatSliderSpacing.getVal();

		}
		if (this.lastWidth != slider.getVal()) {
			if (GLOBAL.sketchChairs.getCurChair() != null) {

				GLOBAL.sketchChairs.getCurChair()
						.setWidth(slider.getVal());
				GLOBAL.sketchChairs.getCurChair().rebuildLength = true;
				GLOBAL.sketchChairs.getCurChair().buildLen();
			}
			this.lastWidth = slider.getVal();
		} else {
			if (GLOBAL.sketchChairs.getCurChair() != null)
				slider.setVal(GLOBAL.sketchChairs.getCurChair().getWidth());
			else
				slider.setVal(0);
		}

		if (this.planes != null && GLOBAL.sketchChairs.getCurChair() != null) {

			this.chairWidth = GLOBAL.sketchChairs.getCurChair().getWidth();
			// buttonAddPlane.setPos((chairWidth/2*scale) + offsetX , 10 +
			// offsetY);
			// buttonDeletePlane.setPos((chairWidth/2*scale) + + offsetX , 30 +
			// offsetY);
			displayPlane = GLOBAL.sketchChairs.getCurChair().selectedPlanes
					.getFirst();
		}

		getMouseOver(GLOBAL.uiTools.mouseX, GLOBAL.uiTools.mouseY);
	}

	public void unselectAll() {
		GLOBAL.sketchChairs.getCurChair().slicePlanesSlatSlices.unselectAll();
		GLOBAL.sketchChairs.getCurChair().selectedPlanes.unselectAll();
		GLOBAL.sketchChairs.getCurChair().selectedPlanes.empty();		
	}

}
