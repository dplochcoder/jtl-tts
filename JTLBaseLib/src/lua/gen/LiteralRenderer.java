package lua.gen;

public final class LiteralRenderer implements Renderer, MethodCallRenderer {
  private final String literal;
  
  public LiteralRenderer(String literal) {
    this.literal = literal;
  }
  
  @Override
  public void render(StringBuilder sb, MethodCallRendererContext context) {
    sb.append(literal);
  }

  @Override
  public void render(StringBuilder sb, RendererContext context) {
    sb.append(literal);
  }
}