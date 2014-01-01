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

import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

import cc.sketchchair.core.KeyEventSK;
import cc.sketchchair.core.MouseEventSK;
import cc.sketchchair.core.MouseWheelEventSK;
import cc.sketchchair.sketch.LOGGER;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import cc.sketchchair.core.MouseEventSK;

public abstract class GUIComponent {
	protected  boolean mouseOver = false;
	protected float x;
	protected float y;

	protected float w;
	protected float h;

	public GUILabel label;
	public boolean wasClicked = false;
	protected List<GUIListener> listeners = new ArrayList();
	public ModalGUI controller = null;
	protected int index;
	protected PImage toolTip = null;
	float toolTipAlpha = 0;
	protected boolean reRender = true; // if renderOnUpdate is on only render if told.
	GUIPanel parentPanel = null;
	GUIComponent parentComponent = null;

	public boolean visible = true;
	private boolean ignorePanel;
	boolean destroy;
	public boolean isDown = false;

	int fillCol = -2;
	int fillColDown = 100;
	int strokeCol = 0;
	int strokeColDown = -2;
	int fillColOver = -2;
	int strokeColOver = -2;

	//user is interacting with this component eg typing into a text field
	protected boolean hasFocus = false;

	public void actionPerformed(GUIEvent guievent) {
	}

	public void addActionListener(Object newListener) {
		listeners.add(new GUIListener(newListener));
	}

	public void addActionListener(Object newListener, String name) {
		listeners.add(new GUIListener(newListener, name));
	}

	public void addActionListener(Object newListener, String methodName,
			int methodVal) {
		listeners.add(new GUIListener(newListener, methodName, methodVal));
	}

	public void addActionListener(Object newListener, String methodName,
			String methodVal) {
		listeners.add(new GUIListener(newListener, methodName, methodVal));
	}

	public void addToolTip(PApplet applet, String toolTipPath, String language) {
		this.toolTip = applet.loadImage(toolTipPath + "_" + language + ".png");
	}

	public void destroy() {
	//	controller.applet.unregisterMouseEventSK(this);
	//	controller.applet.unregisterKeyEvent(this);
		controller.reBuildStencilBuffer();
		this.destroy = true;
		//System.out.println("destroy");

	}

