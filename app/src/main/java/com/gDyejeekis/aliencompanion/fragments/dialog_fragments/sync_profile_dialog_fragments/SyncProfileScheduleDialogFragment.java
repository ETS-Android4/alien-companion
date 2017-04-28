package com.gDyejeekis.aliencompanion.fragments.dialog_fragments.sync_profile_dialog_fragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.activities.EditSyncProfileActivity;
import com.gDyejeekis.aliencompanion.activities.SyncProfilesActivity;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.ScalableDialogFragment;
import com.gDyejeekis.aliencompanion.models.SyncProfile;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.DaysEnum;
import com.gDyejeekis.aliencompanion.models.SyncSchedule;
import com.gDyejeekis.aliencompanion.utils.ToastUtils;

/**
 * Created by sound on 2/10/2016.
 */
public class SyncProfileScheduleDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    private static final String numbers[] = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
    private static final String periods[] = {"AM", "PM"};

    private static final int activeDayColor = Color.parseColor("#66ff66");

    public static final int inactiveDayColor = Color.parseColor("#d9d9d9");

    private int dropdownResource = (MyApplication.nightThemeEnabled) ? R.layout.spinner_dropdown_item_dark : R.layout.spinner_dropdown_item_light;

    private SyncProfile profile;
    private SyncSchedule schedule;
    private Spinner from1;
    private Spinner from2;
    private Spinner to1;
    private Spinner to2;
    private Button button_mon;
    private Button button_tue;
    private Button button_wed;
    private Button button_thu;
    private Button button_fri;
    private Button button_sat;
    private Button button_sun;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        profile = (SyncProfile) getArguments().getSerializable("profile");
        schedule = new SyncSchedule();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sync_profile_schedule, container, false);

        Button btnAdd = (Button) view.findViewById(R.id.button_add_schedule);
        Button btnCancel = (Button) view.findViewById(R.id.button_cancel);
        btnAdd.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        TextView title = (TextView) view.findViewById(R.id.textView_title);
        title.setText(profile.getName());

        from1 = (Spinner) view.findViewById(R.id.spinner_from1);
        from2 = (Spinner) view.findViewById(R.id.spinner_from2);
        to1 = (Spinner) view.findViewById(R.id.spinner_to1);
        to2 = (Spinner) view.findViewById(R.id.spinner_to2);
        ArrayAdapter<String> numbersAdapter = new ArrayAdapter<String>(getActivity(), dropdownResource, numbers);
        ArrayAdapter<String> periodsAdapter = new ArrayAdapter<String>(getActivity(), dropdownResource, periods);
        from1.setAdapter(numbersAdapter);
        to1.setAdapter(numbersAdapter);
        from2.setAdapter(periodsAdapter);
        to2.setAdapter(periodsAdapter);

        button_mon = (Button) view.findViewById(R.id.button_mon);
        button_tue = (Button) view.findViewById(R.id.button_tue);
        button_wed = (Button) view.findViewById(R.id.button_wed);
        button_thu = (Button) view.findViewById(R.id.button_thu);
        button_fri = (Button) view.findViewById(R.id.button_fri);
        button_sat = (Button) view.findViewById(R.id.button_sat);
        button_sun = (Button) view.findViewById(R.id.button_sun);
        button_mon.setOnClickListener(this);
        button_tue.setOnClickListener(this);
        button_wed.setOnClickListener(this);
        button_thu.setOnClickListener(this);
        button_fri.setOnClickListener(this);
        button_sat.setOnClickListener(this);
        button_sun.setOnClickListener(this);

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_mon:
                button_mon.setBackgroundColor((schedule.toggleActiveDay(DaysEnum.MONDAY))? activeDayColor : inactiveDayColor);
                break;
            case R.id.button_tue:
                button_tue.setBackgroundColor((schedule.toggleActiveDay(DaysEnum.TUESDAY)) ? activeDayColor : inactiveDayColor);
                break;
            case R.id.button_wed:
                button_wed.setBackgroundColor((schedule.toggleActiveDay(DaysEnum.WEDNESDAY)) ? activeDayColor : inactiveDayColor);
                break;
            case R.id.button_thu:
                button_thu.setBackgroundColor((schedule.toggleActiveDay(DaysEnum.THURSDAY)) ? activeDayColor : inactiveDayColor);
                break;
            case R.id.button_fri:
                button_fri.setBackgroundColor((schedule.toggleActiveDay(DaysEnum.FRIDAY)) ? activeDayColor : inactiveDayColor);
                break;
            case R.id.button_sat:
                button_sat.setBackgroundColor((schedule.toggleActiveDay(DaysEnum.SATURDAY)) ? activeDayColor : inactiveDayColor);
                break;
            case R.id.button_sun:
                button_sun.setBackgroundColor((schedule.toggleActiveDay(DaysEnum.SUNDAY)) ? activeDayColor : inactiveDayColor);
                break;
            case R.id.button_cancel:
                dismiss();
                break;
            case R.id.button_add_schedule:
                addSchedule();
                break;
        }
    }

    private void addSchedule() {
        if(schedule.getDays().isEmpty()) {
            ToastUtils.showToast(getActivity(), "Must select at least one day");
        }
        else {
            setScheduleTimes();
            ((EditSyncProfileActivity) getActivity()).addSchedule(schedule);
        }
    }

    private void setScheduleTimes() {
        int fromTime = Integer.valueOf(from1.getSelectedItem().toString());
        int toTime = Integer.valueOf(to1.getSelectedItem().toString());
        if(from2.getSelectedItemPosition()==0) {
            if(fromTime == 12) {
                fromTime = 24;
            }
        }
        else {
            if(fromTime != 12) {
                fromTime = fromTime + 12;
            }
        }

        if(to2.getSelectedItemPosition()==0) {
            if(toTime == 12) {
                toTime = 24;
            }
        }
        else {
            if(toTime != 12) {
                toTime = toTime + 12;
            }
        }
        schedule.setStartTime(fromTime);
        schedule.setEndTime(toTime);
    }
}
