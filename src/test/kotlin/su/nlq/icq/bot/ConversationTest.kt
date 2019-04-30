package su.nlq.icq.bot

import org.junit.Assert
import org.junit.Test

class ConversationTest {
  private val penpal = PenPal("42")

  @Test
  fun sendMessage() =
      whenever {
        conversation(penpal).message("Hello World!")
      }.request {
        Assert.assertEquals("/im/sendIM", url.encodedPath)
        Assert.assertTrue(url.parameters.contains("t", penpal.id))
        Assert.assertTrue(url.parameters.contains("message", "Hello World!"))
      }.response { }

  @Test
  fun typingStatus() =
      whenever {
        conversation(penpal).typing()
      }.request {
        Assert.assertEquals("/im/setTyping", url.encodedPath)
        Assert.assertTrue(url.parameters.contains("t", penpal.id))
        Assert.assertTrue(url.parameters.contains("typingStatus", "Typing"))
      }.response { }
}