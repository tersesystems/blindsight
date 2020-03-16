# Source Code

SLF4J can give access to the line and file of source code, but this is done at runtime and is very expensive.  Blindsight provides this information for free, at compile time, through [sourcecode](https://github.com/lihaoyi/sourcecode) macros, using the @scaladoc[SourceInfoMixin](com.tersesystems.blindsight.api.mixins.SourceInfoMixin) on the logger.

```scala
trait SourceInfoMixin {
  def sourceInfoMarker(
      level: Level,
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Markers
}
```

When using `blindsight-generic`, this returns `Markers.empty`, but when using `blindsight-logstash`, this adds `source.line`, `source.file` and `source.enclosing` to the JSON logs automatically:

```json
{
  "@timestamp": "2020-04-12T17:58:45.410Z",
  "@version": "1",
  "message": "this is a test",
  "logger_name": "example.slf4j.Slf4jMain$",
  "thread_name": "run-main-0",
  "level": "DEBUG",
  "level_value": 10000,
  "source.line": 39,
  "source.file": "/home/wsargent/work/blindsight/example/src/main/scala/example/slf4j/Slf4jMain.scala",
  "source.enclosing": "example.slf4j.Slf4jMain.main"
}
```

This is the default behavior, and you can override `sourceInfoMarker` in your own implementation to return whatever you like.