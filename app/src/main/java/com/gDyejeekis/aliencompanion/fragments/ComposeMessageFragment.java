package com.gDyejeekis.aliencompanion.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.gDyejeekis.aliencompanion.asynctask.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

/**
 * Created by sound on 11/20/2015.
 */
public class ComposeMessageFragment extends Fragment {

    private EditText recipientField;
    private EditText subjectField;
    private EditText messageField;
    private String replyingToUser;
    private String replyingToSubject;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        replyingToUser = getActivity().getIntent().getStringExtra("recipient");
        replyingToSubject = getActivity().getIntent().getStringExtra("subject");
        if(replyingToSubject!=null) {
            if(!replyingToSubject.substring(0,3).equalsIgnoreCase("re:")) replyingToSubject = "re: " + replyingToSubject;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compose_message, container, false);
        recipientField = (EditText) view.findViewById(R.id.editText_recipient);
        subjectField = (EditText) view.findViewById(R.id.editText_subject);
        messageField = (EditText) view.findViewById(R.id.editText_message);
        if(replyingToUser !=null) {
            assert replyingToSubject != null;
            recipientField.setText(replyingToUser);
            subjectField.setText(replyingToSubject);
        }

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_submit) {
            String recipient = recipientField.getText().toString();
            recipient = recipient.replaceAll("\\s","");
            String subject = subjectField.getText().toString();
            String message = messageField.getText().toString();

            if(recipient.length()==0 || subject.replaceAll("\\s","").length()==0 || message.replaceAll("\\s","").length()==0) {
                if(recipient.length()==0) {
                    recipientField.setText("");
                    recipientField.setHint("enter a recipient");
                    recipientField.setHintTextColor(Color.RED);
                }
                if(subject.replaceAll("\\s","").length()==0) {
                    subjectField.setText("");
                    subjectField.setHint("enter a subject");
                    subjectField.setHintTextColor(Color.RED);
                }
                if(message.replaceAll("\\s","").length()==0) {
                    messageField.setText("");
                    messageField.setHint("enter a message");
                    messageField.setHintTextColor(Color.RED);
                }
            }
            else {
                //send message here
                //ToastUtils.showToast(getActivity(), "message ready for delivery");
                ToastUtils.showToast(getActivity(), "Sending message...");
                LoadUserActionTask task = new LoadUserActionTask(getActivity(), recipient, subject, message);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                //getActivity().finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
