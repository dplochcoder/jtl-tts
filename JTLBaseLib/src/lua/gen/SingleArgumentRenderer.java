package lua.gen;

public final class SingleArgumentRenderer implements MethodCallRenderer {
  private final int index;
  
  public SingleArgumentRenderer(int index) {
    this.index = index;
  }
  
  @Override
  public void render(StringBuilder sb, MethodCallRendererContext context) {
    context.getArgument(index).render(sb, context);
  }
}