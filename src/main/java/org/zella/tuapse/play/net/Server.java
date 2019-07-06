package org.zella.tuapse.play.net;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zella.tuapse.play.config.IConfig;
import org.zella.tuapse.play.core.SingletonCore;
import org.zella.tuapse.play.model.download.DownloadStarted;
import org.zella.tuapse.play.model.net.PlayInput;
import org.zella.tuapse.play.utils.Json;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("x-requested-with");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("origin");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("accept");
        allowedHeaders.add("X-PINGARUNER");
        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.OPTIONS);
        allowedMethods.add(HttpMethod.DELETE);
        allowedMethods.add(HttpMethod.PATCH);
        allowedMethods.add(HttpMethod.PUT);

        router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));

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
            readBody(ctx, PlayInput.class).flatMap(in -> {
                logger.debug("Play... " + in.toString());
                return core.playNow(in.hash, in.index, in.streaming);
            })
                    .subscribeOn(Schedulers.io())
                    .subscribe(b -> ctx.response().end(), err -> {
                        logger.error("Error", err);
                        ctx.fail(err);
                    });
        });

        router.get("/api/v1/fetchFile").handler(ctx -> {
            //TODO rewrite all
            var isDone = new AtomicBoolean(false);
            var index = Integer.parseInt(ctx.request().getParam("index"));
            var hash = ctx.request().getParam("hash");
            logger.debug("Fetch file... hash: " + hash + "index: " + index);
            core.download(hash, index)
                    .subscribeOn(Schedulers.io())
                    .subscribe(ff -> {
                        if (ff instanceof DownloadStarted) {
                            var f = (DownloadStarted) ff;
//                            var d = config.torrentsDir();
//                            var path = Paths.get(hash).resolve(f.file);
//                            var sdsd = f.file;
                            isDone.set(true);
                            //http://192.168.1.21:9800/files/28a179d3b86721b1db5c56e3b26834106dad15c5/Rus_Rock_Novog_2018_sevmakc_/004.%20%D0%9C%D1%83%D0%BC%D0%B8%D0%B9%20%D0%A2%D1%80%D0%BE%D0%BB%D0%BB%D1%8C%20-%20%D0%A1%20%D0%9D%D0%BE%D0%B2%D1%8B%D0%BC%20%D0%B3%D0%BE%D0%B4%D0%BE%D0%BC,%20%D0%BA%D1%80%D0%BE%D1%88%D0%BA%D0%B0!.mp3
//                            ctx.response().end();
                            ctx.reroute("/files/" + Paths.get(hash).resolve(f.file));
                        }

                    }, err -> {
                        logger.error("Error", err);
                        if (!isDone.get())
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
