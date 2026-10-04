package app.aimal.extension.crunchyroll;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import app.aimal.extension.subtitles.*;
import java.util.WeakHashMap;

/** Keeps native tracks intact; uses their exact render clock for the custom text. */
public final class SubtitleStyler {
    private static final WeakHashMap<Object, Track> tracks = new WeakHashMap<>();
    private static final WeakHashMap<View, CaptionPainter> painters = new WeakHashMap<>();
    private static volatile Track current;
    private static final class Track {
        final AssTimeline timeline; volatile String text = "";
        Track(String script) { timeline = new AssTimeline(script); }
    }
    private SubtitleStyler() { }
    static void init(Context context) { SubtitleFonts.init(context); SubtitleSettings.style(context); }
    public static String capture(Object renderer, String script) {
        try {
            synchronized (tracks) {
                Track track = new Track(script == null ? "" : script);
                tracks.put(renderer, track); current = track;
            }
        } catch (Throwable error) { Log.e("AimalSubtitles", "Cannot parse ASS track", error); current = null; }
        return script;
    }
    public static void onRender(Object renderer, long milliseconds) {
        synchronized (tracks) {
            Track track = tracks.get(renderer);
            if (track == null) { current = null; return; }
            track.text = track.timeline.at(milliseconds); current = track;
        }
    }
    public static void clear(Object renderer) {
        synchronized (tracks) { Track old = tracks.remove(renderer); if (old == current) current = null; }
    }
    public static boolean draw(View view, Canvas canvas) {
        try {
            init(view.getContext()); SubtitleSettings.watch(view);
            if (!SubtitleSettings.style(view.getContext()).enabled) return false;
            Track track = current;
            if (track == null || track.timeline.isEmpty()) return false;
            CaptionPainter painter = painters.get(view);
            if (painter == null) { painter = new CaptionPainter(); painters.put(view, painter); }
            painter.draw(canvas, view.getWidth(), view.getHeight(), track.text, SubtitleSettings.style(view.getContext()), SubtitleSettings.revision());
            return true;
        } catch (Throwable error) { Log.e("AimalSubtitles", "ASS caption draw failed", error); return false; }
    }
}
