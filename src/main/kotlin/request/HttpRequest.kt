package su.nlq.icq.bot.request

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import su.nlq.icq.bot.Bot
import java.net.URL
import java.util.*

internal class HttpRequest(
    val path: String,
    val httpMethod: HttpMethod = HttpMethod.Get,
    val parameters: HttpRequestBuilder.() -> Unit = {}
) {

  companion object {
    const val base = "https://botapi.icq.net"
  }

  suspend inline fun <reified Result> request(bot: Bot) = bot.client.runCatching {
    request<Result> {
      url(URL("$base/$path"))
      method = httpMethod
      parameter("r", UUID.randomUUID())
      parameter("aimsid", bot.token)
      apply(parameters)
    }
  }
}