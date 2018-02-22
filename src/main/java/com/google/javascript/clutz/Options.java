package com.google.javascript.clutz;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.DependencyOptions;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.parsing.Config;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

public class Options {
  /**
   * An {@link OptionHandler} the parses an array of strings as an option.
   *
   * <p>Whereas the builtin {@link org.kohsuke.args4j.spi.StringArrayOptionHandler} will split
   * parameters with spaces in them into their space-delimited components (e.g. {@code "foo bar"}
   * into {@code foo} and {@code bar}), this option handler does not:
   *
   * <pre>
   *  java Example --foo bar baz 'bar baz'
   *  // => Results in 'bar', 'baz', and 'bar baz'
   * </pre>
   *
   * This approach follows the conventions of shell quoting to allow option values with spaces.
   */
  public static class StringArrayOptionHandler extends OptionHandler<String> {
    public StringArrayOptionHandler(CmdLineParser parser, OptionDef option, Setter<String> setter) {
      super(parser, option, setter);
    }

    @Override
    public String getDefaultMetaVariable() {
      return "ARG ...";
    }

    @Override
    public int parseArguments(Parameters params) throws CmdLineException {
      final int paramsSize = params.size();
      for (int i = 0; i < paramsSize; i++) {
        String param = params.getParameter(i);
        if (param.startsWith("-")) {
          return i;
        }

        setter.addValue(param);
      }
      return paramsSize;
    }
  }

  @Option(name = "-o", usage = "output to this file", metaVar = "OUTPUT")
  String output = "-";

  @Option(name = "--debug", usage = "run in debug mode (prints compiler warnings)")
  boolean debug = false;

  @Option(
    name = "--externs",
    usage = "list of files to read externs definitions (as separate args)",
    metaVar = "EXTERN...",
    handler = StringArrayOptionHandler.class
  )
  List<String> externs = new ArrayList<>();

  @Option(
    name = "--depgraphs",
    usage = "only generate output for files listed as a root in the given depgraphs",
    metaVar = "file.depgraph...",
    handler = StringArrayOptionHandler.class
  )
  List<String> depgraphFiles = new ArrayList<>();

  @Option(
    name = "--strict_deps",
    usage =
        "generates no modules for nonroots (but does generate types), so that nonroots "
            + "cannot be imported by TypeScript code."
  )
  boolean strictDeps = false;

  @Option(
    name = "--depgraphs_filter_sources",
    usage = "only include sources from the arguments list that appear in the given depgraphs"
  )
  boolean filterSourcesWithDepgraphs = false;

  @Option(
    name = "--closure_entry_points",
    usage =
        "only generate output for the given entry points to the program. Must be"
            + " goog.provide'd symbols.",
    metaVar = "ENTRYPOINT...",
    handler = StringArrayOptionHandler.class
  )
  List<String> entryPoints = new ArrayList<>();

  @Option(
    name = "--partialInput",
    usage =
        "allow input of incomplete programs. All unknown types will be treated as forward"
            + " declared."
  )
  boolean partialInput;

  @Option(
    name = "--skipEmitRegExp",
    usage =
        "Symbols in files that match this RegExp will not be included in the emit. Note that"
            + "the files would still be part of the internal compilation."
  )
  String skipEmitRegExp = null;

  @Option(
    name = "--googProvides",
    usage =
        "file containing a list of namespaces names that we know come from goog.provides (not goog.modules)"
  )
  String googProvidesFile = null;

  @Option(
    name = "--collidingProvides",
    usage = "file containing a list of names that we know conflict with namespaces"
  )
  String collidingProvidesFile = null;

  // https://github.com/google/closure-compiler/blob/036a6dd24c4b0831838a63f983d63670b1f1a9b6/src/com/google/javascript/jscomp/CommandLineRunner.java#L667
  @Option(
    name = "--tracer_mode",
    hidden = true,
    usage =
        "Shows the duration of each compiler pass and the impact to "
            + "the compiled output size. "
            + "Options: ALL, AST_SIZE, RAW_SIZE, TIMING_ONLY, OFF"
  )
  private CompilerOptions.TracerMode tracerMode = CompilerOptions.TracerMode.OFF;

  @Argument List<String> arguments = new ArrayList<>();

