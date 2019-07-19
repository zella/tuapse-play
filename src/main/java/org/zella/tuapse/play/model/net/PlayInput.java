package org.zella.tuapse.play.model.net;

import io.reactivex.annotations.Nullable;

public class PlayInput {

    public String hash;
    @Nullable
    public Integer index;
    @Nullable
    public String path;
    public String streaming;

    public PlayInput() {
    }

    @Override
    public String toString() {
        return "PlayInput{" +
                "hash='" + hash + '\'' +
                ", index=" + index +
                ", path=" + path +
                ", streaming='" + streaming + '\'' +
                '}';
    }
}
