package http

import javax.inject._

import com.twitter.finagle.http._
import com.twitter.finatra.http.exceptions.HttpException
import com.twitter.finatra.http.exceptions.ExceptionMapper
import com.twitter.finatra.http.response._
import com.twitter.inject.Logging
import helpers._

@Singleton
class ExceptionHandler @Inject() (response: ResponseBuilder)
  extends ExceptionMapper[Exception]
  with Logging {

  override def toResponse(request: Request, throwable: Exception): Response = {
    logger.error(s"${request.path} - ${throwable.getMessage}", throwable)
    throwable match {
      case e: HttpException =>
        response
          .status(e.statusCode)
          .headers(e.headers: _*)
          .json(ErrorsResponse(e.errors))
      case e: BaseException =>
        response
          .status(Status.BadRequest)
          .json(e)
      case _: Exception =>
        response
          .status(Status.InternalServerError)
          .json("""{"status": 500, "message": "Internal Server Error"}""")
    }
  }
}