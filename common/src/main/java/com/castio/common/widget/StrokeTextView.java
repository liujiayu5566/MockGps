package com.castio.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import com.castio.common.utils.DensityUtils;


/**
 * 中英文混排自动换行描边 描边
 * 需要设置width或者maxWidth才能做到适配
 *
 * @author jiayu.liu
 */
public class StrokeTextView extends MixedTextView {
    public TextView borderText;
    private final int mWidth;

    public StrokeTextView(Context context) {
        this(context, null);
    }

    public StrokeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StrokeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mWidth = DensityUtils.dp2px(context, 2);
        borderText = new TextView(context, attrs);
        init();
    }

    public void init() {
        TextPaint tp1 = borderText.getPaint();
        tp1.setStrokeWidth(mWidth);
        tp1.setStyle(Paint.Style.STROKE);
        borderText.setTextColor(Color.WHITE);
        borderText.setGravity(getGravity());
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
        borderText.setLayoutParams(params);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        CharSequence tt = borderText.getText();
        if (tt == null || !tt.equals(this.getText())) {
            borderText.setText(getText());
            this.postInvalidate();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        borderText.measure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        borderText.layout(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        borderText.draw(canvas);
        super.onDraw(canvas);
    }

}
