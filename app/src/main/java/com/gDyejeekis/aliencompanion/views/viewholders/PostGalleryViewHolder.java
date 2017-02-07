package com.gDyejeekis.aliencompanion.views.viewholders;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.models.Thumbnail;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemOptionsListener;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by George on 1/24/2017.
 */

public class PostGalleryViewHolder extends PostViewHolder {

    public static final int GALLERY_COLUMN_WIDTH = 120;

    //private LinearLayout layout;
    private ImageView postImage;
    private TextView textView;

    private int postLinkResource, postLinkClickedResource;

    public PostGalleryViewHolder(View itemView) {
        super(itemView);
        //layout = (LinearLayout) itemView.findViewById(R.id.gallery_container);
        postImage = (ImageView) itemView.findViewById(R.id.imageView_post_image);
        textView = (TextView) itemView.findViewById(R.id.textView_gallery);
        initIcons();
    }

    @Override
    public void bindModel(Context context, Submission post) {
        Thumbnail thumbnailObject = post.getThumbnailObject() == null ? new Thumbnail() : post.getThumbnailObject();
        if(post.isNSFW() && !MyApplication.showNSFWpreview) {
            postImage.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            SpannableString nsfwSpan = new SpannableString("NSFW");
            nsfwSpan.setSpan(new TextAppearanceSpan(context, R.style.nsfwLabelGallery), 0, 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            textView.setText(nsfwSpan);
        }
        else if(post.isSelf()) {
            postImage.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            textView.setText("SELF");
            textView.setTextColor(post.isStickied() && post.showAsStickied ? MyApplication.textColorStickied : MyApplication.textHintColor);
        }
        else if(thumbnailObject.hasThumbnail()) {
            textView.setVisibility(View.GONE);
            postImage.setVisibility(View.VISIBLE);
            try {
                Picasso.with(context).load(thumbnailObject.getUrl()).placeholder(R.drawable.noimage).into(postImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        postImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }

                    @Override
                    public void onError() {
                        postImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            textView.setVisibility(View.GONE);
            postImage.setVisibility(View.VISIBLE);
            postImage.setImageResource(post.isClicked() ? postLinkClickedResource : postLinkResource);
            postImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }

    @Override
    public void setClickListeners(PostItemListener postItemListener, View.OnLongClickListener postLongListener, PostItemOptionsListener postItemOptionsListener) {
        postImage.setOnClickListener(postItemListener);
        postImage.setOnLongClickListener(postLongListener);
        textView.setOnClickListener(postItemListener);
        textView.setOnLongClickListener(postLongListener);
    }

    @Override
    public void setPostOptionsVisible(boolean flag) {

    }

    private void initIcons() {
        switch (MyApplication.currentBaseTheme) {
            case MyApplication.LIGHT_THEME:
                postLinkResource = R.drawable.ic_link_grey_48dp;
                postLinkClickedResource = R.drawable.ic_link_light_grey_48dp;
                break;
            case MyApplication.DARK_THEME_LOW_CONTRAST:
                postLinkResource = R.drawable.ic_link_light_grey_48dp;
                postLinkClickedResource = R.drawable.ic_link_grey_48dp;
                break;
            default:
                postLinkResource = R.drawable.ic_link_white_48dp;
                postLinkClickedResource = R.drawable.ic_link_light_grey_48dp;
                break;
        }
    }
}
