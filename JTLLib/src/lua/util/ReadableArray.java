package lua.util;

import lua.Native;
import lua.NativeClass;
import lua.NativeSelf;
import lua.TableValueIterable;

@NativeClass
public interface ReadableArray<T> extends TableValueIterable<T> {
  @SuppressWarnings("unchecked")
  public static <T> ReadableArray<T> empty() {
    return (ReadableArray<T>) ReadableTable.empty();
  }

  // Indices are 1-based
  @Native("$0[$1]")
  T get(int index);

  default boolean isEmpty() {
    return size() == 0;
  }

  @Native("#$0")
  int size();

  @NativeSelf
  ReadableTable<Integer, T> asTable();
}
