import ShameUtil.getStreamingConsumer
import zio._
import zio.logging.backend.SLF4J
import zio.stream.{Take, ZStream}

/** This is an example of how to use hubs with stream input/output.
 */
object StreamHub extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = for {
    /** Hub will be created with repeating zio to simulate an external input stream. */
    hub <- Hub.bounded[Take[Nothing, String]](2)
    _ <- ZStream
      .repeatZIOWithSchedule(
        Random.nextUUID.map(_.toString).tap(x => ZIO.logInfo(s"< streaming message published $x")),
        Schedule.spaced(1.seconds)
      )
      .runIntoHub(hub)
      .forkDaemon
    // Create two streaming consumers streaming to console out.
    _ <- getStreamingConsumer("streaming consumer 1", hub).forkDaemon
    _ <- getStreamingConsumer("streaming consumer 2", hub).forkDaemon
    _ <- ZIO.never
  } yield ()

}
