package org.zella.tuapse.play.model.net;

public class PlayInput {

    public String hash;
    public int index;
    public String streaming;

    public PlayInput() {
    }

    public PlayInput(String hash, int index, String streaming) {
        this.hash = hash;
        this.index = index;
        this.streaming = streaming;
    }
}
