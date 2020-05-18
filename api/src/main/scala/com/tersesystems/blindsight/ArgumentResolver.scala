package com.tersesystems.blindsight

import java.util.ServiceLoader

import com.tersesystems.blindsight.AST.BObject

trait ArgumentResolver {
  def resolve(bobject: BObject): Argument
}

object ArgumentResolver {

  def apply(bobject: BObject): Argument = {
    argumentResolver.resolve(bobject)
  }

  private val argumentResolverLoader = ServiceLoader.load(classOf[ArgumentResolver])

  private lazy val argumentResolver: ArgumentResolver = {
    import javax.management.ServiceNotFoundException

    import scala.collection.JavaConverters._

    argumentResolverLoader.iterator().asScala.find(_ != null).getOrElse {
      throw new ServiceNotFoundException("No argument resolver found!")
    }
  }

  class Passthrough extends ArgumentResolver {
    override def resolve(bobject: BObject): Argument = new Argument(bobject)
  }

}
