package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.enums.CommentNavSetting;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;

/**
 * Created by George on 3/8/2017.
 */

public class CommentNavDialogFragment extends ScalableDialogFragment implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        dismiss();
        PostFragment postFragment = ((PostActivity) getActivity()).getPostFragment();
        switch (v.getId()) {
            case R.id.layout_threads:
                postFragment.setCommentNavSetting(CommentNavSetting.threads);
                postFragment.commentNavListener.firstTopParentComment();
                break;
            case R.id.layout_time:
                postFragment.setCommentNavSetting(CommentNavSetting.time);
                postFragment.showTimeFilterDialog();
                break;
            case R.id.layout_search_text:
                postFragment.setCommentNavSetting(CommentNavSetting.searchText);
                postFragment.showSearchTextDialog();
                break;
            case R.id.layout_op:
                postFragment.setCommentNavSetting(CommentNavSetting.op);
                postFragment.commentNavListener.firstOpComment();
                break;
            case R.id.layout_ama:
                postFragment.setCommentNavSetting(CommentNavSetting.ama);
                postFragment.commentNavListener.firstAmaComment();
                break;
            case R.id.layout_gilded:
                postFragment.setCommentNavSetting(CommentNavSetting.gilded);
                postFragment.commentNavListener.firstGildedComment();
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_nav_options, container, false);

        LinearLayout threadsNav = (LinearLayout) view.findViewById(R.id.layout_threads);
        LinearLayout timeNav = (LinearLayout) view.findViewById(R.id.layout_time);
        LinearLayout searchNav = (LinearLayout) view.findViewById(R.id.layout_search_text);
        LinearLayout opNav = (LinearLayout) view.findViewById(R.id.layout_op);
        LinearLayout amaNav = (LinearLayout) view.findViewById(R.id.layout_ama);
        LinearLayout gildedNav = (LinearLayout) view.findViewById(R.id.layout_gilded);
        threadsNav.setOnClickListener(this);
        timeNav.setOnClickListener(this);
        searchNav.setOnClickListener(this);
        opNav.setOnClickListener(this);
        amaNav.setOnClickListener(this);
        gildedNav.setOnClickListener(this);

        ImageView threadsImg = (ImageView) view.findViewById(R.id.imageView_threads);
        ImageView timeImg = (ImageView) view.findViewById(R.id.imageView_time);
        ImageView searchImg = (ImageView) view.findViewById(R.id.imageView_search_text);
        ImageView opImg = (ImageView) view.findViewById(R.id.imageView_op);
        ImageView amaImg = (ImageView) view.findViewById(R.id.imageView_ama);
        ImageView gildedImg = (ImageView) view.findViewById(R.id.imageView_gilded);
        threadsImg.setImageResource(CommentNavSetting.threads.getIconResource());
        timeImg.setImageResource(CommentNavSetting.time.getIconResource());
        searchImg.setImageResource(CommentNavSetting.searchText.getIconResource());
        opImg.setImageResource(CommentNavSetting.op.getIconResource());
        amaImg.setImageResource(CommentNavSetting.ama.getIconResource());
        gildedImg.setImageResource(CommentNavSetting.gilded.getIconResource());

        RadioButton threadsRdo = (RadioButton) view.findViewById(R.id.radio_btn_threads);
        RadioButton timeRdo = (RadioButton) view.findViewById(R.id.radio_btn_time);
        RadioButton searchRdo = (RadioButton) view.findViewById(R.id.radio_btn_search_text);
        RadioButton opRdo = (RadioButton) view.findViewById(R.id.radio_btn_op);
        RadioButton amaRdo = (RadioButton) view.findViewById(R.id.radio_btn_ama);
        RadioButton gildedRdo= (RadioButton) view.findViewById(R.id.radio_btn_gilded);
        threadsRdo.setText(CommentNavSetting.threads.value());
        timeRdo.setText(CommentNavSetting.time.value());
        searchRdo.setText(CommentNavSetting.searchText.value());
        opRdo.setText(CommentNavSetting.op.value());
        amaRdo.setText(CommentNavSetting.ama.value());
        gildedRdo.setText(CommentNavSetting.gilded.value());

        CommentNavSetting currentSetting = (CommentNavSetting) getArguments().getSerializable("commentNav");
        threadsRdo.setChecked(currentSetting == CommentNavSetting.threads);
        timeRdo.setChecked(currentSetting == CommentNavSetting.time);
        searchRdo.setChecked(currentSetting == CommentNavSetting.searchText);
        opRdo.setChecked(currentSetting == CommentNavSetting.op);
        amaRdo.setChecked(currentSetting == CommentNavSetting.ama);
        gildedRdo.setChecked(currentSetting == CommentNavSetting.gilded);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        return view;
    }
}
