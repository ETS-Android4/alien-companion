package com.gDyejeekis.aliencompanion.views.adapters;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
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

import com.gDyejeekis.aliencompanion.asynctask.LoadMoreCommentsTask;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.CommentItemOptionsListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemListener;
import com.gDyejeekis.aliencompanion.views.on_click_listeners.PostItemOptionsListener;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.MyClickableSpan;
import com.gDyejeekis.aliencompanion.utils.MyHtmlTagHandler;
import com.gDyejeekis.aliencompanion.utils.MyLinkMovementMethod;
import com.gDyejeekis.aliencompanion.utils.ConvertUtils;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.views.viewholders.PostCardViewHolder;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.models.MoreComment;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.views.multilevelexpindlistview.MultiLevelExpIndListAdapter;
import com.gDyejeekis.aliencompanion.views.multilevelexpindlistview.Utils;

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

    public static final int POSITION_NOT_FOUND = -1;

    /**
     * This is called when the user click on an item or group.
     */
    //private final View.OnClickListener mListener;
    //private final View.OnLongClickListener mLongListener;

    private AppCompatActivity activity;

    /**
     * Unit of indentation.
     */
    private final int mPaddingDP = 5;

    private static final String[] indColors = {"#000000", "#3366FF", "#E65CE6",
            "#E68A5C", "#00E68A", "#CCCC33"};

    private static final int upvoteColor = Color.parseColor("#ff8b60");

    private static final int downvoteColor = Color.parseColor("#9494ff");

    private String author = "";
    public int selectedPosition;

    public PostAdapter (AppCompatActivity activity) {
        super();
        this.activity = activity;
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
                //v.setOnClickListener(mListener);
                //v.setOnLongClickListener(mLongListener);
                break;
            case VIEW_TYPE_MORE:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_list_item_more, parent, false);
                viewHolder = new MoreViewHolder(v);
                break;
            case VIEW_TYPE_CONTENT:
                resource = R.layout.post_details_card;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                viewHolder = new PostCardViewHolder(v, true);
                break;
            default:
                throw new IllegalStateException("unknown viewType");
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        final PostFragment postFragment = (PostFragment) activity.getFragmentManager()
                .findFragmentByTag("postFragment");
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_ITEM:
                final CommentViewHolder cvh = (CommentViewHolder) viewHolder;
                final Comment comment = (Comment) getItemAt(position);

                //modify based on indentation
                if (comment.getIndentation() == 0) {
                    cvh.colorBand.setVisibility(View.GONE);
                    cvh.setPaddingLeft(0);
                } else {
                    cvh.colorBand.setVisibility(View.VISIBLE);
                    cvh.setColorBandColor(comment.getIndentation());
                    int leftPadding = Utils.getPaddingPixels(activity, mPaddingDP) * (comment.getIndentation() - 1);
                    cvh.setPaddingLeft(leftPadding);
                }

                //check if gilded
                if(comment.getGilded() > 0) {
                    cvh.layoutGilded.setVisibility(View.VISIBLE);
                    cvh.goldCount.setText("x" + String.valueOf(comment.getGilded()));
                }
                else {
                    cvh.layoutGilded.setVisibility(View.GONE);
                }

                //modify depending on if group or not
                if (comment.isGroup()) {
                    cvh.commentHidden.setVisibility(View.VISIBLE);
                    int hiddenComments = comment.getGroupSize();
                    if (hiddenComments + 1 == 1)
                        cvh.hiddenCommentsCountTextView.setVisibility(View.GONE);
                    else {
                        cvh.hiddenCommentsCountTextView.setVisibility(View.VISIBLE);
                        cvh.hiddenCommentsCountTextView.setText("+" + Integer.toString(hiddenComments));
                    }
                    cvh.commentTextView.setVisibility(View.GONE);
                }
                else {
                    cvh.commentHidden.setVisibility(View.GONE);
                    cvh.hiddenCommentsCountTextView.setVisibility(View.GONE);
                    cvh.commentTextView.setVisibility(View.VISIBLE);
                }

                //check if current selected position
                if (selectedPosition == position) {
                    cvh.commentLayout.setBackgroundColor(MyApplication.colorPrimaryLight);
                    cvh.commentOptionsLayout.setVisibility(View.VISIBLE);
                    CommentItemOptionsListener listener = new CommentItemOptionsListener(activity, comment, this);
                    cvh.upvote.setOnClickListener(listener);
                    cvh.downvote.setOnClickListener(listener);
                    cvh.reply.setOnClickListener(listener);
                    cvh.viewUser.setOnClickListener(listener);
                    cvh.more.setOnClickListener(listener);
                } else {
                    //Comment permalink case
                    if (comment.getIdentifier().equals(postFragment.commentLinkId))
                        cvh.commentLayout.setBackgroundColor(MyApplication.commentPermaLinkBackgroundColor);
                    else cvh.commentLayout.setBackground(null);

                    cvh.commentOptionsLayout.setVisibility(View.GONE);
                }
                //cvh.commentOptionsLayout.setBackgroundColor(MyApplication.currentColor);

                //set listeners
                cvh.rootLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = cvh.getAdapterPosition();
                        int previousPosition = selectedPosition;
                        selectedPosition = -1;
                        notifyItemChanged(previousPosition);
                        toggleGroup(pos);
                    }
                });
                cvh.rootLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if(!comment.isGroup()) {
                            int pos = cvh.getAdapterPosition();
                            int previousPosition = selectedPosition;
                            if (pos == selectedPosition) selectedPosition = -1;
                            else selectedPosition = pos;
                            notifyItemChanged(previousPosition);
                            notifyItemChanged(selectedPosition);
                            return true;
                        }
                        return false;
                    }
                });

                //bind comment data
                cvh.score.setText(Long.toString(comment.getScore()));
                String ageString = " pts · " + comment.agePrepared;
                if (comment.getEdited()) ageString += "*";
                cvh.age.setText(ageString);

                //Author textview
                if (author.equals(comment.getAuthor()) && !author.equals("[deleted]")) {
                    cvh.authorTextView.setTextColor(Color.WHITE);
                    cvh.authorTextView.setBackgroundResource(R.drawable.rounded_corner_blue);
                } else {
                    cvh.authorTextView.setTextColor(Color.parseColor("#5972ff"));
                    cvh.authorTextView.setBackgroundColor(Color.TRANSPARENT);
                }
                cvh.authorTextView.setText(comment.getAuthor());

                if (MyApplication.useMarkdownParsing) {

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
                            int pos = cvh.getAdapterPosition();
                            selectedPosition = (selectedPosition == pos) ? -1 : pos;
                            notifyItemChanged(previousSelected);
                            notifyItemChanged(selectedPosition);
                            //notifyDataSetChanged();
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

                //user logged in
                if (MyApplication.currentUser != null) {
                    //check user vote
                    if (comment.getLikes().equals("true")) {
                        cvh.score.setTextColor(upvoteColor);
                        cvh.upvote.setImageResource(R.mipmap.ic_arrow_upward_orange_48dp);
                        if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                            cvh.downvote.setImageResource(R.mipmap.ic_arrow_downward_light_grey_48dp);
                        }
                        else {
                            cvh.downvote.setImageResource(R.mipmap.ic_arrow_downward_white_48dp);
                        }
                    }
                    else if (comment.getLikes().equals("false")) {
                        cvh.score.setTextColor(downvoteColor);
                        if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                            cvh.upvote.setImageResource(R.mipmap.ic_arrow_upward_light_grey_48dp);
                        }
                        else {
                            cvh.upvote.setImageResource(R.mipmap.ic_arrow_upward_white_48dp);
                        }
                        cvh.downvote.setImageResource(R.mipmap.ic_arrow_downward_blue_48dp);
                    }
                    else {
                        cvh.score.setTextColor(MyApplication.textHintColor);
                        if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                            cvh.upvote.setImageResource(R.mipmap.ic_arrow_upward_light_grey_48dp);
                            cvh.downvote.setImageResource(R.mipmap.ic_arrow_downward_light_grey_48dp);
                        }
                        else {
                            cvh.upvote.setImageResource(R.mipmap.ic_arrow_upward_white_48dp);
                            cvh.downvote.setImageResource(R.mipmap.ic_arrow_downward_white_48dp);
                        }
                    }
                }
                break;
            case VIEW_TYPE_MORE:
                final MoreViewHolder moreViewHolder = (MoreViewHolder) viewHolder;
                final MoreComment moreComment = (MoreComment) getItemAt(position);

                if (moreComment.getIndentation() == 0) {
                    moreViewHolder.colorBand.setVisibility(View.GONE);
                    moreViewHolder.setPaddingLeft(0);
                } else {
                    moreViewHolder.colorBand.setVisibility(View.VISIBLE);
                    moreViewHolder.setColorBandColor(moreComment.getIndentation());
                    int leftPadding = Utils.getPaddingPixels(activity, mPaddingDP) * (moreComment.getIndentation() - 1);
                    moreViewHolder.setPaddingLeft(leftPadding);
                }

                if(moreComment.isLoadingMore()) {
                    moreViewHolder.rootLayout.setOnClickListener(null);
                    moreViewHolder.moreReplies.setVisibility(View.GONE);
                    moreViewHolder.loadMore.setText("loading...");
                }
                else {
                    moreViewHolder.rootLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int pos = moreViewHolder.getAdapterPosition();
                            moreComment.setLoadingMore(true);
                            notifyItemChanged(pos);
                            LoadMoreCommentsTask task = new LoadMoreCommentsTask(activity, moreComment);
                            task.execute();
                        }
                    });
                    moreViewHolder.moreReplies.setVisibility(View.VISIBLE);
                    int repliesCount = moreComment.getMoreCommentIds().size();
                    String textEnd = (repliesCount == 1) ? "y)" : "ies)";
                    moreViewHolder.moreReplies.setText("(" + repliesCount + " repl" + textEnd);
                    moreViewHolder.loadMore.setText("load more comments");
                }
                break;
            case VIEW_TYPE_CONTENT:
                PostCardViewHolder postViewHolder = (PostCardViewHolder) viewHolder;
                Submission post = (Submission) getItemAt(position);
                author = post.getAuthor();
                postViewHolder.bindModel(activity, post);

                PostItemListener listener = new PostItemListener(activity, post, this, position);
                PostItemOptionsListener optionsListener = new PostItemOptionsListener(activity, post, this);
                postViewHolder.setClickListeners(listener, null, optionsListener);

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
                    // TODO: 3/19/2017 highlight text
                    return true;
                }
            }
            else {
                if(Pattern.compile(Pattern.quote(toFind), Pattern.CASE_INSENSITIVE).matcher(mainText).find()) {
                    // TODO: 3/19/2017 highlight text
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
        return POSITION_NOT_FOUND;
    }

    public int firstGildedIndex() {
        for(ExpIndData item : getData()) {
            if(isGilded(item)) {
                return getData().indexOf(item);
            }
        }
        return POSITION_NOT_FOUND;
    }

    public int nextGildedIndex(int start) {
        List<ExpIndData> sublist = getData().subList(start+1, getData().size());
        for(ExpIndData item : sublist) {
            if(isGilded(item)) {
                return getData().indexOf(item);
            }
        }
        return POSITION_NOT_FOUND;
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
        return POSITION_NOT_FOUND;
    }

    public int firstTimeFilteredIndex(long timestampMilis) {
        for(ExpIndData item : getData()) {
            if(isTimeFiltered(item, timestampMilis)) {
                return getData().indexOf(item);
            }
        }
        return POSITION_NOT_FOUND;
    }

    public int nextTimeFilteredIndex(int start, long timestampMilis) {
        List<ExpIndData> sublist = getData().subList(start+1, getData().size());
        for(ExpIndData item : sublist) {
            if(isTimeFiltered(item, timestampMilis)) {
                return getData().indexOf(item);
            }
        }
        return POSITION_NOT_FOUND;
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
        return POSITION_NOT_FOUND;
    }

    public int firstSearchResultIndex(String text, boolean matchCase) {
        for(ExpIndData item : getData()) {
            if(findText(item, text, matchCase)) {
                return getData().indexOf(item);
            }
        }
        return POSITION_NOT_FOUND;
    }

    public int nextSearchResultIndex(int start, String text, boolean matchCase) {
        List<ExpIndData> sublist = getData().subList(start+1, getData().size());
        for(ExpIndData item : sublist) {
            if(findText(item, text, matchCase)) {
                return getData().indexOf(item);
            }
        }
        return POSITION_NOT_FOUND;
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
        return POSITION_NOT_FOUND;
    }

    public int firstOpCommentIndex() {
        final String postAuthor = ((Submission) getItemAt(0)).getAuthor();
        for(ExpIndData item : getData()) {
            if(isOpComment(postAuthor, item)) {
                return getData().indexOf(item);
            }
        }
        return POSITION_NOT_FOUND;
    }

    public int nextOpCommentIndex(int start) {
        final String postAuthor = ((Submission) getItemAt(0)).getAuthor();
        List<ExpIndData> sublist = getData().subList(start+1, getData().size());
        for(ExpIndData item : sublist) {
            if(isOpComment(postAuthor, item)) {
                return getData().indexOf(item);
            }
        }
        return POSITION_NOT_FOUND;
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
        return POSITION_NOT_FOUND;
    }

    public int nextTopParentCommentIndex(int start) {
        List<ExpIndData> sublist = getData().subList(start+1, getData().size());
        for(ExpIndData item : sublist) {
            if(item.getIndentation()==0) {
                return getData().indexOf(item);
            }
        }
        return POSITION_NOT_FOUND;
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
        return POSITION_NOT_FOUND;
    }

    public int firstAmaIndex(List<String> usernames) {
        for(ExpIndData item : getData()) {
            if(isListedAuthor(item, usernames)) {
                int parentIndex = getParentCommentIndex(item);
                return parentIndex == POSITION_NOT_FOUND ? getData().indexOf(item) : parentIndex;
            }
        }
        return POSITION_NOT_FOUND;
    }

    public int nextAmaIndex(int start, int currentAmaIndex, List<String> usernames) {
        List<ExpIndData> sublist = getData().subList(start+1, getData().size());
        for(ExpIndData item : sublist) {
            if(isListedAuthor(item, usernames)) {
                int parentIndex = getParentCommentIndex(item);
                return currentAmaIndex >= parentIndex ? getData().indexOf(item) : parentIndex;
            }
        }
        return POSITION_NOT_FOUND;
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
        return POSITION_NOT_FOUND;
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
            rootLayout = (LinearLayout) v.findViewById(R.id.rootLayout);
            //moreLayout = (FlowLayout) v.findViewById(R.id.moreLayout);
            loadMore = (TextView) v.findViewById(R.id.textView_load_more);
            moreReplies = (TextView) v.findViewById(R.id.textView_more_replies);
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

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        private View colorBand;
        public TextView authorTextView;
        public TextView commentTextView;
        public TextView hiddenCommentsCountTextView;
        public TextView commentHidden;
        public TextView score;
        public TextView age;
        public TextView goldCount;
        private View view;
        public LinearLayout layoutGilded;
        public LinearLayout rootLayout;
        public LinearLayout commentLayout;
        public LinearLayout commentOptionsLayout;
        public FlowLayout moreLayout;
        public ImageView upvote;
        public ImageView downvote;
        public ImageView reply;
        public ImageView viewUser;
        public ImageView more;

        public CommentViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            authorTextView = (TextView) itemView.findViewById(R.id.author_textview);
            commentTextView = (TextView) itemView.findViewById(R.id.comment_textview);
            score = (TextView) itemView.findViewById(R.id.txtView_score);
            commentHidden = (TextView) itemView.findViewById(R.id.txtView_commentHidden);
            goldCount = (TextView) itemView.findViewById(R.id.textView_gilded);
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
            rootLayout = (LinearLayout) itemView.findViewById(R.id.rootLayout);
            layoutGilded = (LinearLayout) itemView.findViewById(R.id.layout_gilded);
            moreLayout = (FlowLayout) itemView.findViewById(R.id.moreLayout);

            commentOptionsLayout.setBackgroundColor(MyApplication.currentColor);

            if(MyApplication.currentBaseTheme == MyApplication.DARK_THEME_LOW_CONTRAST) {
                viewUser.setImageResource(R.mipmap.ic_person_light_grey_48dp);
                reply.setImageResource(R.mipmap.ic_reply_light_grey_48dp);
                more.setImageResource(R.mipmap.ic_more_vert_light_grey_48dp);
            }
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
