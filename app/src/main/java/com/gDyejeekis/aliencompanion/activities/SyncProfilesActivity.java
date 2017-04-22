package com.gDyejeekis.aliencompanion.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gDyejeekis.aliencompanion.views.adapters.SyncProfileListAdapter;
import com.gDyejeekis.aliencompanion.models.SyncProfile;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.views.DividerItemDecoration;

/**
 * Created by sound on 2/4/2016.
 */
public class SyncProfilesActivity extends ToolbarActivity {

    private RecyclerView profilesView;
    //private SyncProfileListAdapter adapter;

    //public boolean changesMade = false;

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_profiles);
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
        setProfilesViewAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setProfilesViewAdapter();
    }

    private void setProfilesViewAdapter() {
        SyncProfileListAdapter adapter = new SyncProfileListAdapter(this);
        profilesView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sync_profiles, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_add_profile) {
            newProfile();
            return true;
        }
        //else if(item.getItemId() == R.id.action_sort_by_alpha) {
        //    adapter.sortProfilesByAlpha();
        //    return true;
        //}
        return super.onOptionsItemSelected(item);
    }

    private void newProfile() {
        startActivity(new Intent(this, EditSyncProfileActivity.class));
    }

    //@Override
    //public void onStop() {
    //    adapter.saveSyncProfiles();
    //    super.onStop();
    //}

    //@Override
    //public void onBackPressed() {
    //    if(adapter.renamingProfilePosition != -1) {
    //        SyncProfile profile = new SyncProfile(adapter.getItemAt(adapter.renamingProfilePosition));
    //        adapter.profileRenamedAt(adapter.renamingProfilePosition, profile, false);
    //    }
    //    else if(adapter.addingNewProfile) {
    //        adapter.removeTempProfile();
    //    }
    //    else {
    //        if(changesMade) {
    //            showSaveChangesDialog();
    //        }
    //        else {
    //            super.onBackPressed();
    //        }
    //    }
    //}

    //private void showSaveChangesDialog() {
    //    new AlertDialog.Builder(this).setMessage("Save changes?").setPositiveButton("Yes", this).setNegativeButton("No", this).show();
    //}

    //@Override
    //public void onClick(DialogInterface dialogInterface, int which) {
    //    switch (which) {
    //        case DialogInterface.BUTTON_POSITIVE:
    //            adapter.saveSyncProfiles();
    //            adapter.unscheduleDeletedProfiles();
    //            break;
    //        case DialogInterface.BUTTON_NEGATIVE:
    //            break;
    //    }
    //    super.onBackPressed();
    //}

    //public SyncProfileListAdapter getAdapter() {
    //    return adapter;
    //}

}
