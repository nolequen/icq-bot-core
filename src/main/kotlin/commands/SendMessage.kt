package su.nlq.icq.bot.commands

import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import su.nlq.icq.bot.api.HttpAPI

class SendMessage(
    private val to: String, //todo: companion or chat
    private val message: String,
    private val mentions: Collection<String> = emptyList()
) : Command<Unit> {

  override suspend fun execute(api: HttpAPI) = api.request<Unit>("im/sendIM", HttpMethod.Post) {
    parameter("t", to)
    parameter("message", message)
    parameter("mentions", mentions.joinToString())
  }
}