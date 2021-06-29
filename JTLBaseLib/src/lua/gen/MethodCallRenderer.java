package lua.gen;

import java.io.Serializable;

public interface MethodCallRenderer extends Serializable {
  void render(StringBuilder sb, MethodCallRendererContext context) throws UnsupportedLuaMethodException;
}