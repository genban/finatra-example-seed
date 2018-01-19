package persistence.redis

import java.util.concurrent.ConcurrentHashMap

import helpers.Logging
import persistence.redis.protocol._
import redis.clients.jedis._

import scala.collection.JavaConverters._
import scala.concurrent.{ Future, Promise }
import scala.util.control.NonFatal

trait RedisClient extends Logging {

  def initialEndpoint: RedisEndpoint

  protected val client: Jedis = initialEndpoint.connect()

  private val subscribers = new ConcurrentHashMap[String, (Thread, Jedis, JedisPubSub)]()

  def publish(message: Message, channel: Channel): Future[Long] = withClient {
    _.publish(channel.name, message.context)
  }

  def subscribe[T](channel: Channel, receiver: MessageReceiver): String = {
    val pubsub = jedisPubSub(receiver)
    val thread = new Thread(() => {
      Logger.debug(s"start subscribe to ${channel.name}")
      client.subscribe(pubsub, channel.name)
    })
    subscribers.put(thread.getName, (thread, client, pubsub))

    thread.start()
    thread.getName
  }

  def unsubscribe(name: String): Unit = {
    val (thread, client, pubsub) = subscribers.asScala(name)
    pubsub.unsubscribe()
    client.quit()
    thread.interrupt()
  }

  protected def withClient[T](f: Jedis => T): Future[T] = {
    val promise = Promise[T]()
    try {
      f(client)
    } catch {
      case NonFatal(e) => promise.failure(e)
    }
    promise.future
  }

  //INTERNAL
  private def jedisPubSub[T](receiver: MessageReceiver): JedisPubSub = new JedisPubSub() {
    override def onSubscribe(channel: String, subscribedChannels: Int): Unit =
      Logger.debug("onSubscribe")

    override def onUnsubscribe(channel: String, subscribedChannels: Int): Unit =
      Logger.debug("onUnsubscribe")

    override def onPSubscribe(pattern: String, subscribedChannels: Int): Unit =
      Logger.debug("onPSubscribe")

    //override def onPUnsubscribe(pattern: String, subscribedChannels: Int): Unit = Logger.debug("onPUnsubscribe")

    //override def onPMessage(pattern: String, channel: String, message: String): Unit = Logger.debug("onPMessage")

    override def onMessage(channel: String, message: String): Unit = {
      Logger.debug(s"Message on Channel[$channel] and receive Messages[$message]")
      receiver.handleMessage(Message(message))
    }
  }
}

trait MessageReceiver {

  def handleMessage(m: Message): Unit
}