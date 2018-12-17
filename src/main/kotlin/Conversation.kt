package su.nlq.icq.bot

import su.nlq.icq.bot.commands.SendMessage
import su.nlq.icq.bot.commands.Typing

class Conversation(
    private val bot: Bot,
    private val companion: String
) {

  suspend fun message(message: String, mentions: Collection<String> = emptyList()) =
      bot.execute(SendMessage(companion, message, mentions))

  suspend fun typing(status: Typing.Status = Typing.Status.Typing) =
      bot.execute(Typing(companion, status))

  suspend fun sticker(sticker: String) = {
    //todo
  }
}