package com.dyejeekis.aliencompanion.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.dyejeekis.aliencompanion.R;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;

/**
 * Created by sound on 10/29/2015.
 */
public class EditSubredditsActivity extends BackNavActivity {

    private ArrayList<String> subreddits;
    private DragSortListView dslv;
    private ArrayAdapter adapter;

    @Override
    public void onCreate(Bundle bundle) {
        //getTheme().applyStyle(MainActivity.fontStyle, true);
        if(MainActivity.nightThemeEnabled) {
            getTheme().applyStyle(R.style.PopupDarkTheme, true);
            getTheme().applyStyle(R.style.selectedTheme_night, true);
        }
        else getTheme().applyStyle(R.style.selectedTheme_day, true);
        super.onCreate(bundle);
        setContentView(R.layout.activity_edit_subreddits);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if(MainActivity.nightThemeEnabled) toolbar.setPopupTheme(R.style.OverflowStyleDark);
        toolbar.setBackgroundColor(MainActivity.currentColor);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) getWindow().setStatusBarColor(MainActivity.colorPrimaryDark);
        toolbar.setNavigationIcon(MainActivity.homeAsUpIndicator);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dslv = (DragSortListView) findViewById(R.id.dslv);
        subreddits = getIntent().getStringArrayListExtra("subreddits");
        adapter = new ArrayAdapter(this, R.layout.draggable_subreddit_item, R.id.subreddit_text, subreddits);
        dslv.setAdapter(adapter);
        dslv.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                if (from != to) {
                    String temp = subreddits.get(from);
                    subreddits.remove(from);
                    subreddits.add(to, temp);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        dslv.setRemoveListener(new DragSortListView.RemoveListener() {
            @Override
            public void remove(int which) {
                subreddits.remove(which);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