	public void fireEventNotification(boolean bool) {
		for (int i = 0; i < listeners.size(); i++) {
			GUIListener listener = listeners.get(i);

			if (listener == null || listener.methodName == "")
				return;
			Field field;
			try {

				field = listener.object.getClass()
						.getField(listener.methodName);
				Class classType = field.getType();

				if (classType.toString().equals("boolean"))
					field.setBoolean(listener.object, bool);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
	}

	public void fireEventNotification(float val) {
		fireEventNotificationMethod(val);

		for (int i = 0; i < listeners.size(); i++) {
			GUIListener listener = listeners.get(i);

			if (listener == null || listener.methodName == "")
				return;
			Field field;
			try {

				field = listener.object.getClass()
						.getField(listener.methodName);
				Class classType = field.getType();

				if (classType.toString().equals("float"))
					field.setFloat(listener.object, val);
				else if (classType.toString().equals("int"))
					field.setFloat(listener.object, (int) val);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				//	e.printStackTrace();
			}
		}

	}

	public void fireEventNotification(GUIComponent argComponent,
			String argMessage) {

		if (this.destroy)
			return;

		if (!this.visible)
			return;
		for (int i = 0; i < listeners.size(); i++) {
			GUIListener listener = listeners.get(i);

			if (listener == null)
				return;
			try {
				GUIEvent e = new GUIEvent(argComponent, argMessage);

				e.val = listener.val;

				if (listener.stringVal != null)
					e.stringVal = listener.stringVal;

				Method m = listener.object.getClass().getDeclaredMethod(
						listener.getMethodToCall(),
						new Class[] { e.getClass() });
				//listener.object.getClass().getField(name)
				try {

					m.invoke(listener.object, new Object[] { e });

					// m.invoke(listener.object);
				} catch (InvocationTargetException ex) {
					System.out.println(ex.getCause().getMessage());
				} catch (IllegalAccessException illegalaccessexception) {
				}
			} catch (NoSuchMethodException ex) {
				System.out.println("NoSuchMethodException");
			}
		}
	}

	public void fireEventNotification(String str) {

	}

	public void fireEventNotificationMethod(float val) {

		if (this.destroy)
			return;

		if (!this.visible)
			return;

		for (int i = 0; i < listeners.size(); i++) {
			GUIListener listener = listeners.get(i);

			if (listener == null)
				return;
			try {

				Method m = listener.object.getClass().getDeclaredMethod(
						listener.getMethodToCall(), float.class);
				//listener.object.getClass().getField(name)
				try {

					m.invoke(listener.object, new Object[] { val });

					// m.invoke(listener.object);
				} catch (InvocationTargetException ex) {
					System.out.println(ex.getCause().getMessage());
				} catch (IllegalAccessException illegalaccessexception) {
				}
			} catch (NoSuchMethodException ex) {
				//System.out.println("NoSuchMethodException");
			}
		}
	}

	public ModalGUI getController() {
		return controller;
	}

	public int getFillColour() {
		return this.fillCol;
	}

	public int getFillColourDown() {
		return this.fillColDown;
	}

	public int getFillColourOver() {
		return this.fillColOver;
	}

	public float getHeight() {
		return this.h;
	}

	GUILabel getLabel() {
		return this.label;
	}

	public int getStrokeColour() {
		return this.strokeCol;
	}

	public int getStrokeColourDown() {
		return this.strokeColDown;
	}

	public int getStrokeColourOver() {
		return this.strokeColOver;
	}

	public float getWidth() {
		return this.w;
	}

	public float getX() {

		if (this.parentPanel != null)
			return this.parentPanel.getX() + this.x;
		else if (this.parentComponent != null)
			return this.parentComponent.getX() + this.x;
		else
			return this.x;
	}

	public float getY() {
		if (this.parentPanel != null)
			return this.parentPanel.getY() + this.y;
		else if (this.parentComponent != null)
			return this.parentComponent.getY() + this.y;
		else
			return this.y;
	}

	public boolean hasFocus() {
		return this.hasFocus;
	}

	public void hide() {
		//controller.reBuildStencilBuffer();
		this.hasFocus = false;
		this.visible = false;
	}

	public void ignorePanel() {
		this.ignorePanel = true;
	}

	private void initWithParent() {

	}

	public boolean isMouseOver() {
		if (!this.visible)
			return false;

		int mouseX = 0;
		if (controller != null)
			mouseX = controller.applet.mouseX;

		int mouseY = 0;
		if (controller != null)
			mouseY = controller.applet.mouseY;

		return mouseX >= this.getX() && mouseX <= this.getX() + w
				&& mouseY >= this.getY() && mouseY <= this.getY() + h;
	}

	public void keyEvent(KeyEventSK keyevent) {

	}
	

	public void mouseEvent(MouseEventSK e) {
		if (this.destroy)
			return;

		if (!this.visible)
			return;

			
		

		//MouseEventSK.MOUSE_PRESSED

		if (e.getAction() == MouseEventSK.CLICK){
			if (isMouseOver() && this.visible){
				wasClicked = true;
				reRender();
			}
		} else if (e.getAction()  == MouseEventSK.RELEASE && wasClicked && isMouseOver()
				&& this.visible) {
			fireEventNotification(this, "Clicked");
			wasClicked = false;
		}
	}

	
	

	public void mouseEventLegacy(MouseEventSK e) {
		if (this.destroy)
			return;

		if (!this.visible)
			return;

			
		

		//MouseEventSK.MOUSE_PRESSED

		if (e.getAction() == MouseEventSK.CLICK){
			if (isMouseOver() && this.visible){
				wasClicked = true;
				reRender();
			}
		} else if (e.getAction()  == MouseEventSK.RELEASE && wasClicked && isMouseOver()
				&& this.visible) {
			fireEventNotification(this, "Clicked");
			wasClicked = false;
		}
	}

	public void render(PGraphics g) {


	}
	
	/*
	 * Only reRender when requested.
	 */
	public void reRender(){
		reRender = true;
	}

	public void renderLabel(PGraphics g) {

		if (this.label == null)
			return;

		if (this.label.layout == GUILabel.RIGHT_OF_COMPONENT ) {
			this.label.align = GUILabel.LEFT;
			this.label
					.render(g, this.getX() + this.getWidth() + 5, this.getY());
		}

		if (this.label.layout == GUILabel.LEFT_OF_COMPONENT) {
			this.label.align = GUILabel.RIGHT;
			this.label.render(g, this.getX() - 1 + 5,
					this.getY() + (this.getHeight() / 2));
		}

		if (this.label.layout == GUILabel.CENTRE_OF_COMPONENT) {
			this.label.align = GUILabel.CENTRE;
			this.label.render(g, this.getX() + (this.getWidth() / 2),
					this.getY() + (this.getHeight() / 2)+3);

		}

		if ( this.label.layout == GUILabel.UNDER_COMPONENT) {
			this.label.align = GUILabel.CENTRE;
			this.label.render(g, this.getX() + (int)(this.getWidth() / 2),
					this.getY() + this.getHeight() + 10);
		}

		if (this.label.layout == GUILabel.ABOVE_COMPONENT) {
			this.label.align = GUILabel.CENTRE;
			this.label.render(g, this.getX() + (int)(this.getWidth() / 2),
					this.getY() - this.getHeight());
		}
		
		

	}

	void renderToolTip(PGraphics g, float x, float y) {
		float xPos = this.getX() + this.w;
		float yPos = this.getY() + this.h - this.toolTip.height;
		g.tint(255, 255, 255, this.toolTipAlpha);
		g.image(this.toolTip, (int)xPos, (int)yPos);
		g.noTint();
	}

	public void renderTop(PGraphics g) {
		// TODO Auto-generated method stub

	}

	public void setController(ModalGUI c) {
		controller = c;
		this.initWithParent();
		if (this.label != null)
			this.label.controller = c;
		this.setup();
	}

	public void setFillColour(int c) {
		this.fillCol = c;
	}

	public void setFillColourDown(int c) {
		this.fillColDown = c;
	}

	public void setFillColourOver(int c) {
		this.fillColOver = c;
	}

	public void setFocus(boolean focus) {
		this.hasFocus = focus;
	}

	public void setLabel(String str) {
		this.label = new GUILabel(this, str, this.controller);
	}

	public void setParentComponent(GUIComponent component) {
		this.parentComponent = component;
	}

	public void setParentPanel(GUIPanel guiPanel) {
		this.parentPanel = guiPanel;
	}

	public void setPos(float newX, float newY) {
		//  if(newX > 0 && newY > 0)
		//  {
		x = newX;
		y = newY;
		// }
	}

	public void setSize(float w, float h) {
		this.w = w;
		this.h = h;
	}

	public void setStrokeColour(int c) {
		this.strokeCol = c;
	}

	public void setStrokeColourDown(int c) {
		this.strokeColDown = c;
	}

	public void setStrokeColourOver(int c) {
		this.strokeColOver = c;
	}

	public abstract void setup();

	public void show() {
		//controller.reBuildStencilBuffer();
		this.visible = true;

	}

	public void update() {
		// TODO Auto-generated method stub

	}

	public void mouseWheelMoved(MouseWheelEventSK e) {
		// TODO Auto-generated method stub
		
	}

}
