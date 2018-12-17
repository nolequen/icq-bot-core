package su.nlq.icq.bot

import commands.UploadFile
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import kotlinx.coroutines.io.ByteReadChannel
import su.nlq.icq.bot.commands.FileInfo
import java.io.File
import java.net.URL
import java.nio.file.Path

class Files(private val owner: Bot) {

  suspend fun upload(file: File) = owner.execute(UploadFile(file))

  suspend fun info(id: String) = owner.execute(FileInfo(id))

  suspend fun download(url: URL): Result<ByteReadChannel> = info(Path.of(url.path).fileName.toString())
      .mapCatching {
        HttpClient().call {
          url(it)
          method = HttpMethod.Get
        }.response.content
      }
}