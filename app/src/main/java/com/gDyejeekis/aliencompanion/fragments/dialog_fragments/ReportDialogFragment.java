package com.gDyejeekis.aliencompanion.fragments.dialog_fragments;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.gDyejeekis.aliencompanion.asynctask.LoadUserActionTask;
import com.gDyejeekis.aliencompanion.asynctask.SaveOfflineActionTask;
import com.gDyejeekis.aliencompanion.models.offline_actions.OfflineUserAction;
import com.gDyejeekis.aliencompanion.models.offline_actions.ReportAction;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;
import com.gDyejeekis.aliencompanion.enums.UserActionType;

/**
 * Created by sound on 10/7/2015.
 */
public class ReportDialogFragment extends ScalableDialogFragment implements View.OnClickListener{

    private EditText otherField;
    private String postId;
    private RadioGroup radioGroup;
    private int hintColor;
    private int textColor;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Resources resources = getActivity().getResources();
        if(MyApplication.nightThemeEnabled) {
            textColor = Color.WHITE;
            hintColor = resources.getColor(R.color.darkHintText);
        }
        else {
            textColor = Color.BLACK;
            hintColor = resources.getColor(R.color.darkHintText);
        }
        postId = getArguments().getString("postId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report_dialog, container, false);
        otherField = (EditText) view.findViewById(R.id.editText_other_reason);
        otherField.setEnabled(false);
        radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup_report_reasons);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radioButton_other) {
                    otherField.setTextColor(textColor);
                    otherField.setEnabled(true);
                }
                else {
                    otherField.setTextColor(hintColor);
                    otherField.setHint("your reason");
                    otherField.setHintTextColor(hintColor);
                    otherField.setEnabled(false);
                }
            }
        });
        Button reportButton = (Button) view.findViewById(R.id.button_report);
        Button cancelButton = (Button) view.findViewById(R.id.button_cancel);
        reportButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_report) {
            String reason = null;
            int selected = radioGroup.getCheckedRadioButtonId();
            switch (selected) {
                case R.id.radioButton_spam:
                case R.id.radioButton_vote_manip:
                case R.id.radioButton_personal_info:
                case R.id.radioButton_minors:
                case R.id.radioButton_broke_reddit:
                    reason = ((RadioButton) getDialog().findViewById(selected)).getText().toString();
                    break;
                case R.id.radioButton_other:
                    reason = otherField.getText().toString();
                    if(reason.length()==0) {
                        reason = null;
                        otherField.setText("");
                        otherField.setHint("enter a reason");
                        otherField.setHintTextColor(Color.RED);
                    }
                    break;
            }
            if(reason!=null) {
                //ToastUtils.showToast(getActivity(), reason);
                if(GeneralUtils.isNetworkAvailable(getActivity())) {
                    LoadUserActionTask task = new LoadUserActionTask(getActivity(), postId, UserActionType.report, reason);
                    task.execute();
                }
                else {
                    OfflineUserAction action = new ReportAction(MyApplication.currentAccount.getUsername(), postId, reason);
                    SaveOfflineActionTask task = new SaveOfflineActionTask(getActivity(), action);
                    task.execute();
                }
            }
            else if(selected!=R.id.radioButton_other) ToastUtils.showToast(getActivity(), "Please select a reason");
        }
        else dismiss();
    }
}
