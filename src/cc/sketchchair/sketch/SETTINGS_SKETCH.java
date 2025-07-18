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

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.functions.functions;

/**
 * Settings file for Sketch, this is separate from the SketchChair settings file to maintain modularity of the code. 
 * @author gregsaul
 *
 */
//#ENDIF JAVA
public class SETTINGS_SKETCH {

	//RENDER SETTINGS COLOURS
	public static final int OUTLINE_COLOUR = functions.color(30, 30,
			30);
	public static final int FILL_COLOUR = functions.color(255, 255,
			255);
	public static final int EDGE_COLOUR = functions.color(180, 180,
			180);
	public static final int TRANSPARENT_COLOUR = functions.color(255, 255,
			255,0);
	
	public static final int UNSELECTED_FILL_COLOUR = functions.color(255, 255,
			255,50);
	public static final int UNSELECTED_SIDE_FILL_COLOUR = functions.color(180, 180,
			180,50);
	
	public static final int UNSELECTED_STROKE_COLOUR = functions.color(120, 120,
			120,100);
	
	public static final int HIGHLIGHT_COLOUR = functions.color(0, 174,
			239);
	
	public static final int BLACK = functions.color(0, 0,
			0);
	
	//RENDER_3D_NORMAL
	public static final int RENDER_3D_NORMAL_SKETCHOUTLINE_STROKE_COLOUR = OUTLINE_COLOUR;
	public static final float RENDER_3D_NORMAL_SKETCHOUTLINE_STROKE_WEIGHT = 2.0f;
	public static final int RENDER_3D_NORMAL_SKETCHSHAPE_SIDE_FILL_COLOUR = EDGE_COLOUR;
	public static final int RENDER_3D_NORMAL_SKETCHSHAPE_FILL_COLOUR = FILL_COLOUR;

	
	
	//RENDER_3D_EDITING_PLANES
	public static final int RENDER_3D_EDITING_PLANES_SKETCHSHAPE_SIDE_FILL_COLOUR_SELECTED = EDGE_COLOUR;
	public static final int RENDER_3D_EDITING_PLANES_SKETCHOUTLINE_STROKE_COLOUR_SELECTED = OUTLINE_COLOUR;
	public static final float RENDER_3D_EDITING_PLANES_SKETCHOUTLINE_STROKE_WEIGHT_SELECTED = 3.0f;
	
	public static final int RENDER_3D_EDITING_PLANES_SKETCHSHAPE_SIDE_FILL_COLOUR_UNSELECTED = UNSELECTED_SIDE_FILL_COLOUR;
	public static final int RENDER_3D_EDITING_PLANES_SKETCHOUTLINE_STROKE_COLOUR_UNSELECTED = UNSELECTED_STROKE_COLOUR;
	public static final int RENDER_3D_EDITING_PLANES_SKETCHSHAPE_FILL_COLOUR_UNSELECTED = UNSELECTED_FILL_COLOUR;
	public static final float RENDER_3D_EDITING_PLANES_SKETCHOUTLINE_STROKE_WEIGHT_UNSELECTED = 1.0f;
	
	public static final int RENDER_3D_EDITING_PLANES_SKETCHSHAPE_FILL_COLOUR_SELECTED = TRANSPARENT_COLOUR;
	public static final int RENDER_3D_EDITING_PLANES_SKETCHSHAPE_STROKE_COLOUR_SELECTED = OUTLINE_COLOUR;
	public static final float RENDER_3D_EDITING_PLANES_SKETCHSHAPE_STROKE_WEIGHT_SELECTED = 1;

	public static final int RENDER_3D_EDITING_PLANES_SKETCHSHAPE_STROKE_COLOUR_UNSELECTED = TRANSPARENT_COLOUR;
	public static final float RENDER_3D_EDITING_PLANES_SKETCHSHAPE_STROKE_WEIGHT_UNSELECTED = 1;
	
	
	
	public static final int RENDER_3D_EDITING_PLANES_COLOUR = OUTLINE_COLOUR; // this is slices
	public static final float RENDER_3D_EDITING_PLANES_WEIGHT = 0.5f;
	
	
	
	public static int SKETCHSHAPE_PATH_COLOUR_SELECTED = HIGHLIGHT_COLOUR;
	public static float SKETCHSHAPE_PATH_WEIGHT_SELECTED = 3.0f;
	
	
	public static int SKETCHSHAPE_PATH_COLOUR_UNSELECTED = functions.color(100,
			100, 100);
	
	
	
	public static final int RED = functions.color(255, 0,0);

	public static int sChair_unselected_line_colour = functions.color(30, 30,30);
	public static int sChair_selected_line_colour = functions.color(250, 0, 0);
	public static float sChair_unselected_line_width = 1f;

	public static float sChair_selected_line_width = 1f;

	public static float dist_between_points = 100;
	public static int offset_size = 200;

	public static float select_on_path_step = 5.0f;
	public static final int SKETCHSHAPE_STROKE_SELETEDSHAPE_COLOUR = functions
			.color(255, 30, 30);

	public static int CONTROL_POINT_FILL_COLOUR = functions
			.color(255, 255, 255);
	public static int CONTROL_POINT_FILL_SELECTED_COLOUR = functions.color(
			100, 174, 239);

