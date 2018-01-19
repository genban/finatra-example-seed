package http

import com.google.inject.Singleton
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

@Singleton
class InternalController extends Controller {

  get("/internal/health") { _: Request =>
    response.ok
  }
}
