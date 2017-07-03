package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

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
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.activities.SearchActivity;
import com.gDyejeekis.aliencompanion.fragments.SearchFragment;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by George on 6/22/2015.
 */
public class SearchRedditDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    private AppCompatActivity activity;
    private EditText editText;
    private CheckBox checkBox;
    private String subreddit;
    private boolean isMulti;
    private SearchFragment searchFragment;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        activity = (AppCompatActivity) getActivity();
        searchFragment = null;
        if(!SearchActivity.isForeground) {
            subreddit = getArguments().getString("subreddit");
            isMulti = getArguments().getBoolean("isMulti", false);
        }
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
        if(subreddit == null || isMulti || subreddit.equalsIgnoreCase("all")) {
            checkBox.setChecked(false);
            checkBox.setVisibility(View.GONE);
        }
        else {
            checkBox.setText("Limit to " + subreddit);
        }
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
    public void onClick(View view) {
        if(view.getId() == R.id.button_cancel) {
            dismiss();
        }
        else {
            String query = editText.getText().toString();
            if(!query.trim().isEmpty()) {
                dismiss();
                if(!SearchActivity.isForeground) {
                    Intent intent = new Intent(activity, SearchActivity.class);
                    intent.putExtra("query", query);
                    if (checkBox.isChecked()) {
                        intent.putExtra("subreddit", subreddit);
                    }
                    activity.startActivity(intent);
                }
                else {
                    if(checkBox.isChecked()) searchFragment.subreddit = subreddit;
                    else searchFragment.subreddit = null;
                    searchFragment.changeQuery(query);
                }
            }
            else {
                editText.setText("");
                editText.setHint(R.string.enter_search_term);
                editText.setHintTextColor(getResources().getColor(R.color.red));
            }
        }
    }

}
