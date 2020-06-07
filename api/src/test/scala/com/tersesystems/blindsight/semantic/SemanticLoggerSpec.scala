package com.tersesystems.blindsight.semantic

import java.util.UUID

import com.tersesystems.blindsight._
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.MarkerFactory

class SemanticLoggerSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  def resourceName: String = "/logback-test-list.xml"

  def core(underlying: org.slf4j.Logger): CoreLogger = {
    CoreLogger(underlying)
  }

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
        new SemanticLogger.Impl[PayloadModel](core(underlying))
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
        Statement(
          markers = Markers("secretToken" -> instance.userSecretToken),
          message = Message("herp" -> "derp"),
          arguments = Arguments(instance.payloadId.toString),
          None
        )
      }

      val underlying = loggerContext.getLogger("testing")
      val payloadLogger: SemanticLogger[PayloadModel] =
        new SemanticLogger.Impl[PayloadModel](core(underlying))
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
