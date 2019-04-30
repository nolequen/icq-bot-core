package su.nlq.icq.bot

import org.junit.Assert
import org.junit.Test

class ContactsTest {

  @Test
  fun list() {
    val buddy = "mychat"
    whenever {
      contacts().all()
    }.request {
      Assert.assertEquals("/getBuddyList", url.encodedPath)
    }.response(
        "{\"response\":{\"statusCode\":200,\"statusText\":\"Ok\","
            + "\"data\":{\"groups\":[{\"id\":1,\"name\":\"Conferences\","
            + "\"buddies\":[{\"aimId\":\"42@chat.agent\",\"displayId\":\"42@chat.agent\","
            + "\"friendly\":\"$buddy\",\"userType\":\"aim\"}]}]}}}"
    ) {
      Assert.assertEquals(1, it.size)
      Assert.assertEquals(buddy, it.first().name)
    }
  }
}