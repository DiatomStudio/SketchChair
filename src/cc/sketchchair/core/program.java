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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import processing.core.PApplet;

/**
 * Used to start SketchChair in a new window.
 * @author gregsaul
 *
 */
public class program extends Frame {
	public static void main(String[] s) {
		new program().setVisible(true);
	}

	public program() {

		super("Embedded PApplet");

		setLayout(new BorderLayout());
		setSize(1200, 700); // setup and OPENGL window

		PApplet p5sketch = new main();
		add(p5sketch, BorderLayout.CENTER);
		
		/*
		PApplet p5UIFrame = new UIFrame();
		add(p5UIFrame,BorderLayout.SOUTH);
*/
		
		
		GLOBAL.frame = this;
		//p5UIFrame.init();
		p5sketch.init();
		show();
		//p5sketch.setup();

		// allow window and application to be closed
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

	}
}
