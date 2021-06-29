package lua.gen;

import java.util.Collection;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

/** A serializable template for the Lua representation of calling a specific Java method. */
final class NativeTemplateParser {
  private NativeTemplateParser() {}

  private enum ParseState {
    TOKEN,
    COMMAND,
    BRACE_COMMAND,
  }
  
  private static class ParseException extends RuntimeException {
    public ParseException(String msg) {
      super(msg);
    }
  }
  
  public static MethodCallRenderer parseNativeTemplate(String repr) {
    ImmutableList.Builder<MethodCallRenderer> components = ImmutableList.builder();

    StringBuilder token = new StringBuilder();
    ParseState state = ParseState.TOKEN;
    for (char ch : repr.toCharArray()) {
      switch (state) {
        case TOKEN:
          if (ch == '$') {
            String str = token.toString();
            token = new StringBuilder();
            if (!str.isEmpty()) {
              components.add(new LiteralRenderer(str));
            }
            state = ParseState.COMMAND;
          } else {
            token.append(ch);
          }
          break;
        case COMMAND:
          if (ch == '$') {
            token = new StringBuilder("$$");
            state = ParseState.TOKEN;
          } else if (ch == '{') {
            state = ParseState.BRACE_COMMAND;
          } else if (Character.isDigit(ch)) {
            components.add(new SingleArgumentRenderer(ch - '0'));
            state = ParseState.TOKEN;
          } else {
            throw new ParseException("Illegal substitution: $" + String.valueOf(ch));
          }
          break;
        case BRACE_COMMAND:
          if (ch == '}') {
            String indexStr = token.toString();
            token = new StringBuilder();
            Integer index = Ints.tryParse(indexStr);
            if (index == null) {
              throw new ParseException("Illegal substitution: ${" + indexStr + "}");
            }
            components.add(new SingleArgumentRenderer(index));
            state = ParseState.TOKEN;
          } else {
            token.append(ch);
          }
          break;
      }
    }
    
    switch (state) {
      case TOKEN:
        final String str = token.toString();
        if (!str.isEmpty()) {
          components.add((sb, args) -> sb.append(str));
        }
        break;
      case COMMAND:
        throw new ParseException("Unterminated $");
      case BRACE_COMMAND:
        throw new ParseException("Unterminated ${");
    }
    
    return new CompositeMethodCallRenderer(components.build());
  }
}
