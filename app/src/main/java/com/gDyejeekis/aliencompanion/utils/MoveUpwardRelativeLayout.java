package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by George on 3/22/2017.
 */

@CoordinatorLayout.DefaultBehavior(MoveUpwardBehavior.class)
public class MoveUpwardRelativeLayout extends RelativeLayout {
    public MoveUpwardRelativeLayout(Context context) {
        super(context);
    }

    public MoveUpwardRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MoveUpwardRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MoveUpwardRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
