package com.tersesystems

import scala.quoted.{Quotes, Expr, Varargs}

package object blindsight {

  extension (inline sc: StringContext) {
    inline def st(inline args: Any*): Statement = ${ statement('sc, 'args) }
  }

  private val OnlyOneMarker =
    "More than one marker argument is defined! Please provide a single Markers instance for multiple markers."
  private val MarkerAfterArguments =
    "Marker argument is after Arguments!  Please move the marker to the beginning of the string."
  private val MarkerAfterThrowable =
    "Marker argument is after throwable!  Please move the marker to the beginning of the string."

  private def statement(sc: Expr[StringContext], argsExpr: Expr[Seq[Any]])(using quotes: Quotes): Expr[Statement] = {
    import quotes.reflect.report
    argsExpr match
      case Varargs(argExprs) =>
        val argShowedExprs: Seq[Expr[Argument]] = argExprs.map {
          case '{ $arg: tp } =>
          Expr.summon[ToArgument[tp]] match
            case Some(toArg) =>
              '{ $toArg.toArgument($arg) }
            case None =>
              report.error(s"could not find implicit", arg); '{???}
        }
        // sc.parts.head
        '{ Statement("message", Arguments($argShowedExprs)) }
      case _ =>
        // `new StringContext(...).showMeExpr(args: _*)` not an explicit `showMeExpr"..."`
        report.error(s"Args must be explicit", argsExpr)
        '{???}
  }
}
