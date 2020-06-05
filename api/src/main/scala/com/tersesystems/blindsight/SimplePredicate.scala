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

package com.tersesystems.blindsight

import org.slf4j.event.Level

/**
 * This is the predicate that must be met before logging can happen.
 *
 * It corresponds to the SLF4J "isLoggingDebug" / "isLoggingDebug(marker)" calls.
 */
trait SimplePredicate {

  /**
   * "isLogging*()" with no arguments.
   *
   * @return true if logging should happen, false otherwise.
   */
  def apply(): Boolean

  /**
   * "isLogging*(marker)" with a single marker argument.
   *
   * @param instance an instance which can be resolved to a marker through the ToMarker type class.
   * @tparam T the type of the instance.
   * @return true if logging should happen, false otherwise.
   */
  def apply[T: ToMarkers](instance: T): Boolean
}

object SimplePredicate {

  /**
   * This class does the work of calling the predicate methods on SLF4J: no-args and marker essentially.
   */
  class Impl(val level: Level, logger: LoggerState) extends SimplePredicate {
    protected val parameterList: ParameterList = logger.parameterList(level)

    override def apply(): Boolean = {
      if (logger.markers.nonEmpty)
        executePredicate(logger.markers)
      else
        executePredicate
    }

    override def apply[T: ToMarkers](instance: T): Boolean = {
      val markers = implicitly[ToMarkers[T]].toMarkers(instance)
      executePredicate(logger.markers + markers)
    }

    protected def executePredicate(markers: Markers): Boolean = {
      parameterList.executePredicate(markers.marker)
    }

    protected def executePredicate: Boolean = {
      parameterList.executePredicate()
    }
  }
}
