package su.nlq.icq.bot

import com.fasterxml.jackson.annotation.JsonValue
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import su.nlq.icq.bot.request.HttpRequest
import java.util.regex.Pattern

class Conversation internal constructor(
    private val bot: Bot,
    private val penpal: PenPal,
    wrapThreshold: Int
) {

  private val messageWrap: Pattern = Pattern.compile("\\b.{1,${wrapThreshold - 1}}\\b\\W?")

  suspend fun message(message: String, mentions: Collection<PenPal> = emptyList()): Result<Unit> {
    val matcher = messageWrap.matcher(message)
    while (matcher.find()) {
      val sendResult = HttpRequest("im/sendIM", HttpMethod.Post) {
        parameter("t", penpal.id)
        parameter("message", matcher.group().trim())
        parameter("mentions", mentions.joinToString(",") { it.id })
      }.request<Unit>(bot)

      if (sendResult.isFailure) {
        return sendResult
      }
    }
    return Result.success(Unit)
  }

  suspend fun typing(status: Status = Status.Typing) = HttpRequest("im/setTyping", HttpMethod.Post) {
    parameter("t", penpal.id)
    parameter("typingStatus", status)
  }.request<Unit>(bot)

  enum class Status(@get:JsonValue val value: String) {
    Looking("looking"),
    Typing("typing"),
    Typed("typed"),
    None("none");
  }

  suspend fun sticker(sticker: String) = {
    //todo: not implemented
  }
}