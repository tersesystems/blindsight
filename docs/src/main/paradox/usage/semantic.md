# Semantic API

A semantic logging API is [strongly typed](https://github.com/microsoft/perfview/blob/master/documentation/TraceEvent/TraceEventProgrammersGuide.md) and does not have the same construction oriented approach as the fluent API.  Instead, the type of the instance is presumed to have a mapping directly to the attributes being logged.

The semantic API works against @scaladoc[Statement](com.tersesystems.blindsight.Statement) directly.  The application is expected to handle the type class mapping to @scaladoc[Statement](com.tersesystems.blindsight.Statement).

Here is an example:

```scala
import com.tersesystems.blindsight._
import com.tersesystems.blindsight.semantic._
import net.logstash.logback.argument.StructuredArguments.kv

object SemanticMain {

  trait UserEvent {
    def name: String
  }

  final case class UserLoggedInEvent(name: String, ipAddr: String) extends UserEvent

  object UserLoggedInEvent {
    implicit val toMessage: ToMessage[UserLoggedInEvent] = ToMessage { instance =>
      Message(instance.toString)
    }

    implicit val toArguments: ToArguments[UserLoggedInEvent] = ToArguments { instance =>
      Arguments(
        kv("name", instance.name),
        kv("ipAddr", instance.ipAddr)
      )
    }

    implicit val toStatement: ToStatement[UserLoggedInEvent] = ToStatement { instance =>
      Statement().withMessage(instance).withArguments(instance)
    }
  }

  final case class UserLoggedOutEvent(name: String, reason: String) extends UserEvent

  object UserLoggedOutEvent {
    implicit val toMessage: ToMessage[UserLoggedOutEvent] = ToMessage { instance =>
      Message(instance.toString)
    }

    implicit val toArguments: ToArguments[UserLoggedOutEvent] = ToArguments { instance =>
      Arguments(
        kv("name", instance.name),
        kv("reason", instance.reason)
      )
    }

    implicit val toStatement: ToStatement[UserLoggedOutEvent] = ToStatement { instance =>
      Statement().withMessage(instance).withArguments(instance)
    }
  }

  def main(args: Array[String]): Unit = {
    val userEventLogger: SemanticLogger[UserEvent] = LoggerFactory.getLogger.semantic[UserEvent] 
    userEventLogger.info(UserLoggedInEvent("steve", "127.0.0.1"))
    userEventLogger.info(UserLoggedOutEvent("steve", "timeout"))

    val onlyLoggedInEventLogger: SemanticLogger[UserLoggedInEvent] = userEventLogger.refine[UserLoggedInEvent]
    onlyLoggedInEventLogger.info(UserLoggedInEvent("mike", "10.0.0.1")) // won't work with logged out event
  }
}
```

in plain text:

```
FgEdhil2znw6O0Qbm7EAAA 2020-04-05T23:09:08.359+0000 [INFO ] example.semantic.SemanticMain$ in main  - UserLoggedInEvent(steve,127.0.0.1)
FgEdhil2zsg6O0Qbm7EAAA 2020-04-05T23:09:08.435+0000 [INFO ] example.semantic.SemanticMain$ in main  - UserLoggedOutEvent(steve,timeout)
FgEdhil2zsk6O0Qbm7EAAA 2020-04-05T23:09:08.436+0000 [INFO ] example.semantic.SemanticMain$ in main  - UserLoggedInEvent(mike,10.0.0.1)
```

and in JSON:

```json
{
  "id": "FgEdhil2znw6O0Qbm7EAAA",
  "relative_ns": -298700,
  "tse_ms": 1586128148359,
  "start_ms": null,
  "@timestamp": "2020-04-05T23:09:08.359Z",
  "@version": "1",
  "message": "UserLoggedInEvent(steve,127.0.0.1)",
  "logger_name": "example.semantic.SemanticMain$",
  "thread_name": "main",
  "level": "INFO",
  "level_value": 20000,
  "name": "steve",
  "ipAddr": "127.0.0.1"
}
```

## Refinement Types

Semantic Logging works very well with [refinement types](https://github.com/fthomas/refined).  

For example, you can add compile time limitations on the kinds of messages that are passed in:

```scala
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string._
import eu.timepit.refined._

implicit def stringToStatement[R]: ToStatement[Refined[String, R]] = ToStatement { str =>
  Statement().withMessage(str.value)
}

val notEmptyLogger: SemanticLogger[String Refined NonEmpty] = logger.semantic[String Refined NonEmpty]
notEmptyLogger.info(refineMV[NonEmpty]("this is a statement"))
// will not compile
//notEmptyLogger.info(refineMV(""))

val urlLogger: SemanticLogger[String Refined Url] = logger.semantic[String Refined Url]
urlLogger.info(refineMV[Url]("http://google.com"))
// will not compile
//urlLogger.info(refineMV("this is a statement"))
``` 

