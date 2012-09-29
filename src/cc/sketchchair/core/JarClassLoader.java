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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarClassLoader {

	static String[] getResourceListing(Class<main> clazz, String path)
			throws URISyntaxException, IOException {
		URL dirURL = clazz.getClassLoader().getResource(path);
		if (dirURL != null && dirURL.getProtocol().equals("file")) {
			/* A file path: easy enough */
			return new File(dirURL.toURI()).list();
		}

		if (dirURL == null) {
			/* 
			 * In case of a jar file, we can't actually find a directory.
			 * Have to assume the same jar as clazz.
			 */
			String me = clazz.getName().replace(".", "/") + ".class";
			dirURL = clazz.getClassLoader().getResource(me);
		}

		if (dirURL.getProtocol().equals("jar")) {
			/* A JAR path */
			String jarPath = dirURL.getPath().substring(5,
					dirURL.getPath().indexOf("!")); //strip out only the JAR file
			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
			Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
			while (entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
				if (name.startsWith(path)) { //filter according to the path
					String entry = name.substring(path.length());
					int checkSubdir = entry.indexOf("/");
					if (checkSubdir >= 0) {
						// if it is a subdirectory, we just return the directory name
						entry = entry.substring(0, checkSubdir);
					}
					result.add(entry);
				}
			}
			return result.toArray(new String[result.size()]);
		}

		throw new UnsupportedOperationException("Cannot list files for URL "
				+ dirURL);
	}

	static void load(String path, String mylibName) {
		System.out.println("about to load " + mylibName);
		try {
			URL libUrl = main.class.getResource(path + mylibName);
			File dir1 = new File(".");

			File file = new File(dir1.getCanonicalPath() + "/" + mylibName);

			if (!file.exists())
				file.createNewFile();
			//System.out.println(System.getProperty("java.library.path")));
			file.deleteOnExit();
			FileInputStream in = new FileInputStream(new File(libUrl.getFile()));
			FileOutputStream out = new FileOutputStream(file);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			out.close();
			in.close();
			System.load(file.getAbsolutePath());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static void loadAllJOGL() {

		String system = System.getProperty("os.name");
		String arch;

		String libExtension = "Windows".contains(system) ? ".dll" : "Unix"
				.contains(system) ? ".so" : "Mac".contains(system) ? ".jnilib"
				: "";

		boolean is64bit = false;
		if (system.contains("Windows")) {
			is64bit = (System.getenv("ProgramFiles(x86)") != null);
		} else {
			is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
		}

		boolean isPPC = System.getProperty("os.arch").equals("ppc");
		boolean isIntel = System.getProperty("os.arch").equals("i386");

		if (system.contains("Windows")) {
			if (is64bit)
				loadFolder("nativeLibs/windows-64bit/");
			else
				loadFolder(".");

		} else if (system.contains("Mac")) {
			if (isPPC)
				loadFolder("nativeLibs/macosx-ppc/");
			else
				loadFolder("nativeLibs/macosx-universal/");

		}

		//String mylibName = "myLibrary" + libExtension;

	}

	static void loadFolder(String folderName) {
		try {
			String[] libs = getResourceListing(main.class, folderName);

			for (int i = 0; i < libs.length; i++)
				System.out.println(libs[i]);
			//load(folderName,libs[i]);

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
