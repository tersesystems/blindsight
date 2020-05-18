package com.tersesystems.blindsight

import java.util.ServiceLoader

import com.tersesystems.blindsight.AST.BObject

trait MarkersResolver {
  def resolve(bobject: BObject): Markers
}

object MarkersResolver {

  def apply(bobject: BObject): Markers = resolver.resolve(bobject)

  private val resolverLoader = ServiceLoader.load(classOf[MarkersResolver])

  private lazy val resolver: MarkersResolver = {
    import javax.management.ServiceNotFoundException
    import scala.collection.JavaConverters._

    resolverLoader.iterator().asScala.find(_ != null).getOrElse {
      throw new ServiceNotFoundException("No markers resolver found!")
    }
  }

  class Empty extends MarkersResolver {
    override def resolve(bobject: BObject): Markers = Markers.empty
  }
}
