package com.gDyejeekis.aliencompanion.fragments.dialog_fragments.info_dialog_fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

public class CrashCollectionDialogFragment extends InfoDialogFragment implements View.OnClickListener {

    public static void showDialog(AppCompatActivity activity) {
        CrashCollectionDialogFragment dialog = new CrashCollectionDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TITLE_KEY, activity.getResources().getString(R.string.crash_collection_title));
        bundle.putString(INFO_TEXT_KEY, activity.getResources().getString(R.string.crash_collection_disclaimer));
        bundle.putString(BUTTON_TEXT_KEY, activity.getResources().getString(R.string.crash_collection_confirmation));
        bundle.putBoolean(IS_CANCELABLE_KEY, false);
        dialog.setArguments(bundle);
        dialog.show(activity.getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_confirm:
                dismiss();
                MyApplication.agreedCrashCollection = true;
                SharedPreferences.Editor editor = MyApplication.prefs.edit();
                editor.putBoolean("crashCollection", MyApplication.agreedCrashCollection);
                editor.apply();
                break;
        }
    }

}
