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
package cc.sketchchair.widgets;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.Legacy;
import cc.sketchchair.core.MouseEventSK;
import cc.sketchchair.core.PickBuffer;
import cc.sketchchair.core.SETTINGS;
import cc.sketchchair.core.UITools;
import cc.sketchchair.sketch.LOGGER;
import cc.sketchchair.sketch.Sketch;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import toxi.geom.Vec2D;
import ModalGUI.GUIButton;
import ModalGUI.GUIComponents;
import ModalGUI.GUIEvent;
import ModalGUI.GUILabel;
import ModalGUI.GUIPanel;
import ModalGUI.GUIScrollbar;
import ModalGUI.GUISlider;
import ModalGUI.GUIToggle;
import ModalGUI.ModalGUI;
import ShapePacking.spShapePack;

public class WidgetPreviewPanel extends GUIPanel {

	ModalGUI gui;
	float previewW = 150;
	float previewH = 150;
	float previewBorderY = 160;
	float previewBorderX = 10;
	PImage ergoFig = null;
	PGraphics patternPreview = null;
	PGraphics chairPreview = null;
	
	public GUIButton makeItBtn = null;
	public GUIButton designItBtn = null;


	PImage patternPreviewCache ;
	GUIScrollbar patternSlider = null;
	float prevPatternSlider = -1;
	
	boolean minimizeTop = false;
	boolean minimizePattern = false;
	
	public GUIComponents topComponents = new GUIComponents();
	public GUIComponents middleComponents = new GUIComponents();
	public GUIComponents bottomComponents = new GUIComponents();
	
	private spShapePack previewShapePack;
	
	float modelPannelYPos = 35;
	float patternPannelYPos = 270;
	float modelPannelYPosBase = patternPannelYPos - 25;

