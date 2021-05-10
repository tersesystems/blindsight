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

/*
  Running a watch service may be more efficient than calling getLastModifiedTime repeatedly.

 public static void main(String[] args) throws IOException, InterruptedException {
     WatchService watchService = FileSystems.getDefault().newWatchService();
     Path path = Paths.get(System.getProperty("user.home"));
     path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
     WatchKey key;
     while ((key = watchService.take()) != null) {
         for (WatchEvent<?> event : key.pollEvents()) {
             System.out.println("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
         }
         key.reset();
     }

     watchService.close();
 }
*/

/**
 * A condition source that uses a direct path and verifies it.  Errors are sent to the reporter.
 */
class FileScriptHandle(val path: Path, verifier: String => Boolean, reporter: Throwable => Unit) extends ScriptHandle {

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

    val reader = Files.newBufferedReader(path)
    try {
      val str = reader.lines().collect(Collectors.joining("\n"))
      if (verifier(str)) {
        val newTime = Files.getLastModifiedTime(path)
        lastModified.set(newTime)
        str
      } else {
        throw new IllegalStateException(s"Failed signature check on $path")
      }
    } finally {
      reader.close()
    }
  }

  override def report(e: Throwable): Unit = reporter(e)
}

