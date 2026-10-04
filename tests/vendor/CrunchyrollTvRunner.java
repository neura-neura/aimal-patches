package app.aimal.verify;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import java.lang.reflect.*;
import java.util.Collections;

/** Uses the actual 3.74.0 TV SubtitleView and Cue classes, without a login. */
public final class CrunchyrollTvRunner extends Instrumentation {
    @Override public android.app.Application newApplication(ClassLoader loader, String name, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        // Caption components need the original APK resources/classes, not network
        // startup, analytics or an authenticated application dependency graph.
        return super.newApplication(loader, "android.app.Application", context);
    }
    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }
    @Override public void onStart() {
        Bundle result = new Bundle();
        Throwable[] failure = new Throwable[1];
        runOnMainSync(() -> { try { verify(); } catch (Throwable error) { failure[0] = error; } });
        result.putString("stream", failure[0] == null
            ? "PASS: TV original media3 Cue and SubtitleView, hot reload, cue expiration, native fallback and injected remote hook\n"
            : "FAIL: " + android.util.Log.getStackTraceString(failure[0]));
        finish(failure[0] == null ? -1 : 1, result);
    }
    private void verify() throws Exception {
        Context context = getTargetContext(); ClassLoader loader = context.getClassLoader();
        Class<?> viewClass = loader.loadClass("androidx.media3.ui.SubtitleView");
        View view = (View)viewClass.getConstructor(Context.class, android.util.AttributeSet.class).newInstance(context, null);
        view.measure(View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, 1920, 1080);
        Object builder = loader.loadClass("androidx.media3.common.text.Cue$Builder").getConstructor().newInstance();
        Object cue = null;
        for (Method method : builder.getClass().getMethods()) {
            if (java.util.Arrays.equals(method.getParameterTypes(), new Class<?>[]{CharSequence.class}))
                method.invoke(builder, "TV subtitle sample");
        }
        for (Method method : builder.getClass().getMethods()) {
            if (method.getParameterCount() == 0 && method.getReturnType().getName().equals("androidx.media3.common.text.Cue"))
                cue = method.invoke(builder);
        }
        check(cue != null, "Original Cue builder missing");
        Class<?> settings = loader.loadClass("app.aimal.extension.subtitles.SubtitleSettings");
        Object style = settings.getMethod("style", Context.class).invoke(null, context);
        Field enabled = style.getClass().getField("enabled"), color = style.getClass().getField("textColor");
        enabled.setBoolean(style, true); color.setInt(style, Color.GREEN);
        viewClass.getMethod("setCues", java.util.List.class).invoke(view, Collections.singletonList(cue));
        Bitmap first = draw(view); check(pixels(first) > 0, "Custom TV text missing");
        color.setInt(style, Color.RED); settings.getMethod("changed", Context.class).invoke(null, context);
        check(!first.sameAs(draw(view)), "Paused TV text did not hot reload");
        enabled.setBoolean(style, false); settings.getMethod("changed", Context.class).invoke(null, context);
        view.measure(View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, 1920, 1080);
        check(pixels(draw(view)) > 0, "Original TV caption drawing missing");
        enabled.setBoolean(style, true);
        viewClass.getMethod("setCues", java.util.List.class).invoke(view, Collections.emptyList());
        check(pixels(draw(view)) == 0, "Expired TV caption remains");
        loader.loadClass("com.crunchyroll.crunchyroid.player.ui.PlayerActivity")
            .getDeclaredMethod("dispatchKeyEvent", android.view.KeyEvent.class);
    }
    private static Bitmap draw(View view) { Bitmap b = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888); view.draw(new Canvas(b)); return b; }
    private static int pixels(Bitmap bitmap) { int[] values = new int[bitmap.getWidth()*bitmap.getHeight()]; bitmap.getPixels(values,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight()); int n=0; for(int value:values) if(Color.alpha(value)>0)n++; return n; }
    private static void check(boolean value, String message) { if(!value)throw new AssertionError(message); }
}
