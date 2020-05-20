# Logback with Logstash 

The recommended option for Logback is to use `blindsight-logstash`.
 
Structured Logging is provided through the @ref:[DSL](../usage/dsl.md) on the logger and provides a mapping for @scaladoc[Argument](com.tersesystems.blindsight.Argument) and @scaladoc[Markers](com.tersesystems.blindsight.Markers) through [Logstash Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields).  Source information (line, file, enclosing) is rendered as logstash markers.

Add the following resolver:
 
```scala
resolvers += Resolver.bintrayRepo("tersesystems", "maven")
```

And then add the given dependencies:

@@dependency[sbt,Maven,Gradle] {
  group="com.tersesystems.blindsight"
  artifact="blindsight-logstash_$scala.binary.version$"
  version="$project.version.short$"
}

See [Github](https://github.com/tersesystems/blindsight#blindsight) for the latest version.

It is recommended (but not required) to use [Terse Logback](https://tersesystems.github.io/terse-logback/) on the backend.  Please see the documentation for what modules are appropriate for your use case.