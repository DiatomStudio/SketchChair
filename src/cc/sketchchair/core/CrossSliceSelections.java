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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cc.sketchchair.geometry.SlicePlanes;

import nu.xom.Element;
import processing.core.PGraphics;

/**
 * Container class for CrossSliceSelections.
 * @author gregsaul
 *
 */
public class CrossSliceSelections {
	public List<CrossSliceSelection> l = new ArrayList<CrossSliceSelection>();

	public CrossSliceSelections() {

	}

	public CrossSliceSelections(Element element, SlicePlanes linkedPlanes,
			SketchChair linkedChair) {

		//wrong type
		if (!element.getLocalName().equals("CrossSliceSelections"))
			return;

		for (int i = 0; i < element.getChildCount(); i++) {
			Element child = (Element) element.getChild(i);

			if (child != null
					&& child.getLocalName().equals("CrossSliceSelection")) {
				CrossSliceSelection sliceSelection = new CrossSliceSelection(
						child, linkedPlanes, linkedChair);
				if (sliceSelection != null)
					this.add(sliceSelection);
			}

		}
	}

	public void add(CrossSliceSelection cross) {
		this.l.add(cross);

		//if(GLOBAL.slicesWidget != null)
		//	GLOBAL.slicesWidget.rebuild(this);

	}

	public void clear() {
		this.l.clear();
	}

	public CrossSliceSelections copy(SlicePlanes slicePlanes,
			SketchChair linkedChair) {
		CrossSliceSelections newCrossSliceSelections = new CrossSliceSelections();
		for (int i = 0; i < this.l.size(); i++) {
			CrossSliceSelection creossSelection = this.l.get(i);
			newCrossSliceSelections.add(creossSelection.copy(slicePlanes,
					linkedChair));//creossSelection.select();
		}

		return newCrossSliceSelections;
	}

	public CrossSliceSelection get(int i) {
		return this.l.get(i);
	}

	public CrossSliceSelection getLast() {
		if (this.l.size() > 0)
			return this.l.get(this.l.size() - 1);
		else
			return null;
	}

	public void render(PGraphics g) {

		//if(getOutterOutline() != null)
		//getOutterOutline().render(g);
		for (int i = 0; i < this.l.size(); i++) {
			CrossSliceSelection creossSelection = this.l.get(i);
			creossSelection.render(g);
		}

		this.update();
	}

	
	public void mouseReleased() {
		//if(getOutterOutline() != null)
		//getOutterOutline().render(g);
		for (int i = 0; i < this.l.size(); i++) {
			CrossSliceSelection creossSelection = this.l.get(i);
			creossSelection.mouseReleased();
		}

	}
	
	void select() {
		for (int i = 0; i < this.l.size(); i++) {
			CrossSliceSelection creossSelection = this.l.get(i);
			creossSelection.select();
		}
	}

	public int size() {
		return this.l.size();
	}

	public Element toXML() {

		Element element = new Element("CrossSliceSelections");
		for (int i = 0; i < this.l.size(); i++) {
			CrossSliceSelection curSelection = this.l.get(i);

			element.appendChild(curSelection.toXML());

		}

		return element;
	}

	public void unselect() {
		for (int i = 0; i < this.l.size(); i++) {
			CrossSliceSelection creossSelection = this.l.get(i);
			creossSelection.unselect();
		}

	}

	public void update() {
		boolean rebuild = false;
		//if(getOutterOutline() != null)
		//getOutterOutline().render(g);
		for (int i = 0; i < this.l.size(); i++) {
			CrossSliceSelection creossSelection = this.l.get(i);

			if (creossSelection.destroy) {
				this.l.remove(i);
				rebuild = true;
			} else
				creossSelection.update();
		}
		if (rebuild && GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().buildLen();

	}

}
