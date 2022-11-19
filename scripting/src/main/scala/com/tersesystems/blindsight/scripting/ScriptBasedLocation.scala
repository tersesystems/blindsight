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

package com.tersesystems.blindsight.scripting

import com.tersesystems.blindsight.{Condition, Markers}
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

/**
 * A script condition manager that returns conditions tied to a script that
 * can evaluate based on the source code.
 *
 * This can be useful when you only want some logging statements to be evaluated
 * on a script basis.
 *
 * {{{
 * val location = new ScriptBasedLocation(sm, false)
 * logger.debug.when(location.here) { debug =>
 *   debug("Hello world!")
 * }
 * }}}
 *
 * @param sm the script manager
 * @param default the default to return if the script does not complete.
 */
class ScriptBasedLocation(sm: ScriptManager, default: Boolean) {

  def here(implicit line: Line, enclosing: Enclosing, file: File): Condition = {
    new ScriptCondition(line, enclosing, file)
  }

  class ScriptCondition(line: Line, enclosing: Enclosing, file: File) extends Condition {
    override def apply(level: Level, markers: Markers): Boolean = {
      sm.execute(default, level, enclosing, line, file)
    }
  }
}
