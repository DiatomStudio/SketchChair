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

import java.awt.Desktop;
import java.awt.Frame;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;
import javax.jnlp.FileSaveService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import toxi.geom.Plane;
import toxi.geom.Ray3D;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;
import ModalGUI.GUIButton;
import ModalGUI.GUIComponentSet;
import ModalGUI.GUIEvent;
import ModalGUI.GUIImage;
import ModalGUI.GUIToggle;
import ModalGUI.GUIWindow;
import ModalGUI.ModalGUI;
import ShapePacking.BezierControlNode;

import cc.sketchchair.environments.Environment;
import cc.sketchchair.functions.functions;
import cc.sketchchair.geometry.SlicePlane;
import cc.sketchchair.ragdoll.ergoDoll;
import cc.sketchchair.sketch.SETTINGS_SKETCH;
import cc.sketchchair.sketch.SketchPoint;
import cc.sketchchair.sketch.SketchShape;
import cc.sketchchair.sketch.SketchTools;
import cc.sketchchair.widgets.WidgetLoad;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;

/**
 * Acts as a interface between the GUI and the SketchChair engine. 
 * This class also proves some functions such as mapping a 2d position into model space that might be best put else where later.
 * This class could do with some tidying up. 
 * @author gregsaul
 *
 */

public class UITools {

	public static final int DRAW_TOOL = 1;
	public static final int SELECT_TOOL = 2;
	public static final int SELECT_BEZIER_TOOL = 9;
	public static int LEG_TOOL = 7;

	public static final int MOVE_CAM_TOOL = 3;

	public static final int ZOOM_CAM_TOOL = 4;

	public static final int ROTATE_CAM_TOOL = 5;

	public static final int ADD_DOLL_TOOL = 6;
	public static final int SCALE_PERSON_TOOL = 8;
	public static final int VIEW_SHAPE_PACK = 1;
	public static final int VIEW_CHAIR_EDIT = 2;
	public static final int VIEW_SAVE_CHAIR = 3;
	public static final int DRAW_PATH_TOOL = 12;
	public static final int MOVE_OBJECT = 16;

	public static final int SCALE_TOOL = 19;
	public static final int MOVE_2D_TOOL = 20;
	public static final int SLICES_SINGLE_SLICE = 23;
	public static final int SLICES_GROUP_SLICES = 24;
	public static final int SLICES_SINGLE_SLAT = 28;
	public static final int SLICES_SLATSLICE_GROUP = 29;
	public static final int SLICES_EDIT = 30;

	
	GUIComponentSet toggleSet = new GUIComponentSet();

	static void doLoad() {
		load("chair.cha");

	}

	static void doSave() {

		save("chair.cha");

	}

	static void load(Document doc) {

		LOGGER.debug("UITools: about to load Document");

		//return fd.getFile();
		//FileInputStream fileIn = new FileInputStream(name);
		//ObjectInputStream in = new ObjectInputStream(fileIn);
		Element e = (Element) doc.getChild(0);

		//check to make sure that it's a sketch chair doc. 
		if (!e.getQualifiedName().equals("SketchChairDoc"))
			return;

		e = (Element) e.getChild(0);

		SketchChair loadedChair = null;
		LOGGER.debug("UITools: about to load chair structure");

		//loading a chair
		if (e.getQualifiedName().equals("SketchChair")) {
			loadedChair = new SketchChair(e);
		}
		LOGGER.debug("UITools: loaded chair object");

		if (loadedChair != null) {

			//	loadedChair.justLoaded();

			GLOBAL.sketchChairs.add(loadedChair);
			//GLOBAL.sketchChairs.setCurChair(loadedChair);

			if (GLOBAL.planesWidget != null
					&& GLOBAL.planesWidget.slider != null) {
				GLOBAL.planesWidget.slider.setVal(loadedChair.getWidth());
				//GLOBAL.planesWidget.SlatSliderStart.setVal(loadedChair.startCoverPercent);
				// GLOBAL.planesWidget.SlatSliderEnd.setVal(loadedChair.endCoverPercent);
				// GLOBAL.planesWidget.SlatSliderSpacing.setVal(loadedChair.slatSpacingX);
				GLOBAL.slicesWidget.rebuild(loadedChair.crossSliceSelections);
			}

			GLOBAL.sketchChairs.getCurChair().localSavelocation = GLOBAL.lastLoadLocation;

		} else {
			LOGGER.error("Could not load chair from XML.");
		}

	}

