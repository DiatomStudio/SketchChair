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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cc.sketchchair.core.GLOBAL;

import processing.core.PGraphics;

/**
 * Container class for SketchSlots. 
 * @author gregsaul
 *
 */
//#ENDIF JAVA

public class SliceSlots {

	List<SliceSlot> l = new ArrayList<SliceSlot>();
	private Sketch parentSketch;

	public void add(SliceSlot slot) {
		this.l.add(slot);
	}
	
	public void removeTrappedSlots(Sketch outlineSketch) {
		for (int i = 0; i < this.l.size(); i++) {
			SliceSlot curSlot = this.l.get(i);
			curSlot.removeTrappedSlots(outlineSketch);
		}
	}
	public void removeNonPiercing(Sketch outlineSketch) {
		for (int i = 0; i < this.l.size(); i++) {
			SliceSlot curSlot = this.l.get(i);
			curSlot.removeNonPiercing(outlineSketch);
		}
	}
	
	public void checkForSlotCollisions() {
		for (int i = 0; i < this.l.size(); i++) {
			SliceSlot curSlot = this.l.get(i);
			for (int j = 0; j < this.l.size(); j++) {
				SliceSlot otherSlot = this.l.get(j);

				if (curSlot != otherSlot && !curSlot.destroy
						&& !otherSlot.destroy)
					curSlot.checkForCollision(otherSlot);
			}
		}
	}

	public SliceSlots clone() {
		SliceSlots newSlots = new SliceSlots();
		for (int i = 0; i < this.l.size(); i++) {
			SliceSlot curSlot = this.l.get(i);
			newSlots.l.add(curSlot.clone());
		}
		return newSlots;
	}

	public void empty() {
		this.l = new ArrayList<SliceSlot>();
	}

	public SliceSlot get(int k) {
		return l.get(k);
	}

	
	public void renderEdge(PGraphics g) {
		for (int i = 0; i < this.l.size(); i++) {
			SliceSlot curSlot = this.l.get(i);
			curSlot.renderEdge(g);
		}		
	}
	
	
	public void render(PGraphics g) {
		this.update();

		//if(GLOBAL.uiTools.currentTool != UITools.SELECT_TOOL || GLOBAL.uiTools.currentTool != UITools.SELECT_BEZIER_TOOL)
		//return;

		// RENDER MODES
		// RENDER MODES
				switch(getParentSketch().getRenderMode()){
				
				
				
				case Sketch.RENDER_3D_EDITING_PLANES :
				{
					g.stroke(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_COLOUR);
					g.strokeWeight(SETTINGS_SKETCH.RENDER_3D_EDITING_PLANES_WEIGHT);
				
				}
				break;
	
				default: 
				g.stroke(SETTINGS_SKETCH.SKETCHSHAPE_PATH_COLOUR_UNSELECTED);
				g.strokeWeight(SETTINGS_SKETCH.SKETCHOUTLINE_UNSELECTED_WEIGHT);
				break;
				}
		

		for (int i = 0; i < this.l.size(); i++) {
			SliceSlot curSlot = this.l.get(i);
			curSlot.render(g);
		}
	}

	public int size() {
		return l.size();
	}

	public void update() {
		for (int i = 0; i < this.l.size(); i++) {
			SliceSlot curSlot = this.l.get(i);

			if (curSlot.destroy || curSlot.slice == null
					|| curSlot.slice.destroy == true) {
				this.l.remove(i);
			}
			//else
			//curSlot.update();

		}

	}





	public void removeIntersecting() {
		for (int i = 0; i < this.l.size(); i++) {
			SliceSlot curSlot = this.l.get(i);

			for (int i2 = 0; i2 < this.l.size(); i2++) {
				SliceSlot otherSlot = this.l.get(i2);
			
				
				if(!curSlot.equals(otherSlot)){
					SketchPath curSlotPath = new SketchPath(curSlot.slice.getSketch(),curSlot.getOutline(0, curSlot.slotLen*2));
					SketchPath otherSlotPath = new SketchPath(otherSlot.slice.getSketch(),otherSlot.getOutline(0, otherSlot.slotLen*2));
					curSlotPath.setClosed(true);
					otherSlotPath.setClosed(true);
					curSlotPath.build();
					otherSlotPath.build();


					if(curSlotPath.intersects(otherSlotPath)){
						if(otherSlot.slice.tiedToLeg){
							curSlot.slice.destroy();
							curSlot.destroy();
							i2 = this.l.size();
						}else{
							otherSlot.slice.destroy();
							otherSlot.destroy();
						}
						
						
					}
					
					
					
				}
			
			}
			

		}

			
	}

	public void setParentSketch(Sketch sketch) {
		this.parentSketch = sketch;		
	}
	public Sketch getParentSketch() {
		return this.parentSketch;		
	}


}
