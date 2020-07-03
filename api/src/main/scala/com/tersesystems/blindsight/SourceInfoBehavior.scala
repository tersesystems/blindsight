package com.tersesystems.blindsight

import sourcecode.{Enclosing, File, Line}

trait SourceInfoBehavior {
  def apply(
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Markers
}
