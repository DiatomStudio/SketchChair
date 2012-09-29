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

import cc.sketchchair.functions.functions;

import com.bulletphysics.BulletGlobals;
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

import javax.vecmath.Vector3f;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Backup of the main figure class.
 * 
 */

public class dollBackup {

	//protected final BulletStack stack = BulletStack.get();

	public enum BodyPart {
		BODYPART_PELVIS, BODYPART_SPINE, BODYPART_HEAD,

		BODYPART_LEFT_UPPER_LEG, BODYPART_LEFT_LOWER_LEG,

		BODYPART_RIGHT_UPPER_LEG, BODYPART_RIGHT_LOWER_LEG,

		BODYPART_LEFT_UPPER_ARM, BODYPART_LEFT_LOWER_ARM,

		BODYPART_RIGHT_UPPER_ARM, BODYPART_RIGHT_LOWER_ARM,

		BODYPART_COUNT;
	}

	// Expressions
	public enum faceExpressions {
		HAPPY, SAD, SCARED, EXPRESSION_COUNT
	}

	public enum JointType {
		JOINT_PELVIS_SPINE, JOINT_SPINE_HEAD,

		JOINT_LEFT_HIP, JOINT_LEFT_KNEE,

		JOINT_RIGHT_HIP, JOINT_RIGHT_KNEE, JOINT_PLANE, JOINT_LEFT_SHOULDER, JOINT_LEFT_ELBOW,

		JOINT_RIGHT_SHOULDER, JOINT_RIGHT_ELBOW,

		JOINT_COUNT
	}

	boolean hasArms = true;

	private PImage FaceImages[] = new PImage[faceExpressions.EXPRESSION_COUNT
			.ordinal()];

	private CollisionShape[] shapes = new CollisionShape[BodyPart.BODYPART_COUNT
			.ordinal()];
	private RigidBody[] bodies = new RigidBody[BodyPart.BODYPART_COUNT
			.ordinal()];
	private MotionState[] motionState = new MotionState[BodyPart.BODYPART_COUNT
			.ordinal()];
	private TypedConstraint[] joints = new TypedConstraint[JointType.JOINT_COUNT
			.ordinal()];
	private boolean scaling = false;
	float scale = 1;
	private DynamicsWorld ownerWorld;
	private Vector3f startPos;

	private float buildScale;

	public dollBackup(DynamicsWorld ownerWorld, Vector3f positionOffset) {
		this(ownerWorld, positionOffset, 1.0f);
	}

