package com.codepath.bigheartapp.helpers;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    // Specify a final variable for space between cardviews
    private final int horizontalSpaceWidth;

    // function to set the space height
    public HorizontalSpaceItemDecoration(int horizontalSpaceWidth) {
        this.horizontalSpaceWidth = horizontalSpaceWidth;
    }
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.right = horizontalSpaceWidth;
        outRect.left = horizontalSpaceWidth;
    }
}
