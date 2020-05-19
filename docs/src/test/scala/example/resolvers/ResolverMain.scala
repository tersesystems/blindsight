package example.resolvers

import com.tersesystems.blindsight.{LoggerFactory, LoggerResolver}

object ResolverMain {

  def main(args: Array[String]): Unit = {
    // #class-resolver
    import com.tersesystems.blindsight.LoggerFactory

    val loggerFromName = LoggerFactory.getLogger("some.Logger")
    val loggerFromClass = LoggerFactory.getLogger(getClass)
    // #class-resolver

    // #macro-resolver
    val loggerFromEnclosing = LoggerFactory.getLogger
    // #macro-resolver
  }

  case class Request(id: String)

  // #request-resolver
  implicit val requestToResolver: LoggerResolver[Request] = new LoggerResolver[Request] {
    override def resolveLogger(instance: Request): org.slf4j.Logger = {
      org.slf4j.LoggerFactory.getLogger("requests." + instance.id)
    }
  }
  // #request-resolver

  // #logger-from-request
  def loggerFromRequest(request: Request): Unit = {
    val logger = com.tersesystems.blindsight.LoggerFactory.getLogger(request)
  }
  // #logger-from-request

}
