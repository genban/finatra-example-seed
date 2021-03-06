
import com.twitter.finagle.http.Status.Ok
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.inject.server.FeatureTest

class FinatraFeatureTest extends FeatureTest {

  override val server = new EmbeddedHttpServer(FinatraServer)

  test("Server#ping") {
    server.httpGet(
      path = "/ping",
      andExpect = Ok,
      withBody = """{"pong":"pong"}"""
    )
  }
}
