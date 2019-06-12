package org.zella.tuapse.play.torrent

import better.files.File
import org.scalatest._
import org.zella.tuapse.play.torrent.impl.DefaultWebTorrent

class WebtorrentSpec extends FlatSpec with Matchers {

  "DefaultWebTorrent" should "fetch full file paths" in {
    val dir = File.newTemporaryDirectory()
    val files = new DefaultWebTorrent(sys.env("WEB_TORRENT_EXECUTABLE")).fetchFiles("bcf1c2703e08392ae9e40e524acff93fc648f58f").blockingGet()
    files.size() shouldEqual 15
    dir.delete()
  }

  "DefaultWebTorrent" should "download" in {
    val dir = File.newTemporaryDirectory()
   // DefaultWebTorrent.streamFile("bcf1c2703e08392ae9e40e524acff93fc648f58f", 4, "xbmc").blockingAwait()
    new DefaultWebTorrent(sys.env("WEB_TORRENT_EXECUTABLE")).download("bcf1c2703e08392ae9e40e524acff93fc648f58f", dir.path).blockingAwait()
    dir.size > 100000000 shouldEqual true
    dir.size < 200000000 shouldEqual true
  }
}
