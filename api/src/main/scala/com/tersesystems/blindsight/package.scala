package com.tersesystems

import scala.language.experimental.macros

package object blindsight {

  implicit class StatementContext(val sc: StringContext) extends AnyVal {
    def st(args: Any*): Statement = macro impl.statement
  }

  object StatementContext {

    def stringBuilder(): ParameterizedStringBuilder = ParameterizedStringBuilder()

    /**
     * Creates an SLF4J parameterized string, swapping out [[Argument]] instances for `{}` and
     * rendering nothing if a [[Markers]] or [[org.slf4j.Marker]] is passed in.
     *
     * This class will use a threadlocal instance of StringBuilder and reuse it for parsing,
     * which makes it faster than your average StringBuilder.
     *
     * It is used by the statement interpolation macro, and should not be used directly in your application.
     *
     * @param initialCapacity the initial capacity of a string builder.
     */
    class ParameterizedStringBuilder private (initialCapacity: Int) {
      private[this] val sb = new java.lang.StringBuilder(initialCapacity)

      def capacity: Int = sb.capacity

      def setLength(length: Int): Unit = {
        sb.setLength(length)
      }

      def append(sb: StringBuffer): ParameterizedStringBuilder = {
        sb.append(sb)
        this
      }

      def append(s: CharSequence): ParameterizedStringBuilder = {
        sb.append(s)
        this
      }

      def append(s: String): ParameterizedStringBuilder = {
        sb.append(s)
        this
      }

      def append(boolean: Boolean): ParameterizedStringBuilder = {
        sb.append(boolean)
        this
      }

      def append(byte: Byte): ParameterizedStringBuilder = {
        sb.append(byte)
        this
      }

      def append(ch: Char): ParameterizedStringBuilder = {
        sb.append(ch)
        this
      }

      def append(short: Short): ParameterizedStringBuilder = {
        sb.append(short)
        this
      }

      def append(int: Int): ParameterizedStringBuilder = {
        sb.append(int)
        this
      }

      def append(long: Long): ParameterizedStringBuilder = {
        sb.append(long)
        this
      }

      def append(double: Double): ParameterizedStringBuilder = {
        sb.append(double)
        this
      }

      def append(float: Float): ParameterizedStringBuilder = {
        sb.append(float)
        this
      }

      def append(marker: org.slf4j.Marker): ParameterizedStringBuilder = {
        this
      }

      def append(markers: Markers): ParameterizedStringBuilder = {
        this
      }

      def append(t: Throwable): ParameterizedStringBuilder = {
        sb.append(t.toString)
        this
      }

      def append[A: ToArgument](instance: A): ParameterizedStringBuilder = {
        sb.append("{}")
        this
      }

      override def toString: String = sb.toString
    }

    private object ParameterizedStringBuilder {
      private[this] final val size = 4096

      def apply(): ParameterizedStringBuilder = pool.get()

      private[this] final val pool = new ThreadLocal[ParameterizedStringBuilder] {
        override def initialValue(): ParameterizedStringBuilder = new ParameterizedStringBuilder(size)

        override def get(): ParameterizedStringBuilder = {
          var sb = super.get()
          if (sb.capacity > size) {
            sb = initialValue()
            set(sb)
          } else sb.setLength(0)
          sb
        }
      }
    }
  }

  private object impl {
    import scala.collection.mutable
    import scala.reflect.macros.blackbox

    private[this] val OnlyOneMarker =
      "More than one marker argument is defined! Please provide a single Markers instance for multiple markers."
    private[this] val MarkerAfterArguments =
      "Marker argument is after Arguments!  Please move the marker to the beginning of the string."
    private[this] val MarkerAfterThrowable =
      "Marker argument is after throwable!  Please move the marker to the beginning of the string."

    def statement(c: blackbox.Context)(args: c.Expr[Any]*): c.Expr[Statement] = {
      import c.universe._

      def isThrowable(el: Tree): Boolean = el.tpe <:< typeOf[Throwable]
      def isMarker(el: Tree): Boolean    = el.tpe <:< typeOf[org.slf4j.Marker]
      def isMarkers(el: Tree): Boolean   = el.tpe <:< typeOf[Markers]
      def isPrimitive(el: Tree): Boolean = {
        val tpe = el.tpe
        (tpe <:< typeOf[Boolean]) ||
          (tpe <:< typeOf[Byte]) ||
          (tpe <:< typeOf[Short]) ||
          (tpe <:< typeOf[Char]) ||
          (tpe <:< typeOf[Int]) ||
          (tpe <:< typeOf[Long]) ||
          (tpe <:< typeOf[Float]) ||
          (tpe <:< typeOf[Double]) ||
          (tpe <:< typeOf[String])
      }

      def createMessage(constants: List[String]): c.Expr[String] = {
        val (valDeclarations, values) = args.map { arg =>
          arg.tree match {
            case tree @ Literal(Constant(_)) =>
              (EmptyTree, if (tree.tpe <:< definitions.NullTpe) q"(null: String)" else tree)
            case tree =>
              val name = TermName(c.freshName())
              val tpe = if (tree.tpe <:< definitions.NullTpe) typeOf[String] else tree.tpe
              (q"val $name: $tpe = $arg", Ident(name))
          }
        }.unzip

        val appends = constants.zipAll(values, "", null)
          .foldLeft(q"com.tersesystems.blindsight.StatementContext.stringBuilder()") { case (sb, (s, v)) =>
            val len = s.length
            if (len == 0) {
              if (v == null) sb
              else q"$sb.append($v)"
            } else if (len == 1) {
              if (v == null) q"$sb.append(${s.charAt(0)})"
              else q"$sb.append(${s.charAt(0)}).append($v)"
            } else {
              if (v == null) q"$sb.append($s)"
              else q"$sb.append($s).append($v)"
            }
          }

        c.Expr(c.typecheck(q"..$valDeclarations; $appends.toString"))
      }

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

            for (index <- 0 until args.size) {
              val t = args(index)
              val el = t.tree
              el match {
                case _ if isMarker(el) =>
                  assertMarkerConditions()
                  markersExpr = Some(c.Expr[Markers](q"com.tersesystems.blindsight.Markers($el)"))
                case _ if isMarkers(el) =>
                  assertMarkerConditions()
                  markersExpr = Some(c.Expr[Markers](q"$el"))
                case throwableTree if isThrowable(el) =>
                  throwableExpr = Some(c.Expr[Throwable](q"$el"))
                case _ if isPrimitive(el) =>
                // do not add it as an argument, parameterized string will inline it.
                case _ =>
                  argumentList += c.Expr[Argument](
                    q"com.tersesystems.blindsight.Argument($el)"
                  )
              }
            }
            val arguments =
              q"com.tersesystems.blindsight.Arguments.fromArray(Array[Argument](..$argumentList))"

            val constants = partz.map { case Literal(Constant(const: String)) => const }
            val message = createMessage(constants)
            if (markersExpr.isEmpty) {
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
