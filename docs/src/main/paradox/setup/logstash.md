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

The version of `logstash-logback-encoder` depends on Jackson 2.11, which is newer than the packaged version used by many Scala libraries such as Play and Akka which depend on an older version of [`jackson-module-scala`](https://github.com/FasterXML/jackson-module-scala).  If you see library incompatibility errors, add an explicit dependency on 2.11 to your project:

```text
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.0"
```

It is recommended (but not required) to use [Terse Logback](https://tersesystems.github.io/terse-logback/) on the backend.  Please see the documentation for what modules are appropriate for your use case.