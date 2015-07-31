package com.george.redditreader.Adapters;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.george.redditreader.Activities.PostActivity;
import com.george.redditreader.Fragments.PostFragment;
import com.george.redditreader.Utils.ConvertUtils;
import com.george.redditreader.R;
import com.george.redditreader.Models.Thumbnail;
import com.george.redditreader.api.entity.Comment;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.multilevelexpindlistview.MultiLevelExpIndListAdapter;
import com.george.redditreader.multilevelexpindlistview.Utils;
import com.squareup.picasso.Picasso;

/**
 * Created by George on 5/17/2015.
 */

public class PostAdapter extends MultiLevelExpIndListAdapter {
    /**
     * View type of an item or group.
     */
    public static final int VIEW_TYPE_ITEM = 0;

    /**
     * View type of the content.
     */
    public static final int VIEW_TYPE_CONTENT = 1;

    /**
     * This is called when the user click on an item or group.
     */
    private final View.OnClickListener mListener;

    private final Activity activity;

    /**
     * Unit of indentation.
     */
    private final int mPaddingDP = 5;

    //private ContentViewHolder contentVH;

    public PostAdapter (Activity activity, View.OnClickListener listener) {
        super();
        this.activity = activity;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder viewHolder;
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                int resource = R.layout.comment_list_item;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                viewHolder = new CommentViewHolder(v);
                break;
            case VIEW_TYPE_CONTENT:
                resource = R.layout.post_details;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                viewHolder = new ContentViewHolder(v);
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }

        v.setOnClickListener(mListener);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                CommentViewHolder cvh = (CommentViewHolder) viewHolder;
                final Comment comment = (Comment) getItemAt(position);

                if(comment.getIdentifier().equals(PostFragment.commentLinkId))
                    cvh.commentLayout.setBackgroundColor(Color.parseColor("#e8ff95"));
                else cvh.commentLayout.setBackgroundColor(Color.WHITE);

                cvh.authorTextView.setText(comment.getAuthor());

                //CharSequence trimmedHtml = ConvertUtils.trim(Html.fromHtml(comment.getBodyHTML()), 0,
                //        comment.getBodyHTML().length()-1);
                cvh.commentTextView.setText(ConvertUtils.noTrailingwhiteLines(Html.fromHtml(comment.getBodyHTML())));
                cvh.commentTextView.setMovementMethod(LinkMovementMethod.getInstance());
                //cvh.commentTextView.setText(comment.getBody());
                //Linkify.addLinks(cvh.commentTextView, Linkify.WEB_URLS);

                if (comment.getIndentation() == 0) {
                    cvh.colorBand.setVisibility(View.GONE);
                    cvh.setPaddingLeft(0);
                } else {
                    cvh.colorBand.setVisibility(View.VISIBLE);
                    cvh.setColorBandColor(comment.getIndentation());
                    int leftPadding = Utils.getPaddingPixels(activity, mPaddingDP) * (comment.getIndentation() - 1);
                    cvh.setPaddingLeft(leftPadding);
                }

                if (comment.isGroup()) {
                    cvh.hiddenCommentsCountTextView.setVisibility(View.VISIBLE);
                    cvh.hiddenCommentsCountTextView.setText(Integer.toString(comment.getGroupSize() + 1));
                    cvh.commentTextView.setVisibility(View.GONE);
                } else {
                    cvh.hiddenCommentsCountTextView.setVisibility(View.GONE);
                    cvh.commentTextView.setVisibility(View.VISIBLE);
                }
                break;
            case VIEW_TYPE_CONTENT:
                ContentViewHolder contentVH = (ContentViewHolder) viewHolder;
                Submission post = (Submission) getItemAt(position);

