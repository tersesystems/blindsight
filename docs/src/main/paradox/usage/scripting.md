# Scripting

There are times when you want to reconfigure logging behavior.  You can do that at the macro level with logging levels, but Blindsight gives you far more control, allowing you to change logging by individual method or even line number, in conjunction with [Tweakflow Scripts](https://twineworks.github.io/tweakflow/index.html) that can be modified while the JVM is running.

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
    if (enc == "com.tersesystems.blindsight.scripting.Main.logDebugSpecial") then true
    if (enc == "com.tersesystems.blindsight.scripting.Main.logInfoSpecial") then false
    else (level >= 20); # info_int = 20
}
```

You can integrate tweakflow scripts through `ScriptHandle` and `ScriptManager` instances.  When the handle's `isInvalid` method returns `true`, the script is re-evaluated by the `ScriptManager` on the fly.  An example `FileScriptHandle` is provided.

## ScriptingLoggerFactory

You can leverage scripting from a logger factory by pointing it to a script.  This will mean that all calls to the logger will pass through the script automatically.

@@snip [ScriptingLoggerFactory.scala](../../../test/scala/example/scripting/ScriptingLoggerFactory.scala) { #scripting_logger_factory }

To activate the `ScriptingLoggerFactory`, you must register it with the service loader.  In a `resources` directory, create a `META-INF/services` directory, and create a `com.tersesystems.blindsight.LoggerFactory` file containing the following

```
# com.tersesystems.blindsight.LoggerFactory
com.tersesystems.blindsight.scripting.ScriptingLoggerFactory
```

## Script Conditions

If you only want some logging statements to be source aware, you can use a `SourceAwareLocation`, which returns a condition containing the source code information used by a script.

@@snip [ConditionExample.scala](../../../test/scala/example/scripting/ConditionExample.scala) { #scripting_condition }

## Other Scripting Languages

You can also integrate Blindsight with more powerful scripting languages like Groovy using [JSR-223](https://docs.oracle.com/en/java/javase/12/scripting/java-scripting-api.html#GUID-C4A6EB7C-0AEA-45EC-8662-099BDEFC361A), but they do allow arbitrary execution and can pose a security risk.  Please see the [blog post](https://tersesystems.com/blog/2021/05/02/dynamic-logging-with-conditions/) for details.

