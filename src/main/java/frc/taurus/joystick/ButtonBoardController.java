package frc.taurus.joystick;

import java.nio.ByteBuffer;

import frc.taurus.messages.MessageQueue;

public class ButtonBoardController extends Controller {
  public enum Axis {
    L_STICK_X_AXIS(0), L_STICK_Y_AXIS(1), L_STICK_TRIGGER_AXIS(2), 
    R_STICK_X_AXIS(4), R_STICK_Y_AXIS(5), R_STICK_TRIGGER_AXIS(3);

    public final int id;

    Axis(int id) {
      this.id = id;
    }
  }

  public enum Button {
    A(1), B(2), X(3), Y(4), LB(5), RB(6), SHARE(7), OPTIONS(8), SL(9), SR(10);

    public final int id;

    Button(int id) {
      this.id = id;
    }
  }

  public ButtonBoardController(final MessageQueue<ByteBuffer> joystickStatusQueue,
                               final MessageQueue<ByteBuffer> joystickGoalQueue) {
    super(joystickStatusQueue, joystickGoalQueue);

    // add all enumerated buttons to button list
    for (Button button : Button.values()) {
      addButton(button.id);
    }
  }

  public double getAxis(Axis axis) {
    // invert the y-axis
    boolean invert = (axis == Axis.L_STICK_Y_AXIS) || (axis == Axis.R_STICK_Y_AXIS);
    double value = (invert ? -1 : 1) * getAxis(axis.id);
    return value;
  }

  public int getPOV() {
    return super.getPOV(0);
  }

  // setRumble(RumbleType, double) available from base class  
}