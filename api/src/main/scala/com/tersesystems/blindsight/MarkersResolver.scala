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

import java.util.ServiceLoader

import com.tersesystems.blindsight.AST.BObject

trait MarkersResolver {
  def resolve(bobject: BObject): Markers
}

object MarkersResolver {

  def apply(bobject: BObject): Markers = resolver.resolve(bobject)

  private val resolverLoader = ServiceLoader.load(classOf[MarkersResolver])

  private lazy val resolver: MarkersResolver = {
    import javax.management.ServiceNotFoundException
    import scala.collection.JavaConverters._

    resolverLoader.iterator().asScala.find(_ != null).getOrElse {
      throw new ServiceNotFoundException("No markers resolver found!")
    }
  }
}
