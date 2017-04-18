package com.gDyejeekis.aliencompanion.fragments.settings_fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

import petrov.kristiyan.colorpicker.ColorPicker;

/**
 * Created by George on 9/10/2016.
 */
public class AppearanceSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.appearance_preferences);

        Preference colorPrimaryPref = findPreference("colorPrimary");
        colorPrimaryPref.setOnPreferenceClickListener(this);

        Preference colorSecondaryPref = findPreference("colorSecondary");
        colorSecondaryPref.setOnPreferenceClickListener(this);

        Preference colorInDarkThemesPref = findPreference("colorDarkTheme");
        colorInDarkThemesPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey().equals("colorPrimary")) {
            ColorPicker colorPicker = new ColorPicker(getActivity());
            colorPicker.setColors(R.array.colorPrimaryValues);
            colorPicker.setDefaultColorButton(MyApplication.colorPrimary);
            colorPicker.setRoundColorButton(true);
            colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {

                @Override
                public void onChooseColor(int position, int color) {
                    MyApplication.colorPrimaryChanged = true;
                    MyApplication.colorPrimary = color;
                    SharedPreferences.Editor editor = MyApplication.prefs.edit();
                    editor.putInt("colorPrimary", color);
                    editor.apply();
                }

                @Override
                public void onCancel() {

                }
            });
            colorPicker.show();
            return true;
        }
        else if(preference.getKey().equals("colorSecondary")) {
            ColorPicker colorPicker = new ColorPicker(getActivity());
            colorPicker.setColors(R.array.colorSecondaryValues);
            colorPicker.setDefaultColorButton(MyApplication.colorSecondary);
            colorPicker.setRoundColorButton(true);
            colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {

                @Override
                public void onChooseColor(int position, int color) {
                    MyApplication.colorSecondaryChanged = true;
                    MyApplication.colorSecondary = color;
                    SharedPreferences.Editor editor = MyApplication.prefs.edit();
                    editor.putInt("colorSecondary", color);
                    editor.apply();
                }

                @Override
                public void onCancel() {

                }
            });
            colorPicker.show();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getKey().equals("colorDarkTheme")) {
            MyApplication.themeFieldsInitialized = false;
            return true;
        }
        return false;
    }
}
