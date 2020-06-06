package com.tersesystems.blindsight

import org.slf4j.event.Level

trait CoreLogger {

  def markers: Markers

  def underlying: org.slf4j.Logger

  def condition: Condition

  def sourceInfoBehavior: SourceInfoBehavior

  def predicate(level: Level): SimplePredicate

  def parameterList(level: Level): ParameterList

  def withMarker[M: ToMarkers](markers: M): CoreLogger

  def onCondition(condition: Condition): CoreLogger
}

object CoreLogger {
  def apply(underlying: org.slf4j.Logger): CoreLogger = {
    DefaultCoreLogger(Markers.empty, underlying, Condition.always, SourceInfoBehavior.empty)
  }
}

final case class DefaultCoreLogger(
    markers: Markers,
    underlying: org.slf4j.Logger,
    condition: Condition,
    sourceInfoBehavior: SourceInfoBehavior
) extends CoreLogger {
  private val parameterLists: Seq[ParameterList] = ParameterList.lists(this.underlying)

  override def withMarker[M: ToMarkers](m: M): CoreLogger = {
    val markers = implicitly[ToMarkers[M]].toMarkers(m)
    copy(markers = this.markers + markers)
  }

  override def onCondition(t: Condition): CoreLogger = {
    copy(condition = level => condition(level) && t(level))
  }

  @inline
  def parameterList(level: Level): ParameterList = parameterLists(level.ordinal)

  override def predicate(level: Level): SimplePredicate =
    new SimplePredicate.Impl(level, this)
}
