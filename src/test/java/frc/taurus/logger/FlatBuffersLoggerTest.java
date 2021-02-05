package frc.taurus.logger;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import com.google.flatbuffers.FlatBufferBuilder;

import org.junit.Test;

import edu.wpi.first.wpilibj.Timer;
import frc.taurus.config.ChannelManager;
import frc.taurus.config.Config;
import frc.taurus.config.TestConfig;
import frc.taurus.config.generated.Channel;
import frc.taurus.config.generated.Configuration;
import frc.taurus.driverstation.generated.DriverStationStatus;
import frc.taurus.logger.generated.LogFileHeader;
import frc.taurus.logger.generated.Packet;
import frc.taurus.messages.MessageQueue;
import frc.taurus.messages.generated.TestMessage1;
import frc.taurus.messages.generated.TestMessage2;

public class FlatBuffersLoggerTest {

  static double eps = 1e-9;

  private void driverstationEnable(ChannelManager channelManager, boolean enabled) {
    
    waitABit();   // wait for message to be logged to file

    FlatBufferBuilder builder = new FlatBufferBuilder(0);

    int gameSpecificMessageOffset = builder.createString("");

    DriverStationStatus.startDriverStationStatus(builder);
    DriverStationStatus.addTimestamp(builder, Timer.getFPGATimestamp());
    DriverStationStatus.addEnabled(builder, enabled);
    DriverStationStatus.addAutonomous(builder, false);
    DriverStationStatus.addTeleop(builder, false);
    DriverStationStatus.addTest(builder, false);
    DriverStationStatus.addDsAttached(builder, false);
    DriverStationStatus.addFmsAttached(builder, false);
    DriverStationStatus.addGameSpecificMessage(builder, gameSpecificMessageOffset);
    DriverStationStatus.addMatchTime(builder, 0);
    int offset = DriverStationStatus.endDriverStationStatus(builder);
    DriverStationStatus.finishDriverStationStatusBuffer(builder, offset);
    ByteBuffer bb = builder.dataBuffer();

    channelManager.fetch(Config.DRIVER_STATION_STATUS).write(bb);

    waitABit();   // wait for log file to be closed
  }

