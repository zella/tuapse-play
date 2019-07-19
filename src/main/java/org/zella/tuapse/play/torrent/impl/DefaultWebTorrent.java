package org.zella.tuapse.play.torrent.impl;

import com.github.davidmoten.rx2.Strings;
import com.github.zella.rxprocess2.RxNuProcessBuilder;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zella.tuapse.play.torrent.IWebTorrentCli;
import org.zella.tuapse.play.model.torrent.TorrentFileDesc;
import org.zella.tuapse.play.model.torrent.TorrentState;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class DefaultWebTorrent implements IWebTorrentCli {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWebTorrent.class);

    private static final String WEB_TORR_DELIMTER = "<:>";


    private final String execPath;

    public DefaultWebTorrent(String execPath) {
        this.execPath = execPath;
    }

    public Single<Map<Integer, TorrentFileDesc>> fetchFiles(String hash) {
        logger.debug("Fetch files for torrent: " + hash);
        List<String> cmd = io.vavr.collection.List.of((execPath.split(" ")))
                .appendAll(List.of("magnet:?xt=urn:btih:" + hash, "--select")).asJava();

        return RxNuProcessBuilder.fromCommand(cmd)
                .asStdOut().toFlowable(BackpressureStrategy.BUFFER)
                .compose(src -> Strings.decode(src, Charset.defaultCharset()))
                .compose(src -> Strings.split(src, System.lineSeparator()))
                .skipWhile(s -> !s.equals("files begin"))
                .skip(1)
                .takeWhile(s -> !s.equals("files end"))
                .map(s -> {
                            String[] l = s.split(WEB_TORR_DELIMTER);
                            int idx = Integer.parseInt(l[0]);
                            long size = Long.parseLong(l[2]);
                            String[] paths = l[1].split("/"); //on windows webtorrent is ok?
                            return new TorrentFileDesc(idx, paths, size, hash);
                        }
                )
                .toMap(t -> t.index)
                .doOnSuccess(donCare -> logger.debug("Files fetched for torrent: " + hash));

    }

    public Completable download(String hash, Path dir) {
        return downloadFile(hash, dir, -1);
    }

    public Completable downloadFile(String hash, Path dir, int index) {

        List<String> cmd = io.vavr.collection.List.of((execPath.split(" ")))
                .appendAll(List.of("download", "magnet:?xt=urn:btih:" + hash, "--out", dir.toAbsolutePath().toString()))
                .appendAll((index == -1)
                        ? io.vavr.collection.List.empty()
                        : io.vavr.collection.List.of("--select", String.valueOf(index)))
                .asJava();

        return RxNuProcessBuilder.fromCommand(cmd)
                .asWaitDone()
                .doOnSubscribe(d -> logger.debug("Download torrent: " + hash + " index: " + index))
                .map(e -> {
                    if (e.err.isPresent()) throw e.err.get();
                    return e;
                })
                .ignoreElement()
                .doFinally(() -> logger.debug("Download torrent: " + hash + " index: " + index + " done"));
    }

    public Observable<TorrentState> streamFile(String hash, int index, String stream) {

        List<String> cmd = io.vavr.collection.List.of((execPath.split(" ")))
                .appendAll(List.of("magnet:?xt=urn:btih:" + hash))
                .appendAll(List.of("--select", String.valueOf(index), "--" + stream))
                .asJava();

        return RxNuProcessBuilder.fromCommand(cmd)
                .asStdOut().toFlowable(BackpressureStrategy.BUFFER)
                .compose(src -> Strings.decode(src, Charset.defaultCharset()))
                .compose(src -> Strings.split(src, System.lineSeparator()))
                .filter(s -> s.startsWith("Speed:"))
                .map(s -> {
                    var l = s.split(WEB_TORR_DELIMTER);
                    return new TorrentState((int) Float.parseFloat(l[1]), Long.parseLong(l[3]), Long.parseLong(l[4]), Long.parseLong(l[6]));
                })
                .toObservable()
                .doFinally(() -> logger.debug("Stream torrent: " + hash + " index: " + index + " finished"))
                .doOnSubscribe(d -> logger.debug("Stream torrent: " + hash + " index: " + index + " to " + stream));

    }


}
