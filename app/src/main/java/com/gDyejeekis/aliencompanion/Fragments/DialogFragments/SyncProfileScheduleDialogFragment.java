package com.gDyejeekis.aliencompanion.Fragments.DialogFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.Models.SyncProfile;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;

/**
 * Created by sound on 2/10/2016.
 */
public class SyncProfileScheduleDialogFragment extends ScalableDialogFragment implements View.OnClickListener {

    private final String numbers[] = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
    private final String periods[] = {"AM", "PM"};

    private SyncProfile profile;
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
        ArrayAdapter<String> numbersAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, numbers);
        ArrayAdapter<String> periodsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, periods);
        from1.setAdapter(numbersAdapter);
        to1.setAdapter(numbersAdapter);
        from2.setAdapter(periodsAdapter);
        to2.setAdapter(periodsAdapter);

        button_mon = (Button) view.findViewById(R.id.button_mon);
        button_tue = (Button) view.findViewById(R.id.button_tue);
        button_tue = (Button) view.findViewById(R.id.button_tue);
        button_tue = (Button) view.findViewById(R.id.button_tue);
        button_tue = (Button) view.findViewById(R.id.button_tue);
        button_tue = (Button) view.findViewById(R.id.button_tue);
        button_tue = (Button) view.findViewById(R.id.button_tue);

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
        if(view.getId() == R.id.button_cancel) {

        }
        else {

        }
        dismiss();
    }
}
