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

import scala.annotation.implicitNotFound

// https://riptutorial.com/scala/example/21134/preventing-inferring-nothing
@implicitNotFound("Nothing was inferred")
sealed trait NotNothing[-T]

object NotNothing {
  implicit object notNothing extends NotNothing[Any]
  //We do not want Nothing to be inferred, so make an ambigous implicit
  implicit object `\n The error is because the type parameter was resolved to Nothing`
      extends NotNothing[Nothing]
}

//object NotEqual {
//  trait <:!<[A, B]
//
//  // implicit ev1: A1 <:!< Throwable
//
//  implicit def nsub[A, B] : A <:!< B = new <:!<[A, B] {}
//  implicit def nsubAmbig1[A, B >: A] : A <:!< B = sys.error("Unexpected call")
//  implicit def nsubAmbig2[A, B >: A] : A <:!< B = sys.error("Unexpected call")
//}
