package com.tersesystems.blindsight.scripting

import java.io.FileNotFoundException
import java.nio.file.attribute.FileTime
import java.nio.file.{Files, Path}
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Collectors

trait ScriptHandle {
  def isInvalid: Boolean

  def script: String

  def report(e: Throwable): Unit
}

/**
 * A condition source that uses a direct path and verifies it.  Errors are sent to the reporter.
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
