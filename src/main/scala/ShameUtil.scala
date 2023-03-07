import zio.stream.{Take, ZSink, ZStream}
import zio.{Hub, _}

import java.io.IOException

/** Shared Code Between Examples
 */
object ShameUtil {

  def hubConsumers(hub: Hub[Take[Nothing, String]], consumerCount: Int, delay: Int = 1): URIO[Scope, Unit] =
    ZIO
      .foreachParDiscard(1 to consumerCount) { i =>
        getConsumer(s"consumer $i", hub, delay)
      }
      .orDie

  def getStreamingConsumer(id: String, hub: Hub[Take[Nothing, String]]): ZIO[Any, IOException, Long] = ZStream
    .fromHub(hub)
    .flattenTake
    .run(ZSink
      .fromOutputStream(java.lang.System.out)
      .contramapChunks[String](_.flatMap(x => s"> $id $x \n".getBytes))
    )

  def getConsumer(id: String, hub: Hub[Take[Nothing, String]], delay: Int = 1): ZIO[Scope, IOException, Long] = for {
    subscription <- hub.subscribe
    subscriber <- subscription.poll.flatMap {
      case Some(k) => k.tap(x => ZIO.logInfo(s"> $id ${x.mkString}"))
      case _ => ZIO.unit
    }.schedule(Schedule.spaced(delay.seconds))
  } yield subscriber

  def sized[A](hub: Hub[A]): ZIO[Any, Nothing, Unit] =
    hub.size.flatMap(i => ZIO.logInfo(s"Hub Size: $i"))

}
