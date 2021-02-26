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

object SourceInfoBehavior {

  class Impl(
      fileLabel: String,
      lineLabel: String,
      enclosingLabel: String
  ) extends SourceCodeImplicits(fileLabel, lineLabel, enclosingLabel)
      with SourceInfoBehavior {
    override def apply(line: Line, file: File, enclosing: Enclosing): Markers = {
      import com.tersesystems.blindsight.AST.BField
      import com.tersesystems.blindsight.DSL._
      Markers((line: BField) ~ file ~ enclosing)
    }
  }
}
