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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import cc.sketchchair.core.LOGGER;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

public class GUILabel extends GUIComponent {

	public static final int CENTRE = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;

	public static final int RIGHT_OF_COMPONENT = 1;
	public static final int LEFT_OF_COMPONENT = 2;
	public static final int CENTRE_OF_COMPONENT = 3;
	public static final int UNDER_COMPONENT = 4;
	public static final int ABOVE_COMPONENT = 5;

	private static int countLines(String str) {
		String[] lines = str.split("\r\n|\r|\n");
		return lines.length;
	}

	PImage preRenderedLabel = null;
	private String labelStr = null;
	float textSize = 12;
	public int align;

	public int layout = 1;
	boolean preRenderLabels = true;

	public GUILabel(float x, float y, String string, ModalGUI c) {
		this.setController(c);
		this.setPos(x, y);
		this.setLabelStr(string);

	}

	public GUILabel(GUIComponent component, int x, float y, String string,
			ModalGUI c) {
		this.setController(c);
		this.setPos(x, y);
		this.setLabelStr(string);
		this.setParentComponent(component);
		this.setSize(this.getLabelStr().length(), textSize);
	}

	public GUILabel(GUIComponent component, String string, ModalGUI c) {
		this(component, 0, 0, string, c);
	}

	@Override
	public void keyEvent(KeyEvent keyevent) {
	super.keyEvent(keyevent);
	}

	@Override
	public void mouseEvent(MouseEvent e) {
	super.mouseEvent(e);
	}

	public void preRenderLabel(String str, PGraphics g) {

		
		
		g.textSize(this.textSize);
		int ln = countLines(str);
		float w = (int)g.textWidth(str) + 4;
		float h = (getController().labelSize + g.textDescent()+ g.textAscent()) * ln;
		PGraphics textG = getController().appletStatic.createGraphics((int) w,
				(int) h, PApplet.OPENGL);
		
		this.setSize(w, h);
		textG.beginDraw();
		textG.smooth(2);
		//textG.background(255);
		
		//textG.textMode(PApplet.SCREEN);
		textG.textSize(this.textSize);
		textG.textFont(getController().myFontMedium,this.textSize);
		//textG.textFont(myFont);
		//textG.textMode(PApplet.SCREEN);
		//textG.alpha(1);
		//textG.background(255, 255, 255, 0);
		textG.fill(0);
		
		
		//LOGGER.info("s"+this.textSize);
		/*
		LOGGER.info(this.labelStr);
		g.printMatrix();
		g.printCamera();
		g.printProjection();
		*/	
		
		textG.text(
				str,
				2,
				h
						- (g.textDescent()+g.textAscent() - 1)
						- ((getController().labelSize + g.textDescent() + 1) * (ln - 1)));
		textG.endDraw();
		this.preRenderedLabel = textG.get();
	}

	public void render(PGraphics g) {
		
		if(controller.renderOnUpdate && !reRender){
			return;
		}
		
		if(controller.renderOnUpdate)
		reRender = false; // only render once

		if (!this.visible)
			return;
		
		

		if (parentComponent == null) {
			this.render(g, this.getX(), this.getY());
			return;
		}

		if (this.layout == GUILabel.RIGHT_OF_COMPONENT) {
			this.align = GUILabel.LEFT;
			this.render(g, this.getX() + (parentComponent.getWidth() + 5),
					this.getY());
		}

		if (this.layout == GUILabel.LEFT_OF_COMPONENT) {
			this.align = GUILabel.RIGHT;
			this.render(g, this.getX() - 5, this.getY());
		}

		if (this.layout == GUILabel.CENTRE_OF_COMPONENT) {
			this.align = GUILabel.CENTRE;
			this.render(g, this.getX() + (parentComponent.getWidth() / 2),
					this.getY() + (this.getHeight() / 2));
		}

		if (this.layout == GUILabel.UNDER_COMPONENT) {
			this.align = GUILabel.CENTRE;
			this.render(g, this.getX() + (parentComponent.getWidth() / 2),
					this.getY() - this.getHeight());
		}

		if (this.layout == GUILabel.ABOVE_COMPONENT) {
			this.align = GUILabel.CENTRE;
			this.render(g, this.getX() + (parentComponent.getWidth() / 2),
					this.getY() - this.getHeight());
		}

		//this.render(g,this.getX(),this.getY());
	}

	public void render(PGraphics g, float x, float y) {

		
		
		if (this.controller == null)
			return;

		if (this.getLabelStr() == null)
			return;

		g.pushMatrix();
		
		if (preRenderedLabel == null && preRenderLabels )
			preRenderLabel(this.getLabelStr(), this.controller.appletStatic.g);



		if (align == CENTRE) {
			g.translate(-preRenderedLabel.width / 2,
					-preRenderedLabel.height / 2);
		}

		if (align == LEFT) {
			//nothing?
		}

		if (align == RIGHT) {
			g.translate(-preRenderedLabel.width, 0);
		}

		
		if(preRenderedLabel != null && preRenderLabels)
		g.image(preRenderedLabel, (int)x, (int)y);
		else{
			g.textSize(this.textSize);
			g.textFont(getController().myFontMedium,this.textSize);
			g.fill(0);
			g.text(this.getLabelStr(),(int)x, (int)y);
		}

		g.popMatrix();

	}

	@Override
	public void setup() {
		// TODO Auto-generated method stub

	}
	
	
	public void setText(String string_){
		setLabelStr(string_);
		preRenderedLabel = null;
	}

	/**
	 * @return the labelStr
	 */
	public String getLabelStr() {
		return labelStr;
	}

	/**
	 * @param labelStr the labelStr to set
	 */
	public void setLabelStr(String labelStr) {
		this.labelStr = labelStr;
	}

}
