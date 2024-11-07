package com.poc.photoeditor.provider.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import com.poc.photoeditor.R;

public class RulerView extends View implements GestureDetector.OnGestureListener {
    public interface OnDegreeChange {
        void onChanged(int degree);
    }

    public static final int MAX_DEGREES = 45;
    private GestureDetector mGestureDetector;

    private Paint paint;
    private Paint centerPaint;
    private Paint textPaint;

    private int maxNum;

    private int maxRulerWidth;
    private int maxRulerHeight;

    private int scaleSpace;
    private int scaleWidth;
    private int scaleHeight;
    private int centerWidth;
    private int centerHeight;
    private int periodWidth;
    private int periodHeight;
    private int textCenterMargin;
    private float radius;

    private int scaleHeightDiffer;

    private float textSize;
    private int textColor;

    private int fontHeight;

    private int currentNumber;

    private Scroller mScroller;
    private boolean forceScrolling = false;
    private OnDegreeChange rulerListener = null;

    public RulerView(Context context) {
        super(context);

        initView(context, null);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initView(context, attrs);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView(context, attrs);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initView(context, attrs);
    }

    public void setRulerListener(OnDegreeChange l) {
        this.rulerListener = l;
    }

    private void initView(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RulerView);
            scaleWidth = (int) typedArray.getDimension(R.styleable.RulerView_scaleWidth, 0);
            scaleHeight = (int) typedArray.getDimension(R.styleable.RulerView_scaleHeight, 0);
            scaleSpace = (int) typedArray.getDimension(R.styleable.RulerView_scaleSpace, 0);

            centerHeight = (int) typedArray.getDimension(R.styleable.RulerView_centerHeight, 0);
            centerWidth = (int) typedArray.getDimension(R.styleable.RulerView_centerWidth, 0);
            periodHeight = (int) typedArray.getDimension(R.styleable.RulerView_periodHeight, 0);
            periodWidth = (int) typedArray.getDimension(R.styleable.RulerView_periodWidth, 0);
            textCenterMargin = (int) typedArray.getDimension(R.styleable.RulerView_textCenterMargin, 0);

            maxNum = typedArray.getInt(R.styleable.RulerView_maxNumber, 0);
            textSize = typedArray.getDimension(R.styleable.RulerView_textSize, 10);
            textColor = typedArray.getColor(R.styleable.RulerView_textColor, 0);
            radius = typedArray.getDimension(R.styleable.RulerView_radius, 0);

            typedArray.recycle();
        }

        if (paint == null) {
            paint = new Paint();
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.parseColor("#C9EAFA"));
            paint.setTextSize(textSize);
            paint.setPathEffect(new CornerPathEffect(radius));
        }

        if (centerPaint == null) {
            centerPaint = new Paint();
            centerPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            centerPaint.setColor(Color.parseColor("#25B866"));
            centerPaint.setPathEffect(new CornerPathEffect(radius));
        }

        if (textPaint == null) {
            textPaint = new TextPaint();
            textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(textColor);
            textPaint.setTextSize(textSize);
        }

        if (mGestureDetector == null) {
            mGestureDetector = new GestureDetector(getContext(), this);
        }

        if (mScroller == null) {
            mScroller = new Scroller(getContext());
        }

        maxRulerWidth = (scaleSpace + scaleWidth) * maxNum;

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        fontHeight = (int) Math.abs(fontMetrics.bottom - fontMetrics.top);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

