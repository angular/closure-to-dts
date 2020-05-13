package com.google.javascript.gents;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.parsing.Config;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StopOptionHandler;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

/**
 * A class that parses the command line arguments and generates {@code CompilerOptions} to use with
 * Closure Compiler.
 */
public class Options {

  @Option(name = "-o", usage = "output to this directory", metaVar = "OUTPUT")
  String output = "-";

  @Option(name = "--root", usage = "root directory of imports", metaVar = "ROOT")
  String root = ".";

  @Option(name = "--debug", usage = "run in debug mode (prints compiler warnings)")
  boolean debug = false;

  @Option(
    name = "--log",
    usage = "output a log of module rewriting to this location",
    metaVar = "MODULE_REWRITE_LOG"
  )
  String moduleRewriteLog = null;

  @Option(
      name = "--dependenciesManifest",
      usage =
          "the path to a manifest file containing all dependencies\n"
              + "Passing dependency files is disallowed when \"--dependenciesManifest\" option is"
              + " used",
      metaVar = "DEPENDENCIES_MANIFEST")
  private String dependenciesManifest = null;

  @Option(
    name = "--sourcesManifest",
    usage =
        "the path to a manifest file containing all files that need to be converted to TypeScript\n"
            + "\"--convert\" option is disallowed when \"--sourcesManifest\" option is used",
    metaVar = "SOURCES_MANIFEST"
  )
  private String sourcesManifest = null;

  @Option(
    name = "--convert",
    usage =
        "list of all files to be converted to TypeScript\n"
            + "This list of files does not have to be mutually exclusive from the source files",
    metaVar = "CONV...",
    handler = StringArrayOptionHandler.class
  )
  List<String> filesToConvert = new ArrayList<>();

  @Option(
    name = "--externs",
    usage = "list of files to read externs definitions (as separate args)",
    metaVar = "EXTERN...",
    handler = StringArrayOptionHandler.class
  )
  List<String> externs = new ArrayList<>();

  @Option(
    name = "--externsMap",
    usage = "File mapping externs to their TypeScript typings equivalent. Formatted as json",
    metaVar = "EXTERNSMAP"
  )
  String externsMapFile = null;

  /*
   * Note that the override simply overwrites the mapping specified in --externsMap.
   * It does not follow any transitive connections between mappings in --externsMap to
   * change other mappings in --externsMap.
   *
   * For example, suppose --externsMap specifies:
   *
   *   A -> B
   *   B -> C
   *   X -> Y
   *
   * and --externsOverride=B:Z is specified.  Then, the resulting externs mapping will be
   *
   *   A -> B
   *   B -> Z
   *   X -> Y
   *
   * Note that the B:Z just overrides the B->C in the original --externsMap to instead be
   * B->Z.
   *
   * That is, although the externs map specifies A->B and B->C and the override specifies B->Z,
   * the --externsMap is not modified to have A->Z.  Only the current mapping of B is changed.
   *
   * Note also that if --externsOverride=P:Q is specified (with the original --externsMap), then
   * the resulting --externsMap will be:
   *
   *   A -> B
   *   B -> C
   *   X -> Y
   *   P -> Q
   *
   * That is, the P->Q mapping will be added to the --externsMap.
   */
  @Option(
      name = "--externsOverride",
      usage = "list of externs to override in the form <oldType>:<newType> (as separate args)",
      metaVar = "EXTERNSOVERRIDE...")
  List<String> externsOverride = new ArrayList<>();

  @Option(
    name = "--alreadyConvertedPrefix",
    usage = "Goog.modules starting with this prefix are assumed to be already in TypeScript",
    metaVar = "ALREADY_CONVERTED_PREFIX"
  )
  String alreadyConvertedPrefix = "google3";

  @Option(
    name = "--absolutePathPrefix",
    usage = "Prefix for emitting absolute module references in import statements.",
    metaVar = "ABSOLUTE_PATH_PREFIX"
  )
  String absolutePathPrefix = "google3";

  @Argument
  @Option(name = "--", handler = StopOptionHandler.class)
  List<String> arguments = new ArrayList<>();

  Set<String> srcFiles = new LinkedHashSet<>();
  Map<String, String> externsMap = null;

