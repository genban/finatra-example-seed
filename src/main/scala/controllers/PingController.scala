package controllers

import com.twitter.finagle.http.Request
import com.twitter.util.Future

class PingController extends FinatraController {

  get("/ping") { _: Request =>
    Future.value("pong")
  }
}
