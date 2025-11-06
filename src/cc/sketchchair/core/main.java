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

//TODO: Add all buttons to GUI
//TODO: Add GUI panels?
//TODO: Add Ragdoll class
//TODO: Render ragdoll correctly
//TODO: sitting controle for ragdoll




import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.vecmath.Vector3f;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PSurface;
import processing.opengl.PJOGL;
import cc.sketchchair.functions.functions;
import cc.sketchchair.ragdoll.ergoDoll;
import cc.sketchchair.sketch.SketchSpline;
import cc.sketchchair.widgets.WidgetLoad;
import cc.sketchchair.widgets.WidgetMaterials;
import cc.sketchchair.widgets.WidgetPlanes;
/**
 * Main program class. Used to start SketchChair. 
 * @author gregsaul
 *
 */
public class main extends PApplet {


	public static void main(String args[]) {

		//send log to a file
		/*
		File file=new File("debug.log");
			try {
				if(!file.exists()){
					file.createNewFile();
				}
				System.setOut(new PrintStream(new FileOutputStream(file)));
				System.setErr(new PrintStream(new FileOutputStream(file)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		*/

		// Debug: Print JOGL version info
		try {
			Package joglPkg = Package.getPackage("com.jogamp.opengl");
			if (joglPkg != null) {
				System.out.println("=== JOGL Version Info ===");
				System.out.println("Specification Title: " + joglPkg.getSpecificationTitle());
				System.out.println("Specification Version: " + joglPkg.getSpecificationVersion());
				System.out.println("Implementation Title: " + joglPkg.getImplementationTitle());
				System.out.println("Implementation Version: " + joglPkg.getImplementationVersion());
				System.out.println("Implementation Vendor: " + joglPkg.getImplementationVendor());
				System.out.println("========================");
			} else {
				System.out.println("JOGL package not yet loaded");
			}
		} catch (Exception e) {
			System.out.println("Could not get JOGL version: " + e.getMessage());
		}

		PApplet.main(new String[] { main.class.getName() });

	}

	ExceptionHandler exception = new ExceptionHandler();
	private static final long serialVersionUID = 1L;
	UI UI;

	// Static global components
	GLOBAL GLOBAL;
	SETTINGS SETTINGS;
	Localization Localization;

	static LOGGER LOGGER = new LOGGER();
	static String openChair = null;

	SketchSpline curSpline;
	boolean mouseDown = false;

	SketchChair curChair = null;

	float rotateModelsX = 0;
	float rotateModelsY = 0;
	
	int w = 0;
	int h = 0;
	PImage img;

	int framerateLastMillis = 0;
	int framerate = 0;
	PFont font;
	private int autoSaveCounter = 0;
	private boolean useOPENGL;
	//private MovieMaker mm;
	private PImage vidFrame;
	private boolean setupCompleted = false;
	private int sec0;
	private boolean firstLoop = false;
	private boolean startingApp = true; //set this to true until the user ineracts. 
	private boolean useGLGRAPHICS;
	
	PGraphics previewBuffer;
	private boolean generateGUIStencil;
	private boolean debugPickBuffer = false;
	private boolean mouseSingleClick = false;
	private boolean mouseDoubleClick = false;
	private int lastMouseMove;
	private int initiatingFrames;


	void applyWorldTranslation(PGraphics renderer) {
		
		if (GLOBAL.autoRotate)
			GLOBAL.rotateModelsY -= 0.017f;

		renderer.translate(GLOBAL.windowWidth / 2, GLOBAL.windowHeight / 2, 0);
		renderer.rotateX(GLOBAL.rotateModelsX);
		renderer.rotateY(GLOBAL.rotateModelsY);
		renderer.scale((float) GLOBAL.getZOOM());
		
		
		//System.out.println(GLOBAL.rotateModelsY);
		renderer.translate((-width / 2) + (int)GLOBAL.CAM_OFFSET_X, (-height / 2)
				+ (int)GLOBAL.CAM_OFFSET_Y);

     

	}

