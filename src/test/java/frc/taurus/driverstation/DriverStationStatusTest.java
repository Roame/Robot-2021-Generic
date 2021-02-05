package frc.taurus.driverstation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.junit.Test;

import edu.wpi.first.wpilibj.DriverStation;
import frc.taurus.config.ChannelManager;
import frc.taurus.config.Config;
import frc.taurus.driverstation.generated.DriverStationStatus;

public class DriverStationStatusTest {

  double eps = 1e-9;

  @Test
  public void driverStationStatusTest() {

    ChannelManager channelManager = new ChannelManager();
    var statusQueue = channelManager.fetch(Config.DRIVER_STATION_STATUS);
    var statusReader = statusQueue.makeReader();

    DriverStation mockDriverStation = mock(DriverStation.class);
    
    DriverStationData dsData = new DriverStationData(mockDriverStation, channelManager);
    
    when(mockDriverStation.isEnabled()).thenReturn(true);
    when(mockDriverStation.isAutonomous()).thenReturn(false);
    when(mockDriverStation.isOperatorControl()).thenReturn(true);
    when(mockDriverStation.isTest()).thenReturn(false);
    when(mockDriverStation.isDSAttached()).thenReturn(true);
    when(mockDriverStation.isFMSAttached()).thenReturn(false);
    when(mockDriverStation.getGameSpecificMessage()).thenReturn("blep");
    when(mockDriverStation.getMatchTime()).thenReturn(123.0);
    
    dsData.update();

    Optional<ByteBuffer> obb = statusReader.read();
    assertTrue(obb.isPresent());
    DriverStationStatus dsStatus = DriverStationStatus.getRootAsDriverStationStatus(obb.get());

    assertTrue(dsStatus.enabled());
    assertFalse(dsStatus.autonomous());
    assertTrue(dsStatus.teleop());
    assertFalse(dsStatus.test());
    assertTrue(dsStatus.dsAttached());
    assertFalse(dsStatus.fmsAttached());
    assertEquals("blep", dsStatus.gameSpecificMessage());
    assertEquals(123.0, dsStatus.matchTime(), eps);
  }

}