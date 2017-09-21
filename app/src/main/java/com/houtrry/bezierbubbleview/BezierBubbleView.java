package com.houtrry.bezierbubbleview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * @author houtrry
 * @version $Rev$
 * @time 2017/9/20 19:43
 * @desc ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDesc $TODO$
 */

public class BezierBubbleView extends View {

    private static final String TAG = BezierBubbleView.class.getSimpleName();
    private PointF currentPointF = new PointF();
    private int mBubbleColor = Color.parseColor("#ffe91e63");
    private String mTextValue = String.valueOf(99);
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
    private float mSettledRadius;
    private PointF mCenterPoint = new PointF();
    private double mDistance;
    private BezierBubbleStatus mBezierBubbleStatus = BezierBubbleStatus.STATUS_IDLE;
    private Path mBezierPath = new Path();
    private Bitmap mCurrentDismissingBitmap = null;
    /**
     * 固定位置的圆, 最小半径(手指滑动到mCriticalDistance的时候), 与最大半径(尚未滑动的时候)的比
     * mMinSettledRadiusProportion = 最小半径/最大半径
     */
    private float mMinSettledRadiusProportion = 0.3f;
    /**
     * 贝塞尔存在的最大距离
     */
    private float mCriticalDistance = 0;
    /**
     * 保存贝塞尔曲线的5个关键点
     * mBezierPoints[0]: 控制点
     * mBezierPoints[1]: 固定圆的y值小的那个点
     * mBezierPoints[2]: 固定圆的y值大的那个点
     * mBezierPoints[3]: 移动圆的y值大的那个点
     * mBezierPoints[4]: 移动圆的y值大的那个点
     */
    private PointF[] mBezierPoints = {new PointF(), new PointF(), new PointF(), new PointF(), new PointF()};
    private ObjectAnimator mObjectAnimator;

    /**
     * 滑动距离是否曾超过mCriticalDistance
     * 如果超过, 即使滑动距离小于mCriticalDistance, 贝塞尔曲线也不再显示, 固定圆也不再显示
     */
    private boolean mHasBeyondCriticalDistance = false;
    private ObjectAnimator mDismissingObjectAnimator;

    public BezierBubbleView(Context context) {
        this(context, null);
    }

    public BezierBubbleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BezierBubbleView(Context context, AttributeSet attrs, int defStyle) {
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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mRadius = Math.min(mWidth, mHeight) * 0.5f;
        currentPointF.set(mRadius, mRadius);
        mCenterPoint.set(currentPointF);
        mSettledRadius = mRadius * 0.6f;
        mCriticalDistance = 6 * mRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBubble(canvas);
        drawText(canvas);
        drawDismissing(canvas);
    }

    private void drawBubble(Canvas canvas) {
        drawSettledCircle(canvas);
        drawMoveCircle(canvas);
        drawBezier(canvas);
    }

