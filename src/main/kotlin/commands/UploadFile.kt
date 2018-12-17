package commands

import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.PartData
import io.ktor.http.headersOf
import kotlinx.io.streams.asInput
import su.nlq.icq.bot.api.HttpAPI
import su.nlq.icq.bot.commands.Command
import java.io.File
import java.io.FileInputStream

class UploadFile(private val file: File) : Command<String> {

  override suspend fun execute(api: HttpAPI) = api.request<Response>("im/sendFile", HttpMethod.Post) {
    val stream = FileInputStream(file)
    body = MultiPartFormDataContent(listOf(PartData.FileItem(
        { stream.asInput() },
        {},
        headersOf(
            HttpHeaders.ContentDisposition,
            ContentDisposition.File.withParameter(ContentDisposition.Parameters.FileName, file.name).toString()
        )
    )))
  }.map { it.data.static_url }

  private data class Response(
      val status: Int,
      val body: String,
      val data: FileData
  )

  private data class FileData(
      val snapId: String?,
      val ttl_id: String?,
      val is_previewable: Int,
      val fileid: String,
      val filesize: Int,
      val filename: String?,
      val mime: String,
      val content_id: String,
      val static_url: String
  )
}