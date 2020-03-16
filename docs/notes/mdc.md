

https://olegpy.com/better-logging-monix-1/

https://github.com/skjolber/json-log-domain#mdc-style-logging

```java
try (AutoCloseable a =  mdc(host("localhost").port(8080))) { // network
    logger.info().name("java").version(1.7).tags(JIT)  // programming language
        .and(system("Fedora").tags(LINUX)) // global
        .message("Hello world");
}
```