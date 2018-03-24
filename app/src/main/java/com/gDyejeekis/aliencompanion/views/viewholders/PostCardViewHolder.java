package com.gDyejeekis.aliencompanion.views.viewholders;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.enums.PostViewType;
import com.gDyejeekis.aliencompanion.utils.HtmlTagHandler;
import com.gDyejeekis.aliencompanion.utils.SpanUtils;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemOptionsListener;
import com.gDyejeekis.aliencompanion.models.Thumbnail;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.utils.MyLinkMovementMethod;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.squareup.picasso.Picasso;

/**
 * Created by George on 1/22/2017.
 */

public class PostCardViewHolder extends PostViewHolder  {

    public static boolean viewUserIconVisible, shareIconVisible;

    private final boolean showDetails;

    public TextView title;
    public TextView postDets1;
    public TextView fullUrl;
    public TextView commentsText;
    public TextView scoreText;
    public TextView domain2;
    public TextView selfText;
    public TextView goldCount;
    public ImageView upvote;
    public ImageView downvote;
    public ImageView save;
    public ImageView hide;
    public ImageView viewUser;
    public ImageView share;
    public ImageView moreOptions;
    public ImageView postImage;
    public ImageView imageButton;
    public LinearLayout fullComments;
    public LinearLayout linkButton;
    public LinearLayout layoutPostOptions;
    public LinearLayout commentsButton;
    public LinearLayout layoutSelfText;
    public LinearLayout layoutGilded;
    public ProgressBar commentsProgress;

    private float defaultIconOpacity, defaultIconOpacityDisabled;

    public PostCardViewHolder(View itemView, boolean showDetails) {
        super(itemView);

        this.showDetails = showDetails;
        if(showDetails) {
            fullComments = itemView.findViewById(R.id.fullLoad);
            commentsProgress = itemView.findViewById(R.id.pBar_comments);
            commentsProgress.getIndeterminateDrawable().setColorFilter(MyApplication.colorSecondary, PorterDuff.Mode.SRC_IN);
        }

        title = itemView.findViewById(R.id.txtView_postTitle);
        goldCount = itemView.findViewById(R.id.textView_gilded);
        commentsText = itemView.findViewById(R.id.textView_comments);
        postImage = itemView.findViewById(R.id.imgView_postImage);
        linkButton = itemView.findViewById(R.id.layout_postLinkButton);
        upvote = itemView.findViewById(R.id.btn_upvote);
        layoutPostOptions = itemView.findViewById(R.id.layout_postOptions);
        downvote = itemView.findViewById(R.id.btn_downvote);
        save = itemView.findViewById(R.id.btn_save);
        hide = itemView.findViewById(R.id.btn_hide);
        viewUser = itemView.findViewById(R.id.btn_view_user);
        share = itemView.findViewById(R.id.btn_share);
        moreOptions = itemView.findViewById(R.id.btn_more);
        postDets1 = itemView.findViewById(R.id.textView_dets1);
        layoutGilded = itemView.findViewById(R.id.layout_gilded);
        commentsButton = itemView.findViewById(R.id.layout_postCommentsButton);
        domain2 = itemView.findViewById(R.id.txtView_postDomain_two);
        fullUrl = itemView.findViewById(R.id.txtView_postUrl);
        layoutSelfText = itemView.findViewById(R.id.layout_selfTextPreview);
        selfText = itemView.findViewById(R.id.txtView_selfTextPreview);
        scoreText = itemView.findViewById(R.id.textView_score);
        imageButton = itemView.findViewById(R.id.imageButton);

        initIcons();
    }

    private void initIcons() {
        initIconResources(showDetails ? PostViewType.cardDetails : PostViewType.cards);
        switch (MyApplication.currentBaseTheme) {
            case AppConstants.LIGHT_THEME:
                defaultIconOpacity = 0.54f;
                defaultIconOpacityDisabled = 0.27f;
                break;
            case AppConstants.DARK_THEME_LOW_CONTRAST:
                defaultIconOpacity = 0.6f;
                defaultIconOpacityDisabled = 0.3f;
                break;
            default:
                defaultIconOpacity = 1f;
                defaultIconOpacityDisabled = 0.5f;
                break;
        }
        // set unchanging properties of icons
        viewUser.setImageResource(viewUserResource);
        share.setImageResource(shareResource);
        moreOptions.setImageResource(moreResource);
        viewUser.setAlpha(defaultIconOpacity);
        share.setAlpha(defaultIconOpacity);
        moreOptions.setAlpha(defaultIconOpacity);
    }

