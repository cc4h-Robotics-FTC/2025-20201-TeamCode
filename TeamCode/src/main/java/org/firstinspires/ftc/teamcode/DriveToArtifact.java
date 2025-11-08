package org.firstinspires.ftc.teamcode;

import android.graphics.Color;
import android.util.Size;

import com.arcrobotics.ftclib.drivebase.MecanumDrive;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.opencv.Circle;
import org.firstinspires.ftc.vision.opencv.ColorBlobLocatorProcessor;
import org.firstinspires.ftc.vision.opencv.ColorRange;
import org.firstinspires.ftc.vision.opencv.ImageRegion;

import java.util.List;

@Autonomous
public class DriveToArtifact extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        MotorEx frontLeftMotor = new MotorEx(hardwareMap, "frontLeftMotor");
        MotorEx frontRightMotor = new MotorEx(hardwareMap, "frontRightMotor");
        MotorEx backLeftMotor = new MotorEx(hardwareMap, "backLeftMotor");
        MotorEx backRightMotor = new MotorEx(hardwareMap, "backRightMotor");

        frontLeftMotor.setInverted(true);
        backLeftMotor.setInverted(true);

        MecanumDrive drive = new MecanumDrive(frontLeftMotor, frontRightMotor, backLeftMotor, backRightMotor);

        ColorBlobLocatorProcessor purpleColorLocator = new ColorBlobLocatorProcessor.Builder()
                .setTargetColorRange(ColorRange.ARTIFACT_PURPLE)   // Use a predefined color match
                .setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)
                .setRoi(ImageRegion.asUnityCenterCoordinates(-1, 1, 1, -1))
                .setDrawContours(true)   // Show contours on the Stream Preview
                .setBoxFitColor(0)       // Disable the drawing of rectangles
                .setCircleFitColor(Color.rgb(255, 255, 0)) // Draw a circle
                .setBlurSize(5)          // Smooth the transitions between different colors in image

                // the following options have been added to fill in perimeter holes.
                .setDilateSize(15)       // Expand blobs to fill any divots on the edges
                .setErodeSize(15)        // Shrink blobs back to original size
                .setMorphOperationType(ColorBlobLocatorProcessor.MorphOperationType.CLOSING)

                .build();

        ColorBlobLocatorProcessor greenColorLocator = new ColorBlobLocatorProcessor.Builder()
                .setTargetColorRange(ColorRange.ARTIFACT_GREEN)   // Use a predefined color match
                .setContourMode(ColorBlobLocatorProcessor.ContourMode.EXTERNAL_ONLY)
                .setRoi(ImageRegion.asUnityCenterCoordinates(-1, 1, 1, -1))
                .setDrawContours(true)   // Show contours on the Stream Preview
                .setBoxFitColor(0)       // Disable the drawing of rectangles
                .setCircleFitColor(Color.rgb(255, 255, 0)) // Draw a circle
                .setBlurSize(5)          // Smooth the transitions between different colors in image

                // the following options have been added to fill in perimeter holes.
                .setDilateSize(15)       // Expand blobs to fill any divots on the edges
                .setErodeSize(15)        // Shrink blobs back to original size
                .setMorphOperationType(ColorBlobLocatorProcessor.MorphOperationType.CLOSING)

                .build();

        VisionPortal portal = new VisionPortal.Builder()
                .addProcessor(purpleColorLocator)
                .addProcessor(greenColorLocator)
                .setCameraResolution(new Size(320, 240))
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .build();

        telemetry.setMsTransmissionInterval(100);   // Speed up telemetry updates for debugging.
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.MONOSPACE);

        int selectedBlob = 0;

        waitForStart();
        while (opModeIsActive()) {
            List<ColorBlobLocatorProcessor.Blob> blobs = purpleColorLocator.getBlobs();
            blobs.addAll(greenColorLocator.getBlobs());

            ColorBlobLocatorProcessor.Util.filterByCriteria(
                    ColorBlobLocatorProcessor.BlobCriteria.BY_CONTOUR_AREA,
                    50, 20000, blobs);  // filter out very small blobs.

//            ColorBlobLocatorProcessor.Util.filterByCriteria(
//                    ColorBlobLocatorProcessor.BlobCriteria.BY_CIRCULARITY,
//                    0.6, 1, blobs);     // filter out non-circular blobs.

            telemetry.addLine("I Circularity Radius Center");

            // Display the Blob's circularity, and the size (radius) and center location of its circleFit.
            for (int i = 0; i < blobs.size(); i++) {
                ColorBlobLocatorProcessor.Blob b = blobs.get(i);
                Circle circleFit = b.getCircle();
                telemetry.addLine(String.format("%s %1d %5.3f      %3d     (%3d,%3d)",
                        i != selectedBlob ? " " : "*", i, b.getCircularity(), (int) circleFit.getRadius(), (int) circleFit.getX(), (int) circleFit.getY()));
            }

            double forwardSpeed = 0.0;
            double turnSpeed = 0.0;
            if (!blobs.isEmpty()) {
                ColorBlobLocatorProcessor.Blob b = blobs.get(0);
                if (b.getCircle().getX() > 150 + b.getCircle().getRadius()) {
                    forwardSpeed = 0.0;
                    turnSpeed = 0.3;
                } else if (b.getCircle().getX() < 150 + b.getCircle().getRadius()) {
                    forwardSpeed = 0.0;
                    turnSpeed = -0.3;
                } else {
                    forwardSpeed = 0.5;
                    turnSpeed = 0.0;
                }
            }

//            frontLeftMotor.set(forwardSpeed);
//            frontRightMotor.set(turnSpeed);

            drive.driveRobotCentric(0.0, forwardSpeed, turnSpeed);

            telemetry.update();
            sleep(100); // Match the telemetry update interval.
        }
    }
}