//        int width = 0;
//        int height = 0;
//
//        int minWidth = getMinimumWidth();
//        int minHeight = getMinimumHeight();
//
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//
//        if (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(widthMeasureSpec, 300);
//        } else {
//            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
//        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void drawRuler(Canvas canvas) {
        drawRulerBody(canvas);
        drawRulerTitle(canvas);
    }

    private void drawRulerBody(Canvas canvas) {
        Log.d("RulerView", "drawRulerBody");
        int start_y = fontHeight + textCenterMargin + centerHeight / 2 - periodHeight / 2;
        int currentX = getWidth() / 2;
        int centerX = getWidth() / 2;
        Log.d("RulerView", "drawRulerBody centerX=" + centerX);
        paint.setTextAlign(Paint.Align.CENTER);

        for (int i = 0; i <= maxNum; i++) {
            Rect rect;
            if (i % 10 == 0 || i == maxNum) {
                rect = new Rect(currentX, start_y, currentX + periodWidth, start_y + periodHeight);
            } else {
                rect = new Rect(currentX, start_y + periodHeight / 2 - (scaleHeight / 2), currentX + scaleWidth, start_y + periodHeight / 2 - (scaleHeight / 2) + scaleHeight);
            }

            canvas.drawRect(rect, paint);

            currentX += scaleSpace + scaleWidth;
        }

    }

    private void drawRulerTitle(Canvas canvas) {
        int drawDegree = currentNumber - MAX_DEGREES;
        if (rulerListener != null) {
            rulerListener.onChanged(drawDegree);
        }
        String numberStr = String.valueOf(drawDegree);

        int start_y = 0;
        int currentX = getWidth() / 2;
        float textWidth = textPaint.measureText(numberStr);
        centerPaint.setTextAlign(Paint.Align.CENTER);
        canvas.save();
        canvas.translate(getScrollX(), 0);
        canvas.drawText(numberStr, currentX - textWidth / 2, start_y + fontHeight, textPaint);

        Rect rect = new Rect(currentX - centerWidth / 2, start_y + fontHeight + textCenterMargin, currentX - centerWidth / 2 + centerWidth, start_y + fontHeight + textCenterMargin + centerHeight);
        canvas.drawRect(rect, centerPaint);

        canvas.restore();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        drawRuler(canvas);
        canvas.restore();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            int x = mScroller.getCurrX();

            int width = scaleSpace + scaleWidth;
            int mod = x % width;
            int modDx = width - mod;

            if (mod > 0) {
                x = x + modDx;
            }
            currentNumber = x / width;
            scrollTo(x,0);
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        scrollBy((int) distanceX, 0);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if (Math.abs(velocityX) > 1000) {

            mScroller.fling(getScrollX(), getScrollY(),(int)-velocityX,0,0,maxRulerWidth,0,0);
            Log.d("final X" , mScroller.getFinalX()+"");

            int x = mScroller.getFinalX();
            int width = scaleSpace + scaleWidth;
            int mod = Math.abs(x) % width;
            if (mod > 0) {
                if (velocityX<0) {
                    int modDx = width - mod;
                    x = x + modDx;
                } else {
                    x = x - mod;
                }
            }
            mScroller.setFinalX(x);

            invalidate();

            return true;
        }

        return false;
    }

    public void scrollToCenter() {
        if (currentNumber != MAX_DEGREES) {
            // reset ruler
            int currentWidth =  (currentNumber * maxRulerWidth) / maxNum;
            int rulerWidthCurrentGap = maxRulerWidth / 2  - currentWidth;
            if (rulerWidthCurrentGap < 0) {
                forceScrolling = true;
            }
            scrollBy(rulerWidthCurrentGap, 0);
        }
    }

    @Override
    public void scrollBy(int x, int y) {

        int dx;
        if (forceScrolling) {
            forceScrolling = false;
            dx = x;
        } else {
            dx = Math.abs(x);
        }
        int width = scaleSpace + scaleWidth;
        int mod = dx % width;
        int modDx = width - mod;

        if (mod > 0) {
            if (x > 0) {
                dx = x + modDx;
            } else if (x < 0) {
                dx = x - modDx;
            }
        }

        int temp = getScrollX() + dx;

        if (temp <= maxRulerWidth && temp >= 0) {
            currentNumber = temp / (scaleSpace + scaleWidth);
            super.scrollBy(dx, y);
        } else {
            Log.d("RulerView", String.valueOf(maxRulerWidth));
        }

    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            currentNumber = mScroller.getCurrX() / (scaleSpace + scaleWidth);
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }
}
