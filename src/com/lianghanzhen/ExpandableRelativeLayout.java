package com.lianghanzhen;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * RelativeLayout that can be expanded and collapsed.
 */
public class ExpandableRelativeLayout extends RelativeLayout {

    private static final int INVALID_EXPANDER_ID = 0;
    private static final boolean DEFAULT_EXPANDED = false;
    private static final int DEFAULT_MIN_LINES = 3;
    public static final int EXPANDER_MAX_LINES = Integer.MAX_VALUE;

    private int mExpanderId;
    private TextView mExpander;
    private boolean mExpanded;
    private int mMinLines;
    private int mOriginalWidth;
    private int mOriginalHeight;
    private int mCollapseHeight;

    private boolean mInitialized;
    private boolean mAnimating;

    private OnExpandListener mOnExpandListener;
    private OnCollapseListener mOnCollapseListener;

    //region Constructor

    public ExpandableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ExpandableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableRelativeLayout);
        mExpanderId = typedArray.getResourceId(R.styleable.ExpandableRelativeLayout_expander, INVALID_EXPANDER_ID);
        mExpanded = typedArray.getBoolean(R.styleable.ExpandableRelativeLayout_expanded, DEFAULT_EXPANDED);
        mMinLines = typedArray.getInteger(R.styleable.ExpandableRelativeLayout_minLines, DEFAULT_MIN_LINES);
        typedArray.recycle();
    }

    //endregion

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mExpanderId == INVALID_EXPANDER_ID) {
            throw new IllegalStateException("Do you forget to set expander property to ExpandableRelativeLayout.");
        }
        mExpander = (TextView) findViewById(mExpanderId);
        if (mExpander == null) {
            throw new NullPointerException("ExpandableRelativeLayout must have a TextView with id that you set the property expander.");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!mInitialized) {
            mOriginalWidth = mExpander.getMeasuredWidth();
            mOriginalHeight = mExpander.getMeasuredHeight();
            mExpander.setMaxLines(mMinLines);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            mCollapseHeight = mExpander.getMeasuredHeight();
            mExpander.setMaxLines(EXPANDER_MAX_LINES);
            mExpander.setMaxLines(mExpanded ? EXPANDER_MAX_LINES : mMinLines);
            mInitialized = true;
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
            startAnimation(new ExpandAnimation());
        } else {
            mExpander.setMaxLines(EXPANDER_MAX_LINES);
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
            startAnimation(new ExpandAnimation());
        } else {
            mExpander.setMaxLines(mMinLines);
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

    public ExpandableRelativeLayout setOnExpandListener(OnExpandListener onExpandListener) {
        mOnExpandListener = onExpandListener;
        return this;
    }

    public ExpandableRelativeLayout setOnCollapseListener(OnCollapseListener onCollapseListener) {
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
        changeExpanderHeight(expanded ? mOriginalHeight : mCollapseHeight);
        mExpander.setMaxLines(mExpanded ? EXPANDER_MAX_LINES : mMinLines);
    }

    public boolean isAnimating() {
        return mAnimating;
    }

    public TextView getExpander() {
        return mExpander;
    }

    //endregion

    private void changeExpanderHeight(int height) {
        mExpander.setLayoutParams(new LayoutParams(new ViewGroup.LayoutParams(mOriginalWidth, height)));
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
                mExpander.setMaxLines(EXPANDER_MAX_LINES);
            }
            mDistance = endHeight - mStartHeight;
            setDuration(500);
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
                mExpander.setMaxLines(mMinLines);
                toggleOnCollapseListener();
            } else {
                toggleOnExpandListener();
            }
        }

    }

    //endregion

    //region OnExpandListener And OnCollapseListener

    public interface OnExpandListener {
        void onExpand(ExpandableRelativeLayout parent);
    }

    public interface OnCollapseListener {
        void onCollapse(ExpandableRelativeLayout parent);
    }

    //endregion

}
