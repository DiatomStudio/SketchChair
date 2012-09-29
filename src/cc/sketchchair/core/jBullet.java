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

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Vector3f;

import processing.core.PGraphics;

import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConcaveShape;
import com.bulletphysics.collision.shapes.PolyhedralConvexShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.Point2PointConstraint;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.linearmath.Clock;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

import cz.advel.stack.Stack;

public class jBullet {
	// JBullet stuff

	public float scale = .03f;
	public DiscreteDynamicsWorld myWorld;
	// create 125 (5x5x5) dynamic object
	private static final int ARRAY_SIZE_X = 4;
	private static final int ARRAY_SIZE_Y = 4;
	private static final int ARRAY_SIZE_Z = 4;

	// maximum number of objects (and allow user to shoot additional boxes)
	private static final int MAX_PROXIES = (ARRAY_SIZE_X * ARRAY_SIZE_Y
			* ARRAY_SIZE_Z + 1024);

	private static final int START_POS_X = -5;
	private static final int START_POS_Y = -5;
	private static final int START_POS_Z = -3;
	public static RigidBody ZeroBodyChair = null;

	// keep the collision shapes, for deletion/cleanup
	public ArrayList<CollisionShape> collisionShapes = new ArrayList<CollisionShape>();
	public BroadphaseInterface overlappingPairCache;
	public CollisionDispatcher dispatcher;
	public ConstraintSolver solver;
	public DefaultCollisionConfiguration collisionConfiguration;

	ArrayList<RigidBody> rigidBodies = new ArrayList<RigidBody>();

	// constraint for mouse picking
	protected TypedConstraint pickConstraint = null;
	public static RigidBody pickedBody = null; // for deactivation state
	protected float pickDist = 0;
	protected Vector3f cameraPosition = new Vector3f(30, 30, 0f);
	protected Vector3f cameraTargetPosition = new Vector3f(0, 0, 1f);
	protected Vector3f cameraUp = new Vector3f(0f, 1f, 0f);
	protected float glutScreenWidth = 600;
	protected float glutScreenHeight = 600;

	protected Clock clock = new Clock();
	private StaticPlaneShape constrainPlane;
	RigidBody constrainPlane2D;
	public RigidBody ZeroBody;
	public boolean physics_on = false;
	private RigidBody groundBody;

	jBullet() {
		this.initPhysics();
	}

	public void clientMoveAndDisplay() {
		// gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// simple dynamics world doesn't handle fixed-time-stepping
		// float ms = getDeltaTimeMicroseconds();

		// step the simulation
		// if (dynamicsWorld != null) {
		// dynamicsWorld.stepSimulation(ms / 1000000f);
		// optional but useful: debug drawing
		// dynamicsWorld.debugDrawWorld();
		// }

		// renderme();

		// glFlush();
		// glutSwapBuffers();
	}

	public Vector3f getCameraPosition() {
		return cameraPosition;
	}

	// ccdDemo.initPhysics();
	// ccdDemo.getDynamicsWorld().setDebugDrawer(new
	// GLDebugDrawer(LWJGL.getGL()));

	public Vector3f getCameraTargetPosition() {
		return cameraTargetPosition;
	}

	public float getDeltaTimeMicroseconds() {
		//#ifdef USE_BT_CLOCK
		float dt = clock.getTimeMicroseconds();
		clock.reset();
		return dt;
		//#else
		//return btScalar(16666.);
		//#endif
	}

	public RigidBody getOver(float x, float y) {
		x = (int) this.scaleVal(x);
		y = (int) this.scaleVal(y);

		Vector3f rayTo = new Vector3f(getRayToCustom(x, y));

		// add a point to point constraint for picking
		if (myWorld != null) {
			CollisionWorld.ClosestRayResultCallback rayCallback = new CollisionWorld.ClosestRayResultCallback(
					cameraPosition, rayTo);
			myWorld.rayTest(cameraPosition, rayTo, rayCallback);
			if (rayCallback.hasHit()) {

				RigidBody body = RigidBody.upcast(rayCallback.collisionObject);
				if (body != null) {

					return body;
				} else {
					return null;

				}
			}
		}
		return null;
	}

