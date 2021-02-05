package frc.taurus.joystick;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;

import com.google.flatbuffers.FlatBufferBuilder;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.Timer;
import frc.taurus.joystick.generated.AxisVector;
import frc.taurus.joystick.generated.ButtonVector;
import frc.taurus.joystick.generated.JoystickGoal;
import frc.taurus.joystick.generated.JoystickStatus;
import frc.taurus.joystick.generated.RumbleSide;
import frc.taurus.messages.MessageQueue;

/**
 * A wrapper for WPILib's Joystick class
 * 
 * This is where we connect our JoystickStatus and JoystickGoal queues
 */

public class Controller {

  final MessageQueue<ByteBuffer>.QueueReader joystickStatusQueueReader;
  final MessageQueue<ByteBuffer> rumbleQueue; 

  public static final int maxNumAxes = 6;
  public static final int maxNumButtons = 16; 

  double[] rawAxis = new double[Controller.maxNumAxes];
  boolean[] rawButton = new boolean[Controller.maxNumButtons];
  int[] rawPov = {-1, -1, -1, -1};

  ArrayList<Button> buttons;


  /**
   * Controller wraps WPILib Joystick class  
   * and connects them with JoystickStatus and JoystickGoal queues
   * @param statusQueue queue where axis and button values will be read from
   * @return new Controller
   */
  public Controller(final MessageQueue<ByteBuffer> statusQueue, final MessageQueue<ByteBuffer> rumbleQueue) {
    this.joystickStatusQueueReader = statusQueue.makeReader();
    this.rumbleQueue = rumbleQueue;
    buttons = new ArrayList<>();
  }

  public Button addButton(int buttonId) {
    Button button = new Button(this, buttonId);
    addButton(button); // add to button list
    return button;
  }

  public AxisButton addAxisButton(int axisId, double threshold) {
    AxisButton axisButton = new AxisButton(this, axisId, threshold);
    addButton(axisButton); // add to button list
    return axisButton;
  }

  public PovButton addPovButton(int povId, int minRange, int maxRange) {
    PovButton povButton = new PovButton(this, povId, minRange, maxRange);
    addButton(povButton); // add to button list
    return povButton;
  }

  private void addButton(Button button) {
    /**
     * All buttons should be added to the buttons list as they are constructed
     */
    if (!buttons.contains(button)) {
      buttons.add(button);
    } else {
      System.out.println("Warning: Adding an already existing button");
      System.out.println("         (This is OK if using the same button for multiple things");
      System.out.println("          Otherwise, check your code)");
    }
  }

  public void update() {
    
    // read values from JoystickStatus queue into local member variables
    readJoystickStatusQueue();
    
    // update button pressed / released for all buttons
    // including PovButtons and AxisButtons
    for (var button : buttons) {
      button.update();
    }
  }

  public void readJoystickStatusQueue() {
    // first read raw axes & buttons from queue
    Optional<ByteBuffer> obb = joystickStatusQueueReader.readLast();
    if (obb.isPresent()) {
      JoystickStatus status = JoystickStatus.getRootAsJoystickStatus(obb.get());

      AxisVector axesVector = status.axes();
      for (int k = 0; k < Controller.maxNumAxes; k++) {
        rawAxis[k] = axesVector.axis(k);
      }

      ButtonVector buttonVector = status.buttons();
      for (int k = 0; k < 16; k++) {
        rawButton[k] = buttonVector.button(k);
      }

      rawPov[0] = status.pov();      
    }
  }

  public double getAxis(int axisId) {
    return rawAxis[axisId];
  }

  public boolean getButton(int buttonId) {
    // called only from Button.update();
    return rawButton[buttonId-1];   // buttonIds start at index 1
  }

  /**
   * Get the angle in degrees of a POV on the HID.
   *
   * <p>
   * The POV angles start at 0 in the up direction, and increase clockwise (eg
   * right is 90, upper-left is 315).
   *
   * @return the angle of the POV in degrees, or -1 if the POV is not pressed.
   */
  public int getPOV(int povId) {
    return rawPov[povId];
  }


  int bufferSize = 0;
  /**
   * Write rumble message to JoystickGoal queue
   * @param rumbleType RumbleType.kLeftRumble or RumbleType.kRightRumble
   * @param rumbleValue between 0.0 and 1.0
   */
  public void setRumble(final RumbleType rumbleType, final double rumbleValue) {
    FlatBufferBuilder builder = new FlatBufferBuilder(bufferSize);

    JoystickGoal.startJoystickGoal(builder);
    JoystickGoal.addTimestamp(builder, Timer.getFPGATimestamp());
    JoystickGoal.addRumbleSide(builder, rumbleType==RumbleType.kLeftRumble ? RumbleSide.LEFT_RUMBLE : RumbleSide.RIGHT_RUMBLE);    
    JoystickGoal.addRumbleValue(builder, (float)rumbleValue);
    int offset = JoystickGoal.endJoystickGoal(builder);

    JoystickGoal.finishJoystickGoalBuffer(builder, offset);
    ByteBuffer bb = builder.dataBuffer();
    rumbleQueue.write(bb);

    bufferSize = Math.max(bufferSize, bb.remaining());
  }



  public class Button {
    protected Controller mController; // controller with this button
    protected int mId; // id of button on controller
    private boolean mCurrent = false;
    private boolean mLast = false;

    /**
     * Button has a private constructor so that it can only be created through
     * Controller.addButton()
     */
    private Button(final Controller controller, final int id) {
      mController = controller;
      mId = id;
    }

    public void update() {
      update(mController.getButton(mId));
    }

    public void update(boolean val) {
      mLast = mCurrent;
      mCurrent = val;
    }

    // WPILib defines these functions, but with this Button class
    // we can extend this to PovButtons and AxisButtons

    public boolean isPressed() {
      return mCurrent;
    }

    public boolean posEdge() {
      return mCurrent && !mLast;
    }

    public boolean negEdge() {
      return !mCurrent && mLast;
    }
  }





  /**
   * Use when an axis is used as a button
   */
  public class AxisButton extends Button {
    double mThreshold; // threshold at which to trigger

    /**
     * AxisButton has a private constructor so that it can only be created through
     * Controller.addAxisButton()
     */
    private AxisButton(final Controller controller, final int id, double threshold) {
      super(controller, id);
      mThreshold = threshold;
    }

    public void update() {
      double value = mController.getAxis(mId);
      boolean pressed = (Math.signum(value) == Math.signum(mThreshold)) && (value >= mThreshold);
      update(pressed);
    }
  }





  /**
   * To use the D-Pad (POV) as up to 8 distinct buttons
   */
  public class PovButton extends Button {
    // minimum and maximum values that would result in a button press
    int mMin;
    int mMax;

    /**
     * PovButton has a private constructor so that it can only be created through
     * Controller.addPovButton()
     */
    private PovButton(final Controller controller, final int id, final int min, final int max) {
      super(controller, id);
      mMin = min;
      mMax = max;
    }

    public void update() {
      int value = mController.getPOV(mId);
      boolean pressed = false;
      // if POV is not pressed, it returns -1
      if (value >= 0) {
        // the negative value check lets us specify a
        // range of -45 to +45 for north, for example
        int negValue = value - 360;
        pressed = ((value >= mMin) && (value <= mMax)) || ((negValue >= mMin) && (negValue <= mMax));
      }
      update(pressed);
    }

  }
};
