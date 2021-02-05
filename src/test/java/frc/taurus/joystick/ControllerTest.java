package frc.taurus.joystick;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import com.google.flatbuffers.FlatBufferBuilder;

import org.junit.Test;

import edu.wpi.first.wpilibj.Timer;
import frc.taurus.config.ChannelManager;
import frc.taurus.joystick.Controller.AxisButton;
import frc.taurus.joystick.Controller.Button;
import frc.taurus.joystick.Controller.PovButton;
import frc.taurus.joystick.generated.AxisVector;
import frc.taurus.joystick.generated.ButtonVector;
import frc.taurus.joystick.generated.JoystickStatus;
import frc.taurus.messages.MessageQueue;

public class ControllerTest {

  double eps = 1e-6; // using floats for joystick axes

  @Test
  public void addButtonTest() {
    ChannelManager channelManager = new ChannelManager();
    int port = 1;
    Controller controller = new Controller(channelManager.fetchJoystickStatusQueue(port),
        channelManager.fetchJoystickGoalQueue(port));

    Button button = controller.addButton(2);
    button.update();
    assertEquals(1, controller.buttons.size());
  }

  int bufferSize = 0;

  public void sendJoystickStatusMessage(int port, float[] axes, boolean[] buttons, int pov,
      MessageQueue<ByteBuffer> statusQueue) {

    FlatBufferBuilder builder = new FlatBufferBuilder(bufferSize);
    JoystickStatus.startJoystickStatus(builder);
    JoystickStatus.addTimestamp(builder, Timer.getFPGATimestamp());
    JoystickStatus.addPort(builder, port);
    JoystickStatus.addAxes(builder, AxisVector.createAxisVector(builder, axes));
    JoystickStatus.addButtons(builder, ButtonVector.createButtonVector(builder, buttons));
    JoystickStatus.addPov(builder, pov);
    int offset = JoystickStatus.endJoystickStatus(builder);

    JoystickStatus.finishJoystickStatusBuffer(builder, offset);
    ByteBuffer bb = builder.dataBuffer();
    bufferSize = Math.max(bufferSize, bb.remaining()); // correct buffer size for next time

    statusQueue.write(bb);
  }

  @Test
  public void buttonTest() {

    int port = 1;
    float[] axes = new float[Controller.maxNumAxes];
    boolean[] buttons = new boolean[Controller.maxNumButtons];
    int pov = -1;

    var statusQueue = new MessageQueue<ByteBuffer>();
    var goalQueue = new MessageQueue<ByteBuffer>();

    Controller controller = new Controller(statusQueue, goalQueue);

    Button button1 = controller.addButton(1);
    Button button2 = controller.addButton(2);

    // initially not pressed
    buttons[0] = false;
    buttons[1] = false;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(button1.isPressed());
    assertFalse(button1.posEdge());
    assertFalse(button1.negEdge());
    assertFalse(button2.isPressed());

    buttons[0] = false;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(button1.isPressed());
    assertFalse(button1.negEdge());
    assertFalse(button1.negEdge());
    assertFalse(button2.isPressed());

    // press button 0
    buttons[0] = true;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertTrue(button1.isPressed());
    assertTrue(button1.posEdge());
    assertFalse(button1.negEdge());
    assertFalse(button2.isPressed());

    buttons[0] = true;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertTrue(button1.isPressed());
    assertFalse(button1.posEdge());
    assertFalse(button1.negEdge());
    assertFalse(button2.isPressed());

    // release
    buttons[0] = false;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(button1.isPressed());
    assertFalse(button1.posEdge());
    assertTrue(button1.negEdge());
    assertFalse(button2.isPressed());

    buttons[0] = false;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(button1.isPressed());
    assertFalse(button1.posEdge());
    assertFalse(button1.negEdge());
    assertFalse(button2.isPressed());

    // press button 1
    buttons[1] = true;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(button1.isPressed());
    assertTrue(button2.isPressed());
  }

