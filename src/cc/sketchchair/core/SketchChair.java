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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import nu.xom.Attribute;
import nu.xom.Element;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import toxi.geom.Matrix4x4;
import toxi.geom.Plane;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;
import ShapePacking.BezierControlNode;
import ShapePacking.spShape;
import ShapePacking.spShapePack;

import cc.sketchchair.environments.Environment;
import cc.sketchchair.geometry.GeometryOperations;
import cc.sketchchair.geometry.SlicePlane;
import cc.sketchchair.geometry.SlicePlanes;
import cc.sketchchair.sketch.SETTINGS_SKETCH;
import cc.sketchchair.sketch.Sketch;
import cc.sketchchair.sketch.SketchPath;
import cc.sketchchair.sketch.SketchPoint;
import cc.sketchchair.sketch.SketchShape;
import cc.sketchchair.sketch.SketchShapes;
import cc.sketchchair.sketch.SketchSpline;
import cc.sketchchair.sketch.SketchTools;
import cc.sketchchair.sketch.SliceSlot;


import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.IndexedMesh;
import com.bulletphysics.collision.shapes.TriangleIndexVertexArray;
import com.bulletphysics.collision.shapes.VertexData;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysics.extras.gimpact.GImpactCollisionAlgorithm;
import com.bulletphysics.extras.gimpact.GImpactMeshShape;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;


/**
 * The SketchChair Class represents one SketchChair or SketchObject in the program, 
 * This Class Brings together a chairs geometry and settings specific to that chair.
 *		
 *<pre>
 *			 |\|\|\|\|\
 *			 || | | | | 
 *			 || | | | | 
 *			 || | | | |
 *			 || | | | |
 *			 ||\ \ \ \ \
 *			 || \ \ \ \ \
 * 			 ||  \ \ \ \ \
 *			 ||  || | | |||
 *			 ||  ||      ||
 *			     ||      ||
 * 				 ||      ||
 * 
 * </pre>	
 * 
 * @author Diatom Studio
 */
public class SketchChair {
	float slatSpacingX = 39f;

	boolean built = false;

	private boolean builtOuline = false;
	Vec3D centreOfMass = new Vec3D();

	float coverLength = 0;

	float coverWidth = 0;
	// private SketchSpline crossSpline;
	// SlicePlane curSlicePlane = null;
	boolean destroy;
	public transient RigidBody rigidBody = null;
	transient Vector3f rigidRenderOffset = new Vector3f();
	public SlicePlanes selectedPlanes = new SlicePlanes();
	// SketchShapes sketchShapes = new SketchShapes();

	// SketchSpline sketchSpline = new SketchSpline();

	float slicePlaceSpacing = SETTINGS_SKETCH.slat_x_spacing;// spacing in
	public SlicePlanes slicePlanesSlatSlices = new SlicePlanes();
	public SlicePlanes slicePlanesY = new SlicePlanes();

	public CrossSliceSelections crossSliceSelections = new CrossSliceSelections();
	public CrossSliceSelection creossSelectionTempOver = null;

	transient public DefaultMotionState startMotionState;

	transient public Transform startWorldTransform;
	transient public Transform currentWorldTransform = new Transform();
	transient GImpactMeshShape trimesh = null;

	private boolean updateCollisionMesh = false;
	private float width = SETTINGS.chair_width; // 600mm

	private boolean reScaling = false;

	transient private Generic6DofConstraint chairCentreConstraint;

	// private FeltCover feltCover;

	private float scale = 1f;

	private float mass = 8f;

	private boolean dragging;

	public float startCoverPercent = 0;

	public float endCoverPercent = .5f;

	private boolean personSeated = false;

	private float imgBorder = 25;

	public String cloudID = null;

	public String localSavelocation = null;

	public boolean rebuildLength;
	public int chairColour = 255;

	private float density = 0.0001f;

	private TriangleIndexVertexArray indexVertexArrays;

	private float rotateRend = 0;

	private float materialWidth;

	private boolean initailSliceAdded = false;

	public int selectedPlaneNumber;

	SketchChair() {
		// this.sketchSpline.slots_on_inside = true;
		// this.sketchShapes.add(this.sketchSpline);
		// this.sketchShapes.currentShape = sketchSpline;
		GLOBAL.undo.addOperation(new UndoAction(this, UndoAction.ADD_CHAIR));

		//GLOBAL.planesWidget.planes = this.getSlicePlanesY();
		buildWidth();
		rebuildWidth();

	}

	/**
	 * Build design from a XML element
	 * @param element 
	 */
	public SketchChair(Element element) {

		// wrong type
		if (!element.getLocalName().equals("SketchChair"))
			return;

		// get slat spacing
		if (element.getAttributeValue("cloudID") != null)
			cloudID = element.getAttributeValue("cloudID");

		// get slat spacing
		if (element.getAttributeValue("slatSpacingX") != null)
			slatSpacingX = Float.valueOf(element
					.getAttributeValue("slatSpacingX"));

		if (element.getAttributeValue("materialWidth") != null)
			SETTINGS.materialThickness = Float.valueOf(element
					.getAttributeValue("materialWidth"));

		if (element.getAttributeValue("scale") != null)
			SETTINGS.scale = Float.valueOf(element.getAttributeValue("scale"));

		element.addAttribute(new Attribute("materialWidth", String
				.valueOf(SETTINGS.materialThickness)));

		element.addAttribute(new Attribute("scale", String
				.valueOf(SETTINGS.scale)));

		for (int i = 0; i < element.getChildCount(); i++) {
			Element child = (Element) element.getChild(i);

			if (child.getLocalName().equals("SlicePlanes"))
				this.setSlicePlanesY(new SlicePlanes(child));

			if (child.getLocalName().equals("CrossSliceSelections"))
				this.crossSliceSelections = new CrossSliceSelections(child,
						this.getSlicePlanesY(), this);

		}

		if (GLOBAL.widgetMaterials != null
				&& GLOBAL.widgetMaterials.slotSizeTextfield != null) {
			GLOBAL.widgetMaterials.slotSizeTextfield.setText(
					Float.toString(SETTINGS.materialThickness));
		}

		// now we are a "live" object again, so let's run rebuild and start
		this.currentWorldTransform = new Transform();
		this.selectedPlanes.empty();
		this.selectedPlanes.add(this.getSlicePlanesY());
		this.selectedPlanes.buildCurrentSketch();

		// this.updateCollisionShape();
		LOGGER.debug("SketchChair: finished load: about to build chair for the first time");
		this.buildPreview();
		this.addRigidModel();
		//buildWidth();

		//move the model to the ground plane
		this.setAtGroundHeightCentred();
		this.personSeated = false;

		setCurrentPositionAsStartTransform();
		GLOBAL.jBullet.update();
		this.update();
		this.initailSliceAdded = true;
		//	seatPerson();
		//buildWidth();
	}

	/**
	 * Build design from SketchShape element
	 * @param shape
	 */
	public SketchChair(SketchShape shape) {

		SlicePlane plane = new SlicePlane(new Plane(new Vec3D(0, 0, 0),
				new Vec3D(0, 0, -1)));
		plane.setSelected(true);
		plane.getSketch().getSketchShapes().add(shape);
		this.selectedPlanes.add(plane);
		this.mouseReleased(0, 0);
		

	}

	/**
	 * Constrain the current design to the center plane in physics engine. This stops the design from falling over.x
	 */
	void addCentrePlaneConstraint() {
		/*
		 * 
		 * Transform localA = new Transform(); Transform localB = new
		 * Transform();
		 * 
		 * localA.setIdentity();
		 * 
		 * localB.setIdentity(); Vec3D centre = this.getCentreOfMass();
		 * localA.origin.set(centre.x, centre.y, centre.z);
		 * 
		 * localB.origin.set(0f, 0f, 0f);
		 * 
		 * CollisionShape ZeroShape = null; jBullet.ZeroBodyChair = new
		 * RigidBody(0.0f, null, ZeroShape);
		 * 
		 * Transform Rotation = new Transform(); Rotation.setIdentity();
		 * 
		 * MatrixUtil.setEulerZYX(Rotation.basis, BulletGlobals.SIMD_PI, 0, 0);
		 * 
		 * jBullet.ZeroBodyChair.setWorldTransform(Rotation);
		 * 
		 * this.chairCentreConstraint = new
		 * Generic6DofConstraint(this.rigidBody, jBullet.ZeroBodyChair, localA,
		 * localB, false);
		 * 
		 * this.chairCentreConstraint.setLimit(0, 1, 0); // Disable X axis
		 * limits this.chairCentreConstraint.setLimit(1, 1, 0); // Disable Y
		 * axis limits this.chairCentreConstraint.setLimit(2, 1, 0); // Set the
		 * Z axis to // always be equal to // zero
		 * this.chairCentreConstraint.setLimit(3, 0, 0); // Disable X rotational
		 * // axes this.chairCentreConstraint.setLimit(4, 0, 0); // Disable Y
		 * rotational // axes this.chairCentreConstraint.setLimit(5, 1, 0); //
		 * Uncap the rotational // axes
		 * 
		 * // this.rotationConstrains = new Vector3f(0,0,1); //
		 * this.rigidBody.setMassProps(this.mass ,rotationConstrains);
		 * GLOBAL.jBullet.update();
		 */
		/*
		 * 
		 * this.chairCentreConstraint.setLimit(0,1,0); // Disable X axis limits
		 * this.chairCentreConstraint.setLimit(1,1,0); // Disable Y axis limits
		 * this.chairCentreConstraint.setLimit(2,0,0); // Set the Z axis to
		 * always be equal to zero
		 * //this.chairCentreConstraint.setLimit(3,(float)-Math.PI+
		 * .001f,(float)-Math.PI+.01f); // Disable X rotational axes // this
		 * this
		 * .chairCentreConstraint.setLimit(3,(float)-Math.PI,(float)Math.PI); //
		 * Disable X rotational axes // this
		 * 
		 * //this.chairCentreConstraint.setLimit(3,0,0); // Disable X rotational
		 * axes // this this.chairCentreConstraint.setLimit(4,0,0); // Disable Y
		 * rotational axes this.chairCentreConstraint.setLimit(5,1,0); // Uncap
		 * the Z rotational axes //#endif
		 */
		// GLOBAL.jBullet.myWorld.addConstraint(this.chairCentreConstraint,
		// true);
		// this.rigidBody.setAngularFactor(.001f);
		this.rigidBody.setAngularFactor(new Vector3f(0, 0, 1));

		// GLOBAL.jBullet.myWorld
		// EnableVelocityXYZ(body,true,true,false);
		// EnableRotationXYZ(body,false,false,true);

		// GLOBAL.jBullet.myWorld.
		// EnableWorldRotationXYZ(false,false,true);

	}

	private void addCrossPlanes() {

		// add all of our profiles in the z dir and link them to a sketch
		// int numSlices = (int)(this.width / this.slicePlaceSpacing);

	}

	/**
	 * Add a new slice plane to the design. 
	 */
	public void addNewSlicePlane() {

		SketchShape selected = this.selectedPlanes.getSelectedShape();
		SlicePlane selectedPlane = this.selectedPlanes.getSelectedShapePlane();

		if (selected != null && selectedPlane != null) {
			if (selected instanceof SketchSpline) {
				SketchSpline SketchSpline = (SketchSpline) selected;
				selected = SketchSpline.getPath();
			}

			CrossSliceSelection selection = new CrossSliceSelection(selected,
					selectedPlane, .1f, .5f, 25f, this);
			selection.editing = true;
			this.crossSliceSelections.add(selection);

		}

	}

	/**
	 * Add a new layer to the design. 
	 */
	public void addLayer() {
		if (this.selectedPlanes.size() > 0) {
			SlicePlane copyPlane = this.selectedPlanes.getFirst().clone();

			int index = this.getSlicePlanesY().indexOf(
					this.selectedPlanes.getFirst());

			this.getSlicePlanesY().add(index, copyPlane);
			this.rebuildWidth();

		} else {
			SlicePlane copyPlane = this.getSlicePlanesY().getFirst().clone();
			this.getSlicePlanesY().add(copyPlane);
			this.rebuildWidth();

		}
		//  SETTINGS.slat_num++;

	}

	/**
	 * Set the current designs position as the start position to start from after rewinding. 
	 */
	public void setCurrentPositionAsStartTransform() {
		Transform startTransform = new Transform();

		Vector3f localInertia = new Vector3f(0, 0, 0);
		float mass = this.getMass();
		trimesh.calculateLocalInertia(mass, localInertia);
		startTransform.origin.set(
				this.rigidBody.getMotionState().getWorldTransform(
						currentWorldTransform).origin.x,
				this.rigidBody.getMotionState().getWorldTransform(
						currentWorldTransform).origin.y,
				this.rigidBody.getMotionState().getWorldTransform(
						currentWorldTransform).origin.z);

		DefaultMotionState myMotionState = new DefaultMotionState(
				startTransform);

		this.startWorldTransform = startTransform;
	}

