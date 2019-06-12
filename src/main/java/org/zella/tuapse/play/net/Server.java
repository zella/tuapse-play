package org.zella.tuapse.play.net;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zella.tuapse.play.config.IConfig;
import org.zella.tuapse.play.core.SingletonCore;
import org.zella.tuapse.play.model.download.DownloadStarted;
import org.zella.tuapse.play.model.net.PlayInput;
import org.zella.tuapse.play.utils.Json;

import java.io.File;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final SingletonCore core;

    private final IConfig config;

    public Server(SingletonCore core, IConfig config) {
        this.core = core;
        this.config = config;
    }

    public Single<HttpServer> create() {

        Router router = Router.router(Vertx.vertx());

        router.get("/").handler(ctx -> ctx.reroute("/files"));
        router.get("/files/*").handler(StaticHandler.create()
                .setAllowRootFileSystemAccess(true)
                .setWebRoot(config.torrentsDir().toAbsolutePath().toString())
                .setDirectoryListing(true)
                .setFilesReadOnly(true)
                .setCachingEnabled(false)
        );
        router.get("/healthcheck").handler(ctx -> ctx.response().end("ok"));
        router.post().handler(BodyHandler.create());

        router.post("/api/v1/play").handler(ctx -> {
            logger.debug("Play...");
            readBody(ctx, PlayInput.class).flatMap(in ->
                    core.playNow(in.hash, in.index, in.streaming))
                    .subscribeOn(Schedulers.io())
                    .subscribe(b -> ctx.response().end(), err -> {
                        logger.error("Error", err);
                        ctx.fail(err);
                    });
        });

        router.get("/api/v1/fetchFile").handler(ctx -> {
            logger.debug("Fetch file...");
            var index = Integer.parseInt(ctx.request().getParam("index"));
            var hash = ctx.request().getParam("hash");
            core.download(hash, index).firstOrError()
                    .subscribeOn(Schedulers.io())
                    .cast(DownloadStarted.class)
                    .subscribe(f -> ctx.reroute("/files" + File.separator + config.torrentsDir().relativize(f.file)), err -> {
                        logger.error("Error", err);
                        ctx.fail(err);
                    });
        });
        //TODO remove it if v1 working good
        router.get("/api/v2/fetchFile").handler(ctx -> {
            logger.debug("Fetch file...");
            var index = Integer.parseInt(ctx.request().getParam("index"));
            var hash = ctx.request().getParam("hash");
            core.download(hash, index).firstOrError()
                    .subscribeOn(Schedulers.io())
                    .cast(DownloadStarted.class)
                    .subscribe(f -> ctx.response().sendFile(f.file.toAbsolutePath().toString()), err -> {
                        logger.error("Error", err);
                        ctx.fail(err);
                    });
        });

        return Vertx.vertx().createHttpServer().
                requestHandler(router).rxListen(config.port());
    }

    private <T> Single<T> readBody(RoutingContext body, Class<T> valueType) {
        return Single.fromCallable(() -> {
            var d = Json.mapper.readValue(body.getBodyAsString(), valueType);
            return d;
        });
    }
}