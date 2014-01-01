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
//#IF JAVA
package cc.sketchchair.core;
import java.awt.Component;
import java.awt.Frame;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PGraphicsOpenGL;


/**
 * Main program file to start Sketch in standalone mode, this is good for debugging and can be used to test compatibility with javascript without relying on jBullet. 
 * @author gregsaul
 *
 */
public class simpleTest extends PApplet {
	
	PGraphics pg;

	
	
	public static void main(String args[]) {
		PApplet.main(new String[] { simpleTest.class.getName() });
	}


	public void draw() {
		
		PGraphics renderer = (PGraphics)g;

		
		
		renderer.pushMatrix();
		renderer.hint(Legacy.DISABLE_STROKE_PERSPECTIVE);
		renderer.perspective();


		//renderer.hint(DISABLE_ACCURATE_2D); // disable depth testing so that we

		renderer.hint(ENABLE_DEPTH_TEST); // disable depth testing so that we

	//	ortho(-(int)(width / 2), (int)(width / 2), -(int)(height / 2), (int)(height / 2),
	//			-10000, 10000);


		renderer.background(255);
		renderer.fill(255,0,0);
		renderer.translate(width/2,height/2);
		renderer.rotateY(((float)millis())/100.0f);
		renderer.ellipse(0,0,50,50);
		renderer.popMatrix();


pg.beginDraw();
pg.background(102);
pg.stroke(255);
pg.line(pg.width*0.5f, pg.height*0.5f, mouseX, mouseY);
pg.endDraw();
renderer.image(pg, 50, 50); 
	}

	public void setup() {
		size(600, 600, OPENGL);
		//smooth(4);
		////noSmooth();
		
		// Detect current openGL version and warn if necessary 
		//((PGraphicsOpenGL) this.g).endDraw();
		
		  pg = createGraphics(100, 100, P3D);

		  //his.frame.getParent()ABORT;
		  
		 this.frame.setLocation(20,29);
			frameRate(120);
			

	}
	
	

	
	

}
