package http

import com.google.inject.Module
import modules._
import com.twitter.finatra.http.extend.HttpServer
import com.twitter.finatra.http.routing.HttpRouter

trait FinatraHttpServer extends HttpServer with helpers.Configurable {

  /** Add Custom Common Modules */
  //addFrameworkModules(ConfigModule)

  override protected def defaultHttpServerName: String = configuration.getEssentialString("app.name")

  override protected def defaultHttpsServerName: String = defaultHttpServerName

  override protected def defaultFinatraHttpPort: String = configuration.getEssentialString("app.port")

  override protected def disableAdminHttpServer: Boolean = true

  override def jacksonModule: Module = DefaultJacksonModule

  override protected def configureHttp(router: HttpRouter): Unit = {
    logger info "invoke configure http"
    router
      .add[InternalController]
      .exceptionMapper[ExceptionHandler]
  }
}
