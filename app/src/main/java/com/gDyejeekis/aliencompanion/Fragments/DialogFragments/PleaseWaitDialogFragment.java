package com.gDyejeekis.aliencompanion.Fragments.DialogFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.R;

/**
 * Created by George on 7/20/2016.
 */
public class PleaseWaitDialogFragment extends DialogFragment {

    private String message;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        message = getArguments().getString("message");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_move_app_data, container, false);
        TextView textView = (TextView) view.findViewById(R.id.textView_operation);
        textView.setText((message==null) ? "Please wait" : message);

        setCancelable(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }
}
