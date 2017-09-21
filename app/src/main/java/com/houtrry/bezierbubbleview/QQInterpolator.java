package com.houtrry.bezierbubbleview;

import android.view.animation.Interpolator;

/**
 * @author houtrry
 * @version $Rev$
 * @time 2017/9/20 19:20
 * @desc ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDesc $TODO$
 */

public class QQInterpolator implements Interpolator {

    /*
    factor = 0.4
pow(2, -10 * x) * sin((x - factor / 4) * (2 * PI) / factor) + 1
     */

    private float factor = 0.4f;

    public QQInterpolator() {
    }

    public QQInterpolator(float factor) {
        this.factor = factor;
    }

    @Override
    public float getInterpolation(float v) {
        return (float) (Math.pow(2, -10 * v) * Math.sin((v - factor * 0.25f) * (2 * Math.PI) / factor) + 1);
    }
}
