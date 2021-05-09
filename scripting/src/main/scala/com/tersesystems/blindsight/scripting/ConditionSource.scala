package com.tersesystems.blindsight.scripting

import java.io.FileNotFoundException
import java.nio.file.attribute.FileTime
import java.nio.file.{Files, Path}
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Collectors

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
    } else false
  }

  override def script: String = {
    val reader = Files.newBufferedReader(path)
    try {
      val str = reader.lines().collect(Collectors.joining())
      if (verifier(str)) {
        str
      } else {
        throw new IllegalStateException(s"Failed signature check on $path")
      }
    } finally {
      reader.close()
    }
  }
}
