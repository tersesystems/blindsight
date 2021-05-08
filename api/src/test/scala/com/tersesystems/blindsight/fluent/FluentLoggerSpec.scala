package com.tersesystems.blindsight.fluent

import com.tersesystems.blindsight.core.CoreLogger
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import com.tersesystems.blindsight.{Argument, ToArgument}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.MarkerFactory

import java.util.UUID

class FluentLoggerSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  def resourceName: String = "/logback-test-list.xml"

  case class PayloadModel(payloadId: UUID, userSecretToken: String, data: String) {
    override def toString: String = s"PayloadModel(uuid=$payloadId)"
  }

  "work with everything" in {

    implicit val payloadToArguments: ToArgument[PayloadModel] = ToArgument { instance =>
      Argument(instance.payloadId.toString)
    }

    val underlying = loggerContext.getLogger("logger")
    val fluentLogger: FluentLogger =
      new FluentLogger.Impl(CoreLogger(underlying))
    val uuid = UUID.randomUUID()
    fluentLogger.info
      .marker(MarkerFactory.getDetachedMarker("HELLO"))
      .message("User logged out")
      .argument(PayloadModel(uuid, "secretToken", "data"))
      .cause(new Exception("exception"))
      .logWithPlaceholders()

    val event = listAppender.list.get(0)
    event.getMarker.contains(MarkerFactory.getMarker("HELLO")) must be(true)
    event.getMessage must equal("User logged out {}")
    event.getThrowableProxy.getMessage must equal("exception")
    event.getArgumentArray must ===(Array(uuid.toString))
  }

  "work with exception" in {
    val underlying = loggerContext.getLogger("logger")

    val fluentLogger: FluentLogger =
      new FluentLogger.Impl(CoreLogger(underlying))

    fluentLogger.info.cause(new Exception("exception")).log()

    val event = listAppender.list.get(0)
    event.getMessage must equal("")
    event.getThrowableProxy.getMessage must equal("exception")
  }

  "work with string arguments only" in {
    val underlying = loggerContext.getLogger("logger")

    val fluentLogger: FluentLogger =
      new FluentLogger.Impl(CoreLogger(underlying))
    fluentLogger.info.argument("only arguments").logWithPlaceholders()

    val event = listAppender.list.get(0)
    event.getMessage must equal(" {}")
    event.getArgumentArray must ===(Array("only arguments"))
  }

}
