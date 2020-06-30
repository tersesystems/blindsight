package com.tersesystems.blindsight

import com.tersesystems.blindsight.mixins.{
  MarkerMixin,
  OnConditionMixin,
  TransformLogEntryMixin,
  UnderlyingMixin
}
import org.slf4j.event.Level

trait CoreLogger
    extends UnderlyingMixin
    with MarkerMixin
    with OnConditionMixin
    with TransformLogEntryMixin {
  type Self = CoreLogger

  def state: CoreLogger.State

  def condition: Condition

  def sourceInfoBehavior: SourceInfoBehavior

  def predicate(level: Level): SimplePredicate

  def parameterList(level: Level): ParameterList

  def when(level: Level, condition: Condition): Boolean
}

object CoreLogger {

  trait State {
    def markers: Markers
    def underlying: org.slf4j.Logger
    def condition: Condition
    def sourceInfoBehavior: SourceInfoBehavior
    def parameterLists: Array[ParameterList]

    def withParameterLists(lists: Array[ParameterList]): State

    def withMarker[M: ToMarkers](m: M): State

    def onCondition(c: Condition): State
  }

  object State {

    final case class Impl(
        markers: Markers,
        underlying: org.slf4j.Logger,
        condition: Condition,
        sourceInfoBehavior: SourceInfoBehavior,
        parameterLists: Array[ParameterList]
    ) extends State {
      def withMarker[M: ToMarkers](m: M): State = {
        val markers = implicitly[ToMarkers[M]].toMarkers(m)
        copy(markers = this.markers + markers)
      }

      def onCondition(c: Condition): State = {
        val f = (level: Level, s: State) => condition(level, s) && c(level, s)
        copy(condition = Condition(f))
      }

      def withParameterLists(lists: Array[ParameterList]): State = {
        copy(parameterLists = lists)
      }
    }
  }

  def apply(underlying: org.slf4j.Logger): CoreLogger = {
    apply(underlying, SourceInfoBehavior.empty)
  }

  def apply(underlying: org.slf4j.Logger, sourceInfoBehavior: SourceInfoBehavior): CoreLogger = {
    val state = State.Impl(
      Markers.empty,
      underlying,
      Condition.always,
      sourceInfoBehavior,
      ParameterList.lists(underlying)
    )
    new Impl(state)
  }

  abstract class Abstract extends CoreLogger {
    val state: State

    override def predicate(level: Level): SimplePredicate =
      new SimplePredicate.Impl(level, this)

    override def markers: Markers = state.markers

    override def underlying: org.slf4j.Logger = state.underlying

    override def condition: Condition = state.condition

    override def sourceInfoBehavior: SourceInfoBehavior = state.sourceInfoBehavior

    override def parameterList(level: Level): ParameterList = {
      state.parameterLists(level.ordinal())
    }

    override def when(level: Level, condition: Condition): Boolean = {
      // because conditions are AND, if there's a never in the condition we
      // can always return false right off the bat.
      if (condition != Condition.never && condition(level, state)) {
        val list = parameterList(level)
        if (state.markers.isEmpty) {
          list.executePredicate()
        } else {
          list.executePredicate(state.markers.marker)
        }
      } else {
        false
      }
    }

    override def onCondition(c: Condition): CoreLogger = {
      if (c == Condition.never) {
        new Noop(state)
      } else {
        new Conditional(new Impl(state.onCondition(c)))
      }
    }
  }

  class Impl(val state: State) extends Abstract {
    override def withMarker[M: ToMarkers](m: M): CoreLogger = {
      new Impl(state.withMarker(m))
    }

    override def withTransform(
        level: Level,
        f: LogEntry => LogEntry
    ): CoreLogger = {
      val newParameterLists: Array[ParameterList] = new Array(5)
      state.parameterLists.copyToArray(newParameterLists)
      newParameterLists(level.ordinal()) = new ParameterList.Spy(parameterList(level), f)
      new Impl(state.withParameterLists(newParameterLists))
    }
  }

  class Conditional(impl: Impl) extends Impl(impl.state) {
    @inline
    override def parameterList(level: Level): ParameterList =
      new ParameterList.Conditional(level, impl)

    override def withMarker[M: ToMarkers](m: M): CoreLogger = {
      new Conditional(new Impl(state.withMarker(m)))
    }

    override def withTransform(
        level: Level,
        f: LogEntry => LogEntry
    ): CoreLogger = {
      val newParameterLists: Array[ParameterList] = new Array(5)
      state.parameterLists.copyToArray(newParameterLists)
      newParameterLists(level.ordinal()) = new ParameterList.Spy(parameterList(level), f)
      new Conditional(new Impl(state.withParameterLists(newParameterLists)))
    }
  }

  class Noop(val state: State) extends Abstract {
    override def parameterList(level: Level): ParameterList = ParameterList.Noop

    override def when(level: Level, condition: Condition): Boolean = false

    override def onCondition(condition: Condition): CoreLogger =
      new Noop(state.onCondition(condition))

    override def withMarker[T: ToMarkers](instance: T): CoreLogger =
      new Noop(state.withMarker(instance))

    override def withTransform(
        level: Level,
        f: LogEntry => LogEntry
    ): CoreLogger = {
      this
    }
  }

}
