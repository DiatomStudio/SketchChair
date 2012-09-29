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
package cc.sketchchair.environments;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.MeasureTool;
import cc.sketchchair.core.UITools;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;

/**
 * Environments are 2d textures loaded from png's that can be places of a sketchPlane for reference. 
 *
 * @author gregsaul
 *
 */
public class Environment {
	float scale = 1f;
	public Vec2D pos = new Vec2D(400, 150);
	Vec2D posDelta = new Vec2D(0, 0);

	private PImage img = null;
	public boolean visible = true;
	public boolean beingDragged = false;
	public boolean beingScaled = false;
	public String fileName;
	boolean destroy;
	private int minX = -1;
	private int maxX = -1;
	private int minY = -1;
	private int maxY = -1;
	public String linkedChair = null;
	public boolean beingPushed = false;
	private float zPos = 0;

	Environment(PImage imgIn) {
		this.img = imgIn;
		this.getMinMax();
	}

	public Environment(String fileName, PApplet applet) {
		this.fileName = fileName;
		this.img = applet.loadImage(fileName);

		if (this.img == null)
			destroy();
		else
			this.getMinMax();

	}

	public void destroy() {
		this.destroy = true;
	}

	void getMinMax() {
		img.loadPixels();

		for (int x = 0; x < img.width; x++) {
			for (int y = 0; y < img.height; y++) {

				int col = img.get(x, y);

				if (col != 16777215) {
					if (x > this.maxX || this.maxX == -1)
						this.maxX = x;

					if (x < this.minX || this.minX == -1)
						this.minX = x;

					if (y > this.maxY || this.maxY == -1)
						this.maxY = y;

					if (y < this.minY || this.minY == -1)
						this.minY = y;

				}

			}

		}

	}

	boolean isOver(Vec2D p) {
		//System.out.println(p + " " + pos);
		LOGGER.info("isOver?");
		if (p.x < pos.x || p.x > pos.x + (img.width * this.scale)
				|| p.y < pos.y || p.y > pos.y + (img.height * this.scale)) {
			LOGGER.info("not in bounds" + p.x + " " + pos.x);

			return false;

		}

		img.loadPixels();
		int col = img.get((int) ((p.x - pos.x) / this.scale),
				(int) ((p.y - pos.y) / this.scale));

		LOGGER.info("COLOR" + col);
		//System.out.println((int)((p.x-pos.x)*this.scale)+ " " +(int)((p.y-pos.y)*this.scale) + "col" + GLOBAL.applet.brightness(col));

		if (GLOBAL.applet.brightness(col) != 0
				&& !GLOBAL.person.clickedOnPerson)
			return true;
		else
			return false;

	}

	public void render(PGraphics g) {
		// TODO Auto-generated method stub
		g.pushMatrix();
		g.translate(pos.x, pos.y, zPos);
		g.scale(this.scale);
		g.image(this.img, 0, 0);
		g.popMatrix();

		if (beingScaled) {
			MeasureTool.measure(pos.x + (this.maxX * this.scale), pos.x
					+ (this.minX * this.scale), pos.y
					+ (this.maxY * this.scale), pos.y
					+ (this.minY * this.scale), g);

		}
	}

	void update() {
		if (beingDragged) {
			pos.x += (GLOBAL.uiTools.mouseX - GLOBAL.uiTools.pmouseX)
					/ GLOBAL.getZOOM();
			pos.y += (GLOBAL.uiTools.mouseY - GLOBAL.uiTools.pmouseY)
					/ GLOBAL.getZOOM();

			posDelta.x += (GLOBAL.uiTools.mouseX - GLOBAL.uiTools.pmouseX)
					/ GLOBAL.getZOOM();
			posDelta.y += (GLOBAL.uiTools.mouseY - GLOBAL.uiTools.pmouseY)
					/ GLOBAL.getZOOM();

		}

		if (beingScaled) {
			scale += ((GLOBAL.uiTools.mouseX - GLOBAL.uiTools.pmouseX) / GLOBAL
					.getZOOM()) * .01f;
			scale += ((GLOBAL.uiTools.mouseY - GLOBAL.uiTools.pmouseY) / GLOBAL
					.getZOOM()) * .01f;
		}

		if (beingPushed) {
			zPos += ((GLOBAL.uiTools.mouseX - GLOBAL.uiTools.pmouseX) / GLOBAL
					.getZOOM());
		}

	}

	void wakeUp() {

		if (this.linkedChair != null) {
			GLOBAL.sketchChairs.hybernate();
			UITools.load(this.linkedChair);

			if (GLOBAL.sketchChairs.getCurChair() != null) {
				GLOBAL.sketchChairs.getCurChair().drag(
						new Vec3D(posDelta.x, posDelta.y, 0));
				//float height = (this.maxY - minY)*this.scale; 
				//GLOBAL.sketchChairs.curChair.setHeight(height);
			}
			this.destroy();
		}

	}
}
