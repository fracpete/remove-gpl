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
 * PatternUtils.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.removegpl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Pattern-related methods.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class PatternUtils {

  /** the default patterns. */
  public final static String[] DEFAULT_PATTERNS = new String[]{
    "com/github/fracpete/removegpl/java_jflex_cup.props",
    "com/github/fracpete/removegpl/python.props",
  };

  /** the filepattern key. */
  public final static String KEY_FILEPATTERN = "filepattern";

  /** the number of patterns key. */
  public final static String KEY_NUMPATTERNS = "numpatterns";

  /** the find prefix key. */
  public final static String KEY_FIND = "find";

  /** the replace prefix key. */
  public final static String KEY_REPLACE = "replace";

  /**
   * Checks a pattern file.
   *
   * @param props	the pattern file to check
   * @return		null if successful, otherwise error message
   */
  public static String checkPattern(Properties props) {
    String 	numStr;
    int		num;
    int		i;
    String	key;

    if (!props.containsKey(KEY_FILEPATTERN))
      return "Missing key: " + KEY_FILEPATTERN;
    try {
      Pattern.compile(props.getProperty(KEY_FILEPATTERN));
    }
    catch (Exception e) {
      return "Failed to compile regexp: " + props.getProperty(KEY_FILEPATTERN);
    }

    if (!props.containsKey(KEY_NUMPATTERNS))
      return "Missing key: " + KEY_NUMPATTERNS;
    numStr = props.getProperty(KEY_NUMPATTERNS);
    try {
      num = Integer.parseInt(numStr);
    }
    catch (Exception e) {
      return "Failed to parse value for '" + KEY_NUMPATTERNS + "': " + numStr;
    }

    for (i = 0; i < num; i++) {
      // find
      key = KEY_FIND + (i+1);
      if (!props.containsKey(key))
        return "Missing key: " + key;
      try {
        Pattern.compile(props.getProperty(key));
      }
      catch (Exception e) {
        return "Failed to compile find pattern '" + key + ": " + e;
      }

      // replace
      key = KEY_REPLACE + (i+1);
      if (!props.containsKey(key))
        return "Missing key: " + key;
    }

    return null;
  }

  /**
   * Loads and checks a pattern file from the input stream.
   *
   * @param in		the stream to load from
   * @return		the properties object
   * @throws Exception	if failed to load or invalid format
   */
  public static Properties loadPattern(InputStream in) throws Exception {
    Properties	result;
    String	check;

    result = new Properties();
    result.load(in);

    check = checkPattern(result);
    if (check != null)
      throw new IllegalStateException(check);

    return result;
  }

  /**
   * Updates the file using the provided patterns.
   *
   * @param f		the file to process
   * @param props	the patterns to use
   * @param verbose 	true if verbose mode
   * @param dryRun 	does not perform a write operation
   * @return		null if sucessfully processed, otherwise error message
   */
  public static String processFile(File f, Properties props, boolean verbose, boolean dryRun) {
    String		result;
    List<String>	original;
    List<String>	updated;
    int			num;
    int			i;
    int			n;
    String		find;
    String		replace;
    boolean		different;

    result = null;

    if (verbose)
      System.out.println("Processing file '" + f + "' using: " + props.getProperty(PatternUtils.KEY_FILEPATTERN));

    original = FileUtils.loadFile(f);
    if (original == null)
      return "Failed to load file: " + f;
    updated = new ArrayList<>(original);

    // apply patterns
    num = Integer.parseInt(props.getProperty(KEY_NUMPATTERNS));
    for (i = 0; i < num; i++) {
      find    = props.getProperty(KEY_FIND + (i+1));
      replace = props.getProperty(KEY_REPLACE + (i+1));
      for (n = 0; n < updated.size(); n++)
        updated.set(n, updated.get(n).replaceAll(find, replace));
    }

    // changed?
    different = false;
    for (i = 0; i < original.size(); i++) {
      if (!original.get(i).equals(updated.get(i))) {
        different = true;
        break;
      }
    }
    if (different) {
      if (!dryRun)
	result = FileUtils.saveFile(f, updated);
      if (verbose)
	System.out.println(" --> updated" + (dryRun ? " [dry-run]" : ""));
    }

    return result;
  }
}
