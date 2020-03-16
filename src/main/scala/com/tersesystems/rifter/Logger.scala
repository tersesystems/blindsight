package com.tersesystems.rifter

import net.logstash.logback.argument.{StructuredArgument, StructuredArguments}
import org.slf4j.Marker

// TODO Does this trace only on a particular session?  particular user?  particular request?
// TODO Provide line, file and enclosing data automatically (see MessageWriter)
// TODO Integrate with LogstashLogbackEncoder?
// TODO Work with tracing API?
//      https://tracing.rs/tracing/
//      https://docs.honeycomb.io/getting-data-in/java/beeline/
// TODO Work with "child logger" statements?
// TODO Use MDC as context if nothing else is around?  Leverage context as much as possible?

trait TraceLogger {
  def isTraceEnabled: Boolean
  def isTraceEnabled(marker: Marker): Boolean
  def trace: StatementConsumer
}

trait DebugLogger {
  def isDebugEnabled: Boolean
  def isDebugEnabled(marker: Marker): Boolean
  def debug: StatementConsumer
}

trait InfoLogger {
  def isInfoEnabled: Boolean
  def isInfoEnabled(marker: Marker): Boolean
  def info: StatementConsumer
}

trait WarnLogger {
  def isWarnEnabled: Boolean
  def isWarnEnabled(marker: Marker): Boolean
  def warn: StatementConsumer
}

trait ErrorLogger {
  def isErrorEnabled: Boolean
  def isErrorEnabled(marker: Marker): Boolean
  def error: StatementConsumer
}

trait StatementConsumer {
  def apply(statement: StatementMagnet): Unit
}

trait Logger extends TraceLogger with DebugLogger with InfoLogger with WarnLogger with ErrorLogger

object Logger {
  def apply(): Logger = new Logger {
    private val slf4jLogger = org.slf4j.LoggerFactory.getLogger(getClass)

    override def isTraceEnabled: Boolean = slf4jLogger.isTraceEnabled
    override def isTraceEnabled(marker: Marker): Boolean = slf4jLogger.isTraceEnabled(marker)

    override val trace: StatementConsumer = (statement: StatementMagnet) => {
      statement() match {
        case message: String =>
          slf4jLogger.trace(message)
        case (message: String, args: Array[_]) => // array must come first
          slf4jLogger.trace(message, args:_*)
        case (message: String, pair: (String, Any)) =>
          slf4jLogger.trace(message, StructuredArguments.kv(pair._1, pair._2))
        case (message: String, map: Map[String, Any]) =>
          import scala.collection.JavaConverters._
          slf4jLogger.trace(message, StructuredArguments.entries(map.asJava))

        case (message: String, arg) =>
          slf4jLogger.trace(message, arg)
        case (message: String, arg1, arg2) =>
          slf4jLogger.trace(message, arg1, arg2)

        case (marker: Marker, message: String) =>
          slf4jLogger.trace(marker, message)
        case (marker: Marker, message: String, args: Array[_]) =>
          slf4jLogger.trace(marker, message, args:_*)
        case (marker: Marker, message: String, arg) =>
          slf4jLogger.trace(marker, message, arg)
        case (marker: Marker, message: String, arg1, arg2) =>
          slf4jLogger.trace(marker, message, arg1, arg2)
        case other =>
          throw new IllegalStateException(s"No matching statement for other: ${other}")
      }
    }

    override def isDebugEnabled: Boolean = slf4jLogger.isDebugEnabled
    override def isDebugEnabled(marker: Marker): Boolean = slf4jLogger.isDebugEnabled(marker)

    override val debug: StatementConsumer = (statement: StatementMagnet) => {
      statement() match {
        case message: String =>
          slf4jLogger.debug(message)
        case (message: String, args: Array[_]) => // array must come first
          slf4jLogger.debug(message, args:_*)
        case (message: String, pair: (String, Any)) =>
          slf4jLogger.debug(message, StructuredArguments.kv(pair._1, pair._2))
        case (message: String, map: Map[String, Any]) =>
          import scala.collection.JavaConverters._
          slf4jLogger.debug(message, StructuredArguments.entries(map.asJava))

        case (message: String, arg) =>
          slf4jLogger.debug(message, arg)
        case (message: String, arg1, arg2) =>
          slf4jLogger.debug(message, arg1, arg2)

        case (marker: Marker, message: String) =>
          slf4jLogger.debug(marker, message)
        case (marker: Marker, message: String, args: Array[_]) =>
          slf4jLogger.debug(marker, message, args:_*)
        case (marker: Marker, message: String, arg) =>
          slf4jLogger.debug(marker, message, arg)
        case (marker: Marker, message: String, arg1, arg2) =>
          slf4jLogger.debug(marker, message, arg1, arg2)
        case other =>
          throw new IllegalStateException(s"No matching statement for other: ${other}")
      }
    }

    override def isInfoEnabled: Boolean = slf4jLogger.isInfoEnabled
    override def isInfoEnabled(marker: Marker): Boolean = slf4jLogger.isInfoEnabled(marker)

    override val info: StatementConsumer = (statement: StatementMagnet) => {
      statement() match {
        case message: String =>
          slf4jLogger.info(message)
        case (message: String, args: Array[_]) => // array must come first
          slf4jLogger.info(message, args:_*)
        case (message: String, arg) =>
          slf4jLogger.info(message, arg)
        case (message: String, arg1, arg2) =>
          slf4jLogger.info(message, arg1, arg2)

        case (marker: Marker, message: String) =>
          slf4jLogger.info(marker, message)
        case (marker: Marker, message: String, args: Array[_]) =>
          slf4jLogger.info(marker, message, args:_*)
        case (marker: Marker, message: String, arg) =>
          slf4jLogger.info(marker, message, arg)
        case (marker: Marker, message: String, arg1, arg2) =>
          slf4jLogger.info(marker, message, arg1, arg2)
        case other =>
          throw new IllegalStateException(s"No matching statement for other: ${other}")
      }
    }

    override def isWarnEnabled: Boolean = slf4jLogger.isWarnEnabled
    override def isWarnEnabled(marker: Marker): Boolean = slf4jLogger.isWarnEnabled(marker)

    override val warn: StatementConsumer = (statement: StatementMagnet) => {
      statement() match {
        case message: String =>
          slf4jLogger.warn(message)
        case (message: String, args: Array[_]) => // array must come first
          slf4jLogger.warn(message, args:_*)
        case (message: String, arg) =>
          slf4jLogger.warn(message, arg)
        case (message: String, arg1, arg2) =>
          slf4jLogger.warn(message, arg1, arg2)

        case (marker: Marker, message: String) =>
          slf4jLogger.warn(marker, message)
        case (marker: Marker, message: String, args: Array[_]) =>
          slf4jLogger.warn(marker, message, args:_*)
        case (marker: Marker, message: String, arg) =>
          slf4jLogger.warn(marker, message, arg)
        case (marker: Marker, message: String, arg1, arg2) =>
          slf4jLogger.warn(marker, message, arg1, arg2)
        case other =>
          throw new IllegalStateException(s"No matching statement for other: ${other}")
      }
    }

    override def isErrorEnabled: Boolean = slf4jLogger.isErrorEnabled
    override def isErrorEnabled(marker: Marker): Boolean = slf4jLogger.isErrorEnabled(marker)

    override val error: StatementConsumer = (statement: StatementMagnet) => {
      statement() match {
        case message: String =>
          slf4jLogger.error(message)
        case (message: String, args: Array[_]) => // array must come first
          slf4jLogger.error(message, args:_*)
        case (message: String, arg) =>
          slf4jLogger.error(message, arg)
        case (message: String, arg1, arg2) =>
          slf4jLogger.error(message, arg1, arg2)

        case (marker: Marker, message: String) =>
          slf4jLogger.error(marker, message)
        case (marker: Marker, message: String, args: Array[_]) =>
          slf4jLogger.error(marker, message, args:_*)
        case (marker: Marker, message: String, arg) =>
          slf4jLogger.error(marker, message, arg)
        case (marker: Marker, message: String, arg1, arg2) =>
          slf4jLogger.error(marker, message, arg1, arg2)
        case other =>
          throw new IllegalStateException(s"No matching statement for other: ${other}")
      }
    }
  }
}

