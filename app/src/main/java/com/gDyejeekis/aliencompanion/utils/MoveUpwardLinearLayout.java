package com.gDyejeekis.aliencompanion.utils;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.widget.LinearLayout;

@CoordinatorLayout.DefaultBehavior(MoveUpwardBehavior.class)
public class MoveUpwardLinearLayout extends LinearLayout {
    public MoveUpwardLinearLayout(Context context) {
        super(context);
    }

    public MoveUpwardLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MoveUpwardLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}