	public WidgetPreviewPanel(float x, float y, float w, float h, ModalGUI gui) {
		super(x, y, w, h, gui);
		
		this.gui = gui;
		this.renderBorder = false;
		this.hideSelectBar = true;
		ergoFig = gui.applet.loadImage("gui/GUI_ERGO_FIGURE.png");
		chairPreview = gui.applet.createGraphics((int)previewW,(int)previewH, Legacy.instance().get3DRenderMode() );
		chairPreview.smooth();
		gui.applet.image(chairPreview, -1000, 0); //load into cache
		//chairPreview.hint(PApplet.DISABLE_TRANSFORM_CACHE);
		//chairPreview.hint(PApplet.ENABLE_ACCURATE_2D);
		
	
		patternPreview = gui.applet.createGraphics((int) getWidth(), (int) (getHeight()-patternPannelYPos), Legacy.instance().get2DRenderMode());
		patternPreview.smooth();
		gui.applet.image(patternPreview, -1000, 0); //load into cache

		//patternPreview.hint(PApplet.DISABLE_TRANSFORM_CACHE);
		//patternPreview.hint(PApplet.ENABLE_ACCURATE_2D);

		
		previewShapePack = new spShapePack();
		
		float posX = 0;
		float posY = 0;
		/*
		GUIToggle toggle = new GUIToggle(yPos, 10,
				"gui/GUI_ERGO_FIGURE.png", "gui/GUI_ERGO_FIGURE_OFF.png", gui);
		toggle.addActionListener(GLOBAL.uiTools, "togglePerson", null);

		gui.add(toggle);
*/
		
		
		
		 posY = 65;
		 GUIToggle toggle = new GUIToggle(this.getWidth()-20,modelPannelYPos - 15,"gui/GUI_MINMIMIZE_PANEL_UP.png","gui/GUI_MINMIMIZE_PANEL_DOWN.png",gui );
		 toggle.addActionListener(this, "minimizeToggleTop", null);
		 this.add(toggle);
		 topComponents.add(toggle);
		 
		  toggle = new GUIToggle(this.getWidth()-20,patternPannelYPos - 15,"gui/GUI_MINMIMIZE_PANEL_UP.png","gui/GUI_MINMIMIZE_PANEL_DOWN.png",gui );
		 toggle.addActionListener(this, "minimizeTogglePattern", null);
		 middleComponents.add(toggle);
		 this.add(toggle);
		 
		
		 posX += 80;
		 
		 
		  toggle = new GUIToggle(posX-5+150,modelPannelYPosBase-75,"gui/GUI_ERGO_SHOW.png","gui/GUI_ERGO_HIDE.png",gui );
		 toggle.addActionListener(GLOBAL.uiTools, "togglePerson", null);
		 this.add(toggle);
		 middleComponents.add(toggle);

		 /*
		 label = new GUILabel(2,modelPannelYPos-20,"model | view", gui);
			label.addActionListener(GLOBAL.uiTools, "viewModel", null);
			this.add(label);
			*/
			//middleComponents.add(label);
		
		 
		 makeItBtn = new GUIButton(posX,-7,"gui/make_it_up.png","gui/make_it_down.png",gui );
		 makeItBtn.addActionListener(GLOBAL.uiTools, "viewPattern", null);
		 this.add(makeItBtn);
		 
		 designItBtn = new GUIButton(posX,-7,"gui/design_it_up.png","gui/design_it_down.png",gui );
		 designItBtn.addActionListener(GLOBAL.uiTools, "viewModel", null);
		 this.add(designItBtn);
		 
		 
		 
		 if(GLOBAL.uiTools.currentView == UITools.VIEW_CHAIR_EDIT){
		 designItBtn.hide();
		 makeItBtn.show();
		 }else{
		 designItBtn.show();
		 makeItBtn.hide(); 	 
		 }
		 
		GUIButton button = null;
		
		button = new GUIButton(posX+150,modelPannelYPosBase-50,"gui/GUI_ERGO_BIGGER.png","gui/GUI_ERGO_BIGGER.png",gui );
		button.addActionListener(GLOBAL.uiTools, "figureGrow", null);
		this.add(button);
		 middleComponents.add(button);

		
		button = new GUIButton(posX+150,modelPannelYPosBase - 35,"gui/GUI_ERGO_BAR.png","gui/GUI_ERGO_BAR.png",gui );
		this.add(button);
		 middleComponents.add(button);
		
		button = new GUIButton(posX+150,modelPannelYPosBase-10,"gui/GUI_ERGO_SMALLER.png","gui/GUI_ERGO_SMALLER.png",gui );
		button.addActionListener(GLOBAL.uiTools, "figureShrink", null);
		this.add(button);
		 middleComponents.add(button);
		 
		 /*
		label = new GUILabel(2,patternPannelYPos-20,"pattern | view", gui);
		label.addActionListener(GLOBAL.uiTools, "viewPattern", null);
		this.add(label);
		middleComponents.add(label);
		 */
		
		patternSlider = new GUIScrollbar(this.getWidth()-10,patternPannelYPos+10,getHeight()-patternPannelYPos-20,0,200,GUISlider.VERTICAL,gui);
		this.add(patternSlider);
		bottomComponents.add(patternSlider);

		
	}


	