  CompilerOptions getCompilerOptions() {
    final CompilerOptions options = new CompilerOptions();
    options.setClosurePass(true);

    // Turns off common warning messages, when PhaseOptimizer decides to skip some passes due to
    // unsupported code constructs. They are not very actionable to users and do not matter to
    // gents.
    Logger phaseLogger = Logger.getLogger("com.google.javascript.jscomp.PhaseOptimizer");
    phaseLogger.setLevel(Level.OFF);

    options.setCheckGlobalNamesLevel(CheckLevel.ERROR);
    // Report duplicate definitions, e.g. for accidentally duplicated externs.
    options.setWarningLevel(DiagnosticGroups.DUPLICATE_VARS, CheckLevel.ERROR);

    options.setLanguage(CompilerOptions.LanguageMode.ECMASCRIPT_NEXT);
    // We have to emit _TYPED, as any other mode triggers optimizations
    // in the Closure code printer. Notably optimizations break typed
    // syntax for arrow functions, by removing the surrounding parens
    // emitting `foo(x:string => x)`, which is invalid.
    options.setLanguageOut(LanguageMode.ECMASCRIPT6_TYPED);

    // Do not transpile module declarations
    options.setWrapGoogModulesForWhitespaceOnly(false);
    // Stop escaping the characters "=&<>"
    options.setTrustedStrings(true);
    options.setPreferSingleQuotes(true);

    // Compiler passes must be disabled to disable down-transpilation to ES5.
    options.skipAllCompilerPasses();
    // turns off optimizations.
    options.setChecksOnly(true);
    options.setPreserveDetailedSourceInfo(true);
    // Changing this to INCLUDE_DESCRIPTIONS_WITH_WHITESPACE, which feels more natural to the goals
    // of gents, makes no difference in golden test. Likely we ignore this info.
    options.setParseJsDocDocumentation(Config.JsDocParsing.INCLUDE_DESCRIPTIONS_NO_WHITESPACE);

    options.clearConformanceConfigs();

    return options;
  }

  private Map<String, String> getExternsMap() throws IOException {
    Map<String, String> externsMap = getFileExternsMap();
    if (this.externsOverride != null) {
      Splitter splitter = Splitter.on(':').trimResults().omitEmptyStrings();
      for (String mapping : this.externsOverride) {
        List<String> parts = Lists.newArrayList(splitter.split(mapping));
        if (parts.size() != 2) {
          throw new IllegalArgumentException(
              "Expected an externsOverride to be of the "
                  + "form <oldType>:<newType> but found '"
                  + mapping
                  + "'");
        }
        String oldType = parts.get(0);
        String newType = parts.get(1);
        externsMap.put(oldType, newType);
      }
    }
    return ImmutableMap.copyOf(externsMap);
  }

  private Map<String, String> getFileExternsMap() throws IOException {
    if (this.externsMapFile != null) {
      Type mapType =
          new TypeToken<Map<String, String>>() {
            /* empty */
          }.getType();
      try (JsonReader reader =
          new JsonReader(
              Files.newBufferedReader(Paths.get(externsMapFile), StandardCharsets.UTF_8))) {
        return new Gson().fromJson(reader, mapType);
      }
    } else {
      return new HashMap<>();
    }
  }

  Options(String[] args) throws CmdLineException {
    CmdLineParser parser = new CmdLineParser(this);
    parser.parseArgument(args);

    if (!filesToConvert.isEmpty() && sourcesManifest != null) {
      throw new CmdLineException(
          parser,
          "Don't specify a sources manifest file and source files (\"--convert\") at the same"
              + " time.");
    }
    if (!arguments.isEmpty() && dependenciesManifest != null) {
      throw new CmdLineException(
          parser,
          "Don't specify a dependencies manifest file and dependency files as arguments at the"
              + " same time.");
    }

    if (sourcesManifest != null) {
      try {
        filesToConvert = Files.readAllLines(Paths.get(sourcesManifest), StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new CmdLineException(
            parser, "sources manifest file " + sourcesManifest + " not found.", e);
      }
    }

    if (dependenciesManifest == null) {
      srcFiles.addAll(arguments);
    } else {
      try {
        srcFiles.addAll(
            Files.readAllLines(Paths.get(dependenciesManifest), StandardCharsets.UTF_8));
      } catch (IOException e) {
        throw new CmdLineException(
            parser, "dependencies manifest file " + dependenciesManifest + " not found.", e);
      }
    }

    srcFiles.addAll(filesToConvert);

    if (srcFiles.isEmpty()) {
      throw new CmdLineException(parser, "No files were given");
    }

    try {
      externsMap = getExternsMap();
    } catch (IOException e) {
      throw new CmdLineException(parser, "externs file " + externsMapFile + " not found.", e);
    }
  }

  Options() {
    externsMap = ImmutableMap.of();
  }

  Options(String externsMapFile) throws IOException {
    this(externsMapFile, Lists.newArrayList());
  }

  Options(String externsMapFile, List<String> externsOverride) throws IOException {
    this.externsMapFile = externsMapFile;
    this.externsOverride = externsOverride;
    externsMap = getExternsMap();
  }
}
