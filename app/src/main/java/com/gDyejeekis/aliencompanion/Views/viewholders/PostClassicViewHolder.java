package com.gDyejeekis.aliencompanion.Views.viewholders;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.ClickListeners.PostItemListener;
import com.gDyejeekis.aliencompanion.ClickListeners.PostItemOptionsListener;
import com.gDyejeekis.aliencompanion.Models.Thumbnail;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.squareup.picasso.Picasso;

/**
 * Created by George on 1/22/2017.
 */

public class PostClassicViewHolder extends PostViewHolder  {

    public TextView title;
    public TextView postDets1;
    public TextView postDets2;
    public TextView scoreText;
    public ImageView postImage;
    public ImageView upvote;
    public ImageView downvote;
    public ImageView save;
    public ImageView hide;
    public ImageView viewUser;
    public ImageView openBrowser;
    public ImageView moreOptions;
    public ImageView upvoteClassic;
    public ImageView downvoteClassic;
    public ImageView commentsIcon;
    //public LinearLayout linkButton;
    public LinearLayout layoutPostInfo;
    public LinearLayout layoutPostOptions;
    public LinearLayout commentsButton;

    private int postLinkResource, commentsResource;

    public PostClassicViewHolder(View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.txtView_postTitle);
        scoreText = (TextView) itemView.findViewById(R.id.textView_score_classic);
        postImage = (ImageView) itemView.findViewById(R.id.imgView_postImage);
        //linkButton = (LinearLayout) itemView.findViewById(R.id.layout_postLinkButton);
        layoutPostOptions = (LinearLayout) itemView.findViewById(R.id.layout_options);
        upvote =  (ImageView) itemView.findViewById(R.id.btn_upvote);
        upvoteClassic = (ImageView) itemView.findViewById(R.id.imageView_upvote_classic);
        downvote =  (ImageView) itemView.findViewById(R.id.btn_downvote);
        downvoteClassic = (ImageView) itemView.findViewById(R.id.imageView_downvote_classic);
        save =  (ImageView) itemView.findViewById(R.id.btn_save);
        hide =  (ImageView) itemView.findViewById(R.id.btn_hide);
        viewUser = (ImageView) itemView.findViewById(R.id.btn_view_user);
        openBrowser = (ImageView) itemView.findViewById(R.id.btn_open_browser);
        moreOptions =  (ImageView) itemView.findViewById(R.id.btn_more);

