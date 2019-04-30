package su.nlq.icq.bot

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonValue
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import su.nlq.icq.bot.request.HttpRequest
import java.time.Duration
import java.util.*

class Subscription internal constructor(
    private val bot: Bot,
    private val timeout: Duration
) {

  fun subscribe(block: (EventData) -> Unit) {
    GlobalScope.launch(Dispatchers.Default) {
      HttpRequest("fetchEvents") { parameter("first", intArrayOf(1)) }.request<Response>(bot).onSuccess {
        listen(it.url(), block)
      }
    }
  }

  private suspend fun listen(initital: String, block: (EventData) -> Unit) {
    var url = initital
    while (true) {
      url = fetch(url, block)
    }
  }

  private suspend fun fetch(url: String, block: (EventData) -> Unit) =
      bot.client.runCatching { get<Response>(update(url)) }
          .fold(
              {
                it.response.data.events.map { event -> event.eventData }.forEach(block)
                delay(it.timeout())
                it.url()
              },
              {
                delay(timeout)
                url
              }
          )

  private fun Duration.max(other: Duration) = if (compareTo(other) > 0) this else other

  private fun update(url: String): Url {
    val builder = URLBuilder(url)
    builder.parameters.remove("first")
    builder.parameters["r"] = UUID.randomUUID().toString()
    builder.parameters["aimsid"] = bot.token
    builder.parameters["timeout"] = timeout.toMillis().toString()
    return builder.build()
  }

  private data class Response(val response: Content) {

    fun url() = response.data.fetchBaseURL

    fun timeout() = Duration.ofMillis(response.data.timeToNextFetch.toLong())
  }

  private data class Content(
      val statusCode: Int,
      val statusText: String,
      val requestId: String,
      val data: Data
  )

  private data class Data(
      val pollTime: Int,
      val ts: Long,
      val timeToNextFetch: Int,
      val fetchBaseURL: String,
      val fetchTimeout: Int,
      val events: List<Event<EventData>>
  )

  private data class Event<out T : EventData>(
      val type: String,
      val seqNum: Int,

      @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
      @JsonSubTypes(
          JsonSubTypes.Type(value = ServiceData::class, name = "service"),
          JsonSubTypes.Type(value = TypingData::class, name = "typing"),
          JsonSubTypes.Type(value = MessageData::class, name = "im"),
          JsonSubTypes.Type(value = ContactsData::class, name = "buddylist"),
          JsonSubTypes.Type(value = PersonalData::class, name = "myInfo")
      )
      val eventData: T
  )

  abstract class EventData

  data class ServiceData(
      //todo: one of
      val serviceURLs: List<ServiceURLs>?,
      val serviceConfigs: List<ServiceConfig>?,
      val servicePromo: ServicePromo?
  ) : EventData()

  data class ServiceURLs(
      val name: String,
      val urls: Map<String, String>
  )

  data class ServiceConfig(
      val name: String,
      val friendlyName: String,
      val associated: Int
  )

  data class ServicePromo(
      val enabled: Int,
      val url: String,
      val image: String,
      val altText: String,
      val promoId: String,
      val service: String
  )

  data class TypingData(
      val aimId: String,
      val typingStatus: Conversation.Status
  ) : EventData()

  data class MessageData(
      val autoresponse: Int,
      val timestamp: Long,
      val notification: NotificationType,
      val stickerId: String?,
      val msgId: String,
      val MChat_Attrs: Attributes?,
      val imf: String,
      val message: String,
      val rawMsg: RawMessage,
      val source: Source
  ) : EventData()

  enum class NotificationType(@get:JsonValue val value: String) {
    Internal("internal"),
    Full("full")
  }

  data class Attributes(
      val method: Method?,
      val sender: String,
      val senderName: String,
      val chat_name: String,
      val members: String?
  )

  enum class Method(@get:JsonValue val value: String) {
    Invite("invite"),
    TurnOut("turn_out")
  }

  data class RawMessage(
      val base64Msg: String,
      val ipCountry: String?,
      val clientCountry: String?
  )

  data class Source(
      val aimId: String,
      val displayId: String,
      val friendly: String,
      val state: State,
      val userType: UserType,
      val buddyIcon: String?
  )

  data class ContactsData(
      val groups: List<Group>
  ) : EventData()

  data class Group(
      val name: String,
      val id: Int,
      val buddies: List<Buddy>
  )

  data class Buddy(
      val aimId: String,
      val displayId: String,
      val friendly: String,
      val state: State,
      val userType: UserType
  )

  data class PersonalData(
      val aimId: String,
      val displayId: String,
      val friendly: String,
      val state: State,
      val userType: UserType,
      val nick: String,
      val userAgreement: List<String>,
      val globalFlags: Int
  ) : EventData()

  enum class State(@get:JsonValue val value: String) {
    Online("online"),
    Offline("offline")
  }

  enum class UserType(@get:JsonValue val value: String) {
    ICQ("icq"),
    Interoperable("interop")
  }
}