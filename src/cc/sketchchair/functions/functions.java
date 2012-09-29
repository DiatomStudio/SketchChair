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
package cc.sketchchair.functions;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.vecmath.Matrix4f;

import com.bulletphysics.linearmath.Transform;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.sketch.SketchPoint;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Vec2D;

/**
 * Static non specific functions used in Sketchchair.
 * @author gregsaul
 *
 */
public class functions {
	public static int DONT_INTERSECT = 0;
	public static int COLLINEAR = 1;
	public static int DO_INTERSECT = 2;

	public static float x = 0;
	public static float y = 0;

	public static float angleOf(Vec2D v1) {
		v1.normalize();
		float an = (float) Math.atan2(v1.y, v1.x);

		if (an > 0.0) {
			an = (float) (Math.PI + (Math.PI - an));
		} else {
			// no negative nums
			an = (float) (Math.PI - (Math.PI - Math.abs(an)));
		}

		return an;
	}

	public static float angleOfDot(Vec2D v1) {
		v1.normalize();

		float an = (float) Math.acos(v1.dot(new Vec2D(0, 0)));

		return (float) Math.atan2(v1.y, v1.x);

	}

	public static float bezierPoint(float a, float b, float c, float d, float t) {
		float t1 = 1.0f - t;
		return a * t1 * t1 * t1 + 3 * b * t * t1 * t1 + 3 * c * t * t * t1 + d
				* t * t * t;
	}

	/*
	public static void drawAllSprings(VerletPhysics physics, PGraphics g){
	  g.stroke(255,0,0);
	g.strokeWeight(1f);
	
	  for ( int i = 0; i < physics.springs.size(); ++i )
	 {
	
	VerletSpring s = (VerletSpring) physics.springs.get( i );
	    g.line(s.a.x, s.a.y,s.a.z,s.b.x, s.b.y,s.b.z);
	
	  }


	}


	void drawAllParticles(VerletPhysics physics, PGraphics g){
	    g.stroke(0,0,0,100);
	
	  for ( int i = 0; i < physics.particles.size(); ++i )
	  {
	
	VerletParticle p = (VerletParticle) physics.particles.get( i );
	g.sphereDetail(2);
	g.pushMatrix();
	g.translate(p.x,p.y,p.z);
	g.sphere(p.weight*2);
	g.popMatrix();
	
	
	  }
	
	}
	/*

	public static void boundAllParticles(VerletPhysics physics){

	for ( int i = 0; i < physics.particles.size(); ++i )
	{
	VerletParticle p = (VerletParticle) physics.particles.get( i );

	if(p.y > 550)
	p.y -= p.y-550;

	}

	}
	*/

	public final static int color(int x, int y, int z) {

		if (x > 255)
			x = 255;
		else if (x < 0)
			x = 0;
		if (y > 255)
			y = 255;
		else if (y < 0)
			y = 0;
		if (z > 255)
			z = 255;
		else if (z < 0)
			z = 0;

		return 0xff000000 | (x << 16) | (y << 8) | z;

	}

	public static float curvePoint(float x2, float x3, float x4, float x5,
			float t) {
		// TODO Auto-generated method stub
		return 0;
	}

	public static void cylinder(float w1, float w2, float h, int sides,
			PGraphics g) {
		/*
		float angle;
		
		float[] x1 = new float[sides + 1];
		float[] z1 = new float[sides + 1];

		float[] x2 = new float[sides + 1];
		float[] z2 = new float[sides + 1];

		//get the x and z position on a circle for all the sides
		for (int i = 0; i < x1.length; i++) {
			angle = (float) ((Math.PI * 2) / (sides) * i + 1);
			x1[i] = (float) (Math.sin(angle) * w1);
			z1[i] = (float) (Math.cos(angle) * w1);
		}

		//get the x and z position on a circle for all the sides
		for (int i = 0; i < x2.length; i++) {
			angle = (float) ((Math.PI * 2) / (sides) * i + 1);
			x2[i] = (float) (Math.sin(angle) * w2);
			z2[i] = (float) (Math.cos(angle) * w2);
		}

		//draw the top of the cylinder
		g.beginShape(PConstants.TRIANGLE_FAN);

		g.vertex(0, -h / 2, 0);

		for (int i = 0; i < x1.length; i++) {
			g.vertex(x1[i], -h / 2, z1[i]);
		}

		g.endShape();

		//draw the center of the cylinder
		g.beginShape(PConstants.QUAD_STRIP);

		for (int i = 0; i < x1.length; i++) {
			g.vertex(x1[i], -h / 2, z1[i]);
			g.vertex(x2[i], h / 2, z2[i]);
		}

		g.endShape();

		//draw the bottom of the cylinder
		g.beginShape(PConstants.TRIANGLE_FAN);

		g.vertex(0, h / 2, 0);

		for (int i = 0; i < x2.length; i++) {
			g.vertex(x2[i], h / 2, z2[i]);
		}

		g.endShape();
		*/
	}

