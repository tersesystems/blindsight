package example.conditional

import com.tersesystems.blindsight.LoggerFactory
import com.tersesystems.blindsight.api.Argument

object SimpleConditionalExample {
  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger

    // #simple-conditional
    def booleanCondition: Boolean = sys.props("java.version").startsWith("1.8")
    val conditionalLogger         = logger.onCondition(booleanCondition)
    conditionalLogger.info("Only logs when condition is true")
    // #simple-conditional

    // #composed-conditional
    def anotherCondition: Boolean = sys.props.get("user.home").isDefined
    val bothConditionsLogger      = conditionalLogger.onCondition(anotherCondition)
    bothConditionsLogger.info("Only logs when both conditions are true")
    // #composed-conditional

    // #when-conditional
    logger.info.when(booleanCondition) { info => info("log") }
    //#when-conditional

    // #function-when-conditional
    val infoFunction = logger.info.when(1 == 1)(_)
    infoFunction(info => info("when true"))
    // #function-when-conditional

    // #circuitbreaker-conditional
    val operationLogger = logger.onCondition(isCircuitBreakerClosed)
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
    val userLogger = logger.onCondition(userFeatureFlag)
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
    import java.time.Duration
    import java.util.concurrent.TimeUnit
    import io.timeandspace.cronscheduler.CronScheduler
    import java.util.concurrent.atomic.AtomicBoolean
    import io.timeandspace.cronscheduler.CronTask

    val latch      = new AtomicBoolean()
    val syncPeriod = Duration.ofMinutes(1)
    val cron       = CronScheduler.create(syncPeriod)
    cron.scheduleAtFixedRateSkippingToLatest(0, 1, TimeUnit.MINUTES, new CronTask {
      override def run(scheduledRunTimeMillis: Long): Unit = latch.set(true)
    })
    logger.debug.when(latch.getAndSet(false)) { debug =>
      debug("Only run once per minute max")
    }
    // #periodic-conditional

  }

  def isLowPressure: Boolean = true

  def createLotsOfObjects(): Argument = {
    Argument("some result")
  }

  def isCircuitBreakerClosed: Boolean = false

  def isUserDebugFlagOn(request: Request): Boolean = false

  case class Request(user: String)
}
