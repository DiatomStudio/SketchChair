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
		// Processing 4 with P3D: Map Processing's action constants to AWT constants
		// Processing uses: PRESS=1, RELEASE=2, CLICK=3, DRAG=4, MOVE=5, ENTER=6, EXIT=7, WHEEL=8
		// AWT uses: MOUSE_PRESSED=501, MOUSE_RELEASED=502, MOUSE_CLICKED=500
		int processingAction = e.getAction();

		if (processingAction == processing.event.MouseEvent.PRESS) {
			action = MouseEvent.MOUSE_PRESSED;
		} else if (processingAction == processing.event.MouseEvent.RELEASE) {
			action = MouseEvent.MOUSE_RELEASED;
		} else if (processingAction == processing.event.MouseEvent.CLICK) {
			action = MouseEvent.MOUSE_CLICKED;
		} else {
			// For other events (DRAG, MOVE, etc.), keep the Processing constant
			action = processingAction;
		}

		// Processing 4: getClickCount() → getCount()
		clickCount = e.getCount();
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
