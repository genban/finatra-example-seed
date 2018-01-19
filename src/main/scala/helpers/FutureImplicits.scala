package helpers

import com.twitter.util.{ Return, Throw, Future => TwitterFuture, Promise => TwitterPromise, Try => TwitterTry }

import scala.concurrent.{ ExecutionContext, Future => ScalaFuture, Promise => ScalaPromise }
import scala.util.control.NonFatal
import scala.util._

trait FutureImplicits {

  /** Convert from a Twitter Future to a Scala Future */
  implicit class RichTwitterFuture[T](val future: TwitterFuture[T]) {

    def asScala(implicit e: ExecutionContext): ScalaFuture[T] = {
      val promise: ScalaPromise[T] = ScalaPromise()
      future respond (promise complete _.asScala)
      promise.future
    }

    def andThen[U](pf: PartialFunction[TwitterTry[T], U]): TwitterFuture[T] = {
      future.transform {
        result =>
          val promise: TwitterPromise[T] = TwitterPromise()

          try pf.applyOrElse[TwitterTry[T], Any](result, Predef.identity[TwitterTry[T]])
          catch { case NonFatal(t) => TwitterFuture.exception(t) }

          promise update result
          promise
      }
    }
  }

  /** Convert from a Scala Future to a Twitter Future */
  implicit class RichScalaFuture[T](val future: ScalaFuture[T]) {

    def asTwitter(implicit ex: ExecutionContext): TwitterFuture[T] = {
      val promise: TwitterPromise[T] = TwitterPromise()
      future onComplete (promise update _.asTwitter)
      promise
    }
  }

  /** Convert from a Scala Try to a Twitter Try */
  implicit class RichScalaTry[T](val t: Try[T]) {

    def asTwitter: TwitterTry[T] = t match {
      case Success(value)     => Return(value)
      case Failure(exception) => Throw(exception)
    }
  }

  /** Convert from a Twitter Try to a Scala Try */
  implicit class RichTwitterTry[T](val t: TwitterTry[T]) {

    def asScala: Try[T] = t match {
      case Return(value)    => Success(value)
      case Throw(exception) => Failure(exception)
    }
  }

  implicit class TwitterFutureFlatten[T](val future: TwitterFuture[TwitterFuture[T]]) {

    def flatten(): TwitterFuture[T] = future.flatMap(identity)
  }

  case class FutureOption[+A](future: TwitterFuture[Option[A]]) {

    def flatMap[B](f: A => FutureOption[B])(implicit ec: ExecutionContext): FutureOption[B] =
      FutureOption(future flatMap {
        _.fold(TwitterFuture.value(Option.empty[B]))(o => f(o).future)
      })

    def map[B](f: A => B)(implicit ec: ExecutionContext): FutureOption[B] = FutureOption(future.map(_.map(f)))

    def filter(p: A ⇒ Boolean)(implicit ec: ExecutionContext): FutureOption[A] = withFilter(p)(ec)

    def withFilter(p: A => Boolean)(implicit ec: ExecutionContext): FutureOption[A] =
      FutureOption(future.map(_.fold(Option.empty[A])(a => Some(a).filter(p))))
  }

  case class FutureSeq[+A](future: TwitterFuture[Seq[A]]) {

    def flatMap[B](f: A => FutureSeq[B])(implicit ec: ExecutionContext): FutureSeq[B] =
      FutureSeq(future.flatMap(a => TwitterFuture.collect(a.map(f andThen (_.future))).map(_.flatten)))

    def map[B](f: A => B)(implicit ec: ExecutionContext): FutureSeq[B] =
      FutureSeq(future.map(_.map(f)))

    def filter(p: A ⇒ Boolean)(implicit ec: ExecutionContext): FutureSeq[A] = withFilter(p)(ec)

    def withFilter(p: A => Boolean)(implicit ec: ExecutionContext): FutureSeq[A] =
      FutureSeq(future.map(_.filter(p)))
  }

  implicit def TwitterFutureOption[T](future: TwitterFuture[Option[T]]): FutureOption[T] = FutureOption(future)

  implicit def TwitterFutureSeq[T](future: TwitterFuture[Seq[T]]): FutureSeq[T] = FutureSeq(future)
}