    @Override
    public void bindModel(Context context, Submission post) {
        // set title
        title.setText(post.getTitle());
        // check if post is clicked
        if(post.isClicked() && !showDetails) {
            title.setTextColor(post.isStickied() && post.showAsStickied ? MyApplication.textColorStickiedClicked : MyApplication.textSecondaryColor);
        }
        else {
            title.setTextColor(post.isStickied() && (post.showAsStickied || showDetails) ? MyApplication.textColorStickied : MyApplication.textPrimaryColor);
        }
        // set post thumbnail or self text
        Thumbnail thumbnailObject = post.getThumbnailObject()==null ? new Thumbnail() : post.getThumbnailObject();
        if(post.isSelf()) {
            linkButton.setVisibility(View.GONE);
            imageButton.setVisibility(View.GONE);

            if(AppConstants.USER_MARKDOWN_PARSER) {
                // TODO: 1/23/2017
            }
            // parse html string using fromHtml()
            else {
                try {
                    if(post.getSelftextHTML().trim().isEmpty()) {
                        layoutSelfText.setVisibility(View.GONE);
                    }
                    else if(showDetails) {
                        layoutSelfText.setVisibility(View.VISIBLE);
                        //selfText.setTextIsSelectable(true);
                        selfText.setTextColor(MyApplication.textPrimaryColor);

                        SpannableStringBuilder stringBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(post.getSelftextHTML(), null,
                                new HtmlTagHandler(selfText.getPaint())));
                        stringBuilder = SpanUtils.modifyURLSpan(context, stringBuilder);
                        if(post.getHighlightText()!=null) {
                            stringBuilder = SpanUtils.highlightText(stringBuilder, post.getHighlightText(), post.highlightMatchCase());
                        }
                        selfText.setText(stringBuilder);
                        selfText.setMovementMethod(MyLinkMovementMethod.getInstance());
                    }
                    else {
                        if(post.isSpoiler()) {
                            layoutSelfText.setVisibility(View.GONE);
                        }
                        else {
                            layoutSelfText.setVisibility(View.VISIBLE);
                            //selfText.setTextIsSelectable(false);
                            selfText.setTextColor(MyApplication.textSecondaryColor);

                            String text = ConvertUtils.noTrailingwhiteLines(Html.fromHtml(post.getSelftextHTML())).toString();
                            if (text.length() > 200) text = text.substring(0, 200) + " ...";
                            selfText.setText(text);
                        }
                    }
                } catch (NullPointerException  e) {
                    layoutSelfText.setVisibility(View.GONE);
                }
            }
        }
        // case post has large thumbnail
        else if(post.hasImageButton && thumbnailObject.hasThumbnail() && !post.isSpoiler()) {
            layoutSelfText.setVisibility(View.GONE);
            linkButton.setVisibility(View.GONE);
            imageButton.setVisibility(View.VISIBLE);
            try {
                Picasso.with(context).load(thumbnailObject.getUrl()).placeholder(R.drawable.noimage).into(imageButton);
            } catch (IllegalArgumentException e) {e.printStackTrace();}
        }
        else {
            layoutSelfText.setVisibility(View.GONE);
            imageButton.setVisibility(View.GONE);
            linkButton.setVisibility(View.VISIBLE);
            domain2.setText(post.getDomain());
            domain2.setTextColor(MyApplication.linkColor);
            fullUrl.setText(post.getURL());
            if(thumbnailObject.hasThumbnail() && !post.isSpoiler()) {
                if(post.isNSFW() && !MyApplication.showNSFWpreview) {
                    postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                }
                else {
                    postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                    try {
                        //Get Post Thumbnail
                        Picasso.with(context).load(post.getThumbnailObject().getUrl()).placeholder(R.drawable.noimage).into(postImage);
                    } catch (IllegalArgumentException e) {e.printStackTrace();}
                }
            }
            else {
                postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
            }
        }
        // check if gilded
        if(post.getGilded() > 0) {
            layoutGilded.setVisibility(View.VISIBLE);
            goldCount.setText("x" + String.valueOf(post.getGilded()));
        }
        else {
            layoutGilded.setVisibility(View.GONE);
        }
        // set post details
        String detsText = post.getAuthor() + " · " + post.agePrepared + " · ";
        SpannableString detsSpannable;
        if(post.isSelf()) {
            detsText += post.getDomain();
        }
        else {
            detsText += post.getSubreddit() + " · " + post.getDomain();
        }
        if(post.getLinkFlairText() != null && !post.getLinkFlairText().trim().isEmpty()) {
            detsSpannable = new SpannableString(post.getLinkFlairText() + " · " + detsText);
            detsSpannable.setSpan(new ForegroundColorSpan(MyApplication.linkColor), 0, post.getLinkFlairText().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        else {
            detsSpannable = new SpannableString(detsText);
        }
        postDets1.setText(detsSpannable);
        if(post.isNSFW()) {
            appendNsfwLabel(context, postDets1);
        }
        if(post.isSpoiler()) {
            appendSpoilerLabel(postDets1);
        }
        // set post score and icon colors depending on user
        SpannableString scoreSpannable = new SpannableString(post.getScore() + " points");
        if(MyApplication.currentUser != null) {
            // check user vote
            int scoreEnd = post.getScore().toString().length();
            if (post.getLikes().equals("true")) {
                scoreSpannable.setSpan(new TextAppearanceSpan(context, R.style.upvotedStyle), 0, scoreEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                upvote.setImageResource(R.drawable.ic_arrow_upward_upvote_orange_48dp);
                upvote.setAlpha(1f);
                downvote.setImageResource(downvoteResource);
                downvote.setAlpha(defaultIconOpacity);
            }
            else if (post.getLikes().equals("false")) {
                scoreSpannable.setSpan(new TextAppearanceSpan(context, R.style.downvotedStyle), 0, scoreEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                upvote.setImageResource(upvoteResource);
                upvote.setAlpha(defaultIconOpacity);
                downvote.setImageResource(R.drawable.ic_arrow_downward_downvote_blue_48dp);
                downvote.setAlpha(1f);
            }
            else {
                scoreText.setTextColor(MyApplication.textSecondaryColor);
                upvote.setImageResource(upvoteResource);
                upvote.setAlpha(defaultIconOpacity);
                downvote.setImageResource(downvoteResource);
                downvote.setAlpha(defaultIconOpacity);
            }
            // check saved post
            if(post.isSaved()) {
                save.setImageResource(saveResourceYellow);
                save.setAlpha(1f);
            } else {
                save.setImageResource(saveResource);
                save.setAlpha(defaultIconOpacity);
            }
            // check hidden post
            if(post.isHidden()) {
                hide.setImageResource(hideResourceRed);
                hide.setAlpha(1f);
            } else {
                hide.setImageResource(hideResource);
                hide.setAlpha(defaultIconOpacity);
            }
        }
        else {
            upvote.setImageResource(upvoteResource);
            downvote.setImageResource(downvoteResource);
            save.setImageResource(saveResource);
            hide.setImageResource(hideResource);
            upvote.setAlpha(defaultIconOpacityDisabled);
            downvote.setAlpha(defaultIconOpacityDisabled);
            save.setAlpha(defaultIconOpacityDisabled);
            hide.setAlpha(defaultIconOpacityDisabled);
        }
        scoreText.setText(scoreSpannable);
        // set post comments
        commentsText.setText(post.getCommentCount() + " comments");

        //change menu bar item visibility depending on available space
        boolean islandscape = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if((MyApplication.isLargeScreen || islandscape)) {
            viewUserIconVisible = MyApplication.isLargeScreen || !MyApplication.dualPaneActive;
            shareIconVisible = MyApplication.isLargeScreen || !MyApplication.dualPaneActive;
        }
        else {
            viewUserIconVisible = false;
            shareIconVisible = false;
        }
        viewUser.setVisibility(viewUserIconVisible ? View.VISIBLE : View.GONE);
        share.setVisibility(shareIconVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setClickListeners(PostItemListener postItemListener, View.OnLongClickListener postLongListener, PostItemOptionsListener postItemOptionsListener) {
        linkButton.setOnClickListener(postItemListener);
        imageButton.setOnClickListener(postItemListener);

        if(showDetails) {
            commentsButton.setOnClickListener(null);
        }
        else {
            commentsButton.setOnClickListener(postItemListener);
        }

        upvote.setOnClickListener(postItemOptionsListener);
        downvote.setOnClickListener(postItemOptionsListener);
        save.setOnClickListener(postItemOptionsListener);
        hide.setOnClickListener(postItemOptionsListener);
        viewUser.setOnClickListener(postItemOptionsListener);
        share.setOnClickListener(postItemOptionsListener);
        moreOptions.setOnClickListener(postItemOptionsListener);
    }

    @Override
    public void setPostOptionsVisible(boolean flag) {

    }

}
