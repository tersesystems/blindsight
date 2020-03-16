package example

import java.time.{Duration, Instant}
import java.util.UUID

import com.tersesystems.rifter.{ArgumentMagnet, StatementMagnet}
import net.logstash.logback.argument.StructuredArguments._
import net.logstash.logback.argument.{StructuredArgument, StructuredArguments}
import net.logstash.logback.marker.Markers
import org.slf4j.Marker

object Main {

  def main(args: Array[String]): Unit = {
    val logger = com.tersesystems.rifter.Logger()

    val payloadModel = PayloadModel(UUID.randomUUID(), "1234", "data")

    logger.debug("this is a string")
    logger.debug("string with arg! {}", "lol")
    logger.debug("string with two args! {} {}", "lol", "foo")
    logger.debug("string with array! {} {}", Array("lol", "foo"))

    val marker = Markers.append("foo", "bar")
    logger.debug("Hello world, payload = {}", Map("uuid" -> payloadModel.payloadId, "data" -> payloadModel.data))
    logger.debug(marker, "Hello world, payload = {}", payloadModel)
    //logger.debug(payloadModel)
    logger.error(Map("foo" -> "bar"))
  }
}

trait LoggingSchema {
  def eventDetail(detail: String): LoggingSchema
  def duration(duration: Duration): LoggingSchema
  def duration(start: Instant, end: Instant): LoggingSchema = duration(Duration.between(start, end))
  def payload(payload: PayloadModel): LoggingSchema
}

final case class PayloadModel(val payloadId: UUID, val userSecretToken: String, val data: String)

object PayloadModel {

  implicit def statementFromPayload(payloadModel: PayloadModel): StatementMagnet =
    new StatementMagnet {
      type Result = (String, StructuredArgument)
      override def apply(): Result = "payloadModel: {}" -> kv("uuid", payloadModel.payloadId)
    }


  implicit def argumentFromPayloadModel(payloadModel: PayloadModel): ArgumentMagnet =
    new ArgumentMagnet {
      type Result = (String, StructuredArgument)
      override def apply(): Result = "PayloadModel({})" -> kv("uuid", payloadModel.payloadId)
    }

}
