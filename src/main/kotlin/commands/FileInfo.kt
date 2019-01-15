package su.nlq.icq.bot.commands

import io.ktor.client.request.parameter
import su.nlq.icq.bot.api.HttpAPI

class FileInfo(private val id: String) : Command<String> {

  override suspend fun execute(api: HttpAPI) = api.request<Response>("files/getInfo") {
    parameter("file_id", id)
  }.map { response -> response.file_list.map { it.dlink }.first() }

  private data class Response(
      val status: Int,
      val delay_main: Int,
      val file_count: Int,
      val delay: Int,
      val file_notready_count: Int,
      val delay_tech: Int,
      val file_list: List<FileData>
  )

  private data class FileData(
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
}