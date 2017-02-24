package com.gDyejeekis.aliencompanion.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;

/**
 * Created by George on 2/24/2017.
 */

public abstract class ToolbarActivity extends BackNavActivity {

    public static final int TOOLBAR_ANIM_DURATION = 500;

    protected Toolbar toolbar;
    protected boolean toolbarVisible;

    public void hideToolbar() {
        if(toolbarVisible) {
            toolbarVisible = false;
            toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2)).setDuration(TOOLBAR_ANIM_DURATION);
        }
    }

    public void showToolbar() {
        if(!toolbarVisible) {
            toolbarVisible = true;
            toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).setDuration(TOOLBAR_ANIM_DURATION);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbarVisible = true;
    }
}
