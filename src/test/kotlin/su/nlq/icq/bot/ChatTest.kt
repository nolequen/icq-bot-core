package su.nlq.icq.bot

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.content.TextContent
import org.junit.Assert
import org.junit.Test

class ChatTest {

  private val chat = "mychat"

  @Test
  fun pending() {
    whenever {
      chat(chat).pending()
    }.request {
      Assert.assertEquals("/rapi", url.encodedPath)
      Assert.assertTrue(content is TextContent)
      val json = (content as TextContent).json()
      Assert.assertEquals("getPendingList", json["method"].asText())
      Assert.assertEquals(chat, json["params"]["sn"].asText())
    }.response(
        "{\"ts\":0,\"status\":{\"code\":20000,\"reason\":null},"
            + "\"method\":\"getPendingList\",\"reqId\":\"request-id\","
            + "\"results\":{\"list\":[],\"persons\":[]}}"
    ) {
      Assert.assertEquals(0, it.size)
    }
  }

  @Test
  fun blocked() {
    val botname = "thebot"

    whenever {
      chat(chat).blocked()
    }.request {
      Assert.assertEquals("/rapi", url.encodedPath)
      Assert.assertTrue(content is TextContent)
      val json = (content as TextContent).json()
      Assert.assertEquals("getChatBlocked", json["method"].asText())
      Assert.assertEquals(chat, json["params"]["sn"].asText())
    }.response(
        "{\"ts\":0,\"status\":{\"code\":20000,\"reason\":null},"
            + "\"method\":\"getChatBlocked\",\"reqId\":\"request-id\","
            + "\"results\":{\"list\":[{\"sn\":\"42\",\"role\":\"member\",\"noAvatar\":null,"
            + "\"friendly\":\"$botname\",\"creator\":false,\"anketa\":{\"sn\":\"42\",\"friendly\":\"$botname\","
            + "\"nick\":null,\"nickname\":\"$botname\",\"firstName\":\"$botname\",\"lastName\":null,\"country\":null,"
            + "\"birthDate\":null,\"role\":null},\"lastseen\":null}],\"persons\":[]}}"
    ) {
      Assert.assertEquals(1, it.size)
      Assert.assertEquals(botname, it.first().name)
    }
  }

  @Test
  fun history() {
    val botname = "thebot"
    val message = "test message"

    whenever {
      chat(chat).history(0, 1)
    }.request {
      Assert.assertEquals("/rapi", url.encodedPath)
      Assert.assertTrue(content is TextContent)
      val json = (content as TextContent).json()
      Assert.assertEquals("getHistory", json["method"].asText())
      Assert.assertEquals(chat, json["params"]["sn"].asText())
      Assert.assertEquals(0, json["params"]["fromMsgId"].asInt())
      Assert.assertEquals(1, json["params"]["count"].asInt())
    }.response(
        "{\"ts\":0,\"status\":{\"code\":20000,\"reason\":null},"
            + "\"method\":\"getHistory\",\"reqId\":\"request-id\","
            + "\"results\":{\"messages\":[{\"msgId\":1,\"time\":0,\"wid\":\"09ae10ab2e09-65c8-9e11-06b6-02dee0bf\","
            + "\"chat\":{\"sender\":\"$botname\",\"name\":\"$chat\",\"memberEvent\":null,\"modifiedInfo\":null},"
            + "\"readsCount\":0,\"outgoing\":false,\"text\":\"$message\",\"sticker\":null,\"eventTypeId\":null,"
            + "\"mentions\":null,\"pendingJoin\":false,\"class\":null}],\"lastMsgId\":1,\"newerMsgId\":0,"
            + "\"patchVersion\":\"1\",\"yours\":{\"lastRead\":1,\"lastDelivered\":1,\"lastReadMention\":1},"
            + "\"unreads\":4,\"unreadCnt\":4,\"lastMessageHeads\":null,\"patch\":[],\"pinned\":null,\"persons\":[]}}"
    ) {
      Assert.assertEquals(1, it.messages.size)
      Assert.assertEquals(1, it.messages.first().id)
      Assert.assertTrue(it.messages.first() is Chat.Text)
      Assert.assertEquals(message, (it.messages.first() as Chat.Text).text)
    }
  }

