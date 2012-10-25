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
package cc.sketchchair.ragdoll;

/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Ragdoll Demo
 * Copyright (c) 2007 Starbreeze Studios
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose, 
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 * 
 * Written by: Marten Svanfeldt
 */

import java.util.HashMap;
import java.util.Map;

import cc.sketchchair.core.GLOBAL;
import cc.sketchchair.core.LOGGER;
import cc.sketchchair.core.SETTINGS;
import cc.sketchchair.core.SketchChair;
import cc.sketchchair.core.UITools;
import cc.sketchchair.functions.functions;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.collision.broadphase.CollisionAlgorithm;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.Generic6DofConstraint;
import com.bulletphysics.dynamics.constraintsolver.HingeConstraint;
import com.bulletphysics.dynamics.constraintsolver.TranslationalLimitMotor;
import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MatrixUtil;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * The ergonomic figure representing the user. 
 * This is based on code from the jBullet examples.
 * This needs to be improved. 
 * Arms removed as they were gettign in the way. 
 * @author gregsaul
 *
 */
public class ergoDoll {

	//protected final BulletStack stack = BulletStack.get();

	public enum BodyPart {
		BODYPART_PELVIS, BODYPART_SPINE, BODYPART_HEAD, BODYPART_NECK, BODYPART_LEFT_UPPER_LEG, BODYPART_LEFT_LOWER_LEG,

		BODYPART_RIGHT_UPPER_LEG, BODYPART_RIGHT_LOWER_LEG,

		BODYPART_RIGHT_FOOT, BODYPART_LEFT_FOOT,

		BODYPART_LEFT_UPPER_ARM, BODYPART_LEFT_LOWER_ARM,

		BODYPART_RIGHT_UPPER_ARM, BODYPART_RIGHT_LOWER_ARM,

		BODYPART_RIGHT_HAND, BODYPART_LEFT_HAND,

		BODYPART_COUNT;
	}

	// Expressions
	public enum faceExpressions {
		HAPPY, SAD, SCARED, EXPRESSION_COUNT
	}

	public enum JointType {
		JOINT_PLANE, JOINT_PELVIS_SPINE, JOINT_NECK_HEAD, JOINT_LEFT_HIP, JOINT_LEFT_KNEE,

		JOINT_RIGHT_HIP, JOINT_RIGHT_KNEE, JOINT_LEFT_SHOULDER, JOINT_LEFT_ELBOW,

		JOINT_RIGHT_SHOULDER, JOINT_RIGHT_ELBOW,

		JOINT_RIGHT_WRIST, JOINT_LEFT_WRIST,

		JOINT_RIGHT_ANKLE, JOINT_LEFT_ANKLE,

		JOINT_COUNT
	}

	boolean hasArms = false;

	//private PImage FaceImages[] = new PImage[faceExpressions.EXPRESSION_COUNT.ordinal()];

	private CollisionShape[] shapes = new CollisionShape[BodyPart.BODYPART_COUNT
			.ordinal()];
	private RigidBody[] bodies = new RigidBody[BodyPart.BODYPART_COUNT
			.ordinal()];
	private MotionState[] motionState = new MotionState[BodyPart.BODYPART_COUNT
			.ordinal()];
	private TypedConstraint[] joints = new TypedConstraint[JointType.JOINT_COUNT
			.ordinal()];
	private float[] proportions = new float[BodyPart.BODYPART_COUNT.ordinal()];
	private float[] proportionsWidth = new float[BodyPart.BODYPART_COUNT
			.ordinal()];

	private float[] limbLengths = new float[BodyPart.BODYPART_COUNT.ordinal()];
	private float[] limbWidths = new float[BodyPart.BODYPART_COUNT.ordinal()];

	private boolean scaling = false;
	float scale = 1f;
	float destScale = 1f;
	float maxScale = 1.5f;
	float minScale = 0.15f;
	
	private DynamicsWorld ownerWorld;
	private Vector3f startPos;
	float height = 23.5f;//mm 
	private float mass = 30;

	Map proportionsGrowthMap = new HashMap();
	private Map proportionsGrowthLenMap = new HashMap();
	private Map proportionsGrowthWidMap = new HashMap();

	private float buildScale;

	public boolean clickedOnPerson;
	public boolean dragged;
	private boolean on = true;

	public ergoDoll(DynamicsWorld ownerWorld, Vector3f positionOffset) {
		this(ownerWorld, positionOffset, 1.0f);
	}

	public ergoDoll(DynamicsWorld ownerWorld, Vector3f positionOffset,
			float scale_ragdoll) {
		this.ownerWorld = ownerWorld;
		this.startPos = positionOffset;

		this.scale = scale_ragdoll;

		makeBody(this.ownerWorld, positionOffset, this.scale);
		this.translate(this.startPos.x, this.startPos.y, this.startPos.z);
		//FaceImages[faceExpressions.HAPPY.ordinal()] = GLOBAL.applet.loadImage("faceExpressionsHappy.png");

	}

	void applyMatrix(Transform myTransform, PGraphics g) {

		g.translate(myTransform.origin.x, myTransform.origin.y,
				myTransform.origin.z);

		g.applyMatrix(myTransform.basis.m00, myTransform.basis.m01,
				myTransform.basis.m02, 0, myTransform.basis.m10,
				myTransform.basis.m11, myTransform.basis.m12, 0,
				myTransform.basis.m20, myTransform.basis.m21,
				myTransform.basis.m22, 0, 0, 0, 0, 1);

	}

