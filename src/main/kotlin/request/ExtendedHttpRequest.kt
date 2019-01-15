package su.nlq.icq.bot.request

import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import su.nlq.icq.bot.Bot
import java.util.*

internal class ExtendedHttpRequest<out ParametersType>(val apiMethod: String, val parameters: ParametersType) {

  suspend inline fun <reified ResultType> request(bot: Bot) =
      HttpRequest("rapi", HttpMethod.Post) {
        //url.parameters.clear()
        contentType(ContentType.Application.Json)
        body = Request(apiMethod, bot.token, parameters)
      }.request<ResultType>(bot)

  data class Request<out T>(
      val method: String,
      val aimsid: String,
      val params: T,
      val reqId: UUID = UUID.randomUUID()
  )
}