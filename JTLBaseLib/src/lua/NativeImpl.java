package lua;

/** Annotates interface methods that are implemented by a separate class. */
public @interface NativeImpl {
  Class<?> value();
}