package com.george.redditreader.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    static class ViewHolder {
        TextView title;
        TextView score;
        TextView age;
        TextView author;
        TextView dets;
        TextView comments;
        ImageView image;
        LinearLayout commentsButton;
        LinearLayout linkButton;
    }

    public PostListAdapter(Activity activity, List<Submission> posts) {
        super(activity, R.layout.post_list_item, posts);
        inflater = activity.getWindow().getLayoutInflater();
        this.posts = posts;
        this.activity = activity;
    }

    //public void setThumbnails(List<Thumbnail> thumbnails) {
    //    this.thumbnails = thumbnails;
    //}
//
    //public void addThumbnails(List<Thumbnail> moreThumbnails) {
    //    thumbnails.addAll(moreThumbnails);
    //}

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

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
            holder.commentsButton = (LinearLayout) row.findViewById(R.id.commentsButton);
            holder.linkButton = (LinearLayout) row.findViewById(R.id.linkButton);

            row.setTag(holder);
        }
        else {
            holder = (ViewHolder) row.getTag();
        }
        holder.title.setText(posts.get(position).getTitle());
        holder.score.setText(Long.toString(posts.get(position).getScore()));
        holder.age.setText(" - " + ConvertUtils.getSubmissionAge(posts.get(position).getCreatedUTC()));
        holder.author.setText(" - " + posts.get(position).getAuthor());
        holder.comments.setText(Long.toString(posts.get(position).getCommentCount()));

        String postSubreddit = posts.get(position).getSubreddit();
        String postDomain = posts.get(position).getDomain();

        if(posts.get(position).isSelf()) {
            holder.dets.setText(postDomain);
        }
        else {
            holder.dets.setText(postSubreddit + " - " + postDomain);
        }
        //Set the thumbnail
        Thumbnail postThumbnail = posts.get(position).getThumbnailObject();
        if (postThumbnail.hasThumbnail()){
            holder.image.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
            if (postThumbnail.isSelf()) {
                holder.image.setImageResource(R.drawable.self_default2);
            } else if (postThumbnail.isNSFW()) {
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
        }

        PostItemListener listener = new PostItemListener(activity, posts.get(position));
        //holder.commentsButton.setTag(position);
        holder.commentsButton.setOnClickListener(listener);
        //holder.linkButton.setTag(position);
        holder.linkButton.setOnClickListener(listener);

        return row;
    }

    //public static String getSubmissionAge(Double createdUTC) {
    //    Date createdDate = new Date((long) (createdUTC*1000));
    //    Date currentDate = new Date();
    //    long createdTime = createdDate.getTime();
    //    long currentTime = currentDate.getTime();
    //    long diffTime = currentTime - createdTime;
    //    long diffHours = diffTime / (1000 * 60 * 60);
    //    if(diffHours < 25 && diffHours >= 1) return Long.toString(diffHours) + " h";
    //    else if(diffHours < 1) {
    //        long diffMins = diffTime / (1000 * 60);
    //        return Long.toString(diffMins) + " m";
    //    }
    //    else {
    //        long diffDays = diffTime / (1000 * 60 * 60 * 24);
    //        return Long.toString(diffDays) + " d";
    //    }
    //}

    //@Override
    //public void onClick(View v) {
    //    Submission post = posts.get((Integer) v.getTag());
//
    //    if(v.getId() == R.id.commentsButton || post.isSelf()) {
    //        Intent intent = new Intent(activity, PostActivity.class);
    //        intent.putExtra("post", post);
    //        if(!post.isSelf() && thumbnails.get((Integer) v.getTag()).hasThumbnail()) {
    //            String thumbUrl = thumbnails.get((Integer) v.getTag()).getUrl();
    //            intent.putExtra("thumbUrl", thumbUrl);
    //        }
    //        Log.d("Clicks", "Post number " + v.getTag() + " ,taking you to post page...");
    //        activity.startActivity(intent);
    //    }
    //    else {
    //        Log.d("Clicks", "Post number " + v.getTag() + " ,loading post content...");
    //        LinkHandler linkHandler = new LinkHandler(activity, post);
    //        linkHandler.handleIt();
    //    }
    //}

    public Submission getLastPost() {
        return posts.get(posts.size()-1);
    }

}
