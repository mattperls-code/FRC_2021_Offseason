package frc.robot.autonomous;

import com.revrobotics.ControlType;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.drivetrain.Drivetrain;
import frc.robot.subsystems.misc.Limelight;
import frc.robot.subsystems.shooter.Shooter;

public class RevUsingTarget extends CommandBase {
    private Limelight limelight;
    private Drivetrain drivetrain;
    private Shooter shooter;

    // All distances in measurements in meters
    // All angles in radians

    // ! All of these should be calculated properly at some point

    // Height of the limelight off the ground
    public static double limeLightHeight = 0.3048;
    // Height of the target off the ground
    public static double targetHeight = 2.286;
    // Angle the limeLight is mounted to the robot at
    public static double limeLightMountingAngle = 50 * Math.PI / 180;
    // Angle the shooter releases the ball at
    public static double shooterReleaseAngle = 50.0 * Math.PI / 180;
    // Radius of the flywheels used to shoot
    public static double shooterFlywheelRadius = 0.0635;
    // Hom much should the robot move backward each update cycle if the robot is too close to be able to hit the target
    public static double seekAdjustment = -0.05;

    public RevUsingTarget(Limelight limelight, Drivetrain drivetrain, Shooter shooter){
        this.limelight = limelight;
        this.drivetrain = drivetrain;
        this.shooter = shooter;
        addRequirements(drivetrain, shooter);
    }

    // Called on update cycles where the limelight has a target
    void revWithTarget(){
        /*
            given: ∆x, ∆y, θ
            solve for: v

            ∆x = v * cos θ * t
            ∆y = v * sin θ * t - 4.9 * t ^ 2
            t = ∆x / (v * cos θ)
            ∆y = tan θ * ∆x - 4.9 * (∆x / (v * cos θ)) ^ 2
            4.9 * (∆x / (v * cos θ)) ^ 2 = tan θ * ∆x - ∆y
            (∆x / (v * cos θ)) ^ 2 = (tan θ * ∆x - ∆y) / 4.9
            ∆x / (v * cos θ) = sqrt((tan θ * ∆x - ∆y) / 4.9)
            ∆x = (v * cos θ) * sqrt((tan θ * ∆x - ∆y) / 4.9)
            v = ∆x / (cos θ * sqrt((tan θ * ∆x - ∆y) / 4.9))
        */

        double deltaY = RevUsingTarget.targetHeight - RevUsingTarget.limeLightHeight;
        double deltaX = deltaY / Math.tan(RevUsingTarget.limeLightMountingAngle + limelight.getYOffset() * Math.PI / 180);

        double targetReleaseVelocity = deltaX / (Math.cos(RevUsingTarget.shooterReleaseAngle) * Math.sqrt((Math.tan(RevUsingTarget.shooterReleaseAngle * deltaX - deltaY)) / 4.9));
        if(Double.isNaN(targetReleaseVelocity)){
            SmartDashboard.putNumber("Target Release Velocity", 0);
            SmartDashboard.putNumber("Target RPM", 0);
            this.drivetrain.getLeftMainMotor().set(RevUsingTarget.seekAdjustment);
            this.drivetrain.getRightMainMotor().set(RevUsingTarget.seekAdjustment);
        } else {
            SmartDashboard.putNumber("Target Release Velocity", 0);
            /*
                v = r * ω
                ω = v / r
                ω = ∆θ / t
                v / r = ∆θ / t
                ∆θ = v * t / r
                rotations = ∆θ / 2π
                rotations = 2π * v * t / r
                rpm = 60 * 2π * v / r
            */
            double targetRPM = 60 * 2 * Math.PI * targetReleaseVelocity / RevUsingTarget.shooterFlywheelRadius;
            SmartDashboard.putNumber("Target Release Velocity", targetReleaseVelocity);
            SmartDashboard.putNumber("Target RPM", targetRPM);
            this.shooter.getPidController().setReference(targetRPM, ControlType.kVelocity);
        }
    }

    @Override
    public void execute(){
        if(limelight.hasTarget()){
            this.revWithTarget();
        }
    }

    @Override
    public boolean isFinished(){
        // how to read the current rpm
        return true;
    }
}