	/**
	 * Return the centre position of the design in physics space. 
	 * @return
	 */
	public Vec3D getPhysicsOrigin() {

		if (currentWorldTransform == null || rigidBody == null)
			return new Vec3D(0, 0, 0);

		float scaleBy = 1f / GLOBAL.jBullet.getScale();
		Transform myTransform = new Transform();
		myTransform = rigidBody.getMotionState().getWorldTransform(myTransform);

		Vector3f center = new Vector3f();
		rigidBody.getCenterOfMassPosition(center);

		return new Vec3D(center.x, center.y, center.z);
	}
	
	
	

	/**
	 * Generate a collision mesh for the design and add this to the physics engine.
	 */
	public void addRigidModel() {
		// create trimesh
		//if already contains a rigid model remove it.
		if (this.chairCentreConstraint != null)
			GLOBAL.jBullet.myWorld.removeConstraint(this.chairCentreConstraint);

		if (this.rigidBody != null) {
			removeRigidModel();
			GLOBAL.jBullet.myWorld.removeRigidBody(this.rigidBody);
			GLOBAL.jBullet.rigidBodies.remove(this.rigidBody);
			GLOBAL.jBullet.myWorld.stepSimulation(1);
		}

		Vec3D centreOfMass = this.getCentreOfMass();
		this.centreOfMass = centreOfMass;
		indexVertexArrays = this.getVertexArray(centreOfMass.x, centreOfMass.y,
				centreOfMass.z);
		//this.buildCoverMesh(indexVertexArrays, centreOfMass);

		if (indexVertexArrays == null)
			return;

		// if(indexVertexArrays.getIndexedMeshArray().size() < 2)
		// return;

		trimesh = new GImpactMeshShape(indexVertexArrays);
		trimesh.setLocalScaling(new Vector3f(1f, 1f, 1f));
		trimesh.updateBound();

		if (trimesh == null)
			return;

		CollisionDispatcher dispatcher = (CollisionDispatcher) GLOBAL.jBullet.myWorld
				.getDispatcher();
		if (dispatcher == null)
			return;

		GImpactCollisionAlgorithm.registerAlgorithm(dispatcher);
		Transform startTransform = new Transform();

		Vector3f localInertia = new Vector3f(0, 0, 0);
		float mass = this.getMass();
		trimesh.calculateLocalInertia(mass, localInertia);
		startTransform.origin.set(centreOfMass.x, centreOfMass.y,
				centreOfMass.z);
		DefaultMotionState myMotionState = new DefaultMotionState(
				startTransform);

		this.startWorldTransform = startTransform;
		RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass,
				myMotionState, trimesh, localInertia);
		rigidBody = new RigidBody(rbInfo);
		Transform center = new Transform();
		center.origin.set(centreOfMass.x, centreOfMass.y, centreOfMass.z);
		rigidBody.setCenterOfMassTransform(center);

		rigidBody.setDamping(SETTINGS.chair_damping_linear,
				SETTINGS.chair_damping_ang);
		rigidBody.setDeactivationTime(0.8f);
		rigidBody.setSleepingThresholds(1.6f, 2.5f);
		rigidBody.setFriction(SETTINGS.chair_friction);

		// rigidBody.setCcdMotionThreshold(1f);
		GLOBAL.jBullet.myWorld.addRigidBody(rigidBody);
		GLOBAL.jBullet.rigidBodies.add(rigidBody);

		GImpactCollisionAlgorithm.registerAlgorithm(GLOBAL.jBullet.dispatcher);

		// just to load everything in
		GLOBAL.jBullet.update();
		rigidBody.getWorldTransform(this.currentWorldTransform);

		this.addCentrePlaneConstraint();
	}
	
	
	

	/**
	 * Add the current design to the 2D shape packing engine.
	 */
	void addToShapePack() {

		this.buildRender();
		
		GLOBAL.shapePack.empty();
		// GLOBAL.shapePack.pdf_pixels_per_mm = SETTINGS.pixels_per_mm;
		GLOBAL.shapePack.content_scale = SETTINGS.scale;
		//GLOBAL.shapePack.ZOOM = ((float) GLOBAL.applet.height) / 390f;
		for (int i = 0; i < this.getSlicePlanesY().size(); i++) {
			SlicePlane slicePlane = this.getSlicePlanesY().get(i);

			if (!slicePlane.guide) {
				spShape shape = slicePlane.getspShape();
				shape.shapePack = GLOBAL.shapePack; //hacky

				shape.build();
				shape.setLabel(((char) ('A' + i)) + "");
				shape.linkedObject = slicePlane;
				GLOBAL.shapePack.add(shape);
			}
		}

		for (int i = 0; i < this.slicePlanesSlatSlices.size(); i++) {
			SlicePlane slicePlane = this.slicePlanesSlatSlices.get(i);
			if (!slicePlane.guide) {
				spShape shape = slicePlane.getspShape();
				shape.shapePack = GLOBAL.shapePack; //hacky

				shape.build();
				shape.setLabel("" + (i + 1));
				shape.linkedObject = slicePlane;
				GLOBAL.shapePack.add(shape);
			}
		}
		GLOBAL.shapePack.scaleAll(SETTINGS.scale);
		GLOBAL.shapePack.build();
		this.buildPreview();

		/*
		 * 
		 * spShape coverShape = new spShape(); ArrayList l = new ArrayList();
		 * l.add(new Vec2D(0, 0)); l.add(new Vec2D(this.coverWidth, 0));
		 * l.add(new Vec2D(this.coverWidth, this.coverLength)); l.add(new
		 * Vec2D(0, this.coverLength)); coverShape.addOutline(l);
		 * GLOBAL.shapePack.add(coverShape);
		 */
	}
	
	
	
	/**
	 * Add the current design to the 2D shape packing engine.
	 */
	public void addToPreviewShapePack(spShapePack shapePack) {

		LOGGER.debug("addToPreviewShapePack");
		for (int i = 0; i < this.getSlicePlanesY().size(); i++) {
			SlicePlane slicePlane = this.getSlicePlanesY().get(i);

			if (!slicePlane.guide) {
				spShape shape = slicePlane.getspShape();
				shape.shapePack = shapePack; //hacky
				shape.build();
				shape.linkedObject = slicePlane;
				shapePack.add(shape);
			}
		}

		for (int i = 0; i < this.slicePlanesSlatSlices.size(); i++) {
			SlicePlane slicePlane = this.slicePlanesSlatSlices.get(i);
			if (!slicePlane.guide) {
				spShape shape = slicePlane.getspShape();
				shape.shapePack = shapePack; //hacky

				shape.build();
				shape.linkedObject = slicePlane;
				shapePack.add(shape);
			}
		}

	}
	
	
	/**
	 * Apply the current translation matrix from the physics mesh in the physics engine to the display context.
	 * @param g
	 */
	public void applyTranslationMatrix(PGraphics g) {

		if (this.built == true) {

			if (rigidBody == null)
				return;

			float scaleBy = 1f / GLOBAL.jBullet.getScale();
			Transform myTransform = new Transform();
			myTransform = rigidBody.getMotionState().getWorldTransform(
					myTransform);

			Vector3f center = new Vector3f();
			rigidBody.getCenterOfMassPosition(center);

			g.translate(myTransform.origin.x * scaleBy,
					(myTransform.origin.y * scaleBy),
					(myTransform.origin.z * scaleBy));
			// g.rotateX((float) Math.PI);
			g.applyMatrix(myTransform.basis.m00, myTransform.basis.m01,
					myTransform.basis.m02, 0, myTransform.basis.m10,
					myTransform.basis.m11, myTransform.basis.m12, 0,
					myTransform.basis.m20, myTransform.basis.m21,
					myTransform.basis.m22, 0, 0, 0, 0, 1);
			g.rotateX((float) Math.PI);
			g.translate(-this.centreOfMass.x * scaleBy,
					(-this.centreOfMass.y * scaleBy), -this.centreOfMass.z
							* scaleBy);

		}

	}

	
	/**
	 * Build all parts of the chair at preview quality for editing
	 */
	public void buildPreview() {
		GLOBAL.SketchGlobals.BEZIER_DETAIL_CALCULATIONS = SETTINGS_SKETCH.BEZIER_DETAIL_CALCULATIONS_PREVIEW;
		GLOBAL.SketchGlobals.BEZIER_DETAIL_OFFSET = SETTINGS_SKETCH.BEZIER_DETAIL_OFFSET_PREVIEW;
		build();
	}
	
	
	/**
	 * Build all parts of the chair at render quality for exporting or saving.
	 */
	public void buildRender() {
		GLOBAL.SketchGlobals.BEZIER_DETAIL_CALCULATIONS = SETTINGS_SKETCH.BEZIER_DETAIL_CALCULATIONS_RENDER;
		GLOBAL.SketchGlobals.BEZIER_DETAIL_OFFSET = SETTINGS_SKETCH.BEZIER_DETAIL_OFFSET_RENDER;
	build();
	}
	
	
	/**
	 * Rebuild all parametric parts of the chairs.
	 */
	public void build() {
		
		LOGGER.debug("build");
		
		setPlaneWidth(SETTINGS.materialThickness);

		this.getSlicePlanesY().build();
		SlicePlane lastPlane = null;

		
		selectedPlanes.empty();
		for (int i = 0; i < this.getSlicePlanesY().getList().size(); i++) {
			SlicePlane plane = this.getSlicePlanesY().getList().get(i);
			if (plane.isSelected()){
				this.selectedPlanes.add(plane);
			}
		}

		this.builtOuline = true;
		this.updateCollisionMesh = true;
		this.personSeated = false;
		this.buildLen();
		this.setPlaneWidth(this.materialWidth);
		this.built = true;

		
		//rebuild the preview pattern
		slicePlanesSlatSlices.setEditable(false);
		if(GLOBAL.previewWidget != null && GLOBAL.previewWidget.visible)
			GLOBAL.previewWidget.rebuildPatternPreview();
	}

	/**
	 * Add a mesh to the physics engine to simulate a cover. 
	 * 
	 * @param triangleArray
	 * @param offset
	 */
	void buildCoverMesh(TriangleIndexVertexArray triangleArray, Vec3D offset) {
		// this.feltCover = new FeltCover();

		/*
		 * return new TriangleIndexVertexArray(TrianglesNum, this
		 * .getIndexBuffer(gIndices), 4 * 3, VerticesNum / 3, this
		 * .getVertexBuffer(gVertices), 4 * 3);
		 */

		SlicePlane lastPlane = null;

		for (int i = 0; i < this.getSlicePlanesY().getList().size(); i++) {
			SlicePlane plane = this.getSlicePlanesY().getList().get(i);
			plane.buildCoverPath();
			if (lastPlane != null) {
				IndexedMesh vertexArray = lastPlane.loftCollisonMeshBetween(
						lastPlane.coverPath, plane, plane.coverPath, 0.2f,
						offset);
				if (vertexArray != null) {
					triangleArray.addIndexedMesh(vertexArray);
					// System.out.println("got mesh");

				}
			}

			lastPlane = plane;
		}
	}

	
	
	/**
	 * Generate slices on design.
	 */
	public void buildLen() {
		if (creossSelectionTempOver == null
				&& this.getSlicePlanesY().size() < 1)
			return;
		
		
		LOGGER.debug("buildLen");

		

		//clear the cross planes of this object
		this.slicePlanesSlatSlices.destroyPlanes();
		this.slicePlanesSlatSlices.empty();

		//clear all slots
		for (int j = 0; j < this.getSlicePlanesY().size(); j++) {
			SlicePlane tempPlane = this.getSlicePlanesY().get(j);
			tempPlane.getSketch().getSlots().empty();
		}

		//what does this do?
		for (int s = 0; s < this.crossSliceSelections.l.size(); s++) {
			CrossSliceSelection crossSliceSelection;

			crossSliceSelection = (CrossSliceSelection) this.crossSliceSelections.l
					.get(s);

			//destroy selection
			if (crossSliceSelection.path != null
					&& crossSliceSelection.path.isDestroying() == true)
				crossSliceSelection.destroy(null);

			Object sketch = crossSliceSelection.path;
			SketchShape guideSpline = (SketchShape) sketch;

			//if (guideSpline == null || guideSpline.size() < 2) 
			//	return;

			if (crossSliceSelection != null
					&& crossSliceSelection.type == CrossSliceSelection.SLICES
					|| crossSliceSelection.type == CrossSliceSelection.LEG
					|| crossSliceSelection.type == CrossSliceSelection.SINGLE_SLICE)
				GeometryOperations.generateSlices(this.slicePlanesY,
						this.slicePlanesSlatSlices, crossSliceSelection,
						guideSpline);

			if (crossSliceSelection != null
					&& (crossSliceSelection.type == CrossSliceSelection.PLANE_ON_EDGE || crossSliceSelection.type == CrossSliceSelection.PLANE))
				GeometryOperations.generateSlat(this.slicePlanesY,
						this.slicePlanesSlatSlices, crossSliceSelection,
						guideSpline, this);

			if (crossSliceSelection != null
					&& crossSliceSelection.type == CrossSliceSelection.SLATSLICES)
				GeometryOperations.generateSlatSlices(this.slicePlanesY,
						this.slicePlanesSlatSlices, crossSliceSelection,
						guideSpline, this);

		}

		//this.slicePlanesSlatSlices.checkForCollisions();
		if (SETTINGS.useSliceCollisionDetection)
			this.slicePlanesY.checkForCollisions();
		
		
		selectedPlanes.empty();
		

		for (int i = 0; i < slicePlanesSlatSlices.size(); i++) {
			SlicePlane plane = slicePlanesSlatSlices.get(i);
			if(plane.getCrossSliceSelection() != null && plane.getCrossSliceSelection().selected){
				selectedPlanes.add(plane);
			}else{
				plane.unselect();
			}
		}
		
		
		for (int i = 0; i < slicePlanesY.size(); i++) {
			SlicePlane plane = slicePlanesY.get(i);
			if(plane.isSelected()){
				selectedPlanes.add(plane);
			}else{
				plane.unselect();
			}
		}

		//this.slicePlanesSlatSlices.unselectAll();

	}

	
	/**
	 * Copy initial Sketch and automatically generate additional layers from this.
	 */
	public void buildWidth() {

		LOGGER.debug("buildWidth");

		
		this.getSlicePlanesY().empty();
		this.setWidth(SETTINGS.chair_width);
		int numSlices = (int) SETTINGS.slat_num;// (int)
		float spacing = this.getWidth() / (numSlices);
		
		

		for (float i = (-this.getWidth() / 2); i < (this.getWidth() / 2); i += spacing) {
			SlicePlane slicePlane = new SlicePlane(new Plane(
					new Vec3D(0, 0, i), new Vec3D(0, 0, -1)));
			this.getSlicePlanesY().add(slicePlane);
		}

		//this.updateCollisionShape();
		this.selectedPlanes.empty();
		
		this.getSlicePlanesY().selectAll();
		this.selectedPlanes.add(getSlicePlanesY());

	}

	/**
	 * Change the width of the current design by a delta ammount. 
	 * @param deltaMouseY
	 */
	public void changeWidth(float deltaMouseY) {
		// System.out.println(deltaMouseY);
		if (this.getSlicePlanesY().getList().size() > 0) {
			this.setWidth(this.getWidth() + deltaMouseY);
			this.rebuildWidth();
		}
	}


