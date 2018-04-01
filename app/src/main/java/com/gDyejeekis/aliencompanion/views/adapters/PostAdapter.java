package com.gDyejeekis.aliencompanion.views.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.asynctask.LoadMoreCommentsTask;
import com.gDyejeekis.aliencompanion.enums.PostViewType;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.utils.HtmlTagHandler;
import com.gDyejeekis.aliencompanion.utils.SpanUtils;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.CommentItemOptionsListener;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.MyLinkMovementMethod;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemOptionsListener;
import com.gDyejeekis.aliencompanion.views.viewholders.PostCardViewHolder;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.models.MoreComment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.views.multilevelexpindlistview.MultiLevelExpIndListAdapter;
import com.gDyejeekis.aliencompanion.views.multilevelexpindlistview.Utils;
import com.gDyejeekis.aliencompanion.views.viewholders.PostViewHolder;

import org.apmem.tools.layouts.FlowLayout;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by George on 5/17/2015.
 */

public class PostAdapter extends MultiLevelExpIndListAdapter {
    /**
     * View type of an item or group.
     */
    public static final int VIEW_TYPE_ITEM = 1;

    /**
     * View type of the content.
     */
    public static final int VIEW_TYPE_CONTENT = 0;

    public static final int VIEW_TYPE_MORE = 9;

    public static final int NO_POSITION = RecyclerView.NO_POSITION;

    /**
     * This is called when the user click on an item or group.
     */
    //private final View.OnClickListener mListener;
    //private final View.OnLongClickListener mLongListener;

    private AppCompatActivity activity;
    private PostFragment postFragment;

    /**
     * Unit of indentation.
     */
    private static final int mPaddingDP = 5;

    private static final String[] indColors = {"#000000", "#3366FF", "#E65CE6",
            "#E68A5C", "#00E68A", "#CCCC33"};

    private static final int upvoteColor = Color.parseColor("#ff8b60");

    private static final int downvoteColor = Color.parseColor("#9494ff");

    private String originalPoster = "";
    public int selectedPosition;

