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

import java.awt.Frame;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JFileChooser;

import cc.sketchchair.environments.Environments;
import cc.sketchchair.ragdoll.ergoDoll;
import cc.sketchchair.sketch.Sketch;
import cc.sketchchair.sketch.SketchGlobals;
import cc.sketchchair.sketch.SketchTools;
import cc.sketchchair.widgets.WidgetLoad;
import cc.sketchchair.widgets.WidgetMaterials;
import cc.sketchchair.widgets.WidgetPlanes;
import cc.sketchchair.widgets.WidgetPreviewPanel;
import cc.sketchchair.widgets.WidgetSlices;
import cc.sketchchair.widgets.WidgetToolSettings;

import CloudHook.CloudHook;
import ModalGUI.GUIComponentSet;
import ModalGUI.GUIPanel;
import ModalGUI.GUIPanelTabbed;
import ModalGUI.ModalGUI;
import ShapePacking.spShapePack;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;

import toxi.geom.Ray3D;
import toxi.geom.Vec3D;

//import toxi.physics.VerletPhysics;
/**
 * Static global variables used in SketchChair. 
 * Since keeping one large Global class is not good coding practice this class might be either split up into several singleton or factory classes. 
 * @author gregsaul
 *
 */
public class GLOBAL {

	public static boolean useMaskedUpdating = false;

	static SketchProperties sketchProperties = new SketchProperties();
	
	public static double CAM_OFFSET_X = 0;
	public static double CAM_OFFSET_Y =  0;
	public static int windowWidth;
	public static int windowHeight;
	public static PFont font;
	public PImage clickToStart;
	public String version = "0.9.0.2";
	public static boolean forceReset = false;
	public static boolean cropExportToScreen = false;
	public static SketchGlobals SketchGlobals;
	public static boolean dxfCapture = false;
	public static String dxfLocation = "./designOutput.dxf";
	//public static MovieMaker mm;
	public static boolean captureScreen = false;
	static int designDisplayList;


	//contains the title img
	static PImage tittleImg;

	static Object myMovie = null;
	static int tittleImageNum = 0;

	static public SketchChairs sketchChairs;

	//static public VerletPhysics physics;
	static public jBullet jBullet;
	static public UITools uiTools;
	//static public RagDoll ragDoll = null;
	public static ModalGUI gui;
	static public Environments environments;

	/*
	 * What mode are we in? 0 - sketch seat 1 - view in 3d
	 */
	static public int mode = 0;
	static public float rotateModelsX;

	static public float rotateModelsY;
	static public PGraphics g;

	public static PApplet applet;
	public static boolean savePDF = false;
	public static int sketch_id;
	static double ZOOM = 1.5f;
	public static Ray3D debugRay = null;

	public static Vec3D debugRayIntersection = null;
	public static ergoDoll person = null;

	public static spShapePack shapePack;
	static public Undo undo = new Undo();

	static public Long tick = 0l;
	static public WidgetPlanes planesWidget;

	static public WidgetSlices slicesWidget;
	static public WidgetLoad loadWidget;
	static public WidgetMaterials widgetMaterials;
	static public WidgetToolSettings widgetToolSettings;
	public static boolean performanceMode = true;

	static JFileChooser fc;

	public static String pdfSaveLocation = null;
	public static MeasureTool measuretTool;
	public static String LAST_SAVED_LOCATION = null;
	public static int timeoutCounter = 1200000 * 1000; //seconds of inactivity untill timeout. 

	public static int inativeCounter = 0; //seconds of inactivity untill timeout. 
	public static boolean personTranslate = true;
	public static boolean autoRotate = false;
	public static boolean modeChanged = false;
	public static float rememberLasty;
	public static boolean resetting = false;
	static CloudHook cloudHook = new CloudHook(
			"http://sketchchair.cc/framework/CloudHook.php");
	public static boolean autoOpenPDF = true;
	public static String lastLoadLocation = null;
	public static boolean screenshot = false;
	public static int renderChairColour = 255;
	public static WidgetLoad widgetLoad;
	public static String dxfSaveLocation;
	public static boolean saveDXF;
	public static SketchChair copyOfChair;
	public static GUIComponentSet toggleSetSlices;
	public static String pngPreviewSaveLocation;
	public static boolean exportPreviewPNG = false;
	static SkchAutamata skchAutomatic = new SkchAutamata();
	public static boolean floorOn = true;
	public static boolean saveChairToFile =false;
	public static boolean saveChairToFileAuto = false;
	public static boolean displayIntroPanel = true;
	public static boolean forceResize = false;
	public static boolean deleteAllChairsFlag =false;

	
	public static String username = null;
	public static String password = null;
	public static boolean authenticated = false;
	public static String sessionID = null;

