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
package ModalGUI;

import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PGraphics;
import cc.sketchchair.core.KeyEventSK;
import cc.sketchchair.core.MouseEventSK;
import cc.sketchchair.core.MouseWheelEventSK;

public class GUIPanelTabbed extends GUIComponent {

	GUIComponents panels = new GUIComponents();

	private GUIPanel basePanel;
	GUIPanel topPanel = null;
	GUIComponentSet toggleSet = new GUIComponentSet();

	float lastTabX = 0;

	public GUIPanelTabbed(float xPos, float yPos, int w, int h, ModalGUI c) {
		this.setController(c);
		this.setPos(xPos, yPos);
		this.setSize(w, h);
		this.visible = true;

		setBasePanel(new GUIPanel(xPos, yPos, w, h, this.controller));
	}

	public void addTabbedPanel(GUIPanel panel) {
	    GUIToggle toggle = new GUIToggle(lastTabX, -32f,
				(panel.tittle.length() * 18)+45, 32f, panel.tittle, toggleSet,
				this.controller);
	    toggle.cornerRad = 3f;
		
		toggle.addActionListener(this, "clickedTab", this.panels.size());
		toggle.label.layout = GUILabel.CENTRE_OF_COMPONENT;

		toggle.setStrokeColourDown(function.color(1, 1, 1));
		toggle.setStrokeColourOver(function.color(180, 180, 180));
		toggle.setStrokeColour(function.color(147, 147, 147));

		toggle.setFillColourDown(function.color(249, 249, 249));
		toggle.setFillColour(function.color(91, 91, 91));
		toggle.setFillColourOver(function.color(200, 200, 200));

		
		if(panel.tabUpImg != null)
			toggle.img_up = panel.tabDownImg;
		
		if(panel.tabDownImg != null)
			toggle.img_down = panel.tabUpImg;
		
		
		toggle.label.textSize = 12;
		toggle.isTab = true;

		lastTabX += ((panel.tittle.length() * 18) + 1+45);
		
		panel.setFillColour(function.color(249, 249, 249));
		panel.setStrokeColour(function.color(147, 147, 147));
		getBasePanel().add(panel);
		getBasePanel().add(toggle);
		panel.setController(this.controller);

		panel.hideSelectBar = true;
		panel.hideMinimize = true;

		this.panels.add(panel);

		panel.hide();

		if (this.panels.size() == 1) {
			panel.show();
			toggle.toggle();
		}

	}

	public void addTabbedPanel(GUIPanel panel, String tittle, ModalGUI c) {
		this.setController(c);
		panel.setTittle(tittle);
		this.addTabbedPanel(panel);
		panel.setPos(0, 0);
	}
	
	
	public void addTabbedPanel(GUIPanel panel, String tittle,
			String _upImgSrc, String _downImgSrc, ModalGUI c) {
		this.setController(c);
		panel.setTittle(tittle);
		panel.tabUpImg = c.applet.loadImage(_upImgSrc);
		panel.tabDownImg = c.applet.loadImage(_downImgSrc);
		
		this.addTabbedPanel(panel);
		panel.setPos(0, 0);		
	}
	
	
	

	public void clickedTab(GUIEvent e) {
		int index = (int) e.val;
		this.panels.hideAll();
		if (index <= this.panels.size()) {
			topPanel = (GUIPanel) this.panels.get(index);
			topPanel.show();
		}
	}
	
	public void selectTab(String string) {
		for(int i = 0; i < getBasePanel().components.size();i++){
			GUIComponent component = (GUIComponent)getBasePanel().components.get(i);
			
			if(component instanceof GUIToggle &&  ((GUIToggle)(component)).label.getLabelStr().startsWith(string)){
				((GUIToggle)(component)).fireEventNotification(this, "Clicked");
				((GUIToggle)(component)).toggleDown();
			}
			
		}
	}

	@Override
	public boolean hasFocus() {
		if (this.hasFocus || this.panels.hasFocus())
			return true;
		else
			return false;

	}

	@Override
	public boolean isMouseOver() {
		int mouseX = controller.applet.mouseX;
		int mouseY = controller.applet.mouseY;

		if (this.getBasePanel().isMouseOver())
			return true;

		return false;

	}

	@Override
	public void keyEvent(KeyEventSK theKeyEvent) {
		this.panels.keyEvent(theKeyEvent);
	}

	
	
	@Override
	public void mouseWheelMoved(MouseWheelEventSK e) {
		this.getBasePanel().mouseWheelMoved(e);
		this.panels.mouseWheelMoved(e);

	}
	@Override
	public void mouseEvent(MouseEventSK e) {
		if (!this.visible)
			return ;
		
		this.getBasePanel().mouseEvent(e);
		this.panels.mouseEvent(e);
	}

	@Override
	public void render(PGraphics g) {
		

		
		if (!this.visible)
			return;
		
		if (this.getBasePanel() != null)
			this.getBasePanel().render(g);
		
		if (getBasePanel() != null) 
			this.panels.render(g);
		
		
		for(int i = 0; i < getBasePanel().components.size();i++){
			GUIComponent component = (GUIComponent)getBasePanel().components.get(i);
			
			if(component instanceof GUIToggle && ((GUIToggle) component).isTab){
				component.render(g);	
				
				
			}
			}
		//Render on update optimization
		if(controller.renderOnUpdate && !reRender){return;}
		if(controller.renderOnUpdate)
		reRender = false; // only render once
		
		
		
		
	
	}
	
	
	@Override 
	public void reRender(){
	super.reRender();
	this.panels.reRender();
	this.getBasePanel().reRender();
	}
	

	@Override
	public void setup() {

		//if(basePanel != null)
		//this.basePanel.update();	

		if (getBasePanel() != null)
			getBasePanel().setController(this.controller);
		this.panels.setController(this.controller);
		//basePanel.setParentPanel(this);

	}

	@Override
	public void update() {
		getBasePanel().update();
		this.panels.update();
	}

	/**
	 * @return the basePanel
	 */
	public GUIPanel getBasePanel() {
		return basePanel;
	}

	/**
	 * @param basePanel the basePanel to set
	 */
	public void setBasePanel(GUIPanel basePanel) {
		this.basePanel = basePanel;
	}





}
