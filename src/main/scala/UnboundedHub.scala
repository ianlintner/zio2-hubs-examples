import ShameUtil.{hubConsumers, sized}
import zio._
import zio.logging.backend.SLF4J
import zio.stream.Take

/** Example of unbounded hub.
 */
object UnboundedHub extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = {
    /** The behavior expected is the hub will run unbounded with no dropping or backpressure. */
    for {
      hub <- Hub.unbounded[Take[Nothing, String]]
      _ <- {
        for {
          msg <- Random.nextUUID.map(_.toString)
          _ <- hub.offer(Take.single[String](msg))
          // Watch as hub size grows as consumers fall behind.
          _ <- sized(hub)
          _ <- ZIO.logInfo(s"< message published: $msg")
        } yield ()
      }.schedule(Schedule.spaced(1.seconds)).forkDaemon
      _ <- hubConsumers(hub, 2, delay=2).forkDaemon
      _ <- ZIO.never
    } yield ()
  }.timeout(30.seconds)

}
