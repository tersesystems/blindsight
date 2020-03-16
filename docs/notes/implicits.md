# Implicits

## Dummy Implicits

When you have type erasure from two lists with different elements:

This is a simple class that has only one instance, which is conveniently implicit. Moreover, both the class and the instance `dummyImplicit` are always in the (implicit) scope! That’s great, because it’s all that we need. We can finally have a nice solution to overloaded methods with changing type parameter arguments:

def foo(ls: List[String]): Unit = ???
def foo(ls: List[Int])(implicit d: DummyImplicit): Unit = ???

* https://medium.com/@antoine.doeraene/how-i-discovered-the-dummyimplicit-in-scala-5b471dca42dc

## Overview of Implicit Scope Resolution

* http://eed3si9n.com/revisiting-implicits-without-import-tax
* https://kubuszok.com/compiled/implicits-type-classes-and-extension-methods/

## Tricks to Implicit Scope

Regarding the implicit scope, I don’t know if you remember a trick you helped me come up with a few years ago: if your type class has a phantom type - a tag for a generic Decoder, say - that phantom type’s companion object is part of the implicit scope. - https://twitter.com/NicolasRinaudo/status/1179284750143496193

* https://meta.plasm.us/posts/2019/09/30/implicit-scope-and-cats/