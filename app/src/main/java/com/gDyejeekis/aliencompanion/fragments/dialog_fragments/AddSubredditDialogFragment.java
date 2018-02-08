package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.activities.EditSubredditsActivity;
import com.gDyejeekis.aliencompanion.api.entity.Subreddit;
import com.gDyejeekis.aliencompanion.asynctask.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.UserActionType;
import com.gDyejeekis.aliencompanion.views.DelayAutoCompleteTextView;
import com.gDyejeekis.aliencompanion.views.adapters.SubredditAutoCompleteAdapter;

/**
 * Created by sound on 11/3/2015.
 */
public class AddSubredditDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    private EditSubredditsActivity activity;
    private DelayAutoCompleteTextView subredditField;
    private CheckBox subscribeCheckbox;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        activity = (EditSubredditsActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_subreddit, container, false);

        Button cancelButton = (Button) view.findViewById(R.id.button_cancel);
        Button viewButton = (Button) view.findViewById(R.id.button_view);
        subscribeCheckbox = (CheckBox) view.findViewById(R.id.checkBox_subscribe);
        if(MyApplication.currentUser==null) subscribeCheckbox.setVisibility(View.GONE);
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
    public void onClick(View v) {
        if(v.getId() == R.id.button_cancel) {
            dismiss();
        }
        else {
            String subreddit = subredditField.getText().toString();
            subreddit = subreddit.replaceAll("\\s","");
            if(!subreddit.equals("")) {
                subredditField.setText("");
                subredditField.setHint("subreddit");
                subredditField.setHintTextColor(MyApplication.textHintColor);

                if(subscribeCheckbox.isChecked()) {
                    LoadUserActionTask task = new LoadUserActionTask(activity, UserActionType.subscribe, subreddit);
                    task.execute();
                }
                activity.addSubreddit(subreddit);
            }
            else {
                subredditField.setText("");
                subredditField.setHint("enter subreddit");
                subredditField.setHintTextColor(Color.RED);
            }
        }
    }
}
