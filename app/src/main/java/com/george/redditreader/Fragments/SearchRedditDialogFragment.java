package com.george.redditreader.Fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.george.redditreader.Activities.SearchActivity;
import com.george.redditreader.R;

/**
 * Created by George on 6/22/2015.
 */
public class SearchRedditDialogFragment extends DialogFragment implements View.OnClickListener {

    private AppCompatActivity activity;
    private EditText editText;
    private CheckBox checkBox;
    private String subreddit;
    private SearchFragment searchFragment;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        activity = (AppCompatActivity) getActivity();
        searchFragment = null;
        if(!SearchActivity.activityStarted)
            subreddit = getArguments().getString("subreddit");
        else {
            searchFragment = ((SearchActivity) activity).getSearchFragment();
            subreddit = searchFragment.getSubreddit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_reddit, container, false);
        Button cancelButton = (Button) view.findViewById(R.id.button_cancel);
        Button viewButton = (Button) view.findViewById(R.id.button_view);
        checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        if(subreddit == null) {
            checkBox.setChecked(false);
            checkBox.setVisibility(View.GONE);
        }
        else checkBox.setText("Limit to " + subreddit);
        editText = (EditText) view.findViewById(R.id.editText_subreddit);
        editText.requestFocus();

        cancelButton.setOnClickListener(this);
        viewButton.setOnClickListener(this);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
    public void onResume() {
        super.onResume();
        setDialogWidth();
    }

    private void setDialogWidth() {
        Window window = getDialog().getWindow();
        int width = 3 * getResources().getDisplayMetrics().widthPixels / 4;
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.button_cancel) {
            dismiss();
        }
        else {
            String query = editText.getText().toString();
            if(!query.equals("")) {
                dismiss();
                //String capitalized = Character.toUpperCase(subreddit.charAt(0)) + subreddit.substring(1);
                if(!SearchActivity.activityStarted) {
                    Intent intent = new Intent(activity, SearchActivity.class);
                    intent.putExtra("query", query);
                    if (checkBox.isChecked()) {
                        intent.putExtra("subreddit", subreddit);
                        //Log.d("subreddit extra", "sent");
                    }
                    activity.startActivity(intent);
                }
                else {
                    searchFragment.setSearchQuery(query);
                    searchFragment.setActionBarTitle();
                    searchFragment.refreshList();
                }
            }
            else {
                editText.setHint(R.string.enter_search_term);
                editText.setHintTextColor(getResources().getColor(R.color.red));
            }
        }
    }

}
