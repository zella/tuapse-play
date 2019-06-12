package org.zella.tuapse.play.model.torrent;

public class TorrentState {
    public final int speed;
    public final long downloaded;
    public final long torrentSize;
    public final long uploaded;

    public TorrentState(int speed, long downloaded, long torrentSize, long uploaded) {
        this.speed = speed;
        this.downloaded = downloaded;
        this.torrentSize = torrentSize;
        this.uploaded = uploaded;
    }
}