trait StatementMagnet {
  type Result
  def apply(): Result
}

object StatementMagnet {

  implicit def fromString(string: String): StatementMagnet =
    new StatementMagnet {
      type Result = String
      override def apply(): Result = string
    }

  implicit def fromStringArg(tuple: (String, Any)): StatementMagnet =
    new StatementMagnet {
      type Result = (String, Any)
      override def apply(): Result = tuple
    }

  implicit def fromStringAnyAny(tuple: (String, Any, Any)): StatementMagnet =
    new StatementMagnet {
      type Result = (String, Any, Any)
      override def apply(): Result = tuple
    }

  implicit def fromStringArray(tuple: (String, Array[_])): StatementMagnet =
    new StatementMagnet {
      type Result = (String, Array[_])
      override def apply(): Result = tuple
    }

  implicit def fromMarkerString(tuple: (Marker, String)): StatementMagnet =
    new StatementMagnet {
      type Result = (Marker, String)
      override def apply(): Result = tuple
    }

  implicit def fromMarkerStringArg(tuple: (Marker, String, Any)): StatementMagnet =
    new StatementMagnet {
      type Result = (Marker, String, Any)
      override def apply(): Result = tuple
    }

  implicit def fromMarkerStringAnyAny(tuple: (Marker, String, Any, Any)): StatementMagnet =
    new StatementMagnet {
      type Result = (Marker, String, Any, Any)
      override def apply(): Result = tuple
    }

  implicit def fromMarkerStringArray(tuple: (Marker, String, Array[_])): StatementMagnet =
    new StatementMagnet {
      type Result = (Marker, String, Array[_])
      override def apply(): Result = tuple
    }

  implicit def fromMap(map: Map[String, Any]): StatementMagnet =
    new StatementMagnet {
      type Result = (String, StructuredArgument)
      override def apply(): (String, StructuredArgument) = {
        import scala.collection.JavaConverters._
        import net.logstash.logback.argument.StructuredArguments._
        ("{}" -> entries(map.asJava))
      }
    }
}

trait ArgumentMagnet {
  type Result
  def apply(): Result
}