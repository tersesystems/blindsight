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