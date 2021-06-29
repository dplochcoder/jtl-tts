package lua.gen;

public interface RendererContext {
  MethodCallRenderer getMethodCallRenderer(String canonicalClassName, String methodSpec) throws UnsupportedLuaMethodException;
}