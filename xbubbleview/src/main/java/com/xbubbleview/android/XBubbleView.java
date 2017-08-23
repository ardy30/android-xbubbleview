package com.xbubbleview.android;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Random;

public class XBubbleView extends RelativeLayout {

    private static final int DEFAULT_COUNT = 5;
    private static final int DEFAULT_CHILD_DIMENS_DP = 20;
    private static final int DEFAULT_MAX_DEGREE = 15;

    private static final long DURATION_TOTAL_ANIMATE = 1500;
    private static final long DURATION_SCALE_ANIMATE = 800;
    private static final long DURATION_ALPHA_ANIMATE = 300;

    private int mCount;
    private int mChildDimens;
    private int mMaxDegree;

    private boolean isInitialed;
    private boolean isMeasured;
    private boolean shouldStartAnimation;
    private boolean isAnimating;

    private int[] mImageResources;

    private Random mRandom = new Random();

    public XBubbleView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public XBubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public XBubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XBubbleView, defStyleAttr, 0);
        int count = a.getInteger(R.styleable.XBubbleView_xbv_count, DEFAULT_COUNT);
        mChildDimens = a.getDimensionPixelOffset(R.styleable.XBubbleView_xbv_childDimens,
                dp2px(DEFAULT_CHILD_DIMENS_DP));
        mMaxDegree = a.getInteger(R.styleable.XBubbleView_xbv_maxDegree, DEFAULT_MAX_DEGREE);
        a.recycle();

        setCount(count);

        // 获取到真实的高度
        post(new Runnable() {

            @Override
            public void run() {
                isMeasured = true;
                if (shouldStartAnimation) {
                    startAnimation();
                }
            }
        });
    }

    public void setCount(int count) {
        boolean needRefresh = (this.mCount != count);
        this.mCount = (count < 0 ? DEFAULT_COUNT : count);
        if (needRefresh) {
            clearAnimations();
            initChildViews();
            if (isAnimating) {
                startAnimation();
            }
        }
    }

    private void clearAnimations() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.clearAnimation();
        }
    }

    private void initChildViews() {
        removeAllViews();
        for (int i = 0; i < mCount; i++) {
            ImageView child = new ImageView(getContext());
            child.setScaleType(ImageView.ScaleType.FIT_CENTER);
            child.setVisibility(View.GONE);
            if (mImageResources != null && mImageResources.length > 0) {
                child.setImageResource(mImageResources[i % mImageResources.length]);
            }

            LayoutParams lp = new LayoutParams(mChildDimens, mChildDimens);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            addView(child, lp);
        }
    }

    public void setImageResources(int[] resources) {
        mImageResources = resources;
        setupChildResources();
    }

    private void setupChildResources() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ImageView) {
                if (mImageResources != null && mImageResources.length > 0) {
                    ((ImageView) child).setImageResource(mImageResources[i % mImageResources.length]);
                } else {
                    ((ImageView) child).setImageBitmap(null);
                }
            }
        }
    }

    public void startAnimation() {
        if (!isMeasured) { // 控件还没有绘制完成
            shouldStartAnimation = true;
            return;
        }

        isAnimating = true;
        isInitialed = true;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.setVisibility(View.VISIBLE);
            startChildAnimation(i, child);
        }
    }

    public void stopAnimation() {
        isAnimating = false;
    }

    private void startChildAnimation(final int index, final View view) {
        final float degrees;
        // 随机角度，前4个保证左右两边都有
        if (index < 4 && index < getChildCount() - 1) {
            int factor = index % 2 == 0 ? 1 : -1;
            degrees = factor * mRandom.nextFloat() * mMaxDegree;
        } else {
            degrees = mMaxDegree - mRandom.nextFloat() * mMaxDegree * 2;
        }

        Random random = new Random();
        int startOffset = 0;
        if (isInitialed) {
            if (index > 0) {
                startOffset = random.nextInt(300) + index * 300;
            }
            if (index == getChildCount() - 1) {
                isInitialed = false;
            }
        }

        Animation sAnim = createScaleAnim();
        sAnim.setStartOffset(startOffset);

        Animation tAnim = createTranslateAnim(degrees);
        tAnim.setDuration(DURATION_TOTAL_ANIMATE);
        tAnim.setStartOffset(startOffset);

        Animation aAnim = createAlphaAnim();
        aAnim.setStartOffset(DURATION_TOTAL_ANIMATE - DURATION_ALPHA_ANIMATE + startOffset);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(sAnim);
        animationSet.addAnimation(tAnim);
        animationSet.addAnimation(aAnim);
        animationSet.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
                view.setAlpha(1.f);
                view.setRotation(degrees);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isAnimating) {
                    startChildAnimation(index, view);
                } else {
                    view.clearAnimation();
                    view.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.setAnimation(animationSet);
        view.startAnimation(animationSet);
    }

    private Animation createScaleAnim() {
        Animation anim = new ScaleAnimation(0f, 1.0f, 0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
        anim.setDuration(DURATION_SCALE_ANIMATE);
        anim.setFillAfter(true);
        return anim;
    }

    private Animation createTranslateAnim(float degrees) {
        float disX = (float) (Math.tan(degrees * Math.PI / 180) * 100);
        float disY = getHeight() - mChildDimens;

        Animation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, disX, Animation.RELATIVE_TO_PARENT,
                0f, Animation.ABSOLUTE, -disY);
        anim.start();
        return anim;
    }

    private Animation createAlphaAnim() {
        Animation anim = new AlphaAnimation(1.0f, 0.2f);
        anim.setDuration(DURATION_ALPHA_ANIMATE);
        return anim;
    }

    private int dp2px(int dpValue) {
        float value = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
                getResources().getDisplayMetrics());
        return (int) (value + 0.5f);
    }
}
