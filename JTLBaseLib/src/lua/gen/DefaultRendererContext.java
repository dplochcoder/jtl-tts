package lua.gen;

public final class DefaultRendererContext implements RendererContext {
  private final ClassSpecLoader loader;
  
  public DefaultRendererContext(ClassSpecLoader loader) {
    this.loader = loader;
  }
  
  @Override
  public MethodCallRenderer getMethodCallRenderer(String canonicalClassName, String methodSpecString) throws UnsupportedLuaMethodException {
    ClassSpec classSpec = loader.getClassSpec(canonicalClassName);
    MethodSpec methodSpec = classSpec.getMethodSpec(methodSpecString);
    
    if (methodSpec == null) {
      throw new UnsupportedLuaMethodException(canonicalClassName, methodSpecString);
    }
    return methodSpec.callRenderer();
  }
}