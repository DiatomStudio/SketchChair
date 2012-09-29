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
 * Static class used to log debug and program messages. Should be used in place of System.out.println() etc.
 * @author gregsaul
 *
 */
public class LOGGER {

	public static void debug(String message) {
	//System.out.println(message);
	}

	public static void debug(String message, Object obj) {	
	//System.out.println(obj.getClass().getName()+": "+message);
	}

	public static void error(String message) {
		System.out.println(message);
	}

	public static void error(String message, Object obj) {
		System.out.println(obj.getClass().getName() + ": " + message);
	}

	//log levels ERROR > WARNING > INFO > DEBUG
	public static void info(String message) {
		System.out.println(message);
	}

	public static void info(String message, Object obj) {System.out.println(obj.getClass().getName()+": "+message);
	}

	public static void warn(String message) {
		System.out.println(message);
	}

	public static void warn(String message, Object obj) {
		System.out.println(obj.getClass().getName() + ": " + message);
	}

	public static void warning(String message) {
		System.out.println(message);
	}

	public static void warning(String message, Object obj) {
		System.out.println(obj.getClass().getName() + ": " + message);
	}

}
