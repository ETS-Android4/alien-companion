package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

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

/**
 * Created by George on 5/1/2017.
 */

public class InfoDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    public static void showDialog(FragmentManager fm, String title, String info, String buttonText, boolean cancelable) {
        InfoDialogFragment dialogFragment = new InfoDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("info", info);
        bundle.putString("buttonText", buttonText);
        bundle.putBoolean("cancelable", cancelable);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "dialog");
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
            titleText = getArguments().getString("title");
            infoText = getArguments().getString("info");
            buttonText = getArguments().getString("buttonText");
            cancelable = getArguments().getBoolean("cancelable", true);
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
