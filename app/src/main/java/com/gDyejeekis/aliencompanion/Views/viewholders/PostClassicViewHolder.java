package com.gDyejeekis.aliencompanion.Views.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.ClickListeners.PostItemListener;
import com.gDyejeekis.aliencompanion.ClickListeners.PostItemOptionsListener;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Submission;

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
    public LinearLayout linkButton;
    public LinearLayout commentsButton;
    public LinearLayout layoutPostOptions;

    public PostClassicViewHolder(View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.txtView_postTitle);
        scoreText = (TextView) itemView.findViewById(R.id.textView_score_classic);
        postImage = (ImageView) itemView.findViewById(R.id.imgView_postImage);
        linkButton = (LinearLayout) itemView.findViewById(R.id.layout_postLinkButton);
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

        commentsButton = (LinearLayout) itemView.findViewById(R.id.layout_postCommentsButton);
        postDets1 = (TextView) itemView.findViewById(R.id.small_card_details_1);
        postDets2 = (TextView) itemView.findViewById(R.id.small_card_details_2);

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
        // TODO: 1/23/2017
        // set first row post details
        // TODO: 1/23/2017
        // set second row post details
        // TODO: 1/23/2017
        // set score color and icons depending on user
        // TODO: 1/23/2017
        // hide post options default upvote/downvote buttons
        upvote.setVisibility(View.GONE);
        downvote.setVisibility(View.GONE);
        // set post options background color
        layoutPostOptions.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        // set remaining icon resources
        viewUser.setImageResource(viewUserResource);
        moreOptions.setImageResource(moreResource);
    }

    @Override
    public void setClickListeners(PostItemListener postItemListener, View.OnLongClickListener postLongListener, PostItemOptionsListener optionsListener) {
        linkButton.setOnClickListener(postItemListener);
        commentsButton.setOnClickListener(postItemListener);
        linkButton.setOnLongClickListener(postLongListener);
        commentsButton.setOnLongClickListener(postLongListener);

        upvoteClassic.setOnClickListener(optionsListener);
        downvoteClassic.setOnClickListener(optionsListener);
        save.setOnClickListener(optionsListener);
        hide.setOnClickListener(optionsListener);
        viewUser.setOnClickListener(optionsListener);
        openBrowser.setOnClickListener(optionsListener);
        moreOptions.setOnClickListener(optionsListener);
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
    }

    @Override
    protected void initGreyColorIcons() {
        super.initGreyColorIcons();
        upvoteResource = R.mipmap.ic_upvote_classic_grey_48dp;
        downvoteResource = R.mipmap.ic_downvote_classic_grey_48dp;
    }

    @Override
    protected void initLightGreyColorIcons() {
        super.initLightGreyColorIcons();
        upvoteResource = R.mipmap.ic_upvote_classic_light_grey_48dp;
        downvoteResource = R.mipmap.ic_downvote_classic_light_grey_48dp;
    }
}
