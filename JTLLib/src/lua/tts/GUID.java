package lua.tts;

import lua.NativeSelf;
import lua.Wrapper;

public class GUID extends Wrapper<String> {
  public GUID() {
    super("");
  }
  
  public GUID(String guid) {
    super(guid);
  }
  
  @NativeSelf
  public String asString() {
    return wrapped();
  }
}