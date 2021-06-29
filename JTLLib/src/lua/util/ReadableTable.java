package lua.util;

import lua.Native;
import lua.NativeClass;
import lua.NativeSelf;
import lua.TableEntryIterable;
import lua.TableKeyIterable;
import lua.TableValueIterable;

@NativeClass
public interface ReadableTable<K, V> extends TableEntryIterable<K, V> {
  @Native(value = "_EMPTY_TABLE", functionDef = "_EMPTY_TABLE = {}")
  public static <K, V> ReadableTable<K, V> empty() { return null; }

  default boolean contains(K key) {
    return get(key) != null;
  }

  @Native("$0[$1]")
  V get(K key);

  default V get(K key, V defaultValue) {
    V ret = get(key);
    
    if (ret != null) {
      return ret;
    } else {
      return defaultValue;
    }
  }

  @Native("next($0) == nil")
  boolean isEmpty();

  default int getSizeExpensive() {
    int ret = 0;
    for (@SuppressWarnings("unused") K key : keys()) {
      ++ret;
    }
    return ret;
  }

  @NativeSelf
  TableEntryIterable<K, V> entries();

  @NativeSelf
  TableKeyIterable<K> keys();

  @NativeSelf
  TableValueIterable<V> values();
}