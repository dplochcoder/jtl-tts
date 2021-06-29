package lua.gen;

public final class DirectMethodCallRenderer implements MethodCallRenderer {
  private final String fullMethodName;
  private final int numParams;
  
  public DirectMethodCallRenderer(String fullMethodName, int numParams) {
    this.fullMethodName = fullMethodName;
    this.numParams = numParams;
  }
  
  @Override
  public void render(StringBuilder sb, MethodCallRendererContext context) {
    sb.append(fullMethodName);
    sb.append('(');
    for (int i = 0; i < numParams; ++i) {
      if (i > 0) {
        sb.append(", ");
      }
      context.getArgument(i).render(sb, context);
    }
    sb.append(')');
  }
}