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

import toxi.geom.Vec2D;


/**
 * A point on a path, can contain bezier control handles. 
 * @author gregsaul
 *
 */
public class spPoint extends Vec2D {

	public Vec2D controlPoint1 = null;
	public Vec2D controlPoint2 = null;

	public spPoint(float x, float y) {
		super(x, y);
		this.controlPoint1 = new Vec2D(x, y);
		this.controlPoint2 = new Vec2D(x, y);
	}

	public spPoint(Vec2D vec) {
		this.x = vec.x;
		this.y = vec.y;

		this.controlPoint1 = new Vec2D(vec.x, vec.y);
		this.controlPoint2 = new Vec2D(vec.x, vec.y);
	}

	@Override
	protected spPoint clone() {
		spPoint returnPoint = new spPoint(this.x, this.y);

		if (this.controlPoint1 != null)
			returnPoint.controlPoint1 = new Vec2D(this.controlPoint1.x,
					this.controlPoint1.y);

		if (this.controlPoint2 != null)
			returnPoint.controlPoint2 = new Vec2D(this.controlPoint2.x,
					this.controlPoint2.y);
		return returnPoint;

	}

	public boolean containsBezier() {
		if (controlPoint2 != null || controlPoint1 != null)
			return true;
		else
			return false;
	}

	public Vec2D getControlPoint1() {
		if (controlPoint1 == null)
			return new Vec2D(this.x, this.y);
		else
			return controlPoint1;
	}

	public Vec2D getControlPoint2() {
		if (controlPoint2 == null)
			return new Vec2D(this.x, this.y);
		else
			return controlPoint2;
	}

	public Vec2D scaleSelff(float scale) {

		if (this.controlPoint1 != null)
			controlPoint1.scaleSelf(scale);

		if (this.controlPoint2 != null)
			controlPoint2.scaleSelf(scale);

		return super.scaleSelf(scale);

	}
}
