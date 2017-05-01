package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        TextView title = (TextView) view.findViewById(R.id.textView_info_title);
        TextView info = (TextView) view.findViewById(R.id.textView_info);
        Button gotIt = (Button) view.findViewById(R.id.button_got_it);
        String titleText = getArguments().getString("title");
        String infoText = getArguments().getString("info");
        if(titleText!=null && !titleText.trim().isEmpty()) {
            title.setText(titleText);
        }
        else {
            title.setVisibility(View.GONE);
        }
        if(infoText!=null && !infoText.trim().isEmpty()) {
            info.setText(infoText);
        }
        else {
            info.setVisibility(View.GONE);
        }
        gotIt.setOnClickListener(this);

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_got_it:
                dismiss();
                break;
        }
    }
}
