# Source Code

SLF4J can give access to the line and file of source code, but this is done at runtime and is very expensive.  Blindsight provides this information for free, at compile time, through [sourcecode](https://github.com/com-lihaoyi/sourcecode) macros.  Internally, the @scaladoc[LogstashLoggerFactory](com.tersesystems.blindsight.logstash.LogstashLoggerFactory) adds extra markers to logging statements based on the macros.

To enable this, use `blindsight-logstash` and add a `blindsight.source.enabled` property to the Logback context with the value of `true`:

```xml
<configuration>
  <property name="blindsight.source.enabled" value="true" scope="context"/>
 
  <!-- ... -->
</configuration>
```

This adds `source.line`, `source.file` and `source.enclosing` to the JSON logs:

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

This is the default behavior.  You can change the name of the labels used in the event that you have a conflict.

> For example, [Filebeat 6.8 and later](https://www.elastic.co/guide/en/beats/filebeat/6.8/index.html) uses [source.*](https://www.elastic.co/guide/en/beats/filebeat/6.8/migration-changed-fields.html#_the_file_field_was_renamed_to_source) internally and will not collect logs if a field with that prefix already exists.

To change the source labels to fit [Elastic Common Schema](https://www.elastic.co/guide/en/ecs/1.7/ecs-log.html#field-log-origin-file-line), you can set the properties in `logback.xml`:

```xml
<configuration>
  <property name="blindsight.source.file" value="log.origin.file.name" scope="context"/>
  <property name="blindsight.source.line" value="log.origin.file.line" scope="context"/>
  <property name="blindsight.source.enclosing" value="log.origin.function" scope="context"/>

<!-- ... -->
</configuration>
```

