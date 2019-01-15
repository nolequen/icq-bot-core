package su.nlq.icq.bot

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.LocalFileContent
import su.nlq.icq.bot.request.HttpRequest
import java.io.File
import java.net.URL
import java.nio.file.Path

class Files internal constructor(private val bot: Bot) {

  suspend fun upload(file: File) = HttpRequest("im/sendFile", HttpMethod.Post) {
    header(HttpHeaders.ContentDisposition, ContentDisposition.File.withParameter(ContentDisposition.Parameters.FileName, file.name).toString())
    body = LocalFileContent(file)
  }.request<UploadResponse>(bot).map { it.data.static_url }

  suspend fun info(id: String) = HttpRequest("files/getInfo") {
    parameter("file_id", id)
  }.request<FileInfoResponse>(bot).map { response -> response.file_list.map { it.dlink }.first() }

  suspend fun download(url: URL) = info(Path.of(url.path).fileName.toString()).mapCatching {
    HttpClient().call {
      url(it)
      method = HttpMethod.Get
    }.response.content
  }

  private data class FileInfoResponse(
      val status: Int,
      val delay_main: Int,
      val file_count: Int,
      val delay: Int,
      val file_notready_count: Int,
      val delay_tech: Int,
      val file_list: List<FileInfoData>
  )

  private data class FileInfoData(
      val is_antivirable: Int,
      val keepdate: String,
      val is_previewable: Int,
      val filesize: Int,
      val filename: String,
      val avstatus: String,
      val avchecked: String,
      val md5: String,
      val dlink: String,
      val mime: String
  )

  private data class UploadResponse(
      val status: Int,
      val body: String,
      val data: UploadFileData
  )

  private data class UploadFileData(
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