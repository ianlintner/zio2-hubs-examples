import ShameUtil.{hubConsumers, sized}
import zio._
import zio.logging.backend.SLF4J
import zio.stream.Take

/** Example of the bounded hub strategy sliding e.g. discard oldest message.
 */
object SlidingHub extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = for {
    /** The behavior expected is the hub will fill up and start dropping the oldest messages. */
    hub <- Hub.sliding[Take[Nothing, String]](1)
    _ <- {
      for {
        full <- hub.isFull
        _ <- ZIO.logInfo(s"Hub full -- oldest message may be dropped").when(full)
        _ <- sized(hub)
        msg <- Random.nextUUID.map(_.toString)
        _ <- hub.offer(Take.single[String](msg))
        _ <- ZIO.logInfo(s"< message published: $msg")
      } yield ()
    }.schedule(Schedule.spaced(1.seconds)).forkDaemon
    // We should see consumers not outputting some of the oldest messages when full.
    _ <- hubConsumers(hub, 1, delay=2).forkDaemon
    _ <- ZIO.never
  } yield ()
}
