package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Subreddit;
import com.gDyejeekis.aliencompanion.views.adapters.SubredditAutoCompleteAdapter;

/**
 * Created by sound on 1/29/2016.
 */
public class AddMultiredditDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    private ListView subredditsList;
    private ArrayAdapter<String> subredditsListAdapter;
    private AutoCompleteTextView subredditField;
    private Button cancelButton;
    private Button addSubredditButton;
    private Button createMultiButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_multireddit, container, false);
        subredditsList = (ListView) view.findViewById(R.id.listView_subreddits);
        subredditField = (AutoCompleteTextView) view.findViewById(R.id.editText_subreddit);
        cancelButton = (Button) view.findViewById(R.id.button_cancel);
        addSubredditButton = (Button) view.findViewById(R.id.button_addSubreddit);
        createMultiButton = (Button) view.findViewById(R.id.button_createMulti);

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

        subredditsListAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        subredditsList.setAdapter(subredditsListAdapter);

        cancelButton.setOnClickListener(this);
        addSubredditButton.setOnClickListener(this);
        createMultiButton.setOnClickListener(this);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        //getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_cancel:
                dismiss();
                break;
            case R.id.button_addSubreddit:
                String subreddit = subredditField.getText().toString();
                subreddit = subreddit.replaceAll("\\s","");
                if(!subreddit.equals("")) {
                    subredditField.setText("");
                    subredditsListAdapter.add(subreddit);
                    subredditsList.setSelection(subredditsListAdapter.getCount()-1);
                }
                break;
            case R.id.button_createMulti:
                //send create multi request and add it to multis list if successful
                break;
        }
    }
}
