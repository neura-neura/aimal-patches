package fixture;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.widget.FrameLayout;
public final class SubtitleView extends FrameLayout {
    public SubtitleView(Context context) { super(context); }
    public void setApplyEmbeddedStyles(boolean enabled) { }
    public void setCues(java.util.List<?> cues) { }
    @Override protected void dispatchDraw(Canvas canvas) { canvas.drawColor(Color.RED); }
}
