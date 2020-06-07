package com.tersesystems.blindsight

import com.tersesystems.blindsight
import org.slf4j
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
    val state = LoggerState(Markers.empty, underlying, Condition.always, SourceInfoBehavior.empty)
    new DefaultCoreLogger(state)
  }
}

final case class LoggerState(
    markers: Markers,
    underlying: org.slf4j.Logger,
    condition: Condition,
    sourceInfoBehavior: SourceInfoBehavior
)

class DefaultCoreLogger(val state: LoggerState) extends CoreLogger {
  private val parameterLists: Seq[ParameterList] = ParameterList.lists(this.underlying)

  override def withMarker[M: ToMarkers](m: M): CoreLogger = {
    val markers  = implicitly[ToMarkers[M]].toMarkers(m)
    val newState = state.copy(markers = this.markers + markers)
    new DefaultCoreLogger(newState)
  }

  override def onCondition(c: Condition): CoreLogger = {
    val newCondition: Condition = Condition(level => state.condition(level) && c(level))
    val newState                = state.copy(condition = newCondition)
    new ConditionalCoreLogger(new DefaultCoreLogger(newState))
  }

  @inline
  def parameterList(level: Level): ParameterList = parameterLists(level.ordinal)

  override def predicate(level: Level): SimplePredicate =
    new SimplePredicate.Impl(level, this)

  override def markers: Markers = state.markers

  override def underlying: slf4j.Logger = state.underlying

  override def condition: Condition = state.condition

  override def sourceInfoBehavior: SourceInfoBehavior = state.sourceInfoBehavior
}

class ConditionalCoreLogger(coreLogger: DefaultCoreLogger)
    extends DefaultCoreLogger(coreLogger.state) {
  @inline
  override def parameterList(level: Level): ParameterList =
    new ParameterList.Conditional(level, coreLogger)
}
