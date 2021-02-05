package frc.taurus.driverstation;

import java.nio.ByteBuffer;

import com.google.flatbuffers.FlatBufferBuilder;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import frc.taurus.config.ChannelManager;
import frc.taurus.config.Config;
import frc.taurus.driverstation.generated.DriverStationStatus;
import frc.taurus.messages.MessageQueue;


// takes data from DriverStation and places it in MessageQueue

public class DriverStationData {
  DriverStation ds;
  MessageQueue<ByteBuffer> statusQueue;

  public DriverStationData(DriverStation driverStation, ChannelManager channelManager) {
    this.ds = driverStation;
    this.statusQueue = channelManager.fetch(Config.DRIVER_STATION_STATUS);
  }

  
  public void update() {
    writeDriverStationStatusMessage();
  }
  

  int bufferSize = 0;

  private void writeDriverStationStatusMessage() {
    FlatBufferBuilder builder = new FlatBufferBuilder(bufferSize);

    int gameSpecificMessageOffset = builder.createString(ds.getGameSpecificMessage());

    DriverStationStatus.startDriverStationStatus(builder);
    DriverStationStatus.addTimestamp(builder, Timer.getFPGATimestamp());
    DriverStationStatus.addEnabled(builder, ds.isEnabled());
    DriverStationStatus.addAutonomous(builder, ds.isAutonomous());
    DriverStationStatus.addTeleop(builder, ds.isOperatorControl());
    DriverStationStatus.addTest(builder, ds.isTest());
    DriverStationStatus.addDsAttached(builder, ds.isDSAttached());
    DriverStationStatus.addFmsAttached(builder, ds.isFMSAttached());
    DriverStationStatus.addGameSpecificMessage(builder, gameSpecificMessageOffset);
    DriverStationStatus.addMatchTime(builder, ds.getMatchTime());
    int offset = DriverStationStatus.endDriverStationStatus(builder);
    DriverStationStatus.finishDriverStationStatusBuffer(builder, offset);
    ByteBuffer bb = builder.dataBuffer();

    bufferSize = Math.max(bufferSize, bb.remaining());   
    
    statusQueue.write(bb);
  }
}