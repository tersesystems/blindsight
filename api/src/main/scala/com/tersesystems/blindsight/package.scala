package com.tersesystems

import scala.language.experimental.macros

package object blindsight {

  implicit class StatementContext(val sc: StringContext) extends AnyVal {
    def st(args: Any*): Statement = macro impl.statement
  }

  private object impl {
    import scala.collection.mutable
    import scala.reflect.macros.blackbox

    private val OnlyOneMarker =
      "More than one marker argument is defined! Please provide a single Markers instance for multiple markers."
    private val MarkerAfterArguments =
      "Marker argument is after Arguments!  Please move the marker to the beginning of the string."
    private val MarkerAfterThrowable =
      "Marker argument is after throwable!  Please move the marker to the beginning of the string."

    def statement(c: blackbox.Context)(args: c.Expr[Any]*): c.Expr[Statement] = {
      import c.universe._

      def isThrowable(el: Tree) = el.tpe <:< typeOf[Throwable]
      def isMarker(el: Tree)    = el.tpe <:< typeOf[org.slf4j.Marker]
      def isMarkers(el: Tree)   = el.tpe <:< typeOf[Markers]

      if (args.nonEmpty) {
        c.prefix.tree match {
          case Apply(_, List(Apply(_, partz))) =>
            val argumentList: mutable.Buffer[c.Expr[Argument]] = mutable.Buffer()
            var markersExpr: Option[c.Expr[Markers]]           = None
            var throwableExpr: Option[c.Expr[Throwable]]       = None

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
                    q"implicitly[com.tersesystems.blindsight.ToArgument[${el.tpe}]].toArgument($el)"
                  )
              }
            }
            val arguments = q"com.tersesystems.blindsight.Arguments.fromSeq(Array[Argument](..$argumentList))"

            // statement message is made up of the constant parts of string.
            val messageList = partz.map { case Literal(Constant(const: String)) => const }

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
        }).map { case Literal(Constant(s: String)) => s }
        c.Expr(q"com.tersesystems.blindsight.Statement(${constants.mkString})")
      }
    }
  }
}
