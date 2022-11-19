/*
 * Copyright 2020 com.tersesystems.blindsight
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tersesystems.blindsight.scripting

import java.io.FileNotFoundException
import java.nio.file.attribute.FileTime
import java.nio.file.{Files, Path}
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Collectors

/**
 * A script handle returns the script source, and can say if the script is invalid.
 */
trait ScriptHandle {

  /**
   * @return true if the script is invalid and should be re-evaluated, false otherwise.
   */
  def isInvalid: Boolean

  /**
   * @return the code of the script.
   */
  def script: String

  /**
   * If evaluating or parsing the script throws an exception, this method is called.
   */
  def report(e: Throwable): Unit
}

/**
 * A script handle that uses a direct path and verifies it.  Errors are sent to the reporter.
 */
class FileScriptHandle(val path: Path, verifier: String => Boolean, reporter: Throwable => Unit)
    extends ScriptHandle {

  private val lastModified = new AtomicReference[FileTime](Files.getLastModifiedTime(path))

  override def isInvalid: Boolean = {
    if (Files.exists(path)) {
      val newTime = Files.getLastModifiedTime(path)
      newTime.compareTo(lastModified.get) > 0
    } else {
      false
    }
  }

  override def script: String = {
    if (!Files.exists(path)) {
      throw new FileNotFoundException(path.toAbsolutePath.toString)
    }

    val str = readString(path)
    if (verifier(str)) {
      val newTime = Files.getLastModifiedTime(path)
      lastModified.set(newTime)
      str
    } else {
      throw new IllegalStateException(s"Failed verify check on $path")
    }
  }

  protected def readString(path: Path): String = {
    val reader = Files.newBufferedReader(path)
    try {
      reader.lines().collect(Collectors.joining("\n"))
    } finally {
      reader.close()
    }
  }

  override def report(e: Throwable): Unit = reporter(e)
}
