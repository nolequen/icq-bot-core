package su.nlq.icq.bot

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import io.ktor.client.request.parameter
import io.ktor.http.HttpMethod
import su.nlq.icq.bot.request.ExtendedHttpRequest
import su.nlq.icq.bot.request.HttpRequest

class Chat internal constructor(
    private val bot: Bot,
    private val id: String
) {

  suspend fun description() = requestDescription().map { Description(it) }

  class Description internal constructor(raw: RawDescription) {
    val name = raw.name
    val created = raw.createTime
    val creator = raw.creator
    private val stamp = raw.stamp
    private val sn = raw.sn
  }

  suspend fun history(from: Long, count: Int) = ExtendedHttpRequest("getHistory", HistoryParameters(id, from, count)).request<Response<RawHistory>>(bot)
      .map { History(it.results, id, bot) }

  class History internal constructor(raw: RawHistory, chat: String, bot: Bot) {
    val patch = raw.patch
    val version = raw.patchVersion
    val messages = raw.messages.map { message ->
      message.chat.modifiedInfo?.let { Modification(message) }
          ?: message.chat.memberEvent?.let { Event(message) }
          ?: Text(message, chat, bot)
    }
  }

  open class Message internal constructor(
      raw: RawMessage
  ) {
    val id = raw.msgId
    val created = raw.time
  }

  class Text internal constructor(
      raw: RawMessage,
      private val chat: String,
      private val bot: Bot
  ) : Message(raw) {
    val text = raw.text ?: ""
    val mentions = raw.mentions ?: emptyList()

    suspend fun pin() = pin(PinParameters(chat, id))

    private data class PinParameters(
        val sn: String,
        val msgId: Long
    )

    suspend fun unpin() = pin(UnpinParameters(chat, id))

    private data class UnpinParameters(
        val sn: String,
        val msgId: Long,
        val unpin: Boolean = true
    )

    private suspend fun pin(params: Any) = ExtendedHttpRequest("pinMessage", params).request<Unit>(bot)

    suspend fun delete() = ExtendedHttpRequest("delMsg", DeleteParameters(chat, id)).request<Unit>(bot)

    private data class DeleteParameters(
        val sn: String,
        val msgId: Long
    )
  }

  class Modification internal constructor(raw: RawMessage) : Message(raw) {
    val text = raw.text ?: ""
  }

  class Event internal constructor(raw: RawMessage) : Message(raw) {
    val type = raw.chat.memberEvent!!.type
    val role = raw.chat.memberEvent!!.role
    val members = raw.chat.memberEvent!!.members
  }

  internal data class RawHistory(
      val messages: List<RawMessage>,
      val lastMsgId: Long,
      val newerMsgId: Long,
      val patchVersion: String,
      val yours: RequestorMeta,
      val unreads: Int,
      val unreadCnt: Int,
      val lastMessageHeads: List<MessageHead>?,
      val patch: List<Patch>,
      val pinned: List<RawMessage>?,
      val persons: List<Profile>
  )

  data class Patch(
      val msgId: Long,
      val type: PatchType
  )

  enum class PatchType(@get:JsonValue val value: String) {
    Modification("modify"),
    Pin("pinned"),
    Unpin("unpinned")
  }

  internal data class MessageHead(
      val sn: String
  )

  internal data class RequestorMeta(
      val lastRead: Long,
      val lastDelivered: Long,
      val lastReadMention: Long
  )

  internal data class RawMessage(
      val msgId: Long,
      val time: Long,
      val wid: String?,
      val chat: MessageMeta,
      val readsCount: Int,
      val outgoing: Boolean,
      val text: String?,
      val sticker: Sticker?,
      val eventTypeId: String?,
      val mentions: List<String>?,
      val pendingJoin: Boolean,
      @JsonProperty("class") val type: MessageType?
  )

  internal enum class MessageType(@get:JsonValue val value: String) {
    Event("event")
  }

  internal data class Sticker(
      val id: String
  )

  internal data class MessageMeta(
      val sender: String,
      val name: String,
      val memberEvent: RawMemberEvent?,
      val modifiedInfo: RawModification?
  )

  internal data class RawMemberEvent(
      val type: MemberEventType,
      val role: Role,
      val members: List<String>
  )

  enum class MemberEventType(@get:JsonValue val value: String) {
    Invite("invite"),
    Add("addMembers"),
    Leave("leave"),
    Delete("delMembers")
  }

  internal data class RawModification(
      val name: String?,
      val live: Boolean,
      val blocked: Boolean,
      val privatized: Boolean,
      val approved: Boolean,
      val public: Boolean,
      val controlled: Boolean,
      val joinModeration: Boolean,
      val ageRestriction: Boolean,
      val video: Boolean,
      val official: Boolean,
      val defaultRole: Role?,
      val memberIncrease: Int
  )

  private data class HistoryParameters(
      val sn: String,
      val fromMsgId: Long,
      val count: Int,
      val patchVersion: String = "init"
  )

  suspend fun invite(members: List<PenPal>) = HttpRequest("chat/add", HttpMethod.Post) {
    parameter("chat_id", id)
    parameter("members", members.joinToString(";") { it.id })
  }.request<Unit>(bot)

  suspend fun members() = requestDescription().map { response -> response.members.map { Member(it, id, response.stamp, bot) } }

  private suspend fun requestDescription() = ExtendedHttpRequest("getChatInfo", ChatParameters(id)).request<Response<RawDescription>>(bot).map { it.results }

  suspend fun admins() = requestAdmins().map { response ->
    response.members.filter { it.role == Role.Moderator || it.role == Role.Administrator }.map { Member(it, id, response.stamp, bot) }
  }

  suspend fun creator() = requestAdmins().map { response ->
    response.members.find { it.creator }?.let { Member(it, id, response.stamp, bot) }
  }

  private suspend fun requestAdmins() = ExtendedHttpRequest("getChatAdmins", ChatParameters(id)).request<Response<RawDescription>>(bot).map { it.results }

  class Member internal constructor(
      raw: RawMember,
      private val chat: String,
      private val stamp: String,
      private val bot: Bot
  ) : PenPal(raw.sn) {
    val name = raw.friendly
    val role = raw.role

    suspend fun role(role: Role): Result<Unit> = ExtendedHttpRequest("modChatMember", RoleParameters(stamp, id, role)).request(bot)

    private data class RoleParameters(
        val stamp: String,
        val memberSn: String,
        val role: Role
    )

    suspend fun block(): Result<Unit> = ExtendedHttpRequest("blockChatMembers", BlockParameters(chat, listOf(BlockedMember(id, true)))).request(bot)
  }

  enum class Role(@get:JsonValue val value: String) {
    Administrator("admin"),
    Moderator("moder"),
    Member("member"),
    ReadOnly("readonly")
  }

  suspend fun blocked() = ExtendedHttpRequest("getChatBlocked", ChatParameters(id)).request<Response<RawNonMembers>>(bot).map { response -> response.results.list.map { Blocked(it, id, bot) } }

  class Blocked internal constructor(
      raw: RawMember,
      private val chat: String,
      private val bot: Bot) : PenPal(raw.sn) {
    val name = raw.anketa.nickname ?: "${raw.anketa.firstName} ${raw.anketa.lastName}"

    suspend fun unblock(): Result<Unit> = ExtendedHttpRequest("unblockChatMembers", BlockParameters(chat, listOf(BlockedMember(id, false)))).request(bot)
  }

  private data class BlockParameters(
      val sn: String,
      val members: List<BlockedMember>
  )

  private data class BlockedMember(
      val sn: String,
      val block: Boolean
  )

  suspend fun pending() = ExtendedHttpRequest("getPendingList", ChatParameters(id)).request<Response<RawNonMembers>>(bot).map { response -> response.results.list.map { Pending(it, id, bot) } }

  class Pending internal constructor(
      raw: RawMember,
      private val chat: String,
      private val bot: Bot
  ) : PenPal(raw.sn) {
    val name = raw.friendly

    suspend fun approve() = ExtendedHttpRequest("chatResolvePending", Parameters(chat, listOf(PendingMember(id)))).request<Unit>(bot)

    private data class Parameters(
        val sn: String,
        val members: List<PendingMember>
    )

    private data class PendingMember(
        val sn: String,
        val approve: Boolean = true
    )
  }

  private data class ChatParameters(
      val sn: String
  )

  private data class RawNonMembers(
      val list: List<RawMember>,
      val persons: List<Person>
  )

  private data class Response<out T>(
      val ts: Int,
      val status: ResponseStatus,
      val method: String,
      val reqId: String,
      val results: T
  )

  private data class ResponseStatus(
      val code: Int,
      val reason: String?
  )

  internal data class RawDescription(
      val name: String,
      val stamp: String,
      val createTime: Long,
      val creator: String?, // channel creator is hidden
      val controlled: Boolean,
      val live: Boolean,
      val joinModeration: Boolean,
      val infoVersion: Long,
      val membersVersion: Long,
      val membersCount: Int,
      val adminsCount: Int,
      val pendingCount: Int,
      val blockedCount: Int,
      val memberIncrease: Int,
      val defaultRole: Role,
      val regions: String,
      val sn: String,
      val abuseReportsCurrentCount: Int,
      val you: Requestor,
      val members: List<RawMember>,
      val persons: List<Person>
  )

  internal data class Person(
      val sn: String,
      val friendly: String,
      val nick: String,
      val firstName: String?
  )

  internal data class RawMember(
      val sn: String,
      val role: Role,
      val noAvatar: Boolean?,
      val friendly: String,
      val creator: Boolean,
      val anketa: Profile,
      val lastseen: String?
  )

  internal data class Profile(
      val sn: String,
      val friendly: String?,
      val nick: String?,
      val nickname: String?,
      val firstName: String?,
      val lastName: String?,
      val country: String?,
      val birthDate: Date?,
      val role: Role?
  )

  internal data class Date(
      val year: Int,
      val month: Int,
      val day: Int
  )

  internal data class Requestor(
      val role: Role
  )
}