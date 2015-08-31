package com.george.redditreader.Fragments;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.george.redditreader.Activities.BackNavActivity;
import com.george.redditreader.Activities.PostActivity;
import com.george.redditreader.R;
import com.george.redditreader.api.entity.Submission;

/**
 * A simple {@link Fragment} subclass.
 */
public class BrowserFragment extends Fragment {

    private WebView webView;
    private ProgressBar progressBar;
    private Submission post;
    private AppCompatActivity activity;
    private Bundle webViewBundle;
    private String url;
    private String domain;

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //if (Uri.parse(url).getHost().equals("www.example.com")) {
            //    // This is my web site, so do not override; let my WebView load the page
            //    return false;
            //}
            //// Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            //startActivity(intent);
            //return true;
            return false;
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView webView, int progress) {
            if(progress < 100 && progressBar.getVisibility() == ProgressBar.GONE){
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }
            progressBar.setProgress(progress);
            if(progress == 100) {
                progressBar.setVisibility(ProgressBar.GONE);
            }
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        post = (Submission) activity.getIntent().getSerializableExtra("post");
        if(post != null) {
            url = post.getUrl();
            domain = post.getDomain();
        }
        else {
            url = activity.getIntent().getStringExtra("url");
            domain = activity.getIntent().getStringExtra("domain");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (AppCompatActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.activity = null;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        activity.getSupportActionBar().setTitle(domain);
        if(post != null) activity.getSupportActionBar().setSubtitle(post.getCommentCount() + " comments");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browser, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar4);
        webView = (WebView) view.findViewById(R.id.webView);

        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setBuiltInZoomControls(true);

        if(webViewBundle == null) {
            webView.setWebChromeClient(new MyWebChromeClient());
            webView.clearCache(true);
            webView.loadUrl(url);
        }
        else {
            webView.restoreState(webViewBundle);
        }
        return view;
    }

    public void onPause() {
        super.onPause();
        webViewBundle = new Bundle();
        webView.saveState(webViewBundle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_back:
                webView.goBack();
                return true;
            case R.id.action_forward:
                webView.goForward();
                return true;
            case R.id.action_open_browser:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            case R.id.action_refresh:
                webView.reload();
                return true;
            case R.id.action_comments:
                Intent intent = new Intent(activity, PostActivity.class);
                intent.putExtra("post", post);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
