package com.gDyejeekis.aliencompanion.Activities;

import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by sound on 3/4/2016.
 */
public class ImageActivity extends BackNavActivity {

    public static final int TYPE_IMAGE = 1;

    public static final int TYPE_GIF = 2;

    public static final int TYPE_ALBUM = 3;

    private String url;

    private String domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setBackgroundColor(Color.parseColor("#00000000"));
        toolbar.setTitle("");
        toolbar.setNavigationIcon(R.mipmap.ic_close_white_24dp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupFragments() {
        url = getIntent().getStringExtra("url");
        domain = getIntent().getStringExtra("domain");
        FragmentManager fragmentManager = getFragmentManager();

        if(domain.equals("imgur.com")) {

        }
        else if(domain.equals("gfycat.com")) {

        }
        else if(url.endsWith(".png") || url.endsWith(".jpg")) {

        }
        else if(url.endsWith(".gifv") || url.endsWith(".gif")) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return true;
    }

    //@Override
    //public boolean onOptionsItemSelected(MenuItem item) {
    //    switch (item.getItemId()) {
    //        case R.id.action_save:
    //            return true;
    //        case R.id.action_share:
    //            return true;
    //        default:
    //            return super.onOptionsItemSelected(item);
    //    }
    //}
}
