package com.dyejeekis.aliencompanion.Fragments.DialogFragments;

import android.app.DialogFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dyejeekis.aliencompanion.R;

/**
 * Created by sound on 10/3/2015.
 */
public class ConfirmationDialogFragment extends DialogFragment {

    public String text;
    public View.OnClickListener listener;

    public static ConfirmationDialogFragment newInstance(String text, View.OnClickListener listener) {
        ConfirmationDialogFragment newInstance = new ConfirmationDialogFragment();
        newInstance.text = text;
        newInstance.listener = listener;

        return newInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.confirmation_dialog, container, false);
        Button okButton = (Button) view.findViewById(R.id.button_ok);
        Button cancelButton = (Button) view.findViewById(R.id.button_cancel);
        TextView textView = (TextView) view.findViewById(R.id.textView_confirmation);
        textView.setText(text);
        okButton.setOnClickListener(listener);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

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
