package app.in.lafolle.musendrid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

/**
 * Created by lafolle on 28/11/14.
 */
public class TouchPad extends View {

    private int screen_width;
    private int screen_height;

    public TouchPad(Context context, int width, int height) {
        super(context);
        screen_height = height;
        screen_width = width;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(VIEW_LOG_TAG, "onDraw called");

        Paint paint = new Paint();
        paint.setColor(Color.rgb(54, 115, 136));
        canvas.drawRect(0, 0, screen_width, screen_height, paint);
        super.onDraw(canvas);
    }
}