  public void waitABit() {
    try {
      Thread.sleep(20);  // give it a little time to close down the file
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Test
  public void writeOneMessageTest() {

    ChannelManager channelManager = new ChannelManager();
    channelManager.setUnitTest();               // log to "unit_test" folder
    driverstationEnable(channelManager, true);  // we only log when enabled, so we must mock enable during unit tests

    MessageQueue<ByteBuffer> queue1 = channelManager.fetch(TestConfig.TEST_MESSAGE_1);
    FlatBuffersLogReader reader1 = new FlatBuffersLogReader(TestConfig.TEST_MESSAGE_1.getLogFilename());


    // sent data over queue
    FlatBufferBuilder builder1 = new FlatBufferBuilder(64);
    int offset1 = TestMessage1.createTestMessage1(builder1, 686);
    TestMessage1.finishTestMessage1Buffer(builder1, offset1);
    queue1.write(builder1.dataBuffer());

    // disable driver station so that we close the logger files
    driverstationEnable(channelManager, false);


    // Read log file and check its contents
    ByteBuffer bb = reader1.getNextTable();

    LogFileHeader logFileHdr = LogFileHeader.getRootAsLogFileHeader(bb);
    assertEquals(logFileHdr.timestamp(), Timer.getFPGATimestamp(), 100); // we should read the file back within 100
                                                                         // seconds of it being started

    Configuration configuration = logFileHdr.configuration();
    assertEquals(1+1, configuration.channelsLength());

    int k = 1;
    Channel channel = configuration.channels(k);
    assertEquals(TestConfig.TEST_MESSAGE_1.getNum(), channel.channelType());
    assertEquals(TestConfig.TEST_MESSAGE_1.getName(), channel.name());
    assertEquals(TestConfig.TEST_MESSAGE_1.getLogFilename(), channel.logFilename());

    bb = reader1.getNextTable();
    Packet packet = Packet.getRootAsPacket(bb);
    assertEquals(0, packet.packetCount());
    assertEquals(1, packet.queueSize());
    assertEquals(TestConfig.TEST_MESSAGE_1.getNum(), packet.channelType());

    bb = packet.payloadAsByteBuffer();
    TestMessage1 testMessage1 = TestMessage1.getRootAsTestMessage1(bb);
    assertEquals(686, testMessage1.intValue());

    reader1.close();
  }

  @Test
  public void writeTwoMessagesTest() {

    ChannelManager channelManager = new ChannelManager();
    channelManager.setUnitTest();               // log to "unit_test" folder
    driverstationEnable(channelManager, true);  // we only log when enabled, so we must mock enable during unit tests

    MessageQueue<ByteBuffer> queue1 = channelManager.fetch(TestConfig.TEST_MESSAGE_1);
    MessageQueue<ByteBuffer> queue2 = channelManager.fetch(TestConfig.TEST_MESSAGE_2);
    FlatBuffersLogReader reader12 = new FlatBuffersLogReader(TestConfig.TEST_MESSAGE_1.getLogFilename());

    // sent data over queue
    FlatBufferBuilder builder1 = new FlatBufferBuilder(64);
    int offset1 = TestMessage1.createTestMessage1(builder1, 686);
    TestMessage1.finishTestMessage1Buffer(builder1, offset1);
    queue1.write(builder1.dataBuffer());

    FlatBufferBuilder builder2 = new FlatBufferBuilder(64);
    int offset2 = TestMessage2.createTestMessage2(builder2, 686.0);
    TestMessage2.finishTestMessage2Buffer(builder2, offset2);
    queue2.write(builder2.dataBuffer());

    // disable driver station so that we close the logger files
    driverstationEnable(channelManager, false);

    // Read log file and check its contents
    ByteBuffer bb = reader12.getNextTable();

    LogFileHeader logFileHdr = LogFileHeader.getRootAsLogFileHeader(bb);
    assertEquals(logFileHdr.timestamp(), Timer.getFPGATimestamp(), 100); // we should read the file back within 100
                                                                         // seconds of it being started

    // Check Configuration
    Configuration configuration = logFileHdr.configuration();
    assertEquals(2+1, configuration.channelsLength());

    // 1st channel in configuration is TEST_MESSAGE_1
    Channel channel = configuration.channels(1);
    assertEquals(TestConfig.TEST_MESSAGE_1.getNum(), channel.channelType());
    assertEquals(TestConfig.TEST_MESSAGE_1.getName(), channel.name());
    assertEquals(TestConfig.TEST_MESSAGE_1.getLogFilename(), channel.logFilename());

    // 2nd channel in configuration is TEST_MESSAGE_2
    channel = configuration.channels(2);
    assertEquals(TestConfig.TEST_MESSAGE_2.getNum(), channel.channelType());
    assertEquals(TestConfig.TEST_MESSAGE_2.getName(), channel.name());
    assertEquals(TestConfig.TEST_MESSAGE_1.getLogFilename(), channel.logFilename());

    // 1st packet is TEST_MESSAGE_1
    bb = reader12.getNextTable();
    Packet packet = Packet.getRootAsPacket(bb);
    assertEquals(0, packet.packetCount());
    assertEquals(1, packet.queueSize());
    assertEquals(TestConfig.TEST_MESSAGE_1.getNum(), packet.channelType());

    bb = packet.payloadAsByteBuffer();
    TestMessage1 testMessage1 = TestMessage1.getRootAsTestMessage1(bb);
    assertEquals(686, testMessage1.intValue());

    // 2nd packet is TEST_MESSAGE_2
    bb = reader12.getNextTable();
    packet = Packet.getRootAsPacket(bb);
    assertEquals(1, packet.packetCount());
    assertEquals(1, packet.queueSize());
    assertEquals(TestConfig.TEST_MESSAGE_2.getNum(), packet.channelType());

    bb = packet.payloadAsByteBuffer();
    TestMessage2 testMessage2 = TestMessage2.getRootAsTestMessage2(bb);
    assertEquals(686.0, testMessage2.dblValue(), eps);

    reader12.close();

  }



  @Test
  public void twoChannelsLongInterleavedFileTest() {

    final int numMessages = 100;

    ChannelManager channelManager = new ChannelManager();
    channelManager.setUnitTest();               // log to "unit_test" folder
    driverstationEnable(channelManager, true);  // we only log when enabled, so we must mock enable during unit tests

    MessageQueue<ByteBuffer> queue1 = channelManager.fetch(TestConfig.TEST_MESSAGE_1);
    MessageQueue<ByteBuffer> queue2 = channelManager.fetch(TestConfig.TEST_MESSAGE_2);
    FlatBuffersLogReader reader12 = new FlatBuffersLogReader(TestConfig.TEST_MESSAGE_1.getLogFilename());

    for (int k=0; k<numMessages; k++) {
      // sent data over queue
      FlatBufferBuilder builder1 = new FlatBufferBuilder(64);
      int offset1 = TestMessage1.createTestMessage1(builder1, k);
      TestMessage1.finishTestMessage1Buffer(builder1, offset1);
      queue1.write(builder1.dataBuffer());

      FlatBufferBuilder builder2 = new FlatBufferBuilder(64);
      int offset2 = TestMessage2.createTestMessage2(builder2, k);
      TestMessage2.finishTestMessage2Buffer(builder2, offset2);
      queue2.write(builder2.dataBuffer());

      waitABit();
    }
    // disable driver station so that we close the logger files
    driverstationEnable(channelManager, false);

    // Read log file and check its contents
    ByteBuffer bb = reader12.getNextTable();

    LogFileHeader logFileHdr = LogFileHeader.getRootAsLogFileHeader(bb);
    assertEquals(logFileHdr.timestamp(), Timer.getFPGATimestamp(), 100); // we should read the file back within 100
                                                                         // seconds of it being started

    // Check Configuration
    Configuration configuration = logFileHdr.configuration();
    assertEquals(2+1, configuration.channelsLength());

    // 1st channel in configuration is TEST_MESSAGE_1
    Channel channel = configuration.channels(1);
    assertEquals(TestConfig.TEST_MESSAGE_1.getNum(), channel.channelType());
    assertEquals(TestConfig.TEST_MESSAGE_1.getName(), channel.name());
    assertEquals(TestConfig.TEST_MESSAGE_1.getLogFilename(), channel.logFilename());

    // 2nd channel in configuration is TEST_MESSAGE_2
    channel = configuration.channels(2);
    assertEquals(TestConfig.TEST_MESSAGE_2.getNum(), channel.channelType());
    assertEquals(TestConfig.TEST_MESSAGE_2.getName(), channel.name());
    assertEquals(TestConfig.TEST_MESSAGE_1.getLogFilename(), channel.logFilename());

    for (int k=0; k<numMessages; k++) {
      // 1st packet is TEST_MESSAGE_1
      bb = reader12.getNextTable();
      Packet packet = Packet.getRootAsPacket(bb);
      assertEquals(2*k, packet.packetCount());
      assertEquals(1, packet.queueSize());
      assertEquals(TestConfig.TEST_MESSAGE_1.getNum(), packet.channelType());

      bb = packet.payloadAsByteBuffer();
      TestMessage1 testMessage1 = TestMessage1.getRootAsTestMessage1(bb);
      assertEquals(k, testMessage1.intValue());

      // 2nd packet is TEST_MESSAGE_2
      bb = reader12.getNextTable();
      packet = Packet.getRootAsPacket(bb);
      assertEquals(2*k+1, packet.packetCount());
      assertEquals(1, packet.queueSize());
      assertEquals(TestConfig.TEST_MESSAGE_2.getNum(), packet.channelType());

      bb = packet.payloadAsByteBuffer();
      TestMessage2 testMessage2 = TestMessage2.getRootAsTestMessage2(bb);
      assertEquals(k, testMessage2.dblValue(), eps);
    }
    
    reader12.close();

  }
  

  // check queue_size field
  @Test
  public void queueSizeTest() {

    ChannelManager channelManager = new ChannelManager();
    channelManager.setUnitTest();               // log to "unit_test" folder
    driverstationEnable(channelManager, true);  // we only log when enabled, so we must mock enable during unit tests

    MessageQueue<ByteBuffer> queue1 = channelManager.fetch(TestConfig.TEST_MESSAGE_1);
    MessageQueue<ByteBuffer> queue2 = channelManager.fetch(TestConfig.TEST_MESSAGE_2);
    FlatBuffersLogReader reader12 = new FlatBuffersLogReader(TestConfig.TEST_MESSAGE_1.getLogFilename());


    // sent data over queue
    for (int k=0; k<2; k++) {
      FlatBufferBuilder builder1 = new FlatBufferBuilder(64);
      int offset1 = TestMessage1.createTestMessage1(builder1, 686);
      TestMessage1.finishTestMessage1Buffer(builder1, offset1);
      queue1.write(builder1.dataBuffer());
    }

    for (int k=0; k<3; k++) {
      FlatBufferBuilder builder2 = new FlatBufferBuilder(64);
      int offset2 = TestMessage2.createTestMessage2(builder2, 686.0);
      TestMessage2.finishTestMessage2Buffer(builder2, offset2);
      queue2.write(builder2.dataBuffer());
    }

    waitABit();

    // sent data over queue
    FlatBufferBuilder builder1 = new FlatBufferBuilder(64);
    int offset1 = TestMessage1.createTestMessage1(builder1, 686);
    TestMessage1.finishTestMessage1Buffer(builder1, offset1);
    queue1.write(builder1.dataBuffer());

    FlatBufferBuilder builder2 = new FlatBufferBuilder(64);
    int offset2 = TestMessage2.createTestMessage2(builder2, 686.0);
    TestMessage2.finishTestMessage2Buffer(builder2, offset2);
    queue2.write(builder2.dataBuffer());

    // disable driver station so that we close the logger files
    driverstationEnable(channelManager, false);

    // Read log file and check its contents
    ByteBuffer bb = reader12.getNextTable();

    LogFileHeader logFileHdr = LogFileHeader.getRootAsLogFileHeader(bb);
    assertEquals(logFileHdr.timestamp(), Timer.getFPGATimestamp(), 100); // we should read the file back within 100
                                                                         // seconds of it being started

    // Check Configuration
    Configuration configuration = logFileHdr.configuration();
    assertEquals(2+1, configuration.channelsLength());

    // 1st channel in configuration is TEST_MESSAGE_1
    Channel channel = configuration.channels(1);
    assertEquals(TestConfig.TEST_MESSAGE_1.getNum(), channel.channelType());
    assertEquals(TestConfig.TEST_MESSAGE_1.getName(), channel.name());
    assertEquals(TestConfig.TEST_MESSAGE_1.getLogFilename(), channel.logFilename());

    // 2nd channel in configuration is TEST_MESSAGE_2
    channel = configuration.channels(2);
    assertEquals(TestConfig.TEST_MESSAGE_2.getNum(), channel.channelType());
    assertEquals(TestConfig.TEST_MESSAGE_2.getName(), channel.name());
    assertEquals(TestConfig.TEST_MESSAGE_1.getLogFilename(), channel.logFilename());

    // 1st 2 packets are TEST_MESSAGE_1
    int packetCnt = 0;
    for (int k=2; k>0; k--) {
      bb = reader12.getNextTable();
      Packet packet = Packet.getRootAsPacket(bb);
      assertEquals(packetCnt++, packet.packetCount());
      assertEquals(k, packet.queueSize());
      assertEquals(TestConfig.TEST_MESSAGE_1.getNum(), packet.channelType());
      
      bb = packet.payloadAsByteBuffer();
      TestMessage1 testMessage1 = TestMessage1.getRootAsTestMessage1(bb);
      assertEquals(686, testMessage1.intValue());
    }
    
    // next 3 packets are TEST_MESSAGE_2
    for (int k=3; k>0; k--) {
      bb = reader12.getNextTable();
      Packet packet = Packet.getRootAsPacket(bb);
      assertEquals(packetCnt++, packet.packetCount());
      assertEquals(k, packet.queueSize());
      assertEquals(TestConfig.TEST_MESSAGE_2.getNum(), packet.channelType());

      bb = packet.payloadAsByteBuffer();
      TestMessage2 testMessage2 = TestMessage2.getRootAsTestMessage2(bb);
      assertEquals(686.0, testMessage2.dblValue(), eps);
    }

    bb = reader12.getNextTable();
    Packet packet = Packet.getRootAsPacket(bb);
    assertEquals(packetCnt++, packet.packetCount());
    assertEquals(1, packet.queueSize());
    assertEquals(TestConfig.TEST_MESSAGE_1.getNum(), packet.channelType());
    
    bb = packet.payloadAsByteBuffer();
    TestMessage1 testMessage1 = TestMessage1.getRootAsTestMessage1(bb);
    assertEquals(686, testMessage1.intValue());
    
    // next 3 packets are TEST_MESSAGE_2
    bb = reader12.getNextTable();
    packet = Packet.getRootAsPacket(bb);
    assertEquals(packetCnt++, packet.packetCount());
    assertEquals(1, packet.queueSize());
    assertEquals(TestConfig.TEST_MESSAGE_2.getNum(), packet.channelType());

    bb = packet.payloadAsByteBuffer();
    TestMessage2 testMessage2 = TestMessage2.getRootAsTestMessage2(bb);
    assertEquals(686.0, testMessage2.dblValue(), eps);

    reader12.close();

  }

}
