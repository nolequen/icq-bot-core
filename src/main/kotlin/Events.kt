package su.nlq.icq.bot

import su.nlq.icq.bot.request.HttpRequest

//todo: replace with Subscription
class Events(private val token: String) {

  suspend fun events() = HttpRequest("fetchEvents").request<Unit>(token)
}
