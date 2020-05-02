package com.tersesystems.blindsight.flow

import com.tersesystems.blindsight.api.{Markers, Statement, ToArguments}
import com.tersesystems.blindsight.fixtures.OneContextPerTest
import com.tersesystems.blindsight.slf4j.SLF4JLogger
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.event.Level

import scala.util.Try

class FlowLoggerSpec extends AnyWordSpec with Matchers with OneContextPerTest {

  "example.flow logger" should {
    "print entry and exit statements" in {
      val underlying       = loggerContext.getLogger("logger")
      val flow: FlowLogger = new FlowLogger.Impl(new NoSourceSLF4JLogger(underlying))

      import LowPriorityBehavior._

      flow.info {
        1 + 1
      }

      val entry = listAppender.list.get(0)
      entry.getMessage must equal("entry")
      val exit = listAppender.list.get(1)
      exit.getMessage must equal("exit")
    }

    "render an exception" in {
      val underlying       = loggerContext.getLogger("logger")
      val flow: FlowLogger = new FlowLogger.Impl(new NoSourceSLF4JLogger(underlying))
      import LowPriorityBehavior._

      Try {
        flow.info {
          if (System.currentTimeMillis() > 0) {
            throw new IllegalStateException("oh noes")
          }
        }
      }

      val entry = listAppender.list.get(0)
      entry.getMessage must equal("entry")
      val exit = listAppender.list.get(1)
      exit.getMessage must equal("throwing")
    }

    "log nothing when using only defaults" in {
      val underlying       = loggerContext.getLogger("logger")
      val flow: FlowLogger = new FlowLogger.Impl(new NoSourceSLF4JLogger(underlying))

      implicit def flowMapping[B]: FlowBehavior[B] = new FlowBehavior[B] {}

      Try {
        flow.info {
          if (System.currentTimeMillis() > 0) {
            throw new IllegalStateException("oh noes")
          }
        }
      }
      listAppender.list.size() must be(0)
    }
  }

  "example.flow logger conditional" should {

    "log when a predicate is present" in {
      val underlying       = loggerContext.getLogger("logger")
      val flow: FlowLogger = new FlowLogger.Impl(new NoSourceSLF4JLogger(underlying))
      import LowPriorityBehavior._

      val condition = flow.onCondition(true)
      val result = condition.info {
        1 + 1
      }
      result must be(2)
      val entry = listAppender.list.get(0)
      entry.getMessage must equal("entry")
      val exit = listAppender.list.get(1)
      exit.getMessage must equal("exit")
    }

    "not log when a predicate is off" in {
      val underlying       = loggerContext.getLogger("logger")
      val flow: FlowLogger = new FlowLogger.Impl(new NoSourceSLF4JLogger(underlying))
      import LowPriorityBehavior._

      val condition = flow.onCondition(false)
      val result = condition.info {
        1 + 1
      }
      result must be(2)
      listAppender.list.size must be(0)
    }
  }

}

trait LowPriorityBehavior {
  implicit def flowBehavior[B: ToArguments]: FlowBehavior[B] = new FlowBehavior[B] {
    override def entryStatement(source: FlowBehavior.Source): Option[Statement] =
      Some(Statement().withMessage("entry"))
    override def exitStatement(resultValue: B, source: FlowBehavior.Source): Option[Statement] =
      Some(Statement().withMessage("exit"))
    override def throwingStatement(
        exc: Throwable,
        source: FlowBehavior.Source
    ): Option[(Level, Statement)] =
      Some(Level.ERROR, Statement().withMessage("throwing"))
  }
}

object LowPriorityBehavior extends LowPriorityBehavior

class NoSourceSLF4JLogger(underlying: org.slf4j.Logger, markers: Markers = Markers.empty)
    extends SLF4JLogger.Unchecked(underlying, markers)
