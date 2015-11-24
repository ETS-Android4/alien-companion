package com.gDyejeekis.aliencompanion.Fragments.DialogFragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.AsyncTasks.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.Models.NavDrawer.NavDrawerAccount;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.User;
import com.gDyejeekis.aliencompanion.enums.UserActionType;

import java.util.List;

/**
 * Created by sound on 10/6/2015.
 */
public class ChangePasswordDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    private EditText currentField;
    private EditText newField;
    private EditText confirmField;
    private User user;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        String username = getArguments().getString("username");
        assert username!=null;
        user = findUser(username);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);
        Button changeButton = (Button) view.findViewById(R.id.button_change);
        Button cancelButton = (Button) view.findViewById(R.id.button_cancel);
        currentField = (EditText) view.findViewById(R.id.editText_current_pass);
        newField = (EditText) view.findViewById(R.id.editText_new_pass);
        confirmField = (EditText) view.findViewById(R.id.editText_confirm_pass);
        currentField.requestFocus();
        confirmField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onClick(v);
                return true;
            }
        });
        changeButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_change || v.getId() == R.id.editText_confirm_pass) {
            String currentPass = currentField.getText().toString();
            String newPass = newField.getText().toString();
            String confirmPass = confirmField.getText().toString();
            if(currentPass.length()==0 || newPass.length()==0 || confirmPass.length()==0) {
                ToastUtils.displayShortToast(getActivity(), "All fields are required");
            }
            else if(!newPass.equals(confirmPass)) {
                ToastUtils.displayShortToast(getActivity(), "Last two fields should match!");
            }
            else {
                dismiss();
                LoadUserActionTask task = new LoadUserActionTask(getActivity(), UserActionType.changePassword, user, currentPass, newPass);
                task.execute();
            }
        }
        else dismiss();
    }

    private User findUser(String username) {
        User user = null;
        List<NavDrawerAccount> accountItems = ((MainActivity) getActivity()).getNavDrawerAdapter().accountItems;
        for(NavDrawerAccount item : accountItems) {
            if(item.getName().equals(username)) {
                user = new User(null, username, item.savedAccount.getModhash(), item.savedAccount.getCookie());
                break;
            }
        }
        return user;
    }
}
