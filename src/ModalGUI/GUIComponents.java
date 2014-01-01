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

import cc.sketchchair.core.KeyEventSK;
import cc.sketchchair.core.MouseEventSK;
import cc.sketchchair.core.MouseWheelEventSK;
import processing.core.PGraphics;

import cc.sketchchair.core.MouseEventSK;

public class GUIComponents extends GUIComponent {

	List l = new ArrayList();

	public void add(GUIComponent component) {
		this.l.add(component);
	}

	public void clear() {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			component.destroy();
			this.l.remove(i);
		}

		this.l = new ArrayList();
	}

	public GUIComponent get(int index) {
		return (GUIComponent) this.l.get(index);
	}

	public boolean hasFocus() {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);

			if (component.hasFocus()) {
				return true;
			}

		}
		return false;
	}

	public void hideAll() {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			component.hide();
		}
	}

	public boolean isMouseOver() {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			if (component.isMouseOver())
				return true;

		}

		return false;
	}

	public void keyEvent(KeyEventSK keyevent) {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			component.keyEvent(keyevent);
		}
	}

	public void mouseEvent(MouseEventSK e) {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			component.mouseEvent(e);
		}
	}
	
	public void mouseWheelMoved(MouseWheelEventSK e) {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			component.mouseWheelMoved(e);
		}		
	}
	
	

	public boolean overComponent() {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			if (component.isMouseOver() && component.visible)
				return true;

		}
		return false;
	}
	
	

	public void render(PGraphics g) {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			component.render(g);

		}

		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			component.renderTop(g);
		}

	}
	
	@Override 
	public void reRender(){
		super.reRender();
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			component.reRender();
		}
	}

	public void render(PGraphics g, float x, float y, float w, float h) {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);

			if (component.getX() >= x
					&& component.getX() <= x + w
					&& component.getY()+component.getHeight() >= y
					&& component.getY() - component.getHeight() <= y + h) {
				component.visible = true;
				component.render(g);

			} else
				component.visible = false;

		}
	}

	public void reset() {
		this.clear();
	}

	public void setController(ModalGUI controller) {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			component.setController(controller);
		}
	}

	public void setPos(float newX, float newY) {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			component.setPos(newX, newY);
		}
	}

	@Override
	public void setup() {
		// TODO Auto-generated method stub

	}

	public void showAll() {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			component.show();

		}
	}

	public int size() {
		return this.l.size();

	}

	public boolean textfieldHasFocus() {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);

			if (component instanceof GUITextfield) {
				GUITextfield field = (GUITextfield) component;
				if (field.isFocus())
					return true;

			}

		}
		return false;
	}

	public void translate(float x, float y) {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			component.setPos(component.getX() - component.parentPanel.getX()
					+ x, component.getY() - component.parentPanel.getY() + y);
		}
	}

	public void update() {
		for (int i = 0; i < this.l.size(); i++) {
			GUIComponent component = (GUIComponent) this.l.get(i);
			if (component.destroy)
				this.l.remove(i);
			else
				component.update();

		}

	}





}
