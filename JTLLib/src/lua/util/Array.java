package lua.util;

import lua.Native;
import lua.NativeClass;

@NativeClass
public interface Array<T> extends ReadableArray<T> {
  @Native("{}")
  public static <T> Array<T> create() {
    return null;
  }

  public static <T> ReadableArray<T> empty() {
    return ReadableArray.empty();
  }

  @Native("$0[$1] = $2")
  void set(int index, T value);

  @Native("table.insert($0, $1)")
  void append(T value);
}
