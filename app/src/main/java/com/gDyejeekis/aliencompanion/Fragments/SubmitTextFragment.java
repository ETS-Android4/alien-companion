package com.gDyejeekis.aliencompanion.Fragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.gDyejeekis.aliencompanion.AsyncTasks.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.enums.UserActionType;

/**
 * A simple {@link Fragment} subclass.
 */
public class SubmitTextFragment extends Fragment {

    private String subreddit;
    private EditText titleField;
    private EditText textField;
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
        //return inflater.inflate(R.layout.fragment_submit_text, container, false);
        View view = inflater.inflate(R.layout.fragment_submit_text, container, false);
        titleField = (EditText) view.findViewById(R.id.editText_title);
        textField = (EditText) view.findViewById(R.id.editText_text);
        subredditField = (EditText) view.findViewById(R.id.editText_subreddit);
        subredditField.setText(subreddit);
        titleField.requestFocus();

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_submit) {
            String title = titleField.getText().toString();
            String text = textField.getText().toString();
            String subreddit = subredditField.getText().toString();
            if(title.replaceAll("\\s","").length()==0) title = "";
            subreddit = subreddit.replaceAll("\\s","");
            if(title.length()==0 || subreddit.length()==0) {
                //ToastUtils.displayShortToast(getActivity(), "All fields are required");
                if(title.length()==0) {
                    titleField.setText("");
                    titleField.setHint("enter a title");
                    titleField.setHintTextColor(Color.RED);
                }
                if(subreddit.length()==0) {
                    subredditField.setText("");
                    subredditField.setHint("enter subreddit");
                    subredditField.setHintTextColor(Color.RED);
                }
            }
            else {
                ToastUtils.displayShortToast(getActivity(), "Submitting..");
                LoadUserActionTask task = new LoadUserActionTask(getActivity(), UserActionType.submitText, title, text, subreddit);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
