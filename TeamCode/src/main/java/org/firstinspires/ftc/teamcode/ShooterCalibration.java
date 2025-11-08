package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp
@Config
public class ShooterCalibration extends LinearOpMode {
    // 1 rotation = 28 ticks
    // tps = rpm * 28 * 60
    public static int startTPS = 1200; // ticks per second
    public static int changeTPS = 100;
    @Override
    public void runOpMode() throws InterruptedException {
        DcMotorEx shooter = hardwareMap.get(DcMotorEx.class, "shooterMotor");
        int tps = startTPS;

        waitForStart();

        while (opModeIsActive()) {
            if      (gamepad1.dpadUpWasPressed())   tps += changeTPS;
            else if (gamepad1.dpadDownWasPressed()) tps -= changeTPS;
            else if (gamepad1.aWasPressed()) tps = -tps;

            shooter.setVelocity(tps);

            telemetry.addData("target tps", tps);
            telemetry.addData("actual tps", shooter.getVelocity());
            telemetry.update();
        }
    }
}
