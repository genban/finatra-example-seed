package helpers

abstract class BaseException(val code: String) extends Exception {

  override def getMessage: String = code
}

