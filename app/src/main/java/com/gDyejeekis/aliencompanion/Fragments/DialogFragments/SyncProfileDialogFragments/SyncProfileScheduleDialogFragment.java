package com.gDyejeekis.aliencompanion.Fragments.DialogFragments.SyncProfileDialogFragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.Activities.SyncProfilesActivity;
import com.gDyejeekis.aliencompanion.Fragments.DialogFragments.ScalableDialogFragment;
import com.gDyejeekis.aliencompanion.Models.SyncProfile;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.enums.DaysEnum;

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
    private String oldDaysString;
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

    //private Button button_cancel;
    //private Button button_done;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        profile = (SyncProfile) getArguments().getSerializable("profile");
        oldDaysString = new String(profile.getDaysString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sync_profile_schedule, container, false);

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
        if(profile.hasTime()) {
            if(profile.getFromTime()==12) {
                from1.setSelection(11);
                from2.setSelection(1);
            }
            else if (profile.getFromTime()==24) {
                from1.setSelection(11);
                from2.setSelection(0);
            }
            else if(profile.getFromTime() > 12) {
                from1.setSelection(profile.getFromTime() - 13);
                from2.setSelection(1);
            }
            else {
                from1.setSelection(profile.getFromTime() - 1);
                from2.setSelection(0);
            }

            if(profile.getToTime()==12) {
                to1.setSelection(11);
                to2.setSelection(1);
            }
            else if (profile.getToTime()==24) {
                to1.setSelection(11);
                to2.setSelection(0);
            }
            else if(profile.getToTime() > 12) {
                to1.setSelection(profile.getToTime() - 13);
                to2.setSelection(1);
            }
            else {
                to1.setSelection(profile.getToTime() - 1);
                to2.setSelection(0);
            }
        }

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
        button_mon.setBackgroundColor((profile.isActiveDay(DaysEnum.MONDAY))? activeDayColor : inactiveDayColor);
        button_tue.setBackgroundColor((profile.isActiveDay(DaysEnum.TUESDAY))? activeDayColor : inactiveDayColor);
        button_wed.setBackgroundColor((profile.isActiveDay(DaysEnum.WEDNESDAY))? activeDayColor : inactiveDayColor);
        button_thu.setBackgroundColor((profile.isActiveDay(DaysEnum.THURSDAY))? activeDayColor : inactiveDayColor);
        button_fri.setBackgroundColor((profile.isActiveDay(DaysEnum.FRIDAY))? activeDayColor : inactiveDayColor);
        button_sat.setBackgroundColor((profile.isActiveDay(DaysEnum.SATURDAY))? activeDayColor : inactiveDayColor);
        button_sun.setBackgroundColor((profile.isActiveDay(DaysEnum.SUNDAY))? activeDayColor : inactiveDayColor);

        Button button_cancel = (Button) view.findViewById(R.id.button_cancel);
        Button button_done = (Button) view.findViewById(R.id.button_done);
        button_cancel.setOnClickListener(this);
        button_done.setOnClickListener(this);

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_mon:
                button_mon.setBackgroundColor((profile.toggleActiveDay(DaysEnum.MONDAY))? activeDayColor : inactiveDayColor);
                break;
            case R.id.button_tue:
                button_tue.setBackgroundColor((profile.toggleActiveDay(DaysEnum.TUESDAY)) ? activeDayColor : inactiveDayColor);
                break;
            case R.id.button_wed:
                button_wed.setBackgroundColor((profile.toggleActiveDay(DaysEnum.WEDNESDAY)) ? activeDayColor : inactiveDayColor);
                break;
            case R.id.button_thu:
                button_thu.setBackgroundColor((profile.toggleActiveDay(DaysEnum.THURSDAY)) ? activeDayColor : inactiveDayColor);
                break;
            case R.id.button_fri:
                button_fri.setBackgroundColor((profile.toggleActiveDay(DaysEnum.FRIDAY)) ? activeDayColor : inactiveDayColor);
                break;
            case R.id.button_sat:
                button_sat.setBackgroundColor((profile.toggleActiveDay(DaysEnum.SATURDAY)) ? activeDayColor : inactiveDayColor);
                break;
            case R.id.button_sun:
                button_sun.setBackgroundColor((profile.toggleActiveDay(DaysEnum.SUNDAY)) ? activeDayColor : inactiveDayColor);
                break;
            case R.id.button_cancel:
                profile.setDaysString(oldDaysString);
                dismiss();
                break;
            case R.id.button_done:
                ((SyncProfilesActivity) getActivity()).changesMade = true;
                setProfileTimes();
                dismiss();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(android.content.DialogInterface dialog, int keyCode, android.view.KeyEvent event) {

                if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                    ((SyncProfilesActivity) getActivity()).changesMade = true;
                    setProfileTimes();
                    dismiss();
                    return true; // pretend we've processed it
                } else
                    return false; // pass on to be processed as normal
            }
        });
    }

    private void setProfileTimes() {
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
        profile.setFromTime(fromTime);
        profile.setToTime(toTime);
        profile.setHasTime(true);
        //Log.d("schedule test", "from time: " + fromTime);
        //Log.d("schedule test", "to time: " + toTime);
    }
}
