package com.lianghanzhen;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

/**
 *
 */
public class ExpandableTextView extends TextView {

    private static final boolean DEFAULT_EXPANDED = false;
    private static final int DEFAULT_MIN_LINES = 3;
    public static final int EXPANDER_MAX_LINES = Integer.MAX_VALUE;
    private static final long DEFAULT_ANIMATION_DURATION = 400;

    private boolean mExpanded = DEFAULT_EXPANDED;
    private int mMinLines = DEFAULT_MIN_LINES;
    private int mOriginalWidth;
    private int mOriginalHeight;
    private int mCollapseHeight;

    private boolean mInitialized;
    private boolean mAnimating;
    private long mAnimationDuration = DEFAULT_ANIMATION_DURATION;

    private OnExpandListener mOnExpandListener;
    private OnCollapseListener mOnCollapseListener;

    //region Constructor

    public ExpandableTextView(Context context) {
        super(context);
        init(context, null);
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ExpandableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        mExpanded = typedArray.getBoolean(R.styleable.ExpandableTextView_expanded, DEFAULT_EXPANDED);
        mMinLines = typedArray.getInteger(R.styleable.ExpandableTextView_minLines, DEFAULT_MIN_LINES);
        typedArray.recycle();
    }

    //endregion

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!mInitialized) {
            mOriginalWidth = getMeasuredWidth();
            mOriginalHeight = getMeasuredHeight();
            setMaxLines(mMinLines);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            mCollapseHeight = getMeasuredHeight();
            mInitialized = true;
            setMeasuredDimension(mOriginalWidth, mExpanded ? mOriginalHeight : mCollapseHeight);
        } else if (getTag(R.id.tag_expandable_text_view_reused) != null && !mAnimating) {
            setTag(R.id.tag_expandable_text_view_reused, null);
            mOriginalWidth = getMeasuredWidth();
            final int lineHeight = getLineHeight();
            mOriginalHeight = lineHeight * getLineCount() + 1;
            mCollapseHeight = lineHeight * mMinLines + 1;
            setMeasuredDimension(mOriginalWidth, mExpanded ? mOriginalHeight : mCollapseHeight);
        }
    }

    //region Expand And Collapse

    public void toggle() {
        toggle(true);
    }

    public void toggle(boolean animated) {
        if (mExpanded) {
            collapse(animated);
        } else {
            expand(animated);
        }
    }

    public void expand() {
        expand(true);
    }

    public void expand(boolean animated) {
        if (mAnimating) {
            return;
        }

        if (animated) {
            mAnimating = true;
            startAnimation(new ExpandAnimation());
        } else {
            setMaxLines(EXPANDER_MAX_LINES);
            toggleOnExpandListener();
        }
        mExpanded = true;
    }

    public void collapse() {
        collapse(true);
    }

    public void collapse(boolean animated) {
        if (mAnimating) {
            return;
        }

        if (animated) {
            mAnimating = true;
            startAnimation(new ExpandAnimation());
        } else {
            setMaxLines(mMinLines);
            toggleOnCollapseListener();
        }
        mExpanded = false;
    }

    //endregion

    //region toggle listener

    private void toggleOnExpandListener() {
        if (mOnExpandListener != null) {
            mOnExpandListener.onExpand(this);
        }
    }

    private void toggleOnCollapseListener() {
        if (mOnCollapseListener != null) {
            mOnCollapseListener.onCollapse(this);
        }
    }

    //endregion

    //region Setters And Getters

    public ExpandableTextView setOnExpandListener(OnExpandListener onExpandListener) {
        mOnExpandListener = onExpandListener;
        return this;
    }

    public ExpandableTextView setOnCollapseListener(OnCollapseListener onCollapseListener) {
        mOnCollapseListener = onCollapseListener;
        return this;
    }

    /**
     * If you use animation and the animation is not finished, it will return the previous state.
     */
    public boolean isExpanded() {
        return mExpanded;
    }

    /**
     * When you use this method in ListView with ViewHolder pattern, set it when convertView is not null.
     */
    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
        mAnimating = false;
        setMaxLines(EXPANDER_MAX_LINES);
    }

    public boolean isAnimating() {
        return mAnimating;
    }

    public void setAnimationDuration(long animationDuration) {
        mAnimationDuration = animationDuration;
    }

    //endregion

    private void changeExpanderHeight(int height) {
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = mOriginalWidth;
        params.height = height;
        setLayoutParams(params);
    }

    //region ExpandAnimation

    private class ExpandAnimation extends Animation {

        private final int mStartHeight;
        private final int mDistance;

        public ExpandAnimation() {
            super();
            int endHeight;
            if (mExpanded) {
                mStartHeight = mOriginalHeight;
                endHeight = mCollapseHeight;
            } else {
                mStartHeight = mCollapseHeight;
                endHeight = mOriginalHeight;
                setMaxLines(EXPANDER_MAX_LINES);
            }
            mDistance = endHeight - mStartHeight;
            setDuration(mAnimationDuration);
            setAnimationListener(new ExpandAnimationListener());
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            changeExpanderHeight(mStartHeight + Math.round(mDistance * interpolatedTime));
        }

    }

    //endregion

    //region ExpandAnimationListener

    private class ExpandAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            mAnimating = true;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mAnimating = false;
            if (!mExpanded) {
                setMaxLines(mMinLines);
                toggleOnCollapseListener();
            } else {
                toggleOnExpandListener();
            }
        }

    }

    //endregion

    //region OnExpandListener And OnCollapseListener

    public interface OnExpandListener {
        void onExpand(ExpandableTextView parent);
    }

    public interface OnCollapseListener {
        void onCollapse(ExpandableTextView parent);
    }

    //endregion

}