	public Vector3f getRayTo(int x, int y) {
		float top = 1f;
		float bottom = -1f;
		float nearPlane = 1f;
		float tanFov = (top - bottom) * 0.5f / nearPlane;
		float fov = 2f * (float) Math.atan(tanFov);

		Vector3f rayFrom = new Vector3f(getCameraPosition());
		Vector3f rayForward = new Vector3f();
		rayForward.sub(getCameraTargetPosition(), getCameraPosition());
		rayForward.normalize();
		float farPlane = 10000f;
		rayForward.scale(farPlane);

		Vector3f rightOffset = new Vector3f();
		Vector3f vertical = new Vector3f(cameraUp);

		Vector3f hor = new Vector3f();
		// TODO: check: hor = rayForward.cross(vertical);
		hor.cross(rayForward, vertical);
		hor.normalize();
		// TODO: check: vertical = hor.cross(rayForward);
		vertical.cross(hor, rayForward);
		vertical.normalize();

		float tanfov = (float) Math.tan(0.5f * fov);

		float aspect = glutScreenHeight / glutScreenWidth;

		hor.scale(2f * farPlane * tanfov);
		vertical.scale(2f * farPlane * tanfov);

		if (aspect < 1f) {
			hor.scale(1f / aspect);
		} else {
			vertical.scale(aspect);
		}

		Vector3f rayToCenter = new Vector3f();
		rayToCenter.add(rayFrom, rayForward);
		Vector3f dHor = new Vector3f(hor);
		dHor.scale(1f / glutScreenWidth);
		Vector3f dVert = new Vector3f(vertical);
		dVert.scale(1.f / glutScreenHeight);

		Vector3f tmp1 = new Vector3f();
		Vector3f tmp2 = new Vector3f();
		tmp1.scale(0.5f, hor);
		tmp2.scale(0.5f, vertical);

		Vector3f rayTo = new Vector3f();
		rayTo.sub(rayToCenter, tmp1);
		rayTo.add(tmp2);

		tmp1.scale(x, dHor);
		tmp2.scale(y, dVert);

		rayTo.add(tmp1);
		rayTo.sub(tmp2);
		return rayTo;
	}

	public Vector3f getRayToCustom(float x, float y) {
		Vector3f rayTo = new Vector3f(x, y, -1000);
		//	cameraPosition = new Vector3f(x,y,10);
		cameraPosition.x = x;
		cameraPosition.y = y;
		cameraPosition.z = 1000;
		return rayTo;

	}

