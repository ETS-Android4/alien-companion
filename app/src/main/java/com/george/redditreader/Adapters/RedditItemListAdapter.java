package com.george.redditreader.Adapters;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
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

import com.george.redditreader.Activities.MainActivity;
import com.george.redditreader.ClickListeners.CommentItemOptionsListener;
import com.george.redditreader.ClickListeners.CommentLinkListener;
import com.george.redditreader.ClickListeners.PostItemListener;
import com.george.redditreader.ClickListeners.PostItemOptionsListener;
import com.george.redditreader.ClickListeners.ShowMoreListener;
import com.george.redditreader.Models.RedditItem;
import com.george.redditreader.Models.ShowMore;
import com.george.redditreader.R;
import com.george.redditreader.Utils.ConvertUtils;
import com.george.redditreader.Views.viewholders.PostViewHolder;
import com.george.redditreader.api.entity.Comment;
import com.george.redditreader.api.entity.Submission;
import com.george.redditreader.api.entity.Trophy;
import com.george.redditreader.api.entity.UserInfo;
import com.george.redditreader.enums.PostViewType;
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

    private final Context context;

    private List<RedditItem> redditItems;

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
            redditItems.add(showMoreButton);
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

    public RedditItem getLastItem() {
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
                int resource = R.layout.post_list_item;
                v = LayoutInflater.from(parent.getContext())
                        .inflate(resource, parent, false);
                viewHolder = new PostViewHolder(v, PostViewType.listItem);
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
                postViewHolder.linkButton.setOnLongClickListener(longListener);
                postViewHolder.commentsButton.setOnLongClickListener(longListener);

                //post item selected
                PostItemOptionsListener optionsListener = new PostItemOptionsListener(context, post, this);
                if(selectedPosition == position) postViewHolder.showPostOptions(optionsListener);
                else postViewHolder.hidePostOptions();
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

        public UserCommentViewHolder(View itemView) {
            super(itemView);
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
            commentBody.setText(ConvertUtils.noTrailingwhiteLines(Html.fromHtml(comment.getBodyHTML())));
            commentScore.setText(Long.toString(comment.getScore()));
            commentAge.setText(ConvertUtils.getSubmissionAge(comment.getCreatedUTC()));

            layoutComment.setOnClickListener(new CommentLinkListener(context, comment));

            //user logged in
            if(MainActivity.currentUser != null) {
                //check user vote
                if (comment.getLikes().equals("true")) {
                    commentScore.setTextColor(Color.parseColor("#FF6600"));
                    upvote.setImageResource(R.mipmap.ic_action_upvote_orange);
                    downvote.setImageResource(R.mipmap.ic_action_downvote);
                } else if (comment.getLikes().equals("false")) {
                    commentScore.setTextColor(Color.BLUE);
                    upvote.setImageResource(R.mipmap.ic_action_upvote);
                    downvote.setImageResource(R.mipmap.ic_action_downvote_blue);
                } else {
                    commentScore.setTextColor(Color.BLACK);
                    upvote.setImageResource(R.mipmap.ic_action_upvote);
                    downvote.setImageResource(R.mipmap.ic_action_downvote);
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