	@Override
	public void draw() {
		
		
		//not the first loop
		if(firstLoop){
			
	
			
			

			
			//this.initiatingFrames++;
			//if(this.initiatingFrames > 100)
				firstLoop = false;

		}
		
		
		
		if(UI.menuListen.quedAction != null)
			UI.menuListen.processAction();
		
		
		//HACKy stuff 
		 if(GLOBAL.loadChairLocation != null){
			 GLOBAL.uiTools.load(GLOBAL.loadChairLocation);
			 GLOBAL.loadChairLocation = null;
		 }
		 
		 if(GLOBAL.saveChairLocation != null){
			 GLOBAL.uiTools.save(GLOBAL.saveChairLocation);
			 GLOBAL.saveChairLocation = null;
		 }
		 
		 
		 if(GLOBAL.savePatternLocation != null){
			 GLOBAL.uiTools.savePattern(GLOBAL.savePatternLocation);
			 GLOBAL.savePatternLocation = null;
		 }
		 
		 
		 
		 if(GLOBAL.importSVGLocation != null){
				GLOBAL.sketchChairs.getCurChair().importSVG( GLOBAL.importSVGLocation);
				GLOBAL.importSVGLocation = null;

		 }

		 
		//GLGraphics renderer = (GLGraphics)g;
		PGraphics renderer = (PGraphics)g;
		//if(useGLGRAPHICS)
			//renderer.beginGL();
		
		
		//if not in focus don't worry about running the draw loop
		if(!focused && !GLOBAL.skchAutomatic.hasChairToProcess() && !firstLoop && !startingApp){
			frameRate(1);
		//	return;
		}else{
			frameRate(120);
		}
		
		if(SETTINGS.autoReset && millis() - lastMouseMove > (SETTINGS.autoResetSeconds*1000)){
			GLOBAL.uiTools.reset(null);
			lastMouseMove = millis();
		}
			
		if(mouseX != pmouseX || mouseY != pmouseY)
		lastMouseMove = millis();

		GLOBAL.gui.update();
		
		

		
		//SKetchGlobals
		if ((GLOBAL.rotateModelsX != 0 || GLOBAL.rotateModelsY != 0))
			GLOBAL.SketchGlobals.renderVolume = true;
		else
			GLOBAL.SketchGlobals.renderVolume = false;

		GLOBAL.SketchGlobals.physicsEngineScale = GLOBAL.jBullet.scale;
		GLOBAL.SketchGlobals.mousePressed = mousePressed;
		
		
		
		if(width != GLOBAL.windowWidth || height != GLOBAL.windowHeight)
		resize();
		//renderer.noSmooth();

		//make sure we always have a chair to edit
		if (GLOBAL.sketchChairs.getCurChair() == null) {
			SketchChair curChair = new SketchChair();
			GLOBAL.sketchChairs.add(curChair);
		}

		//tittle images
		if (GLOBAL.tittleImg != null || GLOBAL.myMovie != null) {
			background(250, 250, 250);
			renderer.imageMode(CENTER);

			if (GLOBAL.myMovie != null) {
				renderer.pushMatrix();
				renderer.scale(1.6f);
				//   image(GLOBAL.myMovie,width/3.2f,height/3.2f);
				renderer.popMatrix();
				renderer.image(GLOBAL.clickToStart, width / 2, height - 25);

			} else {
				renderer.image(GLOBAL.tittleImg, width / 2, height / 2);
			}
			renderer.imageMode(CORNER);
			renderer.image(UITools.SELECT_TOOL_CURSOR, mouseX - 10, mouseY - 8);
			//mouse clicked 
			//if(mousePressed && !this.mouseDown && millis() > 1000){
			if (pmouseX != mouseX && millis() - GLOBAL.inativeCounter > 3000) {
				GLOBAL.tittleImageNum++;

				if (GLOBAL.myMovie != null) {
					//if(functions.fileExists("./introVideos/" + GLOBAL.tittleImageNum + ".mov"))
					//	GLOBAL.myMovie = new FasterMovie(this,"./introVideos/"+GLOBAL.tittleImageNum+".mov");
					//else
					//	GLOBAL.myMovie = null;

				} else {
					if (functions.fileExists("./tittleImage/"
							+ GLOBAL.tittleImageNum + ".jpg"))
						GLOBAL.tittleImg = loadImage("./tittleImage/"
								+ GLOBAL.tittleImageNum + ".jpg");
					else
						GLOBAL.tittleImg = null;
				}
				//reset the timeout counter
				GLOBAL.inativeCounter = millis();
			}

			if (mousePressed)
				this.mouseDown = true;
			else
				this.mouseDown = false;

			return;
		}

		if (mouseY != pmouseY || mouseX != pmouseX)
			GLOBAL.inativeCounter = millis();

		//if (millis() - GLOBAL.inativeCounter > GLOBAL.timeoutCounter) {
			/*
			setup();
			GLOBAL.tittleImageNum = 0;
			GLOBAL.inativeCounter = millis();
			GLOBAL.myMovie = new FasterMovie(this,"./introVideos/"+GLOBAL.tittleImageNum+".mov");
			GLOBAL.myMovie.loop();
			//GLOBAL.tittleImg = loadImage("./tittleImage/" + GLOBAL.tittleImageNum + ".jpg");	
			 */

		//}

		GLOBAL.windowWidth = width;
		GLOBAL.windowHeight = height;

		if (!GLOBAL.performanceMode) {

		}

		UI.toggleButtons();
	
		  float cameraY = (float) (height/2.0);
		  float fov = (width) * PI/2;
		  float cameraZ = 0;
		  float aspect = (width)/(height);
		  if (mousePressed) {
		    aspect = (float) (aspect / 2.0);
		  }
		  
		
		  if(SETTINGS.LEGACY_MODE){
			  renderer.ortho(-(int)(width / 2), (int)(width / 2), -(int)(height / 2), (int)(height / 2),-10000, 10000);	  
		  }else{
			  renderer.ortho(0,width, 0, height,-10000, 10000);
		  }
		  
		 // 
		  
		//GLOBAL.uiTools.pickBuffer.background(255);
		renderer.noFill();
		renderer.strokeWeight(2);	
		

		//GLOBAL.uiTools.update();
		UI.updateMouse(mouseX, mouseY, pmouseX, pmouseY, mousePressed,
				mouseButton);

		//MOUSE STUFF
		GLOBAL.uiTools.updateMouse(mouseX, mouseY, pmouseX, pmouseY, mouseDown,
				mouseButton);
		
		if(GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT)
		GLOBAL.uiTools.SketchTools.updateMouse(mouseX, mouseY, pmouseX,pmouseY, mouseDown, mouseButton);

		GLOBAL.uiTools.updateMouseWorld();

		if(mouseDoubleClick){GLOBAL.uiTools.mouseDoubleClick();}

		GLOBAL.uiTools.keyPressed = keyPressed;
		GLOBAL.uiTools.key = key;
		GLOBAL.uiTools.keyCode = keyCode;

		GLOBAL.uiTools.SketchTools.keyPressed = keyPressed;
		GLOBAL.uiTools.SketchTools.key = key;
		GLOBAL.uiTools.SketchTools.keyCode = keyCode;
		//update zoom
		GLOBAL.uiTools.SketchTools.zoom = (float) GLOBAL.getZOOM();

		
		
	

		if (mousePressed && !this.mouseDown)
			UI.mouseClicked = true;
		else
			UI.mouseClicked = false;

		GLOBAL.uiTools.mousePressed = false;
		// add a new chair when clicked
		if (mousePressed && !this.mouseDown) {
			GLOBAL.uiTools.mousePressed();
			GLOBAL.uiTools.SketchTools.mousePressed();
			GLOBAL.uiTools.mousePressed = true;
		}

		if (mousePressed) {
			GLOBAL.uiTools.mouseDown();
			GLOBAL.uiTools.SketchTools.mouseDown();
		}

		if (mousePressed && (mouseX != pmouseX || mouseY != pmouseY)
				&& !GLOBAL.gui.overComponent()) {
			GLOBAL.uiTools.mouseDragged();
			GLOBAL.uiTools.SketchTools.mouseDragged();
		}

		if (!mousePressed && this.mouseDown && !mouseDoubleClick) {
			GLOBAL.uiTools.mouseReleased();
			
			if(GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT)			
			GLOBAL.uiTools.SketchTools.mouseReleased();
			GLOBAL.jBullet.mouseReleased(mouseX, mouseY);
		}

		mouseDoubleClick = false;
		
		
		if (mousePressed)
			this.mouseDown = true;
		else
			this.mouseDown = false;

		if (SETTINGS.autoSave) {
			this.autoSaveCounter++;
			if (this.autoSaveCounter > SETTINGS.autoSaveInterval) {

				GLOBAL.uiTools.autoSave();
				this.autoSaveCounter = 0;
			}

		}

		//uggly hack set the physics togge 
		//UI.physicsToggle.setState(SETTINGS.physics_on);	
		if (GLOBAL.savePDF) {
			this.makePDF();
			GLOBAL.savePDF = false;
		}


		if (GLOBAL.saveDXF) {
			this.makeDXF();
			GLOBAL.saveDXF = false;
		}

		
		if (GLOBAL.sketchChairs.getCurChair() != null)
			GLOBAL.planesWidget.planes = GLOBAL.sketchChairs.getCurChair()
					.getSlicePlanesY();

		
		
		//Updates 
		GLOBAL.sketchChairs.update();
		GLOBAL.uiTools.update();
		GLOBAL.widgetMaterials.update();
		

		render(renderer);
		//renderer.camera();
		

		
		
		
		if(GLOBAL.applet.millis()%3000 == 1 && SETTINGS.autoRefreshTextures)
			GLOBAL.forceResize = true;


		GLOBAL.tick++;
		if (GLOBAL.tick > 1000000)
			GLOBAL.tick = 0l;
		
		
		if(GLOBAL.forceReset){
			GLOBAL.forceReset = false;
			this.setup();
		}

		//if(useGLGRAPHICS)
		//renderer.endGL();
		

		
	}



