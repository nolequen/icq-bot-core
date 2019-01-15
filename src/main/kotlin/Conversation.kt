package su.nlq.icq.bot

import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import su.nlq.icq.bot.request.HttpRequest

class Conversation internal constructor(
    private val bot: Bot,
    private val penpal: PenPal
) {

  suspend fun message(message: String, mentions: Collection<PenPal> = emptyList()) = HttpRequest("im/sendIM", HttpMethod.Post) {
    parameter("t", penpal.id)
    parameter("message", message)
    parameter("mentions", mentions.joinToString(",") { it.id })
  }.request<Unit>(bot)

  suspend fun typing(status: Status = Status.Typing) = HttpRequest("im/setTyping", HttpMethod.Post) {
    parameter("t", penpal.id)
    parameter("typingStatus", status)
  }.request<Unit>(bot)

  enum class Status {
    Looking,
    Typing,
    Typed,
    None;
  }

  suspend fun sticker(sticker: String) = {
    //todo: later
  }
}