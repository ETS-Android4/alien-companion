package com.gDyejeekis.aliencompanion.Fragments.DialogFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.R;

/**
 * Created by George on 7/17/2016.
 */
public class MoveAppDataDialogFragment extends ScalableDialogFragment {

    private boolean moveToExternal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moveToExternal = getArguments().getBoolean("external");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_move_app_data, container, false);

        TextView textView = (TextView) view.findViewById(R.id.textView_operation);
        String string = (moveToExternal) ? "external" : "internal";
        string = "Moving data to " + string + " memory";
        textView.setText(string);
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                SystemClock.sleep(2000);
                if(moveToExternal) {
                    // TODO: 7/17/2016
                }
                else {
                    // TODO: 7/17/2016
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                dismiss();
            }
        }.execute();

        setCancelable(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }
}
