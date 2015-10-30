package com.dyejeekis.aliencompanion.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.dyejeekis.aliencompanion.Activities.MainActivity;
import com.dyejeekis.aliencompanion.ClickListeners.CommentItemOptionsListener;
import com.dyejeekis.aliencompanion.ClickListeners.CommentLinkListener;
import com.dyejeekis.aliencompanion.ClickListeners.MessageItemListener;
import com.dyejeekis.aliencompanion.ClickListeners.PostItemListener;
import com.dyejeekis.aliencompanion.ClickListeners.PostItemOptionsListener;
import com.dyejeekis.aliencompanion.ClickListeners.ShowMoreListener;
import com.dyejeekis.aliencompanion.Models.RedditItem;
import com.dyejeekis.aliencompanion.Models.ShowMore;
import com.dyejeekis.aliencompanion.MyHtmlTagHandler;
import com.dyejeekis.aliencompanion.MyLinkMovementMethod;
import com.dyejeekis.aliencompanion.R;
import com.dyejeekis.aliencompanion.Utils.ConvertUtils;
import com.dyejeekis.aliencompanion.Views.viewholders.PostViewHolder;
import com.dyejeekis.aliencompanion.api.entity.Comment;
import com.dyejeekis.aliencompanion.api.entity.Message;
import com.dyejeekis.aliencompanion.api.entity.Submission;
import com.dyejeekis.aliencompanion.api.entity.Trophy;
import com.dyejeekis.aliencompanion.api.entity.UserInfo;
import com.dyejeekis.aliencompanion.enums.PostViewType;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sound on 8/28/2015.
 */
public class RedditItemListAdapter extends RecyclerView.Adapter {

    public static final int VIEW_TYPE_POST = 0;

    public static final int VIEW_TYPE_USER_COMMENT = 1;

    public static final int VIEW_TYPE_USER_INFO = 2;

    public static final int VIEW_TYPE_SHOW_MORE = 3;

    public static final int VIEW_TYPE_MESSAGE = 4;

    private final Context context;

    public List<RedditItem> redditItems;

    private ShowMore showMoreButton;

    private int selectedPosition;

    private boolean loadingMoreItems;

    public RedditItemListAdapter(Context context) {
        this.context = context;
        selectedPosition = -1;
        loadingMoreItems = false;
        redditItems = new ArrayList<>();
        showMoreButton = new ShowMore();
    }

    public RedditItemListAdapter(Context context, List<RedditItem> items) {
        this.context = context;
        selectedPosition = -1;
        loadingMoreItems = false;
        redditItems = new ArrayList<>();
        showMoreButton = new ShowMore();
        redditItems.addAll(items);
        //if(items.size() == RedditConstants.DEFAULT_LIMIT)
        if(!MainActivity.offlineModeEnabled) redditItems.add(showMoreButton);
    }

    public void add(RedditItem item) {
        int position;
        if(redditItems.size()==0) {
            redditItems.add(item);
            position = 0;
        }
        else {
            position = redditItems.size()-1;
            redditItems.add(position, item);
        }
        notifyItemInserted(position);
    }

    public void addAll(List<RedditItem> items) {
        redditItems.remove(showMoreButton);
        redditItems.addAll(items);
        //if(items.size() >= RedditConstants.DEFAULT_LIMIT)
            redditItems.add(showMoreButton);
        notifyItemRangeInserted(redditItems.size(), items.size());
    }

