/*
 * Copyright 2020 com.tersesystems.blindsight
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tersesystems.blindsight.slf4j

trait SLF4JLoggerComponent[P, M] {
  type Predicate <: P
  type Method <: M
  type Self
}

/**
 * This trait defines an SLF4J compatible logger with all five levels of logging.
 */
trait SLF4JLoggerAPI[P, M]
    extends SLF4JLoggerComponent[P, M]
    with SLF4JLoggerAPI.Trace[P, M]
    with SLF4JLoggerAPI.Debug[P, M]
    with SLF4JLoggerAPI.Info[P, M]
    with SLF4JLoggerAPI.Warn[P, M]
    with SLF4JLoggerAPI.Error[P, M]

object SLF4JLoggerAPI {

  /**
   * This trait defines only "isTraceLogging" and "trace" methods.
   */
  trait Trace[P, M] extends SLF4JLoggerComponent[P, M] {
    def isTraceEnabled: Predicate
    def trace: Method
  }

  /**
   * This trait defines only "isDebugLogging" and "debug" methods.
   */
  trait Debug[P, M] extends SLF4JLoggerComponent[P, M] {
    def isDebugEnabled: Predicate
    def debug: Method
  }

  /**
   * This trait defines only "isInfoLogging" and "info" methods.
   */
  trait Info[P, M] extends SLF4JLoggerComponent[P, M] {
    def isInfoEnabled: Predicate
    def info: Method
  }

  /**
   * This trait defines only "isWarnLogging" and "warn" methods.
   */
  trait Warn[P, M] extends SLF4JLoggerComponent[P, M] {
    def isWarnEnabled: Predicate
    def warn: Method
  }

  /**
   * This trait defines only "isErrorLogging" and "error" methods.
   */
  trait Error[P, M] extends SLF4JLoggerComponent[P, M] {
    def isErrorEnabled: Predicate
    def error: Method
  }

  trait Proxy[P, M] extends SLF4JLoggerAPI[P, M] {
    type Parent <: SLF4JLoggerAPI[P, M]

    protected val logger: Parent

    override type Method    = logger.Method
    override type Predicate = logger.Predicate

    override def isTraceEnabled: Predicate = logger.isTraceEnabled
    override def trace: Method             = logger.trace

    override def isDebugEnabled: Predicate = logger.isDebugEnabled
    override def debug: Method             = logger.debug

    override def isInfoEnabled: Predicate = logger.isInfoEnabled
    override def info: Method             = logger.info

    override def isWarnEnabled: Predicate = logger.isWarnEnabled
    override def warn: Method             = logger.warn

    override def isErrorEnabled: Predicate = logger.isErrorEnabled
    override def error: Method             = logger.error
  }
}
