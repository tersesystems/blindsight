package com.tersesystems.blindsight

import com.tersesystems.blindsight.slf4j.SLF4JPredicate
import org.slf4j.event.Level
import sourcecode.{Enclosing, File, Line}

trait LoggerState {
  def sourceInfoMarker(level: Level, line: Line, file: File, enclosing: Enclosing): Markers

  def parameterList(level: Level): ParameterList

  def predicate(level: Level): SLF4JPredicate

  def withMarker[M: ToMarkers](markers: M): LoggerState

  def onCondition(test: () => Boolean): LoggerState

  def markers: Markers

  def underlying: org.slf4j.Logger

  def condition: Option[() => Boolean]
}

final case class DefaultLoggerState(
    markers: Markers,
    underlying: org.slf4j.Logger,
    condition: Option[() => Boolean]
) extends LoggerState {

  override def withMarker[M: ToMarkers](m: M): LoggerState = {
    val markers = implicitly[ToMarkers[M]].toMarkers(m)
    copy(markers = this.markers + markers)
  }

  override def onCondition(t: () => Boolean): LoggerState = {
    condition match {
      case Some(c) =>
        copy(condition = Some(() => c() && t()))
      case None =>
        copy(condition = Some(t))
    }
  }

}
