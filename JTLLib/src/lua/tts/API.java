package lua.tts;

import lua.NativeGlobal;
import lua.NativeGlobalSource;

public class API {
  @NativeGlobal(NativeGlobalSource.LUA)
  public static GameObject getObjectFromGUID(GUID guid) { return null; }
}