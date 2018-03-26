package com.gDyejeekis.aliencompanion.views.viewholders;

import android.content.Context;
import android.content.res.Configuration;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.AppConstants;
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

public class PostListViewHolder extends PostViewHolder {

    public static boolean shareIconVisible, openBrowserIconVisible;

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
    public ImageView share;
    public ImageView openBrowser;
    public ImageView commentsIcon;
    public LinearLayout linkButton;
    public LinearLayout layoutPostOptions;
    public LinearLayout layoutThumbnail;
    public LinearLayout commentsButton;

    private float defaultIconOpacity, defaultIconOpacityDisabled, commentIconOpacity, commentIconOpacityClicked;

    public PostListViewHolder(View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.txtView_postTitle);
        commentsText = (TextView) itemView.findViewById(R.id.textView_comments);
        postImage = (ImageView) itemView.findViewById(R.id.imgView_postImage);
        linkButton = (LinearLayout) itemView.findViewById(R.id.layout_postLinkButton);
        upvote =  (ImageView) itemView.findViewById(R.id.btn_upvote);
        layoutPostOptions = (LinearLayout) itemView.findViewById(R.id.layout_postOptions);
        layoutThumbnail = itemView.findViewById(R.id.layout_thumbnail);
        downvote =  (ImageView) itemView.findViewById(R.id.btn_downvote);
        save =  (ImageView) itemView.findViewById(R.id.btn_save);
        hide =  (ImageView) itemView.findViewById(R.id.btn_hide);
        moreOptions =  (ImageView) itemView.findViewById(R.id.btn_more);
        viewUser = (ImageView) itemView.findViewById(R.id.btn_view_user);
        share = (ImageView) itemView.findViewById(R.id.btn_share);
        openBrowser = (ImageView) itemView.findViewById(R.id.btn_open_browser);
        commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);
        commentsIcon = (ImageView) itemView.findViewById(R.id.imgView_commentsIcon);
        postDets1 = (TextView) itemView.findViewById(R.id.textView_dets1);
        postDets2 = (TextView) itemView.findViewById(R.id.textView_dets2);

        initIcons();
    }

    private void initIcons() {
        initIconResources(PostViewType.list);

        switch (MyApplication.currentBaseTheme) {
            case AppConstants.DARK_THEME_LOW_CONTRAST:
                defaultIconOpacity = 0.6f;
                defaultIconOpacityDisabled = 0.3f;
                commentIconOpacity = 0.4f;
                commentIconOpacityClicked = 0.2f;
                break;
            case AppConstants.LIGHT_THEME:
                defaultIconOpacity = 1f;
                defaultIconOpacityDisabled = 0.5f;
                commentIconOpacity = 0.44f;
                commentIconOpacityClicked = 0.28f;
                break;
            default:
                defaultIconOpacity = 1f;
                defaultIconOpacityDisabled = 0.5f;
                commentIconOpacity = 0.6f;
                commentIconOpacityClicked = 0.3f;
                break;
        }
        // set unchanging properties of icons
        commentsIcon.setImageResource(commentsResource);
        viewUser.setImageResource(viewUserResource);
        share.setImageResource(shareResource);
        openBrowser.setImageResource(openBrowserResource);
        moreOptions.setImageResource(moreResource);
        viewUser.setAlpha(defaultIconOpacity);
        share.setAlpha(defaultIconOpacity);
        openBrowser.setAlpha(defaultIconOpacity);
        moreOptions.setAlpha(defaultIconOpacity);
    }

    @Override
    public void bindModel(Context context, Submission post) {
        // set title
        title.setText(post.getTitle());
        // check if post is clicked
        if(post.isClicked()) {
            title.setTextColor(post.isStickied() && post.showAsStickied ? MyApplication.textColorStickiedClicked : MyApplication.textSecondaryColor);
            commentsText.setTextColor(MyApplication.textSecondaryColor);
            commentsIcon.setAlpha(commentIconOpacityClicked);
        }
        else {
            title.setTextColor(post.isStickied() && post.showAsStickied ? MyApplication.textColorStickied : MyApplication.textPrimaryColor);
            commentsText.setTextColor(MyApplication.textPrimaryColor);
            commentsIcon.setAlpha(commentIconOpacity);
        }
        // set post thumbnail
        Thumbnail thumbnailObject = post.getThumbnailObject()==null ? new Thumbnail() : post.getThumbnailObject();
        if(thumbnailObject.hasThumbnail() && !post.isSpoiler()) {
            if (post.isNSFW() && !MyApplication.showNsfwPreviews) {
                //postImage.setImageResource(R.drawable.nsfw2);
                layoutThumbnail.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
            }
            else {
                layoutThumbnail.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 2f));
                try {
                    Picasso.with(context).load(thumbnailObject.getUrl()).placeholder(R.drawable.noimage).into(postImage);
                } catch (IllegalArgumentException e) {e.printStackTrace();}
            }
        }
        else {
            layoutThumbnail.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
        }
        // set first row of post details
        SpannableString detsOneSpannable;
        String detsOneText = post.getScore() + " · " + post.agePrepared + " · " + post.getAuthor();
        int scoreStart;
        int scoreEnd;
        if(post.getLinkFlairText() != null && !post.getLinkFlairText().trim().isEmpty()) {
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
                upvote.setAlpha(1f);
                downvote.setImageResource(downvoteResource);
                downvote.setAlpha(defaultIconOpacity);
            }
            else if (post.getLikes().equals("false")) {
                detsOneSpannable.setSpan(new TextAppearanceSpan(context, R.style.downvotedStyle), scoreStart, scoreEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                upvote.setImageResource(upvoteResource);
                upvote.setAlpha(defaultIconOpacity);
                downvote.setImageResource(downvoteResourceBlue);
                downvote.setAlpha(1f);
            }
            else {
                postDets1.setTextColor(MyApplication.textSecondaryColor);
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

        // set post menu bar backgound color
        layoutPostOptions.setBackgroundColor(MyApplication.currentPrimaryColor);

        //change menu bar item visibility depending on available space
        boolean islandscape = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if((MyApplication.isLargeScreen || islandscape)) {
            shareIconVisible = MyApplication.isLargeScreen || !MyApplication.dualPaneActive;
            if(MyApplication.isVeryLargeScreen || (MyApplication.isLargeScreen && islandscape)) {
                openBrowserIconVisible = MyApplication.isVeryLargeScreen || !MyApplication.dualPaneActive;
            }
            else {
                openBrowserIconVisible = false;
            }
        }
        else {
            shareIconVisible = false;
            openBrowserIconVisible = false;
        }
        share.setVisibility(shareIconVisible ? View.VISIBLE : View.GONE);
        openBrowser.setVisibility(openBrowserIconVisible ? View.VISIBLE : View.GONE);
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
        share.setOnClickListener(optionsListener);
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
