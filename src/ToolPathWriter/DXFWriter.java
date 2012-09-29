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
package ToolPathWriter;

import java.io.*;

import cc.sketchchair.functions.functions;

/**
 * Writes 2D dxf files. 
 * @author gregsaul
 *
 */
public class DXFWriter extends HPGLWriter {
	/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

	/*
	 * RawDXF - Code to write DXF files with beginRaw/endRaw
	 * An extension for the Processing project - http://processing.org
	 * <p/>
	 * This library is free software; you can redistribute it and/or
	 * modify it under the terms of the GNU Lesser General Public
	 * License as published by the Free Software Foundation; either
	 * version 2.1 of the License, or (at your option) any later version.
	 * <p/>
	 * This library is distributed in the hope that it will be useful,
	 * but WITHOUT ANY WARRANTY; without even the implied warranty of
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	 * Lesser General Public License for more details.
	 * <p/>
	 * You should have received a copy of the GNU Lesser General
	 * Public License along with the Processing project; if not,
	 * write to the Free Software Foundation, Inc., 59 Temple Place,
	 * Suite 330, Boston, MA  02111-1307  USA
	 */

	/**
	 * A simple library to write DXF files with Processing.
	 * Because this is used with beginRaw() and endRaw(), only individual
	 * triangles and (discontinuous) line segments will be written to the file.
	 * <P/>
	 * Use something like a keyPressed() in PApplet to trigger it,
	 * to avoid writing a bazillion .dxf files.
	 * <P/>
	 * Usually, the file will be saved to the sketch's folder.
	 * Use Sketch &rarr; Show Sketch Folder to see it from the PDE.
	 * <p/>
	 * A simple example of how to use:
	 * <PRE>
	 * import processing.dxf.*;
	 *
	 * boolean record;
	 *
	 * void setup() {
	 *   size(500, 500, P3D);
	 * }
	 *
	 * void keyPressed() {
	 *   // use a key press so that it doesn't make a million files
	 *   if (key == 'r') record = true;
	 * }
	 *
	 * void draw() {
	 *   if (record) {
	 *     beginRaw(DXF, "output.dxf");
	 *   }
	 *
	 *   // do all your drawing here
	 *
	 *   if (record) {
	 *     endRaw();
	 *     record = false;
	 *   }
	 * }
	 * </PRE>
	 * or to use it and be able to control the current layer:
	 * <PRE>
	 * import processing.dxf.*;
	 *
	 * boolean record;
	 * RawDXF dxf;
	 *
	 * void setup() {
	 *   size(500, 500, P3D);
	 * }
	 *
	 * void keyPressed() {
	 *   // use a key press so that it doesn't make a million files
	 *   if (key == 'r') record = true;
	 * }
	 *
	 * void draw() {
	 *   if (record) {
	 *     dxf = (RawDXF) createGraphics(width, height, DXF, "output.dxf");
	 *     beginRaw(dxf);
	 *   }
	 *
	 *   // do all your drawing here, and to set the layer, call:
	 *   // if (record) {
	 *   //   dxf.setLayer(num);
	 *   // }
	 *   // where 'num' is an integer.
	 *   // the default is zero, or you can set it to whatever.
	 *
	 *   if (record) {
	 *     endRaw();
	 *     record = false;
	 *     dxf = null;
	 *   }
	 * }
	 * </PRE>
	 * Note that even though this class is a subclass of PGraphics, it only
	 * implements the parts of the API that are necessary for beginRaw/endRaw.
	 * <P/>
	 * Based on the original DXF writer from Simon Greenwold, February 2004.
	 * Updated for Processing 0070 by Ben Fry in September 2004,
	 * and again for Processing beta in April 2005.
	 * Rewritten to support beginRaw/endRaw by Ben Fry in February 2006.
	 * Updated again for inclusion as a core library in March 2006.
	 * Constructor modifications in September 2008 as we approach 1.0.
	 */

	File file;
	PrintWriter writer;
	int currentLayer;
	private int vertexCount;
	private float prevX;
	private float prevY;
	private float prevZ;
	private float scale = 1;

	public DXFWriter(String path) {
		super(path);

		if (path != null) {
			file = new File(path);
			if (!file.isAbsolute())
				file = null;
		}
		if (file == null) {
			throw new RuntimeException(
					"PGraphicsPDF requires an absolute path "
							+ "for the location of the output file.");
		}

		if (writer == null) {
			try {
				writer = new PrintWriter(new FileWriter(file));
			} catch (IOException e) {
				throw new RuntimeException(e); // java 1.4+
			}
		}

		allocate();
		writeHeader();

	}

	// ..............................................................

	protected void allocate() {
		/*
		for (int i = 0; i < MAX_TRI_LAYERS; i++) {
		  layerList[i] = NO_LAYER;
		}
		*/
		setLayer(0);
	}

	@Override
	public void bezier(float x1, float y1, float cx1, float cy1, float cx2,
			float cy2, float x2, float y2) {

		for (float t = 0; t <= 1; t += 0.1f) {
			float x = functions.bezierPoint(x1, cx1, cx2, x2, t);
			float y = functions.bezierPoint(y1, cy1, cy2, y2, t);
			lineTo(x, y);
		}

	}

	// ..............................................................

	public void close() {
		writeFooter();
		dispose();
	}

	// ..............................................................

	public void dispose() {
		writeFooter();

		writer.flush();
		writer.close();
		writer = null;
	}

	@Override
	public void lineTo(float x, float y) {
		lineTo(x, y, 0);
	}

	public void lineTo(float x, float y, float z) {

		writeLine(prevX, prevY, prevZ, getTranslatedX(x), getTranslatedY(y), z
				* scale);
		prevX = getTranslatedX(x);
		prevY = getTranslatedY(y);
		prevZ = z * scale;
	}

	@Override
	public void move(float x, float y) {
		move(x, y, 0);
	}

	public void move(float x, float y, float z) {
		prevX = getTranslatedX(x);
		prevY = getTranslatedY(y);
		prevZ = z * scale;
	}

	// ..............................................................

	public void nextPage() {
		// TODO Auto-generated method stub

	}

	/**
	 * Write a line to the dxf file. Available for anyone who wants to
	 * insert additional commands into the DXF stream.
	 */
	public void println(String what) {
		writer.println(what);
	}

	/**
	 * Set the current layer being used in the DXF file.
	 * The default is zero.
	 */
	public void setLayer(int layer) {
		currentLayer = layer;
	}

	/**
	   * Write a command on one line (as a String), then start a new line
	   * and write out a formatted float. Available for anyone who wants to
	   * insert additional commands into the DXF stream.
	   */
	public void write(String cmd, float val) {
		writer.println(cmd);
		// don't format, will cause trouble on systems that aren't en-us
		// http://dev.processing.org/bugs/show_bug.cgi?id=495
		writer.println(val);
	}

	private void writeFooter() {
		writer.println("0");
		writer.println("ENDSEC");
		writer.println("0");
		writer.println("EOF");
	}

	private void writeHeader() {
		writer.println("0");
		writer.println("SECTION");
		writer.println("2");
		writer.println("ENTITIES");
	}

	protected void writeLine(float x1, float y1, float z1, float x2, float y2,
			float z2) {
		writer.println("0");
		writer.println("LINE");

		// write out the layer
		writer.println("8");
		writer.println(String.valueOf(currentLayer));

		write("10", x1);
		write("20", y1 * -1);
		write("30", z1);

		write("11", x2);
		write("21", y2 * -1);
		write("31", z2);
	}

}