	public void keyPressed() {

		if (keyCode == PApplet.ESC || key == PApplet.ESC) {
			key = 0;
			keyCode = 0;
		}

		if (key == 'm') {
			this.switchResolution();
		}
		
		if(key == '4'){
//GLOBAL.gui.reBuildStencilBuffer();
}
		
	
		//if(key == '=')
		//renderSunflow();

		UI.keyPressed(key, keyCode);

	}

	public void keyReleased() {
		if (keyCode == PApplet.ESC || key == PApplet.ESC) {
			key = 0;
			keyCode = 0;
		}
		UI.keyReleased(key, keyCode);

	}

	public void keyTyped() {
		if (keyCode == PApplet.ESC || key == PApplet.ESC) {
			key = 0;
			keyCode = 0;
		}

	}

	public void makeDXF() {
		if (GLOBAL.dxfSaveLocation != null) {
			GLOBAL.shapePack.makeDXF(this, GLOBAL.dxfSaveLocation);

		}
	}

	
	private void makePNGPreview() {
		
		int previewWidth = width;
		int previewHeight = height;
		PImage img = GLOBAL.sketchChairs.getCurChair().renderDiagram(SETTINGS.renderWidth,SETTINGS.renderHeight,GLOBAL.cropExportToScreen);
		img.save(GLOBAL.pngPreviewSaveLocation);
		//img.delete();
		img = null;
		System.gc();
	}
	
	
	public void makePDF() {
		if (GLOBAL.pdfSaveLocation != null) {
			
			
			GLOBAL.shapePack.makePDF(this, GLOBAL.pdfSaveLocation);
			if (GLOBAL.autoOpenPDF)
				GLOBAL.uiTools.printOpen(GLOBAL.pdfSaveLocation);

			GLOBAL.autoOpenPDF = false;
			GLOBAL.pdfSaveLocation =null;
			GLOBAL.savePDF = false;
		} else
			GLOBAL.shapePack.makePDF(this);
		/*PGraphics pdf = createGraphics(this.width, this.height, PApplet.PDF,
				"output.pdf");
		pdf.beginDraw();
		this.curChair.saveToPDF(pdf);
		pdf.dispose();
		pdf.endDraw();
		*/
	}

	void movieImageAvailable(PImage _video) {
		vidFrame = _video;
	}

	public void OSXFileHandler(String path) {
		GLOBAL.uiTools.load(path);
	}

