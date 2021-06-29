package lua.tts;

import lua.NativeMember;

/** Native wrapper for an Object in the TTS API. */
public interface GameObject {
  @NativeMember
  GUID getGUID();

  @NativeMember
  void setLock(boolean lock);

  @NativeMember
  Position getPosition();

  @NativeMember
  void setPosition(Position position);

  @NativeMember
  void setPositionSmooth(Position position, boolean collide, boolean fast);

  @NativeMember
  Rotation getRotation();

  @NativeMember
  void setRotation(Rotation rotation);

  @NativeMember
  void setRotationSmooth(Rotation rotation, boolean coolide, boolean fast);
}