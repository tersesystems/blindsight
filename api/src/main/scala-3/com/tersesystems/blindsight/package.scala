package com.tersesystems

import scala.quoted.{Expr, Quotes, Varargs}

package object blindsight {

  implicit class StatementOps(sc: StringContext) {
    inline def st(inline args: Any*): Statement = ${ impl('this, 'args) }
  }

  private val OnlyOneMarker =
    "More than one marker argument is defined! Please provide a single Markers instance for multiple markers."
  private val MarkerAfterArguments =
    "Marker argument is after Arguments!  Please move the marker to the beginning of the string."
  private val MarkerAfterThrowable =
    "Marker argument is after throwable!  Please move the marker to the beginning of the string."

  private def impl(sops: Expr[StatementOps], argsExpr: Expr[Seq[Any]])(using quotes: Quotes): Expr[Statement] = {
    import quotes.reflect.*

    val partz: List[String] = sops.asTerm.underlyingArgument match {
      case Apply(conv, List(Apply(fun, List(Typed(Repeated(values, _), _))))) =>
        values.collect { case Literal(StringConstant(value)) => value }
    }

    // https://github.com/lampepfl/dotty/blob/master/tests/run-macros/f-interpolation-1/FQuote_1.scala
    val Typed(Repeated(allArgs, _), _) = argsExpr.asTerm.underlyingArgument

    if (allArgs.nonEmpty) {
      argsExpr match
        case Varargs(elements) =>
          val argumentList = scala.collection.mutable.Buffer[Expr[Argument]]()
          var markersExpr: Option[Expr[Markers]] = None
          var throwableExpr: Option[Expr[Throwable]] = None

          def assertMarkerConditions(): Unit =
            if (markersExpr.isDefined) {
              report.error(OnlyOneMarker)
            } else if (throwableExpr.isDefined) {
              // must be before arguments (args adds ex to both argslist and throwableExpr)
              report.error(MarkerAfterThrowable)
            } else if (argumentList.nonEmpty) {
              report.error(MarkerAfterArguments)
            }

          elements.foreach {
            case '{ $marker: org.slf4j.Marker } =>
              assertMarkerConditions()
              markersExpr = Some('{ Markers($marker) })

            case '{ $markers: Markers } =>
              assertMarkerConditions()
              markersExpr = Some(markers)

            case '{ $t: Throwable } =>
              throwableExpr = Some(t)

            case '{ $arg: tp } =>
              Expr.summon[ToArgument[tp]].foreach { toArg =>
                argumentList.append('{ $toArg.toArgument($arg) })
              }
              Expr.summon[ToMarkers[tp]].foreach { toMarkers =>
                markersExpr = Some('{ $toMarkers.toMarkers($arg) })
              }
            }
          val inputSeq = Expr.ofSeq(argumentList.toSeq)
          val arguments = '{Arguments($inputSeq)}

          // statement message is made up of the constant parts of string.
          //val messageList = partz.map {
          //  case Literal(Constant(const: String)) => const
          //  case other                            => throw new IllegalStateException("Unknown case " + other)
          //}
          val messageList = partz

          if (markersExpr.isEmpty) {
            val message = Expr(messageList.mkString("{}"))
            if (throwableExpr.isEmpty) {
              '{Statement(Message($message), $arguments)}
            } else {
              val throwable = throwableExpr.get
              '{Statement(Message($message), $arguments, $throwable)}
            }
          } else {
            // Remove the first {} as it is a marker.
            // While it's technically possible to have multiple markers in the statement,
            // st"$marker1 $marker2 etc"
            // then we'd have to figure out what to do with the whitespace in between (trim? leave?),
            // and it's much simpler to require a preaggregated one.
            val str = messageList.mkString("{}").replaceFirst("\\{}", "")
            val message: Expr[String] = Expr(str)

            val markers = markersExpr.get
            if (throwableExpr.isEmpty) {
              '{Statement($markers, Message($message), $arguments)}
            } else {
              val throwable = throwableExpr.get
              '{Statement($markers, Message($message), $arguments, $throwable)}
            }
          }
        case tree =>
          report.error("Arguments without varargs?")
          return '{???}
    } else {
      val parts = Expr(partz.mkString(""))
      // No args, just a string, halp
      '{ Statement(Message($parts)) }
    }
  }
}