	private void buildLimbConstraints() {

		Vector3f tmp = new Vector3f();

		for (int i = 0; i < JointType.JOINT_COUNT.ordinal(); ++i) {
			if (joints[i] != null) {
				ownerWorld.removeConstraint(joints[i]);
			}
		}

		///////////////////////////// SETTING THE CONSTRAINTS /////////////////////////////////////////////7777
		// Now setup the constraints
		Generic6DofConstraint joint6DOF;
		Transform localA = new Transform(), localB = new Transform();
		boolean useLinearReferenceFrameA = true;
		/// ******* SPINE HEAD ******** ///
		{
			localA.setIdentity();
			localB.setIdentity();

			localA.origin
					.set(0f,
							-(limbLengths[BodyPart.BODYPART_SPINE.ordinal()] + limbLengths[BodyPart.BODYPART_NECK
									.ordinal()]), 0f);

			localB.origin.set(0f,
					limbLengths[BodyPart.BODYPART_HEAD.ordinal()], 0f);

			joint6DOF = new Generic6DofConstraint(
					bodies[BodyPart.BODYPART_SPINE.ordinal()],
					bodies[BodyPart.BODYPART_HEAD.ordinal()], localA, localB,
					useLinearReferenceFrameA);

			//#ifdef RIGID
			//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
			//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
			//#else

			tmp.set(-BulletGlobals.SIMD_PI * 0.2f, -BulletGlobals.FLT_EPSILON,
					-BulletGlobals.SIMD_PI * 0.1f);
			joint6DOF.setAngularLowerLimit(tmp);
			tmp.set(BulletGlobals.SIMD_PI * 0.1f, BulletGlobals.FLT_EPSILON,
					BulletGlobals.SIMD_PI * 0.10f);
			joint6DOF.setAngularUpperLimit(tmp);
			//#endif
			joints[JointType.JOINT_NECK_HEAD.ordinal()] = joint6DOF;
			ownerWorld.addConstraint(
					joints[JointType.JOINT_NECK_HEAD.ordinal()], true);

		}
		/// *************************** ///

		/// ******* PELVIS ******** ///
		{
			localA.setIdentity();
			localB.setIdentity();

			MatrixUtil.setEulerZYX(localA.basis, 0, BulletGlobals.SIMD_HALF_PI,
					0);
			localA.origin.set(0f,
					-limbLengths[BodyPart.BODYPART_PELVIS.ordinal()], 0f);
			MatrixUtil.setEulerZYX(localB.basis, 0, BulletGlobals.SIMD_HALF_PI,
					0);
			localB.origin.set(0f,
					limbLengths[BodyPart.BODYPART_SPINE.ordinal()], 0f);
			joint6DOF = new Generic6DofConstraint(
					bodies[BodyPart.BODYPART_PELVIS.ordinal()],
					bodies[BodyPart.BODYPART_SPINE.ordinal()], localA, localB,
					useLinearReferenceFrameA);

			//#ifdef RIGID
			//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
			//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
			//#else
			tmp.set(-BulletGlobals.SIMD_PI * 0.1f, -BulletGlobals.SIMD_EPSILON,
					-BulletGlobals.SIMD_PI * 0.1f);
			joint6DOF.setAngularLowerLimit(tmp);
			tmp.set(BulletGlobals.SIMD_PI * 0.2f, BulletGlobals.SIMD_EPSILON,
					BulletGlobals.SIMD_PI * 0.15f);
			joint6DOF.setAngularUpperLimit(tmp);
			//#endif
			joints[JointType.JOINT_PELVIS_SPINE.ordinal()] = joint6DOF;
			ownerWorld.addConstraint(
					joints[JointType.JOINT_PELVIS_SPINE.ordinal()], true);
		}
		/// *************************** ///

		/// ******* SPINE 2D ******** ///
		{
			localA.setIdentity();
			localB.setIdentity();

			localA.origin.set(0f, 0f, 0f);

			localB.origin.set(0f, 0f, 0f);

			CollisionShape ZeroShape = null;
			GLOBAL.jBullet.ZeroBody = new RigidBody(0.0f, null, ZeroShape);

			Transform Rotation = new Transform();
			Rotation.setIdentity();
			GLOBAL.jBullet.ZeroBody.setWorldTransform(Rotation);

			joint6DOF = new Generic6DofConstraint(
					bodies[BodyPart.BODYPART_SPINE.ordinal()],
					GLOBAL.jBullet.ZeroBody, localA, localB, false);

			joint6DOF.setLimit(0, 1, 0); // Disable X axis limits
			joint6DOF.setLimit(1, 1, 0); // Disable Y axis limits
			joint6DOF.setLimit(2, 0, 0); // Set the Z axis to always be equal to zero
			joint6DOF.setLimit(3, 0, 0); // Disable X rotational axes
			joint6DOF.setLimit(4, 0, 0); // Disable Y rotational axes
			joint6DOF.setLimit(5, 1, 0); // Uncap the rotational axes
			//#endif
			joints[JointType.JOINT_PLANE.ordinal()] = joint6DOF;
			ownerWorld.addConstraint(joints[JointType.JOINT_PLANE.ordinal()],
					true);

			bodies[BodyPart.BODYPART_SPINE.ordinal()]
					.setAngularFactor(new Vector3f(0, 0, 1));

		}
		/// *************************** ///

		if (hasArms) {

			/// ******* LEFT SHOULDER ******** ///
			{
				
				localA.setIdentity();
				localB.setIdentity();

				
				
				localA.origin.set(0f,
						-limbLengths[BodyPart.BODYPART_SPINE.ordinal()],
						limbWidths[BodyPart.BODYPART_SPINE.ordinal()] * 1.2f); //joint on spine
				MatrixUtil.setEulerZYX(localB.basis, BulletGlobals.SIMD_PI, BulletGlobals.SIMD_PI, BulletGlobals.SIMD_PI);
				localB.origin
						.set(0f, limbLengths[BodyPart.BODYPART_LEFT_UPPER_ARM
								.ordinal()], 0f); // upper arm

				joint6DOF = new Generic6DofConstraint(
						bodies[BodyPart.BODYPART_SPINE.ordinal()],
						bodies[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()],
						localA, localB, useLinearReferenceFrameA);

						
				tmp.set(-BulletGlobals.SIMD_EPSILON, 
						-BulletGlobals.SIMD_EPSILON,
						BulletGlobals.SIMD_HALF_PI * 0.5f);
				joint6DOF.setAngularLowerLimit(tmp);
				tmp.set(BulletGlobals.SIMD_HALF_PI * 0.6f,
						BulletGlobals.SIMD_EPSILON,
						-BulletGlobals.SIMD_HALF_PI * 1.3f); //bend foward
				joint6DOF.setAngularUpperLimit(tmp);

				
				joints[JointType.JOINT_LEFT_SHOULDER.ordinal()] = joint6DOF;
				ownerWorld.addConstraint(
						joints[JointType.JOINT_LEFT_SHOULDER.ordinal()], true);
						
			}
			/// *************************** ///

			/// ******* RIGHT SHOULDER ******** ///
			{
				localA.setIdentity();
				localB.setIdentity();

				
				localA.origin.set(0f,
						-limbLengths[BodyPart.BODYPART_SPINE.ordinal()],
						-limbWidths[BodyPart.BODYPART_SPINE.ordinal()] * 1.2f); //joint on spine
				//MatrixUtil.setEulerZYX(localB.basis,BulletGlobals.SIMD_PI, BulletGlobals.SIMD_PI, BulletGlobals.SIMD_PI);
				localB.origin
						.set(0f, limbLengths[BodyPart.BODYPART_RIGHT_UPPER_ARM
								.ordinal()], 0f); // upper arm
				joint6DOF = new Generic6DofConstraint(
						bodies[BodyPart.BODYPART_SPINE.ordinal()],
						bodies[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()],
						localA, localB, useLinearReferenceFrameA);

				//#ifdef RIGID
				//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
				//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
				//#else

				tmp.set(-BulletGlobals.SIMD_EPSILON, 
						-BulletGlobals.SIMD_EPSILON,
						BulletGlobals.SIMD_HALF_PI * 0.5f);
				joint6DOF.setAngularLowerLimit(tmp);
				tmp.set(BulletGlobals.SIMD_HALF_PI * 0.6f,
						BulletGlobals.SIMD_EPSILON,
						-BulletGlobals.SIMD_HALF_PI * 1.3f); //bend foward
				joint6DOF.setAngularUpperLimit(tmp);

				//#endif
				joints[JointType.JOINT_RIGHT_SHOULDER.ordinal()] = joint6DOF;
				ownerWorld.addConstraint(
						joints[JointType.JOINT_RIGHT_SHOULDER.ordinal()], true);
			}
			/// *************************** ///

			
			/// ******* LEFT ELBOW ******** ///
			{
				localA.setIdentity();
				localB.setIdentity();

				localA.origin
						.set(0f, -limbLengths[BodyPart.BODYPART_LEFT_UPPER_ARM
								.ordinal()], 0f); // upper arm
				localB.origin
						.set(0f, -limbLengths[BodyPart.BODYPART_LEFT_LOWER_ARM
								.ordinal()], 0f); // lower arm
				joint6DOF = new Generic6DofConstraint(
						bodies[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()],
						bodies[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()],
						localA, localB, useLinearReferenceFrameA);

				//#ifdef RIGID
				//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
				//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
				//#else
				tmp.set(-BulletGlobals.SIMD_EPSILON, 
						-BulletGlobals.SIMD_EPSILON,
						-BulletGlobals.SIMD_HALF_PI * 0.5f);
				joint6DOF.setAngularLowerLimit(tmp);
				tmp.set(BulletGlobals.SIMD_HALF_PI * 0.6f,
						BulletGlobals.SIMD_EPSILON,
						BulletGlobals.SIMD_HALF_PI * 1.3f); //bend foward
				joint6DOF.setAngularUpperLimit(tmp);
				//#endif
				joints[JointType.JOINT_LEFT_ELBOW.ordinal()] = joint6DOF;
				ownerWorld.addConstraint(
						joints[JointType.JOINT_LEFT_ELBOW.ordinal()], true);
			}
			/// *************************** ///

			/// ******* RIGHT ELBOW ******** ///
			{
				localA.setIdentity();
				localB.setIdentity();

				localA.origin
						.set(0f, -limbLengths[BodyPart.BODYPART_LEFT_UPPER_ARM
								.ordinal()], 0f); // upper arm
				localB.origin
						.set(0f, -limbLengths[BodyPart.BODYPART_LEFT_LOWER_ARM
								.ordinal()], 0f); // lower arm
				joint6DOF = new Generic6DofConstraint(
						bodies[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()],
						bodies[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()],
						localA, localB, useLinearReferenceFrameA);

				//#ifdef RIGID
				//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
				//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
				//#else
				tmp.set(-BulletGlobals.SIMD_EPSILON, 
						-BulletGlobals.SIMD_EPSILON,
						-BulletGlobals.SIMD_HALF_PI * 0.5f);
				joint6DOF.setAngularLowerLimit(tmp);
				tmp.set(BulletGlobals.SIMD_HALF_PI * 0.6f,
						BulletGlobals.SIMD_EPSILON,
						BulletGlobals.SIMD_HALF_PI * 1.3f); //bend foward
				joint6DOF.setAngularUpperLimit(tmp);
				//#endif

				joints[JointType.JOINT_RIGHT_ELBOW.ordinal()] = joint6DOF;
				ownerWorld.addConstraint(
						joints[JointType.JOINT_RIGHT_ELBOW.ordinal()], true);
			}
			/// *************************** ///

			/// ******* RIGHT Wrist ******** ///
			{
				localA.setIdentity();
				localB.setIdentity();

				localA.origin
						.set(0f, limbLengths[BodyPart.BODYPART_RIGHT_LOWER_ARM
								.ordinal()], 0f); // upper arm
				localB.origin.set(0f,
						-limbLengths[BodyPart.BODYPART_RIGHT_HAND.ordinal()],
						0f); // lower arm
				joint6DOF = new Generic6DofConstraint(
						bodies[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()],
						bodies[BodyPart.BODYPART_RIGHT_HAND.ordinal()], localA,
						localB, useLinearReferenceFrameA);

				//#ifdef RIGID
				//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
				//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
				//#else
				tmp.set(-BulletGlobals.SIMD_EPSILON,
						-BulletGlobals.SIMD_EPSILON,
						-BulletGlobals.SIMD_EPSILON);
				joint6DOF.setAngularLowerLimit(tmp);
				tmp.set(BulletGlobals.SIMD_PI * 0.7f,
						BulletGlobals.SIMD_EPSILON, BulletGlobals.SIMD_EPSILON);
				joint6DOF.setAngularUpperLimit(tmp);
				//#endif

				joints[JointType.JOINT_RIGHT_WRIST.ordinal()] = joint6DOF;
				ownerWorld.addConstraint(
						joints[JointType.JOINT_RIGHT_WRIST.ordinal()], true);
			}
			/// *************************** ///

			/// ******* LEFT Wrist ******** ///
			{
				localA.setIdentity();
				localB.setIdentity();

				localA.origin
						.set(0f, limbLengths[BodyPart.BODYPART_LEFT_LOWER_ARM
								.ordinal()], 0f); // upper arm
				localB.origin
						.set(0f, -limbLengths[BodyPart.BODYPART_LEFT_HAND
								.ordinal()], 0f); // lower arm
				joint6DOF = new Generic6DofConstraint(
						bodies[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()],
						bodies[BodyPart.BODYPART_LEFT_HAND.ordinal()], localA,
						localB, useLinearReferenceFrameA);

				//#ifdef RIGID
				//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
				//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
				//#else
				tmp.set(-BulletGlobals.SIMD_EPSILON,
						-BulletGlobals.SIMD_EPSILON,
						-BulletGlobals.SIMD_EPSILON);
				joint6DOF.setAngularLowerLimit(tmp);
				tmp.set(BulletGlobals.SIMD_PI * 0.7f,
						BulletGlobals.SIMD_EPSILON, BulletGlobals.SIMD_EPSILON);
				joint6DOF.setAngularUpperLimit(tmp);
				//#endif

				joints[JointType.JOINT_LEFT_WRIST.ordinal()] = joint6DOF;
				ownerWorld.addConstraint(
						joints[JointType.JOINT_LEFT_WRIST.ordinal()], true);
			}
			/// *************************** ///
			 
			 
		}

		/// ******* LEFT HIP ******** ///
		{
			localA.setIdentity();
			localB.setIdentity();

			localA.origin.set(0f,
					limbLengths[BodyPart.BODYPART_PELVIS.ordinal()],
					limbWidths[BodyPart.BODYPART_PELVIS.ordinal()]);

			localB.origin
					.set(0f, -limbLengths[BodyPart.BODYPART_LEFT_UPPER_LEG
							.ordinal()] * .6f, 0f);

			joint6DOF = new Generic6DofConstraint(
					bodies[BodyPart.BODYPART_PELVIS.ordinal()],
					bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()], localA,
					localB, useLinearReferenceFrameA);

			//#ifdef RIGID
			//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
			//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
			//#else
			tmp.set(-BulletGlobals.SIMD_EPSILON, 
					-BulletGlobals.SIMD_EPSILON,
					-BulletGlobals.SIMD_HALF_PI * 0.5f);
			joint6DOF.setAngularLowerLimit(tmp);
			tmp.set(BulletGlobals.SIMD_HALF_PI * 0.6f,
					BulletGlobals.SIMD_EPSILON,
					BulletGlobals.SIMD_HALF_PI * 1.3f); //bend foward
			joint6DOF.setAngularUpperLimit(tmp);
			//#endif
			joints[JointType.JOINT_LEFT_HIP.ordinal()] = joint6DOF;
			ownerWorld.addConstraint(
					joints[JointType.JOINT_LEFT_HIP.ordinal()], true);
		}
		/// *************************** ///

		/// ******* RIGHT HIP ******** ///
		{
			localA.setIdentity();
			localB.setIdentity();

			localA.origin.set(0f,
					limbLengths[BodyPart.BODYPART_PELVIS.ordinal()],
					-limbWidths[BodyPart.BODYPART_PELVIS.ordinal()]);

			localB.origin
					.set(0f, -limbLengths[BodyPart.BODYPART_RIGHT_UPPER_LEG
							.ordinal()] * .6f, 0f);

			joint6DOF = new Generic6DofConstraint(
					bodies[BodyPart.BODYPART_PELVIS.ordinal()],
					bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()],
					localA, localB, useLinearReferenceFrameA);

