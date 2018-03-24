package com.gDyejeekis.aliencompanion.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Subreddit;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.sync_profile_dialog_fragments.SyncOptionsDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.sync_profile_dialog_fragments.SyncProfileScheduleDialogFragment;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncProfile;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncProfileOptions;
import com.gDyejeekis.aliencompanion.models.sync_profile.SyncSchedule;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.DelayAutoCompleteTextView;
import com.gDyejeekis.aliencompanion.views.adapters.RemovableItemListAdapter;
import com.gDyejeekis.aliencompanion.views.adapters.SubredditAutoCompleteAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 4/19/2017.
 */

public class EditSyncProfileActivity extends ToolbarActivity implements View.OnClickListener, DialogInterface.OnClickListener, TextView.OnEditorActionListener {

    private SyncProfile profile;
    private boolean isNewProfile;
    private List<String> originalSubreddits;
    private List<String> originalMultis;
    private List<SyncSchedule> originalSchedules;
    private SyncProfileOptions originalSyncOptions;
    private EditText nameField;
    private EditText multiredditField;
    private DelayAutoCompleteTextView subredditField;
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
        initProfile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    private void initFields() {
        nameField = findViewById(R.id.editText_profile_name);
        multiredditField = findViewById(R.id.editText_multireddit);
        subredditField = findViewById(R.id.editText_subreddit);
        subredditsListView = findViewById(R.id.listView_subreddits);
        multiredditsListView = findViewById(R.id.listView_multireddits);
        schedulesListView = findViewById(R.id.listView_schedules);
        ImageView addSubredditButton = findViewById(R.id.button_add_subreddit);
        ImageView addMultiredditButton = findViewById(R.id.button_add_multireddit);
        Button addScheduleButton = findViewById(R.id.button_add_schedule);
        Button syncOptionsButton = findViewById(R.id.button_sync_options);
        Button saveButton = findViewById(R.id.button_save_changes);

        styleAddImageView(addSubredditButton);
        styleAddImageView(addMultiredditButton);

        subredditField.setOnEditorActionListener(this);
        multiredditField.setOnEditorActionListener(this);

        addSubredditButton.setOnClickListener(this);
        addMultiredditButton.setOnClickListener(this);
        addScheduleButton.setOnClickListener(this);
        syncOptionsButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        subredditField.setAdapter(new SubredditAutoCompleteAdapter(this));
        subredditField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Subreddit subreddit = (Subreddit) adapterView.getItemAtPosition(i);
                String name = subreddit.getDisplayName();
                subredditField.setText(name);
                subredditField.setSelection(name.length());
            }
        });
    }

    private void styleAddImageView(ImageView imageView) {
        int drawable;
        float alpha;
        switch (MyApplication.currentBaseTheme) {
            case AppConstants.LIGHT_THEME:
                drawable = R.drawable.ic_add_circle_outline_black_24dp;
                alpha = 0.54f;
                break;
            case AppConstants.DARK_THEME_LOW_CONTRAST:
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

    private void initProfile() {
        SyncProfile originalProfile = (SyncProfile) getIntent().getSerializableExtra("profile");
        isNewProfile = (originalProfile==null);

        if (isNewProfile) {
            this.profile = new SyncProfile();
            getSupportActionBar().setTitle("Create sync profile");
            nameField.requestFocus();
        } else {
            this.profile = originalProfile;
            getSupportActionBar().setTitle("Edit sync profile");
            nameField.setText(originalProfile.getName());

            // ideally we'd wanna have a SyncProfile(profile, id) constructor so we can create a new instance of the same profile with the same id
            // and keep original profile as a class field but adding that breaks already serialized objects
            // (prolly cuz Profile class doesn't have serialVersionUID static field)
            // so this is the next best thing, fuck me i hate serialization (never again)
            try {
                originalSubreddits = new ArrayList<>(originalProfile.getSubreddits());
                originalMultis = new ArrayList<>(originalProfile.getMultireddits());
                originalSchedules = new ArrayList<>(originalProfile.getSchedules());
                originalSyncOptions = originalProfile.getSyncOptions()==null
                        ? null : new SyncProfileOptions(originalProfile.getSyncOptions());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        refreshSubreddits();
        refreshMultireddits();
        refreshSchedules();
    }

    private void refreshSubreddits() {
        if (profile.getSubreddits()==null || profile.getSubreddits().isEmpty()) {
            subredditsListView.setVisibility(View.GONE);
        } else {
            subredditsListView.setVisibility(View.VISIBLE);
            subredditsListView.setAdapter(new RemovableItemListAdapter(this, profile.getSubreddits(), RemovableItemListAdapter.SUBREDDITS));
            GeneralUtils.setListViewHeightBasedOnChildren(subredditsListView);
        }
    }

    private void refreshMultireddits() {
        if (profile.getMultireddits()==null || profile.getMultireddits().isEmpty()) {
            multiredditsListView.setVisibility(View.GONE);
        } else {
            multiredditsListView.setVisibility(View.VISIBLE);
            multiredditsListView.setAdapter(new RemovableItemListAdapter(this, profile.getMultireddits(), RemovableItemListAdapter.MULTIREDDITS));
            GeneralUtils.setListViewHeightBasedOnChildren(multiredditsListView);
        }
    }

    private void refreshSchedules() {
        if (profile.getSchedules() == null || profile.getSchedules().isEmpty()) {
            schedulesListView.setVisibility(View.GONE);
        } else {
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
        if (profile.removeSchedule(schedule)) {
            // init unschedule list if needed and add the removed schedule
            if (unscheduleList == null) {
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
        if (subreddit.isEmpty()) {
            GeneralUtils.clearField(subredditField, "enter subreddit", Color.RED);
        } else if (profile.getSubreddits().contains(subreddit)) {
            GeneralUtils.clearField(subredditField, "subreddit");
            ToastUtils.showSnackbarOverToast(this, "Subreddit already in list");
        } else if (!GeneralUtils.isValidSubreddit(subreddit)) {
            GeneralUtils.clearField(subredditField, "subreddit");
            ToastUtils.showSnackbarOverToast(this, "Subreddit can contain only alphanumeric characters (a-z,0-9) and underscores (_)");
        } else {
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
        if (multireddit.isEmpty()) {
            GeneralUtils.clearField(multiredditField, "enter multireddit", Color.RED);
        } else if (profile.getMultireddits().contains(multireddit)) {
            GeneralUtils.clearField(multiredditField, "multireddit");
            ToastUtils.showSnackbarOverToast(this, "Multireddit already in list");
        } else if (!GeneralUtils.isValidSubreddit(multireddit)) {
            GeneralUtils.clearField(multiredditField, "multireddit");
            ToastUtils.showSnackbarOverToast(this, "Multireddit can contain only alphanumeric characters (a-z,0-9) and underscores (_)");
        } else {
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
        SyncOptionsDialogFragment dialog = new SyncOptionsDialogFragment();
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
        if (name.trim().isEmpty()) {
            if (isNewProfile) {
                profile.setName(getIntent().getStringExtra("defaultName"));
            }
        } else {
            profile.setName(name);
        }

        if (unscheduleList!=null && !unscheduleList.isEmpty()) {
            profile.unschedulePendingIntents(this, unscheduleList);
        }
        profile.save(this, isNewProfile);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (changesMade()) {
            showSaveChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    private boolean changesMade() {
        try {
            String nameFieldString = nameField.getText().toString();
            if (isNewProfile) {
                if (!nameFieldString.trim().isEmpty()) return true;
                if (!profile.getSubreddits().isEmpty()) return true;
                if (!profile.getMultireddits().isEmpty()) return true;
                if (!profile.getSchedules().isEmpty()) return true;
                if (profile.getSyncOptions()!=null) return true;
            } else {
                if (!nameFieldString.equals(profile.getName())) return true;
                if (!originalSubreddits.equals(profile.getSubreddits())) return true;
                if (!originalMultis.equals(profile.getMultireddits())) return true;
                if (!originalSchedules.equals(profile.getSchedules())) return true;
                if (originalSyncOptions==null) {
                    if (profile.getSyncOptions()!=null) return true;
                } else if (!originalSyncOptions.equals(profile.getSyncOptions())) return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    private void showSaveChangesDialog() {
        String message = isNewProfile ? "Save profile?" : "Save changes?";
        new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyAlertDialogStyle))
                .setMessage(message)
                .setPositiveButton("Yes", this)
                .setNegativeButton("No", this)
                .show();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                saveProfile();
                super.onBackPressed();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                super.onBackPressed();
                break;
        }
    }

}
