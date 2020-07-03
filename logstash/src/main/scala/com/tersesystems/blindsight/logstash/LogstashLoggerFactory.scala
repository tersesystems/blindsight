/*
 * Copyright 2020 Terse Systems
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
import com.tersesystems.blindsight.core.{CoreLogger, SourceInfoBehavior}
import sourcecode.{Enclosing, File, Line}

/**
 * A logger factory that returns logstash enabled loggers.
 */
class LogstashLoggerFactory extends LoggerFactory {
  override def getLogger[T: LoggerResolver](instance: T): Logger = {
    val underlying = implicitly[LoggerResolver[T]].resolveLogger(instance)
    new Logger.Impl(CoreLogger(underlying, sourceInfoBehavior(underlying)))
  }

  private def sourceInfoBehavior(underlying: org.slf4j.Logger): Option[SourceInfoBehavior] = {
    if (sourceInfoEnabled(underlying)) {
      Some(sourceInfoAsMarker)
    } else {
      None
    }
  }

  private def sourceInfoEnabled(underlying: org.slf4j.Logger): Boolean = {
    // We know this is Logback, so we can ask the logger context for data
    val logbackLogger = underlying.asInstanceOf[ch.qos.logback.classic.Logger]
    val enabled =
      logbackLogger.getLoggerContext.getProperty(LogstashLoggerFactory.SourceEnabledProperty)
    java.lang.Boolean.parseBoolean(enabled)
  }

  private val sourceInfoAsMarker: SourceInfoBehavior = new MarkerSourceInfoBehavior()

  final class MarkerSourceInfoBehavior extends SourceInfoBehavior {
    override def apply(line: Line, file: File, enclosing: Enclosing): Markers = {
      import com.tersesystems.blindsight.AST.BField
      import com.tersesystems.blindsight.DSL._
      import com.tersesystems.blindsight.core.SourceCodeImplicits._
      Markers((line: BField) ~ file ~ enclosing)
    }
  }
}

object LogstashLoggerFactory {
  val SourceEnabledProperty = "blindsight.source.enabled"
}
