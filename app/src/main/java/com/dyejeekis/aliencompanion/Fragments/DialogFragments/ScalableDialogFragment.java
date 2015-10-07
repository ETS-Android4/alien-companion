package com.dyejeekis.aliencompanion.Fragments.DialogFragments;

import android.app.DialogFragment;
import android.content.res.Configuration;
import android.view.Window;
import android.widget.LinearLayout;

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
        Window window = getDialog().getWindow();
        int width = 6 * getResources().getDisplayMetrics().widthPixels / 7;
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
    }
}
