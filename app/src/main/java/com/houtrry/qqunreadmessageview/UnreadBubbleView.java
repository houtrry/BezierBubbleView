package com.houtrry.qqunreadmessageview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author houtrry
 * @version $Rev$
 * @time 2017/9/20 19:43
 * @desc ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDesc $TODO$
 */

public class UnreadBubbleView extends View {

    private static final String TAG = UnreadBubbleView.class.getSimpleName();
    private float mCurrentY;
    private float mCurrentX;
    private int mBubbleColor = Color.parseColor("#ffe91e63");
    private String mTextValue = String.valueOf(1);
    private int mTextColor = Color.WHITE;
    private int mTextSize = 30;
    private int mWidth;
    private int mHeight;
    private Paint mPaint;
    private Paint mTextPaint;
    private Rect mTextRect = new Rect();
    private float mTextX;
    private float mTextY;
    private float mRadius;
    private PointF mCenterPoint = new PointF();

    public UnreadBubbleView(Context context) {
        this(context, null);
    }

    public UnreadBubbleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UnreadBubbleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mBubbleColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);


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

        mCurrentX = mRadius;
        mCurrentY = mRadius;

        mCenterPoint.set(mCurrentX, mCurrentY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw: ("+mCurrentX+", "+mCurrentY+")");
        drawBubble(canvas);
        drawText(canvas);
    }

    private void drawBubble(Canvas canvas) {
        Log.d(TAG, "drawBubble: ("+mCurrentX+", "+mCurrentY+"), "+mRadius+", ");
        canvas.drawCircle(mCurrentX, mCurrentY, mRadius, mPaint);
    }

    private void drawText(Canvas canvas) {
        mTextPaint.getTextBounds(mTextValue, 0, mTextValue.length(), mTextRect);
        mTextX = mCurrentX - mTextPaint.measureText(mTextValue) * 0.5f;
        mTextY = mCurrentY + mTextRect.height() * 0.5f;
        canvas.drawText(String.valueOf(mTextValue), mTextX, mTextY, mTextPaint);
    }

    private float mDownX = 0;
    private float mDownY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                getParent().requestDisallowInterceptTouchEvent(true);
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                mCurrentX = event.getX();
                mCurrentY = event.getY();

                double distance = Math.hypot(mCurrentX - mCenterPoint.x, mCurrentY - mCenterPoint.y);

                if (distance > 2 * mRadius) {

                } else {
                    
                }

                ViewCompat.postInvalidateOnAnimation(this);
                break;
            }
            case MotionEvent.ACTION_UP: {
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            }
        }
        return true;
    }
}
