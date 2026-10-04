package com.crunchyroll.subtitles;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
public final class AssCaptionView extends View {
    public AssFrame frame;
    public AssCaptionView(Context context) { super(context); }
    @Override protected void onDraw(Canvas canvas) { canvas.drawColor(Color.RED); }
}
