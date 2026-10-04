package fixture;
import android.content.Context;
import android.widget.FrameLayout;
public final class InternalPlayerView extends FrameLayout {
    public InternalPlayerView(Context context) { super(context); }
    public void setupAdView(boolean enabled) { android.util.Log.d("fixture", "getAdViewGroup(...)"); }
    @Override public final void onAttachedToWindow() { super.onAttachedToWindow(); }
    public void setResizeMode(int mode) { }
}
