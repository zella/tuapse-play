package org.zella.tuapse.play.model.torrent;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TorrentFileDesc {
    public final int index;
    public final String[] paths;
    public final long size;
    public final String hash;

    public TorrentFileDesc(int index, String[] paths, long size, String hash) {
        this.index = index;
        this.paths = paths;
        this.size = size;
        this.hash = hash;
    }

    public Path toPath() {
        return Paths.get(toStringPath());
    }

    public String toStringPath() {
        return String.join(File.separator, paths);
    }

    public String toHumanText() {
        return FilenameUtils.removeExtension(toPath().getFileName().toString());
    }

    @Override
    public String toString() {
        return "TorrentFileDesc{" +
                "paths=" + toPath() +
                '}';
    }
}
