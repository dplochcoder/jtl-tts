package lua.gen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Messager;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Type;
import lua.TableEntryIterable;
import lua.TableKeyIterable;
import lua.TableValueIterable;

final class BlockRenderer {
  private final CompilationUnitTree compilationTree;
  private final CanonicalTypes canonicalTypes;
  private final Types types;
  private final Trees trees;
  private final Messager messager;

  private final List<Renderer> renderers;
  private final Set<MethodReference> methodReferences;
  private String indent;

  BlockRenderer(CompilationUnitTree compilationTree, CanonicalTypes canonicalTypes, Types types, Trees trees, Messager messager) {
    this.compilationTree = compilationTree;
    this.canonicalTypes = canonicalTypes;
    this.types = types;
    this.trees = trees;
    this.messager = messager;

    this.renderers = new ArrayList<>();
    this.methodReferences = new HashSet<>();
  }

  List<Renderer> renderers() {
    return renderers;
  }

  Set<MethodReference> methodReferences() {
    return methodReferences;
  }

  void acceptBlock(BlockTree block) {
    indent = "  ";
    acceptBlockInternal(block);
  }

  private void acceptBlockInternal(BlockTree blockTree) {
    for (StatementTree statement : blockTree.getStatements()) {
      acceptStatement(statement);
    }
  }

  private void acceptStatement(StatementTree statementTree) {
    switch (statementTree.getKind()) {
      case BREAK:
        acceptBreakTree((BreakTree) statementTree);
        break;
      case DO_WHILE_LOOP:
        acceptDoWhileLoop((DoWhileLoopTree) statementTree);
        break;
      case EMPTY_STATEMENT:
        break;
      case ENHANCED_FOR_LOOP:
        acceptForEachLoop((EnhancedForLoopTree) statementTree);
        break;
      case EXPRESSION_STATEMENT:
        acceptExpressionStatement((ExpressionStatementTree) statementTree);
        break;
      case FOR_LOOP:
        acceptForLoop((ForLoopTree) statementTree);
        break;
      case IF:
        acceptIf((IfTree) statementTree);
        break;
      case RETURN:
        acceptReturn((ReturnTree) statementTree);
        break;
      case WHILE_LOOP:
        acceptWhileLoop((WhileLoopTree) statementTree);
        break;
      default:
        throw abortWithError("Unsupported statement KIND: " + statementTree.getKind(), statementTree);
    }
  }
  
  private void acceptBreakTree(BreakTree breakTree) {
    // FIXME: Break labels.
    if (breakTree.getLabel() != null && !breakTree.getLabel().toString().isEmpty()) {
      throw abortWithError("Break labels are unsupported", breakTree);
    }
    
    addLine("break");
  }
  
  private void acceptDoWhileLoop(DoWhileLoopTree loopTree) {
    addLine("repeat");
    try (Indent newIndent = new Indent()) {
      acceptStatement(loopTree.getStatement());
    }
    add(new LiteralRenderer("until not ("));
    acceptExpression(loopTree.getCondition());
    addLine(")");
  }
  
  private void acceptForEachLoop(EnhancedForLoopTree loopTree) {
    // Determine kind.
    TypeMirror type = types.erasure(ASTHelpers.getResultType(loopTree.getExpression()));
    
    boolean isKey = types.isSubtype(type, canonicalTypes.get(TableKeyIterable.class));
    boolean isValue = types.isSubtype(type, canonicalTypes.get(TableValueIterable.class));
    boolean isEntry = types.isSubtype(type, canonicalTypes.get(TableEntryIterable.class));

    int count = (isKey ? 1 : 0) + (isValue ? 1 : 0) + (isEntry ? 1 : 0);
    if (count == 0) {
      throw abortWithError("Unknown iterable type for for-each loop", loopTree.getExpression());
    } else if (count > 1) {
      throw abortWithError("Cannot iterate over multiple Table iterable types", loopTree.getExpression());
    }

    String expr;
    String var = loopTree.getVariable().getName().toString();
    if (isKey || isValue) {
      expr = isKey ? (var + ", _" ): ("_, " + var);
    } else {
      // FIXME: k, v
      throw abortWithError("k, v not supported yet", loopTree);
    }

    addString(indent + "for " + expr + " in pairs(");
    acceptExpression(loopTree.getExpression());
    addString(") do\n");
    try (Indent plusIndent = new Indent()) {
      acceptStatement(loopTree.getStatement());
    }
    addLine("end");
  }
  
  private void acceptExpressionStatement(ExpressionStatementTree expr) {
    addString(indent);
    acceptExpression(expr.getExpression());
  }
  
  private void acceptForLoop(ForLoopTree loopTree) {
    for (StatementTree statement : loopTree.getInitializer()) {
      acceptStatement(statement);
    }
    acceptWhileLoopExpr(loopTree.getCondition(), loopTree.getStatement());
  }
  
  private void acceptIf(IfTree ifTree) {
    addString(indent + "if (");
    acceptExpression(ifTree.getCondition());
    addString(") then\n");
    try (Indent plusIndent = new Indent()) {
      acceptStatement(ifTree.getThenStatement());
    }
    if (ifTree.getElseStatement() != null && ifTree.getElseStatement().getKind() != Tree.Kind.EMPTY_STATEMENT) {
      addLine("else");
      try (Indent plusIndent = new Indent()) {
        acceptStatement(ifTree.getElseStatement());
      }
    }
    addLine("end");
  }
  
  private void acceptReturn(ReturnTree returnTree) {
    addString(indent + "return (");
    acceptExpression(returnTree.getExpression());
    addString(")\n");
  }
  
  private void acceptWhileLoop(WhileLoopTree loopTree) {
    acceptWhileLoopExpr(loopTree.getCondition(), loopTree.getStatement());
  }
  
  private void acceptWhileLoopExpr(ExpressionTree condition, StatementTree statement) {
    addString(indent + "while (");
    acceptExpression(condition);
    addString(") do\n");
    try (Indent plusIndent = new Indent()) {
      acceptStatement(statement);
    }
    addLine("end");
  }
  
  private void acceptExpression(ExpressionTree expr) {
    // FIXME
  }

  private void add(Renderer renderer) {
    renderers.add(renderer);
  }
  
  private void addLine(String line) {
    renderers.add(new LiteralRenderer(indent + line + "\n"));
  }
  
  private void addString(String str) {
    renderers.add(new LiteralRenderer(str));
  }
  
  private AbortProcessingException abortWithError(String msg, Tree tree) {
    messager.printMessage(Kind.ERROR, msg, trees.getElement(trees.getPath(compilationTree, tree)));
    return new AbortProcessingException();
  }
  
  private class Indent implements AutoCloseable {
    Indent() {
      BlockRenderer.this.indent += "  ";
    }
    
    @Override
    public void close() {
      BlockRenderer.this.indent = BlockRenderer.this.indent.substring(2);
    }
  }
}
