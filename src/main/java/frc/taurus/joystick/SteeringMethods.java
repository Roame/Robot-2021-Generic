package frc.taurus.joystick;

import frc.taurus.util.Util;

/**
 * Library of steering functions
 */
public class SteeringMethods {

  public class LeftRightMotor {
    public double left;
    public double right;

    public LeftRightMotor(double left, double right) {
      this.left = left;
      this.right = right;
    }
  }

  public static enum NonLinearityEnum {
    NONE, 
    SQUARED,    // increases fine control while still permitting full power
    CUBED,      // increases fine control even more than SQUARED
    SINE_0p50,  // 971's polydrivetrain, low gear
    SINE_0p65;  // 971's polydrivetrain, high gear
  }

  double deadband1;                 // deadband for the first axis
  double deadband2;                 // deadband for the second axis
  NonLinearityEnum nonLinearity1;   // non-linearity for the first axis
  NonLinearityEnum nonLinearity2;   // non-linearity for the second axis


  
  public SteeringMethods(double deadband1, NonLinearityEnum nonLinearity1, double deadband2, NonLinearityEnum nonLinearity2) {
    this.deadband1 = deadband1;
    this.deadband2 = deadband2;
    this.nonLinearity1 = nonLinearity1;
    this.nonLinearity2 = nonLinearity2;
  }




  // Tank Drive: directly apply joystick inputs to drive motors
  public LeftRightMotor tankDrive(double _lStick, double _rStick) {
    // apply deadband
    double lStick = applyDeadband(_lStick, deadband1);
    double rStick = applyDeadband(_rStick, deadband2);

    // apply non-linearity
    lStick = applyNonLinearity(lStick, nonLinearity1);
    rStick = applyNonLinearity(rStick, nonLinearity2);

    return new LeftRightMotor(lStick, rStick);
  }




  // Arcade Drive: directly apply joystick inputs to drive motors
  public LeftRightMotor arcadeDrive(double _throttle, double _turn) {
    // apply deadband
    double moveValue = applyDeadband(_throttle, deadband1);
    double rotateValue = applyDeadband(_turn, deadband2);

    // apply non-linearity
    moveValue = applyNonLinearity(moveValue, nonLinearity1);
    rotateValue = applyNonLinearity(rotateValue, nonLinearity2);

    // arcade drive mapping of joysticks to motors
    double lMotor, rMotor;
    if (moveValue > 0.0) {
      if (rotateValue > 0.0) {
        lMotor = moveValue - rotateValue;
        rMotor = Math.max(moveValue, rotateValue);
      } else {
        lMotor = Math.max(moveValue, -rotateValue);
        rMotor = moveValue + rotateValue;
      }
    } else {
      if (rotateValue > 0.0) {
        lMotor = -Math.max(-moveValue, rotateValue);
        rMotor = moveValue + rotateValue;
      } else {
        lMotor = moveValue - rotateValue;
        rMotor = -Math.max(-moveValue, -rotateValue);
      }
    }

    return new LeftRightMotor(lMotor, rMotor);
  }




  // Trigger Drive: Triggers control forward/reverse throttle. Left stick controls
  // steering
  public LeftRightMotor triggerDrive(double _throttle, double _turn, boolean _quickTurn) {
    // apply deadband
    double moveValue = applyDeadband(_throttle, deadband1);
    double rotateValue = applyDeadband(_turn, deadband2);

    // apply non-linearity
    moveValue = applyNonLinearity(moveValue, nonLinearity1);
    rotateValue = applyNonLinearity(rotateValue, nonLinearity2);

    double lMotor, rMotor;
    if (rotateValue > 0.0) {
      lMotor = moveValue;
      rMotor = moveValue - 2 * rotateValue * moveValue;
    } else {
      lMotor = moveValue + 2 * rotateValue * moveValue;
      rMotor = moveValue;
    }

    // override normal trigger turning when quick turn button is pressed
    if (_quickTurn) {
      lMotor = +rotateValue;
      rMotor = -rotateValue;
    }

    return new LeftRightMotor(lMotor, rMotor);
  }




  public static double mQuickStopAccumulator = 0.0;

  // Trigger Drive: Triggers control forward/reverse throttle. Left stick controls
  // steering
  public LeftRightMotor cheesyDrive(double _throttle, double _turn, boolean _quickTurn, double _kTurnSensitivity) {
    // apply deadband
    double throttle = applyDeadband(_throttle, deadband1);
    double turn = applyDeadband(_turn, deadband2);

    // apply non-linearity
    throttle = applyNonLinearity(throttle, nonLinearity1);
    turn = applyNonLinearity(turn, nonLinearity2);

    double overPower;
    double angularPower;

    if (_quickTurn) {
      if (Math.abs(throttle) < 0.2) {
        double alpha = 0.1;
        mQuickStopAccumulator = (1 - alpha) * mQuickStopAccumulator + alpha * Util.limit(turn, 1.0) * 2;
      }
      overPower = 1.0;
      angularPower = turn;
    } else {
      overPower = 0.0;
      angularPower = Math.abs(throttle) * turn * _kTurnSensitivity - mQuickStopAccumulator;
      if (mQuickStopAccumulator > 1)
        mQuickStopAccumulator -= 1;
      else if (mQuickStopAccumulator < -1)
        mQuickStopAccumulator += 1;
      else
        mQuickStopAccumulator = 0.0;
    }

    double rMotor = throttle - angularPower;
    double lMotor = throttle + angularPower;

    // scale motor power to keep within limits
    if (lMotor > 1.0) {
      rMotor -= overPower * (lMotor - 1.0);
      lMotor = 1.0;
    } else if (rMotor > 1.0) {
      lMotor -= overPower * (rMotor - 1.0);
      rMotor = 1.0;
    } else if (lMotor < -1.0) {
      rMotor += overPower * (-1.0 - lMotor);
      lMotor = -1.0;
    } else if (rMotor < -1.0) {
      lMotor += overPower * (-1.0 - rMotor);
      rMotor = -1.0;
    }

    return new LeftRightMotor(lMotor, rMotor);
  }




  static double applyDeadband(double _in, double _deadband) {
    double out = _in;
    if (Math.abs(_in) < _deadband) {
      out = 0.0;
    }
    return out;
  }




  static double applyNonLinearity(double _in, NonLinearityEnum _nonLinearity) {
    double out = 0.0;

    switch (_nonLinearity) {
    case NONE:
      out = _in;
      break;

    case SQUARED:
      // square the inputs (while preserving the sign) to increase fine control
      // while permitting full power
      if (_in >= 0.0) {
        out = (_in * _in);
      } else {
        out = -(_in * _in);
      }
      break;

    case CUBED:
      // cube the inputs to increase fine control while permitting full power
      out = _in * _in * _in;
      break;

    case SINE_0p50:
      // 971's polydrivetrain non-linearity
      // make an new NonLinearityEnum type if you want to use different angularRange
      double angularRange = Math.PI/2 * 0.50;
      out = _in;
      out = Math.sin(angularRange * out) / Math.sin(angularRange * out);
      out = Math.sin(angularRange * out) / Math.sin(angularRange * out);
      out = 2.0*_in - out;
      break;
      
    case SINE_0p65:
      // 971's polydrivetrain non-linearity
      // make an new NonLinearityEnum type if you want to use different angularRange
      angularRange = Math.PI/2 * 0.65;
      out = _in;
      out = Math.sin(angularRange * out) / Math.sin(angularRange * out);
      out = Math.sin(angularRange * out) / Math.sin(angularRange * out);
      out = 2.0*_in - out;
      break;

    }
    return out;
  }

}
