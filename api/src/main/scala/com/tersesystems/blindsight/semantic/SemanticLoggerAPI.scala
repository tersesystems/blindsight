/*
 * Copyright 2020 Terse Systems
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

package com.tersesystems.blindsight.semantic

import com.tersesystems.blindsight.{Condition, NotNothing, ToStatement}

trait SemanticLoggerComponent[StatementType, P, M[_]] {
  type Predicate = P
  type Method[T] = M[T]
  type Self[T]
}

trait SemanticRefineMixin[StatementType] {
  type Self[T]

  /**
   * Refines the statement type to the given subtype.
   *
   * @tparam T the refined type
   * @return a semantic logger with the new type
   */
  def refine[T <: StatementType: ToStatement: NotNothing]: Self[T]
}

/**
 * The semantic logger API.
 *
 * @tparam StatementType the type class instance of [[com.tersesystems.blindsight.ToStatement]].
 * @tparam P the predicate type.
 * @tparam M the method type.
 */
trait SemanticLoggerAPI[StatementType, P, M[_]]
    extends SemanticLoggerComponent[StatementType, P, M]
    with SemanticLoggerAPI.Trace[StatementType, P, M]
    with SemanticLoggerAPI.Debug[StatementType, P, M]
    with SemanticLoggerAPI.Info[StatementType, P, M]
    with SemanticLoggerAPI.Warn[StatementType, P, M]
    with SemanticLoggerAPI.Error[StatementType, P, M]

object SemanticLoggerAPI {
  trait Proxy[StatementType, P, M[_]] extends SemanticLoggerAPI[StatementType, P, M] {
    protected def logger: SemanticLoggerAPI[StatementType, Predicate, Method]

    override def isTraceEnabled: Predicate    = logger.isTraceEnabled
    override def trace: Method[StatementType] = logger.trace

    override def isDebugEnabled: Predicate    = logger.isDebugEnabled
    override def debug: Method[StatementType] = logger.debug

    override def isInfoEnabled: Predicate    = logger.isInfoEnabled
    override def info: Method[StatementType] = logger.info

    override def isWarnEnabled: Predicate    = logger.isWarnEnabled
    override def warn: Method[StatementType] = logger.warn

    override def isErrorEnabled: Predicate    = logger.isErrorEnabled
    override def error: Method[StatementType] = logger.error
  }

  /**
   * This trait defines only "isTraceLogging" and "trace" methods.
   */
  trait Trace[StatementType, P, M[_]] extends SemanticLoggerComponent[StatementType, P, M] {
    def isTraceEnabled: Predicate
    def trace: Method[StatementType]
  }

  /**
   * This trait defines only "isDebugLogging" and "debug" methods.
   */
  trait Debug[StatementType, P, M[_]] extends SemanticLoggerComponent[StatementType, P, M] {
    def isDebugEnabled: Predicate
    def debug: Method[StatementType]
  }

  /**
   * This trait defines only "isInfoLogging" and "info" methods.
   */
  trait Info[StatementType, P, M[_]] extends SemanticLoggerComponent[StatementType, P, M] {
    def isInfoEnabled: Predicate
    def info: Method[StatementType]
  }

  /**
   * This trait defines only "isWarnLogging" and "warn" methods.
   */
  trait Warn[StatementType, P, M[_]] extends SemanticLoggerComponent[StatementType, P, M] {
    def isWarnEnabled: Predicate
    def warn: Method[StatementType]
  }

  /**
   * This trait defines only "isErrorLogging" and "error" methods.
   */
  trait Error[StatementType, P, M[_]] extends SemanticLoggerComponent[StatementType, P, M] {
    def isErrorEnabled: Predicate
    def error: Method[StatementType]
  }

}