	public dollBackup(DynamicsWorld ownerWorld, Vector3f positionOffset,
			float scale_ragdoll) {
		this.ownerWorld = ownerWorld;
		this.startPos = positionOffset;

		this.scale = scale_ragdoll;

		makeBody(this.ownerWorld, positionOffset, this.scale);

		FaceImages[faceExpressions.HAPPY.ordinal()] = GLOBAL.applet
				.loadImage("faceExpressionsHappy.png");

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

		///////////////////////////// SETTING THE CONSTRAINTS /////////////////////////////////////////////7777
		// Now setup the constraints
		Generic6DofConstraint joint6DOF;
		Transform localA = new Transform(), localB = new Transform();
		boolean useLinearReferenceFrameA = true;
		/// ******* SPINE HEAD ******** ///
		{
			localA.setIdentity();
			localB.setIdentity();

			localA.origin.set(0f, -0.30f * this.buildScale, 0f);

			localB.origin.set(0f, 0.14f * this.buildScale, 0f);

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
			joints[JointType.JOINT_SPINE_HEAD.ordinal()] = joint6DOF;
			ownerWorld.addConstraint(
					joints[JointType.JOINT_SPINE_HEAD.ordinal()], true);
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
		}
		/// *************************** ///

		if (hasArms) {
			/// ******* LEFT SHOULDER ******** ///
			{
				localA.setIdentity();
				localB.setIdentity();

				localA.origin.set(0f, 0.15f * -this.buildScale,
						0.2f * this.buildScale);

				MatrixUtil.setEulerZYX(localB.basis,
						BulletGlobals.SIMD_HALF_PI, 0,
						-BulletGlobals.SIMD_HALF_PI);
				localB.origin.set(0f, 0.18f * this.buildScale, 0f);

				joint6DOF = new Generic6DofConstraint(
						bodies[BodyPart.BODYPART_SPINE.ordinal()],
						bodies[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()],
						localA, localB, useLinearReferenceFrameA);

				//#ifdef RIGID
				//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
				//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
				//#else
				tmp.set(-BulletGlobals.SIMD_PI * 0.8f,
						-BulletGlobals.FLT_EPSILON,
						-BulletGlobals.SIMD_PI * 0.5f);
				joint6DOF.setAngularLowerLimit(tmp);
				tmp.set(BulletGlobals.SIMD_PI * 0.8f,
						BulletGlobals.FLT_EPSILON, BulletGlobals.SIMD_PI * 0.5f);
				joint6DOF.setAngularUpperLimit(tmp);
				//#endif
				joints[JointType.JOINT_LEFT_SHOULDER.ordinal()] = joint6DOF;
				ownerWorld.addConstraint(
						joints[JointType.JOINT_LEFT_SHOULDER.ordinal()], true);
			}
			/// *************************** ///

			/// ******* RIGHT SHOULDER ******** ///
			{
				localA.setIdentity();
				localB.setIdentity();

				localA.origin.set(0f, 0.15f * -this.buildScale, -0.2f
						* this.buildScale);
				MatrixUtil.setEulerZYX(localB.basis, 0, 0,
						BulletGlobals.SIMD_HALF_PI);
				localB.origin.set(0f, 0.18f * this.buildScale, 0f);
				joint6DOF = new Generic6DofConstraint(
						bodies[BodyPart.BODYPART_SPINE.ordinal()],
						bodies[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()],
						localA, localB, useLinearReferenceFrameA);

				//#ifdef RIGID
				//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
				//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
				//#else
				tmp.set(-BulletGlobals.SIMD_PI * 0.8f,
						-BulletGlobals.SIMD_EPSILON,
						-BulletGlobals.SIMD_PI * 0.5f);
				joint6DOF.setAngularLowerLimit(tmp);
				tmp.set(BulletGlobals.SIMD_PI * 0.8f,
						BulletGlobals.SIMD_EPSILON,
						BulletGlobals.SIMD_PI * 0.5f);
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

				localA.origin.set(0f, -0.18f * this.buildScale, 0f);
				localB.origin.set(0f, -0.14f * this.buildScale, 0f);
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
						-BulletGlobals.SIMD_EPSILON);
				joint6DOF.setAngularLowerLimit(tmp);
				tmp.set(BulletGlobals.SIMD_PI * 0.7f,
						BulletGlobals.SIMD_EPSILON, BulletGlobals.SIMD_EPSILON);
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

				localA.origin.set(0f, -0.18f * this.buildScale, 0f);
				localB.origin.set(0f, -0.14f * this.buildScale, 0f);
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
						-BulletGlobals.SIMD_EPSILON);
				joint6DOF.setAngularLowerLimit(tmp);
				tmp.set(BulletGlobals.SIMD_PI * 0.7f,
						BulletGlobals.SIMD_EPSILON, BulletGlobals.SIMD_EPSILON);
				joint6DOF.setAngularUpperLimit(tmp);
				//#endif

				joints[JointType.JOINT_RIGHT_ELBOW.ordinal()] = joint6DOF;
				ownerWorld.addConstraint(
						joints[JointType.JOINT_RIGHT_ELBOW.ordinal()], true);
			}
			/// *************************** ///
		}

		/// ******* PELVIS ******** ///
		{
			localA.setIdentity();
			localB.setIdentity();

			MatrixUtil.setEulerZYX(localA.basis, 0, BulletGlobals.SIMD_HALF_PI,
					0);
			localA.origin.set(0f, -0.15f * this.buildScale, 0f);
			MatrixUtil.setEulerZYX(localB.basis, 0, BulletGlobals.SIMD_HALF_PI,
					0);
			localB.origin.set(0f, 0.15f * this.buildScale, 0f);
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

		/// ******* LEFT HIP ******** ///
		{
			localA.setIdentity();
			localB.setIdentity();

			localA.origin.set(0f, 0.10f * this.buildScale,
					0.08f * this.buildScale);

			localB.origin.set(0f, -0.225f * this.buildScale, 0f);

			joint6DOF = new Generic6DofConstraint(
					bodies[BodyPart.BODYPART_PELVIS.ordinal()],
					bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()], localA,
					localB, useLinearReferenceFrameA);

			//#ifdef RIGID
			//joint6DOF->setAngularLowerLimit(btVector3(-SIMD_EPSILON,-SIMD_EPSILON,-SIMD_EPSILON));
			//joint6DOF->setAngularUpperLimit(btVector3(SIMD_EPSILON,SIMD_EPSILON,SIMD_EPSILON));
			//#else
			tmp.set(-BulletGlobals.SIMD_EPSILON, -BulletGlobals.SIMD_EPSILON,
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

			localA.origin.set(0f, 0.10f * this.buildScale, -0.08f
					* this.buildScale);
			localB.origin.set(0f, -0.225f * this.buildScale, 0f);

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

			localA.origin.set(0f, 0.265f * this.buildScale, 0f);
			localB.origin.set(0f, -0.225f * this.buildScale, 0f);
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
			localA.origin.set(0f, 0.265f * this.buildScale, 0f);
			localB.origin.set(0f, -0.225f * this.buildScale, 0f);
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

	}

	public void destroy() {
		int i;

		// Remove all constraints
		for (i = 0; i < JointType.JOINT_COUNT.ordinal(); ++i) {
			if (joints[i] != null)
				ownerWorld.removeConstraint(joints[i]);
			//joints[i].destroy();
			joints[i] = null;
		}

		// Remove all bodies and shapes
		for (i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			ownerWorld.removeRigidBody(bodies[i]);

			//bodies[i].getMotionState().destroy();

			bodies[i].destroy();
			bodies[i] = null;

			//shapes[i].destroy();
			shapes[i] = null;
		}
	}

	public void dragScale(float mouseX, float mouseY) {
		this.freeze();
		RigidBody body = GLOBAL.jBullet.getOver(mouseX, mouseY);

		if (body == bodies[BodyPart.BODYPART_PELVIS.ordinal()]
				|| body == bodies[BodyPart.BODYPART_SPINE.ordinal()]
				|| body == bodies[BodyPart.BODYPART_HEAD.ordinal()]
				|| body == bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()]
				|| body == bodies[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()]
				|| body == bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()]
				|| body == bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()]

		) {
			GLOBAL.person
					.scale((GLOBAL.uiTools.mouseY - GLOBAL.uiTools.pmouseY)
							+ (GLOBAL.uiTools.mouseX - GLOBAL.uiTools.pmouseX));
			GLOBAL.jBullet.update();

			System.out.print("freeze");
		}
	}

	public void freeze() {

		// Setup some damping on the m_bodies
		for (int i = 0; i < JointType.JOINT_COUNT.ordinal() - 1; ++i) {
			Generic6DofConstraint joint = (Generic6DofConstraint) joints[i];
			joint.setLimit(3, joint.getAngle(0), joint.getAngle(0));
			joint.setLimit(4, joint.getAngle(1), joint.getAngle(1));
			joint.setLimit(5, joint.getAngle(2), joint.getAngle(2));
			//joint.setLimit(3, joint.getAngle(3), joint.getAngle(3));
			//joint.setLimit(4, joint.getAngle(4), joint.getAngle(4));
			//joint.setLimit(5, joint.getAngle(5), joint.getAngle(5));
		}

	}

	public float getScale() {
		return this.scale;
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
		shapes[BodyPart.BODYPART_PELVIS.ordinal()] = new CapsuleShape(
				scale_ragdoll * 0.11f, scale_ragdoll * 0.20f);
		shapes[BodyPart.BODYPART_SPINE.ordinal()] = new CapsuleShape(
				scale_ragdoll * 0.13f, scale_ragdoll * 0.28f);
		shapes[BodyPart.BODYPART_HEAD.ordinal()] = new CapsuleShape(
				scale_ragdoll * 0.10f, scale_ragdoll * 0.1f);
		shapes[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()] = new CapsuleShape(
				scale_ragdoll * 0.08f, scale_ragdoll * 0.45f);
		shapes[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()] = new CapsuleShape(
				scale_ragdoll * 0.06f, scale_ragdoll * 0.45f);
		shapes[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()] = new CapsuleShape(
				scale_ragdoll * 0.08f, scale_ragdoll * 0.45f);
		shapes[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()] = new CapsuleShape(
				scale_ragdoll * 0.06f, scale_ragdoll * 0.45f);
		if (hasArms) {
			shapes[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()] = new CapsuleShape(
					scale_ragdoll * 0.05f, scale_ragdoll * 0.33f);
			shapes[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()] = new CapsuleShape(
					scale_ragdoll * 0.04f, scale_ragdoll * 0.25f);
			shapes[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()] = new CapsuleShape(
					scale_ragdoll * 0.05f, scale_ragdoll * 0.33f);
			shapes[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()] = new CapsuleShape(
					scale_ragdoll * 0.04f, scale_ragdoll * 0.25f);
		}
		// Setup all the rigid bodies
		Transform offset = new Transform();
		offset.setIdentity();
		offset.origin.set(positionOffset);

		Transform transform = new Transform();
		transform.setIdentity();
		transform.origin.set(0f, scale_ragdoll * -1f, 0f);
		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_PELVIS.ordinal()] = localCreateRigidBody(3f,
				tmpTrans, shapes[BodyPart.BODYPART_PELVIS.ordinal()]);

		transform.setIdentity();
		transform.origin.set(0f, scale_ragdoll * -1.2f, 0f);
		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_SPINE.ordinal()] = localCreateRigidBody(.1f,
				tmpTrans, shapes[BodyPart.BODYPART_SPINE.ordinal()]);

		transform.setIdentity();
		transform.origin.set(0f, scale_ragdoll * -1.6f, 0f);
		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_HEAD.ordinal()] = localCreateRigidBody(.1f,
				tmpTrans, shapes[BodyPart.BODYPART_HEAD.ordinal()]);

		transform.setIdentity();
		transform.origin.set(0f, -0.7f * scale_ragdoll, 0.08f * scale_ragdoll);
		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()] = localCreateRigidBody(
				.7f, tmpTrans,
				shapes[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()]);

		transform.setIdentity();
		transform.origin.set(0f, -0.2f * scale_ragdoll, 0.06f * scale_ragdoll);
		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()] = localCreateRigidBody(
				1.0f, tmpTrans,
				shapes[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()]);

		transform.setIdentity();
		transform.origin.set(0f, -0.7f * scale_ragdoll, -0.08f * scale_ragdoll);
		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()] = localCreateRigidBody(
				.7f, tmpTrans,
				shapes[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()]);

		transform.setIdentity();
		transform.origin.set(0f, -0.2f * scale_ragdoll, -0.08f * scale_ragdoll);
		tmpTrans.mul(offset, transform);
		bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()] = localCreateRigidBody(
				1.0f, tmpTrans,
				shapes[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()]);

		if (hasArms) {
			transform.setIdentity();
			transform.origin.set(0f, -1.75f * scale_ragdoll,
					0.35f * scale_ragdoll);
			MatrixUtil.setEulerZYX(transform.basis, BulletGlobals.SIMD_HALF_PI,
					0, 0);
			tmpTrans.mul(offset, transform);
			bodies[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()] = localCreateRigidBody(
					1f, tmpTrans,
					shapes[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()]);

			transform.setIdentity();
			transform.origin.set(0f, -1.75f * scale_ragdoll,
					0.7f * scale_ragdoll);
			MatrixUtil.setEulerZYX(transform.basis, BulletGlobals.SIMD_HALF_PI,
					0, 0);
			tmpTrans.mul(offset, transform);
			bodies[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()] = localCreateRigidBody(
					1f, tmpTrans,
					shapes[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()]);

			transform.setIdentity();
			transform.origin.set(0f, -1.75f * scale_ragdoll, -0.35f
					* scale_ragdoll);
			MatrixUtil.setEulerZYX(transform.basis,
					-BulletGlobals.SIMD_HALF_PI, 0, 0);
			tmpTrans.mul(offset, transform);
			bodies[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()] = localCreateRigidBody(
					1f, tmpTrans,
					shapes[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()]);

			transform.setIdentity();
			transform.origin.set(0f, -1.75f * scale_ragdoll, -0.7f
					* scale_ragdoll);
			MatrixUtil.setEulerZYX(transform.basis,
					-BulletGlobals.SIMD_HALF_PI, 0, 0);
			tmpTrans.mul(offset, transform);
			bodies[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()] = localCreateRigidBody(
					1f, tmpTrans,
					shapes[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()]);
		}

		// Setup some damping on the m_bodies
		for (int i = 0; i < BodyPart.BODYPART_COUNT.ordinal(); ++i) {
			bodies[i].setDamping(0.05f, 9.95f);
			bodies[i].setDeactivationTime(0.8f);
			bodies[i].setSleepingThresholds(1.6f, 2.5f);
			bodies[i].setFriction(SETTINGS.person_friction);
		}
		//this.buildLimbConstraints();
	}

	public void rememberPosition() {
		motionState[BodyPart.BODYPART_PELVIS.ordinal()] = bodies[BodyPart.BODYPART_PELVIS
				.ordinal()].getMotionState();
		motionState[BodyPart.BODYPART_SPINE.ordinal()] = bodies[BodyPart.BODYPART_SPINE
				.ordinal()].getMotionState();
		motionState[BodyPart.BODYPART_HEAD.ordinal()] = bodies[BodyPart.BODYPART_HEAD
				.ordinal()].getMotionState();
		motionState[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()] = bodies[BodyPart.BODYPART_LEFT_UPPER_LEG
				.ordinal()].getMotionState();
		motionState[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()] = bodies[BodyPart.BODYPART_LEFT_LOWER_LEG
				.ordinal()].getMotionState();
		motionState[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()] = bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG
				.ordinal()].getMotionState();
		motionState[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()] = bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG
				.ordinal()].getMotionState();

	}

	public void render(float renderScale, PGraphics g) {
		g.pushMatrix();
		g.scale(renderScale);
		g.noStroke();
		g.fill(SETTINGS.ERGODOLL_FILL_COLOUR);
		Transform myTransform = new Transform();

		g.sphereDetail(SETTINGS.sphere_res);
		//head 

		float ratio = .9f;
		//shapes[BodyPart.BODYPART_HEAD.ordinal()].
		//HEAD//
		g.pushMatrix();
		CapsuleShape capsule = (CapsuleShape) shapes[BodyPart.BODYPART_HEAD
				.ordinal()];
		float radius = capsule.getRadius();
		float halfHeight = capsule.getHalfHeight();
		myTransform = bodies[BodyPart.BODYPART_HEAD.ordinal()].getMotionState()
				.getWorldTransform(myTransform);
		//g.fill(0);
		applyMatrix(myTransform, g);

		if (this.scaling) {
			g.pushMatrix();
			g.scale(GLOBAL.jBullet.getScale());
			g.fill(SETTINGS.person_height_text_fill_colour);
			g.text((int) (this.scale * 6.9f) + " cm", -50, (-radius - 2)
					* (1 / GLOBAL.jBullet.getScale()));
			g.fill(SETTINGS.ERGODOLL_FILL_COLOUR);
			g.popMatrix();

		}

		functions.cylinder(radius, radius * ratio, halfHeight * 2,
				SETTINGS.cylinder_res, g);

		g.translate(0, halfHeight, 0);
		g.sphere(radius * ratio); //jaw
		g.translate(0, -halfHeight * 2, 0);
		g.sphere(radius);

		this.renderFace(g);
		g.popMatrix();

		//gl.drawCylinder(radius, halfHeight, upAxis);

		ratio = .8f;
		//SPINE
		g.pushMatrix();
		capsule = (CapsuleShape) shapes[BodyPart.BODYPART_SPINE.ordinal()];
		radius = capsule.getRadius();
		halfHeight = capsule.getHalfHeight();
		myTransform = bodies[BodyPart.BODYPART_SPINE.ordinal()]
				.getMotionState().getWorldTransform(myTransform);
		applyMatrix(myTransform, g);
		functions.cylinder(radius, radius * ratio, halfHeight * 2,
				SETTINGS.cylinder_res, g);
		g.translate(0, halfHeight, 0);
		g.sphere(radius * ratio);
		g.translate(0, -halfHeight * 2, 0);
		g.sphere(radius); // top
		g.popMatrix();

		//Pelvis
		ratio = .7f;
		g.pushMatrix();
		capsule = (CapsuleShape) shapes[BodyPart.BODYPART_PELVIS.ordinal()];
		radius = capsule.getRadius();
		halfHeight = capsule.getHalfHeight();
		myTransform = bodies[BodyPart.BODYPART_PELVIS.ordinal()]
				.getMotionState().getWorldTransform(myTransform);
		applyMatrix(myTransform, g);
		functions.cylinder(radius * ratio, radius, halfHeight * 2,
				SETTINGS.cylinder_res, g);
		g.translate(0, halfHeight, 0);
		g.sphere(radius);
		g.translate(0, -halfHeight * 2, 0);
		g.sphere(radius * ratio); // top
		g.popMatrix();

		//leg
		ratio = .7f;

		g.pushMatrix();
		capsule = (CapsuleShape) shapes[BodyPart.BODYPART_LEFT_UPPER_LEG
				.ordinal()];
		radius = capsule.getRadius();
		halfHeight = capsule.getHalfHeight();
		myTransform = bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()]
				.getMotionState().getWorldTransform(myTransform);
		applyMatrix(myTransform, g);
		functions.cylinder(radius, radius * ratio, halfHeight * 2,
				SETTINGS.cylinder_res, g);
		g.translate(0, -halfHeight, 0);
		g.sphere(radius);
		g.popMatrix();

		g.pushMatrix();
		capsule = (CapsuleShape) shapes[BodyPart.BODYPART_RIGHT_UPPER_LEG
				.ordinal()];
		radius = capsule.getRadius();
		halfHeight = capsule.getHalfHeight();
		myTransform = bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()]
				.getMotionState().getWorldTransform(myTransform);
		applyMatrix(myTransform, g);
		functions.cylinder(radius, radius * ratio, halfHeight * 2,
				SETTINGS.cylinder_res, g);
		g.translate(0, -halfHeight, 0);
		g.sphere(radius);
		g.popMatrix();

		//LOWER LEFT LEG
		g.pushMatrix();
		capsule = (CapsuleShape) shapes[BodyPart.BODYPART_LEFT_LOWER_LEG
				.ordinal()];
		radius = capsule.getRadius();
		halfHeight = capsule.getHalfHeight();
		myTransform = bodies[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()]
				.getMotionState().getWorldTransform(myTransform);
		applyMatrix(myTransform, g);
		functions.cylinder(radius, radius * .5f, halfHeight * 2,
				SETTINGS.cylinder_res, g);
		g.translate(0, halfHeight, 0);
		g.sphere(radius * 0.7f); //jaw

		g.translate(0, -halfHeight * 2, 0);
		g.sphere(radius * 1.1f);
		g.popMatrix();

		//LOWER RIGHT LEG
		g.pushMatrix();
		capsule = (CapsuleShape) shapes[BodyPart.BODYPART_RIGHT_LOWER_LEG
				.ordinal()];
		radius = capsule.getRadius();
		halfHeight = capsule.getHalfHeight();
		myTransform = bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()]
				.getMotionState().getWorldTransform(myTransform);
		applyMatrix(myTransform, g);
		functions.cylinder(radius, radius * .5f, halfHeight * 2,
				SETTINGS.cylinder_res, g);
		g.translate(0, halfHeight, 0);
		g.sphere(radius * 0.7f); //jaw
		g.translate(0, -halfHeight * 2, 0);
		g.sphere(radius * 1.1f);
		g.popMatrix();

		if (this.hasArms) {

			//LOWER RIGHT LEG
			g.pushMatrix();
			capsule = (CapsuleShape) shapes[BodyPart.BODYPART_RIGHT_UPPER_ARM
					.ordinal()];
			radius = capsule.getRadius();
			halfHeight = capsule.getHalfHeight();
			myTransform = bodies[BodyPart.BODYPART_RIGHT_UPPER_ARM.ordinal()]
					.getMotionState().getWorldTransform(myTransform);
			applyMatrix(myTransform, g);
			functions.cylinder(radius, radius * .5f, halfHeight * 2,
					SETTINGS.cylinder_res, g);
			g.translate(0, halfHeight, 0);
			g.sphere(radius * 0.7f); //jaw
			g.translate(0, -halfHeight * 2, 0);
			g.sphere(radius * 1.1f);
			g.popMatrix();

			//LOWER RIGHT LEG
			g.pushMatrix();
			capsule = (CapsuleShape) shapes[BodyPart.BODYPART_LEFT_UPPER_ARM
					.ordinal()];
			radius = capsule.getRadius();
			halfHeight = capsule.getHalfHeight();
			myTransform = bodies[BodyPart.BODYPART_LEFT_UPPER_ARM.ordinal()]
					.getMotionState().getWorldTransform(myTransform);
			applyMatrix(myTransform, g);
			functions.cylinder(radius, radius * .5f, halfHeight * 2,
					SETTINGS.cylinder_res, g);
			g.translate(0, halfHeight, 0);
			g.sphere(radius * 0.7f); //jaw
			g.translate(0, -halfHeight * 2, 0);
			g.sphere(radius * 1.1f);
			g.popMatrix();

			//LOWER RIGHT LEG
			g.pushMatrix();
			capsule = (CapsuleShape) shapes[BodyPart.BODYPART_RIGHT_LOWER_ARM
					.ordinal()];
			radius = capsule.getRadius();
			halfHeight = capsule.getHalfHeight();
			myTransform = bodies[BodyPart.BODYPART_RIGHT_LOWER_ARM.ordinal()]
					.getMotionState().getWorldTransform(myTransform);
			applyMatrix(myTransform, g);
			functions.cylinder(radius, radius * .5f, halfHeight * 2,
					SETTINGS.cylinder_res, g);
			g.translate(0, halfHeight, 0);
			g.sphere(radius * 0.7f); //jaw
			g.translate(0, -halfHeight * 2, 0);
			g.sphere(radius * 1.1f);
			g.popMatrix();

			//LOWER RIGHT LEG
			g.pushMatrix();
			capsule = (CapsuleShape) shapes[BodyPart.BODYPART_LEFT_LOWER_ARM
					.ordinal()];
			radius = capsule.getRadius();
			halfHeight = capsule.getHalfHeight();
			myTransform = bodies[BodyPart.BODYPART_LEFT_LOWER_ARM.ordinal()]
					.getMotionState().getWorldTransform(myTransform);
			applyMatrix(myTransform, g);
			functions.cylinder(radius, radius * .5f, halfHeight * 2,
					SETTINGS.cylinder_res, g);
			g.translate(0, halfHeight, 0);
			g.sphere(radius * 0.7f); //jaw
			g.translate(0, -halfHeight * 2, 0);
			g.sphere(radius * 1.1f);
			g.popMatrix();

		}

		g.popMatrix();

		scaling = false;
	}

	private void renderFace(PGraphics g) {
		g.rotateY((float) (Math.PI / 2));
		g.translate(0, 0, 3);
		g.scale(.003f * this.getScale());
		g.image(FaceImages[faceExpressions.HAPPY.ordinal()], -45, -25);
	}

	public void resetPhysics() {
		// TODO Auto-generated method stub
		//System.out.print("HERE");
		//this.scale = 10;
		destroy();
		makeBody(this.ownerWorld, this.startPos, this.scale);

		/*
		bodies[BodyPart.BODYPART_PELVIS.ordinal()].setMotionState(motionState[BodyPart.BODYPART_PELVIS.ordinal()] );
		bodies[BodyPart.BODYPART_SPINE.ordinal()].setMotionState(motionState[BodyPart.BODYPART_SPINE.ordinal()]);
		bodies[BodyPart.BODYPART_HEAD.ordinal()].setMotionState(motionState[BodyPart.BODYPART_HEAD.ordinal()]);		
		bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()].setMotionState(motionState[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()]);
		bodies[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()].setMotionState(motionState[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()] );
		bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()].setMotionState(motionState[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()]);
		bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()].setMotionState(motionState[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()]);
		*/
	}

	public void scale(float scaleIn) {

		///bodies[BodyPart.BODYPART_PELVIS.ordinal()].

		this.scale -= scaleIn * GLOBAL.jBullet.getScale();
		scaling = true;

		motionState[BodyPart.BODYPART_PELVIS.ordinal()] = bodies[BodyPart.BODYPART_PELVIS
				.ordinal()].getMotionState();
		motionState[BodyPart.BODYPART_SPINE.ordinal()] = bodies[BodyPart.BODYPART_SPINE
				.ordinal()].getMotionState();
		motionState[BodyPart.BODYPART_HEAD.ordinal()] = bodies[BodyPart.BODYPART_HEAD
				.ordinal()].getMotionState();
		motionState[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()] = bodies[BodyPart.BODYPART_LEFT_UPPER_LEG
				.ordinal()].getMotionState();
		motionState[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()] = bodies[BodyPart.BODYPART_LEFT_LOWER_LEG
				.ordinal()].getMotionState();
		motionState[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()] = bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG
				.ordinal()].getMotionState();
		motionState[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()] = bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG
				.ordinal()].getMotionState();
		destroy();
		makeBody(this.ownerWorld, this.startPos, this.scale);

		bodies[BodyPart.BODYPART_PELVIS.ordinal()]
				.setMotionState(motionState[BodyPart.BODYPART_PELVIS.ordinal()]);
		bodies[BodyPart.BODYPART_SPINE.ordinal()]
				.setMotionState(motionState[BodyPart.BODYPART_SPINE.ordinal()]);
		bodies[BodyPart.BODYPART_HEAD.ordinal()]
				.setMotionState(motionState[BodyPart.BODYPART_HEAD.ordinal()]);
		bodies[BodyPart.BODYPART_LEFT_UPPER_LEG.ordinal()]
				.setMotionState(motionState[BodyPart.BODYPART_LEFT_UPPER_LEG
						.ordinal()]);
		bodies[BodyPart.BODYPART_LEFT_LOWER_LEG.ordinal()]
				.setMotionState(motionState[BodyPart.BODYPART_LEFT_LOWER_LEG
						.ordinal()]);
		bodies[BodyPart.BODYPART_RIGHT_UPPER_LEG.ordinal()]
				.setMotionState(motionState[BodyPart.BODYPART_RIGHT_UPPER_LEG
						.ordinal()]);
		bodies[BodyPart.BODYPART_RIGHT_LOWER_LEG.ordinal()]
				.setMotionState(motionState[BodyPart.BODYPART_RIGHT_LOWER_LEG
						.ordinal()]);

	}

}
