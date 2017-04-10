package com.gDyejeekis.aliencompanion.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by George on 4/10/2017.
 */

public class SyncQueueActivity extends ToolbarActivity {

    public static final String TAG = "SyncQueueActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_queue);
        initToolbar();
        // TODO: 4/10/2017
    }

}
