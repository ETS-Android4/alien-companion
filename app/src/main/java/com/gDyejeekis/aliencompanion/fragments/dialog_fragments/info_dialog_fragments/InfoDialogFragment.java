package com.gDyejeekis.aliencompanion.fragments.dialog_fragments.info_dialog_fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.ScalableDialogFragment;

/**
 * Created by George on 5/1/2017.
 */

public class InfoDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    public static final String TITLE_KEY = "title";
    public static final String INFO_TEXT_KEY = "infoText";
    public static final String BUTTON_TEXT_KEY = "buttonText";
    public static final String IS_CANCELABLE_KEY = "cancelable";

    public static void showDialog(FragmentManager fm, String title, String info) {
        showDialog(fm, title, info, null, true);
    }

    public static void showDialog(FragmentManager fm, String title, String info, String buttonText, boolean cancelable) {
        InfoDialogFragment dialogFragment = new InfoDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TITLE_KEY, title);
        bundle.putString(INFO_TEXT_KEY, info);
        bundle.putString(BUTTON_TEXT_KEY, buttonText);
        bundle.putBoolean(IS_CANCELABLE_KEY, cancelable);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "dialog");
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            super.show(manager, tag);
        } catch (IllegalStateException e) {
            manager.beginTransaction().add(this, tag).commitAllowingStateLoss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        TextView title = view.findViewById(R.id.textView_info_title);
        TextView info = view.findViewById(R.id.textView_info);
        Button button = view.findViewById(R.id.button_confirm);

        String titleText = null;
        String infoText = null;
        String buttonText = null;
        boolean cancelable = true;
        if (getArguments()!=null) {
            titleText = getArguments().getString(TITLE_KEY);
            infoText = getArguments().getString(INFO_TEXT_KEY);
            buttonText = getArguments().getString(BUTTON_TEXT_KEY);
            cancelable = getArguments().getBoolean(IS_CANCELABLE_KEY, true);
        }

        if (titleText!=null && !titleText.trim().isEmpty()) {
            title.setText(titleText);
        } else {
            title.setVisibility(View.GONE);
        }

        if (infoText!=null && !infoText.trim().isEmpty()) {
            info.setText(infoText);
        } else {
            info.setVisibility(View.GONE);
        }

        if (buttonText!=null && !buttonText.trim().isEmpty()) {
            button.setText(buttonText);
        }
        button.setOnClickListener(this);

        getDialog().setCancelable(cancelable);
        getDialog().setCanceledOnTouchOutside(cancelable);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_confirm:
                dismiss();
                break;
        }
    }

}
