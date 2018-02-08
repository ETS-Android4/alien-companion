package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.SubredditActivity;
import com.gDyejeekis.aliencompanion.api.entity.Subreddit;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.views.DelayAutoCompleteTextView;
import com.gDyejeekis.aliencompanion.views.adapters.SubredditAutoCompleteAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class EnterRedditDialogFragment extends ScalableDialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private DelayAutoCompleteTextView subredditField;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_reddit, container, false);

        Button cancelButton = (Button) view.findViewById(R.id.button_cancel);
        Button viewButton = (Button) view.findViewById(R.id.button_view);

        CheckBox newWindowCheckbox = (CheckBox) view.findViewById(R.id.checkBox_new_window);
        newWindowCheckbox.setChecked(MyApplication.prefs.getBoolean("newSubredditWindow", false));
        newWindowCheckbox.setOnCheckedChangeListener(this);

        subredditField = view.findViewById(R.id.editText_subreddit);
        subredditField.setAdapter(new SubredditAutoCompleteAdapter(getContext()));
        subredditField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Subreddit subreddit = (Subreddit) adapterView.getItemAtPosition(i);
                String name = subreddit.getDisplayName();
                subredditField.setText(name);
                subredditField.setSelection(name.length());
            }
        });
        subredditField.requestFocus();

        cancelButton.setOnClickListener(this);
        viewButton.setOnClickListener(this);
        subredditField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onClick(v);
                return true;
            }
        });

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences.Editor editor = MyApplication.prefs.edit();
        editor.putBoolean("newSubredditWindow", isChecked);
        editor.commit();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.button_cancel) {
            dismiss();
        }
        else {
            String subreddit = subredditField.getText().toString();
            subreddit = subreddit.replaceAll("\\s","");
            if(subreddit.isEmpty()) {
                GeneralUtils.clearField(subredditField, "enter subreddit", Color.RED);
            }
            else if(!GeneralUtils.isValidSubreddit(subreddit)) {
                GeneralUtils.clearField(subredditField, "subreddit");
                ToastUtils.showToast(getActivity(), "Subreddit can contain only alphanumeric characters (a-z,0-9) and underscores (_)");
            }
            else {
                dismiss();
                subreddit = subreddit.toLowerCase();
                if(MyApplication.prefs.getBoolean("newSubredditWindow", false)) {
                    Intent intent = new Intent(getActivity(), SubredditActivity.class);
                    intent.putExtra("subreddit", subreddit);
                    startActivity(intent);
                }
                else {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.getNavDrawerAdapter().notifyDataSetChanged();
                    PostListFragment listFragment = activity.getListFragment();
                    listFragment.isMulti = false;
                    listFragment.isOther = false;
                    listFragment.changeSubreddit(subreddit);
                }
            }
        }
    }

}