	public static void cylinder(float w, float h, int sides, PGraphics g) {
		float angle;
		float[] x = new float[sides + 1];
		float[] z = new float[sides + 1];

		//get the x and z position on a circle for all the sides
		for (int i = 0; i < x.length; i++) {
			angle = (float) ((Math.PI * 2) / (sides) * i);
			x[i] = (float) (Math.sin(angle) * w);
			z[i] = (float) (Math.cos(angle) * w);
		}

		//draw the top of the cylinder
		g.beginShape(PConstants.TRIANGLE_FAN);

		g.vertex(0, -h / 2, 0);

		for (int i = 0; i < x.length; i++) {
			g.vertex(x[i], -h / 2, z[i]);
		}

		g.endShape();

		//draw the center of the cylinder
		g.beginShape(PConstants.QUAD_STRIP);

		for (int i = 0; i < x.length; i++) {
			g.vertex(x[i], -h / 2, z[i]);
			g.vertex(x[i], h / 2, z[i]);
		}

		g.endShape();

		//draw the bottom of the cylinder
		g.beginShape(PConstants.TRIANGLE_FAN);

		g.vertex(0, h / 2, 0);

		for (int i = 0; i < x.length; i++) {
			g.vertex(x[i], h / 2, z[i]);
		}

		g.endShape();
	}

	
	public static void flatCylinder(float r1, float r2, float len, Transform myTransform, PGraphics g) {
		PMatrix3D worldMatrix = new PMatrix3D();
		float matrixWorldScale = (float) (GLOBAL.getZOOM()*70.0f);//Where does this come from? 
		List points = new ArrayList();
		//g.beginShape();

		
		g.pushMatrix();
		g.translate(0, -len/2.0f, 0);
		float screenX1 = g.screenX(0,0,0);
		float screenY1 = g.screenY(0,0,0);
		float screenZ1 = g.screenZ(0,0,0);
		


		
		g.popMatrix();
		
		g.pushMatrix();
		g.translate(0, len/2.0f, 0);
		float screenX2 = g.screenX(0,0,0);
		float screenY2 = g.screenY(0,0,0);
		float screenZ2 = g.screenZ(0,0,0);

		g.popMatrix();
		
		
		float atan = (float)Math.atan((screenY1-screenY2)/(screenX1-screenX2));

			
		
		g.pushMatrix();
		g.translate(0, -len/2.0f-0.1f, 0);

		 worldMatrix = new PMatrix3D();
		g.getMatrix(worldMatrix);
		worldMatrix.m00 = matrixWorldScale;worldMatrix.m01 = 0;worldMatrix.m02 = 0;
		worldMatrix.m10 = 0;worldMatrix.m11 = matrixWorldScale;worldMatrix.m12 = 0;
		g.setMatrix(worldMatrix);
		g.rotate(atan);
		float worldX1 = g.screenX(0,0,0);
		float worldY1 = g.screenY(0,0,0);
		float worldZ1 = g.screenZ(0,0,0);
		
		g.popMatrix();
		
		
		g.pushMatrix();
		g.translate(0, len/2.0f+0.1f, 0);

		 worldMatrix = new PMatrix3D();
		g.getMatrix(worldMatrix);
		worldMatrix.m00 = matrixWorldScale;worldMatrix.m01 = 0;worldMatrix.m02 = 0;
		worldMatrix.m10 = 0;worldMatrix.m11 = matrixWorldScale;worldMatrix.m12 = 0;
		g.setMatrix(worldMatrix);
		g.rotate(atan);
		float worldX2 = g.screenX(0,0,0);
		float worldY2 = g.screenY(0,0,0);
		float worldZ2 = g.screenZ(0,0,0);
		
		g.popMatrix();
		

		float worldDist =(float) Math.sqrt(Math.pow(worldX2-worldX1,2) + Math.pow(worldY2-worldY1,2)+ Math.pow(worldZ2-worldZ1,2) )/matrixWorldScale;
	
if(screenX1 < screenX2)
	atan -= (float)(Math.PI/2);
else
	atan += (float)(Math.PI/2);


		//side curve
		g.pushMatrix();
		g.translate(0, -len/2.0f-0.1f, 0);

		 worldMatrix = new PMatrix3D();
		g.getMatrix(worldMatrix);
		//billboard matrix
		worldMatrix.m00 = matrixWorldScale;worldMatrix.m01 = 0;worldMatrix.m02 = 0;
		worldMatrix.m10 = 0;worldMatrix.m11 = matrixWorldScale;worldMatrix.m12 = 0;
		g.setMatrix(worldMatrix);
		g.rotate(atan);

		
		
		g.noStroke();
		g.fill(228);
		g.beginShape();
		g.vertex(-r1/2, 0,-0.1f);
		g.vertex( -r2/2, worldDist,-0.1f);
		g.vertex(r2/2, worldDist,-0.1f);
		g.vertex(r1/2, 0,-0.1f);
		g.endShape(PApplet.CLOSE);
		
		g.noFill();
		g.stroke(0);
		g.strokeWeight(2);
		
		g.line(-r1/2, 0, -r2/2, worldDist);
		g.line(r1/2,0, r2/2, worldDist);
		
		
		

		
		g.popMatrix();

		
		
		
		//Top curve
		g.pushMatrix();
		g.translate(0, -len/2.0f, 0);
		worldMatrix = new PMatrix3D();
		g.getMatrix(worldMatrix);
		//billboard matrix
		worldMatrix.m00 = matrixWorldScale;worldMatrix.m01 = 0;worldMatrix.m02 = 0;
		worldMatrix.m10 = 0;worldMatrix.m11 = matrixWorldScale;worldMatrix.m12 = 0;
		//worldMatrix.m20 = 0;worldMatrix.m21 = 0;worldMatrix.m22 = worldMatrix.m22;
		g.setMatrix(worldMatrix);
		g.rotate(atan);
		
		
		g.noStroke();
		g.fill(228);
		g.beginShape();
		  for(float a=(float)(float) (Math.PI+(Math.PI/2)) ; a >= (float) (Math.PI/2); a -=0.1f) {
			    g.vertex((float)(Math.sin(a)*(r1/2))+0,(float)(Math.cos(a)*(r1/2))+0,-0.1f);
		} 
		g.endShape();
		
		
		g.noFill();
		g.stroke(0);
		g.strokeWeight(2);//top curve weight
		g.beginShape();
		  for(float a=(float)(float) (Math.PI+(Math.PI/2)) ; a >= (float) (Math.PI/2); a -=0.1f) {
			    g.vertex((float)(Math.sin(a)*(r1/2))+0,(float)(Math.cos(a)*(r1/2))+0);
			    points.add(new PVector(g.modelX((float)(Math.sin(a)*(r1/2))+0,(float)(Math.cos(a)*(r1/2))+0,0),
			    		g.modelY((float)(Math.sin(a)*(r1/2))+0,(float)(Math.cos(a)*(r1/2))+0,0),
			    		g.modelZ((float)(Math.sin(a)*(r1/2))+0,(float)(Math.cos(a)*(r1/2))+0,0)));
		} 
		g.endShape();
		
		
		g.strokeWeight(1);
		g.stroke(255,0,0);
		//g.ellipse(0, 0, r1,r1);
		g.popMatrix();
		
		
		
		
		//bottom curve
		g.pushMatrix();
		g.translate(0, len/2.0f, 0);

		 worldMatrix = new PMatrix3D();
		g.getMatrix(worldMatrix);
		//billboard matrix
		worldMatrix.m00 = matrixWorldScale;worldMatrix.m01 = 0;worldMatrix.m02 = 0;
		worldMatrix.m10 = 0;worldMatrix.m11 = matrixWorldScale;worldMatrix.m12 = 0;
		//worldMatrix.m20 = 0;worldMatrix.m21 = 0;worldMatrix.m22 = worldMatrix.m22;

		//g.printMatrix();
		g.setMatrix(worldMatrix);
		g.rotate(atan);

		
		g.noStroke();
		g.fill(228);
		g.beginShape();
		  for(float a= (float)(Math.PI/2); a >= (float) -(Math.PI/2) ; a -=0.1f) {
			    g.vertex((float)(Math.sin(a)*(r2/2))+0,(float)(Math.cos(a)*(r2/2))+0,-0.1f);
			    points.add(new PVector(g.modelX((float)(Math.sin(a)*(r2/2))+0,(float)(Math.cos(a)*(r2/2))+0,0),
			    		g.modelY((float)(Math.sin(a)*(r2/2))+0,(float)(Math.cos(a)*(r2/2))+0,0),
			    		g.modelZ((float)(Math.sin(a)*(r2/2))+0,(float)(Math.cos(a)*(r2/2))+0,0)));
		} 
		  g.endShape();
		
		g.noFill();
		g.stroke(0);
		g.strokeWeight(2);
		
		g.beginShape();
		  for(float a= (float)(Math.PI/2); a >= (float) -(Math.PI/2) ; a -=0.1f) {
			    g.vertex((float)(Math.sin(a)*(r2/2))+0,(float)(Math.cos(a)*(r2/2))+0);
		} 
		  g.endShape();

		g.strokeWeight(1);
		g.stroke(255,0,0);
		//g.ellipse(0, 0, r2,r2);
		
		g.popMatrix();	
		//g.endShape();
		
		g.pushMatrix();
		g.resetMatrix();
		//g.translate(-GLOBAL.windowWidth/2, -GLOBAL.windowHeight/2);
		g.translate(-GLOBAL.windowWidth/2, -GLOBAL.windowHeight/2);
		g.stroke(0);
		g.strokeWeight(3);
		g.fill(255);

		g.beginShape();
		for(int i = 0 ; i < points.size() ; i++){
			PVector p = (PVector)points.get(i);
			//g.vertex(p.x,p.y,p.z/matrixWorldScale);
		}
		g.endShape(PApplet.CLOSE);
		g.popMatrix();
		g.noStroke();
		g.fill(228);
		
	}

	
	public static boolean fileExists(String filename) {

		File file = new File(filename);

		if (!file.exists())
			return false;

		return true;
	}

