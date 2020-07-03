package com.tersesystems.blindsight.core

import com.tersesystems.blindsight.Markers
import sourcecode.{Enclosing, File, Line}

trait SourceInfoBehavior {
  def apply(
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Markers
}