  @Test
  fun admins() {
    val botname = "thebot"

    whenever {
      chat(chat).admins()
    }.request {
      Assert.assertEquals("/rapi", url.encodedPath)
      Assert.assertTrue(content is TextContent)
      val json = (content as TextContent).json()
      Assert.assertEquals("getChatAdmins", json["method"].asText())
      Assert.assertEquals(chat, json["params"]["sn"].asText())
    }.response(
        "{\"ts\":0,\"status\":{\"code\":20000,\"reason\":null},"
            + "\"method\":\"getChatAdmins\",\"reqId\":\"request-id\","
            + "\"results\":{\"name\":\"$chat\",\"stamp\":\"mystamp\",\"createTime\":100,\"creator\":\"$botname\","
            + "\"controlled\":true,\"live\":false,\"joinModeration\":false,\"infoVersion\":1,\"membersVersion\":1,"
            + "\"membersCount\":3,\"adminsCount\":1,\"pendingCount\":0,\"blockedCount\":0,\"memberIncrease\":0,"
            + "\"defaultRole\":\"member\",\"regions\":\"RU\",\"sn\":\"$chat\",\"abuseReportsCurrentCount\":0,"
            + "\"you\":{\"role\":\"member\"},\"members\":[{\"sn\":\"$botname\",\"role\":\"admin\",\"noAvatar\":true,"
            + "\"friendly\":\"$botname\",\"creator\":true,\"anketa\":{\"sn\":\"$botname\",\"friendly\":\"$botname\","
            + "\"nick\":null,\"nickname\":null,\"firstName\":\"$botname\",\"lastName\":\"$botname\",\"country\":null,"
            + "\"birthDate\":null,\"role\":null},\"lastseen\":\"0\"}],\"persons\":[{\"sn\":\"42\","
            + "\"friendly\":\"$botname\",\"nick\":\"$botname\",\"firstName\":\"$botname\"}]}}"
    ) {
      Assert.assertEquals(1, it.size)
      Assert.assertEquals(botname, it.first().name)
      Assert.assertEquals(Chat.Role.Administrator, it.first().role)
    }
  }

  @Test
  fun invite() {
    val penpal = "penpal"

    whenever {
      chat(chat).invite(listOf(PenPal(penpal)))
    }.request {
      Assert.assertEquals("/chat/add", url.encodedPath)
      Assert.assertTrue(url.parameters.contains("chat_id", chat))
      Assert.assertTrue(url.parameters.contains("members", penpal))
    }.response { }
  }

  @Test
  fun description() {
    val botname = "thebot"
    val created = 100L

    whenever {
      chat(chat).description()
    }.request {
      Assert.assertEquals("/rapi", url.encodedPath)
      Assert.assertTrue(content is TextContent)
      val json = (content as TextContent).json()
      Assert.assertEquals("getChatInfo", json["method"].asText())
      Assert.assertEquals(chat, json["params"]["sn"].asText())
    }.response(
        "{\"ts\":0,\"status\":{\"code\":20000,\"reason\":null},"
            + "\"method\":\"getChatInfo\",\"reqId\":\"request-id\","
            + "\"results\":{\"name\":\"$chat\",\"stamp\":\"mystamp\",\"createTime\":$created,"
            + "\"creator\":\"$botname\",\"controlled\":true,\"live\":false,\"joinModeration\":false,"
            + "\"infoVersion\":1,\"membersVersion\":1,\"membersCount\":2,"
            + "\"adminsCount\":1,\"pendingCount\":0,\"blockedCount\":0,\"memberIncrease\":0,"
            + "\"defaultRole\":\"member\",\"regions\":\"RU\",\"sn\":\"$chat\","
            + "\"abuseReportsCurrentCount\":0,\"you\":{\"role\":\"member\"},\"members\":[{\"sn\":\"43\","
            + "\"role\":\"member\",\"noAvatar\":true,\"friendly\":\"$botname\",\"creator\":true,"
            + "\"lastseen\":\"0\",\"anketa\":{\"sn\":\"43\",\"friendly\":\"$botname\","
            + "\"nick\":\"$botname\",\"nickname\":\"thebot\",\"firstName\":\"thebot\",\"lastName\":null,"
            + "\"country\":null,\"birthDate\":null,\"role\":null}},{\"sn\":\"42\",\"role\":\"member\"," +
            "\"noAvatar\":true,\"friendly\":\"somebody\",\"creator\":false,\"lastseen\":\"0\",\"anketa\":{"
            + "\"sn\":\"42\",\"friendly\":\"somebody\",\"nick\":\"somebody\",\"nickname\":\"somebody\","
            + "\"firstName\":\"somebody\",\"lastName\":null,\"country\":null,\"birthDate\":null,\"role\":null}}],"
            + "\"persons\":[{\"sn\":\"43\",\"friendly\":\"$botname\",\"nick\":\"thebot\"," +
            "\"firstName\":\"$botname\"}]}}"
    ) {
      Assert.assertEquals(chat, it.name)
      Assert.assertEquals(created, it.created)
      Assert.assertEquals(botname, it.creator)
    }
  }
}

private val mapper = jacksonObjectMapper()

private fun TextContent.json() = mapper.readTree(text)
