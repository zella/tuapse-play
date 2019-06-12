package org.zella.tuapse.play;

import com.typesafe.config.ConfigFactory;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zella.tuapse.play.config.impl.TypesafeConfig;
import org.zella.tuapse.play.core.SingletonCore;
import org.zella.tuapse.play.net.Server;
import org.zella.tuapse.play.torrent.impl.DefaultWebTorrent;
import org.zella.tuapse.play.utils.Utils;

import java.util.concurrent.TimeUnit;

public class RunnerMain {

    private static final Logger logger = LoggerFactory.getLogger(RunnerMain.class);

    public static void main(String[] args) {
        var config = new TypesafeConfig(ConfigFactory.defaultApplication().resolve());

        var torrent = new DefaultWebTorrent(config.webTorrentExecutable());
        var core = new SingletonCore(torrent, config);

        //TODO env and remove config
        Observable.interval(1, TimeUnit.HOURS)
                //TODO env
                .flatMap(aLong -> Observable.fromCallable(() -> Utils.recursiveDeleteFilesAcessedOlderThanNDays(1, config.torrentsDir())))
                .subscribeOn(Schedulers.io())
                .subscribe(n -> logger.info("Deleted " + n + " files"));


        new Server(core, config).create()
                .subscribe(httpServer -> logger.info("Server started at port: " + httpServer.actualPort()));

    }
}
