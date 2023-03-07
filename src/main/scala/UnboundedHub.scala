import ShameUtil.{hubConsumers, sized}
import zio._
import zio.logging.backend.SLF4J
import zio.stream.Take

/** Example of unbounded hub.
 */
object UnboundedHub extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = {
    for {
      hub <- Hub.unbounded[Take[Nothing, String]]
      _ <- {
        for {
          msg <- Random.nextUUID.map(_.toString)
          _ <- hub.offer(Take.single[String](msg))
          _ <- sized(hub)
          _ <- ZIO.logInfo(s"< message published: $msg")
        } yield ()
      }.schedule(Schedule.spaced(1.seconds)).forkDaemon
      _ <- hubConsumers(hub, 2, 2)
      _ <- ZIO.never
    } yield ()
  }.timeout(30.seconds)

}
