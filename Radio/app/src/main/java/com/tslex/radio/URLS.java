package com.tslex.radio;

public enum URLS {

    //STREAMS
    ANISON_STREAM_320("http://pool.anison.fm:9000/AniSonFM(320)"),
    ANISON_STREAM_128("http://pool.anison.fm:9000/AniSonFM(128)"),

    //DATA,
    ANISON_META("http://anison.fm/status.php?widget=false");

    private String url;

    URLS(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
