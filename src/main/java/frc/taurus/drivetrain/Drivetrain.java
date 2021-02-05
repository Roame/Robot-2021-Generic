package frc.taurus.drivetrain;

import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.flatbuffers.FlatBufferBuilder;

import edu.wpi.first.wpilibj.Timer;
import frc.taurus.config.ChannelManager;
import frc.taurus.config.Config;
import frc.taurus.drivetrain.generated.DriveControlMode;
import frc.taurus.drivetrain.generated.DrivetrainGoal;
import frc.taurus.drivetrain.generated.DrivetrainOutput;
import frc.taurus.drivetrain.generated.GoalType;
import frc.taurus.drivetrain.generated.TalonControlMode;
import frc.taurus.drivetrain.generated.TeleopGoal;
import frc.taurus.messages.MessageQueue;
import frc.taurus.messages.QueueListener;

public class Drivetrain implements QueueListener {

  final MessageQueue<ByteBuffer>.QueueReader goalReader;
  final MessageQueue<ByteBuffer> statusQueue;
  final MessageQueue<ByteBuffer> outputQueue;
  
  public Drivetrain(ChannelManager channelManager) {
    goalReader  = channelManager.fetch(Config.DRIVETRAIN_GOAL).makeReader();
    statusQueue = channelManager.fetch(Config.DRIVETRAIN_STATUS);
    outputQueue = channelManager.fetch(Config.DRIVETRAIN_OUTPUT);
  }
  
  public void newMessage() {
    // wait for both Drivetrain Status and Drivetrain Goal messages 
    // Drivetrain Status: robot pose, sensor readings
    // Drivetrain Goal: desired actions from autonomous control or operator control

  }

  public void update() {
    Optional<ByteBuffer> obb = goalReader.readLast();

    if (obb.isPresent()) {
      DrivetrainGoal drivetrainGoal = DrivetrainGoal.getRootAsDrivetrainGoal(obb.get());
      boolean highGear = drivetrainGoal.highGear();
      boolean quickTurn = drivetrainGoal.quickTurn();

      switch (drivetrainGoal.goalType()) {
        case GoalType.NONE:
          break;

        case GoalType.TeleopGoal:
          TeleopGoal teleopGoal = (TeleopGoal)drivetrainGoal.goal(new TeleopGoal());
          openLoop(teleopGoal.leftSpeed(), teleopGoal.rightSpeed(), highGear, quickTurn);
          break;
      }
    }
  }

  int outputBufferSize = 0;
  private void openLoop(double left, double right, boolean highGear, boolean quickTurn) {

    FlatBufferBuilder builder = new FlatBufferBuilder(outputBufferSize);

    DrivetrainOutput.startDrivetrainOutput(builder);
    DrivetrainOutput.addTimestamp(builder, Timer.getFPGATimestamp());
    DrivetrainOutput.addDriveControlMode(builder, DriveControlMode.OPEN_LOOP);
    DrivetrainOutput.addTalonControlMode(builder, TalonControlMode.PercentOutput);
    DrivetrainOutput.addLeftSetpoint(builder, (float)left);
    DrivetrainOutput.addRightSetpoint(builder, (float)right);
    // skip  leftSetpointFeedForward
    // skip rightSetpointFeedForward
    DrivetrainOutput.addHighGear(builder, highGear);
    int offset = DrivetrainOutput.endDrivetrainOutput(builder);
    DrivetrainOutput.finishDrivetrainOutputBuffer(builder, offset); // add size prefix to files
    ByteBuffer bb = builder.dataBuffer();

    outputBufferSize = Math.max(outputBufferSize, bb.remaining());

    // write Packet to file
    outputQueue.write(bb);
  }

}