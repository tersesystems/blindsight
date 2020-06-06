package com.tersesystems.blindsight

import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

trait SourceInfoBehavior {
  def apply(
      level: Level,
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Markers
}

object SourceInfoBehavior {
  val empty: SourceInfoBehavior = new SourceInfoBehavior {
    override def apply(level: Level, line: Line, file: File, enclosing: Enclosing): Markers =
      Markers.empty
  }
}
