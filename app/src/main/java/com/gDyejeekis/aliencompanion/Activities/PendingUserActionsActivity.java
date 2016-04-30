package com.gDyejeekis.aliencompanion.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.gDyejeekis.aliencompanion.Adapters.PendingActionsAdapter;
import com.gDyejeekis.aliencompanion.Models.OfflineActions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Views.DividerItemDecoration;

/**
 * Created by sound on 4/9/2016.
 */
public class PendingUserActionsActivity extends BackNavActivity {

    private RecyclerView pendingActionsView;
    private PendingActionsAdapter adapter;
    public static boolean isActive;
    public static int editedIndex = -1;
    public static OfflineUserAction newAction;

    @Override
    public void onCreate(Bundle bundle) {
        isActive = true;
        if(MyApplication.nightThemeEnabled) {
            getTheme().applyStyle(R.style.PopupDarkTheme, true);
            getTheme().applyStyle(R.style.selectedTheme_night, true);
        }
        else getTheme().applyStyle(R.style.selectedTheme_day, true);
        super.onCreate(bundle);
        setContentView(R.layout.activity_pending_actions);
        if(MyApplication.nightThemeEnabled)
            getTheme().applyStyle(R.style.Theme_AppCompat_Dialog, true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(MyApplication.nightThemeEnabled) toolbar.setPopupTheme(R.style.OverflowStyleDark);
        toolbar.setBackgroundColor(MyApplication.currentColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(MyApplication.colorPrimaryDark);
        toolbar.setNavigationIcon(MyApplication.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pendingActionsView = (RecyclerView) findViewById(R.id.recyclerView_pendingActions);
        adapter = new PendingActionsAdapter(this);
        pendingActionsView.setLayoutManager(new LinearLayoutManager(this));
        pendingActionsView.setAdapter(adapter);
        pendingActionsView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    }

    public PendingActionsAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(editedIndex != -1) {
            adapter.actionEdited(editedIndex, newAction);
            editedIndex = -1;
            newAction = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pending_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_execute_all_offline_actions:
                adapter.executeAllActions();
                return true;
            case R.id.action_cancel_all_offline_actions:
                adapter.cancelAllActions();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        isActive = false;
        super.onDestroy();
    }
}
