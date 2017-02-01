package com.gDyejeekis.aliencompanion.utils;

import android.os.Handler;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by George on 8/16/2015.
 */
public class MyLinkMovementMethod extends LinkMovementMethod {

    public static final String TAG = "MyLinkMovementMethod";

    private static MyLinkMovementMethod sInstance;

    private static final int MAX_CLICK_DURATION = 250;

    private static final int LONG_CLICK_DURATION = 500;

    private static final float CANCEL_LONG_CLICK_THRESHOLD_FACTOR = 0.05f; //decrease this value for a more strict threshold

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

            //if(off >= widget.getText().length()) {
            //    handler.removeCallbacks(mLongPressed);
            //    return true;
            //}

            final MyClickableSpan[] link = buffer.getSpans(off, off, MyClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    //Log.d(TAG, "ACTION_UP");
                    if (System.currentTimeMillis() - lastClickTime < MAX_CLICK_DURATION) {
                        link[0].onClick(widget);
                    }
                    handler.removeCallbacks(mLongPressed);
                }
                else if(action == MotionEvent.ACTION_MOVE) {
                    //Log.d(TAG, "ACTION_MOVE");
                    DisplayMetrics metrics = widget.getContext().getResources().getDisplayMetrics();
                    int thresholdX = Math.round(metrics.widthPixels * CANCEL_LONG_CLICK_THRESHOLD_FACTOR);
                    int thresholdY = Math.round(metrics.heightPixels * CANCEL_LONG_CLICK_THRESHOLD_FACTOR);
                    //Log.d(TAG, "thresholdX: " + thresholdX);
                    //Log.d(TAG, "thresholdY: " + thresholdY);
                    if(deltaX > thresholdX || deltaY > thresholdY) {
                        //Log.d(TAG, "REMOVING LONG PRESS CALLBACK");
                        handler.removeCallbacks(mLongPressed);
                    }
                }
                else if (action == MotionEvent.ACTION_DOWN) {
                    //Log.d(TAG, "--------------------------------------------------");
                    //Log.d(TAG, "ACTION_DOWN");
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
                    handler.postDelayed(mLongPressed, LONG_CLICK_DURATION);
                }

                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        }

        //Log.d(TAG, "REMOVING LONG PRESS CALLBACK ANYWAY");
        handler.removeCallbacks(mLongPressed);
        return super.onTouchEvent(widget, buffer, event);
    }

    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new MyLinkMovementMethod();

        return sInstance;
    }

}
