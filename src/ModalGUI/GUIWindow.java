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

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.KeyEventSK;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.MouseEventSK;
import cc.sketchchair.core.UITools;
import processing.core.PApplet;
import processing.core.PGraphics;

public class GUIWindow extends GUIPanel {
	public GUIComponents components = new GUIComponents();
	boolean lightboxed = false;
	public void setLightboxed(boolean lightbox){lightboxed = lightbox;}
	public boolean getLightboxed(){return this.lightboxed;}
	
	
	public GUIWindow(float x, float y, float w, float h,
			ModalGUI c) {
		super( x,  y,  w,  h,c);
		this.setController(c);
		this.setPos(x, y);
		this.setSize(w, h);
		
		int closeWidth = 20;
		int closeHeight = 7;
		GUIToggle closeButton = new GUIToggle(this.getWidth() - closeWidth-5,closeHeight,closeWidth,closeWidth ,"gui/GUI_CLOSE.png",c);
		closeButton.addActionListener(this, "close");
		this.controller.reBuildStencilBuffer();
		this.add(closeButton);
	}

	@Override
	public void setup() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void render(PGraphics g) {
		
		
		//Render on update optimization
		if(controller.renderOnUpdate && !reRender){
			this.components.render(g); //components may still need to re-render
			return;}
		if(controller.renderOnUpdate)
		reRender = false; // only render once
		
		
		if(this.getLightboxed()){
			g.fill(0,0,0,200);
			g.rect(0, 0, g.width, g.height);
		}
			
		g.fill(255);
		controller.roundrect(g, this.getX(), this.getY(),
				this.getWidth(), this.getHeight(), 5);
		
		
		this.components.render(g);


	}
	
	@Override
	public
	void reRender(){
		super.reRender();
		this.components.reRender();
	}

	public void centre() {

		this.setPos((this.getController().applet.width - this.getWidth())/2,(this.getController().applet.height - this.getHeight())/2);
	}
	
	@Override
	public void mouseEvent(MouseEventSK e) {

		if (!this.visible)
			return;

		this.components.mouseEvent(e);
		
	}
	public void close(GUIEvent e) {
		this.destroy();
	}
	
	@Override
	public void update() {
		this.components.update();
	}
	
	@Override
	public boolean hasFocus() {
		if (this.hasFocus || this.components.hasFocus())
			return true;
		else
			return false;

	}
	
	@Override
	public void keyEvent(KeyEventSK theKeyEvent) {
		this.components.keyEvent(theKeyEvent);
	}

	

	public void add(GUIComponent component) {
		this.components.add(component);		
		component.setController(this.controller);
		component.setParentPanel(this);
	}



}
