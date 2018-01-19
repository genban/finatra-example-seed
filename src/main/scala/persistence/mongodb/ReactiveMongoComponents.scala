//package persistence.mongodb
//
//import helpers._
//import reactivemongo.api._
//import reactivemongo.api.gridfs.GridFS
//import reactivemongo.core.nodeset.Authenticate
//import reactivemongo.play.json.JSONSerializationPack
//
//import scala.concurrent._
//import duration._
//
///**
//* @author Corbin
//*/
//trait ReactiveMongodb {
//  def connection: MongoConnection
//  def db: Future[DefaultDB]
//  def asyncGridFS: Future[GridFS[JSONSerializationPack.type]]
//  def close(): Unit
//}
//
//class ReactiveMongodbContext(
//  val configuration: Configuration,
//  val prefix: String = "mongodb"
//)(
//  implicit
//  val driver: MongoDriver
//) extends ReactiveMongodb with Configurable {
//  import driver.system.dispatcher
//  import reactivemongo.play.json.collection._
//
//  private def defaultOpts = MongoConnectionOptions(authMode=ScramSha1Authentication)
//
//  def authenticate = configuration.getOptional[Boolean](appendable("authenticate")) getOrElse false
//  def database = configuration.getEssentialString(appendable("database"))
//  def host = configuration.getOptional[String](appendable("host")) getOrElse "localhost"
//  def port = configuration.getOptional[Int](appendable("port")) getOrElse 27017
//  def servers = configuration.getOptional[Seq[String]](appendable("servers")) getOrElse List(s"$host:$port")
//
//  def authentications = if (authenticate) {
//    val username = configuration.getEssentialString(appendable("username"))
//    val password = configuration.getEssentialString(appendable("password"))
//    Seq(Authenticate(database, username, password))
//  } else Nil
//
//  def parsedURI = MongoConnection.ParsedURI(
//    hosts = List((host, port)),
//    options = defaultOpts,
//    ignoredOptions = Nil,
//    db = Some(database),
//    authenticate = Some(Authenticate(
//      defaultOpts.authenticationDatabase.getOrElse(database),
//      configuration.getEssentialString(appendable("username")),
//      configuration.getEssentialString(appendable("password"))
//    ))
//  )
//
//  val connection: MongoConnection = driver.connection(servers, options = defaultOpts ,authentications = authentications)
//  val db: Future[DefaultDB] = connection.database(database)
//  val asyncGridFS: Future[GridFS[JSONSerializationPack.type]] = db.map(GridFS[JSONSerializationPack.type](_))
//  def close():Unit = Await.result(connection.askClose()(5.second),5.second)
//
//  private[this] def appendable(part:String) = if(prefix.isEmpty) part else s"$prefix.$part"
//}
//
//trait ReactiveMongoComponents {
//
//  def MO: ReactiveMongodbContext
//}
