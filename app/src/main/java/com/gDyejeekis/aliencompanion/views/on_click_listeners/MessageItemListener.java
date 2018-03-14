package com.gDyejeekis.aliencompanion.views.on_click_listeners;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.activities.MessageActivity;
import com.gDyejeekis.aliencompanion.activities.PostActivity;
import com.gDyejeekis.aliencompanion.activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.ReportDialogFragment;
import com.gDyejeekis.aliencompanion.fragments.PostFragment;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Message;
import com.gDyejeekis.aliencompanion.enums.SubmitType;
import com.gDyejeekis.aliencompanion.views.adapters.RedditItemListAdapter;

/**
 * Created by sound on 10/12/2015.
 */
public class MessageItemListener implements View.OnClickListener, View.OnLongClickListener {

    private Context context;
    private RecyclerView.ViewHolder viewHolder;
    private RedditItemListAdapter adapter;

    public MessageItemListener(Context context, RecyclerView.ViewHolder viewHolder, RedditItemListAdapter adapter) {
        this.context = context;
        this.viewHolder = viewHolder;
        this.adapter = adapter;
    }

    @Override
    public void onClick(View v) {
        Message message = (Message) adapter.getItemAt(viewHolder.getAdapterPosition());
        if (message.wasComment) {
            if (MyApplication.dualPaneActive) {
                PostFragment fragment = PostFragment.newInstance(message.context);
                ((MessageActivity) context).setupPostFragment(fragment);
            } else {
                Intent intent = new Intent(context, PostActivity.class);
                intent.putExtra("url", message.context);
                context.startActivity(intent);
            }
        } else {
            //TODO: implement show message reply screen
        }
    }

    @Override
    public boolean onLongClick(View v) {
        Message message = (Message) adapter.getItemAt(viewHolder.getAdapterPosition());
        showMessageOptionsPopup(v, message);
        return true;
    }

    private void showMessageOptionsPopup(View v, final Message message) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        final int resource = (message.wasComment) ? R.menu.menu_message_comment_options : R.menu.menu_message_options;
        popupMenu.inflate(resource);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_reply:
                        Intent intent;
                        if (message.wasComment) {
                            intent = new Intent(context, SubmitActivity.class);
                            intent.putExtra("submitType", SubmitType.comment);
                            intent.putExtra("originalComment", new Comment(message));
                            context.startActivity(intent);
                        } else {
                            intent = new Intent(context, SubmitActivity.class);
                            intent.putExtra("submitType", SubmitType.message);
                            intent.putExtra("recipient", message.author);
                            intent.putExtra("subject", message.subject);
                            context.startActivity(intent);
                        }
                        return true;
                    case R.id.action_copy_text:
                        GeneralUtils.copyTextToClipboard(context, "Message body", message.body);
                        return true;
                    case R.id.action_block_user:
                        //TODO: implement this
                        ToastUtils.showToast(context, "Coming soon!");
                        return true;
                    case R.id.action_report:
                        ReportDialogFragment dialog = new ReportDialogFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("postId", message.getFullName());
                        dialog.setArguments(bundle);
                        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }
}
