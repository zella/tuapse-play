package org.zella.tuapse.play.core;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zella.tuapse.play.config.IConfig;
import org.zella.tuapse.play.model.download.DownloadCompleted;
import org.zella.tuapse.play.model.download.DownloadStarted;
import org.zella.tuapse.play.model.download.IDownload;
import org.zella.tuapse.play.model.torrent.TorrentFileDesc;
import org.zella.tuapse.play.torrent.IWebTorrentCli;

import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SingletonCore {

    private static final Logger logger = LoggerFactory.getLogger(SingletonCore.class);

    private final IWebTorrentCli torrent;

    private AtomicReference<Disposable> currentPlay = new AtomicReference<>();

    private final IConfig config;

    public SingletonCore(IWebTorrentCli torrent, IConfig config) {
        this.torrent = torrent;
        this.config = config;
    }


    /**
     * Note: it cancel current playing
     *
     * @param hash      torrent hash
     * @param index     file index, see webtorrent-cli docs
     * @param streaming webtorrent-cli streaming
     * @return Start notification
     */
    //TODO smells...
    public Single<Boolean> playNow(String hash, int index, String streaming) {
        return Single.fromCallable(() -> {
            if (currentPlay.get() != null)
                currentPlay.get().dispose();
            currentPlay.set(torrent.streamFile(hash, index, streaming).subscribe());
            return true;
        }).timeout(config.streamTimeoutSec(), TimeUnit.SECONDS)
                .doOnError(e -> {
                    if (currentPlay.get() != null)
                        currentPlay.get().dispose();
                });
    }

    private Map.Entry<Integer, TorrentFileDesc> findEntry(Map<Integer, TorrentFileDesc> files, String path) {
        return files.entrySet().stream()
                .filter(kv -> kv.getValue().toStringPath().equals(path))
                .findFirst()
                .orElseGet(() -> {
                    throw new RuntimeException("Cant find " + path);
                });
    }

    public Single<Boolean> playNow(String hash, String path, String streaming) {
        return torrent.fetchFiles(hash)
                .map(files -> findEntry(files, path))
                .flatMap(e -> playNow(hash, e.getKey(), streaming));
    }

    //download file
    public Flowable<IDownload> download(String hash, String path) {
        Single<Map.Entry<Integer, TorrentFileDesc>> findEntry = torrent.fetchFiles(hash)
                .map(files -> findEntry(files, path)).cache();

        Single<IDownload> prefetch = findEntry
                .map(e -> new DownloadStarted(e.getValue().toPath()))
                .cast(IDownload.class)
                .cache();

        //TODO check file exist before

        Single<IDownload> download = findEntry
                .flatMap(e -> torrent.downloadFile(hash, config.torrentsDir().resolve(hash), e.getKey())
                        .toSingleDefault(new DownloadCompleted()));

        return prefetch.concatWith(download)
                .timeout(config.downloadTimeoutSec(), TimeUnit.SECONDS);
    }

}
