
package cs3540;

import java.util.ArrayList;
import java.util.Collections;
// P2 TESTING CHANGE: Persist completed-run results as CSV rows.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import processing.core.PApplet;

public class Prototype3 extends PApplet {

    // These are variables you should probably leave alone
    int index = 0;
    float border = 0;
    int trialCount = 2;
    int trialIndex = 0;
    int errorCount = 0;
    float errorPenalty = 0.5f;
    int startTime = 0;
    int finishTime = 0;
    boolean userDone = false;

    // P2 TESTING CHANGE: Track individual trial time and the ten-run summary.
    int trialStartTime = 0;
    int completedRunCount = 0;
    double accuracyTotal = 0;
    double timeWithoutPenaltyTotal = 0;

    final int screenPPI = 72;

    // Original cursor square variables
    float logoX = 500;
    float logoY = 500;
    float logoZ = 50f;
    float logoRotation = 0;

    // ==================================================
    // P2 CHANGE 1: Track which control mode is active
    // ==================================================
    // 0 = Ready, 1 = Move, 2 = Scale, 3 = Rotate
    int activeMode = 0;

    // P2 CHANGE: Colors for each mode
    final int MOVE_MODE = 1;
    final int SCALE_MODE = 2;
    final int ROTATE_MODE = 3;

    public static void main(String[] args) {
        PApplet.main("cs3540.Prototype3");
    }

    private class Destination {
        float x = 0;
        float y = 0;
        float rotation = 0;
        float z = 0;
    }

    ArrayList<Destination> destinations =
            new ArrayList<Destination>();

    public void settings() {
        size(800, 600);
    }

    public void setup() {
        rectMode(CENTER);
        textFont(createFont("Arial", inchToPix(.3f)));
        textAlign(CENTER);
        rectMode(CENTER);

        // Original target generation — unchanged
        border = inchToPix(2f);

        for (int i = 0; i < trialCount; i++) {
            Destination d = new Destination();
            d.x = random(border, width - border);
            d.y = random(border, height - border);
            d.rotation = random(0, 360);
            int j = (int) random(20);
            d.z = ((j % 12) + 1) * inchToPix(.25f);
            destinations.add(d);

            println("created target with " + d.x + ","
                    + d.y + "," + d.rotation + "," + d.z);
        }

        Collections.shuffle(destinations);
    }

    public void draw() {

        background(40);
        fill(200);
        noStroke();

        // Original results display — unchanged
        if (userDone) {
            text("User completed " + trialCount + " trials",
                    width / 2, inchToPix(.4f));

            text("User had " + errorCount + " error(s)",
                    width / 2, inchToPix(.4f) * 2);

            text("User took "
                    + (finishTime - startTime) / 1000f / trialCount
                    + " sec per destination",
                    width / 2, inchToPix(.4f) * 3);

            text("User took "
                    + ((finishTime - startTime) / 1000f / trialCount
                    + (errorCount * errorPenalty))
                    + " sec per destination inc. penalty",
                    width / 2, inchToPix(.4f) * 4);
            return;
        }

        // =========== DRAW DESTINATION SQUARES ===========
        // Original destination drawing — unchanged
        for (int i = trialIndex; i < trialCount; i++) {
            pushMatrix();
            Destination d = destinations.get(i);
            translate(d.x, d.y);
            rotate(radians(d.rotation));
            noFill();
            strokeWeight(3f);

            if (trialIndex == i)
                stroke(255, 0, 0, 192);
            else
                stroke(128, 128, 128, 128);

            rect(0, 0, d.z, d.z);
            popMatrix();
        }

        // ==================================================
        // P2 CHANGE 2: Change cursor color based on mode
        // ==================================================
        pushMatrix();
        translate(logoX, logoY);
        rotate(radians(logoRotation));
        noStroke();

        if (activeMode == MOVE_MODE) {
            fill(0, 170, 255, 192); // Blue: movement
        } else if (activeMode == SCALE_MODE) {
            fill(0, 200, 80, 192); // Green: scaling
        } else if (activeMode == ROTATE_MODE) {
            fill(255, 150, 0, 192); // Orange: rotation
        } else {
            fill(60, 60, 192, 192); // Original blue: ready
        }

        rect(0, 0, logoZ, logoZ);
        popMatrix();

        // =========== DRAW CONTROLS ===========
        scaffoldControlLogic();

        // Original trial counter
        fill(255);
        text("Trial " + (trialIndex + 1) + " of "
                + trialCount, width / 2, inchToPix(.8f));

        // ==================================================
        // P2 CHANGE 3: Show the active mode label
        // ==================================================
        textSize(18);

        if (activeMode == MOVE_MODE) {
            fill(0, 170, 255);
            text("MOVE MODE (X/Y)", width / 2, 95);
        } else if (activeMode == SCALE_MODE) {
            fill(0, 200, 80);
            text("SCALE MODE (Z)", width / 2, 95);
        } else if (activeMode == ROTATE_MODE) {
            fill(255, 150, 0);
            text("ROTATE MODE (R)", width / 2, 95);
        } else {
            fill(220);
            text("READY", width / 2, 95);
        }

        textSize(inchToPix(.3f));
    }

