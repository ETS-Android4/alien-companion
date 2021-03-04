package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;

import java.util.Arrays;
import java.util.List;

/**
 * Created by George on 3/19/2017.
 */

public class AmaUsernamesDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    private EditText usernamesField;

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_cancel) {
            dismiss();
        }
        else {
            String usernamesString = usernamesField.getText().toString().replace(" ", "");
            if(usernamesString.length() == 0) {
                usernamesField.setText("");
                usernamesField.setHintTextColor(Color.RED);
                usernamesField.setHint("enter usernames");
            }
            else {
                dismiss();
                PostFragment fragment;
                if (getActivity() instanceof PostActivity) {
                    fragment = ((PostActivity) getActivity()).getPostFragment();
                } else {
                    fragment = ((MainActivity) getActivity()).getPostFragment();
                }
                String[] usernames = usernamesString.split(",");
                for(int i=0;i<usernames.length;i++) {
                    usernames[i] = usernames[i].toLowerCase();
                }
                fragment.commentNavListener.setAmaUsernames(Arrays.asList(usernames));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ama_usernames, container, false);
        usernamesField = (EditText) view.findViewById(R.id.editText_usernames);
        usernamesField.requestFocus();
        usernamesField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onClick(v);
                return true;
            }
        });
        String usernames = getArguments().getString("usernames", null);
        if(usernames != null) {
            usernamesField.setText(usernames);
            usernamesField.selectAll();
        }
        Button cancel = (Button) view.findViewById(R.id.button_cancel);
        Button done = (Button) view.findViewById(R.id.button_done);
        cancel.setOnClickListener(this);
        done.setOnClickListener(this);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }
}