	public float getScale() {
		return this.scale;
	}

	
	public void resetCollisons(){
		
		// collision configuration contains default setup for memory, collision
		// setup
		//collisionConfiguration = new DefaultCollisionConfiguration();

		// use the default collision dispatcher. For parallel processing you can
		// use a diffent dispatcher (see Extras/BulletMultiThreaded)
		//dispatcher = new CollisionDispatcher(collisionConfiguration);
		
		// the default constraint solver. For parallel processing you can use a
		// different solver (see Extras/BulletMultiThreaded)
		//SequentialImpulseConstraintSolver sol = new SequentialImpulseConstraintSolver();
		//solver = sol;
		//this.myWorld.dispatcher1 = dispatcher;
		//this.myWorld.setConstraintSolver(this.solver);
		//this.myWorld.destroy();
		
	for(int i = 0 ; i < this.myWorld.getCollisionWorld().getDispatcher().getNumManifolds(); i++){
		PersistentManifold manifold = 	this.myWorld.getCollisionWorld().getDispatcher().getManifoldByIndexInternal(i);
		this.myWorld.getCollisionWorld().getDispatcher().clearManifold(manifold);
	}
		
	}
	public void initPhysics() {

		// collision configuration contains default setup for memory, collision
		// setup
		collisionConfiguration = new DefaultCollisionConfiguration();

		// use the default collision dispatcher. For parallel processing you can
		// use a diffent dispatcher (see Extras/BulletMultiThreaded)
		dispatcher = new CollisionDispatcher(collisionConfiguration);

		// the maximum size of the collision world. Make sure objects stay
		// within these boundaries
		// TODO: AxisSweep3
		// Don't make the world AABB size too large, it will harm simulation
		// quality and performance
		Vector3f worldAabbMin = new Vector3f(-100000, -100000, -100000);
		Vector3f worldAabbMax = new Vector3f(100000, 100000, 100000);
		overlappingPairCache = new AxisSweep3(worldAabbMin, worldAabbMax);

		//overlappingPairCache = new AxisSweep3(worldAabbMin, worldAabbMax,
		//		MAX_PROXIES);
		// overlappingPairCache = new SimpleBroadphase(MAX_PROXIES);

		// the default constraint solver. For parallel processing you can use a
		// different solver (see Extras/BulletMultiThreaded)
		SequentialImpulseConstraintSolver sol = new SequentialImpulseConstraintSolver();
		solver = sol;

		// TODO: needed for SimpleDynamicsWorld
		// sol.setSolverMode(sol.getSolverMode() &
		// ~SolverMode.SOLVER_CACHE_FRIENDLY.getMask());

		myWorld = new DiscreteDynamicsWorld(dispatcher, overlappingPairCache,
				solver, collisionConfiguration);
		// dynamicsWorld = new SimpleDynamicsWorld(dispatcher,
		// overlappingPairCache, solver, collisionConfiguration);

		myWorld.setGravity(new Vector3f(0f, 3, 0f));

		// create a few basic rigid bodies
		// CollisionShape groundShape = new BoxShape(new Vector3f(50f, 50f,
		// 50f));
		CollisionShape groundShape = new StaticPlaneShape(
				new Vector3f(0, -1, 0), 1);

		collisionShapes.add(groundShape);

		Transform groundTransform = new Transform();
		groundTransform.setIdentity();
		groundTransform.origin.set(this.scaleVal(600), this.scaleVal(1600),
				this.scaleVal(600));

		CollisionShape constrainPlane = new StaticPlaneShape(new Vector3f(0, 0,
				1), 1);

		Vector3f localInertia2 = new Vector3f(0, 0, 0);
		Transform planeTransform = new Transform();
		planeTransform.setIdentity();
		planeTransform.origin.set(this.scaleVal(600), this.scaleVal(600),
				this.scaleVal(0));

		// using motionstate is recommended, it provides interpolation
		// capabilities, and only synchronizes 'active' objects
		DefaultMotionState myMotionState2 = new DefaultMotionState(
				planeTransform);
		RigidBodyConstructionInfo rbInfo2 = new RigidBodyConstructionInfo(0,
				myMotionState2, constrainPlane, localInertia2);
		RigidBody body2 = new RigidBody(rbInfo2);
		this.constrainPlane2D = body2;

		// We can also use DemoApplication::localCreateRigidBody, but for
		// clarity it is provided here:
		{
			float mass = 0f;

			// rigidbody is dynamic if and only if mass is non zero, otherwise
			// static
			boolean isDynamic = (mass != 0f);

			Vector3f localInertia = new Vector3f(0, 0, 0);
			if (isDynamic) {
				groundShape.calculateLocalInertia(mass, localInertia);
			}

			// using motionstate is recommended, it provides interpolation
			// capabilities, and only synchronizes 'active' objects
			DefaultMotionState myMotionState = new DefaultMotionState(
					groundTransform);
			RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(
					mass, myMotionState, groundShape, localInertia);
			 groundBody = new RigidBody(rbInfo);

			this.addGround();
			
		}
/*
		{
			// create a few dynamic rigidbodies
			// Re-using the same collision is better for memory usage and
			// performance

			CollisionShape colShape = new BoxShape(new Vector3f(10, 10, 10));
			// CollisionShape colShape = new SphereShape(1f);
			collisionShapes.add(colShape);

			// Create Dynamic Objects
			Transform startTransform = new Transform();
			// startTransform.origin.set(new Vector3f(300,0,0));
			startTransform.setIdentity();

			float mass = 10f;

			// rigidbody is dynamic if and only if mass is non zero, otherwise
			// static
			boolean isDynamic = (mass != 0f);

			Vector3f localInertia = new Vector3f(0, 0, 0);
			if (isDynamic) {
				colShape.calculateLocalInertia(mass, localInertia);
			}

			for (int i = 0; i < 20; i++) {
				startTransform.origin.set(i * 30, 0, 0);

				// using motionstate is recommended, it provides interpolation
				// capabilities, and only synchronizes 'active' objects
				DefaultMotionState myMotionState = new DefaultMotionState(
						startTransform);
				RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(
						mass, myMotionState, colShape, localInertia);
				RigidBody body = new RigidBody(rbInfo);
				// myWorld.addRigidBody(body);
				//this.rigidBodies.add(body);

			}
		}
*/
		// this.myWorld.updateActivationState(1.0f);

	}

	public void addGround() {
		// add the body to the dynamics world
					myWorld.addRigidBody(groundBody);
					this.rigidBodies.add(groundBody);		
	}
	
