package org.zella.tuapse.play.core;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zella.tuapse.play.config.IConfig;
import org.zella.tuapse.play.model.download.DownloadCompleted;
import org.zella.tuapse.play.model.download.DownloadStarted;
import org.zella.tuapse.play.model.download.IDownload;
import org.zella.tuapse.play.torrent.IWebTorrentCli;

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

    //download file
    public Flowable<IDownload> download(String hash, int index) {
        Single<IDownload> prefetch = torrent.fetchFiles(hash).map(files -> new DownloadStarted(files.get(hash).toPath()));
        Single<IDownload> download = (torrent.downloadFile(hash, config.torrentsDir().resolve(hash), index)
                .toSingleDefault(new DownloadCompleted()));
        return prefetch.concatWith(download)
                .timeout(config.downloadTimeoutSec(), TimeUnit.SECONDS);
    }

}
