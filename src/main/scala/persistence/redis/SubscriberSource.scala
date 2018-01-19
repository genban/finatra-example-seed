package persistence.redis

import java.util.concurrent.CyclicBarrier

import akka.stream._
import akka.stream.stage._
import persistence.redis.protocol._

class SubscriberSource[T](
  val channel:         Channel,
  val initialEndpoint: RedisEndpoint,
  val receiver:        Message => T
) extends GraphStage[SourceShape[T]] with RedisClient {
  self =>

  val out: Outlet[T] = Outlet("SubscriberSource.Out")

  override def shape: SourceShape[T] = new SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with OutHandler {

      private val _Barrier = new CyclicBarrier(2)
      private var _Message: Option[T] = _
      private var _ThreadName: String = _

      //INTERNAL
      private val _MessageReceiver = new MessageReceiver {
        override def handleMessage(m: Message): Unit = {
          _Message = Some(receiver(m))
        }
      }

      override def preStart(): Unit = {
        super.preStart()
        _ThreadName = self.subscribe(channel, _MessageReceiver)
        _Barrier.await()
      }

      override def postStop(): Unit = {
        super.postStop()
        self.unsubscribe(_ThreadName)
      }

      override def onPull(): Unit = {
        _Barrier.await()
        push(out, _Message.get)
        _Message = None
        _Barrier.reset()
      }

      setHandler(out, this)
    }
}