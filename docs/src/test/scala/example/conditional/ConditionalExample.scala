package example.conditional

import java.util.concurrent.atomic.AtomicBoolean

import com.tersesystems.blindsight.api.Argument
import com.tersesystems.blindsight.{Logger, LoggerFactory}
import net.logstash.logback.argument.StructuredArgument

import scala.concurrent.duration.Duration

object SimpleConditionalExample {
  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger

    // #simple-conditional
    def booleanCondition: Boolean = sys.props("java.version").startsWith("1.8")
    val conditionalLogger = logger.onCondition(booleanCondition)
    conditionalLogger.info("Only logs when condition is true")
    // #simple-conditional

    // #composed-conditional
    def anotherCondition: Boolean = sys.props.get("user.home").isDefined
    val bothConditionsLogger = conditionalLogger.onCondition(anotherCondition)
    bothConditionsLogger.info("Only logs when both conditions are true")
    // #composed-conditional

    // #when-conditional
    logger.info.when(booleanCondition) { info =>
      info("log")
    }
    //#when-conditional

    // #function-when-conditional
    val infoFunction = logger.info.when(1 == 1)(_)
    infoFunction(info => info("when true"))
    // #function-when-conditional

    // #low-pressure-conditional
    logger.info.when(isLowPressure) { info =>
      val expensiveResult = createLotsOfObjects()
      info("many statements, much memory pressure {}", expensiveResult)
    }
    // #low-pressure-conditional

    // #featureflag-conditional
    implicit val request: Request = Request(user = "thisuser")
    def userFeatureFlag(implicit request: Request): Boolean = isUserDebugFlagOn(request)
    val userLogger = logger.onCondition(userFeatureFlag)
    userLogger.debug("user debug request", request.user)
    // #featureflag-conditional

    // #deadline-conditional
    import scala.concurrent.duration._
    var debugDeadline: Deadline = Deadline.now

    def debugActiveFor(newDuration: FiniteDuration) = {
      debugDeadline = newDuration.fromNow
    }

    debugActiveFor(30.seconds)
    logger.debug.when(debugDeadline.hasTimeLeft()) { debug =>
      debug("Logging debug information while there is time left")
    }
    // #deadline-conditional

  }

  def isLowPressure: Boolean = true

  def createLotsOfObjects(): Argument = {
    Argument("some result")
  }

  def isUserDebugFlagOn(request: Request): Boolean = false

  case class Request(user: String)
}
