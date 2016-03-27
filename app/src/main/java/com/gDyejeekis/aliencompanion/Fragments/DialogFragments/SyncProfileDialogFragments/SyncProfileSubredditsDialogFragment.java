package com.gDyejeekis.aliencompanion.Fragments.DialogFragments.SyncProfileDialogFragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.Activities.SyncProfilesActivity;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.ScalableDialogFragment;
import com.gDyejeekis.aliencompanion.Models.SyncProfile;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.Views.ListViewMaxHeight;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 2/10/2016.
 */
public class SyncProfileSubredditsDialogFragment extends ScalableDialogFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    static class SubredditsList extends ArrayList<String> {

        public SubredditsList(List<String> list) {
            super(list);
        }

        @Override
        public boolean contains(Object o) {
            String paramStr = (String)o;
            for (String s : this) {
                if (paramStr.equalsIgnoreCase(s)) return true;
            }
            return false;
        }
    }

    private SyncProfile syncProfile;
    private ListViewMaxHeight subredditsList;
    private ArrayAdapter<String> adapter;
    private SubredditsList subreddits;
    private SubredditsList oldSubreddits;
    private AutoCompleteTextView subredditField;
    private CheckBox isMulti;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        syncProfile = (SyncProfile) getArguments().getSerializable("profile");
        if(syncProfile!=null) {
            subreddits = new SubredditsList(syncProfile.getSubreddits());
            oldSubreddits = new SubredditsList(subreddits);
            adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, subreddits);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sync_profile_subreddits, container, false);
        TextView title = (TextView) view.findViewById(R.id.textView_title);
        subredditsList = (ListViewMaxHeight) view.findViewById(R.id.listView_subreddits);
        subredditField = (AutoCompleteTextView) view.findViewById(R.id.editText_subreddit);
        isMulti = (CheckBox) view.findViewById(R.id.checkBox_isMulti);
        Button cancelButton = (Button) view.findViewById(R.id.button_cancel);
        Button addSubredditButton = (Button) view.findViewById(R.id.button_addSubreddit);
        Button doneButton = (Button) view.findViewById(R.id.button_done);

        int dropdownResource = (MyApplication.nightThemeEnabled) ? R.layout.simple_dropdown_item_1line_dark : android.R.layout.simple_dropdown_item_1line;
        ArrayAdapter<String> fieldAdapter = new ArrayAdapter<>(getActivity(), dropdownResource, RedditConstants.popularSubreddits);
        subredditField.setAdapter(fieldAdapter);
        subredditField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                addSubredditToList();
                return true;
            }
        });
        subredditField.requestFocus();

        title.setText(syncProfile.getName());
        int listViewmaxHeight = GeneralUtils.getPortraitHeight(getActivity()) / 5;
        subredditsList.setMaxHeight(listViewmaxHeight);
        subredditsList.setAdapter(adapter);
        subredditsList.setOnItemClickListener(this);
        cancelButton.setOnClickListener(this);
        addSubredditButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        subreddits.remove(i);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(!oldSubreddits.equals(subreddits)) {
            ((SyncProfilesActivity) getActivity()).changesMade = true;
            syncProfile.setSubreddits(subreddits);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_cancel:
                subreddits = oldSubreddits;
                dismiss();
                break;
            case R.id.button_addSubreddit:
                addSubredditToList();
                break;
            case R.id.button_done:
                dismiss();
                if(getArguments().getBoolean("showSchedule") && subreddits.size() > 0) {
                    ((SyncProfilesActivity) getActivity()).getAdapter().showScheduleDialog(syncProfile, true);
                }
                break;
        }
    }

    private void addSubredditToList() {
        String subreddit = subredditField.getText().toString();
        subreddit = subreddit.replaceAll("\\s","");

        if(!subreddit.equals("")) {
            if(isMulti.isChecked()) {
                isMulti.setChecked(false);
                subreddit = subreddit.concat(" (multi)");
            }
            subredditField.setText("");
            if(subreddits.contains(subreddit)) {
                ToastUtils.displayShortToast(getActivity(), "Item already on the list");
            }
            else {
                subreddits.add(subreddit);
            }
            adapter.notifyDataSetChanged();
            subredditsList.setSelection(adapter.getCount() - 1);
        }
    }
}
