package frc.robot.joystick;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.Joystick;
import frc.taurus.config.ChannelManager;
import frc.taurus.joystick.ButtonBoardController;
import frc.taurus.joystick.Controller;
import frc.taurus.joystick.XboxController;

/**
 * The user controls beyond basic drivetrain motion. These will change for every
 * new game.
 */

public class SuperstructureControls implements ISuperstructureControls {

  private final Controller.Button shootButton;
  private final Controller.Button autoAimButton;

  private final ButtonBoardController buttonBoard;
  private final Controller.Button climbButton;
  private final Controller.PovButton turnNorthPovButton;
  private final Controller.PovButton turnSouthPovButton;
  private final Controller.AxisButton intakeAxisButton;

  public SuperstructureControls(ChannelManager channelManager, Joystick joystick, Controller driverController) {

    // do not create another XboxController for the driver -- it is only here to make buttons
    buttonBoard = new ButtonBoardController(channelManager.fetchJoystickStatusQueue(joystick.getPort()),
                                            channelManager.fetchJoystickGoalQueue(joystick.getPort()));

    shootButton = driverController.addButton(XboxController.Button.X.id);
    autoAimButton = driverController.addButton(XboxController.Button.Y.id);
    intakeAxisButton = driverController.addAxisButton(XboxController.Axis.R_TRIGGER_AXIS.id, 0.5);

    climbButton = buttonBoard.addButton(ButtonBoardController.Button.SR.id);
    turnNorthPovButton = buttonBoard.addPovButton(0, -45, 45);
    turnSouthPovButton = buttonBoard.addPovButton(0, 135, 215);
  }

  // Driver has control of Intaking, Aiming and Shooting
  public boolean getIntake()  { return intakeAxisButton.isPressed(); }
  public boolean getAutoAim() { return autoAimButton.isPressed(); }
  public boolean getShoot()   { return shootButton.isPressed(); }

  // Operator has control of Climbing and Turning
  public boolean getClimb()     { return climbButton.negEdge(); }
  public boolean getTurnNorth() { return turnNorthPovButton.posEdge(); }
  public boolean getTurnSouth() { return turnSouthPovButton.posEdge(); }


  public void setRumble(RumbleType rumbleType, double rumbleValue) { 
    buttonBoard.setRumble(rumbleType, rumbleValue); 
  }

  public void update() {
    buttonBoard.update();  // read in all raw axes & buttons
    // TODO: generate one or several Goal messages based on user controls
  }



  int bufferSize = 0;

  public void writeSuperstructureGoalMessage() {
    // send a SuperstructureGoal message
    // or maybe multiple Goal messages for each subsystem
  }  
}