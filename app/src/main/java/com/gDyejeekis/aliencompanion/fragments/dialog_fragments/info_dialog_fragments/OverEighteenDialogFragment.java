package com.gDyejeekis.aliencompanion.fragments.dialog_fragments.info_dialog_fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by George on 3/26/2018.
 */

public class OverEighteenDialogFragment extends InfoDialogFragment implements View.OnClickListener {

    public static void showDialog(AppCompatActivity activity) {
        OverEighteenDialogFragment dialog = new OverEighteenDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TITLE_KEY, activity.getResources().getString(R.string.user_over_18_title));
        bundle.putString(INFO_TEXT_KEY, activity.getResources().getString(R.string.user_over_18_info_text));
        bundle.putString(BUTTON_TEXT_KEY, activity.getResources().getString(R.string.user_over_18_button_text));
        dialog.setArguments(bundle);
        dialog.show(activity.getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_confirm:
                dismiss();
                MyApplication.userOver18 = true;
                SharedPreferences.Editor editor = MyApplication.prefs.edit();
                editor.putBoolean("userOver18", MyApplication.userOver18);
                editor.apply();
                break;
        }
    }

}
