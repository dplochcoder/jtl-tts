package lua;

/**
 * Annotates methods which are invoked with the same name and form in Java as in Lua.
 * 
 * <p>Static methods are presumed to live in the global namespace. Member methods are
 * invoked as fields; prototype methods should be annotated with `@NativeProtoypeLiteral`.
 */
public @interface NativeGlobal {
  /** 
   * {@code LUA} if this method is defined in LUA, in which case the implementation in Java is ignored if present.
   * {@code JAVA} if this method is defined in Java, in which case it is transcribed into Lua.
   */
  NativeGlobalSource value() default NativeGlobalSource.JAVA;
}