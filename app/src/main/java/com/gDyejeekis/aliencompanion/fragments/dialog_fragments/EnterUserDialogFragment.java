package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class EnterUserDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    private EditText editText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_user, container, false);
        Button cancelButton = (Button) view.findViewById(R.id.button_cancel);
        Button viewButton = (Button) view.findViewById(R.id.button_view);
        editText = (EditText) view.findViewById(R.id.editText_subreddit);
        editText.requestFocus();

        cancelButton.setOnClickListener(this);
        viewButton.setOnClickListener(this);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onClick(v);
                return true;
            }
        });

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return view;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.button_cancel) {
            dismiss();
        }
        else {
            String user = editText.getText().toString();
            user = user.replaceAll("\\s","");
            if(user.isEmpty()) {
                GeneralUtils.clearField(editText, "enter user", Color.RED);
            }
            else if(!GeneralUtils.isValidUsername(user)) {
                GeneralUtils.clearField(editText, "user");
                ToastUtils.showToast(getActivity(), "User can contain only alphanumeric characters (a-z,0-9), underscores (_) and dashes (-)");
            }
            else {
                dismiss();
                Intent intent = new Intent(getActivity(), UserActivity.class);
                intent.putExtra("username", user.toLowerCase());
                startActivity(intent);
            }
        }
    }

}
