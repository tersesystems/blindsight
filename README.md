# Blindsight


[![Maven central](https://img.shields.io/badge/mavencentral-com.tersesystems.blindsight-blue.svg)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.tersesystems.blindsight%22)
[![License Apache-2.0](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](https://www.tldrlegal.com/l/apache2)

![Build](https://github.com/tersesystems/blindsight/actions/workflows/ci.yml/badge.svg)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

> Suffering in silence, you check the logs for fresh telemetry.
>
> You think: *That can't be right*.
>
> -- [Blindsight](https://www.rifters.com/real/Blindsight.htm#Prologue), Peter Watts

Blindsight is "observability through logging" where observability is defined as [baked in high cardinality structured data with field types](https://www.honeycomb.io/blog/observability-a-manifesto/).  The name is taken from Peter Watts' excellent first contact novel, [Blindsight](https://en.wikipedia.org/wiki/Blindsight_\(Watts_novel\)).

Blindsight is a logging library written in Scala that wraps SLF4J to add [useful features](https://tersesystems.github.io/blindsight/usage/overview.html) that solve several outstanding problems with logging:

* Rendering structured logs in multiple formats through a format-independent [AST and DSL](https://tersesystems.github.io/blindsight/usage/dsl.html).
* Managing complex relationships and schema through [JSON-LD](https://tersesystems.github.io/blindsight/usage/jsonld.html).  
* Expressing domain specific objects as arguments through [type classes](https://tersesystems.github.io/blindsight/usage/typeclasses.html).
* Resolving operation-specific loggers through [logger resolvers](https://tersesystems.github.io/blindsight/usage/resolvers.html).
* Building up complex logging statements through [fluent logging](https://tersesystems.github.io/blindsight/usage/fluent.html).
* Enforcing user supplied type constraints through [semantic logging](https://tersesystems.github.io/blindsight/usage/semantic.html).
* Minimal-overhead tracing and causality tracking through [flow logging](https://tersesystems.github.io/blindsight/usage/flow.html).
* Providing thread-safe context to logs through [context aware logging](https://tersesystems.github.io/blindsight/usage/context.html).
* Time-based and targeted diagnostic logging through [conditional logging](https://tersesystems.github.io/blindsight/usage/conditional.html).
* Hooks into logging entries through [entry transformation](https://tersesystems.github.io/blindsight/usage/transform.html)
* Application accessible debug and trace logs through [event buffers](https://tersesystems.github.io/blindsight/usage/buffer.html)
* Method or line based logging overrides at runtime through [scripting](https://tersesystems.github.io/blindsight/usage/scripting.html).
* Easier "printf debugging" through macro based [inspections](https://tersesystems.github.io/blindsight/usage/inspections.html).

See [the documentation](https://tersesystems.github.io/blindsight/) for more details.

## Example

You can check out a "starter project" at [https://github.com/tersesystems/blindsight-starter](https://github.com/tersesystems/blindsight-starter).

There's an example application at [https://github.com/tersesystems/play-blindsight](https://github.com/tersesystems/play-blindsight) that integrates with Honeycomb Tracing using the flow logger:

![trace.png](trace.png)

## Dependencies

The only hard dependency is the SLF4J API, but the DSL functionality is only implemented for Logback with [logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder).  

Blindsight is a pure SLF4J wrapper: it delegates all logging through to the SLF4J API and does not configure or manage the SLF4J implementation at all.

Versions are published for Scala 2.11, 2.12, 2.13, and 3.0.0.

## Install

See [Setup](https://tersesystems.github.io/blindsight/setup/index.html) for how to install Blindsight.

You can check out a "starter project" at [https://github.com/tersesystems/blindsight-starter](https://github.com/tersesystems/blindsight-starter).

Because Blindsight uses a very recent version of Logstash that depends on Jackson 2.11.0, you may need to update your dependencies for the `jackson-scala-module` if you're using Play or Akka.

```
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.0"
```

## Usage
 
The easiest way to use Blindsight is to import the base package and the DSL:

```scala
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.DSL._
```

To use a Blindsight Logger:

```scala
val logger = LoggerFactory.getLogger
logger.info("I am an SLF4J-like logger")
```

or in block form for diagnostic logging:

```scala
logger.debug { debug => debug("I am an SLF4J-like logger") }
```

[Structured DSL](https://tersesystems.github.io/blindsight/usage/dsl.html):

```scala
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.DSL._

logger.info("Logs with argument {}", bobj("array" -> Seq("one", "two", "three")))
```

[Statement Interpolation](https://tersesystems.github.io/blindsight/usage/interpolation.html): 

```scala
val dayOfWeek = "Monday"
val temp = 72 

// macro expands this to:
// Statement("It is {} and the temperature is {} degrees.", Arguments(dayOfWeek, temp))
val statement: Statement = st"It is ${dayOfWeek} and the temperature is ${temp} degrees."

logger.info(statement)
```

[Marker/Argument Type Classes](https://tersesystems.github.io/blindsight/usage/typeclass.html):
 
```scala
case class Lotto(
  id: Long,
  winningNumbers: List[Int],
  winners: List[Winner],
  drawDate: Option[java.util.Date]
) {
  lazy val asBObject: BObject = "lotto" ->
      ("lotto-id"          -> id) ~
        ("winning-numbers" -> winningNumbers) ~
        ("draw-date"       -> drawDate.map(_.toString)) ~
        ("winners"         -> winners.map(w => w.asBObject))
}

object Lotto {
  implicit val toArgument: ToArgument[Lotto] = ToArgument { lotto => Argument(lotto.asBObject) }
}

val winners =
  List(Winner(23, List(2, 45, 34, 23, 3, 5)), Winner(54, List(52, 3, 12, 11, 18, 22)))
val lotto = Lotto(5, List(2, 45, 34, 23, 7, 5, 3), winners, None)

logger.info("message {}", lotto) // auto-converted to structured output
```

[JSON-LD](https://tersesystems.github.io/blindsight/usage/jsonld.html):

```scala
implicit val nodeObjectToArgument: ToArgument[NodeObject] = ToArgument[NodeObject] { nodeObject =>
  Argument(BlindsightASTMapping.toBObject(nodeObject))
}

implicit val nodeObjectToMarkers: ToMarkers[NodeObject] = ToMarkers { nodeObject =>
  Markers(BlindsightASTMapping.toBObject(nodeObject))
}

implicit val nodeObjectToStatement: ToStatement[NodeObject] = ...

class Foo extends LDContext { // LDContext contains all the type safe bindings
  def sayHello(): Unit = {
    val willPerson = NodeObject(
      `@type`    -> "Person",
      `@id`      -> willId,
      givenName  -> "Will",
      familyName -> "Sargent",
      parent     -> parentId,
      occupation -> Occupation(
        estimatedSalary = MonetaryAmount(Currency.getInstance("USD"), 1),
        name = "Code Monkey"
      )
    )

    logger.info("as an argument {}", willPerson) // as an argument
    logger.info(Markers(willPerson), "as a marker") // as a marker
    
    logger.semantic[NodeObject].info(willPerson) // or as a statement
  }
}
```

[Fluent logging](https://tersesystems.github.io/blindsight/usage/fluent.html):

```scala
logger.fluent.info
  .message("The Magic Words are")
  .argument(Arguments("Squeamish", "Ossifrage"))
  .logWithPlaceholders()
```

[Semantic logging](https://tersesystems.github.io/blindsight/usage/semantic.html):

```scala
// log only user events
logger.semantic[UserEvent].info(userEvent)

// Works well with refinement types
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string._
import eu.timepit.refined._
logger.semantic[String Refined Url].info(refineMV(Url)("https://tersesystems.com"))
```

[Flow logging](https://tersesystems.github.io/blindsight/usage/flow.html):

```scala
import com.tersesystems.blindsight.flow._

implicit def flowBehavior[B]: FlowBehavior[B] = new SimpleFlowBehavior

val arg1: Int = 1
val arg2: Int = 2
val result:Int = logger.flow.trace(arg1 + arg2)
```

[Conditional logging](https://tersesystems.github.io/blindsight/usage/conditional.html):

```scala
logger.withCondition(booleanCondition).info("Only logs when condition is true")

logger.info.when(booleanCondition) { info => info("when true") }
```

[Context aware logging](https://tersesystems.github.io/blindsight/usage/context.html):

```scala
import DSL._

// Add key/value pairs with DSL and return a logger
val markerLogger = logger.withMarker(bobj("userId" -> userId))

// log with generated logger
markerLogger.info("Logging with user id added as a context marker!")

// can retrieve state markers
val contextMarkers: Markers = markerLogger.markers
```

[Entry Transformation](https://tersesystems.github.io/blindsight/usage/transform.html):

```scala
val logger = LoggerFactory.getLogger
               .withEntryTransform(e => e.copy(message = e.message + " IN BED"))

logger.info("You will discover your hidden talents")
```

[Event Buffer](https://tersesystems.github.io/blindsight/usage/buffer.html):

```scala
val queueBuffer = EventBuffer(1)
val logger      = LoggerFactory.getLogger.withEventBuffer(queueBuffer)

logger.info("Hello world")

val event = queueBuffer.head
```

[Scripting](https://tersesystems.github.io/blindsight/usage/scripting.html):

```scala
val scriptHandle = new ScriptHandle {
  override def isInvalid: Boolean = false // on file modification, etc
  override val script: String =
    """import strings as s from 'std.tf';
      |alias s.ends_with? as ends_with?;
      |
      |library blindsight {
      |  function evaluate: (long level, string enc, long line, string file) ->
      |    if (ends_with?(enc, "specialMethodName")) then true
      |    else false;
      |}
      |""".stripMargin
  override def report(e: Throwable): Unit = e.printStackTrace()
}
val scriptManager = new ScriptManager(scriptHandle) 
val location = new ScriptAwareLocation(scriptManager)

def specialMethodName = {
  // inside the specialMethodName method here :-)
  logger.debug.when(location.here) { log => 
    log("script allows selective logging by method or by line")
  }
}
```

[Inspections](https://tersesystems.github.io/blindsight/usage/inspections.html):

```scala
import com.tersesystems.blindsight.inspection.InspectionMacros._

decorateIfs(dif => logger.debug(s"${dif.code} = ${dif.result}")) {
  if (System.currentTimeMillis() % 17 == 0) {
    println("branch 1")
  } else if (System.getProperty("derp") == null) {
    println("branch 2")
  } else {
    println("else branch")
  }
}
```

## Benchmarks

Benchmarks are available [here](https://tersesystems.github.io/blindsight/benchmarks.html).

## License

Blindsight is released under the "Apache 2" license. See [LICENSE](LICENSE) for specifics and copyright declaration.