    // ==================================================
    // P2 CHANGE 4: Color-code the original controls
    // and update activeMode when a control is used.
    //
    // Original movement/rotation/scale speeds unchanged.
    // ==================================================
    void scaffoldControlLogic() {

        // Orange = rotation controls
        fill(255, 150, 0);

        // Counterclockwise
        text("CCW", inchToPix(.4f), inchToPix(.4f));
        if (mousePressed &&
                dist(0, 0, mouseX, mouseY) < inchToPix(.8f)) {
            logoRotation--;
            activeMode = ROTATE_MODE;
        }

        // Clockwise
        text("CW", width - inchToPix(.4f), inchToPix(.4f));
        if (mousePressed &&
                dist(width, 0, mouseX, mouseY) < inchToPix(.8f)) {
            logoRotation++;
            activeMode = ROTATE_MODE;
        }

        // Green = scale controls
        fill(0, 200, 80);

        // Decrease size
        text("-", inchToPix(.4f), height - inchToPix(.4f));
        if (mousePressed &&
                dist(0, height, mouseX, mouseY) < inchToPix(.8f)) {
            logoZ = constrain(
                    logoZ - inchToPix(.02f),
                    (float) .01,
                    inchToPix(4f));
            activeMode = SCALE_MODE;
        }

        // Increase size
        text("+", width - inchToPix(.4f),
                height - inchToPix(.4f));
        if (mousePressed &&
                dist(width, height, mouseX, mouseY) < inchToPix(.8f)) {
            logoZ = constrain(
                    logoZ + inchToPix(.02f),
                    (float) .01,
                    inchToPix(4f));
            activeMode = SCALE_MODE;
        }

        // Blue = movement controls
        fill(0, 170, 255);

        // Move left
        text("left", inchToPix(.4f), height / 2);
        if (mousePressed &&
                dist(0, height / 2, mouseX, mouseY)
                        < inchToPix(.8f)) {
            logoX -= inchToPix(.02f);
            activeMode = MOVE_MODE;
        }

        // Move right
        text("right", width - inchToPix(.4f), height / 2);
        if (mousePressed &&
                dist(width, height / 2, mouseX, mouseY)
                        < inchToPix(.8f)) {
            logoX += inchToPix(.02f);
            activeMode = MOVE_MODE;
        }

        // Move up
        text("up", width / 2, inchToPix(.4f));
        if (mousePressed &&
                dist(width / 2, 0, mouseX, mouseY)
                        < inchToPix(.8f)) {
            logoY -= inchToPix(.02f);
            activeMode = MOVE_MODE;
        }

        // Move down
        text("down", width / 2,
                height - inchToPix(.4f));
        if (mousePressed &&
                dist(width / 2, height, mouseX, mouseY)
                        < inchToPix(.8f)) {
            logoY += inchToPix(.02f);
            activeMode = MOVE_MODE;
        }
    }

    // Original timer logic — unchanged
    public void mousePressed() {
        if (startTime == 0) {
            startTime = millis();
            // P2 TESTING CHANGE: Start the first destination timer with the original timer.
            trialStartTime = startTime;
            println("time started!");
        }
    }

    // Original trial submission logic,
    // with only the mode reset added
    public void mouseReleased() {
        // P2 TESTING CHANGE: Ignore submissions after a run has completed.
        if (userDone) {
            return;
        }

        if (dist(width / 2, height / 2,
                mouseX, mouseY) < inchToPix(3f)) {

            // P2 TESTING CHANGE: Record the existing result and its component diagnostics.
            int submittedTrial = trialIndex + 1;
            int submittedAt = millis();
            float trialSeconds = (submittedAt - trialStartTime) / 1000f;
            Destination submittedDestination = destinations.get(trialIndex);
            boolean success = checkForSuccess();
            boolean closeDist =
                    dist(submittedDestination.x, submittedDestination.y,
                            logoX, logoY) < inchToPix(.05f);
            boolean closeRotation =
                    calculateDifferenceBetweenAngles(
                            submittedDestination.rotation, logoRotation) <= 5;
            boolean closeZ =
                    abs(submittedDestination.z - logoZ) < inchToPix(.1f);

            println("Trial " + submittedTrial + "/" + trialCount
                    + ": successful=" + success
                    + ", X/Y matched=" + closeDist
                    + ", Z matched=" + closeZ
                    + ", rotation matched=" + closeRotation
                    + ", time=" + trialSeconds + " seconds");

            if (!success)
                errorCount++;

            trialIndex++;
            // P2 TESTING CHANGE: Begin timing the next destination at this submission.
            trialStartTime = submittedAt;

            // P2 CHANGE 5: Reset mode for the next trial
            activeMode = 0;

            if (trialIndex == trialCount) {
                userDone = true;
                finishTime = millis();
                // P2 TESTING CHANGE: Save and summarize only complete runs.
                recordCompletedRun();
            }
        }
    }

