/*
 * Copyright 2020 com.tersesystems.blindsight
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tersesystems.blindsight.logstash

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.AST._
import com.tersesystems.blindsight.core.{CoreLogger, SourceInfoBehavior}

import sourcecode._

/**
 * A logger factory that returns logstash enabled loggers.
 */
class LogstashLoggerFactory extends LoggerFactory {
  override def getLogger[T: LoggerResolver](instance: T): Logger = {
    val underlying = implicitly[LoggerResolver[T]].resolveLogger(instance)
    new Logger.Impl(CoreLogger(underlying, sourceInfoBehavior(underlying)))
  }

  protected def sourceInfoBehavior(underlying: org.slf4j.Logger): Option[SourceInfoBehavior] = {
    if (sourceInfoEnabled(underlying)) {
      Some(sourceInfoAsMarker(underlying))
    } else {
      None
    }
  }

  protected def sourceInfoEnabled(underlying: org.slf4j.Logger): Boolean = {
    val enabled = property(underlying, LogstashLoggerFactory.SourceEnabledProperty)
    java.lang.Boolean.parseBoolean(enabled.getOrElse(java.lang.Boolean.FALSE.toString))
  }

  protected def property(underlying: org.slf4j.Logger, propertyName: String): Option[String] = {
    val logbackLogger = underlying.asInstanceOf[ch.qos.logback.classic.Logger]
    Option(logbackLogger.getLoggerContext.getProperty(propertyName))
  }

  protected def sourceInfoAsMarker(underlying: org.slf4j.Logger): SourceInfoBehavior = {
    import LogstashLoggerFactory._
    val fileLabel      = property(underlying, SourceFileProperty).getOrElse("source.file")
    val lineLabel      = property(underlying, SourceLineProperty).getOrElse("source.line")
    val enclosingLabel = property(underlying, SourceEnclosingProperty).getOrElse("source.enclosing")
    new SourceInfoBehaviorImpl(fileLabel, lineLabel, enclosingLabel)
  }

  class SourceInfoBehaviorImpl(
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

}

object LogstashLoggerFactory {
  val SourceEnabledProperty   = "blindsight.source.enabled"
  val SourceFileProperty      = "blindsight.source.file"
  val SourceLineProperty      = "blindsight.source.line"
  val SourceEnclosingProperty = "blindsight.source.enclosing"
}
