package com.gDyejeekis.aliencompanion.views.viewholders;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.ImageView;
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

    //private final int textColor = MyApplication.textHintColor;
    //private final int clickedTextColor = ConvertUtils.adjustAlpha(textColor, 0.75f);
    //private float defaultIconOpacity, defaultIconOpacityClicked;

    public PostGalleryViewHolder(View itemView) {
        super(itemView);
        //layout = (LinearLayout) itemView.findViewById(R.id.gallery_container);
        postImage = (ImageView) itemView.findViewById(R.id.imageView_post_image);
        textView = (TextView) itemView.findViewById(R.id.textView_gallery);
        //initIcons();
    }

    //private void initIcons() {
    //    initIconResources(PostViewType.gallery);
    //    switch (MyApplication.currentBaseTheme) {
    //        case MyApplication.LIGHT_THEME:
    //            defaultIconOpacity = 0.54f;
    //            defaultIconOpacityClicked = 0.38f;
    //            break;
    //        case MyApplication.DARK_THEME_LOW_CONTRAST:
    //            defaultIconOpacity = 0.6f;
    //            defaultIconOpacityClicked = 0.3f;
    //            break;
    //        default:
    //            defaultIconOpacity = 1f;
    //            defaultIconOpacityClicked = 0.5f;
    //            break;
    //    }
    //}

    @Override
    public void bindModel(Context context, Submission post) {
        Thumbnail thumbnailObject = post.getThumbnailObject() == null ? new Thumbnail() : post.getThumbnailObject();
        // nsfw post (nsfw thumbnails disabled)
        if(post.isNSFW() && !MyApplication.showNsfwPreviews) {
            postImage.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            String text = "NSFW";
            if(post.isSpoiler()) {
                text += "\nSPOILER";
            }
            SpannableString nsfwSpan = new SpannableString(text);
            int style = post.isClicked() ? R.style.nsfwLabelGalleryClicked : R.style.nsfwLabelGallery;
            nsfwSpan.setSpan(new TextAppearanceSpan(context, style), 0, 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            textView.setText(nsfwSpan);
        }
        // has thumbnail
        else if(thumbnailObject.hasThumbnail() && !post.isSpoiler()) {
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
        // no thumbnail
        else {
            postImage.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            String text = post.isSelf() ? "SELF" : "LINK";
            if(post.isSpoiler()) {
                text += "\nSPOILER";
            }
            textView.setText(text);
            if(post.isClicked()) {
                textView.setTextColor(post.isStickied() && post.showAsStickied ? MyApplication.textColorStickiedClicked : MyApplication.textSecondaryColor);
            }
            else {
                textView.setTextColor(post.isStickied() && post.showAsStickied ? MyApplication.textColorStickied : MyApplication.textPrimaryColor);
            }
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

}
