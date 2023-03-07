import ShameUtil.{hubConsumers, sized}
import zio._
import zio.logging.backend.SLF4J
import zio.stream.Take

/** Example of the bounded hub strategy new messages are blocked.
 */
object BoundedHub extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = for {
    /** The behavior expected is the hub will rapidly fill up. Then create backpressure by blocking. */
    hub <- Hub.bounded[Take[Nothing, String]](5)
    _ <- {
      for {
        full <- hub.isFull
        _ <- ZIO.logInfo("Hub is full publishing maybe blocked.").when(full)
        _ <- sized(hub)
        msg <- Random.nextUUID.map(_.toString)
        // The offer timing log will demonstrate back pressure with significant increase as it running every 100 ms.
        _ <- hub.offer(Take.single[String](msg))
          .timed.tap(x => ZIO.logInfo(s"offer timing ${x._1.getNano.toString} ns"))
        _ <- ZIO.logInfo(s"< message published: $msg")
      } yield ()
    }.schedule(Schedule.spaced(100.millis)).forkDaemon
    _ <- hubConsumers(hub, 2, delay=3).forkDaemon
    _ <- ZIO.never
  } yield ()

}
