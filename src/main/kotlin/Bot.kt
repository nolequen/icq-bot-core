package su.nlq.icq.bot

import su.nlq.icq.bot.api.HttpAPI
import su.nlq.icq.bot.commands.Command

class Bot(token: String) {

  private val api: HttpAPI = HttpAPI(token)

  internal suspend fun <T> execute(command: Command<T>) = command.execute(api)
}