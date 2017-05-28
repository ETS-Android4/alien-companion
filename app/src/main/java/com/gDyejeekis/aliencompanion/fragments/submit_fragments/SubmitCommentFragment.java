package com.gDyejeekis.aliencompanion.fragments.submit_fragments;


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

import com.gDyejeekis.aliencompanion.activities.PendingUserActionsActivity;
import com.gDyejeekis.aliencompanion.asynctask.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.asynctask.SaveOfflineActionTask;
import com.gDyejeekis.aliencompanion.models.offline_actions.CommentAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.HtmlTagHandler;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.enums.UserActionType;

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
    private CommentAction offlineAction;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        originalComment = (Comment) getActivity().getIntent().getSerializableExtra("originalComment");
        postName = getActivity().getIntent().getStringExtra("postName");
        selfText = getActivity().getIntent().getStringExtra("selfText");
        edit = getActivity().getIntent().getBooleanExtra("edit", false);
        offlineAction = (CommentAction) getActivity().getIntent().getSerializableExtra("offline");
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
                if(offlineAction != null) {
                    title = "Edit pending comment";
                    replyField.setText(offlineAction.getCommentText());
                }
                else if(selfText != null) {
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
            if(MyApplication.useMarkdownParsing) {

            }
            else {
                originalCommentTextView.setText(ConvertUtils.noTrailingwhiteLines(Html.fromHtml(originalComment.getBodyHTML(), null,
                        new HtmlTagHandler(originalCommentTextView.getPaint()))));
            }
        }

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_submit) {

            String commentText = replyField.getText().toString();

            if(offlineAction != null && edit) {
                int index = getActivity().getIntent().getIntExtra("index", -1);
                if(index != -1) {
                    getActivity().finish();
                    offlineAction.setCommentText(commentText);
                    PendingUserActionsActivity.editedIndex = index;
                    PendingUserActionsActivity.newAction = offlineAction;
                }
            }
            else {
                String fullname = (selfText != null || !edit) ? postName : originalComment.getFullName();
                if(fullname==null) fullname = originalComment.getFullName();

                if (GeneralUtils.isNetworkAvailable(getActivity())) {
                    UserActionType actionType = (edit) ? UserActionType.edit : UserActionType.submitComment;
                    ToastUtils.showToast(getActivity(), "Submitting..");
                    LoadUserActionTask task = new LoadUserActionTask(getActivity(), fullname, actionType, commentText);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    ToastUtils.showToast(getActivity(), "Adding to pending actions..");
                    OfflineUserAction action = new CommentAction(MyApplication.currentAccount.getUsername(), fullname, commentText);
                    SaveOfflineActionTask task = new SaveOfflineActionTask(getActivity(), action);
                    task.execute();
                }
            }

            //getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean displayConfirmDialog() {
        return replyField.getText().toString().replaceAll("\\s", "").length() > 0;
    }

}
