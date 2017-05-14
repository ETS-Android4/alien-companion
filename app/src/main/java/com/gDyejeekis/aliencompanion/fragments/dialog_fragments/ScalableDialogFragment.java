package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.content.res.Configuration;
import android.support.v4.app.DialogFragment;
import android.view.Window;
import android.widget.LinearLayout;

import com.gDyejeekis.aliencompanion.MyApplication;

/**
 * Created by sound on 10/6/2015.
 */
public abstract class ScalableDialogFragment extends DialogFragment {

    @Override
    public void onResume() {
        super.onResume();
        setDialogWidth();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setDialogWidth();
    }

    private void setDialogWidth() {
        boolean isLandscape = getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        float widthFactor;
        if(MyApplication.isLargeScreen) {
            widthFactor = isLandscape ? 0.6f : 0.8f;
        }
        else if(MyApplication.isVeryLargeScreen) {
            widthFactor = isLandscape ? 0.5f : 0.7f;
        }
        else {
            widthFactor = isLandscape ? 0.79f : 0.99f;
        }
        int width = Math.round(getResources().getDisplayMetrics().widthPixels * widthFactor);
        Window window = getDialog().getWindow();
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

}
