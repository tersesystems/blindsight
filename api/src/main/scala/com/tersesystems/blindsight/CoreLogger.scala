package com.tersesystems.blindsight

import com.tersesystems.blindsight.mixins.{MarkerMixin, OnConditionMixin, UnderlyingMixin}
import org.slf4j.event.Level

trait CoreLogger extends UnderlyingMixin with MarkerMixin with OnConditionMixin {
  type Self = CoreLogger

  def condition: Condition

  def sourceInfoBehavior: SourceInfoBehavior

  def predicate(level: Level): SimplePredicate

  def parameterList(level: Level): ParameterList
}

object CoreLogger {

  final case class State(
      markers: Markers,
      underlying: org.slf4j.Logger,
      condition: Condition,
      sourceInfoBehavior: SourceInfoBehavior
  )

  def apply(underlying: org.slf4j.Logger): CoreLogger = {
    val state = State(Markers.empty, underlying, Condition.always, SourceInfoBehavior.empty)
    new Impl(state)
  }

  class Impl(val state: State) extends CoreLogger {
    private val parameterLists: Seq[ParameterList] = ParameterList.lists(this.underlying)

    override def withMarker[M: ToMarkers](m: M): CoreLogger = {
      val markers  = implicitly[ToMarkers[M]].toMarkers(m)
      val newState = state.copy(markers = this.markers + markers)
      new Impl(newState)
    }

    override def onCondition(c: Condition): CoreLogger = {
      val newCondition: Condition = Condition(level => state.condition(level) && c(level))
      val newState                = state.copy(condition = newCondition)
      new Conditional(new Impl(newState))
    }

    @inline
    def parameterList(level: Level): ParameterList = parameterLists(level.ordinal)

    override def predicate(level: Level): SimplePredicate =
      new SimplePredicate.Impl(level, this)

    override def markers: Markers = state.markers

    override def underlying: org.slf4j.Logger = state.underlying

    override def condition: Condition = state.condition

    override def sourceInfoBehavior: SourceInfoBehavior = state.sourceInfoBehavior
  }

  class Conditional(coreLogger: Impl) extends Impl(coreLogger.state) {
    @inline
    override def parameterList(level: Level): ParameterList =
      new ParameterList.Conditional(level, coreLogger)
  }

}
