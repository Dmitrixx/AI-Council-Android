package com.aicouncil.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/** Small dependency-free wrapping layout for accessible mode chips. */
public final class ChipFlowLayout extends ViewGroup {
    private int lineHeight;
    public ChipFlowLayout(Context context) { super(context); setClipToPadding(false); }
    public ChipFlowLayout(Context context, AttributeSet attrs) { super(context, attrs); setClipToPadding(false); }
    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int available = Math.max(0, width - getPaddingLeft() - getPaddingRight());
        int x = 0, y = getPaddingTop(), rowHeight = 0;
        int childWidthSpec = MeasureSpec.makeMeasureSpec(available, MeasureSpec.AT_MOST);
        int childHeightSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.AT_MOST);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            measureChild(child, childWidthSpec, childHeightSpec);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int cw = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int ch = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            if (x > 0 && x + cw > available) { x = 0; y += rowHeight; rowHeight = 0; }
            x += cw; rowHeight = Math.max(rowHeight, ch);
        }
        lineHeight = rowHeight;
        int desiredHeight = y + rowHeight + getPaddingBottom();
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec));
    }
    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int available = r - l - getPaddingLeft() - getPaddingRight();
        int x = getPaddingLeft(), y = getPaddingTop(), rowHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) continue;
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int cw = child.getMeasuredWidth(), ch = child.getMeasuredHeight();
            int total = cw + lp.leftMargin + lp.rightMargin;
            if (x > getPaddingLeft() && x - getPaddingLeft() + total > available) { x = getPaddingLeft(); y += rowHeight; rowHeight = 0; }
            int cl = x + lp.leftMargin, ct = y + lp.topMargin;
            child.layout(cl, ct, cl + cw, ct + ch);
            x += total; rowHeight = Math.max(rowHeight, ch + lp.topMargin + lp.bottomMargin);
        }
    }
    @Override protected LayoutParams generateDefaultLayoutParams() { return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); }
    @Override public LayoutParams generateLayoutParams(AttributeSet attrs) { return new MarginLayoutParams(getContext(), attrs); }
    @Override protected LayoutParams generateLayoutParams(LayoutParams p) { return new MarginLayoutParams(p); }
    @Override protected boolean checkLayoutParams(LayoutParams p) { return p instanceof MarginLayoutParams; }
}
