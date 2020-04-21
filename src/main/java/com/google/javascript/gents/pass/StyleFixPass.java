package com.google.javascript.gents.pass;

import static com.google.javascript.rhino.TypeDeclarationsIR.anyType;
import static com.google.javascript.rhino.TypeDeclarationsIR.arrayType;

import com.google.common.collect.Iterables;
import com.google.javascript.gents.pass.comments.GeneralComment;
import com.google.javascript.gents.pass.comments.NodeComments;
import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.CompilerPass;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Node.TypeDeclarationNode;
import com.google.javascript.rhino.Token;
import java.util.List;

/** Fixes the style of the final TypeScript code to be more idiomatic. */
public final class StyleFixPass extends AbstractPostOrderCallback implements CompilerPass {

  private final AbstractCompiler compiler;
  private final NodeComments nodeComments;

  public StyleFixPass(AbstractCompiler compiler, NodeComments nodeComments) {
    this.compiler = compiler;
    this.nodeComments = nodeComments;
  }

  @Override
  public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, this);
  }

  @Override
  public void visit(NodeTraversal t, Node n, Node parent) {
    switch (n.getToken()) {
        // Var is converted to let
        // This is to output more idiomatic TypeScript even if it slightly changes the semantics
        // of the original code.
      case VAR:
        n.setToken(Token.LET);
        //$FALL-THROUGH$
      case LET:
        if (hasGrandchildren(n)) {
          Node rhs = n.getFirstFirstChild();
          // ONLY convert classes (not functions) for var and let
          if (isTypeDefinition(rhs)) {
            liftClassOrFunctionDefinition(n);
          }
        }
        // Mutable exports are forbidden in both JS and TS, so change them all to CONST
        if (parent.isExport()) {
          n.setToken(Token.CONST);
        }
        break;
      case CONST:
        if (hasGrandchildren(n)) {
          Node rhs = n.getFirstFirstChild();
          if (isTypeDefinition(rhs)) {
            liftClassOrFunctionDefinition(n);
          } else if (rhs.isFunction()) {
            if (rhs.isArrowFunction()) {
              break;
            }
            // Convert const functions only
            TypeDeclarationNode type = n.getFirstChild().getDeclaredTypeExpression();
            // Untyped declarations are lifted
            if (type == null) {
              liftClassOrFunctionDefinition(n);
              break;
            }
            // Declarations that have invalid typings are ignored
            int numParams = Iterables.size(rhs.getSecondChild().children());
            if (numParams != Iterables.size(type.children()) - 1) {
              break;
            }

            // Annotate constant function return type and parameters
            Node newNode = type.getFirstChild();
            Node nextNode = newNode.getNext();
            newNode.detach();

            rhs.setDeclaredTypeExpression((TypeDeclarationNode) newNode);
            for (Node param : rhs.getSecondChild().children()) {
              // Replace params with their corresponding type
              newNode = nextNode;
              nextNode = nextNode.getNext();
              newNode.detach();

              if (newNode.isRest()) {
                newNode.getFirstChild().setString(param.getString());
                // Rest without types are automatically declared to be any[]
                if (newNode.getDeclaredTypeExpression() == null) {
                  newNode.setDeclaredTypeExpression(arrayType(anyType()));
                }
              } else {
                newNode.setString(param.getString());
              }
              nodeComments.replaceWithComment(param, newNode);
            }
            n.getFirstChild().setDeclaredTypeExpression(null);

            liftClassOrFunctionDefinition(n);
          }
        }
        break;
      case MEMBER_FUNCTION_DEF:
        // Remove empty constructors
        if ("constructor".equals(n.getString())) {
          Node params = n.getFirstChild().getSecondChild();
          Node block = n.getFirstChild().getLastChild();
          List<GeneralComment> comments = nodeComments.getComments(n);

          if (!params.hasChildren() && !block.hasChildren() && comments == null) {
            compiler.reportChangeToEnclosingScope(n);
            n.detach();
          }
        }
        break;
      default:
        break;
    }
  }

  private boolean isTypeDefinition(Node rhs) {
    return rhs.isClass() || rhs.getToken() == Token.INTERFACE;
  }

  /** Returns if a node has grandchildren */
  private boolean hasGrandchildren(Node n) {
    return n.hasChildren() && n.getFirstChild().hasChildren();
  }

  /**
   * Attempts to lift class or functions declarations of the form 'var/let/const x = class/function
   * {...}' into 'class/function x {...}'
   */
  private void liftClassOrFunctionDefinition(Node n) {
    Node rhs = n.getFirstFirstChild();
    Node oldName = rhs.getFirstChild();
    Node newName = n.getFirstChild();

    // Replace name node with declared name
    rhs.detach();
    newName.detach();
    nodeComments.replaceWithComment(oldName, newName);
    nodeComments.replaceWithComment(n, rhs);
  }
}
