package com.google.javascript.gents;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.javascript.gents.CollectModuleMetadata.FileModule;
import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.CompilerPass;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.jscomp.NodeTraversal.AbstractPreOrderCallback;
import com.google.javascript.jscomp.NodeUtil;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Converts Closure-style modules into TypeScript (ES6) modules (not namespaces). All module
 * metadata must be populated before running this CompilerPass.
 */
public final class ModuleConversionPass implements CompilerPass {

  private static final String EXPORTS = "exports";

  private final AbstractCompiler compiler;
  private final PathUtil pathUtil;
  private final NameUtil nameUtil;
  private final NodeComments nodeComments;

  private final Map<String, FileModule> fileToModule;
  private final Map<String, FileModule> namespaceToModule;

  /**
   * Map of metadata about a potential export to the node that should be exported.
   *
   * <p>For example, in the case below, the {@code Node} would point to {@code class Foo}.
   * <code><pre>
   * class Foo {}
   * exports {Foo}
   * </pre><code>
   */
  private final Map<ExportedSymbol, Node> exportsToNodes = new HashMap<>();

  // Used for rewriting usages of imported symbols
  /** fileName, namespace -> local name */
  private final Table<String, String, String> valueRewrite = HashBasedTable.create();
  /** fileName, namespace -> local name */
  private final Table<String, String, String> typeRewrite = HashBasedTable.create();

  private final String alreadyConvertedPrefix;

  public Table<String, String, String> getTypeRewrite() {
    return typeRewrite;
  }

  public ModuleConversionPass(
      AbstractCompiler compiler,
      PathUtil pathUtil,
      NameUtil nameUtil,
      Map<String, FileModule> fileToModule,
      Map<String, FileModule> namespaceToModule,
      NodeComments nodeComments,
      String alreadyConvertedPrefix) {
    this.compiler = compiler;
    this.pathUtil = pathUtil;
    this.nameUtil = nameUtil;
    this.nodeComments = nodeComments;

    this.fileToModule = fileToModule;
    this.namespaceToModule = namespaceToModule;
    this.alreadyConvertedPrefix = alreadyConvertedPrefix;
  }

  @Override
  public void process(Node externs, Node root) {
    NodeTraversal.traverseEs6(compiler, root, new ModuleExportConverter());
    NodeTraversal.traverseEs6(compiler, root, new ModuleImportConverter());
    NodeTraversal.traverseEs6(compiler, root, new ModuleImportRewriter());
  }

