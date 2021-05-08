package com.tersesystems.blindsight.scripting

import java.io.{FileNotFoundException, IOException, Reader, StringReader}
import java.nio.file.{Files, Path}
import java.nio.file.attribute.FileTime
import java.util.concurrent.atomic.AtomicReference

trait ConditionSource {

  def isInvalid: Boolean

  def script: String
}


class FileConditionSource(val path: Path, verifier: String => Boolean) extends ConditionSource {

  if (!Files.exists(path)) throw new FileNotFoundException(path.toAbsolutePath.toString)

  private val lastModified = new AtomicReference[FileTime](Files.getLastModifiedTime(path))

  override def isInvalid: Boolean = {
    val newTime = Files.getLastModifiedTime(path)
    if (newTime.compareTo(lastModified.get) > 0) {
      lastModified.set(newTime)
      true
    }
    else false
  }

  override def script: String = {
    val str = Files.readString(path)
    if (verifier(str)) {
      str
    } else {
      throw new IllegalStateException(s"Failed signature check on $path")
    }
  }
}
