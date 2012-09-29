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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import cc.sketchchair.core.LOGGER;
/**
 * CloudHook
 * 
 * Used to communicate with server side script. Uses a custom protocol to perform actions on server.
 * <pre> 
 *                _____
 *       ____/-- /      \--\____
 *      /    \__/        \__/   \
 *     /                         \
 *    |                           |
 *     \_/_\   /         \   /_\_/ 
 *          \_/_\   \/  /_\_/
 *               \_/_\_/   |
 *                         |
 *                         |
 *                         | 
 *                         |/
 *     
 * </pre>
 */
public class CloudHook extends Thread {
	String CLOUD_URL = "http://sketchchair.cc/framework/CloudHook.php";

	boolean actionQued = false;
	String quedAction = "";
	String[][] quedArgs;
	byte[] quedBytes;

	private boolean threadStarted = false;

	public CloudHook(String script_url) {
		CLOUD_URL = script_url;
	}

	public String getActionURL(String action, String[][] args) {

		String url = CLOUD_URL + "?action=" + action;

		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				try {
					url += "&" + URLEncoder.encode(args[i][0], "UTF-8") + "="
							+ URLEncoder.encode(args[i][1], "UTF-8");

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		return url;
	}

	public String post(String url) {
		String returnString = "";
		try {

			URL u = new URL(url);
			URLConnection c = u.openConnection();
			c.setDoOutput(true);
			c.setDoInput(true);
			c.setUseCaches(false);
			//c.setRequestProperty("Content-Type", "multipart/form-data; boundary=AXi93A");
			DataOutputStream dstream = new DataOutputStream(c.getOutputStream());
			// close the multipart form request		 
			dstream.writeBytes("\r\n--AXi93A--\r\n\r\n");
			dstream.flush();
			dstream.close();

			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						c.getInputStream()));
				String sIn = in.readLine();
				while (sIn != null) {
					if (sIn != null) {
						if (returnString.length() > 0)
							returnString = returnString + "\n" + sIn;
						else
							returnString += sIn;

					}
					sIn = in.readLine();
				}
			}

			catch (Exception e) {

				e.printStackTrace();

			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return returnString;

	}

