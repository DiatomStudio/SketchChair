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
//#IF JAVA
package cc.sketchchair.sketch;

import ModalGUI.GUIEvent;
import ModalGUI.ModalGUI;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.UITools;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import toxi.geom.Plane;
import toxi.geom.Vec2D;

/**
 *  SketchTools provides a interface between the GUI and Sketch engine. 
 * @author gregsaul
 *
 */

//#ENDIF JAVA

public class SketchTools {
	private int currentTool = 1;
	public float brush_dia = 40;
	public float mouseX;
	public float mouseY;
	public int keyCode;
	public boolean keyPressed;
	public boolean renderNodesFlag = true;
	public boolean mouseDown;
	private float pmouseX;
	private float pmouseY;
	private int mouseButton;
	public char key;
	
	
	public static final int DRAW_TOOL = 1;
	public static final int SELECT_TOOL = 2;
	public static final int SELECT_BEZIER_TOOL = 9;
	public static final int MOVE_CAM_TOOL = 9;
	public static final int DRAW_PATH_TOOL = 234;
	public static final int DRAW_OFFSETPATH_TOOL = 235;

	
	public int drawPathToolState = 0;
	public static final int DRAW_PATH_TOOL_STATE_NORMAL = 0;
	public static final int DRAW_PATH_TOOL_STATE_ADD = 1;
	public static final int DRAW_PATH_TOOL_STATE_REMOVE = 2;
	public static final int DRAW_PATH_TOOL_STATE_CONNECT = 3;

	
	public int loadedCursor = 0;
	private static final int MOVE_2D_TOOL = 0;
	public static final int NONE = -1;
	public static int MOUSE_RIGHT = 39;
	public static int MOUSE_MIDDLE = 3;
	public static final int MOUSE_LEFT = 37;

	public static int LEG_TOOL = 7;

	public double zoom = 1;
	private int currentCapType = SketchSpline.CAP_ROUND;
	private PApplet applet;
	private float gridWidth = 50f;
	private float gridHeight = 50f;
	public boolean useGrid = false;
	private float minorGridnumber = 10;

	public static PImage DRAW_TOOL_CURSOR = null;

	public static PImage SELECT_TOOL_CURSOR = null;
	public static PImage LINE_TOOL_CURSOR = null;
	
	public static PImage POINT_TOOL_CURSOR = null;
	public static PImage POINT_TOOL_CURSOR_ADD = null;
	public static PImage POINT_TOOL_CURSOR_REMOVE = null;
	public static PImage POINT_TOOL_CURSOR_CONNECT = null;

	
	public static PImage ADD_DOLL_TOOL_CURSOR = null;
	public static PImage MOVE_OBJECT_CURSOR = null;


	public static PImage MOVE_CAM_TOOL_CURSOR = null;
	public static PImage ROTATE_CAM_TOOL_CURSOR;
	public static PImage ZOOM_CAM_TOOL_CURSOR;
	
	public static PImage SCALE_TOOL_CURSOR;

	public SketchTools(PApplet applet) {
		this.applet = applet;
		build(applet);
	}

	public void build(PApplet applet) {
		DRAW_TOOL_CURSOR = applet.loadImage("gui/cursors/CURSOR_DRAW.png");
		SELECT_TOOL_CURSOR = applet.loadImage("gui/cursors/CURSOR_SELECT.png");
		POINT_TOOL_CURSOR = applet.loadImage("gui/cursors/CURSOR_POINT.png");
		POINT_TOOL_CURSOR_ADD = applet.loadImage("gui/cursors/CURSOR_POINT_ADD.png");
		POINT_TOOL_CURSOR_REMOVE = applet.loadImage("gui/cursors/CURSOR_POINT_REMOVE.png");
		POINT_TOOL_CURSOR_CONNECT = applet.loadImage("gui/cursors/CURSOR_POINT_CONNECT.png");

		
		LINE_TOOL_CURSOR = applet.loadImage("gui/GUI_LINE_TOOL_UP.png");
		SCALE_TOOL_CURSOR = applet.loadImage("gui/cursors/CURSOR_SCALE.png");
		MOVE_OBJECT_CURSOR = applet.loadImage("gui/cursors/CURSOR_HAND_UP.png");

	}

	int getCurrentCapType() {
		return this.currentCapType;
	}


	public int getCurrentTool() {
		return currentTool;
	}
	
	public void setCurrentTool(int _toolType) {
		 currentTool = _toolType;
	}

	public int getMouseButton() {
		return mouseButton;
	}

	public PGraphics getPickBuffer() {
		// TODO Auto-generated method stub
		return null;
	}

	public Vec2D getPointOnPlane(Vec2D vec2d, Plane plane) {
		// TODO Auto-generated method stub
		return null;
	}

	public Vec2D getPointTranslated(Vec2D mousePos) {
		// TODO Auto-generated method stub
		return null;
	}

	public void mouseDown() {
		// TODO Auto-generated method stub

	}

	public void mouseDragged() {
		// TODO Auto-generated method stub

	}

	public void mousePressed() {
		// TODO Auto-generated method stub

	}

	public void mouseReleased() {
		// TODO Auto-generated method stub

	}

	public void render(PGraphics g) {
		renderPointer(g);

	}

