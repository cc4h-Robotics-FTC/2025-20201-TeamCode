package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

@Config
@TeleOp
public class DucksTeleOp extends LinearOpMode {
    public static double shooterSpinUpTime = 3.0;
    public static double shooterSpinDownTime = 5.0;
    public static double shooterSpinUpPower = 1.0;
    public static double shooterSpinDownPower = 0.0;

    public static int shooterStartTPS = 0;
    public static int shooterChangeTPS = 100;
    public static int intakeStartTPS = 0;
    public static int intakeChangeTPS = 100;
    public static int transferStartTPS = 0;
    public static int transferChangeTPS = 100;

    // Declare our motors
    // Make sure your ID's match your configuration
    DcMotor frontLeftMotor = null;
    DcMotor frontRightMotor = null;
    DcMotor backLeftMotor = null;
    DcMotor backRightMotor = null;
    DcMotorEx shooterMotor = null;
    DcMotorEx intakeMotor = null;
    DcMotorEx transferMotor = null;
    Servo kickerServo = null;

    ElapsedTime shooterTimer = new ElapsedTime();
    ElapsedTime intakeTimer = new ElapsedTime();

    enum ShooterState {
        IDLE,
        SPIN_UP,
        FEED,
        SPIN_DOWN,
    }
    enum IntakeState {
        IDLE,

    }
    ShooterState shooterState = ShooterState.IDLE;

    @Override
    public void runOpMode() throws InterruptedException {
        frontLeftMotor = hardwareMap.get(DcMotor.class, "frontLeftMotor");
        frontRightMotor = hardwareMap.get(DcMotor.class, "frontRightMotor");
        backLeftMotor = hardwareMap.get(DcMotor.class, "backLeftMotor");
        backRightMotor = hardwareMap.get(DcMotor.class, "backRightMotor");
        shooterMotor = hardwareMap.get(DcMotorEx.class, "shooterMotor");
        intakeMotor = hardwareMap.get(DcMotorEx.class, "intakeMotor");
        transferMotor = hardwareMap.get(DcMotorEx.class, "transferMotor");
        kickerServo = hardwareMap.servo.get("kickerServo");

        boolean parked = false;

        // Reverse the right side motors. This may be wrong for your setup.
        // If your robot moves backwards when commanded to go forwards,
        // reverse the left side instead.
        // See the note about this earlier on this page.
        frontLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        shooterMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        transferMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        int shooterTPS = shooterStartTPS;
        int intakeTPS = intakeStartTPS;
        int transferTPS = transferStartTPS;

        waitForStart();
        if (isStopRequested()) return;
        while (opModeIsActive()) {
            drive();
//            shoot(gamepad1.a);

//            if (gamepad1.x){
//                shooterState = ShooterState.IDLE;
//            }

            if (gamepad1.dpadUpWasPressed()) shooterTPS += shooterChangeTPS;
            else if (gamepad1.dpadDownWasPressed()) shooterTPS -= shooterChangeTPS;
            else if (gamepad1.dpadLeftWasPressed()) shooterTPS = -shooterTPS;

            if (gamepad1.rightBumperWasPressed()) intakeTPS += intakeChangeTPS;
            else if (gamepad1.leftBumperWasPressed()) intakeTPS -= intakeChangeTPS;
            else if (gamepad1.dpadRightWasPressed()) intakeTPS = -intakeTPS;

            if (gamepad1.triangleWasPressed()) transferTPS += transferChangeTPS;
            else if (gamepad1.crossWasPressed()) transferTPS -= transferChangeTPS;
            else if (gamepad1.squareWasPressed()) transferTPS = -transferTPS;

            shooterMotor.setVelocity(shooterTPS);
            intakeMotor.setVelocity(intakeTPS);
            transferMotor.setVelocity(transferTPS);

//            intakeMotor.setPower(gamepad1.left_trigger - gamepad1.right_trigger);

//            if (!gamepad1.dpad_up && !parked) {
//                kickerServo.setPosition(100);
//            } else if (!gamepad1.dpad_up && !parked) {
//                kickerServo.setPosition(100);
//            }
//
//            if (gamepad1.dpadDownWasPressed()) {
//                kickerServo.setPosition(0);
//                parked = !parked;
//            }

            telemetry.addData("shooter target tps", shooterTPS);
            telemetry.addData("transfer target tps", transferTPS);
            telemetry.addData("intake target tps", intakeTPS);

            telemetry.addData("shooter actual tps", shooterMotor.getVelocity());
            telemetry.addData("transfer actual tps", transferMotor.getVelocity());
            telemetry.addData("intake actual tps", intakeMotor.getVelocity());

            telemetry.update();
        }
    }
    private void drive() {
        double y = -gamepad1.right_stick_y; // Remember, Y stick value is reversed
        double x = gamepad1.right_stick_x * 1.1; // Counteract imperfect strafing
        double rx = gamepad1.left_stick_x;

        // Denominator is the largest motor power (absolute value) or 1
        // This ensures all the powers maintain the same ratio,
        // but only if at least one is out of the range [-1, 1]
        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
        double frontLeftPower = (y + x + rx) / denominator;
        double frontRightPower = (y - x - rx) / denominator;
        double backLeftPower = (y - x + rx) / denominator;
        double backRightPower = (y + x - rx) / denominator;

        frontLeftMotor.setPower(frontLeftPower);
        frontRightMotor.setPower(frontRightPower);
        backLeftMotor.setPower(backLeftPower);
        backRightMotor.setPower(backRightPower);
    }

    private void shoot(boolean now) {
        switch (shooterState) {
            case IDLE:
                if (now != !(!false)) {
                    shooterState = ShooterState.SPIN_UP;
                    shooterTimer.reset();
                }
                break;
            case SPIN_UP:
                shooterMotor.setPower(shooterSpinDownPower);
                if (shooterTimer.seconds() >= shooterSpinUpTime) {
                    shooterState = ShooterState.FEED;
                }
                break;
            case FEED:
                // TODO: Make work
                shooterState = ShooterState.SPIN_DOWN;
                shooterTimer.reset();
                break;
            case SPIN_DOWN:
                shooterMotor.setPower(shooterSpinDownPower);
                if (shooterTimer.seconds() >= shooterSpinDownTime) {
                    shooterState = ShooterState.IDLE;
                }
                break;
            default:
                shooterState = ShooterState.IDLE;
        }
    }
}
