package com.houtrry.bezierbubbleview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author houtrry
 * @version $Rev$
 * @time 2017/9/20 19:43
 * @desc 仿QQ未读消息的气泡
 * 注意: 父控件以及根控件要使用android:clipChildren="false", 否则, 无法正确显示.
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDesc $TODO$
 */

public class BezierBubbleView extends View {

    private static final String TAG = BezierBubbleView.class.getSimpleName();
    private PointF currentPointF = new PointF();//当前手指的点的坐标
    private int mBubbleColor = Color.parseColor("#ffe91e63");//气泡的颜色
    private String mTextValue = String.valueOf(99);//气泡的文字内容
    private int mTextColor = Color.WHITE;//文字的颜色
    private int mTextSize = 30;//文字的大小
    private int mWidth;//当前控件的宽
    private int mHeight;//当前控件的高
    private Paint mPaint;
    private Paint mTextPaint;
    private Rect mTextRect = new Rect();
    private float mTextX;//绘制文字时, 文字左下角的x值
    private float mTextY;//绘制文字时, 文字左下角的y值
    private float mRadius;//移动圆的半径
    private float mSettledRadius;//固定圆的半径
    private PointF mCenterPoint = new PointF();//固定圆的圆心, 也是当前控件的中心点
    private double mDistance;//当前位置到mCenterPoint的距离
    private int mBezierBubbleStatus = STATUS_IDLE;//当前气泡的状态
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
    private ObjectAnimator mRecoverObjectAnimator;

    /**
     * 滑动距离是否曾超过mCriticalDistance
     * 如果超过, 即使滑动距离小于mCriticalDistance, 贝塞尔曲线也不再显示, 固定圆也不再显示
     */
    private boolean mHasBeyondCriticalDistance = false;
    private ObjectAnimator mDismissingObjectAnimator;
    /**
     * 最大距离(mCriticalDistance)是半径的多少倍
     */
    private float mCriticalDistanceMultipleRadius = 6.0f;

    private float currentDismissingProgress = 0.0f;//当前消失动画的进度
    private int[] mBitmapResources = {R.mipmap.icon_dismissing1, R.mipmap.icon_dismissing2, R.mipmap.icon_dismissing3, R.mipmap.icon_dismissing4, R.mipmap.icon_dismissing5};


    public BezierBubbleView(Context context) {
        this(context, null);
    }

