@@@ index

* [Logback](logstash.md)
* [Generic](generic.md)
* [Ring Buffer](ringbuffer.md)

@@@

# Setup

Blindsight depends on SLF4J using a service loader pattern, which is typically [Logback](http://logback.qos.ch/) or [Log4J 2](https://logging.apache.org/log4j/2.x/).  This means you should also plug in one of the service loader implementations, provided below.

@@toc { depth=1 }