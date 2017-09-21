package com.houtrry.bezierbubbleview;

import android.view.animation.Interpolator;

/**
 * @author houtrry
 * @version $Rev$
 * @time 2017/9/20 19:20
 * @desc 可以通过网站(http://inloop.github.io/interpolator/)查看不同的曲线的效果
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDesc $TODO$
 */

public class BezierBubbleInterpolator implements Interpolator {

    /**
    factor = 0.4
pow(2, -10 * x) * sin((x - factor / 4) * (2 * PI) / factor) + 1
     */

    private float factor = 0.4f;

    public BezierBubbleInterpolator() {
    }

    public BezierBubbleInterpolator(float factor) {
        this.factor = factor;
    }

    @Override
    public float getInterpolation(float v) {
        return (float) (Math.pow(2, -10 * v) * Math.sin((v - factor * 0.25f) * (2 * Math.PI) / factor) + 1);
    }
}
