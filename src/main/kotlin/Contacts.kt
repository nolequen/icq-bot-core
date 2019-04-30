package su.nlq.icq.bot

import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import su.nlq.icq.bot.request.HttpRequest

class Contacts internal constructor(private val bot: Bot) {

  suspend fun all() = HttpRequest("getBuddyList").request<Response>(bot)
      .map { (json) ->
        json.data.groups
            ?.flatMap { it.buddies }
            ?.map { Buddy(it.aimId, it.friendly, bot) }
            ?: emptyList()
      }

  private data class Response(
      @JsonProperty("response") val response: Content
  )

  private data class Content(
      val statusCode: Int,
      val statusText: String,
      val data: RawData
  )

  private data class RawData(
      @JsonProperty("groups") val groups: List<RawGroup>?
  )

  private data class RawGroup(
      val id: Int,
      val name: String,
      val buddies: List<RawBuddy> = emptyList()
  )

  private data class RawBuddy(
      val aimId: String,
      val displayId: String,
      val friendly: String,
      val userType: String
  )

  class Buddy internal constructor(
      id: String,
      val name: String,
      private val bot: Bot
  ) : PenPal(id) {

    suspend fun remove(group: String) = remove { parameter("group", group) }

    suspend fun remove() = remove { parameter("allGroups", 1) }

    private suspend fun remove(parameters: HttpRequestBuilder.() -> Unit) = HttpRequest("/buddylist/removeBuddy", HttpMethod.Post) {
      parameter("buddy", id)
      parameters()
    }.request<Unit>(bot)
  }
}