	public String post(String actionURL, byte[] bytes) {
		String returnString = "";
		try {

			URL u = new URL(actionURL);

			URLConnection c = u.openConnection();

			c.setDoOutput(true);
			c.setDoInput(true);
			c.setUseCaches(false);
			// set request headers	 
			c.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=AXi93A");
			DataOutputStream dstream = new DataOutputStream(c.getOutputStream());
			dstream.writeBytes("--AXi93A\r\n");
			dstream.writeBytes("Content-Disposition: form-data; name=\"data\"; filename=\"whatever\" \r\n");
			dstream.writeBytes("Content-Type: image/png\r\n");
			dstream.writeBytes("Content-Transfer-Encoding: binary\r\n\r\n");
			dstream.write(bytes, 0, bytes.length);

			// close the multipart form request

			dstream.writeBytes("\r\n--AXi93A--\r\n\r\n");

			dstream.flush();

			dstream.close();

			// read the output from the URL

			try {

				BufferedReader in = new BufferedReader(new InputStreamReader(
						c.getInputStream()));

				String sIn = in.readLine();

				boolean b = true;

				while (sIn != null) {

					if (sIn != null) {

						//if(popup) if(sIn.substring(0,folder.length()).equals(folder)) link(CLOUD_URL+sIn, "_blank"); 
						returnString = returnString + sIn;
					}

					sIn = in.readLine();

				}

			}

			catch (Exception e) {

				e.printStackTrace();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnString;
	}

	public String postAction(String action) {

		return this.post(CLOUD_URL + "?action=" + action);
	}

	public String postAction(String action, String[][] args) {
		String url = getActionURL(action, args);
		LOGGER.debug(url);
		return this.post(url);
	}

	public String postAction(String action, String[][] args, byte[] bytes) {
		String url = getActionURL(action, args);
		return this.post(url, bytes);
	}

	public String postData(String url, String location) {
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(new File(location));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String returnString = "";
		try {

			String fileName = "thumb.jpg";
			String folder = "";
			URL u = new URL(url);

			URLConnection c = u.openConnection();

			c.setDoOutput(true);
			c.setDoInput(true);
			c.setUseCaches(false);
			// set request headers	 
			c.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=AXi93A");
			// open a stream which can write to the url

			DataOutputStream dstream = new DataOutputStream(c.getOutputStream());

			// write content to the server, begin with the tag that says a content element is comming

			dstream.writeBytes("--AXi93A\r\n");
			// discribe the content

			dstream.writeBytes("Content-Disposition: form-data; name=\"data\"; filename=\"whatever\" \r\n");

			dstream.writeBytes("Content-Type: image/jpeg\r\n");

			dstream.writeBytes("Content-Transfer-Encoding: binary\r\n\r\n");

			int bytesAvailable = fileInputStream.available();

			int maxBufferSize = 1024;
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);

			byte[] buffer = new byte[bufferSize];

			// read file and write it into form...

			int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				dstream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			}

			// close the multipart form request

			dstream.writeBytes("\r\n--AXi93A--\r\n\r\n");

			dstream.flush();

			dstream.close();

			// read the output from the URL

			try {

				BufferedReader in = new BufferedReader(new InputStreamReader(
						c.getInputStream()));

				String sIn = in.readLine();

				boolean b = true;

				while (sIn != null) {

					if (sIn != null) {

						//if(popup) if(sIn.substring(0,folder.length()).equals(folder)) link(CLOUD_URL+sIn, "_blank"); 
						returnString = returnString + sIn;
					}

					sIn = in.readLine();

				}

			}

			catch (Exception e) {

				e.printStackTrace();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnString;
	}
	
	public String postData(String action, String folder, String fileName,
			byte[] bytes) {
		return postData(action, folder,fileName,null,bytes);
	}

	public String postData(String action, String folder, String fileName,String sessionID_,
			byte[] bytes) {
		String returnString = "";
		try {

			String url = CLOUD_URL + "?action=" + action + "&folder="
					+ folder + "&name=" + fileName;
					
					if(sessionID_ != null)
						url += "&sessionID="+sessionID_;
					
			URL u = new URL(url);

			URLConnection c = u.openConnection();

			c.setDoOutput(true);
			c.setDoInput(true);
			c.setUseCaches(false);
			// set request headers	 
			c.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=AXi93A");
			// open a stream which can write to the url

			DataOutputStream dstream = new DataOutputStream(c.getOutputStream());

			// write content to the server, begin with the tag that says a content element is comming

			dstream.writeBytes("--AXi93A\r\n");
			// discribe the content

			dstream.writeBytes("Content-Disposition: form-data; name=\"data\"; filename=\"whatever\" \r\n");

			dstream.writeBytes("Content-Type: image/png\r\n");

			dstream.writeBytes("Content-Transfer-Encoding: binary\r\n\r\n");

			dstream.write(bytes, 0, bytes.length);

			// close the multipart form request

			dstream.writeBytes("\r\n--AXi93A--\r\n\r\n");

			dstream.flush();

			dstream.close();

			// read the output from the URL

			try {

				BufferedReader in = new BufferedReader(new InputStreamReader(
						c.getInputStream()));

				String sIn = in.readLine();

				boolean b = true;

				while (sIn != null) {

					if (sIn != null) {

						//if(popup) if(sIn.substring(0,folder.length()).equals(folder)) link(CLOUD_URL+sIn, "_blank"); 
						returnString = returnString + sIn;
					}

					sIn = in.readLine();

				}

			}

			catch (Exception e) {

				e.printStackTrace();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnString;
	}

	public String postData(String action, String[][] args, String location) {
		String url = CLOUD_URL + "?action=" + action;
		for (int i = 0; i < args.length; i++) {
			try {
				url += "&" + URLEncoder.encode(args[i][0], "UTF-8") + "="
						+ URLEncoder.encode(args[i][1], "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return this.postData(url, location);
	}

	public boolean queAction(String action, String[][] args, byte[] bytes) {
		if (actionQued)
			return false;

		this.quedAction = action;
		this.quedArgs = args;
		this.quedBytes = bytes;

		this.actionQued = true;

		if (!this.threadStarted)
			this.start();

		return true;

	}

	public void run() {
		this.threadStarted = true;
		while (true) {
			///super.run();
			if (this.actionQued) {
				//System.out.println(this.postAction(this.quedAction,this.quedArgs,this.quedBytes));

				this.actionQued = false;
			}

			try {
				this.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	String SetupNewChair() {

		return CLOUD_URL;

	}

}
