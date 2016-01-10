package com.gDyejeekis.aliencompanion.Fragments.DialogFragments;


import android.app.DialogFragment;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.gDyejeekis.aliencompanion.AsyncTasks.AddAccountTask;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.utils.RedditOAuth;

/**
 * A simple {@link Fragment} subclass.
 */
public class VerifyAccountDialogFragment extends DialogFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        String username = getArguments().getString("username");
        String password = getArguments().getString("password");
        String oauthCode = getArguments().getString("code");

        AddAccountTask task;
        if(RedditOAuth.useOAuth2) task =  new AddAccountTask(this, oauthCode);
        else task =  new AddAccountTask(this, username, password);
        task.execute();

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(R.layout.fragment_verify_account_dialog, container, false);
    }


}
