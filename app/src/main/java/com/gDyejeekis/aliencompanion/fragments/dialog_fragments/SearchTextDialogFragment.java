package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;

/**
 * Created by George on 3/17/2017.
 */

public class SearchTextDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    private EditText searchField;
    private CheckBox matchCase;

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_cancel) {
            dismiss();
        }
        else {
            String searchTerm = searchField.getText().toString();
            if(searchTerm.replace(" ", "").length() == 0) {
                searchField.setText("");
                searchField.setHintTextColor(Color.RED);
                searchField.setHint("enter search term");
            }
            else {
                dismiss();
                PostFragment fragment;
                // TODO: 3/17/2017 maybe add abstraction
                if (getActivity() instanceof PostActivity) {
                    fragment = ((PostActivity) getActivity()).getPostFragment();
                } else {
                    fragment = ((MainActivity) getActivity()).getPostFragment();
                }
                fragment.commentNavListener.setSearchQuery(searchTerm, matchCase.isChecked());
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_text, container, false);
        matchCase = (CheckBox) view.findViewById(R.id.checkBox_match_case);
        matchCase.setChecked(getArguments().getBoolean("matchCase", false));
        searchField = (EditText) view.findViewById(R.id.editText_search);
        String searchTerm = getArguments().getString("searchTerm", null);
        if(searchTerm!=null) {
            searchField.setText(searchTerm);
            searchField.selectAll();
        }
        searchField.requestFocus();
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onClick(v);
                return true;
            }
        });
        Button cancel = (Button) view.findViewById(R.id.button_cancel);
        Button search = (Button) view.findViewById(R.id.button_search);
        cancel.setOnClickListener(this);
        search.setOnClickListener(this);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }
}
