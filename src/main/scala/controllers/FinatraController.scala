package controllers

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.inject.Logging
import com.twitter.util._
import helpers.FutureImplicits

abstract class FinatraController
  extends Controller with FutureImplicits with Logging {

  import com.twitter.finagle.http.Status

  type CallBack = Request => Future[ResponseBuilder]

  def get[Req: Manifest, Rep: Manifest](
    route: String
  )(callback: Req => Future[Rep]): Unit = {
    super.get(route)(callback)
  }

  def Post[Req: Manifest, Rep: Manifest](
    route: String
  )(callback: Req => Future[Rep]): Unit = {
    super.post(route)(callback)
  }

  def options[Req: Manifest, Rep: Manifest](
    route: String
  )(callback: Req => Future[Rep]): Unit = {
    super.options(route)(callback)
  }

  /** 响应结构 */
  sealed abstract class ResponseStructure(
    status:  Int,
    message: Option[String]
  ) extends Serializable

  case class ApiResponse(
    status:  Int,
    message: Option[String] = None,
    data:    Option[Any]    = None
  ) extends ResponseStructure(status, message)

  def success[A, Object](
    status: Status,
    future: Future[Option[A]]
  ): Future[ApiResponse] = retFutureHandler(future) {
    case Return(Some(value)) => Future.value(ApiResponse(status.code, data = Some(value)))
    case Return(None)        => Future.value(ApiResponse(status.code, message = Some(status.reason)))
    case Return(value)       => Future.value(ApiResponse(status.code, data = value))
    case Throw(exception)    => Future.value(ApiResponse(status.code, message = Some(exception.getMessage)))
  }

  def success[A, Object](future: Future[A]): Future[ApiResponse] =
    success[A, Object](Status.Ok, future.map(Option(_)))

  def successList[A, Object](
    status: Status,
    future: Future[Traversable[A]]
  ): Future[ApiResponse] = retFutureHandler(future) {
    case Return(Nil)   => Future.value(ApiResponse(status.code, data = Some(List.empty[A])))
    case Return(value) => Future.value(ApiResponse(status.code, data = Some(value)))
    case Throw(ex)     => Future.value(ApiResponse(status.code, message = Some(ex.getMessage)))
  }

  def failure[A, Object](
    status:  Status,
    message: String,
    future:  Future[Option[A]]
  ): Future[ApiResponse] = retFutureHandler(future) {
    case Return(_) => Future.value(ApiResponse(status.code, Some(message)))
    case Throw(ex) => Future.value(ApiResponse(status.code, Some(ex.getMessage)))
  }

  private def retFutureHandler[A, B](future: Future[A])(f: Try[A] => Future[B]) = future
    .andThen { case Throw(e) => logger error (e.getMessage, e.getCause) }
    .transform(f)

  def acceptCheck(request: Request)(callback: Request => Future[ResponseBuilder]): Future[ResponseBuilder] =
    request.headerMap.get("Accept").getOrElse("").split(",").map(_.trim) match {
      case array if array.contains("*/*") || array.contains("application/json") => callback(request)
      case _ => throw new UnsupportedOperationException
    }
}
