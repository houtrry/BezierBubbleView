package com.houtrry.qqunreadmessageview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Scroller;

/**
 * @author houtrry
 * @version $Rev$
 * @time 2017/9/20 15:14
 * @desc ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDesc $TODO$
 */

public class QQUnreadMessageView extends View {

    private static final String TAG = QQUnreadMessageView.class.getSimpleName();
    private Paint mPaint;
    private PointF mCenterPoint = new PointF();
    private int mWidth;
    private int mHeight;
    private float mRadius;
    private String mTextValue = String.valueOf(1);
    private int mTextColor = Color.WHITE;
    private int mTextSize = 30;
    private int mBackgroundColor = Color.parseColor("#ffe91e63");
    private Paint mTextPaint;
    private float mTextX;
    private float mTextY;
    private Rect mTextRect = new Rect();
    private Scroller mScroller;


    public QQUnreadMessageView(Context context) {
        this(context, null);
    }

    public QQUnreadMessageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QQUnreadMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mBackgroundColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);


        mScroller = new Scroller(context, new FastOutLinearInInterpolator());

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mRadius = Math.min(mWidth, mHeight) * 0.5f;
        mCenterPoint.set(mWidth * 0.5f, mHeight * 0.5f);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawText(canvas);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mRadius, mPaint);
    }

    private void drawText(Canvas canvas) {
        mTextPaint.getTextBounds(mTextValue, 0, mTextValue.length(), mTextRect);
        mTextX = mCenterPoint.x - mTextPaint.measureText(mTextValue) * 0.5f;
        mTextY = mCenterPoint.y + mTextRect.height() * 0.5f;
        canvas.drawText(String.valueOf(mTextValue), mTextX, mTextY, mTextPaint);
    }

    private float mDownX = 0;
    private float mDownY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mDownX = event.getRawX();
                mDownY = event.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float x = event.getRawX();
                float y = event.getRawY();
                setTranslationX(x - mDownX);
                setTranslationY(y - mDownY);
                break;
            }
            case MotionEvent.ACTION_UP: {
                Log.d(TAG, "onTouchEvent: translation: (" + getTranslationX() + ", " + getTranslationY() + ")");
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    animate().setDuration(200).translationX(0).translationY(0)
                            .setInterpolator(new OvershootInterpolator(2)).start();
                }
                break;
            }
        }
        return true;
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            Log.d(TAG, "computeScroll: (" + mScroller.getCurrX() + ", " + mScroller.getCurrY() + ")");
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }
}
