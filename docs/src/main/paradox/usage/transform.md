# Entry Transformation

An @scaladoc[Entry](com.tersesystems.blindsight.Entry) is a "raw" statement that is a representation of what will be sent to SLF4J.  It consists of an `Option[org.slf4j.Marker]`, a message of type `String`, and arguments of type `Option[Array[Any]]`.

Blindsight can add transformation steps to modify or replace the @scaladoc[Entry](com.tersesystems.blindsight.Entry) just before it is logged.  

You can apply multiple transformations, and they will be processed in order.

You can use entry transformations in conjunction with @ref:[event buffering](buffer.md).