package com.tersesystems.blindsight;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.*;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

// https://gist.github.com/forax/7bf08669f58804991fd45656a671c381
class InliningCacheInvoker {
  private InliningCacheInvoker() {
    throw new AssertionError();
  }
  
  static MethodHandle createInvoker(int depth, MethodType type) {
    if (depth <= 0) {
      throw new IllegalArgumentException("depth should be positive");
    }
    return new InliningCacheCallSite(type.insertParameterTypes(0, MethodHandle.class), depth).dynamicInvoker();
  }
  
  private static class InliningCacheCallSite extends MutableCallSite {
    private static final MethodHandle FALLBACK, TYPECHECK;
    static {
      Lookup lookup = lookup();
      try {
        FALLBACK = lookup.findVirtual(InliningCacheCallSite.class, "fallback",
            methodType(MethodHandle.class, MethodHandle.class));
        TYPECHECK = lookup.findStatic(InliningCacheCallSite.class, "typecheck",
            methodType(boolean.class, MethodHandle.class, MethodHandle.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
    
    private final InliningCacheCallSite head;
    private final int depth;
    
    InliningCacheCallSite(MethodType type, int depth) {
      super(type);
      head = this;
      this.depth = depth;
      setTarget(foldArguments(exactInvoker(type), FALLBACK.bindTo(this)));
    }
    InliningCacheCallSite(MethodType type, InliningCacheCallSite head, int depth) {
      super(type);
      this.head = head;
      this.depth = depth;
      setTarget(foldArguments(exactInvoker(type), FALLBACK.bindTo(this)));
    }
    
    @SuppressWarnings("unused")
    private MethodHandle fallback(MethodHandle mh) {
      MethodHandle target = MethodHandles.dropArguments(mh, 0, MethodHandle.class);
      if (depth == 0) {  // inlining too deep
        head.setTarget(exactInvoker(type().dropParameterTypes(0, 1)));
        return target;
      }
      setTarget(MethodHandles.guardWithTest(TYPECHECK.bindTo(mh),
          target,
          new InliningCacheCallSite(type(), head, depth - 1).dynamicInvoker()));
      return target;
    }
    
    @SuppressWarnings("unused")
    private static boolean typecheck(MethodHandle mh1, MethodHandle mh2) {
      return mh1 == mh2;
    }
  }
}
