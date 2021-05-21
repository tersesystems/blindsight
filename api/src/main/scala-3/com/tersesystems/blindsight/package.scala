package com.tersesystems

import com.tersesystems.blindsight.impl

import scala.quoted.{Quotes, Expr}

package object blindsight {

  implicit class StatementContext(val sc: StringContext) extends AnyVal {
    inline def st(inline args: Any*): Statement = ${statement('args)}
  }

  private val OnlyOneMarker =
    "More than one marker argument is defined! Please provide a single Markers instance for multiple markers."
  private val MarkerAfterArguments =
    "Marker argument is after Arguments!  Please move the marker to the beginning of the string."
  private val MarkerAfterThrowable =
    "Marker argument is after throwable!  Please move the marker to the beginning of the string."

  private def statement(args: Expr[Seq[Any]])(using quotes: Quotes): Expr[Statement] = {
    import scala.collection.mutable

    def isThrowable(el: Tree) = el.tpe <:< typeOf[Throwable]
    def isMarker(el: Tree)    = el.tpe <:< typeOf[org.slf4j.Marker]
    def isMarkers(el: Tree)   = el.tpe <:< typeOf[Markers]

    // https://docs.scala-lang.org/scala3/guides/macros/macros.html#working-with-varargs
    if (args.nonEmpty) {
      quotes.reflect.tree match {
        case Apply(_, List(Apply(_, partz))) =>
          val argumentList: mutable.Buffer[Expr[Argument]] = mutable.Buffer()
          var markersExpr: Option[Expr[Markers]]           = None
          var throwableExpr: Option[Expr[Throwable]]       = None

          def assertMarkerConditions(): Unit =
            if (markersExpr.isDefined) {
              c.abort(c.enclosingPosition, OnlyOneMarker)
            } else if (throwableExpr.isDefined) {
              // must be before arguments (args adds ex to both argslist and throwableExpr)
              c.abort(c.enclosingPosition, MarkerAfterThrowable)
            } else if (argumentList.nonEmpty) {
              c.abort(c.enclosingPosition, MarkerAfterArguments)
            }

          // arguments are converted using ToArgument, or are throwable which render as strings.
          args.foreach { t =>
            val el = t.tree
            el match {
              case _ if isMarker(el) =>
                assertMarkerConditions()
                markersExpr = Some(c.Expr[Markers](q"com.tersesystems.blindsight.Markers($el)"))
              case _ if isMarkers(el) =>
                assertMarkerConditions()
                markersExpr = Some(c.Expr[Markers](q"$el"))
              case _ if isThrowable(el) =>
                throwableExpr = Some(c.Expr[Throwable](q"$el"))
                argumentList += c.Expr[Argument](
                  q"com.tersesystems.blindsight.Argument($el.toString)"
                )
              case _ =>
                argumentList += c.Expr[Argument](
                  q"com.tersesystems.blindsight.Argument($el)"
                )
            }
          }
          val arguments =
            q"com.tersesystems.blindsight.Arguments.fromArray(Array[Argument](..$argumentList))"

          // statement message is made up of the constant parts of string.
          val messageList = partz.map {
            case Literal(Constant(const: String)) => const
            case other                            => throw new IllegalStateException("Unknown case " + other)
          }

          if (markersExpr.isEmpty) {
            val message = messageList.mkString("{}")
            if (throwableExpr.isEmpty) {
              c.Expr(
                q"com.tersesystems.blindsight.Statement($message, $arguments)"
              )
            } else {
              val throwable = throwableExpr.get
              c.Expr(
                q"com.tersesystems.blindsight.Statement($message, $arguments, $throwable)"
              )
            }
          } else {
            // Remove the first {} as it is a marker.
            // While it's technically possible to have multiple markers in the statement,
            // st"$marker1 $marker2 etc"
            // then we'd have to figure out what to do with the whitespace in between (trim? leave?),
            // and it's much simpler to require a preaggregated one.
            val message = messageList.mkString("{}").replaceFirst("\\{}", "")

            val markers = markersExpr.get
            if (throwableExpr.isEmpty) {
              c.Expr(
                q"com.tersesystems.blindsight.Statement($markers, $message, $arguments)"
              )
            } else {
              val throwable = throwableExpr.get
              c.Expr(
                q"com.tersesystems.blindsight.Statement($markers, $message, $arguments, $throwable)"
              )
            }
          }
        case _ =>
          c.abort(c.prefix.tree.pos, "The pattern can't be used with the interpolation.")
      }
    } else {
      val constants = (c.prefix.tree match {
        case Apply(_, List(Apply(_, literals))) => literals
        case other                              => throw new IllegalStateException("unknown case " + other)
      }).map {
        case Literal(Constant(s: String)) => s
        case other                        => throw new IllegalStateException("unknown case " + other)
      }
      c.Expr(q"com.tersesystems.blindsight.Statement(${constants.mkString})")
    }
  }
}
