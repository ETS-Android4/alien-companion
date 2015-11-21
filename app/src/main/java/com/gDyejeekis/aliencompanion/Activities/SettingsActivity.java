package com.gDyejeekis.aliencompanion.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.gDyejeekis.aliencompanion.Fragments.SettingsFragment;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by George on 8/4/2015.
 */
public class SettingsActivity extends BackNavActivity {

    private Toolbar toolbar;
    //private int currentColor;

    @Override
    public void onCreate(Bundle bundle) {
        if(MainActivity.nightThemeEnabled) {
            getTheme().applyStyle(R.style.selectedTheme_night, true);
        }
        else getTheme().applyStyle(R.style.selectedTheme_day, true);
        super.onCreate(bundle);
        setContentView(R.layout.activity_settings);
        if(MainActivity.nightThemeEnabled)
            getTheme().applyStyle(R.style.Theme_AppCompat_Dialog, true);

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
