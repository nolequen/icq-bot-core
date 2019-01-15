package su.nlq.icq.bot.commands

import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import su.nlq.icq.bot.api.HttpAPI
import java.util.*

class ChatInfo(private val id: String) : Command<String> {

  //todo: another one api
  override suspend fun execute(api: HttpAPI) = api.request<String>("rapi", HttpMethod.Post) {
    contentType(ContentType.Application.Json) //todo: useless?
    body = Request("getChatInfo", api.token, Parameters(id))
  }

  private data class Request(
      val method: String,
      val aimsid: String,
      val params: Parameters,
      val reqId: UUID = UUID.randomUUID()
  )

  private data class Parameters(
//      val stamp: String,
//      val member_limit: Int,
      val sn: String
  )
}