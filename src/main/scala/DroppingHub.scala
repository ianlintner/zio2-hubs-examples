import ShameUtil.{hubConsumers, sized}
import zio._
import zio.logging.backend.SLF4J
import zio.stream.Take

/** Example of the bounded hub strategy dropping e.g. discard new messages.
 */
object DroppingHub extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = for {
    /** The behavior expected is the hub will fill up and start dropping new messages. */
    hub <- Hub.dropping[Take[Nothing, String]](2)
    _ <- {
      for {
        full <- hub.isFull
        _ <- ZIO.logInfo(s"Hub is full new messages may be dropped").when(full)
        _ <- sized(hub)
        msg <- Random.nextUUID.map(_.toString)
        _ <- hub.offer(Take.single[String](msg))
        _ <- ZIO.logInfo(s"< message published: $msg")
      } yield ()
    }.schedule(Schedule.spaced(1.seconds)).forkDaemon
    // We should see consumers not outputting some of the latest messages when full.
    _ <- hubConsumers(hub, 1, delay=2)
    _ <- ZIO.never
  } yield ()

}
