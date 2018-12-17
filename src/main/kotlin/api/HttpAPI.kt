package su.nlq.icq.bot.api

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import java.net.URL
import java.util.*

class HttpAPI(val token: String) {

  companion object {
    const val base = "https://botapi.icq.net"
  }

  suspend inline fun <reified T> request(
      url: String,
      httpMethod: HttpMethod = HttpMethod.Get,
      parameters: HttpRequestBuilder.() -> Unit = {}
  ) =
      HttpClient {
        install(JsonFeature) {
          serializer = JacksonSerializer()
        }
      }.runCatching {
        request<T> {
          url(URL("$base/$url"))
          method = httpMethod
          parameter("r", UUID.randomUUID())
          parameter("aimsid", token)
          apply(parameters)
        }.also { println(it) }
      }
}