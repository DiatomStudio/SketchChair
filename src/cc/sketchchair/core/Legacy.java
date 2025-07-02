package cc.sketchchair.core;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JFileChooser;

import processing.core.PApplet;
import ModalGUI.ModalGUI;

public class Legacy {
	private static Legacy instance = null;

	public static final int DISABLE_STROKE_PERSPECTIVE = 0;
	public static int displayWidth = 1024;
	public static int displayHeight = 768;
	
	public static void registerMouseEvent(PApplet _main, ModalGUI _modalGUI) {
		
		if(SETTINGS.LEGACY_MODE)
			_main.registerMouseEvent(_modalGUI);
		else
		_main.registerMethod("mouseEvent", _modalGUI);
	}
	public static void registerKeyEvent(PApplet _main, ModalGUI _modalGUI) {
		
		if(SETTINGS.LEGACY_MODE)
			_main.registerKeyEvent(_modalGUI);
		else
		_main.registerMethod("keyEvent", _modalGUI);
		//
		
		
	}
	public static void addMouseWheelListener(PApplet _main, ModalGUI _modalGUI) {
		
		if(SETTINGS.LEGACY_MODE)
			_main.addMouseWheelListener(_modalGUI);
		//else
	//	_main.registerMethod("mouseWheelMoved", _modalGUI);
		
		
		
	}
	
	
	
	
	
	  //////////////////////////////////////////////////////////////

	  // FILE/FOLDER SELECTION


	  private Frame selectFrame;
	private Frame frame;
	protected static boolean useNativeSelect = true;

	  private Frame selectFrame() {
	    if (frame != null) {
	      selectFrame = frame;

	    } else if (selectFrame == null) {
	      Component comp = GLOBAL.applet.getParent();
	      while (comp != null) {
	        if (comp instanceof Frame) {
	          selectFrame = (Frame) comp;
	          break;
	        }
	        comp = comp.getParent();
	      }
	      // Who you callin' a hack?
	      if (selectFrame == null) {
	        selectFrame = new Frame();
	      }
	    }
	    return selectFrame;
	  }

	
	 /**
	   * Open a platform-specific file chooser dialog to select a file for input.
	   * After the selection is made, the selected File will be passed to the
	   * 'callback' function. If the dialog is closed or canceled, null will be
	   * sent to the function, so that the program is not waiting for additional
	   * input. The callback is necessary because of how threading works.
	   *
	   * <pre>
	   * void setup() {
	   *   selectInput("Select a file to process:", "fileSelected");
	   * }
	   *
	   * void fileSelected(File selection) {
	   *   if (selection == null) {
	   *     println("Window was closed or the user hit cancel.");
	   *   } else {
	   *     println("User selected " + fileSeleted.getAbsolutePath());
	   *   }
	   * }
	   * </pre>
	   *
	   * For advanced users, the method must be 'public', which is true for all
	   * methods inside a sketch when run from the PDE, but must explicitly be
	   * set when using Eclipse or other development environments.
	   *
	   * @webref input:files
	   * @param prompt message to the user
	   * @param callback name of the method to be called when the selection is made
	   */
	  public void selectInput(String prompt, String callback) {
	    selectInput(prompt, callback, null);
	  }


	  public void selectInput(String prompt, String callback, File file) {
	    selectInput(prompt, callback, file, this);
	  }


	  public void selectInput(String prompt, String callback,
	                          File file, Object callbackObject) {
	    selectInput(prompt, callback, file, callbackObject, selectFrame());
	  }


	  static public void selectInput(String prompt, String callbackMethod,
	                                 File file, Object callbackObject, Frame parent) {
	    selectImpl(prompt, callbackMethod, file, callbackObject, parent, FileDialog.LOAD);
	  }


	  /**
	   * See selectInput() for details.
	   *
	   * @webref output:files
	   * @param prompt message to the user
	   * @param callback name of the method to be called when the selection is made
	   */
	  public void selectOutput(String prompt, String callback) {
	    selectOutput(prompt, callback, null);
	  }

	  public void selectOutput(String prompt, String callback, File file) {
	    selectOutput(prompt, callback, file, this);
	  }


	  public void selectOutput(String prompt, String callback,
	                           File file, Object callbackObject) {
	    selectOutput(prompt, callback, file, callbackObject, selectFrame());
	  }


	  static public void selectOutput(String prompt, String callbackMethod,
	                                  File file, Object callbackObject, Frame parent) {
	    selectImpl(prompt, callbackMethod, file, callbackObject, parent, FileDialog.SAVE);
	  }


