package su.nlq.icq.bot

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockHttpRequest
import io.ktor.client.engine.mock.MockHttpResponse
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.hostWithPort
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import org.junit.Assert

interface Response<out T> {
  fun response(response: String = "", block: (T) -> Unit)
}

interface Request<out T> {
  fun request(block: MockHttpRequest.() -> Unit): Response<T>
}

private const val token = "001.1234567890.1234567890:123456789"

fun <T> whenever(request: suspend Bot.() -> Result<T>) = object : Request<T> {
  override fun request(block: MockHttpRequest.() -> Unit) = object : Response<T> {
    override fun response(response: String, block: (T) -> Unit) {

      val httpClient = HttpClient(MockEngine {
        Assert.assertEquals("Invalid request host:port", "botapi.icq.net:443", url.hostWithPort)
        Assert.assertTrue("'aimsid' parameter not found or invalid, bot token expected", url.parameters.contains("aimsid", token))
        block(this)
        MockHttpResponse(
            call,
            HttpStatusCode.OK,
            ByteReadChannel(response.toByteArray(Charsets.UTF_8)),
            headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
        )
      }) {
        install(JsonFeature) { serializer = JacksonSerializer() }
      }

      runBlocking {
        Assert.assertTrue(request(Bot(token, httpClient)).onSuccess(block).isSuccess)
      }
    }
  }
}