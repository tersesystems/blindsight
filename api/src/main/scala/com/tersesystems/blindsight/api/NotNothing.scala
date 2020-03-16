package com.tersesystems.blindsight.api

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
