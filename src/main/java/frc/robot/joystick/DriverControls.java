package frc.robot.joystick;

import java.nio.ByteBuffer;

import com.google.flatbuffers.FlatBufferBuilder;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.Constants.ControllerConstants.ControllerConfig1;
import frc.taurus.config.ChannelManager;
import frc.taurus.config.Config;
import frc.taurus.drivetrain.generated.DrivetrainGoal;
import frc.taurus.drivetrain.generated.GoalType;
import frc.taurus.drivetrain.generated.TeleopGoal;
import frc.taurus.joystick.Controller;
import frc.taurus.joystick.SteeringMethods;
import frc.taurus.joystick.XboxController;
import frc.taurus.messages.MessageQueue;
import io.github.oblarg.oblog.annotations.Log;

/**
 * This file defines the user controls / button mappings
 */

public class DriverControls {

  final XboxController driverController;
  final SteeringMethods steeringMethods;
  final MessageQueue<ByteBuffer> drivetrainGoalQueue;
  SteeringMethods.LeftRightMotor lrMotor;

  public DriverControls(ChannelManager channelManager, Joystick joystick) {

    driverController = new XboxController(channelManager.fetchJoystickStatusQueue(joystick.getPort()),
                                          channelManager.fetchJoystickGoalQueue(joystick.getPort()));  
    steeringMethods = new SteeringMethods(ControllerConfig1.kDriveDeadband, ControllerConfig1.kDriveNonLinearity,
                                          ControllerConfig1.kDriveDeadband, ControllerConfig1.kDriveNonLinearity);
    drivetrainGoalQueue = channelManager.fetch(Config.DRIVETRAIN_GOAL);

  }

  public void update() {
    driverController.update();  // read in all raw axes & buttons
    double throttle = driverController.getAxis(XboxController.Axis.L_STICK_Y_AXIS);
    double steering = driverController.getAxis(XboxController.Axis.L_STICK_X_AXIS);
    lrMotor = steeringMethods.arcadeDrive(throttle, steering);
    writeDrivetrainGoalMessage();
  }

  public double getLeft()       { return lrMotor.left; }
  public double getRight()      { return lrMotor.right; }
  public boolean getQuickTurn() { return false; }
  public boolean getLowGear()   { return false; }
  // don't add controls here for anything not related to simply moving the drivetrain around the field
  // most controls (even if they are mapped to the driver's joystick) should be in SuperstructureControls 

  // for SuperstructureControls
  public Controller getController() {
    return driverController;
  }

  public void setRumble(RumbleType rumbleType, double rumbleValue) { 
    driverController.setRumble(rumbleType, rumbleValue); 
  }


  @Log.NumberBar(name = "Left Motor")
  float lMotor;
  @Log.NumberBar(name = "Right Motor")
  float rMotor;
  boolean quickTurn;
  boolean lowGear;  
  
  int bufferSize = 0;

  public void writeDrivetrainGoalMessage() {
    // send a DrivetrainGoal message
    lMotor = (float)getLeft();
    rMotor = (float)getRight();
    quickTurn = getQuickTurn();
    lowGear = getLowGear();    

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
