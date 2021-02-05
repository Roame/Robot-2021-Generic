package frc.robot.joystick.examples;

import java.nio.ByteBuffer;

import com.google.flatbuffers.FlatBufferBuilder;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.Constants.ControllerConstants.ControllerConfig2;
import frc.taurus.config.ChannelManager;
import frc.taurus.config.Config;
import frc.taurus.drivetrain.generated.DrivetrainGoal;
import frc.taurus.drivetrain.generated.GoalType;
import frc.taurus.drivetrain.generated.TeleopGoal;
import frc.taurus.joystick.Controller;
import frc.taurus.joystick.SteeringMethods;
import frc.taurus.joystick.ThrustmasterController;
import frc.taurus.messages.MessageQueue;

/**
 * This file defines the user controls / button mappings
 */

public class DriverControlsDualThrustmasterExample {

  final ThrustmasterController leftController;
  final ThrustmasterController rightController;
  final SteeringMethods steeringMethods;
  final MessageQueue<ByteBuffer> drivetrainGoalQueue;
  SteeringMethods.LeftRightMotor lrMotor;

  public DriverControlsDualThrustmasterExample(ChannelManager channelManager, Joystick lJoystick, Joystick rJoystick) {

     leftController = new ThrustmasterController(channelManager.fetchJoystickStatusQueue(lJoystick.getPort()),
                                                 channelManager.fetchJoystickGoalQueue(lJoystick.getPort()));   
    rightController = new ThrustmasterController(channelManager.fetchJoystickStatusQueue(rJoystick.getPort()),
                                                 channelManager.fetchJoystickGoalQueue(rJoystick.getPort()));   
    steeringMethods = new SteeringMethods(ControllerConfig2.kDriveDeadband, ControllerConfig2.kDriveNonLinearity,
                                          ControllerConfig2.kDriveDeadband, ControllerConfig2.kDriveNonLinearity);
    drivetrainGoalQueue = channelManager.fetch(Config.DRIVETRAIN_GOAL);
  }

  public void update() {
     leftController.update();  // read in all raw axes & buttons
    rightController.update();  // read in all raw axes & buttons
    double throttle = leftController.getAxis(ThrustmasterController.Axis.Y_AXIS);
    double steering = rightController.getAxis(ThrustmasterController.Axis.X_AXIS);
    lrMotor = steeringMethods.arcadeDrive(throttle, steering);
    writeDrivetrainGoalMessage();
  }

  public double getLeft()       { return lrMotor.left; }
  public double getRight()      { return lrMotor.right; }
  public boolean getQuickTurn() { return false; }
  public boolean getLowGear()   { return false; }
  // don't add controls here for anything not related to simply moving the
  // drivetrain around the field
  // most controls (even if they are mapped to the driver's joystick) should be in
  // SuperstructureControls

  // for SuperstructureControls
  public Controller getLeftController() {
    return leftController;
  }
  // for SuperstructureControls
  public Controller getDriverController() {
    return rightController;
  }

  public void setRumble(RumbleType rumbleType, double rumbleValue) { 
     leftController.setRumble(rumbleType, rumbleValue); 
    rightController.setRumble(rumbleType, rumbleValue); 
  }

    
  int bufferSize = 0;

  public void writeDrivetrainGoalMessage() {
    // send a DrivetrainGoal message
    float lMotor = (float) getLeft();
    float rMotor = (float) getRight();
    boolean quickTurn = getQuickTurn();
    boolean lowGear = getLowGear();

    FlatBufferBuilder builder = new FlatBufferBuilder(bufferSize);
    int teleopGoalOffset = TeleopGoal.createTeleopGoal(builder, lMotor, rMotor);
    double timestamp = Timer.getFPGATimestamp();
    int offset = DrivetrainGoal.createDrivetrainGoal(builder, timestamp, GoalType.TeleopGoal, teleopGoalOffset, !lowGear, quickTurn);
    builder.finish(offset);
    ByteBuffer bb = builder.dataBuffer();
    bufferSize = Math.max(bufferSize, bb.remaining());

    drivetrainGoalQueue.write(bb);
  }
}