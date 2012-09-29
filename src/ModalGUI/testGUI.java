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
package ModalGUI;

import processing.core.PApplet;



public class testGUI extends PApplet{
ModalGUI gui ;
public float sliderVal = 0;
public boolean bool = false;

	public void setup(){
		size(300,600,OPENGL);
		smooth();
		
		gui = new ModalGUI(this);
		gui.myFontMedium = loadFont("TrebuchetMS-12.vlw");
		
		
		float xPos = 10;
		float yPos = 30;
		//Slider
		GUISlider slider = new GUISlider(xPos, yPos, 100, 0, 10,gui);
		slider.addActionListener(this, "changeVal");
		slider.addActionListener(this, "sliderVal");
		slider.setLabel("change val");
		
		gui.add(slider);
		
		yPos += 35;
		GUIButton button = new GUIButton(xPos, yPos, 60, 20, "button",gui);
		gui.add(button);
		
		yPos += 35;
		button = new GUIButton(xPos, yPos, "GUI_SELECT_TOOL_UP.png","GUI_SELECT_TOOL_DOWN.png",gui);
		gui.add(button);
		
		button = new GUIButton(xPos + 100, yPos, "GUI_SELECT_TOOL_UP.png",null);
		button.setLabel("generated");
		gui.add(button);
		
		yPos += 45;
		
		GUIToggle toggle = new GUIToggle(xPos + 100, yPos,75f,75f, "GUI_SELECT_TOOL_UP.png","",gui);
		toggle.setLabel("generated");
		gui.add(toggle);
		
		yPos += 45;
		
		GUIToggleSlide sliderToggle = new GUIToggleSlide(xPos+25,yPos,"on","off",gui);
		sliderToggle.addActionListener(this, "bool");
		gui.add(sliderToggle);
		yPos += 25;
		
		 toggle = new GUIToggle(xPos, yPos, 10,10);
		toggle.setLabel("toggle");
		gui.add(toggle);
		

		yPos += 25;
		GUITextfield textfield = new GUITextfield(xPos, yPos, 60, 15,gui);
		gui.add(textfield);
		
		yPos += 25;
		GUINumberfield numberfield = new GUINumberfield(xPos, yPos, 60, 15,gui);
		gui.add(numberfield);
		
		
		yPos += 25;
		 /*
		GUIPanel panel = new GUIPanel(xPos, yPos, 100, 100,gui);
		gui.add(panel);
		
		
		 slider = new GUISlider(5, 15, 90, 0, 10,gui);
		 panel.add(slider);
		
		  slider = new GUISlider(10, 35, 60, 0, 10,gui);
		  slider.orientation = GUISlider.VERTICAL;
		 panel.add(slider);
		 
			 panel = new GUIPanel(xPos + 110, yPos, 100, 100,true,this,gui);
			gui.add(panel);
			 button = new GUIButton(0, 30, 10, 10, "1",gui);
			 panel.placeComponentLeft(button);
			 button = new GUIButton(0, 30, 10, 10, "2",gui);
			 panel.placeComponentLeft(button);
			 button = new GUIButton(0, 30, 10, 10, "3",gui);
			 panel.placeComponentLeft(button);
			 button = new GUIButton(0, 30, 10, 10, "4",gui);
			 panel.placeComponentLeft(button);
			 button = new GUIButton(0, 30, 10, 10, "5",gui);
			 panel.placeComponentLeft(button);
			 button = new GUIButton(0, 30, 10, 10, "6",gui);
			 panel.placeComponentLeft(button);
			 button = new GUIButton(0, 30, 10, 10, "7",gui);
			 panel.placeComponentLeft(button);
			 button = new GUIButton(0, 30, 10, 10, "8",gui);
			 panel.placeComponentLeft(button);
			 button = new GUIButton(0, 30, 10, 10, "9",gui);
			 panel.placeComponentLeft(button);
			 button = new GUIButton(0, 30, 10, 10, "10",gui);
			 panel.placeComponentLeft(button);
			 */
			 yPos+=130;
			 
			 GUIComponentSet radioSet = new GUIComponentSet();
			 GUIRadioBox radioBox = new GUIRadioBox(xPos, yPos,10,radioSet,gui);
			 gui.add(radioBox);
			 yPos+=15;
			  radioBox = new GUIRadioBox(xPos, yPos,10,radioSet,gui);
			 gui.add(radioBox);
			 yPos+=15;
			  radioBox = new GUIRadioBox(xPos, yPos,10,radioSet,gui);
			 gui.add(radioBox);
			 yPos+=15;
			 
			 
			 yPos-=45;
			 xPos += 50;
			 
			 GUIComponentSet toggleSet = new GUIComponentSet();
			 
			 toggle = new GUIToggle(xPos, yPos, 10,10,toggleSet,gui);
			 toggle.setFillColour(this.color(255,200,200));
			 gui.add(toggle);
			 yPos+=15;
			 
			 toggle = new GUIToggle(xPos, yPos, 10,10,toggleSet,gui);
			 toggle.setFillColour(this.color(200,255,200));
			 gui.add(toggle);
			 yPos+=15;
			 
			 toggle = new GUIToggle(xPos, yPos, 10,10,toggleSet,gui);
			 toggle.setFillColour(this.color(200,200,255));
			 gui.add(toggle);
			 yPos+=15;
			 
			 GUILabel label = new GUILabel(xPos + 50,yPos - 20, "label",gui);
			 gui.add(label);
		
			 xPos = 10;
			 yPos += 20;
			 GUIPanelTabbed tabbedPanel = new GUIPanelTabbed(xPos, yPos, 250, 100,gui);
			 gui.add(tabbedPanel);
				
			 GUIPanel panel = new GUIPanel(0, 0, 250, 100,gui);
			 tabbedPanel.addTabbedPanel(panel, "tools",gui);

			 
			 button = new GUIButton(0, 0, 60, 20, "panel 1",gui);
			 panel.placeComponentLeft(button);
				
				
				
			 
			 panel = new GUIPanel(0, 0, 250, 100,gui);
			 tabbedPanel.addTabbedPanel(panel, "physics",gui);

			 button = new GUIButton(0, 0, 60, 20, "panel 2",gui);
			 panel.placeComponentLeft(button);
			 
			GUIToggleSlide sliderToggle2 = new GUIToggleSlide(110,1110,"on","off",gui);
			 panel.placeComponentLeft(sliderToggle2);
			 
				GUISlider slider4 = new GUISlider(110,1110,100,1,100,gui);
				panel.placeComponentLeft(slider4);

				 panel = new GUIPanel(0, 0, 250, 100,gui);
				 tabbedPanel.addTabbedPanel(panel, "text",gui);
				
				GUITextfield textfield2 = new GUITextfield(xPos, yPos, 60, 15,gui);
				panel.placeComponent(textfield2);
				
				panel = new GUIPanel(0, 0, 250, 100,gui);
				 tabbedPanel.addTabbedPanel(panel, "panels",gui);
				
				 GUIPanel panel2 = new GUIPanel(0, 0, 100, 100,gui);
				 panel2.add(new GUISlider(0,10,100,1,100,gui));
				 panel2.add(new GUISlider(0,40,100,1,100,gui));
				 panel.add(panel2);
				 
				 
				 
				 yPos+= 120;
				 GUIWindow window = new GUIWindow(100, (float)yPos, 100, 100,gui);
				 gui.add(window);
				 panel = new GUIPanel(10, 10, window.getWidth()-20, window.getHeight()-20,true,this,gui);
				 window.add(panel);
					 button = new GUIButton(0, 30, 10, 10, "1",gui);
					 panel.placeComponentLeft(button);
					 button = new GUIButton(0, 30, 10, 10, "2",gui);
					 panel.placeComponentLeft(button);
					 button = new GUIButton(0, 30, 10, 10, "3",gui);
					 panel.placeComponentLeft(button);
					 button = new GUIButton(0, 30, 10, 10, "4",gui);
					 panel.placeComponentLeft(button);
					 button = new GUIButton(0, 30, 10, 10, "5",gui);
					 panel.placeComponentLeft(button);
					 button = new GUIButton(0, 30, 10, 10, "6",gui);
					 panel.placeComponentLeft(button);
					 button = new GUIButton(0, 30, 10, 10, "7",gui);
					 panel.placeComponentLeft(button);
					 button = new GUIButton(0, 30, 10, 10, "8",gui);
					 panel.placeComponentLeft(button);
					 button = new GUIButton(0, 30, 10, 10, "9",gui);
					 panel.placeComponentLeft(button);
					 button = new GUIButton(0, 30, 10, 10, "10",gui);
					 panel.placeComponentLeft(button);
				
					 
				 

	}
	
	
	public void draw(){
		background(255);
		gui.update();
		gui.render(g);
	}
	
	 public void changeVal(GUIEvent e){
		System.out.println(e.val);
	}
	
	
}
