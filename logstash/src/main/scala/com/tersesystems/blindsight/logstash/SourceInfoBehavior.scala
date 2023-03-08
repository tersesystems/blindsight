package com.tersesystems.blindsight.logstash

import com.tersesystems.blindsight.AST._
import com.tersesystems.blindsight.Markers
import com.tersesystems.blindsight.core.SourceInfoBehavior
import sourcecode._

object SourceInfoBehavior {

  class Impl(
      fileLabel: String,
      lineLabel: String,
      enclosingLabel: String
  ) extends SourceCodeImplicits(fileLabel, lineLabel, enclosingLabel)
      with SourceInfoBehavior {
    override def apply(line: Line, file: File, enclosing: Enclosing): Markers = {
      val obj: BObject = BObject(List(line, file, enclosing))
      Markers(obj)
    }
  }
}

/**
 * This trait converts SourceCode to BFields, using labels.
 *
 * {{{
 * val implicits = new SourceCodeImplicits(/* labels */)
 * import implicits._
 * }}}
 */
class SourceCodeImplicits(
    fileLabel: String,
    lineLabel: String,
    enclosingLabel: String
) {
  import com.tersesystems.blindsight.AST._

  implicit def fileToField(file: File): BField = fileLabel -> BString(file.value)

  implicit def lineToField(line: Line): BField = lineLabel -> BInt(line.value)

  implicit def enclosingToField(enclosing: Enclosing): BField =
    enclosingLabel -> BString(enclosing.value)
}
