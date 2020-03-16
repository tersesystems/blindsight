# Principles

Blindsight has some organizing principles that inform the design.

* Loggers depend directly and solely on the SLF4J API, which can always be accessed directly.
* APIs can be extended or replaced for domain specific logging.
* Knowing **when** and **when not** to log is more important than "how fast" you log.
* Loggers can be resolved from user defined context, not simply by name or by class.
* Structured logging is baked in, uses standard Scala idioms, and can be overridden.

Likewise, there are things that Blindsight eschews:

* No effects; logging is always a side effect.
* No constraints or configuration on SLF4J implementation.
* No FP library requirements; no need for scalaz, cats, zio etc.
* No formatting on the front end; messages should not contain JSON/XML.

See the overview page for how Blindsight does this.
