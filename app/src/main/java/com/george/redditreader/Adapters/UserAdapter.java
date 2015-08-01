package com.george.redditreader.Adapters;

import android.app.Activity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.george.redditreader.ClickListeners.CommentLinkListener;
import com.george.redditreader.Utils.ConvertUtils;
import com.george.redditreader.ClickListeners.PostItemListener;
import com.george.redditreader.R;
import com.george.redditreader.Models.Thumbnail;
import com.george.redditreader.api.entity.Comment;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.api.entity.Trophy;
import com.george.redditreader.api.entity.UserInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by George on 6/13/2015.
 */
public class UserAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_INFO = 0;
    private static final int VIEW_TYPE_POST = 1;
    private static final int VIEW_TYPE_COMMENT = 2;

    private List<Object> mData;
    private LayoutInflater mInflater;
    private Activity mActivity;

    public UserAdapter(Activity activity) {
        mActivity = activity;
        mInflater = activity.getWindow().getLayoutInflater();
        mData = new ArrayList<>();
    }

    public void addAll(List<Object> data) {
        mData.addAll(data);
    }

    public void add(Object object) {
        mData.add(object);
    }

    public Object getLastObject() {
        return mData.get(mData.size()-1);
    }

    @Override
    public int getItemViewType(int position) {
        Object object = mData.get(position);
        if(object instanceof UserInfo) return VIEW_TYPE_INFO;
        else if(object instanceof Submission) return VIEW_TYPE_POST;
        else return VIEW_TYPE_COMMENT;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        int type = getItemViewType(position);
       switch (type) {
           case VIEW_TYPE_INFO:
               InfoViewHolder infoViewHolder;
               UserInfo userInfo = (UserInfo) getItem(position);
               if(row == null) {
                   row = mInflater.inflate(R.layout.user_info, parent, false);
                   infoViewHolder = new InfoViewHolder(row);
                   row.setTag(infoViewHolder);
                   setupTrophiesView(infoViewHolder.trophiesLayout, userInfo.getTrophyList());
               }
               else infoViewHolder = (InfoViewHolder) row.getTag();

               infoViewHolder.linkKarma.setText(Long.toString(userInfo.getLinkKarma()));
               infoViewHolder.commentKarma.setText(Long.toString(userInfo.getCommentKarma()));
               break;
           case VIEW_TYPE_COMMENT:
               CommentViewHolder commentViewHolder;
               if(row == null) {
                   row = mInflater.inflate(R.layout.user_comment, parent, false);
                   commentViewHolder = new CommentViewHolder(row);
                   row.setTag(commentViewHolder);
               }
               else commentViewHolder = (CommentViewHolder) row.getTag();

               Comment comment = (Comment) getItem(position);
               commentViewHolder.postTitle.setText(comment.getLinkTitle());
               commentViewHolder.commentDets.setText(comment.getAuthor() + " - " + comment.getScore() + " points - " +
                       ConvertUtils.getSubmissionAge(comment.getCreatedUTC()));
               commentViewHolder.commentBody.setText(ConvertUtils.noTrailingwhiteLines
                       (Html.fromHtml(comment.getBodyHTML())));
               commentViewHolder.commentLayout.setOnClickListener(new CommentLinkListener(mActivity, comment));
               break;
           case VIEW_TYPE_POST:
               PostViewHolder postViewHolder;
               if(row == null) {
                   row = mInflater.inflate(R.layout.post_list_item, parent, false);
                   postViewHolder = new PostViewHolder(row);
                   row.setTag(postViewHolder);
               }
               else postViewHolder = (PostViewHolder) row.getTag();

               Submission submission = (Submission) mData.get(position);
               setupPostView(postViewHolder, submission, position);
               break;
       }
        return row;
    }

    private void setupTrophiesView(TableLayout trophiesLayout, List<Trophy> trophiesList) {

        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        //int height=dm.heightPixels;
        int dens=dm.densityDpi;
        double wi=(double)width/(double)dens;
        //double hi=(double)height/(double)dens;
        //double x = Math.pow(wi,2);
        //double y = Math.pow(hi,2);
        //double screenInches = Math.sqrt(x+y);

        //Log.d("screen width inches: ", Double.toString(wi));
        //Log.d("screen width pixels: ", Integer.toString(width));
//
        //Log.d("screen density in dpi: ", Integer.toString(dens));

        int margin = (int) Math.round(dens / 26.62);
        TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, margin);

        int size = (int) Math.round(dens / 3.2);
        TableRow.LayoutParams imageParams = new TableRow.LayoutParams(size, size);
        imageParams.setMargins(margin, 0, 0, 0);

        int count = 0;
        int MAX_TROPHY_COLUMNS = (int) Math.round(2.7 * wi);
        int mRows = trophiesList.size() / MAX_TROPHY_COLUMNS;
        if(trophiesList.size() % MAX_TROPHY_COLUMNS != 0)
            mRows++;
        for(int i=0; i<mRows; i++) {
            TableRow tr = new TableRow(mActivity);

            for(int j=0; j< MAX_TROPHY_COLUMNS; j++) {
                if((count) == trophiesList.size()) break;
                ImageView iv = new ImageView(mActivity);
                Picasso.with(mActivity).load(trophiesList.get(count).getIcon70url()).into(iv);
                iv.setLayoutParams(imageParams);
                tr.addView(iv);
                count++;
            }
            tr.setLayoutParams(rowParams);
            trophiesLayout.addView(tr);
        }
    }

    private void setupPostView(PostViewHolder holder, Submission submission, int position) {
        holder.title.setText(submission.getTitle());
        holder.score.setText(Long.toString(submission.getScore()));
        holder.age.setText(" - " + ConvertUtils.getSubmissionAge(submission.getCreatedUTC()));
        holder.author.setText(" - " + submission.getAuthor());
        holder.comments.setText(Long.toString(submission.getCommentCount()));

        String postSubreddit = submission.getSubreddit();
        String postDomain = submission.getDomain();

        if(submission.isSelf()) {
            holder.dets.setText(postDomain);
        }
        else {
            holder.dets.setText(postSubreddit + " - " + postDomain);
        }
        //Set the thumbnail
        Thumbnail postThumbnail = submission.getThumbnailObject();
        if (postThumbnail.hasThumbnail()){
            holder.image.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
            if (postThumbnail.isSelf()) {
                holder.image.setImageResource(R.drawable.self_default2);
            } else if (postThumbnail.isNSFW()) {
                holder.image.setImageResource(R.drawable.nsfw2);
            } else {
                try {
                    //Get Post Thumbnail
                    Picasso.with(mActivity).load(postThumbnail.getUrl()).placeholder(R.drawable.noimage).into(holder.image);
                } catch (IllegalArgumentException e) {}
            }
        }
        else {
            holder.image.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
        }

        PostItemListener listener = new PostItemListener(mActivity, submission);
        //holder.commentsButton.setTag(position);
        holder.commentsButton.setOnClickListener(listener);
        //holder.linkButton.setTag(position);
        holder.linkButton.setOnClickListener(listener);
    }

    public static class InfoViewHolder {
        public TextView linkKarma;
        public TextView commentKarma;
        public TableLayout trophiesLayout;

        public InfoViewHolder(View itemView) {
            linkKarma = (TextView) itemView.findViewById(R.id.txtView_linkKarma);
            commentKarma = (TextView) itemView.findViewById(R.id.txtView_commentKarma);
            trophiesLayout = (TableLayout) itemView.findViewById(R.id.layout_trophies);
        }
    }

    public static class PostViewHolder {
        TextView title;
        TextView score;
        TextView age;
        TextView author;
        TextView dets;
        TextView comments;
        ImageView image;
        //Drawable noImage;
        //Drawable nsfw;
        //Drawable self;
        LinearLayout commentsButton;
        LinearLayout linkButton;

        public PostViewHolder(View itemView) {
            title = (TextView) itemView.findViewById(R.id.postTitle);
            score = (TextView) itemView.findViewById(R.id.score);
            age = (TextView) itemView.findViewById(R.id.age);
            author = (TextView) itemView.findViewById(R.id.author);
            dets = (TextView) itemView.findViewById(R.id.postDets2);
            comments = (TextView) itemView.findViewById(R.id.numberOfComments);
            image = (ImageView) itemView.findViewById(R.id.postImage);
            commentsButton = (LinearLayout) itemView.findViewById(R.id.commentsButton);
            linkButton = (LinearLayout) itemView.findViewById(R.id.linkButton);
        }
    }

    public static class CommentViewHolder {
        public TextView postTitle;
        public TextView commentDets;
        public TextView commentBody;
        public LinearLayout commentLayout;

        public CommentViewHolder(View itemView) {
            postTitle = (TextView) itemView.findViewById(R.id.txtView_postTitle);
            commentDets = (TextView) itemView.findViewById(R.id.txtView_commentDets);
            commentBody = (TextView) itemView.findViewById(R.id.txtView_commentBody);
            commentLayout = (LinearLayout) itemView.findViewById(R.id.layout_comment);
        }
    }
}
