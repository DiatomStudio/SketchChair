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
package cc.sketchchair.geometry;

import java.util.ArrayList;
import java.util.List;

import toxi.geom.Plane;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;
import ShapePacking.BezierControlNode;
import cc.sketchchair.core.CrossSliceSelection;
import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.SETTINGS;
import cc.sketchchair.core.SketchChair;
import cc.sketchchair.sketch.SETTINGS_SKETCH;
import cc.sketchchair.sketch.SketchPath;
import cc.sketchchair.sketch.SketchPoint;
import cc.sketchchair.sketch.SketchShape;
import cc.sketchchair.sketch.SketchShapes;
import cc.sketchchair.sketch.SketchSpline;
import cc.sketchchair.sketch.SliceSlot;



/**
 * Functions to perform geometry operations in SketchChair for example intersecting planes.
 * @author gregsaul
 *
 */
public class GeometryOperations {

	public static void generateSlat(SlicePlanes planesToSlice,
			SlicePlanes crossSlices, CrossSliceSelection crossSliceSelection,
			SketchShape guideSpline, SketchChair chair) {

		float dirSign = -1; //direction of plane
		
		float planeOffset = ((SETTINGS.materialThickness / 2) / SETTINGS.scale)
				* dirSign;
		crossSliceSelection.spacing = planeOffset;

		float widthBetweenPlanes = planesToSlice.getMaxZ()
				- planesToSlice.getMinZ();
		float startX = 0;

		if (guideSpline == null)
			return;

		float start = Math.min(crossSliceSelection.start,
				crossSliceSelection.end);
		float end = Math
				.max(crossSliceSelection.start, crossSliceSelection.end);

		if (end >= 1)
			end = .999f;

		if (start <= 0)
			start = .01f;

		Vec2D startPos2D = guideSpline.getPos(start);
		Vec2D endPos2D = guideSpline.getPos(end);

		if (startPos2D == null || endPos2D == null)
			return;

		Vec3D vecPosStart = new Vec3D(startPos2D.x, startPos2D.y, 0);
		Vec3D vecPosEnd = new Vec3D(endPos2D.x, endPos2D.y, 0);

		float pLen = vecPosStart.distanceTo(vecPosEnd);

		Vec3D vecDir = new Vec3D(vecPosStart.x - vecPosEnd.x, vecPosStart.y
				- vecPosEnd.y, 0);
		vecDir = vecDir.normalize();

		Vec3D vecDirOffset = vecDir.copy();

		vecDirOffset.rotateZ((float) (Math.PI / 2));

		vecDirOffset.scaleSelf(crossSliceSelection.spacing);

		vecPosStart.addSelf(vecDirOffset);

		vecDir.rotateZ((float) (Math.PI / 2));
		//vecDir.rotateX((float) (Math.PI/2 ));

		

		int sign = 1;
		if(Math.atan2(vecDir.x, vecDir.y) < 0){
			sign = -1;
			}
		
		SlicePlane slicePlane = new SlicePlane(new Plane(vecPosStart, vecDir));
		
		slicePlane.setCrossSliceSelection(crossSliceSelection);
		//LOGGER.info("vecDir"+Math.atan2(vecDir.x, vecDir.y));
		
		SketchPath sketchPath = new SketchPath(slicePlane.getSketch());
		/*
		float offsetWidth = widthBetweenPlanes / 2;
		sketchPath.add(new SketchPoint(startX + offsetWidth
				+ crossSliceSelection.boarderX, -crossSliceSelection.boarderY));
		sketchPath.add(new SketchPoint(startX
				- (offsetWidth + crossSliceSelection.boarderX),
				-crossSliceSelection.boarderY));
		sketchPath.add(new SketchPoint(startX
				- (offsetWidth + crossSliceSelection.boarderX), pLen
				+ crossSliceSelection.boarderY));
		sketchPath.add(new SketchPoint(startX + offsetWidth
				+ crossSliceSelection.boarderX, pLen
				+ crossSliceSelection.boarderY));
		 */
		
		float penetration = 0.01f;
		
		float minZ = sign*planesToSlice.getMinZ();
		float maxZ = sign*planesToSlice.getMaxZ();
		//TODO: Adding +1 to width fixes overlapping issue but does not seem right. 
		float materialHalfWidth = ((SETTINGS.materialThickness / 2) / SETTINGS.scale);
		sketchPath.add(new SketchPoint(maxZ-materialHalfWidth+penetration,0));
		sketchPath.add(new SketchPoint(minZ+materialHalfWidth-penetration,0));
		sketchPath.add(new SketchPoint(minZ+materialHalfWidth-penetration, pLen-penetration));
		sketchPath.add(new SketchPoint(maxZ-materialHalfWidth+penetration, pLen-penetration));

		slicePlane.getSketch().getSketchShapes().add(sketchPath);
		sketchPath.setClosed(true);
		
		
		
		
		
		
		/**
		 * Go though each Plane/Layer
		 */
		
		// add slot to all pieces
		for (int j = 0; j < planesToSlice.size(); j++) {
			SlicePlane currentPlaneY = planesToSlice.get(j);
		//LOGGER.info(currentPlaneY.getPlane().toString());
			Vec2D slotPos = new Vec2D(vecPosStart.x, vecPosStart.y);
			Vec2D slotDir = new Vec2D(vecDir.x, vecDir.y);

			SliceSlot slot = new SliceSlot(slicePlane, slotPos, slotDir,
					(float) (pLen / crossSliceSelection.teethCount),
					(float) (pLen / crossSliceSelection.teethCount),
					SETTINGS.materialThickness,
					(int) (crossSliceSelection.teethCount), false,
					SliceSlot.SLOT);
			
			
			

			slot.setFingerTollerance(-(crossSliceSelection.fingerTollerance / SETTINGS.scale));

			if(crossSliceSelection.type == CrossSliceSelection.PLANE_ON_EDGE)
				slot.setOnEdge(new Vec2D(-1, 0));

			currentPlaneY.getSketch().getSketchShapes().getSlots().add(slot);
			
			
			Vec2D slotPosSlice = new Vec2D(sign*currentPlaneY.getPlane().z,0);
			Vec2D slotDirSlice = new Vec2D(1, 0);
			
			SliceSlot slotSlice = new SliceSlot(slicePlane, slotPosSlice, slotDirSlice,
					(float) (pLen / crossSliceSelection.teethCount),
					(float) (pLen / crossSliceSelection.teethCount),
					SETTINGS.materialThickness,
					(int) (crossSliceSelection.teethCount), true,
					SliceSlot.SLOTS_AND_FINGER);
			
			
			SliceSlot slotSlice2 = new SliceSlot(slicePlane, slotPosSlice, slotDirSlice,
					(float) (pLen),
					(float) (pLen),
					SETTINGS.materialThickness,
					(int) (1), true,
					SliceSlot.SLOTS_AND_FINGER);
			
			
			if(!currentPlaneY.guide)
			slicePlane.getSketch().getSketchShapes().getSlots().add(slotSlice);
			
			

		}

		
		/*
		Vec2D slotPos = new Vec2D(offsetWidth + crossSliceSelection.boarderX,
				-crossSliceSelection.boarderY);
		Vec2D slotDir = new Vec2D(1, 0);
		SliceSlot slot = new SliceSlot(slicePlane, slotPos, slotDir,
				(int) (pLen / crossSliceSelection.teethCount),
				(int) (pLen / crossSliceSelection.teethCount),
				SETTINGS.plane_thickness,
				(int) (crossSliceSelection.teethCount), true,
				SliceSlot.SLOTS_AND_FINGER);
		// slot.setOnEdge(new Vec2D(0,1));
		slicePlane.getSketch().getSketchShapes().getSlots().add(slot);

		slotPos = new Vec2D(-(offsetWidth + crossSliceSelection.boarderX),
				-crossSliceSelection.boarderY);
		slotDir = new Vec2D(1, 0);
		slot = new SliceSlot(slicePlane, slotPos, slotDir,
				(int) (pLen / crossSliceSelection.teethCount),
				(int) (pLen / crossSliceSelection.teethCount),
				SETTINGS.plane_thickness,
				(int) (crossSliceSelection.teethCount), true,
				SliceSlot.SLOTS_AND_FINGER);

		slicePlane.getSketch().getSketchShapes().getSlots().add(slot);

		
*/
		//build the outline
	
		slicePlane.getSketch().buildOutline();
		crossSlices.add(slicePlane);
	}