	public static String getComputerName() {

		return "name";
		/*
		try{
		  String computername=InetAddress.getLocalHost().getHostName();
		return computername;
		}catch (Exception e){
			return null;
		}\*/
	}

	public static String getFileName() {

		Calendar now = Calendar.getInstance();
		Long time = now.getTimeInMillis();
		return time.toString();
	}


	
	public static ByteBuffer getIndexBuffer(int[] indices) {
		ByteBuffer buf = ByteBuffer.allocateDirect(indices.length * 4).order(
				ByteOrder.nativeOrder());
		for (int i = 0; i < indices.length; i++) {
			buf.putInt(indices[i]);
		}
		buf.flip();
		return buf;
	}

	public static ByteBuffer getVertexBuffer(float[] vertices) {
		ByteBuffer buf = ByteBuffer.allocateDirect(vertices.length * 4).order(
				ByteOrder.nativeOrder());
		for (int i = 0; i < vertices.length; i++) {
			buf.putFloat(vertices[i]);
		}
		buf.flip();
		return buf;
	}

	public static int intersect(float x1, float y1, float x2, float y2,
			float x3, float y3, float x4, float y4) {

		float a1, a2, b1, b2, c1, c2;
		float r1, r2, r3, r4;
		float denom, offset, num;

		// Compute a1, b1, c1, where line joining points 1 and 2
		// is "a1 x + b1 y + c1 = 0".
		a1 = y2 - y1;
		b1 = x1 - x2;
		c1 = (x2 * y1) - (x1 * y2);

		// Compute r3 and r4.
		r3 = ((a1 * x3) + (b1 * y3) + c1);
		r4 = ((a1 * x4) + (b1 * y4) + c1);

		// Check signs of r3 and r4. If both point 3 and point 4 lie on
		// same side of line 1, the line segments do not intersect.
		if ((r3 != 0) && (r4 != 0) && same_sign(r3, r4)) {
			return DONT_INTERSECT;
		}

		// Compute a2, b2, c2
		a2 = y4 - y3;
		b2 = x3 - x4;
		c2 = (x4 * y3) - (x3 * y4);

		// Compute r1 and r2
		r1 = (a2 * x1) + (b2 * y1) + c2;
		r2 = (a2 * x2) + (b2 * y2) + c2;

		// Check signs of r1 and r2. If both point 1 and point 2 lie
		// on same side of second line segment, the line segments do
		// not intersect.
		if ((r1 != 0) && (r2 != 0) && (same_sign(r1, r2))) {
			return DONT_INTERSECT;
		}

		//Line segments intersect: compute intersection point.
		denom = (a1 * b2) - (a2 * b1);

		if (denom == 0) {
			return COLLINEAR;
		}

		if (denom < 0) {
			offset = -denom / 2;
		} else {
			offset = denom / 2;
		}

		// The denom/2 is to get rounding instead of truncating. It
		// is added or subtracted to the numerator, depending upon the
		// sign of the numerator.
		num = (b1 * c2) - (b2 * c1);
		if (num < 0) {
			x = (num - offset) / denom;
		} else {
			x = (num + offset) / denom;
		}

		num = (a2 * c1) - (a1 * c2);
		if (num < 0) {
			y = (num - offset) / denom;
		} else {
			y = (num + offset) / denom;
		}

		// lines_intersect
		return DO_INTERSECT;
	}

