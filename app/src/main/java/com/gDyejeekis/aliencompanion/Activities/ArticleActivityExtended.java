package com.gDyejeekis.aliencompanion.Activities;

import com.gDyejeekis.aliencompanion.Utils.LinkHandler;

import xyz.klinker.android.article.ArticleActivity;

/**
 * Created by George on 1/6/2017.
 */

public class ArticleActivityExtended extends ArticleActivity {
    @Override
    protected void openChromeCustomTab() {
        LinkHandler.startInAppBrowser(this, null, url, null);

        finish();
    }
}
