package com.dyejeekis.aliencompanion.Activities;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.dyejeekis.aliencompanion.Fragments.SettingsFragment;
import com.dyejeekis.aliencompanion.R;

/**
 * Created by George on 8/4/2015.
 */
public class SettingsActivity extends BackNavActivity {

    private Toolbar toolbar;
    //private int currentColor;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if(MainActivity.nightThemeEnabled) getTheme().applyStyle(R.style.SettingsDarkTheme, true);
        else getTheme().applyStyle(R.style.SettingsLightTheme, true);
        setContentView(R.layout.activity_settings);

        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setBackgroundColor(MainActivity.currentColor);
        //currentColor = MainActivity.colorPrimary;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(MainActivity.colorPrimaryDark);
        toolbar.setNavigationIcon(MainActivity.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        getFragmentManager().beginTransaction()
                .replace(R.id.optionsHolder, new SettingsFragment())
                .commit();
    }

    //@Override
    //public void onResume() {
    //    super.onResume();
    //    Log.d("geo test", "on resume called in settings");
    //    if(currentColor != MainActivity.colorPrimary) {
    //        currentColor = MainActivity.colorPrimary;
    //        toolbar.setBackgroundColor(MainActivity.colorPrimary);
    //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(MainActivity.colorPrimaryDark);
    //        Log.d("geo test", "settings color changed");
    //    }
    //}

    @Override
    public void onBackPressed() {
        MainActivity.getCurrentSettings();
        super.onBackPressed();
    }

}
