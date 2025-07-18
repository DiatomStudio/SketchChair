package cc.sketchchair.core;

import java.awt.event.KeyEvent;

import javax.swing.plaf.basic.BasicSliderUI.ActionScroller;

public class KeyEventSK {

	public static final int PRESS = KeyEvent.KEY_PRESSED;

	public char c; 
	public int action;
	public int keyCode;
	public KeyEventSK(KeyEvent e) {
		c = e.getKeyChar();
		action = e.getID();
		keyCode = e.getKeyCode();
		
		
	}

	public KeyEventSK(processing.event.KeyEvent e) {
		c = e.getKey();
		keyCode = e.getKeyCode();
		action = e.getAction();
	}

	public int getKeyCode() {
		return keyCode;
	}

	public int getAction() {
		return action;
	}

	public char getKey() {
		return c;
	}

}
