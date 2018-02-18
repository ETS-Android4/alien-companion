package com.gDyejeekis.aliencompanion.views.viewholders;

import android.content.Context;
import android.content.res.Configuration;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by George on 1/22/2017.
 */

public class PostSmallCardViewHolder extends PostViewHolder  {

    public static boolean shareIconVisible, openBrowserIconVisible;

    public TextView title;
    public TextView postDets1;
    public TextView postDets2;
    public ImageView postImage;
    public ImageView upvote;
    public ImageView downvote;
    public ImageView save;
    public ImageView hide;
    public ImageView share;
    public ImageView openBrowser;
    public ImageView moreOptions;
    public ImageView viewUser;
    public LinearLayout linkButton;
    public LinearLayout commentsButton;
    public LinearLayout layoutPostOptions;

    private float defaultIconOpacity, defaultIconOpacityDisabled;

    public PostSmallCardViewHolder(View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.txtView_postTitle);
        postImage = (ImageView) itemView.findViewById(R.id.imgView_postImage);
        linkButton = (LinearLayout) itemView.findViewById(R.id.layout_postLinkButton);
        layoutPostOptions = (LinearLayout) itemView.findViewById(R.id.layout_options);
        upvote =  (ImageView) itemView.findViewById(R.id.btn_upvote);
        downvote =  (ImageView) itemView.findViewById(R.id.btn_downvote);
        save =  (ImageView) itemView.findViewById(R.id.btn_save);
        hide =  (ImageView) itemView.findViewById(R.id.btn_hide);
        viewUser = (ImageView) itemView.findViewById(R.id.btn_view_user);
        share = (ImageView) itemView.findViewById(R.id.btn_share);
        openBrowser = (ImageView) itemView.findViewById(R.id.btn_open_browser);
        moreOptions =  (ImageView) itemView.findViewById(R.id.btn_more);
        commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);
        postDets1 = (TextView) itemView.findViewById(R.id.small_card_details_1);
        postDets2 = (TextView) itemView.findViewById(R.id.small_card_details_2);

        initIcons();
    }

    private void initIcons() {
        initIconResources(PostViewType.smallCards);
        switch (MyApplication.currentBaseTheme) {
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
        }
        else {
            title.setTextColor(post.isStickied() && post.showAsStickied ? MyApplication.textColorStickied : MyApplication.textPrimaryColor);
        }
        // set post thumbnail
        if(post.isSelf()) {
            linkButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
        }
        else {
            Thumbnail postThumbnail = post.getThumbnailObject()==null ? new Thumbnail() : post.getThumbnailObject();
            if (post.isNSFW() && !MyApplication.showNSFWpreview) {
                linkButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                linkButton.setPadding(10, 10, 0, 10);
                linkButton.setBackground(null);
                postImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                postImage.setImageResource(R.drawable.nsfw2);
                postImage.setAlpha(1f);
            }
            else if(postThumbnail.hasThumbnail() && !post.isSpoiler()) {
                linkButton.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2f));
                linkButton.setPadding(10, 10, 0, 10);
                linkButton.setBackground(null);
                postImage.setAlpha(1f);
                try {
                    //Get Post Thumbnail
                    Callback callback = new Callback() {
                        @Override
                        public void onSuccess() {
                            postImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        }

                        @Override
                        public void onError() {
                            postImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        }
                    };
                    Picasso.with(context).load(postThumbnail.getUrl()).placeholder(R.drawable.noimage).into(postImage, callback);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            else {
                linkButton.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
                linkButton.setPadding(10, 10, 10, 10);
                linkButton.setBackgroundColor(MyApplication.smallCardLinkBackground);
                postImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                postImage.setImageResource(linkResource);
                postImage.setAlpha(defaultIconOpacity);
            }
        }
        // set first row of post details
        String detsOneText = post.getAuthor() + " · " + post.agePrepared + " · ";
        if(post.isSelf()) {
            detsOneText += post.getDomain();
        }
        else {
            detsOneText += post.getSubreddit() + " · " + post.getDomain();
        }
        SpannableString detsOneSpannable;
        if(post.getLinkFlairText() != null && !post.getLinkFlairText().trim().isEmpty()) {
            detsOneSpannable = new SpannableString(post.getLinkFlairText() + " · " + detsOneText);
            detsOneSpannable.setSpan(new ForegroundColorSpan(MyApplication.linkColor), 0, post.getLinkFlairText().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        else {
            detsOneSpannable = new SpannableString(detsOneText);
        }
        postDets1.setText(detsOneSpannable);
        if(post.isNSFW()) {
            appendNsfwLabel(context, postDets1);
        }
        if(post.isSpoiler()) {
            appendSpoilerLabel(postDets1);
        }
        // set second row of post details
        SpannableString detsTwoSpannable = new SpannableString(post.getScore() + " points · " + post.getCommentCount() + " comments");
        // set icon colors and score color depending on user
        if(MyApplication.currentUser != null) {
            // check user vote
            int scoreEnd = post.getScore().toString().length();
            if (post.getLikes().equals("true")) {
                detsTwoSpannable.setSpan(new TextAppearanceSpan(context, R.style.upvotedStyle), 0, scoreEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                upvote.setImageResource(upvoteResourceOrange);
                upvote.setAlpha(1f);
                downvote.setImageResource(downvoteResource);
                downvote.setAlpha(defaultIconOpacity);
            }
            else if (post.getLikes().equals("false")) {
                detsTwoSpannable.setSpan(new TextAppearanceSpan(context, R.style.downvotedStyle), 0, scoreEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                upvote.setImageResource(upvoteResource);
                upvote.setAlpha(defaultIconOpacity);
                downvote.setImageResource(downvoteResourceBlue);
                downvote.setAlpha(1f);
            }
            else {
                postDets2.setTextColor(MyApplication.textSecondaryColor);
                upvote.setImageResource(upvoteResource);
                upvote.setAlpha(defaultIconOpacity);
                downvote.setImageResource(downvoteResource);
                downvote.setAlpha(defaultIconOpacity);
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
            upvote.setImageResource(upvoteResource);
            downvote.setImageResource(downvoteResource);
            save.setImageResource(saveResource);
            hide.setImageResource(hideResource);
            upvote.setAlpha(defaultIconOpacityDisabled);
            downvote.setAlpha(defaultIconOpacityDisabled);
            save.setAlpha(defaultIconOpacityDisabled);
            hide.setAlpha(defaultIconOpacityDisabled);
        }
        postDets2.setText(detsTwoSpannable);

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
