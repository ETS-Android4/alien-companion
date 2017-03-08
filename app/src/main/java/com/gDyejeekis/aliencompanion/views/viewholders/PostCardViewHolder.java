package com.gDyejeekis.aliencompanion.views.viewholders;

import android.content.Context;
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

import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemOptionsListener;
import com.gDyejeekis.aliencompanion.models.Thumbnail;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.utils.MyHtmlTagHandler;
import com.gDyejeekis.aliencompanion.utils.MyLinkMovementMethod;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

/**
 * Created by George on 1/22/2017.
 */

public class PostCardViewHolder extends PostViewHolder  {

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
    public ImageView moreOptions;
    public ImageView postImage;
    public RoundedImageView imageButton;
    public LinearLayout fullComments;
    public LinearLayout linkButton;
    public LinearLayout layoutPostOptions;
    public LinearLayout commentsButton;
    public LinearLayout layoutSelfText;
    public LinearLayout layoutGilded;
    public ProgressBar commentsProgress;

    public PostCardViewHolder(View itemView, boolean showDetails) {
        super(itemView);

        this.showDetails = showDetails;
        if(showDetails) {
            fullComments = (LinearLayout) itemView.findViewById(R.id.fullLoad);
            commentsProgress = (ProgressBar) itemView.findViewById(R.id.pBar_comments);
        }

        title = (TextView) itemView.findViewById(R.id.txtView_postTitle);
        goldCount = (TextView) itemView.findViewById(R.id.textView_gilded);
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
        layoutGilded = (LinearLayout) itemView.findViewById(R.id.layout_gilded);
        commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);
        domain2 = (TextView) itemView.findViewById(R.id.txtView_postDomain_two);
        fullUrl = (TextView) itemView.findViewById(R.id.txtView_postUrl);
        layoutSelfText = (LinearLayout) itemView.findViewById(R.id.layout_selfTextPreview);
        selfText = (TextView) itemView.findViewById(R.id.txtView_selfTextPreview);
        scoreText = (TextView) itemView.findViewById(R.id.textView_score);
        imageButton = (RoundedImageView) itemView.findViewById(R.id.imageButton);

        initIcons();
    }

    @Override
    public void bindModel(Context context, Submission post) {
        // set title
        title.setText(post.getTitle());
        // check if post is clicked
        if(post.isClicked() && !showDetails) {
            title.setTextColor(post.isStickied() && post.showAsStickied ? MyApplication.textColorStickiedClicked : clickedTextColor);
        }
        else {
            title.setTextColor(post.isStickied() && (post.showAsStickied || showDetails) ? MyApplication.textColorStickied : MyApplication.textColor);
        }
        // set post thumbnail or self text
        Thumbnail thumbnailObject = post.getThumbnailObject()==null ? new Thumbnail() : post.getThumbnailObject();
        if(post.isSelf()) {
            linkButton.setVisibility(View.GONE);
            imageButton.setVisibility(View.GONE);

            if(MyApplication.useMarkdownParsing) {
                // TODO: 1/23/2017
            }
            // parse html string using fromHtml()
            else {
                try {
                    if(showDetails) {
                        SpannableStringBuilder stringBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(post.getSelftextHTML(), null, new MyHtmlTagHandler()));
                        stringBuilder = ConvertUtils.modifyURLSpan(context, stringBuilder);
                        selfText.setText(stringBuilder);
                        selfText.setMovementMethod(MyLinkMovementMethod.getInstance());
                    }
                    else {
                        String text = ConvertUtils.noTrailingwhiteLines(Html.fromHtml(post.getSelftextHTML())).toString();
                        if (text.length() > 200) text = text.substring(0, 200) + " ...";
                        selfText.setText(text);
                    }
                    layoutSelfText.setVisibility(View.VISIBLE);
                } catch (NullPointerException  e) {
                    layoutSelfText.setVisibility(View.GONE);
                }
            }
        }
        else if(post.hasImageButton && thumbnailObject.hasThumbnail()) {
            layoutSelfText.setVisibility(View.GONE);
            linkButton.setVisibility(View.GONE);
            imageButton.setVisibility(View.VISIBLE);
            try {
                Picasso.with(context).load(thumbnailObject.getUrl())/*.centerCrop().resize(1000, 300)*/.into(imageButton);
            } catch (IllegalArgumentException e) {e.printStackTrace();}
        }
        else {
            layoutSelfText.setVisibility(View.GONE);
            imageButton.setVisibility(View.GONE);
            linkButton.setVisibility(View.VISIBLE);
            domain2.setText(post.getDomain());
            domain2.setTextColor(MyApplication.linkColor);
            fullUrl.setText(post.getURL());
            if(thumbnailObject.hasThumbnail()) {
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
        if(post.getLinkFlairText() != null) {
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
        SpannableString scoreSpannable = new SpannableString(post.getScore() + " score");
        if(MyApplication.currentUser != null) {
            // check user vote
            int scoreEnd = post.getScore().toString().length();
            if (post.getLikes().equals("true")) {
                scoreSpannable.setSpan(new TextAppearanceSpan(context, R.style.upvotedStyle), 0, scoreEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                upvote.setImageResource(R.mipmap.ic_arrow_upward_orange_48dp);
                downvote.setImageResource(downvoteResource);
            }
            else if (post.getLikes().equals("false")) {
                scoreSpannable.setSpan(new TextAppearanceSpan(context, R.style.downvotedStyle), 0, scoreEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                upvote.setImageResource(upvoteResource);
                downvote.setImageResource(R.mipmap.ic_arrow_downward_blue_48dp);
            }
            else {
                scoreText.setTextColor(MyApplication.textHintColor);
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
        scoreText.setText(scoreSpannable);
        // set post comments
        commentsText.setText(post.getCommentCount() + " comments");
        // set remaining icon resources
        moreOptions.setImageResource(moreResource);
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
        moreOptions.setOnClickListener(postItemOptionsListener);
    }

    @Override
    public void setPostOptionsVisible(boolean flag) {

    }

    private void initIcons() {
        upvoteResourceOrange = R.mipmap.ic_arrow_upward_orange_48dp;
        downvoteResourceBlue = R.mipmap.ic_arrow_downward_blue_48dp;
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
        upvoteResource = R.mipmap.ic_arrow_upward_white_48dp;
        downvoteResource = R.mipmap.ic_arrow_downward_white_48dp;
    }

    @Override
    protected void initGreyColorIcons() {
        super.initGreyColorIcons();
        upvoteResource = R.mipmap.ic_arrow_upward_grey_48dp;
        downvoteResource = R.mipmap.ic_arrow_downward_grey_48dp;
    }

    @Override
    protected void initLightGreyColorIcons() {
        super.initLightGreyColorIcons();
        upvoteResource = R.mipmap.ic_arrow_upward_light_grey_48dp;
        downvoteResource = R.mipmap.ic_arrow_downward_light_grey_48dp;
    }

}
