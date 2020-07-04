package com.tersesystems.blindsight

import org.slf4j.event.Level

trait Condition extends ((Level, Markers) => Boolean)

object Condition {

  implicit def functionToCondition(f: => Boolean): Condition = Condition(f)

  def apply(f: => Boolean): Condition =
    new Condition {
      override def apply(level: Level, markers: Markers): Boolean = f
    }

  def apply(predicate: java.util.function.Predicate[Level]): Condition =
    new Condition {
      override def apply(level: Level, markers: Markers): Boolean = predicate.test(level)
    }

  def apply(f: Markers => Boolean): Condition =
    new Condition {
      override def apply(level: Level, markers: Markers): Boolean = f(markers)
    }

  def apply(f: (Level, Markers) => Boolean): Condition =
    new Condition {
      override def apply(level: Level, markers: Markers): Boolean = f(level, markers)
    }

  val always: Condition = Condition(true)

  val never: Condition = Condition(false)
}
