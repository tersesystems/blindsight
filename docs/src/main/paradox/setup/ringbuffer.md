# Ring Buffer

Blindsight comes with the option to add @ref[event buffering](../usage/buffer.md) to a logger, so that logged entries can be accessible from the application.  This can be very useful for debugging and for verifying test output.

A bounded in-memory ring buffer implementation based on `org.jctools.queues.MpmcArrayQueue` from the [JCTools project](https://jctools.github.io/JCTools/) is provided.  This implementation is thread-safe and [performant](http://psy-lob-saw.blogspot.com/p/lock-free-queues.html).  If you need another implementation (for example, you want to use an off-heap buffer or buffer to disk), then the @scaladoc[EventBuffer](com.tersesystems.blindsight.EventBuffer) interface is relatively straightforward to implement.

To add the ringbuffer implementation, add the following resolver:
 
```scala
resolvers += Resolver.bintrayRepo("tersesystems", "maven")
```

And then add the dependency:

@@dependency[sbt,Maven,Gradle] {
  group="com.tersesystems.blindsight"
  artifact="blindsight-ringbuffer_$scala.binary.version$"
  version="$project.version.short$"
}
