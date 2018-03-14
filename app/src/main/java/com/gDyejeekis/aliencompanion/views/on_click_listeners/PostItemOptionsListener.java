package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.MessageActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.activities.SearchActivity;
import com.gDyejeekis.aliencompanion.activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.activities.SubredditActivity;
import com.gDyejeekis.aliencompanion.activities.UserActivity;
import com.gDyejeekis.aliencompanion.api.retrieval.params.UserSubmissionsCategory;
import com.gDyejeekis.aliencompanion.enums.PostViewType;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.fragments.RedditContentFragment;
import com.gDyejeekis.aliencompanion.models.RedditItem;
import com.gDyejeekis.aliencompanion.utils.CleaningUtils;
import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;
import com.gDyejeekis.aliencompanion.asynctask.SaveOfflineActionTask;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.ReportDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.TwoOptionDialogFragment;
import com.gDyejeekis.aliencompanion.asynctask.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.fragments.PostListFragment;
import com.gDyejeekis.aliencompanion.models.offline_actions.DownvoteAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.HideAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.NoVoteAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.SaveAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.UnhideAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.UnsaveAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.UpvoteAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.services.DownloaderService;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Submission;
import com.gDyejeekis.aliencompanion.api.utils.ApiEndpointUtils;
import com.gDyejeekis.aliencompanion.enums.SubmitType;
import com.gDyejeekis.aliencompanion.enums.UserActionType;
import com.gDyejeekis.aliencompanion.views.viewholders.PostCardViewHolder;
import com.gDyejeekis.aliencompanion.views.viewholders.PostListViewHolder;
import com.gDyejeekis.aliencompanion.views.viewholders.PostSmallCardViewHolder;

/**
 * Created by George on 8/9/2015.
 */
public class PostItemOptionsListener implements View.OnClickListener {

    private Context context;
    private RecyclerView.ViewHolder viewHolder;
    private RecyclerView.Adapter currentAdapter;
    private PostViewType viewType;

    public PostItemOptionsListener(Context context, RecyclerView.ViewHolder viewHolder, RecyclerView.Adapter adapter, PostViewType postViewType) {
        this.context = context;
        this.viewHolder = viewHolder;
        this.currentAdapter = adapter;
        this.viewType = postViewType;
    }

    private void viewUser(Submission post) {
        Intent intent = new Intent(context, UserActivity.class);
        intent.putExtra("username", post.getAuthor());
        context.startActivity(intent);
    }