	public static int CONTROL_POINT_STROKE_COLOUR = functions.color(0, 174,
			239);

	//SLICE COLOURS / SETTINGS
	public static float SKETCHOUTLINE_EDITING_SELECTED_WEIGHT = 1f;
	public static float SKETCHOUTLINE_SELECTED_WEIGHT = 2f;
	public static float SKETCHOUTLINE_UNSELECTED_WEIGHT = 2f;


	public static int SKETCHOUTLINE_PATH_COLOUR_SELECTED = functions.color(30,
			30, 30);
	public static int SKETCHOUTLINE_PATH_COLOUR_DIAGRAM = functions.color(0,
			0, 0);
	
	
	
	public static int SKETCHOUTLINE_PATH_COLOUR_UNSELECTED = functions.color(
			200, 200, 200);
	public static final int SKETCHSHAPE_PATH_COLOUR_DEBUG = functions.color(
			255, 0, 0);

	public static boolean SKETCHOUTLINE_FILL_SELECTED = false;

	public static final boolean build_collision_mesh_detailed = true;

	public static final int SKETCHSHAPE_FILL_UNSELECTED_LAYER_COLOUR = functions
			.color(225, 225, 225);
	public static final int SKETCHSHAPE_PATH_COLOUR_UNSELECTED_LAYER = functions
			.color(200, 200, 200);

	public static boolean Draw_Curve_Segments = false;
	public static boolean Draw_Curves = false;
	public static boolean calculate_based_on_curves = true;

	public static boolean SLICEPLACE_RENDER_VOLUME = true;
	public static float scale = .1f;
	public static float select_dia = 20; // dist to select points
	public static float select_dia_touch = 40;
	public static float select_dia_default = 20;


	public static final float build_collision_mesh_res = 50f; //how course chair collision mesh is

	public static float SELECT_EDGE_DIST = 10;
	public static final float RENDER_PIXELS_PER_TRIANGLE_BEZIER = 3;
	public static int SKETCHSHAPE_FILL_SELECTED_COLOUR = functions.color(240,
			240, 240);
	public static int SKETCHSHAPE_FILL_UNSELECTED_COLOUR = functions.color(240,
			240, 240,50);
	public static int SKETCHSHAPE_FILL_DIAGRAM_COLOUR = functions.color(255,
			255, 255);
	
	//TODO: change diagram fill colour ?
	public static final int SKETCHSHAPE_FILL_SELECTED_DIAGRAM_COLOUR  = functions.color(0, 174,
			239);

	
	public static final float SKETCHSHAPE_FILL_UNSELECTED_WEIGHT = 1f;
	public static final float SKETCHSHAPE_FILL_SELECTED_WEIGHT = 1;

	public static int slot_selected_fill_colour = functions
			.color(255, 255, 255);
	public static int slot_unselected_fill_colour = functions.color(255, 255,
			255);
	public static int slat_selected_fill_colour = functions
			.color(255, 255, 255);
	public static int slat_unselected_fill_colour = functions.color(255, 255,
			255);

	public static boolean render_outline = true;
	public static float plane_thickness = 0.3f;
	public static float plane_thickness_default = 0.3f;

	public static final float LEG_BRUSH_RATIO_TOP = 0.75f;
	public static final float LEG_BRUSH_RATIO_BOTTOM = 0.5f;
	public static final int BEZIER_DETAIL_EDIT = 15;
	public static final int BEZIER_DETAIL_3D_PREVIEW = 15;
	public static final int BEZIER_DETAIL_3D_DIAGRAM = 20;
	public static final float MIN_CLOSE_SHAPE_DIST = 10;

	public static final float BEZIER_DETAIL_OFFSET_PREVIEW = 0.1f;
	public static float BEZIER_DETAIL_CALCULATIONS_PREVIEW = 0.01f; // smaller numbers more accurate but very very slow.

	public static final float BEZIER_DETAIL_OFFSET_RENDER = 0.01f; //TODO change this in release
	public static final float SMOOTH_AMOUNT = 0.3f;
	public static final float SKETCHOUTLINE_PATH_WEIGHT_DIAGRAM = 0.5f;
	public static final float SKETCHSLOTEDGE_PATH_WEIGHT_DIAGRAM = 0.5f;
	public static final float OUTLINE_RENDER_OFFSET = 1.0f;
	public static final float CONTROL_SPLINE_WEIGHT = 3;
	

	
	
	
	
	
	public static float BEZIER_DETAIL_CALCULATIONS_RENDER = 0.001f; // smaller numbers more accurate but very very slow.

	public static int offsetSide = SketchSpline.OFFSET_BOTH;
	public static boolean crossSectionsConstrainedToShape = true;
	public static float feltResolution = .1f;
	public static float chair_slat_end_size = 45; // how much should the last slat stick out

	public static float chair_width = 400;
	public static float spline_point_every = 30;
	public static float dist_between_adding_points = 20; //distance between points on splline

	public static float slat_x_spacing = 250;
	public static float slat_y_spacing = 300;
	public static float slat_spacing_x = 39f;

	public static boolean fill_sketch = false;
	public static boolean dynamic_offset = true;
	public static int renderChairColour = functions.color(255, 255, 255);
	public static float PATH_WIDTH_ZOOM = 1.5f;
	public static float splineMoveFalloff = 100;

}
