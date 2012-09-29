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



import cc.sketchchair.geometry.SlicePlane;
import cc.sketchchair.geometry.SlicePlanes;
import cc.sketchchair.sketch.Sketch;
import cc.sketchchair.sketch.SketchPath;
import cc.sketchchair.sketch.SketchShape;
import cc.sketchchair.sketch.SketchSpline;
import cc.sketchchair.sketch.SketchTools;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;
import ModalGUI.GUIEvent;
import processing.core.PGraphics;
import toxi.geom.Plane;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;


/**
 * This Class holds parameters for generating slice selections.
 * 
 *  <pre>
 *       	   start       
 *             /|/|/|/|/|
 *      0.1f -----------< --- (crossSliceSelection)
 *           / / / / / 
 *    0.2f -----------<
 *         / / / / /
 *  0.3f -----------<
 *       |/|/|/|/|/
 *       end
 *       
 *       </pre>
 */


public class CrossSliceSelection implements Clickable {

	public static final int SLATSLICES = 4;
	public static final int PLANE = 5;
	public static int SINGLE_SLICE = 3;
	public static int SLICES = 0;
	public static int LEG = 1;
	public static int PLANE_ON_EDGE = 2;

	public static int CAP_CURVE = 1;
	public static int CAP_BUTT = 2;
	public static int CAP_INSIDE = 3;
	public static int CAP_ROUND_SQUARE = 4;

	public static int JOIN_CURVE = 1;
	public static int JOIN_STRAIGHT = 2;

	public int type = SLICES;

	public SketchShape path = null;
	public float start = 0;
	public float end = .5f;
	boolean editing = false;
	public SlicePlane plane = null;
	public SlicePlanes planes = new SlicePlanes();;

	public boolean extendLegSliceToTopOfLeg = false;
	public float mousePercent;
	public boolean cropToCurrentShape = true;
	public boolean tieToLeg = false;
	//boolean top = false;
	public boolean flipSide = false;
	int clicks = 0;
	public float spacing = 0.9f;
	public boolean destroy;
	public ArrayList tiedToPlanes = new ArrayList();
	public SketchSpline legSpline;
	public SketchChair parentChair;
	public float boarderX = 0;
	public float boarderY = 0;
	public boolean selected = false;
	private boolean mouseOnButton = false;
	public float slatHeight = 0;
	public float teethCount = 5;

	public int capType = CAP_CURVE;
	public int joinType = JOIN_STRAIGHT;

	public float offsetRotation = 0f;
	public float cornerRadius = 10;

	public boolean smooth = false;
	public boolean generateFlushTops = false;

	public float fingerTollerance = 0.0f;
	public List<Sketch> legSplines = null;
	public boolean tempSlice = false;

	public CrossSliceSelection() {
		// TODO Auto-generated constructor stub
	}

	//#IF JAVA
	public CrossSliceSelection(Element element, SlicePlanes linkedPlanes,
			SketchChair linkedChair) {

		//wrong type
		if (!element.getLocalName().equals("CrossSliceSelection"))
			return;

		if (element.getAttributeValue("path") != null
				&& element.getAttributeValue("start") != null
				&& element.getAttributeValue("end") != null
				&& element.getAttributeValue("spacing") != null) {

			if (element.getAttributeValue("boarderX") != null)
				this.boarderX = Float.valueOf(element
						.getAttributeValue("boarderX"));

			if (element.getAttributeValue("boarderY") != null)
				this.boarderY = Float.valueOf(element
						.getAttributeValue("boarderY"));

			if (element.getAttributeValue("slatHeight") != null)
				this.slatHeight = Float.valueOf(element
						.getAttributeValue("slatHeight"));

			if (element.getAttributeValue("teethCount") != null)
				this.teethCount = Float.valueOf(element
						.getAttributeValue("teethCount"));

			if (element.getAttributeValue("capType") != null)
				this.capType = Integer.valueOf(element
						.getAttributeValue("capType"));
			
			
			if(element.getAttributeValue("extendLegSliceToTopOfLeg") != null)
				extendLegSliceToTopOfLeg = Boolean.valueOf(element.getAttributeValue("extendLegSliceToTopOfLeg")).booleanValue()  ;

			int linkedSketchId = Integer.valueOf(element
					.getAttributeValue("path"));

			if (linkedPlanes.getSketchShapeById(linkedSketchId) != null) {
				this.path = linkedPlanes.getSketchShapeById(linkedSketchId);
				//System.out.println("linked sketch");	
			}

			this.start = Float.valueOf(element.getAttributeValue("start"));
			this.end = Float.valueOf(element.getAttributeValue("end"));
			this.spacing = Float.valueOf(element.getAttributeValue("spacing"));

			if (element.getAttributeValue("type") != null)
				this.type = Integer.valueOf(element.getAttributeValue("type"));

			if (element.getAttributeValue("tieToLeg") != null) {
				this.tieToLeg = true;
				if (element.getAttributeValue("legId") != null) {
					this.legSpline = (SketchSpline) linkedPlanes
							.getSketchShapeById(Integer.valueOf(element
									.getAttributeValue("legId")));

				}
			}
			if (element.getFirstChildElement("legPlanes") != null) {

				Element legElements = element.getFirstChildElement("legPlanes");
				

				for (int i = 0; i < legElements.getChildCount(); i++) {
					Element child = (Element) legElements.getChild(i);

					if (child != null && child.getLocalName().equals("legPlane")) {
						

						if (child.getAttributeValue("planeId") != null){
							int planeId = Integer.parseInt(child.getAttributeValue("planeId"));
							//LOGGER.info(linkedPlanes + "linkedPlanes");
							SlicePlane linkedPlane = (SlicePlane) linkedPlanes.getById(planeId);
							
							if(linkedPlane != null)
							this.tiedToPlanes.add(linkedPlane);
							
						}

						
						
					}

				}
				
				
			}
			

			if (element.getAttributeValue("constrainToShape") != null)
				this.cropToCurrentShape = true;
			else
				this.cropToCurrentShape = false;

			if (element.getAttributeValue("flipSide") != null)
				this.flipSide = true;

			if (element.getAttributeValue("generateFlushTops") != null)
				this.generateFlushTops = true;

			if (element.getAttributeValue("smooth") != null)
				this.smooth = true;

			this.plane = linkedPlanes.getPlaneContainingShape(this.path);
			GLOBAL.uiTools.addListener(this);
			editing = false;
			this.parentChair = linkedChair;

		} else {

		}
	}

