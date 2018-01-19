package persistence.quill

import com.github.mauricio.async.db.general.ArrayRowData
import com.github.mauricio.async.db.mysql.{ MySQLConnection, MySQLQueryResult }
import com.github.mauricio.async.db.pool.PartitionedConnectionPool
import com.github.mauricio.async.db.{ QueryResult => DBQueryResult }
import com.typesafe.config.{ Config, ConfigFactory }
import helpers.Configuration
import io.getquill._
import io.getquill.context.async._

/**
 * @author Corbin
 */
class ExtQuillContext[N <: NamingStrategy](
  naming: N,
  pool:   PartitionedConnectionPool[MySQLConnection]
) extends AsyncContext(MySQLDialect, naming, pool)
  with ImplicitQuery
  with DateTimeEncoding
  //with JsValueEncoding
  with UUIDStringEncoding {

  def this(naming: N, config: MysqlAsyncContextConfig) = this(naming, config.pool)
  def this(naming: N, config: Configuration, prefix: String) =
    this(naming, MysqlAsyncContextConfig(config.getOptional[Config](prefix) getOrElse ConfigFactory.empty))

  override protected def extractActionResult[O](returningColumn: String, returningExtractor: Extractor[O])(result: DBQueryResult): O = {
    result match {
      case r: MySQLQueryResult =>
        returningExtractor(new ArrayRowData(0, Map.empty, Array(r.lastInsertId)))
      case _ =>
        throw new IllegalStateException("This is a bug. Cannot extract returning value.")
    }
  }
}

object ExtQuillContext {

  object NotFound extends Exception("Oops! Not found")

  object InvalidRecord extends Exception("Oops! Invalid record")
}