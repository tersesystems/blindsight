package com.tersesystems.blindsight.semantic

import java.util.UUID

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import com.tersesystems.blindsight.slf4j.SLF4JLogger
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.MarkerFactory

class SemanticLoggerSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  def resourceName: String = "/logback-test-list.xml"

  "a logger" when {

    "run against statement" in {
      implicit val payloadToArguments: ToStatement[PayloadModel] = ToStatement { instance =>
        Statement(
          Markers.empty,
          Message("payloadModel:"),
          Arguments(instance.payloadId.toString),
          None
        )
      }

      val underlying = loggerContext.getLogger("testing")
      val payloadLogger: SemanticLogger[PayloadModel] =
        new SemanticLogger.Impl[PayloadModel](new NoSourceSLF4JLogger(underlying))
      val uuid = UUID.randomUUID()
      payloadLogger.info(PayloadModel(uuid, "1234", "data"))

      val event = listAppender.list.get(0)
      event.getMessage must be("payloadModel:")
      event.getArgumentArray must equal(Array(uuid.toString))
    }

    "run against a constructed statement" in {
      implicit val tupleStringToMarkers: ToMarkers[(String, String)] = ToMarkers {
        case (k, v) =>
          Markers(MarkerFactory.getDetachedMarker(s"$k=$v"))
      }

      implicit val payloadToArguments: ToStatement[PayloadModel] = ToStatement { instance =>
        // XXX Make a builder out of Statement
        Statement(
          markers = Markers("secretToken" -> instance.userSecretToken),
          message = Message("herp" -> "derp"),
          arguments = Arguments(instance.payloadId.toString),
          None
        )
      }

      val underlying = loggerContext.getLogger("testing")
      val payloadLogger: SemanticLogger[PayloadModel] =
        new SemanticLogger.Impl[PayloadModel](new NoSourceSLF4JLogger(underlying))
      val uuid = UUID.randomUUID()
      payloadLogger.info(PayloadModel(uuid, "1234", "data"))

      val event = listAppender.list.get(0)
      event.getMessage must be("herp=derp")
      event.getArgumentArray must equal(Array(uuid.toString))
    }
  }
}

final case class PayloadModel(payloadId: UUID, userSecretToken: String, data: String) {
  override def toString: String = s"PayloadModel(uuid=$payloadId)"
}

class NoSourceSLF4JLogger(underlying: org.slf4j.Logger, markers: Markers = Markers.empty)
    extends SLF4JLogger.Unchecked(underlying, markers)
