package com.w4po.securechat;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration{
    private Drawable mDivider;
    private int bLeft, bRight;

    public SimpleDividerItemDecoration(Drawable divider, int bLeft, int bRight) {
        mDivider = divider;
        this.bLeft = bLeft;
        this.bRight = bRight;
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int dividerTop = child.getBottom() + params.bottomMargin;
            int dividerBottom = dividerTop + mDivider.getIntrinsicHeight();

            mDivider.setBounds(bLeft, dividerTop, bRight, dividerBottom);
            mDivider.draw(canvas);
        }
    }
}