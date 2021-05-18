# Scripting

There are times when you want to reconfigure logging behavior.  You can do that at the macro level with logging levels, but Blindsight gives you far more control, allowing you to change logging by individual method or even line number, in conjunction with [Tweakflow Scripts](https://twineworks.github.io/tweakflow/index.html) that can be modified while the JVM is running.

## Installation

This library is in `blindsight-scripting` and depends on `blindsight-api`: 

@@dependency[sbt,Maven,Gradle] {
group="com.tersesystems.blindsight"
artifact="blindsight-scripting_$scala.binary.version$"
version="$project.version.short$"
}

## Usage

Here's an example Tweakflow script that will only enable logging that are in given methods and default to a level:

```tweakflow
library blindsight {
  # level: the result of org.slf4j.event.Level.toInt()
  # enc: <class>.<method> i.e. com.tersesystems.blindsight.groovy.Main.logDebugSpecial
  # line: line number of the source code where condition was created
  # file: absolute path of the file containing the condition
  #
  doc 'Evaluates a condition'
  function evaluate: (long level, string enc, long line, string file) ->
    if (enc == "exampleapp.MyClass.logDebugSpecial") then true
    else (level >= 20); # info_int = 20
}
```

In this case, the script will return `true` if the logging statement is in the `logDebugSpecial` method of `exampleapp.MyClass`:

```scala
package exampleapp
class MyClass {
  def logDebugSpecial(): Unit = {
    logger.debug.when(location.here) { log => log("This will log!")}
  }
}
```

Otherwise, the script will return true iff the level is above or equal to 20 (the integer value of `INFO`).

Tweakflow has its own reference documentation, but it does not cover the standard library functions which include string matching.  The [test suite](https://github.com/twineworks/tweakflow/tree/master/src/test/resources/spec/std/strings) is a good place to start to show the standard library's capabilities.

## Configuration

Script-driven logging is most useful when it replaces level based logging, so in `logback.xml` you should set the level to `ALL` for the package you want:

```xml
<logger name="exampleapp" value="ALL"/>
```

@@@warning

You should **not** set the root logger level to `ALL`.   

Since Blindsight can only add source level information to your own code, packages based on libraries, i.e. `play.api` and `akka` will still use the SLF4J API directly and will not go through scripting. 

@@@

You can integrate tweakflow scripts through @scaladoc[ScriptHandle](com.tersesystems.blindsight.scripting.ScriptHandle) and  @scaladoc[ScriptManager](com.tersesystems.blindsight.scripting.ScriptManager) instances.  When the handle's `isInvalid` method returns `true`, the script is re-evaluated by the @scaladoc[ScriptManager](com.tersesystems.blindsight.scripting.ScriptManager) on the fly.  

An example @scaladoc[FileScriptHandle](com.tersesystems.blindsight.scripting.FileScriptHandle) that compares the file's last modified date to determine validity. A verifier function is provided, which can be leveraged to check the script with a message authentication code.  Please see the `SignatureBuilder` in the test cases on Github for examples.

## Script Aware Logging

You can leverage scripting from a @scaladoc[ScriptAwareLogger](com.tersesystems.blindsight.scripting.ScriptAwareLogger).  All calls to the logger will pass through the script automatically.

You can create @scaladoc[ScriptAwareLogger](com.tersesystems.blindsight.scripting.ScriptAwareLogger) directly with a CoreLogger:

```scala
import com.tersesystems.blindsight.scripting._
val slf4jLogger = org.slf4j.LoggerFactory.getLogger(getClass)
val scriptManager: ScriptManager = ???
val logger = new ScriptAwareLogger(CoreLogger(slf4jLogger), scriptManager)
```

Or you can register all logging using a custom logger factory.

Start by installing @ref:[`blindsight-generic`](../setup/index.md) which does not have a service provider already exposed.

Create a factory class:

@@snip [ScriptingLoggerFactory.scala](../../../test/scala/example/scripting/ScriptingLoggerFactory.scala) { #scripting_logger_factory }

To activate the `ScriptingLoggerFactory`, you must register it with the [service loader](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html#register-service-providers) by creating a service provider configuration file.  In a `resources` directory, create a `META-INF/services` directory, and create a `com.tersesystems.blindsight.LoggerFactory` file containing the following:

```
# com.tersesystems.blindsight.LoggerFactory
com.tersesystems.blindsight.scripting.ScriptingLoggerFactory
```

## Script Conditions

If you only want some logging statements to be source aware, you can use a @scaladoc[ScriptBasedLocation](com.tersesystems.blindsight.scripting.ScriptBasedLocation), which returns a condition containing the source code information used by a script.

@@snip [ConditionExample.scala](../../../test/scala/example/scripting/ConditionExample.scala) { #scripting_condition }

## Other Scripting Languages

You can also integrate Blindsight with more powerful scripting languages like Groovy using [JSR-223](https://docs.oracle.com/en/java/javase/12/scripting/java-scripting-api.html#GUID-C4A6EB7C-0AEA-45EC-8662-099BDEFC361A), but they do allow arbitrary execution and can pose a security risk.  Please see the [blog post](https://tersesystems.com/blog/2021/05/02/dynamic-logging-with-conditions/) for details.

