package com.dyejeekis.aliencompanion.Fragments.DialogFragments;


import android.app.DialogFragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.Activities.SubredditActivity;
import com.dyejeekis.aliencompanion.Fragments.PostListFragment;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.api.retrieval.params.SubmissionSort;
import com.dyejeekis.aliencompanion.api.utils.RedditConstants;

/**
 * A simple {@link Fragment} subclass.
 */
public class EnterRedditDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    private MainActivity activity;
    private AutoCompleteTextView subredditField;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_reddit, container, false);

        int dropdownResource = (MainActivity.nightThemeEnabled) ? R.layout.simple_dropdown_item_1line_dark : android.R.layout.simple_dropdown_item_1line;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, dropdownResource, RedditConstants.popularSubreddits);

        Button cancelButton = (Button) view.findViewById(R.id.button_cancel);
        Button viewButton = (Button) view.findViewById(R.id.button_view);
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

    //@Override
    //public void onResume() {
    //    super.onResume();
    //    setDialogWidth();
    //}
//
    //@Override
    //public void onConfigurationChanged(Configuration newConfig) {
    //    super.onConfigurationChanged(newConfig);
    //    setDialogWidth();
    //}
//
    //private void setDialogWidth() {
    //    Window window = getDialog().getWindow();
    //    int width = 3 * getResources().getDisplayMetrics().widthPixels / 4;
    //    window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
    //}

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.button_cancel) {
            dismiss();
        }
        else {
            String subreddit = subredditField.getText().toString();
            subreddit = subreddit.replaceAll("\\s","");
            if(!subreddit.equals("")) {
                dismiss();
                subreddit = subreddit.toLowerCase();
                //String capitalized = Character.toUpperCase(subreddit.charAt(0)) + subreddit.substring(1);
                if(MainActivity.prefs.getBoolean("newSubredditWindow", false)) {
                    Intent intent = new Intent(activity, SubredditActivity.class);
                    intent.putExtra("subreddit", subreddit);
                    startActivity(intent);
                }
                else {
                    activity.getNavDrawerAdapter().notifyDataSetChanged();
                    PostListFragment listFragment = activity.getListFragment();
                    listFragment.changeSubreddit(subreddit);
                    //listFragment.setSubreddit(subreddit);
                    //listFragment.setSubmissionSort(SubmissionSort.HOT);
                    //listFragment.refreshList();
                }
            }
            else {
                subredditField.setText("");
                subredditField.setHint(R.string.enter_subreddit);
                subredditField.setHintTextColor(getResources().getColor(R.color.red));
            }
        }
    }

}
