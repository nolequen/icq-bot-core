package su.nlq.icq.bot

import su.nlq.icq.bot.commands.Buddies

class Buddies(private val owner: Bot) {

  suspend fun list() = owner.execute(Buddies())
}