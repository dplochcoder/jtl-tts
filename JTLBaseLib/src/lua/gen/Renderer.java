package lua.gen;

import java.io.Serializable;

public interface Renderer extends Serializable {
  public static final Renderer EMPTY = new LiteralRenderer("");
  
  void render(StringBuilder sb, RendererContext context);
}