	public CrossSliceSelection(SketchShape spline, SlicePlane plane,
			float start, float end, float spacing, SketchChair chair) {
		this.path = spline;
		this.start = start;
		this.spacing = spacing;
		this.end = end;
		this.plane = plane;
		GLOBAL.uiTools.addListener(this);
		editing = false;
		this.parentChair = chair;

	}

	public CrossSliceSelection(SketchShape curSpline, SlicePlane extrudeSlice,
			SketchChair chair) {
		this(curSpline, extrudeSlice, 0, 1, 10, chair);
	}

	public CrossSliceSelection copy(SlicePlanes slicePlanes,
			SketchChair linkedChair) {
		CrossSliceSelection newCrossSliceSelection = new CrossSliceSelection();

		newCrossSliceSelection.start = this.start;
		newCrossSliceSelection.end = this.end;
		newCrossSliceSelection.spacing = this.spacing;

		newCrossSliceSelection.type = this.type;
		newCrossSliceSelection.boarderX = this.boarderX;
		newCrossSliceSelection.boarderY = this.boarderY;
		newCrossSliceSelection.slatHeight = this.slatHeight;
		newCrossSliceSelection.tieToLeg = this.tieToLeg;
		
		newCrossSliceSelection.extendLegSliceToTopOfLeg = this.extendLegSliceToTopOfLeg;

		newCrossSliceSelection.cropToCurrentShape = this.cropToCurrentShape;
		newCrossSliceSelection.flipSide = this.flipSide;

		newCrossSliceSelection.teethCount = this.teethCount;

		if (this.path != null
				&& slicePlanes.getSketchShapeById(this.path.getId()) != null) {
			newCrossSliceSelection.path = slicePlanes
					.getSketchShapeById(this.path.getId());
		}
		if (this.legSpline != null
				&& slicePlanes.getSketchShapeById(this.legSpline.getId()) != null) {
			newCrossSliceSelection.legSpline = (SketchSpline) slicePlanes
					.getSketchShapeById(this.legSpline.getId());
		}
		
		if(this.tiedToPlanes !=null){
			
			for(int i = 0; i < slicePlanes.size(); i++){
				SlicePlane slicePlane = (SlicePlane)slicePlanes.get(i);
			}
			
			for(int i = 0; i < this.tiedToPlanes.size(); i++){
				SlicePlane slicePlane = (SlicePlane)this.tiedToPlanes.get(i);
				SlicePlane linkedSlicePlane = (SlicePlane) slicePlanes.getById(slicePlane.getId());
												if(linkedSlicePlane != null)
			newCrossSliceSelection.tiedToPlanes.add(linkedSlicePlane);
			}
			
		}

		if (newCrossSliceSelection.path != null)
			newCrossSliceSelection.plane = slicePlanes
					.getPlaneContainingShape(newCrossSliceSelection.path);

		newCrossSliceSelection.parentChair = linkedChair;
		newCrossSliceSelection.editing = false;
		return newCrossSliceSelection;
	}

	public void destroy(GUIEvent e) {
		this.destroy = true;

		if (GLOBAL.slicesWidget != null)
			GLOBAL.slicesWidget.removeRow(this);

	}

