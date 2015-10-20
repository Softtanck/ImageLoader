package com.softtanck.imageloader.imageloader.anim;

import android.view.View;

import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.softtanck.imageloader.imageloader.listener.AnimListener;

/**
 * @author : Tanck
 * @Description : TODO 加载动画核心
 * @date 10/20/2015
 */
public class LoadAnimCore implements ValueAnimator.AnimatorUpdateListener {

    protected ValueAnimator valueAnimator;


    private View view;


    public LoadAnimCore(View view) {
        this.view = view;
        initCore();
    }

    /**
     * 初始化设置
     */
    private void initCore() {
        valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimator.addUpdateListener(this);
        valueAnimator.setDuration(500);
        valueAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float currentValue = (float) animation.getAnimatedValue();
        ViewHelper.setAlpha(view, currentValue);
        currentValue = 2 * currentValue;
        currentValue = currentValue > 1.0f ? 1.0f : currentValue;
        ViewHelper.setScaleX(view, currentValue);
        ViewHelper.setScaleY(view, currentValue);
    }


}