	public boolean OSXQuit() {
		GLOBAL.applet.exit();
		return true;
	}

	
	
	
	/**
	 * Main render loop 
	 * @param renderer
	 */
	void render(PGraphics renderer) {
		
		
		//

		
		renderer.background(SETTINGS.background_colour);
		/*
		renderer.fill(SETTINGS.background_colour);
		renderer.hint(DISABLE_DEPTH_TEST); // disable depth testing so that we
		renderer.rect(0,0,cc.sketchchair.core.GLOBAL.windowWidth, GLOBAL.windowHeight);
		renderer.hint(ENABLE_DEPTH_TEST); // disable depth testing so that we
*/
		//this.g.smooth();
		//
		if (GLOBAL.getZOOM() < .01f)
			GLOBAL.setZOOM(.01f);

		if (GLOBAL.getZOOM() > 10)
			GLOBAL.setZOOM(10);

		// reference image
		renderer.pushMatrix();
		applyWorldTranslation(renderer);
		if(!useGLGRAPHICS){
			/*
		//render settings
		PGraphicsOpenGL pgl = (PGraphicsOpenGL)g;
		GL gl = pgl.beginGL();
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_BLEND);
		;
		//gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
		//gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
	//	gl.setSwapInterval(1);
		//gl.glEnable (GL.GL_LINE_SMOOTH);
		//gl.glEnable(GL.GL_POLYGON_SMOOTH);
		pgl.endGL(); 
		*/
		}
	    renderer.ambientLight(200, 200, 200);
		renderer.directionalLight(69, 69, 69, 0, 0, -1);
		renderer.lightFalloff(1, 0, 0);
		//renderer.specular(0, 0, 0);

		if (GLOBAL.rotateModelsX == 0 && GLOBAL.rotateModelsY == 0)
			renderer.noLights();

		

			if (GLOBAL.dxfCapture) {
				beginRaw(DXF, GLOBAL.dxfLocation);
				LOGGER.info("exporting dxf now " + GLOBAL.dxfLocation);
			}

		//Automated actions
		if(GLOBAL.skchAutomatic.hasChairToProcess())
			GLOBAL.skchAutomatic.processNext();
		
		if (GLOBAL.exportPreviewPNG) {
			this.makePNGPreview();
			GLOBAL.exportPreviewPNG = false;
		}
		
		if(GLOBAL.saveChairToFile){
			GLOBAL.uiTools.saveChairToFile();
			GLOBAL.saveChairToFile = false;
		}
		
		if(GLOBAL.saveChairToFile){
			GLOBAL.uiTools.saveChairToFile();
			GLOBAL.saveChairToFile = false;
		}
		
		if(GLOBAL.saveChairToFileAuto){
			GLOBAL.uiTools.saveChairToFileAuto();
			GLOBAL.saveChairToFileAuto = false;
		}
		
		if (SETTINGS.render_chairs && GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT)
		GLOBAL.sketchChairs.render(renderer);
		
		
		

		  
		if (GLOBAL.dxfCapture) {
			endRaw();
			GLOBAL.dxfCapture = false;
		}
		
		renderer.noLights();
		
		//pick buffer rendering
		if(PickBuffer.getInstance().usePickBuffer && ((GLOBAL.uiTools.mousePressed && SETTINGS.ENABLE_SELECT_MODEL_PLANES) || firstLoop) ){

			LOGGER.debug("Update PickBuffer");
			PickBuffer.getInstance().pickBuffer.beginDraw();
			//PickBuffer.getInstance().pickBuffer.ortho(-(width / 2), (width / 2), -(height / 2), (height / 2),
			//		-1000, 10000);
			
			//PickBuffer.getInstance().pickBuffer.ortho(width/2, width , height/2, height,
				//	-1000, 10000);
			
			float scale = PickBuffer.getInstance().pickBufferRes;
			
			  //renderer.ortho(-(int)(width / 2), (int)(width / 2), -(int)(height / 2), (int)(height / 2),-10000, 10000);	  

			  if(SETTINGS.LEGACY_MODE){
				  PickBuffer.getInstance().pickBuffer.ortho(-(width*scale)/(2*scale),(width*scale)/(2*scale), -(height*scale)/(2*scale), (height*scale)/(2*scale),
							-10000, 10000);
				  }else{
				  PickBuffer.getInstance().pickBuffer.ortho(-(width*scale),width-(width*scale), -(height*scale), height-(height*scale),
							-10000, 10000);
				  }
			  
			  
			  
		
			
			PickBuffer.getInstance().reset();
			PickBuffer.getInstance().pickBuffer.resetMatrix();
			PickBuffer.getInstance().pickBuffer.background(255);
			PickBuffer.getInstance().pickBuffer.setMatrix(renderer.getMatrix());
			//PickBuffer.getInstance().pickBuffer.translate(width/1.35f, -height/1.35f, -400);
			//PickBuffer.getInstance().pickBuffer.scale(2f);
			if ( GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT)
			GLOBAL.sketchChairs.renderPickBuffer(PickBuffer.getInstance().pickBuffer);

			PickBuffer.getInstance().pickBuffer.endDraw();
		}
		
		
		// Physics updates
		if (GLOBAL.jBullet.physics_on) {
			//GLOBAL.jBullet.update();
			//functions.boundAllParticles(GLOBAL.physics);
		}

		//constrain ZOOm 

		//	GLOBAL.gui.update();

		//if (SETTINGS.draw_springs)
		//	functions.drawAllSprings(GLOBAL.physics, this.g);

		if (GLOBAL.jBullet.physics_on)
			GLOBAL.jBullet.step();

		if (GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT) {
			GLOBAL.jBullet.render(renderer);

			float GROUND_WIDTH = 10000;
			float GROUND_LENGTH = 10000;
			float GROUND_DEPTH = 1000;
			renderer.pushMatrix();
			renderer.noStroke();
			renderer.fill(SETTINGS.world_ground_side_colour);

			if (GLOBAL.rotateModelsX > 0)
				renderer.fill(SETTINGS.world_ground_under_colour);

			
			renderer.translate((int)(GLOBAL.windowWidth / 2), 1670 + (GROUND_DEPTH / 2.5f), 0);
			
			if(GLOBAL.floorOn){
			 renderer.box(GROUND_WIDTH, GROUND_DEPTH, GROUND_LENGTH);
			
			if(GLOBAL.rotateModelsX < 0){
				renderer.fill(SETTINGS.world_ground_colour);
				renderer.translate(0, -501,0);
			renderer.rotateX(PApplet.PI/2);
					renderer.rect(-GROUND_WIDTH/2,-GROUND_WIDTH/2, GROUND_WIDTH, GROUND_LENGTH);		
			}
			}
			
			renderer.popMatrix();
			
			
		}

		

		
		// debug
		if (GLOBAL.debugRayIntersection != null) {
			renderer.pushMatrix();
			renderer.fill(255, 0, 0);
			renderer.translate(GLOBAL.debugRayIntersection.x,
					GLOBAL.debugRayIntersection.y,
					GLOBAL.debugRayIntersection.z);
			renderer.sphere(10);
			renderer.popMatrix();

		}
		
		
		
		

		if (GLOBAL.debugRay != null) {

			renderer.pushMatrix();
			renderer.fill(255, 255, 0);
			renderer.translate(GLOBAL.debugRay.x, GLOBAL.debugRay.y, GLOBAL.debugRay.z);
			renderer.sphere(5);
			renderer.popMatrix();

			renderer.stroke(0, 255, 0);

			renderer.line(GLOBAL.debugRay.x, GLOBAL.debugRay.y, GLOBAL.debugRay.z,
					GLOBAL.debugRay.x
							+ (GLOBAL.debugRay.getDirection().x * 300),
					GLOBAL.debugRay.y
							+ (GLOBAL.debugRay.getDirection().y * 300),
					GLOBAL.debugRay.z
							+ (GLOBAL.debugRay.getDirection().z * 300));

		}
		
		
		renderer.textFont(GLOBAL.font);

		if (GLOBAL.uiTools.currentView == GLOBAL.uiTools.VIEW_CHAIR_EDIT) {

			if (GLOBAL.rotateModelsX != 0 || GLOBAL.rotateModelsY != 0)
				renderer.noLights();

			
			if (GLOBAL.person != null)
				GLOBAL.person.render((1f / GLOBAL.jBullet.getScale()), renderer);
			renderer.noLights();
		}
		if (SETTINGS.EXPERT_MODE) {
			GLOBAL.environments.render(renderer);
		}

		// draw the GUI
		//this.renderer.fill(255, 255, 255);
		//this.renderer.hint(DISABLE_DEPTH_TEST); // disable depth testing so that we
		renderer.noLights();
		
		renderer.hint(DISABLE_DEPTH_TEST); // disable depth testing so that we

		
		renderer.popMatrix(); // end of camera

		 if (GLOBAL.uiTools.currentView == GLOBAL.uiTools.VIEW_SHAPE_PACK){
			GLOBAL.shapePack.render(renderer);
		
			
			if(PickBuffer.getInstance().usePickBuffer && GLOBAL.uiTools.mousePressed && SETTINGS.ENABLE_SELECT_MODEL_PLANES){

				PickBuffer.getInstance().pickBuffer.beginDraw();
				PickBuffer.getInstance().pickBuffer.resetMatrix();
				PickBuffer.getInstance().pickBuffer.setMatrix(g.getMatrix());
				
			GLOBAL.shapePack.renderPickBuffer(PickBuffer.getInstance().pickBuffer);
			PickBuffer.getInstance().pickBuffer.endDraw();
			
			}
			
		 }
		
		GLOBAL.measuretTool.render(renderer);


		
		//render chair preview
		renderSelectorThumbnail(renderer);

		// draw the pattern to the front of
		// the screen

	

		//if(!SETTINGS.EXPERT_MODE){
		//fill(255);
		//rect(width-100,0,width,height);
		//}
		//renderer.hint(ENABLE_NATIVE_FONTS); // disable depth testing so that we

	
		//renderer.resetMatrix();
		//renderer.popMatrix();
		GLOBAL.uiTools.renderGrid(renderer);
		GLOBAL.uiTools.render(renderer);

		
		
		
	
		
		if (SETTINGS.show_framerate) {
			LOGGER.info("FPS" +GLOBAL.applet.frameRate);
			renderer.fill(0);
			renderer.textSize(12);
			renderer.text((int) GLOBAL.applet.frameRate + " fps", 10, 80);
		}
		
		
		
	
		//glLoadIdentity(); 
		//g.resetMatrix();
		//g.printCamera();
		//g.printMatrix();
		//g.printProjection();
//		renderer.ortho();
		
		renderer.perspective();

		GLOBAL.gui.render(renderer);

		//renderer.hint(DISABLE_ACCURATE_2D); // disable depth testing so that we

		renderer.hint(ENABLE_DEPTH_TEST); // disable depth testing so that we

	
		//	if(SETTINGS.EXPERT_MODE){
		//GLOBAL.planesWidget.render(this.g);
		//GLOBAL.planesWidget.render(g);
		//}

		//this.renderer.hint(ENABLE_DEPTH_TEST); // disable depth testing so that we draw
		// the patern to the front of the screen

		if (GLOBAL.captureScreen) {
			screenCapture();
			GLOBAL.captureScreen = false;
		}

		if (SETTINGS.REC) {
			//	 GLOBAL.mm.addFrame();  // Add window's pixels to movie
		}
		
		
		if(GLOBAL.forceResize){
			this.resize(true);
			GLOBAL.forceResize = false;
		}
		
		if(GLOBAL.deleteAllChairsFlag){
			GLOBAL.uiTools.deleteAllChairs(null);
			GLOBAL.deleteAllChairsFlag = false;
		}
		
		
	   // int sec = second();
	   // if (sec != sec0) println("FPS: " + frameRate);
	   // sec0 = sec;
	    
		//render debug pick buffer 
		if(PickBuffer.getInstance().usePickBuffer && GLOBAL.debugPickBuffer){
			PickBuffer.getInstance().pickBuffer.beginDraw();
			renderer.pushMatrix();
			renderer.scale(1.0f/PickBuffer.getInstance().pickBufferRes);
			renderer.image(PickBuffer.getInstance().pickBuffer,0,0);
			renderer.popMatrix();
			PickBuffer.getInstance().pickBuffer.endDraw();
		}
		
	    	}

