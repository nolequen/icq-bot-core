package su.nlq.icq.bot.commands

import su.nlq.icq.bot.api.HttpAPI

//todo: replace with Subscription
class FetchEvents : Command<Unit> {

  override suspend fun execute(api: HttpAPI) = api.request<Unit>("fetchEvents")
}