  /**
   * Converts "exports" assignments into TypeScript export statements. This also builds a map of all
   * the declared modules.
   */
  private class ModuleExportConverter extends AbstractTopLevelCallback {
    @Override
    public void visit(NodeTraversal t, Node n, Node parent) {
      String fileName = n.getSourceFileName();
      if (n.isScript()) {
        if (fileToModule.containsKey(fileName)) {
          // Module is declared purely for side effects
          FileModule module = fileToModule.get(fileName);
          if (!module.hasImports() && !module.hasExports()) {
            // export {};
            Node commentNode = new Node(Token.EMPTY);
            commentNode.useSourceInfoFrom(n);
            nodeComments.addComment(
                commentNode,
                "\n// gents: force this file to be an ES6 module (no imports or exports)");

            Node exportNode = new Node(Token.EXPORT, new Node(Token.EXPORT_SPECS, commentNode));
            commentNode.useSourceInfoFromForTree(n);

            if (n.hasChildren() && n.getFirstChild().isModuleBody()) {
              n.getFirstChild().addChildToFront(exportNode);
            } else {
              n.addChildToFront(exportNode);
            }
          }
        }
      }

      if (!n.isExprResult()) {
        if (n.isConst() || n.isClass() || n.isFunction()) {
          collectMetdataForExports(n, fileName);
        }
        return;
      }

      Node child = n.getFirstChild();
      switch (child.getToken()) {
        case CALL:
          String callName = child.getFirstChild().getQualifiedName();
          if ("goog.module".equals(callName) || "goog.provide".equals(callName)) {
            // Remove the goog.module and goog.provide calls.
            if (nodeComments.hasComment(n)) {
              nodeComments.replaceWithComment(n, new Node(Token.EMPTY));
            } else {
              compiler.reportChangeToEnclosingScope(n);
              n.detach();
            }
          }
          break;
        case GETPROP:
          {
            JSDocInfo jsdoc = NodeUtil.getBestJSDocInfo(child);
            if (jsdoc == null || !jsdoc.containsTypeDefinition()) {
              // GETPROPs on the root level are only exports for @typedefs
              break;
            }
            if (!fileToModule.containsKey(fileName)) {
              break;
            }
            FileModule module = fileToModule.get(fileName);
            Map<String, String> symbols = module.exportedNamespacesToSymbols;
            String exportedNamespace = nameUtil.findLongestNamePrefix(child, symbols.keySet());
            if (exportedNamespace != null) {
              String localName = symbols.get(exportedNamespace);
              Node export =
                  new Node(
                      Token.EXPORT,
                      new Node(
                          Token.EXPORT_SPECS,
                          new Node(Token.EXPORT_SPEC, Node.newString(Token.NAME, localName))));
              export.useSourceInfoFromForTree(child);
              parent.addChildAfter(export, n);
              // Registers symbol for rewriting local uses.
              registerLocalSymbol(
                  child.getSourceFileName(), exportedNamespace, exportedNamespace, localName);
            }
            break;
          }
        case ASSIGN:
          if (!fileToModule.containsKey(fileName)) {
            break;
          }
          FileModule module = fileToModule.get(fileName);
          Node lhs = child.getFirstChild();
          Map<String, String> symbols = module.exportedNamespacesToSymbols;

          // We export the longest valid prefix
          String exportedNamespace = nameUtil.findLongestNamePrefix(lhs, symbols.keySet());
          if (exportedNamespace != null) {
            convertExportAssignment(
                child, exportedNamespace, symbols.get(exportedNamespace), fileName);
            // Registers symbol for rewriting local uses
            registerLocalSymbol(
                child.getSourceFileName(),
                exportedNamespace,
                exportedNamespace,
                symbols.get(exportedNamespace));
          }
          break;
        default:
          break;
      }
    }

    private void collectMetdataForExports(Node namedNode, String fileName) {
      if (!fileToModule.containsKey(fileName)) {
        return;
      }

      String nodeName = namedNode.getFirstChild().getQualifiedName();
      if (nodeName == null) {
        return;
      }
      exportsToNodes.put(ExportedSymbol.of(fileName, nodeName, nodeName), namedNode);
    }
  }

