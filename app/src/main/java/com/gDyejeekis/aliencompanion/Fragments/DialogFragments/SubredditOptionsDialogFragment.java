package com.gDyejeekis.aliencompanion.Fragments.DialogFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.Activities.EditSubredditsActivity;
import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.AsyncTasks.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.enums.UserActionType;

/**
 * Created by sound on 11/5/2015.
 */
public class SubredditOptionsDialogFragment extends ScalableDialogFragment {

    private EditSubredditsActivity activity;
    private String subreddit;
    //private int index;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        activity = (EditSubredditsActivity) getActivity();
        //index = getArguments().getInt("index");
        subreddit = getArguments().getString("subreddit");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_subreddit_options, container, false);
        TextView title = (TextView) view.findViewById(R.id.textView_title);
        title.setText(subreddit);
        TextView remove = (TextView) view.findViewById(R.id.textView_remove);
        TextView sub = (TextView) view.findViewById(R.id.textView_sub);
        TextView unsub = (TextView) view.findViewById(R.id.textView_unsub);
        TextView removeUnsub = (TextView) view.findViewById(R.id.textView_remove_unsub);

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                activity.removeSubreddit(subreddit);
            }
        });

        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(MyApplication.currentUser!=null) {
                    LoadUserActionTask task = new LoadUserActionTask(activity, UserActionType.subscribe, subreddit);
                    task.execute();
                }
                else ToastUtils.displayShortToast(activity, "Must be logged in to subscribe");
            }
        });

        unsub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(MyApplication.currentUser!=null) {
                    LoadUserActionTask task = new LoadUserActionTask(activity, UserActionType.unsubscribe, subreddit);
                    task.execute();
                }
                else ToastUtils.displayShortToast(activity, "Must be logged in to unsubscribe");
            }
        });

        removeUnsub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                activity.removeSubreddit(subreddit);
                if(MyApplication.currentUser!=null) {
                    LoadUserActionTask task = new LoadUserActionTask(activity, UserActionType.unsubscribe, subreddit);
                    task.execute();
                }
                else ToastUtils.displayShortToast(activity, "Must be logged in to unsubscribe");
            }
        });

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return view;
    }
}
