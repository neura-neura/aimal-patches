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
                    String result = getWindow().getDecorView().hasWindowFocus() ? "FAIL: long OK did not open editor" : "PASS: TV injected cue/draw hooks, normal OK replay and long OK editor";
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
