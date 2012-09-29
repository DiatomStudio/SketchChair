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

/**
 * Custom exception handler. Used to restart app on crash.
 * @author gregsaul
 *
 */
class ExceptionHandler implements Thread.UncaughtExceptionHandler {
	public static void registerExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		System.setProperty("sun.awt.exception.handler",
				ExceptionHandler.class.getName());
	}

	public void handle(Throwable throwable) {
		try {
			throwable.printStackTrace();
			//System.out.println();
			System.exit(-1);
			GLOBAL.applet.setup();

		} catch (Throwable t) {
			// don't let the exception get thrown out, will cause infinite looping!
		}
	}

	public void uncaughtException(Thread t, Throwable e) {
		handle(e);
	}
}
