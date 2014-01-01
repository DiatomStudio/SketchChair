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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cc.sketchchair.functions.MCrypt;
import cc.sketchchair.sketch.SketchTools;

import nu.xom.Attribute;
import nu.xom.Element;
import processing.core.PGraphics;
import processing.core.PImage;
import ModalGUI.GUIButton;
import ModalGUI.GUIEvent;
import ModalGUI.GUILabel;
import ModalGUI.GUITextfield;
import ModalGUI.GUIToggle;
import ModalGUI.GUIWindow;

public class SketchChairCloudhook {

	GUIWindow loginWindow = null;
	GUITextfield loginUsername = null;
	GUITextfield loginPassword = null;
	GUILabel loginLabel = null;

	private static SketchChairCloudhook instance = null;
	
	  public static SketchChairCloudhook getInstance() {
	      if(instance == null) {
	         instance = new SketchChairCloudhook();
	      }
	      return instance;
	   }
	  
	  
	
	/*
	 * Share The chaire online upload and setup everything needed for the design
	 */
	public void ShareChairOnline(GUIEvent e) {
		
		LOGGER.info("About to upload:");
		
		//Make sure a design is selected
		if (GLOBAL.sketchChairs.getCurChair() == null)
			return;

		String sharedChairID = "";
		
		String[][] sessionIDargs = new String[1][2];
		if(GLOBAL.sessionID != null){
		sessionIDargs[0][0] = "sessionID";
		sessionIDargs[0][1]	= GLOBAL.sessionID;
		}else{
			sessionIDargs = new String[0][0];
		}
		//System.out.println(GLOBAL.cloudHook.postAction("isLoggedIn_CH",sessionIDargs));
		
		//Upload the design file
				if(GLOBAL.cloudHook.postAction("isLoggedIn_CH",sessionIDargs).startsWith("FALSE")){
					loginBox();
					return;
				}else{
					LOGGER.info("logged in with session");
					
				}

		//If the chair doesn't already have a online ID make one and setup the design on the server
		//Here we might also want to check if we have rights to change this chair?
		if (GLOBAL.sketchChairs.getCurChair().cloudID != null) 
		sharedChairID = GLOBAL.sketchChairs.getCurChair().cloudID;
		else
		sharedChairID = null; 
		
		LOGGER.info("ABOUT TO SETUP");
		//setup the chair online, If the design already exists but we do not have right to update it then create as a new design
		sharedChairID = setupOnline(sharedChairID);	

		LOGGER.info("Current ID: " + sharedChairID);

		
		//Could not setup the chair
		if (sharedChairID.startsWith("ERROR")) {
			return;
		}
		
		
		//set the new cloudID
		GLOBAL.sketchChairs.getCurChair().cloudID = sharedChairID;
		
		
		LOGGER.info("Uploading Model:");
		//Upload the design file
		if(uploadModel(sharedChairID).startsWith("ERROR")){
			LOGGER.info("ERROR Uploading Model:");
			return;
		}
		

		LOGGER.info("Uploading Screenshot:");
		//upload a screenshot of the chair, this takes a bit of bandwidth 
		if(uploadScreenshot(sharedChairID).startsWith("ERROR")){
			LOGGER.info("ERROR Uploading Screenshot:");
			return;
		}
		
		LOGGER.info("Uploading Pattern: 1:12");
		//upload the pattern, do we need to do this all the time? takes a long time to compute
		if(uploadPattern(sharedChairID, 0.08333333333333f,1.0f,197,210,false,false,false,true).startsWith("ERROR")){
			LOGGER.info("ERROR Uploading Pattern:");
			return;
		}
		
		
		LOGGER.info("Uploading Pattern: 1:9 0.15 paper");
		//upload the pattern, do we need to do this all the time? takes a long time to compute
		if(uploadPattern(sharedChairID, 0.11111111111111f,0.15f,197,210,false,true,false,true).startsWith("ERROR")){
			LOGGER.info("ERROR Uploading Pattern:");
			return;
		}
		
		LOGGER.info("Uploading Pattern: 1:1 12mm ply");
		//upload the pattern, do we need to do this all the time? takes a long time to compute
		if(uploadPattern(sharedChairID, 1f,12.0f,1200,2100,false,false,true,true).startsWith("ERROR")){
			LOGGER.info("ERROR Uploading Pattern:");
			return;
		}
		
		
		
		
	
			
		
		LOGGER.info("FINISHED: shared online :) ");

			//result = GLOBAL.cloud.postData("uploadFile", sharedChairname, "chair.xml", root.toXML().getBytes() );
			//System.out.println(result);
			if (!SETTINGS.WEB_MODE) {
				GLOBAL.applet
						.link("http://www.SketchChair.cc/design/"+sharedChairID+"/edit");
			}

			if (SETTINGS.WEB_MODE) {
				// not compatible in JRE 1.5 

				/*
				if( java.awt.Desktop.isDesktopSupported() )
				{
				java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
				if( desktop.isSupported( java.awt.Desktop.Action.BROWSE ) )
				{
				try
				{
				    java.net.URI uri = new java.net.URI( "http://www.SketchChair.com/viewChair.php?id=" +sharedChairID );
				    desktop.browse( uri );
				}
				catch( Exception ex )
				{
				    java.io.StringWriter sw = new java.io.StringWriter();
				    java.io.PrintWriter pw = new java.io.PrintWriter( sw );
				    ex.printStackTrace( pw );
				   // log.error( "Error " + sw.toString() );
				}
				}
				
				}
				*/
			}	
	}
	
