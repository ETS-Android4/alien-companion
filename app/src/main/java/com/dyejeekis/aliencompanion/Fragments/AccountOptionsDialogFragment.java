package com.dyejeekis.aliencompanion.Fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.R;

/**
 * Created by sound on 8/27/2015.
 */
public class AccountOptionsDialogFragment extends DialogFragment {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_options, container, false);
        final String accountName = getArguments().getString("accountName");
        TextView title = (TextView) view.findViewById(R.id.textView_title);
        title.setText(accountName);
        TextView remove = (TextView) view.findViewById(R.id.textView_removeAccount);
        TextView changePass = (TextView) view.findViewById(R.id.textView_changePassowrd);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                MainActivity activity = (MainActivity) getActivity();
                activity.getNavDrawerAdapter().deleteAccount(accountName);
            }
        });
        changePass.setOnClickListener(null); //TODO: add functionality

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
        int width = 6 * getResources().getDisplayMetrics().widthPixels / 7;
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    //@Override
    //public Dialog onCreateDialog(Bundle bundle) {
    //    Context context = (MainActivity.nightThemeEnabled) ? new ContextThemeWrapper(getActivity(), R.style.AlertDialogDark) : getActivity();
    //    final String accountName = getArguments().getString("accountName");
    //    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    //    builder.setTitle(accountName).setItems(R.array.accountOptions, new DialogInterface.OnClickListener() {
    //        @Override
    //        public void onClick(DialogInterface dialog, int which) {
    //            switch (which) {
    //                case 0:
    //                    MainActivity activity = (MainActivity) getActivity();
    //                    activity.getNavDrawerAdapter().deleteAccount(accountName);
    //                    break;
    //                case 1:
    //                    break;
    //            }
    //        }
    //    });
    //    return builder.create();
    //}
}
