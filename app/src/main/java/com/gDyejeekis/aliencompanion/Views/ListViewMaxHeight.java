package com.gDyejeekis.aliencompanion.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.gDyejeekis.aliencompanion.R;

/**
 * Created by sound on 2/1/2016.
 */
public class ListViewMaxHeight extends ListView {

    private int maxHeight;

    public ListViewMaxHeight(Context context) {
        this(context, null);
    }

    public ListViewMaxHeight(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListViewMaxHeight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ListViewMaxHeight);
            maxHeight = a.getDimensionPixelSize(R.styleable.ListViewMaxHeight_listViewMaxHeight, Integer.MAX_VALUE);
            a.recycle();
        } else {
            maxHeight = 0;
        }
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        if (maxHeight > 0 && maxHeight < measuredHeight) {
            int measureMode = View.MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(maxHeight, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}