package com.gDyejeekis.aliencompanion.views.viewholders;

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

import com.gDyejeekis.aliencompanion.enums.PostViewType;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemOptionsListener;
import com.gDyejeekis.aliencompanion.models.Thumbnail;
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
    //public ImageView upvote;
    //public ImageView downvote;
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

    private float defaultIconOpacity, defaultIconOpacityDisabled;

    public PostClassicViewHolder(View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.txtView_postTitle);
        scoreText = (TextView) itemView.findViewById(R.id.textView_score_classic);
        postImage = (ImageView) itemView.findViewById(R.id.imgView_postImage);
        //linkButton = (LinearLayout) itemView.findViewById(R.id.layout_postLinkButton);
        layoutPostOptions = (LinearLayout) itemView.findViewById(R.id.layout_options);
        upvoteClassic = (ImageView) itemView.findViewById(R.id.imageView_upvote_classic);
        downvoteClassic = (ImageView) itemView.findViewById(R.id.imageView_downvote_classic);
        save =  (ImageView) itemView.findViewById(R.id.btn_save);
        hide =  (ImageView) itemView.findViewById(R.id.btn_hide);
        viewUser = (ImageView) itemView.findViewById(R.id.btn_view_user);
        openBrowser = (ImageView) itemView.findViewById(R.id.btn_open_browser);
        moreOptions =  (ImageView) itemView.findViewById(R.id.btn_more);

        // hide upvote/downvote buttons from post menu bar
        ImageView upvote =  (ImageView) itemView.findViewById(R.id.btn_upvote);
        upvote.setVisibility(View.GONE);
        ImageView downvote =  (ImageView) itemView.findViewById(R.id.btn_downvote);
        downvote.setVisibility(View.GONE);

        layoutPostInfo = (LinearLayout) itemView.findViewById(R.id.layout_postInfo);
        postDets1 = (TextView) itemView.findViewById(R.id.small_card_details_1);
        postDets2 = (TextView) itemView.findViewById(R.id.small_card_details_2);
        commentsIcon = (ImageView) itemView.findViewById(R.id.imageView_comments_icon);
        commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);

        initIcons();
    }

    private void initIcons() {
        initIconResources(PostViewType.classic);
        switch (MyApplication.currentBaseTheme) {
            case MyApplication.LIGHT_THEME:
                defaultIconOpacity = 0.54f;
                defaultIconOpacityDisabled = 0.38f;
                break;
            case MyApplication.DARK_THEME_LOW_CONTRAST:
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
        openBrowser.setImageResource(openBrowserResource);
        moreOptions.setImageResource(moreResource);
        commentsIcon.setImageResource(commentsResource);
        viewUser.setAlpha(defaultIconOpacity);
        openBrowser.setAlpha(defaultIconOpacity);
        moreOptions.setAlpha(defaultIconOpacity);
        commentsIcon.setAlpha(defaultIconOpacity);
    }

    @Override
    public void bindModel(Context context, Submission post) {
        // set title
        title.setText(post.getTitle());
        // check if post is clicked
        if(post.isClicked()) {
            title.setTextColor(post.isStickied() && post.showAsStickied ? MyApplication.textColorStickiedClicked : MyApplication.textHintColor);
        }
        else {
            title.setTextColor(post.isStickied() && post.showAsStickied ? MyApplication.textColorStickied : MyApplication.textPrimaryColor);
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
            else if(thumbnailObject.hasThumbnail() && !post.isSpoiler()) {
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
        SpannableString scoreSpannable = new SpannableString(getCondensedScore(post.getScore()));
        if(MyApplication.currentUser != null) {
            // check user vote
            if (post.getLikes().equals("true")) {
                scoreSpannable.setSpan(new TextAppearanceSpan(context, R.style.upvotedStyleClassic), 0, scoreSpannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                upvoteClassic.setImageResource(upvoteResourceOrange);
                upvoteClassic.setAlpha(1f);
                downvoteClassic.setImageResource(downvoteResource);
                upvoteClassic.setAlpha(defaultIconOpacity);
            }
            else if (post.getLikes().equals("false")) {
                scoreSpannable.setSpan(new TextAppearanceSpan(context, R.style.downvotedStyleClassic), 0, scoreSpannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                upvoteClassic.setImageResource(upvoteResource);
                upvoteClassic.setAlpha(defaultIconOpacity);
                downvoteClassic.setImageResource(downvoteResourceBlue);
                downvoteClassic.setAlpha(1f);
            }
            else {
                scoreText.setTextColor(MyApplication.textSecondaryColor);
                upvoteClassic.setImageResource(upvoteResource);
                upvoteClassic.setAlpha(defaultIconOpacity);
                downvoteClassic.setImageResource(downvoteResource);
                downvoteClassic.setAlpha(defaultIconOpacity);
            }
            // check saved post
            if(post.isSaved()) {
                save.setImageResource(saveResourceYellow);
                save.setAlpha(1f);
            }
            else {
                save.setImageResource(saveResource);
                save.setAlpha(defaultIconOpacity);
            }
            // check hidden post
            if(post.isHidden()) {
                hide.setImageResource(hideResourceRed);
                hide.setAlpha(1f);
            }
            else {
                hide.setImageResource(hideResource);
                hide.setAlpha(defaultIconOpacity);
            }
        }
        else {
            upvoteClassic.setImageResource(upvoteResource);
            downvoteClassic.setImageResource(downvoteResource);
            save.setImageResource(saveResource);
            hide.setImageResource(hideResource);
            upvoteClassic.setAlpha(defaultIconOpacityDisabled);
            downvoteClassic.setAlpha(defaultIconOpacityDisabled);
            save.setAlpha(defaultIconOpacityDisabled);
            hide.setAlpha(defaultIconOpacityDisabled);
        }
        scoreText.setText(scoreSpannable);
        // set post menu bar background color
        layoutPostOptions.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
    }

    private String getCondensedScore(long score) {
        if(score > 9999) {
            Long divideBy = 1000L;
            String suffix = "k";
            long truncated = score / (divideBy / 10); //the number part of the output times 10
            boolean hasDecimal = truncated < 1000 && (truncated / 10d) != (truncated / 10);
            return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
        }
        return Long.toString(score);
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

}