    public PostAdapter (AppCompatActivity activity) {
        super();
        this.activity = activity;
        this.postFragment = (PostFragment) activity.getSupportFragmentManager()
                .findFragmentByTag("postFragment");
        selectedPosition = NO_POSITION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder viewHolder;
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                int resource = R.layout.comment_list_item;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                viewHolder = new CommentViewHolder(v);
                setCommentViewHolderListeners((CommentViewHolder) viewHolder);
                break;
            case VIEW_TYPE_MORE:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_list_item_more, parent, false);
                viewHolder = new MoreViewHolder(v);
                setMoreViewHolderListeners((MoreViewHolder) viewHolder);
                break;
            case VIEW_TYPE_CONTENT:
                resource = R.layout.post_details_card;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                viewHolder = new PostCardViewHolder(v, true);
                setPostViewHolderListeners((PostViewHolder) viewHolder);
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }
        return viewHolder;
    }

    private void setMoreViewHolderListeners(final MoreViewHolder viewHolder) {
        viewHolder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int position = viewHolder.getAdapterPosition();
                    MoreComment moreComment = (MoreComment) getItemAt(position);
                    if (!moreComment.isLoadingMore()) {
                        moreComment.setLoadingMore(true);
                        notifyItemChanged(position);
                        LoadMoreCommentsTask task = new LoadMoreCommentsTask(activity, moreComment);
                        task.execute();
                    }
                } catch (Exception e) {}
            }
        });
    }

    private void setPostViewHolderListeners(PostViewHolder viewHolder) {
        PostItemOptionsListener optionsListener = new PostItemOptionsListener(activity, viewHolder, this, PostViewType.cardDetails);
        PostItemListener postItemListener = new PostItemListener(activity, viewHolder, this);
        viewHolder.setClickListeners(postItemListener, null, optionsListener);
        if (viewHolder instanceof PostCardViewHolder) {
            ((PostCardViewHolder) viewHolder).fullComments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    postFragment.loadFullComments();
                }
            });
        }
    }

    private void setCommentViewHolderListeners(final CommentViewHolder viewHolder) {
        viewHolder.setCommentOptionsListener(new CommentItemOptionsListener(activity, viewHolder, this));
        viewHolder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int previousPosition = selectedPosition;
                selectedPosition = NO_POSITION;
                notifyItemChanged(previousPosition);
                toggleGroup(viewHolder.getAdapterPosition());
            }
        });
        viewHolder.rootLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = viewHolder.getAdapterPosition();
                ExpIndData item = getItemAt(position);
                if(!item.isGroup()) {
                    toggleMenuBar(position);
                    return true;
                }
                return false;
            }
        });
        viewHolder.menuBarToggleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMenuBar(viewHolder.getAdapterPosition());
            }
        });
    }

    private void toggleMenuBar(int newSelected) {
        int previousSelected = selectedPosition;
        if (previousSelected == NO_POSITION) {
            selectedPosition = newSelected;
        } else {
            selectedPosition = (previousSelected == newSelected) ? NO_POSITION : newSelected;
            notifyItemChanged(previousSelected);
        }
        notifyItemChanged(selectedPosition);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_ITEM:
                final CommentViewHolder cvh = (CommentViewHolder) viewHolder;
                final Comment comment = (Comment) getItemAt(position);
                cvh.bindModel(activity, comment, originalPoster);
                cvh.setCommentOptionsVisible(selectedPosition == position,
                        comment.getIdentifier().equals(postFragment.post.getLinkedCommentId()));
                break;
            case VIEW_TYPE_MORE:
                final MoreViewHolder moreViewHolder = (MoreViewHolder) viewHolder;
                final MoreComment moreComment = (MoreComment) getItemAt(position);
                moreViewHolder.bindModel(activity, moreComment);
                break;
            case VIEW_TYPE_CONTENT:
                PostCardViewHolder postViewHolder = (PostCardViewHolder) viewHolder;
                Submission post = (Submission) getItemAt(position);
                originalPoster = post.getAuthor();
                postViewHolder.bindModel(activity, post);

                if(postFragment.showFullCommentsButton)
                    postViewHolder.fullComments.setVisibility(View.VISIBLE);
                else postViewHolder.fullComments.setVisibility(View.GONE);

                if (postFragment.commentsLoaded)
                    postViewHolder.commentsProgress.setVisibility(View.GONE); //TODO: replace commentsLoaded field and condition
                else postViewHolder.commentsProgress.setVisibility(View.VISIBLE);
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }
    }

    private boolean isOpComment(String op, ExpIndData item) {
        try {
            if(item instanceof Comment) {
                return ((Comment) item).getAuthor().equals(op);
            }
        } catch (Exception e) {}
        return false;
    }

    private boolean isGilded(ExpIndData item) {
        try {
            if(item instanceof Submission) {
                return ((Submission)item).getGilded() != 0;
            }
            else if(item instanceof Comment) {
                return ((Comment)item).getGilded() != 0;
            }
        } catch (Exception e) {}
        return false;
    }

    private boolean isTimeFiltered(ExpIndData item, long timestampMilis) {
        try {
            if(item instanceof Comment) {
                return ((Comment)item).getCreatedUTC()*1000 >= timestampMilis;
            }
        } catch (Exception e) {}
        return false;
    }

    private boolean findText(ExpIndData item, String toFind, boolean matchCase) {
        try {
            String mainText = "";
            if(item instanceof Submission) {
                mainText = ((Submission)item).getSelftext();
            }
            else if(item instanceof Comment) {
                mainText = ((Comment)item).getBody();
            }

            if(matchCase) {
                if(mainText.contains(toFind)) {
                    item.setHighlightText(toFind, true);
                    notifyItemChanged(getData().indexOf(item));
                    return true;
                }
            }
            else {
                if(Pattern.compile(Pattern.quote(toFind), Pattern.CASE_INSENSITIVE).matcher(mainText).find()) {
                    item.setHighlightText(toFind, false);
                    notifyItemChanged(getData().indexOf(item));
                    return true;
                }
            }
        } catch (Exception e) {}
        return false;
    }

    private boolean isListedAuthor(ExpIndData item, List<String> authors) {
        try {
            if(item instanceof Comment) {
                return authors.contains(((Comment)item).getAuthor().toLowerCase());
            }
        } catch (Exception e) {}
        return false;
    }

    private int getParentCommentIndex(ExpIndData commentItem) {
        try {
            String parentName = ((Comment)commentItem).getParentId();
            if((parentName.startsWith("t1"))) {
                for(ExpIndData item : getData()) {
                    if(item instanceof Comment && ((Comment)item).getFullName().equals(parentName)) {
                        return getData().indexOf(item);
                    }
                }
            }
        } catch (Exception e) {}
        return NO_POSITION;
    }

    public int firstGildedIndex() {
        for(ExpIndData item : getData()) {
            if(isGilded(item)) {
                return getData().indexOf(item);
            }
        }
        return NO_POSITION;
    }

    public int nextGildedIndex(int start) {
        List<ExpIndData> sublist = getData().subList(start+1, getData().size());
        for(ExpIndData item : sublist) {
            if(isGilded(item)) {
                return getData().indexOf(item);
            }
        }
        return NO_POSITION;
    }

    public int previousGildedIndex(int start) {
        if(start-1>=0) {
            for(int i=start-1;i>=0;i--) {
                ExpIndData item = getData().get(i);
                if(isGilded(item)) {
                    return getData().indexOf(item);
                }
            }
        }
        return NO_POSITION;
    }

    public int firstTimeFilteredIndex(long timestampMilis) {
        for(ExpIndData item : getData()) {
            if(isTimeFiltered(item, timestampMilis)) {
                return getData().indexOf(item);
            }
        }
        return NO_POSITION;
    }

    public int nextTimeFilteredIndex(int start, long timestampMilis) {
        List<ExpIndData> sublist = getData().subList(start+1, getData().size());
        for(ExpIndData item : sublist) {
            if(isTimeFiltered(item, timestampMilis)) {
                return getData().indexOf(item);
            }
        }
        return NO_POSITION;
    }

    public int previousTimeFilteredIndex(int start, long timestampMilis) {
        if(start-1>=0) {
            for(int i=start-1;i>=0;i--) {
                ExpIndData item = getData().get(i);
                if(isTimeFiltered(item, timestampMilis)) {
                    return getData().indexOf(item);
                }
            }
        }
        return NO_POSITION;
    }

    public int firstSearchResultIndex(String text, boolean matchCase) {
        for(ExpIndData item : getData()) {
            if(findText(item, text, matchCase)) {
                return getData().indexOf(item);
            }
        }
        return NO_POSITION;
    }

    public int nextSearchResultIndex(int start, String text, boolean matchCase) {
        List<ExpIndData> sublist = getData().subList(start+1, getData().size());
        for(ExpIndData item : sublist) {
            if(findText(item, text, matchCase)) {
                return getData().indexOf(item);
            }
        }
        return NO_POSITION;
    }

    public int previousSearchResultIndex(int start, String text, boolean matchCase) {
        if(start-1>=0) {
            for(int i=start-1;i>=0;i--) {
                ExpIndData item = getData().get(i);
                if(findText(item, text, matchCase)) {
                    return getData().indexOf(item);
                }
            }
        }
        return NO_POSITION;
    }

    public int firstOpCommentIndex() {
        final String postAuthor = ((Submission) getItemAt(0)).getAuthor();
        for(ExpIndData item : getData()) {
            if(isOpComment(postAuthor, item)) {
                return getData().indexOf(item);
            }
        }
        return NO_POSITION;
    }

    public int nextOpCommentIndex(int start) {
        final String postAuthor = ((Submission) getItemAt(0)).getAuthor();
        List<ExpIndData> sublist = getData().subList(start+1, getData().size());
        for(ExpIndData item : sublist) {
            if(isOpComment(postAuthor, item)) {
                return getData().indexOf(item);
            }
        }
        return NO_POSITION;
    }

    public int previousOpCommentIndex(int start) {
        final String postAuthor = ((Submission) getItemAt(0)).getAuthor();
        if(start-1>=0) {
            for(int i=start-1;i>=0;i--) {
                ExpIndData item = getData().get(i);
                if(isOpComment(postAuthor, item)) {
                    return getData().indexOf(item);
                }
            }
        }
        else if(start==0) {
            return 0;
        }
        return NO_POSITION;
    }

    public int nextTopParentCommentIndex(int start) {
        List<ExpIndData> sublist = getData().subList(start+1, getData().size());
        for(ExpIndData item : sublist) {
            if(item.getIndentation()==0) {
                return getData().indexOf(item);
            }
        }
        return NO_POSITION;
    }

    public int previousTopParentCommentIndex(int start) {
        if(start-1>=0) {
            for(int i=start-1;i>=0;i--) {
                ExpIndData item = getData().get(i);
                if(item.getIndentation()==0) {
                    return getData().indexOf(item);
                }
            }
        }
        else if(start==0) {
            return 0;
        }
        return NO_POSITION;
    }

    public int firstAmaIndex(List<String> usernames) {
        for(ExpIndData item : getData()) {
            if(isListedAuthor(item, usernames)) {
                int parentIndex = getParentCommentIndex(item);
                return parentIndex == NO_POSITION ? getData().indexOf(item) : parentIndex;
            }
        }
        return NO_POSITION;
    }

    public int nextAmaIndex(int start, int currentAmaIndex, List<String> usernames) {
        List<ExpIndData> sublist = getData().subList(start+1, getData().size());
        for(ExpIndData item : sublist) {
            if(isListedAuthor(item, usernames)) {
                int parentIndex = getParentCommentIndex(item);
                return currentAmaIndex >= parentIndex ? getData().indexOf(item) : parentIndex;
            }
        }
        return NO_POSITION;
    }

    public int previousAmaIndex(int start, int currentAmaIndex, List<String> usernames) {
        if(start-1>=0) {
            for(int i=start;i>=0;i--) {
                ExpIndData item = getData().get(i);
                if(isListedAuthor(item, usernames)) {
                    int parentIndex = getParentCommentIndex(item);
                    return currentAmaIndex == parentIndex ? getData().indexOf(item) : parentIndex;
                }
            }
        }
        else if(start==0) {
            return 0;
        }
        return NO_POSITION;
    }

    public void clearHighlightedText() {
        for(ExpIndData item : getData()) {
            item.setHighlightText(null, false);
        }
        notifyDataSetChanged();
    }

    public void commentsRefreshed(Submission post, List<Comment> comments) {
        getData().clear();
        getData().add(post);
        addAll(comments);
        notifyDataSetChanged();
    }

    public void addComment(int position, Comment comment) {
        int previousSelected = selectedPosition;
        selectedPosition = NO_POSITION;
        notifyItemChanged(previousSelected);
        try { // update comment indentation
            RedditItem aboveItem = (RedditItem) getItemAt(position - 1);
            if (aboveItem instanceof Comment) {
                Comment aboveComment = (Comment) aboveItem;
                if (comment.getParentId().equals(aboveComment.getFullName())) {
                    comment.setIndentation(aboveComment.getIndentation() + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        add(position, comment);
    }

    public int indexOf(RedditItem item) {
        return getData().indexOf(item);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_CONTENT;
        }
        return getItemAt(position).getViewType();
    }

    public static class MoreViewHolder extends RecyclerView.ViewHolder {

        private View view;
        public View colorBand;
        public LinearLayout rootLayout;
        //public FlowLayout moreLayout;
        public TextView loadMore;
        public TextView moreReplies;

        public MoreViewHolder(View v) {
            super(v);
            view = v;
            colorBand = v.findViewById(R.id.color_band);
            rootLayout = v.findViewById(R.id.rootLayout);
            //moreLayout = v.findViewById(R.id.moreLayout);
            loadMore = v.findViewById(R.id.textView_load_more);
            moreReplies = v.findViewById(R.id.textView_more_replies);
        }

        void bindModel(Context context, MoreComment moreComment) {
            final int leftPadding;
            if (moreComment.getIndentation() == 0) {
                colorBand.setVisibility(View.GONE);
                leftPadding = 0;
            } else {
                colorBand.setVisibility(View.VISIBLE);
                setColorBandColor(moreComment.getIndentation());
                leftPadding = Utils.getPaddingPixels(context, mPaddingDP) * (moreComment.getIndentation() - 1);
            }
            setPaddingLeft(leftPadding);

            if (moreComment.isLoadingMore()) {
                moreReplies.setVisibility(View.GONE);
                loadMore.setText("loading...");
            } else {
                moreReplies.setVisibility(View.VISIBLE);
                int repliesCount = moreComment.getMoreCommentIds().size();
                String textEnd = (repliesCount == 1) ? "y)" : "ies)";
                moreReplies.setText("(" + repliesCount + " repl" + textEnd);
                loadMore.setText("load more comments");
            }
        }

        private void setColorBandColor(int indentation) {
            int index = (indentation >= indColors.length) ? (indentation - indColors.length)+1 : indentation;
            int color = Color.parseColor(indColors[index]);
            colorBand.setBackgroundColor(color);
        }

        private void setPaddingLeft(int paddingLeft) {
            view.setPadding(paddingLeft,0,0,0);
        }
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private View colorBand;
        public TextView authorTextView;
        public TextView commentTextView;
        public TextView hiddenCommentsCountTextView;
        public TextView commentHidden;
        public TextView score;
        public TextView age;
        public TextView goldCount;
        public LinearLayout layoutGilded;
        public LinearLayout rootLayout;
        public LinearLayout commentLayout;
        public LinearLayout menuBarToggleLayout;
        public LinearLayout commentOptionsLayout;
        public FlowLayout moreLayout;
        public ImageView upvote;
        public ImageView downvote;
        public ImageView reply;
        public ImageView viewUser;
        public ImageView save;
        public ImageView share;
        public ImageView more;
        public ImageView menuBarToggle;
        public GradientDrawable hiddenCommentsBackground;

        public float defaultIconOpacity, defaultIconOpacityDisabled;

        CommentViewHolder(View itemView) {
            super(itemView);

            defaultIconOpacity = MyApplication.currentBaseTheme == AppConstants.DARK_THEME_LOW_CONTRAST ? 0.6f : 1f;
            defaultIconOpacityDisabled = MyApplication.currentBaseTheme == AppConstants.DARK_THEME_LOW_CONTRAST ? 0.3f : 0.5f;

            view = itemView;
            authorTextView = itemView.findViewById(R.id.author_textview);
            commentTextView = itemView.findViewById(R.id.comment_textview);
            score = itemView.findViewById(R.id.txtView_score);
            commentHidden = itemView.findViewById(R.id.txtView_commentHidden);
            goldCount = itemView.findViewById(R.id.textView_gilded);
            age = itemView.findViewById(R.id.txtView_age);
            colorBand = itemView.findViewById(R.id.color_band);
            hiddenCommentsCountTextView = itemView.findViewById(R.id.hidden_comments_count_textview);
            commentLayout = itemView.findViewById(R.id.commentLayout);
            commentOptionsLayout = itemView.findViewById(R.id.commentOptionsLayout);
            upvote = itemView.findViewById(R.id.btn_upvote);
            downvote = itemView.findViewById(R.id.btn_downvote);
            reply = itemView.findViewById(R.id.btn_reply);
            viewUser = itemView.findViewById(R.id.btn_view_user);
            save = itemView.findViewById(R.id.btn_save);
            share = itemView.findViewById(R.id.btn_share);
            more = itemView.findViewById(R.id.btn_more);
            rootLayout = itemView.findViewById(R.id.rootLayout);
            layoutGilded = itemView.findViewById(R.id.layout_gilded);
            moreLayout = itemView.findViewById(R.id.moreLayout);
            menuBarToggle = itemView.findViewById(R.id.imageView_toggle_menu_bar);
            menuBarToggleLayout = itemView.findViewById(R.id.layout_toggle_menu_bar);

            hiddenCommentsBackground = (GradientDrawable) ContextCompat.getDrawable(itemView.getContext(), R.drawable.rounded_corner_orange);
            hiddenCommentsBackground.setColor(MyApplication.colorSecondary);

            commentOptionsLayout.setBackgroundColor(MyApplication.currentPrimaryColor);

            if (MyApplication.currentBaseTheme == AppConstants.LIGHT_THEME) menuBarToggle.setAlpha(0.54f);
            else if (MyApplication.currentBaseTheme == AppConstants.DARK_THEME_LOW_CONTRAST) menuBarToggle.setAlpha(0.6f);
            else menuBarToggle.setAlpha(1f);

            viewUser.setAlpha(defaultIconOpacity);
            share.setAlpha(defaultIconOpacity);
            more.setAlpha(defaultIconOpacity);

            save.setVisibility(View.GONE);
            share.setVisibility(View.GONE);
        }

        void bindModel(Context context, Comment comment, String originalPoster) {
            // modify based on indentation
            final int leftPadding;
            if (comment.getIndentation() == 0) {
                colorBand.setVisibility(View.GONE);
                leftPadding = 0;
            } else {
                colorBand.setVisibility(View.VISIBLE);
                setColorBandColor(comment.getIndentation());
                leftPadding = Utils.getPaddingPixels(context, mPaddingDP) * (comment.getIndentation() - 1);
            }
            setPaddingLeft(leftPadding);

            // check if gilded
            if(comment.getGilded() > 0) {
                layoutGilded.setVisibility(View.VISIBLE);
                goldCount.setText("x" + String.valueOf(comment.getGilded()));
            } else {
                layoutGilded.setVisibility(View.GONE);
            }

            // modify depending on if group or not
            if (comment.isGroup()) {
                commentHidden.setVisibility(View.VISIBLE);
                int hiddenComments = comment.getGroupSize();
                if (hiddenComments + 1 == 1)
                    hiddenCommentsCountTextView.setVisibility(View.GONE);
                else {
                    hiddenCommentsCountTextView.setBackground(hiddenCommentsBackground);
                    hiddenCommentsCountTextView.setPadding(6,6,6,6);
                    hiddenCommentsCountTextView.setVisibility(View.VISIBLE);
                    hiddenCommentsCountTextView.setText("+" + Integer.toString(hiddenComments));
                }
                commentTextView.setVisibility(View.GONE);
                menuBarToggleLayout.setVisibility(View.GONE);
            } else {
                commentHidden.setVisibility(View.GONE);
                hiddenCommentsCountTextView.setVisibility(View.GONE);
                commentTextView.setVisibility(View.VISIBLE);
                menuBarToggleLayout.setVisibility(View.VISIBLE);
            }

            // bind comment data
            String scoreText = comment.isScoreHidden() ? "[score hidden]" : Long.toString(comment.getScore());
            score.setText(scoreText);
            String ageString = " · " + comment.agePrepared;
            if (!comment.isScoreHidden()) ageString = " pts" + ageString;
            if (comment.getEdited()) ageString += "*";
            age.setText(ageString);

            // Author textview
            if (originalPoster.equals(comment.getAuthor()) && !originalPoster.equals("[deleted]")) {
                authorTextView.setTextColor(Color.WHITE);
                authorTextView.setBackgroundResource(R.drawable.rounded_corner_blue);
            } else {
                authorTextView.setTextColor(Color.parseColor("#5972ff"));
                authorTextView.setBackgroundColor(Color.TRANSPARENT);
            }
            authorTextView.setText(comment.getAuthor());

            if (AppConstants.USER_MARKDOWN_PARSER) {

            } else {
                // Comment textview
                // parse html body using fromHTML
                SpannableStringBuilder strBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(
                        Html.fromHtml(comment.getBodyHTML(), null, new HtmlTagHandler(commentTextView.getPaint())));

                strBuilder = SpanUtils.modifyURLSpan(context, strBuilder);
                // check for highlight text
                if(comment.getHighlightText()!=null) {
                    strBuilder = SpanUtils.highlightText(strBuilder, comment.getHighlightText(), comment.highlightMatchCase());
                }
                commentTextView.setText(strBuilder);
                commentTextView.setMovementMethod(MyLinkMovementMethod.getInstance());
            }

            // user logged in
            if (MyApplication.currentUser != null) {
                reply.setAlpha(defaultIconOpacity);
                // check user vote
                if (comment.getLikes().equals("true")) {
                    score.setTextColor(upvoteColor);
                    upvote.setImageResource(R.drawable.ic_arrow_upward_upvote_orange_48dp);
                    upvote.setAlpha(1f);
                    downvote.setImageResource(R.drawable.ic_arrow_downward_white_48dp);
                    downvote.setAlpha(defaultIconOpacity);
                } else if (comment.getLikes().equals("false")) {
                    score.setTextColor(downvoteColor);
                    upvote.setImageResource(R.drawable.ic_arrow_upward_white_48dp);
                    upvote.setAlpha(defaultIconOpacity);
                    downvote.setImageResource(R.drawable.ic_arrow_downward_downvote_blue_48dp);
                    downvote.setAlpha(1f);
                } else {
                    score.setTextColor(MyApplication.textSecondaryColor);
                    upvote.setImageResource(R.drawable.ic_arrow_upward_white_48dp);
                    upvote.setAlpha(defaultIconOpacity);
                    downvote.setImageResource(R.drawable.ic_arrow_downward_white_48dp);
                    downvote.setAlpha(defaultIconOpacity);
                }
                // check saved state
                if (comment.isSaved()) {
                    save.setImageResource(R.drawable.ic_star_border_yellow_500_48dp);
                    save.setAlpha(1f);
                } else {
                    save.setImageResource(R.drawable.ic_star_border_white_48dp);
                    save.setAlpha(defaultIconOpacity);
                }
            }
            // logged out
            else {
                reply.setAlpha(defaultIconOpacityDisabled);
                save.setAlpha(defaultIconOpacityDisabled);
                score.setTextColor(MyApplication.textSecondaryColor);
                upvote.setImageResource(R.drawable.ic_arrow_upward_white_48dp);
                upvote.setAlpha(defaultIconOpacityDisabled);
                downvote.setImageResource(R.drawable.ic_arrow_downward_white_48dp);
                downvote.setAlpha(defaultIconOpacityDisabled);
            }
        }

        void setCommentOptionsListener(CommentItemOptionsListener listener) {
            upvote.setOnClickListener(listener);
            downvote.setOnClickListener(listener);
            reply.setOnClickListener(listener);
            viewUser.setOnClickListener(listener);
            save.setOnClickListener(listener);
            share.setOnClickListener(listener);
            more.setOnClickListener(listener);
        }

        void setCommentOptionsVisible(boolean flag, boolean isCommentPermalink) {
            if (flag) showCommentOptions();
            else hideCommentOptions(isCommentPermalink);
        }

        private void showCommentOptions() {
            commentLayout.setBackgroundColor(MyApplication.colorPrimaryLight);
            menuBarToggle.setImageResource(MyApplication.nightThemeEnabled ? R.drawable.ic_expand_less_white_48dp : R.drawable.ic_expand_less_black_48dp);
            commentOptionsLayout.setVisibility(View.VISIBLE);
        }

        private void hideCommentOptions(boolean permalinkHighlight) {
            if (permalinkHighlight)
                commentLayout.setBackgroundColor(MyApplication.commentPermaLinkBackgroundColor);
            else commentLayout.setBackground(null);
            menuBarToggle.setImageResource(MyApplication.nightThemeEnabled ? R.drawable.ic_expand_more_white_48dp : R.drawable.ic_expand_more_black_48dp);
            commentOptionsLayout.setVisibility(View.GONE);
        }

        void setColorBandColor(int indentation) {
            int index = (indentation >= indColors.length) ? (indentation - indColors.length)+1 : indentation;
            int color = Color.parseColor(indColors[index]);
            colorBand.setBackgroundColor(color);
        }

        public void setPaddingLeft(int paddingLeft) {
            view.setPadding(paddingLeft,0,0,0);
        }
    }

}