        layoutPostInfo = (LinearLayout) itemView.findViewById(R.id.layout_postInfo);
        postDets1 = (TextView) itemView.findViewById(R.id.small_card_details_1);
        postDets2 = (TextView) itemView.findViewById(R.id.small_card_details_2);
        commentsIcon = (ImageView) itemView.findViewById(R.id.imageView_comments_icon);
        commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);

        initIcons();
    }

    @Override
    public void bindModel(Context context, Submission post) {
        // set title
        title.setText(post.getTitle());
        // check if post is clicked
        if(post.isClicked()) {
            title.setTextColor(clickedTextColor);
        }
        else {
            title.setTextColor(MyApplication.textColor);
        }
        // set post thumbnail
        if(post.isSelf()) {
            postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
            layoutPostInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 15f));
        }
        else {
            Thumbnail thumbnailObject = post.getThumbnailObject()==null ? new Thumbnail() : post.getThumbnailObject();
            if (post.isNSFW() && !MyApplication.showNSFWpreview) {
                postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                layoutPostInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 15f));
            }
            else if(thumbnailObject.hasThumbnail()) {
                postImage.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 3f));
                layoutPostInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 12f));
                //postImage.setBackground(null);
                try {
                    Picasso.with(context).load(thumbnailObject.getUrl()).placeholder(R.drawable.noimage).into(postImage);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            else {
                postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                layoutPostInfo.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 15f));
            }
        }
        // set first row post details
        SpannableString dets1Spannable;
        String dets1 = post.getAuthor() + " · " + post.agePrepared + " · ";
        if(post.isSelf()) {
            dets1 += post.getDomain();
        }
        else {
            dets1 += post.getSubreddit() + " · " + post.getDomain();
        }
        if(post.getLinkFlairText() != null) {
            dets1Spannable = new SpannableString(post.getLinkFlairText() + " · " + dets1);
            dets1Spannable.setSpan(new ForegroundColorSpan(MyApplication.linkColor), 0, post.getLinkFlairText().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        else {
            dets1Spannable = new SpannableString(dets1);
        }
        postDets1.setText(dets1Spannable);
        // set second row post details
        String dets2 = post.getCommentCount() + " comments";
        postDets2.setText(dets2);
        if(post.isNSFW()) {
            appendNsfwLabel(context, postDets2);
        }
        if(post.isSpoiler()) {
            appendSpoilerLabel(postDets2);
        }
        // set score color and icons depending on user
        SpannableString scoreSpannable = new SpannableString(post.getScore().toString()); // TODO: 1/24/2017 get condensed score
        if(MyApplication.currentUser != null) {
            // check user vote
            if (post.getLikes().equals("true")) {
                scoreSpannable.setSpan(new TextAppearanceSpan(context, R.style.upvotedStyle), 0, scoreSpannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                upvoteClassic.setImageResource(upvoteResourceOrange);
                downvoteClassic.setImageResource(downvoteResource);
            }
            else if (post.getLikes().equals("false")) {
                scoreSpannable.setSpan(new TextAppearanceSpan(context, R.style.downvotedStyle), 0, scoreSpannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                upvoteClassic.setImageResource(upvoteResource);
                downvoteClassic.setImageResource(downvoteResourceBlue);
            }
            else {
                scoreText.setTextColor(MyApplication.textHintColor);
                upvoteClassic.setImageResource(upvoteResource);
                downvoteClassic.setImageResource(downvoteResource);
            }
            // check saved post
            if(post.isSaved()) save.setImageResource(saveResourceYellow);
            else save.setImageResource(saveResource);
            // check hidden post
            if(post.isHidden()) hide.setImageResource(hideResourceRed);
            else hide.setImageResource(hideResource);
        }
        else {
            upvoteClassic.setImageResource(upvoteResource);
            downvoteClassic.setImageResource(downvoteResource);
            save.setImageResource(saveResource);
            hide.setImageResource(hideResource);
        }
        scoreText.setText(scoreSpannable);
        // hide post options default upvote/downvote buttons
        upvote.setVisibility(View.GONE);
        downvote.setVisibility(View.GONE);
        // set post options background color
        layoutPostOptions.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        // set remaining icon resources
        viewUser.setImageResource(viewUserResource);
        openBrowser.setImageResource(openBrowserResource);
        moreOptions.setImageResource(moreResource);
        commentsIcon.setImageResource(commentsResource);
    }

    @Override
    public void setClickListeners(PostItemListener postItemListener, View.OnLongClickListener postLongListener, PostItemOptionsListener optionsListener) {
        postImage.setOnClickListener(postItemListener);
        title.setOnClickListener(postItemListener);
        commentsButton.setOnClickListener(postItemListener);

        postImage.setOnLongClickListener(postLongListener);
        title.setOnLongClickListener(postLongListener);
        commentsButton.setOnLongClickListener(postLongListener);

        upvoteClassic.setOnClickListener(optionsListener);
        downvoteClassic.setOnClickListener(optionsListener);
        save.setOnClickListener(optionsListener);
        hide.setOnClickListener(optionsListener);
        viewUser.setOnClickListener(optionsListener);
        openBrowser.setOnClickListener(optionsListener);
        moreOptions.setOnClickListener(optionsListener);
    }

    @Override
    public void setPostOptionsVisible(boolean flag) {
        if(flag) {
            layoutPostOptions.setVisibility(View.VISIBLE);
        }
        else {
            layoutPostOptions.setVisibility(View.GONE);
        }
    }

    private void initIcons() {
        upvoteResourceOrange = R.mipmap.ic_upvote_classic_orange_48dp;
        downvoteResourceBlue = R.mipmap.ic_downvote_classic_blue_48dp;
        switch (MyApplication.currentBaseTheme) {
            case MyApplication.LIGHT_THEME:
                initGreyColorIcons();
                break;
            case MyApplication.DARK_THEME_LOW_CONTRAST:
                initLightGreyColorIcons();
                break;
            default:
                initWhiteColorIcons();
                break;
        }
    }

    @Override
    protected void initWhiteColorIcons() {
        super.initWhiteColorIcons();
        upvoteResource = R.mipmap.ic_upvote_classic_white_48dp;
        downvoteResource = R.mipmap.ic_downvote_classic_white_48dp;
        postLinkResource = R.drawable.ic_link_white_48dp;
        commentsResource = R.mipmap.ic_comment_white_24dp;
    }

    @Override
    protected void initGreyColorIcons() {
        super.initGreyColorIcons();
        upvoteResource = R.mipmap.ic_upvote_classic_grey_48dp;
        downvoteResource = R.mipmap.ic_downvote_classic_grey_48dp;
        postLinkResource = R.drawable.ic_link_grey_48dp;
        commentsResource = R.mipmap.ic_comment_grey_600_24dp;
    }

    @Override
    protected void initLightGreyColorIcons() {
        super.initLightGreyColorIcons();
        upvoteResource = R.mipmap.ic_upvote_classic_light_grey_48dp;
        downvoteResource = R.mipmap.ic_downvote_classic_light_grey_48dp;
        postLinkResource = R.drawable.ic_link_light_grey_48dp;
        commentsResource = R.mipmap.ic_comment_light_grey_24dp;
    }
}
