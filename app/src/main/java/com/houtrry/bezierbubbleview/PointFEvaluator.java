package com.houtrry.bezierbubbleview;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * Created by houtrry on 2017/9/21.
 */

public class PointFEvaluator implements TypeEvaluator<PointF> {

    private PointF mPointF = new PointF();

    @Override
    public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
        mPointF.set(startValue.x + (endValue.x - startValue.x) * fraction, startValue.y + (endValue.y - startValue.y) * fraction);
        return mPointF;
    }
}
