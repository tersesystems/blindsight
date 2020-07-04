# Event Buffers

Blindsight can buffer logged events before they are sent to SLF4J.  Events contain a timestamp, the logging level, the logger name, and the @scaladoc[Entry](com.tersesystems.blindsight.Entry) that is about to be logged.

@@@ note

Because the event is generated just before being sent to Log4J, the timestamp of the event and the logging timestamp may differ, typically by milliseconds.

@@@