	public void render(PGraphics g) {
		
		
	
		if (this.minimized)
			return;
		
		
		if (prevPatternSlider != patternSlider.getVal()
				&& previewShapePack != null) 
			rebuildPatternPreview();
		
		//Render on update optimization
		if(gui.renderOnUpdate && !reRender){
			components.render(g);
			return;
		}
		
		//if(controller.renderOnUpdate)
		//reRender = false; // only render once
		

		g.pushMatrix();
		g.translate(getX(), getY());

		g.fill(250);
		g.noStroke();
		g.rect(0, 0, getWidth(), getHeight());

		g.stroke(100);
		g.strokeWeight(0.5f);
		g.line(0, modelPannelYPos, getWidth(), modelPannelYPos);

		g.popMatrix();
		
		
		super.render(g);

		g.pushMatrix();
		g.translate(getX(), getY());
		
		if(minimizeTop){
		g.popMatrix();	
		return;
		}
		
		g.stroke(100);
		g.strokeWeight(0.5f);
		g.line(0, patternPannelYPos, getWidth(), patternPannelYPos);

		float scale = 0.15f;
		
		
		float scaleFactor = (scale * GLOBAL.person.getScale()) * 6.5f;
		
		
		g.pushMatrix();
		g.translate(45 - ((ergoFig.width / 2) * scaleFactor)+150,  modelPannelYPosBase - (ergoFig.height * scaleFactor));
		g.scale(scaleFactor); // what is the scale factor between the full size furniture and the ergo fig bitmap?
		g.image(ergoFig, 0, 0);
		g.popMatrix();
		
		

		
		
		if(chairPreview != null){
			float x = previewBorderX;
			float y = modelPannelYPosBase - chairPreview.height;
			//chairPreview.beginDraw();
			//g.rect(x,y,chairPreview.width,chairPreview.height);
			g.pushMatrix();
			
			if(GLOBAL.uiTools.currentView == UITools.VIEW_SHAPE_PACK){
			g.translate(125*(1.0f-SETTINGS.scale), 250*(1.0f-SETTINGS.scale));

			g.scale(SETTINGS.scale);

			}
			
			
			g.image(chairPreview,x,y);

			g.popMatrix();
			//chairPreview.endDraw();
		}
		
		

		
		if(minimizePattern){
			g.popMatrix();
			return;
		}
		
		if (patternPreview != null){
			//patternPreview.beginDraw();
			g.image(patternPreview, 0, patternPannelYPos+5);
			//patternPreview.endDraw();
			//g.rect(0, patternPannelYPos+5, 1, 1);
		}

		g.popMatrix();

		//scroll pattern
		/*
		if (prevPatternSlider != patternSlider.getVal()
				&& previewShapePack != null) {

			patternPreview.beginDraw();
			patternPreview.translate(0, -patternSlider.getVal());
			patternPreview.background(250);
			patternPreview.fill(250);
			patternPreview.noStroke();
			patternPreview.rect(0,0,patternPreview.width,patternPreview.height);
			previewShapePack.renderList(patternPreview);
			//patternPreviewCache = patternPreview.get();
			patternPreview.endDraw();
			patternSlider.setMaxVal(previewShapePack.getHeight()-patternPreview.height+50);

		}
		*/
		prevPatternSlider = patternSlider.getVal();

	
		
			
			if (GLOBAL.undo.getMouseUpChair() != null &&
					PickBuffer.getInstance().usePickBuffer
					&& GLOBAL.uiTools.mousePressed) {
				
				
			GLOBAL.undo.getMouseUpChair().slicePlanesY
					.setRenderMode(Sketch.RENDER_3D_PREVIW);
			GLOBAL.undo.getMouseUpChair().slicePlanesSlatSlices
					.setRenderMode(Sketch.RENDER_3D_PREVIW);


		
			float minX = GLOBAL.undo.getMouseUpChair().getSlicePlanesY()
					.getMinX();
			float minY = GLOBAL.undo.getMouseUpChair().getSlicePlanesY()
					.getMinY();
			float maxX = GLOBAL.undo.getMouseUpChair().getSlicePlanesY()
					.getMaxX();
			float maxY = GLOBAL.undo.getMouseUpChair().getSlicePlanesY()
					.getMaxY();

			float width = Math.abs(maxX - minX);
			float height = Math.abs(maxY - minY);

			float widthDelta = previewW / width;
			float heightDelta = previewH / height;
			scale = Math.min(widthDelta, heightDelta);

			//now we want to clamp this scale to a max and min;

			//scale changes in 5% increments;
			scale = (((int) ((scale * 100.0f) / 5.0f)) * 5.0f) / 100.0f;
			scale = gui.applet.constrain(scale, 0.1f, 0.15f);
			scale = 0.15f; //just keep one scale at the moment

			
			Vec2D v = new Vec2D(width/2,GLOBAL.sketchChairs.getCurChair().getWidth()/2.0f);
			v.rotate((float) (Math.PI/2));
			float rotatedWidth = -v.x*2;
			float totalWidth = ((rotatedWidth*scale)+(width*scale/2));

			float yBorder = 10;
			
			float x = this.getX()+previewBorderX+((rotatedWidth/2)*scale)  +  ((((previewW)-totalWidth)/2));//((previewW/scale) -rotatedWidth);)
			//float x =((rotatedWidth/2)*scale) + 100;
			
			float y = (this.getY()+modelPannelYPosBase) -(height*scale);//previewH - previewH + modelPannelYPosBase-100 + this.getY();

			
			
			
			g.pushMatrix();
			g.ortho(-(int)(GLOBAL.windowWidth / 2), (int)(GLOBAL.windowWidth / 2), -(int)(GLOBAL.windowHeight / 2), (int)(GLOBAL.windowHeight / 2),
					-10000, 10000);
			//g.hint(PApplet.DISABLE_STROKE_PERSPECTIVE);

			
			g.scale(scale);
			g.translate(x/scale , y/scale );
			g.rotateY((float) (-Math.PI / 4));
			g.translate(-minX, -minY);
	
				PickBuffer.getInstance().pickBuffer.beginDraw();
				PickBuffer.getInstance().pickBuffer.resetMatrix();
				PickBuffer.getInstance().pickBuffer.setMatrix(g.getMatrix());
				GLOBAL.sketchChairs.getCurChair().slicePlanesY
						.renderPickBuffer(PickBuffer.getInstance().pickBuffer);
				PickBuffer.getInstance().pickBuffer.endDraw();
			
				g.perspective();
				g.popMatrix();
			

			
			
			
			
				float xp = (this.getX()-getWidth())/2.0f;//((previewW/scale) -rotatedWidth);)				
			//	float yp =-(patternPannelYPos/2.0f)+(15);//previewH - previewH + modelPannelYPosBase-100 + this.getY();
				float yp =-(GLOBAL.windowHeight/2)+patternPannelYPos+5;//previewH - previewH + modelPannelYPosBase-100 + this.getY();

			
			PickBuffer.getInstance().pickBuffer.beginDraw();
			PickBuffer.getInstance().pickBuffer.resetMatrix();
			PickBuffer.getInstance().pickBuffer.translate(0, -patternSlider.getVal());
			PickBuffer.getInstance().pickBuffer.translate(xp,yp);
			previewShapePack.renderPickBufferList(PickBuffer.getInstance().pickBuffer);
			//previewShapePack.renderList(patternPreview);
			
			
			PickBuffer.getInstance().pickBuffer.endDraw();

			
			
			patternPreview.endDraw();
			
			
			}
			

		}
	
		
		
		
	

	
	
	
	
	
	@Override
	public void setup() {
		

		
	}


