package com.gDyejeekis.aliencompanion.Views.viewholders;

import android.content.Context;
import android.view.View;

import com.gDyejeekis.aliencompanion.ClickListeners.PostItemListener;
import com.gDyejeekis.aliencompanion.ClickListeners.PostItemOptionsListener;
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
