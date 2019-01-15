package su.nlq.icq.bot

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature

class Bot(
    internal val token: String,
    internal val client: HttpClient = HttpClient {
      install(JsonFeature) {
        serializer = JacksonSerializer()
      }
    }
) {

  fun conversation(penpal: PenPal) = Conversation(this, penpal)

  fun chat(id: String) = Chat(this, id)

  fun files() = Files(this)

  fun contacts() = Contacts(this)

  fun events() = Events(this)
}