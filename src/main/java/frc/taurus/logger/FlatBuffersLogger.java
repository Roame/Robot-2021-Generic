package frc.taurus.logger;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;

import com.google.flatbuffers.FlatBufferBuilder;

import frc.taurus.config.ChannelIntf;
import frc.taurus.config.ChannelManager;
import frc.taurus.logger.generated.Packet;
import frc.taurus.messages.MessageQueue;

/**
 * A log file is a sequence of size prefixed flatbuffers.
 * 
 * The first flatbuffer will be the LogFileHeader, followed by an arbitrary
 * number of MessageHeaders
 */

public class FlatBuffersLogger {

  ChannelManager channelManager;
  String filename;
  final Supplier<ByteBuffer> getFileHeaderCallback;
  SortedMap<ChannelIntf, MessageQueue<ByteBuffer>.QueueReader> channelReaderMap = new TreeMap<ChannelIntf, MessageQueue<ByteBuffer>.QueueReader>();

  BinaryLogFileWriter writer;
  int maxHeaderSize = 0;
  long packetCount = 0;

  public FlatBuffersLogger(ChannelManager channelManager, final String filename, final Supplier<ByteBuffer> getFileHeaderCallback) {
    this.channelManager = channelManager;
    this.filename = filename;  
    this.getFileHeaderCallback = getFileHeaderCallback;
    writer = new BinaryLogFileWriter(filename);
  }

  /**
   * Used to open the same log filename in a new folder (when switching to auto, teleop, or test)
   */
  public void relocate(final String suffix) {
    writer.close();     // close old file
    packetCount = 0;    // make sure new file writes a header
    LogFileWriterBase.updateLogFolderTimestamp(suffix);    
    writer = new BinaryLogFileWriter(filename);
  }

  public void register(ChannelIntf channel) {
    MessageQueue<ByteBuffer> queue = channelManager.fetch(channel);
    MessageQueue<ByteBuffer>.QueueReader reader = queue.makeReader();
    channelReaderMap.put(channel, reader);
  }

  public void update() {
    if (packetCount == 0) {
      // write file header before writing first packet
      writer.write(getFileHeaderCallback.get());
    }
    for (var channel : channelReaderMap.keySet()) {
      var channelType = channel.getNum();
      var reader = channelReaderMap.get(channel);
      while (!reader.isEmpty()) {
        byte queueSize = (byte)reader.size();
        ByteBuffer bb = reader.read().get(); // we know Optional::isPresent() is true because of earlier !isEmpty()
        writePacket(channelType, queueSize, bb); // write to file
      }
    }
    // writer.flush();
  }

  public void writePacket(final byte channelType, final byte queueSize, final ByteBuffer bbPayload) {
    int payloadSize = bbPayload.remaining();

    FlatBufferBuilder builder = new FlatBufferBuilder(maxHeaderSize + payloadSize);

    // Create Payload
    // important: need to make a read-only copy so the position of the 
    // original buffer isn't modified
    int dataOffset = Packet.createPayloadVector(builder, bbPayload.asReadOnlyBuffer());

    // Create Packet
    int offset = Packet.createPacket(builder, packetCount++, channelType, queueSize, dataOffset);
    Packet.finishSizePrefixedPacketBuffer(builder, offset); // add size prefix to files
    ByteBuffer bb_packet = builder.dataBuffer();

    maxHeaderSize = Math.max(maxHeaderSize, bb_packet.remaining() - payloadSize);

    // write Packet to file
    writer.write(bb_packet);
  }

  public void close() {
    writer.close();
  }

}