  Depgraph depgraph;
  // TODO(martinprobst): Remove when internal Google is upgraded to a more recent args4j
  // library that supports Pattern arguments.
  Pattern skipEmitPattern;
  Set<String> knownGoogProvides = new HashSet<>();
  Set<String> collidingProvides = new HashSet<>();

  public CompilerOptions getCompilerOptions() {
    final CompilerOptions options = new CompilerOptions();
    options.setClosurePass(true);
    options.setTracerMode(this.tracerMode);

    DependencyOptions deps = new DependencyOptions();
    deps.setDependencySorting(true);
    options.setDependencyOptions(deps);

    if (!this.entryPoints.isEmpty()) {
      options.setManageClosureDependencies(this.entryPoints);
    }

    // All diagnostics are WARNINGs (or off) and thus ignored unless debug == true.
    // Only report issues (and fail for them) that are specifically causing problems for Clutz.
    // The idea is to not do a general sanity check of Closure code, just make sure Clutz works.
    // Report missing types as errors.
    options.setCheckGlobalNamesLevel(CheckLevel.ERROR);
    // Report duplicate definitions, e.g. for accidentally duplicated externs.
    options.setWarningLevel(DiagnosticGroups.DUPLICATE_VARS, CheckLevel.ERROR);

    // Late Provides are errors by default, but they do not prevent clutz from transpiling.
    options.setWarningLevel(DiagnosticGroups.LATE_PROVIDE, CheckLevel.OFF);

    options.setLanguage(LanguageMode.ECMASCRIPT_2017);
    options.setLanguageOut(LanguageMode.ECMASCRIPT5);
    options.setCheckTypes(true);
    options.setInferTypes(true);
    // turns off optimizations.
    options.setChecksOnly(true);
    options.setPreserveDetailedSourceInfo(true);
    options.setParseJsDocDocumentation(Config.JsDocParsing.INCLUDE_DESCRIPTIONS_NO_WHITESPACE);
    if (partialInput) {
      options.setAssumeForwardDeclaredForMissingTypes(true);
      options.setWarningLevel(DiagnosticGroups.MISSING_SOURCES_WARNINGS, CheckLevel.OFF);
    }
    return options;
  }

  Options(String[] args) throws CmdLineException {
    CmdLineParser parser = new CmdLineParser(this);
    parser.parseArgument(args);
    if (skipEmitRegExp != null) {
      skipEmitPattern = Pattern.compile(skipEmitRegExp);
    }
    depgraph = Depgraph.parseFrom(depgraphFiles);
    if (filterSourcesWithDepgraphs) {
      // Clutz still takes the list of files to compile from the outside, because Closure depends
      // on source order in many places. The depgraph files are not sorted, build order is instead
      // established by the outside tool driving compilation (e.g. bazel).
      Set<String> merged = Sets.union(depgraph.getRoots(), depgraph.getNonroots());
      arguments.retainAll(merged);
    }
    // set union command line externs and depgraph.externs.
    Set<String> allExterns = new LinkedHashSet<>();
    allExterns.addAll(depgraph.getRootExterns());
    if (!partialInput) {
      allExterns.addAll(depgraph.getNonrootExterns());
    }
    allExterns.addAll(externs);
    externs = new ArrayList<>(allExterns);

    // Exclude externs that are already in the sources to avoid duplicated symbols.
    arguments.removeAll(externs);
    if (!strictDeps) {
      depgraph = depgraph.withNonrootsAsRoots();
    }
    if (arguments.isEmpty() && externs.isEmpty()) {
      throw new CmdLineException(parser, "No files or externs were given");
    }

    if (googProvidesFile != null) {
      try {
        knownGoogProvides.addAll(Files.readLines(new File(googProvidesFile), UTF_8));
      } catch (IOException e) {
        throw new RuntimeException("Error reading goog provides file " + googProvidesFile, e);
      }
    }
    if (collidingProvidesFile != null) {
      try {
        collidingProvides.addAll(Files.readLines(new File(collidingProvidesFile), UTF_8));
      } catch (IOException e) {
        throw new RuntimeException("Error reading aliased names file " + collidingProvidesFile, e);
      }
    }
  }

  Options() {}
}
