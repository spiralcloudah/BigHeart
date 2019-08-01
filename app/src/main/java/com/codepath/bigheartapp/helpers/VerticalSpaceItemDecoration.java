package com.codepath.bigheartapp.helpers;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    // Specify a final variable for space between cardviews
    private final int verticalSpaceHeight;

    // function to set the space height
    public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
        this.verticalSpaceHeight = verticalSpaceHeight;
    }
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.top = verticalSpaceHeight;
    }
}
