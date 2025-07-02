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

import cc.sketchchair.core.KeyEventSK;
import cc.sketchchair.core.MouseEventSK;
import cc.sketchchair.core.MouseWheelEventSK;
import cc.sketchchair.sketch.LOGGER;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

public class GUIPanel extends GUIComponent {

	public GUIComponents components = new GUIComponents();
	protected float currentX = 15;
	float currentY = 20f;

	float minimizedH = 5;
	float minimizedW = 5;
	float maximizedH = 5;
	float maximizedW = 5;

	public float spacingX = 10;
	public float spacingY = 10;
	public boolean minimized = false;

	public boolean isDragging = false;
	public boolean isDraggable = false;

	private float selectBarHeight = 15;
	public boolean autoHide = false;
	private float cornerRad = 7f;
	PGraphics canvas = null;
	PImage bufferedCanvas = null;
	public boolean useCanvas = false;
	float scrollX = 0;
	float scrollY = 0;

	float prevScrollX = 0;
	float prevScrollY = 0;

	float deltaScrollX = 0;
	float deltaScrolY = 0;

	private float bottomScrollY;
	private float rightScrollX;
	private float topScrollY;
	private float leftScrollX;
	public boolean useScroolBarX;
	public boolean useScroolBarY;

	GUIScrollbar sliderX = null;
	GUIScrollbar sliderY = null;
	public boolean hideSelectBar = false;
	public boolean hideMinimize = true;

	GUIPanel dockedPanelTop = null;
	GUIPanel dockedPanelBottom = null;
	int DOC_TOP = 0;
	int DOC_BOTTOM = 0;
	private GUIButton minimize;

	PImage mask = null;
	String tittle = "";
	private boolean invisible = false;
	public boolean renderBorder = true;
	public PImage tabUpImg = null;
	public PImage tabDownImg = null;

	
	
	public GUIPanel(float x, float y, float w, float h, boolean useCanvas,
			PApplet applet, ModalGUI c) {
		this(x, y, w, h, c);
		makeCanvas(w, h, applet);
		
		//this.useCanvas = useCanvas;

	}

	public GUIPanel(float x, float y, float w, float h, ModalGUI c) {
		this.setController(c);
		this.minimizedH = 10;
		this.minimizedW = w;
		this.maximizedH = h;
		this.maximizedW = w;
		this.setPos(x, y);
		this.setSize(w, h);
		this.mask = c.applet.loadImage("mask.png");
	}

	public void add(GUIComponent component) {

		this.components.add(component);

		component.setController(this.controller);

		component.setParentPanel(this);

		//if(component.getY() > currentY && this.useCanvas)
		//	currentY = component.getY();

		//if(component.getX() > currentX && this.useCanvas)
		//	currentX = component.getX();

		if (component.getY() + component.getHeight() > this.getY()
				+ this.getHeight() - 20
				&& useScroolBarY) {
			this.bottomScrollY = (component.getY() + (component.getHeight() * 2))
					- (this.getY() + this.getHeight());
			this.updateYSlider(0, this.bottomScrollY);
		}
		if (component.getX() + component.getWidth() > this.getX()
				+ this.getWidth()
				&& useScroolBarX) {
			this.rightScrollX = component.getX() + component.getWidth();
			this.updateXSlider(0, this.rightScrollX - this.getX());

		}

		if (this.useCanvas)
			this.updateCanvas();

	}

	public void clear() {
		this.currentX = 5;
		this.currentY = 20;
		this.bottomScrollY = 0;
		this.rightScrollX = 0;
		this.sliderX = null;
		this.sliderY = null;

		//this.updateXSlider(0, 0);
		//this.updateYSlider(0, 0);
		this.components.clear();
	}

	public void docTo(GUIPanel panel) {

		//if(DOC_TOP == this.DOC_TOP)
		this.dockedPanelTop = panel;

		//if(DOC_TOP == this.DOC_BOTTOM)
		//	this.dockedPanelBottom = panel;

	}

	@Override
	public boolean hasFocus() {
		if (this.hasFocus || this.components.hasFocus())
			return true;
		else
			return false;

	}

	@Override
	public void hide() {
		this.visible = false;
		this.components.hideAll();
	}

	void hideAll() {
		this.components.hideAll();
	}

