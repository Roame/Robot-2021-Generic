package frc.taurus.config;

import java.nio.ByteBuffer;
import java.util.HashMap;

import frc.taurus.logger.LoggerManager;
import frc.taurus.messages.MessageQueue;


public class ChannelManager {

  // HashMap has high performance contains() and get(), needed by fetch()
  HashMap<ChannelIntf, MessageQueue<ByteBuffer>> channelMap = new HashMap<ChannelIntf, MessageQueue<ByteBuffer>>();
  LoggerManager loggerManager;

  public ChannelManager() {
    loggerManager = new LoggerManager(this);    
  }

  private void register(ChannelIntf channel) {
    // add the new channel to our list
    if (!channelMap.keySet().contains(channel)) {
      this.channelMap.put(channel, new MessageQueue<ByteBuffer>());
    }
  }

  public MessageQueue<ByteBuffer> fetch(ChannelIntf channel) {
    if (!channelMap.keySet().contains(channel)) {
      this.register(channel);
      loggerManager.register(channel);
    }
    return channelMap.get(channel);
  }

  public MessageQueue<ByteBuffer> fetchJoystickStatusQueue(int port) {
    switch (port) {
      case 1:   return fetch(Config.JOYSTICK_PORT_1_STATUS);
      case 2:   return fetch(Config.JOYSTICK_PORT_2_STATUS); 
      case 3:   return fetch(Config.JOYSTICK_PORT_3_STATUS); 
      case 4:   return fetch(Config.JOYSTICK_PORT_4_STATUS); 
      default:
        throw new IllegalArgumentException("Joystick port must be 1-4");
    }
  }

  public MessageQueue<ByteBuffer> fetchJoystickGoalQueue(int port) {
    switch (port) {
      case 1:   return fetch(Config.JOYSTICK_PORT_1_GOAL);
      case 2:   return fetch(Config.JOYSTICK_PORT_2_GOAL); 
      case 3:   return fetch(Config.JOYSTICK_PORT_3_GOAL); 
      case 4:   return fetch(Config.JOYSTICK_PORT_4_GOAL); 
      default:
        throw new IllegalArgumentException("Joystick port must be 1-4");
    }
  }  

  public void setUnitTest() {
    loggerManager.setUnitTest();
  }

}