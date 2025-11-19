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
    public int tps;
    public int shots;

    private DcMotorEx intakeMotor;
    private DcMotorEx transferMotor;
    private DcMotorEx shooterMotor;
    public Shooter(DcMotorEx intakeMotor, DcMotorEx transferMotor, DcMotorEx shooterMotor, int tps) {
        this.state = States.IDLE;
        this.tps = tps;
        this.shots = 0;
        this.intakeMotor = intakeMotor;
        this.transferMotor = transferMotor;  
        this.shooterMotor = shooterMotor;
    }

    public void shoot(boolean start, boolean stop) {
        switch (this.state) {
            case IDLE:
                if (start) this.state = States.SPIN_UP; this.shots = 0;
            case SPIN_UP:
                shooterMotor.setVelocity(this.tps);
                if (shooterMotor.getVelocity() > this.tps - 200) state = States.FEED;
            case FEED:
                if (shooterMotor.getVelocity() > this.tps - 200) {
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
