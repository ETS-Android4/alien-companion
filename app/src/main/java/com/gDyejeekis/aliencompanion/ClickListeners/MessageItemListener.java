package com.gDyejeekis.aliencompanion.ClickListeners;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.gDyejeekis.aliencompanion.Activities.MainActivity;
import com.gDyejeekis.aliencompanion.Activities.MessageActivity;
import com.gDyejeekis.aliencompanion.Activities.PostActivity;
import com.gDyejeekis.aliencompanion.Activities.SubmitActivity;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.ReportDialogFragment;
import com.gDyejeekis.aliencompanion.Fragments.PostFragment;
import com.gDyejeekis.aliencompanion.Utils.LinkHandler;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;
import com.gDyejeekis.aliencompanion.api.entity.Comment;
import com.gDyejeekis.aliencompanion.api.entity.Message;
import com.gDyejeekis.aliencompanion.enums.SubmitType;

/**
 * Created by sound on 10/12/2015.
 */
public class MessageItemListener implements View.OnClickListener, View.OnLongClickListener {

    private Context context;
    private Message message;

    public MessageItemListener(Context context, Message message) {
        this.context = context;
        this.message = message;
    }

    @Override
    public void onClick(View v) {
        if(message.wasComment) {
            String[] postInfo = LinkHandler.getRedditPostInfo(message.context);
            if(MainActivity.dualPaneActive) {
                PostFragment fragment = PostFragment.newInstance(postInfo);
                ((MessageActivity) context).setupPostFragment(fragment);
            }
            else {
                Intent intent = new Intent(context, PostActivity.class);
                intent.putExtra("postInfo", postInfo);
                context.startActivity(intent);
            }
        }
        else {
            //TODO: implement show message reply screen
        }
    }

    @Override
    public boolean onLongClick(View v) {
        showMessageOptionsPopup(v);
        return true;
    }

    private void showMessageOptionsPopup(View v) {
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
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Message body", message.body); //TODO: escape markdown/HTML foramtting (maybe)
                        clipboard.setPrimaryClip(clip);
                        return true;
                    case R.id.action_block_user:
                        //TODO: implement this
                        ToastUtils.displayShortToast(context, "Coming soon!");
                        return true;
                    case R.id.action_report:
                        ReportDialogFragment dialog = new ReportDialogFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("postId", message.getFullName());
                        dialog.setArguments(bundle);
                        dialog.show(((Activity) context).getFragmentManager(), "dialog");
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }
}