	public static int intersect(Vec2D a1, Vec2D a2, Vec2D b1, Vec2D b2,
			Vec2D b3, Vec2D b4) {

		if (functions.intersect(a1.x, a1.y, a2.x, a2.y, b1.x, b1.y, b2.x, b2.y) == functions.DO_INTERSECT)
			return functions.DO_INTERSECT;

		if (functions.intersect(a1.x, a1.y, a2.x, a2.y, b2.x, b2.y, b3.x, b3.y) == functions.DO_INTERSECT)
			return functions.DO_INTERSECT;

		if (functions.intersect(a1.x, a1.y, a2.x, a2.y, b3.x, b3.y, b4.x, b4.y) == functions.DO_INTERSECT)
			return functions.DO_INTERSECT;

		if (functions.intersect(a1.x, a1.y, a2.x, a2.y, b4.x, b4.y, b1.x, b1.y) == functions.DO_INTERSECT)
			return functions.DO_INTERSECT;

		return functions.DONT_INTERSECT;
	}

	public static int intersect(Vec2D a1, Vec2D a2, Vec2D a3, Vec2D a4,
			Vec2D b1, Vec2D b2, Vec2D b3, Vec2D b4) {

		if (functions.intersect(a1, a2, b1, b2, b3, b4) == functions.DO_INTERSECT)
			return functions.DO_INTERSECT;

		if (functions.intersect(a2, a3, b1, b2, b3, b4) == functions.DO_INTERSECT)
			return functions.DO_INTERSECT;

		if (functions.intersect(a3, a4, b1, b2, b3, b4) == functions.DO_INTERSECT)
			return functions.DO_INTERSECT;

		if (functions.intersect(a4, a1, b1, b2, b3, b4) == functions.DO_INTERSECT)
			return functions.DO_INTERSECT;

		return functions.DONT_INTERSECT;
	}

