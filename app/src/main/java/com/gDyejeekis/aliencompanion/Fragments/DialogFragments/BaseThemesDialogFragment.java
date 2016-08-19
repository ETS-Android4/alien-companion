package com.gDyejeekis.aliencompanion.Fragments.DialogFragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
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


        switch (MyApplication.currentBaseTheme) {
            case MyApplication.LIGHT_THEME:
                RadioButton light = (RadioButton) view.findViewById(R.id.radioButtonLight);
                light.setChecked(true);
                break;
            case MyApplication.MATERIAL_BLUE_THEME:
                RadioButton materialBlue = (RadioButton) view.findViewById(R.id.radioButtonMaterialBlue);
                materialBlue.setChecked(true);
                break;
            case MyApplication.MATERIAL_GREY_THEME:
                RadioButton materialGrey = (RadioButton) view.findViewById(R.id.radioButtonMaterialGrey);
                materialGrey.setChecked(true);
                break;
            case MyApplication.DARK_THEME:
                RadioButton dark = (RadioButton) view.findViewById(R.id.radioButtonDark);
                dark.setChecked(true);
                break;
            case MyApplication.DARK_THEME_LOW_CONTRAST:
                RadioButton darkLowContrast = (RadioButton) view.findViewById(R.id.radioButtonDarkLowContrast);
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
                theme = MyApplication.LIGHT_THEME;
                night = false;
                break;
            case R.id.material_blue:
                theme = MyApplication.MATERIAL_BLUE_THEME;
                night = true;
                break;
            case R.id.material_grey:
                theme = MyApplication.MATERIAL_GREY_THEME;
                night = true;
                break;
            case R.id.dark:
                theme = MyApplication.DARK_THEME;
                night = true;
                break;
            case R.id.dark_low_contrast:
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
