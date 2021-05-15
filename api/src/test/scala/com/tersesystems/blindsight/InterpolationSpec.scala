package com.tersesystems.blindsight

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.MarkerFactory

class InterpolationSpec extends AnyWordSpec with Matchers {

  "statement interpolation" should {
    "be empty" in {
      val statement: Statement = st""
      statement.message must be(Message.empty)
    }

    "be constant string" in {
      val statement: Statement = st"constant string"
      statement.message must be(Message("constant string"))
    }

    "be argument" in {
      val arg: Argument        = Argument("arg1")
      val statement: Statement = st"$arg"
      statement.message must be(Message("{}"))
      statement.arguments.toSeq.head must be("arg1")
    }

    "be arguments" in {
      val arg1                 = Argument("arg1")
      val arg2                 = Argument("arg2")
      val statement: Statement = st"$arg1 $arg2"
      statement.message must be(Message("{} {}"))
      val args = statement.arguments.toSeq
      args(0) must be("arg1")
      args(1) must be("arg2")
    }

    "be arguments with constants" in {
      val arg1                 = Argument("arg1")
      val arg2                 = Argument("arg2")
      val statement: Statement = st"prefix $arg1 middlefix $arg2 postfix"
      statement.message must be(Message("prefix {} middlefix {} postfix"))
      val args = statement.arguments.toSeq
      args(0) must be("arg1")
      args(1) must be("arg2")
    }

    "be arguments with constants and exceptions" in {
      val arg1                 = Argument("arg1")
      val arg2                 = Argument("arg2")
      val ex                   = new Exception("ex")
      val statement: Statement = st"prefix $arg1 middlefix $arg2 postfix $ex"
      statement.message must be(Message("prefix {} middlefix {} postfix {}"))
      val args = statement.arguments.toSeq
      args(0) must be("arg1")
      args(1) must be("arg2")
      args(2) must be(ex.toString)
      statement.throwable.get must be(ex)
    }

    "be marker" in {
      val marker1   = MarkerFactory.getMarker("marker1")
      val statement = st"$marker1"
      statement.message must be(Message.empty)
      statement.markers.marker.contains(marker1) must be(true)
    }

    "be markers" in {
      val marker1   = MarkerFactory.getMarker("marker1")
      val markers   = Markers(marker1)
      val statement = st"$markers"
      statement.message must be(Message.empty)
      statement.markers.marker.contains(marker1) must be(true)
    }

    "be markers and arguments" in {
      val marker1   = MarkerFactory.getMarker("marker1")
      val markers   = Markers(marker1)
      val arg1      = Argument("arg1")
      val arg2      = Argument("arg2")
      val statement = st"${markers}arg1=$arg1 arg2=$arg2"
      statement.message must be(Message("arg1={} arg2={}"))
      statement.markers.marker.contains(marker1) must be(true)
      val args = statement.arguments.toSeq
      args(0) must be("arg1")
      args(1) must be("arg2")
    }

    "be markers and arguments and exception" in {
      val marker1   = MarkerFactory.getMarker("marker1")
      val markers   = Markers(marker1)
      val arg1      = Argument("arg1")
      val arg2      = Argument("arg2")
      val ex        = new Exception("ex")
      val statement = st"${markers}$arg1 $arg2 $ex"
      statement.message must be(Message("{} {} {}"))
      statement.markers.marker.contains(marker1) must be(true)
      val args = statement.arguments.toSeq
      args(0) must be("arg1")
      args(1) must be("arg2")
      statement.throwable.get must be(ex)
    }

    "be multiple exceptions" in {
      val ex1                  = new Exception("ex")
      val ex2                  = new Exception("ex")
      val ex3                  = new Exception("ex")
      val statement: Statement = st"$ex1 $ex2 $ex3"

      statement.message must be(Message("{} {} {}"))
      val args = statement.arguments.toSeq
      args(0) must be(ex1.toString)
      args(1) must be(ex2.toString)
      args(2) must be(ex3.toString)
      statement.throwable.get must be(ex3)
    }

  }
}
