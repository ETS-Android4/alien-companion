package com.gDyejeekis.aliencompanion.Fragments.DialogFragments;

import android.app.DialogFragment;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.AsyncTasks.LoadSubredditSidebarTask;
import com.gDyejeekis.aliencompanion.AsyncTasks.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.Fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.Utils.MyHtmlTagHandler;
import com.gDyejeekis.aliencompanion.Utils.MyLinkMovementMethod;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.SubredditInfo;
import com.gDyejeekis.aliencompanion.enums.UserActionType;

/**
 * Created by sound on 2/29/2016.
 */
public class SubredditSidebarDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final int UNSUB_COLOR = Color.parseColor("#ff4d4d");

    public static final int SUB_COLOR = Color.parseColor("#66ff66");

    public static final String UNSUB_TEXT = "UNSUBSCRIBE";

    public static final String SUB_TEXT = "SUBSCRIBE";

    public static final String UNSUBBING_TEXT = "UNSUBSCRIBING..";

    public static final String SUBBING_TEXT = "SUBSCRIBING..";

    private ScrollView layoutContent;
    private ProgressBar progressBar;
    private Button buttonSubUnsub;
    private TextView textViewSubscribersCount;
    private TextView textViewActiveCount;
    private TextView textViewSubmitText;
    private TextView textViewPublicDescription;
    private TextView textViewDescription;

    private String subreddit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        subreddit = getArguments().getString("subreddit");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subreddit_sidebar, container, false);

        layoutContent = (ScrollView) view.findViewById(R.id.layout_content);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar3);
        buttonSubUnsub = (Button) view.findViewById(R.id.button_sub_unsub);
        textViewSubscribersCount = (TextView) view.findViewById(R.id.textView_subs);
        textViewActiveCount = (TextView) view.findViewById(R.id.textView_active_accounts);
        textViewSubmitText = (TextView) view.findViewById(R.id.textView_submit_text);
        textViewPublicDescription = (TextView) view.findViewById(R.id.textView_public_descr);
        textViewDescription = (TextView) view.findViewById(R.id.textView_description);

        progressBar.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);

        LoadSubredditSidebarTask task = new LoadSubredditSidebarTask(subreddit, this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onClick(View view) {
        if(MyApplication.currentUser != null) {
            if (buttonSubUnsub.getText().equals(SUB_TEXT)) {
                buttonSubUnsub.setEnabled(false);
                buttonSubUnsub.setText(SUBBING_TEXT);
                //start unsub task
                LoadUserActionTask task = new LoadUserActionTask(getActivity(), UserActionType.subscribe, subreddit, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                buttonSubUnsub.setEnabled(false);
                buttonSubUnsub.setText(UNSUBBING_TEXT);
                //start unsub task
                LoadUserActionTask task = new LoadUserActionTask(getActivity(), UserActionType.unsubscribe, subreddit, this);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        else {
            ToastUtils.displayShortToast(getActivity(), "Must be logged in to do that");
        }
    }

    public void bindData(SubredditInfo info) {
        //bind data to button
        if(info.userIsSubscriber) {
            setButtonToUnsub();
        }
        else {
            setButtonToSub();
        }

        //current subscribers
        textViewSubscribersCount.setText(String.valueOf(info.subscribers));
        //active accounts
        textViewActiveCount.setText(String.valueOf(info.activeAccounts));

        Html.TagHandler tagHandler = new MyHtmlTagHandler();
        //submit text
        SpannableStringBuilder stringBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(info.submitTextHtml, null, tagHandler));
        stringBuilder = ConvertUtils.modifyURLSpan(getActivity(), stringBuilder);
        textViewSubmitText.setText(stringBuilder);
        textViewSubmitText.setMovementMethod(MyLinkMovementMethod.getInstance());
        //public description
        stringBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(info.publicDescriptionHtml, null, tagHandler));
        stringBuilder = ConvertUtils.modifyURLSpan(getActivity(), stringBuilder);
        textViewPublicDescription.setText(stringBuilder);
        textViewPublicDescription.setMovementMethod(MyLinkMovementMethod.getInstance());
        //description
        stringBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(info.descriptionHtml, null, tagHandler));
        stringBuilder = ConvertUtils.modifyURLSpan(getActivity(), stringBuilder);
        textViewDescription.setText(stringBuilder);
        textViewDescription.setMovementMethod(MyLinkMovementMethod.getInstance());

        progressBar.setVisibility(View.GONE);
        layoutContent.setVisibility(View.VISIBLE);
    }

    private void setButtonToSub() {
        buttonSubUnsub.setText(SUB_TEXT);
        buttonSubUnsub.setBackgroundColor(SUB_COLOR);
        buttonSubUnsub.setOnClickListener(this);
        buttonSubUnsub.setEnabled(true);
    }

    private void setButtonToUnsub() {
        buttonSubUnsub.setText(UNSUB_TEXT);
        buttonSubUnsub.setBackgroundColor(UNSUB_COLOR);
        buttonSubUnsub.setOnClickListener(this);
        buttonSubUnsub.setEnabled(true);
    }

    public void updateSubUnsubButton(boolean requestSuccessful) {
        if(buttonSubUnsub.getText().equals(UNSUBBING_TEXT)) {
            if(requestSuccessful) {
                setButtonToSub();
            }
            else {
                setButtonToUnsub();
            }
        }
        else {
            if(requestSuccessful) {
                setButtonToUnsub();
            }
            else {
                setButtonToSub();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setDialogDimens();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setDialogDimens();
    }

    private void setDialogDimens() {
        Window window = getDialog().getWindow();
        int width = 95 * getResources().getDisplayMetrics().widthPixels / 100;
        //int height = 95 * getResources().getDisplayMetrics().heightPixels / 100;
        window.setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

}
