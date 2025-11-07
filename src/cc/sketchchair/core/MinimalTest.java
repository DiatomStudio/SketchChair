package cc.sketchchair.core;

import processing.core.PApplet;

/**
 * Minimal P3D test with spinning cube - no P2D, just pure P3D
 */
public class MinimalTest extends PApplet {

	public static void main(String args[]) {
		System.out.println("=== MINIMAL P3D SPINNING CUBE TEST ===");
		PApplet.main(new String[] { MinimalTest.class.getName() });
	}

	public void settings() {
		System.out.println("Settings: Creating 400x400 P3D window");
		size(400, 400, P3D);
	}

	public void setup() {
		System.out.println("Setup: P3D window created successfully!");
		System.out.println("Renderer: " + g.getClass().getName());
	}

	public void draw() {
		background(200);
		lights();

		translate(width/2, height/2);
		rotateY(frameCount * 0.01f);
		rotateX(frameCount * 0.005f);

		fill(100, 150, 250);
		box(100);

		if (frameCount % 60 == 0) {
			System.out.println("Frame " + frameCount + " - P3D running");
		}
	}
}
