package com.vuforia.samples.VuforiaSamples.ui.my;

import android.widget.ProgressBar;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.vuforia.samples.VuforiaSamples.R;


/**
 * Created by Administrator on 2017/1/7.
 */

public class HealthPowerBar extends ProgressBar{
    private static final int DEFAULT_START_COLOR = 0xFFFF6632;
    private static final int DEFAULT_FINISH_COLOR = 0xFFFFca32;
    private static final int DEFAULT_PROGRESS_BAR_HEIGHT = 5;

    protected int mProgressBarHeight = dp2px(DEFAULT_PROGRESS_BAR_HEIGHT);
    protected int mInnerBorderInterval = dp2px(2);
    protected int mRealWidth;
    protected int mStartColor;
    protected int mFinishColor;
    protected int mBorderColor;

    public HealthPowerBar(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public HealthPowerBar(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        setHorizontalScrollBarEnabled(true);
        obtainStyledAttributes(attrs);
    }

    private void obtainStyledAttributes(AttributeSet attrs){
        final TypedArray attributes = getContext().obtainStyledAttributes(
                attrs, R.styleable.HealthPowerBar);
        mStartColor = attributes.getColor(R.styleable.HealthPowerBar_progress_start_color,
                DEFAULT_START_COLOR);
        mFinishColor = attributes.getColor(R.styleable.HealthPowerBar_progress_finish_color,
                DEFAULT_FINISH_COLOR);
        mBorderColor = attributes.getColor(R.styleable.HealthPowerBar_border_color,
                DEFAULT_FINISH_COLOR);
        mProgressBarHeight = (int)attributes.getDimension(R.styleable.HealthPowerBar_progress_bar_height,
                DEFAULT_PROGRESS_BAR_HEIGHT);
        mInnerBorderInterval = (int)attributes.getDimension(R.styleable.HealthPowerBar_inner_border_interval,
                DEFAULT_PROGRESS_BAR_HEIGHT - 1);

        attributes.recycle();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if(heightMode != MeasureSpec.EXACTLY){
            int exceptHeight = (int)(getPaddingBottom() + getPaddingTop() + mProgressBarHeight);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(exceptHeight,
                    MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        boolean drawLoaded = true;
        if(getProgress() == 0)
            drawLoaded = false;
        int rectWidth = mProgressBarHeight;
        int halfRectWidth = rectWidth / 2;
        int bottom = mProgressBarHeight - dp2px(1);
        canvas.save();
        canvas.translate(getPaddingLeft(), 0);
        Paint paint = new Paint();
        // 绘制轮廓
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(mBorderColor);
        paint.setStrokeWidth(dp2px(1));
        paint.setStyle(Paint.Style.STROKE);
        RectF rectF = new RectF(0, 1, rectWidth, bottom);
        canvas.drawArc(rectF, 90, 180, false, paint);
        canvas.drawLine(halfRectWidth, 1, mRealWidth - halfRectWidth - 1, 1, paint);
        canvas.drawLine(halfRectWidth, bottom, mRealWidth - halfRectWidth - 1, bottom, paint);
        rectF.set(mRealWidth - rectWidth, 1, mRealWidth - 1, bottom);
        canvas.drawArc(rectF, 270, 180, false, paint);
        if(drawLoaded) {
            // 绘制已加载部分
            int innerTop = mInnerBorderInterval;
            int innerBottom = bottom - mInnerBorderInterval;
            int innerLeft = mInnerBorderInterval;
            int loadedWidth = (int) ((mRealWidth - rectWidth - mInnerBorderInterval) * getProgress() * 1.0 / getMax());
            Path path = new Path();
            path.moveTo(halfRectWidth, innerTop);
            path.lineTo(loadedWidth + halfRectWidth, innerTop);
            rectF.set(loadedWidth, innerTop, loadedWidth + rectWidth - 1, innerBottom);
            path.addArc(rectF, 270, 180);
            path.lineTo(mProgressBarHeight, innerBottom);
            rectF.set(innerLeft, innerTop, rectWidth, innerBottom);
            path.arcTo(rectF, 90, 180);
            LinearGradient linearGradient = new LinearGradient(0, 0, loadedWidth + rectWidth, 0, mStartColor, mFinishColor, Shader.TileMode.CLAMP);
            paint.setShader(linearGradient);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(path, paint);
        }
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRealWidth = w - getPaddingRight() - getPaddingLeft();
    }

    protected int dp2px(int dpVal){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    public void setColor(int startColor, int finishColor){
        mStartColor = startColor;
        mFinishColor = finishColor;
        invalidate();
    }
}
