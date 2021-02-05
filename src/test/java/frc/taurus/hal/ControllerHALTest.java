package frc.taurus.hal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.flatbuffers.FlatBufferBuilder;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import frc.taurus.config.ChannelManager;
import frc.taurus.joystick.Controller;
import frc.taurus.joystick.generated.JoystickGoal;
import frc.taurus.joystick.generated.JoystickStatus;
import frc.taurus.joystick.generated.RumbleSide;

public class ControllerHALTest {

  double eps = 1e-6; // using floats for joystick axes

  @Test
  public void registerTest() {
    final ControllerHAL controllerHAL = new ControllerHAL(new ChannelManager());

    final Joystick joystick1 = new Joystick(1);
    final Joystick joystick2 = new Joystick(2);
    final Joystick joystick3 = new Joystick(3);

    assertEquals(0, controllerHAL.size());

    controllerHAL.register(joystick3);
    assertEquals(1, controllerHAL.size());

    controllerHAL.register(joystick2);
    assertEquals(2, controllerHAL.size());

    controllerHAL.register(joystick2);
    assertEquals(2, controllerHAL.size());

    controllerHAL.register(joystick1);
    assertEquals(3, controllerHAL.size());
  }



  @Test
  public void readSensorsTest() {

    final int port1 = 1;
    final int port2 = 2;
    final double axes1[] = new double[Controller.maxNumAxes];
    final double axes2[] = new double[Controller.maxNumAxes];
    final boolean buttons1[] = new boolean[Controller.maxNumButtons];
    final boolean buttons2[] = new boolean[Controller.maxNumButtons];
    final int pov1 = 45;
    final int pov2 = 225;

    final Joystick mockJoystick1 = mock(Joystick.class);
    final Joystick mockJoystick2 = mock(Joystick.class);
    when(mockJoystick1.getPort()).thenReturn(port1);
    when(mockJoystick2.getPort()).thenReturn(port2);

    for (int k = 0; k < Controller.maxNumAxes; k++) {
      axes1[k] = 0.0 + (double) k / 10.0;
      axes2[k] = 0.5 + (double) k / 10.0;
      when(mockJoystick1.getRawAxis(k)).thenReturn(axes1[k]);
      when(mockJoystick2.getRawAxis(k)).thenReturn(axes2[k]);
    }
    for (int k = 0; k < Controller.maxNumButtons; k++) {
      buttons1[k] = (k % 2) == 0;
      buttons2[k] = (k % 2) == 1;
      when(mockJoystick1.getRawButton(k + 1)).thenReturn(buttons1[k]);
      when(mockJoystick2.getRawButton(k + 1)).thenReturn(buttons2[k]);
    }
    when(mockJoystick1.getPOV(0)).thenReturn(pov1);
    when(mockJoystick2.getPOV(0)).thenReturn(pov2);

    final ChannelManager channelManager = new ChannelManager();
    final ControllerHAL controllerHAL = new ControllerHAL(channelManager);
    controllerHAL.register(mockJoystick1);
    controllerHAL.register(mockJoystick2);

    controllerHAL.readSensors(); // creates JoystickStatus messages

    // check that JoystickStatus messages are correct
    final var reader1 = channelManager.fetchJoystickStatusQueue(mockJoystick1.getPort()).makeReader();
    final var reader2 = channelManager.fetchJoystickStatusQueue(mockJoystick2.getPort()).makeReader();

    final Optional<ByteBuffer> obb1 = reader1.readLast();
    final Optional<ByteBuffer> obb2 = reader2.readLast();

    assertTrue(obb1.isPresent());
    assertTrue(obb2.isPresent());

    final JoystickStatus joystickStatus1 = JoystickStatus.getRootAsJoystickStatus(obb1.get());
    final JoystickStatus joystickStatus2 = JoystickStatus.getRootAsJoystickStatus(obb2.get());

    assertEquals(port1, joystickStatus1.port());
    assertEquals(port2, joystickStatus2.port());
    for (int k = 0; k < Controller.maxNumAxes; k++) {
      assertEquals((float) axes1[k], joystickStatus1.axes().axis(k), eps);
      assertEquals((float) axes2[k], joystickStatus2.axes().axis(k), eps);
    }
    for (int k = 0; k < Controller.maxNumButtons; k++) {
      assertEquals(buttons1[k], joystickStatus1.buttons().button(k));
      assertEquals(buttons2[k], joystickStatus2.buttons().button(k));
    }
    assertEquals(pov1, joystickStatus1.pov());
    assertEquals(pov2, joystickStatus2.pov());
  }




  @Test
  public void writeActuatorsTest() {

    final int port1 = 1;
    final int port2 = 2;
    final Joystick mockJoystick1 = mock(Joystick.class);
    final Joystick mockJoystick2 = mock(Joystick.class);
    when(mockJoystick1.getPort()).thenReturn(port1);
    when(mockJoystick2.getPort()).thenReturn(port2);

    final ChannelManager channelManager = new ChannelManager();
    final ControllerHAL controllerHAL = new ControllerHAL(channelManager);
    controllerHAL.register(mockJoystick1);
    controllerHAL.register(mockJoystick2);

    // write a rumble message
    final FlatBufferBuilder builder1 = new FlatBufferBuilder(0);

    JoystickGoal.startJoystickGoal(builder1);
    JoystickGoal.addTimestamp(builder1, Timer.getFPGATimestamp());
    JoystickGoal.addRumbleSide(builder1, RumbleSide.LEFT_RUMBLE);    
    JoystickGoal.addRumbleValue(builder1, (float)0.11);
    int offset = JoystickGoal.endJoystickGoal(builder1);

    JoystickGoal.finishJoystickGoalBuffer(builder1, offset);
    ByteBuffer bb = builder1.dataBuffer();
    channelManager.fetchJoystickGoalQueue(mockJoystick1.getPort()).write(bb);

    final FlatBufferBuilder builder2 = new FlatBufferBuilder(0);

    // write a rumble message    
    JoystickGoal.startJoystickGoal(builder2);
    JoystickGoal.addTimestamp(builder2, Timer.getFPGATimestamp());
    JoystickGoal.addRumbleSide(builder2, RumbleSide.RIGHT_RUMBLE);    
    JoystickGoal.addRumbleValue(builder2,(float) 0.22);
    offset = JoystickGoal.endJoystickGoal(builder2);

    JoystickGoal.finishJoystickGoalBuffer(builder2, offset);
    bb = builder2.dataBuffer();
    channelManager.fetchJoystickGoalQueue(mockJoystick2.getPort()).write(bb);



    controllerHAL.writeActuators();   // creates JoystickStatus messages

    

    // verify rumble was written to joysticks
    ArgumentCaptor<RumbleType> rumbleTypeCaptor = ArgumentCaptor.forClass(RumbleType.class);
    ArgumentCaptor<Double> rumbleValueCaptor = ArgumentCaptor.forClass(Double.class);

    verify(mockJoystick1).setRumble(rumbleTypeCaptor.capture(), rumbleValueCaptor.capture());
    assertEquals(RumbleType.kLeftRumble, rumbleTypeCaptor.getValue());
    assertEquals(0.11, rumbleValueCaptor.getValue(), eps);

    verify(mockJoystick2).setRumble(rumbleTypeCaptor.capture(), rumbleValueCaptor.capture());
    assertEquals(RumbleType.kRightRumble, rumbleTypeCaptor.getValue());
    assertEquals(0.22, rumbleValueCaptor.getValue(), eps);
  }

}