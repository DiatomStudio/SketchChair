package cc.sketchchair.core;

import java.awt.event.MouseEvent;

public class MouseEventSK{

	public static final int PRESS = MouseEvent.MOUSE_PRESSED;
	public static final int RELEASE = MouseEvent.MOUSE_RELEASED;
	public static final int CLICK = MouseEvent.MOUSE_CLICKED;
	public static final int MOUSE_PRESSED = MouseEvent.MOUSE_PRESSED;
	public static final int MOUSE_RELEASED = MouseEvent.MOUSE_RELEASED;
	public int action = 0;
	public int clickCount = 0;
	
	public MouseEventSK(MouseEvent e) {
		action = e.getID();
		
		clickCount = e.getClickCount();
	}

	public MouseEventSK(processing.event.MouseEvent e) {
		action = ((MouseEvent)(e.getNative())).getID();
		
		clickCount = e.getClickCount();
		}

	public int getAction() {
		// TODO Auto-generated method stub
		return action;
	}

	public int getClickCount() {
		// TODO Auto-generated method stub
		return clickCount;
	}

	public int getID() {
		// TODO Auto-generated method stub
		return action;
	}

}
