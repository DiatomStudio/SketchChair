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

import java.util.ArrayList;

import cc.sketchchair.geometry.SlicePlane;
import cc.sketchchair.sketch.SketchShape;

/**
 * Undo stores iterations of a design so that it may be undone. Designs are copied so that only their parameters are copied to save memory. If a design is undone it is restored and built from it paramaters. 
 * This class used to store a stack of actions performed on a model and a way of undoing each action, however this became cumbersome to maintain.  
 * @author gregsaul
 *
 */
public class Undo {
	ArrayList<ArrayList<UndoAction>> l = new ArrayList<ArrayList<UndoAction>>();
	int index = 0;
	Long lastTick = 0l;

	ArrayList<SketchChair> undoChairs = new ArrayList<SketchChair>();
	int undoLevelChairs = 10;
	int indexChairs = 0;
	
	//This stores the last  of the design but not include changes currently being made to the chair by dragging points etc.  
	SketchChair mouseUpChair = null;

	public void addChair(SketchChair curChair) {

		undoChairs.add(0, curChair);

		for (int i = undoLevelChairs; i < undoChairs.size(); i++) {
			if (i > undoLevelChairs && i < undoChairs.size())
				undoChairs.remove(i);
		}

	}

	public void addOperation(UndoAction action) {
		//System.out.println("UNDO");
		//System.out.println(action.obj.getClass());

		if (lastTick != GLOBAL.tick || this.l.size() < 1) {
			ArrayList<UndoAction> actions = new ArrayList<UndoAction>();
			actions.add(action);
			this.l.add(actions);
		} else {
			ArrayList<UndoAction> actions = this.l.get(this.l.size() - 1);
			actions.add(action);
		}

		lastTick = GLOBAL.tick;

		if (this.l.size() > SETTINGS.UNDO_LEVELS)
			this.l.remove(0);
	}

	void undo() {
		if (this.l.size() > 0) {

			ArrayList actions = this.l.get(this.l.size() - 1);

			for (int i = 0; i < actions.size(); i++) {
				UndoAction action = (UndoAction) actions.get(i);

				if (action.action == UndoAction.ADD_SHAPE) {
					//if(() )
					SketchShape shape = (SketchShape) action.obj;
					shape.destroy();

				}

				if (action.action == UndoAction.ADD_PLANE) {
					//if(action.obj.getClass() )
					SlicePlane plane = (SlicePlane) action.obj;
					plane.destroy = true;
				}

				if (action.action == UndoAction.ADD_CHAIR) {
					//if(action.obj.getClass() )
					SketchChair chair = (SketchChair) action.obj;
					chair.destroy();
				}

				if (action.action == UndoAction.EDIT_SHAPE) {
					SketchShape shape = (SketchShape) action.obj;
					shape.replace((SketchShape) action.objClone);
					//action.objClone;
					shape.setSelectedNodes(new ArrayList<Object>());

					if (GLOBAL.sketchChairs.getCurChair() != null) {

						//GLOBAL.sketchChairs.curChair.build();
						GLOBAL.sketchChairs.getCurChair().getSlicePlanesY()
								.buildCurrentSketch();
						GLOBAL.sketchChairs.getCurChair().slicePlanesSlatSlices
								.buildCurrentSketch();
						GLOBAL.sketchChairs.getCurChair().buildLen();
						GLOBAL.sketchChairs.getCurChair().build();
						GLOBAL.sketchChairs.getCurChair()
								.updateCollisionShape();
					}

				}

			}
			this.l.remove(this.l.size() - 1);

		}
	}

	public void undoChair(SketchChair curChair) {

		SketchChair prevChair = undoChairs.remove(0);

		if (prevChair != null) {
			GLOBAL.sketchChairs.getCurChair().destroy();
			GLOBAL.sketchChairs.l.remove(GLOBAL.sketchChairs.getCurChair());
			GLOBAL.sketchChairs.add(prevChair);
			GLOBAL.sketchChairs.getCurChair().build();
		}

	}
	
	SketchChair getLastChair(){
		if(undoChairs.size() <= 0)
			return null;
		else
			return(SketchChair)undoChairs.get(0);
	
	}
	
	void setMouseUpChair(SketchChair chair){
		mouseUpChair = chair;	
	}
	
	public SketchChair getMouseUpChair(){
		return mouseUpChair;
	}
	

}
