package com.george.redditreader.Views.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.george.redditreader.R;

/**
 * Created by sound on 8/27/2015.
 */
public class PostItemListViewholderTemp { //TODO: to be deleted

    public TextView title;
    public TextView score;
    public TextView age;
    public TextView author;
    public TextView dets;
    public TextView comments;
    public ImageView image;
    public LinearLayout commentsButton;
    public LinearLayout linkButton;
    public LinearLayout layoutPostOptions;
    public ImageView upvote;
    public ImageView downvote;
    public ImageView save;
    public ImageView hide;
    public ImageView viewUser;
    public ImageView openBrowser;
    public ImageView moreOptions;

    public PostItemListViewholderTemp(View itemView) {
        title = (TextView) itemView.findViewById(R.id.postTitle);
        score = (TextView) itemView.findViewById(R.id.score);
        age = (TextView) itemView.findViewById(R.id.age);
        author = (TextView) itemView.findViewById(R.id.author);
        dets = (TextView) itemView.findViewById(R.id.postDets2);
        comments = (TextView) itemView.findViewById(R.id.numberOfComments);
        image = (ImageView) itemView.findViewById(R.id.postImage);
        commentsButton = (LinearLayout) itemView.findViewById(R.id.commentsButton);
        linkButton = (LinearLayout) itemView.findViewById(R.id.linkButton);
        upvote =  (ImageView) itemView.findViewById(R.id.btn_upvote);
        layoutPostOptions = (LinearLayout) itemView.findViewById(R.id.layout_post_options);
        downvote =  (ImageView) itemView.findViewById(R.id.btn_downvote);
        save =  (ImageView) itemView.findViewById(R.id.btn_save);
        hide =  (ImageView) itemView.findViewById(R.id.btn_hide);
        viewUser = (ImageView) itemView.findViewById(R.id.btn_view_user);
        openBrowser = (ImageView) itemView.findViewById(R.id.btn_open_browser);
        moreOptions =  (ImageView) itemView.findViewById(R.id.btn_more);
    }
}
