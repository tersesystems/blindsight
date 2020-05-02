# Generic

If you are using another SLF4J compatible framework like Log4J 2 or SLF4J Simple, or don't want to use the Logstash binding, you should use `blindsight-generic`, which has a serviceloader binding that depends solely on `slf4j-api`.

Add the bintray resolver:

```scala
resolvers += Resolver.bintrayRepo("tersesystems", "maven")
```

And then add the dependency:

@@dependency[sbt,Maven,Gradle] {
  group="com.tersesystems.blindsight"
  artifact="blindsight-generic_$scala.binary.version$"
  version="$project.version.short$"
}

See [Github](https://github.com/tersesystems/blindsight#blindsight) for the latest version.
