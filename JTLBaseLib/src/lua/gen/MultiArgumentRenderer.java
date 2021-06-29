package lua.gen;

public final class MultiArgumentRenderer implements MethodCallRenderer {
  private final int first;  // Inclusive
  private final int last;   // Exclusive
  
  public MultiArgumentRenderer(int first, int last) {
    this.first = first;
    this.last = last;
  }
  
  @Override
  public void render(StringBuilder sb, MethodCallRendererContext context) {
    for (int i = first; i < last; i++) {
      if (i > first) {
        sb.append(", ");
      }
      context.getArgument(i).render(sb, context);
    }
  }
}