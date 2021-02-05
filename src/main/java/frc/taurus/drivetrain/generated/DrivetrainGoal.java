// automatically generated by the FlatBuffers compiler, do not modify

package frc.taurus.drivetrain.generated;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class DrivetrainGoal extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_1_12_0(); }
  public static DrivetrainGoal getRootAsDrivetrainGoal(ByteBuffer _bb) { return getRootAsDrivetrainGoal(_bb, new DrivetrainGoal()); }
  public static DrivetrainGoal getRootAsDrivetrainGoal(ByteBuffer _bb, DrivetrainGoal obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public static boolean DrivetrainGoalBufferHasIdentifier(ByteBuffer _bb) { return __has_identifier(_bb, "DRVG"); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public DrivetrainGoal __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public double timestamp() { int o = __offset(4); return o != 0 ? bb.getDouble(o + bb_pos) : 0.0; }
  public byte goalType() { int o = __offset(6); return o != 0 ? bb.get(o + bb_pos) : 0; }
  public Table goal(Table obj) { int o = __offset(8); return o != 0 ? __union(obj, o + bb_pos) : null; }
  public boolean highGear() { int o = __offset(10); return o != 0 ? 0!=bb.get(o + bb_pos) : false; }
  public boolean quickTurn() { int o = __offset(12); return o != 0 ? 0!=bb.get(o + bb_pos) : false; }

  public static int createDrivetrainGoal(FlatBufferBuilder builder,
      double timestamp,
      byte goal_type,
      int goalOffset,
      boolean high_gear,
      boolean quick_turn) {
    builder.startTable(5);
    DrivetrainGoal.addTimestamp(builder, timestamp);
    DrivetrainGoal.addGoal(builder, goalOffset);
    DrivetrainGoal.addQuickTurn(builder, quick_turn);
    DrivetrainGoal.addHighGear(builder, high_gear);
    DrivetrainGoal.addGoalType(builder, goal_type);
    return DrivetrainGoal.endDrivetrainGoal(builder);
  }

  public static void startDrivetrainGoal(FlatBufferBuilder builder) { builder.startTable(5); }
  public static void addTimestamp(FlatBufferBuilder builder, double timestamp) { builder.addDouble(0, timestamp, 0.0); }
  public static void addGoalType(FlatBufferBuilder builder, byte goalType) { builder.addByte(1, goalType, 0); }
  public static void addGoal(FlatBufferBuilder builder, int goalOffset) { builder.addOffset(2, goalOffset, 0); }
  public static void addHighGear(FlatBufferBuilder builder, boolean highGear) { builder.addBoolean(3, highGear, false); }
  public static void addQuickTurn(FlatBufferBuilder builder, boolean quickTurn) { builder.addBoolean(4, quickTurn, false); }
  public static int endDrivetrainGoal(FlatBufferBuilder builder) {
    int o = builder.endTable();
    builder.required(o, 8);  // goal
    return o;
  }
  public static void finishDrivetrainGoalBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset, "DRVG"); }
  public static void finishSizePrefixedDrivetrainGoalBuffer(FlatBufferBuilder builder, int offset) { builder.finishSizePrefixed(offset, "DRVG"); }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public DrivetrainGoal get(int j) { return get(new DrivetrainGoal(), j); }
    public DrivetrainGoal get(DrivetrainGoal obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}
