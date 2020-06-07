package com.tersesystems.blindsight

import org.slf4j.event.Level

trait Condition extends (Level => Boolean)

object Condition {

  implicit def functionToCondition(f: => Boolean): Condition = Condition(f)

  def apply(logger: org.slf4j.Logger): Condition =
    new Condition {
      val parameterLists = ParameterList.lists(logger)
      override def apply(level: Level): Boolean = {
        parameterLists.apply(level.ordinal).executePredicate()
      }
    }

  def apply(f: => Boolean): Condition =
    new Condition {
      override def apply(level: Level): Boolean = f
    }

  def apply(f: Level => Boolean): Condition =
    new Condition {
      override def apply(level: Level): Boolean = f(level)
    }

  def apply(predicate: java.util.function.Predicate[Level]): Condition =
    new Condition {
      override def apply(level: Level): Boolean = predicate.test(level)
    }

  val always: Condition = Condition(true)

  val never: Condition = Condition(false)
}
