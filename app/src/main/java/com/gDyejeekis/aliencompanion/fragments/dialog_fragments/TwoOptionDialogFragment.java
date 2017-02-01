package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.gDyejeekis.aliencompanion.R;

/**
 * Created by sound on 10/18/2015.
 */
public class TwoOptionDialogFragment extends ScalableDialogFragment {

    public View.OnClickListener listener;
    public String option1;
    public String option2;

    public static TwoOptionDialogFragment newInstance(String option1, String option2, View.OnClickListener listener) {
        TwoOptionDialogFragment newInstance = new TwoOptionDialogFragment();
        newInstance.option1 = option1;
        newInstance.option2 = option2;
        newInstance.listener = listener;
        return newInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_two_options, container, false);
        Button button1 = (Button) view.findViewById(R.id.button_option_one);
        Button button2 = (Button) view.findViewById(R.id.button_option_two);
        button1.setText(option1);
        button2.setText(option2);
        View.OnClickListener wrapperListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onClick(v);
            }
        };
        button1.setOnClickListener(wrapperListener);
        button2.setOnClickListener(wrapperListener);

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }
}
