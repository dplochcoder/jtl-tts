package lua.util.collections;

import lua.NativeClass;
import lua.Wrapper;
import lua.util.Table;

@NativeClass
public final class NestedTable<R, C, V> extends Wrapper<Table<R, Table<C, V>>> implements ReadableNestedTable<R, C, V> {
  public NestedTable() {
    super(Table.create());
  }

  @Override
  public V get(R row, C col) {
    Table<C, V> rowTable = wrapped().get(row);
    return rowTable != null ? rowTable.get(col) : null;
  }

  @Override
  public V get(R row, C col, V defaultValue) {
    Table<C, V> rowTable = wrapped().get(row);
    return rowTable != null ? rowTable.get(col, defaultValue) : defaultValue;
  }

  public void set(R row, C col, V value) {
    Table<C, V> rowTable = wrapped().get(row);
    if (rowTable == null) {
      rowTable = Table.create();
      wrapped().set(row, rowTable);
    }
    rowTable.set(col, value);
  }

  public void remove(R row, C col) {
    Table<C, V> rowTable = wrapped().get(row);
    if (rowTable != null) {
      rowTable.remove(col);
      if (rowTable.isEmpty()) {
        wrapped().remove(row);
      }
    }
  }

  @Override
  public boolean isEmpty() {
    return wrapped().isEmpty();
  }
}
