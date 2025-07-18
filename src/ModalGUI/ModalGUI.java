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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.IntBuffer;


import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.KeyEventSK;
import cc.sketchchair.core.Legacy;
import cc.sketchchair.core.MouseEventSK;
import cc.sketchchair.core.MouseWheelEventSK;
import cc.sketchchair.sketch.LOGGER;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;


public class ModalGUI implements MouseWheelListener {
	public static PApplet applet;
	public static PApplet appletStatic;
	boolean clickStartedOn = false;
	public boolean renderOnUpdate = true; //only render components when they're updated or mouse is over.
	public boolean rebuildStencilBuffer = true;
	public static PImage makeImgDown(float w, float h, PImage img) {
		
		
		PGraphics pg = appletStatic.createGraphics((int) w, (int) h,
				Legacy.instance().get2DRenderMode());
		
		pg.beginDraw();

		pg.smooth();
		
		pg.noStroke();
		pg.background(250);
		pg.rect(0,0,w,h);
		
		
		pg.smooth();
		pg.stroke(200, 200, 200);
		pg.fill(194, 217, 216);

		pg.strokeWeight(1);

		roundrect(pg, 0, 0, w + 4, h + 4, 4);

		//pg.strokeWeight(2);
		//pg.stroke(100, 100, 100);

		//roundrectLeft(pg, 2, 2, w - 4, h - 4, 4);

		int offsetX = (int) ((w - img.width) / 2);
		int offsetY = (int) ((h - img.height) / 2);
		
		pg.fill(255);

		
		if(img != null){
		pg.image(img, (int)offsetX, (int)offsetY);
		}
		
		pg.endDraw();

		
		PImage returnImage = pg.get();

		return returnImage;

	}

	public static PImage makeImgDown(PImage img) {
		return makeImgDown(img.width, img.height, img);
	}

	public static PImage makeImgOver(int w, int h, PImage img) {

	
		
		PGraphics pg = appletStatic.createGraphics(w, h, Legacy.instance().get2DRenderMode());
		pg.beginDraw();

		pg.smooth();
		pg.background(250);

		pg.noStroke();

		pg.smooth();
		pg.strokeWeight(1);
		pg.stroke(200, 200, 200);
		pg.noFill();

		roundrect(pg, 0, 0, w + 4, h + 4, 4); //right bottom border off texture
		int offsetX = (int) ((w - img.width) / 2);
		int offsetY = (int) ((h - img.height) / 2);
		
		pg.fill(255);

		if(img != null)
		pg.image(img, (int)offsetX, (int)offsetY);
		
		pg.endDraw();
		PImage returnImage = pg.get();

		//load into the cache 
		appletStatic.image(returnImage, -1000, -1000);
		return returnImage;

	}

	public static PImage makeImgOver(PImage img) {
		return makeImgOver(img.width, img.height, img);
	}

	public static PImage makeImgUp(float w, float h, PImage img) {
		
		
		PGraphics pg = appletStatic.createGraphics((int) w, (int) h,
				Legacy.instance().get2DRenderMode() );

		pg.beginDraw();
		pg.background(250);
		pg.smooth();
		
		pg.noStroke();

		pg.stroke(200, 200, 200);
		//pg.fill(255, 255, 255);
		pg.fill(255);
		int offsetX = (int) ((w - img.width) / 2);
		int offsetY = (int) ((h - img.height) / 2);
		
		if(img != null)
		pg.image(img, (int)offsetX, (int)offsetY);
		pg.endDraw();
		PImage returnImg = pg.get();

		//load into the cache 
		appletStatic.image(returnImg, -1000, -1000);		
		
		return returnImg;

	}

