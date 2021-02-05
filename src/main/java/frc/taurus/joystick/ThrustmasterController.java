package frc.taurus.joystick;

import java.nio.ByteBuffer;

import frc.taurus.messages.MessageQueue;

public class ThrustmasterController extends Controller {
  public enum Axis {
    X_AXIS(0), Y_AXIS(1), Z_ROTATE_AXIS(2), SLIDER_AXIS(3);

    public final int id;

    Axis(int id) {
      this.id = id;
    }
  }

  public enum Button {
    TRIGGER(1), BOTTOM_THUMB(2), LEFT_THUMB(3), RIGHT_THUMB(4),
    // counting from the inner (or thumb) side
    TOP1(5), TOP2(6), TOP3(7), BOTTOM3(8), BOTTOM2(9), BOTTOM1(10), TOP6(11), TOP5(12), TOP4(13), BOTTOM4(14),
    BOTTOM5(15), BOTTOM6(16);

    public final int id;

    Button(int id) {
      this.id = id;
    }
  }

  public ThrustmasterController(final MessageQueue<ByteBuffer> joystickStatusQueue, 
                                final MessageQueue<ByteBuffer> joystickGoalQueue) {
    super( joystickStatusQueue, joystickGoalQueue);

    // add all enumerated buttons to button list
    for (Button button : Button.values()) {
      addButton(button.id);
    }
  }

  public double getAxis(Axis axis) {
    // invert the y-axis
    boolean invert = (axis == Axis.X_AXIS);
    double value = (invert ? -1 : 1) * getAxis(axis.id);
    return value;
  }

  // setRumble(boolean) available from base class
}