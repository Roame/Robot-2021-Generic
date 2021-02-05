package frc.robot.joystick;

/**
 * The user controls beyond basic drivetrain motion.
 * These will change for every new game.
 */

public interface ISuperstructureControls {
    
    // change these functions as needed
    boolean getAutoAim();
    boolean getIntake();
    boolean getShoot();
    boolean getClimb();
    boolean getTurnNorth();

}