	static boolean same_sign(float a, float b) {

		return ((a * b) >= 0);
	}

	public final int color(float x, float y, float z) {

		if (x > 255)
			x = 255;
		else if (x < 0)
			x = 0;
		if (y > 255)
			y = 255;
		else if (y < 0)
			y = 0;
		if (z > 255)
			z = 255;
		else if (z < 0)
			z = 0;

		return 0xff000000 | ((int) x << 16) | ((int) y << 8) | (int) z;

	}

	/**
	 * Creates colors for storing in variables of the <b>color</b> datatype. The parameters are interpreted as RGB or HSB values depending on the current <b>colorMode()</b>. The default mode is RGB values from 0 to 255 and therefore, the function call <b>color(255, 204, 0)</b> will return a bright yellow color. More about how colors are stored can be found in the reference for the <a href="color_datatype.html">color</a> datatype.
	 *
	 * @webref color:creating_reading
	 * @param x red or hue values relative to the current color range
	 * @param y green or saturation values relative to the current color range
	 * @param z blue or brightness values relative to the current color range
	 * @param a alpha relative to current color range
	 *
	 * @see processing.core.PApplet#colorMode(int)
	 * @ref color_datatype
	 */
	public final int color(float x, float y, float z, float a) {
		if (a > 255)
			a = 255;
		else if (a < 0)
			a = 0;
		if (x > 255)
			x = 255;
		else if (x < 0)
			x = 0;
		if (y > 255)
			y = 255;
		else if (y < 0)
			y = 0;
		if (z > 255)
			z = 255;
		else if (z < 0)
			z = 0;

		return ((int) a << 24) | ((int) x << 16) | ((int) y << 8) | (int) z;

	}

	public final static int color(int x, int y, int z, int a) {

		if (a > 255)
			a = 255;
		else if (a < 0)
			a = 0;
		if (x > 255)
			x = 255;
		else if (x < 0)
			x = 0;
		if (y > 255)
			y = 255;
		else if (y < 0)
			y = 0;
		if (z > 255)
			z = 255;
		else if (z < 0)
			z = 0;

		return (a << 24) | (x << 16) | (y << 8) | z;

	}

	public static Vec2D rotate(Vec2D curVec, Vec2D center, float r) {
		Vec2D returnVec = curVec.copy();
		returnVec.subSelf(center);
		returnVec.rotate(r);
		returnVec.addSelf(center);
		return returnVec;
		
		
	}

	public static List getRange(List l, int start,
			int end) {
		List returnList = new ArrayList();
		for(int i = start; i <= end; i++)
			returnList.add(l.get(i));
		
		return returnList;
			}


}