	public static void generateSlatSlices(SlicePlanes planesToSlice,
			SlicePlanes crossSlices, CrossSliceSelection crossSliceSelection,
			SketchShape guideSpline, SketchChair chair) {

		int numSlices = (int) SETTINGS.slat_num;// (int)
		float spacing = SETTINGS.chair_width / (numSlices + 1);

		float scale = 1;
		float width = 10;

		// TODO: option flush edges
		// Add fake profiles on the ends, this might be a option later for flush
		// edges !
		SlicePlane startSlicePlaneend1 = planesToSlice.getList().get(0);
		SlicePlane slicePlane1 = new SlicePlane(startSlicePlaneend1.getSketch()
				.clone(), new Plane(new Vec3D(0, 0,
				(startSlicePlaneend1.getPlane().z)
						- SETTINGS.chair_slatslot_end_size),
				new Vec3D(0, 0, -1)));
		planesToSlice.add(0, slicePlane1);
		// Add fake profiles on the ends
		SlicePlane startSlicePlaneend2 = planesToSlice
				.get(planesToSlice.size() - 1);
		SlicePlane slicePlane2 = new SlicePlane(startSlicePlaneend2.getSketch()
				.clone(), new Plane(new Vec3D(0, 0,
				(startSlicePlaneend2.getPlane().z)
						+ SETTINGS.chair_slatslot_end_size),
				new Vec3D(0, 0, -1)));
		planesToSlice.add(slicePlane2);
		// Add fake profiles on the ends

		float coverWidth = width + (SETTINGS.chair_slatslot_end_size * 2);
		float coverLength = guideSpline.getlength()
				+ (SETTINGS_SKETCH.chair_slat_end_size * 2);

		float step = crossSliceSelection.spacing / guideSpline.getlength();

		float start = Math.min(crossSliceSelection.start,
				crossSliceSelection.end);
		float end = Math
				.max(crossSliceSelection.start, crossSliceSelection.end);

		// special case for lines with 2 points
		if (guideSpline.size() == 2) {
			start += step;
			end -= step;
		}

		for (float i = start; i <= end; i += step) {

			Vec3D vecPos = new Vec3D(guideSpline.getPos(i).x,
					guideSpline.getPos(i).y, 0);
			Vec3D vecDir = new Vec3D(guideSpline.getPerpendicular(i).x,
					guideSpline.getPerpendicular(i).y, 0);

			// do we want to add a strenghtening beam to the leg
			if (crossSliceSelection.tieToLeg) {
				SketchPoint point1 = (SketchPoint) guideSpline.get(0);
				SketchPoint point2 = (SketchPoint) guideSpline.get(1);
				Vec2D dir2D = point1.sub(point2).normalize();
				dir2D.rotate((float) (Math.PI / 2));
				vecPos = new Vec3D(point2.x, point2.y, 0);
				vecDir = new Vec3D(dir2D.x, dir2D.y, 0);
			}

			SlicePlane slicePlane = new SlicePlane(new Plane(vecPos, vecDir));
			slicePlane.setCrossSliceSelection(crossSliceSelection);

			// SketchOutline sktOutline = new SketchOutline();

			List<SketchPoint> topLine = new ArrayList<SketchPoint>();
			List<SketchPoint> bottomLine = new ArrayList<SketchPoint>();

			for (int j = 0; j < planesToSlice.size(); j++) {
				SlicePlane currentPlaneY = planesToSlice.get(j);
				topLine.add(new SketchPoint(currentPlaneY.getPlane().z, 0));
				bottomLine.add(new SketchPoint(currentPlaneY.getPlane().z,
						crossSliceSelection.getSlatHeight()));

				if (j != 0 && j != planesToSlice.size() - 1) {

					SliceSlot slot = new SliceSlot(slicePlane, new Vec2D(
							vecPos.x, vecPos.y), new Vec2D(vecDir.x, vecDir.y),
							(crossSliceSelection.getSlatHeight() / 2),
							SETTINGS.materialThickness);
					//slot.setOnEdge(new Vec2D(0, 1));
					currentPlaneY.getSketch().getSlots().add(slot);

					slot = new SliceSlot(currentPlaneY, new Vec2D(
							currentPlaneY.getPlane().z,
							(crossSliceSelection.getSlatHeight() / 2)),
							new Vec2D(1, 0),
							(crossSliceSelection.getSlatHeight() / 2),
							SETTINGS.materialThickness);
					//slot.setOnEdge(new Vec2D(0,1));
					slicePlane.getSketch().getSlots().add(slot);

				}
			}

			// now go around the lines we collected and add them to a
			// outline
			Vec2D controlPointLeft = null;
			Vec2D bezierPointLeft = null;

			Vec2D controlPointRight = null;
			Vec2D bezierPointRight = null;

			// now change the line caping
			if (bottomLine.size() > 1) {

				controlPointRight = bottomLine.get(0);
				bezierPointRight = bottomLine.get(1);

				controlPointLeft = bottomLine.get(bottomLine.size() - 1);
				bezierPointLeft = bottomLine.get(bottomLine.size() - 2);
				bottomLine.remove(0);
				bottomLine.remove(bottomLine.size() - 1);

				if (crossSliceSelection.tieToLeg) {
					controlPointRight = new Vec2D(bezierPointRight.x - 15,
							bezierPointRight.y);
					controlPointLeft = new Vec2D(bezierPointLeft.x + 15,
							bezierPointLeft.y);

				}
			}

			SketchPath sketchPath = new SketchPath(slicePlane.getSketch());

			for (int i1 = 0; i1 < topLine.size(); i1++) {
				SketchPoint tempP = topLine.get(i1);
				// sktOutline.add(tempVec);
				sketchPath.add(tempP);

				float offsetX = (width / (numSlices - 1)) / 2;

				if (i1 != 0 && i1 != topLine.size() - 1
						&& SETTINGS_SKETCH.Draw_Curves)
					sketchPath.addBezier(tempP, 
							new Vec2D(tempP.x - offsetX, tempP.y), new Vec2D(
									tempP.x + offsetX, tempP.y));

			}

			for (int i1 = bottomLine.size() - 1; i1 >= 0; i1--) {
				// sketchPath.add((Vec2D) bottomLine.get(i1));
				SketchPoint tempP = bottomLine.get(i1);
				sketchPath.add(tempP);

				float offsetX = (width / (numSlices - 1)) / 2;

				if (i1 != bottomLine.size() - 1 && i1 != 0
						&& !crossSliceSelection.tieToLeg
						&& SETTINGS_SKETCH.Draw_Curves)
					sketchPath.addBezier(tempP,
							new Vec2D(tempP.x + offsetX, tempP.y), new Vec2D(
									tempP.x - offsetX, tempP.y));

			}

			if (controlPointLeft != null) {
				sketchPath
						.addBezier((SketchPoint) bezierPointLeft,controlPointLeft,
										bezierPointLeft);
				sketchPath.addBezier((SketchPoint) bezierPointRight,
						bezierPointRight,
								controlPointRight);

			}

			// sketchPath.add((Vec2D) bottomLine.get(bottomLine.size()-1));
			// System.out.println(" shape len " + sktOutline.l.size());

			// slicePlane.sketchShapes.sketchOutlines.add(sktOutline);
			// slicePlane.selected = true;
			// slicePlane.sketchShapes.optimize();
			sketchPath.setClosed(true);
			
			slicePlane.getSketch().add(sketchPath);
			slicePlane.getSketch().buildOutline();
			slicePlane.generate();

			if (crossSliceSelection.tieToLeg)
				slicePlane.tiedToLeg = true;
			// slicePlane.unselect();
			// slicePlane.selected = false;

			crossSlices.add(slicePlane);

			if (crossSliceSelection.tieToLeg)
				i = 100;

			float gap = 3f * SETTINGS.scale;
			float lenPerPecent = guideSpline.getlengthPerPercent()
					/ SETTINGS.scale;

			float stepOffset = ((SETTINGS.materialThickness / 2) + gap)
					/ (lenPerPecent);

			float dirSign = -1;
			float planeOffset = ((SETTINGS.materialThickness / 2) / SETTINGS.scale)
					* dirSign;
			if (i < end - step) {

				SlicePlanes slatPlanes = new SlicePlanes();
				slatPlanes.add(planesToSlice);
				slatPlanes.remove(slicePlane1);
				slatPlanes.remove(slicePlane2);

				CrossSliceSelection planeSelection = new CrossSliceSelection(
						crossSliceSelection.path, crossSliceSelection.plane, i
								+ stepOffset, i - stepOffset + step,
						planeOffset, crossSliceSelection.parentChair);
				planeSelection.type = CrossSliceSelection.SLATSLICES;
				generateSlat(slatPlanes, crossSlices, planeSelection,
						guideSpline, chair);
			}

		}

		// now take away the very outer planes
		planesToSlice.remove(0);
		planesToSlice.remove(planesToSlice.size() - 1);

		//planesToSlice.removeCollisions();
		//planesToSlice.checkForCollisionsSlots();

		// if(SETTINGS.EXPERT_MODE && GLOBAL.slicesWidget != null)
		// GLOBAL.slicesWidget.rebuild(crossSliceSelections);

		// this.rebuildLength = false;

	}

