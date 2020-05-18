/*
 * Copyright 2020 Terse Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tersesystems.blindsight.logstash

import com.tersesystems.blindsight
import com.tersesystems.blindsight.AST._
import com.tersesystems.blindsight._
import net.logstash.logback.argument.{StructuredArgument, StructuredArguments}
import net.logstash.logback.marker.{Markers => LogstashMarkers}

trait ToArgumentsImplicits {
  implicit val structuredArgToArguments: ToArgument[StructuredArgument] = ToArgument { instance =>
    new Argument(instance)
  }

  implicit val bvalueToArgument: ToArgument[BObject] = ToArgument { bobj =>
    import BObjectConverters._
    Argument(StructuredArguments.e(asJava(bobj)))
  }
}

object ToArgumentsImplicits extends ToArgumentsImplicits

trait ToMarkersImplicits {
  implicit val bvalueToMarker: ToMarkers[BObject] = blindsight.ToMarkers { bobj: BObject =>
    import BObjectConverters._
    Markers(LogstashMarkers.appendEntries(asJava(bobj)))
  }
}

object ToMarkersImplicits extends ToMarkersImplicits

trait Implicits extends ToMarkersImplicits with ToArgumentsImplicits with SourceCodeImplicits

object Implicits extends Implicits
