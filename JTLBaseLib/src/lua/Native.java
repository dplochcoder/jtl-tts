package lua;

/**
 * Annotation applied to any method which has native LUA generation. Unannotated methods within a
 * native class are generated directly from the source.
 * 
 * <p>
 * If `value` is not supplied, the native representation must be provided in some other way.
 *
 * <p>
 * Repr accepts substitution arguments, annotated `$X` for single-character and `${...}` for
 * multi-character. The first argument is denoted `$0` (the implicit 'this' argument on member
 * methods), the second $1, etc. The eleventh argument is denoted `${10}` and so on.
 * 
 * <p>
 * Variadic arguments are denoted `${N:}`, to mean "$N and onwards". They are rendered separated by
 * ", ".
 * 
 * <p>
 * The `functionDef()`, if supplied is rendered once if this native method is ever used, similar to
 * a function's definition.
 */
public @interface Native {
  String value() default "";

  String functionDef() default "";
}
