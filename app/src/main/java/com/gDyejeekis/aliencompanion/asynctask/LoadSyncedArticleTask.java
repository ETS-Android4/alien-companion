package com.gDyejeekis.aliencompanion.asynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;

import com.gDyejeekis.aliencompanion.AppConstants;
import com.gDyejeekis.aliencompanion.fragments.ArticleFragment;
import com.gDyejeekis.aliencompanion.models.Article;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.StorageUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

import java.io.File;

/**
 * Created by George on 6/8/2017.
 */

public class LoadSyncedArticleTask extends AsyncTask<Void, Void, Void> {

    private ArticleFragment fragment;
    private Exception exception;
    private Article article;
    private Bitmap bitmap;

    public LoadSyncedArticleTask(ArticleFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            File parentDir = GeneralUtils.getSyncedArticlesDir(fragment.getActivity());
            final String articleId = String.valueOf(fragment.url.hashCode());
            File articleFile = StorageUtils.findFile(parentDir, parentDir.getAbsolutePath(), articleId + AppConstants.SYNCED_ARTICLE_DATA_SUFFIX);
            article = (Article) GeneralUtils.readObjectFromFile(articleFile);
            try {
                File imageFile = StorageUtils.findFile(parentDir, parentDir.getAbsolutePath(), articleId + AppConstants.SYNCED_ARTICLE_IMAGE_SUFFIX);
                bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            } catch (Exception e) {
                //e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        fragment.progressBar.setVisibility(View.GONE);
        if (exception == null) {
            fragment.articleLayout.setVisibility(View.VISIBLE);
            fragment.title.setText(article.getTitle());
            fragment.body.setText(article.getBody());
            if (bitmap == null) {
                fragment.image.setVisibility(View.GONE);
            } else {
                fragment.image.setVisibility(View.VISIBLE);
                fragment.image.setImageBitmap(bitmap);
            }
        } else {
            fragment.articleLayout.setVisibility(View.GONE);
            ToastUtils.showToast(fragment.getActivity(), "Error loading article");
        }
    }
}
