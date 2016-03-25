package com.gDyejeekis.aliencompanion.Utils;

import android.os.Handler;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by George on 8/16/2015.
 */
public class MyLinkMovementMethod extends LinkMovementMethod {

    public static final String TAG = "MyLinkMovementMethod";

    private static MyLinkMovementMethod sInstance;

    private Long lastClickTime = 0l;

    private final Handler handler = new Handler();
    private Runnable mLongPressed;

    private int startX;
    private int startY;
    //private int deltaX;
    //private int deltaY;
    //private int lastX = 0;
    //private int lastY = 0;

    @Override
    public boolean onTouchEvent(final TextView widget, Spannable buffer,
                                MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            int deltaX = Math.abs(x-startX);
            int deltaY = Math.abs(y-startY);

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            final int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            final MyClickableSpan[] link = buffer.getSpans(off, off, MyClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    Log.d(TAG, "ACTION_UP");
                    if (System.currentTimeMillis() - lastClickTime < 600) {
                        link[0].onClick(widget);
                        handler.removeCallbacks(mLongPressed);
                    }
                }
                else if(action == MotionEvent.ACTION_MOVE) {
                    Log.d(TAG, "ACTION_MOVE");
                    if(deltaX > 10 || deltaY > 10) {
                        Log.d(TAG, "REMOVING LONG PRESS CALLBACK");
                        handler.removeCallbacks(mLongPressed);
                    }
                }
                else if (action == MotionEvent.ACTION_DOWN) {
                    Log.d(TAG, "ACTION_DOWN");
                    startX = x;
                    startY = y;

                    Selection.setSelection(buffer,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]));
                    lastClickTime = System.currentTimeMillis();
                    mLongPressed = new Runnable() {
                        @Override
                        public void run() {
                            link[0].onLongClick(widget);
                        }
                    };
                    handler.postDelayed(mLongPressed, 600);
                }

                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new MyLinkMovementMethod();

        return sInstance;
    }

}
