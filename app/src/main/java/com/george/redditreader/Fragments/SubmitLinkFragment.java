package com.george.redditreader.Fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.george.redditreader.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SubmitLinkFragment extends Fragment {

    private String subreddit;
    private EditText titleField;
    private EditText urlField;
    private EditText subredditField;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        subreddit = getActivity().getIntent().getStringExtra("subreddit");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_submit_link, container, false);
        View view = inflater.inflate(R.layout.fragment_submit_link, container, false);
        titleField = (EditText) view.findViewById(R.id.editText_title);
        urlField = (EditText) view.findViewById(R.id.editText_url);
        subredditField = (EditText) view.findViewById(R.id.editText_subreddit);
        subredditField.setText(subreddit);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_submit) {

        }

        return super.onOptionsItemSelected(item);
    }

}
