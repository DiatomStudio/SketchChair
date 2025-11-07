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
package CloudHook;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import processing.core.*;
import javax.imageio.*;
import javax.imageio.stream.*;

/** 
 * Helper class to convert a image buffer into a binary representation of encoded image for sending to server.
 * @author gregsaul
 *
 */
public class makeImage {

	int type = 1;
	String cType = "image/png";
	String imageType = "png";

	public final static int JPEG = 0;
	public final static int PNG = 1;
	public final static int GIF = 2;
	public final static int TIFF = 3;

	static byte TIFF_HEADER[] = { 77, 77, 0, 42, 0, 0, 0, 8, 0, 9, 0, -2, 0, 4,
			0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1,
			0, 3, 0, 0, 0, 1, 0, 0, 0, 0, 1, 2, 0, 3, 0, 0, 0, 3, 0, 0, 0, 122,
			1, 6, 0, 3, 0, 0, 0, 1, 0, 2, 0, 0, 1, 17, 0, 4, 0, 0, 0, 1, 0, 0,
			3, 0, 1, 21, 0, 3, 0, 0, 0, 1, 0, 3, 0, 0, 1, 22, 0, 3, 0, 0, 0, 1,
			0, 0, 0, 0, 1, 23, 0, 4, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8,
			0, 8, 0, 8 };

	public static byte[] bufferImage(PImage srcimg) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedImage img = new BufferedImage(srcimg.width, srcimg.height,
				BufferedImage.TYPE_INT_RGB);
		//img = (BufferedImage) createImage(srcimg.width,srcimg.height);
		for (int i = 0; i < srcimg.width; i++)
			for (int j = 0; j < srcimg.height; j++)
				img.setRGB(i, j, srcimg.pixels[j * srcimg.width + i]);
		try {
			ImageIO.write(img, "jpg", out);
		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
		return out.toByteArray();
	}

	/*
	public static byte[] getBytes(PGraphics src){

	   switch(type){
	   case JPEG:
	   
	     // We need a new buffered image without the alpha channel
	     BufferedImage imageNoAlpha = new BufferedImage(src.width, src.height, BufferedImage.TYPE_INT_RGB);
	     src.loadPixels();
	     imageNoAlpha.setRGB(0, 0, src.width, src.height, src.pixels, 0, src.width);
	     return getBytesJPEG(imageNoAlpha);

	   case PNG:
	   case GIF:

	   case TIFF:
	     return getBytesTIFF(src);

	   default:
	     return new byte[0];
	   }
	   
	 }
	 */

	/* ------------------------------ JPEG ------------------------------ */

	public static byte[] getPNG(PImage src) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		BufferedImage img = new BufferedImage(src.width, src.height,
				BufferedImage.TYPE_INT_ARGB);
		//img = (BufferedImage) createImage(srcimg.width,srcimg.height);
		for (int i = 0; i < src.width; i++)
			for (int j = 0; j < src.height; j++)
				img.setRGB(i, j, src.pixels[j * src.width + i]);
		
		
		try {
			ImageIO.write((BufferedImage) img, "png", out);
		} catch (Exception e) {
			e.printStackTrace();
			return new byte[0]; // Problem
		}
		return out.toByteArray();

	}

	/* ------------------------------ TIFF ------------------------------ */

	/**
	 * Get the image as a jpeg byte array
	 * @param image BufferedImage to create the byte array from
	 * @see BufferedImage
	 */
	protected byte[] getBytesJPEG(BufferedImage image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		java.util.Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
		if (iter.hasNext()) {
			ImageWriter writer = (ImageWriter) iter.next();
			ImageWriteParam iwp = writer.getDefaultWriteParam();
			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			iwp.setCompressionQuality(1f);

			ImageOutputStream ios = new MemoryCacheImageOutputStream(baos);
			writer.setOutput(ios);

			try {
				writer.write(image);
			} catch (Exception e) {
				e.printStackTrace();
				return new byte[0]; // Problem
			}
			return baos.toByteArray();
		}
		return new byte[0];
	}

	/**
	 * Get the image as a tiff byte array
	 * @param srcimg PImage to create the byte array from
	 * @see PImage
	 */
	protected byte[] getBytesTIFF(PImage srcimg) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			byte tiff[] = new byte[768];
			System.arraycopy(TIFF_HEADER, 0, tiff, 0, TIFF_HEADER.length);

			tiff[30] = (byte) ((srcimg.width >> 8) & 0xff);
			tiff[31] = (byte) ((srcimg.width) & 0xff);
			tiff[42] = tiff[102] = (byte) ((srcimg.height >> 8) & 0xff);
			tiff[43] = tiff[103] = (byte) ((srcimg.height) & 0xff);

			int count = srcimg.width * srcimg.height * 3;
			tiff[114] = (byte) ((count >> 24) & 0xff);
			tiff[115] = (byte) ((count >> 16) & 0xff);
			tiff[116] = (byte) ((count >> 8) & 0xff);
			tiff[117] = (byte) ((count) & 0xff);

			// spew the header to the disk
			output.write(tiff);

			srcimg.loadPixels();
			for (int i = 0; i < srcimg.pixels.length; i++) {
				output.write((srcimg.pixels[i] >> 16) & 0xff);
				output.write((srcimg.pixels[i] >> 8) & 0xff);
				output.write(srcimg.pixels[i] & 0xff);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return output.toByteArray();
	}

}
