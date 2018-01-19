package persistence.mongodb.scaladsl

import akka.NotUsed
import akka.stream.scaladsl.Source
import org.mongodb.scala.{ Document, Observable }
import persistence.mongodb.ObservableToPublisher

/**
 * @author Corbin
 */
object MongodbSource {

  def apply(query: Observable[Document]): Source[Document, NotUsed] = {
    Source.fromPublisher(ObservableToPublisher(query))
  }
}