package com.gDyejeekis.aliencompanion.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.InfoDialogFragment;
import com.gDyejeekis.aliencompanion.models.Profile;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.views.adapters.ProfileListAdapter;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.views.DividerItemDecoration;

import java.io.File;
import java.util.List;

/**
 * Created by sound on 2/4/2016.
 */
public class ProfilesActivity extends ToolbarActivity {

    public static final String PROFILES_TYPE_EXTRA = "profilesType";

    public static final int SYNC_PROFILES = 0;

    public static final int FILTER_PROFILES = 1;

    private RecyclerView profilesView;
    private ProfileListAdapter adapter;
    private int profilesType;

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);
        if(MyApplication.nightThemeEnabled)
            getTheme().applyStyle(R.style.Theme_AppCompat_Dialog, true);
        initToolbar();

        profilesView = (RecyclerView) findViewById(R.id.recyclerView_sync_profiles);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(MyApplication.colorSecondary));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newProfile();
            }
        });
        profilesView.setLayoutManager(new LinearLayoutManager(this));
        profilesView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        profilesType = getIntent().getIntExtra(PROFILES_TYPE_EXTRA, -1);
        setActionBarTitle();
        refreshProfiles();
    }

    private void setActionBarTitle() {
        switch (profilesType) {
            case SYNC_PROFILES:
                getSupportActionBar().setTitle("Sync profiles");
                break;
            case FILTER_PROFILES:
                getSupportActionBar().setTitle("Filter profiles");
                break;
        }
    }

    private List<Profile> getProfiles() {
        try {
            String filename = "";
            if(profilesType == ProfilesActivity.SYNC_PROFILES) {
                filename = MyApplication.SYNC_PROFILES_FILENAME;
            }
            else if(profilesType == ProfilesActivity.FILTER_PROFILES) {
                filename = MyApplication.FILTER_PROFILES_FILENAME;
            }
            return (List<Profile>) GeneralUtils.readObjectFromFile(new File(getFilesDir(), filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfiles();
    }

    public int getProfilesType() {
        return profilesType;
    }

    private void refreshProfiles() {
        adapter = new ProfileListAdapter(this, getProfiles());
        profilesView.setAdapter(adapter);
    }

    public void removeProfile(Profile profile) {
        adapter.removeProfile(profile);
    }

    public void notifyProfileChanged(int position) {
        adapter.notifyItemChanged(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profiles, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_add_profile) {
            newProfile();
            return true;
        }
        else if(item.getItemId() == R.id.action_about_profiles) {
            showInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void newProfile() {
        Class cls;
        switch (profilesType) {
            case SYNC_PROFILES:
                cls = EditSyncProfileActivity.class;
                break;
            case FILTER_PROFILES:
                cls = EditFilterProfileActivity.class;
                break;
            default:
                return;
        }
        Intent intent = new Intent(this, cls);
        intent.putExtra("defaultName", "Profile " + (adapter.getItemCount()+1));
        startActivity(intent);
    }

    private void showInfo() {
        String title = "";
        String info = "";
        switch (profilesType) {
            case SYNC_PROFILES:
                title = getResources().getString(R.string.about_sync_profiles_title);
                info = getResources().getString(R.string.about_sync_profiles);
                break;
            case FILTER_PROFILES:
                title = getResources().getString(R.string.about_filter_profiles_title);
                info = getResources().getString(R.string.about_filter_profiles);
                break;
        }
        InfoDialogFragment.showDialog(getSupportFragmentManager(), title, info);
    }

}