    /**
     * 画固定位置的圆
     *
     * @param canvas
     */
    private void drawSettledCircle(Canvas canvas) {
        if ((mBezierBubbleStatus == BezierBubbleStatus.STATUS_CONNECT || mBezierBubbleStatus == BezierBubbleStatus.STATUS_RECOVER) && !mHasBeyondCriticalDistance) {
            mSettledRadius = (float) ((mMinSettledRadiusProportion + (1 - mDistance / mCriticalDistance) * (1 - mMinSettledRadiusProportion)) * mRadius);
            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mSettledRadius, mPaint);
        }
    }

    /**
     * 画随手指移动的圆
     *
     * @param canvas
     */
    private void drawMoveCircle(Canvas canvas) {
        if (mBezierBubbleStatus != BezierBubbleStatus.STATUS_DISMISSED && mBezierBubbleStatus != BezierBubbleStatus.STATUS_DISMISSING) {
            canvas.drawCircle(currentPointF.x, currentPointF.y, mRadius, mPaint);
        }
    }

    /**
     * 画贝塞尔曲线
     *
     * @param canvas
     */
    private void drawBezier(Canvas canvas) {
        if ((mBezierBubbleStatus == BezierBubbleStatus.STATUS_CONNECT || mBezierBubbleStatus == BezierBubbleStatus.STATUS_RECOVER) && !mHasBeyondCriticalDistance) {
            mBezierPath.reset();
            calculateBezierPoints();
            mBezierPath.moveTo(mBezierPoints[1].x, mBezierPoints[1].y);
            mBezierPath.quadTo(mBezierPoints[0].x, mBezierPoints[0].y, mBezierPoints[3].x, mBezierPoints[3].y);
            mBezierPath.lineTo(mBezierPoints[4].x, mBezierPoints[4].y);
            mBezierPath.quadTo(mBezierPoints[0].x, mBezierPoints[0].y, mBezierPoints[2].x, mBezierPoints[2].y);
            mBezierPath.close();
            canvas.drawPath(mBezierPath, mPaint);
        }
    }

    private double mSinx = 0;
    private double mCosx = 0;
    private float mSixFixed = 1;
    /**
     * 控制点的位置, 从mCenterPoint到currentPointF的mControlPointProportion位置就是控制点的位置
     */
    private float mControlPointProportion = 0.25f;

    /**
     * 计算贝塞尔曲线五个关键点的位置
     */
    private void calculateBezierPoints() {
        mBezierPoints[0].x = mCenterPoint.x + (currentPointF.x - mCenterPoint.x) * mControlPointProportion;
        mBezierPoints[0].y = mCenterPoint.y + (currentPointF.y - mCenterPoint.y) * mControlPointProportion;

        mSinx = (mCenterPoint.x - currentPointF.x) / mDistance;
        mCosx = (mCenterPoint.y - currentPointF.y) / mDistance;

        mSixFixed = mSinx > 0 ? 1.0f : -1.0f;

        mBezierPoints[1].x = (float) (mCenterPoint.x + mSettledRadius * mCosx * mSixFixed);
        mBezierPoints[1].y = (float) (mCenterPoint.y - mSettledRadius * mSinx * mSixFixed);

        mBezierPoints[2].x = (float) (mCenterPoint.x - mSettledRadius * mCosx * mSixFixed);
        mBezierPoints[2].y = (float) (mCenterPoint.y + mSettledRadius * mSinx * mSixFixed);

        mBezierPoints[3].x = (float) (currentPointF.x + mRadius * mCosx * mSixFixed);
        mBezierPoints[3].y = (float) (currentPointF.y - mRadius * mSinx * mSixFixed);

        mBezierPoints[4].x = (float) (currentPointF.x - mRadius * mCosx * mSixFixed);
        mBezierPoints[4].y = (float) (currentPointF.y + mRadius * mSinx * mSixFixed);
    }

    /**
     * 画文字
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        if (mBezierBubbleStatus != BezierBubbleStatus.STATUS_DISMISSED && mBezierBubbleStatus != BezierBubbleStatus.STATUS_DISMISSING) {
            mTextPaint.getTextBounds(mTextValue, 0, mTextValue.length(), mTextRect);
            mTextX = currentPointF.x - mTextPaint.measureText(mTextValue) * 0.5f;
            mTextY = currentPointF.y + mTextRect.height() * 0.5f;
            canvas.drawText(String.valueOf(mTextValue), mTextX, mTextY, mTextPaint);
        }
    }

    /**
     * 画消失的动画
     *
     * @param canvas
     */
    private void drawDismissing(Canvas canvas) {
        if (mBezierBubbleStatus == BezierBubbleStatus.STATUS_DISMISSING) {
            canvas.drawBitmap(mCurrentDismissingBitmap, currentPointF.x - mCurrentDismissingBitmap.getWidth() * 0.5f, currentPointF.y - mCurrentDismissingBitmap.getHeight() * 0.5f, mPaint);
        }
    }

    private float currentDismissingProgress = 0.0f;
    private int[] mBitmapResources = {R.mipmap.icon_dismissing1, R.mipmap.icon_dismissing2, R.mipmap.icon_dismissing3, R.mipmap.icon_dismissing4, R.mipmap.icon_dismissing5};

    public void setCurrentDismissingProgress(float currentDismissingProgress) {
        this.currentDismissingProgress = currentDismissingProgress;
        mCurrentDismissingBitmap = BitmapFactory.decodeResource(getResources(), mBitmapResources[(int) currentDismissingProgress]);
        ViewCompat.postInvalidateOnAnimation(this);
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
                currentPointF.set(event.getX(), event.getY());

                mDistance = Math.hypot(currentPointF.x - mCenterPoint.x, currentPointF.y - mCenterPoint.y);

                if (mDistance > mCriticalDistance) {
                    mBezierBubbleStatus = BezierBubbleStatus.STATUS_DRAG;
                    mHasBeyondCriticalDistance = true;
                } else {
                    mBezierBubbleStatus = BezierBubbleStatus.STATUS_CONNECT;
                }

                ViewCompat.postInvalidateOnAnimation(this);
                break;
            }
            case MotionEvent.ACTION_UP: {
                getParent().requestDisallowInterceptTouchEvent(false);

                currentPointF.set(event.getX(), event.getY());

                mDistance = Math.hypot(currentPointF.x - mCenterPoint.x, currentPointF.y - mCenterPoint.y);
                mEndPointF.set(currentPointF);
                if (mDistance > mCriticalDistance) {
                    mBezierBubbleStatus = BezierBubbleStatus.STATUS_DISMISSING;
                    startDismissAnimate();
                } else {
                    mBezierBubbleStatus = BezierBubbleStatus.STATUS_RECOVER;
                    startRecoverAnimate(currentPointF);
                }
                break;
            }
        }
        return true;
    }

    /**
     * 开启消失的动画
     */
    private void startDismissAnimate() {
        if (mDismissingObjectAnimator != null && mDismissingObjectAnimator.isRunning()) {
            mDismissingObjectAnimator.cancel();
        }
        mDismissingObjectAnimator = ObjectAnimator.ofFloat(this, "currentDismissingProgress", 0.0f, 4.99f);
        mDismissingObjectAnimator.setInterpolator(new FastOutLinearInInterpolator());
        mDismissingObjectAnimator.addListener(mAnimatorListener);
        mDismissingObjectAnimator.setDuration(300);
        mDismissingObjectAnimator.start();
    }

    public void setCurrentPointF(PointF currentPointF) {
        this.currentPointF = currentPointF;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private PointF mEndPointF = new PointF();

    /**
     * 开始恢复原状态的动画
     *
     * @param pointF
     */
    private void startRecoverAnimate(PointF pointF) {
        if (mObjectAnimator != null && mObjectAnimator.isRunning()) {
            mObjectAnimator.cancel();
        }
        mObjectAnimator = ObjectAnimator.ofObject(this, "currentPointF", new PointFEvaluator(), pointF, mCenterPoint);
        mObjectAnimator.setDuration(500);
        mObjectAnimator.setInterpolator(new OvershootInterpolator(3));
        mObjectAnimator.addListener(mAnimatorListener);
        mObjectAnimator.start();
    }

    private Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (mBezierBubbleStatus == BezierBubbleStatus.STATUS_RECOVER) {
                mObjectAnimator.removeListener(this);
                recoverStatus();
            } else if (mBezierBubbleStatus == BezierBubbleStatus.STATUS_DISMISSING) {
                mDismissingObjectAnimator.removeListener(mAnimatorListener);
                dismissStatus();
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    private void dismissStatus() {
        mBezierBubbleStatus = BezierBubbleStatus.STATUS_DISMISSED;
        mCurrentDismissingBitmap = null;
        if (mBezierBubbleListener != null) {
            mBezierBubbleListener.dismissed(this);
        }
    }

    /**
     * 恢复状态
     */
    private void recoverStatus() {
        mBezierBubbleStatus = BezierBubbleStatus.STATUS_IDLE;
        mHasBeyondCriticalDistance = false;
    }

    public void setTextValue(int value) {
        mTextValue = String.valueOf(value);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @NonNull
    public void setTextValue(String valueStr) {
        mTextValue = valueStr;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private BezierBubbleListener mBezierBubbleListener;

    public void setBezierBubbleListener(BezierBubbleListener bezierBubbleListener) {
        mBezierBubbleListener = bezierBubbleListener;
    }

    /**
     * 获取当前气泡的状态
     *
     * @return
     */
    public BezierBubbleStatus getBezierBubbleStatus() {
        return mBezierBubbleStatus;
    }
}
