package flow

// #flow_behavior
class SimpleFlowBehavior[B: ToArgument] extends FlowBehavior[B] {

  override def createEntryStatement(source: Source): Option[Statement] = {
    import com.tersesystems.blindsight.logstash.ToArgumentsImplicits._
    Some(
      Statement()
        .withMarkers(entryMarkers(source))
        .withMessage(s"${source.enclosing.value} entry {}")
        .withArguments(source.args)
    )
  }

  override def createThrowingStatement(throwable: Throwable, source: Source): Option[Statement] = {
    Some(
      Statement()
        .withThrowable(throwable)
        .withMessage(s"${source.enclosing.value} exception")
    )
  }

  override def createExitStatement(resultValue: B, source: Source): Option[Statement] = {
    Some(
      Statement()
        .withMarkers(exitMarkers(source))
        .withMessage(s"${source.enclosing.value} exit with result {}")
        .withArguments(resultValue)
    )
  }
}
// #flow_behavior