	  static protected void selectImpl(final String prompt,
	                                   final String callbackMethod,
	                                   final File defaultSelection,
	                                   final Object callbackObject,
	                                   final Frame parentFrame,
	                                   final int mode) {
	    EventQueue.invokeLater(new Runnable() {
	      public void run() {
	        File selectedFile = null;

	        if (useNativeSelect) {
	          FileDialog dialog = new FileDialog(parentFrame, prompt, mode);
	          if (defaultSelection != null) {
	            dialog.setDirectory(defaultSelection.getParent());
	            dialog.setFile(defaultSelection.getName());
	          }
	          dialog.setVisible(true);
	          String directory = dialog.getDirectory();
	          String filename = dialog.getFile();
	          if (filename != null) {
	            selectedFile = new File(directory, filename);
	          }

	        } else {
	          JFileChooser chooser = new JFileChooser();
	          chooser.setDialogTitle(prompt);
	          if (defaultSelection != null) {
	            chooser.setSelectedFile(defaultSelection);
	          }

	          int result = -1;
	          if (mode == FileDialog.SAVE) {
	            result = chooser.showSaveDialog(parentFrame);
	          } else if (mode == FileDialog.LOAD) {
	            result = chooser.showOpenDialog(parentFrame);
	          }
	          if (result == JFileChooser.APPROVE_OPTION) {
	            selectedFile = chooser.getSelectedFile();
	          }
	        }
	        selectCallback(selectedFile, callbackMethod, callbackObject);
	      }
	    });
	  }


	  /**
	   * See selectInput() for details.
	   *
	   * @webref input:files
	   * @param prompt message to the user
	   * @param callback name of the method to be called when the selection is made
	   */
	  public void selectFolder(String prompt, String callback) {
	    selectFolder(prompt, callback, null);
	  }


	  public void selectFolder(String prompt, String callback, File file) {
	    selectFolder(prompt, callback, file, this);
	  }


	  public void selectFolder(String prompt, String callback,
	                           File file, Object callbackObject) {
	    selectFolder(prompt, callback, file, callbackObject, selectFrame());
	  }


	  static public void selectFolder(final String prompt,
	                                  final String callbackMethod,
	                                  final File defaultSelection,
	                                  final Object callbackObject,
	                                  final Frame parentFrame) {
	    EventQueue.invokeLater(new Runnable() {
	      public void run() {
	        File selectedFile = null;

	        if (useNativeSelect != false) {
	          FileDialog fileDialog =
	            new FileDialog(parentFrame, prompt, FileDialog.LOAD);
	          System.setProperty("apple.awt.fileDialogForDirectories", "true");
	          fileDialog.setVisible(true);
	          System.setProperty("apple.awt.fileDialogForDirectories", "false");
	          String filename = fileDialog.getFile();
	          if (filename != null) {
	            selectedFile = new File(fileDialog.getDirectory(), fileDialog.getFile());
	          }
	        } else {
	          JFileChooser fileChooser = new JFileChooser();
	          fileChooser.setDialogTitle(prompt);
	          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	          if (defaultSelection != null) {
	            fileChooser.setSelectedFile(defaultSelection);
	          }

	          int result = fileChooser.showOpenDialog(parentFrame);
	          if (result == JFileChooser.APPROVE_OPTION) {
	            selectedFile = fileChooser.getSelectedFile();
	          }
	        }
	        selectCallback(selectedFile, callbackMethod, callbackObject);
	      }
	    });
	  }


	  static private void selectCallback(File selectedFile,
	                                     String callbackMethod,
	                                     Object callbackObject) {
	    try {
	      Class<?> callbackClass = callbackObject.getClass();
	      Method selectMethod =
	        callbackClass.getMethod(callbackMethod, new Class[] { File.class });
	      selectMethod.invoke(callbackObject, new Object[] { selectedFile });

	    } catch (IllegalAccessException iae) {
	      System.err.println(callbackMethod + "() must be public");

	    } catch (InvocationTargetException ite) {
	      ite.printStackTrace();

	    } catch (NoSuchMethodException nsme) {
	      System.err.println(callbackMethod + "() could not be found");
	    }
	  }
	public static Legacy instance() {
		if(instance  == null)
			instance = new Legacy();
		
		return instance;
	}
	public String get2DRenderMode() {
		if(SETTINGS.LEGACY_MODE)
			return PApplet.P3D;
		else
			return PApplet.P2D;
	}
	
	public String get3DRenderMode() {
		if(SETTINGS.LEGACY_MODE)
			return PApplet.P3D;
		else
			return PApplet.P3D;
	}
	
}