	public void loginReturn(GUIEvent e) {
		
		
		String encrypted = null;
		
		
		MCrypt mcrypt = new MCrypt();
		/* Encrypt */
		try {
			 encrypted = MCrypt.bytesToHex( mcrypt.encrypt(loginPassword.getText()) );
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		/* Decrypt */
		//String decrypted = new String( mcrypt.decrypt( encrypted ) );
		 
		 
String[][] args = new String[4][2];
args[0][0] = "username";
args[0][1]	= loginUsername.getText();

args[1][0] = "password";
args[1][1]	= encrypted;

args[2][0] = "fromapp";
args[2][1]	= "true";

args[3][0] = "encrypted";
args[3][1]	= "true";

String returned = GLOBAL.cloudHook.postAction("login",args);


if(returned.startsWith("TRUE")){
	GLOBAL.username = loginUsername.getText();
	GLOBAL.password = loginPassword.getText();
	GLOBAL.authenticated = true;
	GLOBAL.sessionID = returned.substring(4);
	
	loginWindow.close(null);
	ShareChairOnline(null);
	LOGGER.debug("returned:"+GLOBAL.sessionID);

}else{
	GLOBAL.username = null;
	GLOBAL.password = null;
	GLOBAL.authenticated = false;
	GLOBAL.sessionID = null;
	loginLabel.setText(returned);
}

	}	 
	 
	 

	
	
	void loginBox(){
		
		float windowWidth = 200;
		float windowHeight = 150;
		
 loginWindow = new GUIWindow(0f,0f,windowWidth,windowHeight,GLOBAL.gui);
loginWindow.setLightboxed(true);
loginWindow.centre();
GLOBAL.gui.add(loginWindow);

float posX = 100;
float posY = 100;
float spacingY = 80;

GUILabel tittle  = new GUILabel(20,10,"Login",GLOBAL.gui);

loginWindow.add(tittle);

loginUsername = new GUITextfield(20, 40, 100,
		15, GLOBAL.gui);
loginUsername.setText("");
loginUsername.setLabel(Localization.getString("username"));
loginUsername.setFocus(true);
loginUsername.isTexfieldActive = true;
loginUsername.isActive = true;
loginUsername.wasClicked= true;
loginWindow.add(loginUsername);


loginPassword = new GUITextfield(20, 60, 100,
		15, GLOBAL.gui);
loginPassword.setText("");
loginPassword.setLabel(Localization.getString("password"));
loginPassword.setPasswordMode(true);
loginWindow.add(loginPassword);


loginLabel = new GUILabel(20,80,"",GLOBAL.gui);
loginWindow.add(loginLabel);


GUIButton button;
button = new GUIButton(20,100,60,20,"login", GLOBAL.gui);
//button.addActionListener(loginWindow, "close", UITools.LEG_TOOL);
button.addActionListener(this, "loginReturn", UITools.LEG_TOOL);
loginWindow.add(button);

button = new GUIButton(100,100,60,20,"cancel", GLOBAL.gui);
button.addActionListener(loginWindow, "close", UITools.LEG_TOOL);
//button.addActionListener(this, "loginReturn", UITools.LEG_TOOL);
loginWindow.add(button);
		
	}
	
	
	String uploadModel(String sharedChairID){
		String result = "";
		GLOBAL.sketchChairs.getCurChair().cloudID = sharedChairID;
		
		
		Element root = new Element("SketchChairDoc");
		root.addAttribute(new Attribute("version", String
				.valueOf(SETTINGS.version)));

		root.appendChild(GLOBAL.sketchChairs.getCurChair().toXML());
		OutputStream outXML = new ByteArrayOutputStream();
		
		
		String[][] args = new String[4][2];
		args[0][0] = "sessionID";
		args[0][1] = GLOBAL.sessionID;
		args[1][0] = "folder";
		args[1][1] = "sc."+sharedChairID+"/skchr";
		args[2][0] = "name";
		args[2][1] = "df.skchr";
		args[3][0] = "designID";
		args[3][1] = sharedChairID;
		
		return GLOBAL.cloudHook.postAction("uploadDesign",args,root.toXML().getBytes());
	}
	
	
	
	String uploadPattern(String sharedChairID, float scale, float materialWidth,  float pageWidth, float pageHeight, boolean autoPack, boolean isPaperCut, boolean addDogBones, boolean addLabels){
		LOGGER.info("uploading ....");

		SketchChair skchChair = GLOBAL.sketchChairs.getCurChair();
		String result = "";
		skchChair.cloudID = sharedChairID;
		
		//This is is all very messy can we tidy this away into a single function?
		
		
		
		//save the current settings
		float savedScale = SETTINGS.scale;
		float savedPackScale = GLOBAL.shapePack.scale;
		float savedMaterialThikness = SETTINGS.materialThickness;
				
		float savedMaterialWidth = GLOBAL.shapePack.materialWidth;
		float savedMaterialHeight = GLOBAL.shapePack.materialHeight;
		boolean savedAutoPackPieces = GLOBAL.shapePack.autoPackPieces;
		boolean savedAddGuideDivets = GLOBAL.shapePack.add_guide_divets;
		boolean savedAddDogBones = GLOBAL.shapePack.addDogbones;
		boolean sacedAddLabels = GLOBAL.shapePack.addLabels;
		
		
		
		
		SETTINGS.scale = scale;
		SETTINGS.pixels_per_mm = SETTINGS.pixels_per_mm_base
				/ SETTINGS.scale;

		GLOBAL.shapePack.scale = SETTINGS.scale / .1f;

		skchChair.build();

		SETTINGS.materialThickness = materialWidth;

		skchChair.setPlaneWidth(materialWidth);
		GLOBAL.shapePack.materialWidth = pageWidth;
		GLOBAL.shapePack.materialHeight = pageHeight;
		GLOBAL.shapePack.autoPackPieces = autoPack;
		GLOBAL.shapePack.add_guide_divets = isPaperCut;
		GLOBAL.shapePack.addDogbones = addDogBones;
		GLOBAL.shapePack.addLabels = addLabels;
		
		skchChair.addToShapePack();
		LOGGER.info("addedToShapePack");

		Element root = GLOBAL.shapePack.getSVG(GLOBAL.applet);
				//root.addAttribute(new Attribute("version", String
			//	.valueOf(SETTINGS.version)));

		LOGGER.info("got SVG");

		
		
		OutputStream outXML = new ByteArrayOutputStream();
		
		
		String[][] args = new String[6][2];
		args[0][0] = "sessionID";
		args[0][1] = GLOBAL.sessionID;
		args[1][0] = "folder";
		args[1][1] = "sc."+sharedChairID+"/patterns";
		args[2][0] = "name";
		args[2][1] = "df.svg";
		args[3][0] = "designID";
		args[3][1] = sharedChairID;
		args[4][0] = "scale";
		args[4][1] = scale+"";
		args[5][0] = "materialWidth";
		args[5][1] = materialWidth+"";
		
		//save the current settings
		SETTINGS.scale = savedScale;
		GLOBAL.shapePack.scale = savedPackScale;
		SETTINGS.materialThickness = savedMaterialThikness;
		skchChair.setPlaneWidth(savedMaterialWidth);
		
		GLOBAL.shapePack.materialWidth = savedMaterialWidth;
		GLOBAL.shapePack.materialHeight = savedMaterialHeight;
		GLOBAL.shapePack.autoPackPieces = savedAutoPackPieces;
		GLOBAL.shapePack.add_guide_divets = savedAddGuideDivets;
		GLOBAL.shapePack.addDogbones = savedAddDogBones;
		GLOBAL.shapePack.addLabels = sacedAddLabels;
		
		
		
		
		LOGGER.info("uploading now");
		return GLOBAL.cloudHook.postAction("uploadPattern",args,root.toXML().getBytes());
		
		
		
	
		
		
	}
	
	
	
	
	String uploadScreenshot(String sharedChairID){
		//upload model

	//to do tidy
		GLOBAL.sketchChairs.getCurChair().build();
		SETTINGS.materialThickness = 1.2f;
		GLOBAL.sketchChairs.getCurChair().setPlaneWidth(1.2f);	
		GLOBAL.sketchChairs.getCurChair().build();
		PImage saveImg = GLOBAL.sketchChairs.getCurChair()
				.renderDiagram(600, 600, (float) (-Math.PI / 8),
						(float) (-Math.PI / 4),false);
		
		GLOBAL.applet.smooth(8);
		//saveImg.resize((int)(saveImg.width/2.0f), (int)(saveImg.height/2.0f));
		
		if (saveImg.width > 0 && saveImg.height > 0) {
			LOGGER.debug("saveImg");
			String[][] args = new String[4][2];
			args[0][0] = "sessionID";
			args[0][1] = GLOBAL.sessionID;
			args[1][0] = "folder";
			args[1][1] = "sc."+sharedChairID+"/images";
			args[2][0] = "name";
			args[2][1] = "screenshot.png";
			args[3][0] = "designID";
			args[3][1] = sharedChairID;
			
			return GLOBAL.cloudHook.postAction("uploadDesign", args,CloudHook.makeImage.getPNG(saveImg));
		}
		return null;		
	}
	
	String uploadPattern(String sharedChairID){

		String[][] sessionIDargs = new String[1][2];
		sessionIDargs[0][0] = "sessionID";
		sessionIDargs[0][1]	= GLOBAL.sessionID;
		
		if (GLOBAL.sketchChairs.getCurChair() != null) {

			GLOBAL.sketchChairs.getCurChair().addToShapePack();
			LOGGER.debug("PDF");		

			GLOBAL.shapePack.build();
			LOGGER.debug("built");		

			ByteArrayOutputStream stream = GLOBAL.shapePack
					.getPDFBuffered(GLOBAL.applet);

			
			String[][] args = new String[4][2];
			args[0][0] = "sessionID";
			args[0][1] = GLOBAL.sessionID;
			args[1][0] = "folder";
			args[1][1] = "sc."+sharedChairID+"/patterns";
			args[2][0] = "name";
			args[2][1] = "p.pdf";
			args[3][0] = "designID";
			args[3][1] = sharedChairID;
			
			return GLOBAL.cloudHook.postAction("uploadPattern",args, stream.toByteArray());

		}
		return null;
		
	}
	
	
	String setupOnline(String designID){
		String[][] sessionIDargs; 
		if(designID == null){
		sessionIDargs = new String[1][2];
		sessionIDargs[0][0] = "sessionID";
		sessionIDargs[0][1]	= GLOBAL.sessionID;
		}else{
			sessionIDargs = new String[2][2];
			sessionIDargs[0][0] = "sessionID";
			sessionIDargs[0][1]	= GLOBAL.sessionID;
			sessionIDargs[1][0] = "designID";
			sessionIDargs[1][1]	= designID;
		}
			
		
		return GLOBAL.cloudHook.postAction("CheckInID",sessionIDargs);
			
	
	}
	}

