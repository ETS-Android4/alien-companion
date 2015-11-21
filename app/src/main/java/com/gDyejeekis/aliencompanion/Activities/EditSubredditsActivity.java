package com.gDyejeekis.aliencompanion.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.AddSubredditDialogFragment;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.SubredditOptionsDialogFragment;
import com.gDyejeekis.aliencompanion.R;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;

/**
 * Created by sound on 10/29/2015.
 */
public class EditSubredditsActivity extends BackNavActivity {

    private ArrayList<String> subreddits;
    private DragSortListView dslv;
    private ArrayAdapter adapter;
    public static boolean changesMade;

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

        changesMade = false;
        dslv = (DragSortListView) findViewById(R.id.dslv);
        subreddits = getIntent().getStringArrayListExtra("subreddits");
        adapter = new ArrayAdapter(this, R.layout.draggable_subreddit_item, R.id.subreddit_text, subreddits);
        dslv.setAdapter(adapter);
        dslv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                SubredditOptionsDialogFragment dialog = new SubredditOptionsDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("subreddit", subreddits.get(position));
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(), "dialog");
                return true;
            }
        });
        dslv.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                if (from != to) {
                    changesMade= true;
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
                changesMade = true;
                subreddits.remove(which);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_subreddits, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_subreddit:
                AddSubredditDialogFragment dialog = new AddSubredditDialogFragment();
                dialog.show(getFragmentManager(), "dialog");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(changesMade && MainActivity.currentAccount!=null) MainActivity.currentAccount.setSubreddits(subreddits);
        super.onBackPressed();
    }

    public void addSubreddit(String subreddit) {
        changesMade = true;
        subreddits.add(subreddit);
        adapter.notifyDataSetChanged();
    }

    public void removeSubreddit(String subreddit) {
        changesMade = true;
        subreddits.remove(subreddit);
        adapter.notifyDataSetChanged();
    }

}