	public void edit(GUIEvent e) {
		
 		this.parentChair.unselectAllPlanes();

		
		GLOBAL.uiTools.selectTool(UITools.CROSSSLICE_EDIT);
		this.editing = true;
		this.plane.select();
		this.path.select();

		clicks = 0;
		
		GLOBAL.uiTools.setCurrentTool(UITools.SLICES_EDIT);

	}

	public void flipSide(GUIEvent e) {
		this.flipSide = !this.flipSide;
		this.parentChair.buildLen();
	}

	public int getCapType() {
		return this.capType;
	}

	public int getJoinType() {
		return this.joinType;
	}

	/**
	 * @return the slatHeight
	 */
	public float getSlatHeight() {
		return slatHeight;
	}

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

	//@Override
	public void mouseReleased() {
		//if(GLOBAL.uiTools.currentTool != UITools.CROSSSLICE_EDIT)
		//	return;

		
		
		//System.out.println("CLICKED");
		if (editing) {
			clicks++;


			//any click will add the currnt slice
			if(this.type == CrossSliceSelection.SINGLE_SLICE){
				editing = false;
				clicks = 0;
			     return;
			}

			
			if(this.type ==CrossSliceSelection.PLANE ||
					this.type ==CrossSliceSelection.PLANE_ON_EDGE ){
				
				if (clicks == 2) {
					editing = false;
					clicks = 0;
					return;
				}
				
			}
			
			
			if(this.type == CrossSliceSelection.SLICES ||
					this.type == CrossSliceSelection.SLATSLICES
					){


			if (clicks == 1) {
				this.start = this.mousePercent;
			}

			if (clicks == 2) {
				this.end = this.mousePercent;
			}

			if (clicks >= 3) {
				editing = false;
				clicks = 0;
				return;
			}

			}
		}
	}

	public void render(PGraphics g) {
		if (this.path == null)
			return;

		if (selected || editing) {
			/*
			Vec2D mousePos = this.path.getPos(this.mousePercent);
			g.ellipse(mousePos.x, mousePos.y, 20,20);
			
			
			Vec2D startPos = this.path.getPos(this.start);
			if(startPos != null)
			g.ellipse(startPos.x, startPos.y, 20,20);
			
			Vec2D endPos = this.path.getPos(this.end);
			if(endPos != null)
			g.ellipse(endPos.x, endPos.y, 20,20);
			*/
		}

	}

	public void select() {
		//this.plane.select();
		this.selected = true;
	}

	//#ENDIF JAVA	

	public void select(GUIEvent e) {

		mouseOnButton = true;
	}

	public void setCapType(GUIEvent e) {
		this.capType = (int) e.val;
	}

	public void setJoinType(GUIEvent e) {
		this.joinType = (int) e.val;
	}

	/**
	 * @param slatHeight the slatHeight to set
	 */
	public void setSlatHeight(float slatHeight) {
		this.slatHeight = slatHeight;
	}

	public void setSlatHeight(GUIEvent e) {
		this.setSlatHeight(Float.parseFloat(e.getString()));
	}

	public void smooth(GUIEvent e) {
		this.smooth = !this.smooth;
	}

	public void generateFlushTops(GUIEvent e) {
		this.generateFlushTops = !this.generateFlushTops;
	}

	public void toggleConstrainToshape(GUIEvent e) {
		this.cropToCurrentShape = !this.cropToCurrentShape;
		this.parentChair.buildLen();
	}

	public void toggleSliceMode(GUIEvent e) {
		if (this.type == CrossSliceSelection.LEG
				|| this.type == CrossSliceSelection.SLICES)
			this.type = CrossSliceSelection.PLANE_ON_EDGE;
		else
			this.type = CrossSliceSelection.SLICES;

		this.flipSide = !flipSide;
		this.parentChair.buildLen();
	}

