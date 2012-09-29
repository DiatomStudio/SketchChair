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
package ShapePacking;

import java.io.Serializable;

import toxi.geom.Vec2D;

/**
 * Bezier control nodes on a path.
 * @author gregsaul
 *
 */
public class BezierControlNode implements Serializable {
	public Vec2D c1 = new Vec2D(0, 0);
	public Vec2D c2 = new Vec2D(0, 0);

	public BezierControlNode(Vec2D c1In, Vec2D c2In) {
		this.c1 = new Vec2D(c1In.x, c1In.y);
		this.c2 = new Vec2D(c2In.x, c2In.y);

	}

	@Override
	public BezierControlNode clone() {
		return new BezierControlNode(new Vec2D(this.c1.x, this.c1.y),
				new Vec2D(this.c2.x, this.c2.y));

	}

}
