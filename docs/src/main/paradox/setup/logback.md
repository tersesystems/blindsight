# Logback

The recommended option for Logback is to use `blindsight-logstash`, which includes source information (line, file, enclosing) as @ref:[logstash markers](../usage/structured.md) on the logger and provides a mapping for `Arguments` and `Markers`.

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

See [Github Badge](https://github.com/tersesystems/blindsight#blindsight) for the latest version.

It is recommended (but not required) to use [Terse Logback](https://tersesystems.github.io/terse-logback/) on the backend:

@@dependency[sbt,Maven,Gradle] {
  group="com.tersesystems.logback"
  artifact="logback-structured-config"
  version="latest.version"
}
