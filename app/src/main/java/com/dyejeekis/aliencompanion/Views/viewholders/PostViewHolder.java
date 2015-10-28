package com.dyejeekis.aliencompanion.Views.viewholders;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.ClickListeners.PostItemOptionsListener;
import com.dyejeekis.aliencompanion.Models.Thumbnail;
import com.dyejeekis.aliencompanion.MyHtmlTagHandler;
import com.dyejeekis.aliencompanion.MyLinkMovementMethod;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.Utils.ConvertUtils;
import com.dyejeekis.aliencompanion.api.entity.Submission;
import com.dyejeekis.aliencompanion.enums.PostViewType;
import com.squareup.picasso.Picasso;

/**
 * Created by sound on 8/28/2015.
 */
public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public TextView score;
    public TextView age;
    public TextView author;
    public TextView domain;
    public TextView domain2;
    public TextView fullUrl;
    public TextView subreddit;
    public TextView commentCount;
    public TextView nsfw;
    public TextView selfText;
    public TextView selfTextCard;
    public ImageView postImage;
    public ImageView commentsIcon;
    public LinearLayout layoutSelfTextPreview;
    public LinearLayout commentsButton;
    public LinearLayout linkButton;
    public LinearLayout fullComments;
    public LinearLayout layoutPostOptions;
    public ImageView upvote;
    public ImageView downvote;
    public ImageView save;
    public ImageView hide;
    public ImageView viewUser;
    public ImageView openBrowser;
    public ImageView moreOptions;
    public ProgressBar commentsProgress;

    private int upvoteResource, downvoteResource, saveResource, hideResource, moreResource;

    public PostViewType viewType;

    private static final int clickedColor = Color.GRAY;
    private static int upvoteColor, downvoteColor;
    //private static final int notClickedColor = MainActivity.textColor;

    public PostViewHolder(View itemView, PostViewType type) {
        super(itemView);
        this.viewType = type;
        upvoteColor = Color.parseColor("#ff8b60");
        downvoteColor = Color.parseColor("#9494ff");

        title = (TextView) itemView.findViewById(R.id.txtView_postTitle);
        score = (TextView) itemView.findViewById(R.id.txtView_postScore);
        age = (TextView) itemView.findViewById(R.id.txtView_postAge);
        author = (TextView) itemView.findViewById(R.id.txtView_postAuthor);
        domain = (TextView) itemView.findViewById(R.id.txtView_postDomain);
        subreddit = (TextView) itemView.findViewById(R.id.txtView_postSubreddit);
        commentCount = (TextView) itemView.findViewById(R.id.txtView_postComments);
        nsfw = (TextView) itemView.findViewById(R.id.txtView_nsfw);
        postImage = (ImageView) itemView.findViewById(R.id.imgView_postImage);
        linkButton = (LinearLayout) itemView.findViewById(R.id.layout_postLinkButton);
        upvote =  (ImageView) itemView.findViewById(R.id.btn_upvote);
        layoutPostOptions = (LinearLayout) itemView.findViewById(R.id.layout_postOptions);
        downvote =  (ImageView) itemView.findViewById(R.id.btn_downvote);
        save =  (ImageView) itemView.findViewById(R.id.btn_save);
        hide =  (ImageView) itemView.findViewById(R.id.btn_hide);
        moreOptions =  (ImageView) itemView.findViewById(R.id.btn_more);

        if(viewType == PostViewType.listItem || viewType == PostViewType.details || MainActivity.nightThemeEnabled) {
            viewUser = (ImageView) itemView.findViewById(R.id.btn_view_user);
            openBrowser = (ImageView) itemView.findViewById(R.id.btn_open_browser);
            upvoteResource = R.mipmap.ic_action_upvote_white;
            downvoteResource = R.mipmap.ic_action_downvote_white;
            saveResource = R.mipmap.ic_action_save_white;
            hideResource = R.mipmap.ic_action_hide_white;
            moreResource = R.mipmap.ic_action_more_vertical_white;
        }
        else {
            upvoteResource = R.mipmap.ic_action_upvote_grey;
            downvoteResource = R.mipmap.ic_action_downvote_grey;
            saveResource = R.mipmap.ic_action_save_grey;
            hideResource = R.mipmap.ic_action_hide_grey;
            moreResource = R.mipmap.ic_action_more_vertical_grey;
        }
        switch (viewType) {
            case listItem:
                commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);
                commentsIcon = (ImageView) itemView.findViewById(R.id.imgView_commentsIcon);
                break;
            case details:
                selfText = (TextView) itemView.findViewById(R.id.txtView_selfText);
                fullComments = (LinearLayout) itemView.findViewById(R.id.fullLoad);
                commentsProgress = (ProgressBar) itemView.findViewById(R.id.pBar_comments);
                break;
            case smallCards:
                commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);
                break;
            case cards:
                commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);
                domain2 = (TextView) itemView.findViewById(R.id.txtView_postDomain_two);
                fullUrl = (TextView) itemView.findViewById(R.id.txtView_postUrl);
                layoutSelfTextPreview = (LinearLayout) itemView.findViewById(R.id.layout_selfTextPreview);
                selfTextCard = (TextView) itemView.findViewById(R.id.txtView_selfTextPreview);
                break;
            case cardDetails:
                fullComments = (LinearLayout) itemView.findViewById(R.id.fullLoad);
                commentsProgress = (ProgressBar) itemView.findViewById(R.id.pBar_comments);
                domain2 = (TextView) itemView.findViewById(R.id.txtView_postDomain_two);
                fullUrl = (TextView) itemView.findViewById(R.id.txtView_postUrl);
                layoutSelfTextPreview = (LinearLayout) itemView.findViewById(R.id.layout_selfTextPreview);
                selfTextCard = (TextView) itemView.findViewById(R.id.txtView_selfTextPreview);
                break;
        }
    }

    public void bindModel(Context context, Submission post) {

        title.setText(post.getTitle());
        score.setText(Long.toString(post.getScore()));
        author.setText(post.getAuthor());
        age.setText(post.agePrepared);
        domain.setText(post.getDomain());
        commentCount.setText(Long.toString(post.getCommentCount()));
        if(post.isNSFW()) nsfw.setVisibility(View.VISIBLE);
        else nsfw.setVisibility(View.GONE);

        Thumbnail postThumbnail = post.getThumbnailObject();
        if(postThumbnail == null) postThumbnail = new Thumbnail(); //TODO: check why thumbnail is null
        //TODO: clean this
        if(viewType == PostViewType.smallCards) {
            postImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            if(post.isSelf()) linkButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
            else {
                linkButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                if (post.isNSFW() && !MainActivity.prefs.getBoolean("showNSFWthumb", false)) {
                    postImage.setImageResource(R.drawable.nsfw2);
                }
                else if(postThumbnail.hasThumbnail()){
                    try {
                        //Get Post Thumbnail
                        Picasso.with(context).load(postThumbnail.getUrl()).placeholder(R.drawable.noimage).into(postImage);
                    } catch (IllegalArgumentException e) {e.printStackTrace();}
                }
                else postImage.setImageResource(R.drawable.noimage);
            }
        }
        else {
            if (postThumbnail.hasThumbnail()) {
                postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                if (postThumbnail.isSelf()) {
                    postImage.setImageResource(R.drawable.self_default2);
                    //postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                } else if (post.isNSFW() && !MainActivity.prefs.getBoolean("showNSFWthumb", false)) {
                    //postImage.setImageResource(R.drawable.nsfw2);
                    postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                } else {
                    try {
                        //Get Post Thumbnail
                        Picasso.with(context).load(postThumbnail.getUrl()).placeholder(R.drawable.noimage).into(postImage);
                    } catch (IllegalArgumentException e) {e.printStackTrace();}
                }
            }
            else postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
        }

        //user logged in
        if(MainActivity.currentUser != null) {
            //check user vote
            if (post.getLikes().equals("true")) {
                score.setTextColor(upvoteColor);
                upvote.setImageResource(R.mipmap.ic_action_upvote_orange);
                downvote.setImageResource(downvoteResource);
            } else if (post.getLikes().equals("false")) {
                score.setTextColor(downvoteColor);
                upvote.setImageResource(upvoteResource);
                downvote.setImageResource(R.mipmap.ic_action_downvote_blue);
            } else {
                score.setTextColor(MainActivity.textHintColor);
                upvote.setImageResource(upvoteResource);
                downvote.setImageResource(downvoteResource);
            }
            //check saved post
            if(post.isSaved()) save.setImageResource(R.mipmap.ic_action_save_yellow);
            else save.setImageResource(saveResource);
            //check hidden post
            if(post.isHidden()) hide.setImageResource(R.mipmap.ic_action_hide_red);
            else hide.setImageResource(hideResource);
        }
        else {
            upvote.setImageResource(upvoteResource);
            downvote.setImageResource(downvoteResource);
            save.setImageResource(saveResource);
            hide.setImageResource(hideResource);
        }

        // postviewtype must not be details
        if(post.isSelf()) subreddit.setVisibility(View.GONE);
        else {
            subreddit.setVisibility(View.VISIBLE);
            subreddit.setText(post.getSubreddit() + " · ");
        }

        if(viewType == PostViewType.listItem || viewType == PostViewType.cards || viewType == PostViewType.smallCards) {
            if(post.isClicked()) {
                title.setTextColor(clickedColor);
                if(viewType == PostViewType.listItem) commentCount.setTextColor(clickedColor);
            }
            else {
                title.setTextColor(MainActivity.textColor);
                if(viewType == PostViewType.listItem) commentCount.setTextColor(MainActivity.textColor);
            }
        }

        switch (viewType) {
            case listItem:
                if(MainActivity.nightThemeEnabled) commentsIcon.setImageResource(R.mipmap.ic_chat_bubble_outline_white_24dp);
                else commentsIcon.setImageResource(R.mipmap.ic_chat_bubble_outline_black_24dp);
                layoutPostOptions.setBackgroundColor(MainActivity.currentColor);
                break;
            case details:
                layoutPostOptions.setBackgroundColor(MainActivity.currentColor);
                subreddit.setText(post.getSubreddit());
                break;
            case smallCards:
                moreOptions.setImageResource(moreResource);
                break;
            case cards:
                moreOptions.setImageResource(moreResource);
                if(post.isSelf()) {
                    linkButton.setVisibility(View.GONE);
                    try {
                        String text = ConvertUtils.noTrailingwhiteLines(Html.fromHtml(post.getSelftextHTML())).toString();
                        if(text.length()>200) text = text.substring(0, 200) + " ...";
                        selfTextCard.setText(text);
                        layoutSelfTextPreview.setVisibility(View.VISIBLE);
                    } catch (NullPointerException e) {
                        layoutSelfTextPreview.setVisibility(View.GONE);
                    }
                    //if(post.selfTextPreparedPreview == null) layoutSelfTextPreview.setVisibility(View.GONE);
                    //else {
                    //    layoutSelfTextPreview.setVisibility(View.VISIBLE);
                    //    selfTextCard.setText(post.selfTextPreparedPreview);
                    //}
                }
                else {
                    layoutSelfTextPreview.setVisibility(View.GONE);
                    linkButton.setVisibility(View.VISIBLE);
                    domain2.setText(post.getDomain());
                    domain2.setTextColor(MainActivity.linkColor);
                    fullUrl.setText(post.getURL());
                }
                break;
            case cardDetails:
                moreOptions.setImageResource(moreResource);
                if(post.isSelf()) {
                    linkButton.setVisibility(View.GONE);
                    if(post.getSelftextHTML()!=null) {
                        layoutSelfTextPreview.setVisibility(View.VISIBLE);
                        //if(post.selfTextPrepared == null) {
                        //    post.selfTextPrepared = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(post.getSelftextHTML(), null, new MyHtmlTagHandler()));
                        //    post.selfTextPrepared = ConvertUtils.modifyURLSpan(context, post.selfTextPrepared);
                        //}
                        //selfTextCard.setText(post.selfTextPrepared);
                        SpannableStringBuilder stringBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(post.getSelftextHTML(), null, new MyHtmlTagHandler()));
                        stringBuilder = ConvertUtils.modifyURLSpan(context, stringBuilder);
                        selfTextCard.setText(stringBuilder);
                        selfTextCard.setMovementMethod(MyLinkMovementMethod.getInstance());
                    }
                    else layoutSelfTextPreview.setVisibility(View.GONE);
                }
                else {
                    layoutSelfTextPreview.setVisibility(View.GONE);
                    linkButton.setVisibility(View.VISIBLE);
                    domain2.setText(post.getDomain());
                    domain2.setTextColor(MainActivity.linkColor);
                    fullUrl.setText(post.getURL());
                }
                break;
        }
    }

    public void setCardButtonsListener(PostItemOptionsListener listener) {
        upvote.setOnClickListener(listener);
        downvote.setOnClickListener(listener);
        save.setOnClickListener(listener);
        hide.setOnClickListener(listener);
        moreOptions.setOnClickListener(listener);
    }

    public void showPostOptions(PostItemOptionsListener listener) {
        layoutPostOptions.setVisibility(View.VISIBLE);
        upvote.setOnClickListener(listener);
        downvote.setOnClickListener(listener);
        save.setOnClickListener(listener);
        hide.setOnClickListener(listener);
        viewUser.setOnClickListener(listener);
        openBrowser.setOnClickListener(listener);
        moreOptions.setOnClickListener(listener);
    }

    public void hidePostOptions() {
        layoutPostOptions.setVisibility(View.GONE);
    }
}
