package example.dsl

import java.time.{Duration, Instant}

import com.tersesystems.blindsight.DSL._
import com.tersesystems.blindsight._

object TimeExample {
  private val logger = LoggerFactory.getLogger

  def main(args: Array[String]): Unit = {
    // #created_tse_ms
    logger.info("time = {}", bobj("created_tse_ms" -> Instant.now.toEpochMilli))
    // #created_tse_ms

    // #created_ts
    logger.info("time = {}", bobj("created_ts" -> Instant.now.toString))
    // #created_ts

    // #backoff_dur_ms
    val backoffDuration = 150
    // // assume we call after(backoffDuration, task) etc...
    logger.info("backoff duration = {}", bobj("backoff_dur_ms" -> backoffDuration))
    // #backoff_dur_ms

    // #ride_dur_iso
    val rideDuration = Duration.parse("PT2H15M")
    logger.info("backoff duration = {}", bobj("ride_dur_iso" -> rideDuration.toString))
    // #ride_dur_iso

  }

}
// #coulomb-example
