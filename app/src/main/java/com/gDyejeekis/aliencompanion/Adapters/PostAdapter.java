package com.gDyejeekis.aliencompanion.Adapters;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.ClickListeners.CommentItemOptionsListener;
import com.gDyejeekis.aliencompanion.ClickListeners.PostItemListener;
import com.gDyejeekis.aliencompanion.ClickListeners.PostItemOptionsListener;
import com.gDyejeekis.aliencompanion.Fragments.PostFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.Utils.MyClickableSpan;
import com.gDyejeekis.aliencompanion.Utils.MyHtmlTagHandler;
import com.gDyejeekis.aliencompanion.Utils.MyLinkMovementMethod;
import com.gDyejeekis.aliencompanion.Utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Views.viewholders.PostViewHolder;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.enums.PostViewType;
import com.gDyejeekis.aliencompanion.multilevelexpindlistview.MultiLevelExpIndListAdapter;
import com.gDyejeekis.aliencompanion.multilevelexpindlistview.Utils;

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

    private String author = "";
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
                resource = R.layout.post_details_card;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                //viewHolder = new ContentViewHolder(v);
                viewHolder = new PostViewHolder(v, PostViewType.cardDetails);
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }

        return viewHolder;
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

                cvh.score.setText(Long.toString(comment.getScore()));
                String ageString = " pts · " + comment.agePrepared;
                if(comment.getEdited()) ageString += "*";
                cvh.age.setText(ageString);

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

                if(MyApplication.useMarkdownParsing) {

                }
                else {
                    //Comment textview
                    //parse html body using fromHTML
                    SpannableStringBuilder strBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(
                            Html.fromHtml(comment.getBodyHTML(), null, new MyHtmlTagHandler()));

                    MyClickableSpan clickableSpan = new MyClickableSpan() {
                        @Override
                        public boolean onLongClick(View widget) {
                            return false;
                        }

                        @Override
                        public void onClick(View widget) {
                            int previousSelected = selectedPosition;
                            selectedPosition = (selectedPosition == position) ? -1 : position;
                            notifyItemChanged(previousSelected);
                            notifyItemChanged(selectedPosition);
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            //ds.bgColor = Color.GREEN; //enable for debugging plain text clickable spans
                        }
                    };
                    strBuilder = ConvertUtils.modifyURLSpan(activity, strBuilder, clickableSpan);
                    cvh.commentTextView.setText(strBuilder);
                    cvh.commentTextView.setMovementMethod(MyLinkMovementMethod.getInstance());
                }

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
                    cvh.commentLayout.setBackgroundColor(MyApplication.colorPrimaryLight);
                    cvh.commentOptionsLayout.setVisibility(View.VISIBLE);
                    CommentItemOptionsListener listener = new CommentItemOptionsListener(activity, comment, this);
                    cvh.upvote.setOnClickListener(listener);
                    cvh.downvote.setOnClickListener(listener);
                    cvh.reply.setOnClickListener(listener);
                    cvh.viewUser.setOnClickListener(listener);
                    cvh.more.setOnClickListener(listener);
                }
                else {
                    //Comment permalink case
                    if(comment.getIdentifier().equals(postFragment.commentLinkId))
                        cvh.commentLayout.setBackgroundColor(MyApplication.commentPermaLinkBackgroundColor);
                    else cvh.commentLayout.setBackground(null);

                    cvh.commentOptionsLayout.setVisibility(View.GONE);
                }

                cvh.commentOptionsLayout.setBackgroundColor(MyApplication.currentColor);

                //user logged in
                if(MyApplication.currentUser != null) {
                    //check user vote
                    if (comment.getLikes().equals("true")) {
                        cvh.score.setTextColor(CommentViewHolder.upvoteColor);
                        cvh.upvote.setImageResource(R.mipmap.ic_arrow_upward_orange_48dp);
                        cvh.downvote.setImageResource(R.mipmap.ic_arrow_downward_white_48dp);
                    } else if (comment.getLikes().equals("false")) {
                        cvh.score.setTextColor(CommentViewHolder.downvoteColor);
                        cvh.upvote.setImageResource(R.mipmap.ic_arrow_upward_white_48dp);
                        cvh.downvote.setImageResource(R.mipmap.ic_arrow_downward_blue_48dp);
                    } else {
                        cvh.score.setTextColor(MyApplication.textHintColor);
                        cvh.upvote.setImageResource(R.mipmap.ic_arrow_upward_white_48dp);
                        cvh.downvote.setImageResource(R.mipmap.ic_arrow_downward_white_48dp);
                    }
                }

                break;
            case VIEW_TYPE_CONTENT:
                PostViewHolder postViewHolder = (PostViewHolder) viewHolder;
                Submission post = (Submission) getItemAt(position);
                author = post.getAuthor();
                postViewHolder.bindModel(activity, post);

                PostItemListener listener = new PostItemListener(activity, post, this, position);
                if(post.hasImageButton && post.getThumbnailObject().hasThumbnail()) postViewHolder.imageButton.setOnClickListener(listener);
                else postViewHolder.linkButton.setOnClickListener(listener);
                PostItemOptionsListener optionsListener = new PostItemOptionsListener(activity, post, this);
                postViewHolder.setCardButtonsListener(optionsListener);

                if(postFragment.showFullCommentsButton) {
                    postViewHolder.fullComments.setVisibility(View.VISIBLE);
                    postViewHolder.fullComments.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            postFragment.showFullCommentsButton = false;
                            postFragment.loadFullComments();
                        }
                    });
                }
                else postViewHolder.fullComments.setVisibility(View.GONE);

                if (postFragment.commentsLoaded) postViewHolder.commentsProgress.setVisibility(View.GONE); //TODO: replace commentsLoaded field and condition
                else postViewHolder.commentsProgress.setVisibility(View.VISIBLE);
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
        public static int upvoteColor, downvoteColor;

        private static final String[] indColors = {"#000000", "#3366FF", "#E65CE6",
                "#E68A5C", "#00E68A", "#CCCC33"};

        public CommentViewHolder(View itemView) {
            super(itemView);
            upvoteColor = Color.parseColor("#ff8b60");
            downvoteColor = Color.parseColor("#9494ff");
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
            int index = (indentation >= indColors.length) ? (indentation - indColors.length)+1 : indentation;
            int color = Color.parseColor(indColors[index]);
            colorBand.setBackgroundColor(color);
        }

        public void setPaddingLeft(int paddingLeft) {
            view.setPadding(paddingLeft,0,0,0);
        }
    }

}