	void renderSunflow() {
		PGraphics gSun = createGraphics(width, height, "hipstersinc.P5Sunflow");

		//P5Sunflow sunflow = new P5Sunflow();
		//sunflow.setParent(this);

		// sunflow.width = width;
		//sunflow.height = height;
		// sunflow.allocate();

		// gSun.camera();

		gSun.loadPixels();
		gSun.beginDraw();
		gSun.ortho(-(width / 2), (width / 2), -(height / 2), (height / 2),
				-1000, 10000);

		applyWorldTranslation(gSun);
		GLOBAL.sketchChairs.render(gSun);

		gSun.updatePixels();
		gSun.endDraw();
		gSun.save("render.png");
		//P5Sunflow sunflow = (P5Sunflow) g;

	}

	public void resize() {
		resize(false);

		if (GLOBAL.modeChanged) {

			GLOBAL.modeChanged = false;
			resize(true);

		}
	}

	public void resize(boolean force) {
		
		LOGGER.debug("resizing");
		
		GLOBAL.windowWidth = width;
		GLOBAL.windowHeight = height;
		
		GLOBAL.previewWidget.setPos(GLOBAL.windowWidth-GLOBAL.previewWidget.getWidth(), 0);
		
		
		if(GLOBAL.designToolbarAdvanced != null)
		GLOBAL.designToolbarAdvanced.getBasePanel().setPos((GLOBAL.windowWidth - GLOBAL.designToolbarAdvanced.getWidth())/2.0f, GLOBAL.windowHeight -GLOBAL.designToolbarAdvanced.getHeight());
		
		//resize our pick buffer
		// Only create pickBuffer once - avoid recreating to prevent JOGL threading issues on macOS
		if (PickBuffer.getInstance().pickBuffer == null) {
			PickBuffer.getInstance().pickBuffer = createGraphics((int)(GLOBAL.windowWidth*PickBuffer.getInstance().pickBufferRes),(int)(GLOBAL.windowHeight*PickBuffer.getInstance().pickBufferRes), Legacy.instance().get3DRenderMode());
		}
		image(PickBuffer.getInstance().pickBuffer.get(),0,0);

		/*
		if (GLOBAL.windowWidth != width || GLOBAL.windowHeight != height
				|| force == true) {
		
			float deltaHeight = GLOBAL.windowHeight - height;
			float deltaWidth = GLOBAL.windowWidth - width;

			GLOBAL.windowWidth = width;
			GLOBAL.windowHeight = height;
			
			
			if(useOPENGL){
			( g).beginGL();
			GL gl = (GL)( g);
			gl.glFlush();
			//gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
			gl.glViewport(0, 0, width, height);
			//System.out.println("screen resized");
			}
		
			GLOBAL.gui.reload();
			GLOBAL.gui.reset();
			
			GLOBAL.font = loadFont("SegoeUI-12.vlw");
			//this.font = applet.createFont("Helvetica", 12);
			GLOBAL.gui.myFontMedium = GLOBAL.font;

			if(GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT) {

			if (SETTINGS.EXPERT_MODE)
				UI.setupGUITabsExpert(this, GLOBAL.gui);

			if (!SETTINGS.EXPERT_MODE)
				UI.setupGUITabsBasic(this, GLOBAL.gui);
			}else{
				UI.setupGUIPattern(this, GLOBAL.gui);
			}
			
			GLOBAL.uiTools.build(this);
			GLOBAL.uiTools.SketchTools.build(this);
			//GLOBAL.font = loadFont("TrebuchetMS-12.vlw");
			GLOBAL.gui.myFontMedium = GLOBAL.font;
			
			

			GLOBAL.CAM_OFFSET_Y -= (deltaHeight);


			//GLOBAL.CAM_OFFSET_X += deltaWidth;


		}
		*/

	}

