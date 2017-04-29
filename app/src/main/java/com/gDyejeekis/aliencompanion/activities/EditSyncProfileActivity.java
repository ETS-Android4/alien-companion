package com.gDyejeekis.aliencompanion.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.gDyejeekis.aliencompanion.models.SyncProfile;
import com.gDyejeekis.aliencompanion.models.SyncSchedule;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 4/19/2017.
 */

public class EditSyncProfileActivity extends ToolbarActivity implements View.OnClickListener {

    private SyncProfile profile;
    private boolean isNewProfile;
    private EditText nameField;
    private EditText multiredditField;
    private AutoCompleteTextView subredditField;
    private TextView subredditsTextView;
    private TextView multiredditsTextView;
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

        initFields((SyncProfile) getIntent().getSerializableExtra("profile"));
    }

    private void initFields(final SyncProfile profile) {
        nameField = (EditText) findViewById(R.id.editText_profile_name);
        multiredditField = (EditText) findViewById(R.id.editText_multireddit);
        subredditField = (AutoCompleteTextView) findViewById(R.id.editText_subreddit);
        schedulesListView = (ListView) findViewById(R.id.listView_schedules);
        subredditsTextView = (TextView) findViewById(R.id.textView_subreddits);
        multiredditsTextView = (TextView) findViewById(R.id.textView_multireddits);
        Button addSubredditButton = (Button) findViewById(R.id.button_add_subreddit);
        Button addMultiredditButton = (Button) findViewById(R.id.button_add_multireddit);
        Button addScheduleButton = (Button) findViewById(R.id.button_add_schedule);
        Button syncOptionsButton = (Button) findViewById(R.id.button_sync_options);
        Button doneButton = (Button) findViewById(R.id.button_save_changes);

        subredditField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                addSubreddit();
                return true;
            }
        });
        multiredditField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                addMultireddit();
                return true;
            }
        });
        addSubredditButton.setOnClickListener(this);
        addMultiredditButton.setOnClickListener(this);
        addScheduleButton.setOnClickListener(this);
        syncOptionsButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);

        int dropdownResource = (MyApplication.nightThemeEnabled) ? R.layout.simple_dropdown_item_1line_dark : android.R.layout.simple_dropdown_item_1line;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, dropdownResource, RedditConstants.popularSubreddits);
        subredditField.setAdapter(adapter);

        isNewProfile = (profile==null);

        if(isNewProfile) {
            this.profile = new SyncProfile();
            getSupportActionBar().setTitle("Create profile");
            nameField.requestFocus();
        }
        else {
            this.profile = profile;
            getSupportActionBar().setTitle("Edit profile");
            nameField.setText(profile.getName());
            if(!profile.getSubreddits().isEmpty()) {
                subredditsTextView.setText(StringUtils.join(profile.getSubreddits(), ", "));
            }
            if(!profile.getMultireddits().isEmpty()) {
                multiredditsTextView.setText(StringUtils.join(profile.getMultireddits(), ", "));
            }
        }
        refreshSchedules();
    }

    private void refreshSchedules() {
        ArrayAdapter schedulesListAdapter = new ScheduleListAdapter(this, R.layout.sync_schedule_list_item, this.profile.getSchedules());
        schedulesListView.setAdapter(schedulesListAdapter);
        GeneralUtils.setListViewHeightBasedOnChildren(schedulesListView);
    }

    public void addSchedule(SyncSchedule schedule) {
        profile.setActive(true);
        profile.addSchedule(schedule);
        refreshSchedules();
    }

    public void removeSchedule(int position) {
        removeSchedule(profile.getSchedules().get(position));
    }

    public void removeSchedule(SyncSchedule schedule) {
        // init unschedule list if needed
        if(unscheduleList==null) {
            unscheduleList = new ArrayList<SyncSchedule>();
        }
        // add schedule to the list to be unscheduled
        unscheduleList.add(schedule);
        // remove schedule from profile
        profile.removeSchedule(schedule);
        refreshSchedules();
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
                profile.saveChanges(this, isNewProfile);
                finish();
                break;
        }
    }

    private void addSubreddit() {
        String subreddit = subredditField.getText().toString();
        subreddit = subreddit.replaceAll("\\s","");
        if(!subreddit.isEmpty()) {
            subredditField.setText("");
            subredditField.setHint("subreddit");
            subredditField.setHintTextColor(MyApplication.textHintColor);

            profile.addSubreddit(subreddit);
            subredditsTextView.setText(StringUtils.join(profile.getSubreddits(), ", "));
        }
        else {
            subredditField.setText("");
            subredditField.setHint("enter subreddit");
            subredditField.setHintTextColor(Color.RED);
        }
    }

    private void addMultireddit() {
        String multireddit = multiredditField.getText().toString();
        multireddit = multireddit.replaceAll("\\s","");
        if(!multireddit.isEmpty()) {
            multiredditField.setText("");
            multiredditField.setHint("multireddit");
            multiredditField.setHintTextColor(MyApplication.textHintColor);

            profile.addMultireddit(multireddit);
            multiredditsTextView.setText(StringUtils.join(profile.getMultireddits(), ", "));
        }
        else {
            multiredditField.setText("");
            multiredditField.setHint("enter multireddit");
            multiredditField.setHintTextColor(Color.RED);
        }
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

    public static class ScheduleListAdapter extends ArrayAdapter {

        private EditSyncProfileActivity activity;
        private int layoutResourceId;
        private List<SyncSchedule> schedules;
        private TextView scheduleTextView;
        private ImageView removeScheduleBtn;

        public ScheduleListAdapter(@NonNull EditSyncProfileActivity activity, @LayoutRes int resource, @NonNull List objects) {
            super(activity, resource, objects);
            this.activity = activity;
            this.layoutResourceId = resource;
            this.schedules = objects;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null) {
                convertView = activity.getLayoutInflater().inflate(layoutResourceId, parent, false);
                scheduleTextView = (TextView) convertView.findViewById(R.id.textView_sync_schedule);
                removeScheduleBtn = (ImageView) convertView.findViewById(R.id.imageView_remove_schedule);
            }

            final SyncSchedule schedule = schedules.get(position);
            String scheduleText = schedule.getStartTime() + ":00 - " + schedule.getEndTime() + ":00 " + schedule.getSortedDays();
            scheduleTextView.setText(scheduleText);
            removeScheduleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.removeSchedule(position);
                }
            });
            return convertView;
        }

    }
}
