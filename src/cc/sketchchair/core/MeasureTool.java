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

import processing.core.PConstants;
import processing.core.PGraphics;
import toxi.geom.Vec2D;

/**
 * Measure tool used to measure anything in SketchChair. 
 * @author gregsaul
 *
 */
public class MeasureTool implements Clickable {

	public static void measure(float maxX, float minX, float maxY, float minY,
			PGraphics g) {

		float measureLineLen = 30;
		float measureLineModelOffset = 3;
		float measureLineProportion = .9f;

		float arrowWidth = 20;
		float arrowHeight = 50;

		float lenX = (Math.abs(maxX - minX) * SETTINGS.scale);
		float lenY = (Math.abs(maxY - minY) * SETTINGS.scale);

		lenX = Math.round(lenX);
		lenY = Math.round(lenY);

		g.pushMatrix();
		g.strokeWeight(1f);
		g.stroke(0);
		g.fill(0);
		g.line(minX - measureLineModelOffset, maxY, minX - measureLineLen
				+ measureLineModelOffset, maxY);
		g.line(minX - measureLineModelOffset, minY, minX - measureLineLen
				+ measureLineModelOffset, minY);

		Vec2D arrowSideBottom = new Vec2D(minX + measureLineModelOffset
				- (measureLineLen * measureLineProportion), maxY);
		Vec2D arrowSideTop = new Vec2D(minX + measureLineModelOffset
				- (measureLineLen * measureLineProportion), minY);

		g.line(arrowSideTop.x, arrowSideTop.y, arrowSideBottom.x,
				arrowSideBottom.y);
		g.triangle(arrowSideTop.x, arrowSideTop.y, arrowSideTop.x + arrowWidth
				/ 2, arrowSideTop.y + arrowHeight, arrowSideTop.x - arrowWidth
				/ 2, arrowSideTop.y + arrowHeight);
		g.triangle(arrowSideBottom.x, arrowSideBottom.y, arrowSideBottom.x
				+ arrowWidth / 2, arrowSideBottom.y - arrowHeight,
				arrowSideBottom.x - arrowWidth / 2, arrowSideBottom.y
						- arrowHeight);

		Vec2D texPosY = arrowSideBottom.copy().sub(
				arrowSideBottom.sub(arrowSideTop).scale(.5f));
		texPosY.x -= 30;
		g.textAlignY = PConstants.CENTER;
		g.textSize((float) (12f / GLOBAL.getZOOM()));
		g.text(Float.toString(lenY) + " :mm", texPosY.x, texPosY.y);

		g.line(minX, minY - measureLineModelOffset, minX, minY - measureLineLen
				+ measureLineModelOffset);
		g.line(maxX, minY - measureLineModelOffset, maxX, minY - measureLineLen
				+ measureLineModelOffset);

		Vec2D arrowTopLeft = new Vec2D(minX, minY + measureLineModelOffset
				- (measureLineLen * measureLineProportion));
		Vec2D arrowTopRight = new Vec2D(maxX, minY + measureLineModelOffset
				- (measureLineLen * measureLineProportion));

		g.line(arrowTopLeft.x, arrowTopLeft.y, arrowTopRight.x, arrowTopRight.y);
		g.triangle(arrowTopLeft.x, arrowTopLeft.y,
				arrowTopLeft.x + arrowHeight, arrowTopLeft.y + arrowWidth / 2,
				arrowTopLeft.x + arrowHeight, arrowTopLeft.y - arrowWidth / 2);
		g.triangle(arrowTopRight.x, arrowTopRight.y, arrowTopRight.x
				- arrowHeight, arrowTopRight.y + arrowWidth / 2,
				arrowTopRight.x - arrowHeight, arrowTopRight.y - arrowWidth / 2);

		Vec2D textPosX = arrowTopLeft.copy().sub(
				arrowTopLeft.sub(arrowTopRight).scale(.5f));
		textPosX.y -= 10;
		g.textAlign = PConstants.CENTER;

		g.text(Float.toString(lenX) + " :mm", textPosX.x, textPosX.y);

		g.popMatrix();

	}

	Vec2D startPoint = null;

	Vec2D currentPoint = null;

	//@Override
	public void mouseClicked() {
		// TODO Auto-generated method stub

	}

	//@Override
	public void mouseDragged() {
		// TODO Auto-generated method stub

	}

	//@Override
	public void mousePressed() {
		// TODO Auto-generated method stub

	}

	public void mousePressed(float mouseX, float mouseY) {

		if (GLOBAL.uiTools.getCurrentTool() == UITools.MEASURE_TOOL) {
			if (startPoint == null)
				startPoint = new Vec2D(mouseX, mouseY);

			this.currentPoint = new Vec2D(mouseX, mouseY);
		}
	}

	//@Override
	public void mouseReleased() {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(int mouseX, int mouseY) {
		// TODO Auto-generated method stub
		startPoint = null;
	}

	public void render(PGraphics g) {

		if (startPoint != null) {

			MeasureTool.measure(Math.max(startPoint.x, currentPoint.x),
					Math.min(startPoint.x, currentPoint.x),
					Math.max(startPoint.y, currentPoint.y),
					Math.min(startPoint.y, currentPoint.y), g);
			g.stroke(0);
			g.noFill();
			g.rect(startPoint.x, startPoint.y, currentPoint.x - startPoint.x,
					currentPoint.y - startPoint.y);
		}
	}

}
