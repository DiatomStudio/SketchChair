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
import processing.opengl.PGL;
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
		



		  background(255);
fill(255,0,0);
pushMatrix();
translate(width/2,height/2);
rotateY(((float)millis())/100.0f);
ellipse(0,0,50,50);
popMatrix();


	}

	public void setup() {
		size(600, 600, OPENGL);
		//smooth(4);
		//noSmooth();
		
		// Detect current openGL version and warn if necessary 
		//((PGraphicsOpenGL) this.g).endDraw();
	

		
		 
		  //his.frame.getParent()ABORT;
		  
		 this.frame.setLocation(20,29);
		  
	}
	
	

	
	

}
