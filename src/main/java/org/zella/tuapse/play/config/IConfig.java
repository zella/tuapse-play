package org.zella.tuapse.play.config;

import java.nio.file.Path;

public interface IConfig {
    int port();

    String webTorrentExecutable();

    Path torrentsDir();

    int streamTimeoutSec();

    int downloadTimeoutSec();

}
