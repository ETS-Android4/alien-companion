package com.george.redditreader;

import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.style.UpdateAppearance;
import android.view.View;

/**
 * Created by George on 8/16/2015.
 */
public abstract class MyClickableSpan extends ClickableSpan {

    public abstract boolean onLongClick(View widget);
}
