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

/**
 * controlP5 is a processing and java library for creating simple control GUIs.
 *
 *  2007 by Andreas Schlegel
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 * @author Andreas Schlegel (http://www.sojamo.de)
 *
 */

import java.util.ArrayList;
import java.util.Date;

import processing.core.PConstants;
import processing.core.PGraphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import cc.sketchchair.core.LOGGER;

/**
 * description for singleline textfield. create a texfield with<br />
 * <br />
 * controlP5.addTextfield(theName,theX,theY,theWidth,theHeight);
 *
 * the Textfield implementation for ControlP5 tries its best to imitate the
 * usage and behavior of a terminal, the command line.
 *
 * @example ControlP5textfield
 * @nosuperclasses Controller
 * @related Controller
 */
public class GUITextfield extends GUIComponent {

	/*
	 * TODO needs a lot of work! has gone through massive amounts of little
	 * changes and adjustments. implement new fonts, current one is too small.
	 * make the text go to the left when cursor goes beyond right border. make
	 * textfield work for controlWindow
	 *
	 */

	protected ArrayList myTextList = new ArrayList();

	int myIndex = 1;

	int myPosition = 0;

	StringBuffer myTextline = new StringBuffer();

	public boolean isTexfieldActive = false;

	private boolean isPasswordMode = false;

	protected boolean isAutoClear = true;

	protected boolean isKeepFocus = false;

	protected String _myStringValue;
	protected String myPasswordTextline = "";

	public boolean isActive = false;

	public GUITextfield(float x, float y, float w, float h, ModalGUI c) {
		//this(x,y,w,h,applet);
		this.setController(c);
		this.setPos(x, y);
		this.setSize(w, h);

	}

	/**
	 * @invisible
	 * @param theControllerProperties
	 *            ControllerProperties
	 * @return 
	 */
	public GUITextfield(String theName, String theDefaultValue, int theX,
			int theY, int theWidth, int theHeight, ModalGUI c) {

	}

	/**
	 * @invisible
	 */
	protected void adjust() {
		myPosition = myTextline.length();
		if (myPosition < 0) {
			myPosition = 0;
		}
	}

	protected void checkClear() {
		if (isAutoClear) {
			myTextline = new StringBuffer();
			myIndex = myTextList.size();
			myPosition = 0;
			updateField();
		}
	}

	/**
	 * clear the current content of the textfield.
	 */
	public void clear() {
		myTextline = new StringBuffer();
		myIndex = myTextList.size();
		myPosition = 0;
		updateField();
	}

	/**
	 * get the current text of the textfield.
	 *
	 * @return String
	 */
	public String getText() {
		return myTextline.toString();
	}

	/**
	 * returns a string array that lists all text lines that have been confirmed
	 * with a return.
	 *
	 * @return
	 */
	public String[] getTextList() {
		String[] s = new String[myTextList.size()];
		myTextList.toArray(s);
		return s;
	}

	/**
	 * @invisible
	 * @param theElement
	 *            ControlP5XMLElement
	 */
	/*
	public void addToXMLElement(
	       // ControlP5XMLElement theElement) {
	       // theElement.setAttribute("type", "textfield");
	       // theElement.setAttribute("value", "" + stringValue());
	}
	*/

	@Override
	public boolean hasFocus() {
		if (this.hasFocus || this.isActive)
			return true;
		else
			return false;
	}

	/**
	 * returns the current state of the autoClear flag.
	 *
	 * @return
	 */
	public boolean isAutoClear() {
		return isAutoClear;
	}

	/**
	 * check if the textfield is active and in focus.
	 *
	 * @return boolean
	 */
	public boolean isFocus() {
		return isTexfieldActive;
	}

	/**
	 * set the textfield's focus to true or false.
	 *
	 * @param theFlag
	 *            boolean
	 */
	/*
	public void setFocus(
	        boolean theFlag) {
		
		this.hasFocus = theFlag;
		
	        if (theFlag == true) {
	                mousePressed();
	        } else {
	                mouseReleasedOutside();
	        }
	}
	*/
	/**
	 * use true as parameter to force the textfield to stay in focus. to go back
	 * to normal focus behavior, use false.
	 *
	 * @param theFlag
	 */
	public void keepFocus(boolean theFlag) {
		isKeepFocus = theFlag;
		if (isKeepFocus) {
			this.setFocus(true);
		}
	}

