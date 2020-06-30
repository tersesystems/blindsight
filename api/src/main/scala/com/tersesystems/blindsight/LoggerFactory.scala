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

package com.tersesystems.blindsight

/**
 * The logger factory is a trait that returns a Blindsight logger, using a
 * type class instance of [[LoggerResolver]].
 */
trait LoggerFactory {

  /**
   * Gets a logger from the resolver.
   *
   * @param instance the type class instance
   * @tparam T the type, usually Class[_] or String
   * @return a logger
   */
  def getLogger[T: LoggerResolver](instance: T): Logger

  /**
   * Gets a logger using the enclosing source code as a base, provided as a macro.
   *
   * @param enc the enclosing, provided by macro
   * @param name the name, provided by macro
   * @return a blindsight logger.
   */
  def getLogger(implicit enc: sourcecode.Enclosing, name: sourcecode.Name): Logger = {
    import com.tersesystems.blindsight.LoggerFactory.classNameFromSource
    getLogger(classNameFromSource)
  }

}

object LoggerFactory {

  import java.util.ServiceLoader

  private lazy val loggerFactory: LoggerFactory = {
    import javax.management.ServiceNotFoundException
    val iter                         = loggerFactoryLoader.iterator()
    var loggerFactory: LoggerFactory = null;
    while (iter.hasNext && loggerFactory == null) {
      loggerFactory = iter.next()
    }
    if (loggerFactory == null) {
      throw new ServiceNotFoundException("No logger factory found!")
    } else {
      loggerFactory
    }
  }
  private val loggerFactoryLoader = ServiceLoader.load(classOf[LoggerFactory])

  def getLogger[T: LoggerResolver](instance: => T): Logger = {
    loggerFactory.getLogger(instance)
  }

  def getLogger(implicit enc: sourcecode.Enclosing, name: sourcecode.Name): Logger = {
    loggerFactory.getLogger
  }

  def classNameFromSource(implicit
      enc: sourcecode.Enclosing,
      name: sourcecode.Name
  ): String = {
    val value = enc.value.stripSuffix(s".${name.value}")
    val index = value.indexOf('#')
    val className = if (index > 0) {
      value.substring(0, value.indexOf('#'))
    } else {
      value
    }
    className
  }

  class Impl extends LoggerFactory {
    override def getLogger[T: LoggerResolver](instance: T): Logger = {
      val underlying = implicitly[LoggerResolver[T]].resolveLogger(instance)
      new Logger.Impl(CoreLogger(underlying))
    }
  }

}