	static void roundrect(PGraphics g, float x, float y, float w, float h,
			float r) {
/*
 *http://quasipartikel.at/2010/01/07/quadratic-bezier-curves-for-processingjs/
		g.beginShape();
		g.vertex(x+r, y);
		g.vertex(x+w-r, y);
		quadraticBezierVertex(g,x+w, y, x+w, y+r,x+w-r,y);
		g.vertex(x+w, y+h-r);
		  quadraticBezierVertex(g,x+w, y+h, x+w-r, y+h,x+w, y+h-r);
		  g.vertex(x+r, y+h);
		  quadraticBezierVertex(g,x, y+h, x, y+h-r,x+r, y+h);
		  g.vertex(x, y+r);
		  quadraticBezierVertex(g,x, y, x+r, y,x, y+r);
		  g.endShape();
		  
		*/
		g.beginShape();
		g.vertex(x + r, y);
		g.vertex(x + w - r, y);
		g.bezierVertex(x + w, y, x + w, y + r, x + w, y + r);

		g.vertex(x + w, y + r);
		g.vertex(x + w, y + h - r);
		g.bezierVertex(x + w, y + h, x + w - r, y + h, x + w - r, y + h);

		g.vertex(x + w - r, y + h);
		g.vertex(x + r, y + h);
		g.bezierVertex(x, y + h, x, y + h - r, x, y + h - r);

		g.vertex(x, y + h - r);
		g.vertex(x, y + r);
		g.bezierVertex(x, y, x + r, y, x + r, y);
		g.endShape();

		/*
		//line 1
		g.vertex(x, y+h-r);
		g.vertex(x, y+r);
		g.bezierVertex(x, y, x+r, y, x+r, y);
		
		
		//line 1
		g.vertex(x+r, y);
		g.vertex(x+w-r, y);
		
		g.bezierVertex(x+w, y,x+w, y+r, x+w, y+r);
		
		
		//line 1
		g.vertex(x+w, y+r);
		g.vertex(x+w, y+h-r);
		
		g.bezierVertex(x+w, y+h, x+w-r, y+h, x+w-r, y+h);
		
		
		//line 1
		g.vertex(x+w-r, y+h);
		g.vertex(x+r, y+h);
		
		
		g.bezierVertex(x, y+h, x, y+h-r, x, y+h-r);
		*/

		g.endShape(g.CLOSE);

	}
	
	static void quadraticBezierVertex(PGraphics g, float cpx, float cpy, float x, float y, float prevX ,float prevY) {
		  float cp1x = (float) (prevX + 2.0/3.0*(cpx - prevX));
		  float cp1y = (float) (prevY + 2.0/3.0*(cpy - prevY));
		  float cp2x = (float) (cp1x + (x - prevX)/3.0);
		  float cp2y = (float) (cp1y + (y - prevY)/3.0);

		  // finally call cubic Bezier curve function
		  g.bezierVertex(cp1x, cp1y, cp2x, cp2y, x, y);
		};
		
		

	static void roundrectLeft(PGraphics g, float x, float y, float w, float h,
			float r) {

		g.beginShape();

		g.vertex(x + r, y + h);
		g.bezierVertex(x, y + h, x, y + h - r, x, y + h - r);

		g.vertex(x, y + h - r);
		g.vertex(x, y + r);
		g.bezierVertex(x, y, x + r, y, x + r, y);

		g.endShape(g.OPEN);

	}

	public GUIComponents components = new GUIComponents();
	public PFont myFontMedium = null;

	public boolean performanceMode = true;

	public boolean physics_on = false;

	public boolean useAlphaMouseOver = false;

	private boolean registeredEvent = false;

	public float labelSize = 12;

	public ModalGUI() {

	}

	public ModalGUI(PApplet main) {
		this.setup(main);

	}

	public void add(GUIComponent component) {
		component.setController(this);
		components.add(component);
	}

	public boolean hasFocus() {
		return components.hasFocus();

	}
	
	
	public void keyEvent(processing.event.KeyEvent e) {
		keyEvent(new KeyEventSK(e));
    }
	
	
	 public void keyEvent(java.awt.event.KeyEvent e) {
		 keyEvent(new KeyEventSK(e));
	 }
	public void keyEvent(KeyEventSK keyevent) {
		this.components.keyEvent(keyevent);

	}
	
	
	
	public void mouseEvent(processing.event.MouseEvent e) {
	    	mouseEvent(new MouseEventSK(e));
	    }
	  
	
    public void mouseEvent(java.awt.event.MouseEvent e) {
    	mouseEvent(new MouseEventSK(e));
    }
	public void mouseEvent(MouseEventSK e) {
		
		
		//LOGGER.debug("mouseEvent: " + e.getAction());
		
		this.components.mouseEvent(e);
		if (e.getAction() == MouseEventSK.CLICK) {
			
			reRender();

			
			if (overComponent())
				clickStartedOn = true;
			
		} else if (e.getAction() == MouseEventSK.RELEASE) {
			reRender();

				clickStartedOn = false;
		}
		
		
	}
	
		
	
