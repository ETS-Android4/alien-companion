package com.dyejeekis.aliencompanion.Adapters;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.ClickListeners.CommentItemOptionsListener;
import com.dyejeekis.aliencompanion.ClickListeners.PostItemOptionsListener;
import com.dyejeekis.aliencompanion.Fragments.PostFragment;
import com.dyejeekis.aliencompanion.Fragments.UrlOptionsDialogFragment;
import com.dyejeekis.aliencompanion.LinkHandler;
import com.dyejeekis.aliencompanion.MyClickableSpan;
import com.dyejeekis.aliencompanion.MyHtmlTagHandler;
import com.dyejeekis.aliencompanion.MyLinkMovementMethod;
import com.dyejeekis.aliencompanion.Utils.ConvertUtils;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.Models.Thumbnail;
import com.dyejeekis.aliencompanion.api.entity.Comment;
import com.dyejeekis.aliencompanion.api.entity.Submission;
import com.dyejeekis.aliencompanion.multilevelexpindlistview.MultiLevelExpIndListAdapter;
import com.dyejeekis.aliencompanion.multilevelexpindlistview.Utils;
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
    private final View.OnLongClickListener mLongListener;

    private final Activity activity;

    /**
     * Unit of indentation.
     */
    private final int mPaddingDP = 5;

    private String author;
    public int selectedPosition;

    public PostAdapter (Activity activity, View.OnClickListener listener, View.OnLongClickListener longListener) {
        super();
        this.activity = activity;
        mListener = listener;
        mLongListener = longListener;
        selectedPosition = -1;
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
                v.setOnClickListener(mListener);
                v.setOnLongClickListener(mLongListener);
                break;
            case VIEW_TYPE_CONTENT:
                resource = R.layout.post_details_old;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                viewHolder = new ContentViewHolder(v);
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }

        return viewHolder;
    }

    private SpannableStringBuilder modifyURLSpan(CharSequence sequence) {
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);

        // Get an array of URLSpan from SpannableStringBuilder object
        URLSpan[] urlSpans = strBuilder.getSpans(0, strBuilder.length(), URLSpan.class);

        // Add onClick listener for each of URLSpan object
        for (final URLSpan span : urlSpans) {
            int start = strBuilder.getSpanStart(span);
            int end = strBuilder.getSpanEnd(span);
            // The original URLSpan needs to be removed to block the behavior of browser opening
            strBuilder.removeSpan(span);

            strBuilder.setSpan(new MyClickableSpan()
            {
                @Override
                public void onClick(View widget) {
                    LinkHandler linkHandler = new LinkHandler(activity, span.getURL());
                    linkHandler.handleIt();
                }
//
                @Override
                public boolean onLongClick(View v) {
                    Bundle args = new Bundle();
                    args.putString("url", span.getURL());
                    UrlOptionsDialogFragment dialogFragment = new UrlOptionsDialogFragment();
                    dialogFragment.setArguments(args);
                    dialogFragment.show(activity.getFragmentManager(), "dialog");
                    return true;
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return strBuilder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

        final PostFragment postFragment = (PostFragment) activity.getFragmentManager()
                .findFragmentByTag("postFragment");
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                CommentViewHolder cvh = (CommentViewHolder) viewHolder;
                final Comment comment = (Comment) getItemAt(position);

                //View.OnClickListener listener = new View.OnClickListener() {
                //    @Override
                //    public void onClick(View v) {
                //        selectedPosition = -1;
                //        toggleGroup(position);
                //    }
                //};
                //View.OnLongClickListener longListener = new View.OnLongClickListener() {
                //    @Override
                //    public boolean onLongClick(View v) {
                //        if(!comment.isGroup()) {
                //            int previousPosition = selectedPosition;
                //            if (position == selectedPosition) selectedPosition = -1;
                //            else selectedPosition = position;
                //            //notifyDataSetChanged();
                //            notifyItemChanged(previousPosition);
                //            notifyItemChanged(selectedPosition);
                //            return true;
                //        }
                //        else return false;
                //    }
                //};
//
                //cvh.commentLayout.setOnClickListener(listener);
                //cvh.commentLayout.setOnLongClickListener(longListener);

                cvh.score.setText(Long.toString(comment.getScore()));
                String ageString = " pts · " + ConvertUtils.getSubmissionAge(comment.getCreatedUTC());
                if(comment.getEdited()) ageString += "*";
                cvh.age.setText(ageString);

                //Comment permalink case
                if(comment.getIdentifier().equals(postFragment.commentLinkId))
                    cvh.commentLayout.setBackgroundColor(MainActivity.commentPermaLinkBackgroundColor);
                else cvh.commentLayout.setBackgroundColor(MainActivity.backgroundColor);

                //Author textview
                if(author.equals(comment.getAuthor())) {
                    cvh.authorTextView.setTextColor(Color.WHITE);
                    cvh.authorTextView.setBackgroundResource(R.drawable.rounded_corner_blue);
                }
                else {
                    cvh.authorTextView.setTextColor(Color.parseColor("#5972ff"));
                    cvh.authorTextView.setBackgroundColor(Color.TRANSPARENT);
                }
                cvh.authorTextView.setText(comment.getAuthor());

                //Comment textview
                SpannableStringBuilder strBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(
                        Html.fromHtml(comment.getBodyHTML(), null, new MyHtmlTagHandler()));
                strBuilder = modifyURLSpan(strBuilder);
                cvh.commentTextView.setText(strBuilder);
                cvh.commentTextView.setMovementMethod(MyLinkMovementMethod.getInstance());

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
                    cvh.commentHidden.setVisibility(View.VISIBLE);
                    int hiddenComments = comment.getGroupSize();
                    if(hiddenComments+1 == 1) cvh.hiddenCommentsCountTextView.setVisibility(View.GONE);
                    else {
                        cvh.hiddenCommentsCountTextView.setVisibility(View.VISIBLE);
                        cvh.hiddenCommentsCountTextView.setText("+" + Integer.toString(hiddenComments));
                    }
                    cvh.commentTextView.setVisibility(View.GONE);
                } else {
                    cvh.commentHidden.setVisibility(View.GONE);
                    cvh.hiddenCommentsCountTextView.setVisibility(View.GONE);
                    cvh.commentTextView.setVisibility(View.VISIBLE);
                }

                if(selectedPosition == position) {
                    cvh.commentOptionsLayout.setVisibility(View.VISIBLE);
                    CommentItemOptionsListener listener = new CommentItemOptionsListener(activity, comment, this);
                    cvh.upvote.setOnClickListener(listener);
                    cvh.downvote.setOnClickListener(listener);
                    cvh.reply.setOnClickListener(listener);
                    cvh.viewUser.setOnClickListener(listener);
                    cvh.more.setOnClickListener(listener);
                }
                else {
                    cvh.commentOptionsLayout.setVisibility(View.GONE);
                }

                cvh.commentOptionsLayout.setBackgroundColor(MainActivity.currentColor);

                //user logged in
                if(MainActivity.currentUser != null) {
                    //check user vote
                    if (comment.getLikes().equals("true")) {
                        cvh.score.setTextColor(Color.parseColor("#FF6600"));
                        cvh.upvote.setImageResource(R.mipmap.ic_action_upvote_orange);
                        cvh.downvote.setImageResource(R.mipmap.ic_action_downvote);
                    } else if (comment.getLikes().equals("false")) {
                        cvh.score.setTextColor(Color.BLUE);
                        cvh.upvote.setImageResource(R.mipmap.ic_action_upvote);
                        cvh.downvote.setImageResource(R.mipmap.ic_action_downvote_blue);
                    } else {
                        cvh.score.setTextColor(MainActivity.textColor);
                        cvh.upvote.setImageResource(R.mipmap.ic_action_upvote);
                        cvh.downvote.setImageResource(R.mipmap.ic_action_downvote);
                    }
                }

                break;
            case VIEW_TYPE_CONTENT:
                View.OnLongClickListener longListener = new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int previousPosition = selectedPosition;
                        if (position == selectedPosition) selectedPosition = -1;
                        else selectedPosition = position;
                        notifyItemChanged(previousPosition);
                        notifyItemChanged(position);
                        //notifyDataSetChanged();
                        return true;
                    }
                };
                final ContentViewHolder contentVH = (ContentViewHolder) viewHolder;
                final Submission post = (Submission) getItemAt(position);
                author = post.getAuthor();

                if(postFragment.showFullCommentsButton) {
                    contentVH.fullComments.setVisibility(View.VISIBLE);
                    contentVH.fullComments.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            postFragment.showFullCommentsButton = false;
                            postFragment.loadFullComments();
                        }
                    });
                }
                else contentVH.fullComments.setVisibility(View.GONE);

                contentVH.postTitle.setText(post.getTitle());
                contentVH.comments.setText(Long.toString(post.getCommentCount()) + " comments ");
                if(post.isNSFW()) contentVH.nsfw.setVisibility(View.VISIBLE);
                else contentVH.nsfw.setVisibility(View.GONE);

                if (post.isSelf()) {
                    contentVH.postDets1.setVisibility(View.GONE);

                    if(post.getSelftextHTML() == null || postFragment.showFullCommentsButton) contentVH.selfText.setVisibility(View.GONE);
                    else {
                        //Self text textview
                        contentVH.selfText.setVisibility(View.VISIBLE);
                        strBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(
                                Html.fromHtml(post.getSelftextHTML(), null, new MyHtmlTagHandler()));
                        strBuilder = modifyURLSpan(strBuilder);
                        contentVH.selfText.setText(strBuilder);
                        contentVH.selfText.setMovementMethod(MyLinkMovementMethod.getInstance());
                    }

                    contentVH.selfText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f));
                    contentVH.postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                } else {
                    contentVH.postDets1.setText(post.getDomain() + " · ");
                    contentVH.selfText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0f));
                    contentVH.postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                    Thumbnail thumbnail = post.getThumbnailObject();
                    if (thumbnail!=null && thumbnail.hasThumbnail()) {
                        if(post.isNSFW() && !MainActivity.showNSFWpreview) {
                            //contentVH.postImage.setImageResource(R.drawable.nsfw2);
                            contentVH.postImage.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0f));
                        }
                        else {
                            try {
                                //Get Post Thumbnail
                                Picasso.with(activity).load(thumbnail.getUrl()).placeholder(R.drawable.noimage).into(contentVH.postImage);
                            } catch (IllegalArgumentException e) { e.printStackTrace(); }
                        }
                    } else {
                        contentVH.postImage.setVisibility(View.GONE);
                    }
                }
                contentVH.author.setText(post.getAuthor());
                contentVH.score.setText(Long.toString(post.getScore()));
                contentVH.age.setText(" · " + ConvertUtils.getSubmissionAge(post.getCreatedUTC()));
                contentVH.subreddit.setText(post.getSubreddit());

                contentVH.postOptions.setBackgroundColor(MainActivity.currentColor);
                //user logged in
                if(MainActivity.currentUser != null) {
                    //check user vote
                    if (post.getLikes().equals("true")) {
                        contentVH.score.setTextColor(Color.parseColor("#FF6600"));
                        contentVH.upvote.setImageResource(R.mipmap.ic_action_upvote_orange);
                        contentVH.downvote.setImageResource(R.mipmap.ic_action_downvote);
                    } else if (post.getLikes().equals("false")) {
                        contentVH.score.setTextColor(Color.BLUE);
                        contentVH.upvote.setImageResource(R.mipmap.ic_action_upvote);
                        contentVH.downvote.setImageResource(R.mipmap.ic_action_downvote_blue);
                    } else {
                        contentVH.score.setTextColor(MainActivity.textColor);
                        contentVH.upvote.setImageResource(R.mipmap.ic_action_upvote);
                        contentVH.downvote.setImageResource(R.mipmap.ic_action_downvote);
                    }
                    //check saved post
                    if(post.isSaved()) contentVH.save.setImageResource(R.mipmap.ic_action_save_yellow);
                    else contentVH.save.setImageResource(R.mipmap.ic_action_save);
                    //check hidden post
                    if(post.isHidden()) contentVH.hide.setImageResource(R.mipmap.ic_action_hide_red);
                    else contentVH.hide.setImageResource(R.mipmap.ic_action_hide);
                }

                if (postFragment.commentsLoaded) contentVH.progressBar.setVisibility(View.GONE);
                else contentVH.progressBar.setVisibility(View.VISIBLE);

                contentVH.postDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!post.isSelf()) {
                            LinkHandler linkHandler = new LinkHandler(activity, post);
                            linkHandler.handleIt();
                        }
                    }
                });
                contentVH.postDetails.setOnLongClickListener(longListener);
                if(selectedPosition == position) {
                    contentVH.postOptions.setVisibility(View.VISIBLE);
                    PostItemOptionsListener optionsListener = new PostItemOptionsListener(activity, post, this);
                    contentVH.upvote.setOnClickListener(optionsListener);
                    contentVH.downvote.setOnClickListener(optionsListener);
                    contentVH.save.setOnClickListener(optionsListener);
                    contentVH.hide.setOnClickListener(optionsListener);
                    contentVH.viewUser.setOnClickListener(optionsListener);
                    contentVH.openBrowser.setOnClickListener(optionsListener);
                    contentVH.moreOptions.setOnClickListener(optionsListener);
                }
                else contentVH.postOptions.setVisibility(View.GONE);
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
        public TextView commentHidden;
        public TextView score;
        public TextView age;
        private View view;
        public LinearLayout commentLayout;
        public LinearLayout commentOptionsLayout;
        public ImageView upvote;
        public ImageView downvote;
        public ImageView reply;
        public ImageView viewUser;
        public ImageView more;

        private static final String[] indColors = {"#000000", "#3366FF", "#E65CE6",
                "#E68A5C", "#00E68A", "#CCCC33"};

        public CommentViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            authorTextView = (TextView) itemView.findViewById(R.id.author_textview);
            commentTextView = (TextView) itemView.findViewById(R.id.comment_textview);
            score = (TextView) itemView.findViewById(R.id.txtView_score);
            commentHidden = (TextView) itemView.findViewById(R.id.txtView_commentHidden);
            age = (TextView) itemView.findViewById(R.id.txtView_age);
            colorBand = itemView.findViewById(R.id.color_band);
            hiddenCommentsCountTextView = (TextView) itemView.findViewById(R.id.hidden_comments_count_textview);
            commentLayout = (LinearLayout) itemView.findViewById(R.id.commentLayout);
            commentOptionsLayout = (LinearLayout) itemView.findViewById(R.id.commentOptionsLayout);
            upvote = (ImageView) itemView.findViewById(R.id.btn_upvote);
            downvote = (ImageView) itemView.findViewById(R.id.btn_downvote);
            reply = (ImageView) itemView.findViewById(R.id.btn_reply);
            viewUser = (ImageView) itemView.findViewById(R.id.btn_view_user);
            more = (ImageView) itemView.findViewById(R.id.btn_more);

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
        public TextView score;
        public TextView age;
        public TextView subreddit;
        public TextView selfText;
        public TextView nsfw;
        public ImageView postImage;
        public ProgressBar progressBar;
        public LinearLayout fullComments;
        public LinearLayout postDetails;
        public LinearLayout postOptions;
        public ImageView upvote;
        public ImageView downvote;
        public ImageView save;
        public ImageView hide;
        public ImageView viewUser;
        public ImageView openBrowser;
        public ImageView moreOptions;

        public ContentViewHolder(View itemView) {
            super(itemView);
            postTitle = (TextView) itemView.findViewById(R.id.postTitle);
            postDets1 = (TextView) itemView.findViewById(R.id.postDets1);
            author = (TextView) itemView.findViewById(R.id.author);
            score = (TextView) itemView.findViewById(R.id.postScore);
            age = (TextView) itemView.findViewById(R.id.postAge);
            subreddit = (TextView) itemView.findViewById(R.id.subreddit);
            nsfw = (TextView) itemView.findViewById(R.id.txtView_nsfw);
            selfText = (TextView) itemView.findViewById(R.id.selfText);
            postImage = (ImageView) itemView.findViewById(R.id.postImage);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar3);
            comments = (TextView) itemView.findViewById(R.id.txtView_comments);
            fullComments = (LinearLayout) itemView.findViewById(R.id.fullLoad);
            postDetails = (LinearLayout) itemView.findViewById(R.id.layout_post_details);
            postOptions = (LinearLayout) itemView.findViewById(R.id.layout_post_options);
            upvote =  (ImageView) itemView.findViewById(R.id.btn_upvote);
            downvote =  (ImageView) itemView.findViewById(R.id.btn_downvote);
            save =  (ImageView) itemView.findViewById(R.id.btn_save);
            hide =  (ImageView) itemView.findViewById(R.id.btn_hide);
            viewUser = (ImageView) itemView.findViewById(R.id.btn_view_user);
            openBrowser = (ImageView) itemView.findViewById(R.id.btn_open_browser);
            moreOptions =  (ImageView) itemView.findViewById(R.id.btn_more);
        }
    }

}
