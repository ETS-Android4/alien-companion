package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.activities.EditSubredditsActivity;
import com.gDyejeekis.aliencompanion.asynctask.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.utils.RedditConstants;
import com.gDyejeekis.aliencompanion.enums.UserActionType;

/**
 * Created by sound on 11/3/2015.
 */
public class AddSubredditDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    private EditSubredditsActivity activity;
    private AutoCompleteTextView subredditField;
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

        int dropdownResource = (MyApplication.nightThemeEnabled) ? R.layout.simple_dropdown_item_1line_dark : android.R.layout.simple_dropdown_item_1line;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, dropdownResource, RedditConstants.popularSubreddits);

        Button cancelButton = (Button) view.findViewById(R.id.button_cancel);
        Button viewButton = (Button) view.findViewById(R.id.button_view);
        subscribeCheckbox = (CheckBox) view.findViewById(R.id.checkBox_subscribe);
        if(MyApplication.currentUser==null) subscribeCheckbox.setVisibility(View.GONE);
        subredditField = (AutoCompleteTextView) view.findViewById(R.id.editText_subreddit);
        subredditField.setAdapter(adapter);
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
                dismiss();
                //subreddit = subreddit.toLowerCase();
                //String capitalized = Character.toUpperCase(subreddit.charAt(0)) + subreddit.substring(1);

                if(subscribeCheckbox.isChecked()) {
                    LoadUserActionTask task = new LoadUserActionTask(activity, UserActionType.subscribe, subreddit);
                    task.execute();
                }
                activity.addSubreddit(subreddit);
            }
            else {
                subredditField.setText("");
                subredditField.setHint(R.string.enter_subreddit);
                subredditField.setHintTextColor(getResources().getColor(R.color.red));
            }
        }
    }
}
