package helpers

import java.io._
import java.util.Properties

import com.typesafe.config._
import com.typesafe.config.impl.ConfigImpl

import scala.collection.JavaConverters._
import scala.concurrent.duration.{ Duration, FiniteDuration, _ }
import scala.util.control.NonFatal

/**
 * This object provides a set of operations to create `Configuration` values.
 *
 * For example, to load a `Configuration` in a running application:
 * {{{
 * val config = Configuration.load()
 * val foo = config.getString("foo").getOrElse("boo")
 * }}}
 *
 * The underlying implementation is provided by https://github.com/typesafehub/config.
 */
object Configuration {

  /**
   * Load a new Configuration from the Environment.
   */
  def load(): Configuration = Configuration(ConfigFactory.load())

  /**
   * Load a new Configuration from the the given classpath resource or
   * classpath resource basename, sandwiches it between default reference
   * config and default overrides, and then resolves it.
   */
  def load(resourceBasename: String) = Configuration(ConfigFactory.load(resourceBasename))
  /**
   * Returns an empty Configuration object.
   */
  def empty = Configuration(ConfigFactory.empty())

  /**
   * Returns the reference configuration object.
   */
  def reference = Configuration(ConfigFactory.defaultReference())

  /**
   * Create a new Configuration from the data passed as a Map.
   */
  def from(data: Map[String, Any]): Configuration = {

    def toJava(data: Any): Any = data match {
      case map: Map[_, _]        => map.mapValues(toJava).asJava
      case iterable: Iterable[_] => iterable.map(toJava).asJava
      case v                     => v
    }

    Configuration(ConfigFactory.parseMap(toJava(data).asInstanceOf[java.util.Map[String, AnyRef]]))
  }

  /**
   * Create a new Configuration from the given key-value pairs.
   */
  def apply(data: (String, Any)*): Configuration = from(data.toMap)

  private[helpers] def configError(
    message: String, origin: Option[ConfigOrigin] = None, cause: Option[Throwable] = None): BaseException = {
    /*
      The stable values here help us from putting a reference to a ConfigOrigin inside the anonymous ExceptionSource.
      This is necessary to keep the Exception serializable, because ConfigOrigin is not serializable.
     */
    val originLine = origin.map(_.lineNumber: java.lang.Integer).orNull
    val originSourceName = origin.map(_.filename).orNull
    //val originUrlOpt = origin.flatMap(o => Option(o.url))
    new ConfigurationException(message, originSourceName, originLine)
  }

  private[Configuration] class ConfigurationException(message: String, originSourceName: String, originLine: Int) extends BaseException(message) {

    override def getMessage: String = s"Configuration error: $message - [origin:$originSourceName - $originLine]"
  }

  private[Configuration] def asScalaList[A](l: java.util.List[A]): Seq[A] = asScalaBufferConverter(l).asScala.toList
}

/**
 * A full configuration set.
 *
 * The underlying implementation is provided by https://github.com/typesafehub/config.
 *
 * @param underlying the underlying Config implementation
 */