	public void screenCapture() {
		String currentDir = new File(".").getAbsolutePath();
		saveFrame(currentDir + "\\screenCaptures\\" + "capture-####.png");
		LOGGER.info("Screen captured to: " + currentDir + "\\screenCaptures\\"
				+ "capture-####.png");
	}



	// Processing 4: settings() method must be defined to call size()
	public void settings() {
		System.out.println("=== SketchChair Starting ===");
		System.out.println("Initializing P3D renderer");
		// Use 80% of display size to avoid macOS dock
		size((int)(displayWidth * 0.8), (int)(displayHeight * 0.8), P3D);

		// Set application icon for P3D renderer using PJOGL.setIcon()
		// This must be done in settings() for OpenGL renderers
		PJOGL.setIcon("data/icons/program_icon_02_b_48x48x32.png");
	}

	@Override
	public void setup() {
		LOGGER.debug("Setup()");

		// Processing 4: Enable window resizing
		surface.setResizable(true);

		
		LOGGER.info("Operating System: " + System.getProperty("os.name"));

		LOGGER.info("Operating System architecture: "
				+ System.getProperty("os.arch"));

		/* Total number of processors or cores available to the JVM */
		LOGGER.info("Available processors (cores): "
				+ Runtime.getRuntime().availableProcessors());

		/* Total amount of free memory available to the JVM */
		LOGGER.info("Free memory (MB): " + Runtime.getRuntime().freeMemory()
				/ (1024 * 1024));

		/* This will return Long.MAX_VALUE if there is no preset limit */
		long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);
		/* Maximum amount of memory the JVM will attempt to use */
		LOGGER.info("Maximum memory (MB): "
				+ (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));

		/* Total memory currently in use by the JVM */
		LOGGER.info("Total memory (MB): " + Runtime.getRuntime().totalMemory()
				/ (1024 * 1024));

		LOGGER.info(System.getProperty("java.vm.name"));
		LOGGER.info("Java version : " + System.getProperty("java.vm.version"));

		// Debug: Print JOGL version info
		try {
			Package joglPkg = Package.getPackage("com.jogamp.opengl");
			if (joglPkg != null) {
				LOGGER.info("=== JOGL Version Info ===");
				LOGGER.info("Implementation Version: " + joglPkg.getImplementationVersion());
				LOGGER.info("Implementation Title: " + joglPkg.getImplementationTitle());
			} else {
				LOGGER.info("JOGL package not loaded in setup()");
			}
		} catch (Exception e) {
			LOGGER.warn("Could not get JOGL version: " + e.getMessage());
		}

		LOGGER.info("Starting SketchChair");
		// Processing 4: dataPath() may fail in JAR, handle gracefully
		try {
			LOGGER.info("DataPath set at :" + dataPath("TrebuchetMS-12.vlw"));
		} catch (Exception e) {
			LOGGER.warn("Could not determine dataPath (running from JAR): " + e.getMessage());
		}

		LOGGER.info("After exit");

		//set static link to applet surface
		if(GLOBAL.surface == null)
			GLOBAL.surface = this.getSurface();

		

		
		GLOBAL = new GLOBAL(this);
		SETTINGS = new SETTINGS();
		GLOBAL.sketchProperties.loadDefaults();
		// Processing 4: dataPath() may fail in JAR, provide fallback
		String localizationPath;
		try {
			localizationPath = dataPath("/");
		} catch (Exception e) {
			// Fallback to current directory when running from JAR
			localizationPath = "./";
			LOGGER.warn("Using fallback localization path: " + localizationPath);
		}
		Localization = new Localization(localizationPath, SETTINGS.language);

