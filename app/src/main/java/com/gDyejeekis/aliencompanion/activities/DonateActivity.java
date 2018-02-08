package com.gDyejeekis.aliencompanion.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.gDyejeekis.aliencompanion.MyApplication;
import com.gDyejeekis.aliencompanion.R;
import com.gDyejeekis.aliencompanion.asynctask.LoadDonationsTask;
import com.gDyejeekis.aliencompanion.fragments.dialog_fragments.InfoDialogFragment;
import com.gDyejeekis.aliencompanion.models.Donation;

/**
 * Created by George on 1/22/2018.
 */

public class DonateActivity extends ToolbarActivity implements View.OnClickListener {

    private EditText nameField;
    private EditText messageField;
    private TextView amountText;
    private CheckBox makePublic;
    private LinearLayout layoutPastDonations;
    private ListView donationsListView;
    private int amountIndex;
    private ImageView decrAmountBtn;
    private ImageView incrAmountBtn;

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
    }

    private void updateModifyAmountButtons() {
        styleModifyAmountButton(decrAmountBtn);
        styleModifyAmountButton(incrAmountBtn);
    }

    private void styleModifyAmountButton(ImageView imageView) {
        boolean increase = imageView.getId()==R.id.imageView_donate_increase_amount;
        boolean enabled = increase ? amountIndex < Donation.DONATION_AMOUNTS.length-1 : amountIndex > 0;
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
        amountText.setText(String.valueOf(Donation.DONATION_AMOUNTS[amountIndex]));
        makePublic.setChecked(true);
        updateModifyAmountButtons();
    }

    private void makeDonation(Donation donation) {
        // TODO: 1/25/2018
    }

    private void refreshDonationsList() {
        new LoadDonationsTask(this, layoutPastDonations, donationsListView).execute();
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
                        Float.valueOf(amountText.getText().toString()), makePublic.isChecked());
                makeDonation(donation);
                break;
        }
    }

    private void increaseAmount() {
        if (amountIndex < Donation.DONATION_AMOUNTS.length-1) {
            amountIndex++;
            amountText.setText(String.valueOf(Donation.DONATION_AMOUNTS[amountIndex]));
        }
    }

    private void decreaseAmount() {
        if (amountIndex > 0) {
            amountIndex--;
            amountText.setText(String.valueOf(Donation.DONATION_AMOUNTS[amountIndex]));
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
