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

import com.tersesystems.blindsight.slf4j.{SLF4JLogger, StrictSLF4JMethod, UncheckedSLF4JMethod}
import com.tersesystems.blindsight.{Logger, LoggerFactory, LoggerResolver, Markers}
import org.slf4j.event.Level

/**
 * A logger factory that returns logstash enabled loggers.
 */
class LogstashLoggerFactory extends LoggerFactory {
  override def getLogger[T: LoggerResolver](instance: T): Logger = {
    val underlying = implicitly[LoggerResolver[T]].resolveLogger(instance)
    new Logger.Impl(new LogstashLogger.Strict(underlying, Markers.empty))
  }
}

object LogstashLogger {

  /**
   * Extends the logback logger with logstash markers on source info.
   *
   * @param underlying the slf4j logger.
   * @param markers    the marker state on the logger.
   */
  class Strict(
      underlying: org.slf4j.Logger,
      markers: Markers
  ) extends SLF4JLogger.Base[StrictSLF4JMethod](underlying, markers)
      with LogstashSourceInfoMixin {
    override protected def newInstance(
        underlying: org.slf4j.Logger,
        markerState: Markers
    ): Self = new Strict(underlying, markerState)

    override protected def newMethod(level: Level) = new StrictSLF4JMethod.Impl(level, this)

    override def onCondition(test: => Boolean): SLF4JLogger[StrictSLF4JMethod] = {
      new SLF4JLogger.Strict.Conditional(test, this) with LogstashSourceInfoMixin
    }
  }

  class Unchecked(
      underlying: org.slf4j.Logger,
      markers: Markers
  ) extends SLF4JLogger.Base[UncheckedSLF4JMethod](underlying, markers)
      with LogstashSourceInfoMixin {
    override protected def newInstance(
        underlying: org.slf4j.Logger,
        markerState: Markers
    ): Self = new Unchecked(underlying, markerState)

    override protected def newMethod(level: Level) = new UncheckedSLF4JMethod.Impl(level, this)

    override def onCondition(test: => Boolean): SLF4JLogger[UncheckedSLF4JMethod] = {
      new SLF4JLogger.Unchecked.Conditional(test, this) with LogstashSourceInfoMixin
    }
  }
}
