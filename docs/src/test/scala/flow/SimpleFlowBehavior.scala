package flow

import com.tersesystems.blindsight.api.{Statement, ToArguments}
import com.tersesystems.blindsight.flow.FlowBehavior
import com.tersesystems.blindsight.flow.FlowBehavior.Source
import org.slf4j.event.Level

// #flow_behavior
class SimpleFlowBehavior[B: ToArguments] extends FlowBehavior[B] {

  override def entryStatement(source: Source): Option[Statement] = {
    import com.tersesystems.blindsight.logstash.ToArgumentsImplicits._
    Some(
      Statement()
        .withMarkers(entryMarkers(source))
        .withMessage(s"${source.enclosing.value} entry {}")
        .withArguments(source.args)
    )
  }

  override def throwingStatement(
      throwable: Throwable,
      source: Source
  ): Option[(Level, Statement)] = {
    Some(
      Level.ERROR,
      Statement()
        .withThrowable(throwable)
        .withMessage(s"${source.enclosing.value} exception")
    )
  }

  override def exitStatement(resultValue: B, source: Source): Option[Statement] = {
    Some(
      Statement()
        .withMarkers(exitMarkers(source))
        .withMessage(s"${source.enclosing.value} exit with result {}")
        .withArguments(resultValue)
    )
  }
}
// #flow_behavior
