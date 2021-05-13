package com.tersesystems.blindsight.scripting

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.core.CoreLogger
import com.tersesystems.blindsight.fluent.{FluentLogger, FluentMethod}
import com.tersesystems.blindsight.semantic.{SemanticLogger, SemanticMethod}
import com.tersesystems.blindsight.slf4j.{SLF4JLogger, StrictSLF4JMethod, UncheckedSLF4JMethod}
import org.slf4j.Marker
import org.slf4j.event.Level
import sourcecode._

/**
 * A logger that runs through a script manager for every predicate.  The script manager is responsible
 * for evaluating the script and return true or false.  The only thing the logger does here is pass
 * through sourcecode information and the level.
 *
 * If the script fails or throws an exception, the underlying predicate is called as the default.
 */
class ScriptAwareLogger(core: CoreLogger, scriptManager: ScriptManager) extends Logger.Impl(core) {

  override protected val logger = new ScriptAwareSLF4JLogger(core)

  override def strict: SLF4JLogger[StrictSLF4JMethod] = logger

  override lazy val unchecked: SLF4JLogger[UncheckedSLF4JMethod] =
    new ScriptAwareUncheckedSLF4JLogger(core)

  override lazy val fluent: FluentLogger = new ScriptAwareFluentLogger(core)

  override protected def self(core: CoreLogger): Self = {
    new ScriptAwareLogger(core, scriptManager)
  }

  override def semantic[StatementType: NotNothing]: SemanticLogger[StatementType] = {
    new ScriptAwareSemanticLogger(core)
  }

  class ScriptAwareUncheckedSLF4JLogger(core: CoreLogger) extends SLF4JLogger.Unchecked(core) {
    override def self(core: CoreLogger): SLF4JLogger[UncheckedSLF4JMethod] = {
      new ScriptAwareUncheckedSLF4JLogger(core)
    }

    override def method(level: Level): UncheckedSLF4JMethod =
      new UncheckedSLF4JMethod.Impl(level, core) {
        override protected def enabled(implicit
            line: Line,
            file: File,
            enclosing: Enclosing
        ): Boolean = {
          scriptManager.execute(super.enabled, level, enclosing, line, file)
        }

        override def enabled(
            marker: Marker
        )(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
          scriptManager.execute(super.enabled(marker), level, enclosing, line, file)
        }
      }
  }

  class ScriptAwareSemanticLogger[StatementType: NotNothing](core: CoreLogger)
      extends SemanticLogger.Impl[StatementType](core) {
    override protected def self[T: NotNothing](core: CoreLogger): Self[T] = {
      new ScriptAwareSemanticLogger(core)
    }

    override def method(level: Level): Method[StatementType] =
      new SemanticMethod.Impl[StatementType](level, core) {
        override protected def enabled(
            markers: Markers
        )(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
          scriptManager.execute(super.enabled(markers), level, enclosing, line, file)
        }
      }
  }

  class ScriptAwareFluentLogger(core: CoreLogger) extends FluentLogger.Impl(core) {
    override protected def self(core: CoreLogger): Self = {
      new ScriptAwareFluentLogger(core)
    }

    override def method(level: Level): FluentMethod = new FluentMethod.Impl(level, core) {
      override def enabled(
          markers: Markers
      )(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
        scriptManager.execute(super.enabled(markers), level, enclosing, line, file)
      }
    }
  }

  class ScriptAwareSLF4JLogger(core: CoreLogger) extends SLF4JLogger.Strict(core) {
    override protected def self(core: CoreLogger): Self = {
      new ScriptAwareSLF4JLogger(core)
    }

    override def method(level: Level): StrictSLF4JMethod = {
      new StrictSLF4JMethod.Impl(level, core) {

        override def enabled(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
          scriptManager.execute(super.enabled, level, enclosing, line, file)
        }

        override def enabled(
            marker: Marker
        )(implicit line: Line, file: File, enclosing: Enclosing): Boolean = {
          scriptManager.execute(super.enabled(marker), level, enclosing, line, file)
        }
      }
    }
  }
}
