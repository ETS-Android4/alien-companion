package com.gDyejeekis.aliencompanion.views.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemOptionsListener;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.api.entity.Submission;

/**
 * Created by George on 1/22/2017.
 */

public abstract class PostViewHolder extends RecyclerView.ViewHolder {

    protected static final int clickedTextColor = MyApplication.textHintColor;

    protected int saveResource, hideResource, moreResource, viewUserResource, openBrowserResource, saveResourceYellow, hideResourceRed,
            upvoteResource, downvoteResource, upvoteResourceOrange, downvoteResourceBlue;

    public PostViewHolder(View itemView) {
        super(itemView);
        saveResourceYellow = R.mipmap.ic_star_border_yellow_500_48dp;
        hideResourceRed = R.mipmap.ic_close_red_800_48dp;
    }

    public abstract void bindModel(Context context, Submission submission);

    public abstract void setClickListeners(PostItemListener postItemListener, View.OnLongClickListener postLongListener, PostItemOptionsListener postItemOptionsListener);

    public abstract void setPostOptionsVisible(boolean flag);

    protected void initWhiteColorIcons() {
        saveResource = R.mipmap.ic_star_border_white_48dp;
        hideResource = R.mipmap.ic_close_white_48dp;
        moreResource = R.mipmap.ic_more_vert_white_48dp;
        viewUserResource = R.mipmap.ic_person_white_48dp;
        openBrowserResource = R.mipmap.ic_open_in_browser_white_48dp;
    }

    protected void initGreyColorIcons() {
        saveResource = R.mipmap.ic_star_border_grey_48dp;
        hideResource = R.mipmap.ic_close_grey_48dp;
        moreResource = R.mipmap.ic_more_vert_grey_48dp;
        viewUserResource = R.mipmap.ic_person_grey_48dp;
        openBrowserResource = R.mipmap.ic_open_in_browser_grey_48dp;
    }

    protected void initLightGreyColorIcons() {
        saveResource = R.mipmap.ic_star_border_light_grey_48dp;
        hideResource = R.mipmap.ic_clear_light_grey_48dp;
        moreResource = R.mipmap.ic_more_vert_light_grey_48dp;
        viewUserResource = R.mipmap.ic_person_light_grey_48dp;
        openBrowserResource = R.mipmap.ic_open_in_browser_light_grey_48dp;
    }

    protected void appendNsfwLabel(Context context, TextView textView) {
        SpannableString nsfwSpan = new SpannableString(" · NSFW");
        nsfwSpan.setSpan(new TextAppearanceSpan(context, R.style.nsfwLabel), 2, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.append(nsfwSpan);
    }

    protected void appendSpoilerLabel(TextView textView) {
        SpannableString spoilerSpan = new SpannableString(" · SPOILER");
        spoilerSpan.setSpan(new ForegroundColorSpan(MyApplication.linkColor), 2, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.append(spoilerSpan);
    }
}
