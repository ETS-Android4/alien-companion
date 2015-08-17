package com.george.redditreader.Fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.george.redditreader.R;

/**
 * Created by George on 8/16/2015.
 */
public class AddAccountDialogFragment extends DialogFragment implements View.OnClickListener {

    private Activity activity;
    private EditText usernameField;
    private EditText passwordField;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        activity = getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_login_screen, container, false);
        usernameField = (EditText) view.findViewById(R.id.editText_username);
        passwordField = (EditText) view.findViewById(R.id.editText_password);
        Button cancelBtn = (Button) view.findViewById(R.id.btn_cancel);
        Button addBtn = (Button) view.findViewById(R.id.btn_add);

        cancelBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
    public void onResume() {
        super.onResume();
        setDialogWidth();
    }

    private void setDialogWidth() {
        Window window = getDialog().getWindow();
        int width = 3 * getResources().getDisplayMetrics().widthPixels / 4;
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_cancel) {
            dismiss();
        }
        else {
            String username = usernameField.getText().toString();
            String password = passwordField.getText().toString();
            username = username.replaceAll("\\s","");
            if(username.equals("") || password.equals("")) {
                if(username.equals("")) {
                    usernameField.setText("");
                    usernameField.setHint("enter username");
                    usernameField.setHintTextColor(getResources().getColor(R.color.red));
                }
                if(password.equals("")) {
                    passwordField.setText("");
                    passwordField.setHint("enter password");
                    passwordField.setHintTextColor(getResources().getColor(R.color.red));
                }
            }
            else {
                //attempt login here
            }
        }
    }
}
