package org.zella.tuapse.play.config.impl;

import com.typesafe.config.Config;
import org.zella.tuapse.play.config.IConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TypesafeConfig implements IConfig {

    private final Config config;

    public TypesafeConfig(Config config) {
        this.config = config;
    }

    @Override
    public int port() {
        return config.getInt("http.port");
    }

    @Override
    public String webTorrentExecutable() {
        return config.getString("webTorrent.executable");
    }

    @Override
    public Path torrentsDir() {
        return Paths.get(config.getString("webTorrent.dir"));
    }

    @Override
    public int streamTimeoutSec() {
        return config.getInt("timeouts.streamSec");
    }

    @Override
    public int downloadTimeoutSec() {
        return config.getInt("timeouts.downloadSec");
    }

}
