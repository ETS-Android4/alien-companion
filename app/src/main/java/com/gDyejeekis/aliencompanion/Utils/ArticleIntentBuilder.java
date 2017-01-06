package com.gDyejeekis.aliencompanion.Utils;

import android.content.Context;
import android.content.Intent;

import com.gDyejeekis.aliencompanion.Activities.ArticleActivityExtended;

import xyz.klinker.android.article.ArticleIntent;

import static xyz.klinker.android.article.ArticleIntent.EXTRA_API_TOKEN;

/**
 * Created by George on 1/6/2017.
 */

public class ArticleIntentBuilder extends ArticleIntent.Builder {

    public ArticleIntentBuilder(Context context, String apiToken) {
        mIntent = new Intent(context, ArticleActivityExtended.class);
        mIntent.putExtra(EXTRA_API_TOKEN, apiToken);
    }
}
