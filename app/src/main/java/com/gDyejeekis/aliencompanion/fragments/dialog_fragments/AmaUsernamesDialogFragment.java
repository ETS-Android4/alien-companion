package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;

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
            String[] usernames = usernamesField.getText().toString().trim().split(",");
            if(usernames.length == 0) {
                usernamesField.setHintTextColor(Color.RED);
                usernamesField.setHint("enter usernames");
            }
            else {
                PostFragment fragment;
                // TODO: 3/19/2017 maybe add abstraction
                if (getActivity() instanceof PostActivity) {
                    fragment = ((PostActivity) getActivity()).getPostFragment();
                } else {
                    fragment = ((MainActivity) getActivity()).getPostFragment();
                }
                fragment.commentNavListener.setAmaUsernames(usernames);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ama_usernames, container, false);
        usernamesField = (EditText) view.findViewById(R.id.editText_usernames);
        String usernames = getArguments().getString("usernames");
        if(usernames != null) {
            usernamesField.setText(usernames);
        }

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }
}
