package frc.robot;

import frc.taurus.joystick.SteeringMethods;

public class Constants {
  public static double kLoopDt = 0.010; // 100x per second
  
  public static int kCANTimeoutMs = (int)(kLoopDt*1000); // ms  for real-time updates
  public static int kLongCANTimeoutMs = 100; // ms              for constructors

  public static class ControllerConstants {
    // example joystick config when driver uses single Xbox controller
    public static class ControllerConfig1 {
      public static int kDriveControllerPort = 0;
      public static int kOperatorControllerPort = 1;
      public static double kDriveDeadband = 0.05;
      public static SteeringMethods.NonLinearityEnum kDriveNonLinearity = SteeringMethods.NonLinearityEnum.SQUARED;
    }

    // example joystick config when driver uses dual Thrustmaster controllers
    public static class ControllerConfig2 {
      public static int kDriverLeftControllerPort = 0;
      public static int kDriverRightControllerPort = 1;
      public static int kOperatorControllerPort = 2;
      public static double kDriveDeadband = 0.05;
      public static SteeringMethods.NonLinearityEnum kDriveNonLinearity = SteeringMethods.NonLinearityEnum.SQUARED;
    }
  }

  public static class DriveConstants {
    public static double kDriveWheelCircumInches = 4.0;
  }
}