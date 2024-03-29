package example.conditional

import com.tersesystems.blindsight._
import io.timeandspace.cronscheduler.CronTask
import org.slf4j.MarkerFactory
import org.slf4j.event.Level

import java.util.concurrent.atomic.AtomicBoolean

object SimpleConditionalExample {
  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger

    // #simple-conditional
    def booleanCondition: Boolean = sys.props("java.version").startsWith("1.8")
    val conditionalLogger         = logger.withCondition(booleanCondition)
    conditionalLogger.info("Only logs when condition is true")
    // #simple-conditional

    // #marker-conditional
    val fooMarker       = MarkerFactory.getMarker("FOO")
    val markerCondition = Condition((stateMarkers: Markers) => stateMarkers.contains(fooMarker))
    val conditionalOnFooMarker = logger.withCondition(markerCondition)
    // #marker-conditional

    // #level-marker-conditional
    val levelMarkerCondition = Condition((level, markers) =>
      (level.compareTo(Level.DEBUG) >= 0) && markers.contains(fooMarker)
    )
    val conditionalOnLevelAndMarker = logger.withCondition(levelMarkerCondition)
    // #level-marker-conditional

    // #composed-conditional
    def anotherCondition: Boolean = sys.props.get("user.home").isDefined
    val bothConditionsLogger      = conditionalLogger.withCondition(anotherCondition)
    bothConditionsLogger.info("Only logs when both conditions are true")
    // #composed-conditional

    // #when-conditional
    logger.info.when(booleanCondition) { info => info("log") }
    // #when-conditional

    // #function-when-conditional
    val infoFunction = logger.info.when(1 == 1)(_)
    infoFunction(info => info("when true"))
    // #function-when-conditional

    // #circuitbreaker-conditional
    val operationLogger = logger.withCondition(isCircuitBreakerClosed)
    operationLogger.error("Only errors when circuit breaker is closed")
    // #circuitbreaker-conditional

    // #low-pressure-conditional
    logger.info.when(isLowPressure) { info =>
      val expensiveResult = createLotsOfObjects()
      info("many statements, much memory pressure {}", expensiveResult)
    }
    // #low-pressure-conditional

    // #featureflag-conditional
    implicit val request: Request = Request(user = "thisuser")

    // Define feature flag method in context of a request...
    def userFeatureFlag(implicit request: Request): Boolean = isUserDebugFlagOn(request)

    // the logger will debug if level=DEBUG and feature flag is enabled for user.
    val userLogger = logger.withCondition(userFeatureFlag)
    userLogger.debug("user debug request", request.user)
    // #featureflag-conditional

    // #deadline-conditional
    import scala.concurrent.duration._
    var debugDeadline: Deadline = Deadline.now

    def debugActiveFor(newDuration: FiniteDuration) = {
      debugDeadline = newDuration.fromNow
    }

    // Enable from REST API or admin command
    debugActiveFor(30.seconds)

    // Run until deadline is met.
    logger.debug.when(debugDeadline.hasTimeLeft()) { debug =>
      debug("Logging debug information while there is time left")
    }
    // #deadline-conditional

    // #periodic-conditional
    import java.util.concurrent.atomic.AtomicBoolean

    val latch    = new AtomicBoolean()
    val periodic = new Periodic()
    periodic.schedule(
      java.time.Duration.ofMinutes(1),
      new CronTask {
        override def run(scheduledRunTimeMillis: Long): Unit = latch.set(true)
      }
    )

    logger.debug.when(latch.getAndSet(false)) { debug => debug("Only run once per minute max") }
    periodic.shutdown() // on app shutdown
    // #periodic-conditional

  }

  val latch = new AtomicBoolean()

  def isLowPressure: Boolean = true

  def createLotsOfObjects(): Argument = {
    Argument("some result")
  }

  def isCircuitBreakerClosed: Boolean = false

  def isUserDebugFlagOn(request: Request): Boolean = false

  case class Request(user: String)
}
