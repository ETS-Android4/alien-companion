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
    private SyncProfileListAdapter adapter;

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
        refreshProfiles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshProfiles();
    }

    private void refreshProfiles() {
        adapter = new SyncProfileListAdapter(this);
        profilesView.setAdapter(adapter);
    }

    public void removeProfile(SyncProfile profile) {
        adapter.removeProfile(profile);
    }

    public void notifyProfileChanged(int position) {
        adapter.notifyItemChanged(position);
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
        else if(item.getItemId() == R.id.action_about_profiles) {
            showInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void newProfile() {
        Intent intent = new Intent(this, EditSyncProfileActivity.class);
        intent.putExtra("defaultName", "Profile " + (adapter.getItemCount()+1));
        startActivity(intent);
    }

    private void showInfo() {
        InfoDialogFragment dialog = new InfoDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", getResources().getString(R.string.about_sync_profiles_title));
        bundle.putString("info", getResources().getString(R.string.about_sync_profiles));
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "dialog");
    }

}
