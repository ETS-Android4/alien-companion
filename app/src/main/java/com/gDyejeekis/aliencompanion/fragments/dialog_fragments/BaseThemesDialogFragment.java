package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_base_themes, container, false);
        switch (MyApplication.currentBaseTheme) {
            case AppConstants.LIGHT_THEME:
                RadioButton light = view.findViewById(R.id.radioButtonLight);
                light.setChecked(true);
                break;
            case AppConstants.MATERIAL_BLUE_THEME:
                RadioButton materialBlue = view.findViewById(R.id.radioButtonMaterialBlue);
                materialBlue.setChecked(true);
                break;
            case AppConstants.MATERIAL_GREY_THEME:
                RadioButton materialGrey = view.findViewById(R.id.radioButtonMaterialGrey);
                materialGrey.setChecked(true);
                break;
            case AppConstants.DARK_THEME:
                RadioButton dark = view.findViewById(R.id.radioButtonDark);
                dark.setChecked(true);
                break;
            case AppConstants.DARK_THEME_LOW_CONTRAST:
                RadioButton darkLowContrast = view.findViewById(R.id.radioButtonDarkLowContrast);
                darkLowContrast.setChecked(true);
                break;
        }

        (view.findViewById(R.id.light)).setOnClickListener(this);
        (view.findViewById(R.id.material_blue)).setOnClickListener(this);
        (view.findViewById(R.id.material_grey)).setOnClickListener(this);
        (view.findViewById(R.id.dark)).setOnClickListener(this);
        (view.findViewById(R.id.dark_low_contrast)).setOnClickListener(this);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        return view;
    }

    @Override
    public void onClick(View v) {
        int theme = MyApplication.currentBaseTheme;
        boolean night = MyApplication.nightThemeEnabled;
        switch (v.getId()) {
            case R.id.light:
                theme = AppConstants.LIGHT_THEME;
                night = false;
                break;
            case R.id.material_blue:
                theme = AppConstants.MATERIAL_BLUE_THEME;
                night = true;
                break;
            case R.id.material_grey:
                theme = AppConstants.MATERIAL_GREY_THEME;
                night = true;
                break;
            case R.id.dark:
                theme = AppConstants.DARK_THEME;
                night = true;
                break;
            case R.id.dark_low_contrast:
                theme = AppConstants.DARK_THEME_LOW_CONTRAST;
                night = true;
                break;
        }
        if (theme != MyApplication.currentBaseTheme) {
            MyApplication.themeFieldsInitialized = false;
            SharedPreferences.Editor editor = MyApplication.prefs.edit();
            editor.putInt("baseTheme", theme);
            editor.putBoolean("nightTheme", night);
            editor.apply();
            ((MainActivity) getActivity()).restartApp();
        } else {
            dismiss();
        }
    }

}
