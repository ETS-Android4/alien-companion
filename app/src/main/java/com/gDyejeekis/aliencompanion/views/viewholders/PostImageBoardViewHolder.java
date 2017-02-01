package com.gDyejeekis.aliencompanion.views.viewholders;

import android.content.Context;
import android.view.View;

import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemOptionsListener;
import com.gDyejeekis.aliencompanion.api.entity.Submission;

/**
 * Created by George on 1/24/2017.
 */

public class PostImageBoardViewHolder extends PostViewHolder {
    public PostImageBoardViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindModel(Context context, Submission submission) {

    }

    @Override
    public void setClickListeners(PostItemListener postItemListener, View.OnLongClickListener postLongListener, PostItemOptionsListener postItemOptionsListener) {

    }

    @Override
    public void setPostOptionsVisible(boolean flag) {

    }
}
