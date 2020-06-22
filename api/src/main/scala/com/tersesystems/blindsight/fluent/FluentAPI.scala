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

package com.tersesystems.blindsight.fluent

import com.tersesystems.blindsight.{Statement, ToArgument, ToMarkers, ToMessage}

/**
 * This trait is used for a fluent API builder.  It defers calling the arguments
 * until the `build` method is called, so everything here is call-by-name.
 */
trait FluentAPI {

  def statement(st: => Statement): FluentMethod.Builder

  def marker[T: ToMarkers](instance: => T): FluentMethod.Builder

  def message[T: ToMessage](instance: => T): FluentMethod.Builder

  def argument[T: ToArgument](instance: => T): FluentMethod.Builder

  def cause(e: Throwable): FluentMethod.Builder
}
