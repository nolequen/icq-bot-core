package su.nlq.icq.bot

import io.ktor.http.content.LocalFileContent
import org.junit.Assert
import org.junit.Test
import java.io.File

class FilesTest {
  private val file = "myfile"

  @Test
  fun info() {
    val dlink = "file-url"

    whenever {
      files().info(file)
    }.request {
      Assert.assertEquals("/files/getInfo", url.encodedPath)
      Assert.assertTrue(url.parameters.contains("file_id", file))
    }.response(
        "{\"status\":200,\"file_list\":"
            + "[{\"is_antivirable\":0,\"keepdate\":\"1970-01-01 00:00:01\",\"is_previewable\":0,\"filesize\":\"16\","
            + "\"filename\":\"myfile.txt\",\"avstatus\":\"healthy\",\"avchecked\":\"1970-01-01 00:00:01\","
            + "\"md5\":\"ia13607c3c5N1TJbvKuJ1JsdhFIq3oLfr\",\"dlink\":\"$dlink\",\"mime\":\"text\\/plain\"}],"
            + "\"delay_main\":0,\"file_count\":1,\"delay\":0,\"file_notready_count\":0,\"delay_tech\":0}"
    ) { fileURL ->
      Assert.assertEquals(dlink, fileURL)
    }
  }

  @Test
  fun upload() {
    val filepath = "myfile.txt"
    val fileurl = "https://files.icq.net/get/myfile.txt)"
    whenever {
      files().upload(File(this::class.java.classLoader.getResource(filepath).toURI()))
    }.request {
      Assert.assertEquals("/im/sendFile", url.encodedPath)
      Assert.assertTrue(content is LocalFileContent)
      Assert.assertTrue((content as LocalFileContent).file.path.endsWith(filepath))
    }.response(
        "{\"status\":200,\"body\":\"Ok\","
            + "\"data\":{\"snapId\":null,\"ttl_id\":null,\"fileid\":\"id\",\"filesize\":18,\"filename\":null,"
            + "\"mime\":\"text/plain\",\"content_id\":\"id\",\"static_url\":\"$fileurl\"}}"
    ) {
      Assert.assertEquals(fileurl, it)
    }
  }
}