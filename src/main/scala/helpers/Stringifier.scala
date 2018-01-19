package helpers

import com.twitter.util._

/**
 *
 * @author XiaoBin
 */
trait Stringifier[T] {

  def <<: : String => Try[T]

  def >>: : T => String
}

object Stringifier {

  def of[A](implicit sf: Stringifier[A]): Stringifier[A] = sf

  def apply[T](f: String => T): Stringifier[T] = new Stringifier[T] {

    def <<: = str => Return(f(str))

    def >>: = _.toString
  }
}
