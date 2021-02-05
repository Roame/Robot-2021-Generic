package frc.taurus.joystick;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SteeringMethodsTest {

  double eps = 1e-6; // using floats for joystick axes

  @Test
  public void handleDeadbandTest() {

    assertEquals(0.5, SteeringMethods.applyDeadband(0.5, 0.05), eps);
    assertEquals(-0.5, SteeringMethods.applyDeadband(-0.5, 0.05), eps);
    assertEquals(0.0, SteeringMethods.applyDeadband(0.02, 0.05), eps);
    assertEquals(0.0, SteeringMethods.applyDeadband(-0.03, 0.05), eps);
    assertEquals(0.0, SteeringMethods.applyDeadband(0.0, 0.05), eps);

    assertEquals(0.5, SteeringMethods.applyDeadband(0.5, 0.025), eps);
    assertEquals(-0.5, SteeringMethods.applyDeadband(-0.5, 0.025), eps);
    assertEquals(0.0, SteeringMethods.applyDeadband(0.02, 0.025), eps);
    assertEquals(-0.03, SteeringMethods.applyDeadband(-0.03, 0.025), eps);
    assertEquals(0.0, SteeringMethods.applyDeadband(0.0, 0.025), eps);
  }

  //TODO: applyNonLinearityTest

  //TODO: arcadeDriveTest
  //TODO: tankDriveTest
  //TODO: triggerDriveTest
  //TODO: cheesyDriveTest
}