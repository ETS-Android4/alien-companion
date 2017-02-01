package com.gDyejeekis.aliencompanion.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.AddMultiredditDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.MultiredditOptionsDialogFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;

/**
 * Created by sound on 1/29/2016.
 */
public class EditMultisActivity extends BackNavActivity {

    private ArrayList<String> multireddits;
    private DragSortListView dslv;
    private ArrayAdapter adapter;
    public static boolean changesMade;

    @Override
    public void onCreate(Bundle bundle) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(bundle);
        setContentView(R.layout.activity_edit_subreddits);
        MyApplication.setupStandardToolbar(this);

        changesMade = false;
        dslv = (DragSortListView) findViewById(R.id.dslv);
        multireddits = getIntent().getStringArrayListExtra("multis");
        adapter = new ArrayAdapter(this, R.layout.draggable_subreddit_item, R.id.subreddit_text, multireddits);
        dslv.setAdapter(adapter);
        dslv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                MultiredditOptionsDialogFragment dialog = new MultiredditOptionsDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("multi", multireddits.get(position));
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "dialog");
                return true;
            }
        });
        dslv.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                if (from != to) {
                    changesMade= true;
                    String temp = multireddits.get(from);
                    multireddits.remove(from);
                    multireddits.add(to, temp);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        dslv.setRemoveListener(new DragSortListView.RemoveListener() {
            @Override
            public void remove(int which) {
                changesMade = true;
                multireddits.remove(which);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_multis, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_multi:
                AddMultiredditDialogFragment dialog = new AddMultiredditDialogFragment();
                dialog.show(getSupportFragmentManager(), "dialog");
                return true;
            case R.id.action_refresh_multis:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(changesMade && MyApplication.currentAccount!=null) MyApplication.currentAccount.setMultireddits(multireddits);
        super.onBackPressed();
    }

    public void addMultireddit(String subreddit) {
        changesMade = true;
        multireddits.add(subreddit);
        adapter.notifyDataSetChanged();
    }

    public void removeMultireddit(String subreddit) {
        changesMade = true;
        multireddits.remove(subreddit);
        adapter.notifyDataSetChanged();
    }
}
