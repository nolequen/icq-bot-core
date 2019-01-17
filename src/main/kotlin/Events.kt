package su.nlq.icq.bot

import su.nlq.icq.bot.request.HttpRequest

//todo: replace with Subscription
class Events internal constructor(private val bot: Bot) {

  suspend fun events() = HttpRequest("fetchEvents").request<Unit>(bot)
}
