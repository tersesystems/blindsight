# Setup

Blindsight depends on SLF4J using a service loader pattern, which is typically [Logback](http://logback.qos.ch/) or [Log4J 2](https://logging.apache.org/log4j/2.x/).  This means you should also plug in one of the service loader implementations, provided below.

## Logstash

The recommended option for Logback is to use `blindsight-logstash`.

Structured Logging is provided through the @ref:[DSL](../usage/dsl.md) on the logger and provides a mapping for @scaladoc[Argument](com.tersesystems.blindsight.Argument) and @scaladoc[Markers](com.tersesystems.blindsight.Markers) through [Logstash Markers and StructuredArguments](https://github.com/logstash/logstash-logback-encoder#event-specific-custom-fields).  Source information (line, file, enclosing) is rendered as logstash markers.

Add the given dependencies:

@@dependency[sbt,Maven,Gradle] {
group="com.tersesystems.blindsight"
artifact="blindsight-logstash_$scala.binary.version$"
version="$project.version.short$"
}

See [Github](https://github.com/tersesystems/blindsight#blindsight) for the latest version.

The version of `logstash-logback-encoder` depends on Jackson 2.11, which is newer than the packaged version used by many Scala libraries such as Play and Akka which depend on an older version of [`jackson-module-scala`](https://github.com/FasterXML/jackson-module-scala).  If you see library incompatibility errors, add an explicit dependency on 2.11 to your project:

@@dependency[sbt,Maven,Gradle] {
group="com.fasterxml.jackson.module"
artifact="jackson-module-scala_$scala.binary.version$"
version="2.11.0"
}

It is recommended (but not required) to use [Terse Logback](https://tersesystems.github.io/terse-logback/) on the backend.  Please see the documentation for what modules are appropriate for your use case.

## Generic

If you are using another SLF4J compatible framework like Log4J 2 or SLF4J Simple, or don't want to use the Logstash binding, you should use `blindsight-generic`, which has a service loader binding that depends solely on `slf4j-api`.  This package does not have the already configured implementation for @scaladoc[ArgumentResolver](com.tersesystems.blindsight.ArgumentResolver) or @scaladoc[MarkersResolver](com.tersesystems.blindsight.MarkersResolver), which means that you must implement these yourself for custom source code information.

@@dependency[sbt,Maven,Gradle] {
group="com.tersesystems.blindsight"
artifact="blindsight-generic_$scala.binary.version$"
version="$project.version.short$"
}

See [Github](https://github.com/tersesystems/blindsight#blindsight) for the latest version.

## JSON-LD

@ref:[JSON-LD support](../usage/jsonld.md) can be added for richer structured logging.

@@dependency[sbt,Maven,Gradle] {
group="com.tersesystems.blindsight"
artifact="blindsight-jsonld_$scala.binary.version$"
version="$project.version.short$"
}

See [Github](https://github.com/tersesystems/blindsight#blindsight) for the latest version.

## Ring Buffer

Blindsight comes with the option to add @ref[event buffering](../usage/buffer.md) to a logger, so that logged entries can be accessible from the application.  This can be very useful for debugging and for verifying test output.

A bounded in-memory ring buffer implementation based on `org.jctools.queues.MpmcArrayQueue` from the [JCTools project](https://jctools.github.io/JCTools/) is provided.  This implementation is thread-safe and [performant](http://psy-lob-saw.blogspot.com/p/lock-free-queues.html).  If you need another implementation (for example, you want to use an off-heap buffer or buffer to disk), then the @scaladoc[EventBuffer](com.tersesystems.blindsight.EventBuffer) interface is relatively straightforward to implement.

@@dependency[sbt,Maven,Gradle] {
group="com.tersesystems.blindsight"
artifact="blindsight-ringbuffer_$scala.binary.version$"
version="$project.version.short$"
}
