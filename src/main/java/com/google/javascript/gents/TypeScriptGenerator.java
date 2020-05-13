package com.google.javascript.gents;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.javascript.gents.experimental.ExperimentTracker;
import com.google.javascript.gents.pass.CollectModuleMetadata;
import com.google.javascript.gents.pass.ModuleConversionPass;
import com.google.javascript.gents.pass.RemoveGoogScopePass;
import com.google.javascript.gents.pass.StyleFixPass;
import com.google.javascript.gents.pass.TypeAnnotationPass;
import com.google.javascript.gents.pass.TypeConversionPass;
import com.google.javascript.gents.pass.comments.AstCommentLinkingPass;
import com.google.javascript.gents.pass.comments.BlankLineHandler;
import com.google.javascript.gents.pass.comments.CommentLinkingPass;
import com.google.javascript.gents.pass.comments.NodeComments;
import com.google.javascript.gents.util.NameUtil;
import com.google.javascript.gents.util.PathUtil;
import com.google.javascript.jscomp.CodeConsumer;
import com.google.javascript.jscomp.CodeGenerator;
import com.google.javascript.jscomp.CodePrinter;
import com.google.javascript.jscomp.CodePrinter.Builder.CodeGeneratorFactory;
import com.google.javascript.jscomp.CodePrinter.Format;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DiagnosticType;
import com.google.javascript.jscomp.ErrorFormat;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.parsing.Config.JsDocParsing;
import com.google.javascript.rhino.Node;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import org.kohsuke.args4j.CmdLineException;

/**
 * A tool that transpiles {@code .js} ES6 and ES5 Closure annotated JavaScript to {@code .ts}
 * TypeScript.
 */
public class TypeScriptGenerator {

  /** Diagnostic that indicates Gents somehow produced an incorrect AST structure. */
  private static final DiagnosticType GENTS_INTERNAL_ERROR =
      DiagnosticType.error("CLUTZ_INTERNAL_ERROR", "Gents failed: {0}");

  /**
   * Command line clang-format string to format stdin. The filename 'a.ts' is only used to inform
   * clang-format of the file type (TS).
   */
  private static final String[] CLANG_FORMAT = {
    "./node_modules/.bin/clang-format", "-assume-filename=a.ts", "-style=Google"
  };

  static {
    // In some environments (Mac OS X programs started from Finder, like your IDE) PATH does not
    // contain "clang-format". This environment variable allows explicitly configuring its location.
    String cfLocation = System.getenv("GENTS_CLANG_FORMAT");
    if (cfLocation != null) {
      CLANG_FORMAT[0] = cfLocation;
    }
  }

