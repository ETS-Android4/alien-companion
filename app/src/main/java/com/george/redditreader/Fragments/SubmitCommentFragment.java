package com.george.redditreader.Fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.george.redditreader.LoadTasks.LoadUserActionTask;
import com.george.redditreader.R;
import com.george.redditreader.Utils.ConvertUtils;
import com.george.redditreader.api.entity.Comment;
import com.george.redditreader.enums.UserActionType;

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

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        originalComment = (Comment) getActivity().getIntent().getSerializableExtra("originalComment");
        postName = getActivity().getIntent().getStringExtra("postName");
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

        if(originalComment == null) layoutOriginalComment.setVisibility(View.GONE);
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
            if(originalComment!=null) fullname = originalComment.getFullName();
            else fullname = postName;

            task = new LoadUserActionTask(getActivity(), fullname, UserActionType.submitComment, replyField.getText().toString());
            task.execute();

            getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