    public BezierBubbleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BezierBubbleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttributeSet(context, attrs);
        init(context, attrs);
    }

    private void initAttributeSet(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BezierBubbleView);
        mBubbleColor = typedArray.getColor(R.styleable.BezierBubbleView_bubble_color, mBubbleColor);
        mTextValue = typedArray.getString(R.styleable.BezierBubbleView_bubble_text);
        mTextColor = typedArray.getColor(R.styleable.BezierBubbleView_text_color, mTextColor);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.BezierBubbleView_text_size, mTextSize);
        mRadius = typedArray.getDimensionPixelSize(R.styleable.BezierBubbleView_circle_radius, 0);
        mBubblePadding = typedArray.getDimensionPixelSize(R.styleable.BezierBubbleView_bubble_padding, mBubblePadding);
        mMinSettledRadiusProportion = typedArray.getFloat(R.styleable.BezierBubbleView_min_settled_radius_proportion, mMinSettledRadiusProportion);
        mCriticalDistanceMultipleRadius = typedArray.getFloat(R.styleable.BezierBubbleView_critical_distance_multiple_radius, 0);
        mCriticalDistance = typedArray.getFloat(R.styleable.BezierBubbleView_critical_distance, 0);
        if (mCriticalDistance == 0) {
            if (mCriticalDistanceMultipleRadius == 0) {
                mCriticalDistanceMultipleRadius = 6.0f;
            }
            mCriticalDistance = mCriticalDistanceMultipleRadius * mRadius;
        }
        mControlPointProportion = typedArray.getFloat(R.styleable.BezierBubbleView_control_point_proportion, mControlPointProportion);
        typedArray.recycle();
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
        if (mRadius == 0) {
            mRadius = Math.max(mWidth, mHeight) * 0.5f;
        }
        currentPointF.set(mRadius, mRadius);
        mCenterPoint.set(currentPointF);
        mCriticalDistance = mCriticalDistanceMultipleRadius * mRadius;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int mBubblePadding = 10;

    private int mWidthSize;
    private int mWidthMode;
    private int mHeightSize;
    private int mHeightMode;

    private int measureWidth(int widthMeasureSpec) {
        int result = 0;
        mWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        mWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (mWidthMode == MeasureSpec.EXACTLY) {
            result = mWidthSize;
        } else {
            final float measureTextWidth = mTextPaint.measureText(mTextValue);
            result = (int) (measureTextWidth + mBubblePadding * 2.0f);
        }
        return result;
    }

    private int measureHeight(int heightMeasureSpec) {
        int result = 0;
        mHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        mHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (mHeightMode == MeasureSpec.EXACTLY) {
            result = mHeightSize;
        } else {
            mTextPaint.getTextBounds(mTextValue, 0, mTextValue.length(), mTextRect);
            result = mTextRect.height() + mBubblePadding * 2;
        }
        return result;
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
        if ((mBezierBubbleStatus == STATUS_CONNECT || mBezierBubbleStatus == STATUS_RECOVER) && !mHasBeyondCriticalDistance) {
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
        if (mBezierBubbleStatus != STATUS_DISMISSED && mBezierBubbleStatus != STATUS_DISMISSING) {
            canvas.drawCircle(currentPointF.x, currentPointF.y, mRadius, mPaint);
        }
    }

    /**
     * 画贝塞尔曲线
     *
     * @param canvas
     */
    private void drawBezier(Canvas canvas) {
        if ((mBezierBubbleStatus == STATUS_CONNECT || mBezierBubbleStatus == STATUS_RECOVER) && !mHasBeyondCriticalDistance) {
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
        if (mBezierBubbleStatus != STATUS_DISMISSED && mBezierBubbleStatus != STATUS_DISMISSING) {
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
        if (mBezierBubbleStatus == STATUS_DISMISSING) {
            canvas.drawBitmap(mCurrentDismissingBitmap, currentPointF.x - mCurrentDismissingBitmap.getWidth() * 0.5f, currentPointF.y - mCurrentDismissingBitmap.getHeight() * 0.5f, mPaint);
        }
    }

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
                if (mBezierBubbleStatus != STATUS_DISMISSED) {
                    currentPointF.set(event.getX(), event.getY());

                    mDistance = Math.hypot(currentPointF.x - mCenterPoint.x, currentPointF.y - mCenterPoint.y);

                    if (mDistance > mCriticalDistance) {
                        mBezierBubbleStatus = STATUS_DRAG;
                        mHasBeyondCriticalDistance = true;
                    } else {
                        mBezierBubbleStatus = STATUS_CONNECT;
                    }

                    ViewCompat.postInvalidateOnAnimation(this);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mBezierBubbleStatus != STATUS_DISMISSED) {
                    getParent().requestDisallowInterceptTouchEvent(false);

                    currentPointF.set(event.getX(), event.getY());

                    mDistance = Math.hypot(currentPointF.x - mCenterPoint.x, currentPointF.y - mCenterPoint.y);
                    if (mDistance > mCriticalDistance) {
                        mBezierBubbleStatus = STATUS_DISMISSING;
                        startDismissAnimate();
                    } else {
                        mBezierBubbleStatus = STATUS_RECOVER;
                        startRecoverAnimate(currentPointF);
                    }
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

    /**
     * 开始恢复原状态的动画
     *
     * @param pointF
     */
    private void startRecoverAnimate(PointF pointF) {
        if (mRecoverObjectAnimator != null && mRecoverObjectAnimator.isRunning()) {
            mRecoverObjectAnimator.cancel();
        }
        mRecoverObjectAnimator = ObjectAnimator.ofObject(this, "currentPointF", new PointFEvaluator(), pointF, mCenterPoint);
        mRecoverObjectAnimator.setDuration(500);
        mRecoverObjectAnimator.setInterpolator(new OvershootInterpolator(3));
        mRecoverObjectAnimator.addListener(mAnimatorListener);
        mRecoverObjectAnimator.start();
    }

    private Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (mBezierBubbleStatus == STATUS_RECOVER) {
                mRecoverObjectAnimator.removeListener(this);
                recoverStatus();
            } else if (mBezierBubbleStatus == STATUS_DISMISSING) {
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
        mBezierBubbleStatus = STATUS_DISMISSED;
        mCurrentDismissingBitmap = null;
        if (mBezierBubbleListener != null) {
            mBezierBubbleListener.dismissed(this);
        }
    }

    /**
     * 恢复状态
     */
    private void recoverStatus() {
        mBezierBubbleStatus = STATUS_IDLE;
        mHasBeyondCriticalDistance = false;
    }

    public void setTextValue(int value) {
        mTextValue = String.valueOf(value);
        mDownX = mDownY = 0;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @NonNull
    public void setTextValue(String valueStr) {
        mTextValue = valueStr;
        recoverStatus();
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
    @BezierBubbleStatus
    public int getBezierBubbleStatus() {
        return mBezierBubbleStatus;
    }

    private static final int STATUS_DISMISSED = 0x00000000;//消失状态, 已经消失
    private static final int STATUS_CONNECT = 0x00000001;//拖动, 随手指移动, 有贝塞尔曲线
    private static final int STATUS_DRAG = 0x00000002;//拖动, 随手指移动, 无贝塞尔曲线
    private static final int STATUS_RECOVER = 0x00000003;//手指松开, 正在恢复初始化状态
    private static final int STATUS_DISMISSING = 0x00000004;//手指松开, 正在消失
    private static final int STATUS_IDLE = 0x00000005;//空闲状态

    @IntDef({STATUS_DISMISSED, STATUS_CONNECT, STATUS_DRAG, STATUS_RECOVER, STATUS_DISMISSING, STATUS_IDLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BezierBubbleStatus {
    }
}
