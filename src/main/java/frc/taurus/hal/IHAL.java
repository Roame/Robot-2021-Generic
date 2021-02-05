package frc.taurus.hal;

public interface IHAL {
  void zeroSensors();     // read from sensor hardware, write to input queue
  void readSensors();     // read from sensor hardware, write to input queue
  void writeActuators();  // read from output queue, write to actuator hardware
  void stop();            // stop all actuators when disabled
}