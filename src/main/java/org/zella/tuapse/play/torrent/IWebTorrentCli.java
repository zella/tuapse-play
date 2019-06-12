package org.zella.tuapse.play.torrent;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.zella.tuapse.play.model.torrent.TorrentFileDesc;
import org.zella.tuapse.play.model.torrent.TorrentState;

import java.nio.file.Path;
import java.util.Map;

public interface IWebTorrentCli {
    Completable downloadFile(String hash, Path dir, int index);

    Completable download(String hash, Path dir);

    Single<Map<Integer, TorrentFileDesc>> fetchFiles(String hash);

    Observable<TorrentState> streamFile(String hash, int index, String stream);
}