		//HackHacky
		GLOBAL.planesWidget = new WidgetPlanes(0, 0, 0, 0, GLOBAL.gui);
		GLOBAL.loadWidget = new WidgetLoad();
		GLOBAL.widgetMaterials = new WidgetMaterials(0, 0, 0, 0, GLOBAL.gui);

		//Static global components

		if(SETTINGS.SMOOTH_RENDER)
		smooth();
		else
		noSmooth();
		
		  
		
		
		  
		  
		UI = new UI();
		

		LOGGER.info("SketchChair v " + GLOBAL.version);

		// Processing 4: width and height are now set by size() in settings()
		GLOBAL.windowWidth = width;
		GLOBAL.windowHeight = height;
		
		Thread.currentThread().setDefaultUncaughtExceptionHandler(exception);
		useOPENGL = false;
		useGLGRAPHICS = true;
		if(useGLGRAPHICS){
			
			if (GLOBAL.resetting == false) {
				//if (this.frame != null)
					//size(GLOBAL.windowWidth, GLOBAL.windowHeight,GLConstants.GLGRAPHICS);
				//else
					//size(GLOBAL.windowWidth, GLOBAL.windowHeight, GLConstants.GLGRAPHICS);
				
				//size(GLOBAL.windowWidth, GLOBAL.windowHeight, OldP3D.NOSTALGIA);


			}
		}else{
		//GLConstants.GLGRAPHICS
		if (GLOBAL.resetting == false) {
			
				//size(GLOBAL.windowWidth, GLOBAL.windowHeight,"cc.sketchchair.core.GLGraphics");
			
			//	size(GLOBAL.windowWidth, GLOBAL.windowHeight, OPENGL);
		
		}
		}
		
		/*
		// Detect current openGL version and warn if necessary 
		PGL pgl = ((PGraphicsOpenGL) this.g).beginPGL();

		GL2 gl = pgl.gl.getGL().getGL2();
	    String version = gl.glGetString(GL.GL_VERSION).trim();

		LOGGER.info("This system uses OpenGL:"+version);

	    String[] parts = version.split(" ");
	    
	    if(parts.length > 0 )
	    	parts = parts[0].split(".");
	    
	    if(parts.length > 0){
	    	
	    	float versionFloat; 
	    	try{
		    	 versionFloat = Float.parseFloat(parts[0]);
	    }
		catch(NumberFormatException e) {
			versionFloat = 2;
		}
	    	
	    	if(versionFloat < 2){
	    	JOptionPane.showMessageDialog(frame,
	    		    "SketchChair requires OpenGL v 2.0 or higher to run. Your system currently supports up to OpenGL V "+versionFloat+". Please try SketchChair on a different system or make sure that any dedicated graphics cards are enabled. ",
	    		    "OpenGL Version Not Supported",
	    		    JOptionPane.ERROR_MESSAGE);
	    	}
	    	
	    }
	    
		*/
		//g.printProjection();
		

		if (GLOBAL.surface != null ){
			//causing crash?
			/*
			GLOBAL.surface.setSize(width, height); // setup and OPENGL window
     */

	// Icon setting code removed - causes threading deadlock in Processing 4
	// Processing 4 requires icon to be set in settings() using PJOGL.setIcon()
	// See /tmp/icon_code.txt for the code that was causing issues

	// Just set the title - this works fine
	GLOBAL.surface.setTitle("SketchChair");
	GLOBAL.surface.setResizable(true);
		}

		//if(useOPENGL)
		//hint(ENABLE_OPENGL_4X_SMOOTH);

		//hint(ENABLE_NATIVE_FONTS);
		//textMode(SCREEN);

		
		

		
		
		
		GLOBAL.jBullet.physics_on = false;
		GLOBAL.g = this.g;
		GLOBAL.applet = this;
		GLOBAL.gui.setup(this);
		GLOBAL.gui.renderOnUpdate = GLOBAL.useMaskedUpdating;
		


		
		if (SETTINGS.EXPERT_MODE)
			UI.setupGUITabsExpert(GLOBAL.applet, GLOBAL.gui);
		else
			UI.setupGUITabsBasic(GLOBAL.applet, GLOBAL.gui);
		
		
		
		frameRate(120);


		//GLOBAL.font = createFont("Helvetica", 30);
		//  String[] fontList = PFont.list();
		//  println(fontList);
		GLOBAL.gui.performanceMode = GLOBAL.performanceMode;

		if (GLOBAL.gui.performanceMode) {
			SETTINGS.cylinder_res = 4;
			SETTINGS.sphere_res = 9;
		}
		//if(!SETTINGS.EXPERT_MODE)
		//GLOBAL.clickToStart = loadImage("clickToStart.png");


		
		LOGGER.info("AFTER GUI AGAIN");

		GLOBAL.uiTools.build(this);
		//noCursor();
		GLOBAL.setZOOM(.5f);
		GLOBAL.CAM_OFFSET_Y = (float) -((700 - GLOBAL.windowHeight) + 900);
		// button = new GUI.Button(new Vec2D(60,0),
		// loadImage("delete_out.png"));
		// this.GLOBAL.ui.buttons.add(button);

		//GLOBAL.shapePack.pdf_pixels_per_mm = SETTINGS.pixels_per_mm;
	//	GLOBAL.shapePack.CAM_OFFSET_X += 100;
	//	GLOBAL.shapePack.CAM_OFFSET_Y += 100;

	//	GLOBAL.shapePack.ZOOM = 1.5f;

		// Processing 4: Mouse wheel is handled through mouseWheel() callback instead of listener
		// (Implementation moved to mouseWheel() method)