	public static float prevRotateModelsX = 0;
	public static float prevRotateModelsY = -(float)Math.PI/4;

	public static GUIPanelTabbed designToolbarAdvanced = null;
	public static GUIPanelTabbed  designToolbarBasic = null;

	public static GUIPanel slicePanel;

	public static WidgetPreviewPanel previewWidget;

	public static processing.core.PSurface surface;

	public static int planeID = 0;

	protected static String loadChairLocation = null;
	public static String saveChairLocation = null;
	public static String savePatternLocation = null;

	protected static String saveDXFLocation = null;

	protected static String importSVGLocation = null;



	// create and load default properties
	Properties properties = new Properties();

	public static boolean smoothRender = false;

	public static boolean debugPickBuffer =false;

	public static GUIPanelTabbed designToolbarPattern = null;

	public static GUIPanel cameraPanel;

	public static GUIPanel patternCameraPanel;


	
	public final static int color(int x, int y, int z) {

		if (x > 255)
			x = 255;
		else if (x < 0)
			x = 0;
		if (y > 255)
			y = 255;
		else if (y < 0)
			y = 0;
		if (z > 255)
			z = 255;
		else if (z < 0)
			z = 0;

		return 0xff000000 | (x << 16) | (y << 8) | z;

	}

	public final static int color(int x, int y, int z, int a) {

		if (a > 255)
			a = 255;
		else if (a < 0)
			a = 0;
		if (x > 255)
			x = 255;
		else if (x < 0)
			x = 0;
		if (y > 255)
			y = 255;
		else if (y < 0)
			y = 0;
		if (z > 255)
			z = 255;
		else if (z < 0)
			z = 0;

		return (a << 24) | (x << 16) | (y << 8) | z;

	}

	/**
	 * @return the zOOM
	 */
	public static double getZOOM() {
		return ZOOM;
	}

	public static boolean isMacOSX() {
		String osName = System.getProperty("os.name");
		return osName.startsWith("Mac OS X");
	}

	public static boolean isWindows() {
		String osName = System.getProperty("os.name");
		return osName.startsWith("Windows");
	}

	/**
	* @param zOOM the zOOM to set
	*/
	public static void setZOOM(float zOOM) {
		ZOOM = zOOM;
	}

	GLOBAL(PApplet applet) {
		
		LOGGER.info("Making GLOBAL");
			

		GLOBAL.applet = applet;
		gui = new ModalGUI();
		gui.applet = applet;
		gui.appletStatic = applet;
		
		
		ZOOM = 1f;

		
//String[] fonts = PFont.list();


//for(int i = 0; i < fonts.length; i++)
//	cc.sketchchair.sketch.LOGGER.info(fonts[i]);
		//this.font = applet.loadFont("SegoeUI-12.vlw");
		
		if(isMacOSX())
		this.font = applet.createFont("Helvetica", 12);
		else
		this.font = applet.createFont("Arial", 12);
	
		gui.myFontMedium = this.font;
		/*
		URL url = findResource("TrebuchetMS-12.vlw");
		try {
		    InputStream input = createInput(filename);
		    this.font =  new PFont(input);

		  } catch (Exception e) {
		    die("Could not load font " + filename + ". " +
		        "Make sure that the font has been copied " +
		        "to the data folder of your sketch.", e);
		  }
		  */

		sketchChairs = new SketchChairs();
		//physics = new VerletPhysics(
		//new Vec3D(0, 2f, 0), 300, 90f, .090f);
		jBullet = new jBullet();
		uiTools = new UITools(applet);
		//ragDoll = null;

		environments = new Environments();
		/*
		 * What mode are we in? 0 - sketch seat 1 - view in 3d
		 */
		mode = 0;

		rotateModelsX = 0;
		rotateModelsY = 0;

		savePDF = false;
		//	psketch_id;
		setZOOM(1f);

		debugRay = null;
		debugRayIntersection = null;

		person = null;
		shapePack = new spShapePack();
		undo = new Undo();
		tick = 0l;


		performanceMode = false;

		fc = null;
		pdfSaveLocation = null;
		measuretTool = new MeasureTool();
		LAST_SAVED_LOCATION = null;

		SketchGlobals = new SketchGlobals();
		SketchGlobals.undo = this.undo;

	}

	public final int color(float x, float y, float z) {

		if (x > 255)
			x = 255;
		else if (x < 0)
			x = 0;
		if (y > 255)
			y = 255;
		else if (y < 0)
			y = 0;
		if (z > 255)
			z = 255;
		else if (z < 0)
			z = 0;

		return 0xff000000 | ((int) x << 16) | ((int) y << 8) | (int) z;

	}

}
