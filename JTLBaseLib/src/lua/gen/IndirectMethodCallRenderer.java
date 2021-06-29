package lua.gen;

public final class IndirectMethodCallRenderer implements MethodCallRenderer {
  private final MethodReference methodReference;
  private final int numParams;
  
  public IndirectMethodCallRenderer(MethodReference methodReference, int numParams) {
    this.methodReference = methodReference;
    this.numParams = numParams;
  }
  
  @Override
  public void render(StringBuilder sb, MethodCallRendererContext context) throws UnsupportedLuaMethodException {
    context.getMethodCallRenderer(methodReference.canonicalClassName(), methodReference.methodSpec()).render(sb, context);
  }
}