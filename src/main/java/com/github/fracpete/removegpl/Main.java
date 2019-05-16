/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Main.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.removegpl;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Removes the GPL preamble from source code files (eg when preparing a commercial release).
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Main
  implements Serializable {

  /** the directory to process. */
  protected File m_Dir;

  /** whether to recursively process files. */
  protected boolean m_Recursive;

  /** whether to use the default patterns. */
  protected boolean m_DefaultPatterns;

  /** the additional pattern files. */
  protected List<File> m_PatternFiles;

  /** whether to use verbose output. */
  protected boolean m_Verbose;

  /** whether to perform a dry-run. */
  protected boolean m_DryRun;

  /** the patterns. */
  protected Map<Pattern,Properties> m_Patterns;

  /**
   * Default constructor.
   */
  public Main() {
    m_Dir             = null;
    m_Recursive       = false;
    m_DefaultPatterns = true;
    m_PatternFiles    = new ArrayList<>();
    m_Verbose         = false;
    m_DryRun          = false;
    m_Patterns        = new HashMap<>();
  }

  /**
   * Sets the directory to process.
   *
   * @param value	the directory
   */
  public void setDir(File value) {
    m_Dir = value;
  }

  /**
   * Returns the directory to process.
   *
   * @return		the dir, null if none set
   */
  public File getDir() {
    return m_Dir;
  }

  /**
   * Sets whether to process the files recursively.
   *
   * @param value	true if recursive
   */
  public void setRecursive(boolean value) {
    m_Recursive = value;
  }

  /**
   * Returns whether to process the files recursively.
   *
   * @return		true if recursive
   */
  public boolean getRecursive() {
    return m_Recursive;
  }

  /**
   * Sets whether to use the default patterns.
   *
   * @param value	true if to use
   */
  public void setDefaultPatterns(boolean value) {
    m_DefaultPatterns = value;
  }

  /**
   * Returns whether to use the default patterns.
   *
   * @return		true if to use
   */
  public boolean getDefaultPatterns() {
    return m_DefaultPatterns;
  }

  /**
   * Sets the pattern files to use.
   *
   * @param value	the directory
   */
  public void setPatternFiles(List<File> value) {
    if (value == null)
      m_PatternFiles = new ArrayList<>();
    else
      m_PatternFiles = value;
  }

  /**
   * Returns the patterns files in use.
   *
   * @return		the files
   */
  public List<File >getPatternFiles() {
    return m_PatternFiles;
  }

  /**
   * Sets whether to turn on verbose mode.
   *
   * @param value	true if verbose
   */
  public void setVerbose(boolean value) {
    m_Verbose = value;
  }

  /**
   * Returns whether to turn on verbose mode.
   *
   * @return		true if verbose
   */
  public boolean getVerbose() {
    return m_Verbose;
  }

  /**
   * Sets whether to perform a dry-run (no writing).
   *
   * @param value	true if dry-run
   */
  public void setDryRun(boolean value) {
    m_DryRun = value;
  }

  /**
   * Returns whether to perform a dry-run (no writing).
   *
   * @return		true if dry-run
   */
  public boolean getDryRun() {
    return m_DryRun;
  }

  /**
   * Sets the commandline options.
   *
   * @param options	the options to use
   * @return		true if successful
   * @throws Exception	in case of an invalid option
   */
  public boolean setOptions(String[] options) throws Exception {
    ArgumentParser 	parser;
    Namespace 		ns;

    parser = ArgumentParsers.newFor(Main.class.getName()).build();
    parser.addArgument("--dir")
      .type(Arguments.fileType().verifyExists().verifyIsDirectory())
      .dest("dir")
      .metavar("dir")
      .required(true)
      .help("The input directory with the source code files.");
    parser.addArgument("--recursive")
      .action(Arguments.storeTrue())
      .required(false)
      .dest("recursive")
      .help("Whether to process files recursively.");
    parser.addArgument("--no-default-patterns")
      .action(Arguments.storeTrue())
      .required(false)
      .dest("nodefaultpatterns")
      .help("Whether to disable the use of the default patterns.");
    parser.addArgument("--pattern-file")
      .type(Arguments.fileType().verifyExists().verifyIsFile())
      .dest("patternfile")
      .nargs("+")
      .required(false)
      .help("The pattern files to apply (eg when not using the built-in ones or additional ones).");
    parser.addArgument("--verbose")
      .action(Arguments.storeTrue())
      .required(false)
      .dest("verbose")
      .help("Whether to process files in verbose mode.");
    parser.addArgument("--dry-run")
      .action(Arguments.storeTrue())
      .required(false)
      .dest("dryrun")
      .help("Files don't get updated when modified.");

    try {
      ns = parser.parseArgs(options);
    }
    catch (ArgumentParserException e) {
      parser.handleError(e);
      return false;
    }

    setDir(ns.get("dir"));
    setRecursive(ns.getBoolean("recursive"));
    setDefaultPatterns(!ns.getBoolean("nodefaultpatterns"));
    setPatternFiles(ns.get("patternfiles"));
    setVerbose(ns.getBoolean("verbose"));
    setDryRun(ns.getBoolean("dryrun"));

    return true;
  }

  /**
   * Performs some checks.
   *
   * @return		null if successful, otherwise error message
   */
  protected String check() {
    if (m_Dir == null)
      return "No directory set!";
    if (!m_Dir.exists())
      return "Directory does not exist: " + m_Dir;
    if (!m_Dir.isDirectory())
      return "Not a directory: " + m_Dir;

    return null;
  }

  /**
   * Loads the patterns.
   *
   * @return		null if successful, otherwise error message
   */
  protected String loadPatterns() {
    Properties		props;
    InputStream		in;
    BufferedInputStream	bin;

    // default patterns
    if (m_DefaultPatterns) {
      for (String pattern: PatternUtils.DEFAULT_PATTERNS) {
        if (m_Verbose)
          System.out.println("Loading default pattern: " + pattern);
        in  = null;
        bin = null;
        try {
          in    = ClassLoader.getSystemResourceAsStream(pattern);
	  bin   = new BufferedInputStream(in);
	  props = PatternUtils.loadPattern(in);
	  m_Patterns.put(Pattern.compile(props.getProperty(PatternUtils.KEY_FILEPATTERN)), props);
	}
	catch (Exception e) {
          return "Failed to load default pattern '" + pattern + "': " + e;
	}
	finally {
          FileUtils.closeQuietly(bin);
          FileUtils.closeQuietly(in);
	}
      }
    }

    // external patterns
    for (File patternFile: m_PatternFiles) {
      if (m_Verbose)
	System.out.println("Loading pattern file: " + patternFile);
      in  = null;
      bin = null;
      try {
	in  = new FileInputStream(patternFile);
	bin = new BufferedInputStream(in);
	props = PatternUtils.loadPattern(in);
	m_Patterns.put(Pattern.compile(props.getProperty(PatternUtils.KEY_FILEPATTERN)), props);
      }
      catch (Exception e) {
        return "Failed to load pattern file '" + patternFile + "': " + e;
      }
      finally {
	FileUtils.closeQuietly(bin);
	FileUtils.closeQuietly(in);
      }
    }

    return null;
  }

  /**
   * Processes the file with the specified patterns.
   *
   * @param f		the file to process
   * @param props	the patterns to apply
   * @return		null if successfully processed, otherwise error message
   */
  protected String process(File f, Properties props) {
    return PatternUtils.processFile(f, props, m_Verbose, m_DryRun);
  }

  /**
   * Processes all the files in the specified directory.
   *
   * @param dir 	the directory to search
   * @return		null if successful, otherwise error message
   */
  protected String remove(File dir) {
    String	result;
    File[]	files;

    if (m_Verbose)
      System.out.println("Processing dir: " + dir);

    files = dir.listFiles();
    if (files == null)
      return null;

    result = null;
    for (File f: files) {
      // skip dot files
      if (f.getName().equals(".") || f.getName().equals(".."))
        continue;

      // recurse dirs?
      if (m_Recursive && f.isDirectory()) {
        result = remove(f);
      }
      else {
        for (Pattern pattern: m_Patterns.keySet()) {
          if (pattern.matcher(f.getName()).matches()) {
            result = process(f, m_Patterns.get(pattern));
            if (result != null)
              break;
	  }
	}
      }
      if (result != null)
	break;
    }

    return result;
  }

  /**
   * Processes all the files.
   *
   * @return		null if successful, otherwise error message
   */
  protected String remove() {
    return remove(m_Dir);
  }

  /**
   * Determines the dependencies.
   *
   * @return		null if successful, otherwise error message
   */
  public String execute() {
    String		result;

    result = check();

    if (result == null) {
      result = loadPatterns();
      if (result != null)
        result = "Failed to load patterns: " + result;
    }

    if (result == null)
      result = remove();

    return result;
  }

  /**
   * Launches the application from the commandline, using the provided options.
   *
   * @param args	the options to use
   * @throws Exception	if setting of options or execution failed
   */
  public static void main(String[] args) throws Exception {
    Main 	main;
    String	error;

    main = new Main();
    if (main.setOptions(args)) {
      error = main.execute();
      if (error != null) {
	System.err.println(error);
	System.exit(2);
      }
    }
    else {
      System.exit(1);
    }
  }
}
