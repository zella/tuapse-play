package org.zella.tuapse.play.model.download;

import java.nio.file.Path;

public class DownloadStarted implements IDownload {
    public final Path file;

    public DownloadStarted(Path file) {
        this.file = file;
    }
}
