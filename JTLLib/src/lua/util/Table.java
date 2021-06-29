package lua.util;

import lua.Native;
import lua.NativeClass;

@NativeClass
public interface Table<K, V> extends ReadableTable<K, V> {
  @Native(value = "{}")
  public static <K, V> Table<K, V> create() {
    return null;
  }
  
  public static <K, V> ReadableTable<K, V> empty() { return ReadableTable.empty(); }

  @Native(value = "$0[$1] = $2")
  void set(K key, V value);

  default void remove(K key) {
    set(key, null);
  }
}