case class Configuration(underlying: Config) {

  private[helpers] def reportDeprecation(path: String, deprecated: String): Unit = {
    val origin = underlying.getValue(deprecated).origin
  }

  /**
   * Merge two configurations. The second configuration overrides the first configuration.
   * This is the opposite direction of `Config`'s `withFallback` method.
   */
  def ++(other: Configuration): Configuration = {
    Configuration(other.underlying.withFallback(underlying))
  }

  /**
   * Reads a value from the underlying implementation.
   * If the value is not set this will return None, otherwise returns Some.
   *
   * Does not check neither for incorrect type nor null value, but catches and wraps the error.
   */
  private def readValue[T](path: String, v: => T): Option[T] = {
    try {
      if (underlying.hasPathOrNull(path)) Some(v) else None
    } catch {
      case NonFatal(e) => throw reportError(path, e.getMessage, Some(e))
    }
  }

  /**
   * Check if the given path exists.
   */
  def has(path: String): Boolean = underlying.hasPath(path)

  /**
   * Get the config at the given path.
   */
  def get[A](path: String)(implicit loader: ConfigLoader[A]): A = {
    loader.load(underlying, path)
  }

  /**
   * Get the config at the given path and validate against a set of valid values.
   */
  def getAndValidate[A](path: String, values: Set[A])(implicit loader: ConfigLoader[A]): A = {
    val value = get(path)
    if (!values(value)) {
      throw reportError(path, s"Incorrect value, one of (${values.mkString(", ")}) was expected.")
    }
    value
  }

  /**
   * Get a value that may either not exist or be null. Note that this is not generally considered idiomatic Config
   * usage. Instead you should define all config keys in a reference.conf file.
   */
  def getOptional[A](path: String)(implicit loader: ConfigLoader[A]): Option[A] = {
    readValue(path, get[A](path))
  }

  /**
   * Get a prototyped sequence of objects.
   *
   * Each object in the sequence will fallback to the object loaded from prototype.\$path.
   */
  def getPrototypedSeq(path: String, prototypePath: String = "prototype.$path"): Seq[Configuration] = {
    val prototype = underlying.getConfig(prototypePath.replace("$path", path))
    get[Seq[Config]](path).map { config =>
      Configuration(config.withFallback(prototype))
    }
  }

  /**
   * Get a prototyped map of objects.
   *
   * Each value in the map will fallback to the object loaded from prototype.\$path.
   */
  def getPrototypedMap(path: String, prototypePath: String = "prototype.$path"): Map[String, Configuration] = {
    val prototype = if (prototypePath.isEmpty) {
      underlying
    } else {
      underlying.getConfig(prototypePath.replace("$path", path))
    }
    get[Map[String, Config]](path).map {
      case (key, config) => key -> Configuration(config.withFallback(prototype))
    }
  }

  /**
   * Get a deprecated configuration item.
   *
   * If the deprecated configuration item is defined, it will be returned, and a warning will be logged.
   *
   * Otherwise, the configuration from path will be looked up.
   */
  def getDeprecated[A: ConfigLoader](path: String, deprecatedPaths: String*): A = {
    deprecatedPaths.collectFirst {
      case deprecated if underlying.hasPath(deprecated) =>
        reportDeprecation(path, deprecated)
        get[A](deprecated)
    }.getOrElse {
      get[A](path)
    }
  }

  /**
   * Get a deprecated configuration.
   *
   * If the deprecated configuration is defined, it will be returned, falling back to the new configuration, and a
   * warning will be logged.
   *
   * Otherwise, the configuration from path will be looked up and used as is.
   */
  def getDeprecatedWithFallback(path: String, deprecated: String, parent: String = ""): Configuration = {
    val config = get[Config](path)
    val merged = if (underlying.hasPath(deprecated)) {
      reportDeprecation(path, deprecated)
      get[Config](deprecated).withFallback(config)
    } else config
    Configuration(merged)
  }

  /**
   * Retrieves a configuration value as `Milliseconds`.
   *
   * For example:
   * {{{
   * val configuration = Configuration.load()
   * val timeout = configuration.getMillis("engine.timeout")
   * }}}
   *
   * The configuration must be provided as:
   *
   * {{{
   * engine.timeout = 1 second
   * }}}
   */
  def getMillis(path: String): Long = get[Duration](path).toMillis

  /**
   * Retrieves a configuration value as `Milliseconds`.
   *
   * For example:
   * {{{
   * val configuration = Configuration.load()
   * val timeout = configuration.getNanos("engine.timeout")
   * }}}
   *
   * The configuration must be provided as:
   *
   * {{{
   * engine.timeout = 1 second
   * }}}
   */
  def getNanos(path: String): Long = get[Duration](path).toNanos

  /**
   * Returns available keys.
   *
   * For example:
   * {{{
   * val configuration = Configuration.load()
   * val keys = configuration.keys
   * }}}
   *
   * @return the set of keys available in this configuration
   */
  def keys: Set[String] = underlying.entrySet.asScala.map(_.getKey).toSet

  /**
   * Returns sub-keys.
   *
   * For example:
   * {{{
   * val configuration = Configuration.load()
   * val subKeys = configuration.subKeys
   * }}}
   *
   * @return the set of direct sub-keys available in this configuration
   */
  def subKeys: Set[String] = underlying.root().keySet().asScala.toSet

  /**
   * Returns every path as a set of key to value pairs, by recursively iterating through the
   * config objects.
   */
  def entrySet: Set[(String, ConfigValue)] = underlying.entrySet().asScala.map(e => e.getKey -> e.getValue).toSet

  /**
   * Get String from configuration, which must be set
   *
   * @param key the configuration key
   *
   * @return a configuration exception
   */
  def getEssentialString(key: String): String = getOptional[String](key).getOrElse(
    throw reportError(key, s"Configuration Missing $key")
  )

  /**
   * Get Int from configuration, which must be set
   *
   * @param key the configuration key
   *
   * @return a configuration exception
   */
  def getEssentialInt(key: String): Int = getOptional[Int](key).getOrElse(
    throw reportError(key, s"Configuration Missing $key")
  )

  /**
   * Creates a configuration error for a specific configuration key.
   *
   * For example:
   * {{{
   * val configuration = Configuration.load()
   * throw configuration.reportError("engine.connectionUrl", "Cannot connect!")
   * }}}
   *
   * @param path the configuration key, related to this error
   * @param message the error message
   * @param e the related exception
   * @return a configuration exception
   */
  def reportError(path: String, message: String, e: Option[Throwable] = None): BaseException = {
    val origin = Option(if (underlying.hasPath(path)) underlying.getValue(path).origin else underlying.root.origin)
    Configuration.configError(message, origin, e)
  }

  /**
   * Creates a configuration error for this configuration.
   *
   * For example:
   * {{{
   * val configuration = Configuration.load()
   * throw configuration.globalError("Missing configuration key: [yop.url]")
   * }}}
   *
   * @param message the error message
   * @param e the related exception
   * @return a configuration exception
   */
  def globalError(message: String, e: Option[Throwable] = None): BaseException = {
    Configuration.configError(message, Option(underlying.root.origin), e)
  }
}

