package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.app.DialogFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.gDyejeekis.aliencompanion.R;

/**
 * Created by sound on 1/20/2016.
 */
public class ChangeLogDialogFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.changelog_dialog, container, false);
        //ChangeLogRecyclerView changelogView = (ChangeLogRecyclerView) view.findViewById(R.id.changelogView);
        Button okButton = (Button) view.findViewById(R.id.button_ok);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getDialog().setTitle("WHAT'S NEW");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setDialogDimens();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setDialogDimens();
    }

    private void setDialogDimens() {
        Window window = getDialog().getWindow();
        int width = 95 * getResources().getDisplayMetrics().widthPixels / 100;
        //int height = 95 * getResources().getDisplayMetrics().heightPixels / 100;
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
    }
}