  /** Converts goog.require statements into TypeScript import statements. */
  private class ModuleImportConverter extends AbstractTopLevelCallback {
    @Override
    public void visit(NodeTraversal t, Node n, Node parent) {
      if (NodeUtil.isNameDeclaration(n)) {
        Node node = n.getFirstFirstChild();
        if (node == null) {
          return;
        }

        // var x = goog.require(...);
        if (node.isCall() && node.getFirstChild().matchesQualifiedName("goog.require")) {
          Node callNode = node;
          String requiredNamespace = callNode.getLastChild().getString();
          String localName = n.getFirstChild().getQualifiedName();
          ModuleImport moduleImport =
              new ModuleImport(n, Collections.singletonList(localName), requiredNamespace, false);
          if (moduleImport.validateImport()) {
            convertNonDestructuringRequireToImportStatements(n, moduleImport);
          }
          return;
        }

        // var {foo, bar} = goog.require(...);
        if (node.isObjectPattern()
            && node.getNext().getFirstChild() != null
            && node.getNext().getFirstChild().matchesQualifiedName("goog.require")) {
          Node importedNode = node.getFirstChild();
          // For multiple destructuring imports, there are multiple full local names.
          ArrayList<String> namedExports = new ArrayList<String>();
          while (importedNode != null) {
            namedExports.add(importedNode.getString());
            importedNode = importedNode.getNext();
          }
          String requiredNamespace = node.getNext().getFirstChild().getNext().getString();
          ModuleImport moduleImport = new ModuleImport(n, namedExports, requiredNamespace, true);
          if (moduleImport.validateImport()) {
            convertDestructuringRequireToImportStatements(n, moduleImport);
          }
          return;
        }
      } else if (n.isExprResult()) {
        // goog.require(...);
        Node callNode = n.getFirstChild();
        if (callNode == null || !callNode.isCall()) {
          return;
        }
        if (callNode.getFirstChild().matchesQualifiedName("goog.require")) {
          String requiredNamespace = callNode.getLastChild().getString();
          // For goog.require(...) imports, the full local name is just the required namespace/module.
          // We use the suffix from the namespace as the local name, i.e. for
          // goog.require("a.b"), requiredNamespace = "a.b", fullLocalName = ["a.b"], localName = ["b"]
          ModuleImport moduleImport =
              new ModuleImport(
                  n, Collections.singletonList(requiredNamespace), requiredNamespace, false);
          if (moduleImport.validateImport()) {
            convertNonDestructuringRequireToImportStatements(n, moduleImport);
          }
          return;
        }
      }
    }
  }

