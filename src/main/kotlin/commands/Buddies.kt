package su.nlq.icq.bot.commands

import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import su.nlq.icq.bot.api.HttpAPI

class Buddies : Command<Collection<Buddies.Buddy>> {

  override suspend fun execute(api: HttpAPI): Result<Collection<Buddy>> {
    return api.request<Response>("getBuddyList")
        .map { (json) ->
          json.data.groups
              .flatMap { it.buddies }
              .map { Buddy(it.friendly, it.aimId, api) }
        }
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
      @JsonProperty("groups") val groups: List<RawGroup>
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

  //todo: types: chat, companion etc.
  class Buddy(
      val name: String,
      private val id: String,
      private val api: HttpAPI
  ) {

    suspend fun remove(group: String) = remove { parameter("group", group) }

    suspend fun remove() = remove { parameter("allGroups", 1) }

    private suspend fun remove(parameters: HttpRequestBuilder.() -> Unit) =
        api.request<String>("/buddylist/removeBuddy", HttpMethod.Post) {
          parameter("buddy", id)
          parameters()
        }.map { }
  }
}