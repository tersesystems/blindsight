package com.tersesystems.blindsight

/**
 * The logger resolver is a type class that can return an SLF4J logger from the instance.
 *
 * It should be used with `loggerFactory.getLogger`.
 *
 * It is useful when you want a logger based on something other than a class or string.
 */
trait LoggerResolver[T] {
  def resolveLogger(instance: T): org.slf4j.Logger
}

trait LowPriorityLoggerResolverImplicits {
  implicit val stringToResolver: LoggerResolver[String] = LoggerResolver[String] { str =>
    val factory = org.slf4j.LoggerFactory.getILoggerFactory
    factory.getLogger(str)
  }

  implicit def classToResolver[T]: LoggerResolver[Class[T]] = LoggerResolver[Class[T]] {
    (instance: Class[T]) =>
      val factory = org.slf4j.LoggerFactory.getILoggerFactory
      factory.getLogger(instance.getName)
  }

  implicit val loggerToResolver: LoggerResolver[org.slf4j.Logger] = LoggerResolver[org.slf4j.Logger](identity)
}

object LoggerResolver extends LowPriorityLoggerResolverImplicits {
  def apply[T](f: T => org.slf4j.Logger): LoggerResolver[T] = new LoggerResolver[T] {
    override def resolveLogger(instance: T): org.slf4j.Logger = f(instance)
  }
}