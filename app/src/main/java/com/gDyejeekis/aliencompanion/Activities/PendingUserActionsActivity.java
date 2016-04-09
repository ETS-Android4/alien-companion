package com.gDyejeekis.aliencompanion.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.gDyejeekis.aliencompanion.Adapters.PendingActionsAdapter;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Views.DividerItemDecoration;

/**
 * Created by sound on 4/9/2016.
 */
public class PendingUserActionsActivity extends BackNavActivity {

    private RecyclerView pendingActionsView;
    private PendingActionsAdapter adapter;

    @Override
    public void onCreate(Bundle bundle) {
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
        adapter = new PendingActionsAdapter();
        pendingActionsView.setLayoutManager(new LinearLayoutManager(this));
        pendingActionsView.setAdapter(adapter);
        pendingActionsView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    }
}
