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
package cc.sketchchair.core;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.DebugDrawModes;
import com.bulletphysics.linearmath.IDebugDraw;

import processing.core.PGraphics;

/** 
 * Debug draw class for jbullet physics engine.
 * @author gregsaul
 *
 */
public class IDebugDrawMe extends IDebugDraw {
	PGraphics g;

	public IDebugDrawMe(PGraphics g) {
		this.g = g;

	}

	@Override
	public void draw3dText(Vector3f location, String textString) {
		//System.out.println("text");

	}

	@Override
	public void drawAabb(Vector3f from, Vector3f to, Vector3f color) {
		g.pushMatrix();
		g.translate(from.x, from.y, from.z);
		g.box(from.x - to.x, from.y - to.y, from.z - to.z);
		g.popMatrix();
		// System.out.println("aabb");
	}

	@Override
	public void drawContactPoint(Vector3f PointOnB, Vector3f normalOnB,
			float distance, int lifeTime, Vector3f color) {
	}

	@Override
	public void drawLine(Vector3f from, Vector3f to, Vector3f color) {
		g.stroke(255, 0, 0);
		g.strokeWeight(1);
		g.line(from.x, from.y, from.z, to.x, to.y, to.z);

	}

	@Override
	public void drawTriangle(Vector3f v0, Vector3f v1, Vector3f v2,
			Vector3f color, float alpha) {
	}

	@Override
	public void drawTriangle(Vector3f v0, Vector3f v1, Vector3f v2,
			Vector3f n0, Vector3f n1, Vector3f n2, Vector3f color, float alpha) {
	}

	@Override
	public int getDebugMode() {
		// TODO Auto-generated method stub
		//return 0;
		if (SETTINGS.draw_collision_mesh)
			return DebugDrawModes.MAX_DEBUG_DRAW_MODE;
		else
			return 0;

	}

	@Override
	public void reportErrorWarning(String warningString) {
		System.out.println(warningString);
	}

	@Override
	public void setDebugMode(int arg0) {
		// TODO Auto-generated method stub

	}

}