	public static void generateSlices(SlicePlanes planesToSlice,
			SlicePlanes crossSlices, CrossSliceSelection crossSliceSelection,
			SketchShape guideSpline) {

		if(guideSpline ==  null)
			return;
		

		int numSlices = (int) SETTINGS.slat_num;// (int)
		float spacing = SETTINGS.chair_width / (numSlices + 1);

		float scale = 1;
		float width = 10;

		/*
		 * ADD fake/guide layers, based on adjacent layers.
		 * 
		 *   : | | | :
		 *   : | | | :
		 *   : | | | :
		 *   ^       ^
		 *   |       |
		 *  fake    fake
		 */
		if (crossSliceSelection.getCapType() != CrossSliceSelection.CAP_INSIDE) {
			// TODO: option flush edges
			// Add fake profiles on the ends, this might be a option later for
			// flush edges !
			SlicePlane startSlicePlaneend1 = planesToSlice.getList().get(0);
			SlicePlane slicePlane1 = new SlicePlane(startSlicePlaneend1
					.getSketch().clone(), new Plane(new Vec3D(0, 0,
					(startSlicePlaneend1.getPlane().z)
							- SETTINGS.chair_slat_end_size),
					new Vec3D(0, 0, -1)));
			
			slicePlane1.destroy();//a bit of a hack make sure that our temp slice is deleted later 
			
			slicePlane1.getSketch().removeLegs(); // remove any leg shapes
			slicePlane1.getSketch().build();
			planesToSlice.add(0, slicePlane1);
			// Add fake profiles on the ends
			SlicePlane startSlicePlaneend2 = planesToSlice.get(planesToSlice
					.size() - 1);
			SlicePlane slicePlane2 = new SlicePlane(startSlicePlaneend2
					.getSketch().clone(), new Plane(new Vec3D(0, 0,
					(startSlicePlaneend2.getPlane().z)
							+ SETTINGS.chair_slat_end_size),
					new Vec3D(0, 0, -1)));
			slicePlane2.destroy();//a bit of a hack make sure that our temp slice is deleted later 

			slicePlane2.getSketch().removeLegs(); // remove any leg shapes
			slicePlane2.getSketch().build();
			planesToSlice.add(slicePlane2);
		}

		// Add fake profiles on the ends

		float step = crossSliceSelection.spacing / guideSpline.getlength();
		float start = Math.min(crossSliceSelection.start,
				crossSliceSelection.end);
		float end = Math
				.max(crossSliceSelection.start, crossSliceSelection.end);

		if (SETTINGS.DEBUG)
			guideSpline.debugPercent = start;

		//Clear dubugging info off planes
		if (SETTINGS.DEBUG) {
			for (int j = 0; j < planesToSlice.size(); j++) {
				SlicePlane currentPlaneY = planesToSlice.get(j);
				currentPlaneY.debugIntersectionPoints.clear();
				currentPlaneY.debugIntersectionPointsTop.clear();
				currentPlaneY.debugIntersectionPointsBottom.clear();
			}
		}

		/*
		 *  Step though the spline to slice
		 *                      
		 * <pre>                     
		 *  					
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
		 *  </pre>
		 */
		for (float i = start; i <= end; i += step) {

			if (guideSpline == null || i > 1)
				return;
			Vec2D sPos = guideSpline.getPos(i);
			if(sPos == null)
				return;
			
			Vec3D vecPos = new Vec3D(sPos.x,
					sPos.y, 0);
			Vec3D vecDir = new Vec3D(guideSpline.getPerpendicular(i).x,
					guideSpline.getPerpendicular(i).y, 0);
			vecDir = vecDir.rotateZ(crossSliceSelection.offsetRotation);
			//vecDir = vecDir.rotateZ((float) (Math.PI ));
			// do we want to add a strengthening beam to the leg
			if (crossSliceSelection.tieToLeg) {
				SketchPoint point1 = (SketchPoint) guideSpline.get(0);
				SketchPoint point2 = (SketchPoint) guideSpline.get(1);
				Vec2D dir2D = point1.sub(point2).normalize();
				dir2D.rotate((float) (Math.PI / 2));
				vecPos = new Vec3D(point1.x, point1.y, 0);
				vecDir = new Vec3D(dir2D.x, dir2D.y, 0);
			}

			SlicePlane slicePlane = new SlicePlane(new Plane(vecPos, vecDir));
			slicePlane.setCrossSliceSelection(crossSliceSelection);

			//slicePlane.getPlane().normal.rotateZ((float) -(Math.PI/2));
			SketchPath topLine = new SketchPath(null);
			SketchPath bottomLine = new SketchPath(null);

			topLine.setClosed(false);
			bottomLine.setClosed(false);

			/*
			 * Slice up each plane
			 * 
			 * <pre>
			 * 
			 *  1 2 3 4 5 -> (currentPlaneY) go through each plane
			 *  
			 *  | | | | |
			 *  ---------< (crossSliceSelection) calculate intersects 
			 *  | | | | |
			 *  | | | | |
			 *  |\ \ \ \ \
			 *  | \ \ \ \ \
			 *  |  \ \ \ \ \
			 *  |  |    |  |
			 *     |       |
			 *     |       |
			 *  
			 *  </pre>
			 */
			for (int j = 0; j < planesToSlice.size(); j++) {
				SlicePlane currentPlaneY = planesToSlice.get(j);
				// get the spline we want to produce cross sections along
				// do we want to generate planes across the whole profile or
				// just the current sketch?
				//TODO: crop to outline minus leg! 
				Object spline = null;
				if (crossSliceSelection.cropToCurrentShape) {
					//if(crossSliceSelection.path instanceof SketchSpline)
					//spline = crossSliceSelection.path;
					spline = currentPlaneY.getSketch().getFirst(); ////THIS IS WRONG TODO:
				} else {

				    
					if (currentPlaneY.getSketch().getSketchShapes().sketchOutlines
							.getOutterOutline() == null)
						return;

					spline = currentPlaneY.getSketch().getSketchShapes().sketchOutlines
							.getOutterOutline().getPath();
					
					
					
					

				}

				
				
				
				
				/** GUIDE SKETCHSPLINE
				 * 	
				 * <pre>				
				 * intersect points based on center of spline and width
				 * 					^
				 *      ___|__    __|_____|_____
				 *    \/  _|_ \  /  |_____|_____
				 *    /\ / |\  \/  /|     |
				 *   |  \    \____/    
				 * 
				 * </pre>
				 **/
				// we want to produce cross sections just across the current
				// sketch
				if (spline != null && spline instanceof SketchSpline) {
					SketchSpline currentSpline = (SketchSpline) spline;

					//LOGGER.info("slices on SketchSpline");

					if (currentSpline == null) {
						crossSliceSelection.destroy(null);
						return;
					}

					currentSpline = currentSpline;

					Vec2D topIntersect = null;
					Vec2D bottomIntersect = null;

					Vec2D intersectX = new Vec2D(-1, -1);

					Vec2D vecPosOnSpline = currentSpline.getPos(i);
					
					if(vecPosOnSpline != null){
					vecPos = new Vec3D(vecPosOnSpline.x,
							vecPosOnSpline.y,
							currentPlaneY.getPlane().z);

					Vec2D posPerp = currentSpline.getPerpendicular(i);
					float percent = -1f;
					Vec2D percentVec = new Vec2D(-1, -1);

					posPerp = posPerp.rotate(crossSliceSelection.offsetRotation);

					currentPlaneY.getIntersectionCentre(slicePlane,
							currentSpline, i, percentVec, intersectX);
					
					

					if(intersectX.x != -1 && intersectX.y!=-1){
					
					
					float splineWidth = currentSpline.getOffsetSize();
					splineWidth = currentSpline.getOffsetSize(i);

					//	float splineIntersectPercent = percentVec.x;
					//	if (splineIntersectPercent != -1) {

					Vec2D topIntersectY = null;
					Vec2D bottomIntersectY = null;

					float dist = 0;
					float neartestDist = -1;

					Vec2D tempPos = percentVec.copy();// currentSpline.getPos(percentVec.x);
					Vec2D tempDir = posPerp.copy();// currentSpline.getPerpendicular(percentVec.x);

					if (tempDir == null)
						break;

					tempDir = tempDir.normalize();

					topIntersect = intersectX.add(new Vec2D(0, splineWidth));
					bottomIntersect = intersectX.add(new Vec2D(0, -splineWidth));

					// Switch what side slots are added to
					// slots along Y
					Vec2D slotPos = new Vec2D(tempPos.x, tempPos.y);
					Vec2D slotDir = null;
					if (!crossSliceSelection.flipSide) {
						slotDir = new Vec2D(vecDir.x, vecDir.y);
					} else {
						slotDir = new Vec2D(-vecDir.x, -vecDir.y);
					}
					// slotPos.addSelf(slotDir.scale(100));
					float offsetToEdge = -1;
					if (!crossSliceSelection.flipSide) {
						offsetToEdge = 1;
					} else {
						offsetToEdge = -1;
					}

					if (!currentPlaneY.guide) {
						SliceSlot slot = new SliceSlot(slicePlane, slotPos,
								slotDir, (-splineWidth - offsetToEdge),
								SETTINGS.materialThickness);
						slot.swap();
						slot.setOnEdge(new Vec2D(0, 1));
						
if(crossSliceSelection.getCapType() != CrossSliceSelection.CAP_INSIDE)
	slot.makesEdge = true;

						currentPlaneY.getSketch().getSlots().add(slot);
					}

					if (!currentPlaneY.guide) {
						if (!crossSliceSelection.flipSide) {
							if (j != 0 && j != planesToSlice.size() - 1 ){
								float yPosSlot = intersectX.y + (splineWidth);
								SliceSlot slot = new SliceSlot(
										currentPlaneY,
										new Vec2D(intersectX.x,yPosSlot),
										new Vec2D(-1, 0), splineWidth,
										SETTINGS.materialThickness);
								if(crossSliceSelection.getCapType() != CrossSliceSelection.CAP_INSIDE)
									slot.makesEdge = true;
								        slot.setOnEdge(new Vec2D(0,-1)); // these slots start from the middle so this doesn't work, flip slots. 

								        
										//LOGGER.info("X " + slot.getPos().x + " Y " + slot.getPos().y );

										
								slicePlane
										.getSketch()
										.getSlots()
										.add(slot);
							}
						} else {
							if (j != 0 && j != planesToSlice.size() - 1){

								SliceSlot slot = new SliceSlot(
										currentPlaneY,
										new Vec2D(intersectX.x,
												intersectX.y
														- (splineWidth)),
										new Vec2D(1, 0), splineWidth,
										SETTINGS.materialThickness);
								slot.setOnEdge(new Vec2D(0, -1));
								if(crossSliceSelection.getCapType() != CrossSliceSelection.CAP_INSIDE)
									slot.makesEdge = true;
								slicePlane
										.getSketch()
										.getSlots()
										.add(slot);
								
							}
						}
					}

					// if(topIntersect != null){

					Vec3D tempVec = new Vec3D(bottomIntersect.x,
							bottomIntersect.y, 0);
					tempVec = slicePlane.getWorldPosIntersect(tempVec);

					if (SETTINGS.DEBUG) {
						currentPlaneY.debugIntersectionPoints.add(slotPos);
						currentPlaneY.debugIntersectionPointsTop.add(tempPos
								.scale(tempDir));
						currentPlaneY.debugIntersectionPointsBottom.add(tempPos
								.scale(tempDir.scale(-1)));
					}


					topLine.add(new SketchPoint(bottomIntersect));
					bottomLine.add(new SketchPoint(topIntersect));
					}
				}
				}

				//} 

				/* ACROSS SKETCHPATH */

				/**
				 * Find intersections across cross section
				 *   
				 *   <pre>
				 *   currentPath
				 * 		^
				 * 		|
				 *    ------   x1 -> (topIntersect)
				 *   |       \/ -------------------------> (crossSliceSelection)
				 *   |       /\
				 *   |      / /
				 *    \    /  \
				 *      \_/____\
				 *       /
				 *      x2 -> (bottomIntersect)
				 *   </pre>
				 */

				else if (spline instanceof SketchPath) {

					SketchPath currentPath = (SketchPath) spline;
					
					vecPos = new Vec3D(vecPos.x, vecPos.y,currentPlaneY.getPlane().z);
					
					//always crop to shape as we work out the correct shape earlier
					List<List<Vec2D>> points = currentPlaneY.getIntersection(
							slicePlane, true,
							currentPath);

					if (points != null && points.size() > 1) {

						Vec2D topIntersect = null;
						Vec2D bottomIntersect = null;
						Vec2D topIntersectY = null;
						Vec2D bottomIntersectY = null;
						Vec2D topIntersectX = null;
						Vec2D bottomIntersectX = null;
						float dist = 0;
						float neartestDist = -1;
						Vec2D vecStart = new Vec2D(vecPos.x, vecPos.y);

						//find the closest point 
						for (int k = 0; k < points.size(); k++) {
							
							Vec2D vec2d = (Vec2D) points.get(k).get(0);
							Vec2D vec2dX = (Vec2D) points.get(k).get(1);
							Vec3D vec3d = new Vec3D(vec2dX.x, vec2dX.y,vecPos.z);
							float d = vecStart.distanceTo(vec2dX);

							//Display intersects in debug mode
							if (SETTINGS.DEBUG) {
								currentPlaneY.debugIntersectionPoints
										.add(vec2dX);
								currentPlaneY.debugIntersetStart = new Vec2D(
										vecPos.x, vecPos.y);
							}

							if (d < neartestDist || neartestDist == -1) {
								neartestDist = d;
								topIntersect = vec2d;
								topIntersectX = vec2dX;
							}

						}

						float MIN_DUPLICATE_DIST = 2;

						//Remove the closest point
						for (int k = 0; k < points.size(); k++) {
							Vec2D vec2d = (Vec2D) points.get(k).get(0);
							Vec2D vec2dx = (Vec2D) points.get(k).get(1);

							float distToLastIntersect = topIntersectX
									.distanceTo(vec2dx);

							if (vec2d.equals(topIntersect)
									|| distToLastIntersect < MIN_DUPLICATE_DIST) {
								points.remove(k);
								k--;
							}

						}

						//find the next closest intersect!
						neartestDist = -1;
						for (int k = 0; k < points.size(); k++) {
							Vec2D vec2d = (Vec2D) points.get(k).get(0);
							Vec2D vec2dX = (Vec2D) points.get(k).get(1);

							Vec3D vec3d = new Vec3D(vec2d.x, vec2d.y,
									currentPlaneY.getPlane().z);
							float d = vecStart.distanceTo(vec2dX);

							if ((d < neartestDist && vec2d != topIntersect)
									|| neartestDist == -1) {

								neartestDist = d;
								bottomIntersect = vec2d;
								bottomIntersectX = vec2dX;
							}
						}

						
						/**
						 * Lets Fix any intersects that are swapped around
						 */
						if ((bottomIntersect != null && topIntersect != null)
								&& bottomIntersect.y < topIntersect.y) {
							Vec2D topIntersectTemp = topIntersect.copy();
							Vec2D topIntersectXTemp = topIntersectX.copy();

							topIntersect = bottomIntersect;
							topIntersectX = bottomIntersectX;

							bottomIntersect = topIntersectTemp;
							bottomIntersectX = topIntersectXTemp;

						}

						
						/**
						 * Are slats a set height?
						 */
						if (crossSliceSelection.getSlatHeight() != 0) {
							bottomIntersect = topIntersect.add(0,
									crossSliceSelection.getSlatHeight());
							bottomIntersectX = topIntersectX.add(0,
									crossSliceSelection.getSlatHeight());
						}

						if (SETTINGS.DEBUG) {
							currentPlaneY.debugIntersectionPointsTop
									.add(topIntersectX);
							currentPlaneY.debugIntersectionPointsBottom
									.add(bottomIntersectX);

						}

						//if we have found both intersects then add them to the outline
						if (bottomIntersect != null && topIntersect != null) {
							float len = bottomIntersect
									.distanceTo(topIntersect);

							if (!currentPlaneY.guide) {

								if (crossSliceSelection.flipSide) {
									Vec2D slotPos = bottomIntersectX.copy();
									Vec2D dirPerp = new Vec2D(vecDir.x,
											vecDir.y);
									dirPerp.rotate((float) (Math.PI / 2));
									SliceSlot slot = new SliceSlot(slicePlane,
											slotPos, new Vec2D(
													-vecDir.x,
													-vecDir.y),
											(len / 2f),
											SETTINGS.materialThickness);
									slot.setOnEdge(new Vec2D(0, -1));
									if(crossSliceSelection.getCapType() != CrossSliceSelection.CAP_INSIDE)
										slot.makesEdge = true;
									currentPlaneY
											.getSketch()
											.getSlots()
											.add(slot);

								} else {
									Vec2D slotPos = topIntersectX.copy();
									Vec2D dirPerp = new Vec2D(vecDir.x,
											vecDir.y);
									dirPerp.rotate((float) (Math.PI / 2));
									SliceSlot slot = new SliceSlot(slicePlane,
											slotPos,
											new Vec2D(vecDir.x,
													vecDir.y),
											(len / 2f),
											SETTINGS.materialThickness);
									slot.setOnEdge(new Vec2D(0, -1));
									if(crossSliceSelection.getCapType() != CrossSliceSelection.CAP_INSIDE)
										slot.makesEdge = true;
									// Switch what side slots are added to
									currentPlaneY
											.getSketch()
											.getSlots()
											.add(slot);
								}
							}

							Vec2D topSlotintersect = bottomIntersect.copy();
							Vec2D bottomtopSlotintersect = topIntersect.copy();

							if (!currentPlaneY.guide) {

								if (!crossSliceSelection.flipSide) {
									if (j != 0
											&& j != planesToSlice.getList()
													.size() - 1){
										SliceSlot slot = new SliceSlot(
												currentPlaneY,
												topSlotintersect,
												new Vec2D(-1, 0),
												(len / 2f),
												SETTINGS.materialThickness);
										slot.setOnEdge(new Vec2D(0, -1));
										if(crossSliceSelection.getCapType() != CrossSliceSelection.CAP_INSIDE)
											slot.makesEdge = true;
										slicePlane
												.getSketch()
												.getSlots()
												.add(slot);
									}
								} else {
									if (j != 0
											&& j != planesToSlice.getList()
													.size() - 1){
										SliceSlot slot = new SliceSlot(
												currentPlaneY,
												bottomtopSlotintersect,
												new Vec2D(1, 0),
												((len / 2f)),
												SETTINGS.materialThickness);
										slot.setOnEdge(new Vec2D(0, -1));
										if(crossSliceSelection.getCapType() != CrossSliceSelection.CAP_INSIDE)
											slot.makesEdge = true;
										slicePlane
												.getSketch()
												.getSlots()
												.add(slot);
									}
								}
							}
							// if(topIntersect != null){

							// add bezier points

							/**
							 * BUILD the legs
							 * 
							 * ________ 
							 * |:/  \:|
							 * ||    ||
							 * --    --
							 */
							
							if (crossSliceSelection.tieToLeg) {
								// if(crossSliceSelection.tiedToPlanes.contains(currentPlaneY)){
								
								
								if (crossSliceSelection.legSpline != null
										&& crossSliceSelection.legSpline
												.getPath() != null
										&& crossSliceSelection.tiedToPlanes.contains(currentPlaneY)) {
									
									
									
									
									
									Vec2D seatBottom = null;

									if(crossSliceSelection.extendLegSliceToTopOfLeg){
										seatBottom = new Vec2D(0,0);
									}else{
									
										
										SketchShapes tempSketchShapes = currentPlaneY.getSketch().getSketchShapes().clone(); 
										
										tempSketchShapes.removeLegs();
										tempSketchShapes.sketchOutlines.clear();
										tempSketchShapes.buildOutline();
										
										if (tempSketchShapes.sketchOutlines
												.getOutterOutline() == null)
											return;
										
										
										SketchShape sktch = tempSketchShapes.sketchOutlines.getOutterOutline().getPath();

										
										List<List<Vec2D>> points02 = currentPlaneY.getIntersection(
												slicePlane, true,
												sktch);
										
										
										for (int k = 0; k < points02.size(); k++) {
											
											Vec2D vec2d = (Vec2D) points02.get(k).get(0);
											if(seatBottom == null || vec2d.distanceTo(topIntersect) > vec2d.distanceTo(seatBottom) )
												seatBottom = vec2d;
											
										}
						
									
								 	if(seatBottom == null)
										seatBottom = bottomIntersect;
									
									}
									// this is hard coded so legs are always on
									// the outside slices!
									topLine.add(new SketchPoint(topIntersect));


									//bottomLine.add(new SketchPoint(bottomIntersect));
									float legWidthBottom = 20;
									float legWidthTop = 20;
									SketchSpline legSpline = crossSliceSelection.legSpline;
									float Yoffset = 0;
									float yMin = 0;
									
									SketchPoint p1 = null, p2, p3, p4;
									if (legSpline.getPath().size() == 4) {
										p1 = (SketchPoint) legSpline.getPath()
												.get(1);
										p2 = (SketchPoint) legSpline.getPath()
												.get(2);

										p3 = (SketchPoint) legSpline.getPath()
												.get(0);
										p4 = (SketchPoint) legSpline.getPath()
												.get(3);

										legWidthBottom = p1.distanceTo(p2);
										legWidthTop = p3.distanceTo(p4);
										Yoffset = 0;
										
										
										
										Vec2D p5 = GLOBAL.uiTools.getPointOnPlane(legSpline.getCentrePath().get(0), slicePlane.getPlane());
										Vec2D p6 = GLOBAL.uiTools.getPointOnPlane(legSpline.getCentrePath().get(1), slicePlane.getPlane());

										//slicePlane.getPlane().getIntersectionWithRay(r);
										yMin = 29;//Math.min(p5.y,p6.y);
										


										
									}
									
									//Get the last intersect 
									//TODO this will get the intersect to the left, whitch is not always desirable
									
									if(bottomLine.size() > 0)
										Yoffset = bottomLine.get(bottomLine.size()-1).y;

									//TODO: LEG STUFF 
									//Yoffset = legSpline.getCentrePath().get(0).distanceTo(legSpline.getCentrePath().get(1));
									bottomLine.add(new SketchPoint(
											bottomIntersect.x
													- (legWidthTop / 2),
													seatBottom.y));

									bottomLine.add(new SketchPoint(
											bottomIntersect.x
													- (legWidthBottom / 2),
											bottomIntersect.y));

								//	bottomLine.add(new SketchPoint(
								//			bottomIntersect.x,
								//			yMin));

									bottomLine.add(new SketchPoint(
											bottomIntersect.x
													+ (legWidthBottom / 2),
											bottomIntersect.y));

									bottomLine.add(new SketchPoint(
											bottomIntersect.x
													+ (legWidthTop / 2),
													seatBottom.y));

								} else {

									topLine.add(new SketchPoint(topIntersect));
									bottomLine.add(new SketchPoint(
											bottomIntersect));

								}

							} else {
								topLine.add(new SketchPoint(topIntersect));
								bottomLine
										.add(new SketchPoint(bottomIntersect));

							}

						}
					}
				}
			}

			
			
			/**
			 * Check what direction our slats are going in and reverse them if they're going in the wrong dir 
			 */
			
			if(topLine.size() > 0 && ((SketchPoint)topLine.get(0)).x > ((SketchPoint)topLine.get(topLine.size()-1)).x){
				topLine.reverseWinding();
				bottomLine.reverseWinding();
			}
			
			/**
			 * Should we generate flat tops?
			 * 
			 */

			
			// now go around the lines we collected and add them to a
			// outline
			Vec2D controlPointLeft = null;
			Vec2D bezierPointLeft = null;

			Vec2D controlPointRight = null;
			Vec2D bezierPointRight = null;

			if (crossSliceSelection.smooth) {
				bottomLine.smoothLeft(SETTINGS_SKETCH.SMOOTH_AMOUNT);
				topLine.smoothRight(SETTINGS_SKETCH.SMOOTH_AMOUNT);
			}
			
			
			if(crossSliceSelection.generateFlushTops){
				
				float materialWidth = SETTINGS.materialThickness/SETTINGS.scale; //TODO: change this so it is more general
				for (int i1 = 0; i1 < topLine.size(); i1++) {
					SketchPoint tempP = topLine.get(i1);
					

					
					SketchPoint pLeft = new SketchPoint(tempP.x+((materialWidth/2)), tempP.y);
					SketchPoint pRight = new SketchPoint(tempP.x-((materialWidth/2)), tempP.y);

					if(tempP.containsBezier()){
						//pLeft.controlPoint1 = tempP.controlPoint1.copy();
						
						pLeft.controlPoint2 = tempP.controlPoint2.copy();
						pRight.controlPoint1 = tempP.controlPoint1.copy();
						
						//pRight.controlPoint2 = tempP.controlPoint2.copy();

					}
					topLine.set(i1,pLeft);
					topLine.add(i1, pRight);
					i1++;

				}
				
				
				if(crossSliceSelection.type != CrossSliceSelection.LEG){
					
					
					
					for (int i1 = 0; i1 < bottomLine.size(); i1++) {
						SketchPoint tempP = bottomLine.get(i1);
						SketchPoint pLeft = new SketchPoint(tempP.x-(materialWidth/2), tempP.y);
						SketchPoint pRight = new SketchPoint(tempP.x+(materialWidth/2), tempP.y);

						if(tempP.containsBezier()){
							//pLeft.controlPoint1 = tempP.controlPoint1.copy();
							pLeft.controlPoint2 = tempP.controlPoint2.copy();

							pRight.controlPoint1 = tempP.controlPoint1.copy();
							//pRight.controlPoint2 = tempP.controlPoint2.copy();

						}
						bottomLine.set(i1,pRight);
						bottomLine.add(i1, pLeft);
						i1++;

					}
					
				}
				
				
				
			}
			
			

			/**
			 * <pre>
			 * CAPPING 
			 * |-----------| 
			 * |-----------| 
			 * </pre>
			 */

			SketchPath sketchPath = new SketchPath(slicePlane.getSketch());

			if (crossSliceSelection.getCapType() == CrossSliceSelection.CAP_ROUND_SQUARE) {
				// now change the line caping
				if (bottomLine.size() > 1) {

					float cornerRad = crossSliceSelection.cornerRadius;
					bottomLine.add(0, (SketchPoint) bottomLine.get(0).clone());
					bottomLine.add((SketchPoint) bottomLine.get(
							bottomLine.size() - 1).clone());

					bottomLine.get(0).addSelf(0, -cornerRad);
					bottomLine.get(1).addSelf(cornerRad, 0);
					bottomLine.get(0).controlPoint1 = new Vec2D(
							bottomLine.get(0).x, bottomLine.get(0).y
									+ cornerRad);
					bottomLine.get(0).controlPoint2 = bottomLine.get(0).copy();

					bottomLine.get(bottomLine.size() - 1)
							.addSelf(0, -cornerRad);
					bottomLine.get(bottomLine.size() - 2)
							.addSelf(-cornerRad, 0);
					bottomLine.get(bottomLine.size() - 1).controlPoint2 = new Vec2D(
							bottomLine.get(bottomLine.size() - 1).x,
							bottomLine.get(bottomLine.size() - 1).y + cornerRad);
					bottomLine.get(bottomLine.size() - 1).controlPoint1 = bottomLine
							.get(bottomLine.size() - 1).copy();

					topLine.add(0, (SketchPoint) topLine.get(0).clone());
					topLine.add((SketchPoint) topLine.get(topLine.size() - 1)
							.clone());

					topLine.get(0).addSelf(0, cornerRad);
					topLine.get(1).addSelf(cornerRad, 0);
					topLine.get(0).controlPoint2 = new Vec2D(topLine.get(0).x,
							topLine.get(0).y - cornerRad);
					topLine.get(0).controlPoint1 = topLine.get(0).copy();

					topLine.get(topLine.size() - 1).addSelf(0, cornerRad);
					topLine.get(topLine.size() - 2).addSelf(-cornerRad, 0);
					topLine.get(topLine.size() - 1).controlPoint1 = new Vec2D(
							topLine.get(topLine.size() - 1).x,
							topLine.get(topLine.size() - 1).y - cornerRad);
					topLine.get(topLine.size() - 1).controlPoint2 = topLine
							.get(topLine.size() - 1).copy();

					for (int i1 = 0; i1 < topLine.size(); i1++) {
						SketchPoint tempP = topLine.get(i1);
						sketchPath.add(tempP);
						
					}

					for (int i1 = bottomLine.size() - 1; i1 >= 0; i1--) {
						SketchPoint tempP = bottomLine.get(i1);
						sketchPath.add(tempP);
						
					}

				}
			}

			// adds a curve to the bottom line
			if (crossSliceSelection.getCapType() == CrossSliceSelection.CAP_CURVE) {
				// now change the line capping
				if (bottomLine.size() > 1) {

					controlPointRight = bottomLine.get(0);
					bezierPointRight = bottomLine.get(1);

					controlPointLeft = bottomLine.get(bottomLine.size() - 1);
					bezierPointLeft = bottomLine.get(bottomLine.size() - 2);
					bottomLine.remove(0);
					bottomLine.remove(bottomLine.size() - 1);

					if (crossSliceSelection.tieToLeg) {
						controlPointRight = new Vec2D(bezierPointRight.x - 15,
								bezierPointRight.y);
						controlPointLeft = new Vec2D(bezierPointLeft.x + 15,
								bezierPointLeft.y);

					}
				}

				for (int i1 = 0; i1 < topLine.size(); i1++) {
					SketchPoint tempP = topLine.get(i1);
					sketchPath.add(tempP);

					float offsetX = (width / (numSlices - 1)) / 2;

					if (i1 != 0 && i1 != topLine.size() - 1
							&& SETTINGS_SKETCH.Draw_Curves)
						sketchPath.addBezier(tempP, 
								new Vec2D(tempP.x - offsetX, tempP.y),
								new Vec2D(tempP.x + offsetX, tempP.y));

				}

				for (int i1 = bottomLine.size() - 1; i1 >= 0; i1--) {
					SketchPoint tempP = bottomLine.get(i1);
					sketchPath.add(tempP);

					float offsetX = (width / (numSlices - 1)) / 2;

					if (i1 != bottomLine.size() - 1 && i1 != 0
							&& !crossSliceSelection.tieToLeg
							&& SETTINGS_SKETCH.Draw_Curves)
						sketchPath.addBezier(tempP, 
								new Vec2D(tempP.x + offsetX, tempP.y),
								new Vec2D(tempP.x - offsetX, tempP.y));

				}

				if (controlPointLeft != null) {
					sketchPath.addBezier((SketchPoint) bezierPointLeft,
							controlPointLeft,
									bezierPointLeft);
					sketchPath.addBezier((SketchPoint) bezierPointRight,
							bezierPointRight,
									controlPointRight);

				}

			}

			if (crossSliceSelection.getCapType() == CrossSliceSelection.CAP_BUTT
					|| crossSliceSelection.getCapType() == CrossSliceSelection.CAP_INSIDE) {

				bottomLine.get(0).removeBezier();
				bottomLine.get(bottomLine.size() - 1).removeBezier();

				topLine.get(0).removeBezier();
				topLine.get(topLine.size() - 1).removeBezier();

				for (int i1 = 0; i1 < topLine.size(); i1++) {
					SketchPoint tempP = topLine.get(i1);
					sketchPath.add(tempP);

				}
				for (int i1 = bottomLine.size() - 1; i1 >= 0; i1--) {
					SketchPoint tempP = bottomLine.get(i1);
					sketchPath.add(tempP);
				}

			}

			sketchPath.setClosed(true);

			slicePlane.getSketch().add(sketchPath);
			slicePlane.getSketch().buildOutline();
			slicePlane.generate();

			if (crossSliceSelection.tieToLeg)
				slicePlane.tiedToLeg = true;

			crossSlices.add(slicePlane);

			if (crossSliceSelection.tieToLeg)
				i = 100;

		}

		if (crossSliceSelection.getCapType() != CrossSliceSelection.CAP_INSIDE) {
			// now take away the very outer planes
			planesToSlice.remove(0);
			planesToSlice.remove(planesToSlice.size() - 1);
		}

		
		//planesToSlice.removeCollisions();
		//planesToSlice.checkForCollisions();


	}

}