	/**
	 * flip throught the texfield history with cursor keys UP and DOWN. go back
	 * and forth with cursor keys LEFT and RIGHT.
	 *
	 * @invisible
	 */
	@Override
	public void keyEvent(KeyEvent theKeyEvent) {


		if (isTexfieldActive && isActive
				&& theKeyEvent.getID() == KeyEvent.KEY_PRESSED) {

			if (theKeyEvent.getKeyCode() == KeyEvent.VK_UP) {
				if (myTextList.size() > 0 && myIndex > 0) {
					myIndex--;
					myTextline = new StringBuffer(
							(String) myTextList.get(myIndex));
					adjust();
				}
			} else if (theKeyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
				myIndex++;
				if (myIndex >= myTextList.size()) {
					myIndex = myTextList.size();
					myTextline = new StringBuffer();
				} else {
					myTextline = new StringBuffer(
							(String) myTextList.get(myIndex));
				}
				adjust();
			} else if (theKeyEvent.getKeyCode() == KeyEvent.VK_LEFT) {
				if (myPosition > 0) {
					myPosition--;
				}
			} else if (theKeyEvent.getKeyCode() == KeyEvent.VK_RIGHT) {
				if (myPosition < myTextline.length()) {
					myPosition++;
				}
			} else if (theKeyEvent.getKeyCode() == KeyEvent.VK_DELETE
					|| theKeyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
				if (myTextline.length() > 0) {
					if (myPosition > 0) {
						myTextline.deleteCharAt(myPosition - 1);
						myPosition--;
					}
				}
			} else if (theKeyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
				submit();
			} else if (theKeyEvent.getKeyCode() != KeyEvent.VK_SHIFT
					&& theKeyEvent.getKeyCode() != KeyEvent.VK_ALT
					&& theKeyEvent.getKeyCode() != KeyEvent.VK_CONTROL) {

				if (theKeyEvent.getKeyCode() >= 31
						&& theKeyEvent.getKeyCode() <= 127) {
					myTextline.insert(myPosition, theKeyEvent.getKeyChar());
					myPosition++;
				}
			}
			updateField();
		}
	}

