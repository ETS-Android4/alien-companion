package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;

public class GridAutoFitLayoutManager extends GridLayoutManager {

    public static final String TAG = "GridAutoFitLayout";

    private int mColumnWidth;
    private float mDensity;
    //private boolean mColumnWidthChanged = true;

    public GridAutoFitLayoutManager(Context context, int columnWidth) {
        /* Initially set spanCount to 1, will be changed automatically later. */
        super(context, 1);
        setColumnWidth(checkedColumnWidth(context, columnWidth));
        setDensity(context);
    }

    public GridAutoFitLayoutManager(Context context, int columnWidth, int orientation, boolean reverseLayout) {
        /* Initially set spanCount to 1, will be changed automatically later. */
        super(context, 1, orientation, reverseLayout);
        setColumnWidth(checkedColumnWidth(context, columnWidth));
        setDensity(context);
    }

    private int checkedColumnWidth(Context context, int columnWidth) {
        if (columnWidth <= 0) {
            /* Set default columnWidth value (48dp here). It is better to move this constant
            to static constant on top, but we need context to convert it to dp, so can't really
            do so. */
            columnWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                    context.getResources().getDisplayMetrics());
        }
        return columnWidth;
    }

    public void setColumnWidth(int newColumnWidth) {
        if (newColumnWidth > 0 && newColumnWidth != mColumnWidth)
        {
            mColumnWidth = newColumnWidth;
            //mColumnWidthChanged = true;
        }
    }

    private void setDensity(Context context) {
        mDensity = context.getResources().getDisplayMetrics().density;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //Log.d(TAG, "-----------------------------------------------");
        //Log.d(TAG, "onLayoutChildren");
        int width = getWidth();
        int height = getHeight();
        //Log.d(TAG, width + " width");
        //Log.d(TAG, height + " height");
        if (/*mColumnWidthChanged && */mColumnWidth > 0 && width > 0 && height > 0) {
            int totalSpace;
            if (getOrientation() == VERTICAL) {
                totalSpace = width - getPaddingRight() - getPaddingLeft();
            }
            else {
                totalSpace = height - getPaddingTop() - getPaddingBottom();
            }
            float dpWidth = totalSpace / mDensity;
            int spanCount = Math.max(1, (int) dpWidth / mColumnWidth);
            if(spanCount==1) {
                spanCount++;
            }
            //Log.d(TAG, spanCount + " spans");
            setSpanCount(spanCount);
            //mColumnWidthChanged = false;
        }
        super.onLayoutChildren(recycler, state);
    }
}
