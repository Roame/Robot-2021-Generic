package frc.taurus.logger;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;



abstract class LogFileWriterBase {

  private static File basePath = null;
  private static File logPath = null;
  private static String logFolder = null;



  public static File basePath() {
    if (basePath != null) {
      return basePath;
    }

    String[] possiblePaths = {"/media/sda1/",   // first check if a thumb drive in the USB port
                              "/home/lvuser/",  // next, check if we are running on a roboRIO
                              "C:/",            // next, use project folder on host PC (for testing)
                              "logs/"};         // if all else fails, use something relative to java executable

    for (int k=0; k<possiblePaths.length; k++) {
      possiblePaths[k] = possiblePaths[k].replace("/", File.separator);
    }

    for (var pathName : possiblePaths) {
      File path = new File(pathName);
      if (path.exists()) {
        basePath = new File(path.getAbsolutePath() + File.separator + "logs" + File.separator);
        if (basePath.exists()) {
          break;
        } else {
          // path doesn't exist yet -- make it
          boolean success = basePath.mkdir();
          if (success) {
            break;
          }
          success = false;  // breakpoint
          // TODO: what to do when we fail to create a log folder?
          System.err.println("Failed to mkdir " + logPath.getAbsolutePath());
          System.exit(-1);          
        }
      }
    }
    return basePath;
  }


  public static File logPath() {
    if (logFolder == null) {
      updateLogFolderTimestamp("");
    }

    if (logPath == null) {
      logPath = new File(basePath().getAbsolutePath() + File.separator + logFolder);
      if (!logPath.exists()) {
        // path doesn't exist yet -- make it
        boolean success = logPath.mkdir();
        if (!success) {
          // TODO: what to do when fails to create a log folder?
          System.err.println("Failed to mkdir " + logPath.getAbsolutePath());
          System.exit(-1);
        }
      }
    }
    return logPath;
  }




  // to be called by LoggerManager when we switch into auto, teleop, or test modes
  // creates a subfolder under logs with a timestamp and suffix
  public static void updateLogFolderTimestamp(String suffix) {
    if (suffix.length()>=9 && suffix.substring(0,9).compareTo("unit_test")==0) {
      // do not use a timestamp for unit tests (which would create too many folders)
      logFolder = suffix + File.separator;
    } else {
      LocalDateTime date = LocalDateTime.now();
      logFolder = date.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
      logFolder = logFolder + "_" + suffix + File.separator;
    }
    logPath = null;   // force logPath to make a new folder
  }

}