  /** Rewrites variable names used in the file to correspond to the newly imported symbols. */
  private class ModuleImportRewriter extends AbstractPreOrderCallback {
    @Override
    public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
      // Rewrite all imported variable name usages
      if (n.isName() || n.isGetProp()) {
        if (!valueRewrite.containsRow(n.getSourceFileName())) {
          return true;
        }

        Map<String, String> rewriteMap = valueRewrite.rowMap().get(n.getSourceFileName());
        String importedNamespace = nameUtil.findLongestNamePrefix(n, rewriteMap.keySet());
        if (importedNamespace != null) {
          nameUtil.replacePrefixInName(n, importedNamespace, rewriteMap.get(importedNamespace));
          return false;
        }
      }
      return true;
    }
  }

  /** Encapsulates a {@code goog.require(...)} statement. */
  class ModuleImport {
    /** require statement Node. */
    private Node originalImportNode;

    /**
     * LHS of the requirement statement. If the require statement has no LHS, then local name is the
     * suffix of the required namespace/module.
     */
    private List<String> localNames;

    /**
     * Full local name is used for rewriting imported variables in the file later on. There's a one
     * to one relationship between fullLocalNames and localNames.
     */
    private List<String> fullLocalNames;

    /**
     * For {@code goog.require(...)} with no LHS and no side effects, then we use the required
     * namespace/module's suffix as the local name. The backup name is useful in this case to avoid
     * conflicts when the required namespace/module's suffix is the same with a named exported from
     * the same file.
     */
    private String backupName;

    /** The required namespace or module name */
    private String requiredNamespace;

    /**
     * {@code true}, if the original require statement contains destructuring imports. For
     * destructuring imports, there are one or more local names and full local names. For non
     * destructuring imports, there is exactly one local name and one full local name.
     */
    private boolean isDestructuringImport;

    /** FileModule for the imported file */
    private FileModule module;

    /** Referenced file */
    private String referencedFile;

    /**
     * The last part of the required namespace and module, used to detect local name conflicts and
     * calculate the backup name
     */
    private String moduleSuffix;

    private ModuleImport(
        Node originalImportNode,
        List<String> fullLocalNames,
        String requiredNamespace,
        boolean isDestructuringImport) {
      this.originalImportNode = originalImportNode;
      this.fullLocalNames = fullLocalNames;
      this.requiredNamespace = requiredNamespace;
      this.isDestructuringImport = isDestructuringImport;
      this.module = namespaceToModule.get(requiredNamespace);
      this.referencedFile =
          pathUtil.getImportPath(originalImportNode.getSourceFileName(), module.file);
      this.moduleSuffix = nameUtil.lastStepOfName(requiredNamespace);
      this.backupName = this.moduleSuffix;
      this.localNames = new ArrayList<String>();
      for (String fullLocalName : fullLocalNames) {
        String localName = nameUtil.lastStepOfName(fullLocalName);
        this.localNames.add(localName);
        if (this.moduleSuffix.equals(localName)) {
          this.backupName = this.moduleSuffix + "Exports";
        }
      }
    }

    /** Validate the module import assumptions */
    private boolean validateImport() {
      if (!isDestructuringImport && fullLocalNames.size() != 1) {
        compiler.report(
            JSError.make(
                originalImportNode,
                GentsErrorManager.GENTS_MODULE_PASS_ERROR,
                String.format(
                    "Non destructuring imports should have exactly one local name, got [%s]",
                    String.join(", ", fullLocalNames))));
        return false;
      }

      if (this.module == null && !isAlreadyConverted()) {
        compiler.report(
            JSError.make(
                originalImportNode,
                GentsErrorManager.GENTS_MODULE_PASS_ERROR,
                String.format("Module %s does not exist.", requiredNamespace)));
        return false;
      }
      return true;
    }

    /** Returns {@code true} if the imported file is already in TS */
    private boolean isAlreadyConverted() {
      return requiredNamespace.startsWith(alreadyConvertedPrefix + ".");
    }
  }

  /**
   * Converts a non destructuring Closure goog.require call into a TypeScript import statement.
   *
   * <p>The resulting node is dependent on the exports by the module being imported:
   *
   * <pre>
   *   import localName from "goog:old.namespace.syntax";
   *   import {A as localName, B} from "./valueExports";
   *   import * as localName from "./objectExports";
   *   import "./sideEffectsOnly"
   * </pre>
   */
  void convertNonDestructuringRequireToImportStatements(Node n, ModuleImport moduleImport) {
    // The imported file is already in TS
    if (moduleImport.isAlreadyConverted()) {
      convertRequireForAlreadyConverted(moduleImport);
      return;
    }

    // The imported file is kept in JS
    if (moduleImport.module.shouldUseOldSyntax()) {
      convertRequireToImportsIfImportedIsKeptInJs(moduleImport);
      return;
    }
    // For the rest of the function, the imported and importing files are migrating together

    // For non destructuring imports, there is exactly one local name and full local name
    String localName = moduleImport.localNames.get(0);
    String fullLocalName = moduleImport.fullLocalNames.get(0);
    // If not imported then this is a side effect only import.
    boolean imported = false;

    if (moduleImport.module.importedNamespacesToSymbols.containsKey(
        moduleImport.requiredNamespace)) {
      // import {value as localName} from "./file"
      Node importSpec = new Node(Token.IMPORT_SPEC, IR.name(moduleImport.moduleSuffix));
      // import {a as b} only when a != b
      if (!moduleImport.moduleSuffix.equals(localName)) {
        importSpec.addChildToBack(IR.name(localName));
      }

      Node importNode =
          new Node(
              Token.IMPORT,
              IR.empty(),
              new Node(Token.IMPORT_SPECS, importSpec),
              Node.newString(moduleImport.referencedFile));
      addImportNode(n, importNode);
      imported = true;

      registerLocalSymbol(
          n.getSourceFileName(), fullLocalName, moduleImport.requiredNamespace, localName);
      // Switch to back up name if necessary
      localName = moduleImport.backupName;
    }

    if (moduleImport.module.providesObjectChildren.get(moduleImport.requiredNamespace).size() > 0) {
      // import * as var from "./file"
      Node importNode =
          new Node(
              Token.IMPORT,
              IR.empty(),
              Node.newString(Token.IMPORT_STAR, localName),
              Node.newString(moduleImport.referencedFile));
      addImportNode(n, importNode);
      imported = true;

      for (String child :
          moduleImport.module.providesObjectChildren.get(moduleImport.requiredNamespace)) {
        if (!valueRewrite.contains(n.getSourceFileName(), child)) {
          String fileName = n.getSourceFileName();
          registerLocalSymbol(
              fileName,
              fullLocalName + '.' + child,
              moduleImport.requiredNamespace + '.' + child,
              localName + '.' + child);
        }
      }
    }

    if (!imported) {
      // Convert the require to "import './sideEffectOnly';"
      convertRequireForSideEffectOnlyImport(moduleImport);
    }

    compiler.reportChangeToEnclosingScope(n);
    n.detach();
  }

  /**
   * Converts a destructuring Closure goog.require call into a TypeScript import statement.
   *
   * <p>The resulting node is dependent on the exports by the module being imported:
   *
   * <pre>
   *   import {A as localName, B} from "./valueExports";
   * </pre>
   */
  void convertDestructuringRequireToImportStatements(Node n, ModuleImport moduleImport) {
    // The imported file is already in TS
    if (moduleImport.isAlreadyConverted()) {
      convertRequireForAlreadyConverted(moduleImport);
      return;
    }

    // The imported file is kept in JS
    if (moduleImport.module.shouldUseOldSyntax()) {
      convertRequireToImportsIfImportedIsKeptInJs(moduleImport);
      return;
    }
    // For the rest of the function, the imported and importing files are migrating together

    // import {value as localName} from "./file"
    Node importSpec = new Node(Token.IMPORT_SPEC);
    // import {a as b} only when a != b
    for (String localName : moduleImport.localNames) {
      if (!moduleImport.moduleSuffix.equals(localName)) {
        importSpec.addChildToBack(IR.name(localName));
      }
    }
    Node importNode =
        new Node(
            Token.IMPORT,
            IR.empty(),
            new Node(Token.IMPORT_SPECS, importSpec),
            Node.newString(moduleImport.referencedFile));
    addImportNode(n, importNode);

    for (int i = 0; i < moduleImport.fullLocalNames.size(); i++) {
      registerLocalSymbol(
          n.getSourceFileName(),
          moduleImport.fullLocalNames.get(i),
          moduleImport.requiredNamespace,
          moduleImport.localNames.get(i));
    }

    compiler.reportChangeToEnclosingScope(n);
    n.detach();
  }

  /** If the imported file is kept in JS, then use the special "goog:namespace" syntax */
  private void convertRequireToImportsIfImportedIsKeptInJs(ModuleImport moduleImport) {
    Node nodeToImport = null;
    // For destructuring imports use `import {foo} from 'goog:bar';`
    if (moduleImport.isDestructuringImport) {
      nodeToImport = new Node(Token.OBJECTLIT);
      for (String localName : moduleImport.localNames) {
        nodeToImport.addChildToBack(Node.newString(Token.STRING_KEY, localName));
      }
      // For non destructuring imports, it is safe to assume there's only one localName
    } else if (moduleImport.module.hasDefaultExport) {
      // If it has a default export then use `import foo from 'goog:bar';`
      nodeToImport = Node.newString(Token.NAME, moduleImport.localNames.get(0));
    } else {
      // If it doesn't have a default export then use `import * as foo from 'goog:bar';`
      nodeToImport = Node.newString(Token.IMPORT_STAR, moduleImport.localNames.get(0));
    }

    Node importNode =
        new Node(
            Token.IMPORT,
            IR.empty(),
            nodeToImport,
            Node.newString("goog:" + moduleImport.requiredNamespace));
    nodeComments.replaceWithComment(moduleImport.originalImportNode, importNode);
    compiler.reportChangeToEnclosingScope(importNode);

    for (int i = 0; i < moduleImport.fullLocalNames.size(); i++) {
      registerLocalSymbol(
          moduleImport.originalImportNode.getSourceFileName(),
          moduleImport.fullLocalNames.get(i),
          moduleImport.requiredNamespace,
          moduleImport.localNames.get(i));
    }
  }

  private void convertRequireForAlreadyConverted(ModuleImport moduleImport) {
    // we cannot use referencedFile here, because usually it points to the ES5 js file that is
    // the output of TS, and not the original source TS file.
    // However, we can reverse map the goog.module name to a file name.
    // TODO(rado): sync this better with the mapping done in tsickle.
    String originalPath =
        moduleImport.requiredNamespace.replace(alreadyConvertedPrefix + ".", "").replace(".", "/");
    String referencedFile =
        pathUtil.getImportPath(moduleImport.originalImportNode.getSourceFileName(), originalPath);
    // case of side-effectful imports.
    // goog.require('...'); -> import '...';
    Node importSpec = IR.empty();
    Node requireLHS = moduleImport.originalImportNode.getFirstChild();
    if (moduleImport.isDestructuringImport) {
      importSpec = new Node(Token.IMPORT_SPECS);
      for (String fullLocalName : moduleImport.fullLocalNames) {
        importSpec.addChildToBack(IR.name(fullLocalName));
      }
    } else if (requireLHS != null && requireLHS.isName()) {
      // case of full module import.
      // const A = goog.require('...'); -> import * as A from '...';
      // It is safe to assume there's one full local name because this is validated before.
      importSpec = Node.newString(Token.IMPORT_STAR, moduleImport.fullLocalNames.get(0));
    }
    Node importNode =
        new Node(Token.IMPORT, IR.empty(), importSpec, Node.newString(referencedFile));
    nodeComments.replaceWithComment(moduleImport.originalImportNode, importNode);
    compiler.reportChangeToEnclosingScope(importNode);
  }

  private void convertRequireForSideEffectOnlyImport(ModuleImport moduleImport) {
    Node importNode =
        new Node(Token.IMPORT, IR.empty(), IR.empty(), Node.newString(moduleImport.referencedFile));
    addImportNode(moduleImport.originalImportNode, importNode);
  }

  /**
   * Converts a Closure assignment on a goog.module or goog.provide namespace into a TypeScript
   * export statement. This method should only be called on a node within a module.
   *
   * @param assign Assignment node
   * @param exportedNamespace The prefix of the assignment name that we are exporting
   * @param exportedSymbol The symbol that we want to export from the file For example,
   *     convertExportAssignment(pre.fix = ..., "pre.fix", "name") <-> export const name = ...
   *     convertExportAssignment(pre.fix.foo = ..., "pre.fix", "name") <-> name.foo = ...
   */
  void convertExportAssignment(
      Node assign, String exportedNamespace, String exportedSymbol, String fileName) {
    checkState(assign.isAssign());
    checkState(assign.getParent().isExprResult());
    Node grandParent = assign.getGrandparent();
    checkState(
        grandParent.isScript() || grandParent.isModuleBody(),
        "export assignment must be in top level script or module body");

    Node exprNode = assign.getParent();
    Node lhs = assign.getFirstChild();
    Node rhs = assign.getLastChild();
    JSDocInfo jsDoc = NodeUtil.getBestJSDocInfo(assign);

    ExportedSymbol symbolToExport =
        ExportedSymbol.fromExportAssignment(rhs, exportedNamespace, exportedSymbol, fileName);

    if (lhs.matchesQualifiedName(exportedNamespace)) {
      rhs.detach();
      Node exportSpecNode;
      if (rhs.isName() && exportsToNodes.containsKey(symbolToExport)) {
        // Rewrite the AST to export the symbol directly using information from the export
        // assignment.
        Node namedNode = exportsToNodes.get(symbolToExport);
        Node next = namedNode.getNext();
        Node parent = namedNode.getParent();
        namedNode.detach();

        Node export = new Node(Token.EXPORT, namedNode);
        export.useSourceInfoFromForTree(assign);

        nodeComments.moveComment(namedNode, export);
        parent.addChildBefore(export, next);
        exprNode.detach();

        compiler.reportChangeToEnclosingScope(parent);
        return;
      } else if (rhs.isName() && exportedSymbol.equals(rhs.getString())) {
        // Rewrite the export line to: <code>export {rhs}</code>.
        exportSpecNode = new Node(Token.EXPORT_SPECS, new Node(Token.EXPORT_SPEC, rhs));
        exportSpecNode.useSourceInfoFrom(rhs);
      } else {
        // Rewrite the export line to: <code>export const exportedSymbol = rhs</code>.
        exportSpecNode = IR.constNode(IR.name(exportedSymbol), rhs);
        exportSpecNode.useSourceInfoFrom(rhs);
      }
      exportSpecNode.setJSDocInfo(jsDoc);
      Node exportNode = new Node(Token.EXPORT, exportSpecNode);
      nodeComments.replaceWithComment(exprNode, exportNode);

    } else {
      // Assume prefix has already been exported and just trim the prefix
      nameUtil.replacePrefixInName(lhs, exportedNamespace, exportedSymbol);
    }
  }

  /** Saves the local name for imported symbols to be used for code rewriting later. */
  void registerLocalSymbol(
      String sourceFile, String fullLocalName, String requiredNamespace, String localName) {
    valueRewrite.put(sourceFile, fullLocalName, localName);
    typeRewrite.put(sourceFile, fullLocalName, localName);
    typeRewrite.put(sourceFile, requiredNamespace, localName);
  }

  private void addImportNode(Node n, Node importNode) {
    importNode.useSourceInfoFromForTree(n);
    n.getParent().addChildBefore(importNode, n);
    nodeComments.moveComment(n, importNode);
  }

  /** Metadata about an exported symbol. */
  private static class ExportedSymbol {
    /** The name of the file exporting the symbol. */
    final String fileName;

    /**
     * The name that the symbol is declared in the module.
     *
     * <p>For example, {@code Foo} in: <code> class Foo {}</code>
     */
    final String localName;

    /**
     * The name that the symbol is exported under via named exports.
     *
     * <p>For example, {@code Bar} in: <code> exports {Bar}</code>.
     *
     * <p>If a symbol is directly exported, as in the case of <code>export class Foo {}</code>, the
     * {@link #localName} and {@link #exportedName} will both be {@code Foo}.
     */
    final String exportedName;

    private ExportedSymbol(String fileName, String localName, String exportedName) {
      this.fileName = checkNotNull(fileName);
      this.localName = checkNotNull(localName);
      this.exportedName = checkNotNull(exportedName);
    }

    static ExportedSymbol of(String fileName, String localName, String exportedName) {
      return new ExportedSymbol(fileName, localName, exportedName);
    }

    static ExportedSymbol fromExportAssignment(
        Node rhs, String exportedNamespace, String exportedSymbol, String fileName) {
      String localName = (rhs.getQualifiedName() != null) ? rhs.getQualifiedName() : exportedSymbol;

      String exportedName;
      if (exportedNamespace.equals(EXPORTS)) { // is a default export
        exportedName = localName;
      } else if (exportedNamespace.startsWith(EXPORTS)) { // is a named export
        exportedName =
            exportedNamespace.substring(EXPORTS.length() + 1, exportedNamespace.length());
      } else { // exported via goog.provide
        exportedName = exportedSymbol;
      }

      return new ExportedSymbol(fileName, localName, exportedName);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(getClass())
          .add("fileName", fileName)
          .add("localName", localName)
          .add("exportedName", exportedName)
          .toString();
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.fileName, this.localName, this.exportedName);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      ExportedSymbol that = (ExportedSymbol) obj;
      return Objects.equals(this.fileName, that.fileName)
          && Objects.equals(this.localName, that.localName)
          && Objects.equals(this.exportedName, that.exportedName);
    }
  }
}
