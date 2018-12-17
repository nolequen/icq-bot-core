package su.nlq.icq.bot.commands

import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import su.nlq.icq.bot.api.HttpAPI

//todo: UIN/chat-id
class SendMessage(
    private val to: String,
    private val message: String,
    private val mentions: Collection<String> = emptyList()
) : Command<Unit> {

  override suspend fun execute(api: HttpAPI) = api.request<String>("im/sendIM", HttpMethod.Post) {
    parameter("t", to)
    parameter("message", message)
    parameter("mentions", mentions.joinToString())
  }.map { }
}