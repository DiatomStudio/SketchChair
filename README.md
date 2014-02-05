##SketchChair 
by: Diatom Studio
http://diatom.cc
hello@diatom.cc##

This document attempts to outline the structure and code patterns used in SketchChair in order for someone to be able to navigate, build on and reuse its code. 

###Documentation
Most of SketchChairs custom classes have been documented in a JavaDoc this can be found here:  /doc/index.html

##Compiling

###To compile in legacy mode###
edit src/SETTINGS.java
 
change line:106 > LEGACY_MODE = true;

edit: build.xml

change line:14    dir="libLegacy"

change line:69	  dir="libLegacy" 

from console change to the SketchChair root directory. 
run: ant build

###To compile in Current mode

**note: this does not currently work in the development branch**

edit src/SETTINGS.java
 
change line:106 > LEGACY_MODE = false;

edit: build.xml

change line:14    dir="libCurrent"

change line:69	  dir="libCurrent" 

from console change to the SketchChair root directory. 
run: ant build

Your compiled SketchChair.jar can be found in build/


##Running
java -jar -Xmx1024M SketchChair.jar

##Basic Outline
This is a very basic outline of how the SketchChair engine works and does not take many details into account. 

SketchChair contains basic vector drawing program, when a user draws on a SketchPlane in SketchChair a drawing is added to this plane. Most drawing operations are handled by the cc.sketchcahir.sketch package.  

A build command is then run on the design that generates all parametric parts of the design.

The SketchChair engine looks at what sliceSelections have been added to the  model by the user or automatically and at what points on the SketchShape. The cc.sketchcahir.geometry package then calculates the chairs slice forms based on intersections between SketchPlanes and adds additional inedible SketchPlanes  to the design and slots to each intersecting piece so that they are able to fit together. The geometry engine can calculate a number of different style slices from flat finger jointed surfaces to waffle forms. 

After drawings have been added to multiple SketchPlanes then these drawings are combined together to create the cutting outline for each layer taking into account joining details. 

The outline is then used to generate a 3d mesh that is fed to the jBullet physics engine. 

A simulation step is run on the engine and the resulting position of the chair calculated along with the ergonomic figure. 

The designs SketchPlanes are then matched to the 3D physicaly simulated model and drawn to the screen in the appropriate render mode and position.

When a design is produced a designs SketchOutlines are generated and passed of  to the ShapePacking package where it is automatically packed on a sheet or several sheets of a given size then either displayed on the users screen or outputted in different formats through the ToolPathWriter package. 

If a chair is shared online it is saved using SketchChairs custom DOM format the recursively saves each element of a design to a file by calling the toXML() method found in each saveable class, this method produces DOM element containing the classes parameters. Each saveable element also contains a constructor where it can be loaded from a DOM element containing it's parameters. 

After the design is saved it can be uploaded though the CloudHook package.  This package uses sockets to connect to a remote php script, to log the user into the online system, to upload the new design and to compare it to other designs online to see if a new DB entry should be created for the design or a old design updated. 



##Code Structure
Where possible SketchChair has been broken up into separate modules or libraries that can be used by them selfs and as part of SketchChair. 

These modules are detailed bellow;
 
###cc.sketchchair.core
Contains SketchChair code specific to the SketchChair program. These classes tie the different geometry, shape packing, figure etc libraries together to make SketchChair. 

###cc.sketchcahir.geometry
Contains geometry classes and functions for storing a designs structure and calculating cross slices and slots. 

###cc.sketchcahir.sketch
Contains classes and functions for drawing functions.  If this package is run separately from SketchChair it acts as a simple vector based drawing program. c

###cc.sketchcahir.environments
Environments are 2d textures that can be loaded and placed on a drawing plane as reference. 

###cc.sketchcahir.functions
Miscellaneous static functions used in SketchChair. 

###cc.sketchcahir.ragdoll
Ergonomic figure for testing designs. 

###cc.sketchcahir.sketch.gui
GUI assets. 

###cc.sketchcahir.triangulate
delaunay triangulation library used to triangulate a 3d mesh from a  designs outline. 

###cc.sketchcahir.widgets
GUI Widget classes for GUI elements such as layer selector, slice settings etc. 

###CloudHook
A custom library for performing actions on the SketchChair server, uploading chairs logging a user in etc.  The server component of this library is written php. 

###ModalGUI
A custom GUI library written fro SketchChair. contains most standard GUI components. 

###ShapePacking
A custom library for taking 2d cutting outlines and packing them onto a sheet of material. 

###ToolPathWriter
A class for converting 2d sketch outlines to various output formats, g-code, dxf etc. 

###cc.sketchcahir.main
main program start file shortcut. Used for compatibility with certain systems. 

###main
main program start file shortcut. Used for compatibility with certain systems. 



##Language
SketchChair is written in Java using the processing.org framework. This means that SketchChair is compatible on Windows, OSX and Linux based systems, it can also be embedded in a webpage as a Java applet.

The processing framework used in SketchChair has also been converted to Javascript  (http://processingjs.org/) making it easier to conniver Java program written using processing to be converted to HTML5 web application, although not yet implemented this provides a way of porting SketchChair to Javascript. 



##Libraries
SketchChair uses a number of third party libraries that are listed below. 
A majority of the core libraries used in SketchChair have also been ported to Javascript meaning that it should be possible to use them in a web-based SketchChair. 

##processing
homepage: http://processing.org/
License:  LGPL 

##OPENGL: JOGL, GLUEGEN
homepage: http://www.opengl.org/
License:  

##jBullet:
homepage: http://jbullet.advel.cz/
License: ZLIB

##toxilib:
Homepage: http://toxiclibs.org/
License: LGPLv2

##svgSalemander
homepage: http://svgsalamander.java.net/
license: LGPL 

##xom
homepage: http://www.xom.nu/
license:  LGPL



##IDE
SketchChair is written using the Eclipse IDE.

