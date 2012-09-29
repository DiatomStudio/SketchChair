/*
 * Copyright (c) 2004-2010, P. Simon Tuffs (simon@simontuffs.com)
 * All rights reserved.
 *
 * See the full license at http://one-jar.sourceforge.net/one-jar-license.html
 * This license is also included in the distributions of this software
 * under doc/one-jar-license.txt
 */
package cc.sketchchair.main;

import java.util.Arrays;

/**
 * A main program class, not used delete this? I think I added this because fatJar's required it at one stage. 
 * @author gregsaul
 *
 */
public class SketchChairMain {

	public static void main(String args[]) {
		if (args == null)
			args = new String[0];
		new SketchChairMain().run();
	}

	// Bring up the application: only expected to exit when user interaction
	// indicates so.
	public void run() {
		// Implement the functionality of the application. 
	}

}
