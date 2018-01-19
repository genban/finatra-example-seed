package persistence.mongodb

import org.mongodb.scala.MongoClient

/**
 * @author Corbin
 */
trait MongodbTpl {

  protected def reader: MongoClient

  protected def writer: MongoClient
}
