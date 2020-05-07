package com.tersesystems.blindsight.logstash

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LogstashMarkersSpec extends AnyWordSpec with Matchers {

  //  implicit def mapToMarkers: ToMarkers[Map[String, AsMarkers]] = ToMarkers { markerMap =>
  //    val iterable: Iterable[Markers] = markerMap.map {
  //      case (k, v) =>
  //        Markers(append(k, v.markers))
  //    }
  //    Markers(iterable)
  //  }

  //
  //final class AsMarkers(val markers: Markers)
  //
  //object AsMarkers {
  //  implicit def asMarkers[M: ToMarkers](m: M): AsMarkers = {
  //    val markers = implicitly[ToMarkers[M]].toMarkers(m)
  //    new AsMarkers(markers)
  //  }
  //}

}
