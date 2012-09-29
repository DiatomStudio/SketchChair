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
import java.util.List;

import processing.core.PGraphics;

/**
 * This class stores a list of objects in a pick buffr and allows you to select them by clicking on them.
 * @author gregsaul
 *
 */
public class PickBuffer {
	
	//Pick buffer variables
	
	
	private static PickBuffer instance = null;
	
	  public static PickBuffer getInstance() {
	      if(instance == null) {
	         instance = new PickBuffer();
	      }
	      return instance;
	   }
	  
	  
	 public PGraphics pickBuffer;
	 float pickBufferRes = 0.5f;
	 public boolean renderPickBuffer = false; 
	 public boolean usePickBuffer = true;
	 List pickObjects = new ArrayList();
	 int currentId = 0;
	 int colOffset = 2;
	
	public int getPickColour(Object obj){
		pickObjects.add(obj);
		currentId = pickObjects.size()-1;
		return(getColor(currentId));
	}
	
	
	 Object getObj(int col){
		 int id = getId(col) ;

		 if(id < pickObjects.size() && id >= 0 )
		return pickObjects.get(id);
		 else
		return null;
	}
	
	 void reset(){
		pickObjects.clear();
		currentId = 0;
	}


	public Object getObject(int mouseX, int mouseY) {
		pickBuffer.beginDraw();
		int col = pickBuffer.get((int)(mouseX*pickBufferRes),(int)( mouseY*pickBufferRes));
		pickBuffer.endDraw();
		return getObj(col);

	}
	
	
	public int getId(int mouseX, int mouseY) {
		pickBuffer.beginDraw();
		int col = pickBuffer.get((int)(mouseX*pickBufferRes),(int)( mouseY*pickBufferRes));
		pickBuffer.endDraw();
		return col;

	}
	
	
	// id 0 gives color -2, etc.
	int getColor(int id) {
	  return -(id + colOffset);
	}
	 
	// color -2 gives 0, etc.
	int getId(int c) {
	  return -(c + colOffset);
	}
}
