package com.gDyejeekis.aliencompanion.Views.viewholders;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
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

public class PostListViewHolder extends PostViewHolder {

    public TextView title;
    public TextView commentsText;
    public TextView postDets1;
    public TextView postDets2;
    public ImageView postImage;
    public ImageView upvote;
    public ImageView downvote;
    public ImageView save;
    public ImageView hide;
    public ImageView moreOptions;
    public ImageView viewUser;
    public ImageView openBrowser;
    public ImageView commentsIcon;
    public LinearLayout linkButton;
    public LinearLayout layoutPostOptions;
    public LinearLayout commentsButton;

    private int commentsResource, commentsResourceClicked;

    public PostListViewHolder(View itemView) {
        super(itemView);

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

        initIcons();
    }

    @Override
    public void bindModel(Context context, Submission post) {
        // set title
        title.setText(post.getTitle());
        // check if post is clicked
        if(post.isClicked()) {
            title.setTextColor(post.isStickied() && post.showAsStickied ? MyApplication.textColorStickiedClicked : clickedTextColor);
            commentsText.setTextColor(clickedTextColor);
            commentsIcon.setImageResource(commentsResourceClicked);
        }
        else {
            title.setTextColor(post.isStickied() && post.showAsStickied ? MyApplication.textColorStickied : MyApplication.textColor);
            commentsText.setTextColor(MyApplication.textColor);
            commentsIcon.setImageResource(commentsResource);
        }
        // set post thumbnail
        Thumbnail thumbnailObject = post.getThumbnailObject()==null ? new Thumbnail() : post.getThumbnailObject();
        if(thumbnailObject.hasThumbnail()) {
            if (post.isNSFW() && !MyApplication.showNSFWpreview) {
                //postImage.setImageResource(R.drawable.nsfw2);
                postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
            }
            else {
                postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                try {
                    Picasso.with(context).load(thumbnailObject.getUrl()).placeholder(R.drawable.noimage).into(postImage);
                } catch (IllegalArgumentException e) {e.printStackTrace();}
            }
        }
        else {
            postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
        }
        // set first row of post details
        SpannableString detsOneSpannable;
        String detsOneText = post.getScore() + " · " + post.agePrepared + " · " + post.getAuthor();
        int scoreStart;
        int scoreEnd;
        if(post.getLinkFlairText() != null) {
            detsOneText = post.getLinkFlairText() + " · " + detsOneText;
            detsOneSpannable = new SpannableString(detsOneText);
            detsOneSpannable.setSpan(new ForegroundColorSpan(MyApplication.linkColor), 0, post.getLinkFlairText().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            scoreStart = detsOneText.indexOf("·") + 2;
            scoreEnd = detsOneText.indexOf(" ", scoreStart);
        }
        else {
            detsOneSpannable = new SpannableString(detsOneText);
            scoreStart = 0;
            scoreEnd = detsOneText.indexOf(" ");
        }
        // set icon colors and score color depending on user
        if(MyApplication.currentUser != null) {
            // check user vote
            if (post.getLikes().equals("true")) {
                detsOneSpannable.setSpan(new TextAppearanceSpan(context, R.style.upvotedStyle), scoreStart, scoreEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                upvote.setImageResource(upvoteResourceOrange);
                downvote.setImageResource(downvoteResource);
            }
            else if (post.getLikes().equals("false")) {
                detsOneSpannable.setSpan(new TextAppearanceSpan(context, R.style.downvotedStyle), scoreStart, scoreEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                upvote.setImageResource(upvoteResource);
                downvote.setImageResource(downvoteResourceBlue);
            }
            else {
                postDets1.setTextColor(MyApplication.textHintColor);
                upvote.setImageResource(upvoteResource);
                downvote.setImageResource(downvoteResource);
            }
            // check saved post
            if(post.isSaved()) save.setImageResource(saveResourceYellow);
            else save.setImageResource(saveResource);
            // check hidden post
            if(post.isHidden()) hide.setImageResource(hideResourceRed);
            else hide.setImageResource(hideResource);
        }
        else {
            upvote.setImageResource(upvoteResource);
            downvote.setImageResource(downvoteResource);
            save.setImageResource(saveResource);
            hide.setImageResource(hideResource);
        }
        postDets1.setText(detsOneSpannable);
        // set second row of post details
        String detsTwoText = (post.isSelf()) ? post.getDomain() : post.getSubreddit() + " · " + post.getDomain();
        postDets2.setText(detsTwoText);
        if(post.isNSFW()) {
            appendNsfwLabel(context, postDets2);
        }
        if(post.isSpoiler()) {
            appendSpoilerLabel(postDets2);
        }
        // set comments text
        commentsText.setText(post.getCommentCount().toString());
        // set post options backgound color
        layoutPostOptions.setBackgroundColor(MyApplication.currentColor);
        // set remaining icon resources
        viewUser.setImageResource(viewUserResource);
        openBrowser.setImageResource(openBrowserResource);
        moreOptions.setImageResource(moreResource);
    }

    @Override
    public void setClickListeners(PostItemListener postItemListener, View.OnLongClickListener postLongListener, PostItemOptionsListener optionsListener) {
        linkButton.setOnClickListener(postItemListener);
        commentsButton.setOnClickListener(postItemListener);
        linkButton.setOnLongClickListener(postLongListener);
        commentsButton.setOnLongClickListener(postLongListener);

        upvote.setOnClickListener(optionsListener);
        downvote.setOnClickListener(optionsListener);
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
        upvoteResourceOrange = R.mipmap.ic_arrow_upward_orange_48dp;
        downvoteResourceBlue = R.mipmap.ic_arrow_downward_blue_48dp;
        switch (MyApplication.currentBaseTheme) {
            case MyApplication.DARK_THEME_LOW_CONTRAST:
                initLightGreyColorIcons();
                commentsResource = R.mipmap.ic_comment_light_grey_24dp;
                commentsResourceClicked = R.mipmap.ic_comment_grey_600_24dp;
                break;
            case MyApplication.LIGHT_THEME:
                initWhiteColorIcons();
                commentsResource = R.mipmap.ic_comment_grey_600_24dp;
                commentsResourceClicked = R.mipmap.ic_comment_light_grey_24dp;
                break;
            default:
                initWhiteColorIcons();
                commentsResource = R.mipmap.ic_comment_white_24dp;
                commentsResourceClicked = R.mipmap.ic_comment_light_grey_24dp;
                break;
        }
    }

    @Override
    protected void initWhiteColorIcons() {
        super.initWhiteColorIcons();
        upvoteResource = R.mipmap.ic_arrow_upward_white_48dp;
        downvoteResource = R.mipmap.ic_arrow_downward_white_48dp;
    }

    @Override
    protected void initLightGreyColorIcons() {
        super.initLightGreyColorIcons();
        upvoteResource = R.mipmap.ic_arrow_upward_light_grey_48dp;
        downvoteResource = R.mipmap.ic_arrow_downward_light_grey_48dp;
    }
}
