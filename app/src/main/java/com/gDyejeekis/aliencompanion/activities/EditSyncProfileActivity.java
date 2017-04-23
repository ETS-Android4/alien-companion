package com.gDyejeekis.aliencompanion.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;
import com.gDyejeekis.aliencompanion.models.SyncProfile;
import com.gDyejeekis.aliencompanion.models.SyncSchedule;

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
    private ListView schedulesListView;
    private ArrayAdapter schedulesListAdapter;
    private TextView subredditsTextView;
    private TextView multiredditsTextView;
    private List<SyncSchedule> unscheduleList;

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        Button doneButton = (Button) findViewById(R.id.button_done);

        addSubredditButton.setOnClickListener(this);
        addMultiredditButton.setOnClickListener(this);
        addScheduleButton.setOnClickListener(this);
        syncOptionsButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);

        int dropdownResource = (MyApplication.nightThemeEnabled) ? R.layout.simple_dropdown_item_1line_dark : android.R.layout.simple_dropdown_item_1line;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, dropdownResource, RedditConstants.popularSubreddits);
        subredditField.setAdapter(adapter);

        schedulesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(view.getId() == R.id.imageView_remove_schedule) {
                    // init unschedule list if needed
                    if(unscheduleList==null) {
                        unscheduleList = new ArrayList<SyncSchedule>();
                    }
                    SyncSchedule schedule = profile.getSchedules().get(position);
                    // add schedule to the list to be unscheduled
                    unscheduleList.add(schedule);
                    // remove schedule from profile
                    profile.removeSchedule(schedule);
                }
            }
        });

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
            subredditsTextView.setText(StringUtils.join(profile.getSubreddits(), ", "));
            multiredditsTextView.setText(StringUtils.join(profile.getMultireddits(), ", "));
        }
        schedulesListAdapter = new ScheduleListAdapter(this, R.layout.sync_schedule_list_item, profile==null ? new ArrayList<SyncSchedule>() : profile.getSchedules());
        schedulesListView.setAdapter(schedulesListAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add_subreddit:
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
                break;
            case R.id.button_add_multireddit:
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
                break;
            case R.id.button_add_schedule:
                // TODO: 4/19/2017
                break;
            case R.id.button_sync_options:
                // TODO: 4/19/2017
                break;
            case R.id.button_done:
                if(unscheduleList!=null && !unscheduleList.isEmpty()) {
                    profile.unschedulePendingIntents(this, unscheduleList);
                }
                profile.saveChanges(this, isNewProfile);
                finish();
                break;
        }
    }

    public static class ScheduleListAdapter extends ArrayAdapter {

        private Context context;
        private int layoutResourceId;
        private List<SyncSchedule> schedules;

        public ScheduleListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List objects) {
            super(context, resource, objects);
            this.context = context;
            this.layoutResourceId = resource;
            this.schedules = objects;
        }

        public void addSchedule(SyncSchedule schedule) {
            // TODO: 4/23/2017
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null) {
                convertView = ((Activity) context).getLayoutInflater().inflate(layoutResourceId, parent, false);
            }

            SyncSchedule schedule = schedules.get(position);
            // TODO: 4/23/2017
            return convertView;
        }

    }
}