  public static void main(String[] args) {
    Options options = null;
    try {
      options = new Options(args);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      System.err.println("Usage: gents [options...] arguments...");
      e.getParser().printUsage(System.err);
      System.err.println();
      System.exit(1);
    }
    TypeScriptGenerator generator = null;
    try {
      // options.enableBlankLineExperiment is now a no-op
      ExperimentTracker experimentTracker = ExperimentTracker.withoutExperiments();
      generator = new TypeScriptGenerator(options, experimentTracker);
      generator.generateTypeScript();
      if (generator.hasErrors()) {
        // Already reported through the print stream.
        System.exit(2);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
      System.err.println("Uncaught exception in gents, exiting.");
      System.exit(3);
    }
    System.exit(0);
  }

  private final Options opts;
  private final Compiler compiler;

  final PathUtil pathUtil;
  private final NameUtil nameUtil;
  private GentsErrorManager errorManager;
  private final ExperimentTracker experimentTracker;

  TypeScriptGenerator(Options opts, ExperimentTracker experimentTracker) {
    this.opts = opts;
    this.experimentTracker = experimentTracker;
    this.compiler = new Compiler();
    compiler.disableThreads();
    setErrorStream(System.err);

    this.pathUtil = new PathUtil(opts.root, opts.absolutePathPrefix);
    this.nameUtil = new NameUtil(compiler);
  }

  void setErrorStream(PrintStream errStream) {
    errorManager =
        new GentsErrorManager(
            errStream, ErrorFormat.MULTILINE.toFormatter(compiler, true), opts.debug);
    compiler.setErrorManager(errorManager);
  }

  public boolean hasErrors() {
    return compiler.getErrorManager().getErrorCount() > 0;
  }

  private void generateTypeScript() throws IOException {
    List<SourceFile> srcFiles = getFiles(opts.srcFiles);
    List<SourceFile> externFiles = getFiles(opts.externs);
    Set<String> filesToConvert = Sets.newLinkedHashSet(opts.filesToConvert);

    GentsResult result = generateTypeScript(filesToConvert, srcFiles, externFiles);
    Map<String, String> resultFileMap = result.sourceFileMap;

    for (String filename : filesToConvert) {
      String relativePath = pathUtil.getRelativePath(".", filename);
      String filepath = pathUtil.getFilePathWithoutExtension(relativePath);
      String tsCode = resultFileMap.get(filepath);
      if ("-".equals(opts.output)) {
        System.out.println("========================================");
        System.out.println("File: " + relativePath);
        System.out.println("========================================");
        System.out.println(tsCode);
      } else {
        String tsFilename = pathUtil.removeExtension(relativePath) + ".ts";
        File output = new File(new File(opts.output), tsFilename);
        if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
          throw new IllegalArgumentException("Unable to make directories " + output.getParent());
        }
        try {
          Files.asCharSink(output, UTF_8).write(tsCode);
        } catch (IOException e) {
          throw new IllegalArgumentException("Unable to write to file " + output.getName(), e);
        }
      }
    }
    try {
      if (opts.moduleRewriteLog != null) {
        Files.asCharSink(new File(opts.moduleRewriteLog), UTF_8).write(result.moduleRewriteLog);
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to write to file " + opts.moduleRewriteLog, e);
    }
  }

  /** Returns a map from the basename to the TypeScript code generated for the file. */
  public GentsResult generateTypeScript(
      Set<String> filesToConvert, List<SourceFile> originalSrcFiles, List<SourceFile> externs)
      throws IOException {
    GentsResult result = new GentsResult();

    // Encode blank line information in the source files.  At the end of the pipeline,
    // the blank line information encoded in the source files is decoded and used to
    // ensure the output has the correct number and location of blank lines consistent
    // with the input source.
    List<SourceFile> srcFiles = Lists.newArrayList();
    for (SourceFile sf : originalSrcFiles) {
      String code = sf.getCode();
      String encodedCode = BlankLineHandler.encodeBlankLineInformation(code);
      srcFiles.add(SourceFile.fromCode(sf.getName(), encodedCode));
    }

    final CompilerOptions compilerOpts = opts.getCompilerOptions();
    // needed to associate comments with nodes for the AstCommentLinkingPass
    compilerOpts.setParseJsDocDocumentation(JsDocParsing.INCLUDE_ALL_COMMENTS);
    // needed to print trailing commas in object literals, arrays, etc.
    compilerOpts.setPrettyPrint(true);

    // Compile javascript code
    compiler.compile(externs, srcFiles, compilerOpts);

    Node externRoot = compiler.getRoot().getFirstChild();
    Node srcRoot = compiler.getRoot().getLastChild();

    // It is important that this pass is the first pass run so that comments can be associated
    // with the correct nodes in the AST before other passes run and modify the AST.
    // See the documentation in the AstCommentLinkingPass for more information.
    AstCommentLinkingPass astCommentPass =
        new AstCommentLinkingPass(compiler, new SourceExtractor(srcFiles));
    astCommentPass.process(externRoot, srcRoot);
    final NodeComments astComments = astCommentPass.getComments();

    new RemoveGoogScopePass(compiler).process(externRoot, srcRoot);

    CollectModuleMetadata modulePrePass =
        new CollectModuleMetadata(compiler, nameUtil, filesToConvert);
    modulePrePass.process(externRoot, srcRoot);

    // Strips all file nodes that we are not compiling.
    stripNonCompiledNodes(srcRoot, filesToConvert);

    CommentLinkingPass commentsPass = new CommentLinkingPass(compiler);
    commentsPass.process(externRoot, srcRoot);
    final NodeComments comments = commentsPass.getComments();

    ModuleConversionPass modulePass =
        new ModuleConversionPass(
            compiler,
            pathUtil,
            nameUtil,
            modulePrePass.getFileMap(),
            modulePrePass.getNamespaceMap(),
            comments,
            astComments,
            opts.alreadyConvertedPrefix);
    modulePass.process(externRoot, srcRoot);

    new TypeConversionPass(compiler, modulePrePass, comments).process(externRoot, srcRoot);

    new TypeAnnotationPass(
            compiler,
            pathUtil,
            nameUtil,
            modulePrePass.getSymbolMap(),
            modulePass.getTypeRewrite(),
            comments,
            opts.externsMap)
        .process(externRoot, srcRoot);

    new StyleFixPass(compiler, comments).process(externRoot, srcRoot);

    // We only use the source root as the extern root is ignored for codegen
    for (Node file : srcRoot.children()) {
      try {
        String filepath = pathUtil.getFilePathWithoutExtension(file.getSourceFileName());
        CodeGeneratorFactory factory =
            new CodeGeneratorFactory() {
              @Override
              public CodeGenerator getCodeGenerator(Format outputFormat, CodeConsumer cc) {
                return new GentsCodeGenerator(
                    cc,
                    compilerOpts,
                    astComments,
                    comments,
                    opts.externsMap,
                    new SourceExtractor(srcFiles),
                    compiler,
                    srcRoot);
              }
            };

        String tsCode =
            new CodePrinter.Builder(file)
                .setCompilerOptions(opts.getCompilerOptions())
                .setTypeRegistry(compiler.getTypeRegistry())
                .setCodeGeneratorFactory(factory)
                .setPrettyPrint(true)
                .setLineBreak(true)
                .setOutputTypes(true)
                .build();

        // decode blank line information from the generated source code to
        // ensure the output has the correct placement of blank lines
        String decoded = BlankLineHandler.decodeBlankLineInformation(tsCode);

        result.sourceFileMap.put(filepath, tryClangFormat(decoded));
      } catch (Throwable t) {
        System.err.println("Failed while converting " + file.getSourceFileName());
        t.printStackTrace(System.err);
        compiler.report(
            JSError.make(file.getSourceFileName(), -1, -1, GENTS_INTERNAL_ERROR, t.getMessage()));
      }
    }

    result.moduleRewriteLog =
        new ModuleRenameLogger()
            .generateModuleRewriteLog(filesToConvert, modulePrePass.getNamespaceMap());
    errorManager.doGenerateReport();
    return result;
  }

  /**
   * Attempts to format the generated TypeScript using clang-format. On failure to format (i.e.
   * clang-format does not exist), return the inputed string.
   */
  private static String tryClangFormat(String code) {
    Process process = null;
    try {
      process = new ProcessBuilder().command(CLANG_FORMAT).start();
      final OutputStream stdin = process.getOutputStream();
      // stdout must be final for the nested object byteSource to return it.
      final InputStream stdout = process.getInputStream();

      // Write TypeScript code to stdin of the process
      try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin, UTF_8)); ) {
        writer.write(code);
        writer.close();
      }
      return readStream(stdout);
    } catch (IOException e) {
      System.err.println("clang-format has failed to execute: " + e.getMessage());
      return code;
    } finally {
      if (process != null) {
        try {
          System.err.println(readStream(process.getErrorStream()));
        } catch (
            @SuppressWarnings("unused")
            IOException ignored) {
          // Ignored.
        }
        // TODO(renez): Use .waitFor(n, TimeUnit.SECONDS) and .destroyForcibly() once we moved to
        // Java 8.
        process.destroy();
      }
    }
  }

  private static String readStream(final InputStream stream) throws IOException {
    ByteSource byteSource =
        new ByteSource() {
          @Override
          public InputStream openStream() throws IOException {
            return stream;
          }
        };
    return byteSource.asCharSource(UTF_8).read();
  }

  /** Removes the root nodes for all the library files from the source node. */
  private static void stripNonCompiledNodes(Node n, Set<String> filesToCompile) {
    for (Node child : n.children()) {
      if (!filesToCompile.contains(child.getSourceFileName())) {
        child.detach();
      }
    }
  }

  static List<Path> getJsEntryPathsFromZip(String source) throws IOException {
    try (ZipFile zipFile = new ZipFile(source)) {
      return zipFile.stream()
          .filter(e -> !e.isDirectory())
          .filter(e -> e.getName().endsWith(".js"))
          .map(e -> source + "!/" + e.getName())
          .map(Paths::get)
          .collect(toList());
    } catch (IOException e) {
      throw new IOException("failed to read zip file " + source, e);
    }
  }

  /** Returns a list of source files from a list of file names. */
  private static List<SourceFile> getFiles(Collection<String> fileNames) throws IOException {
    List<SourceFile> files = new ArrayList<>(fileNames.size());
    for (String fileName : fileNames) {
      if (!fileName.endsWith(".zip")) {
        files.add(SourceFile.fromFile(fileName, UTF_8));
        continue;
      }

      getJsEntryPathsFromZip(fileName).stream()
          .map(p -> SourceFile.fromPath(p, UTF_8))
          .forEach(files::add);
    }
    return files;
  }

  static class GentsResult {

    public Map<String, String> sourceFileMap = new LinkedHashMap<>();
    public String moduleRewriteLog = "";
  }
}