	@Override
	public void update() {
		super.update();
		
		
	}

	public void rebuildPatternPreview(){
		
		float scale = 0;

		LOGGER.debug("rebuildPatternPreview");
		
		
		//Render preview chair, if we have mouse pressed use the undo version of our chair otherwise 
		if (GLOBAL.sketchChairs.getCurChair() != null) {
			
			GLOBAL.sketchChairs.getCurChair().slicePlanesY
					.setRenderMode(Sketch.RENDER_3D_PREVIW);
			GLOBAL.sketchChairs.getCurChair().slicePlanesSlatSlices
					.setRenderMode(Sketch.RENDER_3D_PREVIW);

			float minX = GLOBAL.sketchChairs.getCurChair().getSlicePlanesY()
					.getMinX();
			float minY = GLOBAL.sketchChairs.getCurChair().getSlicePlanesY()
					.getMinY();
			float maxX = GLOBAL.sketchChairs.getCurChair().getSlicePlanesY()
					.getMaxX();
			float maxY = GLOBAL.sketchChairs.getCurChair().getSlicePlanesY()
					.getMaxY();

			float width = Math.abs(maxX - minX);
			float height = Math.abs(maxY - minY);
			
			float widthDelta = previewW / width;
			float heightDelta = previewH / height;
			scale = Math.min(widthDelta, heightDelta);

			//now we want to clamp this scale to a max and min;
			//scale changes in 5% increments;
			scale = (((int) ((scale * 100.0f) / 5.0f)) * 5.0f) / 100.0f;
			scale = gui.applet.constrain(scale, 0.1f, 0.15f);
			scale = 0.15f;//0.15f; //just keep one scale at the moment


			Vec2D v = new Vec2D(width/2,GLOBAL.sketchChairs.getCurChair().getWidth()/2.0f);
			v.rotate((float) (Math.PI/2));
			float rotatedWidth = -v.x*2;
			
			float yBorder =10;
			float totalWidth = ((rotatedWidth*scale)+(width*scale/2));
			//setting at 0 0 draws the chair in the top left corner of the fbo 
			float x =  (rotatedWidth/2)+((((previewW)-totalWidth)/2)/scale);//((previewW/scale) -rotatedWidth);
			float y =  (previewH/scale) - height-yBorder;
	
			float goundOffset = (previewH - (height * scale)) / scale;
			chairPreview.beginDraw();
			chairPreview.background(250);
			chairPreview.pushMatrix();
			//chairPreview.ortho(-(int)(chairPreview.width / 2), (int)(chairPreview.width  / 2), -(int)(chairPreview.height / 2), (int)(chairPreview.height / 2),
			//		-1000, 1000);
			
			
			
			  if(SETTINGS.LEGACY_MODE){
				  chairPreview.ortho(-(int)(chairPreview.width / 2), (int)(chairPreview.width / 2), -(int)(chairPreview.height / 2), (int)(chairPreview.height / 2),-10000, 10000);	  
			  }else{
				  chairPreview.ortho(0,chairPreview.width, 0, chairPreview.height,-10000, 10000);
			  }
		
			//chairPreview.hint(PApplet.DISABLE_STROKE_PERSPECTIVE);

			chairPreview.smooth();
			chairPreview.scale(scale);
			chairPreview.translate(x,y);
			//chairPreview.translate(0, +(height / 2));
			chairPreview.rotateY((float) (-Math.PI / 4));
			chairPreview.translate(-minX , -minY );
			GLOBAL.sketchChairs.getCurChair().slicePlanesY.render(chairPreview);

			chairPreview.perspective();
			chairPreview.popMatrix();
			chairPreview.endDraw();	
		}
	
		
		if(minimizePattern)
			return;
		
		if(GLOBAL.sketchChairs.getCurChair() != null && patternPreview!=null){

			
			previewShapePack.empty();
			previewShapePack.materialWidth = this.getWidth()-15;
			previewShapePack.materialHeight = 1000000;
			previewShapePack.autoPackPieces = false;


			GLOBAL.sketchChairs.getCurChair().addToPreviewShapePack(previewShapePack);
			previewShapePack.scaleAll(0.15f);
			previewShapePack.build();
			
			//prevents draw 
		//	if(patternPreview.canDraw()){
			patternPreview.beginDraw();
			patternPreview.translate(0, -patternSlider.getVal());
			patternPreview.fill(250);
			patternPreview.background(250);
			patternPreview.noStroke();
			//patternPreview.rect(0,0,patternPreview.width,patternPreview.height);
			previewShapePack.renderList(patternPreview);
			//patternPreviewCache = patternPreview.get();
			patternPreview.endDraw();
			//}
			patternSlider.setMaxVal(previewShapePack.getHeight());
		}
	}
	
