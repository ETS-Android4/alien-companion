package com.george.redditreader.Activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.george.redditreader.Fragments.MainOptionsFragment;
import com.george.redditreader.R;

public class SettingsActivity extends BackNavActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupFragment();
    }

    private void setupFragment() {
        if(getFragmentManager().findFragmentById(R.id.optionsHolder) == null) {
            getFragmentManager().beginTransaction().add(R.id.optionsHolder, new MainOptionsFragment()).commit();
        }
    }
}