	public void removeGround() {
		// add the body to the dynamics world
		myWorld.removeRigidBody(groundBody);
		this.rigidBodies.remove(groundBody);
	}

	void mouseDown(float x, float y) {

		x = (int) this.scaleVal(x);
		y = (int) this.scaleVal(y);

		Vector3f rayTo = new Vector3f(getRayToCustom(x, y));

		if (pickConstraint != null && myWorld != null) {
			myWorld.removeConstraint(pickConstraint);
			// delete m_pickConstraint;
			//printf("removed constraint %i",gPickingConstraintId);
			pickConstraint = null;
			pickedBody.forceActivationState(CollisionObject.ACTIVE_TAG);
			pickedBody.setDeactivationTime(0f);
			pickedBody = null;
		}

		// add a point to point constraint for picking
		if (myWorld != null) {
			CollisionWorld.ClosestRayResultCallback rayCallback = new CollisionWorld.ClosestRayResultCallback(
					cameraPosition, rayTo);
			myWorld.rayTest(cameraPosition, rayTo, rayCallback);
			if (rayCallback.hasHit()) {

				RigidBody body = RigidBody.upcast(rayCallback.collisionObject);
				if (body != null) {

					//if physics is off turn it on
					//if(	!SETTINGS.physics_on)
					//	SETTINGS.physics_on = true;

					// other exclusions?
					if (!(body.isStaticObject() || body.isKinematicObject())) {
						pickedBody = body;
						pickedBody
								.setActivationState(CollisionObject.DISABLE_DEACTIVATION);

						Vector3f pickPos = new Vector3f(
								rayCallback.hitPointWorld);

						Transform tmpTrans = body
								.getCenterOfMassTransform(new Transform());
						tmpTrans.inverse();
						Vector3f localPivot = new Vector3f(pickPos);
						tmpTrans.transform(localPivot);

						Point2PointConstraint p2p = new Point2PointConstraint(
								body, localPivot);
						myWorld.addConstraint(p2p);
						pickConstraint = p2p;
						// save mouse position for dragging
						BulletStats.gOldPickingPos.set(rayTo);
						Vector3f eyePos = new Vector3f(cameraPosition);
						Vector3f tmp = new Vector3f();
						//tmp.sub(pickPos, eyePos);
						//BulletStats.gOldPickingDist = tmp.length();
						this.pickDist = pickPos.z;
						// very weak constraint for picking
						p2p.setting.tau = 0.2f;
					} else {

					}
				}
			}
		}

	}

	public void mouseDragged(float x, float y) {

		x = (int) this.scaleVal(x);
		y = (int) this.scaleVal(y);
		if (pickConstraint != null) {
			// move the constraint pivot
			Point2PointConstraint p2p = (Point2PointConstraint) pickConstraint;
			if (p2p != null) {
				// keep it at the same picking distance

				//Vector3f newRayTo = new Vector3f(getRayToCustom(x, y));
				//Vector3f eyePos = new Vector3f(cameraPosition);
				//Vector3f dir = new Vector3f();
				//dir.sub(newRayTo, eyePos);
				//dir.normalize();
				//dir.scale(BulletStats.gOldPickingDist);

				Vector3f newPos = new Vector3f(x, y, this.pickDist);
				//newPos.add(eyePos, dir);
				p2p.setPivotB(newPos);
			}
		}

	}

	void mouseReleased(int mouseX, int mouseY) {
		mouseX = (int) this.scaleVal(mouseX);
		mouseY = (int) this.scaleVal(mouseY);

		if (pickConstraint != null && myWorld != null) {
			myWorld.removeConstraint(pickConstraint);
			// delete m_pickConstraint;
			//printf("removed constraint %i",gPickingConstraintId);
			pickConstraint = null;
			pickedBody.forceActivationState(CollisionObject.ACTIVE_TAG);
			pickedBody.setDeactivationTime(0f);
			pickedBody = null;
		}
	}