	@Override
	public void mouseEvent(MouseEventSK e) {
		super.mouseEvent(e);
		if (!this.visible)
			return;


		if (e.getAction() == MouseEventSK.PRESS) {

		} else if (e.getAction() == MouseEventSK.RELEASE) {
			if (!GLOBAL.gui.overComponent() && !GLOBAL.gui.hasFocus()) {
			//rebuildPatternPreview();
			}
			isDragging = false;
		}

	}

	public void minimizeToggleTop(GUIEvent e){
		
		
		minimizeTop = !minimizeTop;
		
		if(minimizeTop){
			this.setSize(this.getWidth(), modelPannelYPos+5);
			middleComponents.hideAll();
			bottomComponents.hideAll();
			this.controller.reBuildStencilBuffer();


		}else{
			this.setSize(this.getWidth(), GLOBAL.windowHeight);
			middleComponents.showAll();
			bottomComponents.showAll();
			minimizePattern = false;

			this.controller.reBuildStencilBuffer();
		}
		
		
	}
	
	public void minimizeTogglePattern(GUIEvent e){
			
		minimizePattern = !minimizePattern;

		if(minimizePattern){
			this.setSize(this.getWidth(), patternPannelYPos+5);
			bottomComponents.hideAll();
			this.controller.reBuildStencilBuffer();
		}else{
			this.setSize(this.getWidth(), GLOBAL.windowHeight);
			bottomComponents.showAll();
			this.rebuildPatternPreview();
			this.controller.reBuildStencilBuffer();
		}
		
		
	}

	
	
	
	
}
