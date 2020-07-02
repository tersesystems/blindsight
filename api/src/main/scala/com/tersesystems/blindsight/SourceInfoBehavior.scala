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
