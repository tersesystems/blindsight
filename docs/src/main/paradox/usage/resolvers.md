# Logger Resolvers

The easiest way to get at a logger is to use @scaladoc[LoggerFactory](com.tersesystems.blindsight.LoggerFactory$).  This looks like the SLF4J version, but is more flexible because it uses a @scaladoc[LoggerResolver](com.tersesystems.blindsight.LoggerResolver) type class under the hood.

@@snip [ResolverMain.scala](../../../test/scala/example/resolvers/ResolverMain.scala) { #simple-resolver }

There is also a macro based version which finds the enclosing class name and hands it to you:

@@snip [ResolverMain.scala](../../../test/scala/example/resolvers/ResolverMain.scala) { #macro-resolver }

Finally, you also have the option of creating your own @scaladoc[LoggerResolver](com.tersesystems.blindsight.LoggerResolver).  This is useful when you want to get away from class based logging, and use a naming strategy based on a correlation id.

For example, you can resolve a logger using a request:

@@snip [ResolverMain.scala](../../../test/scala/example/resolvers/ResolverMain.scala) { #request-resolver }

And from then on, you can do:

@@snip [ResolverMain.scala](../../../test/scala/example/resolvers/ResolverMain.scala) { #logger-from-request }