	public void mouseWheelMoved(MouseWheelEventSK e) {
		this.components.mouseWheelMoved(e);

	}
	
	

	public boolean overComponent() {
		return components.overComponent();
	}

	
	public boolean clickStartedOn() {
		//return clickStartedOn;
		return false;
	}

	public void reload() {
		this.setup(this.applet);
	}

	public void render(PGraphics g) {
		

		components.render(g);
		/*

		if(rebuildStencilBuffer && renderOnUpdate){
			rebuildStencilBuffer= false;
			
			GL gl = (g);
			  GLCapabilities capabilities = new GLCapabilities();
			  int bits = capabilities.getStencilBits();
			  capabilities.setStencilBits(8);
			  bits = capabilities.getStencilBits();
			  IntBuffer i = IntBuffer.allocate(100);

			  
			  gl.glClearStencil(0x0);  
			  gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
			  gl.glClear( GL.GL_STENCIL_BUFFER_BIT);
			  gl.glDisable(GL.GL_DEPTH_TEST);

			  gl.glGetIntegerv(GL.GL_STENCIL_BITS, i);
			  gl.glColorMask(false, false, false, false); 

			  
			  	gl.glEnable(GL.GL_STENCIL_TEST);
				gl.glStencilFunc(GL.GL_ALWAYS, 1, 1);                     // Always Passes, 1 Bit Plane, 1 As Mask
				gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE); 
				  gl.glDisable(GL.GL_DEPTH_TEST);
				  
				  components.reRender();
				  components.render(g);
				  components.reRender();
				gl.glStencilFunc(GL.GL_EQUAL,1, 0xFFFFFFFF);     // mask  

			
		}
	*/
	}
	
	public void reRender(){

		components.reRender();
	}

	public void reBuildStencilBuffer(){
		rebuildStencilBuffer = true;
	}
	
	public void reset() {
		
		this.components.reset();
	   this.reBuildStencilBuffer();

	}

	public void setup(PApplet main) {
		this.applet = main;
		this.appletStatic = main;

		components = new GUIComponents();
		//	URL res = cl.getResource("data/TrebuchetMS-12.vlw");
		/*
		try {
			//InputStream stream = res.openStream();
			
			//InputStream input = new FileInputStream(res.getPath());
			InputStream input = cl.getResourceAsStream("data/HelveticaNeueLT-Medium-48.vlw");
		     // String cn = input.getClass().getName();
		      //System.out.println(cn);
		      
		      DataInputStream is = new DataInputStream(input);

			this.myFontMedium =  new PFont(input);
			
			//if(stream.available() > 0){
		    //this.myFont =  new PFont(stream);
			//}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

		//applet.textMode(PApplet.SCREEN);

		if (!registeredEvent) {
			
			//Legacy.registerMethod("mouseEvent",this);
			//Legacy.registerMethod("keyEvent",this);

			Legacy.registerMouseEvent(main,this);
			Legacy.registerKeyEvent(main,this);
			Legacy.addMouseWheelListener(main,this);
			registeredEvent = true;
		

		}
	}

	public boolean textfieldHasFocus() {
		return components.textfieldHasFocus();
	}

	public void update() {
		this.components.update();
	}

	public static String loadedCursor = null;
	public static void setCursor(PApplet applet, PImage _cursorImg){

		if(loadedCursor == null || !loadedCursor.equals(_cursorImg.toString()) ){
			loadedCursor = _cursorImg.toString();
			applet.cursor(_cursorImg,(int)(_cursorImg.width/2),(int)(_cursorImg.height/2));
		}
	}
	
	public static void setCursor(PApplet applet, PImage _cursorImg, int _x, int _y){

		if(loadedCursor == null || !loadedCursor.equals(_cursorImg.toString()) ){
			loadedCursor = _cursorImg.toString();
			applet.cursor(_cursorImg,_x,_y);
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		
	}


}