	@Override
	public boolean isMouseOver() {
		int mouseX = controller.applet.mouseX;
		int mouseY = controller.applet.mouseY;

		if (mouseX >= this.getX() && mouseY >= this.getY()
				&& mouseX <= this.getX() + this.getWidth()
				&& mouseY <= this.getY() + this.getHeight())
			return true;

		if (this.components.isMouseOver())
			return true;

		return false;

	}

	private boolean isMouseOverMoveBar() {
		int mouseX = controller.applet.mouseX;
		int mouseY = controller.applet.mouseY;

		return mouseX >= this.getX() && mouseY >= this.getY()
				&& mouseX <= this.getX() + this.getWidth()
				&& mouseY <= this.getY() + this.selectBarHeight;

	}

	@Override
	public void keyEvent(KeyEventSK theKeyEvent) {
		this.components.keyEvent(theKeyEvent);
	}

	private void makeCanvas(float w, float h, PApplet applet) {
		this.useCanvas = true;
		//this.canvas = applet.createGraphics((int)w, (int) ((int)h-this.selectBarHeight-2), PApplet.P2D);
		//this.bufferedCanvas = this.canvas.get();

		//this.canvas.alpha(0);
		//this.canvas.smooth();
		//this.canvas.alpha(255);
	}

	public void minimizeToggle(GUIEvent e) {

		this.minimized = !this.minimized;

		if (this.minimized) {
			hideAll();
			this.setSize(this.minimizedW, this.minimizedH);
		} else {
			showAll();
			this.setSize(this.maximizedW, this.maximizedH);
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEventSK e) {
		if (!this.visible)
			return;
		this.components.mouseWheelMoved(e);

	}
	
	
	@Override
	public void mouseEvent(MouseEventSK e) {

		if (!this.visible)
			return;

		this.components.mouseEvent(e);

		if (sliderX != null)
			sliderX.mouseEvent(e);
		if (sliderY != null)
			sliderY.mouseEvent(e);

		if (e.getAction() == MouseEventSK.PRESS) {
			if (isMouseOver())
				wasClicked = true;

			if (isMouseOverMoveBar() && isDraggable)
				isDragging = true;

		} else if (e.getAction() == MouseEventSK.RELEASE && wasClicked
				&& isMouseOver()) {
			fireEventNotification(this, "Clicked");
			wasClicked = false;
			isDragging = false;
		}

		if (e.getAction() == MouseEventSK.RELEASE) {
			isDragging = false;
		}

	}

	public boolean overComponent() {
		return components.overComponent();
	}

	public void placeComponent(GUIComponent component) {

		component.setController(this.controller);

		if (this.components.l.size() > 0) {
			if (this.currentX + (this.spacingX) + (component.getWidth() * 2) > this
					.getWidth()) {
				currentX = this.spacingX;
				currentY += component.getHeight() + this.spacingY;
			} else {
				currentX += this.spacingX + component.getWidth();
				//currentY += component.getHeight() + this.spacingY;
			}
			//currentY += 10;
		}

		if (currentY + component.getHeight() > this.getHeight()) {
			this.bottomScrollY = currentY;
			this.updateYSlider(0, this.bottomScrollY);
		}
		if (currentX + component.getWidth() > this.getWidth()) {
			this.rightScrollX = currentX;
			this.updateXSlider(0, this.rightScrollX - this.getX());
		}

		component.setPos(currentX, currentY);
		component.setParentPanel(this);

		if (this.useCanvas)
			component.ignorePanel();

		this.add(component);

	}

	public void placeComponentLeft(GUIComponent component) {
		component.setController(this.controller);

		if (currentX + component.getWidth() > this.getWidth()) {
			this.rightScrollX = currentX + (component.getWidth() * 2);
			this.updateXSlider(0, this.rightScrollX - this.getX());
		}

		component.setPos(currentX, currentY);
		component.setParentPanel(this);

		if (this.useCanvas)
			component.ignorePanel();

		currentX += this.spacingX + component.getWidth();

		this.add(component);

	}

	@Override
	public void render(PGraphics g) {
		if (controller == null)
			return;
		
		
		if (!this.visible || invisible)
			return;
		
		//Render on update optimization
		if(controller.renderOnUpdate && !reRender){
			this.components.render(g);//we still need to pass on the render command in case other components need refreshing
		return;
		}
		
		if(controller.renderOnUpdate)
		reRender = false; // only render once

		
		
		
		if(this.sliderX!=null|| this.sliderY!=null){
			/*
			GL gl = ((PGraphicsOpenGL) g).gl;
			gl.glEnable(gl.GL_SCISSOR_TEST);
			gl.glScissor((int)this.getX(),(int)this.getY()-100,(int)this.getWidth(),(int)this.getHeight());
*/		
		}
		

		
		controller.performanceMode = true;

		//if(this.useCanvas)
		//	g = this.canvas;

		

		if (!this.minimized) {
			g.stroke(this.getStrokeColour());
			g.strokeWeight(1);
			g.fill(this.getFillColour());

			if(renderBorder ){
			if (this.cornerRad > 0)
				g.rect(this.getX(), this.getY(), this.getWidth(),
						this.getHeight());
			else
				g.rect(this.getX(), this.getY(), this.getWidth(),
						this.getHeight());
			
			//this.cornerRad
		
			}
			g.fill(this.getFillColour());
			g.noStroke();

			if (!hideSelectBar) {
				if (controller.performanceMode)
					g.rect(this.getX() + 1, this.getY() + 1,
							this.getWidth() - 2, selectBarHeight);
				else
					ModalGUI.roundrect(g, this.getX() + 1, this.getY() + 1,
							this.getWidth() - 2, selectBarHeight,
							this.cornerRad);
			}

			g.fill(255, 255, 255);

		} else {
			g.stroke(this.getStrokeColour());
			g.strokeWeight(1);
			g.fill(this.getFillColour());

			if (!hideSelectBar) {
				if (controller.performanceMode)
					g.rect(this.getX(), this.getY(), this.getWidth(),
							this.selectBarHeight + 2);
				else
					controller.roundrect(g, this.getX(), this.getY(),
							this.getWidth(), this.selectBarHeight + 2,
							this.cornerRad);

				g.fill(this.getFillColour());
				g.noStroke();

				if (controller.performanceMode)
					g.rect(this.getX() + 1, this.getY() + 1,
							this.getWidth() - 2, selectBarHeight);
				else
					controller.roundrect(g, this.getX() + 1, this.getY() + 1,
							this.getWidth() - 2, selectBarHeight,
							this.cornerRad);
			}

			g.fill(this.getFillColour());
		}
		if (this.getLabel() != null) {
			g.fill(50, 50, 50);
			g.textSize = 12f;
			g.textAlign = PConstants.LEFT;
			g.textAlignY = PConstants.BOTTOM;
			g.text(this.getLabel().getLabelStr(), this.getX() + 3f,
					this.getY() + 14f);
		}
		

		if (useCanvas) {
			/*
			//this.canvas.beginDraw();
			
			//this.canvas.translate(-this.getX(),-this.getY());
			
			if(this.sliderY != null)
			this.canvas.translate(0, -this.sliderY.getVal());
			
			if(this.sliderX != null)
				this.canvas.translate( -this.sliderX.getVal(),0);    
			
			//this.canvas.noFill();
			this.canvas.background(255,255,255,0);
			this.components.render(this.canvas);
			*/

			//this.canvas.endDraw();

			//if(this.bufferedCanvas != null)
			//g.image(this.bufferedCanvas, this.getX(), this.getY());

			g.pushMatrix();
			//g.translate(0, -this.scrollY);

			//g.translate( -this.scrollX,0);    
			//	
			this.components.translate(this.prevScrollX - this.scrollX,
					this.prevScrollY - this.scrollY);
			this.prevScrollX = this.scrollX;
			this.prevScrollY = this.scrollY;
			

			
			this.components.render(g, this.getX(), this.getY(),
					this.getWidth(), this.getHeight());
			

			g.popMatrix();
		} else{
			this.components.render(g);
		}

		

		if(this.sliderX!=null|| this.sliderY!=null){
			//GL gl = ((PGraphicsOpenGL) g).gl;
			//gl.glDisable(gl.GL_SCISSOR_TEST);
			
		}
		if (this.sliderX != null)
			this.sliderX.render(g);

		if (this.sliderY != null)
			this.sliderY.render(g);

		if (!this.autoHide && !this.hideMinimize && minimize != null)
			minimize.render(g);

		
		
			
			
	}
	
	

	
	@Override 
	public void reRender(){
	super.reRender();
	

	this.components.reRender();
	
	if (this.sliderX != null)
		sliderX.reRender();
	
	if (this.sliderY != null)
		sliderY.reRender();	
	
this.reRender =true;
	
	}
	
	public void setContentPosition(int xOffset, int yOffset) {
		this.currentX = xOffset;
		this.currentY = yOffset;

	}

	void setTittle(String t) {
		tittle = t;
	}

	@Override
	public void setup() {

		minimize = new GUIButton(this.getWidth() - 18, -1,
				"GUI_PLANE_REMOVE_UP.png", "GUI_PLANE_REMOVE_DOWN.png",
				this.controller);
		minimize.addActionListener(this, "minimizeToggle", 0);
		//	minimize.addToolTip(controller.parent,"GUI_PLANE_REMOVE_TOOLTIP", "ENG");
		minimize.setController(controller);
		//controller.add(minimize);
		minimize.setParentPanel(this);

	}

	@Override
	public void show() {
		this.visible = true;
		this.components.showAll();
	}

	void showAll() {
		this.components.showAll();
	}

	public boolean textfieldHasFocus() {
		return components.textfieldHasFocus();
	}

	@Override
	public void update() {
		
		if(sliderX != null && sliderX.wasClicked)
			reRender();
		
		if(sliderY != null && sliderY.wasClicked){
			reRender();
		}
		
		

		
		/*

		if(this.minimized || !this.visible)
			this.components.hideAll();
		
		if(this.visible && !this.minimized)
			this.components.showAll();
		*/

		if (this.visible)
			components.update();

		/*
		if(this.parentPanel != null && (this.parentPanel.minimized || !this.parentPanel.visible)){
			this.components.hideAll();

			this.visible = false;
			this.minimized = true;
		}
		
		if(this.parentPanel != null && ! this.parentPanel.minimized && this.parentPanel.visible){
			this.components.showAll();	
			this.visible = true;
			this.minimized = false;
		}
		*/

		if (this.useCanvas) {
			//if(this.canvas.isModified())
		//	this.updateCanvas();
		}

		if (this.dockedPanelTop != null) {
			this.setPos(this.dockedPanelTop.getX(), this.dockedPanelTop.getY()
					+ this.dockedPanelTop.getHeight() + 5);

		}

		if (isDragging && this.dockedPanelTop == null) {

			float deltaX = controller.applet.mouseX - controller.applet.pmouseX;
			float deltaY = controller.applet.mouseY - controller.applet.pmouseY;
			this.setPos(this.getX() + deltaX, this.getY() + deltaY);

		}
		if (autoHide) {
			if (isMouseOver()) {
				this.showAll();
				this.minimized = false;
				this.setSize(this.maximizedW, this.maximizedH);

			} else {
				this.hideAll();
				this.minimized = true;
				this.setSize(this.minimizedW, this.minimizedH);

			}
		}

		
		if (this.sliderX != null)
			this.sliderX.update();

		if (this.sliderY != null)
			this.sliderY.update();

		if (this.sliderX != null)
			this.scrollX = this.sliderX.getVal();

		if (this.sliderY != null)
			this.scrollY = this.sliderY.getVal();

	}

	private void updateCanvas() {
		/*
		this.canvas.beginDraw();
		this.bufferedCanvas = this.canvas.get();
		//this.bufferedCanvas.
		this.canvas.endDraw();
*/
		if (sliderX != null)
			sliderX.update();
		if (sliderY != null)
			sliderY.update();
	}

	public void updateXSlider(float minVal, float maxVal) {

		if (this.sliderX == null) {
			this.sliderX = new GUIScrollbar(2, this.getHeight() - 16,
					this.getWidth() - 3, minVal, maxVal, GUISlider.HORIZONTAL,
					this.controller);
			this.sliderX.setParentPanel(this);
			this.sliderX.setVal(0f);
			this.useScroolBarX = true;
			this.sliderX.setController(controller);
			
			//controller.add(minimize);    	

		}

		this.sliderX.setMinVal(minVal);
		this.sliderX.setMaxVal(maxVal);

	}

	public void updateYSlider(float minVal, float maxVal) {
		if (this.sliderY == null) {
			this.sliderY = new GUIScrollbar(this.getWidth() - 10,
					selectBarHeight, this.getHeight() - (selectBarHeight+15),
					minVal, maxVal, GUISlider.VERTICAL, this.controller);
			this.sliderY.setParentPanel(this);
			this.sliderY.setVal(0f);
			this.useScroolBarY = true;
			this.sliderY.setController(controller);
			//controller.add(minimize);
		}
		this.sliderY.setMinVal(minVal);
		this.sliderY.setMaxVal(maxVal);

	}

	public void invisible() {
		this.invisible  = true;		
	}
}
