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

    private DcMotorEx intakeMotor;
    private DcMotorEx transferMotor;
    private DcMotorEx shooterMotor;
    public Shooter(DcMotorEx intakeMotor, DcMotorEx transferMotor, DcMotorEx shooterMotor) {
        this.state = States.IDLE;
        this.intakeMotor = intakeMotor;
        this.transferMotor = transferMotor;  
        this.shooterMotor = shooterMotor;
    }


}
