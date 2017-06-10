package com.gDyejeekis.aliencompanion.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.activities.BrowserActivity;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.asynctask.LoadSyncedArticleTask;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;

/**
 * Created by George on 6/7/2017.
 */

public class ArticleFragment extends Fragment {

    public static final String TAG = "ArticleFragment";

    public Submission post;
    public ProgressBar progressBar;
    public LinearLayout articleLayout;
    public ImageView image;
    public TextView title;
    public TextView body;

    //public static ArticleFragment newInstance(Submission post) {
    //    ArticleFragment fragment = new ArticleFragment();
    //    fragment.setPost(post);
    //    return fragment;
    //}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        post = (Submission) getActivity().getIntent().getSerializableExtra("post");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar_article);
        articleLayout = (LinearLayout) view.findViewById(R.id.layout_article);
        image = (ImageView) view.findViewById(R.id.imageView_article_image);
        title = (TextView) view.findViewById(R.id.textView_article_title);
        body = (TextView) view.findViewById(R.id.textView_article_body);

        articleLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new LoadSyncedArticleTask(this).execute();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setTitle(post.getDomain());
            actionBar.setSubtitle(post.getCommentCount() + " comments");
        } catch (Exception e) {}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_load_cache:
                ((BrowserActivity) getActivity()).loadOriginalPage();
                return true;
            case R.id.action_open_browser:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(post.getURL())));
                return true;
            case R.id.action_share_url:
                GeneralUtils.shareUrl(getActivity(), "Share via..", post.getURL());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setPost(Submission post) {
        this.post = post;
    }
}
