package cs3540;

import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;

public class Prototype2 extends PApplet {
	private class Destination {
		float x = 0;
		float y = 0;
		float rotation = 0;
		float z = 0;
	}

	static final int screenPPI = 72; // what is the DPI of the screen you are using
	// you can test this by drawing a 72x72 pixel rectangle in code, and then
	// confirming with a ruler it is 1x1 inch.

	// these are variables you should probably leave alone
	static final int trialCount = 12; // this will be set higher for the bakeoff
	static final float errorPenalty = 0.5f; // for every error, add this value to mean time
	static final float border = inchToPix(2f); // some padding from the sides of window

	boolean keyDown = false; // is a key on the keyboard held down?
	boolean userDone = false; // is the user done
	int trialIndex = 0; // what trial are we on
	int errorCount = 0; // used to keep track of errors
	int startTime = 0; // time starts when the first click is captured
	int finishTime = 0; // records the time of the final click

	// These variables are for my example design. Your input code should
	// modify/replace these!
	float logoX = 500f;
	float logoY = 500f;
	float logoZ = 50f;
	float logoRotation = 0f;
	
	// The argument passed to main must match the class name
	public static void main(String[] args) {
		// Tell processing what class we want to run.
		PApplet.main("cs3540.Prototype2");
	}

	ArrayList<Destination> destinations = new ArrayList<Destination>();

	public void settings() {
		size(800, 600);
	}

	public void setup() {
		rectMode(CENTER); // draw rectangles not from upper left, but from the center outwards
		textFont(createFont("Arial", inchToPix(.3f))); // sets the font to Arial that is 0.3" tall
		textAlign(CENTER);

		for (int i = 0; i < trialCount; i++) // don't change this!
		{
			Destination d = new Destination();
			d.x = random(border, width - border); // set a random x with some padding
			d.y = random(border, height - border); // set a random y with some padding
			d.rotation = random(0, 360); // random rotation between 0 and 360
			int j = (int) random(20);
			d.z = ((j % 12) + 1) * inchToPix(.25f); // increasing size from .25 up to 3.0"
			destinations.add(d);
			println("created target with " + d.x + "," + d.y + "," + d.rotation + "," + d.z);
		}

		Collections.shuffle(destinations); // randomize the order of the button; don't change this.
	}

	public void draw() {
		background(40); // background is dark grey
		fill(200);
		noStroke();

		// shouldn't really modify this printout code unless there is a really good
		// reason to
		if (userDone) {
			text("User completed " + trialCount + " trials", width / 2, inchToPix(.4f));
			text("User had " + errorCount + " error(s)", width / 2, inchToPix(.4f) * 2);
			text("User took " + (finishTime - startTime) / 1000f / trialCount + " sec per destination", width / 2,
					inchToPix(.4f) * 3);
			text("User took " + ((finishTime - startTime) / 1000f / trialCount + (errorCount * errorPenalty))
					+ " sec per destination inc. penalty", width / 2, inchToPix(.4f) * 4);
			return;
		}

		// ===========DRAW DESTINATION SQUARES=================
		for (int i = trialIndex; i < trialCount; i++) // reduces over time
		{
			pushMatrix();
			Destination d = destinations.get(i); // get destination trial
			translate(d.x, d.y); // center the drawing coordinates to the center of the destination trial
			rotate(radians(d.rotation)); // rotate around the origin of the destination trial
			noFill();
			strokeWeight(3f);
			if (trialIndex == i)
				stroke(255, 0, 0, 192); // set color to semi translucent
			else
				stroke(128, 128, 128, 128); // set color to semi translucent
			rect(0, 0, d.z, d.z);
			popMatrix();
		}

		// ===========DRAW LOGO SQUARE=================
		pushMatrix();
		translate(logoX, logoY); // translate draw center to the center oft he logo square
		rotate(radians(logoRotation)); // rotate using the logo square as the origin
		noStroke();
		fill(60, 60, 192, 192);
		rect(0, 0, logoZ, logoZ);
		popMatrix();

		// ===========DRAW UI=================
		fill(255);
		controlLogic();
		text("Trial " + (trialIndex + 1) + " of " + trialCount, width / 2, inchToPix(.8f));
	}

	public void keyPressed() {
		keyDown = true;

		// these are things that should only trigger on the rising edge
		switch (key) {
		case ' ':
			finishTrial();
			break;
		default:
			break;
		}
	}

	public void keyReleased() {
		keyDown = false;

		// these are things that should only trigger on the falling edge
		switch (key) {
		default:
			break;
		}
	}

	// probably shouldn't modify this, but email me if you want to for some good
	// reason.
	public final boolean checkForSuccess() {
		Destination d = destinations.get(trialIndex);
		boolean closeDist = dist(d.x, d.y, logoX, logoY) < inchToPix(.05f); // has to be within +-0.05"
		boolean closeRotation = calculateDifferenceBetweenAngles(d.rotation, logoRotation) <= 5;
		boolean closeZ = abs(d.z - logoZ) < inchToPix(.1f); // has to be within +-0.1"

		println("Close Enough Distance: " + closeDist + " (logo X/Y = " + d.x + "/" + d.y + ", destination X/Y = "
				+ logoX + "/" + logoY + ")");
		println("Close Enough Rotation: " + closeRotation + " (rot dist="
				+ calculateDifferenceBetweenAngles(d.rotation, logoRotation) + ")");
		println("Close Enough Z: " + closeZ + " (logo Z = " + d.z + ", destination Z = " + logoZ + ")");
		println("Close enough all: " + (closeDist && closeRotation && closeZ));

		return closeDist && closeRotation && closeZ;
	}

	public final boolean timerHasStarted() {
		return startTime == 0;
	}

	// utility function I include to calc diference between two angles
	static final double calculateDifferenceBetweenAngles(float a1, float a2) {
		double diff = abs(a1 - a2);
		diff %= 90;
		if (diff > 45)
			return 90 - diff;
		else
			return diff;
	}

	// utility function to convert inches into pixels based on screen PPI
	static final float inchToPix(float inch) {
		return inch * screenPPI;
	}

	void controlLogic() {
		float translationSpeed = inchToPix(.02f);
		float rotationSpeed = 1f;

		// Everything else in this method assumes the user is holding down a key.
		if (!keyDown) {
			return;
		}
		
		if (timerHasStarted()) {
			startTimer();
		}

		switch (key) {
		case 'A':
		case 'a':
			logoX -= translationSpeed;
			break;
		case 'D':
		case 'd':
			logoX += translationSpeed;
			break;
		case 'W':
		case 'w':
			logoY -= translationSpeed;
			break;
		case 'S':
		case 's':
			logoY += translationSpeed;
			break;
		case 'Q':
		case 'q':
			logoRotation -= rotationSpeed;
			break;
		case 'E':
		case 'e':
			logoRotation += rotationSpeed;
			break;
		case CODED:
			switch (keyCode) {
			case DOWN:
				logoZ = constrain(logoZ - translationSpeed, .01f, inchToPix(4f)); // leave min and max alone!
				break;
			case UP:
				logoZ = constrain(logoZ + translationSpeed, .01f, inchToPix(4f)); // leave min and max alone!
				break;
			default:
				break;
			}
		default:
			break;
		}
	}

	void startTimer() {
		startTime = millis();
		println("time started!");
	}

	void stopTimer() {
		userDone = true;
		finishTime = millis();
	}

	void finishTrial() {
		if (userDone) {
			return;
		}

		if (!checkForSuccess()) {
			errorCount++;
		}

		if (++trialIndex >= trialCount) {
			stopTimer();
		}
	}
}
