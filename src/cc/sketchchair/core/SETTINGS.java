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

import ModalGUI.GUIEvent;
import cc.sketchchair.sketch.SketchSpline;

/**
 * Contains settings for sketchChair. We need a better format for there variables.
 * @author gregsaul
 *
 */
public class SETTINGS {

	public static final int UNDO_LEVELS = 20;
	public static final boolean build_collision_mesh_detailed = true;
	public static final float MIN_SPACING = 20f;
	public static final int THUMBNAIL_HEIGHT = 300;
	public static final int THUMBNAIL_WIDTH = 300;

	public static boolean REC = false;
	public static boolean draw_collision_mesh = false;
	
	//public static float slat_width = 50;// drawing tool width
	//public static float leg_width = 25;
	public static float person_friction = 1f;
	public static float chair_damping_linear = .0001f;
	public static float chair_damping_ang = .001f;

	public static int sphere_res = 6;
	public static double optimize_outline_min_angle = 3;

	//Quality settings
	public static int cylinder_res = 15;

	public static float scale = .1f;
	//public static float pixels_per_mm_base = 5.6689342403628117913832199546485f;
	public static float pixels_per_mm_base = 0.35289342403628117913832199546485f;

	public static float pixels_per_mm_screen = 5.6689342403628117913832199546485f;
	public static float pixels_per_mm = pixels_per_mm_base * scale;

	public static float chair_width = 300;
	public static float slat_num = 5;

	public static float slot_tollerance = .03f * pixels_per_mm;
	public static float materialThickness = 0.25f;

	public static float spline_point_every = 30;

	//Colours

	public static int GRID_MAJOR_LINE = GLOBAL.applet.color(0, 0, 0, 255);
	public static float GRID_MAJOR_LINE_WEIGHT = 1f;

	public static int GRID_MINOR_LINE = GLOBAL.applet.color(0, 0, 0, 55);
	public static float GRID_MINOR_LINE_WEIGHT = 1f;

	//public static int SKETCHSHAPE_FILL_UNSELECTED_COLOUR = GLOBAL.applet.color(240, 240, 240);
	public static final float DEFAULT_MATERIAL_WIDTH = .37f;
	public static final float DEFAULT_SLAT_SPACING = 85;

	public static int person_fill_colour = GLOBAL.applet.color(250, 250, 250);

	public static final boolean WEB_MODE = false;
	public static final boolean APPLET_MODE = false;
	public static final float DEFAULT_SLATSLICE_HEIGHT = 100;
	public static final float MIN_RENDER_WIDTH = .3f;
	public static final float MIN_LEG_LEN = 2.0f;
	public static final int MOUSE_CLICKED_MIN_TIME = 1000;
	public static final int MOUSE_PRESSED_MIN_TIME = 1300;

	public static final boolean ENABLE_SELECT_MODEL_PLANES = true;
	
	
	public static final float MIN_ZOOM = 0.2f;
	public static final float MAX_ZOOM = 5;
	
	public static final float MIN_CAM_X_OFFSET = -4000f;
	public static final float MAX_CAM_X_OFFSET = 4000;
	
	public static final float MIN_CAM_Y_OFFSET = -2000f;
	public static final float MAX_CAM_Y_OFFSET = 2000;
	public static final float panelWidth = 900;
	public static final float panelHeight = 110;

	
	
	public static boolean DEBUG = false;
	public static String LANGUAGE = "ENG";
	public static boolean EXHIBITION_MODE = true; // deptreciated 
	public static boolean EXPERT_MODE = true;
	public static boolean TOUCH_SCREEN_MODE = false; 

	
	public static boolean DEVELOPER_MODE = false; 
	
	//PERSON COLOURS
	public static int ERGODOLL_FILL_COLOUR = GLOBAL.applet.color(225, 225, 225);
	public static int ERGODOLL_FILL_COLOUR_PERFORMANCE = GLOBAL.applet.color(
			245, 245, 245);
	public static int person_height_text_fill_colour = GLOBAL.applet.color(10,
			10, 10);

	//ENVIRONMENT COLOURS
	public int world_ground_colour = GLOBAL.applet.color(39, 35, 36);
	public int world_ground_under_colour = GLOBAL.applet.color(39, 35, 36, 10);
	public int background_colour = GLOBAL.applet.color(250, 250, 250);

	public static float gravity = 60f;

	public static boolean show_framerate = false;

	public int autoSaveInterval = 600;
	public float bezierDetail = 1;
	public static int renderWidth = 1500;
	public static int renderHeight = 1500;
	
	public static boolean autoSave = false;
	public static int chairSaveNum = 0;
	public static float chair_friction = 1;
	public static boolean seperate_slots = false;

	public static boolean auto_seat = true;
	public static boolean hybernate = true;
	public static boolean render_chairs = true;
	public static boolean auto_build = true;
	public static float version = .90f;
	public static float chair_slat_end_size = 45;
	public static float chair_slatslot_end_size = 25;
	public static boolean RENDER_CENTRE_MASS = false;
	
	//guide windows
	public static int GUIDE_WINDOW_WIDTH = 800;
	public static int GUIDE_WINDOW_HEIGHT = 500;
	public static boolean displayIntroPanel = false;
	public static boolean useSliceCollisionDetection = false;
	public static float simplifyAmount = 1f;
	public static boolean autoSaveMakePattern = false;
	public static String autoSaveMakeLocation = "/";
	public static boolean addLegSlices = true;
	public static boolean autoRefreshTextures = false;
	public static float mouseMoveClamp = 10;
	public static boolean autoReset = false;
	public static int autoResetSeconds = 60;


	
	

}
