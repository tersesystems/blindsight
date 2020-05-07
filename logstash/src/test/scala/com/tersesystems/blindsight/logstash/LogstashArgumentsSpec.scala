package com.tersesystems.blindsight.logstash

import com.tersesystems.blindsight.api._
import net.logstash.logback.argument.StructuredArguments
import net.logstash.logback.argument.StructuredArguments._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.JavaConverters._

class LogstashArgumentsSpec extends AnyWordSpec with Matchers {

  import ToArgumentsImplicits._

  implicit def mapToArgument: ToArgument[Map[String, AsArgument]] = ToArgument { inputMap =>
    import java.util
    val args: util.Map[String, Argument] = inputMap.map {
      case (k, argumentValue) =>
        k -> argumentValue.argument
    }.asJava
    Argument(StructuredArguments.entries(args))
  }

  "arguments" should {
    //    "match pair containing map" in {
    //      import ToArgumentsImplicits._
    //
    //      val mapOfArguments = Map("name" -> "Will", "age" -> 12)
    //      val actual = "person" -> mapOfArguments
    //      val expected = keyValue("person", entries(Map("name" -> "Will", "age" -> "age").asJava))
    //
    //      val structuredArgument = Arguments(actual).asArray(0)
    //      structuredArgument must be(expected)
    //    }

    "match map of arguments" in {

      val actual: Map[String, AsArgument] = Map("name" -> Argument("Will"), "age" -> Argument(12))
      val expected = entries(Map("name" -> "Will", "age" -> 12).asJava)

      val structuredArgument = Argument(actual).value
      structuredArgument must be(expected)
    }

    "match hetrogenous map" in {
      val actual: Map[String, AsArgument]   = Map("name" -> "Will", "age" -> 12)
      val expected = entries(Map("name" -> "Will", "age" -> 12).asJava)

      val structuredArgument = Argument(actual).value
      structuredArgument must be(expected)
    }

    // obj(String -> JsValueWrapper*)
    // circe encoder/decoder
    // Create an AST, then use a logstash engine to serialize into either Marker or
    // StructuredArgument, or even use Circe/Play-JSON.
    // Macro-based conversion of case classes to AST.
    // https://github.com/playframework/play-json/blob/master/play-json/shared/src/main/scala/play/api/libs/json/Json.scala#L218
    //.obj("person" -> Map("name" -> person.name, "age" -> person.age))

    //    "match hetrogenous list" in {
    //
    //      // JsValueWrapper
    //      val actual = "derp" -> Seq("Will", 12)
    //      val expected = array("derp", "Will", 12)
    //
    //      val structuredArgument = Argument(actual).value
    //      structuredArgument must be(expected)
    //    }
  }

}
