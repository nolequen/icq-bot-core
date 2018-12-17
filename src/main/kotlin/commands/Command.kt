package su.nlq.icq.bot.commands

import su.nlq.icq.bot.api.HttpAPI

interface Command<out T> {

  suspend fun execute(api: HttpAPI): Result<T>
}