	static void load(InputStream inputStream) {

		Builder builder = new Builder();

		Document doc = null;

		try {
			doc = builder.build(inputStream);
			load(doc);
		} catch (ValidityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void load(String name) {

		Builder builder = new Builder();
		InputStream input = null;

		GLOBAL.lastLoadLocation = name;
		LOGGER.debug("loading about to open file pointer.");
		try {
			input = new FileInputStream(name);
		} catch (FileNotFoundException e1) {
			LOGGER.debug("UITolls: FileNotFoundException");
			e1.printStackTrace();
		}
		LOGGER.debug("loading file pointer open.");

		Document doc = null;

		if (input == null)
			LOGGER.debug("UITools: File input is null.");

		try {
			if (builder == null)
				LOGGER.debug("UITools: XOM builder is null.");

			LOGGER.debug("UITools: about to build XML from input.");

			doc = builder.build(input);

			if (doc == null)
				LOGGER.debug("UITools: doc XOM obj is null.");

			load(doc);
		} catch (ValidityException e) {
			// TODO Auto-generated catch block
			LOGGER.error("UITools: Could not make XML document: ValidityException");
			e.printStackTrace();
		} catch (ParsingException e) {
			// TODO Auto-generated catch block
			LOGGER.error("UITools: Could not make XML document: ParsingException");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.error("UITools: Could not make XML document: IOException");
			e.printStackTrace();
		}

	}

	static void save(OutputStream stream, String name) {

		if (GLOBAL.sketchChairs.getCurChair() != null) {
			PGraphics saveImg = GLOBAL.sketchChairs.getCurChair()
					.renderToFile();

			if (saveImg.width > 0 && saveImg.height > 0)
				saveImg.save(name + ".png");

		}

	}

	static void save(String name) {

		if (GLOBAL.sketchChairs.getCurChair() != null) {
			try {
				Element root = new Element("SketchChairDoc");
				root.addAttribute(new Attribute("version", String
						.valueOf(SETTINGS.version)));
				
				root.appendChild(GLOBAL.sketchChairs.getCurChair().toXML());

				Document doc = new Document(root);
				OutputStream outXML = new FileOutputStream(name);
				outXML = new BufferedOutputStream(outXML);
				Serializer serializer = new Serializer(outXML, "ISO-8859-1");
				serializer.write(doc);

				GLOBAL.sketchChairs.getCurChair().localSavelocation = name;

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			float rememberThis = GLOBAL.rotateModelsX;
			GLOBAL.rotateModelsX = .01f;
			
			/*
			PImage saveImg = GLOBAL.sketchChairs.getCurChair().renderDiagram(
					SETTINGS.THUMBNAIL_HEIGHT, SETTINGS.THUMBNAIL_HEIGHT,
					(float) -(Math.PI / 8), (float) -(Math.PI / 4), false);

			GLOBAL.rotateModelsX = rememberThis;
			if (saveImg.width > 0 && saveImg.height > 0)
				saveImg.save(name + ".png");
				*/

		}
	}
	
	
	static void savePattern(String name) {
		GLOBAL.shapePack.makeSVG(GLOBAL.applet, name);
/*
		if (GLOBAL.sketchChairs.getCurChair() != null) {
			try {
				Element root = new Element("SketchChairDoc");
				root.addAttribute(new Attribute("version", String
						.valueOf(SETTINGS.version)));

				root.appendChild(GLOBAL.sketchChairs.getCurChair().toXML());

				Document doc = new Document(root);
				OutputStream outXML = new FileOutputStream(name);
				outXML = new BufferedOutputStream(outXML);
				Serializer serializer = new Serializer(outXML, "ISO-8859-1");
				serializer.write(doc);

				GLOBAL.sketchChairs.getCurChair().localSavelocation = name;

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			float rememberThis = GLOBAL.rotateModelsX;
			GLOBAL.rotateModelsX = .01f;
			PImage saveImg = GLOBAL.sketchChairs.getCurChair().renderDiagram(
					SETTINGS.THUMBNAIL_HEIGHT, SETTINGS.THUMBNAIL_HEIGHT,
					(float) -(Math.PI / 8), (float) -(Math.PI / 4), false);

			GLOBAL.rotateModelsX = rememberThis;
			if (saveImg.width > 0 && saveImg.height > 0)
				saveImg.save(name + ".png");

		}
		*/
	}

	
	
	

	private int currentTool = 1;
	public int currentView = 2;
	public static int MEASURE_TOOL = 21;

	public static int CROSSSLICE_EDIT = 22;
	public static  PImage DRAW_TOOL_CURSOR = null;
	public static  PImage SELECT_TOOL_CURSOR = null;
	public static  PImage ADD_DOLL_TOOL_CURSOR = null;
	public static  PImage MOVE_OBJECT_CURSOR = null;
	
	
	
	public static  PImage ROTATE_CAM_TOOL_CURSOR =null;
	public static  PImage ZOOM_CAM_TOOL_CURSOR = null;
	public static  PImage MOVE_CAM_TOOL_CURSOR = null;

	public static  PImage CURSOR_ADD_SLICE = null;

	public static  PImage LINE_TOOL_CURSOR;
	public static  PImage SCALE_TOOL_CURSOR;
	private PGraphics pickBuffer;
	private SketchPoint selectedVec = new SketchPoint(0, 0);
	public boolean render3dPreview = false;
	private SlicePlane selectedVecPlane = null;

	public SlicePlane curSliceplane = null;

	private SketchShape selectedShape = null;
	private boolean isSelectedVecOnOutline = false;
	public static int MOUSE_RIGHT = 39;
	public static int MOUSE_MIDDLE = 3;
	public static final int MOUSE_LEFT = 37;
	static final int LANGUAGE_ENG = 1;
	static final int LANGUAGE_JP = 2;
	private static final int NONE = -1;
	public static final int SLICE_EDIT_MODE_ADD = 0;
	public static final int SLICE_EDIT_MODE_POS = 1;
	public static final int SLICE_EDIT_MODE_SPACING = 2;
	private int editing = 1;
	public int preview = 2;

	public int editingMode = isEditing();
	public int pmouseX;
	public int pmouseY;
	public int mouseX;
	public int mouseY;
	public float mouseXworld;
	public float mouseYworld;
	public float pmouseXworld;
	public float pmouseYworld;
	public boolean mouseDown = false;

	public int mouseButton;
	public boolean renderNodesFlag = false;
	boolean useGrid = true;
	float gridWidth = 50f;
	float gridHeight = 50f;
	float minorGridnumber = 10;
	public RigidBody selectedBody;
	public boolean keyPressed;

	public char key;
	public int keyCode;
	private int selectCooldown;
	public MeasureTool measureTool = new MeasureTool();

	private PImage MEASURE_TOOL_CURSOR;

	private ArrayList<Clickable> clickables = new ArrayList<Clickable>();
	public boolean patternView = false;
	public float brush_dia = 40;
	public boolean mousePressed = false;
	private int mousePressedTime = 0;
	private int mouseSingleClickTime;
	public int loadedCursor;
	private boolean waitingForSingleClick;
	public boolean hasCursorControl = false;
	private PImage CURSOR_EDIT_SLICE_POS;
	private PImage CURSOR_EDIT_SLICE_SPACING;
	public int sliceToolMode = 0;
	private boolean prevMouseWasMove = false;
	public static SketchTools SketchTools;

	UITools(PApplet applet) {
		SketchTools = new SketchTools(applet);
	}

	public void addListener(Clickable clickable) {
		this.clickables.add(clickable);
	}

	public void addPlane(GUIEvent e) {
		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().addLayer();

	}

	public void autoLoad() {
		load("autoSave.cha");
	}

	public void autoRotate(GUIEvent e) {

		GLOBAL.autoRotate = !GLOBAL.autoRotate;

		setCurrentTool(ROTATE_CAM_TOOL);
		if (!GLOBAL.autoRotate)
			GLOBAL.rotateModelsY = 0;
		
		GLOBAL.slicesWidget.unselectAll();
		GLOBAL.planesWidget.unselectAll();
		
	}

	public void autoSave() {

		save("autoSave.cha");
	}

	void build(PApplet applet) {
		DRAW_TOOL_CURSOR = applet.loadImage("gui/GUI_DRAW_TOOL_CURSOR.png");
		SELECT_TOOL_CURSOR = applet.loadImage("gui/cursors/CURSOR_SELECT.png");
		ADD_DOLL_TOOL_CURSOR = applet
				.loadImage("gui/cursors/CURSOR_HAND_UP.png");
		MOVE_CAM_TOOL_CURSOR = applet
				.loadImage("gui/cursors/CURSOR_CAM_MOVE.png");
		ROTATE_CAM_TOOL_CURSOR = applet
				.loadImage("gui/cursors/CURSOR_CAM_ROTATE.png");
		ZOOM_CAM_TOOL_CURSOR = applet.loadImage("gui/cursors/CURSOR_CAM_ZOOM.png");
		LINE_TOOL_CURSOR = applet.loadImage("gui/GUI_LINE_TOOL_UP.png");
		SCALE_TOOL_CURSOR = applet.loadImage("gui/GUI_SCALE_TOOL_CURSOR.png");
		MEASURE_TOOL_CURSOR = applet
				.loadImage("gui/GUI_MEASURE_TOOL_CURSOR.png");
		MOVE_OBJECT_CURSOR = applet
				.loadImage("gui/cursors/CURSOR_HAND_UP.png");
		CURSOR_ADD_SLICE = applet
				.loadImage("gui/cursors/CURSOR_SLAT_ADD.png");
		this.SketchTools.build(GLOBAL.applet);
		
		CURSOR_EDIT_SLICE_POS = applet
				.loadImage("gui/cursors/CURSOR_SLAT_EDIT.png");
		this.SketchTools.build(GLOBAL.applet);
		
		CURSOR_EDIT_SLICE_SPACING = applet
				.loadImage("gui/cursors/CURSOR_SLAT_EDIT_SPACING.png");
		this.SketchTools.build(GLOBAL.applet);
		
		//GLOBAL.applet.cursor(UITools.SELECT_TOOL_CURSOR, 1,1);
		// buffer is created using applet dimensions
		//this.pickBuffer = applet.createGraphics(applet.width, applet.height,
		//	PConstants.P3D);
		//this.pickBuffer.ortho(-(applet.width / 2), (applet.width / 2),
		//-(applet.height / 2), (applet.height / 2), -1000, 10000);
	}

	public void camJumpFront(GUIEvent e) {
		GLOBAL.rotateModelsX = 0;
		GLOBAL.rotateModelsY = 0;

	}

	public void camJumpSide(GUIEvent e) {
		GLOBAL.rotateModelsX = 0;
		GLOBAL.rotateModelsY = (float) -(Math.PI / 2);

	}

	public void camJumpTop(GUIEvent e) {
		GLOBAL.rotateModelsX = (float) -(Math.PI / 2);
		GLOBAL.rotateModelsY = 0;

	}

	public void camJumpIso(GUIEvent e) {
		GLOBAL.rotateModelsX = (float) -(Math.PI / 8);
		GLOBAL.rotateModelsY = (float) -(Math.PI / 4);

		//g.rotateY(rotateRend);

	}

	public void captureScreen(GUIEvent e) {
		GLOBAL.captureScreen = true;

	}

	public void changeModeBasic(GUIEvent e) {
		SETTINGS.EXPERT_MODE = false;

		UI.setupGUITabsBasic(GLOBAL.applet, GLOBAL.gui);

		GLOBAL.modeChanged = true;
	}

	public void changeModeExpert(GUIEvent e) {
		SETTINGS.EXPERT_MODE = true;
		UI.setupGUITabsExpert(GLOBAL.applet, GLOBAL.gui);


		GLOBAL.modeChanged = true;
	}

	public void changeView(GUIEvent e) {

		this.toggleView(e);

		//	GLOBAL.sketchChairs.curChair.slicePlanesY.update();
		//GLOBAL.sketchChairs.curChair.addToShapePack();

		//	this.currentView = (int) e.val;

	}

	public void copyChair() {
		GLOBAL.copyOfChair = GLOBAL.sketchChairs.getCurChair().copy();
	}

	public void deleteAllChairsFlag(GUIEvent e) {
		GLOBAL.deleteAllChairsFlag = true;
	}

	public void deleteAllChairs(GUIEvent e) {
		//this.saveChairToFileAuto(null);
		//this.captureScreen(null);
		//GLOBAL.applet.draw();

		GLOBAL.sketchChairs.killAll();

	}

	public void deleteLastChair(GUIEvent e) {
		this.saveChairToFileAuto(null);
		this.captureScreen(null);
		GLOBAL.applet.draw();
		GLOBAL.sketchChairs.killLast();

	}

	public void flipChair(GUIEvent e) {
		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().flipDesign();
	}

	/**
	 * @return the currentTool
	 */
	public int getCurrentTool() {
		return currentTool;
	}

	/**
	 * @return the pickBuffer
	 */
	public PGraphics getPickBuffer() {
		return pickBuffer;
	}

	public Vec2D getPointOnPlane(Vec2D mousePoint, Plane planeIn) {

		Plane plane = new Plane(planeIn.copy(), planeIn.normal.copy());

		Vec3D mouseRayPos = new Vec3D(mousePoint.x, mousePoint.y, planeIn.z); // this only works for planes perpendicular to the screen
		Vec3D mouseRayDir = new Vec3D(0, 0, -1);

		Vec3D focusCentre = new Vec3D(
				((GLOBAL.windowWidth / 2) - (float)GLOBAL.CAM_OFFSET_X),
				((GLOBAL.windowHeight / 2) - (float)GLOBAL.CAM_OFFSET_Y), 0);

		//now mouse pos is refereced from the centre of the screen
		mouseRayPos.x -= (GLOBAL.windowWidth / 2);
		mouseRayPos.y -= (GLOBAL.windowHeight / 2);
		mouseRayPos.scaleSelf((float) (1 / GLOBAL.getZOOM()));

		mouseRayPos.addSelf(focusCentre);

		Ray3D ray;

		Vec3D mousePos = null;
		Vec3D intersect;
		mouseRayDir = new Vec3D(0, 0, -1);

		plane.z = 0;
		//we need to rotate the plane so that it matches the one on the draw view
		plane.normal.rotateY(GLOBAL.rotateModelsY);
		plane.normal.rotateX(GLOBAL.rotateModelsX);

		plane.addSelf(plane.normal.scale(planeIn.z));
		//mouseRayDir.rotateY(GLOBAL.rotateModelsY);
		//mouseRayDir.rotateX(GLOBAL.rotateModelsX);

		mouseRayPos.subSelf(focusCentre);
		ray = new Ray3D(mouseRayPos, mouseRayDir); // this should be the world position of the mouse poiner on the 0,0,-1 plane

		intersect = plane.getIntersectionWithRay(ray);

		if (intersect == null) {
			ray = new Ray3D(mouseRayPos, mouseRayDir.invert());
			intersect = plane.getIntersectionWithRay(ray);
		}

		ray = new Ray3D(mouseRayPos, mouseRayDir);
		ray.addSelf(focusCentre);

		//if(this.mouseDown)
		//	GLOBAL.debugRay = ray;

		if (intersect != null) {

			//  System.out.println(plane.getProjectedPoint(intersect));
			//	intersect.z -= plane.z*2;
			//intersect.x += 70;	
			//	System.out.println("before rotate " +intersect);
			//	intersect.subSelf(focusCentre.scale(GLOBAL.getZOOM()));

			// 	intersect.
			intersect.rotateX(-GLOBAL.rotateModelsX);
			intersect.rotateY(-GLOBAL.rotateModelsY);

			intersect.addSelf(focusCentre);

			//intersect.x += (GLOBAL.CAM_OFFSET_X*GLOBAL.getZOOM());
			//intersect.y += GLOBAL.CAM_OFFSET_Y;

			//intersect.rotateAroundAxis(axis, theta)
			mousePos = intersect;
			//mousePos.x += intersect.z;
			//System.out.println("after rotate " +intersect);
		}

		//get the chair matrix
		Matrix4f chairMatrix = new Matrix4f();
		Vec3D chairCentreOfMass = new Vec3D();
		if (GLOBAL.sketchChairs.getCurChair() != null
				&& GLOBAL.sketchChairs.getCurChair().rigidBody != null) {
			Transform transform = new Transform();
			GLOBAL.sketchChairs.getCurChair().rigidBody
					.getWorldTransform(transform);
			transform.getMatrix(chairMatrix);
			chairCentreOfMass = GLOBAL.sketchChairs.getCurChair().centreOfMass;
		}

		if (GLOBAL.sketchChairs.getCurChair() != null
				&& GLOBAL.sketchChairs.getCurChair().rigidBody != null
				&& chairMatrix != null && mousePos != null) {
			float centreMouseDeltaX = mousePos.x
					- ((chairMatrix.m03) / GLOBAL.jBullet.scale);
			float centreMouseDeltaY = mousePos.y
					- ((chairMatrix.m13) / GLOBAL.jBullet.scale);
			float centreMouseDeltaZ = mousePos.z
					- ((chairMatrix.m23) / GLOBAL.jBullet.scale);
			centreMouseDeltaZ = 0;

			float newX = ((chairMatrix.m00) * centreMouseDeltaX)
					+ ((chairMatrix.m01) * centreMouseDeltaY)
					+ ((chairMatrix.m02) * centreMouseDeltaZ);
			float newY = (((chairMatrix.m10) * centreMouseDeltaX)
					+ ((chairMatrix.m11) * centreMouseDeltaY) +
					((chairMatrix.m12) * centreMouseDeltaZ));
			float newZ = ((chairMatrix.m20) * centreMouseDeltaX)
					+ ((chairMatrix.m21) * centreMouseDeltaY)
					+ ((chairMatrix.m22) * centreMouseDeltaZ);

			newY = newY * -1;

			mousePos.x = newX + ((chairCentreOfMass.x) / GLOBAL.jBullet.scale);
			mousePos.y = newY + ((chairCentreOfMass.y) / GLOBAL.jBullet.scale);
			//mousePos.z = centreMouseDeltaZ + chairCentreOfMass.Z;
		}

		Vec2D intersect2D = null;
		if (mousePos != null) {
			intersect2D = new Vec2D(mousePos.x, mousePos.y);
			//intersect2D = new Vec2D(mousePoint.x, mousePoint.y);

		} else {
			intersect2D = new Vec2D(mousePoint.x, mousePoint.y);
		}
		if (intersect2D == null)
			intersect2D = new Vec2D(mousePoint.x, mousePoint.y);

		return intersect2D;

	}

	public Vec2D getPointOnPlaneOld(Vec2D mousePoint, Plane planeIn) {
		//get the chair matrix
		Matrix4f chairMatrix = new Matrix4f();
		Vec3D chairCentreOfMass = new Vec3D();
		if (GLOBAL.sketchChairs.getCurChair() != null
				&& GLOBAL.sketchChairs.getCurChair().rigidBody != null) {
			Transform transform = new Transform();
			GLOBAL.sketchChairs.getCurChair().rigidBody
					.getWorldTransform(transform);
			transform.getMatrix(chairMatrix);
			chairCentreOfMass = GLOBAL.sketchChairs.getCurChair().centreOfMass;
		}

		Plane plane = new Plane(planeIn.copy(), planeIn.normal.copy());

		Vec3D mouseRayPos = new Vec3D(mousePoint.x, mousePoint.y, plane.z); // this only works for planes perpendicular to the screen
		Vec3D mouseRayDir = new Vec3D(0, 0, -1);

		Vec3D focusCentre = new Vec3D(
				((GLOBAL.windowWidth / 2) - (float)GLOBAL.CAM_OFFSET_X),
				((GLOBAL.windowHeight / 2) - (float)GLOBAL.CAM_OFFSET_Y), 0);

		//now mouse pos is refereced from the centre of the screen
		mouseRayPos.x -= (GLOBAL.windowWidth / 2);
		mouseRayPos.y -= (GLOBAL.windowHeight / 2);
		mouseRayPos.scaleSelf((float) (1 / GLOBAL.getZOOM()));

		//mouseRayPos.addSelf(-screenCentre.x,-screenCentre.y, 0);
		mouseRayPos.rotateX((GLOBAL.rotateModelsX));
		mouseRayPos.rotateY((GLOBAL.rotateModelsY));

		mouseRayPos.addSelf(focusCentre);

		mouseRayDir.x = mouseRayDir.x - mouseRayPos.x;
		mouseRayDir.y = mouseRayDir.y - mouseRayPos.y;
		mouseRayDir.z = mouseRayDir.z - mouseRayPos.z;

		mouseRayDir.rotateX((GLOBAL.rotateModelsX));
		mouseRayDir.rotateY((GLOBAL.rotateModelsY));

		Ray3D ray = new Ray3D(mouseRayPos, mouseRayDir);

		Vec3D mousePos = null;

		if (GLOBAL.rotateModelsX == 0 && GLOBAL.rotateModelsY == 0
				&& plane.normal.x == 0 && plane.normal.y == 0)
			mousePos = new Vec3D(ray.x, ray.y, ray.z);

		//put these back in for 3d
		//	if(mousePos == null)	
		//		mousePos = plane.getIntersectionWithRay(ray);

		if (mousePos == null) {

			mouseRayDir = new Vec3D(0, 0, 1);
			mouseRayDir.x = mouseRayDir.x - mouseRayPos.x;
			mouseRayDir.y = mouseRayDir.y - mouseRayPos.y;
			mouseRayDir.z = mouseRayDir.z - mouseRayPos.z;

			mouseRayDir.rotateX((GLOBAL.rotateModelsX));
			mouseRayDir.rotateY((GLOBAL.rotateModelsY));

			ray = new Ray3D(mouseRayPos, mouseRayDir);
			plane.normal.z *= -1;
			//put these back in for 3d
			//	mousePos = plane.getIntersectionWithRay(ray);
		}

		if (GLOBAL.sketchChairs.getCurChair() != null
				&& GLOBAL.sketchChairs.getCurChair().rigidBody != null
				&& chairMatrix != null && mousePos != null) {
			float centreMouseDeltaX = mousePos.x
					- ((chairMatrix.m03) / GLOBAL.jBullet.scale);
			float centreMouseDeltaY = mousePos.y
					- ((chairMatrix.m13) / GLOBAL.jBullet.scale);
			float centreMouseDeltaZ = mousePos.z
					- ((chairMatrix.m23) / GLOBAL.jBullet.scale);
			centreMouseDeltaZ = 0;

			float newX = ((chairMatrix.m00) * centreMouseDeltaX)
					+ ((chairMatrix.m01) * centreMouseDeltaY)
					+ ((chairMatrix.m02) * centreMouseDeltaZ);
			float newY = (((chairMatrix.m10) * centreMouseDeltaX)
					+ ((chairMatrix.m11) * centreMouseDeltaY) + ((chairMatrix.m12) * centreMouseDeltaZ));
			float newZ = ((chairMatrix.m20) * centreMouseDeltaX)
					+ ((chairMatrix.m21) * centreMouseDeltaY)
					+ ((chairMatrix.m22) * centreMouseDeltaZ);

			newY = newY * -1;

			mousePos.x = newX + ((chairCentreOfMass.x) / GLOBAL.jBullet.scale);
			mousePos.y = newY + ((chairCentreOfMass.y) / GLOBAL.jBullet.scale);
			//mousePos.z = centreMouseDeltaZ + chairCentreOfMass.Z;
		}

		Vec2D intersect2D = null;
		if (mousePos != null) {
			intersect2D = new Vec2D(mousePos.x, mousePos.y);
			//intersect2D = new Vec2D(mousePoint.x, mousePoint.y);

		} else {
			intersect2D = new Vec2D(mousePoint.x, mousePoint.y);
		}
		if (intersect2D == null)
			intersect2D = new Vec2D(mousePoint.x, mousePoint.y);

		return intersect2D;

	}

	public Vec2D getPointTranslated(Vec2D mousePoint) {

		Vec2D mouseRayPos = new Vec2D(mousePoint.x, mousePoint.y); // this only works for planes perpendicular to the screen

		Vec2D focusCentre = new Vec2D(
				((GLOBAL.windowWidth / 2) - (float)GLOBAL.CAM_OFFSET_X),
				((GLOBAL.windowHeight / 2) - (float)GLOBAL.CAM_OFFSET_Y));

		mouseRayPos.x -= (GLOBAL.windowWidth / 2);
		mouseRayPos.y -= (GLOBAL.windowHeight / 2);
		mouseRayPos.scaleSelf((float) (1 / GLOBAL.getZOOM()));

		mouseRayPos.addSelf(focusCentre);
		return mouseRayPos;

	}

	Vec2D getPointTranslatedFromWorld(Vec2D mousePoint) {

		Vec2D mouseRayPos = new Vec2D(mousePoint.x, mousePoint.y); // this only works for planes perpendicular to the screen

		Vec2D focusCentre = new Vec2D(
				((GLOBAL.windowWidth / 2) - (float)GLOBAL.CAM_OFFSET_X),
				((GLOBAL.windowHeight / 2) - (float)GLOBAL.CAM_OFFSET_Y));

		mouseRayPos.subSelf(focusCentre);

		mouseRayPos.scaleSelf((float) GLOBAL.getZOOM());

		mouseRayPos.x += (GLOBAL.windowWidth / 2);
		mouseRayPos.y += (GLOBAL.windowHeight / 2);

		return mouseRayPos;

	}

	public void gotoWebsite(GUIEvent e) {
		/*
		String url = "http://www.sketchChair.com"; 
		try {
			// not compatible in JRE 1.5
			//java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		*/

	}

	public void hybernateChairToFileAuto(GUIEvent e) {

		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().addToShapePack();

		String computername = functions.getComputerName();
		String currentDir = new File(".").getAbsolutePath();
		currentDir = currentDir.substring(0, currentDir.length() - 1);

		int id = 0;
		boolean nameFound = false;
		String location = "";

		while (!nameFound && id < 10000) {
			location = currentDir + "savedChairs\\hybernate\\" + computername
					+ "_chair-" + id + ".cha";
			File f = new File(location);

			if (!f.exists())
				nameFound = true;

			id++;
		}

		save(location);
		//System.out.println("saving to " + location);
		SETTINGS.chairSaveNum = id;
		GLOBAL.LAST_SAVED_LOCATION = location;

	}

	/**
	 * @return the editing
	 */
	public int isEditing() {
		return editing;
	}

	boolean isEditingTool(int TOOL) {

		if (TOOL == DRAW_TOOL || TOOL == SELECT_TOOL || TOOL == LEG_TOOL
				|| TOOL == SELECT_BEZIER_TOOL || TOOL == DRAW_PATH_TOOL
				|| TOOL == SCALE_TOOL || TOOL == MOVE_2D_TOOL)
			return true;
		else
			return false;

	}

	public void runAutomatedActions(GUIEvent e) {

		GLOBAL.skchAutomatic.start();

	}

	public void loadTemplateChair(GUIEvent e) {

		//if(GLOBAL.widgetLoad  == null){
		GLOBAL.widgetLoad = new WidgetLoad();
		GLOBAL.widgetLoad.setupGUI(GLOBAL.applet, GLOBAL.gui);
		//	}
		GLOBAL.widgetLoad.show();
	}

	public void mouseDown() {

		
		if (GLOBAL.gui.overComponent())
			GLOBAL.planesWidget.mousePressed(mouseX, mouseY);
		
		//let the gui handle any mouse down events
		if (GLOBAL.gui.overComponent() && GLOBAL.gui.hasFocus()) 
		return;
		
	
//Jump to front if we're in?
		
			if (mouseButton == UITools.MOUSE_LEFT
					&& isEditingTool(this.getCurrentTool()))
				this.camJumpFront(null);
			
			
			

			for (Clickable obj : this.clickables) {
				obj.mousePressed();
			}

			
			
			
			
			
			
			//model manip!
			if (this.getCurrentTool() == UITools.MOVE_OBJECT
					|| this.SketchTools.getCurrentTool() == SketchTools.LEG_TOOL
					|| this.SketchTools.getCurrentTool() == SketchTools.DRAW_TOOL
					|| this.SketchTools.getCurrentTool() == SketchTools.SELECT_TOOL
					&& !GLOBAL.gui.overComponent()
					&& !GLOBAL.sketchChairs.getCurChair().overSelectPoint(
							GLOBAL.uiTools.mouseX, GLOBAL.uiTools.mouseY)) {
				Vec2D mousePos = this.getPointTranslated(new Vec2D(this.mouseX,
						this.mouseY));
				GLOBAL.jBullet.mouseDragged(mousePos.x, mousePos.y);
			}

			if ((GLOBAL.uiTools.getCurrentTool() == UITools.SCALE_TOOL)
					&& !GLOBAL.gui.overComponent()) {
				//GLOBAL.person.dragScale(mouseX,mouseX);	
			}

			// middle click
			if (mouseButton == UITools.MOUSE_RIGHT
					|| this.getCurrentTool() == UITools.MOVE_CAM_TOOL) {

				if (SETTINGS.render_chairs
						&& GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT) {
					
					if(this.prevMouseWasMove){
						
					moveView((UI.mouseX - UI.pmouseX)
							* (float)(1 / GLOBAL.ZOOM),
							(UI.mouseY - UI.pmouseY)
							* (float)(1 / GLOBAL.ZOOM));			
					}else{
						this.prevMouseWasMove = true;
					}
					
					
					}

				
				
				if (GLOBAL.uiTools.currentView == UITools.VIEW_SHAPE_PACK) {
					GLOBAL.shapePack.CAM_OFFSET_X += (UI.mouseX - UI.pmouseX)
							* (1 / GLOBAL.shapePack.ZOOM);
					GLOBAL.shapePack.CAM_OFFSET_Y += (UI.mouseY - UI.pmouseY)
							* (1 / GLOBAL.shapePack.ZOOM);

				}
				
				
				
			}

			if (!GLOBAL.gui.overComponent()) {
				if (this.getCurrentTool() == UITools.ZOOM_CAM_TOOL) {

					if (SETTINGS.render_chairs
							&& GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT){
						
						this.zoomView(((UI.mouseY - UI.pmouseY) * .01f));
						
						
					}

					if (SETTINGS.render_chairs
							&& GLOBAL.uiTools.currentView == UITools.VIEW_SHAPE_PACK) {
						GLOBAL.shapePack.zoomView((UI.mouseY - UI.pmouseY) * .01f, this.mouseX, this.mouseY) ;

					}

				}

				if (this.getCurrentTool() == UITools.ROTATE_CAM_TOOL
						|| mouseButton == UITools.MOUSE_MIDDLE) {
					if (SETTINGS.render_chairs
							&& GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT) {
						GLOBAL.rotateModelsY -= (UI.pmouseX - UI.mouseX) / 90f;
						GLOBAL.rotateModelsX += (UI.pmouseY - UI.mouseY) / 90f;
					}
				}

				if (this.getCurrentTool() == UITools.SELECT_TOOL
						&& GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT) {
					updateVecMove();
				}

			}

			if (this.getCurrentTool() != UITools.ROTATE_CAM_TOOL && GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT)
				GLOBAL.person.mouseDown(mouseX, mouseY);

			Vec2D mousePos = this.getPointTranslated(new Vec2D(this.mouseX,
					this.mouseY));

			GLOBAL.measuretTool.mousePressed(mousePos.x, mousePos.y);

		

	}

	public void mouseDragged() {
		
		
	
		
		
		if (!GLOBAL.gui.overComponent() && !GLOBAL.gui.hasFocus() && !GLOBAL.gui.clickStartedOn()&& GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT) {

			for (Clickable obj : this.clickables) {
				obj.mouseDragged();
			}
			
			
			
			GLOBAL.sketchChairs.mouseDragged(mouseX, mouseY);

			Vec2D MousePos = this.getPointTranslated(new Vec2D(this.mouseX,this.mouseY));

			GLOBAL.environments.mouseDragged(MousePos.x, MousePos.y);
		}

		if (GLOBAL.gui.overComponent())
			GLOBAL.planesWidget.mouseDragged(mouseX, mouseY);

	}

	void mousePressed() {


		//see if this is a fast press
		this.mousePressedTime = GLOBAL.applet.millis();

		if (!GLOBAL.gui.overComponent() && !SETTINGS.EXPERT_MODE
				&& GLOBAL.uiTools.currentView == GLOBAL.uiTools.VIEW_SHAPE_PACK)
			this.toggleView(null);
		

		//unselect auto rotate
		if (GLOBAL.autoRotate && !GLOBAL.gui.overComponent()) {
			GLOBAL.autoRotate = false;
			GLOBAL.rotateModelsY = 0;

		}

		
		//NOT OVER GUI 
		//IF a press starts over GUI and drags outside it's included as still over GUI
		if (!GLOBAL.gui.overComponent() && !GLOBAL.gui.hasFocus() && !GLOBAL.gui.clickStartedOn()&& GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT) {
			GLOBAL.undo.addChair(GLOBAL.sketchChairs.getCurChair().copy());

			for (Clickable obj : this.clickables) {
				obj.mouseClicked();
			}

			Vec2D MousePosPerson = this.getPointTranslated(new Vec2D(
					this.mouseX, this.mouseY));
			GLOBAL.person.mouseClicked(MousePosPerson.x, MousePosPerson.y);

			if (this.getCurrentTool() == UITools.SCALE_TOOL
					&& GLOBAL.sketchChairs.getCurChair() != null) {
				GLOBAL.sketchChairs.getCurChair().mousePressed(
						MousePosPerson.x, MousePosPerson.y);
			}

			if (this.getCurrentTool() == UITools.MOVE_2D_TOOL
					&& GLOBAL.sketchChairs.getCurChair() != null) {
				GLOBAL.sketchChairs.getCurChair().mousePressed(
						MousePosPerson.x, MousePosPerson.y);
			}

			if (this.getCurrentTool() == UITools.SELECT_TOOL
					|| this.SketchTools.getCurrentTool() == SketchTools.SELECT_BEZIER_TOOL
					&& !GLOBAL.person.clickedOnPerson) {
				//getVecOver();
				if (GLOBAL.sketchChairs.getCurChair() != null) {
					GLOBAL.sketchChairs.getCurChair().mousePressed(
							MousePosPerson.x, MousePosPerson.y);
				}
			}

			if (this.getCurrentTool() == UITools.MOVE_OBJECT
					|| GLOBAL.person.clickedOnPerson) {
				Vec2D mousePos = this.getPointTranslated(new Vec2D(this.mouseX,
						this.mouseY));
				GLOBAL.jBullet.mouseDown(mousePos.x, mousePos.y);
			}

			if (this.SketchTools.getCurrentTool() == SketchTools.SELECT_TOOL
					&& GLOBAL.sketchChairs.getCurChair() != null
					&& GLOBAL.sketchChairs.getCurChair().countSelectedNodes() <= 0) {
				Vec2D mousePos = this.getPointTranslated(new Vec2D(this.mouseX,
						this.mouseY));

				if (GLOBAL.jBullet.physics_on)
					GLOBAL.jBullet.mouseDown(mousePos.x, mousePos.y);
			}

			if (this.getCurrentTool() == UITools.SELECT_TOOL
					&& GLOBAL.sketchChairs.getCurChair() == null) {
				Vec2D mousePos = this.getPointTranslated(new Vec2D(this.mouseX,
						this.mouseY));
				GLOBAL.jBullet.mouseDown(mousePos.x, mousePos.y);
			}

			if (this.getCurrentTool() == UITools.ADD_DOLL_TOOL) {
				/*new SittingDoll(GLOBAL.jBullet.myWorld, new Vector3f(
						GLOBAL.applet.mouseX, GLOBAL.applet.mouseY, 0), 1);
				selectedVec = null;
				selectedVecPlane = null;
				*/
			}

			Vec2D MousePos = this.getPointTranslated(new Vec2D(this.mouseX,
					this.mouseY));

			if (!GLOBAL.sketchChairs.getCurChair().mouseOver(MousePos.x,
					MousePos.y))
				GLOBAL.environments.mousePressed(MousePos.x, MousePos.y);

			GLOBAL.sketchChairs.mousePressed(this.mouseX, this.mouseY);

			//	if(	this.currentView == UITools.VIEW_SHAPE_PACK && this.mouseButton  == this.MOUSE_LEFT)
			//		this.changeView(null);

		}

		if (GLOBAL.gui.overComponent())
			GLOBAL.planesWidget.mouseClicked(mouseX, mouseY);
		
		
		//GLOBAL.undo.setMouseUpChair(GLOBAL.sketchChairs.getCurChair().copy());
		//if(GLOBAL.undo.getMouseUpChair() != null)GLOBAL.undo.getMouseUpChair().build();
		

	}

	void mouseReleased() {
		
		
		if(GLOBAL.uiTools.currentView != UITools.VIEW_CHAIR_EDIT)
			return;
		
		//
		this.prevMouseWasMove = false;
		
		
		if(!waitingForSingleClick && GLOBAL.applet.millis() - this.mousePressedTime < SETTINGS.MOUSE_PRESSED_MIN_TIME)
		this.waitingForSingleClick = true;
		else
			waitingForSingleClick = false;
		
		this.mouseSingleClickTime = GLOBAL.applet.millis();
		/*
		//see if this is a fast press
		if (GLOBAL.applet.millis() - this.mousePressedTime < SETTINGS.MOUSE_CLICKED_MIN_TIME) {

			if (GLOBAL.applet.millis() - this.mouseSingleClickTime < SETTINGS.MOUSE_CLICKED_MIN_TIME*2)
				this.mouseDoubleClick();
			else
				this.mouseSingleClick();

			this.mouseSingleClickTime = GLOBAL.applet.millis();
		*/

		if (GLOBAL.resetting)
			return;

		GLOBAL.person.mouseReleased(mouseX, mouseY);

		GLOBAL.measuretTool.mouseReleased(mouseX, mouseY);

		if (this.getCurrentTool() == UITools.ROTATE_CAM_TOOL
				&& !SETTINGS.EXPERT_MODE)
			this.camJumpFront(null);

		if (SETTINGS.EXPERT_MODE && GLOBAL.gui.overComponent())
			GLOBAL.planesWidget.mouseReleased(mouseX, mouseY);

		if (!GLOBAL.gui.overComponent())
			GLOBAL.sketchChairs.mouseReleased(mouseX, mouseY);

		if (!GLOBAL.gui.overComponent())
			GLOBAL.environments.mouseReleased(mouseX, mouseY);

		

		if (!GLOBAL.gui.overComponent()) {
			
			
			
			
			for (Clickable obj : this.clickables) {
			//	obj.mouseReleased();
			}
			
			
		}

		if (selectedVec != null && selectedVecPlane != null) {
			GLOBAL.sketchChairs.getCurChair().selectedPlanes
					.buildCurrentSketch();
			selectedVecPlane.getSketch().buildOutline();
			selectedVecPlane.getSketch().getSketchShapes().sketchOutlines
					.optimize();
			selectedVec = null;
			this.selectedVecPlane.getSketch().getSketchShapes().renderShapes = false;
			this.selectedShape.offset();
		}

		if (GLOBAL.sketchChairs.getCurChair() != null) {
			//	GLOBAL.sketchChairs.curChair.updateCollisionShape();
		}

		if (!SETTINGS.EXPERT_MODE) {
			GLOBAL.rotateModelsX = 0;
			GLOBAL.rotateModelsY = 0;
			//BASIC MODE EXHIBITION MODE
			if (this.SketchTools.getCurrentTool() != SketchTools.LEG_TOOL)
				GLOBAL.sketchChairs.getCurChair().selectedPlanes
						.add(GLOBAL.sketchChairs.getCurChair()
								.getSlicePlanesY());
		}

		//rebuild cross selections widget every click
		if (SETTINGS.EXPERT_MODE)
			GLOBAL.slicesWidget
					.rebuild(GLOBAL.sketchChairs.getCurChair().crossSliceSelections);

		
		GLOBAL.undo.setMouseUpChair(GLOBAL.sketchChairs.getCurChair());


		
	}

	void mouseSingleClick() {


		if(GLOBAL.uiTools.currentView != UITools.VIEW_CHAIR_EDIT)
			return;
		

		//Select planes with the select tool
		if ((!GLOBAL.gui.overComponent() && 
				this.SketchTools.getCurrentTool() == SketchTools.SELECT_TOOL)
				|| GLOBAL.previewWidget.isMouseOver()) {
			
			
			if(GLOBAL.uiTools.isSideView() && !GLOBAL.previewWidget.isMouseOver()){
				GLOBAL.planesWidget.selectAllPlanes(null);
				GLOBAL.gui.reRender();
			return;	
			}
			

			Object obj = PickBuffer.getInstance().getObject(mouseX, mouseY);
			if (obj instanceof SlicePlane) {
				SlicePlane splane = (SlicePlane) obj;

				
				if (splane.getCrossSliceSelection() == null) {
					GLOBAL.planesWidget.planeClickedOn(splane);
				} else {
					if (GLOBAL.designToolbarAdvanced != null)
						GLOBAL.designToolbarAdvanced.selectTab("slices");

					GLOBAL.slicesWidget.selectSlice(splane
							.getCrossSliceSelection());

				}
				
				GLOBAL.previewWidget.rebuildPatternPreview();
			} else {
				if(!GLOBAL.previewWidget.isMouseOver()){
				GLOBAL.slicesWidget.unselectAll();
				GLOBAL.planesWidget.unselectAll();
				}
			}
		}
		
		
		//IF no planes are selected we can select one with the slat tools
		
		if((this.getCurrentTool() == UITools.SLICES_GROUP_SLICES ||
				this.getCurrentTool() == UITools.SLICES_SINGLE_SLAT ||
				this.getCurrentTool() == UITools.SLICES_SINGLE_SLICE ||
				this.getCurrentTool() == UITools.SLICES_SLATSLICE_GROUP) &&
				(GLOBAL.sketchChairs.getCurChair() != null &&
				GLOBAL.sketchChairs.getCurChair().selectedPlanes.size() == 0)
				){
			Object obj = PickBuffer.getInstance().getObject(mouseX, mouseY);
			if (obj instanceof SlicePlane) {
				SlicePlane splane = (SlicePlane) obj;

				
				if (splane.getCrossSliceSelection() == null) {
					GLOBAL.planesWidget.planeClickedOn(splane);
					
				}
			
		}
		}
		
		GLOBAL.undo.setMouseUpChair(GLOBAL.sketchChairs.getCurChair());
		
		//LOGGER.debug("mouseSingleClick()"+GLOBAL.sketchChairs.getCurChair().selectedPlanes.size());
		GLOBAL.gui.reRender();

	}

	void mouseDoubleClick() {
		
		if (!GLOBAL.gui.overComponent())
			GLOBAL.sketchChairs.mouseDoubleClick(mouseX, mouseY);
		
		GLOBAL.undo.setMouseUpChair(GLOBAL.sketchChairs.getCurChair());

	}

	public void newChair(GUIEvent e) {
		this.hybernateChairToFileAuto(e);
	}

	public void openChairFromFile(GUIEvent e) {
	
			GLOBAL.applet.selectInput("select design file.", "openChairFromFileCallback", null, this);
		      	
	}
	
	
	public void openChairFromFileCallback(File file) {

		 GLOBAL.loadChairLocation = file.getAbsolutePath();

	}
	

	public void openChairFromFileAuto(GUIEvent e) {
		// in response to a button click:

		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.killAll();

		String computername = functions.getComputerName();
		String currentDir = new File(".").getAbsolutePath();
		currentDir = currentDir.substring(0, currentDir.length() - 1);

		int id = SETTINGS.chairSaveNum;
		boolean nameFound = false;
		String location = null;

		while (!nameFound && id >= 0) {
			location = currentDir + "savedChairs\\" + computername + "_chair-"
					+ id + ".cha";
			File f = new File(location);

			if (f.exists()) {
				nameFound = true;

			} else {
				id--;
			}

		}

		LOGGER.info("Loading chair from: " + location);

		SETTINGS.chairSaveNum = (id - 1);
		if (location != null)
			load(location);
		else
			SETTINGS.chairSaveNum = 2000;
	}

	public void openEnvironmentFromFile(GUIEvent e) {
		GLOBAL.environments.openEnvironmentFromFile(e);
	}

	public void pasteChair() {
		LOGGER.info("paste");

		if (GLOBAL.copyOfChair != null) {
			GLOBAL.sketchChairs.killAll();
			GLOBAL.sketchChairs.add(GLOBAL.copyOfChair);
			GLOBAL.sketchChairs.getCurChair().build();
		}

	}

	public void physicsPause(GUIEvent e) {
		GLOBAL.jBullet.physics_on = !GLOBAL.jBullet.physics_on;
	}

	public void physicsPlay(GUIEvent e) {
		GLOBAL.jBullet.physics_on = !GLOBAL.jBullet.physics_on;
	}
	
	public void physicsPlayPause(GUIEvent e) {

		if (!GLOBAL.jBullet.physics_on) {
			GLOBAL.jBullet.physics_on = true;
		} else {
			GLOBAL.jBullet.physics_on = false;
		
		}
		}

	public void physicsRewind(GUIEvent e) {
		GLOBAL.jBullet.resetCollisons();
		GLOBAL.jBullet.physics_on = false;
		GLOBAL.person.resetPhysics();
		
		if(GLOBAL.sketchChairs.getCurChair() != null)
		GLOBAL.sketchChairs.getCurChair().resetPhysics();
	}

	public void print(GUIEvent e) {
		LOGGER.info("PRINT");

		if (GLOBAL.sketchChairs.getCurChair() != null) {
			//GLOBAL.shapePack.empty();
			//GLOBAL.sketchChairs.getCurChair().addToShapePack();
			//GLOBAL.shapePack.build();

			String currentDir = new File(".").getAbsolutePath();
			currentDir = currentDir.substring(0, currentDir.length() - 1);

			int id = 0;
			boolean nameFound = false;
			String location = "";
			location = currentDir + "print.pdf";

			GLOBAL.pdfSaveLocation = location;
			GLOBAL.savePDF = true;
			GLOBAL.autoOpenPDF = true;
		}
	}

	void printOpen(String fileLoc) {
		// not compatible in JRE 1.5 
		
		 	    Desktop desktop = null;
		 	    // Before more Desktop API is used, first check 
		 	    // whether the API is supported by this particular 
		 	    // virtual machine (VM) on this particular host.
		 	    if (Desktop.isDesktopSupported()) {
		 	        desktop = Desktop.getDesktop();
		 	        File file = new File(fileLoc);
		 	        try {
						desktop.open(file);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		 	    }
		

	}

	public void redo(GUIEvent e) {
		//	GLOBAL.undo.undo();

	}

	

	
	
	public void removeSelectedPlanes(GUIEvent e) {
		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().removeSelectedPlanes();
	}

	void render(PGraphics g) {

		if(GLOBAL.gui.overComponent()){
			ModalGUI.setCursor(GLOBAL.applet,SELECT_TOOL_CURSOR,15,9);
			return;
		}
		
		
		
		//are we rotating? maybe we should set a flag for this instead of testing the buttons?
		if(this.mouseDown && this.mouseButton == UITools.MOUSE_MIDDLE){
			ModalGUI.setCursor(GLOBAL.applet,ROTATE_CAM_TOOL_CURSOR,17,16);
			this.hasCursorControl = true;

			return;
		}
		
		
		if(this.mouseDown && this.mouseButton == UITools.MOUSE_RIGHT){
			ModalGUI.setCursor(GLOBAL.applet,MOVE_CAM_TOOL_CURSOR,16,16);
			this.hasCursorControl = true;

			return;
		}
		
		

		g.fill(255);
		g.ellipseMode(PApplet.CENTER);

		Vec2D physicsMousePos = this.getPointTranslated(new Vec2D(this.mouseX,
				this.mouseY));

		//
		if ((this.SketchTools.getCurrentTool() == SketchTools.SELECT_TOOL 
				|| this.SketchTools.getCurrentTool() == SketchTools.DRAW_TOOL)
				&& GLOBAL.person.mouseOver(physicsMousePos.x, physicsMousePos.y)) {
			ModalGUI.setCursor(GLOBAL.applet,MOVE_OBJECT_CURSOR, 16,16);
			return;
		}

		if (this.SketchTools.getCurrentTool() == SketchTools.SELECT_TOOL) {
			
			
			
			if (GLOBAL.sketchChairs.getCurChair() != null) {

				if (!GLOBAL.sketchChairs.getCurChair().overSelectPoint(mouseX,
						mouseY)
						&& GLOBAL.sketchChairs.getCurChair().mouseOver(
								physicsMousePos.x, physicsMousePos.y)) {
					
					ModalGUI.setCursor(GLOBAL.applet,MOVE_OBJECT_CURSOR, 16,16);

					this.hasCursorControl = true;
					return;

				}

				

			}
			
		}
		
		
		if (this.getCurrentTool() == UITools.MOVE_CAM_TOOL) {
			
			
			ModalGUI.setCursor(GLOBAL.applet,MOVE_CAM_TOOL_CURSOR,16,16);

			
			this.hasCursorControl = true;
			return;
		} else if (this.getCurrentTool() == UITools.ROTATE_CAM_TOOL) {
			ModalGUI.setCursor(GLOBAL.applet,ROTATE_CAM_TOOL_CURSOR,16,16);
			this.hasCursorControl = true;
			return;
		} else if (this.getCurrentTool() == UITools.ZOOM_CAM_TOOL) {
			ModalGUI.setCursor(GLOBAL.applet,ZOOM_CAM_TOOL_CURSOR,15,14);
			this.hasCursorControl = true;
			return;
		} else if (this.getCurrentTool() == UITools.SLICES_GROUP_SLICES ||
				this.getCurrentTool() == UITools.SLICES_SINGLE_SLAT ||
				this.getCurrentTool() == UITools.SLICES_SINGLE_SLICE ||
				this.getCurrentTool() == UITools.SLICES_SLATSLICE_GROUP ||
						this.getCurrentTool() == UITools.SLICES_EDIT) {
			
			
			if(sliceToolMode ==  UITools.SLICE_EDIT_MODE_ADD){
				ModalGUI.setCursor(GLOBAL.applet,CURSOR_ADD_SLICE,5,7);
			this.hasCursorControl = true;
			return;
			}
			
			if(sliceToolMode ==  UITools.SLICE_EDIT_MODE_POS){
				ModalGUI.setCursor(GLOBAL.applet,CURSOR_EDIT_SLICE_POS,5,7);
				this.hasCursorControl = true;
				return;
				}
			
			if(sliceToolMode ==  UITools.SLICE_EDIT_MODE_SPACING){
				ModalGUI.setCursor(GLOBAL.applet,CURSOR_EDIT_SLICE_SPACING,5,7);
				this.hasCursorControl = true;
				return;
				}
			
			
		} 
		//CURSOR_EDIT_SLICE_POS
		
		

		this.hasCursorControl = false;
		this.SketchTools.render(g);
  
		/*
		if (!GLOBAL.gui.overComponent() || this.selectCooldown > 0) {

			if (this.getCurrentTool() == DRAW_TOOL) {

				g.noFill();
				g.stroke(100, 100, 100);
				//LOGGER.info("SET CURSOR D");

				if(this.loadedCursor != DRAW_TOOL){
				GLOBAL.applet.cursor(UITools.DRAW_TOOL_CURSOR, 1,1);
				this.loadedCursor = DRAW_TOOL;
				}
				return;
				} else if (this.getCurrentTool() == LEG_TOOL) {
				
				if(this.loadedCursor != DRAW_TOOL){
					GLOBAL.applet.cursor(UITools.DRAW_TOOL_CURSOR, 1,1);
				this.loadedCursor = DRAW_TOOL;
				}

			} else if (this.getCurrentTool() == SELECT_TOOL) {
				
				if(this.loadedCursor != SELECT_TOOL){
				GLOBAL.applet.cursor(UITools.SELECT_TOOL_CURSOR, 1,1);
				this.loadedCursor = SELECT_TOOL;
				}
				
				//g.image(UITools.SELECT_TOOL_CURSOR, mouseX - 10, mouseY - 8);
			} else if (this.getCurrentTool() == ADD_DOLL_TOOL) {
				if(this.loadedCursor != ADD_DOLL_TOOL){
				GLOBAL.applet.cursor(ADD_DOLL_TOOL_CURSOR, 1,1);
				this.loadedCursor = ADD_DOLL_TOOL;
				}
			} else if (this.getCurrentTool() == UITools.MOVE_CAM_TOOL) {
				if(this.loadedCursor != MOVE_CAM_TOOL){
				GLOBAL.applet.cursor(MOVE_CAM_TOOL_CURSOR, 1,1);
				this.loadedCursor = MOVE_CAM_TOOL;
				}
			} else if (this.getCurrentTool() == UITools.ROTATE_CAM_TOOL) {
				if(this.loadedCursor != ROTATE_CAM_TOOL){
					GLOBAL.applet.cursor(ROTATE_CAM_TOOL_CURSOR, 1,1);
				this.loadedCursor = ROTATE_CAM_TOOL;
				}
			} else if (this.getCurrentTool() == UITools.ZOOM_CAM_TOOL) {
				if(this.loadedCursor != ZOOM_CAM_TOOL){
					GLOBAL.applet.cursor(ZOOM_CAM_TOOL_CURSOR, 1,1);
				this.loadedCursor = ZOOM_CAM_TOOL;
				}
			} else if (this.getCurrentTool() == UITools.LEG_TOOL) {
				if(this.loadedCursor != LEG_TOOL){
					GLOBAL.applet.cursor(LINE_TOOL_CURSOR, 1,1);
				this.loadedCursor = LEG_TOOL;
				}
			} else if (this.getCurrentTool() == UITools.SCALE_PERSON_TOOL) {
				if(this.loadedCursor != SCALE_PERSON_TOOL){
				GLOBAL.applet.cursor(ADD_DOLL_TOOL_CURSOR, 1,1);
				this.loadedCursor = SCALE_PERSON_TOOL;
				}
			} else if (this.getCurrentTool() == UITools.MOVE_OBJECT) {
				if(this.loadedCursor != MOVE_OBJECT){
					GLOBAL.applet.cursor(ADD_DOLL_TOOL_CURSOR, 1,1);
				this.loadedCursor = MOVE_OBJECT;
				}
			} else if (this.getCurrentTool() == UITools.SCALE_TOOL) {
				if(this.loadedCursor != SCALE_TOOL){
					GLOBAL.applet.cursor(SCALE_TOOL_CURSOR, 1,1);
				this.loadedCursor = SCALE_TOOL;
				}
			} else if (this.getCurrentTool() == UITools.MEASURE_TOOL) {
				if(this.loadedCursor != MEASURE_TOOL){
					GLOBAL.applet.cursor(MEASURE_TOOL_CURSOR, 1,1);
				this.loadedCursor = MEASURE_TOOL;
				}
			} else if (this.getCurrentTool() == UITools.MOVE_2D_TOOL) {
				if(this.loadedCursor != MOVE_2D_TOOL){
					GLOBAL.applet.cursor(MOVE_CAM_TOOL_CURSOR, 1,1);
				this.loadedCursor = MOVE_2D_TOOL;
				}
			} else {
				if(this.loadedCursor != SELECT_TOOL){
					GLOBAL.applet.cursor(UITools.SELECT_TOOL_CURSOR, 1,1);
					this.loadedCursor = SELECT_TOOL;
				}

				//
				// g.fill(255,0,0);
				// g.ellipse(UI.mouseX , UI.mouseY ,2,2);
			}
		} else {


			if(this.loadedCursor != SELECT_TOOL){
			//	GLOBAL.applet.cursor(UITools.SELECT_TOOL_CURSOR, 1,1);
			//	this.loadedCursor = SELECT_TOOL;
			}
			
		}
*/
		// g.image(this.pickBuffer,0,0);
	}

	void renderGrid(PGraphics g) {

		if (this.useGrid && this.gridWidth * GLOBAL.getZOOM() > 2) {

			g.stroke(SETTINGS.GRID_MAJOR_LINE);
			g.strokeWeight(SETTINGS.GRID_MAJOR_LINE_WEIGHT);
			//float offset_x =  GLOBAL.CAM_OFFSET_X % (this.gridWidth);

			//calculate the number of grid squares between the centre of the screen and the edge to  see where our grid should start
			double offset_x = (GLOBAL.windowWidth / 2)
					- (Math.floor((GLOBAL.windowWidth / 2)
							/ (this.gridWidth * GLOBAL.getZOOM()))
							* this.gridWidth * GLOBAL.getZOOM());
			offset_x = (((GLOBAL.CAM_OFFSET_X * GLOBAL.getZOOM()) + offset_x) % (this.gridWidth * GLOBAL
					.getZOOM())) - this.gridWidth * GLOBAL.getZOOM();

			for (float x = (float) offset_x; x < GLOBAL.windowWidth; x += this.gridWidth
					* GLOBAL.getZOOM()) {

				g.stroke(SETTINGS.GRID_MAJOR_LINE);
				g.strokeWeight(SETTINGS.GRID_MAJOR_LINE_WEIGHT);

				g.line(x, 0, x, GLOBAL.windowHeight);

				if (this.gridWidth * GLOBAL.getZOOM() > minorGridnumber * 5) {
					g.stroke(SETTINGS.GRID_MINOR_LINE);
					g.strokeWeight(SETTINGS.GRID_MINOR_LINE_WEIGHT);

					for (float x2 = (float) (x + (this.gridWidth * GLOBAL.getZOOM())
							/ minorGridnumber); x2 < x + this.gridWidth
							* GLOBAL.getZOOM(); x2 += (this.gridWidth * GLOBAL
							.getZOOM()) / minorGridnumber) {
						g.line(x2, 0, x2, GLOBAL.windowHeight);
					}
				}

			}

			double offset_y = (GLOBAL.windowHeight / 2)
					- (Math.floor((GLOBAL.windowHeight / 2)
							/ (this.gridHeight * GLOBAL.getZOOM()))
							* this.gridHeight * GLOBAL.getZOOM());
			offset_y = (((GLOBAL.CAM_OFFSET_Y * GLOBAL.getZOOM()) + offset_y) % (this.gridHeight * GLOBAL
					.getZOOM())) - this.gridHeight * GLOBAL.getZOOM(); // Y offset

			for (float y = (float) offset_y; y < GLOBAL.windowHeight; y += this.gridHeight
					* GLOBAL.getZOOM()) {
				g.stroke(SETTINGS.GRID_MAJOR_LINE);
				g.strokeWeight(SETTINGS.GRID_MAJOR_LINE_WEIGHT);

				g.line(0, y, GLOBAL.windowWidth, y);

				if (this.gridWidth * GLOBAL.getZOOM() > minorGridnumber * 5) {
					g.stroke(SETTINGS.GRID_MINOR_LINE);
					g.strokeWeight(SETTINGS.GRID_MINOR_LINE_WEIGHT);

					for (float y2 = (float) (y + (this.gridHeight * GLOBAL.getZOOM())
							/ minorGridnumber); y2 < y + this.gridWidth
							* GLOBAL.getZOOM(); y2 += (this.gridHeight * GLOBAL
							.getZOOM()) / minorGridnumber) {
						g.line(0, y2, GLOBAL.windowWidth, y2);
					}
				}

			}

		}
	}

	public void reset(GUIEvent e) {

		GLOBAL.forceResize = true;

		GLOBAL.setZOOM(.5f);
		GLOBAL.CAM_OFFSET_X = -400;
		GLOBAL.CAM_OFFSET_Y = (float) -((700 - GLOBAL.windowHeight) + 900);
		GLOBAL.autoRotate = false;
		//GLOBAL.forceResize = true;
		/*
		GLOBAL.resetting = true; 
		
		if(!SETTINGS.APPLET_MODE){
		this.saveChairToFileAuto(null);
		this.captureScreen(null);
		GLOBAL.applet.draw();
		}
		*/
		//GLOBAL.gui.reset();
		//	GLOBAL.applet.removeAll();
		//GLOBAL.person.resetPhysics();
		//GLOBAL.forceReset = true;

		this.currentView = UITools.VIEW_CHAIR_EDIT;
		SETTINGS.EXPERT_MODE = false;
		GLOBAL.displayIntroPanel = true;
		GLOBAL.gui.reload();
		GLOBAL.gui.reset();

		if (SETTINGS.EXPERT_MODE)
			UI.setupGUITabsExpert(GLOBAL.applet, GLOBAL.gui);

		if (!SETTINGS.EXPERT_MODE)
			UI.setupGUITabsBasic(GLOBAL.applet, GLOBAL.gui);

		GLOBAL.sketchChairs.killAll();
		GLOBAL.jBullet = new jBullet();
		GLOBAL.person = new ergoDoll(GLOBAL.jBullet.myWorld, new Vector3f(-80,
				-10, 0), 1f);
		this.physicsRewind(null);
		//GLOBAL.applet.setup();	
		//GLOBAL.resetting = false;
		SETTINGS_SKETCH.plane_thickness = SETTINGS_SKETCH.plane_thickness_default;

	}

	public void saveChairToFile(GUIEvent e) {
		GLOBAL.saveChairToFile = true;
	}

	public void saveChairToFile() {
		if (SETTINGS.APPLET_MODE) {

			try {
				FileSaveService save = (FileSaveService) ServiceManager
						.lookup("javax.jnlp.FileSaveService");

				Element root = new Element("SketchChairDoc");
				root.addAttribute(new Attribute("version", String
						.valueOf(SETTINGS.version)));
				root.appendChild(GLOBAL.sketchChairs.getCurChair().toXML());

				Document doc = new Document(root);
				//   outXML = ;// new FileOutputStream(name+".xml");

				ByteArrayOutputStream outXML = new ByteArrayOutputStream();
				Serializer serializer = new Serializer(outXML, "ISO-8859-1");
				serializer.write(doc);

				FileContents fc = save.saveFileDialog(null,
						new String[] { "chair" }, new ByteArrayInputStream(
								outXML.toByteArray()), null);

			} catch (Exception ex) {
				LOGGER.error("exception: " + ex);
			}

		} else {
			//Create a file chooser
			GLOBAL.applet.selectOutput("select design file.", "saveChairToFileCallback", null, this);
		}
			 
			 

	}
	
	
	public void savePattern(GUIEvent e) {
		this.savePatternToFile();
	}
	
	public void savePatternToFile() {
		if (SETTINGS.APPLET_MODE) {

			try {
				FileSaveService save = (FileSaveService) ServiceManager
						.lookup("javax.jnlp.FileSaveService");

				Element root = new Element("SketchChairDoc");
				root.addAttribute(new Attribute("version", String
						.valueOf(SETTINGS.version)));
				root.appendChild(GLOBAL.sketchChairs.getCurChair().toXML());

				Document doc = new Document(root);
				//   outXML = ;// new FileOutputStream(name+".xml");

				ByteArrayOutputStream outXML = new ByteArrayOutputStream();
				Serializer serializer = new Serializer(outXML, "ISO-8859-1");
				serializer.write(doc);

				FileContents fc = save.saveFileDialog(null,
						new String[] { "chair" }, new ByteArrayInputStream(
								outXML.toByteArray()), null);

			} catch (Exception ex) {
				LOGGER.error("exception: " + ex);
			}

		} else {
			//Create a file chooser

			GLOBAL.applet.selectOutput("select design file.", "savePatternToFileCallback", null, this);
		}
			 
			 

	}
	
	
	
	
	public void saveChairToFileCallback(File file) {
		GLOBAL.saveChairLocation = file.getAbsolutePath();
		
		if(!GLOBAL.saveChairLocation.endsWith(".skchr"))
			GLOBAL.saveChairLocation += ".skchr";
	}
	
	public void savePatternToFileCallback(File file) {
		GLOBAL.savePatternLocation = file.getAbsolutePath();
		
		if(!GLOBAL.savePatternLocation.endsWith(".svg"))
			GLOBAL.savePatternLocation += ".svg";
		
	}
	
	
	public void saveChairToFileAuto(GUIEvent e) {
		GLOBAL.saveChairToFileAuto = true;
		
		
		
	}

	public void saveChairToFileAuto() {

		if (GLOBAL.sketchChairs.getCurChair() != null) {
			GLOBAL.sketchChairs.getCurChair().addToShapePack();

			String location = "";
			int id = 0;

			if (GLOBAL.sketchChairs.getCurChair().localSavelocation != null) {
				location = GLOBAL.sketchChairs.getCurChair().localSavelocation;
			} else {

				String computername = functions.getComputerName();
				String currentDir = new File(".").getAbsolutePath();
				currentDir = currentDir.substring(0, currentDir.length() - 1);

				boolean nameFound = false;

				while (!nameFound && id < 10000) {
					location = currentDir + "savedChairs\\chair-" + id + ".xml";
					GLOBAL.sketchChairs.getCurChair().localSavelocation = location;
					File f = new File(location);

					if (!f.exists())
						nameFound = true;

					id++;
				}
			}

			save(location);
			//System.out.println("saving to " + location);
			SETTINGS.chairSaveNum = id;
			GLOBAL.LAST_SAVED_LOCATION = location;
		}
	}

	public void saveCraftRobo(GUIEvent e) {

		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().addToShapePack();

		//	GLOBAL.shapePack.build();
		GLOBAL.shapePack.printToCraftRobo();

	}

	public void saveModelDxf(GUIEvent e) {

		GLOBAL.dxfCapture = true;

		if (SETTINGS.APPLET_MODE) {

			FileSaveService save;
			try {
				save = (FileSaveService) ServiceManager
						.lookup("javax.jnlp.FileSaveService");
				GLOBAL.shapePack.build();
				FileContents fc = save.saveFileDialog(null,
						new String[] { "pdf" }, new ByteArrayInputStream(
								GLOBAL.shapePack.getPDFBuffered(GLOBAL.applet)
										.toByteArray()), null);

			} catch (UnavailableServiceException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} else {
			GLOBAL.applet.selectOutput("select design file.", "saveModelDxfCallback", null, this);

		}
	}
	
	public void saveModelDxfCallback(File file) {
		GLOBAL.dxfLocation = file.getAbsolutePath();
		GLOBAL.dxfCapture = true;
	}

	public void savePatternDXF(GUIEvent e) {

		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().addToShapePack();

		if (SETTINGS.APPLET_MODE) {

			FileSaveService save;
			try {
				save = (FileSaveService) ServiceManager
						.lookup("javax.jnlp.FileSaveService");
				GLOBAL.shapePack.build();
				FileContents fc = save.saveFileDialog(null,
						new String[] { "dxf" }, new ByteArrayInputStream(
								GLOBAL.shapePack.getDXFBuffered(GLOBAL.applet)
										.toByteArray()), null);

			} catch (UnavailableServiceException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} else {
			//GLOBAL.shapePack.printToCraftRobo();

			GLOBAL.applet.selectOutput("select design file.", "savePatternDXFCallback", null, this);


		}

	}

	public void savePatternDXFCallback(File file) {

	 GLOBAL.dxfSaveLocation = file.getAbsolutePath();
		GLOBAL.saveDXF = true;
		
		if(!GLOBAL.dxfSaveLocation.endsWith(".dxf"))
			GLOBAL.dxfSaveLocation += ".dxf";
		
		
	}
	public void savePDF(GUIEvent e) {

		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().addToShapePack();

		if (SETTINGS.APPLET_MODE) {

			FileSaveService save;
			try {
				save = (FileSaveService) ServiceManager
						.lookup("javax.jnlp.FileSaveService");
				GLOBAL.shapePack.build();
				FileContents fc = save.saveFileDialog(null,
						new String[] { "pdf" }, new ByteArrayInputStream(
								GLOBAL.shapePack.getPDFBuffered(GLOBAL.applet)
										.toByteArray()), null);

			} catch (UnavailableServiceException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} else {
			//GLOBAL.shapePack.printToCraftRobo();

			//Create a file chooser
			GLOBAL.applet.selectOutput("select design file.", "savePDFCallback", null, this);


		}

	}
	
	public void savePDFCallback(File file) {

		 String filename = file.getAbsolutePath();
		if (!filename.endsWith("pdf"))
			filename += ".pdf";
		
		GLOBAL.pdfSaveLocation = filename;
		GLOBAL.savePDF = true;
		
	}

	public void exportPreview(GUIEvent e) {

		if (SETTINGS.APPLET_MODE) {

		} else {

			//Create a file chooser
			GLOBAL.applet.selectOutput("select design file.", "exportPreviewCallback", null, this);



		}

	}
	
	public void exportPreviewCallback(File file) {

		String filename = file.getAbsolutePath();
		
	if (!filename.endsWith("png"))
		filename += ".png";
	
	GLOBAL.pngPreviewSaveLocation =  filename;
	GLOBAL.exportPreviewPNG = true;
	}
	public void savePDFauto(GUIEvent e) {

		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().addToShapePack();

		String computername = functions.getComputerName();
		String currentDir = new File(".").getAbsolutePath();
		currentDir = currentDir.substring(0, currentDir.length() - 1);

		int id = 0;
		boolean nameFound = false;
		String location = "";

		String dropBoxDir = SETTINGS.autoSaveMakeLocation;
		while (!nameFound && id < 10000) {
			location = dropBoxDir + "/" + "_exhibitionChair-" + id + ".pdf";
			File f = new File(location);

			if (!f.exists())
				nameFound = true;

			id++;
		}

		LOGGER.info("Saving PDF to: " + location);
		GLOBAL.pdfSaveLocation = location;
		GLOBAL.savePDF = true;
		SETTINGS.chairSaveNum++;

	}

	public void selectLanguage(GUIEvent e) {

		if (e.val == UITools.LANGUAGE_ENG) {
			SETTINGS.LANGUAGE = "ENG";
			GLOBAL.modeChanged = true;

		}

		if (e.val == UITools.LANGUAGE_JP) {
			SETTINGS.LANGUAGE = "JP";
			GLOBAL.modeChanged = true;

		}

	}

	public void setDisplaypanelAsShown(GUIEvent e) {
		GLOBAL.displayIntroPanel = false;

	}

	public void selectLegPlanes(GUIEvent e) {

		if (GLOBAL.sketchChairs.getCurChair() != null) {
			GLOBAL.sketchChairs.getCurChair().selectedPlanes.clear();
			GLOBAL.sketchChairs.getCurChair().selectedPlanes
					.add(GLOBAL.sketchChairs.getCurChair().getSlicePlanesY()
							.getFirst());
			GLOBAL.sketchChairs.getCurChair().selectedPlanes
					.add(GLOBAL.sketchChairs.getCurChair().getSlicePlanesY()
							.getLast());
		}

	}

	public void selectTool(GUIEvent e) {
		this.selectTool((int) e.val);
		this.selectCooldown = 50;

	}

	public void makeChairWindow(GUIEvent e) {
		float windowWidth = SETTINGS.GUIDE_WINDOW_WIDTH;
		float windowHeight = SETTINGS.GUIDE_WINDOW_HEIGHT;

		GUIWindow window = new GUIWindow(0f, 0f, windowWidth, windowHeight,
				GLOBAL.gui);
		window.setLightboxed(true);
		window.centre();
		GLOBAL.gui.add(window);

		float posX = 100;
		float posY = 100;
		float spacingY = 80;

		GUIImage guiImg;
		guiImg = new GUIImage(0, 50, "gui/WINDOW_TITLE_MAKE.png", GLOBAL.gui);
		window.add(guiImg);

		GUIButton button;
		button = new GUIToggle(400, 50, "gui/WINDOW_MAKE_PREVIEW_PATTERN.png",
				GLOBAL.gui);
		button.addActionListener(GLOBAL.uiTools, "toggleView", UITools.LEG_TOOL);
		button.addActionListener(window, "close", UITools.LEG_TOOL);
		window.add(button);
		posY += spacingY;

		button = new GUIToggle(400, 200, "gui/WINDOW_MAKE_CUT_PATTERN.png",
				GLOBAL.gui);
		button.addActionListener(window, "close", UITools.LEG_TOOL);
		if (SETTINGS.autoSaveMakePattern)
			button.addActionListener(GLOBAL.uiTools, "savePDFauto",
					UITools.LEG_TOOL);
		else
			button.addActionListener(GLOBAL.uiTools, "print", UITools.LEG_TOOL);

		window.add(button);

		posY += spacingY;

	}

	public void selectTool(int toolNum) {
		
		setCurrentTool(toolNum);

		if (this.getCurrentTool() == UITools.SELECT_TOOL
				&& !SETTINGS.EXPERT_MODE) {
			if (GLOBAL.sketchChairs.getCurChair() != null) {
				GLOBAL.sketchChairs.getCurChair().selectedPlanes.clear();
				GLOBAL.sketchChairs.getCurChair().selectedPlanes
						.add(GLOBAL.sketchChairs.getCurChair()
								.getSlicePlanesY());
			}

		}

		//GLOBAL.forceResize = true;
		// System.out.print(toolNum);
	}

	public void setBrushDia(float val) {

		GLOBAL.uiTools.SketchTools.brush_dia = val;

		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().setBrushDia(val);

	}

	public void setChairColour(GUIEvent e) {
		if (GLOBAL.sketchChairs.getCurChair() != null) {
			GLOBAL.sketchChairs.getCurChair().chairColour = (int) e.val;
		}

	}

	/**
	 * @param currentTool the currentTool to set
	 */
	public void setCurrentTool(int currentTool) {
		this.SketchTools.selectTool(SketchTools.NONE);
		this.currentTool = currentTool;

			
	}

	/**
	 * @param editing the editing to set
	 */
	public void setEditing(int editing) {
		this.editing = editing;
	}

	/**
	 * @param pickBuffer the pickBuffer to set
	 */
	public void setPickBuffer(PGraphics pickBuffer) {
		this.pickBuffer = pickBuffer;
	}

	public void showBugsWebpage(GUIEvent e) {

		String url = "http://www.sketchchair.cc/forum/index.php?board=8.0";
		if (java.awt.Desktop.isDesktopSupported()) {
			try {
				// not compatible in JRE 1.5
				java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}

	}

	public void showForumWebpage(GUIEvent e) {

		String url = "http://www.sketchchair.cc/forum/";
		if (java.awt.Desktop.isDesktopSupported()) {
			try {
				// not compatible in JRE 1.5
				java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}

	}

	public void showInstructionMovie(GUIEvent e) {
		//GLOBAL.tittleImageNum = 0;
		//GLOBAL.myMovie = new FasterMovie(GLOBAL.applet,"./introVideos/"+GLOBAL.tittleImageNum+".mov");
		//GLOBAL.myMovie.loop();	
	}

	public void showInstructions(GUIEvent e) {
		GLOBAL.tittleImageNum = 0;
		GLOBAL.tittleImg = GLOBAL.applet.loadImage("./tittleImage/"
				+ GLOBAL.tittleImageNum + SETTINGS.LANGUAGE + ".jpg");

	}

	public void showReferenceWebpage(GUIEvent e) {

		String url = "http://www.sketchchair.cc/development/description-of-features/";
		if (java.awt.Desktop.isDesktopSupported()) {
			try {
				// not compatible in JRE 1.5
				java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}

	}

	


	
	
	public void sitStand(GUIEvent e) {

		if (!GLOBAL.jBullet.physics_on) {
			GLOBAL.jBullet.physics_on = true;
		} else {
			this.physicsRewind(null);
			GLOBAL.jBullet.physics_on = false;
		}
	}

	public void toggleCentreConstraintChair(GUIEvent e) {
		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.sketchChairs.getCurChair().toggleCentreConstraint();

	}

	
	public void figureGrow(GUIEvent e) {
	GLOBAL.person.bigger();
	}
	
	
	public void figureShrink(GUIEvent e) {
	GLOBAL.person.smaller();

		
	}
	public void toggleExpert(GUIEvent e) {

		SETTINGS.EXPERT_MODE = !SETTINGS.EXPERT_MODE;
		
		if(this.currentView == UITools.VIEW_CHAIR_EDIT) {

		if (SETTINGS.EXPERT_MODE) {
			UI.setupGUITabsExpert(GLOBAL.applet, GLOBAL.gui);
		} else {
			UI.setupGUITabsBasic(GLOBAL.applet, GLOBAL.gui);
		}
				}

		
		if (GLOBAL.slicesWidget != null
				&& GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.slicesWidget
					.rebuild(GLOBAL.sketchChairs.getCurChair().crossSliceSelections);

		GLOBAL.forceResize = true;
	}

	public void togglePerformance(GUIEvent e) {
		GLOBAL.performanceMode = !GLOBAL.performanceMode;

		GLOBAL.gui.performanceMode = GLOBAL.performanceMode;

		if (GLOBAL.gui.performanceMode) {
			SETTINGS.cylinder_res = 4;
			SETTINGS.sphere_res = 9;
		} else {

			SETTINGS.cylinder_res = 12;
			SETTINGS.sphere_res = 7;

		}

	}

	public void togglePerson(GUIEvent e) {

		GLOBAL.person.toggleON();
	}

	public void toggleReferenceGeom(GUIEvent e) {
		GLOBAL.environments.render = !GLOBAL.environments.render;

	}

	public void importSVG(GUIEvent e) {

		
		GLOBAL.applet.selectInput("select design file.", "importSVGCallback", null, this);
		

	}
	public void importSVGCallback(File file) {
		 GLOBAL.importSVGLocation = file.getAbsolutePath();

	}
	
	public void applyMaterialSettings(GUIEvent e) {
	if(GLOBAL.sketchChairs.getCurChair() != null)
		GLOBAL.sketchChairs.getCurChair().addToShapePack();
	
	//LOGGER.info("applyMaterialSettings");
	}
	
	public void toggleView(GUIEvent e) {

		System.gc();//clean up before we hit the memory
		
		if (this.currentView == UITools.VIEW_CHAIR_EDIT)
			this.currentView = UITools.VIEW_SHAPE_PACK;
		else
			this.currentView = UITools.VIEW_CHAIR_EDIT;

		if (this.currentView == UITools.VIEW_SHAPE_PACK) {
			UI.setupGUIPattern(GLOBAL.applet, GLOBAL.gui);
			GLOBAL.sketchChairs.getCurChair().getSlicePlanesY().update();
			GLOBAL.sketchChairs.getCurChair().addToShapePack();
			
		
			
		}else{
			
			if(SETTINGS.EXPERT_MODE)
			UI.setupGUITabsExpert(GLOBAL.applet, GLOBAL.gui);
			else
			UI.setupGUITabsBasic(GLOBAL.applet, GLOBAL.gui);

			if(GLOBAL.designToolbarPattern != null)
			GLOBAL.designToolbarPattern.hide();

			
			if(GLOBAL.designToolbarBasic != null)
				GLOBAL.designToolbarBasic.show();
			
			if(GLOBAL.designToolbarAdvanced != null)
				GLOBAL.designToolbarAdvanced.show();
			
			
		}

	}
	
	public void viewPattern(GUIEvent e) {

		GLOBAL.previewWidget.makeItBtn.hide();
		GLOBAL.previewWidget.designItBtn.hide();
		
		GLOBAL.patternCameraPanel.show();
		GLOBAL.cameraPanel.hide();
		
		this.currentView = UITools.VIEW_SHAPE_PACK;

		UI.setupGUIPattern(GLOBAL.applet, GLOBAL.gui);
		GLOBAL.sketchChairs.getCurChair().getSlicePlanesY().update();
		GLOBAL.sketchChairs.getCurChair().addToShapePack();
		
	}
	
	
	public void viewModel(GUIEvent e) {

		GLOBAL.previewWidget.makeItBtn.show();
		GLOBAL.previewWidget.designItBtn.hide();
		
		GLOBAL.patternCameraPanel.hide();
		GLOBAL.cameraPanel.show();
		
		
		this.currentView = UITools.VIEW_CHAIR_EDIT;

		if(SETTINGS.EXPERT_MODE)
			UI.setupGUITabsExpert(GLOBAL.applet, GLOBAL.gui);
			else
			UI.setupGUITabsBasic(GLOBAL.applet, GLOBAL.gui);
}
	
	

	public void undo(GUIEvent e) {
		System.out.print("UNDO");

		GLOBAL.undo.undoChair(GLOBAL.sketchChairs.getCurChair());

	}

	public void ShareChairOnline(GUIEvent e) {
		SketchChairCloudhook.getInstance().ShareChairOnline(null);
	}

	void update() {
		
		updateVecMove();

		if(GLOBAL.autoRotate){
			GLOBAL.slicesWidget.unselectAll();
			GLOBAL.planesWidget.unselectAll();
		}
		
		if (this.SketchTools.getCurrentTool() != SketchTools.NONE)
			this.currentTool = NONE;

		//if(this.waitingForSingleClick)
		//	LOGGER.info("waitingForSingleClick"+(GLOBAL.applet.millis() - this.mousePressedTime));
		
		
		if((GLOBAL.applet.millis() - this.mousePressedTime) > SETTINGS.MOUSE_CLICKED_MIN_TIME )
		this.waitingForSingleClick = false;
		
		if (this.waitingForSingleClick && 
				 (GLOBAL.applet.millis() - this.mousePressedTime) < SETTINGS.MOUSE_CLICKED_MIN_TIME &&
				(mouseX == pmouseX && mouseY == pmouseY)
				
				){
			this.mouseSingleClick();
			waitingForSingleClick = false;
		}


		this.selectCooldown--;
	}

	public void updateMouse(int mouseX2, int mouseY2, int pmouseX2,
			int pmouseY2, boolean mouseDown2, int mouseButton2) {
		GLOBAL.uiTools.pmouseX = GLOBAL.uiTools.mouseX;
		GLOBAL.uiTools.pmouseY = GLOBAL.uiTools.mouseY;

		GLOBAL.uiTools.mouseX = mouseX2;
		GLOBAL.uiTools.mouseY = mouseY2;

		GLOBAL.uiTools.mouseDown = mouseDown2;
		GLOBAL.uiTools.mouseButton = mouseButton2;

		if (this.useGrid ) {

			float m_gridW = this.gridWidth;
			float m_gridH = this.gridHeight;

			if (this.gridWidth * GLOBAL.getZOOM() > minorGridnumber * 5) {
				m_gridW /= minorGridnumber;
				m_gridH /= minorGridnumber;
			}

			//calculate the number of grid squares between the centre of the screen and the edge to  see where our grid should start
			float offset_x = (float) (Math.round((((float)mouseX2 - ((float)GLOBAL.windowWidth / 2.0f)) / (m_gridW * GLOBAL.getZOOM()))) * (m_gridW * GLOBAL.getZOOM()));
			offset_x +=(float)(GLOBAL.windowWidth / 2)+ ((GLOBAL.CAM_OFFSET_X * GLOBAL.getZOOM()) % (m_gridW * GLOBAL.getZOOM()));
			
			
			float offset_y = (float) (Math.round((((float)mouseY2 - ((float)GLOBAL.windowHeight / 2.0f)) / (m_gridH * GLOBAL.getZOOM()))) * (m_gridH * GLOBAL.getZOOM()));
			offset_y += (float)(GLOBAL.windowHeight / 2)+ ((GLOBAL.CAM_OFFSET_Y * GLOBAL.getZOOM()) % (m_gridH * GLOBAL.getZOOM()));

			int newMouseX = (int) offset_x;
			int newMouseY = (int) offset_y;
			GLOBAL.uiTools.mouseX = newMouseX;
			GLOBAL.uiTools.mouseY = newMouseY;

		}
	}

	public void updateMouseWorld() {

		GLOBAL.uiTools.pmouseXworld = GLOBAL.uiTools.mouseXworld;
		GLOBAL.uiTools.pmouseYworld = GLOBAL.uiTools.mouseYworld;

		Vec2D MouseWorld = this.getPointTranslated(new Vec2D(this.mouseX,
				this.mouseY));

		GLOBAL.uiTools.mouseXworld = MouseWorld.x;
		GLOBAL.uiTools.mouseYworld = MouseWorld.y;

	}

	void updateVecMove() {
		/*
			if (selectedVec != null && selectedVecPlane != null) {
				// System.out.println(selectedVec.x);
				if (UI.mouseDown) {
					//Vec2D planePoint = getPointOnPlane(new Vec2D(UI.mouseX,
							UI.mouseY), this.selectedVecPlane, GLOBAL.g);
					// selectedVec.x += (UI.mouseX - UI.pmouseX) / GLOBAL.ZOOM;
					// selectedVec.y += (UI.mouseY - UI.pmouseY) / GLOBAL.ZOOM;

					if (planePoint != null) {
						// selectedVec.x = planePoint.x;
						// / selectedVec.y = planePoint.y;

						if (this.selectedShape != null) {
							this.selectedShape.movePoint(selectedVec, planePoint);

						}
					}
					// 
				}
			}
			*/
	}

	
	boolean isSideView(){
		return(GLOBAL.rotateModelsX == 0 && GLOBAL.rotateModelsY == 0);
	}
	
	
	void zoomView(float _zoomDelta){
		
		
		float deltaMouseXBefore = (float) (((GLOBAL.applet.width/2)-UI.mouseX)/GLOBAL.ZOOM);
		float deltaMouseYBefore = (float) (((GLOBAL.applet.height/2)-UI.mouseY)/GLOBAL.ZOOM);
		
		
		GLOBAL.ZOOM -= _zoomDelta;

		
		if( (_zoomDelta > 0 && GLOBAL.ZOOM < SETTINGS.MIN_ZOOM)){
			GLOBAL.ZOOM = SETTINGS.MIN_ZOOM;
		}
				
		if(_zoomDelta < 0 && GLOBAL.ZOOM > SETTINGS.MAX_ZOOM){
		GLOBAL.ZOOM = SETTINGS.MAX_ZOOM;
		}
		
		 float deltaMouseXAfter = (float) (((GLOBAL.applet.width/2)-UI.mouseX)/GLOBAL.ZOOM);
		 float deltaMouseYAfter = (float) (((GLOBAL.applet.height/2)-UI.mouseY)/GLOBAL.ZOOM);
		
		 
		 float deltaMouseX = deltaMouseXAfter - deltaMouseXBefore; 
		 float deltaMouseY = deltaMouseYAfter - deltaMouseYBefore;
		 
		 moveView(-deltaMouseX,-deltaMouseY);

	}
	
	void centreView(float _x, float _y){
		
		
		float deltaMouseX = (GLOBAL.applet.width/2)-_x;
		float deltaMouseY = (GLOBAL.applet.height/2)-_y;
				
		GLOBAL.CAM_OFFSET_X += ((deltaMouseX/GLOBAL.ZOOM));
		GLOBAL.CAM_OFFSET_Y += ((deltaMouseY/GLOBAL.ZOOM));
		
		
		
	}
	
	void moveView(float _deltaX, float _deltaY){
		
		
		 GLOBAL.CAM_OFFSET_X +=  _deltaX;
		 GLOBAL.CAM_OFFSET_Y +=  _deltaY;
	
		 
		 float leftEdge = (float) (GLOBAL.CAM_OFFSET_X - ((GLOBAL.applet.width/2)/GLOBAL.ZOOM));
			if(leftEdge < SETTINGS.MIN_CAM_X_OFFSET)
				GLOBAL.CAM_OFFSET_X = SETTINGS.MIN_CAM_X_OFFSET + ((GLOBAL.applet.width/2)/GLOBAL.ZOOM);
			
			
			 float rightEdge = (float) (GLOBAL.CAM_OFFSET_X + ((GLOBAL.applet.width/2)/GLOBAL.ZOOM));
			if(rightEdge > SETTINGS.MAX_CAM_X_OFFSET)
				GLOBAL.CAM_OFFSET_X = SETTINGS.MAX_CAM_X_OFFSET - ((GLOBAL.applet.width/2)/GLOBAL.ZOOM);
			
			double topEdge = (double) GLOBAL.CAM_OFFSET_Y + ((GLOBAL.applet.height/2)/GLOBAL.ZOOM);
			if(topEdge > SETTINGS.MAX_CAM_Y_OFFSET)
				GLOBAL.CAM_OFFSET_Y = SETTINGS.MAX_CAM_Y_OFFSET - ((GLOBAL.applet.height/2)/GLOBAL.ZOOM);
			
			float bottomEdge = (float) (GLOBAL.CAM_OFFSET_Y - ((GLOBAL.applet.height/2)/GLOBAL.ZOOM));
			if(bottomEdge < SETTINGS.MIN_CAM_Y_OFFSET)
				GLOBAL.CAM_OFFSET_Y = SETTINGS.MIN_CAM_Y_OFFSET + ((GLOBAL.applet.height/2)/GLOBAL.ZOOM);
			
		 
	}
	
}
