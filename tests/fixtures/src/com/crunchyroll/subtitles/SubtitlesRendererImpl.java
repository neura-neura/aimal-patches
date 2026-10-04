package com.crunchyroll.subtitles;
/** Shape fixture, not a replacement for testing the vendor's APK. */
public final class SubtitlesRendererImpl {
    public String originalScript;
    private long nextHandle = 42;
    public long loadTrack(String script) { originalScript = script; return nextHandle++; }
    public AssFrame renderFrame(long track, long milliseconds) { return new AssFrame(milliseconds); }
    public void releaseTrack(long track) { originalScript = null; }
}
