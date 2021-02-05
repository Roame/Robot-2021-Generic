package frc.robot;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import io.github.oblarg.oblog.annotations.Config;

// Information on Oblog at 
//  https://oblog-docs.readthedocs.io/en/latest/index.html  
//  https://oblarg.github.io/Oblog/index.html


public class MatchConfig {
  
  public MatchConfig()
  {
      initAutoModeChooser();
  }

  //-------------------------------------------------------
  // Autonomous Mode Chooser
  //-------------------------------------------------------

  @Config(name = "Autonomous Chooser", tabName = "Autonomous")
  SendableChooser<AutoModeOption> autoModeChooser;

  enum AutoModeOption {
    DO_NOTHING        ("Do Nothing"),
    DRIVE_STRAIGHT    ("Drive Straight"),
    NORMAL_AUTO       ("Normal Auto");

    final String name;
    AutoModeOption(String name) {
      this.name = name;
    }
  }

  private void initAutoModeChooser() {
    autoModeChooser = new SendableChooser<>();
    autoModeChooser.addOption(        AutoModeOption.DO_NOTHING.name,           AutoModeOption.DO_NOTHING);
    autoModeChooser.addOption(        AutoModeOption.DRIVE_STRAIGHT.name,       AutoModeOption.DRIVE_STRAIGHT);
    autoModeChooser.setDefaultOption( AutoModeOption.NORMAL_AUTO.name,          AutoModeOption.NORMAL_AUTO);
  }

  public void getAutoMode() {
    switch (autoModeChooser.getSelected()) {
      case DO_NOTHING:
        // TODO: return autoMode
        break;

      case DRIVE_STRAIGHT:
        // TODO: return autoMode
        break;

      case NORMAL_AUTO:
        // TODO: return autoMode
        break;
    } 
  }

  //-------------------------------------------------------
  // Autonomous Start Delay Slider
  //-------------------------------------------------------

  @Config.NumberSlider(name = "Start Delay", 
                       tabName = "Autonomous",
                       methodName = "setStartDelay",
                       methodTypes = {double.class},
                       min = 0.0, 
                       max = 5.0,
                       defaultValue = 0.0)

  private double startDelay = 0.0;  

  @SuppressWarnings("unused")                  
  private void setStartDelay(double val) {
    startDelay = val;
  }                  
  public double getStartDelay() {
    return startDelay;
  }     


  //-------------------------------------------------------
  // ...next control...
  //-------------------------------------------------------


}