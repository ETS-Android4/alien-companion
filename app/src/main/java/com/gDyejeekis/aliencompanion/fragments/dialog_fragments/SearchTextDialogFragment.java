package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;

/**
 * Created by George on 3/17/2017.
 */

public class SearchTextDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    private EditText searchField;

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_cancel) {
            dismiss();
        }
        else {
            PostFragment fragment;
            // TODO: 3/17/2017 maybe add abstraction
            if(getActivity() instanceof PostActivity) {
                fragment = ((PostActivity)getActivity()).getPostFragment();
            }
            else {
                fragment = ((MainActivity)getActivity()).getPostFragment();
            }
            fragment.commentNavListener.setSearchQuery(searchField.getText().toString());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_text, container, false);
        searchField = (EditText) view.findViewById(R.id.editText_search);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }
}
