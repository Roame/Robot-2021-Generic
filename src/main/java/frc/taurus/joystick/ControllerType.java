package frc.taurus.joystick;

/**
 * List of all Controller subclasses Used in ControllerFactory
 */
public enum ControllerType  {
  XBOX          (XboxController.class), 
  THRUSTMASTER  (ThrustmasterController.class), 
  BUTTON_BOARD  (ButtonBoardController.class);

  private Class<?> cls;

  private ControllerType(Class<?> cls) {
    this.cls = cls;
  }

  public Class<?> type() { return cls; }
}