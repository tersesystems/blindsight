@@@ index

* [Overview](overview.md)
* [Logger Resolvers](resolvers.md)
* [Structured DSL](dsl.md)
* [Type Classes](typeclasses.md)
* [String Interpolation](interpolation.md)
* [JSON-LD](jsonld.md)  
* [SLF4J API](slf4j.md)
* [Fluent API](fluent.md)
* [Semantic API](semantic.md)
* [Flow API](flow.md)
* [Conditional Logging](conditional.md)
* [Contextual Logging](context.md)
* [Entry Transformation](transform.md)
* [Event Buffers](buffer.md)
* [Source Code](sourcecode.md)
* [Scripting](scripting.md)
* [Inspections](inspections.md)

@@@

# Usage

To use a Blindsight @scaladoc[Logger](com.tersesystems.blindsight.Logger):

```scala
val logger = com.tersesystems.blindsight.LoggerFactory.getLogger
logger.info("I am an SLF4J-like logger")
```

But there's a lot more, of course.  

## Examples

@ref:[statement interpolation](interpolation.md):

```scala
val dayOfWeek = "Monday"
val temp = 72 
val statement: Statement = st"It is ${dayOfWeek} and the temperature is ${temp} degrees."
logger.info(statement)
```

@ref:[Structured logging](dsl.md) using an internal DSL:

```scala
case class Winner(id: Long, numbers: List[Int])
case class Lotto(id: Long, winningNumbers: List[Int], winners: List[Winner], drawDate: Option[java.util.Date])

val winners = List(Winner(23, List(2, 45, 34, 23, 3, 5)), Winner(54, List(52, 3, 12, 11, 18, 22)))
val lotto = Lotto(5, List(2, 45, 34, 23, 7, 5, 3), winners, None)

import com.tersesystems.blindsight.AST._
import com.tersesystems.blindsight.DSL._
val complexArgument: BObject = "lotto" ->
    ("lotto-id" -> lotto.id) ~
      ("winning-numbers" -> lotto.winningNumbers) ~
      ("draw-date" -> lotto.drawDate.map(_.toString)) ~
      ("winners" -> lotto.winners.map(w => ("winner-id" -> w.id) ~ ("numbers" -> w.numbers)))
logger.info("Logs with an array as marker", complexArgument)
```

@ref:[Fluent Logging](fluent.md):

```scala
logger.fluent.info.message("I am a fluent logger").log()
```

@ref:[Semantic Logging](semantic.md):

```scala
logger.semantic[UserEvent].info(userEvent)
```

@ref:[Flow Logging](flow.md):

```scala
implicit def flowBehavior[B: ToArgument]: FlowBehavior[B] = new SimpleFlowBehavior 
val resultIsThree: Int = logger.flow.trace(1 + 2)
```

@ref:[Conditional Logging](conditional.md):

```scala
logger.withCondition(booleanCondition).info("Only logs when condition is true")

logger.info.when(booleanCondition) { info => info("when true") }
```

@ref:[Context Logging](context.md):

```scala
logger.withMarker("userId" -> userId).info("Logging with user id added as a context marker!")
```

@ref:[Scripting](scripting.md):

```scala
logger.debug.when(location.here) { log =>
  log("script allows selective logging by method or by line")
}
```

@ref:[Inspections](inspections.md):

```scala
import com.tersesystems.blindsight.inspection.InspectionMacros._

decorateVals(dval => logger.debug(s"${dval.name} = ${dval.value}")) {
  val a = 5
  val b = 15
  a + b
}
```

@@toc { depth=1 }