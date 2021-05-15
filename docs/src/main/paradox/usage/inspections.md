# Inspections

Most debugging starts off with "printf debugging", where `println` and `logger.debug` statements are thrown into the code to see the internal state of an object or variable.  Inserting statements into the code base for exploratory debugging is by far the most popular means of debugging, and most developers agree that [the best invention in debugging was printf debugging](https://www.spinellis.gr/pubs/conf/2018-ICSE-debugging-analysis/html/BSSZ18.pdf).

Given that we already know that a good part of debugging involves inspecting internal state, we can leverage Scala's [macro system](https://docs.scala-lang.org/overviews/macros/overview.html) to do the work for us.

@@@ note

This code has not been rigorously tested in the field and should be considered experimental.  Please file bugs and PRs if you run into problems or edge cases.

@@@

## Usage

This library is in `blindsight-inspection` and does not depend on the rest of Blindsight: 

@@dependency[sbt,Maven,Gradle] {
group="com.tersesystems.blindsight"
artifact="blindsight-inspection_$scala.binary.version$"
version="$project.version.short$"
}

You can import the methods using an import, or by incorporating the methods in a trait.

```scala
import com.tersesystems.blindsight.inspection.InspectionMacros._
```

## Decorate Ifs

This inspection decorates an `if` statement with logging code on every branch:

```scala
decorateIfs(dif => logger.debug(s"${dif.code} = ${dif.result}")) {
  if (System.currentTimeMillis() % 17 == 0) {
    println("branch 1")
  } else if (System.getProperty("derp") == null) {
    println("branch 2")
  } else {
    println("else branch")
  }
}
```

## Decorate Match

This inspection decorates a `match` statement with logging code on every `case` statement:

```scala
val string = java.time.Instant.now().toString
decorateMatch(dm => logger.debug(s"${dm.code} = ${dm.result}")) {
  string match {
    case s if s.startsWith("20") =>
      println("this example is still valid")
    case _ =>
      println("oh dear")
  }
}
```

## Decorate Vals

This inspection decorates a block so that every `val` or `var` statement has a logging statement after its definition:

```scala
decorateVals(dval => logger.debug(s"${dval.name} = ${dval.value}")) {
  val a = 5
  val b = 15
  a + b
}
```

## Dump Expression

This inspection dumps the code of an expression together with the result:

```scala
val dr = dumpExpression(1 + 1)
logger.debug(s"result: ${dr.code} = ${dr.value}")
```

## Dump Public Fields

This inspection dumps the public fields of an object:

```scala
class ExampleClass(val someInt: Int) {
  protected val protectedInt = 22
}
val exObj        = new ExampleClass(42)
val publicFields = dumpPublicFields(exObj)

logger.debug(s"We set exObj.someInt as 42, actual value is $publicFields")
```