		GLOBAL.person = new ergoDoll(GLOBAL.jBullet.myWorld, new Vector3f(-80,
				-10, 0), 1f);
		
		
		//GLOBAL.person.translate(-90, -10, 0);
		//GLOBAL.person.scale(60f);
		// just to load everything in
		GLOBAL.jBullet.update();
		/*
		GLOBAL.environments.l.add(new Environment(loadImage("table.png")));
		//GLOBAL.environments.l.add(new Environment(loadImage("lamp.png")));
		//GLOBAL.environments.l.add(new Environment(loadImage("fan.png")));
		GLOBAL.environments.l.add(new Environment(loadImage("cd.png")));
		//GLOBAL.environments.l.add(new Environment(loadImage("officeChair.png")));
		//GLOBAL.environments.l.add(new Environment(loadImage("chair.png")));
		GLOBAL.environments.l.add(new Environment(loadImage("lamps.png")));
		GLOBAL.environments.l.add(new Environment(loadImage("teapot.png")));
		//GLOBAL.environments.l.add(new Environment(loadImage("bookCase.png")));

		*/

		/*
		GLOBAL.environments.panel.hide();
		GLOBAL.planesWidget.panel.hide();
		GLOBAL.slicesWidget.panel.hide();
		GLOBAL.loadWidget.panel.hide();
		GLOBAL.widgetMaterials.panel.hide();
		*/

		//		GLOBAL.gui.myFont = createFont("MS FMincho", 12); 

		LOGGER.info("load " + openChair);

		if (openChair != null)
			GLOBAL.uiTools.load(openChair);

		if (SETTINGS.autoSave) {
			GLOBAL.uiTools.autoLoad();
		}
		// turn off just resizing controls
		//this.frame.setResizable( true );

		
		//this.resize(true);
		mouseDown = false;
		mousePressed = false;
		//contains the title img
		GLOBAL.tittleImageNum = 0;

		//System.out.print(file);
		if (SETTINGS.EXHIBITION_MODE) {
			//	System.out.print("loaded Movie");
			//GLOBAL.myMovie = new FasterMovie(this,"./introVideos/"+GLOBAL.tittleImageNum+".mov");
			//GLOBAL.myMovie.loop();
			//GLOBAL.myMovie.read();
		}
		//if(GLOBAL.myMovie == null)
		//GLOBAL.tittleImg = loadImage("./tittleImage/" + GLOBAL.tittleImageNum + ".jpg");

		LOGGER.info("SketchChair finished setup");
		LOGGER.info("Total memory used (MB): "
				+ Runtime.getRuntime().totalMemory() / (1024 * 1024));

		if (GLOBAL.isMacOSX()) {

			//open with stuff 
			//http://www.devdaily.com/blog/post/jfc-swing/java-handle-drag-drop-events-mac-osx-dock-application-icon
			try {
				OSXAdapter.setFileHandler(
						this,
						getClass().getDeclaredMethod("OSXFileHandler",
								new Class[] { String.class }));
				OSXAdapter
						.setQuitHandler(
								this,
								getClass().getDeclaredMethod("OSXQuit",
										(Class[]) null));
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

		this.firstLoop  = true;
		this.initiatingFrames = 0; 
		
		//previewBuffer = createGraphics((int)previewW,(int)previewH,P3D);
		// Only create pickBuffer once - avoid recreating to prevent JOGL threading issues on macOS
		if (PickBuffer.getInstance().pickBuffer == null) {
			PickBuffer.getInstance().pickBuffer = createGraphics((int)(GLOBAL.windowWidth*PickBuffer.getInstance().pickBufferRes),(int)(GLOBAL.windowHeight*PickBuffer.getInstance().pickBufferRes),Legacy.instance().get3DRenderMode() );
		}
		image(PickBuffer.getInstance().pickBuffer.get(),0,0);
		//PickBuffer.getInstance().pickBuffer.hint(PApplet.DISABLE_TRANSFORM_CACHE);
		//PickBuffer.getInstance().pickBuffer.hint(PApplet.ENABLE_ACCURATE_2D);

		//FlurryAgent.onStartApp(null, "SVAM3TIHA2YVU7KEHHC7") ;
		
		//GLOBAL.uiTools.SketchTools.selectTool(SketchTools.DRAW_TOOL);
		//default tool
		GLOBAL.uiTools.SketchTools.selectTool(UITools.DRAW_TOOL);
		
		
		//Hints
		//g.hint(ENABLE_ACCURATE_2D); // slow but less memory maybe?
		g.hint(Legacy.DISABLE_STROKE_PERSPECTIVE);
		
		
				
	}

	private void switchResolution() {
LOGGER.debug("switchResolution");

		try {
			GraphicsDevice myGraphicsDevice = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getDefaultScreenDevice();

			// Processing 4: Use PSurface instead of frame
			PSurface surface = this.getSurface();
			surface.setResizable(false);

			// Get the native window from PSurface
			// PSurface wraps the underlying AWT/Swing window
			Object nativeWindow = surface.getNative();
			if (nativeWindow instanceof java.awt.Frame) {
				java.awt.Frame frame = (java.awt.Frame) nativeWindow;
				frame.setUndecorated(true);
				myGraphicsDevice.setFullScreenWindow(frame);
				frame.setVisible(true);
			}

			DisplayMode myDisplayMode = new DisplayMode(width, height, 32,
					DisplayMode.REFRESH_RATE_UNKNOWN);

			myGraphicsDevice.setDisplayMode(myDisplayMode);

		} catch (Exception e) {
		}

	}
	
	void renderSelectorThumbnail(PGraphics g){
	}
	
	public void mousePressed() {
		startingApp = false; //there was user interaction
		// Processing 4: Use getCount() instead of getClickCount()
		if (mouseEvent != null) {
			if (mouseEvent.getCount()==1)mouseSingleClick=true;
			if (mouseEvent.getCount()==2)mouseDoubleClick=true;
		}
	}

	// Processing 4: Mouse wheel handler
	public void mouseWheel(processing.event.MouseEvent event) {
		int notches = event.getCount();
		if(!GLOBAL.gui.hasFocus()){
			if (GLOBAL.uiTools.currentView == GLOBAL.uiTools.VIEW_CHAIR_EDIT)
				GLOBAL.uiTools.zoomView((notches / 10f));

			if (GLOBAL.uiTools.currentView == GLOBAL.uiTools.VIEW_SHAPE_PACK)
				GLOBAL.shapePack.zoomView(notches / 10f,GLOBAL.uiTools.mouseX,GLOBAL.uiTools.mouseY);
		}
	}

}
