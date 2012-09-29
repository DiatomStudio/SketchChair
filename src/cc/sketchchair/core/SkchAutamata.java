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

import java.awt.FileDialog;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ModalGUI.GUIButton;

/** 
 * A class used for the developer to perform automated actions on a folder of chair designs. 
 * For example to export thumbnails for each design in a folder. Useful to test compatibility of new features with old design files.
 * @author gregsaul
 *
 */
public class SkchAutamata {

	String folder;
	public boolean readyToStart = false;

	List<String> filesToProcess = new ArrayList<String>();
	int currentPos = 0;
	
	String selectFolder() {

		System.setProperty("apple.awt.fileDialogForDirectories", "true");
		FileDialog fd = new FileDialog(GLOBAL.applet.frame, "open",
				FileDialog.LOAD);
		String currentDir = new File(".").getAbsolutePath();
		fd.setLocation(50, 50);
		fd.pack();

		fd.show();
		System.setProperty("apple.awt.fileDialogForDirectories", "false");

		if (fd.getDirectory() != null) {
			return fd.getDirectory() + fd.getFile();
		} else {
			return "";
		}

	}

	void start() {
		this.folder = selectFolder();
		this.readyToStart  = true;
		if (this.folder != "") {
			this.loadChairs();
		}

		
	}

	void loadChairs() {

		
		filesToProcess.clear();
		currentPos = 0;
		
		
		File fp = new File(folder);

		if (!fp.isDirectory())
			return;

		GUIButton button = null;

		String dirContence[] = fp.list();

		for (int i = 0; i < dirContence.length; i++) {

			String path = folder + "/" + dirContence[i];

			int dot = path.lastIndexOf(".");
			String ext = path.substring(dot + 1);

			if (ext.equals("skchr")) {
				filesToProcess.add(path);
			}
		}

	}

	
	boolean hasChairToProcess(){
		return currentPos < filesToProcess.size();
	}
	void processNext(){
		if(currentPos < filesToProcess.size()){
			loadChair(filesToProcess.get(currentPos));
			currentPos++;
			
		}
		
	}
	void loadChair(String path) {

		GLOBAL.sketchChairs.killAll();
		GLOBAL.uiTools.load(path);
		if (GLOBAL.sketchChairs.getCurChair() != null) {
			runAction(GLOBAL.sketchChairs.getCurChair(), path);
		}

	}
	
	
	

	void runAction(SketchChair skchChair, String location) {
		
		
		LOGGER.info("SkchAutomata Processing: " +location);
		int slash = location.lastIndexOf("/");
		
		skchChair.setPlaneWidth(1.5f);
		skchChair.build();
		//GLOBAL.uiTools.saveChairToFileAuto(null);
		SketchChairCloudhook.getInstance().ShareChairOnline(null);
		/*
		String baseFolder = location.substring(0, slash+1);
		String fileName = location.substring(slash+1, location.length());

	//	GLOBAL.uiTools.camJumpIso(null);
		int previewWidth = GLOBAL.applet.width;
		int previewHeight = GLOBAL.applet.width;
		//GLOBAL.cropExportToScreen = true;
		String saveLocation = baseFolder+"previews/"+ fileName+ ".render.profile.png";
		//LOGGER.info("baseFolder "+baseFolder + " fileName " + fileName);
		//LOGGER.info("loc"+saveLocation);
		//skchChair.renderDiagram(previewWidth,previewHeight,true).save(saveLocation);
		GLOBAL.pngPreviewSaveLocation = saveLocation;
		GLOBAL.exportPreviewPNG = true;
		*/
		
		
		
		
	}

}
