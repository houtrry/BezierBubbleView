package com.houtrry.bezierbubbleview;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * Created by houtrry on 2017/9/21.
 * SDK里面有PointFEvaluator, 但是minsdk需要21, 因此, 重写一个
 */

public class PointFEvaluator implements TypeEvaluator<PointF> {

    private PointF mPointF = new PointF();

    @Override
    public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
        mPointF.set(startValue.x + (endValue.x - startValue.x) * fraction, startValue.y + (endValue.y - startValue.y) * fraction);
        return mPointF;
    }
}
