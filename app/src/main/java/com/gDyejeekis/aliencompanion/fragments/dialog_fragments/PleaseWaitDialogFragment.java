package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by George on 7/20/2016.
 */
public class PleaseWaitDialogFragment extends DialogFragment {

    public static final String DEFAULT_MESSAGE = "Please wait";

    private String message;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            message = getArguments().getString("message");
        if (message == null || message.trim().isEmpty())
            message = DEFAULT_MESSAGE;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_please_wait, container, false);
        ProgressBar progressBar = view.findViewById(R.id.progressBar_operation);
        progressBar.getIndeterminateDrawable().setColorFilter(MyApplication.colorSecondary, PorterDuff.Mode.SRC_IN);
        TextView textView = (TextView) view.findViewById(R.id.textView_operation);
        textView.setText(message);

        setCancelable(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }
}
