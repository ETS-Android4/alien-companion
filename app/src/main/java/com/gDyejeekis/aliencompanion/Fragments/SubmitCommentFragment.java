package com.gDyejeekis.aliencompanion.Fragments;


import android.os.AsyncTask;
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

import com.gDyejeekis.aliencompanion.AsyncTasks.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.MyHtmlTagHandler;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.enums.UserActionType;

import in.uncod.android.bypass.Bypass;

/**
 * A simple {@link Fragment} subclass.
 */
public class SubmitCommentFragment extends Fragment {

    private TextView originalCommentTextView;
    private EditText replyField;
    private LinearLayout layoutOriginalComment;
    //private String originalComment;
    private Comment originalComment;
    private String selfText;
    private String postName;
    private boolean edit;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        originalComment = (Comment) getActivity().getIntent().getSerializableExtra("originalComment");
        postName = getActivity().getIntent().getStringExtra("postName");
        selfText = getActivity().getIntent().getStringExtra("selfText");
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
                String title;
                if(selfText != null) {
                    title = "Edit self-post";
                    replyField.setText(selfText);
                }
                else {
                    title = "Edit comment";
                    replyField.setText(originalComment.getBody());
                }
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
            }
        }
        else {
            if(MyApplication.useBypassParsing) originalCommentTextView.setText(new Bypass().markdownToSpannable(originalComment.getBody()));
            else originalCommentTextView.setText(ConvertUtils.noTrailingwhiteLines(Html.fromHtml(originalComment.getBodyHTML(), null, new MyHtmlTagHandler())));
        }

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_submit) {
            String fullname = (selfText != null || !edit) ? postName : originalComment.getFullName();
            if(fullname==null) fullname = originalComment.getFullName();
            UserActionType actionType = (edit) ? UserActionType.edit : UserActionType.submitComment;
            //TODO: sort this shit

            ToastUtils.displayShortToast(getActivity(), "Submitting..");
            LoadUserActionTask task = new LoadUserActionTask(getActivity(), fullname, actionType, replyField.getText().toString());
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            //getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean displayConfirmDialog() {
        return replyField.getText().length() > 0;
    }

}
