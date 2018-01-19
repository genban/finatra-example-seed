package persistence.redis

import java.net.URI
import java.util.concurrent.ConcurrentHashMap

import helpers.Configuration
import redis.clients.jedis._
import redis.clients.jedis.exceptions.JedisConnectionException
import redis.clients.util.JedisURIHelper

import scala.collection.JavaConverters._

case class RedisEndpoint(
  host:    String = Protocol.DEFAULT_HOST,
  port:    Int    = Protocol.DEFAULT_PORT,
  auth:    String = null,
  dbNum:   Int    = Protocol.DEFAULT_DATABASE,
  timeout: Int    = Protocol.DEFAULT_TIMEOUT
) extends Serializable {

  /**
   * Constructor from config. set params with redis.host, redis.port, redis.auth and redis.db
   *
   * @param conf Configuration
   */
  def this(conf: Configuration) {
    this(
      conf.getOptional[String]("redis.host") getOrElse Protocol.DEFAULT_HOST,
      conf.getOptional[Int]("redis.port") getOrElse Protocol.DEFAULT_PORT,
      conf.getOptional[String]("redis.auth").orNull,
      conf.getOptional[Int]("redis.db") getOrElse Protocol.DEFAULT_DATABASE,
      conf.getOptional[Int]("redis.timeout") getOrElse Protocol.DEFAULT_TIMEOUT
    )
  }

  /**
   * Constructor with Jedis URI
   *
   * @param uri connection URI in the form of redis://:$password@$host:$port/[dbnum]
   */
  def this(uri: URI) {
    this(uri.getHost, uri.getPort, JedisURIHelper.getPassword(uri), JedisURIHelper.getDBIndex(uri))
  }

  /**
   * Constructor with Jedis URI from String
   *
   * @param uri connection URI in the form of redis://:$password@$host:$port/[dbnum]
   */
  def this(uri: String) {
    this(URI.create(uri))
  }

  /**
   * Connect tries to open a connection to the redis endpoint,
   * optionally authenticating and selecting a db
   *
   * @return a new Jedis instance
   */
  def connect(): Jedis = ConnectionPool.connect(this)
}

object RedisEndpoint {

  def default = RedisEndpoint()
}

private[redis] object ConnectionPool {

  @transient private lazy val pools: ConcurrentHashMap[RedisEndpoint, JedisPool] =
    new ConcurrentHashMap[RedisEndpoint, JedisPool]()

  def connect(re: RedisEndpoint): Jedis = {
    val pool = pools.asScala.getOrElseUpdate(
      re,
      {
        val poolConfig: JedisPoolConfig = new JedisPoolConfig()
        poolConfig.setMaxTotal(250)
        poolConfig.setMaxIdle(32)
        poolConfig.setTestOnBorrow(false)
        poolConfig.setTestOnReturn(false)
        poolConfig.setTestWhileIdle(false)
        poolConfig.setMinEvictableIdleTimeMillis(60000)
        poolConfig.setTimeBetweenEvictionRunsMillis(30000)
        poolConfig.setNumTestsPerEvictionRun(-1)
        new JedisPool(poolConfig, re.host, re.port, re.timeout, re.auth, re.dbNum)
      }
    )
    var sleepTime: Int = 4
    var conn: Jedis = null
    while (conn == null) {
      try {
        conn = pool.getResource
      } catch {
        case e: JedisConnectionException if e.getCause.toString.
          contains("ERR max number of clients reached") =>
          if (sleepTime < 500) sleepTime *= 2
          Thread.sleep(sleepTime)
        case e: Exception => throw e
      }
    }
    conn
  }
}