package persistence.redis

import akka.stream._
import akka.stream.stage._
import helpers.Logging
import persistence.redis.protocol._

class PublisherSink(
  val channel:         Channel,
  val initialEndpoint: RedisEndpoint
) extends GraphStage[SinkShape[Message]] with RedisClient with Logging {

  val in: Inlet[Message] = Inlet("PublisherSink.In")

  override def shape: SinkShape[Message] = SinkShape(in)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with InHandler {

      override def preStart(): Unit = pull(in)

      override def postStop(): Unit = client.quit()

      override def onPush(): Unit = {
        val message = grab(in)
        Logger.debug(s"will publish message: $message")
        publish(message, channel)
        pull(in)
      }

      setHandler(in, this)
    }
}
