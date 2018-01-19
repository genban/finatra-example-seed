package persistence

import org.mongodb.scala._

import scala.concurrent._
import scala.concurrent.duration._

/**
 * @author Corbin
 */
package object mongodb {

  implicit class DocumentObservable[C](val observable: Observable[Document]) extends ImplicitObservable[Document] {

    override val converter: (Document) => String = (doc) => doc.toJson
  }

  implicit class GenericObservable[C](val observable: Observable[C]) extends ImplicitObservable[C] {

    override val converter: (C) => String = (doc) => doc.toString
  }

  trait ImplicitObservable[C] {

    def observable: Observable[C]
    def converter: (C) => String

    def one: C = Await.result(observable.head(), 5.second)
    def fetch: Future[Seq[C]] = observable.toFuture()
    def future: Future[Seq[C]] = observable.toFuture()
    def rows(implicit ec: ExecutionContext): Future[Long] = observable.toFuture().map(_.size)
    def results: Seq[C] = Await.result(observable.toFuture(), 5.second)
    def headOption()(implicit ec: ExecutionContext): Future[Option[C]] = observable.toFuture().map(_.headOption)
  }
}
