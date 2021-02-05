package frc.taurus.config;

/**
 * TestConfig is used for unit testing in place of frc.taurus.config.Config
 */

public enum TestConfig implements ChannelIntf {
  TEST_MESSAGE_1((byte)251, "TestMessage1", "test12.log"), // using large num to avoid conflicts with the normal Config
  TEST_MESSAGE_2((byte)252, "TestMessage2", "test12.log"),
  TEST_MESSAGE_3((byte)253, "TestMessage3", "test3.log"),
  TEST_MESSAGE_4((byte)254, "TestMessage4", "test4.log");

  private final byte num;
  private final String name;
  private final String logFilename;

  TestConfig(final byte num, final String name, final String logFilename) {
    this.num = num;
    this.name = name; // can't use ChannelType.name() for test
    this.logFilename = logFilename;
  }

  public byte getNum() {
    return num;
  }

  public String getName() {
    return name;
  }

  public String getLogFilename() {
    return logFilename;
  }

  public static TestConfig findTestConfig(final byte findNum) {
    for (TestConfig config : values()) {
      if (config.num == findNum) {
        return config;
      }
    }
    throw new IllegalArgumentException(String.valueOf(findNum));
  }
}
