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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import cc.sketchchair.sketch.SETTINGS_SKETCH;


/**
 * Stores and retrieves properties from the SketchChair properties file. SketchChair.properties
 * @author gregsaul
 *
 */
public class SketchProperties {

	
	Properties properties = new Properties();
	
	SketchProperties(){
		FileInputStream in;
		try {
			LOGGER.info(System.getProperty("user.dir"));
			in = new FileInputStream(System.getProperty("user.dir")+"/SketchChair.properties");
			properties.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	void loadDefaults(){
		if(properties.containsKey("addGuideDivets"))
		GLOBAL.shapePack.add_guide_divets = "yes".equals(properties.getProperty("addGuideDivets"));
		
		if(properties.containsKey("addDogbones"))
		GLOBAL.shapePack.addDogbones = "yes".equals(properties.getProperty("addDogbones"));
			
		if(properties.containsKey("materialHeight"))
		GLOBAL.shapePack.materialHeight = Float.parseFloat(properties.getProperty("materialHeight"));
		
		if(properties.containsKey("materialWidth"))
		GLOBAL.shapePack.materialWidth = Float.parseFloat(properties.getProperty("materialWidth"));
		
		if(properties.containsKey("autoPackPieces"))
		GLOBAL.shapePack.autoPackPieces = "yes".equals(properties.getProperty("autoPackPieces"));
			
		if(properties.containsKey("materialThickness"))
		SETTINGS.materialThickness =  Float.parseFloat(properties.getProperty("materialThickness"));
		
		if(properties.containsKey("displayIntroPanel"))
		SETTINGS.displayIntroPanel = "yes".equals(properties.getProperty("displayIntroPanel"));
		
		if(properties.containsKey("useSliceCollisionDetection"))
		SETTINGS.useSliceCollisionDetection = "yes".equals(properties.getProperty("useSliceCollisionDetection"));
			
		if(properties.containsKey("addLabelsToPattern"))
		GLOBAL.shapePack.addLabels = "yes".equals(properties.getProperty("addLabelsToPattern"));

		if(properties.containsKey("cropPNGExportToCurrentView"))
		GLOBAL.cropExportToScreen = "yes".equals(properties.getProperty("cropPNGExportToCurrentView"));
		
		if(properties.containsKey("exportPNGWidth"))
			SETTINGS.renderWidth =  (int)Float.parseFloat(properties.getProperty("exportPNGWidth"));
		
		if(properties.containsKey("exportPNGHeight"))
			SETTINGS.renderHeight =  (int)Float.parseFloat(properties.getProperty("exportPNGHeight"));
		
		if(properties.containsKey("autoSaveMakePattern"))
			SETTINGS.autoSaveMakePattern = "yes".equals(properties.getProperty("autoSaveMakePattern"));
		
		if(properties.containsKey("autoSaveMakeLocation"))
			SETTINGS.autoSaveMakeLocation = properties.getProperty("autoSaveMakeLocation");
		
		if(properties.containsKey("addLegSlices"))		
			SETTINGS.addLegSlices = "yes".equals(properties.getProperty("addLegSlices"));

		if(properties.containsKey("autoRefreshTextures"))		
			SETTINGS.autoRefreshTextures = "yes".equals(properties.getProperty("autoRefreshTextures"));

		if(properties.containsKey("autoReset"))		
			SETTINGS.autoReset = "yes".equals(properties.getProperty("autoReset"));

		if(properties.containsKey("autoResetSeconds"))		
			SETTINGS.autoResetSeconds = (int)Float.parseFloat(properties.getProperty("autoResetSeconds"));

		if(properties.containsKey("autoSelectLegLayers"))		
			SETTINGS.selectLegPlanesAuto = "yes".equals(properties.getProperty("autoSelectLegLayers"));


		if(properties.containsKey("SketchSplineSmoothPixels"))
			SETTINGS_SKETCH.splineMoveFalloff = Float.parseFloat(properties.getProperty("SketchSplineSmoothPixels"));

		
		if(properties.containsKey("SketchSplinePointsEvey"))
			SETTINGS_SKETCH.spline_point_every = Float.parseFloat(properties.getProperty("SketchSplinePointsEvey"));

		
		if(properties.containsKey("language"))
			SETTINGS.language = properties.getProperty("language");
			

		if(properties.containsKey("SlatSpacing"))
			SETTINGS.DEFAULT_SLAT_SPACING = Float.parseFloat(properties.getProperty("SlatSpacing"));

		//if(properties.containsKey("groundColour"))		
		//	SETTINGS.groundColour = String.
		
		if(properties.containsKey("furnitureScale")){	
			SETTINGS.scale = Float.parseFloat(properties.getProperty("furnitureScale"));
			SETTINGS.pixels_per_mm = SETTINGS.pixels_per_mm_base
					/ SETTINGS.scale;
		}
		
		
		if(properties.containsKey("cameraScale"))	
			GLOBAL.ZOOM = Float.parseFloat(properties.getProperty("cameraScale"));
			
		if(properties.containsKey("cameraOffsetX"))	
			GLOBAL.CAM_OFFSET_X = Float.parseFloat(properties.getProperty("cameraOffsetX"));
			
		if(properties.containsKey("cameraOffsetY"))	
			GLOBAL.CAM_OFFSET_Y = Float.parseFloat(properties.getProperty("cameraOffsetY"));
				
		
		
		if(properties.containsKey("useStencilBuffer"))
			GLOBAL.useMaskedUpdating = "yes".equals(properties.getProperty("useStencilBuffer"));
		
		if(properties.containsKey("startInExpertMode"))
		SETTINGS.EXPERT_MODE = "yes".equals(properties.getProperty("startInExpertMode"));
		
		
		if(properties.containsKey("smoothRender"))			
			SETTINGS.SMOOTH_RENDER = "yes".equals(properties.getProperty("smoothRender"));
		
	}
	
	
	void rememberDefaults(){
		if(properties.containsKey("addGuideDivets"))
		GLOBAL.shapePack.add_guide_divets = "yes".equals(properties.getProperty("addGuideDivets"));
		
		if(properties.containsKey("addDogbones"))
		GLOBAL.shapePack.addDogbones = "yes".equals(properties.getProperty("addDogbones"));
			
		if(properties.containsKey("materialHeight"))
		GLOBAL.shapePack.materialHeight = Float.parseFloat(properties.getProperty("materialHeight"));
		
		if(properties.containsKey("materialWidth"))
		GLOBAL.shapePack.materialWidth = Float.parseFloat(properties.getProperty("materialWidth"));
		
		if(properties.containsKey("materialThickness"))
		SETTINGS.materialThickness =  Float.parseFloat(properties.getProperty("materialThickness"));
		
	
	}
	
	
	

}
