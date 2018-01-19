package persistence.mongodb

import com.mongodb.MongoCredential._
import helpers._
import org.mongodb.scala._
import org.mongodb.scala.connection.ClusterSettings

import scala.collection.JavaConverters._
import scala.concurrent._
import scala.concurrent.duration._

/**
 * @author Corbin
 */
abstract class MongodbContext(
  configuration: Configuration,
  prefix:        String        = "mongodb"
) extends DefaultExecutor with Logging {
  private final val settings = MongoClientSettings.builder()

  def authenticate = configuration.getOptional[Boolean](appendable("authenticate")) getOrElse false
  def database = configuration.getEssentialString(appendable("database"))
  def username = configuration.getOptional[String](appendable("username")) getOrElse ""
  def password = configuration.getOptional[String](appendable("password")) getOrElse ""
  def server = configuration.getOptional[String](appendable("server")) getOrElse "localhost"
  def port = configuration.getOptional[Int](appendable("port")) getOrElse 27017
  def servers = configuration.getOptional[Seq[String]](appendable("servers")) getOrElse Nil
  def mechanism = configuration.getOptional[String](appendable("mechanism"))

  def authentications: MongoCredential = mechanism match {
    case Some("MONGODB-CR")  => createMongoCRCredential(username, database, password.toCharArray)
    case Some("x.509")       => createMongoX509Credential(username)
    case Some("GSSAPI")      => createGSSAPICredential(username)
    case Some("PLAIN")       => createPlainCredential(username, database, "$external".toCharArray)
    case Some("SCRAM-SHA-1") => createScramSha1Credential(username, database, password.toCharArray)
    case _                   => createCredential(username, database, password.toCharArray)
  }

  settings.clusterSettings(ClusterSettings.builder().hosts((servers :+ server).map(ServerAddress(_)).asJava).build())
  if (authenticate) settings.credentialList(List(authentications).asJava)

  lazy val connection: MongoClient = MongoClient(settings.build())
  lazy val db: MongoDatabase = connection.getDatabase(database)

  {
    val collections = Await.result(db.listCollectionNames.fetch, 10.second)
    Logger.debug(s"Mongodb $database connected and collectionNames:[${collections.mkString(",")}] ")
  }

  def collection(name: String): MongoCollection[Document] = db.getCollection(name)
  def collection(dbName: String, collectionName: String): MongoCollection[Document] = connection.getDatabase(dbName).getCollection(collectionName)
  def close(): Unit = connection.close()

  protected def appendable(part: String): String = if (prefix.isEmpty) part else s"$prefix.$part"
}