  @Test
  public void axisButtonTest() {

    int port = 1;
    float[] axes = new float[Controller.maxNumAxes];
    boolean[] buttons = new boolean[Controller.maxNumButtons];
    int pov = -1;

    var statusQueue = new MessageQueue<ByteBuffer>();
    var goalQueue = new MessageQueue<ByteBuffer>();

    Controller controller = new Controller(statusQueue, goalQueue);
    int id = 0;
    AxisButton axisButton = controller.addAxisButton(id, 0.5);

    axes[id] = 0.1f;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(axisButton.isPressed());
    assertFalse(axisButton.posEdge());
    assertFalse(axisButton.negEdge());

    axes[id] = 0.4f;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(axisButton.isPressed());
    assertFalse(axisButton.negEdge());
    assertFalse(axisButton.negEdge());

    // press
    axes[id] = 0.6f;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertTrue(axisButton.isPressed());
    assertTrue(axisButton.posEdge());
    assertFalse(axisButton.negEdge());

    axes[id] = 1.0f;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertTrue(axisButton.isPressed());
    assertFalse(axisButton.posEdge());
    assertFalse(axisButton.negEdge());

    // release
    axes[id] = 0.3f;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(axisButton.isPressed());
    assertFalse(axisButton.posEdge());
    assertTrue(axisButton.negEdge());

    axes[id] = 0.3f;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(axisButton.isPressed());
    assertFalse(axisButton.posEdge());
    assertFalse(axisButton.negEdge());
  }

  @Test
  public void povButtonTest() {

    int port = 1;
    float[] axes = new float[Controller.maxNumAxes];
    boolean[] buttons = new boolean[Controller.maxNumButtons];
    int pov = -1;

    var statusQueue = new MessageQueue<ByteBuffer>();
    var goalQueue = new MessageQueue<ByteBuffer>();

    Controller controller = new Controller(statusQueue, goalQueue);

    int id = 0;
    PovButton povButtonNorth = controller.addPovButton(id, -45, 45);
    PovButton povButtonEast = controller.addPovButton(id, 90, 90);
    PovButton povButtonSouth = controller.addPovButton(id, 135, 225);
    PovButton povButtonWest = controller.addPovButton(id, 270, 270);

    // use the when().thenReturn() functions to have the mock controller
    // return fake POV values

    // not pressed
    pov = -1;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(povButtonNorth.isPressed());
    assertFalse(povButtonNorth.posEdge());
    assertFalse(povButtonNorth.negEdge());
    assertFalse(povButtonEast.isPressed());
    assertFalse(povButtonEast.posEdge());
    assertFalse(povButtonEast.negEdge());
    assertFalse(povButtonSouth.isPressed());
    assertFalse(povButtonSouth.posEdge());
    assertFalse(povButtonSouth.negEdge());
    assertFalse(povButtonWest.isPressed());
    assertFalse(povButtonWest.posEdge());
    assertFalse(povButtonWest.negEdge());

    // North
    pov = 0;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);

    controller.update();
    assertTrue(povButtonNorth.isPressed());
    assertTrue(povButtonNorth.posEdge());
    assertFalse(povButtonNorth.negEdge());
    assertFalse(povButtonEast.isPressed());
    assertFalse(povButtonEast.posEdge());
    assertFalse(povButtonEast.negEdge());
    assertFalse(povButtonSouth.isPressed());
    assertFalse(povButtonSouth.posEdge());
    assertFalse(povButtonSouth.negEdge());
    assertFalse(povButtonWest.isPressed());
    assertFalse(povButtonWest.posEdge());
    assertFalse(povButtonWest.negEdge());

