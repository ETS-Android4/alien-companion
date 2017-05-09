package com.gDyejeekis.aliencompanion.views.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.enums.PostViewType;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemOptionsListener;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Submission;

/**
 * Created by George on 1/22/2017.
 */

public abstract class PostViewHolder extends RecyclerView.ViewHolder {

    int saveResource, hideResource, moreResource, viewUserResource, shareResource, openBrowserResource, saveResourceYellow, hideResourceRed,
            upvoteResource, downvoteResource, upvoteResourceOrange, downvoteResourceBlue, commentsResource, linkResource;

    PostViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bindModel(Context context, Submission submission);

    public abstract void setClickListeners(PostItemListener postItemListener, View.OnLongClickListener postLongListener, PostItemOptionsListener postItemOptionsListener);

    public abstract void setPostOptionsVisible(boolean flag);

    void initIconResources(PostViewType viewType) {
        switch (viewType) {
            case list:
            case listReversed:
            case smallCards:
                commentsResource = MyApplication.nightThemeEnabled ? R.drawable.ic_comment_white_48dp : R.drawable.ic_comment_black_48dp;
                linkResource = R.drawable.ic_language_white_48dp;
                upvoteResource = R.drawable.ic_arrow_upward_white_48dp;
                downvoteResource = R.drawable.ic_arrow_downward_white_48dp;
                saveResource = R.drawable.ic_star_border_white_48dp;
                hideResource = R.drawable.ic_close_white_48dp;
                viewUserResource = R.drawable.ic_person_white_48dp;
                shareResource = R.drawable.ic_share_white_48dp;
                openBrowserResource = R.drawable.ic_open_in_browser_white_48dp;
                moreResource = R.drawable.ic_more_vert_white_48dp;
                upvoteResourceOrange = R.drawable.ic_arrow_upward_upvote_orange_48dp;
                downvoteResourceBlue = R.drawable.ic_arrow_downward_downvote_blue_48dp;
                break;
            case cards:
            case cardDetails:
            case classic:
                // dark background
                if(MyApplication.nightThemeEnabled) {
                    commentsResource = R.drawable.ic_comment_white_48dp;
                    saveResource = R.drawable.ic_star_border_white_48dp;
                    hideResource = R.drawable.ic_close_white_48dp;
                    viewUserResource = R.drawable.ic_person_white_48dp;
                    shareResource = R.drawable.ic_share_white_48dp;
                    openBrowserResource = R.drawable.ic_open_in_browser_white_48dp;
                    moreResource = R.drawable.ic_more_vert_white_48dp;
                    if (viewType == PostViewType.classic) {
                        upvoteResource = R.drawable.ic_expand_less_white_48dp;
                        downvoteResource = R.drawable.ic_expand_more_white_48dp;
                        upvoteResourceOrange = R.drawable.ic_expand_less_upvote_orange_48dp;
                        downvoteResourceBlue = R.drawable.ic_expand_more_downvote_blue_48dp;
                    } else {
                        upvoteResource = R.drawable.ic_arrow_upward_white_48dp;
                        downvoteResource = R.drawable.ic_arrow_downward_white_48dp;
                        upvoteResourceOrange = R.drawable.ic_arrow_upward_upvote_orange_48dp;
                        downvoteResourceBlue = R.drawable.ic_arrow_downward_downvote_blue_48dp;
                    }
                }
                // light background
                else {
                    commentsResource = R.drawable.ic_comment_black_48dp;
                    saveResource = R.drawable.ic_star_border_black_48dp;
                    hideResource = R.drawable.ic_close_black_48dp;
                    viewUserResource = R.drawable.ic_person_black_48dp;
                    shareResource = R.drawable.ic_share_black_48dp;
                    openBrowserResource = R.drawable.ic_open_in_browser_black_48dp;
                    moreResource = R.drawable.ic_more_vert_black_48dp;
                    if (viewType == PostViewType.classic) {
                        upvoteResource = R.drawable.ic_expand_less_black_48dp;
                        downvoteResource = R.drawable.ic_expand_more_black_48dp;
                        upvoteResourceOrange = R.drawable.ic_expand_less_upvote_orange_48dp;
                        downvoteResourceBlue = R.drawable.ic_expand_more_downvote_blue_48dp;
                    } else {
                        upvoteResource = R.drawable.ic_arrow_upward_black_48dp;
                        downvoteResource = R.drawable.ic_arrow_downward_black_48dp;
                        upvoteResourceOrange = R.drawable.ic_arrow_upward_upvote_orange_48dp;
                        downvoteResourceBlue = R.drawable.ic_arrow_downward_downvote_blue_48dp;
                    }
                }
                break;
            //case gallery:
            //    linkResource = MyApplication.nightThemeEnabled ? R.drawable.ic_link_white_48dp : R.drawable.ic_link_black_48dp;
            //    break;
        }
        saveResourceYellow = R.drawable.ic_star_border_yellow_500_48dp;
        hideResourceRed = R.drawable.ic_close_red_600_48dp;
    }

    void appendNsfwLabel(Context context, TextView textView) {
        SpannableString nsfwSpan = new SpannableString(" · NSFW");
        nsfwSpan.setSpan(new TextAppearanceSpan(context, R.style.nsfwLabel), 2, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.append(nsfwSpan);
    }

    void appendSpoilerLabel(TextView textView) {
        SpannableString spoilerSpan = new SpannableString(" · SPOILER");
        spoilerSpan.setSpan(new ForegroundColorSpan(MyApplication.linkColor), 2, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.append(spoilerSpan);
    }

}
