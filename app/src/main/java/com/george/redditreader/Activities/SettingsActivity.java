package com.george.redditreader.Activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.george.redditreader.Fragments.SettingsFragment;
import com.george.redditreader.R;

/**
 * Created by George on 8/4/2015.
 */
public class SettingsActivity extends BackNavActivity {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        getFragmentManager().beginTransaction()
                .replace(R.id.optionsHolder, new SettingsFragment())
                .commit();
    }
}