    public void remove(RedditItem item) {
        redditItems.remove(item);
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    private int getItemPosition(RedditItem item) {
        int i=0;
        for(RedditItem anItem : redditItems) {
            if(item == anItem) return i;
            i++;
        }
        return -1;
    }

    public RedditItem getLastItem() { //TODO: check out of bounds index exception, probably related to load task
        return redditItems.get(redditItems.size()-2);
    }

    public void setLoadingMoreItems(boolean flag) {
        loadingMoreItems = flag;
        notifyItemChanged(redditItems.size() - 1);
    }

    public void hideReadPosts() {
        for(int i = redditItems.size()-1;i>=0;i--) {
            if(redditItems.get(i) instanceof Submission) {
                Submission post = (Submission) redditItems.get(i);
                if(post.isClicked()) redditItems.remove(i);
            }
        }
        selectedPosition = -1;
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case VIEW_TYPE_POST:
                int resource = MainActivity.currentPostListView;
                if(MainActivity.dualPaneActive && resource == R.layout.post_list_item) resource = R.layout.post_list_item_reversed;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                PostViewType type;
                switch (resource) {
                    case R.layout.post_list_item_small_card:
                        type = PostViewType.smallCards;
                        break;
                    case R.layout.post_list_item_card:
                        type = PostViewType.cards;
                        break;
                    default:
                        type = PostViewType.listItem;
                        break;
                }
                viewHolder = new PostViewHolder(v, type);
                break;
            case VIEW_TYPE_USER_COMMENT:
                resource = R.layout.user_comment;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                viewHolder = new UserCommentViewHolder(v);
                break;
            case VIEW_TYPE_USER_INFO:
                resource = R.layout.user_info;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                viewHolder = new UserInfoViewHolder(v);
                break;
            case VIEW_TYPE_SHOW_MORE:
                resource = R.layout.footer_layout;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                viewHolder = new FooterViewHolder(v);
                break;
            case VIEW_TYPE_MESSAGE:
                resource = R.layout.user_message;
                v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
                viewHolder = new MessageViewHolder(v);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
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

        switch (getItemViewType(position)) {
            case VIEW_TYPE_POST:
                PostViewHolder postViewHolder = (PostViewHolder) viewHolder;
                Submission post = (Submission) getItemAt(position);
                postViewHolder.bindModel(context, post);

                PostItemListener listener = new PostItemListener(context, post, this, position);
                postViewHolder.linkButton.setOnClickListener(listener);
                postViewHolder.commentsButton.setOnClickListener(listener);

                PostItemOptionsListener optionsListener = new PostItemOptionsListener(context, post, this);
                switch (postViewHolder.viewType) {
                    case listItem: case details:
                        postViewHolder.linkButton.setOnLongClickListener(longListener);
                        postViewHolder.commentsButton.setOnLongClickListener(longListener);
                        //post item selected
                        if(selectedPosition == position) postViewHolder.showPostOptions(optionsListener);
                        else postViewHolder.hidePostOptions();
                        break;
                    case cards: case cardDetails: case smallCards:
                        postViewHolder.setCardButtonsListener(optionsListener);
                        break;
                }
                break;
            case VIEW_TYPE_USER_COMMENT:
                Comment comment = (Comment) getItemAt(position);
                UserCommentViewHolder userCommentViewHolder = (UserCommentViewHolder) viewHolder;
                userCommentViewHolder.bindModel(context, comment);

                userCommentViewHolder.layoutComment.setOnLongClickListener(longListener);

                //comment item selected
                CommentItemOptionsListener commentListener = new CommentItemOptionsListener(context, comment, this);
                if(selectedPosition == position) userCommentViewHolder.showCommentOptions(commentListener);
                else userCommentViewHolder.hideCommentOptions();
                break;
            case VIEW_TYPE_USER_INFO:
                UserInfo userInfo = (UserInfo) getItemAt(position);
                UserInfoViewHolder userInfoViewHolder = (UserInfoViewHolder) viewHolder;
                userInfoViewHolder.bindModel(context, userInfo);
                break;
            case VIEW_TYPE_SHOW_MORE:
                FooterViewHolder footerViewHolder = (FooterViewHolder) viewHolder;
                Activity activity = (Activity) context;
                footerViewHolder.showMoreButton.setOnClickListener(new ShowMoreListener(context, activity.getFragmentManager().findFragmentByTag("listFragment")));
                if(loadingMoreItems) {
                    footerViewHolder.showMoreProgress.setVisibility(View.VISIBLE);
                    footerViewHolder.showMoreButton.setVisibility(View.GONE);
                }
                else {
                    footerViewHolder.showMoreProgress.setVisibility(View.GONE);
                    footerViewHolder.showMoreButton.setVisibility(View.VISIBLE);
                }
                break;
            case VIEW_TYPE_MESSAGE:
                MessageViewHolder messageViewHolder = (MessageViewHolder) viewHolder;
                Message message = (Message) getItemAt(position);
                messageViewHolder.bindModel(context, message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return redditItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItemAt(position).getViewType();
    }

    public RedditItem getItemAt(int position) {
        return redditItems.get(position);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView subject;
        public TextView body;
        public TextView age;
        public TextView author;
        public TextView dest;
        public LinearLayout layoutMessage;
        //public LinearLayout layoutMessageOptions;

        public MessageViewHolder(View itemView) {
            super(itemView);
            layoutMessage = (LinearLayout) itemView.findViewById(R.id.layout_message);
            //layoutMessageOptions = (LinearLayout) itemView.findViewById(R.id.layout_messageOptions);
            subject = (TextView) itemView.findViewById(R.id.txtView_msgSubject);
            body = (TextView) itemView.findViewById(R.id.txtView_messageBody);
            age = (TextView) itemView.findViewById(R.id.txtView_messageAge);
            author = (TextView) itemView.findViewById(R.id.textView_messageAuthor);
            dest = (TextView) itemView.findViewById(R.id.textView_dest);
        }

        public void bindModel(Context context, Message message) {
            subject.setText(message.subject);
            SpannableStringBuilder strBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(message.bodyHTML, null, new MyHtmlTagHandler()));
            strBuilder = ConvertUtils.modifyURLSpan(context, strBuilder);
            body.setText(strBuilder);
            //body.setText(message.bodyPrepared);
            body.setMovementMethod(MyLinkMovementMethod.getInstance());
            age.setText(message.agePrepared);

            if(message.author.equals(MainActivity.currentUser.getUsername()) && !message.destination.equals(MainActivity.currentUser.getUsername())) {
                dest.setText("to ");
                author.setText(message.destination);
            }
            else {
                dest.setText("from ");
                author.setText(message.author);
            }

            MessageItemListener listener = new MessageItemListener(context, message);
            layoutMessage.setOnClickListener(listener);
            layoutMessage.setOnLongClickListener(listener);
        }
    }

    public static class UserCommentViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout layoutComment;
        public LinearLayout layoutCommentOptions;
        public TextView postTitle;
        public TextView commentSubreddit;
        public TextView commentBody;
        public TextView commentScore;
        public TextView commentAge;
        public ImageView upvote;
        public ImageView downvote;
        public ImageView reply;
        public ImageView viewUser;
        public ImageView more;
        private static int upvoteColor, downvoteColor;

        public UserCommentViewHolder(View itemView) {
            super(itemView);
            upvoteColor = Color.parseColor("#ff8b60");
            downvoteColor = Color.parseColor("#9494ff");
            layoutComment = (LinearLayout) itemView.findViewById(R.id.layout_comment);
            layoutCommentOptions = (LinearLayout) itemView.findViewById(R.id.layout_commentOptions);
            postTitle = (TextView) itemView.findViewById(R.id.txtView_postTitle);
            commentBody = (TextView) itemView.findViewById(R.id.txtView_commentBody);
            commentScore = (TextView) itemView.findViewById(R.id.txtView_commentScore);
            commentSubreddit = (TextView) itemView.findViewById(R.id.txtView_commentSubreddit);
            commentAge = (TextView) itemView.findViewById(R.id.txtView_commentAge);
            upvote = (ImageView) itemView.findViewById(R.id.btn_upvote);
            downvote = (ImageView) itemView.findViewById(R.id.btn_downvote);
            reply = (ImageView) itemView.findViewById(R.id.btn_reply);
            viewUser = (ImageView) itemView.findViewById(R.id.btn_view_user);
            more = (ImageView) itemView.findViewById(R.id.btn_more);
        }

        public void bindModel(Context context, Comment comment) {
            postTitle.setText(comment.getLinkTitle());
            commentSubreddit.setText(comment.getSubreddit());
            SpannableStringBuilder strBuilder = (SpannableStringBuilder) ConvertUtils.noTrailingwhiteLines(Html.fromHtml(comment.getBodyHTML(), null, new MyHtmlTagHandler()));
            strBuilder = ConvertUtils.modifyURLSpan(context, strBuilder);
            commentBody.setText(strBuilder);
            //commentBody.setText(ConvertUtils.modifyURLSpan(context, comment.bodyPrepared));
            commentBody.setMovementMethod(MyLinkMovementMethod.getInstance());
            //commentBody.setText(ConvertUtils.noTrailingwhiteLines(Html.fromHtml(comment.getBodyHTML())));
            commentScore.setText(Long.toString(comment.getScore()));
            commentAge.setText(comment.agePrepared);

            layoutComment.setOnClickListener(new CommentLinkListener(context, comment));

            layoutCommentOptions.setBackgroundColor(MainActivity.currentColor);
            //user logged in
            if(MainActivity.currentUser != null) {
                //check user vote
                if (comment.getLikes().equals("true")) {
                    commentScore.setTextColor(upvoteColor);
                    upvote.setImageResource(R.mipmap.ic_action_upvote_orange);
                    downvote.setImageResource(R.mipmap.ic_action_downvote_white);
                } else if (comment.getLikes().equals("false")) {
                    commentScore.setTextColor(downvoteColor);
                    upvote.setImageResource(R.mipmap.ic_action_upvote_white);
                    downvote.setImageResource(R.mipmap.ic_action_downvote_blue);
                } else {
                    commentScore.setTextColor(MainActivity.textHintColor);
                    upvote.setImageResource(R.mipmap.ic_action_upvote_white);
                    downvote.setImageResource(R.mipmap.ic_action_downvote_white);
                }
            }
        }

        public void showCommentOptions(View.OnClickListener listener) {
            layoutCommentOptions.setVisibility(View.VISIBLE);
            upvote.setOnClickListener(listener);
            downvote.setOnClickListener(listener);
            reply.setOnClickListener(listener);
            viewUser.setOnClickListener(listener);
            more.setOnClickListener(listener);
        }

        public void hideCommentOptions() {
            layoutCommentOptions.setVisibility(View.GONE);
        }
    }

    public static class UserInfoViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout layoutKarma;
        public TableLayout layoutTrophies;
        public TextView linkKarma;
        public TextView commentKarma;

        public UserInfoViewHolder(View itemView) {
            super(itemView);
            layoutKarma = (LinearLayout) itemView.findViewById(R.id.layout_karma);
            layoutTrophies = (TableLayout) itemView.findViewById(R.id.layout_trophies);
            linkKarma = (TextView) itemView.findViewById(R.id.txtView_linkKarma);
            commentKarma = (TextView) itemView.findViewById(R.id.txtView_commentKarma);
        }

        public void bindModel(Context context, UserInfo userInfo) {
            linkKarma.setText(Long.toString(userInfo.getLinkKarma()));
            commentKarma.setText(Long.toString(userInfo.getCommentKarma()));

            //setupTrophiesView((Activity) context, layoutTrophies, userInfo.getTrophyList());
        }

        private void setupTrophiesView(Activity mActivity, TableLayout trophiesLayout, List<Trophy> trophiesList) {

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
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        public Button showMoreButton;
        public ProgressBar showMoreProgress;

        public FooterViewHolder(View itemView) {
            super(itemView);
            showMoreButton = (Button) itemView.findViewById(R.id.showMore);
            showMoreProgress = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }

}