/**
 * Return a copy of the design. 
 * @return
 */
	public SketchChair copy() {
		SketchChair newSketchChair = new SketchChair();
		newSketchChair.slatSpacingX = this.slatSpacingX;
		newSketchChair.setSlicePlanesY(this.getSlicePlanesY().copy());
		newSketchChair.crossSliceSelections = this.crossSliceSelections.copy(
				newSketchChair.getSlicePlanesY(), newSketchChair);
		return newSketchChair;

	}

	/*
	 * Return a count of the number of the selected nodes in the design. 
	 */
	public int countSelectedNodes() {
		return this.getSlicePlanesY().countSelectedNodes();

	}

	/**
	 * Delete selected layers. 
	 */
	public void deleteSelectedLayers() {
		this.selectedPlanes.destroyPlanes();
		this.selectedPlanes.update();
		this.slicePlanesSlatSlices.update();
		this.getSlicePlanesY().update();
		this.selectedPlanes.unselectAll();
	}

	
	/**
	 * Delete selected Shapes. 
	 */
	public void deleteSelectedShapes() {
		this.selectedPlanes.deleteSelectedShapes();
	}

	/*
	 * private TriangleIndexVertexArray getVertexArray() {
	 * 
	 * float colResolution = 0.1f;
	 * 
	 * 
	 * int cSizeFirst =
	 * this.slicePlanesY.getFirst().sketchSpline.getCombinedSize(); int
	 * cSizeNext = this.slicePlanesY.getLast().sketchSpline.getCombinedSize();
	 * 
	 * SketchOutline skOtFirst =
	 * this.slicePlanesY.getFirst().sketchShapes.sketchOutlines
	 * .getOutterOutline(); SketchOutline skOtNext =
	 * this.slicePlanesY.getLast().
	 * sketchShapes.sketchOutlines.getOutterOutline();
	 * 
	 * 
	 * 
	 * 
	 * 
	 * Vec2D centre = this.sketchSpline.getCentreOfMass();
	 * 
	 * int VerticesNum = ((cSizeFirst +cSizeNext)) *3; int TrianglesNum =
	 * (cSizeFirst +cSizeNext) * 2 ;
	 * 
	 * 
	 * int IndicesNum = (TrianglesNum) * 4; float[] gVertices = new
	 * float[VerticesNum]; int[] gIndices = new int[IndicesNum];
	 * 
	 * int verIndex = 0; int IndicesIndex = 0;
	 * 
	 * //add all verticies from end cap 1 for (int i = 0; i < 1;
	 * i+=colResolution) { Vec2D v1l = (Vec2D) skOtFirst.getDist(i);
	 * gVertices[verIndex] = v1l.x - centre.x; verIndex++; gVertices[verIndex] =
	 * -v1l.y + centre.y; verIndex++; gVertices[verIndex] =
	 * (this.slicePlanesY.getFirst().offset.z); verIndex++; }
	 * 
	 * 
	 * for (int i = 0; i < 1; i+=colResolution) { Vec2D v1r = (Vec2D)
	 * skOtNext.getDist(i); gVertices[verIndex] = v1r.x - centre.x; verIndex++;
	 * gVertices[verIndex] = -v1r.y + centre.y; verIndex++; gVertices[verIndex]
	 * = -(this.width / 2); verIndex++; }
	 * 
	 * for (int i = 0; i < cSizeFirst - 1; i++) {
	 * 
	 * // triangle 1 gIndices[IndicesIndex] = i + 1; IndicesIndex++;
	 * gIndices[IndicesIndex] = i + 0; IndicesIndex++; gIndices[IndicesIndex] =
	 * i + cSizeFirst; IndicesIndex++;
	 * 
	 * // triangle 2 gIndices[IndicesIndex] = i + 0 + cSizeNext; IndicesIndex++;
	 * gIndices[IndicesIndex] = i + 1; IndicesIndex++; gIndices[IndicesIndex] =
	 * i + 1 + cSizeNext; IndicesIndex++;
	 * 
	 * }
	 * 
	 * // triangle 1 gIndices[IndicesIndex] = 0; IndicesIndex++;
	 * gIndices[IndicesIndex] = cSizeFirst; IndicesIndex++;
	 * gIndices[IndicesIndex] = cSizeFirst - 1; IndicesIndex++;
	 * 
	 * gIndices[IndicesIndex] = cSizeFirst; IndicesIndex++;
	 * gIndices[IndicesIndex] = cSizeFirst * 2 - 1; IndicesIndex++;
	 * gIndices[IndicesIndex] = cSizeFirst- 1; IndicesIndex++;
	 * 
	 * // side triangle1
	 * 
	 * for (int i = 0; i < cSizeNext; i++) {
	 * 
	 * // triangle 1 gIndices[IndicesIndex] = i + 1; IndicesIndex++;
	 * gIndices[IndicesIndex] = i + 0; IndicesIndex++; gIndices[IndicesIndex] =
	 * (sketchFirst.l.size() - i - 1) + sketchNext.l.size(); IndicesIndex++;
	 * 
	 * // triangle 2 gIndices[IndicesIndex] = (sketchFirst.l.size() - i) +
	 * sketchNext.l.size(); IndicesIndex++; gIndices[IndicesIndex] = i + 0;
	 * IndicesIndex++; gIndices[IndicesIndex] = (sketchFirst.l.size() - i - 1) +
	 * sketchNext.l.size(); IndicesIndex++;
	 * 
	 * }
	 * 
	 * for (int i = 0; i < sketchFirst.l.size(); i++) {
	 * 
	 * // triangle 1 gIndices[IndicesIndex] = i + 1 + (cSizeNext - 1);
	 * IndicesIndex++; gIndices[IndicesIndex] = i + 0 + (cSizeNext - 1);
	 * IndicesIndex++; gIndices[IndicesIndex] = (sketchFirst.l.size() - i - 1) +
	 * sketchFirst.l.size() + (cSizeNext - 1); IndicesIndex++;
	 * 
	 * // triangle 2 gIndices[IndicesIndex] = (sketchFirst.l.size() - i) +
	 * sketchFirst.l.size() + (cSizeNext - 1); IndicesIndex++;
	 * gIndices[IndicesIndex] = i + 0 + cSizeNext; IndicesIndex++;
	 * gIndices[IndicesIndex] = (sketchFirst.l.size() - i - 1) +
	 * sketchFirst.l.size() + (cSizeNext - 1); IndicesIndex++;
	 * 
	 * }
	 * 
	 * // System.out.println(this.getVertexBuffer(gVertices).asDoubleBuffer());
	 * return new TriangleIndexVertexArray(TrianglesNum, this
	 * .getIndexBuffer(gIndices), 4 * 3, VerticesNum / 3, this
	 * .getVertexBuffer(gVertices), 4 * 3);
	 * 
	 * }
	 */
	
	/**
	 * Detroy the design.
	 */
	void destroy() {

		this.destroy = true;

		if (this.chairCentreConstraint != null)
			GLOBAL.jBullet.myWorld.removeConstraint(this.chairCentreConstraint);

		if (this.rigidBody != null)
			GLOBAL.jBullet.myWorld.removeRigidBody(this.rigidBody);

	}

	/**
	 * Move the design by a delta amount inside the physics engine. 
	 * @param offset
	 */
	public void drag(Vec3D offset) {
		Vector3f offset3f = new Vector3f(offset.x, offset.y, offset.z);
		offset3f.scale(GLOBAL.jBullet.scale);
		this.rigidBody.translate(offset3f);
		GLOBAL.jBullet.update();
		this.setCurrentPositionAsStartTransform();
		//this.startWorldTransform.origin.set(offset3f);
	}

	/**
	 * Flip the design around the Z Axis.
	 */
	public void flipDesign() {
		Vec3D centre = this.getCentreOfMass();
		this.getSlicePlanesY().flipHorizontal(centre);
		this.buildLen();
	}

	/**
	 * Get the total area of the 2D cut profiles of the design.
	 * @return
	 */
	float getArea() {
		float area = 0;
		area += this.slicePlanesSlatSlices.getArea();
		area += this.getSlicePlanesY().getArea();
		return area;

	}

	/**
	 * Return the center of mass for the current design.
	 * @return
	 */
	private Vec3D getCentreOfMass() {
		Vec3D centre = new Vec3D();
		int planeCount = 0;
		for (int i = 0; i < this.getSlicePlanesY().size(); i++) {
			SlicePlane slicePlane = this.getSlicePlanesY().get(i);
			if (slicePlane.getCentreOfMass() != null) {
				centre.addSelf(slicePlane.getCentreOfMass());
				planeCount++;
			}
		}

		centre.x /= planeCount;
		centre.y /= planeCount;
		centre.z /= planeCount;

		//cache point
		//this.centreOfMass = centre;

		return centre;

	}

	/**
	 * return the mass of the design based on it's density and surface area.
	 * @return
	 */
	float getMass() {
		return this.getArea() * this.density;
	}

	/**
	 * Return the closest SketchPoint to a point. 
	 * @param x
	 * @param y
	 * @return
	 */
	public SketchPoint getOverSelectPoint(float x, float y) {
		return this.selectedPlanes.getOverSelectPoint(x, y);
	}

	public SketchShapes getOverShapes(float x, float y) {
		return this.selectedPlanes.getOverShapes(x, y);
	}

	public SlicePlane getPlanePickBuffer(int col) {

		SlicePlane plane = this.slicePlanesSlatSlices.getPlanePickBuffer(col);
		if (plane != null)
			return plane;
		else
			return this.getSlicePlanesY().getPlanePickBuffer(col);
	}

	public PGraphics getScreenshot(float w, float h, float rotateX,
			float rotateY) {
		GLOBAL.screenshot = true;
		float border = 250;
		float rememberX = GLOBAL.rotateModelsX;
		float rememberY = GLOBAL.rotateModelsY;

		GLOBAL.rotateModelsX = rotateX;
		GLOBAL.rotateModelsY = rotateY;

		float minX = this.getSlicePlanesY().getMinXWorldSpace(
				this.currentWorldTransform);
		float minY = this.getSlicePlanesY().getMinYWorldSpace(
				this.currentWorldTransform);
		float maxX = this.getSlicePlanesY().getMaxXWorldSpace(
				this.currentWorldTransform);
		float maxY = this.getSlicePlanesY().getMaxYWorldSpace(
				this.currentWorldTransform);

		float minX2 = this.getSlicePlanesY().getMinX();
		float minY2 = this.getSlicePlanesY().getMinY();
		float maxX2 = this.getSlicePlanesY().getMaxX();
		float maxY2 = this.getSlicePlanesY().getMaxY();

		float width = Math.abs(maxX - minX) + (border * 2);
		float height = Math.abs(maxY - minY) + +(border * 2);

		float width2 = Math.abs(maxX2 - minX2) + (border * 2);
		float height2 = Math.abs(maxY2 - minY2) + (border * 2);

		// width += (width );
		// height += (height);

		// System.out.println("out: X world " + minX + ": "+maxX + ":"+width);
		// System.out.println("out: X model " + minX2 + ": "+maxX2 +
		// ":"+width2);
		// System.out.println("out: Y world " + minY + ": "+maxY + ":"+height);
		// System.out.println("out: Y model " + minY2 + ": "+maxY2 +
		// ":"+height2);

		// System.out.println(currentWorldTransform.origin.x);
		PGraphics saveImg = GLOBAL.applet.createGraphics((int) width,
				(int) height, PConstants.P3D);
		saveImg.beginDraw();
		saveImg.ortho(-(width / 2), (width / 2), -(height / 2), (height / 2),
				-1000, 10000);
		//saveImg.hint(PApplet.DISABLE_STROKE_PERSPECTIVE);

		saveImg.ambientLight(200, 200, 200);
		saveImg.directionalLight(69, 69, 69, 0, 0, -1);
		saveImg.lightFalloff(1, 0, 0);

		saveImg.smooth(8);

		saveImg.background(255);

		saveImg.pushMatrix();
		// this.applyTranslationMatrix(g);

		saveImg.translate(-minX + border, -minY + border);
		saveImg.translate(width / 2, height / 2, -(this.getWidth() / 4));
		saveImg.rotateX(rotateX);
		saveImg.rotateY(rotateY);
		saveImg.translate(-width / 2, -height / 2, (this.getWidth() / 4));
		this.render(saveImg);
		// this.renderSilhouette(saveImg);

		saveImg.popMatrix();
		saveImg.endDraw();
		GLOBAL.screenshot = false;

		GLOBAL.rotateModelsY = rememberY;
		GLOBAL.rotateModelsX = rememberX;
		return saveImg;

	}

	public SketchShape getShapePickBuffer(int col) {

		SketchShape shape = this.selectedPlanes.getShapePickBuffer(col);
		if (shape != null)
			return shape;

		shape = this.slicePlanesSlatSlices.getShapePickBuffer(col);
		if (shape != null)
			return shape;

		return this.getSlicePlanesY().getShapePickBuffer(col);

	}

	/**
	 * @return the slicePlanesY
	 */
	public SlicePlanes getSlicePlanesY() {
		return slicePlanesY;
	}

	//translates into model space
	public Vec3D getTranslated(Vec3D pIn) {
		Vec3D p = pIn.copy();
		if (this.built == true) {

			if (rigidBody == null) {
				LOGGER.debug("no rigid body in get Translated");
				return p;

			}

			float scaleBy = 1f / GLOBAL.jBullet.getScale();
			Transform myTransform = new Transform();
			myTransform = rigidBody.getMotionState().getWorldTransform(
					myTransform);

			Vector3f center = new Vector3f();
			rigidBody.getCenterOfMassPosition(center);
			LOGGER.debug("getCenterOfMassPosition" + center);
			/*
			p.addSelf(new Vec3D(center.x * scaleBy,
							(center.y * scaleBy), center.z
									* scaleBy));
				*/
			//	p.addSelf(new Vec3D(myTransform.origin.x * scaleBy,
			//			(myTransform.origin.y * scaleBy),
			//		(myTransform.origin.z * scaleBy)));
			// g.rotateX((float) Math.PI);
			Matrix4x4 m = new Matrix4x4(myTransform.basis.m00,
					myTransform.basis.m01, myTransform.basis.m02, 0,
					myTransform.basis.m10, myTransform.basis.m11,
					myTransform.basis.m12, 0, myTransform.basis.m20,
					myTransform.basis.m21, myTransform.basis.m22, 0, 0, 0, 0, 1);

			p = m.applyTo(p);

			/*
			p = p.rotateX((float) Math.PI);
			p.subSelf(new Vec3D(center.x * scaleBy,
					(center.y * scaleBy), center.z
							* scaleBy));
			*/
		} else {
			LOGGER.debug("not built in get Translated");

		}

		return p;

	}

	public Vec2D getVec2DpickBuffer(int col) {

		Vec2D vec = this.selectedPlanes.getVec2DpickBuffer(col);
		if (vec != null)
			return vec;

		vec = this.slicePlanesSlatSlices.getVec2DpickBuffer(col);
		if (vec != null)
			return vec;

		return this.getSlicePlanesY().getVec2DpickBuffer(col);
	}

	private TriangleIndexVertexArray getVertexArray(float offsetX,
			float offsetY, float offsetZ) {

		TriangleIndexVertexArray triangleArray = new TriangleIndexVertexArray();

		for (int i = 0; i < this.getSlicePlanesY().getList().size(); i++) {
			SlicePlane slicePlane = this.getSlicePlanesY().getList().get(i);
			IndexedMesh vertexArray = slicePlane.getIndexedMesh(offsetX,
					offsetY, offsetZ);
			if (vertexArray != null)
				triangleArray.addIndexedMesh(vertexArray);
			// slicePlane.getIndexedMesh();
		}

		for (int i = 0; i < this.slicePlanesSlatSlices.getList().size(); i++) {
			SlicePlane slicePlane = this.slicePlanesSlatSlices.getList().get(i);
			IndexedMesh vertexArray = slicePlane.getIndexedMesh(offsetX,
					offsetY, offsetZ);
			if (vertexArray != null) {
				triangleArray.addIndexedMesh(vertexArray);
			}
			// slicePlane.getIndexedMesh();
		}

		if (this.getSlicePlanesY().getList().size() > 0)
			return triangleArray;
		else
			return null;

		/*
		 * return new TriangleIndexVertexArray(TrianglesNum, this
		 * .getIndexBuffer(gIndices), 4 * 3, VerticesNum / 3, this
		 * .getVertexBuffer(gVertices), 4 * 3);
		 */

	}

	/**
	 * @return the width
	 */
	public float getWidth() {
		return width;
	}

	public void hybernate() {

		this.hybernateCopy();
		this.destroy();
	}

	public void hybernateCopy() {
		float border = 15;
		GLOBAL.uiTools.hybernateChairToFileAuto(null);
		Environment chair = new Environment(
				GLOBAL.LAST_SAVED_LOCATION + ".png", GLOBAL.applet);
		float minX = this.getSlicePlanesY().getMinXWorldSpace(
				this.currentWorldTransform);
		float minY = this.getSlicePlanesY().getMinYWorldSpace(
				this.currentWorldTransform);
		float maxX = this.getSlicePlanesY().getMaxXWorldSpace(
				this.currentWorldTransform);
		float maxY = this.getSlicePlanesY().getMaxYWorldSpace(
				this.currentWorldTransform);

		float width = Math.abs(maxX - minX);
		float height = Math.abs(maxY - minY);
		// System.out.println(GLOBAL.LAST_SAVED_LOCATION);
		// System.out.println("DIFF" + (this.startWorldTransform.origin.y -
		// this.currentWorldTransform.origin.y));
		chair.pos.x = (minX) - imgBorder;// +
											// ((this.startWorldTransform.origin.x
											// -
											// this.currentWorldTransform.origin.x)/GLOBAL.jBullet.scale);
		chair.pos.y = (minY) - imgBorder;// -
											// ((this.startWorldTransform.origin.y
											// -
											// this.currentWorldTransform.origin.y)/GLOBAL.jBullet.scale);

		chair.linkedChair = GLOBAL.LAST_SAVED_LOCATION;
		GLOBAL.environments.l.add(chair);
	}

	/**
	 * @return the reScaling
	 */
	public boolean isReScaling() {
		return reScaling;
	}

	/**
	 * @return the updateCollisionMesh
	 */
	public boolean isUpdateCollisionMesh() {
		return updateCollisionMesh;
	}

	void justLoaded() {
		// this.trimesh = null;
		this.selectedPlanes.empty();
		this.selectedPlanes.add(this.getSlicePlanesY());

		// this.slicePlanesY.buildCurrentSketch();
		//
		// this.selectedPlanes.buildCurrentSketch();
		// this.updateCollisionMesh = true;

		this.setUpdateCollisionMesh(true);
		this.personSeated = false;

		// this.build();
		// addRigidModel();

		this.update();
	}

	boolean lastSketchOverlaps() {
		return this.selectedPlanes.lastSketchOverlaps();

	}

	// renders a Silhoette to a file, this is
	// without the chairs translation matrix so the silohouette will need to be
	// translated later

	private void makeNewChairFromLastSketch() {

		SketchShape lastShape = this.selectedPlanes.getFirst().getLastSketch();
		SketchChair newChair = new SketchChair(lastShape);

		this.selectedPlanes.removeLastSketch();

		if (SETTINGS.EXHIBITION_MODE) {
			// if (this.rigidBody != null)
			// GLOBAL.jBullet.myWorld.removeRigidBody(this.rigidBody);

			// this.selectedPlanes.buildCurrentSketch();

			// addRigidModel();

			// return;
		}

		this.hybernate();

		Vec3D thisCentre = this.getCentreOfMass();

		Transform worldTransform = new Transform();
		rigidBody.getMotionState().getWorldTransform(worldTransform);
		// worldTransform.origin.setX(
		// this.startWorldTransform.origin.x-worldTransform.origin.x );

		// worldTransform.origin.y - this.startWorldTransform.origin.y,
		// worldTransform.origin.z - this.startWorldTransform.origin.z

		// ));
		Vec3D thatCentre = newChair.getCentreOfMass();
		// System.out.println("ORIGIN" + worldTransform.origin.y+ " : " +
		// (thisCentre.y) + ": " + thatCentre.y);

		worldTransform.origin.x = (worldTransform.origin.x + (thatCentre.x - thisCentre.x));
		worldTransform.origin.y = (worldTransform.origin.y + ((thatCentre.y - thisCentre.y)));
		worldTransform.origin.z = (worldTransform.origin.z + (thatCentre.z - thisCentre.z));
		//
		//
		//
		// = new Vector3f(,
		// thisCentre.y-thatCentre.y,
		// thisCentre.z-thatCentre.z);
		newChair.rigidBody.setWorldTransform(worldTransform);
		// newChair.drag(new
		// Vec3D((this.startWorldTransform.origin.x-worldTransform.origin.x)
		// ,(worldTransform.origin.y-this.startWorldTransform.origin.y),0));

		GLOBAL.sketchChairs.add(newChair);

	}

	public void mouseDragged(float mouseX, float mouseY) {
		//if(GLOBAL.person.mouseOver(mouseX, mouseY))
		//	return;

		if (GLOBAL.person.clickedOnPerson)
			return;

		if (GLOBAL.uiTools.mouseButton != UITools.MOUSE_LEFT)
			return;

		this.selectedPlanes.mouseDragged(mouseX, mouseY);

		if (GLOBAL.uiTools.getCurrentTool() == UITools.SCALE_TOOL
				&& this.isReScaling()) {
			float scaleVal = (GLOBAL.uiTools.pmouseY - GLOBAL.uiTools.mouseY);
			scaleVal *= .01;
			this.scale(scaleVal);
		}

		if (GLOBAL.uiTools.getCurrentTool() == UITools.MOVE_2D_TOOL
				|| GLOBAL.uiTools.SketchTools.getCurrentTool() == SketchTools.SELECT_TOOL
				&& this.dragging) {

			float deltaMx = (GLOBAL.uiTools.mouseX - GLOBAL.uiTools.pmouseX)
					* (float)(1 / GLOBAL.getZOOM());
			float deltaMy = (float) ((GLOBAL.uiTools.mouseY - GLOBAL.uiTools.pmouseY)
					* (float)(1 / GLOBAL.getZOOM()));

			float maxMouse = SETTINGS.mouseMoveClamp;

			if (deltaMx > maxMouse || deltaMx < -maxMouse || deltaMy > maxMouse
					|| deltaMy < -maxMouse)
				return;

			this.drag(new Vec3D(deltaMx, deltaMy, 0));
		}
	}

	boolean mouseOver(float mouseX, float mouseY) {

		RigidBody selectedBod = GLOBAL.jBullet.getOver(mouseX, mouseY);

		if (selectedBod != null && this.rigidBody != null
				&& this.rigidBody.equals(selectedBod)) {
			return true;
		}
		return false;
	}

	public void mousePressed(float mouseX, float mouseY) {

		if (GLOBAL.person.mouseOver(GLOBAL.uiTools.mouseXworld,
				GLOBAL.uiTools.mouseYworld))
			return;

		
		if (GLOBAL.uiTools.mouseButton == UITools.MOUSE_MIDDLE)
			return;

		
		selectedPlanes.mousePressed(mouseX, mouseY);

		
		if (GLOBAL.uiTools.getCurrentTool() == UITools.SCALE_TOOL) {

			if (this.mouseOver(mouseX, mouseY)) {
				this.setReScaling(true);
			}
		}

		
		
		if (GLOBAL.uiTools.getCurrentTool() == UITools.MOVE_2D_TOOL
				|| GLOBAL.uiTools.SketchTools.getCurrentTool() == SketchTools.SELECT_TOOL
				&& this.rigidBody != null) {

			if (this.mouseOver(GLOBAL.uiTools.mouseXworld,
					GLOBAL.uiTools.mouseYworld)
					&& !overSelectPoint(GLOBAL.uiTools.mouseX,
							GLOBAL.uiTools.mouseY)
					&& !GLOBAL.jBullet.physics_on) {
				LOGGER.debug("DRAG");
				this.dragging = true;

			}

		}


	}

	public void mouseReleased(float mouseX, float mouseY) {

		
		
		
		if (GLOBAL.person.clickedOnPerson)
			return;

		if (GLOBAL.uiTools.mouseButton == UITools.MOUSE_MIDDLE)
			return;
		
		selectedPlanes.mouseReleased(mouseX, mouseY);

		if (this.isReScaling()) {
			// this.built = false;
			//	this.updateCollisionShape();

		}
		
		this.crossSliceSelections.mouseReleased();
		
		
		//Sketch Slices finished?
		if(creossSelectionTempOver != null){
			
			
			creossSelectionTempOver.editing = true;
			creossSelectionTempOver.mouseReleased();
			//finished editing
			if(!creossSelectionTempOver.editing){

				if(creossSelectionTempOver.tempSlice){

				this.crossSliceSelections.add(creossSelectionTempOver);
				creossSelectionTempOver.tempSlice = false;
				}
				
				creossSelectionTempOver = null;
				GLOBAL.uiTools.setCurrentTool(UITools.SELECT_TOOL);
				this.buildLen();

			}
		}
		

		
		if(this.selectedPlanes.count() == 0)
			return;
		
		
		this.setReScaling(false);
		this.dragging = false;

		this.selectedPlanes.buildCurrentSketch();
		this.setUpdateCollisionMesh(true);

		
		
		//add initial 
		if (!this.initailSliceAdded && getMass() > 0) {
			LOGGER.debug("initailSliceAdded");

			SlicePlane extrudeSlice = this.getSlicePlanesY().getFirst();
			Object spline = extrudeSlice.getSketch().getLast();

			if (spline != null && spline instanceof SketchPath
					&& !((SketchPath) spline).getClosed())
				return;

			this.initailSliceAdded = true;

			if (spline != null && spline instanceof SketchSpline) {
				

				SketchSpline sketchSpline = (SketchSpline) spline;
				CrossSliceSelection sliceSelection = new CrossSliceSelection(
						sketchSpline.getCentrePath(), extrudeSlice, 0, 1,
						SETTINGS.DEFAULT_SLAT_SPACING, this);
				sliceSelection.cropToCurrentShape = true;
				this.crossSliceSelections.add(sliceSelection);
				
				
				
			}

			if (spline instanceof SketchSpline) {

			}

			if (spline != null && spline instanceof SketchPath) {
				SketchPath path = (SketchPath) spline;
				path.setClosed(true);
				this.crossSliceSelections.add(new CrossSliceSelection(path,
						extrudeSlice, this.endCoverPercent,
						this.startCoverPercent, SETTINGS.DEFAULT_SLAT_SPACING,
						this));
				GLOBAL.uiTools.setCurrentTool(UITools.CROSSSLICE_EDIT);
			}
		} else {
			if (GLOBAL.uiTools.SketchTools.getCurrentTool() == SketchTools.LEG_TOOL
					&& SETTINGS.addLegSlices) {

				SlicePlane extrudeSlice = this.selectedPlanes.getFirst();
				Object spline = extrudeSlice.getSketch().getLast();
				//CrossSliceSelection lastSliceSelection = this.crossSliceSelections.getLast();

				//build a list of all leg splines
				List legSplines = new ArrayList();
				
				for(int i = 0; i < this.selectedPlanes.size(); i++){
					
					this.selectedPlanes.get(i).getSketch().getLast().setType(SketchSpline.TYPE_LEG);
					legSplines.add(this.selectedPlanes.get(i).getSketch().getLast());
				}
				//SketchShape shape = lastSliceSelection.path;

				//if (!shape.equals(spline)) {
				if (spline instanceof SketchSpline) {
					SketchSpline sketchSpline = (SketchSpline) spline;

					if (sketchSpline.getCentrePath().size() == 2
							&& sketchSpline
									.getCentrePath()
									.get(0)
									.distanceTo(
											sketchSpline.getCentrePath().get(1)) > SETTINGS.MIN_LEG_LEN) {
						
						
						CrossSliceSelection sliceSelection = new CrossSliceSelection(
								sketchSpline.getCentrePath(), extrudeSlice,
								this);
						extrudeSlice.tiedToLeg = true;
						sliceSelection.tieToLeg = true;
						sliceSelection.legSpline = sketchSpline;
						sliceSelection.legSplines = legSplines;
						sliceSelection.type = CrossSliceSelection.LEG;
								sliceSelection.tiedToPlanes.addAll(this.selectedPlanes.getList());
						LOGGER.debug("TIED" + this.selectedPlanes.getList().size());
						sliceSelection.cropToCurrentShape = false;
						this.crossSliceSelections.add(sliceSelection);
					}

				}

			}

		}

		if (this.built) {

			if (this.getSlicePlanesY().count() == 1) {
				this.personSeated = false;
			}

		}

		if (GLOBAL.uiTools.SketchTools.getCurrentTool() != SketchTools.DRAW_PATH_TOOL)
			this.buildPreview();

		if (this.isReScaling()) {
			this.setReScaling(false);
			//	this.updateCollisionShape();
		}
		this.getSlicePlanesY().buildCurrentSketch();

	}

	
	public void mouseDoubleClick(int mouseX, int mouseY) {
		this.getSlicePlanesY().mouseDoubleClick(mouseX,mouseY);		
	}
	
	public boolean overSelectPoint(float mouseX, float mouseY) {
		if (this.selectedPlanes.overSelectPoint(mouseX, mouseY))
			return true;
		else
			return false;
	}

	// / Build all of the cross slats.
	// /
	public void previewSlices() {
		if (creossSelectionTempOver != null) {
						
			CrossSliceSelection crossSliceSelection = creossSelectionTempOver;

			//clear the cross planes of this object
			this.slicePlanesSlatSlices.destroyPlanes();
			this.slicePlanesSlatSlices.empty();

			Object sketch = crossSliceSelection.path;
			SketchShape guideSpline = (SketchShape) sketch;

			if (crossSliceSelection != null
					&& crossSliceSelection.type == CrossSliceSelection.SLICES
					|| crossSliceSelection.type == CrossSliceSelection.LEG
					|| crossSliceSelection.type == CrossSliceSelection.SINGLE_SLICE)
				GeometryOperations.generateSlices(this.slicePlanesY,
						this.slicePlanesSlatSlices, crossSliceSelection,
						guideSpline);

			if (crossSliceSelection != null
					&& (crossSliceSelection.type == CrossSliceSelection.PLANE_ON_EDGE || crossSliceSelection.type == CrossSliceSelection.PLANE))
				GeometryOperations.generateSlat(this.slicePlanesY,
						this.slicePlanesSlatSlices, crossSliceSelection,
						guideSpline, this);

			if (crossSliceSelection != null
					&& crossSliceSelection.type == CrossSliceSelection.SLATSLICES)
				GeometryOperations.generateSlatSlices(this.slicePlanesY,
						this.slicePlanesSlatSlices, crossSliceSelection,
						guideSpline, this);

		}

	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		try {
			// our "pseudo-constructor"
			in.defaultReadObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		// now we are a "live" object again, so let's run rebuild and start
		currentWorldTransform = new Transform();
		this.buildPreview();

	}

	private void rebuildWidth() {
		float centreZ = 0;// this.slicePlanesY.getCentre().z;
		// System.out.println(centreZ);

		// System.out.println((this.slicePlanesY.l.size() - 1) + "planes");

		float spacing = (float) this.getWidth()
				/ ((float) (this.getSlicePlanesY().getList().size()) - 1.5f);
		float offset = -(this.getWidth() / 2.0f);

		// System.out.println("width" + this.width);
		// System.out.println("spacing" + spacing);
		// System.out.println("offset" + offset);
		// System.out.println("num planes" + this.slicePlanesY.l.size());

		for (SlicePlane plane : this.getSlicePlanesY().getList()) {
			plane.getPlane().z = offset;
			offset += spacing;

		}
		//this.updateCollisionShape();
		GLOBAL.jBullet.update();
		// this.update();

		// his.buildLen();
	}

	public void removeRigidModel() {

		if (rigidBody != null)
			GLOBAL.jBullet.myWorld.removeRigidBody(rigidBody);

	}

	public void removeSelectedPlanes() {
		/*
		if (this.selectedPlanes.getList().size() > 0) {
			SlicePlane destroyPlane = this.selectedPlanes.getFirst();
			this.selectedPlanes.empty();
			this.getSlicePlanesY().remove(destroyPlane);
			destroyPlane.destroy = true;
			this.rebuildWidth();
		} else {
			SlicePlane destroyPlane = this.getSlicePlanesY().getLast();
			this.selectedPlanes.empty();
			this.getSlicePlanesY().remove(destroyPlane);
			destroyPlane.destroy = true;
			this.rebuildWidth();

		}
		
		*/
		this.selectedPlanes.destroyPlanes();
		this.rebuildWidth();

		//SETTINGS.slat_num--;

	}

	

	/*	Main Render Function
	 * This does the normal rendering when editing or previewing the main design
	 */
	public void render(PGraphics g) {

		if(this.selectedPlanes.count() > 0){
		this.slicePlanesY.setRenderMode(Sketch.RENDER_3D_EDITING_PLANES);
		this.slicePlanesSlatSlices.setRenderMode(Sketch.RENDER_3D_EDITING_PLANES);
		}else{
		this.slicePlanesY.setRenderMode(Sketch.RENDER_3D_NORMAL);
		this.slicePlanesSlatSlices.setRenderMode(Sketch.RENDER_3D_NORMAL);	
		}
		
		if(GLOBAL.rotateModelsX != 0 || GLOBAL.rotateModelsY != 0){
			this.slicePlanesY.setRender3D(true);
			this.slicePlanesSlatSlices.setRender3D(true);
		}else{
			this.slicePlanesY.setRender3D(false);
			this.slicePlanesSlatSlices.setRender3D(false);
		}
		g.pushMatrix();

		this.applyTranslationMatrix(g);
		GLOBAL.renderChairColour = this.chairColour;

		//only render slice planes if not resizing the model
		if (!this.isReScaling()) {	
			
			this.slicePlanesSlatSlices.render(g);
		}
		this.crossSliceSelections.render(g);
		this.getSlicePlanesY().render(g);
		
		
		//g.hint(g.DISABLE_DEPTH_TEST);
		//this.selectedPlanes.render(g);
		//g.hint(g.ENABLE_DEPTH_TEST);

		if (SETTINGS.DEBUG) {
			g.pushMatrix();
			g.translate(this.centreOfMass.x / GLOBAL.jBullet.scale,
					this.centreOfMass.y / GLOBAL.jBullet.scale,
					this.centreOfMass.z / GLOBAL.jBullet.scale);
			g.ellipse(0, 0, 25, 25);
			g.line(-25, 0, 25, 0);
			g.line(0, -25, 0, 25);
			g.popMatrix();
		}

		
		
		//measure tool
		if (this.isReScaling()) {
			float maxY = this.getSlicePlanesY().getMaxY();
			float minY = this.getSlicePlanesY().getMinY();
			float maxX = this.getSlicePlanesY().getMaxX();
			float minX = this.getSlicePlanesY().getMinX();
			MeasureTool.measure(maxX, minX, maxY, minY, g);
		}
		

		
		
		
		if (creossSelectionTempOver != null)
			creossSelectionTempOver.render(g);

		
		
		
		//DEBUG STUFF FOR drawing the collision mesh
		if (SETTINGS.draw_collision_mesh && indexVertexArrays != null) {
			g.pushMatrix();
			float scaleBy = -1 / GLOBAL.jBullet.scale;
			g.translate(-this.centreOfMass.x * scaleBy,
					(-this.centreOfMass.y * scaleBy), -this.centreOfMass.z
							* scaleBy);

			for (int i = 0; i < indexVertexArrays.getNumSubParts(); i++) {

				VertexData vd = indexVertexArrays
						.getLockedReadOnlyVertexIndexBase(i);

				for (int t = 0; t < vd.getIndexCount() / 3; t++) {

					Vector3f[] triangle = new Vector3f[] { new Vector3f(),
							new Vector3f(), new Vector3f() };
					Vector3f scale = new Vector3f(1 / GLOBAL.jBullet.scale, -1
							/ GLOBAL.jBullet.scale, 1 / GLOBAL.jBullet.scale);
					vd.getTriangle(t * 3, scale, triangle);
					g.stroke(255, 0, 0);
					g.beginShape();
					g.vertex(triangle[0].x, triangle[0].y, triangle[0].z);
					g.vertex(triangle[1].x, triangle[1].y, triangle[1].z);
					g.vertex(triangle[2].x, triangle[2].y, triangle[2].z);
					g.endShape();
				}
			}
			g.popMatrix();

		}
		g.popMatrix();

	}

	/*	Pick Buffer Rendering
	 * This renders a buffer that objects can be picked from
	 */
	public void renderPickBuffer(PGraphics pickBuffer) {

		pickBuffer.pushMatrix();
		this.applyTranslationMatrix(pickBuffer);
		this.getSlicePlanesY().renderPickBuffer(pickBuffer);
		this.slicePlanesSlatSlices.renderPickBuffer(pickBuffer);
		pickBuffer.popMatrix();
	}

	void centreFillWindow(PGraphics g, int w, int h, float scaleRes) {

		float border = 40;
		float minX = -1;
		float minY = -1;
		float maxX = -1;
		float maxY = -1;
		float maxZ = -1;
		float minZ = -1;

		float scaleBy = -1 / GLOBAL.jBullet.scale;

		if (indexVertexArrays != null) {

			for (int i = 0; i < indexVertexArrays.getNumSubParts(); i++) {
				VertexData vd = indexVertexArrays
						.getLockedReadOnlyVertexIndexBase(i);

				for (int t = 0; t < vd.getIndexCount() / 3; t++) {

					Vector3f[] triangle = new Vector3f[] { new Vector3f(),
							new Vector3f(), new Vector3f() };
					Vector3f scale = new Vector3f(1 / GLOBAL.jBullet.scale, -1
							/ GLOBAL.jBullet.scale, 1 / GLOBAL.jBullet.scale);
					vd.getTriangle(t * 3, scale, triangle);

					float screenX = g.screenX(triangle[0].x, triangle[0].y,
							triangle[0].z);
					float screenY = g.screenY(triangle[0].x, triangle[0].y,
							triangle[0].z);
					float screenZ = g.screenZ(triangle[0].x, triangle[0].y,
							triangle[0].z);

					if (screenX < minX || minX == -1)
						minX = screenX;

					if (screenX > maxX || maxX == -1)
						maxX = screenX;

					if (screenY < minY || minY == -1)
						minY = screenY;

					if (screenY > maxY || maxY == -1)
						maxY = screenY;

					if (screenZ < minZ || minZ == -1)
						minZ = screenZ;

					if (screenZ > maxZ || maxZ == -1)
						maxZ = screenZ;

					screenX = g.screenX(triangle[1].x, triangle[1].y,
							triangle[1].z);
					screenY = g.screenY(triangle[1].x, triangle[1].y,
							triangle[1].z);

					if (screenX < minX || minX == -1)
						minX = screenX;

					if (screenX > maxX || maxX == -1)
						maxX = screenX;

					if (screenY < minY || minY == -1)
						minY = screenY;

					if (screenY > maxY || maxY == -1)
						maxY = screenY;

					if (screenZ < minZ || minZ == -1)
						minZ = screenZ;

					if (screenZ > maxZ || maxZ == -1)
						maxZ = screenZ;

					screenX = g.screenX(triangle[2].x, triangle[2].y,
							triangle[2].z);
					screenY = g.screenY(triangle[2].x, triangle[2].y,
							triangle[2].z);

					if (screenX < minX || minX == -1)
						minX = screenX;

					if (screenX > maxX || maxX == -1)
						maxX = screenX;

					if (screenY < minY || minY == -1)
						minY = screenY;

					if (screenY > maxY || maxY == -1)
						maxY = screenY;

					if (screenZ < minZ || minZ == -1)
						minZ = screenZ;

					if (screenZ > maxZ || maxZ == -1)
						maxZ = screenZ;

				}
			}

		}

		float scale = 1;

		float modelWidth = maxX - minX;
		float modelHeight = maxY - minY;

		float widthRatio = 1 / ((modelWidth + (0)) / w);
		float heightRatio = 1 / ((modelHeight + (0)) / h);

		if (widthRatio > heightRatio) {
			scale = heightRatio;
		} else {
			scale = widthRatio;
		}
		//scale = 1;

		//scale = 2f;
		scale = scale * 0.8f;
		float cDeltaX = minX - (w / 2) + (modelWidth / 2);
		float cDeltaY = minY - (h / 2) + (modelHeight / 2);
		//LOGGER.info("cDeltaX " + cDeltaX + " cDeltaY " + cDeltaY );
		//LOGGER.info("borderScale" + borderScale);
		float zoom = (float) (GLOBAL.getZOOM() * scale);

		float offsetX = (((cDeltaX) / (zoom * scaleRes)) - (this.centreOfMass.x * scaleBy));
		float offsetY = ((cDeltaY / (zoom * scaleRes)) - (this.centreOfMass.y * scaleBy));
		//g.translate(-((-w / 2) + GLOBAL.CAM_OFFSET_X), -((-h / 2)+ GLOBAL.CAM_OFFSET_Y));

		g.scale(scale);
		//g.translate((-w / 2) + GLOBAL.CAM_OFFSET_X, (-h / 2)
		//		+ GLOBAL.CAM_OFFSET_Y);
		//g.scale(scale);

		//g.translate(-border/(scale), -border/(scale));

		//LOGGER.info("scale "+scale );
		//g.scale(scale);
		//g.translate(-(w/2)/GLOBAL.getZOOM(), 0);
		//LOGGER.info("GLOBAL.rotateModelsX "  + " minX " + minX + "offsetX " + offsetX);

		float offsetRotateX = (this.centreOfMass.x * scaleBy);
		float offsetRotateY = (this.centreOfMass.y * scaleBy);

		//LOGGER.info("offsetRotateX " + offsetRotateX + " offsetRotateY " + offsetRotateY);

		g.translate((offsetRotateX), offsetRotateY);

		g.rotateY(-GLOBAL.rotateModelsY);
		g.rotateX(-GLOBAL.rotateModelsX);

		g.translate(-(offsetRotateX), -offsetRotateY);
		g.translate(-(offsetX), -(offsetY));
		//g.translate(border/(zoom), border/(zoom));
		g.rotateX(GLOBAL.rotateModelsX);
		g.rotateY(GLOBAL.rotateModelsY);
		//g.scale(0.9f);
		//	g.scale(0.5f);

		//g.scale(scale);
		//g.fill(0,255,0);
		//g.ellipse(0,0,1000,1000);
		//g.translate(-(w/2), (h/2));

		//g.scale(1.2f);
		//g.translate((w/2), -(h/2));

	}

	
	
	public PImage renderDiagram(int w, int h, boolean useCurrentView) {
		return renderDiagram(w, h, -1, -1, useCurrentView);

	}

	public PImage renderDiagram(int w, int h, float rotateX, float rotateY,
			boolean useCurrentView) {

		this.updateCollisionShape();

		//Setup the chair for outline diagram rendering
		this.slicePlanesSlatSlices.buildOutline(false, false);
		this.slicePlanesY.buildOutline(false, false);

		int P3D = 1;
		int OPENGL = 2;
		int SUNFLOW = 3;

		int renderMode = OPENGL;

		PGraphics diagramImg = null;
		//P5SunflowAPIAPI sunflow = null;

		float scale = 1f;
		w = (int) (w * scale);
		h = (int) (h * scale);

		float minX = this.getSlicePlanesY().getMinX();
		float minY = this.getSlicePlanesY().getMinY();
		float maxX = this.getSlicePlanesY().getMaxX();
		float maxY = this.getSlicePlanesY().getMaxY();

		float width = Math.abs(maxX - minX);
		float height = Math.abs(maxY - minY);

		float widthDelta = w / width;
		float heightDelta = h / height;

		float delta = Math.min(widthDelta, heightDelta);

		//glewIsSupported("GL_EXT_framebuffer_multisample");
		/*
		
		GL gl = ((PGraphicsOpenGL) GLOBAL.applet.g).gl;
		GLState state = new GLState(gl);
		*/
		//boolean useVBO = true;//state.vbosAvailable;

		if (renderMode == P3D)
			diagramImg = GLOBAL.applet.createGraphics(w, h, GLOBAL.applet.P3D);

		if (renderMode == OPENGL)
			diagramImg = GLOBAL.applet.createGraphics(w, h, GLOBAL.applet.OPENGL);


		/*
		if (renderMode == SUNFLOW) {
			diagramImg = GLOBAL.applet.createGraphics(w, h,
					"sunflowapiapi.P5SunflowAPIAPI");
			sunflow = (P5SunflowAPIAPI) diagramImg;
			// set shader
			//sunflow.setDiffuseShader();
		}
*/
		diagramImg.beginDraw();
		//diagramImg.background(255,255,255,1);
		diagramImg.ortho(-(w / 2), (w / 2), -(h / 2), (h / 2), -1000, 10000);
		//diagramImg.hint(PApplet.DISABLE_STROKE_PERSPECTIVE);

		diagramImg.pushMatrix();
		diagramImg.smooth(8);

		if (useCurrentView) {

			diagramImg.translate(w / 2, h / 2, 0);

			if (rotateX == -1 && rotateY == -1) {
				diagramImg.rotateX(GLOBAL.rotateModelsX);
				diagramImg.rotateY(GLOBAL.rotateModelsY);
			} else {
				diagramImg.rotateX(rotateX);
				diagramImg.rotateY(rotateY);
			}
			diagramImg.scale((float) GLOBAL.getZOOM());
			diagramImg.scale(scale);

			//we scaled up so now we need to scale the window width move
			diagramImg.translate((-(w / scale) / 2) + (float)(GLOBAL.CAM_OFFSET_X),
					(-(h / scale) / 2) + (float)(GLOBAL.CAM_OFFSET_Y));

			//this.applyTranslationMatrix(diagramImg);

		} else {
			diagramImg.translate(w / 2, h / 2, 0);

			if (rotateX == -1 && rotateY == -1) {
				diagramImg.rotateX(GLOBAL.rotateModelsX);
				diagramImg.rotateY(GLOBAL.rotateModelsY);
			} else {
				diagramImg.rotateX(rotateX);
				diagramImg.rotateY(rotateY);
			}

			diagramImg.scale((float) GLOBAL.getZOOM());
			diagramImg.scale(scale);

			//this.applyTranslationMatrix(diagramImg);

			this.centreFillWindow(diagramImg, w, h, scale);

		}
		//g.translate(-minX , -minY);
		//g.translate(-minX , -minY);
		///g.translate(((width/2)) , ((height/2)) );
		//g.rotateX(45);
		//g.translate(-((width/2)) , -((height/2)) );
		//g.translate(x/delta,y/delta);
		//RENDER_3D_PREVIW

		// if(GLOBAL.jBullet.physics_on)
		this.slicePlanesY.setRenderMode(Sketch.RENDER_3D_DIAGRAM);
		this.slicePlanesY.render(diagramImg);
		this.slicePlanesSlatSlices.setRenderMode(Sketch.RENDER_3D_DIAGRAM);
		this.slicePlanesSlatSlices.render(diagramImg);

		//this.slicePlanesX.render(g);

		diagramImg.popMatrix();
		diagramImg.endDraw();

		/*
		if (renderMode == SUNFLOW) {
			sunflow.setPathTracingGIEngine(8);
			sunflow.render();
			return sunflow;
		}
		*/

		/*
		if (renderMode == OPENGL) {
			GLTexture tex = ((GLGraphicsOffScreen) diagramImg).getTexture();
			tex.updateTexture();
			tex.updatePixels();
			//diagramImg.delete();
			//((GLGraphicsOffScreen)diagramImg)
			GLState.deleteAllGLResources();
			return (PImage) tex;
		}
*/
		
		if (renderMode == P3D)
			return diagramImg;

		return diagramImg;

	}

	
	
	/* 3D Preview View ( top right corner)
	 *
	 */
	public float render3Dpreview(PGraphics g, float x, float y, float w, float h) {

		g.smooth(8);

		this.slicePlanesY.setRenderMode(Sketch.RENDER_3D_PREVIW);
		this.slicePlanesSlatSlices.setRenderMode(Sketch.RENDER_3D_PREVIW);

		float minX = this.getSlicePlanesY().getMinX();
		float minY = this.getSlicePlanesY().getMinY();
		float maxX = this.getSlicePlanesY().getMaxX();
		float maxY = this.getSlicePlanesY().getMaxY();

		float width = Math.abs(maxX - minX);
		float height = Math.abs(maxY - minY);

		
		
		float widthDelta = w / width;
		float heightDelta = h / height;
		float delta = Math.min(widthDelta, heightDelta);

		float goundOffset = (h-(height*delta))/delta;
		g.pushMatrix();
		g.scale(delta);
		g.translate(x / delta, y / delta);
		g.translate(0, +(height / 2)+goundOffset);

		g.rotateY((float) (-Math.PI / 4));
		//g.rotateY(rotateRend);
		g.translate(-minX - (width / 2), -minY - (height / 2));
		//g.translate(-minX , -minY);
		//g.translate(-minX , -minY);
		///g.translate(((width/2)) , ((height/2)) );
		//g.rotateX(45);
		//g.translate(-((width/2)) , -((height/2)) );
		//g.translate(x/delta,y/delta);

		//this.slicePlanesY.setRenderMode(Sketch.RENDER_3D_PREVIW);
		this.slicePlanesY.render(g);
		//this.slicePlanesSlatSlices.render(g);
		g.popMatrix();
		
		return delta;

	}

	public void render3DPickPreview(PGraphics g, float x, float y, float w,
			float h) {

		float minX = this.getSlicePlanesY().getMinX();
		float minY = this.getSlicePlanesY().getMinY();
		float maxX = this.getSlicePlanesY().getMaxX();
		float maxY = this.getSlicePlanesY().getMaxY();

		float width = Math.abs(maxX - minX);
		float height = Math.abs(maxY - minY);

		float widthDelta = w / width;
		float heightDelta = h / height;
		float delta = Math.min(widthDelta, heightDelta);
		float goundOffset = (h-(height*delta))/delta;

		g.pushMatrix();
		g.scale(delta);
		g.translate(x / delta, y / delta);
		g.translate(0, +(height / 2));
		g.rotateY((float) (-Math.PI / 4));
		g.translate(-minX - (width / 2), -minY - (height / 2)+goundOffset);

		this.slicePlanesY.renderPickBuffer(g);
		g.popMatrix();

	}

	public void renderSilhouette(int thumbnailWidth, int thumbnailHeight,
			PGraphics g) {
		float minX = this.getSlicePlanesY().getMinX();
		float minY = this.getSlicePlanesY().getMinY();
		float maxX = this.getSlicePlanesY().getMaxX();
		float maxY = this.getSlicePlanesY().getMaxY();

		float width = Math.abs(maxX - minX);
		float height = Math.abs(maxY - minY);

		float widthDelta = thumbnailWidth / width;
		float heightDelta = thumbnailHeight / height;

		float delta = Math.min(widthDelta, heightDelta);
		g.pushMatrix();
		this.applyTranslationMatrix(g);
		g.scale(delta * .9f);
		g.translate(-minX * .95f, -minY * .95f);

		// this.applyTranslationMatrix(g);
		// this.selectedPlanes.renderSilhouette(g);
		// this.slicePlanesX.renderSilhouette(g);
		this.getSlicePlanesY().renderSilhouette(g);
		// g.ellipse(this.centreOfMass.x/GLOBAL.jBullet.scale,this.centreOfMass.y/GLOBAL.jBullet.scale,25,25);
		g.popMatrix();
	}

	public void renderSilhouette(PGraphics g) {
		g.pushMatrix();
		g.smooth(8);
		this.getSlicePlanesY().renderSilhouette(g);
		g.noSmooth();
		g.popMatrix();

	}

	public PGraphics renderToFile() {
		float border = 25;

		float minX = this.getSlicePlanesY().getMinXWorldSpace(
				this.currentWorldTransform);
		float minY = this.getSlicePlanesY().getMinYWorldSpace(
				this.currentWorldTransform);
		float maxX = this.getSlicePlanesY().getMaxXWorldSpace(
				this.currentWorldTransform);
		float maxY = this.getSlicePlanesY().getMaxYWorldSpace(
				this.currentWorldTransform);

		float minX2 = this.getSlicePlanesY().getMinX();
		float minY2 = this.getSlicePlanesY().getMinY();
		float maxX2 = this.getSlicePlanesY().getMaxX();
		float maxY2 = this.getSlicePlanesY().getMaxY();

		float width = Math.abs(maxX - minX) + (imgBorder * 2);
		float height = Math.abs(maxY - minY) + +(imgBorder * 2);

		float width2 = Math.abs(maxX2 - minX2) + (imgBorder * 2);
		float height2 = Math.abs(maxY2 - minY2) + (imgBorder * 2);

		PGraphics saveImg = GLOBAL.applet.createGraphics((int) width,
				(int) height, PConstants.P3D);
		saveImg.beginDraw();
		saveImg.ortho(-(width / 2), (width / 2), -(height / 2), (height / 2),
				-1000, 10000);
		//saveImg.hint(PApplet.DISABLE_STROKE_PERSPECTIVE);

		saveImg.smooth(8);
		saveImg.pushMatrix();

		saveImg.translate(-minX + border, -minY + border);
		this.renderSilhouette(saveImg);

		saveImg.popMatrix();
		saveImg.endDraw();
		return saveImg;

	}

	public void resetPhysics() {
		this.personSeated = false;
		rigidBody.setMotionState(new DefaultMotionState(
				this.startWorldTransform));
		GLOBAL.jBullet.update();

	}

	/*
	 * 
	 */
	public void saveToPDF(PGraphics g) {
		this.getSlicePlanesY().saveToPDF(g);
		this.slicePlanesSlatSlices.saveToPDF(g, true);
	}

	/**
	 * Scale the current design.
	 * @param scale
	 */
	private void scale(float scale) {
		Vec3D centre = this.getCentreOfMass();
		centre.scaleSelf(1 / GLOBAL.jBullet.scale);
		this.getSlicePlanesY().scale(scale, centre);
		this.scale += scale;
	}

	/*
	 * Seat the figure on the design.
	 */
	public void seatPerson() {
		if (SETTINGS.auto_seat && !GLOBAL.person.dragged) {
			GLOBAL.person.seat(
					this,
					this.getSlicePlanesY().getMaxXWorldSpace(
							this.currentWorldTransform), this.getSlicePlanesY()
							.getMaxYWorldSpace(this.currentWorldTransform), 0);
			this.personSeated = true;
		}
	}

	public void selectNodes(int mouseX, int mouseY) {
		this.selectedPlanes.selectNodes(mouseX, mouseY);
	}

	public void setBrushCap(int cap) {
		this.selectedPlanes.setBrushCap(cap);

	}

	public void setBrushDia(float val) {
		this.selectedPlanes.setBrushDia(val);
	}

	public void setHeight(float height) {

	}

	public void setPlaneWidth(float planeThicknes_mm) {
		this.materialWidth = planeThicknes_mm;
		this.selectedPlanes.setPlaneWidth(planeThicknes_mm);
		this.slicePlanesY.setPlaneWidth(planeThicknes_mm);
		this.slicePlanesSlatSlices.setPlaneWidth(planeThicknes_mm);
	}

	public void setPosTopCorner(int minX, int maxY) {

	}

	/**
	 * @param reScaling the reScaling to set
	 */
	public void setReScaling(boolean reScaling) {
		this.reScaling = reScaling;
	}

	/**
	 * @param slicePlanesY the slicePlanesY to set
	 */
	public void setSlicePlanesY(SlicePlanes slicePlanesY) {
		this.slicePlanesY = slicePlanesY;
	}

	/**
	 * @param updateCollisionMesh the updateCollisionMesh to set
	 */
	public void setUpdateCollisionMesh(boolean updateCollisionMesh) {
		this.updateCollisionMesh = updateCollisionMesh;
	}

	public Vec2D setVec2DpickBuffer(int col, SketchPoint selectedVec,
			SketchShape selectedShape, SlicePlane selectedVecPlane,
			boolean isSelectedVecOnOutline) {

		Vec2D vec = this.selectedPlanes.setVec2DpickBuffer(col, selectedVec,
				selectedShape, selectedVecPlane, isSelectedVecOnOutline);
		if (vec != null)
			return vec;

		vec = this.slicePlanesSlatSlices.setVec2DpickBuffer(col, selectedVec,
				selectedShape, selectedVecPlane, isSelectedVecOnOutline);

		if (vec != null)
			return vec;

		return this.getSlicePlanesY().setVec2DpickBuffer(col, selectedVec,
				selectedShape, selectedVecPlane, isSelectedVecOnOutline);
	}

	
	
	/*
	 * Set the current width and rebuilt layers and slices to match.
	 */
	public void setWidth(float w) {
		// System.out.println(deltaMouseY);
		this.width = w;
		if (this.getSlicePlanesY().size() > 1) {
			this.rebuildWidth();
		}
	}
	
	

	//Set the chair in the center of the playfiled at ground height, this can only currently be done before the chair is moved
	void setAtGroundHeightCentred() {
		//float minY = this.getSlicePlanesY().getMinY();
		float maxY = this.getSlicePlanesY().getMaxYWorldSpace(
				this.currentWorldTransform);

		float minX = this.getSlicePlanesY().getMinXWorldSpace(
				currentWorldTransform);
		float maxX = this.getSlicePlanesY().getMaxXWorldSpace(
				currentWorldTransform);
		//float height = (maxY-minY);
		float cWidth = maxX - minX;

		float offsetX = minX - (cWidth / 2);
		//LOGGER.info("minX " + minX);
		float offsetY = offsetY = maxY - 1570; // what is this weird hardcoded number?
		//debug info useful for working out the translations between different cord sys
		/*
				LOGGER.info("getMeshMinY()) " + (getMeshMinY()));
				LOGGER.info("maxY " + maxY);
				LOGGER.info("height " + height);
				LOGGER.info("getPhysicsOrigin().y" + (getPhysicsOrigin().y*(1 / GLOBAL.jBullet.scale)));
				LOGGER.info("getPhysicsOrigin().x" + (getPhysicsOrigin().x*(1 / GLOBAL.jBullet.scale)));
		*/

		this.translate(-offsetX, (-offsetY), 0);

	}

	void toggleCentreConstraint() {
		if (this.chairCentreConstraint != null) {
			GLOBAL.jBullet.myWorld.removeConstraint(this.chairCentreConstraint);
			this.chairCentreConstraint = null;
		} else {
			this.addCentrePlaneConstraint();
		}

	}

	public void toggleUnion() {
		this.selectedPlanes.toggleUnion();
	}

	public Element toXML() {
		Element element = new Element("SketchChair");

		//cloudID 
		// set slat spacing
		element.addAttribute(new Attribute("cloudID", String.valueOf(cloudID)));

		// set slat spacing
		element.addAttribute(new Attribute("slatSpacingX", String
				.valueOf(slatSpacingX)));

		element.addAttribute(new Attribute("materialWidth", String
				.valueOf(SETTINGS.materialThickness)));

		element.addAttribute(new Attribute("scale", String
				.valueOf(SETTINGS.scale)));

		element.appendChild(this.getSlicePlanesY().toXML());
		element.appendChild(crossSliceSelections.toXML());

		return element;

	}

	void translate(float x, float y, float z) {
		Vector3f offset3f = new Vector3f(x, y, z);
		offset3f.scale(GLOBAL.jBullet.scale);
		this.rigidBody.translate(offset3f);
		GLOBAL.jBullet.update();
		this.startWorldTransform.origin.set(offset3f);
	}

	public void update() {

		//this.setAtGroundHeight();

		if (this.rigidBody != null) {
			Transform myTransform = new Transform();
			myTransform = rigidBody.getMotionState().getWorldTransform(
					myTransform);
			this.currentWorldTransform = myTransform;
		}

		if (this.built && this.getSlicePlanesY().countNumberOfShapes() <= 0)
			this.destroy();

		if (trimesh != null)
			trimesh.updateBound();

		//The physics is running and we need to update our mesh so go and do it now !
		if (GLOBAL.jBullet.physics_on && this.isUpdateCollisionMesh()) {
			this.updateCollisionShape();
			this.setUpdateCollisionMesh(false);
		}

		if (GLOBAL.jBullet.physics_on && !this.personSeated
				&& SETTINGS.auto_seat) {
			this.seatPerson();

		}

		boolean wasNotNull = false;
		if (creossSelectionTempOver != null)
			wasNotNull = true;

		boolean updateLen = false;

		if (creossSelectionTempOver != null && !creossSelectionTempOver.editing)
			creossSelectionTempOver = null;

		if (GLOBAL.uiTools.getCurrentTool() == UITools.SLICES_SINGLE_SLICE) {
			SketchShapes overShapes = getOverShapes(GLOBAL.uiTools.mouseX,
					GLOBAL.uiTools.mouseY);

			if (overShapes.size() > 0) {
				SketchShape overShape = overShapes.getClosest(
						GLOBAL.uiTools.mouseX, GLOBAL.uiTools.mouseY);
				creossSelectionTempOver = new CrossSliceSelection(overShape,
						overShape.getParentSketch().getOnSketchPlane(),
						overShape.lastMouseOverPercent,
						overShape.lastMouseOverPercent, 1, this);
				creossSelectionTempOver.type = CrossSliceSelection.SINGLE_SLICE;
				creossSelectionTempOver.select();
				creossSelectionTempOver.tempSlice = true;
				updateLen = true;

			}

		}

		if (GLOBAL.uiTools.getCurrentTool() == UITools.SLICES_GROUP_SLICES
				&& ((creossSelectionTempOver == null || !creossSelectionTempOver.editing))) {

			SketchShapes overShapes = getOverShapes(GLOBAL.uiTools.mouseX,
					GLOBAL.uiTools.mouseY);
			if (overShapes.size() > 0) {
				SketchShape overShape = overShapes.getClosest(
						GLOBAL.uiTools.mouseX, GLOBAL.uiTools.mouseY);
				creossSelectionTempOver = new CrossSliceSelection(overShape,
						overShape.getParentSketch().getOnSketchPlane(),
						overShape.lastMouseOverPercent,
						overShape.lastMouseOverPercent, 10f, this);
				creossSelectionTempOver.type = CrossSliceSelection.SLICES;
				creossSelectionTempOver.mousePercent = overShape.lastMouseOverPercent;
				creossSelectionTempOver.select();
				creossSelectionTempOver.tempSlice = true;

				updateLen = true;
			}

		}

		if (GLOBAL.uiTools.getCurrentTool() == UITools.SLICES_SLATSLICE_GROUP
				&& ((creossSelectionTempOver == null || !creossSelectionTempOver.editing))) {

			SketchShapes overShapes = getOverShapes(GLOBAL.uiTools.mouseX,
					GLOBAL.uiTools.mouseY);

			if (overShapes.size() > 0) {
				SketchShape overShape = overShapes.getClosest(
						GLOBAL.uiTools.mouseX, GLOBAL.uiTools.mouseY);
				creossSelectionTempOver = new CrossSliceSelection(overShape,
						overShape.getParentSketch().getOnSketchPlane(),
						overShape.lastMouseOverPercent,
						overShape.lastMouseOverPercent, 50, this);
				creossSelectionTempOver.type = CrossSliceSelection.SLATSLICES;
				creossSelectionTempOver
						.setSlatHeight(SETTINGS.DEFAULT_SLATSLICE_HEIGHT);
				creossSelectionTempOver.mousePercent = overShape.lastMouseOverPercent;
				creossSelectionTempOver.select();
				creossSelectionTempOver.tempSlice = true;

				updateLen = true;
			}

		}

		if (GLOBAL.uiTools.getCurrentTool() == UITools.SLICES_SINGLE_SLAT
				&& ((creossSelectionTempOver == null || !creossSelectionTempOver.editing))) {

			SketchShapes overShapes = getOverShapes(GLOBAL.uiTools.mouseX,
					GLOBAL.uiTools.mouseY);

			if (overShapes.size() > 0) {
				SketchShape overShape = overShapes.getClosest(
						GLOBAL.uiTools.mouseX, GLOBAL.uiTools.mouseY);
				creossSelectionTempOver = new CrossSliceSelection(overShape,
						overShape.getParentSketch().getOnSketchPlane(),
						overShape.lastMouseOverPercent,
						overShape.lastMouseOverPercent + 0.01f, 1, this);
				creossSelectionTempOver.type = CrossSliceSelection.PLANE_ON_EDGE;
				creossSelectionTempOver.mousePercent = overShape.lastMouseOverPercent;
				creossSelectionTempOver.select();
				creossSelectionTempOver.tempSlice = true;

				updateLen = true;
			} else {
				if (this.selectedPlanes.size() > 0) {
					SlicePlane tempPlane = this.selectedPlanes.get(0);
					;
					Vec2D tempP = new Vec2D(GLOBAL.uiTools.mouseX,
							GLOBAL.uiTools.mouseY);
					SketchPath tempPath = new SketchPath(tempPlane.getSketch());
					tempPath.add(new SketchPoint(GLOBAL.uiTools
							.getPointOnPlane(tempP, tempPlane.getPlane())));
					tempPath.add(new SketchPoint(GLOBAL.uiTools
							.getPointOnPlane(tempP, tempPlane.getPlane()).add(
									100, 0)));
					tempPath.setIsContructionLine(true);
					creossSelectionTempOver = new CrossSliceSelection(tempPath,
							tempPath.getParentSketch().getOnSketchPlane(), 0,
							1, 1, this);
					creossSelectionTempOver.type = CrossSliceSelection.PLANE;
					creossSelectionTempOver.select();
					creossSelectionTempOver.tempSlice = true;

					updateLen = true;
				}
			}
		}

		if (creossSelectionTempOver != null && creossSelectionTempOver.editing) {
			creossSelectionTempOver.update();
			updateLen = true;
		}

		if (GLOBAL.uiTools.getCurrentTool() == UITools.CROSSSLICE_EDIT)
			this.buildLen();

		if (this.rebuildLength || updateLen) {
			this.previewSlices();
			this.rebuildLength = false;
		}

		//this.slicePlanesX.unselectAll();
		//LOGGER.info("UNSELECT " + this.selectedPlanes.size());
		this.getSlicePlanesY().unselectAll();
		this.selectedPlanes.selectAll();
		// this.sketchSpline.update();
		this.slicePlanesSlatSlices.update();
		this.getSlicePlanesY().update();
		this.selectedPlanes.update();
		this.crossSliceSelections.update();

	}

	public void updateCollisionShape() {
		if (this.trimesh == null) {
			addRigidModel();
			return;
		}
		// if (this.rigidBody != null)
		// GLOBAL.jBullet.myWorld.removeRigidBody(this.rigidBody);

		Vec3D centreOfMassOld = this.centreOfMass.copy();
		Vec3D centreOfMassTemp = this.getCentreOfMass();

		LOGGER.debug("current centre" + centreOfMassOld);
		LOGGER.debug("current centre Translated "
				+ getTranslated(centreOfMassOld));

		LOGGER.debug("new centre" + centreOfMassTemp);
		LOGGER.debug("new centre Translated" + getTranslated(centreOfMassTemp));

		this.centreOfMass = centreOfMassTemp;
		Vec3D deltaCentreOfmass = getTranslated(this.centreOfMass).sub(
				getTranslated(centreOfMassOld));

		//	this.centreOfMass = centreOfMassTemp;
		// create trimesh
		indexVertexArrays = this.getVertexArray(centreOfMass.x, centreOfMass.y,
				centreOfMass.z);

		//this.buildCoverMesh(indexVertexArrays, centreOfMass);

		if (indexVertexArrays != null) {
			trimesh = new GImpactMeshShape(indexVertexArrays);
			trimesh.setLocalScaling(new Vector3f(1f, 1f, 1f));
			trimesh.updateBound();
			// rigidBody.setMotionState(myMotionState);
			if (trimesh != null && this.rigidBody != null)
				this.rigidBody.setCollisionShape(trimesh);

			if (this.rigidBody == null || trimesh == null)
				return;

			Transform transform = new Transform();
			MotionState motionstate = rigidBody.getMotionState();
			motionstate.getWorldTransform(transform);
			Matrix4f chairMatrix = new Matrix4f();
			transform.getMatrix(chairMatrix);
			//	LOGGER.debug("current origin"+transform.origin.toString());

			//this is overwritten when settign the chair matrix 
			//transform.origin.set(this.centreOfMass.x, this.centreOfMass.y,
			//		this.centreOfMass.z);

			LOGGER.debug("delta x" + deltaCentreOfmass.x);
			LOGGER.debug("delta y" + deltaCentreOfmass.y);
			LOGGER.debug("delta z" + deltaCentreOfmass.z);

			float newX = ((chairMatrix.m00) * deltaCentreOfmass.x)
					+ ((chairMatrix.m01) * deltaCentreOfmass.y)
					+ ((chairMatrix.m02) * deltaCentreOfmass.z);
			float newY = (((chairMatrix.m10) * deltaCentreOfmass.x)
					+ ((chairMatrix.m11) * deltaCentreOfmass.y) + ((chairMatrix.m12) * deltaCentreOfmass.z));
			float newZ = ((chairMatrix.m20) * deltaCentreOfmass.x)
					+ ((chairMatrix.m21) * deltaCentreOfmass.y)
					+ ((chairMatrix.m22) * deltaCentreOfmass.z);

			//these are the origin
			chairMatrix.m03 -= deltaCentreOfmass.x;//10;//deltaCentreOfmass.x;
			chairMatrix.m13 -= deltaCentreOfmass.y;//deltaCentreOfmass.y;
			chairMatrix.m23 += deltaCentreOfmass.z;//deltaCentreOfmass.z;

			//chairMatrix.m03 = this.centreOfMass.x;
			//chairMatrix.m13 = this.centreOfMass.y;
			//chairMatrix.m23 = this.centreOfMass.z;

			LOGGER.debug("delta x t " + newX);
			LOGGER.debug("delta y t " + newY);
			LOGGER.debug("delta z t " + newZ);

			transform.set(chairMatrix);
			motionstate.setWorldTransform(transform);
			rigidBody.setMotionState(motionstate);

			Vector3f center = new Vector3f();
			rigidBody.getCenterOfMassPosition(center);
			LOGGER.debug("getCenterOfMassPosition set " + center);

			// System.out.println("weight" + this.getArea());
			Vector3f AngularVelocity = new Vector3f(0, 0, 0);
			rigidBody.getAngularVelocity(AngularVelocity);

			Vector3f LinearVelocity = new Vector3f(0, 0, 0);
			rigidBody.getLinearVelocity(LinearVelocity);

			GLOBAL.jBullet.update();

			motionstate.getWorldTransform(transform);
			//	LOGGER.debug("new origin"+transform.origin.toString());

			//this.translate(-newX, newY, newZ);

			// rigidBody.setAngularVelocity(AngularVelocity);
			// rigidBody.setLinearVelocity(LinearVelocity);
			// rigidBody.setMassProps(this.getMass(), new Vector3f(0,1,0));

			rigidBody.setDamping(SETTINGS.chair_damping_linear,
					SETTINGS.chair_damping_ang);
			rigidBody.setDeactivationTime(0.8f);
			rigidBody.setSleepingThresholds(1.6f, 2.5f);
			rigidBody.setFriction(SETTINGS.chair_friction);
			motionstate = rigidBody.getMotionState();
			rigidBody.setCcdSweptSphereRadius(.1f);

		}

		// GLOBAL.person.solveOverlap();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	public void importSVG(String path) {
		
		if(this.selectedPlanes.size() == 0){
			SlicePlane slicePlane = new SlicePlane(new Plane(
					new Vec3D(0, 0, 0), new Vec3D(0, 0, -1)));
			slicePlane.importSVG(path);
			this.getSlicePlanesY().add(slicePlane);
			
		}else{
			this.selectedPlanes.importSVG(path);
		}
			
			
	}

	public void unselectAllPlanes() {
		this.selectedPlanes.unselectAll();
		this.slicePlanesY.unselectAll();
		this.slicePlanesSlatSlices.unselectAll();
		
		this.selectedPlanes.unselectShapesAll();
		this.slicePlanesY.unselectShapesAll();
		this.slicePlanesSlatSlices.unselectShapesAll();
		
		this.selectedPlanes.empty();
	}

	

}
