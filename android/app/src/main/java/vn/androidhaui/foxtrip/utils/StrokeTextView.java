package vn.androidhaui.foxtrip.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class StrokeTextView extends AppCompatTextView {

    private boolean isStroke = true;
    private int strokeWidth = 8;
    private int strokeColor = Color.WHITE;

    public StrokeTextView(@NonNull Context context) {
        super(context);
    }

    public StrokeTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StrokeTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isStroke) {
            // Lưu lại các thuộc tính ban đầu
            int currentTextColor = getCurrentTextColor();
            TextPaint paint = getPaint();

            // Vẽ Stroke (Viền)
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
            setTextColor(strokeColor);
            super.onDraw(canvas);

            // Vẽ văn bản chính đè lên trên
            paint.setStyle(Paint.Style.FILL);
            setTextColor(currentTextColor);
        }
        super.onDraw(canvas);
    }

    public void setStroke(boolean stroke) {
        isStroke = stroke;
        invalidate();
    }

    public void setStrokeWidth(int width) {
        this.strokeWidth = width;
        invalidate();
    }

    public void setStrokeColor(int color) {
        this.strokeColor = color;
        invalidate();
    }
}
