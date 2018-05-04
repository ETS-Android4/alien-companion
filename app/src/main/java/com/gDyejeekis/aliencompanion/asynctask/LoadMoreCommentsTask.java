package com.gDyejeekis.aliencompanion.asynctask;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import com.gDyejeekis.aliencompanion.activities.MainActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.views.adapters.PostAdapter;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.models.MoreComment;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.retrieval.Comments;
import com.gDyejeekis.aliencompanion.api.utils.httpClient.PoliteRedditHttpClient;
import com.gDyejeekis.aliencompanion.views.multilevelexpindlistview.MultiLevelExpIndListAdapter;

import java.util.List;

/**
 * Created by George on 7/28/2016.
 */
public class LoadMoreCommentsTask extends AsyncTask<Void, Void, List<Comment>> {

    private Exception exception;
    private PostFragment postFragment;
    private MoreComment moreChildren;
    private int addIndex;

    public LoadMoreCommentsTask(AppCompatActivity activity, MoreComment moreChildren) {
        this.moreChildren = moreChildren;
        if(activity instanceof MainActivity) {
            postFragment = ((MainActivity)activity).getPostFragment();
        }
        else {
            postFragment = ((PostActivity) activity).getPostFragment();
        }
    }

    @Override
    protected List<Comment> doInBackground(Void... params) {
        try {
            Comments comments = new Comments(new PoliteRedditHttpClient(), MyApplication.currentUser);
            List<Comment> commentList = comments.moreChildren(postFragment.post.getFullName(), moreChildren.getMoreCommentIds(),
                    postFragment.commentSort);
            commentList = processRetrievedComments(commentList);
            return commentList;
        } catch (Exception e) {
            exception = e;
            e.printStackTrace();
        }
        return null;
    }

    private List<Comment> processRetrievedComments(List<Comment> comments) {
        if(comments.size() > 0) {
            String parentName = moreChildren.getParentId();
            List<MultiLevelExpIndListAdapter.ExpIndData> adapterData = postFragment.postAdapter.getData();
            // check if additional comments reply to link
            if(parentName.startsWith("t3")) {
                // nest and indent comments as needed
                //List<Comment> toRemove = new ArrayList<>();
                for(Comment c : comments) {
                    if(!c.getParentId().startsWith("t3")) {
                        for(Comment parent : comments) {
                            if(c.getParentId().equals(parent.getFullName())) {
                                c.setIndentation(parent.getIndentation()+1);
                                parent.addChild(c);
                                //toRemove.add(c);
                                break;
                            }
                        }
                    }
                }
                //for(Comment comment : toRemove) {
                //    comments.remove(comment);
                //}
                addIndex = adapterData.size()-1;
            }
            else {
                // otherwise find the parent of the additional comments
                Comment parentComment = null;
                for (MultiLevelExpIndListAdapter.ExpIndData item : adapterData) {
                    if (item.getClass() == Comment.class) {
                        Comment c = (Comment) item;
                        if (c.getFullName().equals(parentName)) {
                            parentComment = c;
                            break;
                        }
                    }
                }

                // if parent is found nest and indent comments as needed
                if (parentComment != null) {
                    for (Comment c : comments) {
                        c.setIndentation(parentComment.getIndentation() + 1);
                    }
                    parentComment.getChildren().remove(moreChildren);
                    parentComment.addChildren(comments);
                }
                else {
                    throw new RuntimeException("Parent comment not found for retrieved comments");
                }
                addIndex = adapterData.indexOf(moreChildren);
            }
        }
        return comments;
    }

    @Override
    protected void onPostExecute(List<Comment> comments) {
        moreChildren.setLoadingMore(false);
        PostAdapter postAdapter = postFragment.postAdapter;
        if (comments==null || comments.size()==0) {
            ToastUtils.showSnackbar(postFragment.getSnackbarParentView(), exception!=null ? "Error loading comments" : "Replies not found");
            int index = postAdapter.getData().indexOf(moreChildren);
            postAdapter.notifyItemChanged(index);
        }
        else {
            postAdapter.toggleMenuBar(PostAdapter.NO_POSITION);
            postAdapter.remove(moreChildren);
            postAdapter.addAll(addIndex, comments);
            postFragment.updateFabNavOnScroll(0);
        }
    }

}
