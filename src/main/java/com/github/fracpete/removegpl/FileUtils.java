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
 * Utils.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.removegpl;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * File-related methods.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class FileUtils {

  /**
   * Closes the stream, if possible, suppressing any exception.
   *
   * @param is		the stream to close
   */
  public static void closeQuietly(InputStream is) {
    if (is != null) {
      try {
	is.close();
      }
      catch (Exception e) {
	// ignored
      }
    }
  }

  /**
   * Closes the stream, if possible, suppressing any exception.
   *
   * @param os		the stream to close
   */
  public static void closeQuietly(OutputStream os) {
    if (os != null) {
      try {
	os.flush();
      }
      catch (Exception e) {
	// ignored
      }
      try {
	os.close();
      }
      catch (Exception e) {
	// ignored
      }
    }
  }

  /**
   * Loads the specified file.
   *
   * @param f		the file to load
   * @return		the lines of the file, null in case of an error
   */
  public static List<String> loadFile(File f) {
    try {
      return Files.readAllLines(f.toPath());
    }
    catch (Exception e) {
      System.err.println("Failed to read file: " + f);
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Savs the specified file.
   *
   * @param f		the file to save to
   * @param content 	the content lines of the file
   * @return		null if successful, otherwise error message
   */
  public static String saveFile(File f, List<String> content) {
    try {
      Files.write(f.toPath(), content, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
      return null;
    }
    catch (Exception e) {
      return "Failed to write content to '" + f + "': " + e;
    }
  }
}
