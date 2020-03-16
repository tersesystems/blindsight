# Source Info

common "source code" context to your program at runtime.   https://github.com/lihaoyi/sourcecode#logging 

```scala
trait SourceInfoSupport {
  def sourceInfoMarker(
      level: Level,
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Option[Marker]
}
```
 
```scala
trait LogstashSourceInfoSupport extends SourceInfoSupport {
  override def sourceInfoMarker(
      level: Level,
      line: Line,
      file: File,
      enclosing: Enclosing
  ): Option[Marker] = {
    import com.tersesystems.blindsight.Implicits._
    val lineMarker      = Markers.append("line", line.value)
    val fileMarker      = Markers.append("file", file.value)
    val enclosingMarker = Markers.append("enclosing", enclosing.value)
    Some(lineMarker :+ fileMarker :+ enclosingMarker)
  }
}
``` 