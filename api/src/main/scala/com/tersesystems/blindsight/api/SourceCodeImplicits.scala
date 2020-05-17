package com.tersesystems.blindsight.api

import sourcecode._

/**
 * This trait converts SourceCode to BFields
 */
trait SourceCodeImplicits {
  import AST._
  import DSL._

  implicit def fileToBObject(file: File): BField = "source.file" -> file.value

  implicit def lineToField(line: Line): BField = "source.line" -> line.value

  implicit def enclosingToField(enclosing: Enclosing): BField =
    "source.enclosing" -> enclosing.value

  implicit def argsToField(sourceArgs: Args): BField = {
    val args: Seq[BField] =
      sourceArgs.value.flatMap(_.map(a => a.source -> BString(String.valueOf(a.value))))
    BField("source.arguments", args)
  }
}

object SourceCodeImplicits extends SourceCodeImplicits