    // P2 TESTING CHANGE: Print completed-run metrics, append a real-results CSV row,
    // and report aggregate metrics after the tenth completed run.
    private void recordCompletedRun() {
        int successfulTrials = trialCount - errorCount;
        double accuracy = successfulTrials * 100.0 / trialCount;
        float averageTimeWithoutPenalty =
                (finishTime - startTime) / 1000f / trialCount;
        float averageTimeWithPenalty =
                (finishTime - startTime) / 1000f / trialCount
                        + (errorCount * errorPenalty);
        int runNumber = completedRunCount + 1;

        println("Run " + runNumber + " results:");
        println("Successful trials: " + successfulTrials);
        println("Errors: " + errorCount);
        println("Accuracy: " + accuracy + "%");
        println("Average time without penalty: "
                + averageTimeWithoutPenalty + " seconds per destination");
        println("Average time with penalty: "
                + averageTimeWithPenalty + " seconds per destination");

        appendRunResults(runNumber, errorCount, accuracy,
                averageTimeWithoutPenalty);

        completedRunCount++;
        accuracyTotal += accuracy;
        timeWithoutPenaltyTotal += averageTimeWithoutPenalty;

        if (completedRunCount == 10) {
            println("All 10 runs completed.");
            println("Overall average accuracy: "
                    + (accuracyTotal / completedRunCount) + "%");
            println("Overall average time without penalty: "
                    + (timeWithoutPenaltyTotal / completedRunCount)
                    + " seconds per destination");
        } else {
            println("Press N to start run " + (completedRunCount + 1) + ".");
        }
    }

    // P2 TESTING CHANGE: CSV receives a row only after a completed run.
    private void appendRunResults(int runNumber, int errors,
            double accuracy, float averageTimeWithoutPenalty) {
        Path resultsFile = Path.of("results.csv");
        try {
            boolean needsHeader = !Files.exists(resultsFile)
                    || Files.size(resultsFile) == 0;
            StringBuilder row = new StringBuilder();
            if (needsHeader) {
                row.append("run_number,errors,accuracy_percentage,average_time_without_penalty_seconds\n");
            }
            row.append(String.format(Locale.US, "%d,%d,%.2f,%.3f%n",
                    runNumber, errors, accuracy, averageTimeWithoutPenalty));
            Files.writeString(resultsFile, row.toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            println("Saved run " + runNumber + " to " + resultsFile + ".");
        } catch (IOException exception) {
            println("Could not save results.csv: " + exception.getMessage());
        }
    }

    // P2 TESTING CHANGE: N starts a fresh run without resetting the ten-run summary.
    public void keyPressed() {
        if (userDone && completedRunCount < 10
                && (key == 'n' || key == 'N')) {
            startNewRun();
        }
    }

    // P2 TESTING CHANGE: Reset gameplay/timers and create a fresh shuffled target set.
    private void startNewRun() {
        index = 0;
        trialIndex = 0;
        errorCount = 0;
        startTime = 0;
        finishTime = 0;
        trialStartTime = 0;
        userDone = false;
        logoX = 500;
        logoY = 500;
        logoZ = 50f;
        logoRotation = 0;
        activeMode = 0;
        destinations.clear();

        for (int i = 0; i < trialCount; i++) {
            Destination d = new Destination();
            d.x = random(border, width - border);
            d.y = random(border, height - border);
            d.rotation = random(0, 360);
            int j = (int) random(20);
            d.z = ((j % 12) + 1) * inchToPix(.25f);
            destinations.add(d);

            println("created target with " + d.x + ","
                    + d.y + "," + d.rotation + "," + d.z);
        }

        Collections.shuffle(destinations);
        println("Run " + (completedRunCount + 1) + " started.");
    }

    // Original success checking — completely unchanged
    public boolean checkForSuccess() {
        Destination d = destinations.get(trialIndex);

        boolean closeDist =
                dist(d.x, d.y, logoX, logoY) < inchToPix(.05f);

        boolean closeRotation =
                calculateDifferenceBetweenAngles(
                        d.rotation, logoRotation) <= 5;

        boolean closeZ =
                abs(d.z - logoZ) < inchToPix(.1f);

        println("Close Enough Distance: " + closeDist
                + " (logo X/Y = " + d.x + "/" + d.y
                + ", destination X/Y = " + logoX + "/"
                + logoY + ")");

        println("Close Enough Rotation: " + closeRotation
                + " (rot dist="
                + calculateDifferenceBetweenAngles(
                        d.rotation, logoRotation) + ")");

        println("Close Enough Z: " + closeZ
                + " (logo Z = " + d.z
                + ", destination Z = " + logoZ + ")");

        println("Close enough all: "
                + (closeDist && closeRotation && closeZ));

        return closeDist && closeRotation && closeZ;
    }

    // Original angle calculation — unchanged
    double calculateDifferenceBetweenAngles(
            float a1, float a2) {
        double diff = abs(a1 - a2);
        diff %= 90;

        if (diff > 45)
            return 90 - diff;
        else
            return diff;
    }

    // Original inches-to-pixels utility — unchanged
    float inchToPix(float inch) {
        return inch * screenPPI;
    }
}
