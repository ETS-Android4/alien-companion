package com.dyejeekis.aliencompanion.Fragments.DialogFragments;


import android.app.DialogFragment;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.dyejeekis.aliencompanion.LoadTasks.AddAccountTask;
import com.dyejeekis.aliencompanion.R;

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

        AddAccountTask task = new AddAccountTask(this, username, password);
        task.execute();

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(R.layout.fragment_verify_account_dialog, container, false);
    }


}
