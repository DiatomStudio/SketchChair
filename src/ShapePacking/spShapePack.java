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
package ShapePacking;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.SETTINGS;
import cc.sketchchair.functions.functions;
import cc.sketchchair.sketch.Sketch;

import ToolPathWriter.CraftRoboWriter;
import ToolPathWriter.DXFWriter;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.pdf.PGraphicsPDF;

/**
 * Main class responsible for backing spOutlines on a page. 
 * @author gregsaul
 *
 */
public class spShapePack {

	spShapes shapes = new spShapes();
	public spPages pages = new spPages();
	public float pdf_pixels_per_mm = 2.834658423659949f;
	public float dxf_pixels_per_mm = 1;
	public float plotter_pixels_per_mm = 20;
	float shape_scale = 1;
	float zoom = 1;
	float offsetX = 0;
	float offsetY = 0;
	boolean firstRender = true; // is this the first time we have rendered to the screen.
	public float materialWidth = 210;//mm
	public float materialHeight = 297;//mm

	float shapeGap = 2.5f;
	public float scale = 1;

	public float pdfScale = 1;
	private OutputStream out = null;
	private InputStream in = null;

	public float CAM_OFFSET_X = -500;
	public float CAM_OFFSET_Y = -500;
	public float ZOOM = 1;
	public float content_scale = 100;
	public boolean seperate_slots = false;
	public boolean addDogbones = false;
	public boolean add_guide_divets = false;

	
	public float inner_corner_radius = 5;
	private float textSize = 3.5f;
	public boolean autoPackPieces = false;
	public boolean addLabels = true;


	float minZoom = 0.1f;
	float maxZoom = 10.0f;
	float maxCamX = 10000;
	float minCamX = -10000;
	float maxCamY = 10000;
	float minCamY = -10000;
	
	public void add(spShape shape) {
		shape.shapePack = this;

		this.shapes.add(shape);
	}

	void addShape(spShape shape) {
		shape.shapePack = this;

		this.shapes.add(shape);
	}

	public void build() {

		this.pages.shapePack = this;
		
		if(this.autoPackPieces)
		this.pages.packShapesSmart(this.shapes);
		else
		this.pages.packShapesTile(this.shapes);
	}
	
	//This return the total height of the cutting sheet
	public float getHeight(){
		return this.pages.getHeight();

	}

	public void empty() {
		this.shapes.empty();
		this.pages.empty();
	}

	public ByteArrayOutputStream getDXFBuffered(PApplet a) {

		PGraphicsPDF pdf = (PGraphicsPDF) a.createGraphics(
				(int) (this.materialWidth), (int) (this.materialHeight), a.PDF);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pdf.setOutput(out);

		pdf.beginDraw();
		//pdf.scale(pdf_pixels_per_mm );

		pdf.strokeWeight(.0001f);
		pdf.stroke(255, 0, 0);

		//PFont font = a.createFont("Arial", 8);
		//pdf.textFont(font);

		this.pages.renderPDF(pdf, pdf_pixels_per_mm);
		pdf.dispose();
		pdf.endDraw();

		return out;
	}

	public ByteArrayOutputStream getPDFBuffered(PApplet a) {

		PGraphicsPDF pdf = (PGraphicsPDF) a.createGraphics(
				(int) (this.materialWidth), (int) (this.materialHeight), a.PDF);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		pdf.setOutput(out);
	//	pdf.textSize(this.textSize);
		pdf.beginDraw();
		//pdf.scale(pdf_pixels_per_mm );

		pdf.strokeWeight(.0001f);
		pdf.stroke(255, 0, 0);
		this.pages.renderPDF(pdf, pdf_pixels_per_mm);
		pdf.dispose();
		pdf.endDraw();
		return out;
	}

	public void makeDXF(PApplet a, String dxfSaveLocation) {
		a.textSize(this.textSize);

		DXFWriter dxf = new DXFWriter(dxfSaveLocation);
		//d.beginRaw(a.DXF, dxfSaveLocation);

		dxf.scale(dxf_pixels_per_mm);

		this.pages.renderToPlotter(dxf);

		dxf.close();
		//dxf.endDraw();
	}

	public void makePDF(PApplet a) {
		PGraphicsPDF pdf = (PGraphicsPDF) a.createGraphics(
				(int) (this.materialWidth),
				(int) (this.materialHeight),
				a.PDF,
				"C:\\MyMedia\\sketchChair\\pdfOutput\\output"
						+ functions.getFileName() + ".pdf");
		PFont font = a.createFont("Arial", this.textSize);
		pdf.textFont(font);
		pdf.textSize(this.textSize);

		pdf.beginDraw();
		//pdf.scale(pdf_pixels_per_mm );

		pdf.strokeWeight(.0001f);
		pdf.stroke(255, 0, 0);
		this.pages.renderPDF(pdf, pdf_pixels_per_mm);
		pdf.dispose();
		pdf.endDraw();

	}

