package com.tersesystems.blindsight

import org.slf4j.event.Level



trait Condition extends ((Level, CoreLogger.State) => Boolean)

object Condition {

  implicit def functionToCondition(f: => Boolean): Condition = Condition(f)

  def apply(logger: org.slf4j.Logger): Condition =
    new Condition {
      val parameterLists: Array[ParameterList] = ParameterList.lists(logger)
      override def apply(level: Level, state: CoreLogger.State): Boolean = {
        parameterLists.apply(level.ordinal).executePredicate()
      }
    }

  def apply(f: => Boolean): Condition =
    new Condition {
      override def apply(level: Level, state: CoreLogger.State): Boolean = f
    }

  def apply(predicate: java.util.function.Predicate[Level]): Condition =
    new Condition {
      override def apply(level: Level, state: CoreLogger.State): Boolean = predicate.test(level)
    }

  def apply(f: CoreLogger.State => Boolean): Condition =
    new Condition {
      override def apply(level: Level, state: CoreLogger.State): Boolean = f(state)
    }

  def apply(f: (Level, CoreLogger.State) => Boolean): Condition =
    new Condition {
      override def apply(level: Level, state: CoreLogger.State): Boolean = f(level, state)
    }

  val always: Condition = Condition(true)

  val never: Condition = Condition(false)
}
