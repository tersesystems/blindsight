package com.tersesystems.blindsight

import com.tersesystems.blindsight
import org.slf4j.event.Level

trait LoggerState {
  def predicate(level: Level): SimplePredicate

  def parameterList(level: Level): ParameterList

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
  private val parameterLists: Seq[ParameterList] = ParameterList.lists(this.underlying)

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

  @inline
  def parameterList(level: Level): ParameterList = parameterLists(level.ordinal)

  override def predicate(level: Level): SimplePredicate =
    new blindsight.SimplePredicate.Impl(level, this)
}