    // Northeast
    pov = 45;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);

    controller.update();
    assertTrue(povButtonNorth.isPressed());
    assertFalse(povButtonNorth.posEdge());
    assertFalse(povButtonNorth.negEdge());
    assertFalse(povButtonEast.isPressed());
    assertFalse(povButtonEast.posEdge());
    assertFalse(povButtonEast.negEdge());
    assertFalse(povButtonSouth.isPressed());
    assertFalse(povButtonSouth.posEdge());
    assertFalse(povButtonSouth.negEdge());
    assertFalse(povButtonWest.isPressed());
    assertFalse(povButtonWest.posEdge());
    assertFalse(povButtonWest.negEdge());

    // East
    pov = 90;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(povButtonNorth.isPressed());
    assertFalse(povButtonNorth.posEdge());
    assertTrue(povButtonNorth.negEdge());
    assertTrue(povButtonEast.isPressed());
    assertTrue(povButtonEast.posEdge());
    assertFalse(povButtonEast.negEdge());
    assertFalse(povButtonSouth.isPressed());
    assertFalse(povButtonSouth.posEdge());
    assertFalse(povButtonSouth.negEdge());
    assertFalse(povButtonWest.isPressed());
    assertFalse(povButtonWest.posEdge());
    assertFalse(povButtonWest.negEdge());

    // Southeast
    pov = 135;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(povButtonNorth.isPressed());
    assertFalse(povButtonNorth.posEdge());
    assertFalse(povButtonNorth.negEdge());
    assertFalse(povButtonEast.isPressed());
    assertFalse(povButtonEast.posEdge());
    assertTrue(povButtonEast.negEdge());
    assertTrue(povButtonSouth.isPressed());
    assertTrue(povButtonSouth.posEdge());
    assertFalse(povButtonSouth.negEdge());
    assertFalse(povButtonWest.isPressed());
    assertFalse(povButtonWest.posEdge());
    assertFalse(povButtonWest.negEdge());

    // South
    pov = 180;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(povButtonNorth.isPressed());
    assertFalse(povButtonNorth.posEdge());
    assertFalse(povButtonNorth.negEdge());
    assertFalse(povButtonEast.isPressed());
    assertFalse(povButtonEast.posEdge());
    assertFalse(povButtonEast.negEdge());
    assertTrue(povButtonSouth.isPressed());
    assertFalse(povButtonSouth.posEdge());
    assertFalse(povButtonSouth.negEdge());
    assertFalse(povButtonWest.isPressed());
    assertFalse(povButtonWest.posEdge());
    assertFalse(povButtonWest.negEdge());

    // Southwest
    pov = 225;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(povButtonNorth.isPressed());
    assertFalse(povButtonNorth.posEdge());
    assertFalse(povButtonNorth.negEdge());
    assertFalse(povButtonEast.isPressed());
    assertFalse(povButtonEast.posEdge());
    assertFalse(povButtonEast.negEdge());
    assertTrue(povButtonSouth.isPressed());
    assertFalse(povButtonSouth.posEdge());
    assertFalse(povButtonSouth.negEdge());
    assertFalse(povButtonWest.isPressed());
    assertFalse(povButtonWest.posEdge());
    assertFalse(povButtonWest.negEdge());

    // West
    pov = 270;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(povButtonNorth.isPressed());
    assertFalse(povButtonNorth.posEdge());
    assertFalse(povButtonNorth.negEdge());
    assertFalse(povButtonEast.isPressed());
    assertFalse(povButtonEast.posEdge());
    assertFalse(povButtonEast.negEdge());
    assertFalse(povButtonSouth.isPressed());
    assertFalse(povButtonSouth.posEdge());
    assertTrue(povButtonSouth.negEdge());
    assertTrue(povButtonWest.isPressed());
    assertTrue(povButtonWest.posEdge());
    assertFalse(povButtonWest.negEdge());

    // Northwest
    pov = 315;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertTrue(povButtonNorth.isPressed());
    assertTrue(povButtonNorth.posEdge());
    assertFalse(povButtonNorth.negEdge());
    assertFalse(povButtonEast.isPressed());
    assertFalse(povButtonEast.posEdge());
    assertFalse(povButtonEast.negEdge());
    assertFalse(povButtonSouth.isPressed());
    assertFalse(povButtonSouth.posEdge());
    assertFalse(povButtonSouth.negEdge());
    assertFalse(povButtonWest.isPressed());
    assertFalse(povButtonWest.posEdge());
    assertTrue(povButtonWest.negEdge());

    // Unpressed
    pov = -1;
    sendJoystickStatusMessage(port, axes, buttons, pov, statusQueue);
    controller.update();
    assertFalse(povButtonNorth.isPressed());
    assertFalse(povButtonNorth.posEdge());
    assertTrue(povButtonNorth.negEdge());
    assertFalse(povButtonEast.isPressed());
    assertFalse(povButtonEast.posEdge());
    assertFalse(povButtonEast.negEdge());
    assertFalse(povButtonSouth.isPressed());
    assertFalse(povButtonSouth.posEdge());
    assertFalse(povButtonSouth.negEdge());
    assertFalse(povButtonWest.isPressed());
    assertFalse(povButtonWest.posEdge());
    assertFalse(povButtonWest.negEdge());
  }

  // TODO: add setRumbleTest
  // create Controller with a given goalQueue
  // call setRumble()
  // check goalQueue message exists, and has correct rumble settings
}