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

import nu.xom.Attribute;
import nu.xom.Element;
import toxi.geom.Vec2D;

/**
 * Represents a control point on a path.  
 * @author gregsaul
 *
 */
//#ENDIF JAVA

public class SketchPoint extends Vec2D {

	public Vec2D controlPoint1 = null;
	public Vec2D controlPoint2 = null;

	public boolean isOver = false;
	private boolean smooth;

	public SketchPoint(Element element) {

		//wrong type
		if (!element.getLocalName().equals("SketchPoint"))
			return;

		//ERROR checking ?

		if (element.getAttributeValue("x") != null)
			this.x = Float.valueOf(element.getAttributeValue("x"));

		if (element.getAttributeValue("y") != null)
			this.y = Float.valueOf(element.getAttributeValue("y"));

		if (element.getAttributeValue("c1x") != null
				&& element.getAttributeValue("c1x") != null) {
			float xmlC1X = Float.valueOf(element.getAttributeValue("c1x"));
			float xmlC1Y = Float.valueOf(element.getAttributeValue("c1y"));
			this.controlPoint1 = new Vec2D(xmlC1X, xmlC1Y);
		}

		if (element.getAttributeValue("c2x") != null
				&& element.getAttributeValue("c2x") != null) {
			float xmlC2X = Float.valueOf(element.getAttributeValue("c2x"));
			float xmlC2Y = Float.valueOf(element.getAttributeValue("c2y"));
			this.controlPoint2 = new Vec2D(xmlC2X, xmlC2Y);
		}

	}

	public SketchPoint(float x, float y) {
		super(x, y);
	}

	public SketchPoint(Vec2D vec) {
		this.x = vec.x;
		this.y = vec.y;
	}

	public SketchPoint clone() {
		SketchPoint returnPoint = new SketchPoint(this.x, this.y);

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

	public void removeBezier() {
		this.controlPoint1 = null;
		this.controlPoint2 = null;
	}

	public Element toXML() {
		Element element = new Element("SketchPoint");
		element.addAttribute(new Attribute("x", String.valueOf(this.x)));
		element.addAttribute(new Attribute("y", String.valueOf(this.y)));

		if (this.controlPoint1 != null) {
			element.addAttribute(new Attribute("c1x", String
					.valueOf(this.controlPoint1.x)));
			element.addAttribute(new Attribute("c1y", String
					.valueOf(this.controlPoint1.y)));
		}

		if (this.controlPoint2 != null) {
			element.addAttribute(new Attribute("c2x", String
					.valueOf(this.controlPoint2.x)));
			element.addAttribute(new Attribute("c2y", String
					.valueOf(this.controlPoint2.y)));
		}

		return element;
	}

	SketchPoint sub(SketchPoint p ){
	return p;	
	}

	
	

}