	void renderPointer(PGraphics g) {

		
		if(GLOBAL.uiTools != null){
		
		
		if(GLOBAL.uiTools.hasCursorControl)
			return;
		
		
		if(GLOBAL.gui.overComponent())
			return;
		
		}
		
		
		
		if (this.getCurrentTool() == DRAW_TOOL) {
			ModalGUI.setCursor(this.applet,DRAW_TOOL_CURSOR,11,23);
		} else if (this.getCurrentTool() == LEG_TOOL) {
			ModalGUI.setCursor(this.applet,DRAW_TOOL_CURSOR,11,23);
		} else if (this.getCurrentTool() == SELECT_TOOL) {
			ModalGUI.setCursor(this.applet,SELECT_TOOL_CURSOR,16,10);
		} else if (this.getCurrentTool() == SketchTools.MOVE_2D_TOOL) {
			ModalGUI.setCursor(this.applet,MOVE_CAM_TOOL_CURSOR,18,18);	
		} else if (this.getCurrentTool() == SketchTools.DRAW_OFFSETPATH_TOOL) {
	
			ModalGUI.setCursor(this.applet,POINT_TOOL_CURSOR,15,10);	

			
		} else if (this.getCurrentTool() == SketchTools.DRAW_PATH_TOOL ) {
			
			if(drawPathToolState == DRAW_PATH_TOOL_STATE_NORMAL){
				ModalGUI.setCursor(this.applet,POINT_TOOL_CURSOR,15,10);	
			}
			
			if(drawPathToolState == DRAW_PATH_TOOL_STATE_ADD){
				ModalGUI.setCursor(this.applet,POINT_TOOL_CURSOR_ADD,15,10);	
				}
			
			if(drawPathToolState == DRAW_PATH_TOOL_STATE_REMOVE){
				ModalGUI.setCursor(this.applet,POINT_TOOL_CURSOR_REMOVE,15,10);	

			}
				
				
			
			if(drawPathToolState == DRAW_PATH_TOOL_STATE_CONNECT){
				ModalGUI.setCursor(this.applet,POINT_TOOL_CURSOR_CONNECT,15,10);	

				}
			
			
		} else {
			ModalGUI.setCursor(this.applet,SELECT_TOOL_CURSOR,16,10);				
		}
		
		if (this.getCurrentTool() == DRAW_TOOL
				|| this.getCurrentTool() == DRAW_OFFSETPATH_TOOL) {
			
			g.ellipseMode(PApplet.CENTER);
			g.noFill();
			g.stroke(100, 100, 100);
			g.ellipse(mouseX, mouseY, (this.brush_dia * (float)zoom) * 2,
					(this.brush_dia * (float)zoom) * 2);
			g.noStroke();
		}
		
	
	}

	public void selectCap(GUIEvent e) {

		this.currentCapType = (int) e.getVal();
		
		//#IF JAVA
		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().setBrushCap(this.currentCapType);
		//#ENDIF JAVA
	}
	
	
	public int getCap() {

		return this.currentCapType;
		//if (GLOBAL.sketchChairs.getCurChair() != null)
		//	GLOBAL.sketchChairs.getCurChair().setBrushCap(this.currentCapType);

	}

	public void selectTool(GUIEvent e) {
		this.selectTool((int) e.val);
	}


	public void selectTool(int currentTool) {
		this.currentTool = currentTool;
	}

	public void toggleUnion(GUIEvent e) {

		this.currentCapType = (int) e.getVal();

		//#IF JAVA
		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().toggleUnion();
		//#ENDIF JAVA
		
	}

	public void updateMouse(int mouseX2, int mouseY2, int pmouseX2,
			int pmouseY2, boolean mouseDown2, int mouseButton2) {
		pmouseX = mouseX;
		pmouseY = mouseY;

		mouseX = mouseX2;
		mouseY = mouseY2;

		mouseDown = mouseDown2;
		
		if(mouseButton2 != 0)
		mouseButton = mouseButton2;

		
		//#IF JAVA		
		if(this.useGrid){

			
			double m_gridW = this.gridWidth;
			double m_gridH = this.gridHeight;

			if(this.gridWidth * GLOBAL.getZOOM() > minorGridnumber*5){
				m_gridW /= minorGridnumber;
				m_gridH /= minorGridnumber ;
			}
		
						
			//calculate the number of grid squares between the centre of the screen and the edge to  see where our grid should start
			float offset_x = (float) (Math.round((((float)mouseX2 - ((float)GLOBAL.windowWidth / 2.0f)) / (m_gridW * GLOBAL.getZOOM()))) * (m_gridW * GLOBAL.getZOOM()));
			offset_x +=(float)(GLOBAL.windowWidth / 2)+ ((GLOBAL.CAM_OFFSET_X * GLOBAL.getZOOM()) % (m_gridW * GLOBAL.getZOOM()));
			
			
			float offset_y = (float) (Math.round((((float)mouseY2 - ((float)GLOBAL.windowHeight / 2.0f)) / (m_gridH * GLOBAL.getZOOM()))) * (m_gridH * GLOBAL.getZOOM()));
			offset_y += (float)(GLOBAL.windowHeight / 2)+ ((GLOBAL.CAM_OFFSET_Y * GLOBAL.getZOOM()) % (m_gridH * GLOBAL.getZOOM()));

			
			

			
			int newMouseX = (int) offset_x;
			int newMouseY = (int) offset_y;
			mouseX = newMouseX;
			mouseY = newMouseY;
		}
		
		//#ENDIF JAVA

	}



}
