package com.gDyejeekis.aliencompanion.Fragments.DialogFragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.Services.DownloaderService;
import com.gDyejeekis.aliencompanion.Utils.ToastUtils;

/**
 * Created by George on 10/2/2016.
 */

public class AddToSyncedDialogFragment extends ScalableDialogFragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    public static final String TAG = "AddToSyncedDialog";

    private int count;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_to_synced, container, false);

        Button addToSynced = (Button) view.findViewById(R.id.button_add_to_synced);
        addToSynced.setOnClickListener(this);

        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup_post_count);
        radioGroup.setOnCheckedChangeListener(this);

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onClick(View v) {
        if(count != 0) {
            dismiss();
            Intent intent = new Intent(getContext(), DownloaderService.class);
            intent.putExtra("savedCount", count);
            getContext().startService(intent);
        }
        else {
            ToastUtils.displayShortToast(getContext(), "Select post count");
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radioButton_sync_10:
                count = 10;
                break;
            case R.id.radioButton_sync_25:
                count = 25;
                break;
            case R.id.radioButton_sync_50:
                count = 50;
                break;
            case R.id.radioButton_sync_75:
                count = 75;
                break;
            case R.id.radioButton_sync_100:
                count = 100;
                break;
        }
    }
}
