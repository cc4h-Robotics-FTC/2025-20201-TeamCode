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
public class DucksTeleOpExp extends LinearOpMode {
    public static double shooterSpinUpTime = 3.0;
    public static double shooterSpinDownTime = 5.0;
    public static double shooterSpinUpPower = 1.0;
    public static double shooterSpinDownPower = 0.0;

    public static int shooterStartTPS = 1200;
    public static int shooterChangeTPS = 100;
    public static int intakeStartTPS = 2000;
    public static int intakeChangeTPS = 100;
    public static int transferStartTPS = 2000;
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

    double moveSens = 1.0;
    double turnSens = 1.0;

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

        frontLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        shooterMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        transferMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        shooterMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        transferMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        Shooter shooter = new Shooter(intakeMotor, transferMotor, shooterMotor);

        int shooterTPS = shooterStartTPS;
        int intakeTPS = intakeStartTPS;
        int transferTPS = transferStartTPS;
        boolean shooterArmed = false;
        boolean intakeArmed = false;
        boolean transferArmed = false;

        waitForStart();
        if (isStopRequested()) return;
        while (opModeIsActive()) {
            drive();

            if (gamepad1.dpadUpWasPressed()) { shooterTPS += shooterChangeTPS; }
            else if (gamepad1.dpadDownWasPressed()) { shooterTPS -= shooterChangeTPS; }
            else if (gamepad1.dpadLeftWasPressed()) { shooterTPS = -shooterTPS; }
            else if (gamepad1.dpadRightWasPressed()) { shooterArmed = !shooterArmed; }

//            shooterMotor.setVelocity(shooterTPS * (shooterArmed ? 1: 0));
//            if (gamepad1.left_bumper && gamepad1.right_bumper) shooterMotor.setPower(1.0);
//            intakeMotor.setPower((gamepad1.cross ? 1 : 0) - (gamepad1.square ? 1 : 0));
//            transferMotor.setPower((gamepad1.circle ? 1 : 0) - (gamepad1.triangle ? 1 : 0));\

            shooter.shoot(gamepad1.left_bumper, gamepad1.right_bumper, shooterTPS);

            if (shooterMotor.getVelocity() > shooterTPS - 200) gamepad1.rumble(100);
            else gamepad1.stopRumble();


            telemetry.addData("shooter target tps", shooterTPS);
            telemetry.addData("transfer target tps", transferTPS);
            telemetry.addData("intake target tps", intakeTPS);

            telemetry.addData("shooter actual tps", shooterMotor.getVelocity());
            telemetry.addData("transfer actual tps", transferMotor.getVelocity());
            telemetry.addData("intake actual tps", intakeMotor.getVelocity());

            telemetry.addData("shooter armed", shooterArmed);
            telemetry.addData("transfer armed", transferArmed);
            telemetry.addData("intake armed", intakeArmed);

            telemetry.addData("shooter state", shooter.state);
            telemetry.addData("shooter start", gamepad1.left_bumper);
            telemetry.addData("shooter stop", gamepad1.right_bumper);

            telemetry.addData("chassis move", moveSens);
            telemetry.addData("chassis turn", turnSens);

            telemetry.update();
        }
    }
    private void drive() {
        if (gamepad2.dpadUpWasPressed()) moveSens += 0.05;
        if (gamepad2.dpadDownWasPressed()) moveSens -= 0.05;
        if (gamepad2.triangleWasPressed()) turnSens += 0.05;
        if (gamepad2.crossWasPressed()) turnSens -= 0.05;

        double y = -gamepad2.left_stick_y * moveSens; // Remember, Y stick value is reversed
        double x = gamepad2.left_stick_x * 1.1 * moveSens; // Counteract imperfect strafing
        double rx = gamepad2.right_stick_x * turnSens;

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
                if (now) {
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
