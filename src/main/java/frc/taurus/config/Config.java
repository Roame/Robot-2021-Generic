package frc.taurus.config;

import frc.taurus.config.generated.ChannelType;

/**
 * Channels used in the code need to be listed here.  
 * Everything in this list must appear in ChannelType.fbs
 * Not everything in ChannelType.fbs needs to appear here
 * (ChannelType is a superset of Config)
 */

public enum Config implements ChannelIntf {
    DRIVER_STATION_STATUS           (ChannelType.DriverStationStatus, "driver_station.log"),

    JOYSTICK_PORT_1_STATUS          (ChannelType.JoystickStatus,      "joystick.log"),
    JOYSTICK_PORT_2_STATUS          (ChannelType.JoystickStatus,      "joystick.log"),
    JOYSTICK_PORT_3_STATUS          (ChannelType.JoystickStatus,      "joystick.log"),
    JOYSTICK_PORT_4_STATUS          (ChannelType.JoystickStatus,      "joystick.log"),
    JOYSTICK_PORT_1_GOAL            (ChannelType.JoystickGoal,        "joystick.log"),
    JOYSTICK_PORT_2_GOAL            (ChannelType.JoystickGoal,        "joystick.log"),
    JOYSTICK_PORT_3_GOAL            (ChannelType.JoystickGoal,        "joystick.log"),
    JOYSTICK_PORT_4_GOAL            (ChannelType.JoystickGoal,        "joystick.log"),

    DRIVETRAIN_INPUT                (ChannelType.DrivetrainInput,     "drivetrain_input.log"),
    DRIVETRAIN_GOAL                 (ChannelType.DrivetrainGoal,      "drivetrain_goal.log"),
    DRIVETRAIN_STATUS               (ChannelType.DrivetrainStatus,    "drivetrain_status.log"),
    DRIVETRAIN_OUTPUT               (ChannelType.DrivetrainOutput,    "drivetrain_output.log");

    private final byte num;
    private final String name;
    private final String logFilename;

    Config(final byte num, final String logFilename) {
        this.num = num;
        this.name = ChannelType.name(num);        
        this.logFilename = logFilename;
    }

    public byte getNum() { return num; }
    public String getName() { return name; }
    public String getLogFilename() { return logFilename; }
}