	/**
	 * @invisible
	 * @param theElement
	 *            ControlP5XMLElement
	 */
	/*
	public void addToXMLElement(
	       // ControlP5XMLElement theElement) {
	       // theElement.setAttribute("type", "textfield");
	       // theElement.setAttribute("value", "" + stringValue());
	}
	*/

	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	}
	@Override
	public void mouseEvent(MouseEvent e) {
		//

		
		if (e.getID() == MouseEvent.MOUSE_PRESSED) {
			if (isMouseOver())
				wasClicked = true;
		} else if (e.getID() == MouseEvent.MOUSE_RELEASED && wasClicked
				&& isMouseOver()) {
			//  fireEventNotification(this, "Clicked");
			this.isActive = true;
			this.isTexfieldActive = true;
			this.setFocus(true);
		}

		if (e.getID() == MouseEvent.MOUSE_RELEASED && wasClicked
				&& !isMouseOver()) {
			this.isActive = false;
			this.isTexfieldActive = false;
			this.setFocus(false);
		}

	}

	/**
	 * click the texfield to activate.
	 *
	 * @invisible
	 *
	 */
	protected void mousePressed() {
		this.setFocus(true);
		//System.out.println("FOCUS");
		isTexfieldActive = isActive = true;
	}

	/**
	 *
	 */
	protected void mouseReleasedOutside() {

		if (isKeepFocus == false) {
			//System.out.println("UNFOCUS");
			this.setFocus(false);
			isTexfieldActive = isActive = false;
		}
	}

	/**
	 * @invisible
	 * @param theApplet
	 *            PApplet
	 */
	@Override
	public void render(PGraphics g) {

		if (!this.visible)
			return;

		if (isTexfieldActive && isActive) {
			g.stroke(g.color(0, 0, 0));
		} else {
			g.stroke(g.color(100, 100, 100));
		}
		g.strokeWeight(1);
		g.fill(255);
		g.pushMatrix();
		g.translate(this.getX(), this.getY());
		g.rect(0, 1, this.getWidth(), this.getHeight()+1);
		g.fill(0);

		if (this.getText() != null) {
			g.textAlign = PConstants.LEFT;
			g.textAlignY = PConstants.CENTER;

			String activeMark = "";

			if (this.hasFocus() && new Date().getTime() % 800 > 400)
				activeMark = "|";
			if (this.controller.myFontMedium != null){
				g.textFont(this.controller.myFontMedium);

				if(isPasswordMode)
					this.controller.applet.text(myPasswordTextline + activeMark, 5, 8,
							this.getWidth(), this.getHeight());
				else{
				this.controller.applet.text(this.getText() + activeMark, 5, 8);
				//this.controller.applet.text("hi"+this.getText(),0,0);
				}
			}

			g.noStroke();
			//_myValueLabel.draw(theApplet, 4, 7);
			//_myCaptionLabel.draw(theApplet, 0, height + 4);

			//      System.out.println(this.getText());

		}

		g.popMatrix();
		renderLabel(g);
		

	}

	/**
	 * use setAutoClear(false) to not clear the content of the textfield after
	 * confirming with return.
	 *
	 * @param theFlag
	 */
	public void setAutoClear(boolean theFlag) {
		isAutoClear = theFlag;
	}

	/**
	 * set the mode of the textfield to password mode, each character is shown
	 * as a "*" like e.g. in online password forms.
	 *
	 * @param theFlag
	 *            boolean
	 */
	public void setPasswordMode(boolean theFlag) {
		isPasswordMode = theFlag;
	}

	/**
	 * setText does set the text of a textfield, but will not broadcast its
	 * value. use setText to force the textfield to change its text. you can
	 * call setText any time, nevertheless when autoClear is set to true (which
	 * is the default), it will NOT work when called from within controlEvent or
	 * within a method that has been identified by ControlP5 to forward messages
	 * to, when return has been pressed to confirm a textfield.<br /> use
	 * setAutoClear(false) to enable setText to be executed for the above case.
	 * use yourTextfield.isAutoClear() to check if autoClear is true or false.
	 * <br />
	 * setText replaces the current/latest content of a textfield but does NOT
	 * overwrite the content. when scrolling through the list of textlines (use
	 * key up and down), the previous content that has been replaced will be put
	 * back into place again - since it has not been confirmed with return.
	 *
	 * @param theValue
	 */
	public void setText(String theValue) {
		myTextline = new StringBuffer(theValue);
		// myPosition = myTextline.length() - 1;
		_myStringValue = theValue;
		myPosition = myTextline.length();
		// _myValueLabel.setWithCursorPosition(myTextline.toString(), myPosition);
	}

	@Override
	public void setup() {
		// TODO Auto-generated method stub

	}

	/*
	public Textfield(
	        ControlP5 theControlP5,
	        ControllerGroup theParent,
	        String theName,
	        String theDefaultValue,
	        int theX,
	        int theY,
	        int theWidth,
	        int theHeight) {
	        super(theControlP5, theParent, theName, theX, theY, theWidth, theHeight);
	        _myCaptionLabel = new Label(theName.toUpperCase(), color.colorLabel);
	        _myCaptionLabel.setFixedSize(false);
	        myBroadcastType = STRING;
	        _myValueLabel.setWidth(width - 10);
	        _myValueLabel.setHeight(15);
	        _myValueLabel.set(">");
	        _myValueLabel.setColor(color.colorValue);
	        _myValueLabel.toUpperCase(false);
	        _myValueLabel.setFixedSize(true);
	        _myValueLabel.setFont(ControlP5.standard56);

	}
	*/
	@Override
	public
	void hide(){
		this.isActive = false;
		super.hide();
	}
	
	/**
	 * @invisible
	 * @param theValue
	 *            float
	 */
	public void setValue(float theValue) {
		setValue(theValue+"");
	}

	/**
	 * set the value of the textfield and will broadcast the new string value
	 * immediately. what is the difference between setValue and setText?
	 * setValue does broadcast the value that has been set, setText does not
	 * broadcast the value, but only updates the content of a textfield. for
	 * further information about how setText works, see the setText
	 * documentation.
	 *
	 * @param theValue
	 *            String
	 */
	public void setValue(String theValue) {
		myTextline = new StringBuffer(theValue);
		// myPosition = myTextline.length() - 1;
		_myStringValue = theValue;
		myPosition = myTextline.length();
		//_myValueLabel.setWithCursorPosition(myTextline.toString(), myPosition);
		//broadcast(myBroadcastType);
	}

	/**
	 * make the controller execute a return event. submit the current content of
	 * the texfield.
	 *
	 */
	public void submit() {
		if (myTextline.length() > 0) {
			myTextList.add(myTextline.toString());
			update();
			checkClear();
		}
	}

	/**
	 * @invisible
	 */
	public void update() {
		_myStringValue = myTextline.toString();
		setValue(_myStringValue);
	}

	protected void updateField() {
		if (isPasswordMode) {
			 myPasswordTextline = "";
			for (int i = 0; i < myTextline.length(); i++) {
				myPasswordTextline += "*";
			}
			// _myValueLabel.setWithCursorPosition(myPasswordTextline, myPosition);
		} else {
			int offset = 0;
			// int m = _myValueLabel.bitFontRenderer.getWidth(myTextline.toString(), _myValueLabel,myPosition);
			//offset = (m>_myValueLabel.width()) ? _myValueLabel.width() - m:0;                      
			// _myValueLabel.setWithCursorPosition(myTextline.toString(), myPosition,offset);
		}
	}
}