	public void makePDF(PApplet a, String pdfSaveLocation) {

		PGraphicsPDF pdf = (PGraphicsPDF) a.createGraphics(
				(int) (this.materialWidth * pdf_pixels_per_mm),
				(int) (this.materialHeight * pdf_pixels_per_mm), PConstants.PDF,
				pdfSaveLocation);
		
		LOGGER.info("sending PDF to " + pdfSaveLocation);
		pdf.beginDraw();
		//pdf.scale(pdf_pixels_per_mm);
		pdf.strokeWeight(.0001f);
		pdf.stroke(255, 0, 0);
		//PFont font = a.createFont("Arial", this.textSize);
		//pdf.textFont(font);
		pdf.textSize(this.textSize);
		this.pages.renderPDF(pdf, pdf_pixels_per_mm);
		pdf.dispose();
		pdf.endDraw();
	}
	
	public void makeSVG(PApplet a, String svgSaveLocation){
		
		try {
			
			Element root = getSVG(a);

			Document doc = new Document(root);
			OutputStream outXML = new FileOutputStream(svgSaveLocation);
			outXML = new BufferedOutputStream(outXML);
			Serializer serializer = new Serializer(outXML, "ISO-8859-1");
			serializer.write(doc);


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public Element getSVG(PApplet a){
		
		Element patternInfo = new Element("skchptrn","http://www.sketchchair.cc/formats/skchptrn");
		patternInfo.addAttribute(new Attribute("version","0.9"));
		patternInfo.addAttribute(new Attribute("width",materialWidth+"px"));
		patternInfo.addAttribute(new Attribute("height",materialHeight+"px"));
		patternInfo.addAttribute(new Attribute("scale",this.scale+""));
		patternInfo.addAttribute(new Attribute("mm_px","1"));
		patternInfo.addAttribute(new Attribute("designID","0"));
		patternInfo.addAttribute(new Attribute("notes",""));
		patternInfo.addAttribute(new Attribute("materialWidth",""));
		patternInfo.addAttribute(new Attribute("dogbonesAdded",""));
		patternInfo.addAttribute(new Attribute("user",""));

		
		Element root = new Element("svg","http://www.w3.org/2000/svg");
		root.appendChild(patternInfo);

		
		
		root.addAttribute(new Attribute("version","1.1"));
		root.addAttribute(new Attribute("width",materialWidth+"px"));
		root.addAttribute(new Attribute("height",materialHeight+"px"));
		root.addAttribute(new Attribute("style","fill:rgb(0,0,0)"));
		root.appendChild(this.pages.toXML());
		
		
		return root;
		
	}
	

	public void printToCraftRobo() {
		String currentDir = new File(".").getAbsolutePath();
		currentDir = currentDir.substring(0, currentDir.length() - 1);
		LOGGER.info("saving plt file to: " + currentDir + "temp.plt");
		CraftRoboWriter craftRoboWriter = new CraftRoboWriter(currentDir
				+ "temp.plt");
		craftRoboWriter.setupDefault();
		craftRoboWriter.setPenForce(30);
		craftRoboWriter.scale(plotter_pixels_per_mm);
		this.pages.renderToPlotter(craftRoboWriter);
		craftRoboWriter.close();

		

		try

		{

			Runtime rt = Runtime.getRuntime();
			Process p;
			String craftRoboPath = "";
			String osName = System.getProperty("os.name");
			if (osName.startsWith("Mac OS X")){
				String[] cmd = {"/usr/bin/open", "-a" , "Cutting Master 2 for CraftROBO.app",  "/Applications/Cutting Master 2 CraftROBO 1.86/Release/"};
				 p = rt.exec(cmd);
				}else{
				craftRoboPath = "C:/Program Files/Cutting Master 2 for CraftROBO 1.60/Program/App2.exe";
				 p = rt.exec(craftRoboPath);
			}
			LOGGER.info("Running: " + craftRoboPath);
			
			//

			

			
			in = p.getInputStream();

			if (in.available() > 0)
				System.out.println(in.toString());

			out = p.getOutputStream();

			InputStream err = p.getErrorStream();

			//p.destroy() ;

		} catch (Exception exc) {/*handle exception*/
		}

	}

	public void setupFirstRender(PGraphics g){
		ZOOM = ( (float)g.height/this.materialHeight);
		this.CAM_OFFSET_X = (int) -(this.materialWidth/2.0f);
		this.CAM_OFFSET_Y = (int) -(this.materialHeight/2.0f);
		
	}
	public void render(PGraphics g) {
		if(firstRender)
			setupFirstRender(g);//if this is the first time we have rendered then setup the correct postion on the screen
		
	
		if(ZOOM < minZoom)
		ZOOM = minZoom;
		
		if(ZOOM > maxZoom)
			ZOOM = maxZoom;
	
		if(CAM_OFFSET_X > maxCamX)
			CAM_OFFSET_X = maxCamX;
	
		if(CAM_OFFSET_X < minCamX)
			CAM_OFFSET_X = minCamX;
		
		
		if(CAM_OFFSET_Y > maxCamY)
			CAM_OFFSET_Y = maxCamY;
	
		if(CAM_OFFSET_Y < minCamY)
			CAM_OFFSET_Y = minCamY;
		
		
		
		firstRender = false;	
		g.textSize(this.textSize);
		g.fill(0);
		g.noStroke();
		g.pushMatrix();
		g.translate(g.width/2, g.height/2);
		g.scale(this.ZOOM);
		g.translate(this.CAM_OFFSET_X, this.CAM_OFFSET_Y);
		this.pages.render(g);
		g.popMatrix();

	}
	
	
	
	public void renderPickBuffer(PGraphics pickBuffer) {
		firstRender = false;	
		pickBuffer.noFill();
		pickBuffer.stroke(0);
		pickBuffer.pushMatrix();
		pickBuffer.scale(this.ZOOM);
		pickBuffer.translate(this.CAM_OFFSET_X, this.CAM_OFFSET_Y);
		this.pages.renderPickBuffer(pickBuffer);
		pickBuffer.popMatrix();
		
	}

	
	
	
	public void renderList(PGraphics g) {
		g.textSize(this.textSize);
		g.noFill();
		g.stroke(0);
		g.pushMatrix();
		this.pages.renderList(g);
		g.popMatrix();

	}
	

	public void scaleAll(float scale) {
		this.shapes.scale(scale);
	}

	public void renderPickBufferList(PGraphics pickBuffer) {
		this.pages.renderPickBufferList(pickBuffer);
	}

	public void zoomView(float _zoomDelta, float _mouseX, float _mouseY){
		
		
		float deltaMouseXBefore = (float) (((GLOBAL.applet.width/2)-_mouseX)/this.ZOOM);
		float deltaMouseYBefore = (float) (((GLOBAL.applet.height/2)-_mouseY)/this.ZOOM);
		
		
		this.ZOOM -= _zoomDelta;

		
		if( (_zoomDelta > 0 && this.ZOOM < SETTINGS.MIN_ZOOM)){
		//	this.ZOOM = SETTINGS.MIN_ZOOM;
		}
				
		if(_zoomDelta < 0 && this.ZOOM > SETTINGS.MAX_ZOOM){
	//		this.ZOOM = SETTINGS.MAX_ZOOM;
		}
		
		 float deltaMouseXAfter = (float) (((GLOBAL.applet.width/2)-_mouseX)/this.ZOOM);
		 float deltaMouseYAfter = (float) (((GLOBAL.applet.height/2)-_mouseY)/this.ZOOM);
		
		 
		 float deltaMouseX = deltaMouseXAfter - deltaMouseXBefore; 
		 float deltaMouseY = deltaMouseYAfter - deltaMouseYBefore;
		 
		 moveView(-deltaMouseX,-deltaMouseY);

	}

	void moveView(float _deltaX, float _deltaY){
		
		
		this.CAM_OFFSET_X +=  _deltaX;
		this.CAM_OFFSET_Y +=  _deltaY;
	
		 
		 float leftEdge = (float) (this.CAM_OFFSET_X - ((GLOBAL.applet.width/2)/this.ZOOM));
			//if(leftEdge < SETTINGS.MIN_CAM_X_OFFSET)
			//	this.CAM_OFFSET_X = SETTINGS.MIN_CAM_X_OFFSET + ((GLOBAL.applet.width/2)/this.ZOOM);
			
			
			 float rightEdge = (float) (this.CAM_OFFSET_X + ((GLOBAL.applet.width/2)/this.ZOOM));
			//if(rightEdge > SETTINGS.MAX_CAM_X_OFFSET)
				//this.CAM_OFFSET_X = SETTINGS.MAX_CAM_X_OFFSET - ((GLOBAL.applet.width/2)/this.ZOOM);
			
			double topEdge = (double) this.CAM_OFFSET_Y + ((GLOBAL.applet.height/2)/this.ZOOM);
			//if(topEdge > SETTINGS.MAX_CAM_Y_OFFSET)
				//this.CAM_OFFSET_Y = SETTINGS.MAX_CAM_Y_OFFSET - ((GLOBAL.applet.height/2)/this.ZOOM);
			
			float bottomEdge = (float) (GLOBAL.CAM_OFFSET_Y - ((GLOBAL.applet.height/2)/this.ZOOM));
			//if(bottomEdge < SETTINGS.MIN_CAM_Y_OFFSET)
				//this.CAM_OFFSET_Y = SETTINGS.MIN_CAM_Y_OFFSET + ((GLOBAL.applet.height/2)/this.ZOOM);
			
		 
	}
	
}
