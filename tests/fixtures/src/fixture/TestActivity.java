package fixture;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import app.aimal.extension.subtitles.*;
import com.crunchyroll.subtitles.*;
import java.util.*;

/** Runs against the patched fixture APK, exercising injected DEX on Android. */
public final class TestActivity extends Activity {
    private static final String SCRIPT = "[Script Info]\nScriptType: v4.00+\n[Events]\nFormat: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n"
            + "Dialogue: 0,0:00:01.00,0:00:03.00,Default,,0,0,0,,Hello, world!\\NSecond line\n"
            + "Dialogue: 0,0:00:02.00,0:00:04.00,Default,,0,0,0,,Overlap\n";
    public static final class Cue { public final CharSequence renamedText; public Cue(String text) { renamedText = text; } }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF171A20); setContentView(root);
        TextView results = new TextView(this); results.setTextColor(Color.WHITE); root.addView(results);
        try {
            SubtitleSettings.reset(this);
            if (getPackageName().equals("com.crunchyroll.crunchyroid")) testCrunchyroll(); else testMedia3();
            testSchema();
            results.setText("PASS: injected hooks, timing, all 15 style controls, persistence, native fallback");
            Log.i("AimalFixture", results.getText().toString());
        } catch (Throwable error) {
            results.setText("FAIL: " + error); Log.e("AimalFixture", "FAIL", error);
        }
        try (java.io.OutputStream out = openFileOutput("fixture-result.txt", MODE_PRIVATE)) {
            out.write(results.getText().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception error) { Log.e("AimalFixture", "Cannot save test result", error); }
        if (getIntent().getBooleanExtra("fonts", false)) new Thread(() -> {
            String result;
            try {
                List<String> names = SubtitleFonts.loadCss(this, SubtitleFonts.DEFAULT_CSS);
                check(names.contains("GothamPro"), "GothamPro was not loaded");
                SubtitleStyle sample = new SubtitleStyle(); sample.fontFamily = "GothamPro";
                sample.fontSize = 68; sample.textShadow = false; sample.backgroundOpacity = 0;
                sample.fontWeight = 400; Bitmap regular = paint("Gotham real weights", sample);
                sample.fontWeight = 500; Bitmap medium = paint("Gotham real weights", sample);
                sample.fontWeight = 900; Bitmap black = paint("Gotham real weights", sample);
                check(opaquePixels(black) > opaquePixels(regular) * 1.3, "900 is not visibly thicker than 400");
                check(!equal(medium, regular) && !equal(black, medium), "Imported Gotham weights collapse to regular");
                check(SubtitleFonts.resolve("GothamPro", 900).getWeight() == 900, "Black face weight metadata missing");
                check(SubtitleFonts.resolve("GothamPro", 500).getWeight() == 500, "Medium face weight metadata missing");
                result = "PASS: all Gotham weights imported, true Medium/Black faces, 900 visibly thicker";
            } catch (Throwable error) { result = "FAIL: " + error; }
            try (java.io.OutputStream out = openFileOutput("font-result.txt", MODE_PRIVATE)) {
                out.write(result.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } catch (Exception ignored) { }
            final String message = result;
            runOnUiThread(() -> results.setText(message));
        }, "fixture-fonts").start();
        Button open = new Button(this); open.setText("Personalizar subtítulos");
        open.setOnClickListener(v -> SubtitlePanel.show(this)); root.addView(open);
        CaptionView live = new CaptionView(this); live.setText("Así se verán tus subtítulos.\nUna segunda línea de ejemplo.");
        SubtitleSettings.watch(live); root.addView(live, new LinearLayout.LayoutParams(-1, 250));
        if (getPackageName().equals("com.crunchyroll.crunchyroid")) {
            InternalPlayerView player = new InternalPlayerView(this);
            root.addView(player, new LinearLayout.LayoutParams(-1, 150));
            player.postDelayed(() -> {
                String result;
                try {
                    View row = player.findViewById(0x7f0a9990);
                    check(row != null, "Control row hook missing");
                    player.getViewTreeObserver().dispatchOnPreDraw();
                    check(row.getVisibility() == View.VISIBLE, "Visible controller did not reveal row");
                    player.controller.setVisibility(View.GONE);
                    player.getViewTreeObserver().dispatchOnPreDraw();
                    check(row.getVisibility() == View.GONE, "Hidden controller left translucent chips");
                    player.controller.setVisibility(View.VISIBLE);
                    player.getViewTreeObserver().dispatchOnPreDraw();
                    check(row.getVisibility() == View.VISIBLE, "Controller did not restore chips");
                    View aspect = ((android.view.ViewGroup)row).getChildAt(0);
                    check(aspect.isFocusable(), "D-pad focus unavailable");
                    // This fixture starts in touchscreen mode; allow programmatic focus
                    // before dispatching key events directly rather than through InputDispatcher.
                    aspect.setFocusableInTouchMode(true);
                    check(aspect.requestFocus(), "Could not focus fixture chip");
                    aspect.dispatchKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_CENTER));
                    aspect.dispatchKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_CENTER));
                    check(player.resizeMode == 3, "D-pad center did not activate aspect chip");
                    result = "PASS: controls fully hide, restore and activate with D-pad";
                } catch (Throwable error) { result = "FAIL: " + error; }
                try (java.io.OutputStream out = openFileOutput("controls-result.txt", MODE_PRIVATE)) {
                    out.write(result.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                } catch (Exception ignored) { }
            }, 300);
        }
        if (getIntent().getBooleanExtra("panel", false)) root.post(() -> SubtitlePanel.show(this));
    }

    private void testCrunchyroll() {
        SubtitlesRendererImpl renderer = new SubtitlesRendererImpl();
        check(renderer.loadTrack(SCRIPT) == 42, "Native handle changed");
        check(SCRIPT.equals(renderer.originalScript), "Native script changed");
        AssCaptionView view = new AssCaptionView(this); view.layout(0, 0, 1920, 1080);
        ((com.crunchyroll.subtitles.SubtitlesRenderer) renderer).renderFrame(42, 1500);
        check(renderer.nativeCalls == 1, "JNI renderer was renamed or bypassed");
        Bitmap first = render(view);
        check(first.getPixel(0, 0) != Color.RED && opaquePixels(first) > 0, "Crunchyroll draw hook missing");
        check(equal(first, paint("Hello, world!\nSecond line", SubtitleSettings.style(this))), "ASS render clock or text is wrong");
        long preloaded = renderer.loadTrack(SCRIPT.replace("Hello, world!", "Preloaded language"));
        renderer.renderFrame(42, 1500);
        check(equal(first, render(view)), "Preloaded language replaced the active native handle");
        renderer.destroy(preloaded);
        renderer.renderFrame(42, 1500);
        check(equal(first, render(view)), "Releasing another track cleared the current captions");
        SubtitleStyle style = SubtitleSettings.style(this); style.textColor = Color.GREEN;
        SubtitleSettings.changed(this);
        check(!equal(first, render(view)), "Paused frame did not hot reload");
        renderer.renderFrame(42, 4500);
        check(opaquePixels(render(view)) == 0, "Expired ASS cue persisted");
        renderer.renderFrame(42, 1500);
        style.enabled = false; SubtitleSettings.changed(this);
        check(render(view).getPixel(0, 0) == Color.RED, "Native ASS fallback missing");
        style.enabled = true; SubtitleSettings.changed(this);
        renderer.destroy(42);
        check(render(view).getPixel(0, 0) == Color.RED, "Released ASS track persisted");
    }

    private void testMedia3() {
        new FixturePlayer();
        SubtitleView view = new SubtitleView(this); view.layout(0, 0, 1920, 1080);
        view.setCues(Arrays.asList(new Cue("Hello, world!\nSecond line")));
        Bitmap first = render(view);
        check(first.getPixel(0, 0) != Color.RED && opaquePixels(first) > 0, "media3 draw hook missing");
        SubtitleStyle style = SubtitleSettings.style(this); style.textColor = Color.GREEN; SubtitleSettings.changed(this);
        check(!equal(first, render(view)), "media3 paused cue did not hot reload");
        view.setCues(Collections.emptyList()); check(opaquePixels(render(view)) == 0, "Cleared media3 cues persisted");
        view.setCues(Arrays.asList(new Object())); check(render(view).getPixel(0, 0) == Color.RED, "Bitmap fallback missing");
        style.enabled = false; SubtitleSettings.changed(this);
        check(render(view).getPixel(0, 0) == Color.RED, "Native media3 fallback missing");
    }

    private void testSchema() throws Exception {
        String text = "A subtitle with multiple words that wraps onto another line at a limited width.\nSecond line";
        List<java.util.function.Consumer<SubtitleStyle>> changes = Arrays.asList(
            s -> s.fontSize = 72, s -> s.textColor = Color.YELLOW, s -> s.backgroundColor = Color.BLUE,
            s -> s.backgroundOpacity = .9f, s -> s.bottomOffset = 35, s -> s.fontWeight = 900,
            s -> s.fontFamily = "monospace", s -> { s.useCustomMaxWidth = true; s.maxWidth = 15; },
            s -> { s.useCustomMaxWidth = true; s.maxWidth = 30; }, s -> s.paddingX = 60,
            s -> s.paddingY = 60, s -> s.borderRadius = 40, s -> s.lineHeight = 2,
            s -> s.letterSpacing = 8, s -> s.textShadow = false
        );
        for (int i = 0; i < changes.size(); i++) {
            SubtitleStyle base = new SubtitleStyle();
            Bitmap before = paint(text, base);
            changes.get(i).accept(base);
            check(!equal(before, paint(text, base)), "Style property " + i + " has no visual effect");
            base.save(this); SubtitleStyle restored = SubtitleStyle.read(this);
            check(equal(paint(text, base), paint(text, restored)), "Property " + i + " was not persisted");
        }
        AssTimeline timeline = new AssTimeline(SCRIPT);
        check(timeline.at(999).isEmpty(), "Cue started early");
        check(timeline.at(1000).equals("Hello, world!\nSecond line"), "ASS commas or line breaks lost");
        check(timeline.at(2000).endsWith("Overlap"), "Overlapping cues lost");
        check(timeline.at(3000).equals("Overlap"), "Cue end boundary wrong");
        check(timeline.at(1500).startsWith("Hello"), "Backward seek failed");
        AssTimeline reordered = new AssTimeline("[Events]\nFormat: End, Start, Text\n"
                + "Dialogue: 0:00:03.00,0:00:01.00,{\\b1}Bold, text{\\b0}\\NNext\n"
                + "Dialogue: invalid,invalid,Malformed\n"
                + "Dialogue: 0:00:03.00,0:00:01.00,{\\p1}m 0 0 l 1 1{\\p0}Visible\n");
        check(reordered.at(1500).equals("Bold, text\nNext\nVisible"), "Reordered columns, overrides or ASS drawings failed");
        SubtitleSettings.reset(this);
    }
    private Bitmap paint(String text, SubtitleStyle style) {
        Bitmap bitmap = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
        new CaptionPainter().draw(new Canvas(bitmap), 1920, 1080, text, style, 0); return bitmap;
    }
    private Bitmap render(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, 1920, 1080);
        Bitmap b = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888); view.draw(new Canvas(b)); return b;
    }
    private static int opaquePixels(Bitmap bitmap) { int[] pixels = pixels(bitmap); int count = 0; for (int pixel : pixels) if (Color.alpha(pixel) > 0) count++; return count; }
    private static boolean equal(Bitmap a, Bitmap b) { return Arrays.equals(pixels(a), pixels(b)); }
    private static int[] pixels(Bitmap b) { int[] values = new int[b.getWidth() * b.getHeight()]; b.getPixels(values, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight()); return values; }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
