package com.gDyejeekis.aliencompanion.Views.viewholders;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.ClickListeners.PostItemOptionsListener;
import com.gDyejeekis.aliencompanion.Models.Thumbnail;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.MyHtmlTagHandler;
import com.gDyejeekis.aliencompanion.Utils.MyLinkMovementMethod;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.enums.PostViewType;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by sound on 8/28/2015.
 */
public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public TextView domain2;
    public TextView fullUrl;
    public TextView selfText;
    public TextView selfTextCard;
    public TextView postDets1;
    public TextView postDets2;
    public TextView scoreText;
    public TextView commentsText;
    public ImageView postImage;
    public ImageView commentsIcon;
    public RoundedImageView imageButton;
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

    private int upvoteResource, downvoteResource, saveResource, hideResource, moreResource, commentsResource;

    public PostViewType viewType;

    private static final int clickedColor = MyApplication.textHintColor;
    private static final int smallCardLinkButtonColor = Color.parseColor("#404040");

    public PostViewHolder(View itemView, PostViewType type) {
        super(itemView);
        this.viewType = type;

        if(viewType == PostViewType.listItem || viewType == PostViewType.smallCards || MyApplication.nightThemeEnabled) {
            upvoteResource = R.mipmap.ic_arrow_upward_white_48dp;
            downvoteResource = R.mipmap.ic_arrow_downward_white_48dp;
            saveResource = R.mipmap.ic_star_border_white_48dp;
            hideResource = R.mipmap.ic_close_white_48dp;
            moreResource = R.mipmap.ic_more_vert_white_48dp;
            commentsResource = R.mipmap.ic_chat_bubble_outline_light_grey_24dp;
        }
        else {
            upvoteResource = R.mipmap.ic_arrow_upward_grey_48dp;
            downvoteResource = R.mipmap.ic_arrow_downward_grey_48dp;
            saveResource = R.mipmap.ic_star_border_grey_48dp;
            hideResource = R.mipmap.ic_close_grey_48dp;
            moreResource = R.mipmap.ic_more_vert_grey_48dp;
            commentsResource = R.mipmap.ic_chat_bubble_outline_grey_24dp;
        }

        switch (type) {
            case listItem:
                title = (TextView) itemView.findViewById(R.id.txtView_postTitle);
                commentsText = (TextView) itemView.findViewById(R.id.textView_comments);
                postImage = (ImageView) itemView.findViewById(R.id.imgView_postImage);
                linkButton = (LinearLayout) itemView.findViewById(R.id.layout_postLinkButton);
                upvote =  (ImageView) itemView.findViewById(R.id.btn_upvote);
                layoutPostOptions = (LinearLayout) itemView.findViewById(R.id.layout_postOptions);
                downvote =  (ImageView) itemView.findViewById(R.id.btn_downvote);
                save =  (ImageView) itemView.findViewById(R.id.btn_save);
                hide =  (ImageView) itemView.findViewById(R.id.btn_hide);
                moreOptions =  (ImageView) itemView.findViewById(R.id.btn_more);

                viewUser = (ImageView) itemView.findViewById(R.id.btn_view_user);
                openBrowser = (ImageView) itemView.findViewById(R.id.btn_open_browser);
                commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);
                commentsIcon = (ImageView) itemView.findViewById(R.id.imgView_commentsIcon);
                postDets1 = (TextView) itemView.findViewById(R.id.textView_dets1);
                postDets2 = (TextView) itemView.findViewById(R.id.textView_dets2);
                break;
            case smallCards:
                title = (TextView) itemView.findViewById(R.id.txtView_postTitle);
                postImage = (ImageView) itemView.findViewById(R.id.imgView_postImage);
                linkButton = (LinearLayout) itemView.findViewById(R.id.layout_postLinkButton);
                layoutPostOptions = (LinearLayout) itemView.findViewById(R.id.layout_options);
                upvote =  (ImageView) itemView.findViewById(R.id.btn_upvote);
                downvote =  (ImageView) itemView.findViewById(R.id.btn_downvote);
                save =  (ImageView) itemView.findViewById(R.id.btn_save);
                hide =  (ImageView) itemView.findViewById(R.id.btn_hide);
                viewUser = (ImageView) itemView.findViewById(R.id.btn_view_user);
                openBrowser = (ImageView) itemView.findViewById(R.id.btn_open_browser);
                moreOptions =  (ImageView) itemView.findViewById(R.id.btn_more);

                commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);
                postDets1 = (TextView) itemView.findViewById(R.id.small_card_details_1);
                postDets2 = (TextView) itemView.findViewById(R.id.small_card_details_2);
                break;
            case cards:
                title = (TextView) itemView.findViewById(R.id.txtView_postTitle);
                commentsText = (TextView) itemView.findViewById(R.id.textView_comments);
                postImage = (ImageView) itemView.findViewById(R.id.imgView_postImage);
                linkButton = (LinearLayout) itemView.findViewById(R.id.layout_postLinkButton);
                upvote =  (ImageView) itemView.findViewById(R.id.btn_upvote);
                layoutPostOptions = (LinearLayout) itemView.findViewById(R.id.layout_postOptions);
                downvote =  (ImageView) itemView.findViewById(R.id.btn_downvote);
                save =  (ImageView) itemView.findViewById(R.id.btn_save);
                hide =  (ImageView) itemView.findViewById(R.id.btn_hide);
                moreOptions =  (ImageView) itemView.findViewById(R.id.btn_more);

                postDets1 = (TextView) itemView.findViewById(R.id.textView_dets1);
                commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);
                domain2 = (TextView) itemView.findViewById(R.id.txtView_postDomain_two);
                fullUrl = (TextView) itemView.findViewById(R.id.txtView_postUrl);
                layoutSelfTextPreview = (LinearLayout) itemView.findViewById(R.id.layout_selfTextPreview);
                selfTextCard = (TextView) itemView.findViewById(R.id.txtView_selfTextPreview);
                scoreText = (TextView) itemView.findViewById(R.id.textView_score);
                imageButton = (RoundedImageView) itemView.findViewById(R.id.imageButton);
                break;
            case cardDetails:
                title = (TextView) itemView.findViewById(R.id.txtView_postTitle);
                commentsText = (TextView) itemView.findViewById(R.id.textView_comments);
                postImage = (ImageView) itemView.findViewById(R.id.imgView_postImage);
                linkButton = (LinearLayout) itemView.findViewById(R.id.layout_postLinkButton);
                upvote =  (ImageView) itemView.findViewById(R.id.btn_upvote);
                layoutPostOptions = (LinearLayout) itemView.findViewById(R.id.layout_postOptions);
                downvote =  (ImageView) itemView.findViewById(R.id.btn_downvote);
                save =  (ImageView) itemView.findViewById(R.id.btn_save);
                hide =  (ImageView) itemView.findViewById(R.id.btn_hide);
                moreOptions =  (ImageView) itemView.findViewById(R.id.btn_more);

                postDets1 = (TextView) itemView.findViewById(R.id.textView_dets1);
                commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);
                fullComments = (LinearLayout) itemView.findViewById(R.id.fullLoad);
                commentsProgress = (ProgressBar) itemView.findViewById(R.id.pBar_comments);
                domain2 = (TextView) itemView.findViewById(R.id.txtView_postDomain_two);
                fullUrl = (TextView) itemView.findViewById(R.id.txtView_postUrl);
                layoutSelfTextPreview = (LinearLayout) itemView.findViewById(R.id.layout_selfTextPreview);
                selfTextCard = (TextView) itemView.findViewById(R.id.txtView_selfTextPreview);
                scoreText = (TextView) itemView.findViewById(R.id.textView_score);
                imageButton = (RoundedImageView) itemView.findViewById(R.id.imageButton);
                break;
        }
    }

    public void bindModel(Context context, Submission post) {

        title.setText(post.getTitle());

        Thumbnail postThumbnail = post.getThumbnailObject();
        if(postThumbnail == null) postThumbnail = new Thumbnail();
        //ImageLoader.preloadThumbnail(post, context);
        //Thumbnail postThumbnail = post.getThumbnailObject();
        //TODO: clean this
        if(viewType == PostViewType.smallCards) {
            postImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            if(post.isSelf()) {
                linkButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
            }
            else {
                if (post.isNSFW() && !MyApplication.showNSFWpreview) {
                    linkButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                    linkButton.setPadding(10, 10, 0, 10);
                    linkButton.setBackground(null);
                    postImage.setImageResource(R.drawable.nsfw2);
                    postImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
                else if(postThumbnail.hasThumbnail()){
                    linkButton.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2f));
                    linkButton.setPadding(10, 10, 0, 10);
                    linkButton.setBackground(null);
                    try {
                        //Get Post Thumbnail
                        Picasso.with(context).load(postThumbnail.getUrl()).placeholder(R.drawable.noimage).into(postImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                postImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            }

                            @Override
                            public void onError() {
                                postImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            }
                        });
                    } catch (IllegalArgumentException e) {e.printStackTrace();}
                }
                else {
                    linkButton.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
                    linkButton.setPadding(10, 10, 10, 10);
                    linkButton.setBackgroundColor(smallCardLinkButtonColor);
                    postImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    postImage.setImageResource(R.drawable.ic_link_white_48dp);
                }
            }
        }
        else if (viewType == PostViewType.listItem || !post.hasImageButton) {
            if (postThumbnail.hasThumbnail()) {
                postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                if (postThumbnail.isSelf()) {
                    postImage.setImageResource(R.drawable.self_default2);
                    //postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                } else if (post.isNSFW() && !MyApplication.showNSFWpreview) {
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

        if(viewType != PostViewType.cardDetails /*viewType == PostViewType.listItem || viewType == PostViewType.cards || viewType == PostViewType.smallCards*/) {
            if(post.isClicked()) {
                title.setTextColor(clickedColor);
                if(viewType == PostViewType.listItem) commentsText.setTextColor(clickedColor);
            }
            else {
                title.setTextColor(MyApplication.textColor);
                if(viewType == PostViewType.listItem) commentsText.setTextColor(MyApplication.textColor);
            }
        }

        switch (viewType) {
            case listItem:
                commentsIcon.setImageResource(commentsResource);
                layoutPostOptions.setBackgroundColor(MyApplication.currentColor);
                bindPostList(context, post);
                break;
            case smallCards:
                layoutPostOptions.setBackgroundColor(MyApplication.currentColor);
                bindPostSmallCards(context, post);
                break;
            case cards:
                moreOptions.setImageResource(moreResource);
                bindPostCards(context, post);
                if(post.isSelf()) {
                    linkButton.setVisibility(View.GONE);
                    imageButton.setVisibility(View.GONE);

                    if(MyApplication.useMarkdownParsing) {

                    }
                    else {
                        try {
                            String text = ConvertUtils.noTrailingwhiteLines(Html.fromHtml(post.getSelftextHTML())).toString();
                            if(text.length()>200) text = text.substring(0, 200) + " ...";
                            selfTextCard.setText(text);
                            layoutSelfTextPreview.setVisibility(View.VISIBLE);
                        } catch (NullPointerException  e) {
                            layoutSelfTextPreview.setVisibility(View.GONE);
                        }
                    }
                }
                else if(post.hasImageButton && postThumbnail.hasThumbnail()) {
                    layoutSelfTextPreview.setVisibility(View.GONE);
                    linkButton.setVisibility(View.GONE);
                    imageButton.setVisibility(View.VISIBLE);
                    try {
                        Picasso.with(context).load(postThumbnail.getUrl())/*.centerCrop().resize(1000, 300)*/.into(imageButton);
                    } catch (IllegalArgumentException e) {e.printStackTrace();}
                }
                else {
                    layoutSelfTextPreview.setVisibility(View.GONE);
                    imageButton.setVisibility(View.GONE);
                    linkButton.setVisibility(View.VISIBLE);
                    domain2.setText(post.getDomain());
                    domain2.setTextColor(MyApplication.linkColor);
                    fullUrl.setText(post.getURL());
                    if(postThumbnail.hasThumbnail()) {
                        if(post.isNSFW() && !MyApplication.showNSFWpreview) postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                        else postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                    }
                    else postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                }
                break;
            case cardDetails:
                commentsButton.setBackground(null);
                moreOptions.setImageResource(moreResource);
                bindPostCards(context, post);
                if(post.isSelf()) {
                    linkButton.setVisibility(View.GONE);
                    imageButton.setVisibility(View.GONE);
                    if(post.getSelftextHTML()!=null) {
                        layoutSelfTextPreview.setVisibility(View.VISIBLE);

                        if(MyApplication.useMarkdownParsing) {

                        }
                        else {
                            //parse body using fromHTML
                            SpannableStringBuilder stringBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(post.getSelftextHTML(), null, new MyHtmlTagHandler()));
                            stringBuilder = ConvertUtils.modifyURLSpan(context, stringBuilder);
                            selfTextCard.setText(stringBuilder);
                            selfTextCard.setMovementMethod(MyLinkMovementMethod.getInstance());
                        }
                    }
                    else layoutSelfTextPreview.setVisibility(View.GONE);
                }
                else if(post.hasImageButton && postThumbnail.hasThumbnail()) {
                    layoutSelfTextPreview.setVisibility(View.GONE);
                    linkButton.setVisibility(View.GONE);
                    imageButton.setVisibility(View.VISIBLE);
                    try {
                        Picasso.with(context).load(postThumbnail.getUrl())/*.centerCrop().resize(1000, 300)*/.into(imageButton);
                    } catch (IllegalArgumentException e) {e.printStackTrace();}
                }
                else {
                    layoutSelfTextPreview.setVisibility(View.GONE);
                    imageButton.setVisibility(View.GONE);
                    linkButton.setVisibility(View.VISIBLE);
                    domain2.setText(post.getDomain());
                    domain2.setTextColor(MyApplication.linkColor);
                    fullUrl.setText(post.getURL());
                    if(postThumbnail.hasThumbnail()) {
                        if(post.isNSFW() && !MyApplication.showNSFWpreview) postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                        else postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                    }
                    else postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                }
                break;
        }
    }

    private void bindPostList(Context context, Submission post) {
        String dets1 = post.getScore() + " · " + post.agePrepared + " · " + post.getAuthor();
        setIconsAndScoreText(context, postDets1, dets1, post);
        String dets2 = (post.isSelf()) ? post.getDomain() : post.getSubreddit() + " · " + post.getDomain();
        postDets2.setText(dets2);
        commentsText.setText(String.valueOf(post.getCommentCount()));

        if(post.isNSFW()) appendNsfwLabel(context, postDets2);
    }

    private void bindPostSmallCards(Context context, Submission post) {
        String dets1 = post.getAuthor() + " · " + post.agePrepared + " · ";
        if(post.isSelf()) dets1 += post.getDomain();
        else dets1 += post.getSubreddit() + " · " + post.getDomain();
        postDets1.setText(dets1);

        String dets2 = post.getScore() + " score · " + post.getCommentCount() + " comments";
        setIconsAndScoreText(context, postDets2, dets2, post);

        if(post.isNSFW()) appendNsfwLabel(context, postDets1);
    }

    private void bindPostCards(Context context, Submission post) {
        String dets = post.getAuthor() + " · " + post.agePrepared + " · ";
        if(post.isSelf()) dets += post.getDomain();
        else dets += post.getSubreddit() + " · " + post.getDomain();
        postDets1.setText(dets);

        setIconsAndScoreText(context, scoreText, post.getScore() + " score", post);
        commentsText.setText(post.getCommentCount() + " comments");

        if(post.isNSFW()) appendNsfwLabel(context, postDets1);
    }

    private void appendNsfwLabel(Context context, TextView textView) {
        SpannableString nsfwSpan = new SpannableString(" · NSFW");
        nsfwSpan.setSpan(new TextAppearanceSpan(context, R.style.nsfwLabel), 2, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.append(nsfwSpan);
    }

    private void setIconsAndScoreText(Context context, TextView textView, String text, Submission post) {
        if(MyApplication.currentUser != null) {
            //check user vote
            if (post.getLikes().equals("true")) {
                int index = text.indexOf(" ");
                SpannableString spannable = new SpannableString(text);
                spannable.setSpan(new TextAppearanceSpan(context, R.style.upvotedStyle), 0, index, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                textView.setText(spannable);
                upvote.setImageResource(R.mipmap.ic_arrow_upward_orange_48dp);
                downvote.setImageResource(downvoteResource);
            } else if (post.getLikes().equals("false")) {
                int index = text.indexOf(" ");
                SpannableString spannable = new SpannableString(text);
                spannable.setSpan(new TextAppearanceSpan(context, R.style.downvotedStyle), 0, index, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                textView.setText(spannable);
                upvote.setImageResource(upvoteResource);
                downvote.setImageResource(R.mipmap.ic_arrow_downward_blue_48dp);
            } else {
                textView.setText(text);
                textView.setTextColor(MyApplication.textHintColor);
                upvote.setImageResource(upvoteResource);
                downvote.setImageResource(downvoteResource);
            }
            //check saved post
            if(post.isSaved()) save.setImageResource(R.mipmap.ic_star_border_yellow_500_48dp);
            else save.setImageResource(saveResource);
            //check hidden post
            if(post.isHidden()) hide.setImageResource(R.mipmap.ic_close_red_800_48dp);
            else hide.setImageResource(hideResource);
        }
        else {
            textView.setText(text);
            upvote.setImageResource(upvoteResource);
            downvote.setImageResource(downvoteResource);
            save.setImageResource(saveResource);
            hide.setImageResource(hideResource);
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
