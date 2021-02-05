package frc.taurus.joystick;

import java.util.ArrayList;

/**
 * The user controls required for drivetrain motion. These are likely to remain
 * the same from year to year, but some special functions may be needed some
 * years.
 */

public interface IDriverControls {
  double getLeft();
  double getRight();
  boolean getQuickTurn();
  boolean getLowGear();
  // don't add controls here for anything not related to simply moving the drivetrain around the field
  // most controls (even if they are mapped to the driver's joystick) should be in SuperstructureControls

  /**
   * List of controllers to be registered with the ControllerManager
   * @return list of all physical controllers utilized by this Driver/Operator control scheme
   */
  abstract public ArrayList<Controller> getControllerPorts();    
}