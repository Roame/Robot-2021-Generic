package frc.taurus.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.google.flatbuffers.ByteBufferUtil;

public class FlatBuffersLogReader {

  File file;
  RandomAccessFile raFile;

  public FlatBuffersLogReader(final String filename) {
    this(filename, false);
  }

  public FlatBuffersLogReader(final String filename, boolean writeToBase) {
    try {
      File logPath = LogFileWriterBase.logPath();
      if (writeToBase) {
        logPath = LogFileWriterBase.basePath();
      }
      String fullPathFilename = logPath + File.separator + filename;
      file = new File(fullPathFilename);
      raFile = new RandomAccessFile(file, "r");
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public ByteBuffer getNextTable() {
    byte bytes[] = new byte[0];
    try {
      final byte[] prefix = new byte[4];  // prefix is always 4 bytes
      raFile.readFully(prefix);           // get prefix
      final int tableSize = ByteBufferUtil.getSizePrefix(ByteBuffer.wrap(prefix).order(ByteOrder.LITTLE_ENDIAN));
      bytes = new byte[tableSize];
      raFile.readFully(bytes);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return ByteBuffer.wrap(bytes);
  }

  public void close() {
    try {
      raFile.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public String getName() {
    return file.getName();
  }

  public String getAbsolutePath() {
    return file.getAbsolutePath();
  }
}