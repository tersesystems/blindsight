
## Context Resolution

Ties in to operation / "unit of work" activities.

### Through Scoping

You're in an object that has a context already, and can reference it directly.

Either you're an inner class, or it's provided as a parameter, or there's only one.


### Through Thread Local Storage

Works great if you're always using the same thread.

### Instrumentation

Works great if you have byte code instrumentation for the code base.

logback-bytebuddy.

### Through Lookup

Tie the logger / context to the unit of work / operation id.

Then use a resolver with that operation id to find the best context.

Something in scope?  Use it.  Something in thread-local?  Use that.  If not, pull it directly from lookup.

Downside -- anything can access the context and log with it, given the id.
Also have to cache or explicitly remove context.

Also requires that you have a unique id you can look up for everything, and it's fast enough to do so.

FP heavy code can log perfectly well in this scenario, because all you need is the tag and then you can look up from wherever.  It's the resolver's job to find something that can match it.

Ability to deal with FP heavy code (factories for functions?)  Covering exceptional cases and failures.

- 
https://github.com/twitter/util/blob/develop/util-slf4j-api/README.md