	void render(PGraphics g) {

		if (!SETTINGS.DEBUG)
			return;

		g.pushMatrix();
		g.scale(1 /this.getScale());

		IDebugDrawMe debugD = new IDebugDrawMe(g);
		this.myWorld.setDebugDrawer(debugD);
		this.myWorld.debugDrawWorld();

		g.noFill();
		g.stroke(0);
		g.strokeWeight(1);

		Iterator ite = myWorld.getCollisionObjectArray().iterator();

		while (ite.hasNext()) {

			RigidBody rBody = (RigidBody) ite.next();

			Transform myTransform = new Transform();
			myTransform = rBody.getMotionState().getWorldTransform(myTransform);

			g.pushMatrix();

			g.translate(myTransform.origin.x, myTransform.origin.y,
					myTransform.origin.z);

			g.applyMatrix(myTransform.basis.m00, myTransform.basis.m01,
					myTransform.basis.m02, 0, myTransform.basis.m10,
					myTransform.basis.m11, myTransform.basis.m12, 0,
					myTransform.basis.m20, myTransform.basis.m21,
					myTransform.basis.m22, 0, 0, 0, 0, 1);
			// rBody.
			// rBody.
			// System.out.println(myTransform.origin.y);
			// fill(fallRigidBody.c);
			// do the actual drawing of the object

			CollisionShape col = rBody.getCollisionShape();
			//if (col.isPolyhedral()) {
				Vector3f posP = new Vector3f();
				float[] sizeP = { 0 };
				col.getBoundingSphere(posP, sizeP);
				g.pushMatrix();
				g.translate(posP.x, posP.y, posP.z);
				g.sphere(sizeP[0]);
				g.popMatrix();

			//}

			if (col.getShapeType() == BroadphaseNativeType.GIMPACT_SHAPE_PROXYTYPE) {

				ConcaveShape polyshape = (ConcaveShape) col;
				/*
				int i;
				for (i=0;i<polyshape.getNumEdges();i++)
				{
					Vector3f a = Stack.alloc(Vector3f.class);
					Vector3f b = Stack.alloc(Vector3f.class);;
					polyshape.getEdge(i,a,b);
					Vector3f tmp2 = new Vector3f(0f, 0f, 1f);

					debugD.drawLine(a, b, tmp2);
				//					getDebugDrawer()->drawLine(wa,wb,color);
				//
				}
				*/
				//
				//
			}

			if (col.getShapeType() == BroadphaseNativeType.CAPSULE_SHAPE_PROXYTYPE) {
				Vector3f pos = new Vector3f();
				// System.out.println("capsula");
				float[] size = { 0 };
				//col.getAabb(t, aabbMin, aabbMax)
				col.getBoundingSphere(pos, size);
				g.pushMatrix();
				g.translate(pos.x, pos.y, pos.z);
				g.box(05, (int) (size[0] * 1.5), 05);
				g.popMatrix();

			}

			// PolyhedralConvexShape;// tri = col;
			// g.box(20,20,20);
			g.popMatrix();
		}
		g.popMatrix();

	}

	public float scaleVal(float val) {

		return this.scale * val;
	}

	void step() {

		float ms = getDeltaTimeMicroseconds();
		myWorld.stepSimulation(ms / 1000000f);
	}

	public void update() {
		//Vector3f gravity = null;
		//myWorld.getGravity(gravity);
		myWorld.setGravity(new Vector3f(0, 0, 0));

		Iterator ite = myWorld.getCollisionObjectArray().iterator();
		while (ite.hasNext()) {
			RigidBody rBody = (RigidBody) ite.next();
			// rBody.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
			rBody.clearForces();
			rBody.setAngularVelocity(new Vector3f(0, 0, 0));
			rBody.setLinearVelocity(new Vector3f(0, 0, 0));
			rBody.clearForces();
			rBody.activate(false);

		}

		float ms = getDeltaTimeMicroseconds();

		//if(ms > 100)
		try {
			myWorld.stepSimulation(ms / 1000f);
		} catch (Exception ex) {
		}

		ite = myWorld.getCollisionObjectArray().iterator();

		while (ite.hasNext()) {
			RigidBody rBody = (RigidBody) ite.next();
			rBody.activate(true);
			// rBody.setCollisionFlags(CollisionFlags);

		}
		myWorld.setGravity(new Vector3f(0, SETTINGS.gravity, 0));

	}

	public void printDebugInfo() {

		LOGGER.debug("Number of collision objs: "+ this.myWorld.getNumCollisionObjects() );
		LOGGER.debug("Number of Actions: "+ this.myWorld.getNumActions());
		LOGGER.debug("Number of constrints: "+ this.myWorld.getNumConstraints());
		LOGGER.debug("Number of Manifolds: "+ this.myWorld.getCollisionWorld().getDispatcher().getNumManifolds());
		//LOGGER.info("Number of Manifolds: "+ this.myWorld.getCollisionWorld().getDebugDrawer().);



	}

}
