package com.dyejeekis.aliencompanion.Fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dyejeekis.aliencompanion.LoadTasks.LoadUserActionTask;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.Utils.ConvertUtils;
import com.dyejeekis.aliencompanion.api.entity.Comment;
import com.dyejeekis.aliencompanion.enums.UserActionType;

/**
 * A simple {@link Fragment} subclass.
 */
public class SubmitCommentFragment extends Fragment {

    private TextView originalCommentTextView;
    private EditText replyField;
    private LinearLayout layoutOriginalComment;
    //private String originalComment;
    private Comment originalComment;
    private String postName;
    private boolean edit;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        originalComment = (Comment) getActivity().getIntent().getSerializableExtra("originalComment");
        postName = getActivity().getIntent().getStringExtra("postName");
        edit = getActivity().getIntent().getBooleanExtra("edit", false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_submit_comment, container, false);
        View view = inflater.inflate(R.layout.fragment_submit_comment, container, false);
        layoutOriginalComment = (LinearLayout) view.findViewById(R.id.layout_originalComment);
        originalCommentTextView = (TextView) view.findViewById(R.id.txtView_originalComment);
        replyField = (EditText) view.findViewById(R.id.editText_reply);
        replyField.requestFocus();

        if(originalComment == null || edit) {
            layoutOriginalComment.setVisibility(View.GONE);
            if(edit) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Edit comment");
                replyField.setText(originalComment.getBody());
            }
        }
        else {
            originalCommentTextView.setText(ConvertUtils.noTrailingwhiteLines(Html.fromHtml(originalComment.getBodyHTML())));
        }

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_submit) {
            LoadUserActionTask task;
            String fullname;
            UserActionType actionType = UserActionType.submitComment;
            if(originalComment!=null || edit) {
                fullname = originalComment.getFullName();
                if(edit) actionType = UserActionType.edit;
            }
            else fullname = postName;

            task = new LoadUserActionTask(getActivity(), fullname, actionType, replyField.getText().toString());
            task.execute();

            getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
