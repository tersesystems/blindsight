package com.tersesystems.blindsight.logstash

import com.tersesystems.blindsight.api._
import net.logstash.logback.argument.StructuredArguments._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.JavaConverters._

class LogstashArgumentsSpec extends AnyWordSpec with Matchers {

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

    "match map" in {
      import ToArgumentsImplicits._

      val actual   = Map("name" -> Argument("Will"), "age" -> Argument(12))
      val expected = entries(Map("name" -> "Will", "age" -> 12).asJava)

      val structuredArgument = Argument(actual).value
      structuredArgument must be(expected)
    }
  }

}
