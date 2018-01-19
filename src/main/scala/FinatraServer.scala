import com.twitter.finatra.http.routing.HttpRouter
import controllers.PingController
import helpers.Configuration
import http.FinatraHttpServer

object FinatraServer extends FinatraHttpServer {

  override protected def configureHttp(router: HttpRouter): Unit = {
    super.configureHttp(router)
    router.add[PingController]
  }

  override protected val configuration: Configuration = Configuration.load()
}