                if(PostActivity.showFullComments) {
                    contentVH.fullComments.setVisibility(View.VISIBLE);
                    contentVH.fullComments.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PostActivity.showFullComments = false;
                            PostFragment postFragment = (PostFragment) activity.getFragmentManager()
                                    .findFragmentById(R.id.fragment2);
                            postFragment.loadFullComments();
                        }
                    });
                }
                else contentVH.fullComments.setVisibility(View.GONE);

                contentVH.postTitle.setText(post.getTitle());
                contentVH.comments.setText(Long.toString(post.getCommentCount()) + " comments");

                if (post.isSelf()) {
                    contentVH.postDets1.setVisibility(View.GONE);

                    if(post.getSelftextHTML() == null) contentVH.selfText.setVisibility(View.GONE);
                    else {
                        //contentVH.selfText.setVisibility(View.VISIBLE);
                        contentVH.selfText.setText(ConvertUtils.noTrailingwhiteLines(Html.fromHtml(post.getSelftextHTML())));
                        contentVH.selfText.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                    //contentVH.selfText.setText(post.getSelftext());
                    //Linkify.addLinks(contentVH.selfText, Linkify.WEB_URLS);
                    contentVH.selfText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f));
                    contentVH.postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                } else {
                    contentVH.postDets1.setText(post.getDomain() + " - ");
                    contentVH.selfText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0f));
                    contentVH.postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                    Thumbnail thumbnail = post.getThumbnailObject();
                    if (thumbnail.hasThumbnail()) {
                        try {
                            //Get Post Thumbnail
                            Picasso.with(activity).load(thumbnail.getUrl()).placeholder(R.drawable.noimage).into(contentVH.postImage);
                        } catch (IllegalArgumentException e) {
                        }
                    } else {
                        contentVH.postImage.setVisibility(View.GONE);
                    }
                }
                contentVH.author.setText(post.getAuthor());
                if (post.getScore() > 0) {
                    contentVH.postDets2.setText("+ " + post.getScore() + " - " + ConvertUtils.getSubmissionAge(post.getCreatedUTC()));
                } else {
                    contentVH.postDets2.setText(post.getScore() + " - " + ConvertUtils.getSubmissionAge(post.getCreatedUTC()));
                }
                contentVH.subreddit.setText(post.getSubreddit());
                if (PostActivity.commentsLoaded) contentVH.progressBar.setVisibility(View.GONE);
                else contentVH.progressBar.setVisibility(View.VISIBLE);
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_TYPE_CONTENT;
        return VIEW_TYPE_ITEM;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        private View colorBand;
        public TextView authorTextView;
        public TextView commentTextView;
        public TextView hiddenCommentsCountTextView;
        private View view;
        public LinearLayout commentLayout;

        private static final String[] indColors = {"#000000", "#3366FF", "#E65CE6",
                "#E68A5C", "#00E68A", "#CCCC33"};

        public CommentViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            authorTextView = (TextView) itemView.findViewById(R.id.author_textview);
            commentTextView = (TextView) itemView.findViewById(R.id.comment_textview);
            colorBand = itemView.findViewById(R.id.color_band);
            hiddenCommentsCountTextView = (TextView) itemView.findViewById(R.id.hidden_comments_count_textview);
            commentLayout = (LinearLayout) itemView.findViewById(R.id.commentLayout);
        }

        public void setColorBandColor(int indentation) {
            int color = Color.parseColor(indColors[indentation]);
            colorBand.setBackgroundColor(color);
        }

        public void setPaddingLeft(int paddingLeft) {
            view.setPadding(paddingLeft,0,0,0);
        }
    }

    public static class ContentViewHolder extends RecyclerView.ViewHolder {
        public TextView postTitle;
        public TextView postDets1;
        public TextView comments;
        public TextView author;
        public TextView postDets2;
        public TextView subreddit;
        public TextView selfText;
        public ImageView postImage;
        public ProgressBar progressBar;
        public LinearLayout fullComments;

        public ContentViewHolder(View itemView) {
            super(itemView);
            postTitle = (TextView) itemView.findViewById(R.id.postTitle);
            postDets1 = (TextView) itemView.findViewById(R.id.postDets1);
            author = (TextView) itemView.findViewById(R.id.author);
            postDets2 = (TextView) itemView.findViewById(R.id.postDets2);
            subreddit = (TextView) itemView.findViewById(R.id.subreddit);
            selfText = (TextView) itemView.findViewById(R.id.selfText);
            postImage = (ImageView) itemView.findViewById(R.id.postImage);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar3);
            comments = (TextView) itemView.findViewById(R.id.txtView_comments);
            fullComments = (LinearLayout) itemView.findViewById(R.id.fullLoad);
        }
    }

}
