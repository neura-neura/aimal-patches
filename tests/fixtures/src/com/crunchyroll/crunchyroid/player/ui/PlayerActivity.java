package com.crunchyroll.crunchyroid.player.ui;
import android.os.Bundle;
import android.view.KeyEvent;
public final class PlayerActivity extends fixture.TestActivity {
    private int nativeKeys;
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) nativeKeys++;
        return super.dispatchKeyEvent(event);
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().postDelayed(() -> {
            try {
                long time = android.os.SystemClock.uptimeMillis();
                dispatchKeyEvent(new KeyEvent(time, time, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER, 0));
                dispatchKeyEvent(new KeyEvent(time, time + 100, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER, 0));
                if (nativeKeys != 2) throw new AssertionError("Short OK not replayed exactly once: " + nativeKeys);
                dispatchKeyEvent(new KeyEvent(time + 200, time + 200, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER, 0));
                dispatchKeyEvent(new KeyEvent(time + 200, time + 1000, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER, 0));
                if (nativeKeys != 2) throw new AssertionError("Long OK leaked to native player");
                getWindow().getDecorView().postDelayed(() -> {
                    String result;
                    try {
                        java.lang.reflect.Field states = app.aimal.extension.crunchyroll.TvSubtitleHelper.class.getDeclaredField("presses");
                        states.setAccessible(true);
                        Object press = ((java.util.Map<?, ?>)states.get(null)).get(this);
                        java.lang.reflect.Field dialogField = press.getClass().getDeclaredField("dialog");
                        dialogField.setAccessible(true);
                        android.app.Dialog dialog = (android.app.Dialog)((java.lang.ref.WeakReference<?>)dialogField.get(press)).get();
                        if (dialog == null || !dialog.isShowing()) throw new AssertionError("Long OK did not open editor");
                        if (dialog.getWindow().getDecorView().findFocus() == null) throw new AssertionError("TV editor has no initial focus");
                        android.view.View focus = dialog.getWindow().getDecorView().findFocus();
                        if (!(focus instanceof android.widget.Button) || !((android.widget.Button)focus).getText().toString().equals("Cerrar")) throw new AssertionError("Initial focus did not reach Cerrar");
                        dialog.dismiss();
                        dispatchKeyEvent(new KeyEvent(time + 2000, time + 2000, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU, 0));
                        dispatchKeyEvent(new KeyEvent(time + 2000, time + 2100, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU, 0));
                        dialog = (android.app.Dialog)((java.lang.ref.WeakReference<?>)dialogField.get(press)).get();
                        if (dialog == null || !dialog.isShowing()) throw new AssertionError("Menu did not reopen editor");
                        result = "PASS: TV cue/draw hooks, short OK replay, long OK/Menu editor and initial D-pad focus";
                    } catch (Throwable error) { result = "FAIL: " + error; }
                    try (java.io.OutputStream out = openFileOutput("tv-result.txt", MODE_PRIVATE)) { out.write(result.getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
                    catch (Exception error) { throw new RuntimeException(error); }
                }, 500);
            } catch (Throwable error) {
                try (java.io.OutputStream out = openFileOutput("tv-result.txt", MODE_PRIVATE)) { out.write(("FAIL: " + error).getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
                catch (Exception ignored) { }
            }
        }, 500);
    }
}
