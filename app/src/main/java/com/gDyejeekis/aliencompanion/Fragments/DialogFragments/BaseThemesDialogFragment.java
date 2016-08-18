package com.gDyejeekis.aliencompanion.Fragments.DialogFragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by George on 8/18/2016.
 */
public class BaseThemesDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_base_themes, container, false);

        RadioButton light = (RadioButton) view.findViewById(R.id.radioButtonLight);
        RadioButton materialBlue = (RadioButton) view.findViewById(R.id.radioButtonMaterialBlue);
        RadioButton materialGrey = (RadioButton) view.findViewById(R.id.radioButtonMaterialGrey);
        RadioButton dark = (RadioButton) view.findViewById(R.id.radioButtonDark);
        RadioButton darkLowContrast = (RadioButton) view.findViewById(R.id.radioButtonDarkLowContrast);

        switch (MyApplication.currentBaseTheme) {
            case MyApplication.LIGHT_THEME:
                light.setChecked(true);
                break;
            case MyApplication.MATERIAL_BLUE_THEME:
                materialBlue.setChecked(true);
                break;
            case MyApplication.MATERIAL_GREY_THEME:
                materialGrey.setChecked(true);
                break;
            case MyApplication.DARK_THEME:
                dark.setChecked(true);
                break;
            case MyApplication.DARK_THEME_LOW_CONTRAST:
                darkLowContrast.setChecked(true);
                break;
        }

        light.setOnClickListener(this);
        materialBlue.setOnClickListener(this);
        materialGrey.setOnClickListener(this);
        dark.setOnClickListener(this);
        darkLowContrast.setOnClickListener(this);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        return view;
    }

    @Override
    public void onClick(View v) {
        int theme = MyApplication.currentBaseTheme;
        boolean night = MyApplication.nightThemeEnabled;
        switch (v.getId()) {
            case R.id.radioButtonLight:
                theme = MyApplication.LIGHT_THEME;
                night = false;
                break;
            case R.id.radioButtonMaterialBlue:
                theme = MyApplication.MATERIAL_BLUE_THEME;
                night = true;
                break;
            case R.id.radioButtonMaterialGrey:
                theme = MyApplication.MATERIAL_GREY_THEME;
                night = true;
                break;
            case R.id.radioButtonDark:
                theme = MyApplication.DARK_THEME;
                night = true;
                break;
            case R.id.radioButtonDarkLowContrast:
                theme = MyApplication.DARK_THEME_LOW_CONTRAST;
                night = true;
                break;
        }
        if(theme != MyApplication.currentBaseTheme) {
            MyApplication.themeFieldsInitialized = false;
            SharedPreferences.Editor editor = MyApplication.prefs.edit();
            editor.putInt("baseTheme", theme);
            editor.putBoolean("nightTheme", night);
            editor.apply();
            MainActivity activity = (MainActivity) getActivity();
            activity.getNavDrawerAdapter().restartApp();
        }
        else {
            dismiss();
        }
    }
}
