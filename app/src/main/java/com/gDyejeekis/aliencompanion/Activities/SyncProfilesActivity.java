package com.gDyejeekis.aliencompanion.Activities;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.codetroopers.betterpickers.recurrencepicker.RecurrencePickerDialogFragment;
import com.gDyejeekis.aliencompanion.Adapters.SyncProfileListAdapter;
import com.gDyejeekis.aliencompanion.Models.SyncProfile;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Views.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 2/4/2016.
 */
public class SyncProfilesActivity extends BackNavActivity {

    //private boolean changesMade;
    private RecyclerView profilesView;
    private SyncProfileListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //getTheme().applyStyle(MainActivity.fontStyle, true);
        if(MyApplication.nightThemeEnabled) {
            getTheme().applyStyle(R.style.PopupDarkTheme, true);
            getTheme().applyStyle(R.style.selectedTheme_night, true);
        }
        else getTheme().applyStyle(R.style.selectedTheme_day, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_profiles);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(MyApplication.nightThemeEnabled) toolbar.setPopupTheme(R.style.OverflowStyleDark);
        toolbar.setBackgroundColor(MyApplication.currentColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(MyApplication.colorPrimaryDark);
        toolbar.setNavigationIcon(MyApplication.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profilesView = (RecyclerView) findViewById(R.id.recyclerView_sync_profiles);
        adapter = new SyncProfileListAdapter(this);
        profilesView.setLayoutManager(new LinearLayoutManager(this));
        profilesView.setAdapter(adapter);
        profilesView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_subreddits, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_add_subreddit) {
            adapter.newProfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        adapter.saveSyncProfiles();
        super.onStop();
    }

    private void showReccurenceDialog() {
        FragmentManager fm = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        Time time = new Time();
        time.setToNow();
        bundle.putLong(RecurrencePickerDialogFragment.BUNDLE_START_TIME_MILLIS, time.toMillis(false));
        bundle.putString(RecurrencePickerDialogFragment.BUNDLE_TIME_ZONE, time.timezone);
        bundle.putString(RecurrencePickerDialogFragment.BUNDLE_RRULE, "");
        bundle.putBoolean(RecurrencePickerDialogFragment.BUNDLE_HIDE_SWITCH_BUTTON, true);

        RecurrencePickerDialogFragment rpd = new RecurrencePickerDialogFragment();
        rpd.setArguments(bundle);
        //rpd.setOnRecurrenceSetListener(SampleRecurrenceBasicUsage.this);
        rpd.show(fm, "reccurence_dialog");
    }

    private void showSubredditsDialog() {

    }
}
