package com.gDyejeekis.aliencompanion.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.sync_profile_dialog_fragments.SyncProfileOptionsDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.sync_profile_dialog_fragments.SyncProfileScheduleDialogFragment;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncProfile;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncSchedule;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.adapters.RemovableItemListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 4/19/2017.
 */

public class EditSyncProfileActivity extends ToolbarActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private SyncProfile profile;
    private boolean isNewProfile;
    private EditText nameField;
    private EditText multiredditField;
    private AutoCompleteTextView subredditField;
    //private TextView subredditsTextView;
    //private TextView multiredditsTextView;
    //private TextView schedulesTextView;
    private ListView subredditsListView;
    private ListView multiredditsListView;
    private ListView schedulesListView;
    private List<SyncSchedule> unscheduleList;

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sync_profile);
        initToolbar();

        initFields();
        initProfile((SyncProfile) getIntent().getSerializableExtra("profile"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    private void initFields() {
        nameField = (EditText) findViewById(R.id.editText_profile_name);
        multiredditField = (EditText) findViewById(R.id.editText_multireddit);
        subredditField = (AutoCompleteTextView) findViewById(R.id.editText_subreddit);
        subredditsListView = (ListView) findViewById(R.id.listView_subreddits);
        multiredditsListView = (ListView) findViewById(R.id.listView_multireddits);
        schedulesListView = (ListView) findViewById(R.id.listView_schedules);
        //subredditsTextView = (TextView) findViewById(R.id.textView_subreddits);
        //multiredditsTextView = (TextView) findViewById(R.id.textView_multireddits);
        //schedulesTextView = (TextView) findViewById(R.id.textView_schedules);
        ImageView addSubredditButton = (ImageView) findViewById(R.id.button_add_subreddit);
        ImageView addMultiredditButton = (ImageView) findViewById(R.id.button_add_multireddit);
        Button addScheduleButton = (Button) findViewById(R.id.button_add_schedule);
        Button syncOptionsButton = (Button) findViewById(R.id.button_sync_options);
        Button saveButton = (Button) findViewById(R.id.button_save_changes);

        styleAddImageView(addSubredditButton);
        styleAddImageView(addMultiredditButton);

        subredditField.setOnEditorActionListener(this);
        multiredditField.setOnEditorActionListener(this);

        addSubredditButton.setOnClickListener(this);
        addMultiredditButton.setOnClickListener(this);
        addScheduleButton.setOnClickListener(this);
        syncOptionsButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        int dropdownResource = (MyApplication.nightThemeEnabled) ? R.layout.simple_dropdown_item_1line_dark : android.R.layout.simple_dropdown_item_1line;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, dropdownResource, RedditConstants.popularSubreddits);
        subredditField.setAdapter(adapter);
    }

    private void styleAddImageView(ImageView imageView) {
        int drawable;
        float alpha;
        switch (MyApplication.currentBaseTheme) {
            case MyApplication.LIGHT_THEME:
                drawable = R.drawable.ic_add_circle_outline_black_24dp;
                alpha = 0.54f;
                break;
            case MyApplication.DARK_THEME_LOW_CONTRAST:
                drawable = R.drawable.ic_add_circle_outline_white_24dp;
                alpha = 0.6f;
                break;
            default:
                drawable = R.drawable.ic_add_circle_outline_white_24dp;
                alpha = 1f;
                break;
        }
        imageView.setImageResource(drawable);
        imageView.setAlpha(alpha);
    }

    private void initProfile(SyncProfile profile) {
        isNewProfile = (profile==null);

        if(isNewProfile) {
            this.profile = new SyncProfile();
            getSupportActionBar().setTitle("Create sync profile");
            nameField.requestFocus();
        }
        else {
            this.profile = profile;
            getSupportActionBar().setTitle("Edit sync profile");
            nameField.setText(profile.getName());
        }
        refreshSubreddits();
        refreshMultireddits();
        refreshSchedules();
    }

    private void refreshSubreddits() {
        if(profile.getSubreddits()==null || profile.getSubreddits().isEmpty()) {
            //subredditsTextView.setVisibility(View.VISIBLE);
            subredditsListView.setVisibility(View.GONE);
        }
        else {
            //subredditsTextView.setVisibility(View.GONE);
            subredditsListView.setVisibility(View.VISIBLE);
            subredditsListView.setAdapter(new RemovableItemListAdapter(this, profile.getSubreddits(), RemovableItemListAdapter.SUBREDDITS));
            GeneralUtils.setListViewHeightBasedOnChildren(subredditsListView);
        }
    }

    private void refreshMultireddits() {
        if(profile.getMultireddits()==null || profile.getMultireddits().isEmpty()) {
            //multiredditsTextView.setVisibility(View.VISIBLE);
            multiredditsListView.setVisibility(View.GONE);
        }
        else {
            //multiredditsTextView.setVisibility(View.GONE);
            multiredditsListView.setVisibility(View.VISIBLE);
            multiredditsListView.setAdapter(new RemovableItemListAdapter(this, profile.getMultireddits(), RemovableItemListAdapter.MULTIREDDITS));
            GeneralUtils.setListViewHeightBasedOnChildren(multiredditsListView);
        }
    }

    private void refreshSchedules() {
        if(profile.getSchedules() == null || profile.getSchedules().isEmpty()) {
            //schedulesTextView.setVisibility(View.VISIBLE);
            schedulesListView.setVisibility(View.GONE);
        }
        else {
            //schedulesTextView.setVisibility(View.GONE);
            schedulesListView.setVisibility(View.VISIBLE);
            List<String> formattedSchedules = new ArrayList<>();
            for(SyncSchedule schedule : profile.getSchedules()) {
                // TODO: 4/30/2017 maybe tweak this
                formattedSchedules.add(schedule.getStartTime() + ":00 - " + schedule.getEndTime() + ":00 " + schedule.getSortedDays());
            }
            schedulesListView.setAdapter(new RemovableItemListAdapter(this, formattedSchedules, RemovableItemListAdapter.SYNC_SCHEDULES));
            GeneralUtils.setListViewHeightBasedOnChildren(schedulesListView);
        }
    }

    public void addSchedule(SyncSchedule schedule) {
        profile.setActive(profile.addSchedule(schedule));
        refreshSchedules();
    }

    public void removeSchedule(int position) {
        removeSchedule(profile.getSchedules().get(position));
    }

    public void removeSchedule(SyncSchedule schedule) {
        if(profile.removeSchedule(schedule)) {
            // init unschedule list if needed and add the removed schedule
            if(unscheduleList == null) {
                unscheduleList = new ArrayList<>();
            }
            unscheduleList.add(schedule);
            // check if profile has any remaining schedules
            profile.setActive(profile.hasSchedule());
        }
        refreshSchedules();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.editText_subreddit:
                addSubreddit();
                return true;
            case R.id.editText_multireddit:
                addMultireddit();
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add_subreddit:
                addSubreddit();
                break;
            case R.id.button_add_multireddit:
                addMultireddit();
                break;
            case R.id.button_add_schedule:
                showScheduleDialog();
                break;
            case R.id.button_sync_options:
                showSyncOptionsDialog();
                break;
            case R.id.button_save_changes:
                saveProfile();
                break;
        }
    }

    private void addSubreddit() {
        String subreddit = subredditField.getText().toString();
        subreddit = subreddit.replaceAll("\\s","");
        if(subreddit.isEmpty()) {
            GeneralUtils.clearField(subredditField, "enter subreddit", Color.RED);
        }
        else if(profile.getSubreddits().contains(subreddit)) {
            GeneralUtils.clearField(subredditField, "subreddit");
            ToastUtils.showSnackbarOverToast(this, "Subreddit already in list");
        }
        else if(!GeneralUtils.isValidSubreddit(subreddit)) {
            GeneralUtils.clearField(subredditField, "subreddit");
            ToastUtils.showSnackbarOverToast(this, "Subreddit can contain only alphanumeric characters (a-z,0-9) and underscores (_)");
        }
        else {
            GeneralUtils.clearField(subredditField, "subreddit");
            profile.addSubreddit(subreddit);
            refreshSubreddits();
        }
    }

    public void removeSubreddit(int position) {
        profile.removeSubreddit(position);
        refreshSubreddits();
    }

    private void addMultireddit() {
        String multireddit = multiredditField.getText().toString();
        multireddit = multireddit.replaceAll("\\s","");
        if(multireddit.isEmpty()) {
            GeneralUtils.clearField(multiredditField, "enter multireddit", Color.RED);
        }
        else if(profile.getMultireddits().contains(multireddit)) {
            GeneralUtils.clearField(multiredditField, "multireddit");
            ToastUtils.showSnackbarOverToast(this, "Multireddit already in list");
        }
        else if(!GeneralUtils.isValidSubreddit(multireddit)) {
            GeneralUtils.clearField(multiredditField, "multireddit");
            ToastUtils.showSnackbarOverToast(this, "Multireddit can contain only alphanumeric characters (a-z,0-9) and underscores (_)");
        }
        else {
            GeneralUtils.clearField(multiredditField, "multireddit");
            profile.addMultireddit(multireddit);
            refreshMultireddits();
        }
    }

    public void removeMultireddit(int position) {
        profile.removeMultireddit(position);
        refreshMultireddits();
    }

    private void showScheduleDialog() {
        SyncProfileScheduleDialogFragment dialog = new SyncProfileScheduleDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("profile", profile);
        bundle.putBoolean("activate", true);
        dialog.setArguments(bundle);
        dialog.show(this.getSupportFragmentManager(), "dialog");
    }

    private void showSyncOptionsDialog() {
        SyncProfileOptionsDialogFragment dialog = new SyncProfileOptionsDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("profile", profile);
        dialog.setArguments(bundle);
        dialog.show(this.getSupportFragmentManager(), "dialog");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save_profile) {
            saveProfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProfile() {
        String name = nameField.getText().toString();
        if(name.trim().isEmpty()) {
            if(isNewProfile) {
                profile.setName(getIntent().getStringExtra("defaultName"));
            }
        } else {
            profile.setName(name);
        }

        if(unscheduleList!=null && !unscheduleList.isEmpty()) {
            profile.unschedulePendingIntents(this, unscheduleList);
        }
        profile.save(this, isNewProfile);
        finish();
    }

}
