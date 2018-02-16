package com.gDyejeekis.aliencompanion.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gDyejeekis.aliencompanion.BuildConfig;
import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.InfoDialogFragment;
import com.gDyejeekis.aliencompanion.models.Donation;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.views.adapters.DonationListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by George on 1/22/2018.
 */

public class DonateActivity extends ToolbarActivity implements View.OnClickListener {

    public static final String TAG = "DonateActivity";

    public static final float[] DONATION_AMOUNTS = {0.99f, 1.99f, 2.99f, 3.99f, 4.99f, 5.99f, 6.99f, 7.99f, 8.99f, 9.99f};

    public static final String PROD_DONATIONS_DB_NODE = "donations";
    public static final String TEST_DONATIONS_DB_NODE = "donations-test";

    public static final String DONATION_FAILED_MESSAGE = "There was an error processing your donation (you have not been charged)";
    public static final String THANK_YOU_MESSAGE = "Donation received! Thanks for your support :)";

    private EditText nameField;
    private EditText messageField;
    private TextView amountText;
    private CheckBox makePublic;
    private LinearLayout layoutPastDonations;
    private ListView donationsListView;
    private int amountIndex;
    private ImageView decrAmountBtn;
    private ImageView incrAmountBtn;

    private DatabaseReference database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MyApplication.applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);
        initToolbar();
        initFields();
        initDonationForm();
        refreshDonationsList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_donate, menu);
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        MyApplication.setPendingTransitions(this);
    }

    private void initFields() {
        nameField = findViewById(R.id.editText_donate_name);
        messageField = findViewById(R.id.editText_donate_message);
        amountText = findViewById(R.id.textView_donate_amount);
        makePublic = findViewById(R.id.checkBox_donate_public);
        layoutPastDonations = findViewById(R.id.layout_past_donations);
        donationsListView = findViewById(R.id.listView_donations);
        decrAmountBtn = findViewById(R.id.imageView_donate_decrease_amount);
        incrAmountBtn = findViewById(R.id.imageView_donate_increase_amount);
        decrAmountBtn.setOnClickListener(this);
        incrAmountBtn.setOnClickListener(this);
        Button donateButton = findViewById(R.id.button_donate);
        donateButton.setOnClickListener(this);
        String currentNode = BuildConfig.DEBUG ? TEST_DONATIONS_DB_NODE : PROD_DONATIONS_DB_NODE;
        database = FirebaseDatabase.getInstance().getReference(currentNode);
    }

    private void updateModifyAmountButtons() {
        styleModifyAmountButton(decrAmountBtn);
        styleModifyAmountButton(incrAmountBtn);
    }

    private void styleModifyAmountButton(ImageView imageView) {
        boolean increase = imageView.getId()==R.id.imageView_donate_increase_amount;
        boolean enabled = increase ? amountIndex < DONATION_AMOUNTS.length-1 : amountIndex > 0;
        int drawable;
        float alpha;
        switch (MyApplication.currentBaseTheme) {
            case MyApplication.LIGHT_THEME:
                drawable = increase ? R.drawable.ic_add_circle_outline_black_24dp : R.drawable.ic_remove_circle_outline_black_24dp;
                alpha = enabled ? 0.54f : 0.27f;
                break;
            case MyApplication.DARK_THEME_LOW_CONTRAST:
                drawable = increase ? R.drawable.ic_add_circle_outline_white_24dp : R.drawable.ic_remove_circle_outline_white_24dp;
                alpha = enabled ? 0.6f : 0.3f;
                break;
            default:
                drawable = increase ? R.drawable.ic_add_circle_outline_white_24dp : R.drawable.ic_remove_circle_outline_white_24dp;
                alpha = enabled ? 1f : 0.5f;
                break;
        }
        imageView.setImageResource(drawable);
        imageView.setAlpha(alpha);
    }

    private void initDonationForm() {
        amountIndex = 1;
        nameField.setText("");
        messageField.setText("");
        amountText.setText(String.valueOf(DONATION_AMOUNTS[amountIndex]));
        makePublic.setChecked(true);
        updateModifyAmountButtons();
    }

    private void makeDonation(Donation donation) {
        // TODO: 1/25/2018 google play transaction
        GeneralUtils.hideSoftKeyboard(this);
        initDonationForm();
        writeDonationToDatabase(donation);
        refreshDonationsList();
    }

    private void writeDonationToDatabase(Donation donation) {
        String donationId = database.push().getKey(); // TODO: 2/15/2018 look into getting a different key for id, maybe from google play transaction
        database.child(donationId).setValue(donation);
    }

    private void refreshDonationsList() {
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Donation> donations = parseDonationData(dataSnapshot);
                setPastDonations(donations);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                Log.e(TAG, databaseError.getDetails());
            }
        });
    }

    private List<Donation> parseDonationData(DataSnapshot dataSnapshot) {
        List<Donation> donations = new ArrayList<>();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            Donation donation = ds.getValue(Donation.class);
            if (donation!=null && donation.showToPublic())
                donations.add(donation);
        }
        Collections.sort(donations, new Comparator<Donation>() {
            @Override
            public int compare(Donation d1, Donation d2) {
                if (d1.createdAt == d2.createdAt)
                    return 0;
                return d1.createdAt > d2.createdAt ? -1 : 1;
            }
        });
        return donations;
    }

    private void setPastDonations(List<Donation> donations) {
        if (donations == null || donations.isEmpty()) {
            layoutPastDonations.setVisibility(View.GONE);
        } else {
            layoutPastDonations.setVisibility(View.VISIBLE);
            donationsListView.setAdapter(new DonationListAdapter(this, donations));
            //GeneralUtils.setListViewHeightBasedOnChildren(listView);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageView_donate_decrease_amount:
                decreaseAmount();
                updateModifyAmountButtons();
                break;
            case R.id.imageView_donate_increase_amount:
                increaseAmount();
                updateModifyAmountButtons();
                break;
            case R.id.button_donate:
                Donation donation = new Donation(nameField.getText().toString(), messageField.getText().toString(),
                        DONATION_AMOUNTS[amountIndex], makePublic.isChecked());
                makeDonation(donation);
                break;
        }
    }

    private void increaseAmount() {
        if (amountIndex < DONATION_AMOUNTS.length-1) {
            amountIndex++;
            amountText.setText(String.valueOf(DONATION_AMOUNTS[amountIndex]));
        }
    }

    private void decreaseAmount() {
        if (amountIndex > 0) {
            amountIndex--;
            amountText.setText(String.valueOf(DONATION_AMOUNTS[amountIndex]));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_donate_info) {
            InfoDialogFragment dialogFragment = new InfoDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString("title", getResources().getString(R.string.about_donations_title));
            bundle.putString("info", getResources().getString(R.string.about_donations));
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getSupportFragmentManager(), "dialog");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
