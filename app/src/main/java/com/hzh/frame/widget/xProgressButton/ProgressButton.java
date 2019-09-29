package com.hzh.frame.widget.xProgressButton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.hzh.frame.BaseInitData;
import com.hzh.frame.R;

public class ProgressButton extends android.support.v7.widget.AppCompatButton {

    private float mCornerRadius = 0;
    private float mProgressMargin = 0;

    private boolean mFinish;

    private int mProgress;
    private int mMaxProgress = 100;
    private int mMinProgress = 0;

    private GradientDrawable mDrawableButton;
    private GradientDrawable mDrawableProgressBackground;
    private GradientDrawable mDrawableProgress;

    public ProgressButton(Context context) {
        super(context);
    }
    
    public ProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }
    
    
    private void initialize(Context context, AttributeSet attrs) {
        //Progress background drawable
        mDrawableProgressBackground = new GradientDrawable();
        //Progress drawable
        mDrawableProgress = new GradientDrawable();
        //Normal drawable
        mDrawableButton = new GradientDrawable();

        int defaultButtonColor = ContextCompat.getColor(BaseInitData.applicationContext,R.color.base_progressButton_colorGray);
        int defaultProgressColor = ContextCompat.getColor(BaseInitData.applicationContext,R.color.base_progressButton_colorGreen);
        int defaultBackColor = ContextCompat.getColor(BaseInitData.applicationContext,R.color.base_progressButton_colorGray);

        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton);

        try {
            mProgressMargin = attr.getDimension(R.styleable.ProgressButton_progressMargin, mProgressMargin);
            mCornerRadius = attr.getDimension(R.styleable.ProgressButton_cornerRadius, mCornerRadius);
            //Get custom normal color
            int buttonColor = attr.getColor(R.styleable.ProgressButton_buttonColor, defaultButtonColor);
            //Set normal color
            mDrawableButton.setColor(buttonColor);
            //Get custom progress background color
            int progressBackColor = attr.getColor(R.styleable.ProgressButton_progressBackColor, defaultBackColor);
            //Set progress background drawable color
            mDrawableProgressBackground.setColor(progressBackColor);
            //Get custom progress color
            int progressColor = attr.getColor(R.styleable.ProgressButton_progressColor, defaultProgressColor);
            //Set progress drawable color
            mDrawableProgress.setColor(progressColor);

            //Get default progress
            mProgress = attr.getInteger(R.styleable.ProgressButton_progress, mProgress);
            //Get minimum progress
            mMinProgress = attr.getInteger(R.styleable.ProgressButton_minProgress, mMinProgress);
            //Get maximize progress
            mMaxProgress = attr.getInteger(R.styleable.ProgressButton_maxProgress, mMaxProgress);

        } finally {
            attr.recycle();
        }

        //Set corner radius
        mDrawableButton.setCornerRadius(mCornerRadius);
        mDrawableProgressBackground.setCornerRadius(mCornerRadius);
        mDrawableProgress.setCornerRadius(mCornerRadius - mProgressMargin);
        setBackgroundDrawable(mDrawableButton);

        mFinish = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mProgress > mMinProgress && mProgress <= mMaxProgress && !mFinish) {
            //Calculate the width of progress
            float progressWidth =
                    (float) getMeasuredWidth() * ((float) (mProgress - mMinProgress) / mMaxProgress - mMinProgress);

            //If progress width less than 2x corner radius, the radius of progress will be wrong
            if (progressWidth < mCornerRadius * 2) {
                progressWidth = mCornerRadius * 2;
            }

            //Set rect of progress
            mDrawableProgress.setBounds((int) mProgressMargin, (int) mProgressMargin,
                    (int) (progressWidth - mProgressMargin), getMeasuredHeight() - (int) mProgressMargin);

            //Draw progress
            mDrawableProgress.draw(canvas);

            if (mProgress >= mMaxProgress) {
                setBackgroundDrawable(mDrawableButton);
                mFinish = true;
            }
        }
        super.onDraw(canvas);
    }

    /**
     * Set current progress
     */
    public void setProgress(int progress) {
        if (!mFinish) {
            mProgress = progress;
            setBackgroundDrawable(mDrawableProgressBackground);
            invalidate();
        }
    }

    /**
     * Set current ButtonColor
     */
    public void setButtonColor(int color) {
        mDrawableButton.setColor(color);
        invalidate();
    }

    public void setMaxProgress(int maxProgress) {
        mMaxProgress = maxProgress;
    }

    public void setMinProgress(int minProgress) {
        mMinProgress = minProgress;
    }

    public void reset() {
        mFinish = false;
        mProgress = mMinProgress;
    }
}