/**
 * Config Trait
 */
trait Configurable {

  protected def configuration: Configuration
}

/**
 * A config loader
 */
trait ConfigLoader[A] { self =>
  def load(config: Config, path: String = ""): A
  def map[B](f: A => B): ConfigLoader[B] = new ConfigLoader[B] {
    def load(config: Config, path: String): B = {
      f(self.load(config, path))
    }
  }
}

object ConfigLoader {

  def apply[A](f: Config => String => A): ConfigLoader[A] = new ConfigLoader[A] {
    def load(config: Config, path: String): A = f(config)(path)
  }

  import scala.collection.JavaConverters._

  implicit val stringLoader: ConfigLoader[String] = ConfigLoader(_.getString)
  implicit val seqStringLoader: ConfigLoader[Seq[String]] = ConfigLoader(_.getStringList).map(_.asScala)

  implicit val intLoader: ConfigLoader[Int] = ConfigLoader(_.getInt)
  implicit val seqIntLoader: ConfigLoader[Seq[Int]] = ConfigLoader(_.getIntList).map(_.asScala.map(_.toInt))

  implicit val booleanLoader: ConfigLoader[Boolean] = ConfigLoader(_.getBoolean)
  implicit val seqBooleanLoader: ConfigLoader[Seq[Boolean]] =
    ConfigLoader(_.getBooleanList).map(_.asScala.map(_.booleanValue))

  implicit val durationLoader: ConfigLoader[Duration] = ConfigLoader { config => path =>
    if (config.getIsNull(path)) Duration.Inf
    else if (config.getString(path) == "infinite") Duration.Inf
    else config.getDuration(path).toNanos.nanos
  }

  // Note: this does not support null values but it added for convenience
  implicit val seqDurationLoader: ConfigLoader[Seq[Duration]] =
    ConfigLoader(_.getDurationList).map(_.asScala.map(_.toNanos.nanos))

  implicit val finiteDurationLoader: ConfigLoader[FiniteDuration] =
    ConfigLoader(_.getDuration).map(_.toNanos.nanos)
  implicit val seqFiniteDurationLoader: ConfigLoader[Seq[FiniteDuration]] =
    ConfigLoader(_.getDurationList).map(_.asScala.map(_.toNanos.nanos))

  implicit val doubleLoader: ConfigLoader[Double] = ConfigLoader(_.getDouble)
  implicit val seqDoubleLoader: ConfigLoader[Seq[Double]] =
    ConfigLoader(_.getDoubleList).map(_.asScala.map(_.doubleValue))

  implicit val numberLoader: ConfigLoader[Number] = ConfigLoader(_.getNumber)
  implicit val seqNumberLoader: ConfigLoader[Seq[Number]] = ConfigLoader(_.getNumberList).map(_.asScala)

  implicit val longLoader: ConfigLoader[Long] = ConfigLoader(_.getLong)
  implicit val seqLongLoader: ConfigLoader[Seq[Long]] =
    ConfigLoader(_.getDoubleList).map(_.asScala.map(_.longValue))

  implicit val bytesLoader: ConfigLoader[ConfigMemorySize] = ConfigLoader(_.getMemorySize)
  implicit val seqBytesLoader: ConfigLoader[Seq[ConfigMemorySize]] = ConfigLoader(_.getMemorySizeList).map(_.asScala)

  implicit val configLoader: ConfigLoader[Config] = ConfigLoader(_.getConfig)
  implicit val configListLoader: ConfigLoader[ConfigList] = ConfigLoader(_.getList)
  implicit val configObjectLoader: ConfigLoader[ConfigObject] = ConfigLoader(_.getObject)
  implicit val seqConfigLoader: ConfigLoader[Seq[Config]] = ConfigLoader(_.getConfigList).map(_.asScala)

  implicit val configurationLoader: ConfigLoader[Configuration] = configLoader.map(Configuration(_))
  implicit val seqConfigurationLoader: ConfigLoader[Seq[Configuration]] = seqConfigLoader.map(_.map(Configuration(_)))

  /**
   * Loads a value, interpreting a null value as None and any other value as Some(value).
   */
  implicit def optionLoader[A](implicit valueLoader: ConfigLoader[A]): ConfigLoader[Option[A]] = new ConfigLoader[Option[A]] {
    def load(config: Config, path: String): Option[A] = {
      if (config.getIsNull(path)) None else {
        val value = valueLoader.load(config, path)
        Some(value)
      }
    }
  }

  implicit def mapLoader[A](implicit valueLoader: ConfigLoader[A]): ConfigLoader[Map[String, A]] = new ConfigLoader[Map[String, A]] {
    def load(config: Config, path: String): Map[String, A] = {
      val obj = config.getObject(path)
      val conf = obj.toConfig

      obj.keySet().asScala.map { key =>
        // quote and escape the key in case it contains dots or special characters
        val path = "\"" + util.StringEscapeUtils.escapeEcmaScript(key) + "\""
        key -> valueLoader.load(conf, path)
      }(scala.collection.breakOut)
    }
  }
}