    private void sharePost(final Submission post) {
        final String commentsUrl = ApiEndpointUtils.REDDIT_BASE_URL + "/r/" + post.getSubreddit() + "/comments/" + post.getIdentifier();
        if (post.isSelf()) {
            GeneralUtils.shareUrl(context, "Share self-post url to..", commentsUrl);
        }
        else {
            TwoOptionDialogFragment choiceDialog = TwoOptionDialogFragment.newInstance("SHARE LINK", "SHARE COMMENTS", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String label = "Share via..";
                    String url;
                    if (v.getId() == R.id.button_option_one) {
                        label = "Share post url via..";
                        url = post.getURL();
                    } else {
                        label = "Share comments url via..";
                        url = commentsUrl;
                    }
                    GeneralUtils.shareUrl(context, label, url);
                }
            });
            choiceDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
        }
    }

    private void openInBrowser(Submission post) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(post.getURL()));
        context.startActivity(intent);
    }

    private Submission getCurrentPost() {
        int position = viewHolder.getAdapterPosition();
        if (currentAdapter instanceof RedditItemListAdapter)
            return (Submission) ((RedditItemListAdapter) currentAdapter).getItemAt(position);
        else if (currentAdapter instanceof PostAdapter)
            return (Submission) ((PostAdapter) currentAdapter).getItemAt(position);
        return null;
    }

    @Override
    public void onClick(View v) {
        Submission post = getCurrentPost();
        switch (v.getId()) {
            case R.id.btn_upvote:case R.id.imageView_upvote_classic:
                UserActionType actionType;
                LoadUserActionTask task;
                SaveOfflineActionTask task1;
                if (MyApplication.currentUser!=null) {
                    if (post.getLikes().equals("true")) {
                        post.setLikes("null");
                        post.setScore(post.getScore() - 1);
                        actionType = UserActionType.novote;
                    } else {
                        if (post.getLikes().equals("false")) post.setScore(post.getScore() + 2);
                        else post.setScore(post.getScore() + 1);
                        post.setLikes("true");
                        actionType = UserActionType.upvote;
                    }

                    currentAdapter.notifyDataSetChanged();
                    notifySecondPaneChanges();

                    if( GeneralUtils.isNetworkAvailable(context)) {
                        task = new LoadUserActionTask(context, post.getFullName(), actionType);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        OfflineUserAction action;
                        String accountName = MyApplication.currentAccount.getUsername();
                        if (actionType == UserActionType.novote) {
                            action = new NoVoteAction(accountName, post.getFullName(), post.getTitle());
                        } else {
                            action = new UpvoteAction(accountName, post.getFullName(), post.getTitle());
                        }
                        task1 = new SaveOfflineActionTask(context, action);
                        task1.execute();
                    }
                } else {
                    showSnackbar("Must be logged in to vote");
                }
                break;
            case R.id.btn_downvote: case R.id.imageView_downvote_classic:
                if (MyApplication.currentUser!=null) {
                    if (post.getLikes().equals("false")) {
                        post.setLikes("null");
                        post.setScore(post.getScore() + 1);
                        actionType = UserActionType.novote;
                    } else {
                        if (post.getLikes().equals("true")) post.setScore(post.getScore() - 2);
                        else post.setScore(post.getScore() - 1);
                        post.setLikes("false");
                        actionType = UserActionType.downvote;
                    }

                    currentAdapter.notifyDataSetChanged();
                    notifySecondPaneChanges();

                    if (GeneralUtils.isNetworkAvailable(context)) {
                        task = new LoadUserActionTask(context, post.getFullName(), actionType);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        OfflineUserAction action;
                        String accountName = MyApplication.currentAccount.getUsername();
                        if (actionType == UserActionType.novote) {
                            action = new NoVoteAction(accountName, post.getFullName(), post.getTitle());
                        } else {
                            action = new DownvoteAction(accountName, post.getFullName(), post.getTitle());
                        }
                        task1 = new SaveOfflineActionTask(context, action);
                        task1.execute();
                    }
                } else {
                    showSnackbar("Must be logged in to vote");
                }
                break;
            case R.id.btn_save:
                if (MyApplication.currentUser!=null) {
                    if (post.isSaved()) {
                        post.setSaved(false);
                        actionType = UserActionType.unsave;
                    } else {
                        post.setSaved(true);
                        actionType = UserActionType.save;
                    }

                    currentAdapter.notifyDataSetChanged();
                    notifySecondPaneChanges();

                    if (GeneralUtils.isNetworkAvailable(context)) {
                        task = new LoadUserActionTask(context, post, actionType);
                        task.execute();
                    } else {
                        OfflineUserAction action;
                        String accountName = MyApplication.currentAccount.getUsername();
                        if (actionType == UserActionType.save) {
                            action = new SaveAction(accountName, post.getFullName(), post.getTitle());
                        } else {
                            action = new UnsaveAction(accountName, post.getFullName(), post.getTitle());
                        }
                        task1 = new SaveOfflineActionTask(context, action);
                        task1.execute();
                    }
                } else {
                    showSnackbar("Must be logged in to save");
                }
                break;
            case R.id.btn_hide:
                int index = -1;
                boolean notifyOnPost = true;
                if (MyApplication.currentUser!=null) {
                    if (post.isHidden()) {
                        post.setHidden(false);
                        actionType = UserActionType.unhide;
                        currentAdapter.notifyDataSetChanged();
                        // case viewing user's hidden posts
                        if(context instanceof UserActivity && ((UserActivity) context).getListFragment().userContent == UserSubmissionsCategory.HIDDEN) {
                            notifyOnPost = false;
                            notifySecondPaneChanges();
                        }
                    } else {
                        post.setHidden(true);
                        actionType = UserActionType.hide;
                        // case viewing user's hidden posts
                        if (context instanceof UserActivity && ((UserActivity) context).getListFragment().userContent == UserSubmissionsCategory.HIDDEN) {
                            notifyOnPost = false;
                            currentAdapter.notifyDataSetChanged();
                            notifySecondPaneChanges();
                        } else {
                            if (!(context instanceof PostActivity) && currentAdapter instanceof RedditItemListAdapter) {
                                index = ((RedditItemListAdapter) currentAdapter).indexOf(post);
                                ((RedditItemListAdapter) currentAdapter).remove(post);
                                notifyDataSetChangedDelayed();
                                notifySecondPaneChanges();
                            } else {
                                currentAdapter.notifyDataSetChanged();
                                RecyclerView.Adapter secondPaneAdapter = getSecondPaneAdapter();
                                if (secondPaneAdapter != null && secondPaneAdapter instanceof RedditItemListAdapter) {
                                    index = ((RedditItemListAdapter) secondPaneAdapter).indexOf(post);
                                    ((RedditItemListAdapter) secondPaneAdapter).remove(post);
                                    notifyDataSetChangedDelayed(secondPaneAdapter);
                                }
                            }
                        }
                    }

                    if (GeneralUtils.isNetworkAvailable(context)) {
                        task = new LoadUserActionTask(context, post, index, actionType);
                        task.setNotifyOnPostExecute(notifyOnPost);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        OfflineUserAction action;
                        String accountName = MyApplication.currentAccount.getUsername();
                        if (actionType == UserActionType.hide) {
                            action = new HideAction(accountName, post.getFullName(), post.getTitle());
                        } else {
                            action = new UnhideAction(accountName, post.getFullName(), post.getTitle());
                        }
                        task1 = new SaveOfflineActionTask(context, action);
                        task1.execute();
                    }
                } else {
                    showSnackbar("Must be logged in to hide");
                }
                break;
            case R.id.btn_view_user:
                viewUser(post);
                break;
            case R.id.btn_share:
                sharePost(post);
                break;
            case R.id.btn_open_browser:
                openInBrowser(post);
                break;
            case R.id.btn_more:
                showMoreOptionsPopup(v, post);
                break;
        }
    }

    private void showMoreOptionsPopup(View v, final Submission post) {
        final PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.inflate(R.menu.menu_post_more_options);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add_to_synced:
                        String toastMessage;
                        if(GeneralUtils.isNetworkAvailable(context)) {
                            if(MyApplication.syncOverWifiOnly && !GeneralUtils.isConnectedOverWifi(context)) {
                                toastMessage = "Syncing over mobile data connection is disabled";
                            }
                            else {
                                toastMessage = "Post added to sync queue";
                                Intent intent = new Intent(context, DownloaderService.class);
                                intent.putExtra("post", post);
                                context.startService(intent);
                            }
                        }
                        else {
                            toastMessage = "Network connection unavailable";
                        }
                        ToastUtils.showToast(context, toastMessage);
                        return true;
                    case R.id.action_remove_from_synced:
                        //final int index = ((RedditItemListAdapter) currentAdapter).indexOf(post);
                        ((RedditItemListAdapter) currentAdapter).remove(post);
                        notifyDataSetChangedDelayed();
                        new AsyncTask<String, Void, Boolean>() {

                            @Override
                            protected Boolean doInBackground(String... params) {
                                return CleaningUtils.clearSyncedPostFromCategory(context, AppConstants.INDIVIDUALLY_SYNCED_DIR_NAME, params[0]);
                            }

                            @Override
                            protected void onPostExecute(Boolean success) {
                                String message = (success) ? "Post deleted" : "Failed to delete post";
                                ToastUtils.showToast(context, message);
                            }
                        }.execute(post.getIdentifier());
                        return true;
                    case R.id.action_open_in_browser:
                        openInBrowser(post);
                        return true;
                    case R.id.action_copy_to_clipboard:
                        if(post.isSelf()) {
                            String postLink = ApiEndpointUtils.REDDIT_BASE_URL + "/r/" + post.getSubreddit() + "/comments/" + post.getIdentifier();
                            GeneralUtils.copyTextToClipboard(context, "Post permalink", postLink);
                        }
                        else {
                            TwoOptionDialogFragment choiceDialog = TwoOptionDialogFragment.newInstance("POST LINK", "COMMENTS URL",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String stringToCopy;
                                            String label;
                                            if (v.getId() == R.id.button_option_one) {
                                                stringToCopy = post.getURL();
                                                label = "Post url";
                                            } else {
                                                stringToCopy = ApiEndpointUtils.REDDIT_BASE_URL + "/r/" + post.getSubreddit() + "/comments/" + post.getIdentifier();
                                                label = "Comments url";
                                            }
                                            GeneralUtils.copyTextToClipboard(context, label, stringToCopy);
                                        }
                                    });
                            choiceDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
                        }
                        return true;
                    case R.id.action_select_text:
                        // TODO: 4/7/2017
                        return true;
                    case R.id.action_share:
                        sharePost(post);
                        return true;
                    case R.id.action_view_user:
                        viewUser(post);
                        return true;
                    case R.id.action_view_subreddit:
                        Intent intent = new Intent(context, SubredditActivity.class);
                        intent.putExtra("subreddit", post.getSubreddit().toLowerCase());
                        context.startActivity(intent);
                        return true;
                    case R.id.action_mark_nsfw:
                        UserActionType actionType;
                        if (post.isNSFW()) {
                            post.setNSFW(false);
                            actionType = UserActionType.unmarkNSFW;
                        }
                        else {
                            post.setNSFW(true);
                            actionType = UserActionType.markNSFW;
                        }
                        currentAdapter.notifyDataSetChanged();
                        LoadUserActionTask task = new LoadUserActionTask(context, post.getFullName(), actionType);
                        //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        task.execute();
                        return true;
                    case R.id.action_mark_spoiler:
                        if(post.isSpoiler()) {
                            post.setSpoiler(false);
                            actionType = UserActionType.unmarkSpoiler;
                        }
                        else {
                            post.setSpoiler(true);
                            actionType = UserActionType.markSpoiler;
                        }
                        currentAdapter.notifyDataSetChanged();
                        task = new LoadUserActionTask(context, post.getFullName(), actionType);
                        task.execute();
                        return true;
                    case R.id.action_edit:
                        intent = new Intent(context, SubmitActivity.class);
                        intent.putExtra("submitType", SubmitType.comment);
                        intent.putExtra("selfText", post.getSelftext());
                        intent.putExtra("edit", true);
                        intent.putExtra("postName", post.getFullName());
                        context.startActivity(intent);
                        return true;
                    case R.id.action_delete:
                        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LoadUserActionTask task = new LoadUserActionTask(context, post.getFullName(), UserActionType.delete);
                                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        };
                        new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.MyAlertDialogStyle)).setMessage("Are you sure you want to delete this post?").setPositiveButton("Yes", listener)
                                .setNegativeButton("No", null).show();
                        return true;
                    case R.id.action_report:
                        if (MyApplication.currentUser != null) {
                            ReportDialogFragment dialog = new ReportDialogFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("postId", post.getFullName());
                            dialog.setArguments(bundle);
                            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
                        } else {
                            showSnackbar("Must be logged in to report");
                        }
                        return true;
                }
                return false;
            }
        });

        // update menu items
        Menu menu = popupMenu.getMenu();
        // check if post is in synced section
        if(MyApplication.offlineModeEnabled && context instanceof MainActivity) {
            PostListFragment fragment = ((MainActivity) context).getListFragment();
            if(fragment.isOther && fragment.subreddit.equals("synced")) {
                menu.findItem(R.id.action_add_to_synced).setTitle("Sync");
            }
        }
        else {
            menu.removeItem(R.id.action_remove_from_synced);
        }

        // update popup menu item visibility depending on post menu bar items
        if(viewType == PostViewType.cards || viewType == PostViewType.cardDetails) {
            if(PostCardViewHolder.viewUserIconVisible) {
                menu.removeItem(R.id.action_view_user);
            }
            if (PostCardViewHolder.shareIconVisible) {
                menu.removeItem(R.id.action_share);
            }
        }
        else if(viewType == PostViewType.classic) {
            menu.removeItem(R.id.action_view_user);
            menu.removeItem(R.id.action_share);
            menu.removeItem(R.id.action_open_in_browser);
        }
        else if(viewType == PostViewType.list || viewType == PostViewType.listReversed) {
            menu.removeItem(R.id.action_view_user);
            if(PostListViewHolder.shareIconVisible) {
                menu.removeItem(R.id.action_share);
            }
            if(PostListViewHolder.openBrowserIconVisible) {
                menu.removeItem(R.id.action_open_in_browser);
            }
        }
        else if(viewType == PostViewType.smallCards) {
            menu.removeItem(R.id.action_view_user);
            if(PostSmallCardViewHolder.shareIconVisible) {
                menu.removeItem(R.id.action_share);
            }
            if(PostSmallCardViewHolder.openBrowserIconVisible) {
                menu.removeItem(R.id.action_open_in_browser);
            }
        }
        //// check if self post (disabled for now)
        //if(!post.isSelf()) {
        //    menu.removeItem(R.id.action_select_text);
        //}
        // check if post belongs to current user
        final String currentUser = (MyApplication.currentUser!=null) ? MyApplication.currentUser.getUsername() : "";
        if(post.getAuthor().equals(currentUser)) {
            menu.removeItem(R.id.action_report);
            if(post.isNSFW()) {
               menu.findItem(R.id.action_mark_nsfw).setTitle("Unmark NSFW");
            }
            if(post.isSpoiler()) {
                menu.findItem(R.id.action_mark_spoiler).setTitle("Unmark SPOILER");
            }
        }
        else {
            menu.removeItem(R.id.action_edit);
            menu.removeItem(R.id.action_delete);
            menu.removeItem(R.id.action_mark_nsfw);
            menu.removeItem(R.id.action_mark_spoiler);
        }

        // show popup menu
        popupMenu.show();
    }

    private void notifyDataSetChangedDelayed() {
        notifyDataSetChangedDelayed(currentAdapter);
    }

    private void notifyDataSetChangedDelayed(final RecyclerView.Adapter adapter) {
        if(adapter!=null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            }, 500);
        }
    }

    private RecyclerView.Adapter getSecondPaneAdapter() {
        RecyclerView.Adapter adapter = null;
        if(currentAdapter instanceof PostAdapter) {
            adapter = getListFragmentAdapter();
        }
        else if(currentAdapter instanceof RedditItemListAdapter)  {
            adapter = getPostFragmentAdapter();
        }
        return adapter;
    }

    private void notifyDataSetChanged(RecyclerView.Adapter adapter) {
        if(adapter!=null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void notifySecondPaneChanges() {
        RecyclerView.Adapter secondPaneAdapter = getSecondPaneAdapter();
        if(secondPaneAdapter!=null) {
            if(secondPaneAdapter instanceof PostAdapter) {
                secondPaneAdapter.notifyItemChanged(0);
            }
            else if(secondPaneAdapter instanceof RedditItemListAdapter) {
                int index = viewHolder.getAdapterPosition();
                RedditItem item = ((RedditItemListAdapter) secondPaneAdapter).getItemAt(index);
                if(item instanceof Submission) {
                    ((Submission) item).setClicked(true);
                    secondPaneAdapter.notifyItemChanged(index);
                }
            }
        }
    }

    private RedditItemListAdapter getListFragmentAdapter() {
        RedditContentFragment fragment = getListFragment();
        if (fragment!=null)
            return fragment.adapter;
        return null;
    }

    private PostAdapter getPostFragmentAdapter() {
        PostFragment fragment = getPostFragment();
        if (fragment!=null)
            return fragment.postAdapter;
        return null;
    }

    private RedditContentFragment getListFragment() {
        // TODO: 3/23/2017 add abstraction
        try {
            if (context instanceof MainActivity) {
                return ((MainActivity) context).getListFragment();
            } else if (context instanceof SubredditActivity) {
                return ((SubredditActivity) context).getListFragment();
            } else if (context instanceof UserActivity) {
                return ((UserActivity) context).getListFragment();
            } else if (context instanceof SearchActivity) {
                return ((SearchActivity) context).getSearchFragment();
            } else if (context instanceof MessageActivity) {
                return ((MessageActivity) context).getMessageFragment();
            }
        } catch (Exception e) {}
        return null;
    }

    private PostFragment getPostFragment() {
        // TODO: 3/23/2017 add abstraction
        try {
            if (context instanceof MainActivity) {
                return ((MainActivity) context).getPostFragment();
            } else if (context instanceof SubredditActivity) {
                return ((SubredditActivity) context).getPostFragment();
            } else if (context instanceof UserActivity) {
                return ((UserActivity) context).getPostFragment();
            } else if (context instanceof SearchActivity) {
                return ((SearchActivity) context).getPostFragment();
            } else if (context instanceof MessageActivity) {
                return ((MessageActivity) context).getPostFragment();
            } else if (context instanceof PostActivity) {
                return ((PostActivity) context).getPostFragment();
            }
        } catch (Exception e) {}
        return null;
    }

    private void showSnackbar(String message) {
        showSnackbar(message, Snackbar.LENGTH_SHORT);
    }

    private void showSnackbar(String message, int duration) {
        if (currentAdapter instanceof RedditItemListAdapter) {
            RedditContentFragment fragment = getListFragment();
            if (fragment!=null)
                fragment.setSnackbar(ToastUtils.showSnackbar(fragment.getSnackbarParentView(), message, duration));
        } else if (currentAdapter instanceof PostAdapter) {
            PostFragment fragment = getPostFragment();
            if (fragment!=null)
                fragment.setSnackbar(ToastUtils.showSnackbar(fragment.getSnackbarParentView(), message, duration));
        }
    }

}