			//#ifdef RIGID
			//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
			//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
			//#else
			tmp.set(-BulletGlobals.SIMD_HALF_PI * 0.6f,
					-BulletGlobals.SIMD_EPSILON,
					-BulletGlobals.SIMD_HALF_PI * 0.5f);
			joint6DOF.setAngularLowerLimit(tmp);
			tmp.set(BulletGlobals.SIMD_EPSILON, BulletGlobals.SIMD_EPSILON,
					BulletGlobals.SIMD_HALF_PI * 1.3f);
			joint6DOF.setAngularUpperLimit(tmp);
			//#endif
			joints[JointType.JOINT_RIGHT_HIP.ordinal()] = joint6DOF;
			ownerWorld.addConstraint(
					joints[JointType.JOINT_RIGHT_HIP.ordinal()], true);
		}
		/// *************************** ///

		/// ******* LEFT KNEE ******** ///
		{
			localA.setIdentity();
			localB.setIdentity();

			localA.origin
					.set(0f, limbLengths[BodyPart.BODYPART_LEFT_UPPER_LEG
							.ordinal()], 0f);
			localB.origin.set(0f,
					-limbLengths[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()],
					0f);
			joint6DOF = new Generic6DofConstraint(
					bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()],
					bodies[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()], localA,
					localB, useLinearReferenceFrameA);
			//
			//#ifdef RIGID
			//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
			//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
			//#else
			tmp.set(-BulletGlobals.SIMD_EPSILON, -BulletGlobals.SIMD_EPSILON,
					-BulletGlobals.SIMD_PI * .9f);
			joint6DOF.setAngularLowerLimit(tmp);
			tmp.set(BulletGlobals.SIMD_EPSILON, BulletGlobals.SIMD_EPSILON,
					BulletGlobals.SIMD_EPSILON);
			joint6DOF.setAngularUpperLimit(tmp);
			//#endif
			joints[JointType.JOINT_LEFT_KNEE.ordinal()] = joint6DOF;
			ownerWorld.addConstraint(
					joints[JointType.JOINT_LEFT_KNEE.ordinal()], true);
		}
		/// *************************** ///

		/// ******* RIGHT KNEE ******** ///
		{
			localA.setIdentity();
			localB.setIdentity();
			HingeConstraint jointHinge = null;
			localA.origin.set(0f,
					limbLengths[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()],
					0f);
			localB.origin.set(0f,
					-limbLengths[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()],
					0f);
			joint6DOF = new Generic6DofConstraint(
					bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()],
					bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()],
					localA, localB, useLinearReferenceFrameA);
			//
			//#ifdef RIGID
			//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
			//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
			//#else
			tmp.set(-BulletGlobals.SIMD_EPSILON, -BulletGlobals.SIMD_EPSILON,
					-BulletGlobals.SIMD_PI * .9f);
			joint6DOF.setAngularLowerLimit(tmp);
			tmp.set(BulletGlobals.SIMD_EPSILON, BulletGlobals.SIMD_EPSILON,
					BulletGlobals.SIMD_EPSILON);
			joint6DOF.setAngularUpperLimit(tmp);
			//#endif
			joints[JointType.JOINT_RIGHT_KNEE.ordinal()] = joint6DOF;
			ownerWorld.addConstraint(
					joints[JointType.JOINT_RIGHT_KNEE.ordinal()], true);
		}
		/// *************************** ///

		/// ******* RIGHT FOOT ******** ///
		{
			localA.setIdentity();
			localB.setIdentity();
			HingeConstraint jointHinge = null;
			localA.origin.set(0f,
					limbLengths[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()],
					0f);
			localB.origin.set(0f,
					-limbLengths[BodyPart.BODYPART_RIGHT_FOOT.ordinal()] * .5f,
					0f);
			joint6DOF = new Generic6DofConstraint(
					bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()],
					bodies[BodyPart.BODYPART_RIGHT_FOOT.ordinal()], localA,
					localB, useLinearReferenceFrameA);
			//
			//#ifdef RIGID
			//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
			//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
			//#else
			tmp.set(-BulletGlobals.SIMD_EPSILON, -BulletGlobals.SIMD_EPSILON,
					BulletGlobals.SIMD_EPSILON);
			joint6DOF.setAngularLowerLimit(tmp);
			tmp.set(BulletGlobals.SIMD_EPSILON, BulletGlobals.SIMD_EPSILON,
					BulletGlobals.SIMD_PI * .7f);
			joint6DOF.setAngularUpperLimit(tmp);
			//#endif
			joints[JointType.JOINT_RIGHT_ANKLE.ordinal()] = joint6DOF;
			ownerWorld.addConstraint(
					joints[JointType.JOINT_RIGHT_ANKLE.ordinal()], true);
		}
		/// *************************** ///

		/// ******* LEFT FOOT ******** ///
		{
			localA.setIdentity();
			localB.setIdentity();
			HingeConstraint jointHinge = null;
			localA.origin
					.set(0f, limbLengths[BodyPart.BODYPART_LEFT_LOWER_LEG
							.ordinal()], 0f);
			localB.origin.set(0f,
					-limbLengths[BodyPart.BODYPART_LEFT_FOOT.ordinal()] * .5f,
					0f);
			joint6DOF = new Generic6DofConstraint(
					bodies[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()],
					bodies[BodyPart.BODYPART_LEFT_FOOT.ordinal()], localA,
					localB, useLinearReferenceFrameA);
			//
			//#ifdef RIGID
			//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
			//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
			//#else
			tmp.set(-BulletGlobals.SIMD_EPSILON, -BulletGlobals.SIMD_EPSILON,
					BulletGlobals.SIMD_EPSILON);
			joint6DOF.setAngularLowerLimit(tmp);
			tmp.set(BulletGlobals.SIMD_EPSILON, BulletGlobals.SIMD_EPSILON,
					BulletGlobals.SIMD_PI * .7f);
			joint6DOF.setAngularUpperLimit(tmp);
			//#endif
			joints[JointType.JOINT_LEFT_ANKLE.ordinal()] = joint6DOF;
			ownerWorld.addConstraint(
					joints[JointType.JOINT_LEFT_ANKLE.ordinal()], true);
		}
		/// *************************** ///

	}

	void buildProportions() {

		float[] tempProportions1 = new float[BodyPart.BODYPART_COUNT.ordinal()];
		float[] tempProportionsWidth1 = new float[BodyPart.BODYPART_COUNT
				.ordinal()];

		float[] tempProportions2 = new float[BodyPart.BODYPART_COUNT.ordinal()];
		float[] tempProportionsWidth2 = new float[BodyPart.BODYPART_COUNT
				.ordinal()];

		//Adult 
		//these proportions are based on a human body being 7.5 heads tall
		tempProportions1[BodyPart.BODYPART_HEAD.ordinal()] = 0.13f;
		tempProportions1[BodyPart.BODYPART_NECK.ordinal()] = 0.09f;
		tempProportions1[BodyPart.BODYPART_SPINE.ordinal()] = 0.23f;
		tempProportions1[BodyPart.BODYPART_PELVIS.ordinal()] = 0.09f;
		tempProportions1[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()] = 0.28f;
		tempProportions1[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()] = 0.22f;
		tempProportions1[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()] = 0.28f;
		tempProportions1[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()] = 0.22f;
		tempProportions1[BodyPart.BODYPART_RIGHT_FOOT.ordinal()] = 0.15f;
		tempProportions1[BodyPart.BODYPART_LEFT_FOOT.ordinal()] = 0.15f;
		tempProportions1[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()] = 0.175f;
		tempProportions1[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()] = 0.145f;
		tempProportions1[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()] = 0.175f;
		tempProportions1[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()] = 0.145f;
		tempProportions1[BodyPart.BODYPART_RIGHT_HAND.ordinal()] = 0.08f;
		tempProportions1[BodyPart.BODYPART_LEFT_HAND.ordinal()] = 0.08f;

		tempProportionsWidth1[BodyPart.BODYPART_HEAD.ordinal()] = 0.093f;
		tempProportionsWidth1[BodyPart.BODYPART_NECK.ordinal()] = 0.05f;
		tempProportionsWidth1[BodyPart.BODYPART_SPINE.ordinal()] = 0.12f;
		tempProportionsWidth1[BodyPart.BODYPART_PELVIS.ordinal()] = 0.11f;
		tempProportionsWidth1[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()] = 0.087f;
		tempProportionsWidth1[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()] = 0.052f;
		tempProportionsWidth1[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()] = 0.087f;
		tempProportionsWidth1[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()] = 0.052f;
		tempProportionsWidth1[BodyPart.BODYPART_RIGHT_FOOT.ordinal()] = 0.055f;
		tempProportionsWidth1[BodyPart.BODYPART_LEFT_FOOT.ordinal()] = 0.055f;
		tempProportionsWidth1[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()] = 0.045f;
		tempProportionsWidth1[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()] = 0.035f;
		tempProportionsWidth1[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()] = 0.045f;
		tempProportionsWidth1[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()] = 0.035f;
		tempProportionsWidth1[BodyPart.BODYPART_RIGHT_HAND.ordinal()] = 0.05f;
		tempProportionsWidth1[BodyPart.BODYPART_LEFT_HAND.ordinal()] = 0.05f;

		//proportionsGrowthLenMap.put(1, tempProportions);
		//proportionsGrowthWidMap.put(1, tempProportionsWidth);

		//15 years old 
		//these proportions are based on a human body being 7.5 heads tall
		tempProportions2[BodyPart.BODYPART_HEAD.ordinal()] = 0.25f;
		tempProportions2[BodyPart.BODYPART_NECK.ordinal()] = 0.04f;
		tempProportions2[BodyPart.BODYPART_SPINE.ordinal()] = 0.23f;
		tempProportions2[BodyPart.BODYPART_PELVIS.ordinal()] = 0.17f;
		tempProportions2[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()] = 0.19f;
		tempProportions2[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()] = 0.16f;
		tempProportions2[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()] = 0.19f;
		tempProportions2[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()] = 0.16f;
		tempProportions2[BodyPart.BODYPART_RIGHT_FOOT.ordinal()] = 0.1f;
		tempProportions2[BodyPart.BODYPART_LEFT_FOOT.ordinal()] = 0.1f;
		tempProportions2[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()] = 0.16f;
		tempProportions2[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()] = 0.140f;
		tempProportions2[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()] = 0.16f;
		tempProportions2[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()] = 0.140f;
		tempProportions2[BodyPart.BODYPART_RIGHT_HAND.ordinal()] = 0.06f;
		tempProportions2[BodyPart.BODYPART_LEFT_HAND.ordinal()] = 0.06f;

		tempProportionsWidth2[BodyPart.BODYPART_HEAD.ordinal()] = 0.2f;
		tempProportionsWidth2[BodyPart.BODYPART_NECK.ordinal()] = 0.02f;
		tempProportionsWidth2[BodyPart.BODYPART_SPINE.ordinal()] = 0.22f;
		tempProportionsWidth2[BodyPart.BODYPART_PELVIS.ordinal()] = 0.2f;
		tempProportionsWidth2[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()] = 0.11f;
		tempProportionsWidth2[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()] = 0.1f;
		tempProportionsWidth2[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()] = 0.11f;
		tempProportionsWidth2[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()] = 0.1f;
		tempProportionsWidth2[BodyPart.BODYPART_RIGHT_FOOT.ordinal()] = 0.055f;
		tempProportionsWidth2[BodyPart.BODYPART_LEFT_FOOT.ordinal()] = 0.055f;
		tempProportionsWidth2[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()] = 0.035f;
		tempProportionsWidth2[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()] = 0.025f;
		tempProportionsWidth2[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()] = 0.035f;
		tempProportionsWidth2[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()] = 0.025f;
		tempProportionsWidth2[BodyPart.BODYPART_RIGHT_HAND.ordinal()] = 0.03f;
		tempProportionsWidth2[BodyPart.BODYPART_LEFT_HAND.ordinal()] = 0.03f;

		//proportionsGrowthLenMap.put(0, tempProportions);
		//proportionsGrowthWidMap.put(0, tempProportionsWidth);	 

		//  float[] tempProportions1 = (float[]) proportionsGrowthLenMap.get(0);
		//  float[] tempProportionsWidth1 = (float[]) proportionsGrowthWidMap.get(0);

		// 
		//  float[] tempProportions2 = (float[]) proportionsGrowthLenMap.get(1);
		//  float[] tempProportionsWidth2 = (float[]) proportionsGrowthWidMap.get(1);

		float delta = this.scale;

		delta = PApplet.map(delta, 1, .3f, 1, 0);

		if (delta < 0)
			delta = 0;

		// Setup some damping on the m_bodies
		for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
if(delta <= 1){
			proportions[i] = (tempProportions2[i] * (1 - delta))
					+ (tempProportions1[i] * (delta));
}else{
	proportions[i] = tempProportions1[i] * (delta);
}
		}

		for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			if(delta <= 1){
			proportionsWidth[i] = (tempProportionsWidth2[i] * (1 - delta))
					+ (tempProportionsWidth1[i] * (delta));
			}else{
				proportionsWidth[i] = tempProportionsWidth1[i] * (delta);
				
			}
		}

		// Setup some damping on the m_bodies
		for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			limbLengths[i] = (this.height * proportions[i]) * this.scale;
		}

		// Setup some damping on the m_bodies
		for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			limbWidths[i] = (this.height * proportionsWidth[i]) * this.scale;
		}

	}

	void collisions() {
		//Setup some damping on the m_bodies
		for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			if (this.bodies[i] != null)
				this.bodies[i]
						.setCollisionFlags(CollisionFlags.KINEMATIC_OBJECT);
		}
	}

	public void destroy() {
		int i;

		// Remove all constraints
		for (i = 0; i < JointType.JOINT_COUNT.ordinal(); ++i) {
			if (joints[i] != null) {
				ownerWorld.removeConstraint(joints[i]);
				//joints[i].destroy();
				joints[i] = null;
			}
		}

		// Remove all bodies and shapes
		for (i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			if (bodies[i] != null) {
				ownerWorld.removeRigidBody(bodies[i]);
				//ownerWorld.removeCollisionObject(bodies[i].getCollisionShape().);
				//ownerWorld.getCollisionObjectArray().remove(bodies[i].getCollisionShape());
				ownerWorld.removeCollisionObject(bodies[i]);
				//bodies[i].getMotionState().

				//bodies[i].destroy();
				bodies[i] = null;

				//shapes[i].destroy();
				shapes[i] = null;
			}
		}
	}

	public void dragScale(float mouseX, float mouseY) {
		//this.freeze();

	}

	public void freeze() {

		// Setup some damping on the m_bodies
		for (int i = 1; i < JointType.JOINT_COUNT.ordinal(); ++i) {
			if (joints[i] != null) {
				Generic6DofConstraint joint = (Generic6DofConstraint) joints[i];
				joint.setLimit(3, joint.getAngle(0), joint.getAngle(0));
				joint.setLimit(4, joint.getAngle(1), joint.getAngle(1));
				joint.setLimit(5, joint.getAngle(2), joint.getAngle(2));
				//joint.setLimit(3, joint.getAngle(3), joint.getAngle(3));
				//joint.setLimit(4, joint.getAngle(4), joint.getAngle(4));
				//joint.setLimit(5, joint.getAngle(5), joint.getAngle(5));
			}
		}

	}

	public float getScale() {
		return this.scale;
	}

	void hide() {
		this.translate(10000, 100, 0);
		this.noCollisions();

	}

	void liftLeg() {
		Generic6DofConstraint dofConstraint = (Generic6DofConstraint) joints[JointType.JOINT_RIGHT_KNEE
				.ordinal()];
		dofConstraint.buildJacobian();
		float angle = dofConstraint.getAngle(1);
		dofConstraint.testAngularLimitMotor(1);
		//	RotationalLimitMotor lim = dofConstraint.getRotationalLimitMotor(1);
		TranslationalLimitMotor translim = dofConstraint
				.getTranslationalLimitMotor();
		//dofConstraint.
		//	translim.
	}

	private RigidBody localCreateRigidBody(float mass,
			Transform startTransform, CollisionShape shape) {
		boolean isDynamic = (mass != 0f);

		Vector3f localInertia = new Vector3f();
		localInertia.set(0f, 0f, 0f);
		if (isDynamic) {
			shape.calculateLocalInertia(mass, localInertia);
		}

		DefaultMotionState myMotionState = new DefaultMotionState(
				startTransform);
		RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass,
				myMotionState, shape, localInertia);
		rbInfo.additionalDamping = true;
		RigidBody body = new RigidBody(rbInfo);

		ownerWorld.addRigidBody(body);

		return body;
	}

	void makeBody(DynamicsWorld ownerWorld, Vector3f positionOffset,
			float scale_ragdoll) {

		this.buildScale = scale_ragdoll;
		Transform tmpTrans = new Transform();
		Vector3f tmp = new Vector3f();
		// Setup the geometry
		int partNum = 0;
		this.buildProportions();

		// Setup some damping on the m_bodies
		for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			shapes[i] = new CapsuleShape(limbWidths[i], limbLengths[i]);
		}

		// Setup all the rigid bodies
		Transform offset = new Transform();
		offset.setIdentity();
		offset.origin.set(positionOffset);

		Transform transform = new Transform();

		transform.setIdentity();
		transform.origin.set(42.00441f, 28.350739f, -1.8362772f);
		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_HEAD.ordinal()] = localCreateRigidBody(
				.06f * this.mass, tmpTrans,
				shapes[BodyPart.BODYPART_HEAD.ordinal()]);

		transform.setIdentity();
		transform.origin.set(40.328167f, 34.438187f, -0.02232361f);
		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_SPINE.ordinal()] = localCreateRigidBody(
				1f * this.mass, tmpTrans,
				shapes[BodyPart.BODYPART_SPINE.ordinal()]);

		transform.setIdentity();
		transform.origin.set(38.91143f, 39.836838f, 0.29198787f);

		tmpTrans.mul(offset, transform);

		bodies[BodyPart.BODYPART_PELVIS.ordinal()] = localCreateRigidBody(
				1f * this.mass, tmpTrans,
				shapes[BodyPart.BODYPART_PELVIS.ordinal()]);

		//	transform.setIdentity();
		//	transform.origin.set(0f, scale_ragdoll * -1.6f, 0f);
		//	tmpTrans.mul(offset, transform);
		//	bodies[BodyPart.BODYPART_NECK.ordinal()] = localCreateRigidBody(.1f, tmpTrans, shapes[BodyPart.BODYPART_NECK.ordinal()]);

		if (this.hasArms) {
			transform.setIdentity();
			transform.origin.set(41.880104f, 33.694458f, 2.016673f);

			MatrixUtil.setEulerZYX(transform.basis,
					-BulletGlobals.SIMD_HALF_PI, 0, 0);
			tmpTrans.mul(offset, transform);
			bodies[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()] = localCreateRigidBody(
					.006f * this.mass, tmpTrans,
					shapes[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()]);

			transform.setIdentity();
			transform.origin.set(42.41656f, 37.918385f, 0.61924f);

			MatrixUtil.setEulerZYX(transform.basis,
					-BulletGlobals.SIMD_HALF_PI, 0, 0);
			tmpTrans.mul(offset, transform);
			bodies[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()] = localCreateRigidBody(
					.006f * this.mass, tmpTrans,
					shapes[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()]);

			transform.setIdentity();
			transform.origin.set(42.514267f, 33.331062f, -3.8124154f);

			MatrixUtil.setEulerZYX(transform.basis,
					-BulletGlobals.SIMD_HALF_PI, 0, 0);
			tmpTrans.mul(offset, transform);
			bodies[BodyPart.BODYPART_LEFT_HAND.ordinal()] = localCreateRigidBody(
					.006f * this.mass, tmpTrans,
					shapes[BodyPart.BODYPART_LEFT_HAND.ordinal()]);

			transform.setIdentity();
			transform.setIdentity();
			transform.origin.set(39.261406f, 46.36395f, 2.412697f);

			MatrixUtil.setEulerZYX(transform.basis, BulletGlobals.SIMD_HALF_PI,
					0, 0);
			tmpTrans.mul(offset, transform);
			bodies[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()] = localCreateRigidBody(
					.006f * this.mass, tmpTrans,
					shapes[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()]);

			transform.setIdentity();
			transform.origin.set(39.526512f, 54.545227f, 2.1793346f);

			MatrixUtil.setEulerZYX(transform.basis, BulletGlobals.SIMD_HALF_PI,
					0, 0);
			tmpTrans.mul(offset, transform);
			bodies[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()] = localCreateRigidBody(
					.006f * this.mass, tmpTrans,
					shapes[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()]);

			transform.setIdentity();
			transform.origin.set(42.192146f, 40.39129f, -1.8401904f);

			MatrixUtil.setEulerZYX(transform.basis, BulletGlobals.SIMD_HALF_PI,
					0, 0);
			tmpTrans.mul(offset, transform);
			bodies[BodyPart.BODYPART_RIGHT_HAND.ordinal()] = localCreateRigidBody(
					.006f * this.mass, tmpTrans,
					shapes[BodyPart.BODYPART_RIGHT_HAND.ordinal()]);

		}

		//LEFT LEG
		transform.setIdentity();
		transform.origin.set(39.261406f, 46.36395f, 2.412697f);

		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()] = localCreateRigidBody(
				1.2f * this.mass, tmpTrans,
				shapes[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()]);

		transform.setIdentity();
		transform.origin.set(39.526512f, 54.545227f, 2.1793346f);

		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()] = localCreateRigidBody(
				.6f * this.mass, tmpTrans,
				shapes[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()]);

		transform.setIdentity();
		transform.origin.set(39.758045f, 59.2257f, 2.0889432f);

		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_LEFT_FOOT.ordinal()] = localCreateRigidBody(
				.6f * this.mass, tmpTrans,
				shapes[BodyPart.BODYPART_LEFT_FOOT.ordinal()]);

		//RIGHT LEG
		transform.setIdentity();
		transform.origin.set(39.261406f, 46.36395f, -2.412697f);
		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()] = localCreateRigidBody(
				1.2f * this.mass, tmpTrans,
				shapes[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()]);

		transform.setIdentity();
		transform.origin.set(39.526512f, 54.545227f, -2.1793346f);

		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()] = localCreateRigidBody(
				.6f * this.mass, tmpTrans,
				shapes[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()]);

		transform.setIdentity();
		transform.origin.set(39.758045f, 59.2257f, -2.0889432f);
		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_RIGHT_FOOT.ordinal()] = localCreateRigidBody(
				0.6f * this.mass, tmpTrans,
				shapes[BodyPart.BODYPART_RIGHT_FOOT.ordinal()]);

		
		
		//Remembered positions 
		//BODYPART_HEAD
		bodies[BodyPart.BODYPART_HEAD.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.set(new Matrix4f(0.999522f, -0.02457619f, 0.01875775f,
				103.311295f, 0.03083701f, 0.83602184f, -0.547829f,
				22.693146f * this.scale, -0.0022183368f, 0.5481455f, 0.83638f,
				-1.0370132f * this.scale, 0.0f, 0.0f, 0.0f, 1.0f * this.scale));
		bodies[BodyPart.BODYPART_HEAD.ordinal()]
				.setMotionState(new DefaultMotionState(transform));

		//BODYPART_SPINE
		bodies[BodyPart.BODYPART_SPINE.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.setIdentity();
		transform.set(new Matrix4f(0.9932064f, 0.116363324f, 7.786779E-4f,
				104.067566f, -0.1162641f, 0.99203515f, 0.04846615f,
				31.873365f * this.scale, 0.004867207f, -0.048227426f,
				0.99882454f, 0.0021075667f * this.scale, 0.0f, 0.0f, 0.0f,
				1.0f * this.scale));
		bodies[BodyPart.BODYPART_SPINE.ordinal()]
				.setMotionState(new DefaultMotionState(transform));

		//BODYPART_PELVIS
		bodies[BodyPart.BODYPART_PELVIS.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.setIdentity();
		transform.set(new Matrix4f(0.9835286f, 0.17898695f, -0.025202362f,
				105.264946f, -0.18059899f, 0.97884035f, -0.09620573f,
				40.34729f * this.scale, 0.007449517f, 0.099172615f, 0.9950424f,
				0.04683579f * this.scale, 0.0f, 0.0f, 0.0f, 1.0f * this.scale));
		bodies[BodyPart.BODYPART_PELVIS.ordinal()]
				.setMotionState(new DefaultMotionState(transform));

		//BODYPART_LEFT_UPPER_LEG
		bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.setIdentity();
		transform.set(new Matrix4f(0.29188365f, 0.9564537f, -4.4153258E-4f,
				109.79127f, -0.9526396f, 0.29067844f, -0.0893526f,
				44.345417f * this.scale, -0.08533329f, 0.026501186f,
				0.99599993f, 3.9808998f * this.scale, 0.0f, 0.0f, 0.0f,
				1.0f * this.scale));
		bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()]
				.setMotionState(new DefaultMotionState(transform));

		//BODYPART_LEFT_LOWER_LEG
		bodies[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.setIdentity();
		transform.set(new Matrix4f(0.9873329f, -0.15865491f, 0.0015572663f,
				115.62421f, 0.15813647f, 0.98321307f, -0.09102161f,
				52.051834f * this.scale, 0.012909901f, 0.09011489f, 0.9958477f,
				4.6894903f * this.scale, 0.0f, 0.0f, 0.0f, 1.0f * this.scale));
		bodies[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()]
				.setMotionState(new DefaultMotionState(transform));

		//RIGHT_UPPER_LEG
		bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.setIdentity();
		transform.set(new Matrix4f(0.23838913f, 0.9675928f, -0.08327571f,
				110.01697f, -0.9671425f, 0.22872531f, -0.110996194f,
				44.76596f * this.scale, -0.08835185f, 0.10699976f, 0.9903257f,
				-2.6929f * this.scale, 0.0f, 0.0f, 0.0f, 1.0f * this.scale));
		bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()]
				.setMotionState(new DefaultMotionState(transform));

		//BODYPART_RIGHT_LOWER_LEG
		bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.setIdentity();
		transform.set(new Matrix4f(0.9864584f, -0.14225316f, -0.081632055f,
				116.022156f, 0.13265687f, 0.98470926f, -0.11291519f,
				52.043633f * this.scale, 0.09644639f, 0.10055709f, 0.9902456f,
				-1.3562305f * this.scale, 0.0f, 0.0f, 0.0f, 1.0f * this.scale));
		bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()]
				.setMotionState(new DefaultMotionState(transform));

		//BODYPART_RIGHT_FOOT
		bodies[BodyPart.BODYPART_RIGHT_FOOT.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.setIdentity();
		transform.set(new Matrix4f(0.00923717f, 0.9967484f, -0.080045804f,
				116.96003f, -0.99355465f, 1.0448694E-4f, -0.11335374f,
				57.707912f * this.scale, -0.11297679f, 0.08057695f, 0.990325f,
				-0.6338565f * this.scale, 0.0f, 0.0f, 0.0f, 1.0f * this.scale));
		bodies[BodyPart.BODYPART_RIGHT_FOOT.ordinal()]
				.setMotionState(new DefaultMotionState(transform));

		//BODYPART_LEFT_FOOT
		bodies[BodyPart.BODYPART_LEFT_FOOT.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.setIdentity();
		transform.set(new Matrix4f(-3.4880638E-4f, 0.99999404f, 0.0034378879f,
				116.47286f, -0.9958018f, -3.2663345E-5f, -0.09153569f,
				57.707455f * this.scale, -0.09153503f, -0.0034553856f,
				0.99579585f, 5.203514f * this.scale, 0.0f, 0.0f, 0.0f,
				1.0f * this.scale));
		bodies[BodyPart.BODYPART_LEFT_FOOT.ordinal()]
				.setMotionState(new DefaultMotionState(transform));

		// Setup some damping on the m_bodies
		for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			if (bodies[i] != null) {
				bodies[i].setDamping(0.5f, 9.95f);
				bodies[i].setDeactivationTime(0.8f);
				bodies[i].setSleepingThresholds(1.6f, 2.5f);
				bodies[i].setFriction(SETTINGS.person_friction);
			}
		}

		this.buildLimbConstraints();
	//	GLOBAL.jBullet.update();
		this.freeze();

	}

	public void mouseClicked(float mouseX, float mouseY) {

		boolean overPerson = false;

		if (this.mouseOver(mouseX, mouseY)) {
			this.clickedOnPerson = true;
			overPerson = true;

			if (GLOBAL.jBullet.physics_on)
				this.buildLimbConstraints();

		}
	}

	public void mouseDown(float mouseX, float mouseY) {

		if (!GLOBAL.jBullet.physics_on && GLOBAL.personTranslate
				&& this.clickedOnPerson) {
			float deltaMx = (GLOBAL.uiTools.mouseXworld - GLOBAL.uiTools.pmouseXworld)
					* GLOBAL.jBullet.scale;
			float deltaMy = (GLOBAL.uiTools.mouseYworld - GLOBAL.uiTools.pmouseYworld)
					* GLOBAL.jBullet.scale;
			
				float maxMouse = SETTINGS.mouseMoveClamp; 
				
				if(deltaMx > maxMouse || deltaMx < -maxMouse ||
						deltaMy > maxMouse || deltaMy < -maxMouse 
						)
					return;
					
					
				
			this.translate(deltaMx,deltaMy
					, 0);
		}

		if (GLOBAL.uiTools.getCurrentTool() == UITools.SCALE_TOOL
				&& this.clickedOnPerson) {
			float scaleDelta = (GLOBAL.uiTools.mouseY - GLOBAL.uiTools.pmouseY);
			scaleDelta /= 100;

			//if(this.scale > 0 && this.scale < 100 )
			GLOBAL.person.scale(scaleDelta);

			GLOBAL.jBullet.update();

		}
		
		if(this.clickedOnPerson)
		this.dragged = true;


	}

	public boolean mouseOver(float mouseX, float mouseY) {
		RigidBody body = GLOBAL.jBullet.getOver(mouseX, mouseY);
		if (body == null || !this.on) {
			return false;
		} else {

			if (body != null
					&& body == bodies[BodyPart.BODYPART_HEAD.ordinal()]
					|| body == bodies[BodyPart.BODYPART_SPINE.ordinal()]
					|| body == bodies[BodyPart.BODYPART_PELVIS.ordinal()]
					|| body == bodies[BodyPart.BODYPART_LEFT_UPPER_LEG
							.ordinal()]
					|| body == bodies[BodyPart.BODYPART_LEFT_LOWER_LEG
							.ordinal()]
					|| body == bodies[BodyPart.BODYPART_LEFT_FOOT.ordinal()]
					|| body == bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG
							.ordinal()]
					|| body == bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG
							.ordinal()]
					|| body == bodies[BodyPart.BODYPART_RIGHT_FOOT.ordinal()]
					|| body == bodies[BodyPart.BODYPART_LEFT_UPPER_ARM
							.ordinal()]
					|| body == bodies[BodyPart.BODYPART_LEFT_LOWER_ARM
							.ordinal()]
					|| body == bodies[BodyPart.BODYPART_LEFT_HAND.ordinal()]
					|| body == bodies[BodyPart.BODYPART_RIGHT_UPPER_ARM
							.ordinal()]
					|| body == bodies[BodyPart.BODYPART_RIGHT_LOWER_ARM
							.ordinal()]
					|| body == bodies[BodyPart.BODYPART_RIGHT_HAND.ordinal()]

			) {
				return true;
			} else {
				return false;
			}
		}

	}

	public void mouseReleased(float mouseX, float mouseY) {
		this.clickedOnPerson = false;
		//if(GLOBAL.uiTools.currentTool == UITools.MOVE_OBJECT ){
		this.freeze();
		//}	
	}

	void noCollisions() {
		//Setup some damping on the m_bodies
		for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			if (this.bodies[i] != null)
				this.bodies[i]
						.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
		}
	}

	private boolean overlapsWith(RigidBody rigidBody) {
		GLOBAL.jBullet.myWorld.performDiscreteCollisionDetection();
		//performDiscreteCollisionDetection 
		for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			if (bodies[i] != null && rigidBody != null) {

				CollisionAlgorithm Algorithm = GLOBAL.jBullet.myWorld
						.getDispatcher().findAlgorithm(rigidBody, bodies[i]);//getDispatcher()->findAlgorithm( pBulletObj1, pBulletObj2 );
				ManifoldResult manifoldResult = new ManifoldResult(rigidBody,
						bodies[i]);
				Algorithm.processCollision(rigidBody, bodies[i],
						GLOBAL.jBullet.myWorld.getDispatchInfo(),
						manifoldResult);
				PersistentManifold pManifold = manifoldResult
						.getPersistentManifold();
				if (pManifold != null){
					GLOBAL.jBullet.myWorld.getCollisionWorld().getDispatcher().releaseManifold(pManifold);
 					return true;
				}

			}
		}
		return false;

	}

	public void printOrigins() {

		Transform transform = new Transform();
		Vector3f origin = new Vector3f();
		Matrix4f matrix = new Matrix4f();

		bodies[BodyPart.BODYPART_HEAD.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//BODYPART_HEAD");
		System.out
				.println("bodies[BodyPart.BODYPART_HEAD.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.set(new Matrix4f(" + matrix.m00 + "f,"
				+ matrix.m01 + "f," + matrix.m02 + "f," + matrix.m03 + "f,"
				+ matrix.m10 + "f," + matrix.m11 + "f," + matrix.m12 + "f,"
				+ matrix.m13 + "f * this.scale," + matrix.m20 + "f,"
				+ matrix.m21 + "f," + matrix.m22 + "f," + matrix.m23
				+ "f * this.scale," + matrix.m30 + "f," + matrix.m31 + "f,"
				+ matrix.m32 + "f," + matrix.m33 + "f * this.scale));");
		System.out
				.println("bodies[BodyPart.BODYPART_HEAD.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();

		bodies[BodyPart.BODYPART_SPINE.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//BODYPART_SPINE");
		System.out
				.println("bodies[BodyPart.BODYPART_SPINE.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.setIdentity();");
		System.out.println("transform.set(new Matrix4f(" + matrix.m00 + "f,"
				+ matrix.m01 + "f," + matrix.m02 + "f," + matrix.m03 + "f,"
				+ matrix.m10 + "f," + matrix.m11 + "f," + matrix.m12 + "f,"
				+ matrix.m13 + "f * this.scale," + matrix.m20 + "f,"
				+ matrix.m21 + "f," + matrix.m22 + "f," + matrix.m23
				+ "f * this.scale," + matrix.m30 + "f," + matrix.m31 + "f,"
				+ matrix.m32 + "f," + matrix.m33 + "f * this.scale));");
		System.out
				.println("bodies[BodyPart.BODYPART_SPINE.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();

		bodies[BodyPart.BODYPART_PELVIS.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//BODYPART_PELVIS");
		System.out
				.println("bodies[BodyPart.BODYPART_PELVIS.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.setIdentity();");
		System.out.println("transform.set(new Matrix4f(" + matrix.m00 + "f,"
				+ matrix.m01 + "f," + matrix.m02 + "f," + matrix.m03 + "f,"
				+ matrix.m10 + "f," + matrix.m11 + "f," + matrix.m12 + "f,"
				+ matrix.m13 + "f * this.scale," + matrix.m20 + "f,"
				+ matrix.m21 + "f," + matrix.m22 + "f," + matrix.m23
				+ "f * this.scale," + matrix.m30 + "f," + matrix.m31 + "f,"
				+ matrix.m32 + "f," + matrix.m33 + "f * this.scale));");
		System.out
				.println("bodies[BodyPart.BODYPART_PELVIS.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();

		bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//BODYPART_LEFT_UPPER_LEG");
		System.out
				.println("bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.setIdentity();");
		System.out.println("transform.set(new Matrix4f(" + matrix.m00 + "f,"
				+ matrix.m01 + "f," + matrix.m02 + "f," + matrix.m03 + "f,"
				+ matrix.m10 + "f," + matrix.m11 + "f," + matrix.m12 + "f,"
				+ matrix.m13 + "f * this.scale," + matrix.m20 + "f,"
				+ matrix.m21 + "f," + matrix.m22 + "f," + matrix.m23
				+ "f * this.scale," + matrix.m30 + "f," + matrix.m31 + "f,"
				+ matrix.m32 + "f," + matrix.m33 + "f * this.scale));");
		System.out
				.println("bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();

		bodies[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//BODYPART_LEFT_LOWER_LEG");
		System.out
				.println("bodies[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.setIdentity();");
		System.out.println("transform.set(new Matrix4f(" + matrix.m00 + "f,"
				+ matrix.m01 + "f," + matrix.m02 + "f," + matrix.m03 + "f,"
				+ matrix.m10 + "f," + matrix.m11 + "f," + matrix.m12 + "f,"
				+ matrix.m13 + "f * this.scale," + matrix.m20 + "f,"
				+ matrix.m21 + "f," + matrix.m22 + "f," + matrix.m23
				+ "f * this.scale," + matrix.m30 + "f," + matrix.m31 + "f,"
				+ matrix.m32 + "f," + matrix.m33 + "f * this.scale));");
		System.out
				.println("bodies[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();

		bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//RIGHT_UPPER_LEG");
		System.out
				.println("bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.setIdentity();");
		System.out.println("transform.set(new Matrix4f(" + matrix.m00 + "f,"
				+ matrix.m01 + "f," + matrix.m02 + "f," + matrix.m03 + "f,"
				+ matrix.m10 + "f," + matrix.m11 + "f," + matrix.m12 + "f,"
				+ matrix.m13 + "f * this.scale," + matrix.m20 + "f,"
				+ matrix.m21 + "f," + matrix.m22 + "f," + matrix.m23
				+ "f * this.scale," + matrix.m30 + "f," + matrix.m31 + "f,"
				+ matrix.m32 + "f," + matrix.m33 + "f * this.scale));");
		System.out
				.println("bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();

		bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//BODYPART_RIGHT_LOWER_LEG");
		System.out
				.println("bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.setIdentity();");
		System.out.println("transform.set(new Matrix4f(" + matrix.m00 + "f,"
				+ matrix.m01 + "f," + matrix.m02 + "f," + matrix.m03 + "f,"
				+ matrix.m10 + "f," + matrix.m11 + "f," + matrix.m12 + "f,"
				+ matrix.m13 + "f * this.scale," + matrix.m20 + "f,"
				+ matrix.m21 + "f," + matrix.m22 + "f," + matrix.m23
				+ "f * this.scale," + matrix.m30 + "f," + matrix.m31 + "f,"
				+ matrix.m32 + "f," + matrix.m33 + "f * this.scale));");
		System.out
				.println("bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();

		bodies[BodyPart.BODYPART_RIGHT_FOOT.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//BODYPART_RIGHT_FOOT");
		System.out
				.println("bodies[BodyPart.BODYPART_RIGHT_FOOT.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.setIdentity();");
		System.out.println("transform.set(new Matrix4f(" + matrix.m00 + "f,"
				+ matrix.m01 + "f," + matrix.m02 + "f," + matrix.m03 + "f,"
				+ matrix.m10 + "f," + matrix.m11 + "f," + matrix.m12 + "f,"
				+ matrix.m13 + "f * this.scale," + matrix.m20 + "f,"
				+ matrix.m21 + "f," + matrix.m22 + "f," + matrix.m23
				+ "f * this.scale," + matrix.m30 + "f," + matrix.m31 + "f,"
				+ matrix.m32 + "f," + matrix.m33 + "f * this.scale));");
		System.out
				.println("bodies[BodyPart.BODYPART_RIGHT_FOOT.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();

		bodies[BodyPart.BODYPART_LEFT_FOOT.ordinal()].getMotionState()
				.getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//BODYPART_LEFT_FOOT");
		System.out
				.println("bodies[BodyPart.BODYPART_LEFT_FOOT.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.setIdentity();");
		System.out.println("transform.set(new Matrix4f(" + matrix.m00 + "f,"
				+ matrix.m01 + "f," + matrix.m02 + "f," + matrix.m03 + "f,"
				+ matrix.m10 + "f," + matrix.m11 + "f," + matrix.m12 + "f,"
				+ matrix.m13 + "f * this.scale," + matrix.m20 + "f,"
				+ matrix.m21 + "f," + matrix.m22 + "f," + matrix.m23
				+ "f * this.scale," + matrix.m30 + "f," + matrix.m31 + "f,"
				+ matrix.m32 + "f," + matrix.m33 + "f * this.scale));");
		System.out
				.println("bodies[BodyPart.BODYPART_LEFT_FOOT.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();

		/*
		
		bodies[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()].getMotionState().getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//BODYPART_LEFT_UPPER_ARM");
		System.out.println("bodies[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.setIdentity();");
		System.out.println("transform.set(new Matrix4f("+matrix.m00+"f,"+matrix.m01+"f,"+matrix.m02+"f,"+matrix.m03+"f,"
				+matrix.m10+"f,"+matrix.m11+"f,"+matrix.m12+"f,"+matrix.m13+"f * this.scale,"
				+matrix.m20+"f,"+matrix.m21+"f,"+matrix.m22+"f,"+matrix.m23+"f * this.scale," 
				+matrix.m30+"f,"+matrix.m31+"f,"+matrix.m32+"f,"+matrix.m33+"f * this.scale));");	
		System.out.println("bodies[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();
		
		bodies[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()].getMotionState().getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//BODYPART_LEFT_LOWER_ARM");
		System.out.println("bodies[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.setIdentity();");
		System.out.println("transform.set(new Matrix4f("+matrix.m00+"f,"+matrix.m01+"f,"+matrix.m02+"f,"+matrix.m03+"f,"
				+matrix.m10+"f,"+matrix.m11+"f,"+matrix.m12+"f,"+matrix.m13+"f * this.scale,"
				+matrix.m20+"f,"+matrix.m21+"f,"+matrix.m22+"f,"+matrix.m23+"f * this.scale," 
				+matrix.m30+"f,"+matrix.m31+"f,"+matrix.m32+"f,"+matrix.m33+"f * this.scale));");	
		System.out.println("bodies[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();
		
		
		bodies[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()].getMotionState().getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//BODYPART_RIGHT_UPPER_ARM");
		System.out.println("bodies[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.setIdentity();");
		System.out.println("transform.set(new Matrix4f("+matrix.m00+"f,"+matrix.m01+"f,"+matrix.m02+"f,"+matrix.m03+"f,"
				+matrix.m10+"f,"+matrix.m11+"f,"+matrix.m12+"f,"+matrix.m13+"f * this.scale,"
				+matrix.m20+"f,"+matrix.m21+"f,"+matrix.m22+"f,"+matrix.m23+"f * this.scale," 
				+matrix.m30+"f,"+matrix.m31+"f,"+matrix.m32+"f,"+matrix.m33+"f * this.scale));");	
		System.out.println("bodies[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();
		
		bodies[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()].getMotionState().getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//BODYPART_RIGHT_LOWER_ARM");
		System.out.println("bodies[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.setIdentity();");
		System.out.println("transform.set(new Matrix4f("+matrix.m00+"f,"+matrix.m01+"f,"+matrix.m02+"f,"+matrix.m03+"f,"
				+matrix.m10+"f,"+matrix.m11+"f,"+matrix.m12+"f,"+matrix.m13+"f * this.scale,"
				+matrix.m20+"f,"+matrix.m21+"f,"+matrix.m22+"f,"+matrix.m23+"f * this.scale," 
				+matrix.m30+"f,"+matrix.m31+"f,"+matrix.m32+"f,"+matrix.m33+"f * this.scale));");	
		System.out.println("bodies[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();
		
		
		bodies[BodyPart.BODYPART_RIGHT_HAND.ordinal()].getMotionState().getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//BODYPART_RIGHT_HAND");
		System.out.println("bodies[BodyPart.BODYPART_RIGHT_HAND.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.setIdentity();");
		System.out.println("transform.set(new Matrix4f("+matrix.m00+"f,"+matrix.m01+"f,"+matrix.m02+"f,"+matrix.m03+"f,"
				+matrix.m10+"f,"+matrix.m11+"f,"+matrix.m12+"f,"+matrix.m13+"f * this.scale,"
				+matrix.m20+"f,"+matrix.m21+"f,"+matrix.m22+"f,"+matrix.m23+"f * this.scale," 
				+matrix.m30+"f,"+matrix.m31+"f,"+matrix.m32+"f,"+matrix.m33+"f * this.scale));");	
		System.out.println("bodies[BodyPart.BODYPART_RIGHT_HAND.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();
		
		bodies[BodyPart.BODYPART_LEFT_HAND.ordinal()].getMotionState().getWorldTransform(transform);
		transform.getMatrix(matrix);
		System.out.println("//BODYPART_LEFT_HAND");
		System.out.println("bodies[BodyPart.BODYPART_LEFT_HAND.ordinal()].getMotionState().getWorldTransform(transform);");
		System.out.println("transform.setIdentity();");
		System.out.println("transform.set(new Matrix4f("+matrix.m00+"f,"+matrix.m01+"f,"+matrix.m02+"f,"+matrix.m03+"f,"
				+matrix.m10+"f,"+matrix.m11+"f,"+matrix.m12+"f,"+matrix.m13+"f * this.scale,"
				+matrix.m20+"f,"+matrix.m21+"f,"+matrix.m22+"f,"+matrix.m23+"f * this.scale ," 
				+matrix.m30+"f,"+matrix.m31+"f,"+matrix.m32+"f,"+matrix.m33+"f * this.scale));");	
		System.out.println("bodies[BodyPart.BODYPART_LEFT_HAND.ordinal()].setMotionState(new  DefaultMotionState(transform));");
		System.out.println();
		
		*/
	}

	public void rememberPosition() {

		/*
		
		// Setup some damping on the m_bodies
		for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			if(bodies[i] != null){
				System.out.print("rem");
				motionState[i] = bodies[i].getMotionState();
			}
		}
		
		
		*/

	}

	public void render(float renderScale, PGraphics g) {

		if (!on)
			return;

		if(destScale > scale+0.1f){
			scale(( scale-destScale)/10.0f);
			
		}
		if(destScale < scale-0.1f){
			scale(((scale-destScale)/10.0f));
		}
		

		
		if (GLOBAL.performanceMode)
			g.fill(SETTINGS.ERGODOLL_FILL_COLOUR_PERFORMANCE);
		else
			g.fill(SETTINGS.ERGODOLL_FILL_COLOUR);

		g.pushMatrix();
		g.scale(renderScale);
		g.noStroke();
		Transform myTransform = new Transform();

		if (!GLOBAL.performanceMode)
			g.sphereDetail(SETTINGS.sphere_res);
		//head 

		float ratio = 1;
		
		
		
		//shapes[BodyPart.BODYPART_HEAD.ordinal()].
		//HEAD//_________________________________________________________________________
		ratio = 0.9f;
		g.pushMatrix();
		CapsuleShape capsule = (CapsuleShape) shapes[BodyPart.BODYPART_HEAD
				.ordinal()];
		float radius = capsule.getRadius();
		float halfHeight = capsule.getHalfHeight();
		
		
		if (GLOBAL.performanceMode)
			halfHeight = halfHeight * 1.75f;

		myTransform = bodies[BodyPart.BODYPART_HEAD.ordinal()].getMotionState()
				.getWorldTransform(myTransform);
		//g.fill(0);
		applyMatrix(myTransform, g);

		if (this.scaling) {
			g.pushMatrix();
			g.scale(GLOBAL.jBullet.getScale());
			g.fill(SETTINGS.person_height_text_fill_colour);
			g.text((int) (this.scale * 177f) + " cm", -50, (-radius - 2)
					* (1 / GLOBAL.jBullet.getScale()));
			g.fill(SETTINGS.ERGODOLL_FILL_COLOUR);
			g.popMatrix();

		}
		
		
		/*
		functions.cylinder(radius, radius * ratio, halfHeight * 2,
				SETTINGS.cylinder_res, g);

		g.translate(0, halfHeight, 0);

		if (!GLOBAL.performanceMode)
			g.sphere(radius * ratio); //jaw

		g.translate(0, -halfHeight * 2, 0);

		if (!GLOBAL.performanceMode)
			g.sphere(radius);

		this.renderFace(g);
		
		*/


		functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);
		g.popMatrix();

		//gl.drawCylinder(radius, halfHeight, upAxis);

		//SPINE_______________________________________________________________________
		ratio = 0.80f;
		g.pushMatrix();
		capsule = (CapsuleShape) shapes[BodyPart.BODYPART_SPINE.ordinal()];
		radius = capsule.getRadius();
		halfHeight = capsule.getHalfHeight();
		if (GLOBAL.performanceMode)
			halfHeight = halfHeight * 1.75f;

		myTransform = bodies[BodyPart.BODYPART_SPINE.ordinal()]
				.getMotionState().getWorldTransform(myTransform);
		applyMatrix(myTransform, g);
		
		
		g.pushMatrix();
		functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);
		g.popMatrix();
		//functions.cylinder(radius, radius * ratio, halfHeight * 2,
		//		SETTINGS.cylinder_res, g);
		//functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);

		g.translate(0, halfHeight, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius * ratio);
		g.translate(0, -halfHeight * 2, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius); // top
		g.popMatrix();

		//Pelvis_________________________________________________________________________
		ratio = 1.0f;
		g.pushMatrix();
		capsule = (CapsuleShape) shapes[BodyPart.BODYPART_PELVIS.ordinal()];
		radius = capsule.getRadius();
		halfHeight = capsule.getHalfHeight();
		if (GLOBAL.performanceMode)
			halfHeight = halfHeight * 1.75f;

		myTransform = bodies[BodyPart.BODYPART_PELVIS.ordinal()]
				.getMotionState().getWorldTransform(myTransform);
		applyMatrix(myTransform, g);
		functions.cylinder(radius * ratio, radius, halfHeight * 2,
				SETTINGS.cylinder_res, g);
		functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);

		g.translate(0, halfHeight, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius);
		g.translate(0, -halfHeight * 2, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius * ratio); // top
		g.popMatrix();

		if (hasArms) {

			//RIGHT_UPPER_ARM_________________________________________________________________________
			g.pushMatrix();
			capsule = (CapsuleShape) shapes[BodyPart.BODYPART_RIGHT_UPPER_ARM
					.ordinal()];
			radius = capsule.getRadius();
			halfHeight = capsule.getHalfHeight();
			if (GLOBAL.performanceMode)
				halfHeight = halfHeight * 1.75f;

			myTransform = bodies[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()]
					.getMotionState().getWorldTransform(myTransform);
			applyMatrix(myTransform, g);
			functions.cylinder(radius, radius * .5f, halfHeight * 2,
					SETTINGS.cylinder_res, g);
			functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);

			g.translate(0, halfHeight, 0);
			//if (!GLOBAL.performanceMode)
			//	g.sphere(radius * 0.7f); //jaw
			g.translate(0, -halfHeight * 2, 0);
			//if (!GLOBAL.performanceMode)
			//	g.sphere(radius * 1.1f);
			g.popMatrix();

			//LEFT_UPPER_ARM_________________________________________________________________________
			g.pushMatrix();
			capsule = (CapsuleShape) shapes[BodyPart.BODYPART_LEFT_UPPER_ARM
					.ordinal()];
			radius = capsule.getRadius();
			halfHeight = capsule.getHalfHeight();
			if (GLOBAL.performanceMode)
				halfHeight = halfHeight * 1.75f;

			myTransform = bodies[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()]
					.getMotionState().getWorldTransform(myTransform);
			applyMatrix(myTransform, g);
			functions.cylinder(radius, radius * .5f, halfHeight * 2,
					SETTINGS.cylinder_res, g);
			functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);

			g.translate(0, halfHeight, 0);
			//if (!GLOBAL.performanceMode)
			//	g.sphere(radius * 0.7f); //jaw
			g.translate(0, -halfHeight * 2, 0);
			//if (!GLOBAL.performanceMode)
			//	g.sphere(radius * 1.1f);
			g.popMatrix();

			//RIGHT_LOWER_ARM_________________________________________________________________________
			g.pushMatrix();
			capsule = (CapsuleShape) shapes[BodyPart.BODYPART_RIGHT_LOWER_ARM
					.ordinal()];
			radius = capsule.getRadius();
			halfHeight = capsule.getHalfHeight();
			if (GLOBAL.performanceMode)
				halfHeight = halfHeight * 1.75f;

			myTransform = bodies[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()]
					.getMotionState().getWorldTransform(myTransform);
			applyMatrix(myTransform, g);
			functions.cylinder(radius, radius * .5f, halfHeight * 2,
					SETTINGS.cylinder_res, g);
			functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);

			g.translate(0, halfHeight, 0);
			//if (!GLOBAL.performanceMode)
			//	g.sphere(radius * 0.7f); //jaw
			g.translate(0, -halfHeight * 2, 0);
			//if (!GLOBAL.performanceMode)
			//	g.sphere(radius * 1.1f);
			g.popMatrix();

			//LEFT_LOWER_ARM_________________________________________________________________________
			g.pushMatrix();
			capsule = (CapsuleShape) shapes[BodyPart.BODYPART_LEFT_LOWER_ARM
					.ordinal()];
			radius = capsule.getRadius();
			halfHeight = capsule.getHalfHeight();
			if (GLOBAL.performanceMode)
				halfHeight = halfHeight * 1.75f;

			myTransform = bodies[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()]
					.getMotionState().getWorldTransform(myTransform);
			applyMatrix(myTransform, g);
			functions.cylinder(radius, radius * .5f, halfHeight * 2,
					SETTINGS.cylinder_res, g);
			functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);

			g.translate(0, halfHeight, 0);
			//if (!GLOBAL.performanceMode)
			//	g.sphere(radius * 0.7f); //jaw
			g.translate(0, -halfHeight * 2, 0);
			//if (!GLOBAL.performanceMode)
			//	g.sphere(radius * 1.1f);
			g.popMatrix();

			//LEFT HAND_________________________________________________________________________
			g.pushMatrix();
			capsule = (CapsuleShape) shapes[BodyPart.BODYPART_LEFT_HAND
					.ordinal()];
			radius = capsule.getRadius();
			halfHeight = capsule.getHalfHeight();
			if (GLOBAL.performanceMode)
				halfHeight = halfHeight * 1.75f;

			myTransform = bodies[BodyPart.BODYPART_LEFT_HAND.ordinal()]
					.getMotionState().getWorldTransform(myTransform);
			applyMatrix(myTransform, g);
			functions.cylinder(radius, radius * .5f, halfHeight * 2,
					SETTINGS.cylinder_res, g);
			functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);

			g.translate(0, halfHeight, 0);
			//if (!GLOBAL.performanceMode)
			//	g.sphere(radius * 0.7f); //jaw
			g.translate(0, -halfHeight * 2, 0);
			//if (!GLOBAL.performanceMode)
			//	g.sphere(radius * 1.1f);
			g.popMatrix();

			//RIGHT HAND_________________________________________________________________________
			g.pushMatrix();
			capsule = (CapsuleShape) shapes[BodyPart.BODYPART_RIGHT_HAND
					.ordinal()];
			radius = capsule.getRadius();
			halfHeight = capsule.getHalfHeight();
			if (GLOBAL.performanceMode)
				halfHeight = halfHeight * 1.75f;

			myTransform = bodies[BodyPart.BODYPART_RIGHT_HAND.ordinal()]
					.getMotionState().getWorldTransform(myTransform);
			applyMatrix(myTransform, g);
			functions.cylinder(radius, radius * .5f, halfHeight * 2,
					SETTINGS.cylinder_res, g);
			functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);

			g.translate(0, halfHeight, 0);
			//if (!GLOBAL.performanceMode)
			//	g.sphere(radius * 0.7f); //jaw
			g.translate(0, -halfHeight * 2, 0);
			//if (!GLOBAL.performanceMode)
			//	g.sphere(radius * 1.1f);
			g.popMatrix();

		}

		//LEFT Upper Leg_________________________________________________________________________
		//ratio = .7f;
		ratio = 0.9f;

		g.pushMatrix();
		capsule = (CapsuleShape) shapes[BodyPart.BODYPART_LEFT_UPPER_LEG
				.ordinal()];
		radius = capsule.getRadius();
		halfHeight = capsule.getHalfHeight();
		
		if (GLOBAL.performanceMode)
			halfHeight = halfHeight * 1.75f;

		myTransform = bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()]
				.getMotionState().getWorldTransform(myTransform);
		applyMatrix(myTransform, g);

		functions.cylinder(radius, radius * ratio, halfHeight * 2,
				SETTINGS.cylinder_res, g);
		functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);

		g.translate(0, halfHeight, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius * 0.7f); //jaw
		g.translate(0, -halfHeight * 2, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius);
		g.popMatrix();

		
		
		//Right Upper Leg_________________________________________________________________________
		ratio = 0.9f;

		g.pushMatrix();
		capsule = (CapsuleShape) shapes[BodyPart.BODYPART_RIGHT_UPPER_LEG
				.ordinal()];
		radius = capsule.getRadius();
		halfHeight = capsule.getHalfHeight();
		if (GLOBAL.performanceMode)
			halfHeight = halfHeight * 1.75f;

		myTransform = bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()]
				.getMotionState().getWorldTransform(myTransform);
		applyMatrix(myTransform, g);
		functions.cylinder(radius, radius * ratio, halfHeight * 2,
				SETTINGS.cylinder_res, g);
		functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);

		g.translate(0, halfHeight, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius * 0.7f); //jaw
		g.translate(0, -halfHeight * 2, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius);
		g.popMatrix();

		//LOWER LEFT LEG_________________________________________________________________________
		ratio = 0.9f;
		g.pushMatrix();
		capsule = (CapsuleShape) shapes[BodyPart.BODYPART_LEFT_LOWER_LEG
				.ordinal()];
		radius = capsule.getRadius();
		halfHeight = capsule.getHalfHeight();
		if (GLOBAL.performanceMode)
			halfHeight = halfHeight * 1.75f;

		myTransform = bodies[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()]
				.getMotionState().getWorldTransform(myTransform);
		applyMatrix(myTransform, g);
		functions.cylinder(radius, radius * .5f, halfHeight * 2,
				SETTINGS.cylinder_res, g);
		functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);

		g.translate(0, halfHeight, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius * 0.7f); //jaw

		g.translate(0, -halfHeight * 2, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius * 1.1f);
		g.popMatrix();

		//LOWER RIGHT LEG_________________________________________________________________________
		ratio = 0.9f;

		g.pushMatrix();
		capsule = (CapsuleShape) shapes[BodyPart.BODYPART_RIGHT_LOWER_LEG
				.ordinal()];
		radius = capsule.getRadius();
		halfHeight = capsule.getHalfHeight();
		if (GLOBAL.performanceMode)
			halfHeight = halfHeight * 1.75f;

		myTransform = bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()]
				.getMotionState().getWorldTransform(myTransform);
		applyMatrix(myTransform, g);
		functions.cylinder(radius, radius * .5f, halfHeight * 2,
				SETTINGS.cylinder_res, g);
		functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);

		g.translate(0, halfHeight, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius * 0.7f); //jaw
		g.translate(0, -halfHeight * 2, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius * 1.1f);
		g.popMatrix();

		// RIGHT Foot_________________________________________________________________________
		g.pushMatrix();
		capsule = (CapsuleShape) shapes[BodyPart.BODYPART_RIGHT_FOOT.ordinal()];
		radius = capsule.getRadius();
		halfHeight = capsule.getHalfHeight();
		if (GLOBAL.performanceMode)
			halfHeight = halfHeight * 1.75f;

		myTransform = bodies[BodyPart.BODYPART_RIGHT_FOOT.ordinal()]
				.getMotionState().getWorldTransform(myTransform);
		applyMatrix(myTransform, g);
		functions.cylinder(radius, radius * .5f, halfHeight * 2,
				SETTINGS.cylinder_res, g);
		functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);

		g.translate(0, halfHeight, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius * 0.7f); //jaw
		g.translate(0, -halfHeight * 2, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius * 1.1f);
		g.popMatrix();

		//LEFT Foot_________________________________________________________________________
		g.pushMatrix();
		capsule = (CapsuleShape) shapes[BodyPart.BODYPART_LEFT_FOOT.ordinal()];
		radius = capsule.getRadius();
		halfHeight = capsule.getHalfHeight();
		if (GLOBAL.performanceMode)
			halfHeight = halfHeight * 1.75f;

		myTransform = bodies[BodyPart.BODYPART_LEFT_FOOT.ordinal()]
				.getMotionState().getWorldTransform(myTransform);
		applyMatrix(myTransform, g);
		functions.cylinder(radius, radius * .5f, halfHeight * 2,
				SETTINGS.cylinder_res, g);
		functions.flatCylinder(radius, radius * ratio, halfHeight * 2, myTransform,g);

		g.translate(0, halfHeight, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius * 0.7f); //jaw
		g.translate(0, -halfHeight * 2, 0);
		//if (!GLOBAL.performanceMode)
		//	g.sphere(radius * 1.1f);
		g.popMatrix();

		g.popMatrix();

		scaling = false;
	}

	private void renderFace(PGraphics g) {
		g.rotateY((float) (Math.PI / 2));
		g.translate(0, 0, 3);
		g.scale(.003f * this.getScale());
		//g.image(FaceImages[faceExpressions.HAPPY.ordinal()], -45, -25);	
	}

	public void resetPhysics() {
		
		if(!this.on)
			return;
		
		// TODO Auto-generated method stub
		this.dragged = false;

		//this.scale = 10;
		destroy();
		makeBody(this.ownerWorld, this.startPos, this.scale);
		//GLOBAL.person.translate(-40, 0, 0);
		this.translate(this.startPos.x, this.startPos.y, this.startPos.z);
		this.solveOverlap();
		/*	
			
			// Setup some damping on the m_bodies
			for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
				if(bodies[i] != null){
					System.out.print("set");
					bodies[i].setMotionState(motionState[i]);
				}
			}
			GLOBAL.jBullet.update();
			*/

	}

	public void scale(float scaleIn) {


		if(scaleIn > 1 || scaleIn < -1)
			return;

		if (this.scale < 0.1f && scaleIn > 0)
			return;

		float newScale = scale - scaleIn;
	
		if(newScale < minScale || newScale > maxScale)
			return;
		
		
		
		this.scale = newScale;


		scaling = true;

		// Setup some damping on the m_bodies
		for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			if (bodies[i] != null) {
				motionState[i] = bodies[i].getMotionState();
			}
		}

		destroy();
		makeBody(this.ownerWorld, this.startPos, this.scale);

		for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			if (motionState[i] != null) {
				bodies[i].setMotionState(motionState[i]);
				bodies[i].clearForces();
			}
		}

	}

	//place the ergoDoll so that it is in front of the chair for sitting. 
	public void seat(SketchChair sketchChair, float x, float y, float z) {
	
		//if(GLOBAL.sketchChairs.getCurChair() != null && GLOBAL.sketchChairs.getCurChair().rigidBody != null)
	  //  GLOBAL.jBullet.myWorld.removeRigidBody(GLOBAL.sketchChairs.getCurChair().rigidBody);
		//GLOBAL.sketchChairs.getCurChair().rigidBody.clearForces();

	
		
		if (sketchChair.rigidBody == null || !this.on)
			return;
		
		LOGGER.info("Number of Manifolds: "+ GLOBAL.jBullet.myWorld.getCollisionWorld().getDispatcher().getNumManifolds());

		//GLOBAL.jBullet.myWorld.getBroadphase()
		for(int m = 0; m < GLOBAL.jBullet.myWorld.getDispatcher().getNumManifolds(); m++){
			PersistentManifold manifold = GLOBAL.jBullet.myWorld.getDispatcher().getManifoldByIndexInternal(m);
		//	GLOBAL.jBullet.myWorld.getDispatcher().releaseManifold(manifold);
			
			//LOGGER.info("removing " + manifold.index1a);
			//manifold.clearManifold();
			//GLOBAL.jBullet.myWorld.getDispatcher().releaseManifold(arg0)
		}
		GLOBAL.jBullet.myWorld.getDispatcher().dispatchAllCollisionPairs(GLOBAL.jBullet.myWorld.getPairCache(), GLOBAL.jBullet.myWorld.getDispatchInfo(), GLOBAL.jBullet.myWorld.getDispatcher());
		//PhysWorld->getDispatcher()->dispatchAllCollisionPairs(Ghost->getOverlappingPairCache(), PhysWorld->getDispatchInfo(), PhysWorld->getDispatcher());
		
		 GLOBAL.jBullet.update();
		LOGGER.info("Number of Manifolds after: "+ GLOBAL.jBullet.myWorld.getCollisionWorld().getDispatcher().getNumManifolds());

		this.noCollisions();
		//sketchChair.rigidBody
			//	.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);

		x = GLOBAL.jBullet.scaleVal(x);
		y = GLOBAL.jBullet.scaleVal(y);
		z = GLOBAL.jBullet.scaleVal(z);
		destroy();
		
		

		makeBody(this.ownerWorld, this.startPos, this.scale);
		float footWidth = limbLengths[BodyPart.BODYPART_RIGHT_FOOT.ordinal()];
		Vector3f out = new Vector3f();
		joints[JointType.JOINT_RIGHT_ANKLE.ordinal()].getRigidBodyB()
				.getCenterOfMassPosition(out);

		this.translate((x - out.x) + (footWidth * 2), y - out.y, z - out.z);

		solveOverlap();
		//this.collisions();
		//sketchChair.rigidBody.setCollisionFlags(CollisionFlags.KINEMATIC_OBJECT);
		//sketchChair.rigidBody.setActivationState(1);
		//sketchChair.updateCollisionShape();
		this.buildLimbConstraints();
		//sketchChair.removeRigidModel();
		//sketchChair.updateCollisionShape();
		//GLOBAL.jBullet.myWorld.removeRigidBody(sketchChair.rigidBody);
		//	GLOBAL.jBullet.myWorld.addRigidBody(sketchChair.rigidBody);
		//GLOBAL.jBullet.rigidBodies.add(sketchChair.rigidBody);
		//sketchChair.rigidBody
		//.setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);

		this.freeze();
	}

	void show() {
		this.collisions();
		this.resetPhysics();

	}

	void solveOverlap() {
		int offset = 0;

		if (GLOBAL.sketchChairs.getCurChair() == null)
			return;

		while (overlapsWith(GLOBAL.sketchChairs.getCurChair().rigidBody)
				&& offset < 10000) {
			this.translate(0, -1f, 0);
			//this.translate(((x - out.x)+(footWidth*2) )+ offset , y - out.y, z -out.z );
			//LOGGER.info("OVERLAP "+offset);
			offset++;
			//GLOBAL.jBullet.myWorld.stepSimulation(10);
			//..GLOBAL.jBullet.step();
		}
	}

	public void toggleON() {
		this.on = !this.on;

		if (this.on)
			this.show();
		else
			this.hide();

	}

	void translate(float x, float y, float z) {

		Vector3f offset3f = new Vector3f(x, y, z);

		// Setup some damping on the m_bodies
		for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			if (bodies[i] != null) {
				bodies[i].translate(offset3f);
				bodies[i].clearForces();
				bodies[i].setAngularVelocity(new Vector3f(0, 0, 0));
				bodies[i].setLinearVelocity(new Vector3f(0, 0, 0));

			}
		}

		GLOBAL.jBullet.update();

	}

	public void bigger() {
if(destScale < maxScale)
	destScale+=0.1;
	}

	public void smaller() {
		
		if(destScale > minScale)
			destScale-=0.1;
		
	}

}