	public nu.xom.Element toXML() {
		
		Element element = new Element("CrossSliceSelection");
		element.addAttribute(new Attribute("path", String.valueOf(this.path
				.getId())));
		element.addAttribute(new Attribute("start", String.valueOf(this.start)));
		element.addAttribute(new Attribute("end", String.valueOf(this.end)));
		element.addAttribute(new Attribute("spacing", String
				.valueOf(this.spacing)));
		element.addAttribute(new Attribute("type", String.valueOf(this.type)));
		element.addAttribute(new Attribute("boarderX", String
				.valueOf(this.boarderX)));
		element.addAttribute(new Attribute("boarderY", String
				.valueOf(this.boarderY)));
		element.addAttribute(new Attribute("slatHeight", String
				.valueOf(this.slatHeight)));
		element.addAttribute(new Attribute("capType", String
				.valueOf(this.capType)));

		if (type == CrossSliceSelection.PLANE
				|| type == CrossSliceSelection.PLANE_ON_EDGE
				|| type == CrossSliceSelection.SLATSLICES)
			element.addAttribute(new Attribute("teethCount", String
					.valueOf(this.teethCount)));

		
		if(extendLegSliceToTopOfLeg)
			element.addAttribute(new Attribute("extendLegSliceToTopOfLeg", String
					.valueOf(this.extendLegSliceToTopOfLeg)));
		
		if (this.tieToLeg) {
			
			
			element.addAttribute(new Attribute("tieToLeg", String
					.valueOf(this.tieToLeg)));
			
			
			element.addAttribute(new Attribute("legId", String
					.valueOf(this.legSpline.getId())));
			
			
			Element planeElement = new Element("legPlanes");
			for (int i = 0; i < this.tiedToPlanes.size(); i++) {
				SlicePlane curSlice = (SlicePlane)this.tiedToPlanes.get(i);
				Element legPlane = new Element("legPlane");
				
				legPlane.addAttribute(new Attribute("planeId", String
						.valueOf(curSlice.getId())));
				
				planeElement.appendChild(legPlane);

			}
			
			element.appendChild(planeElement);
		}
		if (this.cropToCurrentShape)
			element.addAttribute(new Attribute("constrainToShape", String
					.valueOf(this.cropToCurrentShape)));

		if (this.flipSide)
			element.addAttribute(new Attribute("flipSide", String
					.valueOf(this.flipSide)));

		if (this.generateFlushTops)
			element.addAttribute(new Attribute("generateFlushTops", String
					.valueOf(this.generateFlushTops)));

		if (this.smooth)
			element.addAttribute(new Attribute("smooth", String
					.valueOf(this.smooth)));

		return element;
	}

	public void unselect() {
		//	this.plane.unselect();
		this.selected = false;
		planes.unselectAll();
	}

	public void update() {
		


		if (this.plane == null)
			return;

		
		
		
		if (editing && this.type != CrossSliceSelection.PLANE) {
			
			
			if(this.path != null)
				this.path.highlight();
			
			
			//GLOBAL.uiTools.setCurrentTool(UITools.SLICES_GROUP_SLICES);

			
			Vec2D pointOnPlan = GLOBAL.uiTools.getPointOnPlane(new Vec2D(
					GLOBAL.uiTools.mouseX, GLOBAL.uiTools.mouseY), this.plane
					.getPlane());

			
			this.mousePercent = this.path.getClosestPercent(pointOnPlan.x,
					pointOnPlan.y);

			
		//	LOGGER.info("UPDATE SLICE clicks "+clicks + " percent " +this.mousePercent  );

			
			
			if (this.type == CrossSliceSelection.SINGLE_SLICE) {


				if (clicks == 0) {
					this.start = this.mousePercent;
					this.end = this.mousePercent;
				}

				if (clicks == 1) {
					editing = false;
					this.plane.unselect();
					this.path.unselect();
				}

			} else {
				
				GLOBAL.uiTools.sliceToolMode = UITools.SLICE_EDIT_MODE_SPACING;
				

				if (clicks == 0) {
					this.start = this.mousePercent;
					GLOBAL.uiTools.sliceToolMode = UITools.SLICE_EDIT_MODE_ADD;

				}

				if (clicks == 1) {
					this.end = this.mousePercent;
					GLOBAL.uiTools.sliceToolMode = UITools.SLICE_EDIT_MODE_POS;


				}

				if (clicks == 2) {
					float spacingVal = Math.abs(this.mousePercent - this.end);
					this.spacing = spacingVal * 300;
				}
				
				if(clicks == 3){
				//	this.plane.unselect();
				//	this.path.unselect();
					GLOBAL.uiTools.SketchTools.selectTool(SketchTools.SELECT_TOOL);
				//	editing = false;
				//	clicks = 0;
				//	return;
				}

			}

			if ((this.type == CrossSliceSelection.SLICES || this.type == CrossSliceSelection.SLATSLICES)
					&& this.spacing < SETTINGS.MIN_SPACING)
				this.spacing = SETTINGS.MIN_SPACING;
		}

		//is  plane not on a edge so move the construction lines
		if (editing && this.type == CrossSliceSelection.PLANE) {

			Vec2D pointOnPlan = GLOBAL.uiTools.getPointOnPlane(new Vec2D(
					GLOBAL.uiTools.mouseX, GLOBAL.uiTools.mouseY), this.plane
					.getPlane());

			if (clicks == 1) {
				SketchPath tempP = (SketchPath) this.path;
				tempP.get(0).set(pointOnPlan);
			}

			if (clicks == 2) {
				SketchPath tempP = (SketchPath) this.path;
				tempP.get(1).set(pointOnPlan);
			}

			if (clicks == 3) {
				this.end = 1;
				editing = false;
				this.plane.unselect();
				this.path.unselect();

			}
		}

	}

}
