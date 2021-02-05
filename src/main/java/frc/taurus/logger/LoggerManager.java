package frc.taurus.logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import com.google.flatbuffers.FlatBufferBuilder;

import edu.wpi.first.wpilibj.Timer;
import frc.robot.Constants;
import frc.taurus.config.ChannelIntf;
import frc.taurus.config.ChannelManager;
import frc.taurus.config.Config;
import frc.taurus.config.generated.Channel;
import frc.taurus.config.generated.Configuration;
import frc.taurus.driverstation.generated.DriverStationStatus;
import frc.taurus.logger.generated.LogFileHeader;
import frc.taurus.messages.MessageQueue;

public class LoggerManager {

  ChannelManager channelManager;
  MessageQueue<ByteBuffer>.QueueReader driverStationStatusReader;

  // synchronized methods in this class to protect channelList and loggerMap
  ArrayList<ChannelIntf> channelList = new ArrayList<ChannelIntf>();
  HashMap<String, FlatBuffersLogger> loggerMap = new HashMap<>();
  boolean unitTest = false;

  // Create our own thread instead of using a Notifier() so that we can
  // lower the thread priority of the logger so it never interferes
  // with real-time processes
  private Thread thread;
  private boolean running;

  class LoggerThread extends Thread {
    public void run() {
      while (true) {
        if (running) {
          update();
  
          // sleep for 1/2 of the normal loop period
          // hopefully this is fast enough to keep up with the logging
          // but not enough to reduce performance
          try {
            Thread.sleep((long)(Constants.kLoopDt*1000/2));
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }
  }

  private void start() {
    running = true;
  }

  @SuppressWarnings("unused")
  private void stop() {
    running = false;
  }



  public LoggerManager(ChannelManager channelManager) {
    this.channelManager = channelManager;

    thread = new LoggerThread();
    thread.setName("LoggerThread");
    thread.setPriority(3);  // lower than the default priority of 5
    thread.start();
    start();
  }

  // called when ChannelManager.fetch() is called by robot code
  public synchronized void register(ChannelIntf channel) {
    if (!channelList.contains(channel)) {
      channelList.add(channel);
      openLogger(channel);
    }
  }

  private synchronized void openLogger(ChannelIntf channel) {
    // get the filename listed in Config
    String filename = channel.getLogFilename();

    // if log filename is not empty, log it
    if (!filename.isEmpty()) {
      // if filename has not been seen before, create a logger for that file
      if (!loggerMap.containsKey(filename)) {
        loggerMap.put(filename, new FlatBuffersLogger(channelManager, filename, this::getFileHeader));
      }
      FlatBuffersLogger logger = loggerMap.get(filename);
      logger.register(channel);
    }
  }

  // called when we switch folders between auto/teleop/test
  private synchronized void relocateLoggers(final String suffix) {
    for (var logger : loggerMap.values()) {
      logger.relocate(suffix);
    }
  }  

  private synchronized void update() {
    updateLogFolderTimestamp();

    for (var logger : loggerMap.values()) {
      logger.update();
    }
  }

 
  private synchronized void close() {
    for (var logger : loggerMap.values()) {
      logger.close();
    }
  }

  public void setUnitTest() {
    unitTest = true;
  }



  boolean enabledLast = false;
  boolean autoLast = false;
  boolean teleopLast = false;
  boolean testLast = false;


  private void updateLogFolderTimestamp() {

    // can't put this in the constructor because channelManager hasn't finsished contructing yet
    if (driverStationStatusReader == null) {
      driverStationStatusReader = channelManager.fetch(Config.DRIVER_STATION_STATUS).makeReader();
    }

    while (!driverStationStatusReader.isEmpty()) {
      Optional<ByteBuffer> obb = driverStationStatusReader.read();
      if (obb.isPresent()) {
        DriverStationStatus dsStatus = DriverStationStatus.getRootAsDriverStationStatus(obb.get());
    
        boolean enabled = dsStatus.enabled();
        boolean auto    = dsStatus.autonomous();
        boolean teleop  = dsStatus.teleop();
        boolean test    = dsStatus.test();


        // if we start autonomous, teleop, or test, create a new folder    
        if (auto && !autoLast) {
          if (unitTest) {
            relocateLoggers("unit_test_auto");
          } else {
            relocateLoggers("auto");
          }
        }
        
        if (teleop && !teleopLast) {
          if (unitTest) {
            relocateLoggers("unit_test_teleop");
          } else {
            relocateLoggers("teleop");
          }
        }
        
        if (test && !testLast) {
          if (unitTest) {
            relocateLoggers("unit_test_test");
          } else {
            relocateLoggers("test");
          }
        }

        if (unitTest && !enabledLast && enabled && !auto && !teleop && !test) {
          relocateLoggers("unit_test");
        }        

        if (!enabled && enabledLast) {
          close();
        }

        enabledLast = enabled;
        autoLast = auto;
        teleopLast = teleop;
        testLast = test;
      }
    }
  }



  int bufferSize = 0;

  // synchronized to protect channelList
  public synchronized ByteBuffer getFileHeader() {
  
    FlatBufferBuilder builder = new FlatBufferBuilder(bufferSize);

    // create Channels
    int[] channelOffsets = new int[channelList.size()];
    for (int k = 0; k < channelList.size(); k++) {
      ChannelIntf channel = channelList.get(k);
      channelOffsets[k] = Channel.createChannel(builder, channel.getNum(), builder.createString(channel.getName()),
          builder.createString(channel.getLogFilename()));
    }
    // create Channel vector
    int channelVectorOffset = Configuration.createChannelsVector(builder, channelOffsets);

    // create Config
    int configOffset = Configuration.createConfiguration(builder, channelVectorOffset);

    // create LogFileHeader
    int offset = LogFileHeader.createLogFileHeader(builder, Timer.getFPGATimestamp(), configOffset);
    LogFileHeader.finishSizePrefixedLogFileHeaderBuffer(builder, offset);
    ByteBuffer fileHeader = builder.dataBuffer();

    bufferSize = Math.max(bufferSize, fileHeader.remaining());

    return fileHeader;
  }

}