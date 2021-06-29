package lua.gen;

public final class UnsupportedLuaMethodException extends Exception {
  UnsupportedLuaMethodException(String className, String methodSpec) {
    super(String.format("Method %s on class %s is not supported in Lua", methodSpec, className));
  }
}