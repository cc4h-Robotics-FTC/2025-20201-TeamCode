package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotorEx;

public class Shooter {
    public enum States {
        IDLE,
        SPIN_UP,
        FEED,
        SPIN_DOWN
    }
    public States state;
    public int shots;

    private DcMotorEx intakeMotor;
    private DcMotorEx transferMotor;
    private DcMotorEx shooterMotor;
    public Shooter(DcMotorEx intakeMotor, DcMotorEx transferMotor, DcMotorEx shooterMotor) {
        this.state = States.IDLE;
        this.shots = 0;
        this.intakeMotor = intakeMotor;
        this.transferMotor = transferMotor;  
        this.shooterMotor = shooterMotor;
    }

    public void shoot(boolean start, boolean stop, int tps) {
        switch (this.state) {
            case IDLE:
                if (start) this.state = States.SPIN_UP; this.shots = 0;
            case SPIN_UP:
                intakeMotor.setPower(-0.5);
                transferMotor.setPower(-0.5);
                shooterMotor.setVelocity(tps);
                if (shooterMotor.getVelocity() > tps - 200) {
                    state = States.FEED;
                    intakeMotor.setPower(0);
                    transferMotor.setPower(0);
                }
            case FEED:
                if (shooterMotor.getVelocity() > tps - 200) {
                    transferMotor.setPower(1);
                    intakeMotor.setPower(1);
                } else {
                    transferMotor.setPower(0);
                    intakeMotor.setPower(0);
                    this.shots++;
                    this.state = States.SPIN_UP;
                }
                if (stop) this.state = States.SPIN_DOWN;
            case SPIN_DOWN:
                shooterMotor.setVelocity(0);
        }
    }
}
