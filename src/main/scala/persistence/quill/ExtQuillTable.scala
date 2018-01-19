package persistence.quill

import java.util.Date

import org.joda.time._

import scala.concurrent.Future

/**
 * @author Corbin
 */
trait ExtQuillTable[T] {
  this: QuillComponents =>

  import QDB._

  type TableColumns

  implicit class ForUpdate(q: Query[T]) {
    def forUpdate = quote(infix"$q FOR UPDATE".as[Query[T]])
  }

  implicit class Replace(q: Insert[T]) {
    def replace = quote(infix"REPLACE $q".as[Insert[T]])
  }

  implicit class PageableQuery(q: Query[T]) {
    def page(limit: Int, offset: Int) = quote(infix"$q LIMIT $limit OFFSET $offset".as[Query[T]])
  }

  implicit class SQLLikeQuery[A](a: A) {
    def like(b: A) = quote(infix"$a like $b".as[Boolean])
  }

  implicit class DateTimeQuotes(left: DateTime) {
    def >(right: DateTime) = quote(infix"$left > $right".as[Boolean])
    def <(right: DateTime) = quote(infix"$left < $right".as[Boolean])
    def >=(right: DateTime) = quote(infix"$left >= $right".as[Boolean])
    def <=(right: DateTime) = quote(infix"$left <= $right".as[Boolean])
  }

  implicit class LocalDateTimeQuotes(left: LocalDateTime) {
    def >(right: LocalDateTime) = quote(infix"$left > $right".as[Boolean])
    def <(right: LocalDateTime) = quote(infix"$left < $right".as[Boolean])
    def >=(right: LocalDateTime) = quote(infix"$left >= $right".as[Boolean])
    def <=(right: LocalDateTime) = quote(infix"$left <= $right".as[Boolean])
  }

  implicit class DateQuotes(left: Date) {
    def >(right: Date) = quote(infix"$left > $right".as[Boolean])
    def <(right: Date) = quote(infix"$left < $right".as[Boolean])
    def >=(right: Date) = quote(infix"$left >= $right".as[Boolean])
    def <=(right: Date) = quote(infix"$left <= $right".as[Boolean])
  }

  implicit class QueryResult(result: Future[RunQueryResult[T]]) {

    def one: Future[T] = result.map {
      case Nil          => throw ExtQuillContext.NotFound
      case value :: Nil => value
      case v: List[T]   => throw new IllegalStateException(s"Expected a single result but got $v")
    }

    def headOption: Future[Option[T]] = result.map(_.headOption)
  }
}