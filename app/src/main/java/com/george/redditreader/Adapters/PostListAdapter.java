package com.george.redditreader.Adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.ClickListeners.PostItemOptionsListener;
import com.george.redditreader.Utils.ConvertUtils;
import com.george.redditreader.ClickListeners.PostItemListener;
import com.george.redditreader.R;
import com.george.redditreader.Models.Thumbnail;
import com.george.redditreader.api.entity.Submission;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by George on 5/9/2015.
 */
public class PostListAdapter extends ArrayAdapter<Submission> {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Submission> posts;

    private boolean showNSFW;

    public int selectedPosition = -1;

    static class ViewHolder {
        TextView title;
        TextView score;
        TextView age;
        TextView author;
        TextView dets;
        TextView comments;
        ImageView image;
        LinearLayout layoutRoot;
        LinearLayout commentsButton;
        LinearLayout linkButton;
        LinearLayout layoutPostOptions;
        ImageView upvote;
        ImageView downvote;
        ImageView save;
        ImageView hide;
        ImageView viewUser;
        ImageView openBrowser;
        ImageView moreOptions;
    }

    public PostListAdapter(Activity activity, List<Submission> posts) {
        super(activity, R.layout.post_list_item, posts);
        inflater = activity.getWindow().getLayoutInflater();
        this.posts = posts;
        this.activity = activity;
        showNSFW = MainActivity.prefs.getBoolean("showNSFWthumb", false);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final Submission post = posts.get(position);

        ViewHolder holder = null;
        if(row == null) {
            row = inflater.inflate(R.layout.post_list_item, parent, false);
            holder = new ViewHolder();

            holder.title = (TextView) row.findViewById(R.id.postTitle);
            holder.score = (TextView) row.findViewById(R.id.score);
            holder.age = (TextView) row.findViewById(R.id.age);
            holder.author = (TextView) row.findViewById(R.id.author);
            holder.dets = (TextView) row.findViewById(R.id.postDets2);
            holder.comments = (TextView) row.findViewById(R.id.numberOfComments);
            holder.image = (ImageView) row.findViewById(R.id.postImage);
            holder.layoutRoot = (LinearLayout) row.findViewById(R.id.layout_root);
            holder.commentsButton = (LinearLayout) row.findViewById(R.id.commentsButton);
            holder.linkButton = (LinearLayout) row.findViewById(R.id.linkButton);
            holder.layoutPostOptions = (LinearLayout) row.findViewById(R.id.layout_post_options);
            holder.upvote =  (ImageView) row.findViewById(R.id.btn_upvote);
            holder.downvote =  (ImageView) row.findViewById(R.id.btn_downvote);
            holder.save =  (ImageView) row.findViewById(R.id.btn_save);
            holder.hide =  (ImageView) row.findViewById(R.id.btn_hide);
            holder.viewUser = (ImageView) row.findViewById(R.id.btn_view_user);
            holder.openBrowser = (ImageView) row.findViewById(R.id.btn_open_browser);
            holder.moreOptions =  (ImageView) row.findViewById(R.id.btn_more);

            row.setTag(holder);
        }
        else {
            holder = (ViewHolder) row.getTag();
        }
        holder.title.setText(post.getTitle());
        holder.score.setText(Long.toString(post.getScore()));
        holder.age.setText(" - " + ConvertUtils.getSubmissionAge(post.getCreatedUTC()));
        holder.author.setText(" - " + post.getAuthor());
        holder.comments.setText(Long.toString(post.getCommentCount()));

        String postSubreddit = post.getSubreddit();
        String postDomain = post.getDomain();

        if(post.isSelf()) {
            holder.dets.setText(postDomain);
        }
        else {
            holder.dets.setText(postSubreddit + " - " + postDomain);
        }
        //Set the thumbnail
        Thumbnail postThumbnail = post.getThumbnailObject();
        if (postThumbnail.hasThumbnail()){
            holder.image.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
            //holder.image.setVisibility(View.VISIBLE);
            if (postThumbnail.isSelf()) {
                holder.image.setImageResource(R.drawable.self_default2);
            } else if (post.isNSFW() && !showNSFW) {
                holder.image.setImageResource(R.drawable.nsfw2);
            } else {
                try {
                    //Get Post Thumbnail
                    Picasso.with(activity).load(postThumbnail.getUrl()).placeholder(R.drawable.noimage).into(holder.image);
                } catch (IllegalArgumentException e) {}
            }
        }
        else {
            holder.image.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
            //holder.image.setVisibility(View.GONE);
        }

        //item selected from posts list
        if(selectedPosition == position) {
            holder.layoutPostOptions.setVisibility(View.VISIBLE);
            PostItemOptionsListener optionsListener = new PostItemOptionsListener(activity, post, this);
            holder.upvote.setOnClickListener(optionsListener);
            holder.downvote.setOnClickListener(optionsListener);
            holder.save.setOnClickListener(optionsListener);
            holder.hide.setOnClickListener(optionsListener);
            holder.viewUser.setOnClickListener(optionsListener);
            holder.openBrowser.setOnClickListener(optionsListener);
            holder.moreOptions.setOnClickListener(optionsListener);
        }
        else {
            holder.layoutPostOptions.setVisibility(View.GONE);
        }

        //user logged in
        if(MainActivity.currentUser != null) {
            //check user vote
            if (post.getLikes().equals("true")) {
                holder.score.setTextColor(Color.parseColor("#FF6600"));
                holder.upvote.setImageResource(R.mipmap.ic_action_upvote_orange);
                holder.downvote.setImageResource(R.mipmap.ic_action_downvote);
            } else if (post.getLikes().equals("false")) {
                holder.score.setTextColor(Color.BLUE);
                holder.upvote.setImageResource(R.mipmap.ic_action_upvote);
                holder.downvote.setImageResource(R.mipmap.ic_action_downvote_blue);
            } else {
                holder.score.setTextColor(Color.BLACK);
                holder.upvote.setImageResource(R.mipmap.ic_action_upvote);
                holder.downvote.setImageResource(R.mipmap.ic_action_downvote);
            }
            //check saved post
            if(post.isSaved()) holder.save.setImageResource(R.mipmap.ic_action_save_yellow);
            else holder.save.setImageResource(R.mipmap.ic_action_save);
            //check hidden post
            if(post.isHidden()) holder.hide.setImageResource(R.mipmap.ic_action_hide_red);
            else holder.hide.setImageResource(R.mipmap.ic_action_hide);
        }

        PostItemListener listener = new PostItemListener(activity, post);
        holder.commentsButton.setOnClickListener(listener);
        holder.linkButton.setOnClickListener(listener);
        View.OnLongClickListener longListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (position == selectedPosition) selectedPosition = -1;
                else selectedPosition = position;
                notifyDataSetChanged();
                return false;
            }
        };
        holder.commentsButton.setOnLongClickListener(longListener);
        holder.linkButton.setOnLongClickListener(longListener);

        return row;
    }

    public Submission getLastPost() {
        return posts.get(posts.size()-1);
    }

}
